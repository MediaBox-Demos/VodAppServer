package com.aliyun.appserver.jwt;

/**
 * JWT 相关常量
 *
 * <p>用于集中管理与播放鉴权相关的固定参数。</p>
 *
 * <p>说明：本类仅承载示例所需的常量值，生产环境中建议从配置中心或环境变量中加载，
 * 避免在代码中硬编码敏感信息。</p>
 *
 * <p>前提条件：</p>
 * <p>1. 已开通阿里云视频点播（VOD）服务。</p>
 * <p>2. 已在阿里云视频点播控制台创建应用，并完成基础配置。</p>
 * <p>3. 已获取有效的阿里云访问密钥（AccessKey ID / AccessKey Secret）。</p>
 *
 * <p>默认播放密钥（PlayKey）：</p>
 * <p>用途：视频播放时的身份验证密钥。</p>
 * <p>获取方式：通过阿里云视频点播 API 获取应用播放密钥。</p>
 * <p>接口文档：<a href="https://help.aliyun.com/zh/vod/developer-reference/api-vod-2017-03-21-getappplaykey">GetAppPlayKey - 获取应用播放密钥</a></p>
 *
 * @author keria
 * @date 2025/10/22
 */
public final class JwtConstants {

    private JwtConstants() {
        // 工具类不允许实例化
    }

    /**
     * 默认应用ID
     * <p>
     * 来源：阿里云视频点播控制台 - 应用管理
     * <p>
     * 路径：华东2（上海）区域下的应用标识
     * <p>
     * 默认：app-1000000
     */
    public static final String DEFAULT_APP_ID = "app-1000000";

    /**
     * 默认地域标识
     * 来源：阿里云视频点播控制台 - 华东2（上海）区域
     *
     * <p><b>注意：</b>建议从 {@code application.yml} 配置中的 {@code aliyun.vod.region} 读取，
     * 而不是直接使用此常量。此常量仅作为默认值使用。</p>
     *
     * <p>当前支持的地域：</p>
     * <ul>
     *     <li>上海：cn-shanghai</li>
     *     <li>北京：cn-beijing</li>
     *     <li>深圳：cn-shenzhen</li>
     *     <li>新加坡：ap-southeast-1</li>
     * </ul>
     *
     * <p>后续考虑支持：美西（us-west-1）</p>
     */
    public static final String DEFAULT_REGION_ID = "cn-shanghai";

    /**
     * JWT令牌过期时间（毫秒）
     * <p>
     * 默认：1小时（60分钟 * 60秒 * 1000毫秒）
     */
    public static final long EXPIRED_TIME_MILLS = 60 * 60 * 1000L;
}
