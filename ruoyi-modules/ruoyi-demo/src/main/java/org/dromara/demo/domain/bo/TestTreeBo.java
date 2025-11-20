// 测试树表业务对象层
// 封装前端传入的测试树表参数，用于新增和编辑操作，包含Jakarta Bean Validation校验注解
package org.dromara.demo.domain.bo;

// 新增分组校验接口，用于新增操作的参数校验
import org.dromara.common.core.validate.AddGroup;
// 编辑分组校验接口，用于编辑操作的参数校验
import org.dromara.common.core.validate.EditGroup;
// MyBatis-Plus基础实体类，提供公共字段（createBy、createTime等）
import org.dromara.common.mybatis.core.domain.BaseEntity;
// 测试树表实体类，用于MapStruct转换
import org.dromara.demo.domain.TestTree;
// MapStruct-plus自动映射注解，实现BO与Entity之间的自动转换
import io.github.linpeilie.annotations.AutoMapper;
// Jakarta Bean Validation非空校验注解，用于字符串类型
import jakarta.validation.constraints.NotBlank;
// Jakarta Bean Validation非空校验注解，用于对象类型
import jakarta.validation.constraints.NotNull;
// Lombok注解，自动生成getter、setter、toString、equals、hashCode方法
import lombok.Data;
// Lombok注解，自动生成equals和hashCode方法，callSuper=true表示包含父类字段
import lombok.EqualsAndHashCode;

/**
 * 测试树表业务对象 test_tree
 * 封装前端传入的测试树表参数，用于新增和编辑操作
 * 继承BaseEntity获取公共字段，使用@AutoMapper实现与TestTree的自动转换
 *
 * @author Lion Li
 * @date 2021-07-26
 */

// Lombok注解，自动生成getter、setter、toString、equals、hashCode方法
@Data
// Lombok注解，自动生成equals和hashCode方法，callSuper=true表示包含父类字段
@EqualsAndHashCode(callSuper = true)
// MapStruct-plus自动映射注解，target指定目标类为TestTree，reverseConvertGenerate=false表示不生成反向转换
@AutoMapper(target = TestTree.class, reverseConvertGenerate = false)
// 测试树表业务对象类
public class TestTreeBo extends BaseEntity {

    /**
     * 主键ID
     * 编辑时必须传入，使用@NotNull校验
     */
    // Jakarta Bean Validation非空校验，message定义错误提示，groups指定校验分组为EditGroup
    @NotNull(message = "主键不能为空", groups = {EditGroup.class})
    // 主键ID
    private Long id;

    /**
     * 父节点ID
     * 用于构建树形结构，顶级节点的parentId为0
     */
    // 父节点ID
    private Long parentId;

    /**
     * 部门ID
     * 新增和编辑时必须传入，关联sys_dept表
     */
    // 非空校验，groups指定AddGroup和EditGroup分组
    @NotNull(message = "部门id不能为空", groups = {AddGroup.class, EditGroup.class})
    // 部门ID
    private Long deptId;

    /**
     * 用户ID
     * 新增和编辑时必须传入，关联sys_user表
     */
    // 非空校验，groups指定AddGroup和EditGroup分组
    @NotNull(message = "用户id不能为空", groups = {AddGroup.class, EditGroup.class})
    // 用户ID
    private Long userId;

    /**
     * 树节点名称
     * 新增和编辑时必须传入，树形结构显示的节点名称
     */
    // 非空校验，用于字符串类型，groups指定AddGroup和EditGroup分组
    @NotBlank(message = "树节点名不能为空", groups = {AddGroup.class, EditGroup.class})
    // 树节点名称
    private String treeName;

}
