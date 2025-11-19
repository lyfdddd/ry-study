// 包声明：定义当前类所在的包路径，org.dromara.workflow.service.impl 表示工作流模块服务实现层
package org.dromara.workflow.service.impl;

// Hutool工具类：集合操作工具，提供集合判空、转换等方法
import cn.hutool.core.collection.CollUtil;
// Hutool工具类：类型转换工具，支持各种类型之间的转换
import cn.hutool.core.convert.Convert;
// Hutool工具类：树结构工具，用于构建树形结构
import cn.hutool.core.lang.tree.Tree;
// Hutool工具类：对象判断工具，用于空值判断、类型转换等
import cn.hutool.core.util.ObjectUtil;
// MyBatis-Plus核心组件：Lambda查询包装器，支持类型安全查询
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
// MyBatis-Plus核心组件：查询条件构建工具类
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
// Lombok注解：自动生成包含final字段的构造函数，实现依赖注入
import lombok.RequiredArgsConstructor;
// 公共核心常量：系统常量定义
import org.dromara.common.core.constant.SystemConstants;
// 公共核心异常：业务异常类，用于抛出业务逻辑错误
import org.dromara.common.core.exception.ServiceException;
// 公共核心工具类：MapStruct对象转换工具、Stream流操作工具、字符串操作工具等
import org.dromara.common.core.utils.*;
// MyBatis-Plus数据库助手：提供数据库相关辅助方法
import org.dromara.common.mybatis.helper.DataBaseHelper;
// Warm-Flow流程引擎核心服务：流程定义服务
import org.dromara.warm.flow.core.service.DefService;
// Warm-Flow流程引擎ORM实体：流程定义实体
import org.dromara.warm.flow.orm.entity.FlowDefinition;
// Warm-Flow流程引擎UI服务：分类服务接口
import org.dromara.warm.flow.ui.service.CategoryService;
// 工作流公共组件：条件启用注解（当工作流功能开启时才加载）
import org.dromara.workflow.common.ConditionalOnEnable;
// 工作流公共常量：流程常量定义
import org.dromara.workflow.common.constant.FlowConstant;
// 工作流领域模型：流程分类实体类
import org.dromara.workflow.domain.FlowCategory;
// 工作流业务对象：流程分类业务对象，用于接收前端参数
import org.dromara.workflow.domain.bo.FlowCategoryBo;
// 工作流视图对象：流程分类视图对象，用于返回前端数据
import org.dromara.workflow.domain.vo.FlowCategoryVo;
// 工作流Mapper接口：流程分类Mapper
import org.dromara.workflow.mapper.FlwCategoryMapper;
// 工作流服务接口：流程分类服务接口
import org.dromara.workflow.service.IFlwCategoryService;
// Spring缓存注解：缓存清除，用于数据变更时清除缓存
import org.springframework.cache.annotation.CacheEvict;
// Spring缓存注解：缓存查询，用于查询时缓存结果
import org.springframework.cache.annotation.Cacheable;
// Spring服务注解：标记为服务类，交由Spring容器管理
import org.springframework.stereotype.Service;
// Spring事务注解：声明事务边界，异常时回滚
import org.springframework.transaction.annotation.Transactional;

// Java集合工具类：提供集合操作
import java.util.ArrayList;
import java.util.List;

/**
 * 流程分类服务实现类
 * 核心业务：流程分类管理、树形结构构建、流程定义关联查询
 * 实现接口：IFlwCategoryService（流程分类服务）、CategoryService（Warm-Flow分类服务）
 *
 * @author may
 */
// 条件启用注解：当工作流功能开启时才加载该服务
@ConditionalOnEnable
// Lombok注解：自动生成包含所有final字段的构造函数，实现依赖注入
@RequiredArgsConstructor
// Spring服务注解：标记为服务类，交由Spring容器管理
@Service
public class FlwCategoryServiceImpl implements IFlwCategoryService, CategoryService {

    // Warm-Flow流程定义服务，用于查询流程定义信息
    private final DefService defService;
    // 流程分类Mapper，继承BaseMapperPlus，提供流程分类表CRUD操作
    private final FlwCategoryMapper baseMapper;

