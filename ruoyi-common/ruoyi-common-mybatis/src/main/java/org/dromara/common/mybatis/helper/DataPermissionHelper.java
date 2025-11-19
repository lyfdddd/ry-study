// 数据权限助手工具类，提供数据权限的临时忽略和控制功能
// 基于MyBatis-Plus拦截器机制实现，支持可重入的权限忽略
package org.dromara.common.mybatis.helper;

// Sa-Token上下文持有者，用于获取当前请求的存储对象
import cn.dev33.satoken.context.SaHolder;
// Sa-Token存储模型，类似HttpServletRequest的Attribute
import cn.dev33.satoken.context.model.SaStorage;
// Hutool集合工具类，用于判断集合是否为空
import cn.hutool.core.collection.CollectionUtil;
// Hutool对象工具类，用于判空和比较
import cn.hutool.core.util.ObjectUtil;
// MyBatis-Plus忽略策略类，用于控制各种拦截器的开关
import com.baomidou.mybatisplus.core.plugins.IgnoreStrategy;
// MyBatis-Plus拦截器忽略帮助类，全局管理忽略策略
import com.baomidou.mybatisplus.core.plugins.InterceptorIgnoreHelper;
// Lombok注解，私有化构造函数防止实例化
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
// 反射工具类，用于获取私有静态字段
import org.dromara.common.core.utils.reflect.ReflectUtils;
// 数据权限注解，标记在Mapper方法上
import org.dromara.common.mybatis.annotation.DataPermission;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.function.Supplier;

/**
 * 数据权限助手
 *
 * @author Lion Li
 * @version 3.5.0
 */
// Lombok注解：私有化构造函数，防止外部实例化工具类
@NoArgsConstructor(access = AccessLevel.PRIVATE)
// 抑制类型转换警告，因为Map类型转换是安全的
@SuppressWarnings("unchecked cast")
public class DataPermissionHelper {

    // SaStorage中存储数据权限上下文的Key，用于在请求上下文中传递数据权限参数
    private static final String DATA_PERMISSION_KEY = "data:permission";

    // 可重入忽略计数器，使用ThreadLocal存储每个线程的调用栈
    // Stack<Integer>存储忽略深度，支持嵌套调用
    private static final ThreadLocal<Stack<Integer>> REENTRANT_IGNORE = ThreadLocal.withInitial(Stack::new);

    // 当前执行的Mapper权限注解缓存，使用ThreadLocal存储
    // 在MyBatis-Plus拦截器中设置和获取
    private static final ThreadLocal<DataPermission> PERMISSION_CACHE = new ThreadLocal<>();

    /**
     * 获取当前执行mapper权限注解
     *
     * @return 返回当前执行mapper权限注解
     */
    // 从ThreadLocal缓存中获取当前Mapper方法上的数据权限注解
    public static DataPermission getPermission() {
        return PERMISSION_CACHE.get();
    }

    /**
     * 设置当前执行mapper权限注解
     *
     * @param dataPermission   数据权限注解
     */
    // 将数据权限注解设置到ThreadLocal缓存中，供拦截器使用
    public static void setPermission(DataPermission dataPermission) {
        PERMISSION_CACHE.set(dataPermission);
    }

    /**
     * 删除当前执行mapper权限注解
     */
    // 清除ThreadLocal缓存中的数据权限注解，防止内存泄漏
    public static void removePermission() {
        PERMISSION_CACHE.remove();
    }

    /**
     * 从上下文中获取指定键的变量值，并将其转换为指定的类型
     *
     * @param key 变量的键
     * @param <T> 变量值的类型
     * @return 指定键的变量值，如果不存在则返回 null
     */
    // 从SaStorage上下文中获取数据权限变量，支持泛型转换
    public static <T> T getVariable(String key) {
        // 获取数据权限上下文Map
        Map<String, Object> context = getContext();
        // 根据key获取值并强制类型转换
        return (T) context.get(key);
    }

    /**
     * 向上下文中设置指定键的变量值
     *
     * @param key   要设置的变量的键
     * @param value 要设置的变量值
     */
    // 向SaStorage上下文中设置数据权限变量，供SQL拦截器读取
    public static void setVariable(String key, Object value) {
        // 获取数据权限上下文Map
        Map<String, Object> context = getContext();
        // 将键值对放入Map
        context.put(key, value);
    }

