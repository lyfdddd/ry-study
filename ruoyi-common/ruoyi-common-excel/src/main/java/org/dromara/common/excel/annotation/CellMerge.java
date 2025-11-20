package org.dromara.common.excel.annotation;

import org.dromara.common.excel.core.CellMergeStrategy;

import java.lang.annotation.*;

/**
 * Excel列单元格合并注解（合并列相同项）
 * 用于标记需要自动合并单元格的字段，当该列相邻行数据相同时自动合并
 * 需搭配 {@link CellMergeStrategy} 策略使用，在Excel导出时生效
 *
 * @author Lion Li
 */
// @Target指定注解作用目标为字段（FIELD），表示该注解只能用在类的属性上
@Target(ElementType.FIELD)
// @Retention指定注解保留策略为运行时（RUNTIME），确保在运行期可以通过反射读取
@Retention(RetentionPolicy.RUNTIME)
// @Inherited表示该注解具有继承性，子类会继承父类的该注解
@Inherited
public @interface CellMerge {

	/**
	 * 列索引，指定该字段在Excel中的列位置
	 * 默认值为-1，表示自动根据字段顺序排列
	 * @return 列索引值
	 */
	int index() default -1;

    /**
     * 合并需要依赖的其他字段名称数组
     * 当这些依赖字段的值也相同时才进行合并，确保合并的准确性
     * 例如：按部门合并时，可能需要同时依赖部门和日期字段
     * @return 依赖字段名称数组
     */
    String[] mergeBy() default {};

}
