package org.dromara.web.service;

// Hutool BCrypt加密工具类，用于密码加密（单向哈希，支持盐值自动管理）
import cn.hutool.crypto.digest.BCrypt;
// MyBatis-Plus Lambda查询包装器，用于类型安全的数据库查询（避免硬编码字段名）
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
// Lombok注解：自动生成final字段的构造方法（依赖注入）
import lombok.RequiredArgsConstructor;
// 系统常量定义（如LOGIN_SUCCESS、LOGIN_FAIL、REGISTER等状态码）
import org.dromara.common.core.constant.Constants;
// 全局常量定义（如CAPTCHA_CODE_KEY验证码缓存key前缀）
import org.dromara.common.core.constant.GlobalConstants;
// 注册请求体（封装注册参数：用户名、密码、验证码、租户ID等）
import org.dromara.common.core.domain.model.RegisterBody;
// 用户类型枚举（SYS_ADMIN系统管理员、TENANT_ADMIN租户管理员、ORDINARY_USER普通用户）
import org.dromara.common.core.enums.UserType;
// 验证码异常类（验证码错误时抛出）
import org.dromara.common.core.exception.user.CaptchaException;
// 验证码过期异常类（验证码已过期时抛出）
import org.dromara.common.core.exception.user.CaptchaExpireException;
// 用户异常类（用户相关操作异常时抛出）
import org.dromara.common.core.exception.user.UserException;
// 核心工具类集合（消息工具、Servlet工具、Spring工具、字符串工具）
import org.dromara.common.core.utils.MessageUtils;
import org.dromara.common.core.utils.ServletUtils;
import org.dromara.common.core.utils.SpringUtils;
import org.dromara.common.core.utils.StringUtils;
// 登录信息事件，用于记录注册日志（事件驱动，异步处理）
import org.dromara.common.log.event.LogininforEvent;
// Redis操作工具类（封装Redisson，提供缓存操作API）
import org.dromara.common.redis.utils.RedisUtils;
// 租户助手工具类，用于租户上下文切换（动态数据源）
import org.dromara.common.tenant.helper.TenantHelper;
// 验证码配置属性类（读取验证码相关配置：开关、类型、长度等）
import org.dromara.common.web.config.properties.CaptchaProperties;
// 系统用户实体类（对应数据库sys_user表）
import org.dromara.system.domain.SysUser;
// 用户业务对象（用于业务层数据传输，包含验证逻辑）
import org.dromara.system.domain.bo.SysUserBo;
// 用户Mapper接口（MyBatis-Plus Mapper，提供数据库操作）
import org.dromara.system.mapper.SysUserMapper;
// 用户服务接口（定义用户相关业务方法）
import org.dromara.system.service.ISysUserService;
// Spring服务注解（标记为服务层组件，纳入Spring容器管理）
import org.springframework.stereotype.Service;

/**
 * 用户注册服务类
 * 处理用户注册业务逻辑，包括验证码校验、用户名唯一性检查、密码加密、用户创建等
 *
 * @author Lion Li
 */
// Lombok注解：自动生成final字段的构造方法（依赖注入）
@RequiredArgsConstructor
// Spring服务注解：标记为服务层组件，纳入Spring容器管理
@Service
public class SysRegisterService {

    // 注入用户服务接口（用于用户注册、查询等业务操作）
    private final ISysUserService userService;
    // 注入用户Mapper接口（用于数据库操作，如exists查询）
    private final SysUserMapper userMapper;
    // 注入验证码配置属性（读取验证码开关、过期时间等配置）
    private final CaptchaProperties captchaProperties;

