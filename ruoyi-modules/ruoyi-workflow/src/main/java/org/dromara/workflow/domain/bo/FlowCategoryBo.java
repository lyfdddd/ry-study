// 包声明：定义当前类所在的包路径，org.dromara.workflow.domain.bo 表示工作流模块业务对象层
// package关键字用于声明类所在的包，便于组织和管理代码，避免命名冲突
package org.dromara.workflow.domain.bo;

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
// 流程分类实体类：目标实体类，用于MapStruct-plus自动映射
import org.dromara.workflow.domain.FlowCategory;

/**
 * 流程分类业务对象
 * 继承BaseEntity，获得创建人、创建时间、更新人、更新时间等公共字段
 * 用于封装流程分类相关的业务数据，作为Controller层接收参数的对象
 * 使用MapStruct-plus自动映射到FlowCategory实体类
 *
 * @author may
 * @date 2023-06-27
 */
// Lombok注解：自动生成getter、setter、toString、equals、hashCode等方法
@Data
// Lombok注解：自动生成equals和hashCode方法，callSuper=true表示调用父类方法
@EqualsAndHashCode(callSuper = true)
// MapStruct-plus注解：指定目标映射类为FlowCategory，reverseConvertGenerate=false表示不生成反向转换
@AutoMapper(target = FlowCategory.class, reverseConvertGenerate = false)
public class FlowCategoryBo extends BaseEntity {

    /**
     * 流程分类ID
     * 唯一标识一个流程分类
     * 编辑时必须提供，用于定位要更新的记录
     */
    // Jakarta验证注解：非空验证，message定义错误提示信息，groups指定校验分组
    @NotNull(message = "流程分类ID不能为空", groups = { EditGroup.class })
    private Long categoryId;

    /**
     * 父流程分类ID
     * 父级分类的ID，用于构建分类树形结构
     * 顶级分类的parentId通常为0
     * 必填字段，新增和编辑时都必须提供
     */
    // Jakarta验证注解：非空验证，message定义错误提示信息，groups指定校验分组
    @NotNull(message = "父流程分类id不能为空", groups = {AddGroup.class, EditGroup.class})
    private Long parentId;

    /**
     * 流程分类名称
     * 流程分类的名称，如"人事流程"、"财务流程"
     * 必填字段，新增和编辑时都必须提供
     */
    // Jakarta验证注解：非空字符串验证，message定义错误提示信息，groups指定校验分组
    @NotBlank(message = "流程分类名称不能为空", groups = {AddGroup.class, EditGroup.class})
    private String categoryName;

    /**
     * 显示顺序
     * 分类在列表中的显示顺序，数值越小越靠前
     * 用于控制分类的展示顺序
     */
    private Long orderNum;

    // 继承自BaseEntity的创建人、创建时间、更新人、更新时间字段

}
