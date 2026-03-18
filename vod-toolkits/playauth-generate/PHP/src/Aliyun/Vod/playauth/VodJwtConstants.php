<?php

namespace Aliyun\Vod\playauth;

/**
 * 阿里云视频点播 JWT 常量配置类
 *
 * 定义播放授权 Token 的默认配置参数
 *
 * 获取播放密钥:
 *   - GetAppPlayKey: https://help.aliyun.com/zh/vod/developer-reference/api-vod-2017-03-21-getappplaykey
 *   - SetAppPlayKey: https://help.aliyun.com/zh/vod/developer-reference/api-vod-2017-03-21-setappplaykey
 *
 * @author junHui
 * @date 2025/10/22
 */
final class VodJwtConstants
{
    /**
     * 私有构造函数，防止实例化
     */
    private function __construct()
    {
    }

    /**
     * 默认应用 ID
     *
     * 来源：阿里云视频点播控制台 - 应用管理
     * 地域：华东2（上海）
     */
    public const DEFAULT_APP_ID = 'app-1000000';

    /**
     * 默认播放密钥
     *
     * 用于 JWT 签名验证的身份认证密钥
     * 获取方式：调用阿里云视频点播 OpenAPI 中的 GetAppPlayKey 接口进行获取
     * 重置方式：调用阿里云视频点播 OpenAPI 中的 SetAppPlayKey 接口设置新的播放密钥
     *
     * 注意：由于 firebase/php-jwt 版本强制要求 PlayKey 长度 >= 32 位，
     * 而默认生成的 playKey 不满足 php-jwt 的需求；
     * 因此需要您重新调用 SetAppPlayKey 接口，重新设置一个 32 位以上的 playKey
     *
     * 【重要提示】
     * 请将下方注释掉的空值赋值行放开，并将空字符串替换为您从阿里云获取的播放密钥
     */
    // public const DEFAULT_PLAY_KEY = '';

    /**
     * 默认地域标识
     *
     * 来源：阿里云视频点播控制台
     * 当前配置：华东2（上海）
     */
    public const DEFAULT_REGION_ID = 'cn-shanghai';

    /**
     * JWT 令牌过期时间
     *
     * 单位：毫秒
     * 默认：1小时（60分钟 * 60秒 * 1000毫秒）
     * 当前设置：24小时
     */
    public const EXPIRED_TIME_MILLS = 24 * 60 * 60 * 1000;
}
