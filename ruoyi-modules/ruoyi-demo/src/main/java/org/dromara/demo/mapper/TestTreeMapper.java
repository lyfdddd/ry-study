// 测试树表Mapper接口层
// 继承BaseMapperPlus，提供测试树表的数据库操作能力，在类级别启用数据权限控制
package org.dromara.demo.mapper;

// 数据权限列注解，定义数据权限字段映射
import org.dromara.common.mybatis.annotation.DataColumn;
// 数据权限注解，启用数据权限控制
import org.dromara.common.mybatis.annotation.DataPermission;
// MyBatis-Plus增强Mapper接口，提供通用CRUD方法
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
// 测试树表实体类，对应数据库表
import org.dromara.demo.domain.TestTree;
// 测试树表视图对象，返回给前端的数据格式
import org.dromara.demo.domain.vo.TestTreeVo;

/**
 * 测试树表Mapper接口
 * 继承BaseMapperPlus，提供测试树表的数据库操作能力，在类级别启用数据权限控制
 * 所有继承自BaseMapperPlus的方法都会自动应用类级别的数据权限注解
 *
 * @author Lion Li
 * @date 2021-07-26
 */
// 数据权限注解，启用数据权限控制，作用于整个Mapper接口的所有方法
// 通过@DataColumn定义数据权限字段映射：deptName->dept_id，userName->user_id
@DataPermission({
    // 数据权限列映射：部门名称映射到dept_id字段
    @DataColumn(key = "deptName", value = "dept_id"),
    // 数据权限列映射：用户名称映射到user_id字段
    @DataColumn(key = "userName", value = "user_id")
})
// 测试树表Mapper接口，继承BaseMapperPlus获取通用CRUD能力
// 泛型参数：TestTree-实体类，TestTreeVo-视图对象类
public interface TestTreeMapper extends BaseMapperPlus<TestTree, TestTreeVo> {

    // 接口体为空，所有方法继承自BaseMapperPlus
    // 包括：insert、updateById、deleteById、selectById、selectList、selectPage等
    // 由于类级别使用了@DataPermission注解，所有查询方法都会自动应用数据权限控制

}
