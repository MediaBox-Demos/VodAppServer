package com.aliyun.appserver.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 阿里云 VOD 基础配置
 *
 * <p>
 * 该配置类用于承载 VOD 访问所需的基础参数，支持通过
 * {@code application.yml} / 环境变量 等方式进行外部化配置。
 *
 * <pre>
 * 示例配置（application.yml）：
 * aliyun:
 *   vod:
 *     ak: ${ALIYUN_VOD_AK:}                    # AccessKey ID
 *     sk: ${ALIYUN_VOD_SK:}                    # AccessKey Secret
 *     region: ${ALIYUN_VOD_REGION:cn-shanghai} # 地域标识（Region ID）
 * </pre>
 *
 * <p><b>配置说明：</b></p>
 * <ul>
 *     <li><b>ak / sk</b>：阿里云账号的 AccessKey，用于身份认证和签名</li>
 *     <li><b>region</b>：VOD 服务地域标识，必须与 VOD 控制台中开通服务的区域一致</li>
 * </ul>
 *
 * <p><b>支持的地域：</b></p>
 * <ul>
 *     <li>上海：cn-shanghai（默认）</li>
 *     <li>北京：cn-beijing</li>
 *     <li>深圳：cn-shenzhen</li>
 *     <li>新加坡：ap-southeast-1</li>
 * </ul>
 *
 * <p><b>安全建议：</b></p>
 * <ul>
 *     <li>生产环境建议通过环境变量或密钥管理服务注入敏感信息</li>
 *     <li>避免将 AccessKey / SecretKey 明文写入代码仓库</li>
 *     <li>定期轮换密钥，提高安全性</li>
 * </ul>
 */
@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "aliyun.vod")
public class VodConfig {
    /**
     * 阿里云账号的 AccessKeyId
     *
     * <p>用于标识调用方身份，必须与 VOD 控制台中开通服务的账号一致。</p>
     */
    private String ak;

    /**
     * 阿里云账号的 AccessKeySecret
     *
     * <p>配合 AccessKeyId 一起使用，用于对请求进行签名认证。</p>
     *
     * <p><b>安全建议：</b>不要将该字段的真实值写入仓库，
     * 建议通过环境变量或密钥管理服务注入。</p>
     */
    private String sk;

    /**
     * 地域标识（Region ID）
     *
     * <p>用于指定阿里云 VOD 服务的地域，必须与 VOD 控制台中开通服务的区域一致。</p>
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
     *
     * <p>默认值：cn-shanghai</p>
     */
    private String region = "cn-shanghai";
}
