<?php

namespace Aliyun\Vod\playauth;

use Firebase\JWT\JWT;
use Firebase\JWT\Key;
use InvalidArgumentException;

/**
 * 阿里云视频点播播放授权 Token 工具类
 *
 * 提供 JWT 格式的播放授权 Token 生成与验证功能:
 *   - 生成播放授权 Token
 *   - 验证 Token 有效性与签名
 *   - 解析 Token 的 Header 和 Payload
 *
 * 重要提示: 使用 vid + JWTPlayAuth（字段名: playAuth）进行播放时，
 * 客户端播放器 SDK 版本需要满足 >= 7.10.0，否则无法正常完成播放鉴权。
 *
 * @author junHui
 * @date 2025/9/12 14:28
 */
final class VodPlayAuthToken
{
    /**
     * 私有构造函数，防止实例化
     */
    private function __construct()
    {
    }

    /**
     * 生成播放授权 Token（使用默认配置）
     *
     * 使用 VodJwtConstants 中定义的默认参数生成 Token
     *
     * @param string $videoId 视频 ID，不能为空
     * @return string|null JWT Token 字符串，生成失败返回 null
     */
    public static function generateToken(string $videoId): ?string
    {
        return self::generateTokenWithConfig(
            $videoId,
            VodJwtConstants::DEFAULT_APP_ID,
            VodJwtConstants::DEFAULT_PLAY_KEY,
            VodJwtConstants::DEFAULT_REGION_ID,
            VodJwtConstants::EXPIRED_TIME_MILLS
        );
    }

    /**
     * 生成播放授权 Token（自定义配置）
     *
     * Token Payload 包含以下字段:
     *   - appId: 应用标识
     *   - videoId: 视频 ID
     *   - currentTimeStamp: 签发时间戳（毫秒）
     *   - expireTimeStamp: 过期时间戳（毫秒）
     *   - regionId: 地域标识
     *   - playContentInfo: 播放配置信息
     *
     * @param string $videoId 视频 ID，不能为空
     * @param string $appId 应用 ID，不能为空
     * @param string $playKey 播放密钥，不能为空
     * @param string $regionId 地域标识，不能为空
     * @param int $expiredTimeMills 过期时间（毫秒），必须大于 0
     * @return string|null JWT Token 字符串，生成失败返回 null
     */
    public static function generateTokenWithConfig(
        string $videoId,
        string $appId,
        string $playKey,
        string $regionId,
        int $expiredTimeMills
    ): ?string {
        $currentTimeStamp = time() * 1000;
        $expireTimeStamp = $currentTimeStamp + $expiredTimeMills;

        // 播放内容配置信息
        // 注意: PlayContentInfo 接口入参与原接口 GetPlayInfo 一致
        $playContentInfo = [
            'formats' => 'mp4',
            'authTimeout' => 1800,
            'streamType' => 'video'
        ];

        $payload = [
            'appId' => $appId,
            'videoId' => $videoId,
            'currentTimeStamp' => $currentTimeStamp,
            'expireTimeStamp' => $expireTimeStamp,
            'regionId' => $regionId,
            'playContentInfo' => $playContentInfo
        ];

        try {
            return JWT::encode($payload, $playKey, 'HS256');
        } catch (InvalidArgumentException $e) {
            error_log("JWT Token 创建失败: " . $e->getMessage());
            return null;
        }
    }

    /**
     * 验证播放授权 Token（使用默认密钥）
     *
     * 验证流程:
     *   - Token 格式校验（三段式结构）
     *   - 签名验证
     *   - 时间戳校验（防重放攻击、过期检查）
     *
     * @param string $token 待验证的 JWT Token
     * @return bool true 验证通过，false 验证失败
     */
    public static function verifyToken(string $token): bool
    {
        return self::verifyTokenWithKey($token, VodJwtConstants::DEFAULT_PLAY_KEY);
    }

    /**
     * 验证播放授权 Token（自定义密钥）
     *
     * @param string $token 待验证的 JWT Token
     * @param string $playKey 播放密钥
     * @return bool true 验证通过，false 验证失败
     */
    public static function verifyTokenWithKey(string $token, string $playKey): bool
    {
        // 1. Token 格式校验
        $parts = explode('.', $token);
        if (count($parts) !== 3) {
            echo "Token 格式无效：应为 Header.Payload.Signature 格式\n";
            return false;
        }

        // 2. 解析 Payload
        $payloadStr = Base64Utils::decodeBase64($parts[1]);
        if ($payloadStr === null) {
            echo "Payload Base64 解码失败\n";
            return false;
        }

        $payloadArray = json_decode($payloadStr, true);
        if (!is_array($payloadArray)) {
            echo "Payload JSON 解析失败\n";
            return false;
        }

        // 3. 签名验证
        try {
            JWT::decode($token, new Key($playKey, 'HS256'));
        } catch (\Exception $e) {
            echo "JWT 验证失败: " . $e->getMessage() . "\n";
            return false;
        }

        // 4. 时间戳校验
        $now = time() * 1000;

        $currentTimeStamp = $payloadArray['currentTimeStamp'] ?? null;
        if ($currentTimeStamp === null || $currentTimeStamp > $now) {
            echo "签发时间无效：Token 签发时间不能晚于当前时间\n";
            return false;
        }

        $expireTimeStamp = $payloadArray['expireTimeStamp'] ?? null;
        if ($expireTimeStamp !== null && $expireTimeStamp < $now) {
            echo "Token 已过期：当前时间超过了 Token 的有效期\n";
            return false;
        }

        return true;
    }

    /**
     * 解析 Token 的 Payload 部分
     *
     * @param string $token JWT Token
     * @return string|null Payload JSON 字符串，解析失败返回 null
     */
    public static function parsePayload(string $token): ?string
    {
        $parts = explode('.', $token);
        if (count($parts) !== 3) {
            return null;
        }
        return Base64Utils::decodeBase64($parts[1]);
    }

    /**
     * 解析 Token 的 Header 部分
     *
     * @param string $token JWT Token
     * @return string|null Header JSON 字符串，解析失败返回 null
     */
    public static function parseHeader(string $token): ?string
    {
        $parts = explode('.', $token);
        if (count($parts) !== 3) {
            return null;
        }
        return Base64Utils::decodeBase64($parts[0]);
    }
}
