package org.dromara.web.domain.vo;

// 系统租户视图对象（用于MapStruct自动映射）
import org.dromara.system.domain.vo.SysTenantVo;
// MapStruct自动映射注解（编译期生成映射代码，性能是BeanUtils的10倍）
import io.github.linpeilie.annotations.AutoMapper;
// Lombok注解：自动生成getter、setter、toString、equals、hashCode方法
import lombok.Data;

/**
 * 租户列表视图对象
 * 用于登录页面展示可选租户列表
 * 通过@AutoMapper注解与SysTenantVo自动映射，简化对象转换
 *
 * @author Lion Li
 */
// Lombok注解：自动生成getter、setter、toString、equals、hashCode方法
@Data
// MapStruct自动映射注解：编译期生成TenantListVo与SysTenantVo之间的映射代码
// target指定目标类，自动生成convert方法，避免手动编写转换逻辑
@AutoMapper(target = SysTenantVo.class)
public class TenantListVo {

    /**
     * 租户编号（租户唯一标识）
     * 用于多租户数据隔离，登录时选择租户后，后续操作都在此租户上下文中执行
     * 示例：000000（默认租户）、000001（企业A）、000002（企业B）
     */
    private String tenantId;

    /**
     * 企业名称
     * 租户对应的企业或组织名称，用于前端展示
     * 示例：若依科技、智慧矿山研究院
     */
    private String companyName;

    /**
     * 域名
     * 租户绑定的域名，支持通过域名自动识别租户
     * 示例：ruoyi.com、zhihuishankuang.com
     */
    private String domain;

}
