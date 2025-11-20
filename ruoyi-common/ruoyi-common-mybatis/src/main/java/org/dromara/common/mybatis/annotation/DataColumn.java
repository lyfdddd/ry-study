package org.dromara.common.mybatis.annotation;

// 导入Java注解相关类
import java.lang.annotation.*;

/**
 * 数据权限列注解，用于标记数据权限的占位符关键字和替换值
 * <p>
 * 一个@DataColumn注解只能对应一个数据权限模板
 * 通常与@DataPermission注解配合使用，@DataPermission可以包含多个@DataColumn
 * </p>
 *
 * 使用场景：在Mapper接口方法上标注，指定该方法需要进行数据权限过滤
 * 例如：@DataColumn(key = "deptName", value = "dept_id") 表示将SQL中的#{deptName}占位符替换为dept_id字段
 *
 * @author Lion Li
 * @version 3.5.0
 */
// 注解作用目标：方法级别
@Target(ElementType.METHOD)
// 注解保留策略：运行时保留，可以通过反射读取
@Retention(RetentionPolicy.RUNTIME)
// 注解文档化：生成Javadoc时会包含此注解信息
@Documented
public @interface DataColumn {

    /**
     * 数据权限模板的占位符关键字数组
     * 在SQL模板中使用这些关键字作为占位符，例如：#{deptName}
     *
     * @return 占位符关键字数组，默认为 ["deptName"]
     */
    String[] key() default "deptName";

    /**
     * 数据权限模板的占位符替换值数组
     * 这些值会替换SQL模板中的对应关键字占位符
     *
     * @return 占位符替换值数组，默认为 ["dept_id"]
     */
    String[] value() default "dept_id";

    /**
     * 权限标识符
     * 用于通过菜单权限标识符来获取数据权限规则
     * 拥有此标识符的角色将不会拼接该角色的数据过滤SQL
     * 实现原理：在DataPermissionHelper中判断当前用户角色是否包含此标识符，如果包含则跳过数据权限过滤
     *
     * @return 权限标识符，默认为空字符串
     */
    String permission() default "";
}
