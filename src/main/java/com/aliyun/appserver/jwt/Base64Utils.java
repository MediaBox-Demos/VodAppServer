package com.aliyun.appserver.jwt;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Base64 工具类
 *
 * <p>封装了常用的 Base64 编解码能力，默认使用 UTF-8 字符集。</p>
 *
 * @author keria
 * @date 2025/10/22
 */
public class Base64Utils {

    /**
     * 私有构造函数，防止实例化工具类
     */
    private Base64Utils() {
    }

    /**
     * 对 Base64 编码的字符串进行解码
     *
     * @param encodedString 待解码的 Base64 编码字符串，不能为 null
     * @return 解码后的原始字符串（UTF-8），如果输入为 null 或发生异常则返回 null
     */
    public static String decodeBase64(String encodedString) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(encodedString);
            return new String(decodedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 对字符串进行 Base64 编码
     *
     * @param originalString 待编码的原始字符串，不能为 null
     * @return 编码后的 Base64 字符串，如果输入为 null 或发生异常则返回 null
     */
    public static String encodeBase64(String originalString) {
        try {
            return Base64.getEncoder().encodeToString(originalString.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 检查字符串是否为有效的Base64格式
     *
     * @param str 待检查的字符串
     * @return 如果是有效的Base64格式返回true，否则返回false
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