    /**
     * 根据ID查询流程分类详情
     * 查询指定流程分类的详细信息
     *
     * @param categoryId 流程分类ID
     * @return 流程分类视图对象
     */
    @Override
    public FlowCategoryVo queryById(Long categoryId) {
        // 调用Mapper查询流程分类VO对象
        return baseMapper.selectVoById(categoryId);
    }

    /**
     * 根据流程分类ID查询流程分类名称
     * 使用Redis缓存提升性能，缓存key为流程分类ID
     *
     * @param categoryId 流程分类ID
     * @return 流程分类名称
     */
    // Spring缓存注解：查询时缓存结果，key为流程分类ID
    @Cacheable(cacheNames = FlowConstant.FLOW_CATEGORY_NAME, key = "#categoryId")
    @Override
    public String selectCategoryNameById(Long categoryId) {
        // 如果流程分类ID为空，返回null
        if (ObjectUtil.isNull(categoryId)) {
            return null;
        }
        // 查询流程分类，只查询分类名称字段
        FlowCategory category = baseMapper.selectOne(new LambdaQueryWrapper<FlowCategory>()
            .select(FlowCategory::getCategoryName).eq(FlowCategory::getCategoryId, categoryId));
        // 使用ObjectUtils安全获取分类名称，避免空指针
        return ObjectUtils.notNullGetter(category, FlowCategory::getCategoryName);
    }

    /**
     * 查询符合条件的流程分类列表
     * 根据查询条件查询流程分类列表
     *
     * @param bo 查询条件业务对象
     * @return 流程分类视图对象列表
     */
    @Override
    public List<FlowCategoryVo> queryList(FlowCategoryBo bo) {
        // 构建查询条件
        LambdaQueryWrapper<FlowCategory> lqw = buildQueryWrapper(bo);
        // 调用Mapper查询流程分类VO列表
        return baseMapper.selectVoList(lqw);
    }

    /**
     * 查询流程分类树结构信息
     * 将流程分类列表转换为树形结构，用于前端下拉树组件
     *
     * @param category 流程分类查询条件
     * @return 流程分类树信息集合
     */
    @Override
    public List<Tree<String>> selectCategoryTreeList(FlowCategoryBo category) {
        // 查询流程分类列表
        List<FlowCategoryVo> categoryList = this.queryList(category);
        // 如果列表为空，返回空列表
        if (CollUtil.isEmpty(categoryList)) {
            return CollUtil.newArrayList();
        }
        // 使用TreeBuildUtils工具类构建多根节点树
        return TreeBuildUtils.buildMultiRoot(
            categoryList, // 流程分类列表
            node -> Convert.toStr(node.getCategoryId()), // ID获取函数（转换为String）
            node -> Convert.toStr(node.getParentId()), // 父ID获取函数（转换为String）
            (node, treeNode) -> treeNode // 节点转换函数
                .setId(Convert.toStr(node.getCategoryId())) // 设置节点ID
                .setParentId(Convert.toStr(node.getParentId())) // 设置父节点ID
                .setName(node.getCategoryName()) // 设置节点名称
                .setWeight(node.getOrderNum()) // 设置排序权重
        );
    }

    /**
     * 工作流查询分类（Warm-Flow接口实现）
     * 查询所有流程分类，转换为Warm-Flow框架所需的树结构
     *
     * @return 分类树结构列表
     */
    @Override
    public List<org.dromara.warm.flow.core.dto.Tree> queryCategory() {
        // 查询所有流程分类
        List<FlowCategoryVo> list = this.queryList(new FlowCategoryBo());
        // 使用StreamUtils转换为Warm-Flow框架的树结构
        return StreamUtils.toList(list, category -> new org.dromara.warm.flow.core.dto.Tree()
            .setId(Convert.toStr(category.getCategoryId())) // 设置ID（转换为String）
            .setName(category.getCategoryName()) // 设置名称
            .setParentId(Convert.toStr(category.getParentId())) // 设置父ID（转换为String）
        );
    }

