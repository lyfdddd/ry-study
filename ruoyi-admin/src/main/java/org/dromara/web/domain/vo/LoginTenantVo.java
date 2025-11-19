package org.dromara.web.domain.vo;

// Lombok注解：自动生成getter、setter、toString、equals、hashCode方法
import lombok.Data;

// Java集合List接口
import java.util.List;

/**
 * 登录租户对象视图
 * 用于封装登录页面所需的租户相关信息
 *
 * @author Michelle.Chung
 */
// Lombok注解：自动生成getter、setter、toString、equals、hashCode方法
@Data
public class LoginTenantVo {

    /**
     * 租户开关
     * true表示系统启用了多租户模式，登录时需要选择租户
     * false表示系统未启用多租户模式，登录时无需选择租户
     */
    private Boolean tenantEnabled;

    /**
     * 租户对象列表
     * 当tenantEnabled为true时，此列表包含所有可用的租户信息
     * 用于前端展示租户选择下拉框
     */
    private List<TenantListVo> voList;

}
