// 定义社交登录配置属性类的包路径
package org.dromara.common.social.config.properties;

// Lombok注解，自动生成getter、setter、toString、equals、hashCode方法
import lombok.Data;

// 引入List集合类
import java.util.List;

/**
 * 社交登录配置属性类
 * 封装第三方社交登录平台的配置信息
 * 支持多种社交平台（微信、QQ、GitHub等）的配置
 *
 * @author thiszhc
 */
// Lombok注解，自动生成getter、setter、toString、equals、hashCode方法
@Data
// 社交登录配置属性类
public class SocialLoginConfigProperties {

    /**
     * 应用ID（Client ID）
     * 在第三方平台注册应用时获取的唯一标识
     */
    private String clientId;

    /**
     * 应用密钥（Client Secret）
     * 在第三方平台注册应用时获取的密钥，用于验证应用身份
     */
    private String clientSecret;

    /**
     * 回调地址（Redirect URI）
     * 用户授权后，第三方平台重定向回的地址
     */
    private String redirectUri;

    /**
     * 是否获取unionId
     * 微信特有，用于区分同一用户在不同公众号/应用下的唯一标识
     */
    private boolean unionId;

    /**
     * Coding企业名称
     * Coding平台特有，用于企业级应用
     */
    private String codingGroupName;

    /**
     * 支付宝公钥
     * 支付宝平台特有，用于验证支付宝返回的数据签名
     */
    private String alipayPublicKey;

    /**
     * 企业微信应用ID（AgentId）
     * 企业微信平台特有，用于标识企业内部应用
     */
    private String agentId;

    /**
     * Stack Overflow API Key
     * Stack Overflow平台特有，用于访问Stack Overflow API
     */
    private String stackOverflowKey;

    /**
     * 设备ID
     * 某些平台需要设备标识
     */
    private String deviceId;

    /**
     * 客户端系统类型
     * 标识客户端操作系统类型（iOS、Android等）
     */
    private String clientOsType;

    /**
     * MaxKey服务器地址
     * MaxKey平台特有，指定MaxKey认证服务器地址
     */
    private String serverUrl;

    /**
     * 请求范围（Scopes）
     * OAuth2授权范围，指定需要获取的用户信息权限
     */
    private List<String> scopes;

}
