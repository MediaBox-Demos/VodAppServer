<?php

namespace Aliyun\Vod\playauth;

/**
 * Base64 编解码工具类
 *
 * 提供字符串的 Base64 编码和解码功能
 *
 * @author junHui
 * @date 2025/10/22
 */
final class Base64Utils
{
    /**
     * 私有构造函数，防止实例化
     */
    private function __construct()
    {
    }

    /**
     * 对 Base64 编码的字符串进行解码
     *
     * @param string|null $encodedString 待解码的 Base64 编码字符串
     * @return string|null 解码后的原始字符串，解码失败返回 null
     */
    public static function decodeBase64(?string $encodedString): ?string
    {
        if ($encodedString === null) {
            return null;
        }

        // 移除可能的换行符
        $encodedString = str_replace(["\r", "\n"], '', $encodedString);

        // 补齐 padding（PHP 的 base64_decode 对不完整 padding 容忍度低）
        $missingPadding = strlen($encodedString) % 4;
        if ($missingPadding) {
            $encodedString .= str_repeat('=', 4 - $missingPadding);
        }

        $decoded = base64_decode($encodedString, true);
        if ($decoded === false) {
            return null;
        }

        return $decoded;
    }

    /**
     * 对字符串进行 Base64 编码
     *
     * @param string|null $originalString 待编码的原始字符串
     * @return string|null 编码后的 Base64 字符串，编码失败返回 null
     */
    public static function encodeBase64(?string $originalString): ?string
    {
        if ($originalString === null) {
            return null;
        }

        return base64_encode($originalString);
    }

    /**
     * 检查字符串是否为有效的 Base64 格式
     *
     * @param string|null $str 待检查的字符串
     * @return bool true 有效，false 无效
     */
    public static function isValidBase64(?string $str): bool
    {
        if ($str === null || trim($str) === '') {
            return false;
        }

        // 移除换行
        $str = str_replace(["\r", "\n"], '', $str);

        // 补齐 padding
        $missingPadding = strlen($str) % 4;
        if ($missingPadding) {
            $str .= str_repeat('=', 4 - $missingPadding);
        }

        // 必须只包含 Base64 字符
        if (!preg_match('/^[a-zA-Z0-9+\/]*={0,2}$/', $str)) {
            return false;
        }

        return base64_decode($str, true) !== false;
    }
}
