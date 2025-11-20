package org.dromara.common.tenant.manager;

import com.baomidou.mybatisplus.core.plugins.InterceptorIgnoreHelper;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.constant.GlobalConstants;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.redis.manager.PlusSpringCacheManager;
import org.dromara.common.tenant.helper.TenantHelper;
import org.springframework.cache.Cache;

/**
 * 重写 cacheName 处理方法 支持多租户
 *
 * @author Lion Li
 */
// Lombok日志注解，自动生成log日志对象
@Slf4j
public class TenantSpringCacheManager extends PlusSpringCacheManager {

    // 构造函数
    public TenantSpringCacheManager() {
    }

    // 重写getCache方法，在缓存名称前添加租户ID前缀，实现租户隔离
    @Override
    public Cache getCache(String name) {
        // 判断是否忽略租户（租户拦截器关闭），如果忽略则使用父类方法
        if (InterceptorIgnoreHelper.willIgnoreTenantLine("")) {
            return super.getCache(name);
        }
        // 如果缓存名称已包含全局Redis Key前缀，直接使用父类方法
        if (StringUtils.contains(name, GlobalConstants.GLOBAL_REDIS_KEY)) {
            return super.getCache(name);
        }
        // 获取当前租户ID
        String tenantId = TenantHelper.getTenantId();
        // 如果租户ID为空，记录错误日志
        if (StringUtils.isBlank(tenantId)) {
            log.error("无法获取有效的租户id -> Null");
        }
        // 如果缓存名称已包含租户ID前缀，直接返回（避免重复添加）
        if (StringUtils.startsWith(name, tenantId)) {
            // 如果存在则直接返回
            return super.getCache(name);
        }
        // 在缓存名称前添加租户ID前缀，格式：tenantId:name
        return super.getCache(tenantId + ":" + name);
    }

}
