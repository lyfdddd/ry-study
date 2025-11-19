// 定义登录服务所在的包路径，属于web层的service模块
package org.dromara.web.service;

// Sa-Token未登录异常类，当用户未登录或Token过期时抛出
import cn.dev33.satoken.exception.NotLoginException;
// Sa-Token工具类，用于登录认证相关操作（登录、登出、权限校验等）
import cn.dev33.satoken.stp.StpUtil;
// Hutool Bean工具类，用于对象属性拷贝（浅拷贝）
import cn.hutool.core.bean.BeanUtil;
// Hutool集合工具类，提供集合判空、转换等操作
import cn.hutool.core.collection.CollUtil;
// Hutool Optional工具类，用于优雅处理空值（避免NPE）
import cn.hutool.core.lang.Opt;
// Hutool对象工具类，提供空值判断、默认值设置等
import cn.hutool.core.util.ObjectUtil;
// Redisson分布式锁注解，确保方法执行的原子性（防止并发问题）
import com.baomidou.lock.annotation.Lock4j;
// Lombok注解：自动生成final字段的构造方法（依赖注入）
import lombok.RequiredArgsConstructor;
// Lombok注解：自动生成slf4j日志对象
import lombok.extern.slf4j.Slf4j;
// JustAuth第三方授权用户对象，封装第三方平台用户信息
import me.zhyd.oauth.model.AuthUser;
// 缓存常量定义（Redis Key前缀）
import org.dromara.common.core.constant.CacheConstants;
// 系统常量定义（登录状态、用户状态等）
import org.dromara.common.core.constant.Constants;
// 系统常量定义（超级管理员ID等）
import org.dromara.common.core.constant.SystemConstants;
// 租户常量定义（默认租户ID等）
import org.dromara.common.core.constant.TenantConstants;
// 岗位数据传输对象（DTO）
import org.dromara.common.core.domain.dto.PostDTO;
// 角色数据传输对象（DTO）
import org.dromara.common.core.domain.dto.RoleDTO;
// 登录用户模型，存储在Sa-Token Session中
import org.dromara.common.core.domain.model.LoginUser;
// 登录类型枚举（密码、短信、邮箱等）
import org.dromara.common.core.enums.LoginType;
// 业务异常类
import org.dromara.common.core.exception.ServiceException;
// 用户相关异常类（密码错误、账号锁定等）
import org.dromara.common.core.exception.user.UserException;
// 核心工具类集合（ServletUtils、DateUtils等）
import org.dromara.common.core.utils.*;
// 登录信息事件，用于记录登录日志（异步解耦）
import org.dromara.common.log.event.LogininforEvent;
// MyBatis-Plus数据权限助手，用于临时忽略数据权限
import org.dromara.common.mybatis.helper.DataPermissionHelper;
// Redis操作工具类（缓存、分布式锁等）
import org.dromara.common.redis.utils.RedisUtils;
// Sa-Token登录助手工具类（获取当前用户、判断超级管理员等）
import org.dromara.common.satoken.utils.LoginHelper;
// 租户异常类（租户不存在、已停用等）
import org.dromara.common.tenant.exception.TenantException;
// 租户助手工具类，用于租户上下文切换
import org.dromara.common.tenant.helper.TenantHelper;
// 系统用户实体类（对应数据库表）
import org.dromara.system.domain.SysUser;
// 社交绑定业务对象（BO）
import org.dromara.system.domain.bo.SysSocialBo;
// 系统视图对象集合（VO）
import org.dromara.system.domain.vo.*;
// 用户Mapper接口（MyBatis-Plus）
import org.dromara.system.mapper.SysUserMapper;
// 系统服务接口集合（用户、角色、部门等）
import org.dromara.system.service.*;
// Spring值注入注解（从配置文件读取配置）
import org.springframework.beans.factory.annotation.Value;
// Spring服务注解（标记为服务层组件）
import org.springframework.stereotype.Service;

// Java时间Duration类（用于设置Redis过期时间）
import java.time.Duration;
// Java日期类
import java.util.Date;
// Java列表接口
import java.util.List;
// Java函数式接口Supplier（用于延迟执行）
import java.util.function.Supplier;

/**
 * 登录校验服务
 * 提供用户登录、登出、密码重试限制、租户校验等核心功能
 *
 * @author Lion Li
 */
