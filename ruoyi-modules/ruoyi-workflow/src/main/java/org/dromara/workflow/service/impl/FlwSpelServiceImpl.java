// 包声明：定义当前类所在的包路径，org.dromara.workflow.service.impl 表示工作流模块服务实现层
// package关键字用于声明类所在的包，便于组织和管理代码，避免命名冲突
package org.dromara.workflow.service.impl;

// Hutool工具类：集合操作工具，提供集合判空、转换等方法
import cn.hutool.core.collection.CollUtil;
// MyBatis-Plus核心组件：Lambda查询包装器，支持类型安全查询
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
// MyBatis-Plus核心组件：查询条件构建工具类
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
// MyBatis-Plus分页插件：分页对象
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
// Lombok注解：自动生成包含final字段的构造函数，实现依赖注入
import lombok.RequiredArgsConstructor;
// Lombok注解：自动生成SLF4J日志对象
import lombok.extern.slf4j.Slf4j;
// 系统常量：定义系统级别的常量
import org.dromara.common.core.constant.SystemConstants;
// 任务指派DTO：封装任务指派信息
import org.dromara.common.core.domain.dto.TaskAssigneeDTO;
// 任务指派查询体：封装任务指派查询条件
import org.dromara.common.core.domain.model.TaskAssigneeBody;
// MapStruct工具类：对象转换工具
import org.dromara.common.core.utils.MapstructUtils;
// Stream流操作工具类：提供流式操作工具方法
import org.dromara.common.core.utils.StreamUtils;
// 字符串工具类：提供字符串操作工具方法
import org.dromara.common.core.utils.StringUtils;
// MyBatis-Plus分页组件：分页查询参数
import org.dromara.common.mybatis.core.page.PageQuery;
// MyBatis-Plus分页组件：分页结果封装
import org.dromara.common.mybatis.core.page.TableDataInfo;
// 条件启用注解：当工作流功能开启时才加载该服务
import org.dromara.workflow.common.ConditionalOnEnable;
// SpEL表达式实体：流程SpEL表达式定义
import org.dromara.workflow.domain.FlowSpel;
// SpEL表达式业务对象：封装SpEL表达式查询条件
import org.dromara.workflow.domain.bo.FlowSpelBo;
// SpEL表达式视图对象：封装SpEL表达式响应数据
import org.dromara.workflow.domain.vo.FlowSpelVo;
// SpEL表达式Mapper：提供SpEL表达式数据持久化操作
import org.dromara.workflow.mapper.FlwSpelMapper;
// SpEL表达式服务接口：定义SpEL表达式服务规范
import org.dromara.workflow.service.IFlwSpelService;
// Spring服务注解：标记为服务类，交由Spring容器管理
import org.springframework.stereotype.Service;

// Java集合工具类：提供集合操作
import java.util.Collection;
// Java集合工具类：提供不可变集合
import java.util.Collections;
// Java List接口：用于存储列表数据
import java.util.List;
// Java Map接口：用于存储键值对数据
import java.util.Map;

/**
 * 流程spel达式定义Service业务层处理
 * 核心业务：管理流程中使用的SpEL表达式定义
 * 包括查询、新增、修改、删除等操作
 * 实现接口：IFlwSpelService（SpEL表达式服务接口）
 *
 * @author Michelle.Chung
 * @date 2025-07-04
 */

// 条件启用注解：当工作流功能开启时才加载该服务
@ConditionalOnEnable
// Lombok注解：自动生成SLF4J日志对象
@Slf4j
// Lombok注解：自动生成包含所有final字段的构造函数，实现依赖注入
@RequiredArgsConstructor
// Spring服务注解：标记为服务类，交由Spring容器管理
@Service
public class FlwSpelServiceImpl implements IFlwSpelService {

    // SpEL表达式Mapper，提供数据持久化操作
    private final FlwSpelMapper baseMapper;

    /**
     * 查询流程spel达式定义
     * 根据ID查询单条SpEL表达式记录
     *
     * @param id 主键ID
     * @return SpEL表达式视图对象
     */
    @Override
    public FlowSpelVo queryById(Long id){
        // 调用Mapper查询SpEL表达式VO对象
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询流程spel达式定义列表
     * 根据查询条件分页查询SpEL表达式列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return SpEL表达式分页列表
     */
    @Override
    public TableDataInfo<FlowSpelVo> queryPageList(FlowSpelBo bo, PageQuery pageQuery) {
        // 构建查询条件
        LambdaQueryWrapper<FlowSpel> lqw = buildQueryWrapper(bo);
        // 执行分页查询
        Page<FlowSpelVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        // 返回分页结果
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的流程spel达式定义列表
     * 根据查询条件查询所有SpEL表达式列表
     *
     * @param bo 查询条件
     * @return SpEL表达式列表
     */
    @Override
    public List<FlowSpelVo> queryList(FlowSpelBo bo) {
        // 构建查询条件
        LambdaQueryWrapper<FlowSpel> lqw = buildQueryWrapper(bo);
        // 查询列表
        return baseMapper.selectVoList(lqw);
    }

    /**
     * 构建查询条件
     * 私有方法，封装通用查询逻辑
     *
     * @param bo 查询条件
     * @return LambdaQueryWrapper
     */
    private LambdaQueryWrapper<FlowSpel> buildQueryWrapper(FlowSpelBo bo) {
        // 获取查询参数
        Map<String, Object> params = bo.getParams();
        // 创建LambdaQueryWrapper
        LambdaQueryWrapper<FlowSpel> lqw = Wrappers.lambdaQuery();
        // 按ID升序排序
        lqw.orderByAsc(FlowSpel::getId);
        // 模糊查询组件名称
        lqw.like(StringUtils.isNotBlank(bo.getComponentName()), FlowSpel::getComponentName, bo.getComponentName());
        // 模糊查询方法名称
        lqw.like(StringUtils.isNotBlank(bo.getMethodName()), FlowSpel::getMethodName, bo.getMethodName());
        // 精确查询方法参数
        lqw.eq(StringUtils.isNotBlank(bo.getMethodParams()), FlowSpel::getMethodParams, bo.getMethodParams());
        // 精确查询视图SpEL表达式
        lqw.eq(StringUtils.isNotBlank(bo.getViewSpel()), FlowSpel::getViewSpel, bo.getViewSpel());
        // 精确查询状态
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), FlowSpel::getStatus, bo.getStatus());
        // 模糊查询备注
        lqw.like(StringUtils.isNotBlank(bo.getRemark()), FlowSpel::getRemark, bo.getRemark());
        return lqw;
    }

