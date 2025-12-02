package com.aliyun.appserver.result;

import lombok.Getter;
import lombok.Setter;

/**
 * 统一业务返回结果模型
 *
 * <p>
 * 用于对接口返回进行统一封装，便于前后端 / 调用方约定统一的返回格式。
 * 一般推荐结构：
 * <ul>
 *     <li>{@link #code} 业务状态码，参见 {@link ResultCode}</li>
 *     <li>{@link #httpCode} HTTP 状态码字符串（可选）</li>
 *     <li>{@link #success} 是否成功</li>
 *     <li>{@link #message} 提示信息或错误信息</li>
 *     <li>{@link #data} 具体业务数据</li>
 *     <li>{@link #requestId} 请求链路追踪 ID（可选）</li>
 * </ul>
 * </p>
 *
 * @param <T> 业务数据的类型
 */
@Getter
public class CallResult<T> {

    /**
     * 业务状态码，参见 {@link ResultCode}
     */
    private Integer code;

    /**
     * HTTP 状态码（字符串形式，例如 "200"）
     */
    @Setter
    private String httpCode;

    /**
     * 是否成功
     */
    @Setter
    private Boolean success;

    /**
     * 提示信息 / 错误信息
     */
    @Setter
    private String message;

    /**
     * 业务数据载体
     */
    @Setter
    private T data;

    /**
     * 请求链路 ID，可用于日志与追踪
     */
    @Setter
    private String requestId;

    /**
     * 使用 {@link ResultCode} 设置业务状态码的便捷方法
     */
    public CallResult<T> setCode(ResultCode retCode) {
        this.code = retCode.code;
        return this;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    /**
     * 链式设置 message
     */
    public CallResult<T> setMsg(String message) {
        this.message = message;
        return this;
    }
}