// Lombok注解：自动生成final字段的构造方法（依赖注入）
@RequiredArgsConstructor
// Lombok注解：自动生成slf4j日志对象
@Slf4j
// Spring服务注解：标记为服务层组件
@Service
public class SysLoginService {

    // 从配置文件读取密码最大重试次数（默认5次）
    @Value("${user.password.maxRetryCount}")
    private Integer maxRetryCount;

    // 从配置文件读取密码锁定时间（分钟，默认10分钟）
    @Value("${user.password.lockTime}")
    private Integer lockTime;

    // 租户服务接口，用于租户相关操作（查询、校验等）
    private final ISysTenantService tenantService;
    // 权限服务接口，用于获取用户权限（菜单、角色等）
    private final ISysPermissionService permissionService;
    // 社交用户服务接口，用于第三方账号绑定（微信、QQ等）
    private final ISysSocialService sysSocialService;
    // 角色服务接口，用于查询用户角色
    private final ISysRoleService roleService;
    // 部门服务接口，用于查询用户部门
    private final ISysDeptService deptService;
    // 岗位服务接口，用于查询用户岗位
    private final ISysPostService postService;
    // 用户Mapper接口，用于数据库操作（MyBatis-Plus）
    private final SysUserMapper userMapper;


    /**
     * 绑定第三方用户
     * 将第三方平台账号（微信、QQ等）绑定到系统用户
     *
     * @param authUserData 授权响应实体（包含第三方用户信息）
     */
    // Redisson分布式锁注解：确保同一时间只有一个线程可以执行此方法
    // 防止并发情况下重复绑定同一第三方账号（幂等性保证）
    @Lock4j
    public void socialRegister(AuthUser authUserData) {
        // 构建第三方账号唯一标识：平台+UUID（如：WECHAT_OPEN123456）
        String authId = authUserData.getSource() + authUserData.getUuid();
        // 将第三方授权数据转换为业务对象（BO）
        SysSocialBo bo = BeanUtil.toBean(authUserData, SysSocialBo.class);
        // 拷贝Token信息到业务对象（accessToken、expireIn等）
        BeanUtil.copyProperties(authUserData.getToken(), bo);
        // 获取当前登录用户ID（从Sa-Token Session）
        Long userId = LoginHelper.getUserId();
        bo.setUserId(userId);
        bo.setAuthId(authId);
        bo.setOpenId(authUserData.getUuid());
        bo.setUserName(authUserData.getUsername());
        bo.setNickName(authUserData.getNickname());
        // 查询该第三方账号是否已被绑定（防止重复绑定）
        List<SysSocialVo> checkList = sysSocialService.selectByAuthId(authId);
        if (CollUtil.isNotEmpty(checkList)) {
            // 已被绑定，抛出异常
            throw new ServiceException("此三方账号已经被绑定!");
        }
        // 查询当前用户是否已绑定该平台的其他账号（一个平台只能绑定一个账号）
        SysSocialBo params = new SysSocialBo();
        params.setUserId(userId);
        params.setSource(bo.getSource());
        List<SysSocialVo> list = sysSocialService.queryList(params);
        if (CollUtil.isEmpty(list)) {
            // 没有绑定记录，新增绑定信息
            sysSocialService.insertByBo(bo);
        } else {
            // 已存在绑定记录，更新信息（覆盖绑定）
            bo.setId(list.get(0).getId());
            sysSocialService.updateByBo(bo);
            // 如果要绑定的平台账号已经被绑定过了 是否抛异常自行决断
            // throw new ServiceException("此平台账号已经被绑定!");
        }
    }


    /**
     * 退出登录
     * 清除用户登录状态、记录登出日志
     */
    public void logout() {
        try {
            // 获取当前登录用户信息（从Sa-Token Session）
            LoginUser loginUser = LoginHelper.getLoginUser();
            // 如果用户未登录，直接返回
            if (ObjectUtil.isNull(loginUser)) {
                return;
            }
            // 如果开启租户功能且当前用户是超级管理员
            if (TenantHelper.isEnable() && LoginHelper.isSuperAdmin()) {
                // 超级管理员登出时清除动态租户缓存（防止影响下次登录）
                TenantHelper.clearDynamic();
            }
            // 记录登出日志（异步事件）
            recordLogininfor(loginUser.getTenantId(), loginUser.getUsername(), Constants.LOGOUT, MessageUtils.message("user.logout.success"));
        } catch (NotLoginException ignored) {
            // 忽略未登录异常（用户可能已过期）
        } finally {
            try {
                // 执行Sa-Token登出操作，清除Token、Session
                StpUtil.logout();
            } catch (NotLoginException ignored) {
                // 忽略未登录异常
            }
        }
    }

