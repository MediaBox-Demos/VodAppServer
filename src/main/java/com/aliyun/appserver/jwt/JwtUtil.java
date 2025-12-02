package com.aliyun.appserver.jwt;

import com.alibaba.fastjson.JSONObject;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;

/**
 * JWT 播放鉴权工具类
 *
 * <p>负责为指定视频生成播放凭证（playAuth），以及对 Token 进行基础校验。</p>
 *
 * <p><b>重要提示：</b>使用 JWT 本地签名生成的 {@code vid + playAuth} 进行播放时，
 * 客户端播放器 SDK 版本需要满足 {@code >= 7.10.0}，否则无法正常完成播放鉴权。</p>
 *
 * @author: pxc
 * @date: 2025/10/27 14:10
 */
public class JwtUtil {
    /**
     * 生成播放凭证 Token（使用默认 regionId）
     *
     * @param videoId 视频ID
     * @param playKey 播放密钥
     * @return JWT Token 字符串
     * @deprecated 建议使用 {@link #getPlayAuthToken(String, String, String)} 方法，传入配置的 regionId
     */
    @Deprecated
    public static String getPlayAuthToken(String videoId, String playKey) {
        return getPlayAuthToken(videoId, playKey, JwtConstants.DEFAULT_REGION_ID);
    }

    /**
     * 生成播放凭证 Token
     *
     * @param videoId  视频ID
     * @param playKey  播放密钥
     * @param regionId 地域标识（Region ID），建议从配置中读取
     * @return JWT Token 字符串
     */
    public static String getPlayAuthToken(String videoId, String playKey, String regionId) {
        if (videoId == null || videoId.trim().isEmpty()) {
            throw new IllegalArgumentException("videoId 不能为空");
        }
        if (playKey == null || playKey.trim().isEmpty()) {
            throw new IllegalArgumentException("playKey 不能为空");
        }
        if (regionId == null || regionId.trim().isEmpty()) {
            regionId = JwtConstants.DEFAULT_REGION_ID; // 使用默认值
        }

        long currentTimeStamp = System.currentTimeMillis();
        long expireTimeStamp = currentTimeStamp + JwtConstants.EXPIRED_TIME_MILLS;

        HashMap<String, Object> playContentInfo = new HashMap<>();
        playContentInfo.put("formats", "mp4");
        playContentInfo.put("authTimeout", 1800L); // 可也作为参数
        playContentInfo.put("streamType", "video");

        try {
            Algorithm algorithm = Algorithm.HMAC256(playKey); // 动态密钥

            return JWT.create()
                    .withClaim("appId", JwtConstants.DEFAULT_APP_ID)
                    .withClaim("videoId", videoId)           // 动态 videoId
                    .withClaim("currentTimeStamp", currentTimeStamp)
                    .withClaim("expireTimeStamp", expireTimeStamp)
                    .withClaim("regionId", regionId)
                    .withClaim("playContentInfo", playContentInfo)
                    .sign(algorithm);
        } catch (JWTCreationException e) {
            System.err.println("JWT Token创建失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static boolean checkPlayAuthToken(String token, String playKey) {
        try {
            // 1. Token格式校验：JWT标准格式为三部分用"."分隔
            String[] parts = StringUtils.split(token, ".");
            if (parts == null || parts.length != 3) {
                System.out.println("Token格式无效：应为Header.Payload.Signature格式");
                return false;
            }

            // 2. 解析Payload部分获取Claims信息
            String payLoad = Base64Utils.decodeBase64(parts[1]).replaceAll("\n", "").replaceAll("\r", "");
            System.out.println(payLoad);    // Token Payload
            JSONObject jsonObject = JSONObject.parseObject(payLoad);

            // 3. 签名验证：使用相同的密钥和算法验证Token完整性
            Algorithm algorithm = Algorithm.HMAC256(playKey);
            DecodedJWT decodedJWT = JWT.require(algorithm).build().verify(token);

            // 输出Header信息（调试用）
            String header = Base64Utils.decodeBase64(decodedJWT.getHeader()).replaceAll("\n", "").replaceAll("\r", "");
            System.out.println(header); // Token Header

            // 4. 时间戳校验
            // 4.1 签发时间校验：防止使用未来时间的Token（防重放攻击）
            Long currentTimeStamp = jsonObject.getLong("currentTimeStamp");
            if (currentTimeStamp == null || currentTimeStamp > System.currentTimeMillis()) {
                System.out.println("签发时间无效：Token签发时间不能晚于当前时间");
                return false;
            }

            // 4.2 过期时间校验：检查Token是否已过期
            Long expireTimeStamp = jsonObject.getLong("expireTimeStamp");
            if (expireTimeStamp != null && expireTimeStamp < System.currentTimeMillis()) {
                System.out.println("Token已过期：当前时间超过了Token的有效期");
                return false;
            }

            System.out.println("Token验证通过");
            return true;

        } catch (JWTVerificationException e) {
            // JWT验证异常：签名不匹配、算法不支持等
            System.err.println("JWT验证失败: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            // 其他异常：JSON解析失败、Base64解码失败等
            System.err.println("Token处理异常: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }
}
