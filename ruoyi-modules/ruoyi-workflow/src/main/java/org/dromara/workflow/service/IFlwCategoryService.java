// 包声明：定义当前服务接口所在的包路径，org.dromara.workflow.service 表示工作流模块服务层
// package关键字用于声明接口所在的包，便于组织和管理代码，避免命名冲突
package org.dromara.workflow.service;

// Hutool树形结构类：用于构建树形数据
import cn.hutool.core.lang.tree.Tree;
// 流程分类业务对象：封装流程分类的业务数据
import org.dromara.workflow.domain.bo.FlowCategoryBo;
// 流程分类视图对象：封装流程分类的响应数据
import org.dromara.workflow.domain.vo.FlowCategoryVo;

// List集合接口：用于存储列表数据
import java.util.List;

/**
 * 流程分类Service接口
 * 定义流程分类相关的业务逻辑方法
 * 包括查询、新增、修改、删除、校验等操作
 * 使用接口定义规范，实现类提供具体实现
 *
 * @author may
 */

// 流程分类服务接口
public interface IFlwCategoryService {

    /**
     * 查询流程分类
     * 根据ID查询单条流程分类记录
     *
     * @param categoryId 主键ID
     * @return 流程分类视图对象
     */
    // 根据ID查询流程分类详情
    FlowCategoryVo queryById(Long categoryId);

    /**
     * 根据流程分类ID查询流程分类名称
     * 根据ID查询流程分类的名称字段
     *
     * @param categoryId 流程分类ID
     * @return 流程分类名称
     */
    // 根据ID查询流程分类名称
    String selectCategoryNameById(Long categoryId);

    /**
     * 查询符合条件的流程分类列表
     * 根据查询条件查询所有流程分类列表
     *
     * @param bo 查询条件
     * @return 流程分类列表
     */
    // 查询流程分类列表
    List<FlowCategoryVo> queryList(FlowCategoryBo bo);

    /**
     * 查询流程分类树结构信息
     * 将流程分类列表转换为树形结构
     *
     * @param category 流程分类信息
     * @return 流程分类树信息集合
     */
    // 查询流程分类树
    List<Tree<String>> selectCategoryTreeList(FlowCategoryBo category);

    /**
     * 校验流程分类名称是否唯一
     * 检查流程分类名称是否重复
     *
     * @param category 流程分类信息
     * @return 结果 true表示唯一，false表示重复
     */
    // 校验流程分类名称唯一性
    boolean checkCategoryNameUnique(FlowCategoryBo category);

    /**
     * 查询流程分类是否存在流程定义
     * 检查流程分类是否关联了流程定义
     *
     * @param categoryId 流程分类ID
     * @return 结果 true表示存在，false表示不存在
     */
    // 校验是否关联流程定义
    boolean checkCategoryExistDefinition(Long categoryId);

    /**
     * 是否存在流程分类子节点
     * 检查流程分类是否有下级分类
     *
     * @param categoryId 流程分类ID
     * @return 结果 true表示有子节点，false表示没有
     */
    // 校验是否有子分类
    boolean hasChildByCategoryId(Long categoryId);

    /**
     * 新增流程分类
     * 插入新的流程分类记录
     *
     * @param bo 流程分类业务对象
     * @return 影响行数
     */
    // 新增流程分类
    int insertByBo(FlowCategoryBo bo);

    /**
     * 修改流程分类
     * 更新流程分类信息
     *
     * @param bo 流程分类业务对象
     * @return 影响行数
     */
    // 修改流程分类
    int updateByBo(FlowCategoryBo bo);

    /**
     * 删除流程分类信息
     * 删除流程分类记录
     *
     * @param categoryId 主键ID
     * @return 影响行数
     */
    // 删除流程分类
    int deleteWithValidById(Long categoryId);
}
