package com.aliyun.appserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 应用启动入口
 *
 * <p>
 * 基于 Spring Boot 的 VOD App Server 启动类，默认会扫描
 * {@code com.aliyun.appserver} 包及其子包中的 Spring 组件。
 * </p>
 *
 * <p>本类不建议添加业务逻辑，仅作为引导入口使用。</p>
 *
 * @author: pxc
 * @date: 2025/9/23 15:03
 */
@SpringBootApplication
public class Application {

    /**
     * 启动 Spring Boot 应用
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
