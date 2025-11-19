package org.dromara.web.controller;

// Sa-Token注解：标记此接口无需认证即可访问
import cn.dev33.satoken.annotation.SaIgnore;
// Sa-Token未登录异常类
import cn.dev33.satoken.exception.NotLoginException;
// Sa-Token工具类，用于登录认证相关操作
import cn.dev33.satoken.stp.StpUtil;
// Hutool Base64编码工具
import cn.hutool.core.codec.Base64;
// Hutool集合工具类
import cn.hutool.core.collection.CollUtil;
// Hutool对象工具类
import cn.hutool.core.util.ObjectUtil;
// Jakarta Servlet请求接口
import jakarta.servlet.http.HttpServletRequest;
// Lombok注解：自动生成final字段的构造方法
import lombok.RequiredArgsConstructor;
// Lombok注解：自动生成slf4j日志对象
import lombok.extern.slf4j.Slf4j;
// JustAuth授权响应对象
import me.zhyd.oauth.model.AuthResponse;
// JustAuth授权用户对象
import me.zhyd.oauth.model.AuthUser;
// JustAuth授权请求对象
import me.zhyd.oauth.request.AuthRequest;
// JustAuth状态工具类，用于生成随机状态码
import me.zhyd.oauth.utils.AuthStateUtils;
// 系统常量定义
import org.dromara.common.core.constant.SystemConstants;
// 统一响应结果封装类
import org.dromara.common.core.domain.R;
// 登录请求体
import org.dromara.common.core.domain.model.LoginBody;
// 注册请求体
import org.dromara.common.core.domain.model.RegisterBody;
// 社交登录请求体
import org.dromara.common.core.domain.model.SocialLoginBody;
// 核心工具类集合
import org.dromara.common.core.utils.*;
// API加密注解，用于请求响应加密
import org.dromara.common.encrypt.annotation.ApiEncrypt;
// JSON工具类
import org.dromara.common.json.utils.JsonUtils;
// 限流注解：限制接口访问频率
import org.dromara.common.ratelimiter.annotation.RateLimiter;
// 限流类型枚举
import org.dromara.common.ratelimiter.enums.LimitType;
// Sa-Token登录助手工具类
import org.dromara.common.satoken.utils.LoginHelper;
// 社交登录配置属性
import org.dromara.common.social.config.properties.SocialLoginConfigProperties;
import org.dromara.common.social.config.properties.SocialProperties;
// 社交工具类
import org.dromara.common.social.utils.SocialUtils;
// SSE消息DTO
import org.dromara.common.sse.dto.SseMessageDto;
// SSE消息工具类
import org.dromara.common.sse.utils.SseMessageUtils;
// 租户助手工具类
import org.dromara.common.tenant.helper.TenantHelper;
// 租户业务对象
import org.dromara.system.domain.bo.SysTenantBo;
// 客户端视图对象
import org.dromara.system.domain.vo.SysClientVo;
// 租户视图对象
import org.dromara.system.domain.vo.SysTenantVo;
// 客户端服务接口
import org.dromara.system.service.ISysClientService;
// 配置服务接口
import org.dromara.system.service.ISysConfigService;
// 社交用户服务接口
import org.dromara.system.service.ISysSocialService;
// 租户服务接口
import org.dromara.system.service.ISysTenantService;
// 登录租户视图对象
import org.dromara.web.domain.vo.LoginTenantVo;
// 登录视图对象
import org.dromara.web.domain.vo.LoginVo;
// 租户列表视图对象
import org.dromara.web.domain.vo.TenantListVo;
// 认证策略接口
import org.dromara.web.service.IAuthStrategy;
// 登录服务
import org.dromara.web.service.SysLoginService;
import org.dromara.web.service.SysRegisterService;
// Spring验证注解
import org.springframework.validation.annotation.Validated;
// Spring Web注解
import org.springframework.web.bind.annotation.*;

// Java网络URL类
import java.net.URL;
// Java字符集类
import java.nio.charset.StandardCharsets;
// Java HashMap
import java.util.HashMap;
// Java List接口
import java.util.List;
// Java Map接口
import java.util.Map;
// Java定时线程池
import java.util.concurrent.ScheduledExecutorService;
// Java时间单位枚举
import java.util.concurrent.TimeUnit;

/**
 * 认证
 *
 * @author Lion Li
 */
