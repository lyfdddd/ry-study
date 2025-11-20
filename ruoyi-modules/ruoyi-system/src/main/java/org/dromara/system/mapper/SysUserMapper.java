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
// 系统领域实体类：用户实体
// SysUser是系统用户实体类，对应数据库的sys_user表
import org.dromara.system.domain.SysUser;
// 系统导出视图对象：用户导出视图对象
// SysUserExportVo是用户导出的视图对象，用于Excel导出场景
import org.dromara.system.domain.vo.SysUserExportVo;
// 系统视图对象：用户视图对象
// SysUserVo是用户的视图对象，用于返回给前端的用户信息，不包含敏感字段
import org.dromara.system.domain.vo.SysUserVo;

// Java列表接口
// List是Java集合框架中的列表接口，有序集合，允许重复元素
import java.util.List;

/**
 * 用户表 数据层
 * 继承BaseMapperPlus，提供用户表的CRUD操作和VO转换能力
 * 使用@DataPermission注解实现数据权限控制，确保用户只能查看和操作有权限的数据
 *
 * @author Lion Li
 */
// 继承BaseMapperPlus，泛型参数：SysUser表示实体类型，SysUserVo表示视图对象类型
// BaseMapperPlus提供selectVoList、selectVoPage等方法，自动将实体转换为VO对象
public interface SysUserMapper extends BaseMapperPlus<SysUser, SysUserVo> {

    /**
     * 分页查询用户列表，并进行数据权限控制
     * 使用@DataPermission注解实现部门级和用户级的数据权限控制
     * 通过@DataColumn指定数据权限控制的列映射关系
     *
     * @param page         分页参数
     * @param queryWrapper 查询条件
     * @return 分页的用户信息
     */
    // 数据权限注解：控制用户只能查询有权限的部门数据和创建的用户数据
    // @DataColumn key="deptName"表示部门名称，value="dept_id"表示部门ID字段
    // @DataColumn key="userName"表示用户名称，value="create_by"表示创建人字段
    @DataPermission({
        @DataColumn(key = "deptName", value = "dept_id"),
        @DataColumn(key = "userName", value = "create_by")
    })
    // 默认方法，调用父类的selectVoPage方法，自动将实体转换为VO对象
    // 使用default关键字定义接口默认方法，避免每个实现类都要重复实现
    default Page<SysUserVo> selectPageUserList(Page<SysUser> page, Wrapper<SysUser> queryWrapper) {
        // 调用BaseMapperPlus的selectVoPage方法，实现分页查询和VO转换
        return this.selectVoPage(page, queryWrapper);
    }

    /**
     * 查询用户列表，并进行数据权限控制
     * 使用@DataPermission注解实现部门级和用户级的数据权限控制
     * 返回List集合，不分页
     *
     * @param queryWrapper 查询条件
     * @return 用户信息集合
     */
    // 数据权限注解：控制用户只能查询有权限的部门数据和创建的用户数据
    @DataPermission({
        @DataColumn(key = "deptName", value = "dept_id"),
        @DataColumn(key = "userName", value = "create_by")
    })
    // 默认方法，调用父类的selectVoList方法，自动将实体转换为VO对象
    default List<SysUserVo> selectUserList(Wrapper<SysUser> queryWrapper) {
        // 调用BaseMapperPlus的selectVoList方法，实现列表查询和VO转换
        return this.selectVoList(queryWrapper);
    }

    /**
     * 根据条件分页查询用户列表
     * 用于用户导出功能，返回SysUserExportVo对象
     * 使用@DataPermission注解实现数据权限控制
     *
     * @param queryWrapper 查询条件
     * @return 用户信息集合信息
     */
    // 数据权限注解：控制用户只能查询有权限的部门数据和创建的用户数据
    // 使用别名d和u分别表示部门表和用户表
    @DataPermission({
        @DataColumn(key = "deptName", value = "d.dept_id"),
        @DataColumn(key = "userName", value = "u.create_by")
    })
    // 抽象方法，由XML实现具体的SQL查询逻辑
    // 使用@Param注解指定参数名称，便于在XML中引用
    // Constants.WRAPPER是MyBatis-Plus的常量，表示查询条件包装器
    List<SysUserExportVo> selectUserExportList(@Param(Constants.WRAPPER) Wrapper<SysUser> queryWrapper);