    /**
     * 校验流程分类名称是否唯一
     * 检查同一父分类下分类名称是否重复
     *
     * @param category 流程分类信息
     * @return 结果 true唯一 false不唯一
     */
    @Override
    public boolean checkCategoryNameUnique(FlowCategoryBo category) {
        // 查询是否存在同名分类（同一父分类下）
        boolean exist = baseMapper.exists(new LambdaQueryWrapper<FlowCategory>()
            // 分类名称相同
            .eq(FlowCategory::getCategoryName, category.getCategoryName())
            // 父分类ID相同
            .eq(FlowCategory::getParentId, category.getParentId())
            // 排除当前分类ID（编辑时）
            .ne(ObjectUtil.isNotNull(category.getCategoryId()), FlowCategory::getCategoryId, category.getCategoryId()));
        // 返回是否唯一（不存在重复）
        return !exist;
    }

    /**
     * 查询流程分类是否存在流程定义
     * 判断指定分类下是否有流程定义，用于删除前的校验
     *
     * @param categoryId 流程分类ID
     * @return 结果 true存在 false不存在
     */
    @Override
    public boolean checkCategoryExistDefinition(Long categoryId) {
        // 创建流程定义查询对象
        FlowDefinition definition = new FlowDefinition();
        // 设置分类ID（转换为String）
        definition.setCategory(categoryId.toString());
        // 调用Warm-Flow服务查询是否存在流程定义
        return defService.exists(definition);
    }

    /**
     * 是否存在流程分类子节点
     * 查询指定分类是否有子分类
     *
     * @param categoryId 流程分类ID
     * @return 结果 true存在 false不存在
     */
    @Override
    public boolean hasChildByCategoryId(Long categoryId) {
        // 使用exists方法判断是否存在子分类
        return baseMapper.exists(new LambdaQueryWrapper<FlowCategory>()
            // 父分类ID等于指定分类ID
            .eq(FlowCategory::getParentId, categoryId));
    }

    /**
     * 构建流程分类查询条件
     * 私有方法，封装通用查询逻辑，支持Lambda表达式
     *
     * @param bo 流程分类查询条件
     * @return LambdaQueryWrapper，类型安全，避免硬编码字段名
     */
    private LambdaQueryWrapper<FlowCategory> buildQueryWrapper(FlowCategoryBo bo) {
        // 创建LambdaQueryWrapper，使用实体属性引用，编译期检查
        LambdaQueryWrapper<FlowCategory> lqw = Wrappers.lambdaQuery();
        // 删除标志为正常（未删除）
        lqw.eq(FlowCategory::getDelFlag, SystemConstants.NORMAL);
        // 精确查询分类ID
        lqw.eq(ObjectUtil.isNotNull(bo.getCategoryId()), FlowCategory::getCategoryId, bo.getCategoryId());
        // 精确查询父分类ID
        lqw.eq(ObjectUtil.isNotNull(bo.getParentId()), FlowCategory::getParentId, bo.getParentId());
        // 模糊查询分类名称
        lqw.like(StringUtils.isNotBlank(bo.getCategoryName()), FlowCategory::getCategoryName, bo.getCategoryName());
        // 按祖级列表升序排序（保证父分类在前）
        lqw.orderByAsc(FlowCategory::getAncestors);
        // 按父分类ID升序排序
        lqw.orderByAsc(FlowCategory::getParentId);
        // 按排序号升序排序
        lqw.orderByAsc(FlowCategory::getOrderNum);
        // 按分类ID升序排序
        lqw.orderByAsc(FlowCategory::getCategoryId);
        return lqw;
    }

    /**
     * 新增流程分类
     * 新增流程分类，自动计算祖级列表，校验父分类状态
     *
     * @param bo 流程分类业务对象
     * @return 影响行数
     */
    @Override
    public int insertByBo(FlowCategoryBo bo) {
        // 查询父分类信息
        FlowCategory info = baseMapper.selectById(bo.getParentId());
        // 如果父分类不存在，抛出业务异常
        if (ObjectUtil.isNull(info)) {
            throw new ServiceException("父级流程分类不存在!");
        }
        // 将BO转换为实体对象
        FlowCategory category = MapstructUtils.convert(bo, FlowCategory.class);
        // 设置祖级列表：父分类祖级列表 + 分隔符 + 父分类ID
        category.setAncestors(info.getAncestors() + StringUtils.SEPARATOR + category.getParentId());
        // 插入流程分类数据
        return baseMapper.insert(category);
    }

