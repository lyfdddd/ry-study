// 包声明：定义当前接口所在的包路径，org.dromara.system.mapper 表示系统模块数据访问层
// package关键字用于声明接口所在的包，便于组织和管理代码，避免命名冲突
package org.dromara.system.mapper;

// MyBatis-Plus核心组件：查询条件包装器，支持动态SQL构建
// Wrapper是MyBatis-Plus提供的查询条件抽象类，用于构建动态SQL查询条件
import com.baomidou.mybatisplus.core.conditions.Wrapper;
// MyBatis-Plus核心组件：Lambda查询包装器，支持类型安全查询
// LambdaQueryWrapper是MyBatis-Plus提供的Lambda表达式查询包装器，使用实体属性引用而非字符串，避免硬编码字段名，编译期检查类型安全
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
// MyBatis-Plus核心常量：MyBatis-Plus核心常量定义
// Constants是MyBatis-Plus提供的常量类，包含WRAPPER、ENTITY等常量，用于Mapper方法参数注解
import com.baomidou.mybatisplus.core.toolkit.Constants;
// MyBatis-Plus分页插件：分页对象
// Page是MyBatis-Plus提供的分页插件核心类，支持物理分页查询，自动处理分页逻辑
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
// MyBatis注解：参数注解，用于指定参数名称
// Param是MyBatis提供的注解，用于为Mapper方法参数指定名称，便于在XML中引用
import org.apache.ibatis.annotations.Param;
// 公共MyBatis注解：数据列注解，用于数据权限控制
// DataColumn是项目自定义的注解，用于标记数据权限控制的列信息
import org.dromara.common.mybatis.annotation.DataColumn;
// 公共MyBatis注解：数据权限注解，用于方法级别的数据权限控制
// DataPermission是项目自定义的注解，用于标记需要数据权限控制的方法
import org.dromara.common.mybatis.annotation.DataPermission;
// 公共MyBatis核心Mapper：增强版BaseMapper，提供VO转换能力
// BaseMapperPlus是项目封装的增强版BaseMapper，继承MyBatis-Plus的BaseMapper，提供selectVoList、selectVoPage等VO转换方法
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
// 系统领域实体类：角色实体
// SysRole是系统角色实体类，对应数据库的sys_role表
import org.dromara.system.domain.SysRole;
// 系统视图对象：角色视图对象
// SysRoleVo是角色的视图对象，用于返回给前端的角色信息
import org.dromara.system.domain.vo.SysRoleVo;

// Java列表接口
// List是Java集合框架中的列表接口，有序集合，允许重复元素
import java.util.List;

/**
 * 角色表 数据层
 * 继承BaseMapperPlus，提供角色表的CRUD操作和VO转换能力
 * 使用@DataPermission注解实现数据权限控制，确保用户只能查看和操作有权限的数据
 *
 * @author Lion Li
 */
// 继承BaseMapperPlus，泛型参数：SysRole表示实体类型，SysRoleVo表示视图对象类型
// BaseMapperPlus提供selectVoList、selectVoPage等方法，自动将实体转换为VO对象
public interface SysRoleMapper extends BaseMapperPlus<SysRole, SysRoleVo> {

    /**
     * 构建根据用户ID查询角色ID的SQL子查询
     * 生成SQL子查询语句，用于查询用户关联的角色ID
     * 通常配合inSql方法使用，实现子查询逻辑
     *
     * @param userId 用户ID
     * @return 查询用户对应角色ID的SQL语句字符串
     */
    // 默认方法，使用Java 15+的文本块语法（"""）构建SQL语句
    // 使用formatted方法格式化字符串，将userId插入SQL中
    default String buildRoleByUserSql(Long userId) {
        // 构建子查询SQL：从sys_user_role表中查询指定用户的角色ID
        return """
                select role_id from sys_user_role where user_id = %d
            """.formatted(userId);
    }

