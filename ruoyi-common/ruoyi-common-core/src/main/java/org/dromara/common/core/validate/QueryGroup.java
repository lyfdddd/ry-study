// 校验分组接口 - 查询场景分组
package org.dromara.common.core.validate;

/**
 * 校验分组 query
 * 用于Jakarta Bean Validation的分组校验功能
 * 标识查询操作的校验分组，通常用于查询参数的场景
 * 配合@Validated注解使用，实现不同场景下的差异化校验规则
 * 例如：@NotNull(groups = QueryGroup.class) 表示在查询时该字段不能为空
 *
 * @author Lion Li
 */
// 查询场景校验分组标记接口
// 这是一个标记接口，不包含任何方法，仅用于分组标识
public interface QueryGroup {
    // 该接口不包含任何方法定义，仅作为校验分组的标识
    // 在DTO或VO中配合校验注解使用，如：@NotNull(groups = QueryGroup.class)
}
