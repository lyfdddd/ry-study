package org.dromara.web.domain.vo;

// Jackson注解：指定JSON序列化时的属性名（遵循OAuth2规范）
import com.fasterxml.jackson.annotation.JsonProperty;
// Lombok注解：自动生成getter、setter、toString、equals、hashCode方法
import lombok.Data;

/**
 * 登录验证信息
 * 遵循OAuth2标准格式，包含access_token、refresh_token等字段
 *
 * @author Michelle.Chung
 */
// Lombok注解：自动生成getter、setter、toString、equals、hashCode方法
@Data
public class LoginVo {

    /**
     * 授权令牌（Access Token）
     * 用于访问受保护资源的凭证，短期有效
     * JSON序列化时使用access_token格式（OAuth2标准）
     */
    @JsonProperty("access_token")
    private String accessToken;

    /**
     * 刷新令牌（Refresh Token）
     * 用于获取新的access_token，长期有效
     * JSON序列化时使用refresh_token格式（OAuth2标准）
     */
    @JsonProperty("refresh_token")
    private String refreshToken;

    /**
     * 授权令牌 access_token 的有效期（秒）
     * 表示access_token的剩余有效时间
     * JSON序列化时使用expire_in格式（OAuth2标准）
     */
    @JsonProperty("expire_in")
    private Long expireIn;

    /**
     * 刷新令牌 refresh_token 的有效期（秒）
     * 表示refresh_token的剩余有效时间
     * JSON序列化时使用refresh_expire_in格式
     */
    @JsonProperty("refresh_expire_in")
    private Long refreshExpireIn;

    /**
     * 应用id（Client ID）
     * 标识客户端应用，用于区分不同客户端
     * JSON序列化时使用client_id格式
     */
    @JsonProperty("client_id")
    private String clientId;

    /**
     * 令牌权限范围（Scope）
     * 表示令牌拥有的权限范围，如read、write等
     */
    private String scope;

    /**
     * 用户 openid
     * 第三方登录（如微信、QQ）返回的用户唯一标识
     */
    private String openid;

}
