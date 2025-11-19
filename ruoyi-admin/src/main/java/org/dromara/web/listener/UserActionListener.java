package org.dromara.web.listener;

// Sa-Token监听器接口，用于监听用户登录、注销等行为
import cn.dev33.satoken.listener.SaTokenListener;
// Sa-Token工具类，用于登录认证相关操作
import cn.dev33.satoken.stp.StpUtil;
// Sa-Token登录参数对象，用于配置登录参数
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
// Hutool类型转换工具类
import cn.hutool.core.convert.Convert;
// Hutool UserAgent解析工具类
import cn.hutool.http.useragent.UserAgent;
// Hutool UserAgent解析工具类
import cn.hutool.http.useragent.UserAgentUtil;
// Lombok注解：自动生成final字段的构造方法
import lombok.RequiredArgsConstructor;
// Lombok注解：自动生成slf4j日志对象
import lombok.extern.slf4j.Slf4j;
// 缓存常量定义
import org.dromara.common.core.constant.CacheConstants;
// 系统常量定义
import org.dromara.common.core.constant.Constants;
// 用户在线DTO
import org.dromara.common.core.domain.dto.UserOnlineDTO;
// 核心工具类集合
import org.dromara.common.core.utils.MessageUtils;
import org.dromara.common.core.utils.ServletUtils;
import org.dromara.common.core.utils.SpringUtils;
// IP地址解析工具类
import org.dromara.common.core.utils.ip.AddressUtils;
// 登录信息事件，用于记录登录日志
import org.dromara.common.log.event.LogininforEvent;
// Redis操作工具类
import org.dromara.common.redis.utils.RedisUtils;
// Sa-Token登录助手工具类
import org.dromara.common.satoken.utils.LoginHelper;
// 租户助手工具类，用于租户上下文切换
import org.dromara.common.tenant.helper.TenantHelper;
// 登录服务
import org.dromara.web.service.SysLoginService;
// Spring组件注解
import org.springframework.stereotype.Component;

// Java时间Duration类
import java.time.Duration;

/**
 * 用户行为侦听器的实现
 *
 * @author Lion Li
 */
// Spring组件注解：标记为Spring组件
@Component
// Lombok注解：自动生成slf4j日志对象
@Slf4j
@RequiredArgsConstructor
public class UserActionListener implements SaTokenListener {

    // 注入登录服务
    private final SysLoginService loginService;

    /**
     * 每次登录时触发
     */
    @Override
    public void doLogin(String loginType, Object loginId, String tokenValue, SaLoginParameter loginParameter) {
        // 解析User-Agent，获取浏览器和操作系统信息
        UserAgent userAgent = UserAgentUtil.parse(ServletUtils.getRequest().getHeader("User-Agent"));
        // 获取客户端IP地址
        String ip = ServletUtils.getClientIP();
        // 创建用户在线DTO对象
        UserOnlineDTO dto = new UserOnlineDTO();
        // 设置IP地址
        dto.setIpaddr(ip);
        // 设置登录位置（通过IP解析地理位置）
        dto.setLoginLocation(AddressUtils.getRealAddressByIP(ip));
        // 设置浏览器名称
        dto.setBrowser(userAgent.getBrowser().getName());
        // 设置操作系统名称
        dto.setOs(userAgent.getOs().getName());
        // 设置登录时间（当前时间戳）
        dto.setLoginTime(System.currentTimeMillis());
        // 设置token值
        dto.setTokenId(tokenValue);
        // 从登录参数中获取用户名
        String username = (String) loginParameter.getExtra(LoginHelper.USER_NAME_KEY);
        // 从登录参数中获取租户ID
        String tenantId = (String) loginParameter.getExtra(LoginHelper.TENANT_KEY);
        // 设置用户名
        dto.setUserName(username);
        // 设置客户端标识
        dto.setClientKey((String) loginParameter.getExtra(LoginHelper.CLIENT_KEY));
        // 设置设备类型
        dto.setDeviceType(loginParameter.getDeviceType());
        // 设置部门名称
        dto.setDeptName((String) loginParameter.getExtra(LoginHelper.DEPT_NAME_KEY));
        // 在租户上下文中执行Redis操作
        TenantHelper.dynamic(tenantId, () -> {
            // 判断token是否永久有效（timeout为-1表示永不过期）
            if(loginParameter.getTimeout() == -1) {
                // 将用户在线信息存入Redis，不设置过期时间
                RedisUtils.setCacheObject(CacheConstants.ONLINE_TOKEN_KEY + tokenValue, dto);
            } else {
                // 将用户在线信息存入Redis，设置过期时间（与token过期时间一致）
                RedisUtils.setCacheObject(CacheConstants.ONLINE_TOKEN_KEY + tokenValue, dto, Duration.ofSeconds(loginParameter.getTimeout()));
            }
        });
        // 记录登录日志
        LogininforEvent logininforEvent = new LogininforEvent();
        // 设置租户ID
        logininforEvent.setTenantId(tenantId);
        // 设置用户名
        logininforEvent.setUsername(username);
        // 设置登录状态为成功
        logininforEvent.setStatus(Constants.LOGIN_SUCCESS);
        // 设置成功消息
        logininforEvent.setMessage(MessageUtils.message("user.login.success"));
        // 设置HTTP请求对象
        logininforEvent.setRequest(ServletUtils.getRequest());
        // 发布事件，由监听器异步处理日志记录
        SpringUtils.context().publishEvent(logininforEvent);
        // 更新用户登录信息（IP、时间等）
        loginService.recordLoginInfo((Long) loginParameter.getExtra(LoginHelper.USER_KEY), ip);
        // 记录登录日志
        log.info("user doLogin, userId:{}, token:{}", loginId, tokenValue);
    }

