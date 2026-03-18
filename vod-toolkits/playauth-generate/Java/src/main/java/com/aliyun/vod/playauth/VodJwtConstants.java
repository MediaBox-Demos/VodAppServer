package com.aliyun.vod.playauth;

/**
 * 阿里云视频点播 JWT 常量配置类
 * <p>
 * 定义播放授权 Token 的默认配置参数
 *
 * @author keria
 * @date 2025/10/22
 * @see <a href="https://help.aliyun.com/zh/vod/developer-reference/api-vod-2017-03-21-getappplaykey">GetAppPlayKey 接口文档</a>
 * @see <a href="https://help.aliyun.com/zh/vod/developer-reference/api-vod-2017-03-21-setappplaykey">SetAppPlayKey 接口文档</a>
 */
public final class VodJwtConstants {

    /**
     * 私有构造函数，防止实例化
     */
    private VodJwtConstants() {
    }

    /**
     * 默认应用ID
     * <p>
     * 来源：阿里云视频点播控制台 - 应用管理
     * <p>
     * 地域：华东2（上海）
     * <p>
     * 默认：app-1000000
     */
    public static final String DEFAULT_APP_ID = "app-1000000";

    /**
     * 默认播放密钥
     * <p>
     * 用于 JWT 签名验证的身份认证密钥
     * <p>
     * 获取方式：调用阿里云视频点播 OpenAPI 中的 GetAppPlayKey 接口进行获取
     * <p>
     * 重置方式：调用阿里云视频点播 OpenAPI 中的 SetAppPlayKey 接口设置新的播放密钥
     *
     * 【重要提示】
     * 请将下方注释掉的空值赋值行放开，并将空字符串替换为您从阿里云获取的播放密钥
     *
     * @see <a href="https://help.aliyun.com/zh/vod/developer-reference/api-vod-2017-03-21-getappplaykey">GetAppPlayKey - 获取播放密钥</a>
     * @see <a href="https://help.aliyun.com/zh/vod/developer-reference/api-vod-2017-03-21-setappplaykey">SetAppPlayKey - 设置播放密钥</a>
     */
    // public static final String DEFAULT_PLAY_KEY = "";

    /**
     * 默认地域标识
     * <p>
     * 来源：阿里云视频点播控制台
     * <p>
     * 当前配置：华东2（上海）
     */
    public static final String DEFAULT_REGION_ID = "cn-shanghai";

    /**
     * JWT 令牌过期时间
     * <p>
     * 单位：毫秒
     * <p>
     * 默认：1小时（60分钟 * 60秒 * 1000毫秒）
     * <p>
     * 当前设置：24小时
     */
    public static final long EXPIRED_TIME_MILLS = 24 * 60 * 60 * 1000L;
}
