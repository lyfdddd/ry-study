package org.dromara.web.service.impl;

// Sa-Token工具类，用于登录认证相关操作
import cn.dev33.satoken.stp.StpUtil;
// Sa-Token登录参数对象，用于配置登录参数
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
// Hutool集合工具类
import cn.hutool.core.collection.CollUtil;
// Hutool对象工具类
import cn.hutool.core.util.ObjectUtil;
// Lombok注解：自动生成final字段的构造方法
import lombok.RequiredArgsConstructor;
// Lombok注解：自动生成slf4j日志对象
import lombok.extern.slf4j.Slf4j;
// JustAuth授权响应对象
import me.zhyd.oauth.model.AuthResponse;
// JustAuth授权用户对象
import me.zhyd.oauth.model.AuthUser;
// 系统常量定义
import org.dromara.common.core.constant.SystemConstants;
// 登录用户模型
import org.dromara.common.core.domain.model.LoginUser;
// 社交登录请求体
import org.dromara.common.core.domain.model.SocialLoginBody;
// 业务异常类
import org.dromara.common.core.exception.ServiceException;
// 用户异常类
import org.dromara.common.core.exception.user.UserException;
// Stream工具类
import org.dromara.common.core.utils.StreamUtils;
// 验证工具类
import org.dromara.common.core.utils.ValidatorUtils;
// JSON工具类
import org.dromara.common.json.utils.JsonUtils;
// Sa-Token登录助手工具类
import org.dromara.common.satoken.utils.LoginHelper;
// 社交登录配置属性
import org.dromara.common.social.config.properties.SocialProperties;
// 社交工具类
import org.dromara.common.social.utils.SocialUtils;
// 租户助手工具类，用于租户上下文切换
import org.dromara.common.tenant.helper.TenantHelper;
// 客户端视图对象
import org.dromara.system.domain.vo.SysClientVo;
// 社交用户视图对象
import org.dromara.system.domain.vo.SysSocialVo;
// 用户视图对象
import org.dromara.system.domain.vo.SysUserVo;
// 用户Mapper接口
import org.dromara.system.mapper.SysUserMapper;
// 社交用户服务接口
import org.dromara.system.service.ISysSocialService;
// 登录视图对象
import org.dromara.web.domain.vo.LoginVo;
// 认证策略接口
import org.dromara.web.service.IAuthStrategy;
// 登录服务
import org.dromara.web.service.SysLoginService;
// Spring服务注解
import org.springframework.stereotype.Service;

// Java List接口
import java.util.List;
// Java Optional类，用于优雅处理空值
import java.util.Optional;

/**
 * 第三方授权策略
 *
 * @author thiszhc is 三三
 */
// Lombok注解：自动生成slf4j日志对象
@Slf4j
// Spring服务注解：标记为服务层组件，Bean名称为socialAuthStrategy
@Service("social" + IAuthStrategy.BASE_NAME)
// Lombok注解：自动生成final字段的构造方法
@RequiredArgsConstructor
public class SocialAuthStrategy implements IAuthStrategy {

    // 注入社交登录配置属性
    private final SocialProperties socialProperties;
    // 注入社交用户服务接口
    private final ISysSocialService sysSocialService;
    // 注入用户Mapper接口
    private final SysUserMapper userMapper;
    // 注入登录服务
    private final SysLoginService loginService;

    /**
     * 登录-第三方授权登录
     *
     * @param body     登录信息
     * @param client   客户端信息
     */
    @Override
    public LoginVo login(String body, SysClientVo client) {
        // 将JSON字符串解析为社交登录请求体对象
        SocialLoginBody loginBody = JsonUtils.parseObject(body, SocialLoginBody.class);
        // 验证请求体参数合法性
        ValidatorUtils.validate(loginBody);
        // 执行第三方授权登录，获取授权响应
        AuthResponse<AuthUser> response = SocialUtils.loginAuth(
                loginBody.getSource(), loginBody.getSocialCode(),
                loginBody.getSocialState(), socialProperties);
        // 判断授权响应是否成功
        if (!response.ok()) {
            // 授权失败，抛出业务异常
            throw new ServiceException(response.getMsg());
        }
        // 获取授权用户数据
        AuthUser authUserData = response.getData();

        // 构建第三方账号唯一标识：平台+UUID
        String authId = authUserData.getSource() + authUserData.getUuid();
        // 查询该第三方账号的绑定记录
        List<SysSocialVo> list = sysSocialService.selectByAuthId(authId);
        // 如果没有绑定记录
        if (CollUtil.isEmpty(list)) {
            // 抛出异常，提示用户先绑定账号
            throw new ServiceException("你还没有绑定第三方账号，绑定后才可以登录！");
        }
        // 社交绑定对象
        SysSocialVo social;
        // 如果开启租户功能
        if (TenantHelper.isEnable()) {
            // 在绑定记录中查找当前租户的记录
            Optional<SysSocialVo> opt = StreamUtils.findAny(list, x -> x.getTenantId().equals(loginBody.getTenantId()));
            // 没有找到当前租户的绑定记录
            if (opt.isEmpty()) {
                // 抛出异常，提示无权限登录当前租户
                throw new ServiceException("对不起，你没有权限登录当前租户！");
            }
            // 获取当前租户的绑定记录
            social = opt.get();
        } else {
            // 未开启租户功能，直接使用第一条绑定记录
            social = list.get(0);
        }
        // 在指定租户下执行用户查询和构建
        LoginUser loginUser = TenantHelper.dynamic(social.getTenantId(), () -> {
            // 根据用户ID加载用户信息
            SysUserVo user = loadUser(social.getUserId());
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
     * 根据用户ID加载用户信息
     *
     * @param userId 用户ID
     * @return 用户视图对象
     */
    private SysUserVo loadUser(Long userId) {
        // 使用MyBatis-Plus查询用户视图对象
        SysUserVo user = userMapper.selectVoById(userId);
        // 用户不存在
        if (ObjectUtil.isNull(user)) {
            // 记录日志
            log.info("登录用户：{} 不存在.", "");
            // 抛出用户异常
            throw new UserException("user.not.exists", "");
        } else if (SystemConstants.DISABLE.equals(user.getStatus())) {
            // 用户已停用
            log.info("登录用户：{} 已被停用.", "");
            // 抛出用户异常
            throw new UserException("user.blocked", "");
        }
        // 返回用户视图对象
        return user;
    }

}
