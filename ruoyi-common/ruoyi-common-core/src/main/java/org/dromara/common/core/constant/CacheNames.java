// 定义缓存组名称常量接口，统一管理Redis缓存的key命名规范
// 采用接口形式定义常量，符合Java常量最佳实践
package org.dromara.common.core.constant;

/**
 * 缓存组名称常量
 * <p>
 * key 格式为 cacheNames#ttl#maxIdleTime#maxSize#local
 * <p>
 * ttl 过期时间 如果设置为0则不过期 默认为0
 * maxIdleTime 最大空闲时间 根据LRU算法清理空闲数据 如果设置为0则不检测 默认为0
 * maxSize 组最大长度 根据LRU算法清理溢出数据 如果设置为0则无限长 默认为0
 * local 默认开启本地缓存为1 关闭本地缓存为0
 * <p>
 * 例子: test#60s、test#0#60s、test#0#1m#1000、test#1h#0#500、test#1h#0#500#0
 *
 * @author Lion Li
 */
// 定义公共接口，所有缓存key常量都定义在此接口中
// 使用接口而非类，符合常量接口模式（Constant Interface Pattern）
public interface CacheNames {

    /**
     * 演示案例
     */
    // 演示缓存配置：60秒过期，10分钟最大空闲时间，最多20个元素
    // 格式：命名空间#ttl#maxIdleTime#maxSize
    String DEMO_CACHE = "demo:cache#60s#10m#20";

    /**
     * 系统配置
     */
    // 系统配置缓存，使用默认配置（不过期、无大小限制）
    // 存储系统参数配置，如网站名称、Logo等
    String SYS_CONFIG = "sys_config";

    /**
     * 数据字典
     */
    // 数据字典缓存，使用默认配置
    // 存储字典数据，如性别、状态等下拉选项
    String SYS_DICT = "sys_dict";

    /**
     * 数据字典类型
     */
    // 数据字典类型缓存，使用默认配置
    // 存储字典类型定义，如sys_user_sex、sys_notice_type等
    String SYS_DICT_TYPE = "sys_dict_type";

    /**
     * 租户
     */
    // 租户信息缓存，添加全局Redis前缀，30天过期
    // 使用GlobalConstants.GLOBAL_REDIS_KEY确保多租户环境下key不冲突
    String SYS_TENANT = GlobalConstants.GLOBAL_REDIS_KEY + "sys_tenant#30d";

    /**
     * 客户端
     */
    // 客户端信息缓存，添加全局Redis前缀，30天过期
    // 存储OAuth2客户端信息，如client_id、client_secret等
    String SYS_CLIENT = GlobalConstants.GLOBAL_REDIS_KEY + "sys_client#30d";

    /**
     * 用户账户
     */
    // 用户账户缓存，30天过期
    // 存储用户名到用户信息的映射，用于快速查询
    String SYS_USER_NAME = "sys_user_name#30d";

    /**
     * 用户名称
     */
    // 用户昵称缓存，30天过期
    // 存储用户ID到昵称的映射，用于展示
    String SYS_NICKNAME = "sys_nickname#30d";

    /**
     * 部门
     */
    // 部门信息缓存，30天过期
    // 存储部门树形结构，用于权限控制和数据范围过滤
    String SYS_DEPT = "sys_dept#30d";

    /**
     * OSS内容
     */
    // OSS对象存储缓存，30天过期
    // 存储文件上传记录和配置信息
    String SYS_OSS = "sys_oss#30d";

    /**
     * 角色自定义权限
     */
    // 角色自定义权限缓存，30天过期
    // 存储角色的自定义权限字符串，用于权限校验
    String SYS_ROLE_CUSTOM = "sys_role_custom#30d";

    /**
     * 部门及以下权限
     */
    // 部门及以下权限缓存，30天过期
    // 存储部门及其子部门的权限范围，用于数据权限控制
    String SYS_DEPT_AND_CHILD = "sys_dept_and_child#30d";

    /**
     * OSS配置
     */
    // OSS配置缓存，添加全局Redis前缀
    // 存储OSS服务商配置，如阿里云、腾讯云等
    String SYS_OSS_CONFIG = GlobalConstants.GLOBAL_REDIS_KEY + "sys_oss_config";

    /**
     * 在线用户
     */
    // 在线用户Token缓存，无过期时间（由Sa-Token管理）
    // 存储在线用户的Token信息，用于会话管理
    String ONLINE_TOKEN = "online_tokens";

}