    /**
     * 每次注销时触发
     */
    @Override
    public void doLogout(String loginType, Object loginId, String tokenValue) {
        // 从token的额外数据中获取租户ID
        String tenantId = Convert.toStr(StpUtil.getExtra(tokenValue, LoginHelper.TENANT_KEY));
        // 在租户上下文中执行Redis操作
        TenantHelper.dynamic(tenantId, () -> {
            // 删除Redis中的用户在线信息
            RedisUtils.deleteObject(CacheConstants.ONLINE_TOKEN_KEY + tokenValue);
        });
        // 记录注销日志
        log.info("user doLogout, userId:{}, token:{}", loginId, tokenValue);
    }

    /**
     * 每次被踢下线时触发
     */
    @Override
    public void doKickout(String loginType, Object loginId, String tokenValue) {
        // 从token的额外数据中获取租户ID
        String tenantId = Convert.toStr(StpUtil.getExtra(tokenValue, LoginHelper.TENANT_KEY));
        // 在租户上下文中执行Redis操作
        TenantHelper.dynamic(tenantId, () -> {
            // 删除Redis中的用户在线信息
            RedisUtils.deleteObject(CacheConstants.ONLINE_TOKEN_KEY + tokenValue);
        });
        // 记录被踢下线日志
        log.info("user doKickout, userId:{}, token:{}", loginId, tokenValue);
    }

    /**
     * 每次被顶下线时触发
     */
    @Override
    public void doReplaced(String loginType, Object loginId, String tokenValue) {
        // 从token的额外数据中获取租户ID
        String tenantId = Convert.toStr(StpUtil.getExtra(tokenValue, LoginHelper.TENANT_KEY));
        // 在租户上下文中执行Redis操作
        TenantHelper.dynamic(tenantId, () -> {
            // 删除Redis中的用户在线信息
            RedisUtils.deleteObject(CacheConstants.ONLINE_TOKEN_KEY + tokenValue);
        });
        // 记录被顶下线日志
        log.info("user doReplaced, userId:{}, token:{}", loginId, tokenValue);
    }

    /**
     * 每次被封禁时触发
     */
    @Override
    public void doDisable(String loginType, Object loginId, String service, int level, long disableTime) {
        // 封禁操作不处理，留空实现
    }

    /**
     * 每次被解封时触发
     */
    @Override
    public void doUntieDisable(String loginType, Object loginId, String service) {
        // 解封操作不处理，留空实现
    }

    /**
     * 每次打开二级认证时触发
     */
    @Override
    public void doOpenSafe(String loginType, String tokenValue, String service, long safeTime) {
        // 二级认证开启不处理，留空实现
    }

    /**
     * 每次创建Session时触发
     */
    @Override
    public void doCloseSafe(String loginType, String tokenValue, String service) {
        // 二级认证关闭不处理，留空实现
    }

    /**
     * 每次创建Session时触发
     */
    @Override
    public void doCreateSession(String id) {
        // Session创建不处理，留空实现
    }

    /**
     * 每次注销Session时触发
     */
    @Override
    public void doLogoutSession(String id) {
        // Session注销不处理，留空实现
    }

    /**
     * 每次Token续期时触发
     */
    @Override
    public void doRenewTimeout(String loginType, Object loginId, String tokenValue, long timeout) {
        // Token续期不处理，留空实现
    }
}