// Lombok注解：自动生成slf4j日志对象
@Slf4j
// Sa-Token注解：标记此控制器所有接口无需认证即可访问
@SaIgnore
// Lombok注解：自动生成final字段的构造方法
@RequiredArgsConstructor
// Spring注解：标记为REST控制器，返回JSON数据
@RestController
// 请求映射前缀
@RequestMapping("/auth")
public class AuthController {

    // 注入社交登录配置属性
    private final SocialProperties socialProperties;
    // 注入登录服务
    private final SysLoginService loginService;
    // 注入注册服务
    private final SysRegisterService registerService;
    // 注入配置服务
    private final ISysConfigService configService;
    // 注入租户服务
    private final ISysTenantService tenantService;
    // 注入社交用户服务
    private final ISysSocialService socialUserService;
    // 注入客户端服务
    private final ISysClientService clientService;
    // 注入定时任务线程池，用于延迟发送SSE消息
    private final ScheduledExecutorService scheduledExecutorService;


    /**
     * 登录方法
     *
     * @param body 登录信息
     * @return 结果
     */
    // API加密注解：对请求和响应进行加密解密
    @ApiEncrypt
    // POST请求映射：/auth/login
    @PostMapping("/login")
    public R<LoginVo> login(@RequestBody String body) {
        // 将JSON字符串解析为登录请求体对象
        LoginBody loginBody = JsonUtils.parseObject(body, LoginBody.class);
        // 验证请求体参数合法性
        ValidatorUtils.validate(loginBody);
        // 获取客户端ID和授权类型
        String clientId = loginBody.getClientId();
        String grantType = loginBody.getGrantType();
        // 查询客户端信息
        SysClientVo client = clientService.queryByClientId(clientId);
        // 查询不到client或client内不包含grantType
        if (ObjectUtil.isNull(client) || !StringUtils.contains(client.getGrantType(), grantType)) {
            // 记录异常日志
            log.info("客户端id: {} 认证类型：{} 异常!.", clientId, grantType);
            // 返回错误响应
            return R.fail(MessageUtils.message("auth.grant.type.error"));
        } else if (!SystemConstants.NORMAL.equals(client.getStatus())) {
            // 客户端状态不正常
            return R.fail(MessageUtils.message("auth.grant.type.blocked"));
        }
        // 校验租户
        loginService.checkTenant(loginBody.getTenantId());
        // 执行登录，通过策略模式获取对应认证策略并登录
        LoginVo loginVo = IAuthStrategy.login(body, client, grantType);

        // 获取用户ID
        Long userId = LoginHelper.getUserId();
        // 延迟5秒发送SSE欢迎消息
        scheduledExecutorService.schedule(() -> {
            // 创建SSE消息对象
            SseMessageDto dto = new SseMessageDto();
            // 设置消息内容
            dto.setMessage("欢迎登录RuoYi-Vue-Plus后台管理系统");
            // 设置接收用户ID列表
            dto.setUserIds(List.of(userId));
            // 发布消息
            SseMessageUtils.publishMessage(dto);
        }, 5, TimeUnit.SECONDS);
        // 返回登录成功响应
        return R.ok(loginVo);
    }

    /**
     * 获取跳转URL
     *
     * @param source 登录来源
     * @return 结果
     */
    // GET请求映射：/auth/binding/{source}
    @GetMapping("/binding/{source}")
    public R<String> authBinding(@PathVariable("source") String source,
                                 @RequestParam String tenantId, @RequestParam String domain) {
        // 获取指定平台的社交登录配置
        SocialLoginConfigProperties obj = socialProperties.getType().get(source);
        // 配置不存在
        if (ObjectUtil.isNull(obj)) {
            // 返回错误信息
            return R.fail(source + "平台账号暂不支持");
        }
        // 构建授权请求对象
        AuthRequest authRequest = SocialUtils.getAuthRequest(source, socialProperties);
        // 创建参数Map
        Map<String, String> map = new HashMap<>();
        // 设置租户ID
        map.put("tenantId", tenantId);
        // 设置域名
        map.put("domain", domain);
        // 生成随机状态码，用于防止CSRF攻击
        map.put("state", AuthStateUtils.createState());
        // 构建授权URL，将参数Base64编码后作为state参数
        String authorizeUrl = authRequest.authorize(Base64.encode(JsonUtils.toJsonString(map), StandardCharsets.UTF_8));
        // 返回授权URL
        return R.ok("操作成功", authorizeUrl);
    }

