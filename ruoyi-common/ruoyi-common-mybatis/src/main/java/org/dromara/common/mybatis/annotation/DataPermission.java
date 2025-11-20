package org.dromara.common.mybatis.annotation;

// 导入Java注解相关类
import java.lang.annotation.*;

/**
 * 数据权限组注解，用于标记数据权限配置数组
 * <p>
 * 可以包含多个@DataColumn配置，实现多维度数据权限控制
 * 例如：同时控制部门权限和租户权限
 * </p>
 *
 * 使用场景：
 * 1. 在Mapper接口方法上标注：@DataPermission({@DataColumn(key = "deptName", value = "dept_id")})
 * 2. 在Mapper接口类上标注：作用于该接口的所有方法
 * 3. 支持多个数据权限规则：@DataPermission({@DataColumn(...), @DataColumn(...)})
 *
 * @author Lion Li
 * @version 3.5.0
 */
// 注解作用目标：方法级别和类级别
@Target({ElementType.METHOD, ElementType.TYPE})
// 注解保留策略：运行时保留，可以通过反射读取
@Retention(RetentionPolicy.RUNTIME)
// 注解文档化：生成Javadoc时会包含此注解信息
@Documented
public @interface DataPermission {

    /**
     * 数据权限配置数组，用于指定数据权限的占位符关键字和替换值
     * 每个@DataColumn定义一个数据权限规则
     *
     * @return DataColumn数组，包含一个或多个数据权限配置
     */
    DataColumn[] value();

    /**
     * 权限拼接标识符（用于指定连接语句的SQL逻辑运算符）
     * 控制多个数据权限规则之间的连接方式
     *
     * 如不填，默认规则：
     * - SELECT查询语句使用 OR 连接
     * - 其他语句（UPDATE/DELETE）使用 AND 连接
     *
     * 可填内容：
     * - "OR"：使用OR连接，满足任意一个条件即可
     * - "AND"：使用AND连接，必须同时满足所有条件
     *
     * @return SQL连接符，默认为空字符串（使用默认规则）
     */
    String joinStr() default "";

}
