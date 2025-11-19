package org.dromara.web.service.impl;

// Sa-Token工具类，用于登录认证相关操作
import cn.dev33.satoken.stp.StpUtil;
// Sa-Token登录参数对象，用于配置登录参数
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
// Hutool对象工具类
import cn.hutool.core.util.ObjectUtil;
// Hutool BCrypt加密工具类，用于密码验证
import cn.hutool.crypto.digest.BCrypt;
// MyBatis-Plus Lambda查询包装器，用于类型安全的数据库查询
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
// Lombok注解：自动生成final字段的构造方法
import lombok.RequiredArgsConstructor;
// Lombok注解：自动生成slf4j日志对象
import lombok.extern.slf4j.Slf4j;
// 系统常量定义
import org.dromara.common.core.constant.Constants;
// 全局常量定义
import org.dromara.common.core.constant.GlobalConstants;
// 系统常量定义
import org.dromara.common.core.constant.SystemConstants;
// 登录用户模型
import org.dromara.common.core.domain.model.LoginUser;
// 密码登录请求体
import org.dromara.common.core.domain.model.PasswordLoginBody;
// 登录类型枚举
import org.dromara.common.core.enums.LoginType;
// 验证码异常类
import org.dromara.common.core.exception.user.CaptchaException;
// 验证码过期异常类
import org.dromara.common.core.exception.user.CaptchaExpireException;
// 用户异常类
import org.dromara.common.core.exception.user.UserException;
// 核心工具类集合
import org.dromara.common.core.utils.MessageUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.core.utils.ValidatorUtils;
// JSON工具类
import org.dromara.common.json.utils.JsonUtils;
// Redis操作工具类
import org.dromara.common.redis.utils.RedisUtils;
// Sa-Token登录助手工具类
import org.dromara.common.satoken.utils.LoginHelper;
// 租户助手工具类，用于租户上下文切换
import org.dromara.common.tenant.helper.TenantHelper;
// 验证码配置属性类
import org.dromara.common.web.config.properties.CaptchaProperties;
// 系统用户实体类
import org.dromara.system.domain.SysUser;
// 客户端视图对象
import org.dromara.system.domain.vo.SysClientVo;
// 用户视图对象
import org.dromara.system.domain.vo.SysUserVo;
// 用户Mapper接口
import org.dromara.system.mapper.SysUserMapper;
// 登录视图对象
import org.dromara.web.domain.vo.LoginVo;
// 认证策略接口
import org.dromara.web.service.IAuthStrategy;
// 登录服务
import org.dromara.web.service.SysLoginService;
// Spring服务注解
import org.springframework.stereotype.Service;

/**
 * 密码认证策略
 *
 * @author Michelle.Chung
 */
// Lombok注解：自动生成slf4j日志对象
@Slf4j
// Spring服务注解：标记为服务层组件，Bean名称为passwordAuthStrategy
@Service("password" + IAuthStrategy.BASE_NAME)
// Lombok注解：自动生成final字段的构造方法
@RequiredArgsConstructor
public class PasswordAuthStrategy implements IAuthStrategy {

    // 注入验证码配置属性
    private final CaptchaProperties captchaProperties;
    // 注入登录服务
    private final SysLoginService loginService;
    // 注入用户Mapper接口
    private final SysUserMapper userMapper;