    /**
     * 用户注册方法
     * 处理完整的用户注册流程：验证码校验 → 参数验证 → 用户名唯一性检查 → 密码加密 → 创建用户 → 记录日志
     *
     * @param registerBody 注册请求体（包含用户名、密码、验证码、租户ID等）
     */
    public void register(RegisterBody registerBody) {
        // 从注册请求体中获取租户ID（用于多租户数据隔离）
        String tenantId = registerBody.getTenantId();
        // 从注册请求体中获取用户名（登录账号）
        String username = registerBody.getUsername();
        // 从注册请求体中获取密码（明文，后续会加密）
        String password = registerBody.getPassword();
        // 校验用户类型是否有效，无效会抛出异常（UserType.getUserType会验证并返回枚举）
        String userType = UserType.getUserType(registerBody.getUserType()).getUserType();

        // 从配置获取验证码开关状态（true表示需要验证码验证）
        boolean captchaEnabled = captchaProperties.getEnable();
        // 如果验证码功能开启
        if (captchaEnabled) {
            // 调用验证码校验方法，验证用户输入的验证码是否正确
            // 参数：租户ID（日志用）、用户名（日志用）、验证码、UUID（用于定位Redis中的验证码）
            validateCaptcha(tenantId, username, registerBody.getCode(), registerBody.getUuid());
        }
        // 创建用户业务对象（用于封装用户注册信息）
        SysUserBo sysUser = new SysUserBo();
        // 设置用户名（登录账号）
        sysUser.setUserName(username);
        // 设置昵称（默认使用用户名作为昵称）
        sysUser.setNickName(username);
        // 使用BCrypt算法加密密码（单向哈希，自动加盐，安全性高）
        // BCrypt.hashpw会将密码加密为$2a$10$...格式的哈希值
        sysUser.setPassword(BCrypt.hashpw(password));
        // 设置用户类型（SYS_ADMIN/TENANT_ADMIN/ORDINARY_USER）
        sysUser.setUserType(userType);

        // 在指定租户下检查用户名是否已存在（多租户隔离）
        boolean exist = TenantHelper.dynamic(tenantId, () -> {
            // 使用MyBatis-Plus Lambda查询，检查用户名是否存在
            // LambdaQueryWrapper避免硬编码字段名，类型安全
            return userMapper.exists(new LambdaQueryWrapper<SysUser>()
                // 查询条件：user_name字段等于sysUser.getUserName()
                .eq(SysUser::getUserName, sysUser.getUserName()));
        });
        // 如果用户名已存在
        if (exist) {
            // 抛出用户异常，提示用户名已存在（国际化消息：user.register.save.error）
            throw new UserException("user.register.save.error", username);
        }
        // 调用用户服务执行注册（插入用户数据到sys_user表）
        boolean regFlag = userService.registerUser(sysUser, tenantId);
        // 如果注册失败（regFlag为false）
        if (!regFlag) {
            // 抛出用户异常，提示注册失败（国际化消息：user.register.error）
            throw new UserException("user.register.error");
        }
        // 记录注册成功日志（异步事件，解耦）
        // 参数：租户ID、用户名、状态（REGISTER）、消息（国际化后的成功提示）
        recordLogininfor(tenantId, username, Constants.REGISTER, MessageUtils.message("user.register.success"));
    }

    /**
     * 校验图形验证码
     * 从Redis获取验证码并比对，验证后立即删除（一次性使用，防止重放攻击）
     *
     * @param tenantId 租户ID（用于日志记录）
     * @param username 用户名（用于日志记录）
     * @param code     用户输入的验证码
     * @param uuid     唯一标识（用于定位Redis中的验证码）
     */
    public void validateCaptcha(String tenantId, String username, String code, String uuid) {
        // 构建Redis缓存key，格式：captcha_codes:UUID（如：captcha_codes:550e8400-e29b-41d4-a716-446655440000）
        // StringUtils.blankToDefault处理uuid为空的情况，避免NPE
        String verifyKey = GlobalConstants.CAPTCHA_CODE_KEY + StringUtils.blankToDefault(uuid, "");
        // 从Redis获取验证码（验证码生成时存入，有效期2分钟，见Constants.CAPTCHA_EXPIRATION）
        String captcha = RedisUtils.getCacheObject(verifyKey);
        // 删除Redis中的验证码，确保一次性使用（防止重放攻击：攻击者重复使用同一验证码）
        RedisUtils.deleteObject(verifyKey);
        // 验证码不存在（已过期或被使用）
        if (captcha == null) {
            // 记录登录失败日志（异步事件，记录到sys_logininfor表）
            // 参数：租户ID、用户名、状态（LOGIN_FAIL）、消息（国际化：验证码已过期）
            recordLogininfor(tenantId, username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.expire"));
            // 抛出验证码过期异常（前端捕获后提示"验证码已过期"）
            throw new CaptchaExpireException();
        }
        // 验证码不匹配（不区分大小写，提升用户体验）
        if (!StringUtils.equalsIgnoreCase(code, captcha)) {
            // 记录登录失败日志（异步事件）
            // 参数：租户ID、用户名、状态（LOGIN_FAIL）、消息（国际化：验证码错误）
            recordLogininfor(tenantId, username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.error"));
            // 抛出验证码错误异常（前端捕获后提示"验证码错误"）
            throw new CaptchaException();
        }
    }

    /**
     * 记录登录/注册信息
     * 发布异步事件，由监听器记录到数据库（sys_logininfor表）
     * 采用事件驱动模式，解耦业务逻辑和日志记录，提升性能
     *
     * @param tenantId 租户ID
     * @param username 用户名
     * @param status   状态（成功/失败）
     * @param message  消息内容（国际化后的提示）
     */
    private void recordLogininfor(String tenantId, String username, String status, String message) {
        // 创建登录信息事件对象（事件驱动，解耦）
        LogininforEvent logininforEvent = new LogininforEvent();
        // 设置租户ID（用于多租户数据隔离）
        logininforEvent.setTenantId(tenantId);
        // 设置用户名
        logininforEvent.setUsername(username);
        // 设置状态（Constants.LOGIN_SUCCESS / Constants.LOGIN_FAIL / Constants.REGISTER）
        logininforEvent.setStatus(status);
        // 设置消息内容（国际化后的提示信息）
        logininforEvent.setMessage(message);
        // 设置HTTP请求对象（用于获取IP地址、User-Agent、请求URL等）
        logininforEvent.setRequest(ServletUtils.getRequest());
        // 发布事件，由监听器异步处理（避免阻塞主线程，提升性能）
        // Spring事件机制：ApplicationEventPublisher发布事件，@EventListener监听处理
        SpringUtils.context().publishEvent(logininforEvent);
    }

}
