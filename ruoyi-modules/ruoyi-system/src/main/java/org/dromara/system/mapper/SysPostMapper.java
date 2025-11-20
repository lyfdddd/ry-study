// 包声明：定义当前接口所在的包路径，org.dromara.system.mapper 表示系统模块数据访问层
// package关键字用于声明接口所在的包，便于组织和管理代码，避免命名冲突
package org.dromara.system.mapper;

// MyBatis-Plus核心组件：查询条件包装器，支持动态SQL构建
// Wrapper是MyBatis-Plus提供的查询条件抽象类，用于构建动态SQL查询条件
import com.baomidou.mybatisplus.core.conditions.Wrapper;
// MyBatis-Plus核心组件：Lambda查询包装器，支持类型安全查询
// LambdaQueryWrapper是MyBatis-Plus提供的Lambda表达式查询包装器，使用实体属性引用而非字符串，避免硬编码字段名，编译期检查类型安全
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
// MyBatis-Plus分页插件：分页对象
// Page是MyBatis-Plus提供的分页插件核心类，支持物理分页查询，自动处理分页逻辑
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
// 公共MyBatis注解：数据列注解，用于数据权限控制
// DataColumn是项目自定义的注解，用于标记数据权限控制的列信息
import org.dromara.common.mybatis.annotation.DataColumn;
// 公共MyBatis注解：数据权限注解，用于方法级别的数据权限控制
// DataPermission是项目自定义的注解，用于标记需要数据权限控制的方法
import org.dromara.common.mybatis.annotation.DataPermission;
// 公共MyBatis核心Mapper：增强版BaseMapper，提供VO转换能力
// BaseMapperPlus是项目封装的增强版BaseMapper，继承MyBatis-Plus的BaseMapper，提供selectVoList、selectVoPage等VO转换方法
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
// 系统领域实体类：岗位实体
// SysPost是系统岗位实体类，对应数据库的sys_post表，存储岗位信息
import org.dromara.system.domain.SysPost;
// 系统视图对象：岗位视图对象
// SysPostVo是岗位的视图对象，用于返回给前端的岗位信息
import org.dromara.system.domain.vo.SysPostVo;

// Java列表接口
// List是Java集合框架中的列表接口，有序集合，允许重复元素
import java.util.List;

/**
 * 岗位信息 数据层
 * 继承BaseMapperPlus，提供岗位表的CRUD操作和VO转换能力
 * 使用@DataPermission注解实现数据权限控制，确保用户只能查看和操作有权限的岗位数据
 * 岗位信息用于用户职位管理，支持分页查询、列表查询、数量统计等功能
 *
 * @author Lion Li
 */
// 继承BaseMapperPlus，泛型参数：SysPost表示实体类型，SysPostVo表示视图对象类型
// BaseMapperPlus提供selectVoList、selectVoPage等方法，自动将实体转换为VO对象
public interface SysPostMapper extends BaseMapperPlus<SysPost, SysPostVo> {

    /**
     * 分页查询岗位列表
     * 使用@DataPermission注解实现数据权限控制，确保用户只能查询有权限的岗位数据
     * 通过@DataColumn指定数据权限控制的列映射关系
     * 返回Page分页对象，包含分页信息和岗位列表
     *
     * @param page         分页对象
     * @param queryWrapper 查询条件
     * @return 包含岗位信息的分页结果
     */
    // 数据权限注解：控制用户只能查询有权限的岗位数据
    // @DataColumn key="deptName"表示部门名称，value="dept_id"表示部门ID字段
    // @DataColumn key="userName"表示用户名称，value="create_by"表示创建人字段
    @DataPermission({
        @DataColumn(key = "deptName", value = "dept_id"),
        @DataColumn(key = "userName", value = "create_by")
    })
    // 默认方法，调用父类的selectVoPage方法，自动将实体转换为VO对象
    default Page<SysPostVo> selectPagePostList(Page<SysPost> page, Wrapper<SysPost> queryWrapper) {
        // 调用BaseMapperPlus的selectVoPage方法，实现分页查询和VO转换
        return this.selectVoPage(page, queryWrapper);
    }

    /**
     * 查询岗位列表
     * 使用@DataPermission注解实现数据权限控制，确保用户只能查询有权限的岗位数据
     * 通过@DataColumn指定数据权限控制的列映射关系
     * 返回List集合，不分页
     *
     * @param queryWrapper 查询条件
     * @return 岗位信息列表
     */
    // 数据权限注解：控制用户只能查询有权限的岗位数据
    @DataPermission({
        @DataColumn(key = "deptName", value = "dept_id"),
        @DataColumn(key = "userName", value = "create_by")
    })
    // 默认方法，调用父类的selectVoList方法，自动将实体转换为VO对象
    default List<SysPostVo> selectPostList(Wrapper<SysPost> queryWrapper) {
        // 调用BaseMapperPlus的selectVoList方法，实现列表查询和VO转换
        return this.selectVoList(queryWrapper);
    }

    /**
     * 根据岗位ID集合查询岗位数量
     * 使用@DataPermission注解实现数据权限控制，确保用户只能统计有权限的岗位数据
     * 通过@DataColumn指定数据权限控制的列映射关系
     * 返回匹配的岗位数量
     *
     * @param postIds 岗位ID列表
     * @return 匹配的岗位数量
     */
    // 数据权限注解：控制用户只能统计有权限的岗位数据
    @DataPermission({
        @DataColumn(key = "deptName", value = "dept_id"),
        @DataColumn(key = "userName", value = "create_by")
    })
    // 默认方法，使用LambdaQueryWrapper构建查询条件
    // 使用Lambda表达式避免硬编码字段名，提升代码可维护性
    default long selectPostCount(List<Long> postIds) {
        // 创建LambdaQueryWrapper，添加岗位ID在指定列表中的条件
        return this.selectCount(new LambdaQueryWrapper<SysPost>().in(SysPost::getPostId, postIds));
    }

    /**
     * 根据用户ID查询其关联的岗位列表
     * 使用inSql方法添加子查询条件，查询用户岗位关联表
     * 返回用户关联的岗位信息列表
     *
     * @param userId 用户ID
     * @return 岗位信息列表
     */
    // 默认方法，使用LambdaQueryWrapper构建查询条件
    // 使用inSql方法添加子查询，查询sys_user_post关联表
    default List<SysPostVo> selectPostsByUserId(Long userId) {
        // 创建LambdaQueryWrapper，使用inSql添加子查询条件
        // 子查询：从sys_user_post表中查询指定用户的岗位ID
        return this.selectVoList(new LambdaQueryWrapper<SysPost>()
            .inSql(SysPost::getPostId, "select post_id from sys_user_post where user_id = " + userId));
    }

}
