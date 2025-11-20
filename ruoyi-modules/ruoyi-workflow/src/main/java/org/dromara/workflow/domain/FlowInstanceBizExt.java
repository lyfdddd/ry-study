// 包声明：定义当前类所在的包路径，org.dromara.workflow.domain 表示工作流模块领域模型层
// package关键字用于声明类所在的包，便于组织和管理代码，避免命名冲突
package org.dromara.workflow.domain;

// MyBatis-Plus注解：表ID注解，标记主键字段
import com.baomidou.mybatisplus.annotation.TableId;
// MyBatis-Plus注解：逻辑删除注解，标记逻辑删除字段
import com.baomidou.mybatisplus.annotation.TableLogic;
// MyBatis-Plus注解：表名注解，指定对应的数据库表
import com.baomidou.mybatisplus.annotation.TableName;
// Lombok注解：自动生成getter、setter、toString、equals、hashCode等方法
import lombok.Data;
// Lombok注解：自动生成equals和hashCode方法，callSuper=true表示调用父类方法
import lombok.EqualsAndHashCode;
// 租户实体基类：提供租户相关字段（tenantId）
import org.dromara.common.tenant.core.TenantEntity;

// Java序列化接口：用于序列化版本控制
import java.io.Serial;

/**
 * 流程实例业务扩展实体类
 * 对应数据库表：flow_instance_biz_ext（流程实例业务扩展表）
 * 继承TenantEntity，获得租户ID字段
 * 用于存储流程实例相关的业务扩展信息，如业务编码、业务标题等
 *
 * @author may
 * @date 2025-08-05
 */
// Lombok注解：自动生成getter、setter、toString、equals、hashCode等方法
@Data
// Lombok注解：自动生成equals和hashCode方法，callSuper=true表示调用父类方法
@EqualsAndHashCode(callSuper = true)
// MyBatis-Plus注解：指定数据库表名为flow_instance_biz_ext
@TableName("flow_instance_biz_ext")
public class FlowInstanceBizExt extends TenantEntity {

    // Java序列化版本UID，用于反序列化版本控制
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     * 唯一标识一条流程实例业务扩展记录
     * 对应数据库表id列
     */
    // MyBatis-Plus注解：标记为主键字段，value指定数据库列名
    @TableId(value = "id")
    private Long id;

    /**
     * 流程实例ID
     * 关联流程实例表的主键
     * 建立流程实例与业务扩展信息的关联关系
     * 对应数据库表instance_id列
     */
    private Long instanceId;

    /**
     * 业务ID
     * 关联具体业务数据的主键（如请假单ID、报销单ID）
     * 用于将流程实例与具体业务数据关联
     * 对应数据库表business_id列
     */
    private String businessId;

    /**
     * 业务编码
     * 业务的唯一编码，如"LEAVE202311190001"
     * 用于业务单据的编号管理
     * 对应数据库表business_code列
     */
    private String businessCode;

    /**
     * 业务标题
     * 业务的标题或主题，如"张三的请假申请"
     * 用于在流程列表中显示业务摘要
     * 对应数据库表business_title列
     */
    private String businessTitle;

    /**
     * 删除标志（0代表存在 1代表删除）
     * 逻辑删除字段，0表示正常，1表示已删除
     * 实现软删除功能，保留历史数据
     * 对应数据库表del_flag列
     */
    // MyBatis-Plus注解：标记为逻辑删除字段
    @TableLogic
    private String delFlag;

    // 继承自TenantEntity的租户ID字段，用于多租户数据隔离
    // 继承自BaseEntity的创建人、创建时间、更新人、更新时间字段

}
