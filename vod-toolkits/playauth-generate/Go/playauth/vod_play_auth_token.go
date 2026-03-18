package playauth

import (
	"encoding/base64"
	"encoding/json"
	"fmt"
	"strings"
	"time"

	"github.com/golang-jwt/jwt/v5"
)

// PlayContentInfo 播放内容配置信息
type PlayContentInfo struct {
	Formats     string `json:"formats"`
	AuthTimeout int64  `json:"authTimeout"` // 单位：秒
	StreamType  string `json:"streamType"`
}

// CustomClaims 自定义 JWT Claims
// 嵌入 jwt.RegisteredClaims 以满足 jwt.Claims 接口
type CustomClaims struct {
	AppID            string          `json:"appId"`
	VideoID          string          `json:"videoId"`
	CurrentTimeStamp int64           `json:"currentTimeStamp"`
	ExpireTimeStamp  int64           `json:"expireTimeStamp"`
	RegionID         string          `json:"regionId"`
	PlayContentInfo  PlayContentInfo `json:"playContentInfo"`
	jwt.RegisteredClaims
}

// 阿里云视频点播播放授权 Token 工具类
// 提供 JWT 格式的播放授权 Token 生成与验证功能:
//   - 生成播放授权 Token
//   - 验证 Token 有效性与签名
//   - 解析 Token 的 Header 和 Payload
//
// 重要提示: 使用 vid + JWTPlayAuth（字段名: playAuth）进行播放时，
// 客户端播放器 SDK 版本需要满足 >= 7.10.0，否则无法正常完成播放鉴权。
//
// @author wyq
// @date 2025/9/12 14:28

// GenerateToken 生成播放授权 Token（使用默认配置）
// 使用 vod_jwt_constants 中定义的默认参数生成 Token
func GenerateToken(videoID string) (string, error) {
	return GenerateTokenWithConfig(
		videoID,
		DefaultAppID,
		DefaultPlayKey,
		DefaultRegionID,
		ExpiredTimeMills,
	)
}

// GenerateTokenWithConfig 生成播放授权 Token（自定义配置）
// Token Payload 包含以下字段:
//   - appId: 应用标识
//   - videoId: 视频 ID
//   - currentTimeStamp: 签发时间戳（毫秒）
//   - expireTimeStamp: 过期时间戳（毫秒）
//   - regionId: 地域标识
//   - playContentInfo: 播放配置信息
func GenerateTokenWithConfig(videoID, appID, playKey, regionID string, expiredTimeMills int64) (string, error) {
	currentTime := time.Now().UnixMilli()
	expireTime := currentTime + expiredTimeMills

	// 播放内容配置信息
	// 注意: PlayContentInfo 接口入参与原接口 GetPlayInfo 一致
	playContentInfo := PlayContentInfo{
		Formats:     "mp4",
		AuthTimeout: 1800,
		StreamType:  "video",
	}

	claims := CustomClaims{
		AppID:            appID,
		VideoID:          videoID,
		CurrentTimeStamp: currentTime,
		ExpireTimeStamp:  expireTime,
		RegionID:         regionID,
		PlayContentInfo:  playContentInfo,
	}

	token := jwt.NewWithClaims(jwt.SigningMethodHS256, claims)
	tokenString, err := token.SignedString([]byte(playKey))
	if err != nil {
		return "", err
	}
	return tokenString, nil
}

// VerifyToken 验证播放授权 Token（使用默认密钥）
// 验证流程:
//   - Token 格式校验（三段式结构）
//   - 签名验证
//   - 时间戳校验（防重放攻击、过期检查）
func VerifyToken(tokenStr string) bool {
	return VerifyTokenWithKey(tokenStr, DefaultPlayKey)
}

// VerifyTokenWithKey 验证播放授权 Token（自定义密钥）
func VerifyTokenWithKey(tokenStr, playKey string) bool {
	// 1. Token 格式校验
	parts := strings.Split(tokenStr, ".")
	if len(parts) != 3 {
		fmt.Println("Token 格式无效：应为 Header.Payload.Signature 格式")
		return false
	}

	// 2. 解析 Payload
	payloadB64 := parts[1]
	padding := len(payloadB64) % 4
	if padding > 0 {
		payloadB64 += strings.Repeat("=", 4-padding)
	}
	payloadBytes, err := base64.StdEncoding.DecodeString(payloadB64)
	if err != nil {
		fmt.Printf("Payload Base64 解码失败: %v\n", err)
		return false
	}

	var payloadMap map[string]interface{}
	if err := json.Unmarshal(payloadBytes, &payloadMap); err != nil {
		fmt.Printf("Payload JSON 解析失败: %v\n", err)
		return false
	}

	// 3. 签名验证
	parser := new(jwt.Parser)
	_, err = parser.ParseWithClaims(tokenStr, &CustomClaims{}, func(token *jwt.Token) (interface{}, error) {
		if _, ok := token.Method.(*jwt.SigningMethodHMAC); !ok {
			return nil, fmt.Errorf("unexpected signing method: %v", token.Header["alg"])
		}
		return []byte(playKey), nil
	})
	if err != nil {
		fmt.Printf("JWT 验证失败: %v\n", err)
		return false
	}

	// 4. 时间戳校验
	now := time.Now().UnixMilli()

	currentTimeStamp, ok := payloadMap["currentTimeStamp"].(float64)
	if !ok || int64(currentTimeStamp) > now {
		fmt.Println("签发时间无效：Token 签发时间不能晚于当前时间")
		return false
	}

	expireTimeStamp, ok := payloadMap["expireTimeStamp"].(float64)
	if ok && int64(expireTimeStamp) < now {
		fmt.Println("Token 已过期：当前时间超过了 Token 的有效期")
		return false
	}

	return true
}

// ParsePayload 解析 Token 的 Payload 部分
func ParsePayload(tokenStr string) string {
	parts := strings.Split(tokenStr, ".")
	if len(parts) != 3 {
		return ""
	}
	payloadB64 := parts[1]
	padding := len(payloadB64) % 4
	if padding > 0 {
		payloadB64 += strings.Repeat("=", 4-padding)
	}
	payloadBytes, err := base64.StdEncoding.DecodeString(payloadB64)
	if err != nil {
		return ""
	}
	return string(payloadBytes)
}

// ParseHeader 解析 Token 的 Header 部分
func ParseHeader(tokenStr string) string {
	parts := strings.Split(tokenStr, ".")
	if len(parts) != 3 {
		return ""
	}
	headerB64 := parts[0]
	padding := len(headerB64) % 4
	if padding > 0 {
		headerB64 += strings.Repeat("=", 4-padding)
	}
	headerBytes, err := base64.StdEncoding.DecodeString(headerB64)
	if err != nil {
		return ""
	}
	return string(headerBytes)
}
