package org.dromara.common.core.constant;

/**
 * 返回状态码
 * 定义HTTP状态码和业务状态码常量，统一前后端交互规范
 *
 * @author Lion Li
 */
public interface HttpStatus {
    /**
     * 操作成功
     * HTTP标准状态码200，表示请求处理成功并返回数据
     */
    int SUCCESS = 200;

    /**
     * 对象创建成功
     * HTTP标准状态码201，表示资源创建成功（如新增用户、订单）
     */
    int CREATED = 201;

    /**
     * 请求已经被接受
     * HTTP标准状态码202，表示请求已接受但尚未处理完成（异步任务）
     */
    int ACCEPTED = 202;

    /**
     * 操作已经执行成功，但是没有返回数据
     * HTTP标准状态码204，表示请求成功但响应体为空（如删除操作）
     */
    int NO_CONTENT = 204;

    /**
     * 资源已被永久移除
     * HTTP标准状态码301，表示资源已永久重定向（较少使用）
     */
    int MOVED_PERM = 301;

    /**
     * 重定向
     * HTTP标准状态码303，表示资源临时重定向到新的URL
     */
    int SEE_OTHER = 303;

    /**
     * 资源没有被修改
     * HTTP标准状态码304，表示资源未变更，可使用缓存（配合ETag/Last-Modified）
     */
    int NOT_MODIFIED = 304;

    /**
     * 参数列表错误（缺少参数、格式不匹配）
     * HTTP标准状态码400，表示客户端请求参数错误
     */
    int BAD_REQUEST = 400;

    /**
     * 未授权
     * HTTP标准状态码401，表示用户未登录或Token失效
     */
    int UNAUTHORIZED = 401;

    /**
     * 访问受限，授权过期
     * HTTP标准状态码403，表示用户无权限访问该资源
     */
    int FORBIDDEN = 403;

    /**
     * 资源、服务未找到
     * HTTP标准状态码404，表示请求的资源不存在
     */
    int NOT_FOUND = 404;

    /**
     * 不允许的HTTP方法
     * HTTP标准状态码405，表示请求方法不被允许（如用GET访问POST接口）
     */
    int BAD_METHOD = 405;

    /**
     * 资源冲突，或者资源被锁
     * HTTP标准状态码409，表示资源冲突（如并发修改同一数据）
     */
    int CONFLICT = 409;

    /**
     * 不支持的数据、媒体类型
     * HTTP标准状态码415，表示请求体格式不支持（如需要JSON但发送XML）
     */
    int UNSUPPORTED_TYPE = 415;

    /**
     * 系统内部错误
     * HTTP标准状态码500，表示服务器内部异常（如空指针、数据库连接失败）
     */
    int ERROR = 500;

    /**
     * 接口未实现
     * HTTP标准状态码501，表示请求功能尚未实现
     */
    int NOT_IMPLEMENTED = 501;

    /**
     * 系统警告消息
     * 自定义业务状态码601，表示业务警告（如数据校验不通过但不阻断流程）
     */
    int WARN = 601;
}
