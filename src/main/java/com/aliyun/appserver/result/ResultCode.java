package com.aliyun.appserver.result;

/**
 * 业务通用结果码定义
 *
 * <p>用于标识接口调用的业务状态，与 HTTP 状态码解耦。</p>
 */
public enum ResultCode {

    /**
     * 成功
     */
    SUCCESS(0, "成功"),

    /**
     * 参数无效（格式 / 取值不符合业务要求）
     */
    PARAM_IS_INVALID(10001, "参数无效"),

    /**
     * 必填参数为空
     */
    PARAM_IS_BLANK(10002, "参数为空"),

    /**
     * 参数类型绑定错误（例如字符串无法转换为数字 / 枚举）
     */
    PARAM_TYPE_BIND_ERROR(10003, "参数类型错误"),

    /**
     * 参数缺失（缺少必须的字段）
     */
    PARAM_NOT_COMPLETE(10004, "参数缺失"),

    /**
     * 系统内部错误
     */
    SYSTEM_INNER_ERROR(40001, "系统繁忙，请稍后重试");

    /**
     * 业务状态码
     */
    public final Integer code;

    /**
     * 状态码对应的默认提示信息
     */
    public final String msg;

    ResultCode(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
