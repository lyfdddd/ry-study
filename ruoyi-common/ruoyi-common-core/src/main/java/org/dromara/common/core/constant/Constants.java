package org.dromara.common.core.constant;

/**
 * 通用常量信息
 * 定义系统中通用的字符集、协议、状态等常量，避免硬编码
 *
 * @author ruoyi
 */
public interface Constants {

    /**
     * UTF-8 字符集
     * 国际化标准字符编码，支持全球所有语言字符
     */
    String UTF8 = "UTF-8";

    /**
     * GBK 字符集
     * 中文简体字符编码，兼容GB2312，用于特定场景
     */
    String GBK = "GBK";

    /**
     * www主域
     * 网站主域名前缀，用于URL拼接和域名解析
     */
    String WWW = "www.";

    /**
     * http请求协议
     * 超文本传输协议，明文传输，用于非敏感数据传输
     */
    String HTTP = "http://";

    /**
     * https请求协议
     * 安全的超文本传输协议，SSL/TLS加密，用于敏感数据传输
     */
    String HTTPS = "https://";

    /**
     * 通用成功标识
     * 字符串"0"表示操作成功，与前端约定
     */
    String SUCCESS = "0";

    /**
     * 通用失败标识
     * 字符串"1"表示操作失败，与前端约定
     */
    String FAIL = "1";

    /**
     * 登录成功标识
     * 用于记录登录日志时的状态标识
     */
    String LOGIN_SUCCESS = "Success";

    /**
     * 注销标识
     * 用于记录用户注销日志时的状态标识
     */
    String LOGOUT = "Logout";

    /**
     * 注册标识
     * 用于记录用户注册日志时的状态标识
     */
    String REGISTER = "Register";

    /**
     * 登录失败标识
     * 用于记录登录失败日志时的状态标识
     */
    String LOGIN_FAIL = "Error";

    /**
     * 验证码有效期（分钟）
     * 默认2分钟过期，防止验证码被暴力破解
     */
    Integer CAPTCHA_EXPIRATION = 2;

    /**
     * 顶级父级id
     * 树形结构的根节点ID，表示最顶层父节点
     */
    Long TOP_PARENT_ID = 0L;

    /**
     * 加密头标识
     * 用于标识数据已加密，格式：ENC_密文
     */
    String ENCRYPT_HEADER = "ENC_";

}

