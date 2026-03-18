<?php

require_once __DIR__ . '/vendor/autoload.php';

use Aliyun\Vod\playauth\VodPlayAuthToken;

/**
 * 阿里云视频点播播放授权 Token 示例程序
 *
 * 功能演示:
 *   - 生成播放授权 Token
 *   - 解析 Token 的 Header 和 Payload
 *   - 验证 Token 有效性
 *
 * @author junHui
 * @date 2025/9/12 14:28
 */

// 示例视频 ID
// 来源：阿里云视频点播控制台 - 媒资管理 - 视频列表
const SAMPLE_VIDEO_ID = '10b71107a99871f097b96732b78e0102';

function main(): void
{
    echo "========== 阿里云视频点播播放授权 Token 示例 ==========\n\n";

    // 1. 生成 Token
    echo "【步骤1】生成 Token\n";
    $token = VodPlayAuthToken::generateToken(SAMPLE_VIDEO_ID);
    if ($token === null) {
        echo "Token 生成失败！\n";
        return;
    }
    echo "Token: " . $token . "\n\n";

    // 2. 解析 Token
    echo "【步骤2】解析 Token\n";
    $header = VodPlayAuthToken::parseHeader($token);
    $payload = VodPlayAuthToken::parsePayload($token);
    echo "Header: " . $header . "\n";
    echo "Payload: " . $payload . "\n\n";

    // 3. 验证 Token
    echo "【步骤3】验证 Token\n";
    $isValid = VodPlayAuthToken::verifyToken($token);
    echo "验证结果: " . ($isValid ? "通过" : "失败") . "\n";
}

main();