    /**
     * 根据条件分页查询已配用户角色列表
     * 查询已分配指定角色的用户列表
     * 使用@DataPermission注解实现数据权限控制
     *
     * @param page         分页信息
     * @param queryWrapper 查询条件
     * @return 用户信息集合信息
     */
    // 数据权限注解：控制用户只能查询有权限的部门数据和创建的用户数据
    @DataPermission({
        @DataColumn(key = "deptName", value = "d.dept_id"),
        @DataColumn(key = "userName", value = "u.create_by")
    })
    // 抽象方法，由XML实现具体的SQL查询逻辑
    // 使用@Param注解指定参数名称，page表示分页参数，Constants.WRAPPER表示查询条件
    Page<SysUserVo> selectAllocatedList(@Param("page") Page<SysUser> page, @Param(Constants.WRAPPER) Wrapper<SysUser> queryWrapper);

    /**
     * 根据条件分页查询未分配用户角色列表
     * 查询未分配指定角色的用户列表
     * 使用@DataPermission注解实现数据权限控制
     *
     * @param queryWrapper 查询条件
     * @return 用户信息集合信息
     */
    // 数据权限注解：控制用户只能查询有权限的部门数据和创建的用户数据
    @DataPermission({
        @DataColumn(key = "deptName", value = "d.dept_id"),
        @DataColumn(key = "userName", value = "u.create_by")
    })
    // 抽象方法，由XML实现具体的SQL查询逻辑
    Page<SysUserVo> selectUnallocatedList(@Param("page") Page<SysUser> page, @Param(Constants.WRAPPER) Wrapper<SysUser> queryWrapper);

    /**
     * 根据用户ID统计用户数量
     * 使用数据权限控制，确保只能统计有权限的用户
     *
     * @param userId 用户ID
     * @return 用户数量
     */
    // 数据权限注解：控制用户只能统计有权限的部门数据和创建的用户数据
    @DataPermission({
        @DataColumn(key = "deptName", value = "dept_id"),
        @DataColumn(key = "userName", value = "create_by")
    })
    // 默认方法，使用LambdaQueryWrapper构建查询条件
    // 使用Lambda表达式避免硬编码字段名，提升代码可维护性
    default long countUserById(Long userId) {
        // 创建LambdaQueryWrapper，添加用户ID等于条件
        return this.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUserId, userId));
    }

    /**
     * 根据条件更新用户数据
     * 使用数据权限控制，确保只能更新有权限的用户数据
     *
     * @param user          要更新的用户实体
     * @param updateWrapper 更新条件封装器
     * @return 更新操作影响的行数
     */
    // 重写父类方法，添加数据权限注解
    @Override
    // 数据权限注解：控制用户只能更新有权限的部门数据和创建的用户数据
    @DataPermission({
        @DataColumn(key = "deptName", value = "dept_id"),
        @DataColumn(key = "userName", value = "create_by")
    })
    // 抽象方法，由MyBatis-Plus实现具体的更新逻辑
    // 使用@Param注解指定参数名称，Constants.ENTITY表示实体对象，Constants.WRAPPER表示更新条件
    int update(@Param(Constants.ENTITY) SysUser user, @Param(Constants.WRAPPER) Wrapper<SysUser> updateWrapper);

    /**
     * 根据用户ID更新用户数据
     * 使用数据权限控制，确保只能更新有权限的用户数据
     *
     * @param user 要更新的用户实体
     * @return 更新操作影响的行数
     */
    // 重写父类方法，添加数据权限注解
    @Override
    // 数据权限注解：控制用户只能更新有权限的部门数据和创建的用户数据
    @DataPermission({
        @DataColumn(key = "deptName", value = "dept_id"),
        @DataColumn(key = "userName", value = "create_by")
    })
    // 抽象方法，由MyBatis-Plus实现具体的更新逻辑
    // 使用@Param注解指定参数名称，Constants.ENTITY表示实体对象
    int updateById(@Param(Constants.ENTITY) SysUser user);

}
