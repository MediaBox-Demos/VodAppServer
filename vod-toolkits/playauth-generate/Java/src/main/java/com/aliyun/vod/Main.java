package com.aliyun.vod;

import com.aliyun.vod.playauth.VodPlayAuthToken;

/**
 * 阿里云视频点播播放授权 Token 示例程序
 * <p>
 * 功能演示：
 * <ul>
 *   <li>生成播放授权 Token</li>
 *   <li>解析 Token 的 Header 和 Payload</li>
 *   <li>验证 Token 有效性</li>
 * </ul>
 *
 * @author fulin
 * @date 2025/9/12 14:28
 */
public class Main {

    /**
     * 示例视频 ID
     * <p>
     * 来源：阿里云视频点播控制台 - 媒资管理 - 视频列表
     */
    private static final String SAMPLE_VIDEO_ID = "10b71107a99871f097b96732b78e0102";

    /**
     * 程序入口
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        System.out.println("========== 阿里云视频点播播放授权 Token 示例 ==========\n");

        // 1. 生成 Token
        System.out.println("【步骤1】生成 Token");
        String token = VodPlayAuthToken.generateToken(SAMPLE_VIDEO_ID);
        if (token == null) {
            System.err.println("Token 生成失败！");
            return;
        }
        System.out.println("Token: " + token);
        System.out.println();

        // 2. 解析 Token
        System.out.println("【步骤2】解析 Token");
        String header = VodPlayAuthToken.parseHeader(token);
        String payload = VodPlayAuthToken.parsePayload(token);
        System.out.println("Header: " + header);
        System.out.println("Payload: " + payload);
        System.out.println();

        // 3. 验证 Token
        System.out.println("【步骤3】验证 Token");
        boolean isValid = VodPlayAuthToken.verifyToken(token);
        System.out.println("验证结果: " + (isValid ? "通过" : "失败"));
    }
}