    /**
     * 记录登录信息
     * 发布异步事件，由监听器记录到数据库
     *
     * @param tenantId 租户ID
     * @param username 用户名
     * @param status   状态（成功/失败）
     * @param message  消息内容
     */
    public void recordLogininfor(String tenantId, String username, String status, String message) {
        // 创建登录信息事件对象
        LogininforEvent logininforEvent = new LogininforEvent();
        // 设置租户ID
        logininforEvent.setTenantId(tenantId);
        // 设置用户名
        logininforEvent.setUsername(username);
        // 设置登录状态
        logininforEvent.setStatus(status);
        // 设置消息内容
        logininforEvent.setMessage(message);
        // 设置当前HTTP请求对象，用于获取IP、UserAgent等信息
        logininforEvent.setRequest(ServletUtils.getRequest());
        // 发布事件，由监听器异步处理日志记录（解耦，提升性能）
        SpringUtils.context().publishEvent(logininforEvent);
    }

    /**
     * 构建登录用户
     * 从数据库查询用户完整信息（角色、岗位、部门、权限等），组装成LoginUser对象
     *
     * @param user 用户视图对象（包含基本信息）
     * @return 登录用户对象（包含完整上下文）
     */
    public LoginUser buildLoginUser(SysUserVo user) {
        // 创建登录用户对象
        LoginUser loginUser = new LoginUser();
        // 获取用户ID
        Long userId = user.getUserId();
        // 设置租户ID
        loginUser.setTenantId(user.getTenantId());
        // 设置用户ID
        loginUser.setUserId(userId);
        // 设置部门ID
        loginUser.setDeptId(user.getDeptId());
        // 设置用户名
        loginUser.setUsername(user.getUserName());
        // 设置昵称
        loginUser.setNickname(user.getNickName());
        // 设置用户类型
        loginUser.setUserType(user.getUserType());
        // 获取并设置菜单权限集合（用于前端按钮级权限控制）
        loginUser.setMenuPermission(permissionService.getMenuPermission(userId));
        // 获取并设置角色权限集合（用于后端接口鉴权）
        loginUser.setRolePermission(permissionService.getRolePermission(userId));
        // 如果用户有部门ID
        if (ObjectUtil.isNotNull(user.getDeptId())) {
            // 查询部门信息，使用Opt避免空指针（如果部门被删除，不会NPE）
            Opt<SysDeptVo> deptOpt = Opt.of(user.getDeptId()).map(deptService::selectDeptById);
            // 设置部门名称，如果查询不到则设置为空字符串
            loginUser.setDeptName(deptOpt.map(SysDeptVo::getDeptName).orElse(StringUtils.EMPTY));
            // 设置部门分类
            loginUser.setDeptCategory(deptOpt.map(SysDeptVo::getDeptCategory).orElse(StringUtils.EMPTY));
        }
        // 查询用户角色列表
        List<SysRoleVo> roles = roleService.selectRolesByUserId(userId);
        // 查询用户岗位列表
        List<SysPostVo> posts = postService.selectPostsByUserId(userId);
        // 将角色列表转换为DTO列表并设置（使用MapStruct性能更好）
        loginUser.setRoles(BeanUtil.copyToList(roles, RoleDTO.class));
        // 将岗位列表转换为DTO列表并设置
        loginUser.setPosts(BeanUtil.copyToList(posts, PostDTO.class));
        // 返回构建完成的登录用户对象
        return loginUser;
    }

    /**
     * 记录登录信息（更新用户表）
     * 更新用户的登录IP和登录时间
     *
     * @param userId 用户ID
     * @param ip     登录IP地址
     */
    public void recordLoginInfo(Long userId, String ip) {
        // 创建用户对象，用于更新登录信息
        SysUser sysUser = new SysUser();
        // 设置用户ID
        sysUser.setUserId(userId);
        // 设置登录IP
        sysUser.setLoginIp(ip);
        // 设置登录时间（当前时间）
        sysUser.setLoginDate(DateUtils.getNowDate());
        // 设置更新人
        sysUser.setUpdateBy(userId);
        // 使用数据权限助手临时忽略数据权限，确保能更新自己的信息
        // 避免数据权限导致无法更新自己的登录信息（如只能看本部门数据）
        DataPermissionHelper.ignore(() -> userMapper.updateById(sysUser));
    }

