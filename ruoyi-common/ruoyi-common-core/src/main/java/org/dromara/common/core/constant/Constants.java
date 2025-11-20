// 定义系统通用常量接口，统一管理字符集、协议、状态等常量
// 使用接口形式定义常量，符合Java常量接口模式
package org.dromara.common.core.constant;

/**
 * 通用常量信息
 * 定义系统中通用的字符集、协议、状态等常量，避免硬编码
 * 所有常量都定义为public static final，确保全局可访问且不可修改
 * 是RuoYi-Vue-Plus中定义通用常量的核心接口
 *
 * @author ruoyi
 */
// 定义公共接口，所有常量都定义在此接口中
// 使用接口而非类，符合常量接口模式（Constant Interface Pattern）
public interface Constants {

    /**
     * UTF-8 字符集
     * 国际化标准字符编码，支持全球所有语言字符
     * 是Web开发中最常用的字符集，支持中文、英文、日文、韩文等
     * 在HTTP请求、响应、文件读写等场景广泛使用
     */
    // 定义UTF-8字符集常量，值为"UTF-8"
    // 用于统一系统中所有需要指定字符集的地方
    String UTF8 = "UTF-8";

    /**
     * GBK 字符集
     * 中文简体字符编码，兼容GB2312，用于特定场景
     * 主要用于兼容旧系统或特定中文环境
     * 不推荐在新项目中使用，优先使用UTF-8
     */
    // 定义GBK字符集常量，值为"GBK"
    // 用于兼容需要GBK编码的场景
    String GBK = "GBK";

    /**
     * www主域
     * 网站主域名前缀，用于URL拼接和域名解析
     * 例如：www.example.com中的www.
     */
    // 定义www主域常量，值为"www."
    // 用于构建完整的域名URL
    String WWW = "www.";

    /**
     * http请求协议
     * 超文本传输协议，明文传输，用于非敏感数据传输
     * 默认端口80，不安全，不推荐在生产环境使用
     */
    // 定义HTTP协议常量，值为"http://"
    // 用于构建HTTP协议的URL
    String HTTP = "http://";

    /**
     * https请求协议
     * 安全的超文本传输协议，SSL/TLS加密，用于敏感数据传输
     * 默认端口443，安全可靠，推荐在生产环境使用
     */
    // 定义HTTPS协议常量，值为"https://"
    // 用于构建HTTPS协议的URL，确保数据传输安全
    String HTTPS = "https://";

    /**
     * 通用成功标识
     * 字符串"0"表示操作成功，与前端约定
     * 前端根据此标识判断操作是否成功
     */
    // 定义成功标识常量，值为"0"
    // 与前端约定，0表示成功，1表示失败
    String SUCCESS = "0";

    /**
     * 通用失败标识
     * 字符串"1"表示操作失败，与前端约定
     * 前端根据此标识判断操作是否失败
     */
    // 定义失败标识常量，值为"1"
    // 与前端约定，1表示失败，0表示成功
    String FAIL = "1";

    /**
     * 登录成功标识
     * 用于记录登录日志时的状态标识
     * 存储在sys_logininfor表的status字段
     */
    // 定义登录成功标识常量，值为"Success"
    // 用于记录登录成功的日志状态
    String LOGIN_SUCCESS = "Success";

    /**
     * 注销标识
     * 用于记录用户注销日志时的状态标识
     * 存储在sys_logininfor表的status字段
     */
    // 定义注销标识常量，值为"Logout"
    // 用于记录用户注销的日志状态
    String LOGOUT = "Logout";

    /**
     * 注册标识
     * 用于记录用户注册日志时的状态标识
     * 存储在sys_logininfor表的status字段
     */
    // 定义注册标识常量，值为"Register"
    // 用于记录用户注册的日志状态
    String REGISTER = "Register";

    /**
     * 登录失败标识
     * 用于记录登录失败日志时的状态标识
     * 存储在sys_logininfor表的status字段
     */
    // 定义登录失败标识常量，值为"Error"
    // 用于记录登录失败的日志状态
    String LOGIN_FAIL = "Error";

    /**
     * 验证码有效期（分钟）
     * 默认2分钟过期，防止验证码被暴力破解
     * 存储在Redis中，过期后自动删除
     */
    // 定义验证码有效期常量，值为2（分钟）
    // 用于设置验证码在Redis中的过期时间
    Integer CAPTCHA_EXPIRATION = 2;

    /**
     * 顶级父级id
     * 树形结构的根节点ID，表示最顶层父节点
     * 在部门、菜单等树形结构中表示根节点
     */
    // 定义顶级父级ID常量，值为0L
    // 用于表示树形结构的根节点
    Long TOP_PARENT_ID = 0L;

    /**
     * 加密头标识
     * 用于标识数据已加密，格式：ENC_密文
     * 在数据库存储敏感数据时使用，如密码、身份证号等
     */
    // 定义加密头标识常量，值为"ENC_"
    // 用于标识数据已加密，便于识别和处理
    String ENCRYPT_HEADER = "ENC_";

}

