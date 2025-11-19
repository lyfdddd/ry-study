package org.dromara.common.core.constant;

/**
 * 缓存的key 常量
 * 定义系统中常用的Redis缓存Key前缀，统一管理和避免Key冲突
 *
 * @author Lion Li
 */
public interface CacheConstants {

    /**
     * 在线用户 redis key
     * 格式：online_tokens:{token}
     * 存储在线用户的Token信息，用于会话管理和踢人下线
     */
    String ONLINE_TOKEN_KEY = "online_tokens:";

    /**
     * 参数管理 cache key
     * 格式：sys_config:{configKey}
     * 存储系统配置参数，如网站名称、版权信息等
     */
    String SYS_CONFIG_KEY = "sys_config:";

    /**
     * 字典管理 cache key
     * 格式：sys_dict:{dictType}
     * 存储数据字典，如用户状态、订单状态等枚举值
     */
    String SYS_DICT_KEY = "sys_dict:";

    /**
     * 登录账户密码错误次数 redis key
     * 格式：pwd_err_cnt:{username}
     * 记录用户密码错误次数，用于防暴力破解
     */
    String PWD_ERR_CNT_KEY = "pwd_err_cnt:";

}