    /**
     * 登录校验（密码重试限制）
     * 基于Redis实现密码错误次数统计和锁定机制
     *
     * @param loginType 登录类型（密码、短信、邮箱等）
     * @param tenantId  租户ID
     * @param username  用户名
     * @param supplier  密码验证逻辑（返回true表示验证失败）
     */
    public void checkLogin(LoginType loginType, String tenantId, String username, Supplier<Boolean> supplier) {
        // 构建Redis缓存key，格式：pwd_err_cnt:用户名（如：pwd_err_cnt:admin）
        String errorKey = CacheConstants.PWD_ERR_CNT_KEY + username;
        // 登录失败常量
        String loginFail = Constants.LOGIN_FAIL;

        // 获取用户登录错误次数，默认为0 (可自定义限制策略 例如: key + username + ip)
        // 从Redis获取错误次数，如果为空则默认为0
        int errorNumber = ObjectUtil.defaultIfNull(RedisUtils.getCacheObject(errorKey), 0);
        // 锁定时间内登录则踢出
        // 如果错误次数达到最大重试次数
        if (errorNumber >= maxRetryCount) {
            // 记录登录失败日志（异步事件）
            recordLogininfor(tenantId, username, loginFail, MessageUtils.message(loginType.getRetryLimitExceed(), maxRetryCount, lockTime));
            // 抛出用户异常，提示超过重试限制
            throw new UserException(loginType.getRetryLimitExceed(), maxRetryCount, lockTime);
        }

        // 执行密码验证，supplier.get()返回true表示验证失败
        if (supplier.get()) {
            // 错误次数递增
            errorNumber++;
            // 将新的错误次数存入Redis，设置锁定时间（过期后自动解锁）
            RedisUtils.setCacheObject(errorKey, errorNumber, Duration.ofMinutes(lockTime));
            // 达到规定错误次数则锁定登录
            if (errorNumber >= maxRetryCount) {
                // 记录登录失败日志
                recordLogininfor(tenantId, username, loginFail, MessageUtils.message(loginType.getRetryLimitExceed(), maxRetryCount, lockTime));
                // 抛出用户异常，提示超过重试限制
                throw new UserException(loginType.getRetryLimitExceed(), maxRetryCount, lockTime);
            } else {
                // 未达到规定错误次数
                // 记录登录失败日志，提示剩余次数
                recordLogininfor(tenantId, username, loginFail, MessageUtils.message(loginType.getRetryLimitCount(), errorNumber));
                // 抛出用户异常，提示当前错误次数
                throw new UserException(loginType.getRetryLimitCount(), errorNumber);
            }
        }

        // 登录成功，清空错误次数（删除Redis key）
        RedisUtils.deleteObject(errorKey);
    }

    /**
     * 校验租户
     * 检查租户是否存在、是否启用、是否过期
     *
     * @param tenantId 租户ID
     */
    public void checkTenant(String tenantId) {
        // 如果未开启租户功能，直接返回
        if (!TenantHelper.isEnable()) {
            return;
        }
        // 租户ID不能为空
        if (StringUtils.isBlank(tenantId)) {
            throw new TenantException("tenant.number.not.blank");
        }
        // 默认租户ID直接返回（超级管理员租户，000000）
        if (TenantConstants.DEFAULT_TENANT_ID.equals(tenantId)) {
            return;
        }
        // 查询租户信息（从数据库或缓存）
        SysTenantVo tenant = tenantService.queryByTenantId(tenantId);
        // 租户不存在
        if (ObjectUtil.isNull(tenant)) {
            // 记录日志
            log.info("登录租户：{} 不存在.", tenantId);
            // 抛出租户异常
            throw new TenantException("tenant.not.exists");
        } else if (SystemConstants.DISABLE.equals(tenant.getStatus())) {
            // 租户已停用
            log.info("登录租户：{} 已被停用.", tenantId);
            // 抛出租户异常
            throw new TenantException("tenant.blocked");
        } else if (ObjectUtil.isNotNull(tenant.getExpireTime())
            && new Date().after(tenant.getExpireTime())) {
            // 租户已过期
            log.info("登录租户：{} 已超过有效期.", tenantId);
            // 抛出租户异常
            throw new TenantException("tenant.expired");
        }
    }

}