    /**
     * 前端回调绑定授权(需要token)
     *
     * @param loginBody 请求体
     * @return 结果
     */
    // POST请求映射：/auth/social/callback
    @PostMapping("/social/callback")
    public R<Void> socialCallback(@RequestBody SocialLoginBody loginBody) {
        // 校验token，确保用户已登录
        StpUtil.checkLogin();
        // 获取第三方登录信息
        AuthResponse<AuthUser> response = SocialUtils.loginAuth(
                loginBody.getSource(), loginBody.getSocialCode(),
                loginBody.getSocialState(), socialProperties);
        // 获取授权用户数据
        AuthUser authUserData = response.getData();
        // 判断授权响应是否成功
        if (!response.ok()) {
            // 授权失败，返回错误信息
            return R.fail(response.getMsg());
        }
        // 执行社交账号绑定
        loginService.socialRegister(authUserData);
        // 返回成功响应
        return R.ok();
    }


    /**
     * 取消授权(需要token)
     *
     * @param socialId socialId
     */
    // DELETE请求映射：/auth/unlock/{socialId}
    @DeleteMapping(value = "/unlock/{socialId}")
    public R<Void> unlockSocial(@PathVariable Long socialId) {
        // 校验token，确保用户已登录
        StpUtil.checkLogin();
        // 删除社交绑定记录
        Boolean rows = socialUserService.deleteWithValidById(socialId);
        // 返回操作结果
        return rows ? R.ok() : R.fail("取消授权失败");
    }


    /**
     * 退出登录
     */
    // POST请求映射：/auth/logout
    @PostMapping("/logout")
    public R<Void> logout() {
        // 执行登出操作
        loginService.logout();
        // 返回成功响应
        return R.ok("退出成功");
    }

    /**
     * 用户注册
     */
    // API加密注解：对请求和响应进行加密解密
    @ApiEncrypt
    // POST请求映射：/auth/register
    @PostMapping("/register")
    public R<Void> register(@Validated @RequestBody RegisterBody user) {
        // 检查系统是否开启注册功能
        if (!configService.selectRegisterEnabled(user.getTenantId())) {
            // 注册功能未开启
            return R.fail("当前系统没有开启注册功能！");
        }
        // 执行注册
        registerService.register(user);
        // 返回成功响应
        return R.ok();
    }

    /**
     * 登录页面租户下拉框
     *
     * @return 租户列表
     */
    // 限流注解：基于IP限流，60秒内最多请求20次
    @RateLimiter(time = 60, count = 20, limitType = LimitType.IP)
    // GET请求映射：/auth/tenant/list
    @GetMapping("/tenant/list")
    public R<LoginTenantVo> tenantList(HttpServletRequest request) throws Exception {
        // 返回对象
        LoginTenantVo result = new LoginTenantVo();
        // 检查租户功能是否开启
        boolean enable = TenantHelper.isEnable();
        // 设置租户功能状态
        result.setTenantEnabled(enable);
        // 如果未开启租户则直接返回
        if (!enable) {
            return R.ok(result);
        }

        // 查询所有租户列表
        List<SysTenantVo> tenantList = tenantService.queryList(new SysTenantBo());
        // 转换为视图对象列表
        List<TenantListVo> voList = MapstructUtils.convert(tenantList, TenantListVo.class);
        try {
            // 如果是超管返回所有租户
            if (LoginHelper.isSuperAdmin()) {
                // 设置租户列表
                result.setVoList(voList);
                // 返回响应
                return R.ok(result);
            }
        } catch (NotLoginException ignored) {
            // 忽略未登录异常，继续执行
        }

        // 获取域名
        String host;
        // 从请求头获取referer
        String referer = request.getHeader("referer");
        if (StringUtils.isNotBlank(referer)) {
            // 这里从referer中取值是为了本地使用hosts添加虚拟域名，方便本地环境调试
            // 解析referer获取域名
            host = referer.split("//")[1].split("/")[0];
        } else {
            // 从请求URL获取域名
            host = new URL(request.getRequestURL().toString()).getHost();
        }
        // 根据域名进行筛选
        List<TenantListVo> list = StreamUtils.filter(voList, vo ->
            StringUtils.equalsIgnoreCase(vo.getDomain(), host));
        // 设置筛选后的租户列表，如果为空则返回全部
        result.setVoList(CollUtil.isNotEmpty(list) ? list : voList);
        // 返回响应
        return R.ok(result);
    }

}