    /**
     * 密码登录实现
     *
     * @param body 登录请求体JSON字符串
     * @param client 客户端信息
     * @return 登录视图对象
     */
    @Override
    public LoginVo login(String body, SysClientVo client) {
        // 将JSON字符串解析为密码登录请求体对象
        PasswordLoginBody loginBody = JsonUtils.parseObject(body, PasswordLoginBody.class);
        // 验证请求体参数合法性
        ValidatorUtils.validate(loginBody);
        // 获取租户ID
        String tenantId = loginBody.getTenantId();
        // 获取用户名
        String username = loginBody.getUsername();
        // 获取密码
        String password = loginBody.getPassword();
        // 获取验证码
        String code = loginBody.getCode();
        // 获取验证码UUID
        String uuid = loginBody.getUuid();

        // 从配置获取验证码开关状态
        boolean captchaEnabled = captchaProperties.getEnable();
        // 验证码开关
        if (captchaEnabled) {
            // 校验验证码
            validateCaptcha(tenantId, username, code, uuid);
        }
        // 在指定租户下执行用户查询和验证
        LoginUser loginUser = TenantHelper.dynamic(tenantId, () -> {
            // 根据用户名加载用户信息
            SysUserVo user = loadUserByUsername(username);
            // 校验登录：检查密码是否正确，使用BCrypt验证密码
            // supplier返回true表示验证失败，触发错误计数
            loginService.checkLogin(LoginType.PASSWORD, tenantId, username, () -> !BCrypt.checkpw(password, user.getPassword()));
            // 构建登录用户对象
            // 此处可根据登录用户的数据不同自行创建loginUser，属性不够用继承扩展
            return loginService.buildLoginUser(user);
        });
        // 设置客户端标识
        loginUser.setClientKey(client.getClientKey());
        // 设置设备类型
        loginUser.setDeviceType(client.getDeviceType());
        // 创建Sa-Token登录参数对象
        SaLoginParameter model = new SaLoginParameter();
        // 设置设备类型
        model.setDeviceType(client.getDeviceType());
        // 自定义分配不同用户体系不同token授权时间，不设置默认走全局yml配置
        // 例如：后台用户30分钟过期，app用户1天过期
        // 设置token过期时间
        model.setTimeout(client.getTimeout());
        // 设置token活跃超时时间（长时间不操作自动过期）
        model.setActiveTimeout(client.getActiveTimeout());
        // 设置额外数据：客户端ID
        model.setExtra(LoginHelper.CLIENT_KEY, client.getClientId());
        // 执行登录，生成token
        LoginHelper.login(loginUser, model);

        // 创建登录视图对象
        LoginVo loginVo = new LoginVo();
        // 获取token值
        loginVo.setAccessToken(StpUtil.getTokenValue());
        // 获取token过期时间
        loginVo.setExpireIn(StpUtil.getTokenTimeout());
        // 设置客户端ID
        loginVo.setClientId(client.getClientId());
        // 返回登录视图对象
        return loginVo;
    }

    /**
     * 校验验证码
     *
     * @param username 用户名
     * @param code     验证码
     * @param uuid     唯一标识
     */
    private void validateCaptcha(String tenantId, String username, String code, String uuid) {
        // 构建Redis缓存key，格式：captcha_codes:UUID
        String verifyKey = GlobalConstants.CAPTCHA_CODE_KEY + StringUtils.blankToDefault(uuid, "");
        // 从Redis获取验证码
        String captcha = RedisUtils.getCacheObject(verifyKey);
        // 删除Redis中的验证码，确保一次性使用
        RedisUtils.deleteObject(verifyKey);
        // 验证码不存在（已过期或被使用）
        if (captcha == null) {
            // 记录登录失败日志
            loginService.recordLogininfor(tenantId, username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.expire"));
            // 抛出验证码过期异常
            throw new CaptchaExpireException();
        }
        // 验证码不匹配（不区分大小写）
        if (!StringUtils.equalsIgnoreCase(code, captcha)) {
            // 记录登录失败日志
            loginService.recordLogininfor(tenantId, username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.error"));
            // 抛出验证码错误异常
            throw new CaptchaException();
        }
    }

    /**
     * 根据用户名加载用户信息
     *
     * @param username 用户名
     * @return 用户视图对象
     */
    private SysUserVo loadUserByUsername(String username) {
        // 使用MyBatis-Plus Lambda查询，根据用户名查询用户
        SysUserVo user = userMapper.selectVoOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUserName, username));
        // 用户不存在
        if (ObjectUtil.isNull(user)) {
            // 记录日志
            log.info("登录用户：{} 不存在.", username);
            // 抛出用户异常
            throw new UserException("user.not.exists", username);
        } else if (SystemConstants.DISABLE.equals(user.getStatus())) {
            // 用户已停用
            log.info("登录用户：{} 已被停用.", username);
            // 抛出用户异常
            throw new UserException("user.blocked", username);
        }
        // 返回用户视图对象
        return user;
    }

}
