// 租户助手工具类，提供多租户场景下的租户切换和忽略功能
// 支持动态租户、全局租户和临时租户三种模式，基于ThreadLocal和Redis实现
package org.dromara.common.tenant.helper;

// Sa-Token上下文持有者，用于获取当前请求的存储对象
import cn.dev33.satoken.context.SaHolder;
// Sa-Token存储模型，类似HttpServletRequest的Attribute
import cn.dev33.satoken.context.model.SaStorage;
// Hutool集合工具类，用于判断集合是否为空
import cn.hutool.core.collection.CollectionUtil;
// Hutool类型转换工具类，安全转换各种数据类型
import cn.hutool.core.convert.Convert;
// Hutool对象工具类，用于判空和比较
import cn.hutool.core.util.ObjectUtil;
// MyBatis-Plus忽略策略类，用于控制各种拦截器的开关
import com.baomidou.mybatisplus.core.plugins.IgnoreStrategy;
// MyBatis-Plus拦截器忽略帮助类，全局管理忽略策略
import com.baomidou.mybatisplus.core.plugins.InterceptorIgnoreHelper;
// Lombok注解，私有化构造函数防止实例化
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
// Lombok日志注解，自动生成日志对象
import lombok.extern.slf4j.Slf4j;
// 全局常量定义，包含Redis Key前缀
import org.dromara.common.core.constant.GlobalConstants;
// Spring工具类，用于获取配置和Bean
import org.dromara.common.core.utils.SpringUtils;
// 字符串工具类，提供字符串判空和操作
import org.dromara.common.core.utils.StringUtils;
// 反射工具类，用于获取私有静态字段
import org.dromara.common.core.utils.reflect.ReflectUtils;
// Redis操作工具类，用于缓存动态租户信息
import org.dromara.common.redis.utils.RedisUtils;
// 登录鉴权助手，获取当前登录用户信息
import org.dromara.common.satoken.utils.LoginHelper;

import java.util.Stack;
import java.util.function.Supplier;

/**
 * 租户助手
 *
 * @author Lion Li
 */
