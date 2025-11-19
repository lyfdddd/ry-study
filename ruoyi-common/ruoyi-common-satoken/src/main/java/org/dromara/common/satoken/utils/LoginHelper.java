// 登录鉴权助手工具类，封装了Sa-Token框架的核心操作
// 提供用户登录、信息获取、权限判断等统一入口
package org.dromara.common.satoken.utils;

// Sa-Token会话管理，用于存储登录用户信息
import cn.dev33.satoken.session.SaSession;
// Sa-Token核心认证工具类，处理登录、注销、权限验证
import cn.dev33.satoken.stp.StpUtil;
// Sa-Token登录参数配置类，支持扩展信息存储
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
// Hutool集合工具类，用于判断集合是否为空
import cn.hutool.core.collection.CollUtil;
// Hutool类型转换工具类，安全转换各种数据类型
import cn.hutool.core.convert.Convert;
// Hutool对象工具类，用于判空和默认值处理
import cn.hutool.core.util.ObjectUtil;
// Lombok注解，私有化构造函数防止实例化
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
// 系统常量定义，包含超级管理员ID
import org.dromara.common.core.constant.SystemConstants;
// 租户常量定义，包含租户管理员角色标识
import org.dromara.common.core.constant.TenantConstants;
// 登录用户实体类，封装用户身份和权限信息
import org.dromara.common.core.domain.model.LoginUser;
// 用户类型枚举，区分不同用户体系（PC、APP等）
import org.dromara.common.core.enums.UserType;

import java.util.Set;


/**
 * 登录鉴权助手
 * <p>
 * user_type 为 用户类型 同一个用户表 可以有多种用户类型 例如 pc,app
 * deivce 为 设备类型 同一个用户类型 可以有 多种设备类型 例如 web,ios
 * 可以组成 用户类型与设备类型多对多的 权限灵活控制
 * <p>
 * 多用户体系 针对 多种用户类型 但权限控制不一致
 * 可以组成 多用户类型表与多设备类型 分别控制权限
 *
 * @author Lion Li
 */
