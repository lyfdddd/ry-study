package org.dromara.generator.mapper;

// 导入MyBatis-Plus的拦截器忽略注解，用于关闭数据权限和租户隔离
// @InterceptorIgnore注解可以关闭MyBatis-Plus的拦截器功能，包括数据权限、多租户等
import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
// 导入MyBatis-Plus增强Mapper接口，提供通用CRUD方法
// BaseMapperPlus继承自MyBatis-Plus的BaseMapper，提供了增强的CRUD能力
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
// 导入代码生成表字段实体类
import org.dromara.generator.domain.GenTableColumn;

/**
 * 代码生成业务字段 数据层
 * Mapper接口，继承BaseMapperPlus获得通用CRUD能力
 * 使用@InterceptorIgnore注解关闭数据权限和租户隔离，因为代码生成配置属于系统级配置，不需要数据隔离
 * 代码生成配置是全局共享的，不应该受数据权限和租户隔离的限制
 *
 * @author Lion Li
 */
// 使用@InterceptorIgnore注解关闭数据权限和租户隔离
// dataPermission = "true"表示忽略数据权限拦截器，不进行数据权限过滤
// tenantLine = "true"表示忽略多租户拦截器，不自动添加租户ID条件
@InterceptorIgnore(dataPermission = "true", tenantLine = "true")
public interface GenTableColumnMapper extends BaseMapperPlus<GenTableColumn, GenTableColumn> {
    // BaseMapperPlus已提供通用CRUD方法，无需额外定义
    // 包括：insert（插入）、delete（删除）、update（更新）、selectById（根据ID查询）、selectList（查询列表）、selectPage（分页查询）等
    // 继承BaseMapperPlus后，可以直接使用这些通用方法，无需手动编写SQL
}