    /**
     * 获取数据权限上下文
     *
     * @return 存储在SaStorage中的Map对象，用于存储数据权限相关的上下文信息
     * @throws NullPointerException 如果数据权限上下文类型异常，则抛出NullPointerException
     */
    // 获取或创建数据权限上下文Map，存储在SaStorage中（类似Request域）
    public static Map<String, Object> getContext() {
        // 获取SaStorage对象，用于在当前请求中存储数据
        SaStorage saStorage = SaHolder.getStorage();
        // 从SaStorage中获取数据权限上下文
        Object attribute = saStorage.get(DATA_PERMISSION_KEY);
        // 如果上下文不存在，创建新的HashMap并存储
        if (ObjectUtil.isNull(attribute)) {
            saStorage.set(DATA_PERMISSION_KEY, new HashMap<>());
            attribute = saStorage.get(DATA_PERMISSION_KEY);
        }
        // 判断获取到的对象是否为Map类型，使用Java 14的instanceof模式匹配
        if (attribute instanceof Map map) {
            return map;
        }
        // 如果类型不匹配，抛出空指针异常
        throw new NullPointerException("data permission context type exception");
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
     * 开启忽略数据权限(开启后需手动调用 {@link #disableIgnore()} 关闭)
     */
    // 开启数据权限忽略模式，所有查询将不添加数据权限条件
    // 需要手动调用disableIgnore()关闭，支持可重入
    public static void enableIgnore() {
        // 获取当前的忽略策略对象
        IgnoreStrategy ignoreStrategy = getIgnoreStrategy();
        // 如果策略对象不存在，创建新的忽略策略并设置dataPermission=true
        if (ObjectUtil.isNull(ignoreStrategy)) {
            InterceptorIgnoreHelper.handle(IgnoreStrategy.builder().dataPermission(true).build());
        } else {
            // 如果策略对象已存在，直接设置dataPermission=true
            ignoreStrategy.setDataPermission(true);
        }
        // 获取可重入计数器栈，记录忽略深度
        Stack<Integer> reentrantStack = REENTRANT_IGNORE.get();
        // 将当前深度压入栈，支持嵌套调用
        reentrantStack.push(reentrantStack.size() + 1);
    }

    /**
     * 关闭忽略数据权限
     */
    // 关闭数据权限忽略模式，恢复数据权限控制
    // 根据可重入计数器判断是否需要真正关闭
    public static void disableIgnore() {
        // 获取当前的忽略策略对象
        IgnoreStrategy ignoreStrategy = getIgnoreStrategy();
        // 判断策略对象是否存在
        if (ObjectUtil.isNotNull(ignoreStrategy)) {
            // 判断是否没有其他忽略策略（动态表名、攻击阻断、非法SQL、租户、其他）
            boolean noOtherIgnoreStrategy = !Boolean.TRUE.equals(ignoreStrategy.getDynamicTableName())
                && !Boolean.TRUE.equals(ignoreStrategy.getBlockAttack())
                && !Boolean.TRUE.equals(ignoreStrategy.getIllegalSql())
                && !Boolean.TRUE.equals(ignoreStrategy.getTenantLine())
                && CollectionUtil.isEmpty(ignoreStrategy.getOthers());
            // 获取可重入计数器栈
            Stack<Integer> reentrantStack = REENTRANT_IGNORE.get();
            // 判断栈是否为空或当前深度为1（最外层调用）
            boolean empty = reentrantStack.isEmpty() || reentrantStack.pop() == 1;
            // 如果没有其他忽略策略且是最外层调用，清除整个忽略策略
            if (noOtherIgnoreStrategy && empty) {
                InterceptorIgnoreHelper.clearIgnoreStrategy();
            } else if (empty) {
                // 如果只是最外层调用，仅关闭数据权限忽略
                ignoreStrategy.setDataPermission(false);
            }
            // 如果不是最外层调用（嵌套调用），不做任何操作，等待外层调用关闭
        }
    }

    /**
     * 在忽略数据权限中执行
     *
     * @param handle 处理执行方法
     */
    // 在数据权限忽略模式下执行Runnable任务，自动管理开关
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
     * 在忽略数据权限中执行
     *
     * @param handle 处理执行方法
     */
    // 在数据权限忽略模式下执行Supplier任务，自动管理开关并返回结果
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

}
