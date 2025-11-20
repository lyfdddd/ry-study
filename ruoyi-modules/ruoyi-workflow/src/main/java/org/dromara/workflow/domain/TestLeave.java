// 包声明：定义当前类所在的包路径，org.dromara.workflow.domain 表示工作流模块领域模型层
// package关键字用于声明类所在的包，便于组织和管理代码，避免命名冲突
package org.dromara.workflow.domain;

// MyBatis-Plus注解：表ID注解，标记主键字段
import com.baomidou.mybatisplus.annotation.TableId;
// MyBatis-Plus注解：表名注解，指定对应的数据库表
import com.baomidou.mybatisplus.annotation.TableName;
// Lombok注解：自动生成getter、setter、toString、equals、hashCode等方法
import lombok.Data;
// Lombok注解：自动生成equals和hashCode方法，callSuper=true表示调用父类方法
import lombok.EqualsAndHashCode;
// MyBatis-Plus基础实体类：提供创建人、创建时间、更新人、更新时间等公共字段
import org.dromara.common.mybatis.core.domain.BaseEntity;

// Java序列化接口：用于序列化版本控制
import java.io.Serial;
// Java日期类：用于表示日期和时间
import java.util.Date;

/**
 * 请假实体类
 * 对应数据库表：test_leave（请假申请表）
 * 继承BaseEntity，获得创建人、创建时间、更新人、更新时间等公共字段
 * 用于存储请假申请的基本信息，作为工作流测试用例
 *
 * @author may
 * @date 2023-07-21
 */
// Lombok注解：自动生成getter、setter、toString、equals、hashCode等方法
@Data
// Lombok注解：自动生成equals和hashCode方法，callSuper=true表示调用父类方法
@EqualsAndHashCode(callSuper = true)
// MyBatis-Plus注解：指定数据库表名为test_leave
@TableName("test_leave")
public class TestLeave extends BaseEntity {

    // Java序列化版本UID，用于反序列化版本控制
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     * 唯一标识一条请假申请记录
     * 对应数据库表id列
     */
    // MyBatis-Plus注解：标记为主键字段，value指定数据库列名
    @TableId(value = "id")
    private Long id;

    /**
     * 申请编号
     * 请假申请的唯一编号，如"LEAVE202311190001"
     * 用于业务单据的编号管理
     * 对应数据库表apply_code列
     */
    private String applyCode;

    /**
     * 请假类型
     * 请假的类型，如"事假"、"病假"、"年假"等
     * 通常关联字典表进行数据验证
     * 对应数据库表leave_type列
     */
    private String leaveType;

    /**
     * 开始时间
     * 请假的开始日期和时间
     * 用于计算请假时长和审批时间范围
     * 对应数据库表start_date列
     */
    private Date startDate;

    /**
     * 结束时间
     * 请假的结束日期和时间
     * 用于计算请假时长和审批时间范围
     * 对应数据库表end_date列
     */
    private Date endDate;

    /**
     * 请假天数
     * 请假的总天数
     * 根据开始时间和结束时间自动计算
     * 对应数据库表leave_days列
     */
    private Integer leaveDays;

    /**
     * 请假原因
     * 请假的详细原因说明
     * 用于审批人了解请假事由
     * 对应数据库表remark列
     */
    private String remark;

    /**
     * 状态
     * 请假申请的审批状态
     * 如"草稿"、"审批中"、"已通过"、"已拒绝"等
     * 对应数据库表status列
     */
    private String status;

    // 继承自BaseEntity的创建人、创建时间、更新人、更新时间字段

}