// Lombok日志注解：自动生成slf4j日志对象
@Slf4j
// Lombok注解：私有化构造函数，防止外部实例化工具类
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TenantHelper {

    // Redis中存储动态租户信息的Key前缀，格式：dynamicTenant:userId
    private static final String DYNAMIC_TENANT_KEY = GlobalConstants.GLOBAL_REDIS_KEY + "dynamicTenant";

    // 临时动态租户ThreadLocal，存储未登录状态下的租户ID
    // 只在当前线程内有效，线程结束后自动清理
    private static final ThreadLocal<String> TEMP_DYNAMIC_TENANT = new ThreadLocal<>();

    // 可重入忽略计数器，使用ThreadLocal存储每个线程的调用栈
    // Stack<Integer>存储忽略深度，支持嵌套调用
    private static final ThreadLocal<Stack<Integer>> REENTRANT_IGNORE = ThreadLocal.withInitial(Stack::new);

    /**
     * 租户功能是否启用
     */
    // 从Spring配置中读取tenant.enable配置项，默认false
    // 用于判断系统是否开启多租户功能
    public static boolean isEnable() {
        return Convert.toBool(SpringUtils.getProperty("tenant.enable"), false);
    }

    // 私有方法：通过反射获取MyBatis-Plus的忽略策略ThreadLocal
    // 这是框架内部实现，可能随版本变化
    private static IgnoreStrategy getIgnoreStrategy() {
        // 使用反射工具获取InterceptorIgnoreHelper类的静态字段IGNORE_STRATEGY_LOCAL
        Object ignoreStrategyLocal = ReflectUtils.getStaticFieldValue(ReflectUtils.getField(InterceptorIgnoreHelper.class, "IGNORE_STRATEGY_LOCAL"));
        // 判断获取到的对象是否为ThreadLocal类型
        if (ignoreStrategyLocal instanceof ThreadLocal<?> IGNORE_STRATEGY_LOCAL) {
            // 从ThreadLocal中获取IgnoreStrategy对象
            if (IGNORE_STRATEGY_LOCAL.get() instanceof IgnoreStrategy ignoreStrategy) {
                return ignoreStrategy;
            }
        }
        // 如果获取失败返回null
        return null;
    }

    /**
     * 开启忽略租户(开启后需手动调用 {@link #disableIgnore()} 关闭)
     */
    // 开启租户忽略模式，所有查询将不添加租户ID条件
    // 需要手动调用disableIgnore()关闭，支持可重入
    public static void enableIgnore() {
        // 获取当前的忽略策略对象
        IgnoreStrategy ignoreStrategy = getIgnoreStrategy();
        // 如果策略对象不存在，创建新的忽略策略并设置tenantLine=true
        if (ObjectUtil.isNull(ignoreStrategy)) {
            InterceptorIgnoreHelper.handle(IgnoreStrategy.builder().tenantLine(true).build());
        } else {
            // 如果策略对象已存在，直接设置tenantLine=true
            ignoreStrategy.setTenantLine(true);
        }
        // 获取可重入计数器栈，记录忽略深度
        Stack<Integer> reentrantStack = REENTRANT_IGNORE.get();
        // 将当前深度压入栈，支持嵌套调用
        reentrantStack.push(reentrantStack.size() + 1);
    }

    /**
     * 关闭忽略租户
     */
    // 关闭租户忽略模式，恢复租户隔离控制
    // 根据可重入计数器判断是否需要真正关闭
    public static void disableIgnore() {
        // 获取当前的忽略策略对象
        IgnoreStrategy ignoreStrategy = getIgnoreStrategy();
        // 判断策略对象是否存在
        if (ObjectUtil.isNotNull(ignoreStrategy)) {
            // 判断是否没有其他忽略策略（动态表名、攻击阻断、非法SQL、数据权限、其他）
            boolean noOtherIgnoreStrategy = !Boolean.TRUE.equals(ignoreStrategy.getDynamicTableName())
                && !Boolean.TRUE.equals(ignoreStrategy.getBlockAttack())
                && !Boolean.TRUE.equals(ignoreStrategy.getIllegalSql())
                && !Boolean.TRUE.equals(ignoreStrategy.getDataPermission())
                && CollectionUtil.isEmpty(ignoreStrategy.getOthers());
            // 获取可重入计数器栈
            Stack<Integer> reentrantStack = REENTRANT_IGNORE.get();
            // 判断栈是否为空或当前深度为1（最外层调用）
            boolean empty = reentrantStack.isEmpty() || reentrantStack.pop() == 1;
            // 如果没有其他忽略策略且是最外层调用，清除整个忽略策略
            if (noOtherIgnoreStrategy && empty) {
                InterceptorIgnoreHelper.clearIgnoreStrategy();
            } else if (empty) {
                // 如果只是最外层调用，仅关闭租户忽略
                ignoreStrategy.setTenantLine(false);
            }
            // 如果不是最外层调用（嵌套调用），不做任何操作，等待外层调用关闭
        }
    }

    /**
     * 在忽略租户中执行
     *
     * @param handle 处理执行方法
     */
    // 在租户忽略模式下执行Runnable任务，自动管理开关
    // 使用try-finally确保即使发生异常也能关闭忽略模式
    public static void ignore(Runnable handle) {
        // 开启忽略模式
        enableIgnore();
        try {
            // 执行传入的任务
            handle.run();
        } finally {
            // 确保关闭忽略模式
            disableIgnore();
        }
    }

    /**
     * 在忽略租户中执行
     *
     * @param handle 处理执行方法
     */
    // 在租户忽略模式下执行Supplier任务，自动管理开关并返回结果
    // 使用try-finally确保即使发生异常也能关闭忽略模式
    public static <T> T ignore(Supplier<T> handle) {
        // 开启忽略模式
        enableIgnore();
        try {
            // 执行传入的任务并返回结果
            return handle.get();
        } finally {
            // 确保关闭忽略模式
            disableIgnore();
        }
    }

    // 重载方法：设置动态租户，默认不全局生效
    public static void setDynamic(String tenantId) {
        // 调用重载方法，global参数默认为false
        setDynamic(tenantId, false);
    }

    /**
     * 设置动态租户(一直有效 需要手动清理)
     * <p>
     * 如果为未登录状态下 那么只在当前线程内生效
     *
     * @param tenantId 租户id
     * @param global   是否全局生效
     */
    // 设置动态租户ID，支持全局和临时两种模式
    // 全局模式会将租户ID存储到Redis和SaStorage，跨请求有效
    // 临时模式只存储在ThreadLocal，当前线程有效
    public static void setDynamic(String tenantId, boolean global) {
        // 判断租户功能是否启用，未启用直接返回
        if (!isEnable()) {
            return;
        }
        // 如果用户未登录或不是全局模式，存储到ThreadLocal
        if (!LoginHelper.isLogin() || !global) {
            TEMP_DYNAMIC_TENANT.set(tenantId);
            return;
        }
        // 全局模式下，构建Redis缓存Key：dynamicTenant:userId
        String cacheKey = DYNAMIC_TENANT_KEY + ":" + LoginHelper.getUserId();
        // 将租户ID存储到Redis，设置过期时间（默认2小时）
        RedisUtils.setCacheObject(cacheKey, tenantId);
        // 同时存储到SaStorage，当前请求内有效
        SaHolder.getStorage().set(cacheKey, tenantId);
    }

    /**
     * 获取动态租户(一直有效 需要手动清理)
     * <p>
     * 如果为未登录状态下 那么只在当前线程内生效
     */
    // 获取当前设置的动态租户ID，支持多级缓存
    // 优先级：ThreadLocal > SaStorage > Redis
    public static String getDynamic() {
        // 判断租户功能是否启用，未启用返回null
        if (!isEnable()) {
            return null;
        }
        // 如果用户未登录，直接从ThreadLocal获取
        if (!LoginHelper.isLogin()) {
            return TEMP_DYNAMIC_TENANT.get();
        }
        // 如果线程内有值（临时租户），优先返回
        String tenantId = TEMP_DYNAMIC_TENANT.get();
        if (StringUtils.isNotBlank(tenantId)) {
            return tenantId;
        }
        // 从SaStorage中获取，当前请求内有效
        SaStorage storage = SaHolder.getStorage();
        String cacheKey = DYNAMIC_TENANT_KEY + ":" + LoginHelper.getUserId();
        tenantId = storage.getString(cacheKey);
        // 如果为 -1 说明已经查过redis并且不存在值，直接返回null
        if (StringUtils.isNotBlank(tenantId)) {
            return tenantId.equals("-1") ? null : tenantId;
        }
        // SaStorage中没有，从Redis中获取
        tenantId = RedisUtils.getCacheObject(cacheKey);
        // 将结果存储到SaStorage，使用"-1"标记Redis中不存在
        storage.set(cacheKey, StringUtils.isBlank(tenantId) ? "-1" : tenantId);
        return tenantId;
    }

    /**
     * 清除动态租户
     */
    // 清除当前用户的动态租户设置
    // 清理ThreadLocal、Redis和SaStorage中的数据
    public static void clearDynamic() {
        // 判断租户功能是否启用，未启用直接返回
        if (!isEnable()) {
            return;
        }
        // 如果用户未登录，只清理ThreadLocal
        if (!LoginHelper.isLogin()) {
            TEMP_DYNAMIC_TENANT.remove();
            return;
        }
        // 清理ThreadLocal
        TEMP_DYNAMIC_TENANT.remove();
        // 构建Redis缓存Key
        String cacheKey = DYNAMIC_TENANT_KEY + ":" + LoginHelper.getUserId();
        // 删除Redis中的租户信息
        RedisUtils.deleteObject(cacheKey);
        // 删除SaStorage中的租户信息
        SaHolder.getStorage().delete(cacheKey);
    }

    /**
     * 在动态租户中执行
     *
     * @param handle 处理执行方法
     */
    // 在指定租户上下文中执行Runnable任务，自动管理租户设置和清理
    // 使用try-finally确保即使发生异常也能清理租户上下文
    public static void dynamic(String tenantId, Runnable handle) {
        // 设置动态租户
        setDynamic(tenantId);
        try {
            // 执行传入的任务
            handle.run();
        } finally {
            // 确保清理租户上下文
            clearDynamic();
        }
    }

    /**
     * 在动态租户中执行
     *
     * @param handle 处理执行方法
     */
    // 在指定租户上下文中执行Supplier任务，自动管理租户设置和清理并返回结果
    // 使用try-finally确保即使发生异常也能清理租户上下文
    public static <T> T dynamic(String tenantId, Supplier<T> handle) {
        // 设置动态租户
        setDynamic(tenantId);
        try {
            // 执行传入的任务并返回结果
            return handle.get();
        } finally {
            // 确保清理租户上下文
            clearDynamic();
        }
    }

    /**
     * 获取当前租户id(动态租户优先)
     */
    // 获取当前有效的租户ID，优先级：动态租户 > Token租户
    // 用于MyBatis-Plus租户拦截器获取当前租户ID
    public static String getTenantId() {
        // 判断租户功能是否启用，未启用返回null
        if (!isEnable()) {
            return null;
        }
        // 优先获取动态租户（手动设置的租户）
        String tenantId = TenantHelper.getDynamic();
        // 如果没有动态租户，从Token扩展信息中获取
        if (StringUtils.isBlank(tenantId)) {
            tenantId = LoginHelper.getTenantId();
        }
        return tenantId;
    }

}