// Lombok注解：私有化构造函数，防止外部实例化工具类
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LoginHelper {

    // Token会话中存储登录用户信息的Key，用于从Session中获取LoginUser对象
    public static final String LOGIN_USER_KEY = "loginUser";
    // Token扩展信息中存储租户ID的Key，用于多租户场景下的数据隔离
    public static final String TENANT_KEY = "tenantId";
    // Token扩展信息中存储用户ID的Key，快速获取当前用户标识
    public static final String USER_KEY = "userId";
    // Token扩展信息中存储用户名的Key，用于日志记录和显示
    public static final String USER_NAME_KEY = "userName";
    // Token扩展信息中存储部门ID的Key，用于数据权限控制
    public static final String DEPT_KEY = "deptId";
    // Token扩展信息中存储部门名称的Key，用于前端展示
    public static final String DEPT_NAME_KEY = "deptName";
    // Token扩展信息中存储部门类别编码的Key，用于业务逻辑判断
    public static final String DEPT_CATEGORY_KEY = "deptCategory";
    // Token扩展信息中存储客户端ID的Key，用于区分不同客户端
    public static final String CLIENT_KEY = "clientid";

    /**
     * 登录系统 基于 设备类型
     * 针对相同用户体系不同设备
     *
     * @param loginUser 登录用户信息
     * @param model     配置参数
     */
    // 执行用户登录操作，将用户信息存储到Sa-Token会话中
    // 支持多设备登录，通过model参数配置登录行为
    public static void login(LoginUser loginUser, SaLoginParameter model) {
        // 如果model为null，创建默认的登录参数对象
        model = ObjectUtil.defaultIfNull(model, new SaLoginParameter());
        // 调用Sa-Token的login方法创建登录会话
        // 将租户ID、用户ID、用户名、部门信息等作为扩展数据存储到Token中
        StpUtil.login(loginUser.getLoginId(),
            model.setExtra(TENANT_KEY, loginUser.getTenantId())  // 存储租户ID，用于多租户隔离
                .setExtra(USER_KEY, loginUser.getUserId())        // 存储用户ID，快速定位用户
                .setExtra(USER_NAME_KEY, loginUser.getUsername()) // 存储用户名，用于日志和显示
                .setExtra(DEPT_KEY, loginUser.getDeptId())        // 存储部门ID，数据权限控制
                .setExtra(DEPT_NAME_KEY, loginUser.getDeptName()) // 存储部门名称，前端展示
                .setExtra(DEPT_CATEGORY_KEY, loginUser.getDeptCategory()) // 存储部门类别，业务判断
        );
        // 将完整的LoginUser对象存储到Token会话中，方便后续获取用户详细信息
        StpUtil.getTokenSession().set(LOGIN_USER_KEY, loginUser);
    }

    /**
     * 获取用户(多级缓存)
     */
    // 从当前Token会话中获取登录用户信息，支持泛型转换
    // 使用@SuppressWarnings抑制类型转换警告，因为Session存储的是Object类型
    @SuppressWarnings("unchecked cast")
    public static <T extends LoginUser> T getLoginUser() {
        // 获取当前Token对应的会话对象，如果Token无效返回null
        SaSession session = StpUtil.getTokenSession();
        // 判断会话是否存在，防止空指针异常
        if (ObjectUtil.isNull(session)) {
            return null;
        }
        // 从会话中获取LoginUser对象并强制类型转换
        return (T) session.get(LOGIN_USER_KEY);
    }

    /**
     * 获取用户基于token
     */
    // 根据指定Token获取登录用户信息，用于Token校验场景
    @SuppressWarnings("unchecked cast")
    public static <T extends LoginUser> T getLoginUser(String token) {
        // 通过Token获取对应的会话对象，如果Token无效返回null
        SaSession session = StpUtil.getTokenSessionByToken(token);
        // 判断会话是否存在，防止空指针异常
        if (ObjectUtil.isNull(session)) {
            return null;
        }
        // 从会话中获取LoginUser对象并强制类型转换
        return (T) session.get(LOGIN_USER_KEY);
    }

    /**
     * 获取用户id
     */
    // 从Token扩展信息中获取用户ID，转换为Long类型
    public static Long getUserId() {
        return Convert.toLong(getExtra(USER_KEY));
    }

    /**
     * 获取用户id
     */
    // 从Token扩展信息中获取用户ID，转换为String类型
    public static String getUserIdStr() {
        return Convert.toStr(getExtra(USER_KEY));
    }

    /**
     * 获取用户账户
     */
    // 从Token扩展信息中获取用户名，用于日志记录和显示
    public static String getUsername() {
        return Convert.toStr(getExtra(USER_NAME_KEY));
    }

    /**
     * 获取租户ID
     */
    // 从Token扩展信息中获取租户ID，用于多租户场景下的数据隔离
    public static String getTenantId() {
        return Convert.toStr(getExtra(TENANT_KEY));
    }

    /**
     * 获取部门ID
     */
    // 从Token扩展信息中获取部门ID，用于数据权限控制
    public static Long getDeptId() {
        return Convert.toLong(getExtra(DEPT_KEY));
    }

    /**
     * 获取部门名
     */
    // 从Token扩展信息中获取部门名称，用于前端展示
    public static String getDeptName() {
        return Convert.toStr(getExtra(DEPT_NAME_KEY));
    }

    /**
     * 获取部门类别编码
     */
    // 从Token扩展信息中获取部门类别编码，用于业务逻辑判断
    public static String getDeptCategory() {
        return Convert.toStr(getExtra(DEPT_CATEGORY_KEY));
    }

    /**
     * 获取当前 Token 的扩展信息
     *
     * @param key 键值
     * @return 对应的扩展数据
     */
    // 私有方法：从Token扩展信息中获取指定key的值
    // 使用try-catch捕获异常，防止Token失效时抛出异常
    private static Object getExtra(String key) {
        try {
            // 调用Sa-Token的getExtra方法获取扩展数据
            return StpUtil.getExtra(key);
        } catch (Exception e) {
            // 如果获取失败（如Token过期），返回null避免异常
            return null;
        }
    }

    /**
     * 获取用户类型
     */
    // 从登录ID中解析用户类型，支持多用户体系
    public static UserType getUserType() {
        // 获取当前登录ID字符串，格式为：userType:userId
        String loginType = StpUtil.getLoginIdAsString();
        // 调用UserType枚举的解析方法获取用户类型
        return UserType.getUserType(loginType);
    }

    /**
     * 是否为超级管理员
     *
     * @param userId 用户ID
     * @return 结果
     */
    // 判断指定用户ID是否为超级管理员
    // 超级管理员拥有系统最高权限，不受数据权限限制
    public static boolean isSuperAdmin(Long userId) {
        // 与系统常量中的超级管理员ID进行比较
        return SystemConstants.SUPER_ADMIN_ID.equals(userId);
    }

    /**
     * 是否为超级管理员
     *
     * @return 结果
     */
    // 判断当前登录用户是否为超级管理员
    public static boolean isSuperAdmin() {
        // 获取当前用户ID并调用重载方法判断
        return isSuperAdmin(getUserId());
    }

    /**
     * 是否为租户管理员
     *
     * @param rolePermission 角色权限标识组
     * @return 结果
     */
    // 判断指定角色权限集合是否包含租户管理员角色
    // 租户管理员拥有租户内的最高管理权限
    public static boolean isTenantAdmin(Set<String> rolePermission) {
        // 判断角色集合是否为空，空集合直接返回false
        if (CollUtil.isEmpty(rolePermission)) {
            return false;
        }
        // 判断角色集合中是否包含租户管理员角色标识
        return rolePermission.contains(TenantConstants.TENANT_ADMIN_ROLE_KEY);
    }

    /**
     * 是否为租户管理员
     *
     * @return 结果
     */
    // 判断当前登录用户是否为租户管理员
    public static boolean isTenantAdmin() {
        // 获取当前登录用户信息
        LoginUser loginUser = getLoginUser();
        // 如果用户未登录返回false
        if (loginUser == null) {
            return false;
        }
        // 获取用户的角色权限集合并调用重载方法判断
        return Convert.toBool(isTenantAdmin(loginUser.getRolePermission()));
    }

    /**
     * 检查当前用户是否已登录
     *
     * @return 结果
     */
    // 检查当前用户是否处于登录状态
    // 使用try-catch捕获Sa-Token的登录校验异常
    public static boolean isLogin() {
        try {
            // 调用Sa-Token的checkLogin方法，未登录会抛出异常
            StpUtil.checkLogin();
            // 如果没有异常说明已登录
            return true;
        } catch (Exception e) {
            // 捕获异常返回false，表示未登录
            return false;
        }
    }

}
