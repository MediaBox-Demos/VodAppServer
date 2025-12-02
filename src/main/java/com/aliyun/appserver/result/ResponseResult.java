package com.aliyun.appserver.result;

/**
 * {@link CallResult} 构造工具类
 *
 * <p>
 * 提供一组静态工厂方法，用于快速构建统一返回结果：
 * <ul>
 *     <li>{@link #makeOkRsp()} / {@link #makeOkRsp(Object)}：成功结果</li>
 *     <li>{@link #makeErrRsp(String)}：通用错误结果</li>
 *     <li>{@link #makeRsp(int, String)} / {@link #makeRsp(int, String, Object)}：自定义结果</li>
 * </ul>
 * </p>
 */
public class ResponseResult {

    private static final String SUCCESS = "success";

    @SuppressWarnings("unused")
    private static final String FAIL = "fail";

    /**
     * 构造一个不携带数据的成功结果
     *
     * @param <T> 业务数据类型
     * @return {@code success=true} 且 {@code code=ResultCode.SUCCESS} 的结果
     */
    public static <T> CallResult<T> makeOkRsp() {

        CallResult<T> result = new CallResult<T>();
        result.setCode(ResultCode.SUCCESS);
        result.setSuccess(true);
        result.setMessage(SUCCESS);
        return result;
    }

    /**
     * 构造一个不携带数据但自定义成功文案的结果
     *
     * @param message 成功提示信息
     * @param <T>     业务数据类型
     * @return 包含自定义 {@code message} 的成功结果
     */
    public static <T> CallResult<T> makeOkRsp(String message) {

        CallResult<T> result = new CallResult<T>();
        result.setCode(ResultCode.SUCCESS);
        result.setSuccess(true);
        result.setMessage(message);
        return result;
    }

    /**
     * 构造一个携带业务数据的成功结果
     *
     * @param data 业务数据
     * @param <T>  业务数据类型
     * @return 携带 {@code data} 的成功结果
     */
    public static <T> CallResult<T> makeOkRsp(T data) {

        CallResult<T> result = new CallResult<T>();
        result.setCode(ResultCode.SUCCESS);
        result.setMessage(SUCCESS);
        result.setSuccess(true);
        result.setData(data);
        return result;
    }

    /**
     * 构造一个通用错误结果，使用 {@link ResultCode#SYSTEM_INNER_ERROR} 作为错误码
     *
     * @param message 错误信息
     * @param <T>     业务数据类型
     * @return 通用错误结果
     */
    public static <T> CallResult<T> makeErrRsp(String message) {

        CallResult<T> result = new CallResult<T>();
        result.setCode(ResultCode.SYSTEM_INNER_ERROR);
        result.setSuccess(false);
        result.setMessage(message);
        return result;
    }

    /**
     * 构造一个仅包含自定义状态码和消息的结果
     *
     * @param code 自定义业务状态码
     * @param msg  提示信息
     * @param <T>  业务数据类型
     * @return 不携带数据的结果
     */
    public static <T> CallResult<T> makeRsp(int code, String msg) {

        CallResult<T> result = new CallResult<T>();
        result.setCode(code);
        result.setMessage(msg);
        return result;
    }

    /**
     * 使用 {@link ResultCode} 构造结果
     *
     * @param resultCode 预定义业务状态码
     * @param <T>        业务数据类型
     * @return 使用 {@link ResultCode#code} 和 {@link ResultCode#msg} 填充的结果
     */
    public static <T> CallResult<T> makeRsp(ResultCode resultCode) {

        CallResult<T> result = new CallResult<T>();
        result.setCode(resultCode.code);
        result.setMessage(resultCode.msg);
        return result;
    }

    /**
     * 构造一个携带自定义状态码、消息和数据的结果
     *
     * @param code 自定义业务状态码
     * @param msg  提示信息
     * @param data 业务数据
     * @param <T>  业务数据类型
     * @return 完整自定义的返回结果
     */
    public static <T> CallResult<T> makeRsp(int code, String msg, T data) {

        CallResult<T> result = new CallResult<T>();
        result.setCode(code);
        result.setMessage(msg);
        result.setData(data);
        return result;
    }
}
