package org.dromara.common.tenant.handle;

import com.baomidou.mybatisplus.core.plugins.InterceptorIgnoreHelper;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.constant.GlobalConstants;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.redis.handler.KeyPrefixHandler;
import org.dromara.common.tenant.helper.TenantHelper;

/**
 * 多租户redis缓存key前缀处理
 *
 * @author Lion Li
 */
// Lombok日志注解，自动生成log日志对象
@Slf4j
public class TenantKeyPrefixHandler extends KeyPrefixHandler {

    // 构造函数，传入key前缀
    public TenantKeyPrefixHandler(String keyPrefix) {
        super(keyPrefix);
    }

    /**
     * 增加前缀：在Redis key前添加租户ID前缀，实现租户隔离
     */
    @Override
    public String map(String name) {
        // 如果key为空，直接返回null
        if (StringUtils.isBlank(name)) {
            return null;
        }
        try {
            // 判断是否忽略租户（租户拦截器关闭），如果忽略则使用父类方法
            if (InterceptorIgnoreHelper.willIgnoreTenantLine("")) {
                return super.map(name);
            }
        } catch (NoClassDefFoundError ignore) {
            // 有些服务不需要mp导致类不存在 忽略即可
        }
        // 如果key已包含全局Redis Key前缀，直接使用父类方法
        if (StringUtils.contains(name, GlobalConstants.GLOBAL_REDIS_KEY)) {
            return super.map(name);
        }
        // 获取当前租户ID
        String tenantId = TenantHelper.getTenantId();
        // 如果租户ID为空，记录debug日志并使用父类方法
        if (StringUtils.isBlank(tenantId)) {
            log.debug("无法获取有效的租户id -> Null");
            return super.map(name);
        }
        // 如果key已包含租户ID前缀，直接返回（避免重复添加）
        if (StringUtils.startsWith(name, tenantId + "")) {
            // 如果存在则直接返回
            return super.map(name);
        }
        // 在key前添加租户ID前缀，格式：tenantId:key
        return super.map(tenantId + ":" + name);
    }

    /**
     * 去除前缀：从Redis key中移除租户ID前缀
     */
    @Override
    public String unmap(String name) {
        // 先调用父类方法去除全局前缀
        String unmap = super.unmap(name);
        // 如果结果为空，直接返回null
        if (StringUtils.isBlank(unmap)) {
            return null;
        }
        try {
            // 判断是否忽略租户（租户拦截器关闭），如果忽略则直接返回
            if (InterceptorIgnoreHelper.willIgnoreTenantLine("")) {
                return unmap;
            }
        } catch (NoClassDefFoundError ignore) {
            // 有些服务不需要mp导致类不存在 忽略即可
        }
        // 如果原始key包含全局Redis Key前缀，直接返回（不处理租户前缀）
        if (StringUtils.contains(name, GlobalConstants.GLOBAL_REDIS_KEY)) {
            return unmap;
        }
        // 获取当前租户ID
        String tenantId = TenantHelper.getTenantId();
        // 如果租户ID为空，记录debug日志并直接返回
        if (StringUtils.isBlank(tenantId)) {
            log.debug("无法获取有效的租户id -> Null");
            return unmap;
        }
        // 如果结果以租户ID开头，移除租户ID前缀
        if (StringUtils.startsWith(unmap, tenantId + "")) {
            // 如果存在则删除
            return unmap.substring((tenantId + ":").length());
        }
        // 返回去除前缀后的key
        return unmap;
    }

}
