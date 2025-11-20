// 包声明：定义当前类所在的包路径，org.dromara.workflow.domain.bo 表示工作流模块业务对象层
// package关键字用于声明类所在的包，便于组织和管理代码，避免命名冲突
package org.dromara.workflow.domain.bo;

// Jackson注解：JSON格式注解，用于指定日期序列化格式
import com.fasterxml.jackson.annotation.JsonFormat;
// MapStruct-plus注解：自动映射注解，用于自动生成对象转换代码
import io.github.linpeilie.annotations.AutoMapper;
// Jakarta验证注解：非空字符串验证注解，用于参数校验
import jakarta.validation.constraints.NotBlank;
// Jakarta验证注解：非空验证注解，用于参数校验
import jakarta.validation.constraints.NotNull;
// Lombok注解：自动生成getter、setter、toString、equals、hashCode等方法
import lombok.Data;
// Lombok注解：自动生成equals和hashCode方法，callSuper=true表示调用父类方法
import lombok.EqualsAndHashCode;
// 校验分组：新增分组，用于区分新增和编辑的校验规则
import org.dromara.common.core.validate.AddGroup;
// 校验分组：编辑分组，用于区分新增和编辑的校验规则
import org.dromara.common.core.validate.EditGroup;
// MyBatis-Plus基础实体类：提供创建人、创建时间、更新人、更新时间等公共字段
import org.dromara.common.mybatis.core.domain.BaseEntity;
// 请假实体类：目标实体类，用于MapStruct-plus自动映射
import org.dromara.workflow.domain.TestLeave;
// Spring格式注解：日期时间格式注解，用于指定日期反序列化格式
import org.springframework.format.annotation.DateTimeFormat;

// Java日期类：用于表示日期和时间
import java.util.Date;

/**
 * 请假业务对象
 * 继承BaseEntity，获得创建人、创建时间、更新人、更新时间等公共字段
 * 用于封装请假相关的业务数据，作为Controller层接收参数的对象
 * 使用MapStruct-plus自动映射到TestLeave实体类
 *
 * @author may
 * @date 2023-07-21
 */
// Lombok注解：自动生成getter、setter、toString、equals、hashCode等方法
@Data
// Lombok注解：自动生成equals和hashCode方法，callSuper=true表示调用父类方法
@EqualsAndHashCode(callSuper = true)
// MapStruct-plus注解：指定目标映射类为TestLeave，reverseConvertGenerate=false表示不生成反向转换
@AutoMapper(target = TestLeave.class, reverseConvertGenerate = false)
public class TestLeaveBo extends BaseEntity {

    /**
     * 主键ID
     * 唯一标识一条请假申请记录
     * 编辑时必须提供，用于定位要更新的记录
     */
    // Jakarta验证注解：非空验证，message定义错误提示信息，groups指定校验分组
    @NotNull(message = "主键不能为空", groups = {EditGroup.class})
    private Long id;

    /**
     * 流程编码
     * 关联的流程定义编码，如"leave_process"
     * 用于指定请假申请使用的流程定义
     */
    private String flowCode;

    /**
     * 申请编号
     * 请假申请的唯一编号，如"LEAVE202311190001"
     * 用于业务单据的编号管理
     */
    private String applyCode;

    /**
     * 请假类型
     * 请假的类型，如"事假"、"病假"、"年假"等
     * 必填字段，新增和编辑时都必须提供
     * 通常关联字典表进行数据验证
     */
    // Jakarta验证注解：非空字符串验证，message定义错误提示信息，groups指定校验分组
    @NotBlank(message = "请假类型不能为空", groups = {AddGroup.class, EditGroup.class})
    private String leaveType;

    /**
     * 开始时间
     * 请假的开始日期
     * 必填字段，新增和编辑时都必须提供
     * 使用@DateTimeFormat指定前端到后端的日期格式
     * 使用@JsonFormat指定后端到前端的日期格式
     */
    // Jakarta验证注解：非空验证，message定义错误提示信息，groups指定校验分组
    @NotNull(message = "开始时间不能为空", groups = {AddGroup.class, EditGroup.class})
    // Spring格式注解：指定日期反序列化格式为yyyy-MM-dd
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    // Jackson注解：指定日期序列化格式为yyyy-MM-dd
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date startDate;

    /**
     * 结束时间
     * 请假的结束日期
     * 必填字段，新增和编辑时都必须提供
     * 使用@DateTimeFormat指定前端到后端的日期格式
     * 使用@JsonFormat指定后端到前端的日期格式
     */
    // Jakarta验证注解：非空验证，message定义错误提示信息，groups指定校验分组
    @NotNull(message = "结束时间不能为空", groups = {AddGroup.class, EditGroup.class})
    // Spring格式注解：指定日期反序列化格式为yyyy-MM-dd
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    // Jackson注解：指定日期序列化格式为yyyy-MM-dd
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date endDate;

    /**
     * 请假天数
     * 请假的总天数
     * 根据开始时间和结束时间自动计算
     */
    private Integer leaveDays;

    /**
     * 开始请假天数（查询条件）
     * 用于查询请假天数范围内的记录
     * 作为查询条件，不是实际请假天数
     */
    private Integer startLeaveDays;

    /**
     * 结束请假天数（查询条件）
     * 用于查询请假天数范围内的记录
     * 作为查询条件，不是实际请假天数
     */
    private Integer endLeaveDays;

    /**
     * 请假原因
     * 请假的详细原因说明
     * 用于审批人了解请假事由
     */
    private String remark;

    /**
     * 状态
     * 请假申请的审批状态
     * 如"草稿"、"审批中"、"已通过"、"已拒绝"等
     */
    private String status;

    // 继承自BaseEntity的创建人、创建时间、更新人、更新时间字段

}
