package com.aliyun.vod.playauth;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Base64 编解码工具类
 * <p>
 * 提供字符串的 Base64 编码和解码功能
 *
 * @author keria
 * @date 2025/10/22
 */
public final class Base64Utils {

    /**
     * 私有构造函数，防止实例化
     */
    private Base64Utils() {
    }

    /**
     * 对 Base64 编码的字符串进行解码
     *
     * @param encodedString 待解码的 Base64 编码字符串
     * @return 解码后的原始字符串，解码失败返回 {@code null}
     */
    public static String decodeBase64(String encodedString) {
        if (encodedString == null) {
            return null;
        }

        try {
            byte[] decodedBytes = Base64.getDecoder().decode(encodedString);
            return new String(decodedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 对字符串进行 Base64 编码
     *
     * @param originalString 待编码的原始字符串
     * @return 编码后的 Base64 字符串，编码失败返回 {@code null}
     */
    public static String encodeBase64(String originalString) {
        if (originalString == null) {
            return null;
        }

        try {
            return Base64.getEncoder().encodeToString(originalString.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 检查字符串是否为有效的 Base64 格式
     *
     * @param str 待检查的字符串
     * @return {@code true} 有效，{@code false} 无效
     */
    public static boolean isValidBase64(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        }

        try {
            Base64.getDecoder().decode(str);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
