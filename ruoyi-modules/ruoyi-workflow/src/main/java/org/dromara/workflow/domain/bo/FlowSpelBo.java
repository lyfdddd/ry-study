// 包声明：定义当前类所在的包路径，org.dromara.workflow.domain.bo 表示工作流模块业务对象层
// package关键字用于声明类所在的包，便于组织和管理代码，避免命名冲突
package org.dromara.workflow.domain.bo;

// MyBatis-Plus基础实体类：提供创建人、创建时间、更新人、更新时间等公共字段
import org.dromara.common.mybatis.core.domain.BaseEntity;
// 校验分组：新增分组，用于区分新增和编辑的校验规则
import org.dromara.common.core.validate.AddGroup;
// 校验分组：编辑分组，用于区分新增和编辑的校验规则
import org.dromara.common.core.validate.EditGroup;
// MapStruct-plus注解：自动映射注解，用于自动生成对象转换代码
import io.github.linpeilie.annotations.AutoMapper;
// Lombok注解：自动生成getter、setter、toString、equals、hashCode等方法
import lombok.Data;
// Lombok注解：自动生成equals和hashCode方法，callSuper=true表示调用父类方法
import lombok.EqualsAndHashCode;
// Jakarta验证注解：非空字符串验证注解，用于参数校验
import jakarta.validation.constraints.NotBlank;
// SpEL表达式实体类：目标实体类，用于MapStruct-plus自动映射
import org.dromara.workflow.domain.FlowSpel;

/**
 * 流程SpEL表达式定义业务对象
 * 继承BaseEntity，获得创建人、创建时间、更新人、更新时间等公共字段
 * 用于封装SpEL表达式相关的业务数据，作为Controller层接收参数的对象
 * 使用MapStruct-plus自动映射到FlowSpel实体类
 *
 * @author Michelle.Chung
 * @date 2025-07-04
 */
// Lombok注解：自动生成getter、setter、toString、equals、hashCode等方法
@Data
// Lombok注解：自动生成equals和hashCode方法，callSuper=true表示调用父类方法
@EqualsAndHashCode(callSuper = true)
// MapStruct-plus注解：指定目标映射类为FlowSpel，reverseConvertGenerate=false表示不生成反向转换
@AutoMapper(target = FlowSpel.class, reverseConvertGenerate = false)
public class FlowSpelBo extends BaseEntity {

    /**
     * 主键ID
     * 唯一标识一条SpEL表达式定义记录
     */
    private Long id;

    /**
     * 组件名称
     * Spring Bean的名称，用于定位要调用的组件
     * 如"userService"、"deptService"等
     */
    private String componentName;

    /**
     * 方法名
     * 要调用的方法名称
     * 如"getUserById"、"findDeptList"等
     */
    private String methodName;

    /**
     * 方法参数
     * 方法调用时传递的参数，支持SpEL表达式
     * 如"#{userId}"、"#{deptId}"等
     */
    private String methodParams;

    /**
     * 预览SpEL值
     * 完整的SpEL表达式预览，用于调试和验证
     * 如"@userService.getUserById(#{userId})"
     * 必填字段，新增和编辑时都必须提供
     */
    // Jakarta验证注解：非空字符串验证，message定义错误提示信息，groups指定校验分组
    @NotBlank(message = "预览spel值不能为空", groups = { AddGroup.class, EditGroup.class })
    private String viewSpel;

    /**
     * 状态（0正常 1停用）
     * 表达式的启用状态，0表示正常可用，1表示已停用
     * 用于控制表达式是否生效
     * 必填字段，新增和编辑时都必须提供
     */
    // Jakarta验证注解：非空字符串验证，message定义错误提示信息，groups指定校验分组
    @NotBlank(message = "状态（0正常 1停用）不能为空", groups = { AddGroup.class, EditGroup.class })
    private String status;

    /**
     * 备注
     * 表达式的说明和备注信息
     * 用于记录表达式的用途和注意事项
     */
    private String remark;

    // 继承自BaseEntity的创建人、创建时间、更新人、更新时间字段

}
