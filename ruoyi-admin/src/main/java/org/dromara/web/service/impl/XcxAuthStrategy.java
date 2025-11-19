package org.dromara.web.service.impl;

// Sa-Token工具类，用于登录认证相关操作
import cn.dev33.satoken.stp.StpUtil;
// Sa-Token登录参数对象，用于配置登录参数
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
// Hutool对象工具类
import cn.hutool.core.util.ObjectUtil;
// Lombok注解：自动生成final字段的构造方法
import lombok.RequiredArgsConstructor;
// Lombok注解：自动生成slf4j日志对象
import lombok.extern.slf4j.Slf4j;
// JustAuth配置类
import me.zhyd.oauth.config.AuthConfig;
// JustAuth回调对象
import me.zhyd.oauth.model.AuthCallback;
// JustAuth授权响应对象
import me.zhyd.oauth.model.AuthResponse;
// JustAuth授权令牌对象
import me.zhyd.oauth.model.AuthToken;
// JustAuth授权用户对象
import me.zhyd.oauth.model.AuthUser;
// JustAuth授权请求对象
import me.zhyd.oauth.request.AuthRequest;
// JustAuth微信小程序授权请求实现
import me.zhyd.oauth.request.AuthWechatMiniProgramRequest;
// 系统常量定义
import org.dromara.common.core.constant.SystemConstants;
// 小程序登录请求体
import org.dromara.common.core.domain.model.XcxLoginBody;
// 小程序登录用户模型（扩展了LoginUser）
import org.dromara.common.core.domain.model.XcxLoginUser;
// 业务异常类
import org.dromara.common.core.exception.ServiceException;
// 验证工具类
import org.dromara.common.core.utils.ValidatorUtils;
// JSON工具类
import org.dromara.common.json.utils.JsonUtils;
// Sa-Token登录助手工具类
import org.dromara.common.satoken.utils.LoginHelper;
// 客户端视图对象
import org.dromara.system.domain.vo.SysClientVo;
// 用户视图对象
import org.dromara.system.domain.vo.SysUserVo;
// 登录视图对象
import org.dromara.web.domain.vo.LoginVo;
// 认证策略接口
import org.dromara.web.service.IAuthStrategy;
// 登录服务
import org.dromara.web.service.SysLoginService;
// Spring服务注解
import org.springframework.stereotype.Service;

/**
 * 小程序认证策略
 *
 * @author Michelle.Chung
 */
// Lombok注解：自动生成slf4j日志对象
@Slf4j
// Spring服务注解：标记为服务层组件，Bean名称为xcxAuthStrategy
@Service("xcx" + IAuthStrategy.BASE_NAME)
// Lombok注解：自动生成final字段的构造方法
@RequiredArgsConstructor
public class XcxAuthStrategy implements IAuthStrategy {

    // 注入登录服务
    private final SysLoginService loginService;

    /**
     * 小程序登录实现
     *
     * @param body 登录请求体JSON字符串
     * @param client 客户端信息
     * @return 登录视图对象
     */
    @Override
    public LoginVo login(String body, SysClientVo client) {
        // 将JSON字符串解析为小程序登录请求体对象
        XcxLoginBody loginBody = JsonUtils.parseObject(body, XcxLoginBody.class);
        // 验证请求体参数合法性
        ValidatorUtils.validate(loginBody);
        // xcxCode为小程序调用wx.login授权后获取
        String xcxCode = loginBody.getXcxCode();
        // 多个小程序识别使用
        String appid = loginBody.getAppid();

        // 校验appid + appsrcret + xcxCode调用登录凭证校验接口获取session_key与openid
        // 构建微信小程序授权请求对象
        AuthRequest authRequest = new AuthWechatMiniProgramRequest(AuthConfig.builder()
            // 设置客户端ID为appid
            .clientId(appid)
            // 设置客户端密钥（此处应配置化，根据不同appid填入不同密钥）
            .clientSecret("自行填写密钥 可根据不同appid填入不同密钥")
            // 忽略回调URL校验
            .ignoreCheckRedirectUri(true)
            // 忽略state校验
            .ignoreCheckState(true).build());
        // 创建授权回调对象
        AuthCallback authCallback = new AuthCallback();
        // 设置授权码
        authCallback.setCode(xcxCode);
        // 执行授权登录
        AuthResponse<AuthUser> resp = authRequest.login(authCallback);
        // 定义openid和unionId变量
        String openid, unionId;
        // 判断授权是否成功
        if (resp.ok()) {
            // 获取授权令牌
            AuthToken token = resp.getData().getToken();
            // 获取openid（用户唯一标识）
            openid = token.getOpenId();
            // 微信小程序只有关联到微信开放平台下之后才能获取到unionId，因此unionId不一定能返回
            unionId = token.getUnionId();
        } else {
            // 授权失败，抛出业务异常
            throw new ServiceException(resp.getMsg());
        }
        // 框架登录不限制从什么表查询，只要最终构建出LoginUser即可
        // 根据openid查询绑定用户
        SysUserVo user = loadUserByOpenid(openid);
        // 此处可根据登录用户的数据不同自行创建loginUser，属性不够用继承扩展
        // 创建小程序登录用户对象（继承自LoginUser，额外包含openid）
        XcxLoginUser loginUser = new XcxLoginUser();
        // 设置租户ID
        loginUser.setTenantId(user.getTenantId());
        // 设置用户ID
        loginUser.setUserId(user.getUserId());
        // 设置用户名
        loginUser.setUsername(user.getUserName());
        // 设置昵称
        loginUser.setNickname(user.getNickName());
        // 设置用户类型
        loginUser.setUserType(user.getUserType());
        // 设置客户端标识
        loginUser.setClientKey(client.getClientKey());
        // 设置设备类型
        loginUser.setDeviceType(client.getDeviceType());
        // 设置openid
        loginUser.setOpenid(openid);

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
        // 设置openid，前端可能需要存储
        loginVo.setOpenid(openid);
        // 返回登录视图对象
        return loginVo;
    }

    /**
     * 根据openid加载用户信息
     *
     * @param openid 微信openid
     * @return 用户视图对象
     */
    private SysUserVo loadUserByOpenid(String openid) {
        // 使用openid查询绑定用户，如未绑定用户则根据业务自行处理，例如创建默认用户
        // todo 自行实现 userService.selectUserByOpenid(openid);
        // 创建空的用户视图对象（示例代码，实际应该查询数据库）
        SysUserVo user = new SysUserVo();
        // 用户不存在
        if (ObjectUtil.isNull(user)) {
            // 记录日志
            log.info("登录用户：{} 不存在.", openid);
            // todo 用户不存在，业务逻辑自行实现
            // 实际应该抛出异常或创建新用户
        } else if (SystemConstants.DISABLE.equals(user.getStatus())) {
            // 用户已停用
            log.info("登录用户：{} 已被停用.", openid);
            // todo 用户已被停用，业务逻辑自行实现
            // 实际应该抛出异常
        }
        // 返回用户视图对象
        return user;
    }

}