    /**
     * 新增流程spel达式定义
     * 插入新的SpEL表达式记录
     *
     * @param bo SpEL表达式业务对象
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(FlowSpelBo bo) {
        // 将BO转换为实体对象
        FlowSpel add = MapstructUtils.convert(bo, FlowSpel.class);
        // 保存前数据校验
        validEntityBeforeSave(add);
        // 插入数据
        boolean flag = baseMapper.insert(add) > 0;
        // 如果插入成功，设置ID回写到BO
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改流程spel达式定义
     * 更新SpEL表达式信息
     *
     * @param bo SpEL表达式业务对象
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(FlowSpelBo bo) {
        // 将BO转换为实体对象
        FlowSpel update = MapstructUtils.convert(bo, FlowSpel.class);
        // 保存前数据校验
        validEntityBeforeSave(update);
        // 更新数据
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     * 预留方法，用于数据校验
     *
     * @param entity 实体对象
     */
    private void validEntityBeforeSave(FlowSpel entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除流程spel达式定义信息
     * 批量删除SpEL表达式记录
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        // 如果需要校验
        if(isValid){
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        // 执行删除
        return baseMapper.deleteByIds(ids) > 0;
    }

    /**
     * 查询spel并返回任务指派的列表，支持分页
     * 根据查询条件查询SpEL表达式，并转换为任务指派DTO
     *
     * @param taskQuery 查询条件
     * @return 任务指派DTO
     */
    @Override
    public TaskAssigneeDTO selectSpelByTaskAssigneeList(TaskAssigneeBody taskQuery) {
        // 创建分页查询对象
        PageQuery pageQuery = new PageQuery(taskQuery.getPageSize(), taskQuery.getPageNum());
        // 创建SpEL查询BO
        FlowSpelBo bo = new FlowSpelBo();
        // 设置视图SpEL表达式
        bo.setViewSpel(taskQuery.getHandlerCode());
        // 设置备注
        bo.setRemark(taskQuery.getHandlerName());
        // 设置状态为正常
        bo.setStatus(SystemConstants.NORMAL);
        // 获取查询参数
        Map<String, Object> params = bo.getParams();
        // 设置开始时间
        params.put("beginTime", taskQuery.getBeginTime());
        // 设置结束时间
        params.put("endTime", taskQuery.getEndTime());
        // 分页查询
        TableDataInfo<FlowSpelVo> page = this.queryPageList(bo, pageQuery);
        // 使用封装的字段映射方法进行转换
        List<TaskAssigneeDTO.TaskHandler> handlers = TaskAssigneeDTO.convertToHandlerList(page.getRows(),
            FlowSpelVo::getViewSpel, item -> "", FlowSpelVo::getRemark, item -> "", FlowSpelVo::getCreateTime);
        // 返回任务指派DTO
        return new TaskAssigneeDTO(page.getTotal(), handlers);
    }

    /**
     * 根据视图 SpEL 表达式列表，查询对应的备注信息
     * 批量查询SpEL表达式的备注信息
     *
     * @param viewSpels SpEL 表达式列表
     * @return 映射表：key 为 SpEL 表达式，value 为对应备注；若为空则返回空 Map
     */
    @Override
    public Map<String, String> selectRemarksBySpels(List<String> viewSpels) {
        // 如果列表为空，返回空Map
        if (CollUtil.isEmpty(viewSpels)) {
            return Collections.emptyMap();
        }
        // 查询SpEL表达式列表，只查询视图SpEL和备注字段
        List<FlowSpel> list = baseMapper.selectList(
            new LambdaQueryWrapper<FlowSpel>()
                .select(FlowSpel::getViewSpel, FlowSpel::getRemark)
                .in(FlowSpel::getViewSpel, viewSpels)
        );
        // 转换为Map，key为视图SpEL，value为备注
        return StreamUtils.toMap(list, FlowSpel::getViewSpel, x ->
            StringUtils.isEmpty(x.getRemark()) ? "" : x.getRemark()
        );
    }

}
