// 定义社交工具类的包路径
package org.dromara.common.social.utils;

// 引入Hutool对象工具类
import cn.hutool.core.util.ObjectUtil;
// 引入JustAuth配置类
import me.zhyd.oauth.config.AuthConfig;
// 引入JustAuth异常类
import me.zhyd.oauth.exception.AuthException;
// 引入JustAuth回调模型
import me.zhyd.oauth.model.AuthCallback;
// 引入JustAuth响应模型
import me.zhyd.oauth.model.AuthResponse;
// 引入JustAuth用户模型
import me.zhyd.oauth.model.AuthUser;
// 引入JustAuth各种请求实现类
import me.zhyd.oauth.request.*;
// 引入Spring工具类
import org.dromara.common.core.utils.SpringUtils;
// 引入社交登录配置属性类
import org.dromara.common.social.config.properties.SocialLoginConfigProperties;
import org.dromara.common.social.config.properties.SocialProperties;
// 引入自定义Gitea请求类
import org.dromara.common.social.gitea.AuthGiteaRequest;
// 引入自定义MaxKey请求类
import org.dromara.common.social.maxkey.AuthMaxKeyRequest;
// 引入自定义TopIam请求类
import org.dromara.common.social.topiam.AuthTopIamRequest;

/**
 * 社交登录认证授权工具类
 * 封装第三方社交平台的登录认证逻辑
 * 支持多种社交平台（微信、QQ、GitHub等）的统一认证接口
 *
 * @author thiszhc
 */
// 社交工具类
public class SocialUtils  {

    /**
     * 授权状态缓存实例
     * 从Spring容器中获取AuthRedisStateCache Bean，用于存储OAuth2 state状态
     */
    private static final AuthRedisStateCache STATE_CACHE = SpringUtils.getBean(AuthRedisStateCache.class);

    /**
     * 执行社交登录认证
     * 根据source类型获取对应的AuthRequest，执行登录认证
     *
     * @param source 社交平台类型（如wechat_open、qq、github等）
     * @param code 授权码，第三方平台返回的code
     * @param state 状态码，用于防止CSRF攻击
     * @param socialProperties 社交配置属性
     * @return 认证响应，包含用户信息
     * @throws AuthException 认证异常
     */
    @SuppressWarnings("unchecked")
    public static AuthResponse<AuthUser> loginAuth(String source, String code, String state, SocialProperties socialProperties) throws AuthException {
        // 获取对应社交平台的AuthRequest实例
        AuthRequest authRequest = getAuthRequest(source, socialProperties);
        // 创建回调对象，设置code和state
        AuthCallback callback = new AuthCallback();
        callback.setCode(code);
        callback.setState(state);
        // 执行登录认证，返回认证响应
        return authRequest.login(callback);
    }

    /**
     * 获取指定社交平台的AuthRequest实例
     * 根据source类型创建对应的AuthRequest，用于发起授权请求
     *
     * @param source 社交平台类型（如wechat_open、qq、github等）
     * @param socialProperties 社交配置属性
     * @return AuthRequest实例
     * @throws AuthException 如果配置不存在或不支持的类型
     */
    public static AuthRequest getAuthRequest(String source, SocialProperties socialProperties) throws AuthException {
        // 从配置中获取对应社交平台的配置信息
        SocialLoginConfigProperties obj = socialProperties.getType().get(source);
        // 如果配置不存在，抛出异常
         if (ObjectUtil.isNull(obj)) {
            throw new AuthException("不支持的第三方登录类型");
        }
        // 构建AuthConfig对象，设置通用配置
        AuthConfig.AuthConfigBuilder builder = AuthConfig.builder()
            .clientId(obj.getClientId())
            .clientSecret(obj.getClientSecret())
            .redirectUri(obj.getRedirectUri())
            .scopes(obj.getScopes());
        // 根据source类型返回对应的AuthRequest实例
        return switch (source.toLowerCase()) {
            // 钉钉登录
            case "dingtalk" -> new AuthDingTalkV2Request(builder.build(), STATE_CACHE);
            // 百度登录
            case "baidu" -> new AuthBaiduRequest(builder.build(), STATE_CACHE);
            // GitHub登录
            case "github" -> new AuthGithubRequest(builder.build(), STATE_CACHE);
            // Gitee登录
            case "gitee" -> new AuthGiteeRequest(builder.build(), STATE_CACHE);
            // 微博登录
            case "weibo" -> new AuthWeiboRequest(builder.build(), STATE_CACHE);
            // Coding登录
            case "coding" -> new AuthCodingRequest(builder.build(), STATE_CACHE);
            // 开源中国登录
            case "oschina" -> new AuthOschinaRequest(builder.build(), STATE_CACHE);
            // 支付宝登录（在创建回调地址时，不允许使用localhost或者127.0.0.1，所以这儿的回调地址使用的局域网内的ip）
            case "alipay_wallet" -> new AuthAlipayRequest(builder.build(), socialProperties.getType().get("alipay_wallet").getAlipayPublicKey(), STATE_CACHE);
            // QQ登录
            case "qq" -> new AuthQqRequest(builder.build(), STATE_CACHE);
            // 微信开放平台登录
            case "wechat_open" -> new AuthWeChatOpenRequest(builder.build(), STATE_CACHE);
            // 淘宝登录
            case "taobao" -> new AuthTaobaoRequest(builder.build(), STATE_CACHE);
            // 抖音登录
            case "douyin" -> new AuthDouyinRequest(builder.build(), STATE_CACHE);
            // LinkedIn登录
            case "linkedin" -> new AuthLinkedinRequest(builder.build(), STATE_CACHE);
            // Microsoft登录
            case "microsoft" -> new AuthMicrosoftRequest(builder.build(), STATE_CACHE);
            // 人人网登录
            case "renren" -> new AuthRenrenRequest(builder.build(), STATE_CACHE);
            // Stack Overflow登录
            case "stack_overflow" -> new AuthStackOverflowRequest(builder.stackOverflowKey(obj.getStackOverflowKey()).build(), STATE_CACHE);
            // 华为登录
            case "huawei" -> new AuthHuaweiV3Request(builder.build(), STATE_CACHE);
            // 企业微信登录
            case "wechat_enterprise" -> new AuthWeChatEnterpriseQrcodeV2Request(builder.agentId(obj.getAgentId()).build(), STATE_CACHE);
            // GitLab登录
            case "gitlab" -> new AuthGitlabRequest(builder.build(), STATE_CACHE);
            // 微信公众号登录
            case "wechat_mp" -> new AuthWeChatMpRequest(builder.build(), STATE_CACHE);
            // 阿里云登录
            case "aliyun" -> new AuthAliyunRequest(builder.build(), STATE_CACHE);
            // MaxKey登录
            case "maxkey" -> new AuthMaxKeyRequest(builder.build(), STATE_CACHE);
            // TopIAM登录
            case "topiam" -> new AuthTopIamRequest(builder.build(), STATE_CACHE);
            // Gitea登录
            case "gitea" -> new AuthGiteaRequest(builder.build(), STATE_CACHE);
            // 默认情况，抛出异常
            default -> throw new AuthException("未获取到有效的Auth配置");
        };
    }
}

