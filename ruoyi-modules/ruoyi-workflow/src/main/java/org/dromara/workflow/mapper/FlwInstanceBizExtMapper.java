// 包声明：定义当前类所在的包路径，org.dromara.workflow.mapper 表示工作流模块数据访问层
// package关键字用于声明类所在的包，便于组织和管理代码，避免命名冲突
package org.dromara.workflow.mapper;

// Hutool工具类：对象判断工具，用于空值判断、类型转换等
import cn.hutool.core.util.ObjectUtil;
// MyBatis-Plus核心组件：Lambda查询包装器，支持类型安全查询
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
// MyBatis-Plus增强Mapper：提供通用的CRUD方法
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
// 流程实例业务扩展实体类：对应数据库表
import org.dromara.workflow.domain.FlowInstanceBizExt;

// Java集合工具类：提供集合操作
import java.util.List;

/**
 * 流程实例业务扩展Mapper接口
 * 数据访问层：提供流程实例业务扩展表的CRUD操作
 * 继承BaseMapperPlus，获得通用Mapper方法
 *
 * @author may
 * @date 2025-08-05
 */
// 继承BaseMapperPlus，泛型参数：实体类FlowInstanceBizExt，返回类型FlowInstanceBizExt
public interface FlwInstanceBizExtMapper extends BaseMapperPlus<FlowInstanceBizExt, FlowInstanceBizExt> {

    /**
     * 根据 instanceId 保存或更新流程实例业务扩展
     * 先查询是否存在，存在则更新，不存在则插入
     * 实现幂等操作，确保数据一致性
     *
     * @param entity 流程实例业务扩展实体
     * @return 操作是否成功
     */
    // 默认方法：JDK 8+接口默认方法实现
    default int saveOrUpdateByInstanceId(FlowInstanceBizExt entity) {
        // 查询是否存在相同instanceId的记录
        FlowInstanceBizExt exist = this.selectOne(new LambdaQueryWrapper<FlowInstanceBizExt>()
            .eq(FlowInstanceBizExt::getInstanceId, entity.getInstanceId()));

        // 如果记录已存在
        if (ObjectUtil.isNotNull(exist)) {
            // 设置主键ID，执行更新操作
            entity.setId(exist.getId());
            return updateById(entity);
        } else {
            // 记录不存在，执行插入操作
            return insert(entity);
        }
    }

    /**
     * 按照流程实例ID删除单个流程实例业务扩展
     * 根据instanceId精确删除记录
     *
     * @param instanceId 流程实例ID
     * @return 删除的行数
     */
    // 默认方法：JDK 8+接口默认方法实现
    default int deleteByInstId(Long instanceId) {
        // 创建LambdaQueryWrapper，使用Lambda表达式避免硬编码字段名
        return this.delete(new LambdaQueryWrapper<FlowInstanceBizExt>()
            // 设置删除条件：instanceId等于指定值
            .eq(FlowInstanceBizExt::getInstanceId, instanceId));
    }

    /**
     * 按照流程实例ID批量删除流程实例业务扩展
     * 根据instanceId列表批量删除记录
     *
     * @param instanceIds 流程实例ID列表
     * @return 删除的行数
     */
    // 默认方法：JDK 8+接口默认方法实现
    default int deleteByInstIds(List<Long> instanceIds) {
        // 创建LambdaQueryWrapper，使用Lambda表达式避免硬编码字段名
        return this.delete(new LambdaQueryWrapper<FlowInstanceBizExt>()
            // 设置删除条件：instanceId在指定列表中
            .in(FlowInstanceBizExt::getInstanceId, instanceIds));
    }

}
