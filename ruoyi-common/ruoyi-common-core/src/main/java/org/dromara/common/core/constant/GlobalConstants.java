// 定义全局Key常量接口，统一管理Redis Key前缀，与业务模块解耦
// 使用接口形式定义常量，符合Java常量接口模式
package org.dromara.common.core.constant;

/**
 * 全局的key常量 (业务无关的key)
 * 定义全局通用的Redis Key前缀，与业务模块解耦
 * 所有全局Key都使用global:作为前缀，便于统一管理和清理
 * 是RuoYi-Vue-Plus中定义全局Redis Key的核心接口
 *
 * @author Lion Li
 */
// 定义公共接口，所有全局Key常量都定义在此接口中
// 使用接口而非类，符合常量接口模式（Constant Interface Pattern）
public interface GlobalConstants {

    /**
     * 全局 redis key (业务无关的key)
     * 所有全局Key的统一前缀，便于管理和清理
     * 值为"global:"，所有全局Key都以此开头
     */
    // 定义全局Redis Key前缀常量，值为"global:"
    // 用于统一所有全局Key的命名空间，避免Key冲突
    String GLOBAL_REDIS_KEY = "global:";

    /**
     * 验证码 redis key
     * 格式：global:captcha_codes:{uuid}
     * 存储验证码文本，用于登录、注册等场景的验证码校验
     * 验证码有效期2分钟，过期自动删除
     */
    // 定义验证码Redis Key前缀，使用GLOBAL_REDIS_KEY作为前缀
    // 完整格式：global:captcha_codes:{uuid}，uuid为验证码唯一标识
    String CAPTCHA_CODE_KEY = GLOBAL_REDIS_KEY + "captcha_codes:";

    /**
     * 防重提交 redis key
     * 格式：global:repeat_submit:{userId}:{url}:{md5}
     * 防止用户重复提交表单，基于Token机制实现
     * 默认间隔时间5秒，超过时间后才能再次提交
     */
    // 定义防重提交Redis Key前缀，使用GLOBAL_REDIS_KEY作为前缀
    // 完整格式：global:repeat_submit:{userId}:{url}:{md5}，基于用户ID、URL和请求参数MD5生成唯一Key
    String REPEAT_SUBMIT_KEY = GLOBAL_REDIS_KEY + "repeat_submit:";

    /**
     * 限流 redis key
     * 格式：global:rate_limit:{key}
     * 基于Redis令牌桶算法实现接口限流，防止恶意请求
     * 支持IP限流、用户限流、全局限流等多种策略
     */
    // 定义限流Redis Key前缀，使用GLOBAL_REDIS_KEY作为前缀
    // 完整格式：global:rate_limit:{key}，key可以是IP、用户ID等
    String RATE_LIMIT_KEY = GLOBAL_REDIS_KEY + "rate_limit:";

    /**
     * 三方认证 redis key
     * 格式：global:social_auth_codes:{state}
     * 存储OAuth2授权码，用于第三方社交登录
     * state参数用于防止CSRF攻击，有效期5分钟
     */
    // 定义三方认证Redis Key前缀，使用GLOBAL_REDIS_KEY作为前缀
    // 完整格式：global:social_auth_codes:{state}，state为OAuth2的state参数
    String SOCIAL_AUTH_CODE_KEY = GLOBAL_REDIS_KEY + "social_auth_codes:";
}
