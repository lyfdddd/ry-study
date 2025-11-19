// 校验分组接口 - 新增场景分组
package org.dromara.common.core.validate;

/**
 * 校验分组 add
 * 用于Jakarta Bean Validation的分组校验功能
 * 标识新增操作的校验分组，通常用于创建数据的场景
 * 配合@Validated注解使用，实现不同场景下的差异化校验规则
 * 例如：@NotNull(groups = AddGroup.class) 表示在新增时该字段不能为空
 * 与EditGroup的区别：新增时通常所有必填字段都必须有值
 *
 * @author Lion Li
 */
// 新增场景校验分组标记接口
// 这是一个标记接口，不包含任何方法，仅用于分组标识
public interface AddGroup {
    // 该接口不包含任何方法定义，仅作为校验分组的标识
    // 在DTO或VO中配合校验注解使用，如：@NotNull(groups = AddGroup.class)
    // 通常用于新增操作，与编辑操作的校验规则可能不同
}
