// 包声明：定义当前类所在的包路径，org.dromara.workflow.mapper 表示工作流模块数据访问层
// package关键字用于声明类所在的包，便于组织和管理代码，避免命名冲突
package org.dromara.workflow.mapper;

// MyBatis-Plus核心组件：Lambda查询包装器，支持类型安全查询
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
// 数据列注解：用于数据权限控制，指定数据列
import org.dromara.common.mybatis.annotation.DataColumn;
// 数据权限注解：用于数据权限控制，指定权限类型
import org.dromara.common.mybatis.annotation.DataPermission;
// MyBatis-Plus增强Mapper：提供通用的CRUD方法
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
// 数据库助手：提供数据库相关辅助方法
import org.dromara.common.mybatis.helper.DataBaseHelper;
// 流程分类实体类：对应数据库表
import org.dromara.workflow.domain.FlowCategory;
// 流程分类视图对象：用于返回前端数据
import org.dromara.workflow.domain.vo.FlowCategoryVo;

// Java集合工具类：提供集合操作
import java.util.List;
// Java Stream API：提供流式操作
import java.util.stream.Collectors;
// Java Stream API：提供流式操作
import java.util.stream.Stream;

/**
 * 流程分类Mapper接口
 * 数据访问层：提供流程分类表的CRUD操作
 * 继承BaseMapperPlus，获得通用Mapper方法
 *
 * @author may
 * @date 2023-06-27
 */
// 继承BaseMapperPlus，泛型参数：实体类FlowCategory，返回类型FlowCategoryVo
public interface FlwCategoryMapper extends BaseMapperPlus<FlowCategory, FlowCategoryVo> {

    /**
     * 根据父流程分类ID查询其所有子流程分类的列表
     * 使用FIND_IN_SET函数查询ancestors字段包含parentId的记录
     * 只查询categoryId字段，提升查询性能
     *
     * @param parentId 父流程分类ID
     * @return 包含子流程分类的列表
     */
    // 默认方法：JDK 8+接口默认方法实现
    default List<FlowCategory> selectListByParentId(Long parentId) {
        // 创建LambdaQueryWrapper，使用Lambda表达式避免硬编码字段名
        return this.selectList(new LambdaQueryWrapper<FlowCategory>()
            // 只查询categoryId字段，减少数据传输
            .select(FlowCategory::getCategoryId)
            // 使用FIND_IN_SET函数查询ancestors字段包含parentId的记录
            .apply(DataBaseHelper.findInSet(parentId, "ancestors")));
    }

    /**
     * 根据父流程分类ID查询包括父ID及其所有子流程分类ID的列表
     * 将父ID和子ID合并为一个列表，用于IN查询
     * 使用Stream.concat合并两个流
     *
     * @param parentId 父流程分类ID
     * @return 包含父ID和子流程分类ID的列表
     */
    // 默认方法：JDK 8+接口默认方法实现
    default List<Long> selectCategoryIdsByParentId(Long parentId) {
        // 使用Stream.concat合并两个流：子分类ID流和父ID流
        return Stream.concat(
            // 查询子分类ID并转换为流
            this.selectListByParentId(parentId).stream()
                // 提取categoryId字段
                .map(FlowCategory::getCategoryId),
            // 将父ID转换为单元素流
            Stream.of(parentId)
        // 收集为列表
        ).collect(Collectors.toList());
    }

}
