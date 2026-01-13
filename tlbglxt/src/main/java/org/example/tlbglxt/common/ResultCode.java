package org.example.tlbglxt.common;

/**
 * 响应状态码枚举
 */
public enum ResultCode {

    // ============================== 成功状态码 ==============================
    SUCCESS(200, "操作成功"),
    
    // ============================== 客户端错误状态码 ==============================
    ERROR(400, "操作失败"),
    PARAM_ERROR(400, "参数错误"),
    PARAM_MISSING(400, "缺少必要参数"),
    PARAM_TYPE_ERROR(400, "参数类型错误"),
    PARAM_BIND_ERROR(400, "参数绑定错误"),
    PARAM_VALID_ERROR(400, "参数校验失败"),
    
    // ============================== 认证授权错误状态码 ==============================
    UNAUTHORIZED(401, "未授权访问"),
    TOKEN_INVALID(401, "Token无效"),
    TOKEN_EXPIRED(401, "Token已过期"),
    TOKEN_MISSING(401, "Token缺失"),
    LOGIN_EXPIRED(401, "登录已过期，请重新登录"),
    
    FORBIDDEN(403, "禁止访问"),
    PERMISSION_DENIED(403, "权限不足"),
    
    // ============================== 资源错误状态码 ==============================
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    
    // ============================== 业务错误状态码 ==============================
    USER_NOT_EXIST(1001, "用户不存在"),
    USER_ALREADY_EXIST(1002, "用户已存在"),
    USER_DISABLED(1003, "用户已被禁用"),
    PASSWORD_ERROR(1004, "密码错误"),
    OLD_PASSWORD_ERROR(1005, "原密码错误"),
    
    ROLE_NOT_EXIST(2001, "角色不存在"),
    ROLE_ALREADY_EXIST(2002, "角色已存在"),
    ROLE_HAS_USERS(2003, "角色下还有用户，无法删除"),
    
    MENU_NOT_EXIST(3001, "菜单不存在"),
    MENU_HAS_CHILDREN(3002, "菜单下还有子菜单，无法删除"),
    
    DEPT_NOT_EXIST(4001, "部门不存在"),
    DEPT_HAS_CHILDREN(4002, "部门下还有子部门，无法删除"),
    DEPT_HAS_USERS(4003, "部门下还有用户，无法删除"),
    
    // ============================== 文件错误状态码 ==============================
    FILE_UPLOAD_ERROR(5001, "文件上传失败"),
    FILE_TYPE_ERROR(5002, "文件类型不支持"),
    FILE_SIZE_ERROR(5003, "文件大小超出限制"),
    FILE_NOT_EXIST(5004, "文件不存在"),
    
    // ============================== 数据库错误状态码 ==============================
    DATA_NOT_EXIST(6001, "数据不存在"),
    DATA_ALREADY_EXIST(6002, "数据已存在"),
    DATA_ERROR(6003, "数据异常"),
    DATA_SAVE_ERROR(6004, "数据保存失败"),
    DATA_UPDATE_ERROR(6005, "数据更新失败"),
    DATA_DELETE_ERROR(6006, "数据删除失败"),
    
    // ============================== 服务器错误状态码 ==============================
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务暂不可用"),
    
    // ============================== 第三方服务错误状态码 ==============================
    THIRD_PARTY_SERVICE_ERROR(7001, "第三方服务异常"),
    SMS_SEND_ERROR(7002, "短信发送失败"),
    EMAIL_SEND_ERROR(7003, "邮件发送失败"),
    
    // ============================== 缓存错误状态码 ==============================
    CACHE_ERROR(8001, "缓存异常"),
    REDIS_ERROR(8002, "Redis异常"),
    
    // ============================== 限流错误状态码 ==============================
    RATE_LIMIT(9001, "请求过于频繁，请稍后再试"),
    REQUEST_TIMEOUT(9002, "请求超时");

    /**
     * 状态码
     */
    private final Integer code;

    /**
     * 消息
     */
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    /**
     * 根据状态码获取枚举
     */
    public static ResultCode getByCode(Integer code) {
        for (ResultCode resultCode : values()) {
            if (resultCode.getCode().equals(code)) {
                return resultCode;
            }
        }
        return ERROR;
    }

    @Override
    public String toString() {
        return "ResultCode{" +
                "code=" + code +
                ", message='" + message + '\'' +
                '}';
    }
} 