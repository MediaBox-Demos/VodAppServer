package com.aliyun.vod.playauth;

import com.alibaba.fastjson.JSONObject;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 阿里云视频点播播放授权 Token 工具类
 * <p>
 * 提供 JWT 格式的播放授权 Token 生成与验证功能
 * <ul>
 *   <li>生成播放授权 Token</li>
 *   <li>验证 Token 有效性与签名</li>
 *   <li>解析 Token 的 Header 和 Payload</li>
 * </ul>
 * <p>
 * <b>重要提示：</b>使用 {@code vid + JWTPlayAuth}（字段名：{@code playAuth}）进行播放时，
 * 客户端播放器 SDK 版本需要满足 {@code >= 7.10.0}，否则无法正常完成播放鉴权。
 * </p>
 *
 * @author fulin
 * @date 2025/9/12 14:28
 */
public final class VodPlayAuthToken {

    /**
     * 私有构造函数，防止实例化
     */
    private VodPlayAuthToken() {
    }

    /**
     * 生成播放授权 Token（使用默认配置）
     * <p>
     * 使用 {@link VodJwtConstants} 中定义的默认参数生成 Token
     *
     * @param videoId 视频 ID，不能为空
     * @return JWT Token 字符串，生成失败返回 {@code null}
     */
    public static String generateToken(String videoId) {
        return generateToken(
                videoId,
                VodJwtConstants.DEFAULT_APP_ID,
                VodJwtConstants.DEFAULT_PLAY_KEY,
                VodJwtConstants.DEFAULT_REGION_ID,
                VodJwtConstants.EXPIRED_TIME_MILLS
        );
    }

    /**
     * 生成播放授权 Token（自定义配置）
     * <p>
     * Token Payload 包含以下字段：
     * <ul>
     *   <li>appId - 应用标识</li>
     *   <li>videoId - 视频 ID</li>
     *   <li>currentTimeStamp - 签发时间戳（毫秒）</li>
     *   <li>expireTimeStamp - 过期时间戳（毫秒）</li>
     *   <li>regionId - 地域标识</li>
     *   <li>playContentInfo - 播放配置信息</li>
     * </ul>
     *
     * @param videoId          视频 ID，不能为空
     * @param appId            应用 ID，不能为空
     * @param playKey          播放密钥，不能为空
     * @param regionId         地域标识，不能为空
     * @param expiredTimeMills 过期时间（毫秒），必须大于 0
     * @return JWT Token 字符串，生成失败返回 {@code null}
     */
    public static String generateToken(String videoId, String appId, String playKey,
                                       String regionId, long expiredTimeMills) {
        long currentTimeStamp = System.currentTimeMillis();
        long expireTimeStamp = currentTimeStamp + expiredTimeMills;

        // 播放内容配置信息
        // 注意：PlayContentInfo 接口入参与原接口 GetPlayInfo 一致，@清邻
        Map<String, Object> playContentInfo = new HashMap<>();
        // 视频格式：MP4
        playContentInfo.put("formats", "mp4");
        // 播放地址的过期时间，默认1小时，可自定义为 30分钟（1800秒）
        playContentInfo.put("authTimeout", 1800L);
        // 流类型：视频流
        playContentInfo.put("streamType", "video");

        try {
            Algorithm algorithm = Algorithm.HMAC256(playKey);
            return JWT.create()
                    .withClaim("appId", appId)
                    .withClaim("videoId", videoId)
                    .withClaim("currentTimeStamp", currentTimeStamp)
                    .withClaim("expireTimeStamp", expireTimeStamp)
                    .withClaim("regionId", regionId)
                    .withClaim("playContentInfo", playContentInfo)
                    .sign(algorithm);
        } catch (JWTCreationException e) {
            System.err.println("JWT Token 创建失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 验证播放授权 Token（使用默认密钥）
     * <p>
     * 验证流程：
     * <ol>
     *   <li>Token 格式校验（三段式结构）</li>
     *   <li>签名验证</li>
     *   <li>时间戳校验（防重放攻击、过期检查）</li>
     * </ol>
     *
     * @param token 待验证的 JWT Token
     * @return {@code true} 验证通过，{@code false} 验证失败
     */
    public static boolean verifyToken(String token) {
        return verifyToken(token, VodJwtConstants.DEFAULT_PLAY_KEY);
    }

    /**
     * 验证播放授权 Token（自定义密钥）
     *
     * @param token   待验证的 JWT Token
     * @param playKey 播放密钥
     * @return {@code true} 验证通过，{@code false} 验证失败
     */
    public static boolean verifyToken(String token, String playKey) {
        try {
            // 1. Token 格式校验
            String[] parts = StringUtils.split(token, ".");
            if (parts == null || parts.length != 3) {
                System.out.println("Token 格式无效：应为 Header.Payload.Signature 格式");
                return false;
            }

            // 2. 解析 Payload
            String payload = Base64Utils.decodeBase64(parts[1])
                    .replace("\n", "")
                    .replace("\r", "");
            JSONObject jsonObject = JSONObject.parseObject(payload);

            // 3. 签名验证
            Algorithm algorithm = Algorithm.HMAC256(playKey);
            JWT.require(algorithm).build().verify(token);

            // 4. 时间戳校验
            Long currentTimeStamp = jsonObject.getLong("currentTimeStamp");
            if (currentTimeStamp == null || currentTimeStamp > System.currentTimeMillis()) {
                System.out.println("签发时间无效：Token 签发时间不能晚于当前时间");
                return false;
            }

            Long expireTimeStamp = jsonObject.getLong("expireTimeStamp");
            if (expireTimeStamp != null && expireTimeStamp < System.currentTimeMillis()) {
                System.out.println("Token 已过期：当前时间超过了 Token 的有效期");
                return false;
            }

            return true;

        } catch (JWTVerificationException e) {
            System.err.println("JWT 验证失败: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Token 处理异常: " + e.getMessage());
        }

        return false;
    }

    /**
     * 解析 Token 的 Payload 部分
     *
     * @param token JWT Token
     * @return Payload JSON 字符串，解析失败返回 {@code null}
     */
    public static String parsePayload(String token) {
        try {
            String[] parts = StringUtils.split(token, ".");
            if (parts == null || parts.length != 3) {
                return null;
            }
            return Base64Utils.decodeBase64(parts[1])
                    .replace("\n", "")
                    .replace("\r", "");
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 解析 Token 的 Header 部分
     *
     * @param token JWT Token
     * @return Header JSON 字符串，解析失败返回 {@code null}
     */
    public static String parseHeader(String token) {
        try {
            String[] parts = StringUtils.split(token, ".");
            if (parts == null || parts.length != 3) {
                return null;
            }
            return Base64Utils.decodeBase64(parts[0])
                    .replace("\n", "")
                    .replace("\r", "");
        } catch (Exception e) {
            return null;
        }
    }
}
