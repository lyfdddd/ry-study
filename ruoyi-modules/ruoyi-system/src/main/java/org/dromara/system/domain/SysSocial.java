// 包声明：定义当前实体类所在的包路径，org.dromara.system.domain 表示系统模块领域层
// package关键字用于声明类所在的包，便于组织和管理代码，避免命名冲突
package org.dromara.system.domain;

// MyBatis-Plus核心注解：提供实体类与数据库表映射的注解
// @TableName指定表名，@TableId指定主键
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
// Lombok注解：自动生成getter、setter、toString、equals、hashCode等方法
// @Data生成所有通用方法，@EqualsAndHashCode生成equals和hashCode
import lombok.Data;
import lombok.EqualsAndHashCode;
// 租户实体基类：提供租户相关的基础字段和功能
// TenantEntity是租户模块提供的实体基类，包含租户ID等公共字段
import org.dromara.common.tenant.core.TenantEntity;

// 序列化接口：提供序列化支持
import java.io.Serial;

/**
 * 社会化关系对象 sys_social
 * 继承TenantEntity，继承租户相关的基础字段（tenantId）
 * 使用Lombok注解简化代码，自动生成getter、setter、toString等方法
 * 使用MyBatis-Plus注解实现实体类与数据库表的映射
 * 对应数据库的sys_social表，存储第三方社交账号绑定信息
 * 用于实现第三方登录（微信、QQ、GitHub等）的用户绑定
 * 存储第三方平台的授权令牌、用户信息等
 *
 * @author thiszhc
 */

// Lombok注解：生成getter、setter、toString、equals、hashCode等方法
@Data
// Lombok注解：生成equals和hashCode方法，callSuper=true表示包含父类字段
@EqualsAndHashCode(callSuper = true)
// MyBatis-Plus注解：指定对应的数据库表名为sys_social
@TableName("sys_social")
// 社会化关系实体类，继承租户实体基类
public class SysSocial extends TenantEntity {

    // 序列化版本UID，用于版本控制
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     * 主键字段，使用@TableId注解标记
     * 对应数据库的id字段
     */
    // MyBatis-Plus注解：标记为主键字段，value指定数据库列名为id
    @TableId(value = "id")
    // 主键ID，Long类型
    private Long id;

    /**
     * 用户ID
     * 关联的系统用户ID
     * 对应数据库的user_id字段
     */
    // 用户ID，Long类型
    private Long userId;

    /**
     * 的唯一ID
     * 第三方平台的唯一用户ID
     * 如微信的openid、GitHub的用户ID等
     * 对应数据库的auth_id字段
     */
    // 的唯一ID，String类型
    private String authId;

    /**
     * 用户来源
     * 第三方平台来源标识
     * 如：wechat、qq、github等
     * 对应数据库的source字段
     */
    // 用户来源，String类型
    private String source;

    /**
     * 用户的授权令牌
     * OAuth2访问令牌，用于调用第三方API
     * 对应数据库的access_token字段
     */
    // 用户的授权令牌，String类型
    private String accessToken;

    /**
     * 用户的授权令牌的有效期，部分平台可能没有
     * 访问令牌的有效期（秒）
     * 部分平台返回，用于判断令牌是否过期
     * 对应数据库的expire_in字段
     */
    // 用户的授权令牌的有效期，int类型
    private int expireIn;

    /**
     * 刷新令牌，部分平台可能没有
     * OAuth2刷新令牌，用于获取新的访问令牌
     * 对应数据库的refresh_token字段
     */
    // 刷新令牌，String类型
    private String refreshToken;

    /**
     * 用户的 open id
     * 微信等平台的OpenID
     * 对应数据库的open_id字段
     */
    // 用户的 open id，String类型
    private String openId;

    /**
     * 授权的第三方账号
     * 第三方平台的用户账号
     * 对应数据库的user_name字段
     */
    // 授权的第三方账号，String类型
    private String userName;

    /**
     * 授权的第三方昵称
     * 第三方平台的用户昵称
     * 对应数据库的nick_name字段
     */
    // 授权的第三方昵称，String类型
    private String nickName;

    /**
     * 授权的第三方邮箱
     * 第三方平台的用户邮箱
     * 对应数据库的email字段
     */
    // 授权的第三方邮箱，String类型
    private String email;

    /**
     * 授权的第三方头像地址
     * 第三方平台的用户头像URL
     * 对应数据库的avatar字段
     */
    // 授权的第三方头像地址，String类型
    private String avatar;

    /**
     * 平台的授权信息，部分平台可能没有
     * 额外的授权信息
     * 对应数据库的access_code字段
     */
    // 平台的授权信息，String类型
    private String accessCode;

    /**
     * 用户的 unionid
     * 微信等平台的UnionID，同一用户在不同应用下的统一标识
     * 对应数据库的union_id字段
     */
    // 用户的 unionid，String类型
    private String unionId;

    /**
     * 授予的权限，部分平台可能没有
     * OAuth2授权范围
     * 对应数据库的scope字段
     */
    // 授予的权限，String类型
    private String scope;

    /**
     * 个别平台的授权信息，部分平台可能没有
     * 令牌类型，如Bearer
     * 对应数据库的token_type字段
     */
    // 个别平台的授权信息，String类型
    private String tokenType;

    /**
     * id token，部分平台可能没有
     * OpenID Connect的ID令牌
     * 对应数据库的id_token字段
     */
    // id token，String类型
    private String idToken;

    /**
     * 小米平台用户的附带属性，部分平台可能没有
     * 小米平台的MAC算法
     * 对应数据库的mac_algorithm字段
     */
    // 小米平台用户的附带属性，String类型
    private String macAlgorithm;

    /**
     * 小米平台用户的附带属性，部分平台可能没有
     * 小米平台的MAC密钥
     * 对应数据库的mac_key字段
     */
    // 小米平台用户的附带属性，String类型
    private String macKey;

    /**
     * 用户的授权code，部分平台可能没有
     * OAuth2授权码
     * 对应数据库的code字段
     */
    // 用户的授权code，String类型
    private String code;

    /**
     * Twitter平台用户的附带属性，部分平台可能没有
     * Twitter的OAuth令牌
     * 对应数据库的oauth_token字段
     */
    // Twitter平台用户的附带属性，String类型
    private String oauthToken;

    /**
     * Twitter平台用户的附带属性，部分平台可能没有
     * Twitter的OAuth令牌密钥
     * 对应数据库的oauth_token_secret字段
     */
    // Twitter平台用户的附带属性，String类型
    private String oauthTokenSecret;


}
