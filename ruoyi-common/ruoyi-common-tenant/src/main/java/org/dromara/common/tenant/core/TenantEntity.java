package org.dromara.common.tenant.core;

import org.dromara.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 租户基类
 *
 * @author Michelle.Chung
 */
// Lombok注解，自动生成getter、setter、toString、equals、hashCode方法
@Data
// Lombok注解，生成equals和hashCode方法，callSuper=true表示包含父类字段
@EqualsAndHashCode(callSuper = true)
public class TenantEntity extends BaseEntity {

    /**
     * 租户编号，用于数据隔离
     */
    private String tenantId;

}