    /**
     * 分页查询角色列表
     * 使用@DataPermission注解实现部门级和用户级的数据权限控制
     * 通过@DataColumn指定数据权限控制的列映射关系
     *
     * @param page         分页对象
     * @param queryWrapper 查询条件
     * @return 包含角色信息的分页结果
     */
    // 数据权限注解：控制用户只能查询有权限的部门数据和创建的用户数据
    // @DataColumn key="deptName"表示部门名称，value="create_dept"表示创建部门字段
    // @DataColumn key="userName"表示用户名称，value="create_by"表示创建人字段
    @DataPermission({
        @DataColumn(key = "deptName", value = "create_dept"),
        @DataColumn(key = "userName", value = "create_by")
    })
    // 默认方法，调用父类的selectVoPage方法，自动将实体转换为VO对象
    // 使用@Param注解指定参数名称，page表示分页参数，Constants.WRAPPER表示查询条件
    default Page<SysRoleVo> selectPageRoleList(@Param("page") Page<SysRole> page, @Param(Constants.WRAPPER) Wrapper<SysRole> queryWrapper) {
        // 调用BaseMapperPlus的selectVoPage方法，实现分页查询和VO转换
        return this.selectVoPage(page, queryWrapper);
    }

    /**
     * 根据条件查询角色数据
     * 使用@DataPermission注解实现部门级和用户级的数据权限控制
     * 返回List集合，不分页
     *
     * @param queryWrapper 查询条件
     * @return 角色数据集合信息
     */
    // 数据权限注解：控制用户只能查询有权限的部门数据和创建的用户数据
    @DataPermission({
        @DataColumn(key = "deptName", value = "create_dept"),
        @DataColumn(key = "userName", value = "create_by")
    })
    // 默认方法，调用父类的selectVoList方法，自动将实体转换为VO对象
    default List<SysRoleVo> selectRoleList(@Param(Constants.WRAPPER) Wrapper<SysRole> queryWrapper) {
        // 调用BaseMapperPlus的selectVoList方法，实现列表查询和VO转换
        return this.selectVoList(queryWrapper);
    }

    /**
     * 根据角色ID集合查询角色数量
     * 使用数据权限控制，确保只能统计有权限的角色
     *
     * @param roleIds 角色ID列表
     * @return 匹配的角色数量
     */
    // 数据权限注解：控制用户只能统计有权限的部门数据和创建的用户数据
    @DataPermission({
        @DataColumn(key = "deptName", value = "create_dept"),
        @DataColumn(key = "userName", value = "create_by")
    })
    // 默认方法，使用LambdaQueryWrapper构建查询条件
    // 使用Lambda表达式避免硬编码字段名，提升代码可维护性
    default long selectRoleCount(List<Long> roleIds) {
        // 创建LambdaQueryWrapper，添加角色ID在列表中的条件
        return this.selectCount(new LambdaQueryWrapper<SysRole>().in(SysRole::getRoleId, roleIds));
    }

    /**
     * 根据角色ID查询角色信息
     * 使用数据权限控制，确保只能查询有权限的角色
     *
     * @param roleId 角色ID
     * @return 对应的角色信息
     */
    // 数据权限注解：控制用户只能查询有权限的部门数据和创建的用户数据
    @DataPermission({
        @DataColumn(key = "deptName", value = "create_dept"),
        @DataColumn(key = "userName", value = "create_by")
    })
    // 默认方法，调用父类的selectVoById方法，自动将实体转换为VO对象
    default SysRoleVo selectRoleById(Long roleId) {
        // 调用BaseMapperPlus的selectVoById方法，实现单条查询和VO转换
        return this.selectVoById(roleId);
    }

    /**
     * 根据用户ID查询角色
     * 查询指定用户拥有的角色列表
     * 使用buildRoleByUserSql构建子查询，查询用户角色关联表
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    // 默认方法，使用LambdaQueryWrapper构建查询条件
    // 使用select方法指定查询字段，避免查询不必要的数据，提升性能
    default List<SysRoleVo> selectRolesByUserId(Long userId) {
        // 创建LambdaQueryWrapper，指定查询字段（角色ID、名称、权限字符、排序、数据范围、状态）
        // 使用inSql方法添加子查询条件，查询用户拥有的角色
        return this.selectVoList(new LambdaQueryWrapper<SysRole>()
            .select(SysRole::getRoleId, SysRole::getRoleName, SysRole::getRoleKey,
                SysRole::getRoleSort, SysRole::getDataScope, SysRole::getStatus)
            .inSql(SysRole::getRoleId, this.buildRoleByUserSql(userId)));
    }

}
