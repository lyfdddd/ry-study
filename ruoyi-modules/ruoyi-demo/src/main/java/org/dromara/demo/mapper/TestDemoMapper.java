// 测试单表Mapper接口层
// 继承BaseMapperPlus，提供测试单表的数据库操作能力，包括数据权限控制
package org.dromara.demo.mapper;

// MyBatis-Plus查询条件包装器，用于构建动态SQL
import com.baomidou.mybatisplus.core.conditions.Wrapper;
// MyBatis-Plus分页接口，定义分页查询规范
import com.baomidou.mybatisplus.core.metadata.IPage;
// MyBatis-Plus常量类，包含常用参数名
import com.baomidou.mybatisplus.core.toolkit.Constants;
// MyBatis-Plus分页实现类，提供分页查询能力
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
// MyBatis注解，用于指定参数名称
import org.apache.ibatis.annotations.Param;
// 数据权限列注解，定义数据权限字段映射
import org.dromara.common.mybatis.annotation.DataColumn;
// 数据权限注解，启用数据权限控制
import org.dromara.common.mybatis.annotation.DataPermission;
// MyBatis-Plus增强Mapper接口，提供通用CRUD方法
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
// 测试单表实体类，对应数据库表
import org.dromara.demo.domain.TestDemo;
// 测试单表视图对象，返回给前端的数据格式
import org.dromara.demo.domain.vo.TestDemoVo;

// 可序列化接口，用于集合参数
import java.io.Serializable;
// 集合接口
import java.util.Collection;
// List接口
import java.util.List;

/**
 * 测试单表Mapper接口
 * 继承BaseMapperPlus，提供测试单表的数据库操作能力，包括数据权限控制
 *
 * @author Lion Li
 * @date 2021-07-26
 */
// 测试单表Mapper接口
public interface TestDemoMapper extends BaseMapperPlus<TestDemo, TestDemoVo> {

    /**
     * 自定义分页查询
     * 使用@DataPermission注解启用数据权限控制，限制用户只能查看有权限的数据
     * 通过@DataColumn定义数据权限字段映射：deptName->dept_id，userName->user_id
     *
     * @param page 分页对象
     * @param wrapper 查询条件包装器
     * @return 分页结果
     */
    // 数据权限注解，启用数据权限控制
    @DataPermission({
        // 数据权限列映射：部门名称映射到dept_id字段
        @DataColumn(key = "deptName", value = "dept_id"),
        // 数据权限列映射：用户名称映射到user_id字段
        @DataColumn(key = "userName", value = "user_id")
    })
    // 自定义分页查询方法
    Page<TestDemoVo> customPageList(@Param("page") Page<TestDemo> page, @Param("ew") Wrapper<TestDemo> wrapper);

    /**
     * 重写父类的分页查询方法，添加数据权限控制
     * 使用默认的joinStr="OR"连接多个数据权限条件
     *
     * @param page 分页对象
     * @param wrapper 查询条件包装器
     * @return 分页结果
     */
    @Override
    // 数据权限注解，启用数据权限控制
    @DataPermission({
        // 部门数据权限
        @DataColumn(key = "deptName", value = "dept_id"),
        // 用户数据权限
        @DataColumn(key = "userName", value = "user_id")
    })
    // 默认实现，调用父类的selectVoPage方法
    default <P extends IPage<TestDemoVo>> P selectVoPage(IPage<TestDemo> page, Wrapper<TestDemo> wrapper) {
        // 调用父类方法，指定返回类型为当前VO类
        return selectVoPage(page, wrapper, this.currentVoClass());
    }

    /**
     * 重写父类的列表查询方法，添加数据权限控制
     * 使用默认的joinStr="OR"连接多个数据权限条件
     *
     * @param wrapper 查询条件包装器
     * @return 列表结果
     */
    @Override
    // 数据权限注解，启用数据权限控制
    @DataPermission({
        // 部门数据权限
        @DataColumn(key = "deptName", value = "dept_id"),
        // 用户数据权限
        @DataColumn(key = "userName", value = "user_id")
    })
    // 默认实现，调用父类的selectVoList方法
    default List<TestDemoVo> selectVoList(Wrapper<TestDemo> wrapper) {
        // 调用父类方法，指定返回类型为当前VO类
        return selectVoList(wrapper, this.currentVoClass());
    }

    /**
     * 重写父类的根据ID集合查询方法，添加数据权限控制
     * 使用joinStr="AND"连接多个数据权限条件（更严格）
     *
     * @param idList ID集合
     * @return 实体类列表
     */
    @Override
    // 数据权限注解，启用数据权限控制，使用AND连接条件
    @DataPermission(value = {
        // 部门数据权限
        @DataColumn(key = "deptName", value = "dept_id"),
        // 用户数据权限
        @DataColumn(key = "userName", value = "user_id")
    }, joinStr = "AND")
    // 根据ID集合查询，参数使用MyBatis-Plus常量COLL
    List<TestDemo> selectByIds(@Param(Constants.COLL) Collection<? extends Serializable> idList);

    /**
     * 重写父类的根据ID更新方法，添加数据权限控制
     * 确保用户只能更新有权限的数据
     *
     * @param entity 实体类对象
     * @return 更新影响的行数
     */
    @Override
    // 数据权限注解，启用数据权限控制
    @DataPermission({
        // 部门数据权限
        @DataColumn(key = "deptName", value = "dept_id"),
        // 用户数据权限
        @DataColumn(key = "userName", value = "user_id")
    })
    // 根据ID更新，参数使用MyBatis-Plus常量ENTITY
    int updateById(@Param(Constants.ENTITY) TestDemo entity);

}