    /**
     * 修改流程分类
     * 修改流程分类信息，支持父分类变更，自动更新子分类祖级列表
     *
     * @param bo 流程分类业务对象
     * @return 影响行数
     */
    // Spring缓存注解：删除流程分类名称缓存
    @CacheEvict(cacheNames = FlowConstant.FLOW_CATEGORY_NAME, key = "#bo.categoryId")
    @Override
    // Spring事务注解：发生异常时回滚事务
    @Transactional(rollbackFor = Exception.class)
    public int updateByBo(FlowCategoryBo bo) {
        // 将BO转换为实体对象
        FlowCategory category = MapstructUtils.convert(bo, FlowCategory.class);
        // 查询旧流程分类信息
        FlowCategory oldCategory = baseMapper.selectById(category.getCategoryId());
        // 如果流程分类不存在，抛出业务异常
        if (ObjectUtil.isNull(oldCategory)) {
            throw new ServiceException("流程分类不存在，无法修改");
        }
        // 如果父分类ID发生变化
        if (!oldCategory.getParentId().equals(category.getParentId())) {
            // 查询新父分类信息
            FlowCategory newParentCategory = baseMapper.selectById(category.getParentId());
            // 如果新父分类存在
            if (ObjectUtil.isNotNull(newParentCategory)) {
                // 计算新的祖级列表：新父分类祖级列表 + 分隔符 + 新父分类ID
                String newAncestors = newParentCategory.getAncestors() + StringUtils.SEPARATOR + newParentCategory.getCategoryId();
                // 获取旧的祖级列表
                String oldAncestors = oldCategory.getAncestors();
                // 设置新的祖级列表
                category.setAncestors(newAncestors);
                // 更新所有子分类的祖级列表
                updateCategoryChildren(category.getCategoryId(), newAncestors, oldAncestors);
            } else {
                // 如果新父分类不存在，抛出业务异常
                throw new ServiceException("父级流程分类不存在!");
            }
        } else {
            // 父分类未变化，保持原祖级列表
            category.setAncestors(oldCategory.getAncestors());
        }
        // 更新流程分类信息
        return baseMapper.updateById(category);
    }

    /**
     * 修改子元素关系
     * 当流程分类父节点变更时，更新所有子分类的祖级列表
     *
     * @param categoryId   被修改的流程分类ID
     * @param newAncestors 新的祖级列表
     * @param oldAncestors 旧的祖级列表
     */
    private void updateCategoryChildren(Long categoryId, String newAncestors, String oldAncestors) {
        // 查询所有子分类（ancestors字段包含categoryId）
        List<FlowCategory> children = baseMapper.selectList(new LambdaQueryWrapper<FlowCategory>()
            // 使用FIND_IN_SET函数查询ancestors字段包含categoryId的记录
            .apply(DataBaseHelper.findInSet(categoryId, "ancestors")));
        // 创建需要更新的分类列表
        List<FlowCategory> list = new ArrayList<>();
        // 遍历所有子分类
        for (FlowCategory child : children) {
            // 创建新的分类对象
            FlowCategory category = new FlowCategory();
            // 设置分类ID
            category.setCategoryId(child.getCategoryId());
            // 更新祖级列表：将旧的祖级列表替换为新的祖级列表
            category.setAncestors(child.getAncestors().replaceFirst(oldAncestors, newAncestors));
            // 添加到更新列表
            list.add(category);
        }
        // 如果需要更新的列表不为空
        if (CollUtil.isNotEmpty(list)) {
            // 批量更新子分类
            baseMapper.updateBatchById(list);
        }
    }

    /**
     * 删除流程分类信息
     * 删除指定流程分类，并清除相关缓存
     *
     * @param categoryId 流程分类ID
     * @return 影响行数
     */
    // Spring缓存注解：删除流程分类名称缓存
    @CacheEvict(cacheNames = FlowConstant.FLOW_CATEGORY_NAME, key = "#categoryId")
    @Override
    public int deleteWithValidById(Long categoryId) {
        // 根据流程分类ID删除分类
        return baseMapper.deleteById(categoryId);
    }
}
