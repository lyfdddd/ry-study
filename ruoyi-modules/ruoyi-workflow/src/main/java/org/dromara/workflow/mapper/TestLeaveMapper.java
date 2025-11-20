// 包声明：定义当前类所在的包路径，org.dromara.workflow.mapper 表示工作流模块数据访问层
// package关键字用于声明类所在的包，便于组织和管理代码，避免命名冲突
package org.dromara.workflow.mapper;

// MyBatis-Plus增强Mapper：提供通用的CRUD方法
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
// 请假实体类：对应数据库表
import org.dromara.workflow.domain.TestLeave;
// 请假视图对象：用于返回前端数据
import org.dromara.workflow.domain.vo.TestLeaveVo;

/**
 * 请假Mapper接口
 * 数据访问层：提供请假表的CRUD操作
 * 继承BaseMapperPlus，获得通用Mapper方法
 * 用于请假流程的业务数据持久化
 *
 * @author may
 * @date 2023-07-21
 */
// 继承BaseMapperPlus，泛型参数：实体类TestLeave，返回类型TestLeaveVo
public interface TestLeaveMapper extends BaseMapperPlus<TestLeave, TestLeaveVo> {

    // 继承BaseMapperPlus的所有方法，无需额外实现
    // 包括：selectById, insert, updateById, deleteById等通用CRUD方法
    // 请假流程的业务数据通过此Mapper进行持久化操作

}
