package org.dromara.common.core.constant;

/**
 * 全局的key常量 (业务无关的key)
 * 定义全局通用的Redis Key前缀，与业务模块解耦
 *
 * @author Lion Li
 */
public interface GlobalConstants {

    /**
     * 全局 redis key (业务无关的key)
     * 所有全局Key的统一前缀，便于管理和清理
     */
    String GLOBAL_REDIS_KEY = "global:";

    /**
     * 验证码 redis key
     * 格式：global:captcha_codes:{uuid}
     * 存储验证码文本，用于登录、注册等场景的验证码校验
     */
    String CAPTCHA_CODE_KEY = GLOBAL_REDIS_KEY + "captcha_codes:";

    /**
     * 防重提交 redis key
     * 格式：global:repeat_submit:{userId}:{url}:{md5}
     * 防止用户重复提交表单，基于Token机制实现
     */
    String REPEAT_SUBMIT_KEY = GLOBAL_REDIS_KEY + "repeat_submit:";

    /**
     * 限流 redis key
     * 格式：global:rate_limit:{key}
     * 基于Redis令牌桶算法实现接口限流，防止恶意请求
     */
    String RATE_LIMIT_KEY = GLOBAL_REDIS_KEY + "rate_limit:";

    /**
     * 三方认证 redis key
     * 格式：global:social_auth_codes:{state}
     * 存储OAuth2授权码，用于第三方社交登录
     */
    String SOCIAL_AUTH_CODE_KEY = GLOBAL_REDIS_KEY + "social_auth_codes:";
}
