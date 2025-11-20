// 包声明：定义当前类所在的包路径，org.dromara.workflow.mapper 表示工作流模块数据访问层
// package关键字用于声明类所在的包，便于组织和管理代码，避免命名冲突
package org.dromara.workflow.mapper;

// 流程Spel实体类：对应数据库表
import org.dromara.workflow.domain.FlowSpel;
// 流程Spel视图对象：用于返回前端数据
import org.dromara.workflow.domain.vo.FlowSpelVo;
// MyBatis-Plus增强Mapper：提供通用的CRUD方法
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

/**
 * 流程Spel表达式定义Mapper接口
 * 数据访问层：提供流程Spel表达式表的CRUD操作
 * 继承BaseMapperPlus，获得通用Mapper方法
 * Spel（Spring Expression Language）用于动态表达式配置
 *
 * @author Michelle.Chung
 * @date 2025-07-04
 */
// 继承BaseMapperPlus，泛型参数：实体类FlowSpel，返回类型FlowSpelVo
public interface FlwSpelMapper extends BaseMapperPlus<FlowSpel, FlowSpelVo> {

    // 继承BaseMapperPlus的所有方法，无需额外实现
    // 包括：selectById, insert, updateById, deleteById等通用CRUD方法

}
