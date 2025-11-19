// 包声明：定义当前类所在的包路径，org.dromara.workflow.service.impl 表示工作流模块服务实现层
package org.dromara.workflow.service.impl;

// Hutool工具类：Bean拷贝工具，用于对象属性复制
import cn.hutool.core.bean.BeanUtil;
// Hutool工具类：集合操作工具，提供集合判空、转换等方法
import cn.hutool.core.collection.CollUtil;
// Hutool工具类：类型转换工具，支持各种类型之间的转换
import cn.hutool.core.convert.Convert;
// Hutool工具类：IO工具类，提供流操作
import cn.hutool.core.io.IoUtil;
// MyBatis-Plus核心组件：Lambda查询包装器，支持类型安全查询
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
// MyBatis-Plus核心组件：查询条件构建工具类
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
// MyBatis-Plus分页插件：分页对象
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
// Jakarta Servlet API：HTTP响应对象
import jakarta.servlet.http.HttpServletResponse;
// Lombok注解：自动生成包含final字段的构造函数，实现依赖注入
import lombok.RequiredArgsConstructor;
// Lombok注解：自动生成SLF4J日志对象
import lombok.extern.slf4j.Slf4j;
// 公共核心异常：业务异常类，用于抛出业务逻辑错误
import org.dromara.common.core.exception.ServiceException;
// 公共核心工具类：Stream流操作工具
import org.dromara.common.core.utils.StreamUtils;
// 公共核心工具类：字符串操作工具
import org.dromara.common.core.utils.StringUtils;
// 公共JSON工具类：JSON序列化/反序列化工具
import org.dromara.common.json.utils.JsonUtils;
// MyBatis-Plus分页组件：分页查询参数
import org.dromara.common.mybatis.core.page.PageQuery;
// MyBatis-Plus分页组件：分页结果封装
import org.dromara.common.mybatis.core.page.TableDataInfo;
// Warm-Flow流程引擎核心DTO：流程定义JSON对象
import org.dromara.warm.flow.core.dto.DefJson;
// Warm-Flow流程引擎核心枚举：节点类型枚举
import org.dromara.warm.flow.core.enums.NodeType;
// Warm-Flow流程引擎核心枚举：发布状态枚举
import org.dromara.warm.flow.core.enums.PublishStatus;
// Warm-Flow流程引擎核心服务：流程定义服务
import org.dromara.warm.flow.core.service.DefService;
// Warm-Flow流程引擎ORM实体：流程定义实体
import org.dromara.warm.flow.orm.entity.FlowDefinition;
// Warm-Flow流程引擎ORM实体：历史任务实体
import org.dromara.warm.flow.orm.entity.FlowHisTask;
// Warm-Flow流程引擎ORM实体：流程节点实体
import org.dromara.warm.flow.orm.entity.FlowNode;
// Warm-Flow流程引擎ORM实体：流程跳转实体
import org.dromara.warm.flow.orm.entity.FlowSkip;
// Warm-Flow流程引擎Mapper：流程定义Mapper
import org.dromara.warm.flow.orm.mapper.FlowDefinitionMapper;
// Warm-Flow流程引擎Mapper：历史任务Mapper
import org.dromara.warm.flow.orm.mapper.FlowHisTaskMapper;
// Warm-Flow流程引擎Mapper：流程节点Mapper
import org.dromara.warm.flow.orm.mapper.FlowNodeMapper;
// Warm-Flow流程引擎Mapper：流程跳转Mapper
import org.dromara.warm.flow.orm.mapper.FlowSkipMapper;
// 工作流公共组件：条件启用注解（当工作流功能开启时才加载）
import org.dromara.workflow.common.ConditionalOnEnable;
// 工作流公共常量：流程常量定义
import org.dromara.workflow.common.constant.FlowConstant;
// 工作流领域模型：流程分类实体类
import org.dromara.workflow.domain.FlowCategory;
// 工作流视图对象：流程定义视图对象
import org.dromara.workflow.domain.vo.FlowDefinitionVo;
// 工作流Mapper接口：流程分类Mapper
import org.dromara.workflow.mapper.FlwCategoryMapper;
// 工作流服务接口：工作流通用服务接口
import org.dromara.workflow.service.IFlwCommonService;
// 工作流服务接口：流程定义服务接口
import org.dromara.workflow.service.IFlwDefinitionService;
// Spring服务注解：标记为服务类，交由Spring容器管理
import org.springframework.stereotype.Service;
// Spring事务注解：声明事务边界，异常时回滚
import org.springframework.transaction.annotation.Transactional;
// Spring Web组件：文件上传组件
import org.springframework.web.multipart.MultipartFile;

// Java IO异常
import java.io.IOException;
// Java字符集
import java.nio.charset.StandardCharsets;
// Java集合工具类：提供集合操作
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// 导入租户常量：默认租户ID
import static org.dromara.common.core.constant.TenantConstants.DEFAULT_TENANT_ID;

/**
 * 流程定义服务实现类
 * 核心业务：流程定义管理、发布、导入导出、租户同步
 * 实现接口：IFlwDefinitionService（流程定义服务）
 *
 * @author may
 */
// 条件启用注解：当工作流功能开启时才加载该服务
@ConditionalOnEnable
// Lombok注解：自动生成SLF4J日志对象
@Slf4j
// Lombok注解：自动生成包含所有final字段的构造函数，实现依赖注入
@RequiredArgsConstructor
// Spring服务注解：标记为服务类，交由Spring容器管理
@Service
public class FlwDefinitionServiceImpl implements IFlwDefinitionService {

    // Warm-Flow流程定义服务，用于流程定义核心操作
    private final DefService defService;
    // 流程定义Mapper，用于流程定义数据持久化
    private final FlowDefinitionMapper flowDefinitionMapper;
    // 历史任务Mapper，用于查询历史任务数据
    private final FlowHisTaskMapper flowHisTaskMapper;
    // 流程节点Mapper，用于查询流程节点数据
    private final FlowNodeMapper flowNodeMapper;
    // 流程跳转Mapper，用于查询流程跳转数据
    private final FlowSkipMapper flowSkipMapper;
    // 流程分类Mapper，用于查询流程分类数据
    private final FlwCategoryMapper flwCategoryMapper;
    // 工作流通用服务，用于查询申请人节点等通用功能
    private final IFlwCommonService flwCommonService;

    /**
     * 查询已发布的流程定义列表
     * 分页查询已发布的流程定义，用于流程发起页面
     *
     * @param flowDefinition 流程定义查询条件
     * @param pageQuery      分页参数
     * @return 分页结果
     */
    @Override
    public TableDataInfo<FlowDefinitionVo> queryList(FlowDefinition flowDefinition, PageQuery pageQuery) {
        // 构建查询条件
        LambdaQueryWrapper<FlowDefinition> wrapper = buildQueryWrapper(flowDefinition);
        // 添加发布状态条件：已发布
        wrapper.eq(FlowDefinition::getIsPublish, PublishStatus.PUBLISHED.getKey());
        // 执行分页查询
        Page<FlowDefinition> page = flowDefinitionMapper.selectPage(pageQuery.build(), wrapper);
        // 将实体列表转换为VO列表
        List<FlowDefinitionVo> list = BeanUtil.copyToList(page.getRecords(), FlowDefinitionVo.class);
        // 返回分页结果
        return new TableDataInfo<>(list, page.getTotal());
    }

    /**
     * 查询未发布的流程定义列表
     * 分页查询未发布和已过期的流程定义，用于流程设计页面
     *
     * @param flowDefinition 流程定义查询条件
     * @param pageQuery      分页参数
     * @return 分页结果
     */
    @Override
    public TableDataInfo<FlowDefinitionVo> unPublishList(FlowDefinition flowDefinition, PageQuery pageQuery) {
        // 构建查询条件
        LambdaQueryWrapper<FlowDefinition> wrapper = buildQueryWrapper(flowDefinition);
        // 添加发布状态条件：未发布或已过期
        wrapper.in(FlowDefinition::getIsPublish, Arrays.asList(PublishStatus.UNPUBLISHED.getKey(), PublishStatus.EXPIRED.getKey()));
        // 执行分页查询
        Page<FlowDefinition> page = flowDefinitionMapper.selectPage(pageQuery.build(), wrapper);
        // 将实体列表转换为VO列表
        List<FlowDefinitionVo> list = BeanUtil.copyToList(page.getRecords(), FlowDefinitionVo.class);
        // 返回分页结果
        return new TableDataInfo<>(list, page.getTotal());
    }

    /**
     * 构建流程定义查询条件
     * 私有方法，封装通用查询逻辑，支持Lambda表达式
     *
     * @param flowDefinition 流程定义查询条件
     * @return LambdaQueryWrapper，类型安全，避免硬编码字段名
     */
    private LambdaQueryWrapper<FlowDefinition> buildQueryWrapper(FlowDefinition flowDefinition) {
        // 创建LambdaQueryWrapper，使用实体属性引用，编译期检查
        LambdaQueryWrapper<FlowDefinition> wrapper = Wrappers.lambdaQuery();
        // 模糊查询流程编码
        wrapper.like(StringUtils.isNotBlank(flowDefinition.getFlowCode()), FlowDefinition::getFlowCode, flowDefinition.getFlowCode());
        // 模糊查询流程名称
        wrapper.like(StringUtils.isNotBlank(flowDefinition.getFlowName()), FlowDefinition::getFlowName, flowDefinition.getFlowName());
        // 如果分类ID不为空
        if (StringUtils.isNotBlank(flowDefinition.getCategory())) {
            // 查询该分类及其所有子分类ID
            List<Long> categoryIds = flwCategoryMapper.selectCategoryIdsByParentId(Convert.toLong(flowDefinition.getCategory()));
            // 在分类ID范围内查询（转换为String类型）
            wrapper.in(FlowDefinition::getCategory, StreamUtils.toList(categoryIds, Convert::toStr));
        }
        // 按创建时间降序排序
        wrapper.orderByDesc(FlowDefinition::getCreateTime);
        return wrapper;
    }

    /**
     * 发布流程定义
     * 校验流程节点配置后，发布流程定义
     *
     * @param id 流程定义ID
     * @return 是否发布成功
     */
    @Override
    // Spring事务注解：发生异常时回滚事务
    @Transactional(rollbackFor = Exception.class)
    public boolean publish(Long id) {
        // 查询流程定义的所有节点
        List<FlowNode> flowNodes = flowNodeMapper.selectList(new LambdaQueryWrapper<FlowNode>().eq(FlowNode::getDefinitionId, id));
        // 错误信息列表
        List<String> errorMsg = new ArrayList<>();
        // 如果节点列表不为空
        if (CollUtil.isNotEmpty(flowNodes)) {
            // 获取申请人节点编码（第一个审批节点）
            String applyNodeCode = flwCommonService.applyNodeCode(id);
            // 遍历所有节点，检查审批节点是否配置了办理人
            for (FlowNode flowNode : flowNodes) {
                // 如果是审批节点（BETWEEN类型）且不是申请人节点，且未配置办理人权限标识
                if (StringUtils.isBlank(flowNode.getPermissionFlag()) && !applyNodeCode.equals(flowNode.getNodeCode()) && NodeType.BETWEEN.getKey().equals(flowNode.getNodeType())) {
                    // 将节点名称添加到错误列表
                    errorMsg.add(flowNode.getNodeName());
                }
            }
            // 如果存在未配置办理人的节点，抛出业务异常
            if (CollUtil.isNotEmpty(errorMsg)) {
                throw new ServiceException("节点【{}】未配置办理人!", StringUtils.joinComma(errorMsg));
            }
        }
        // 调用Warm-Flow服务发布流程定义
        return defService.publish(id);
    }

    /**
     * 导入流程定义
     * 从JSON文件导入流程定义，支持分类设置
     *
     * @param file     流程定义JSON文件
     * @param category 流程分类ID
     * @return 是否导入成功
     */
    @Override
    // Spring事务注解：发生异常时回滚事务
    @Transactional(rollbackFor = Exception.class)
    public boolean importJson(MultipartFile file, String category) {
        try {
            // 解析JSON文件为DefJson对象
            DefJson defJson = JsonUtils.parseObject(file.getBytes(), DefJson.class);
            // 设置流程分类
            defJson.setCategory(category);
            // 调用Warm-Flow服务导入流程定义
            defService.importDef(defJson);
        } catch (IOException e) {
            // 记录错误日志
            log.error("读取文件流错误: {}", e.getMessage(), e);
            // 抛出非法状态异常
            throw new IllegalStateException("文件读取失败，请检查文件内容", e);
        }
        return true;
    }

    /**
     * 导出流程定义
     * 将流程定义导出为JSON文件
     *
     * @param id       流程定义ID
     * @param response HTTP响应对象
     * @throws IOException IO异常
     */
    @Override
    public void exportDef(Long id, HttpServletResponse response) throws IOException {
        // 调用Warm-Flow服务导出流程定义为JSON字符串，并转换为字节数组
        byte[] data = defService.exportJson(id).getBytes(StandardCharsets.UTF_8);
        // 设置响应头和内容类型
        response.reset(); // 重置响应
        response.setCharacterEncoding(StandardCharsets.UTF_8.name()); // 设置字符编码
        response.setContentType("application/text"); // 设置内容类型为文本
        response.setHeader("Content-Disposition", "attachment;"); // 设置附件下载头
        response.addHeader("Content-Length", "" + data.length); // 设置内容长度
        // 使用Hutool的IoUtil将字节数组写入响应输出流
        IoUtil.write(response.getOutputStream(), false, data);
    }

    /**
     * 删除流程定义
     * 校验流程定义是否被使用后，删除流程定义
     *
     * @param ids 流程定义ID列表
     * @return 是否删除成功
     */
    @Override
    // Spring事务注解：发生异常时回滚事务
    @Transactional(rollbackFor = Exception.class)
    public boolean removeDef(List<Long> ids) {
        // 查询历史任务，检查流程定义是否被使用
        LambdaQueryWrapper<FlowHisTask> wrapper = Wrappers.lambdaQuery();
        wrapper.in(FlowHisTask::getDefinitionId, ids);
        List<FlowHisTask> flowHisTasks = flowHisTaskMapper.selectList(wrapper);
        // 如果存在历史任务，说明流程定义已被使用
        if (CollUtil.isNotEmpty(flowHisTasks)) {
            // 查询被使用的流程定义
            List<FlowDefinition> flowDefinitions = flowDefinitionMapper.selectByIds(StreamUtils.toList(flowHisTasks, FlowHisTask::getDefinitionId));
            // 如果流程定义存在，抛出业务异常
            if (CollUtil.isNotEmpty(flowDefinitions)) {
                // 拼接流程编码字符串
                String join = StreamUtils.join(flowDefinitions, FlowDefinition::getFlowCode);
                // 记录日志
                log.info("流程定义【{}】已被使用不可被删除！", join);
                // 抛出业务异常
                throw new ServiceException("流程定义【{}】已被使用不可被删除！", join);
            }
        }
        try {
            // 调用Warm-Flow服务删除流程定义
            defService.removeDef(ids);
        } catch (Exception e) {
            // 记录错误日志
            log.error("Error removing flow definitions: {}", e.getMessage(), e);
            // 抛出运行时异常
            throw new RuntimeException("Failed to remove flow definitions", e);
        }
        return true;
    }

    /**
     * 新增租户流程定义
     * 将默认租户的流程定义同步到新租户，用于租户初始化
     *
     * @param tenantId 租户ID
     */
    @Override
    // Spring事务注解：发生异常时回滚事务
    @Transactional(rollbackFor = Exception.class)
    public void syncDef(String tenantId) {
        // 查询默认租户的所有流程定义
        List<FlowDefinition> flowDefinitions = flowDefinitionMapper.selectList(new LambdaQueryWrapper<FlowDefinition>().eq(FlowDefinition::getTenantId, DEFAULT_TENANT_ID));
        // 如果流程定义列表为空，直接返回
        if (CollUtil.isEmpty(flowDefinitions)) {
            return;
        }
        // 查询默认租户的流程分类
        FlowCategory flowCategory = flwCategoryMapper.selectOne(new LambdaQueryWrapper<FlowCategory>()
            .eq(FlowCategory::getTenantId, DEFAULT_TENANT_ID)
            .eq(FlowCategory::getCategoryId, FlowConstant.FLOW_CATEGORY_ID));
        // 将分类ID置空，由数据库自动生成新ID
        flowCategory.setCategoryId(null);
        // 设置新的租户ID
        flowCategory.setTenantId(tenantId);
        // 清空创建部门、创建人、创建时间、更新人、更新时间（由框架自动填充）
        flowCategory.setCreateDept(null);
        flowCategory.setCreateBy(null);
        flowCategory.setCreateTime(null);
        flowCategory.setUpdateBy(null);
        flowCategory.setUpdateTime(null);
        // 插入新的流程分类
        flwCategoryMapper.insert(flowCategory);
        // 提取流程定义ID列表
        List<Long> defIds = StreamUtils.toList(flowDefinitions, FlowDefinition::getId);
        // 查询所有流程节点
        List<FlowNode> flowNodes = flowNodeMapper.selectList(new LambdaQueryWrapper<FlowNode>().in(FlowNode::getDefinitionId, defIds));
        // 查询所有流程跳转
        List<FlowSkip> flowSkips = flowSkipMapper.selectList(new LambdaQueryWrapper<FlowSkip>().in(FlowSkip::getDefinitionId, defIds));
        // 遍历流程定义列表，逐一同步
        for (FlowDefinition definition : flowDefinitions) {
            // 将实体转换为新的对象（避免引用传递）
            FlowDefinition flowDefinition = BeanUtil.toBean(definition, FlowDefinition.class);
            // 将ID置空，由数据库自动生成新ID
            flowDefinition.setId(null);
            // 设置新的租户ID
            flowDefinition.setTenantId(tenantId);
            // 设置为未发布状态
            flowDefinition.setIsPublish(0);
            // 设置新的分类ID（转换为String）
            flowDefinition.setCategory(Convert.toStr(flowCategory.getCategoryId()));
            // 插入流程定义
            int insert = flowDefinitionMapper.insert(flowDefinition);
            // 如果插入失败，记录日志并跳过
            if (insert <= 0) {
                log.info("同步流程定义【{}】失败！", definition.getFlowCode());
                continue;
            }
            // 记录成功日志
            log.info("同步流程定义【{}】成功！", definition.getFlowCode());
            // 获取新生成的流程定义ID
            Long definitionId = flowDefinition.getId();
            // 如果流程节点列表不为空
            if (CollUtil.isNotEmpty(flowNodes)) {
                // 过滤出当前流程定义的节点
                List<FlowNode> nodes = StreamUtils.filter(flowNodes, node -> node.getDefinitionId().equals(definition.getId()));
                // 如果节点不为空
                if (CollUtil.isNotEmpty(nodes)) {
                    // 复制节点列表
                    List<FlowNode> flowNodeList = BeanUtil.copyToList(nodes, FlowNode.class);
                    // 遍历节点，设置新属性
                    flowNodeList.forEach(e -> {
                        e.setId(null); // ID置空，自动生成
                        e.setDefinitionId(definitionId); // 设置新的流程定义ID
                        e.setTenantId(tenantId); // 设置新的租户ID
                        e.setPermissionFlag(null); // 清空办理人权限标识（需要重新配置）
                    });
                    // 批量插入或更新节点
                    flowNodeMapper.insertOrUpdate(flowNodeList);
                }
            }
            // 如果流程跳转列表不为空
            if (CollUtil.isNotEmpty(flowSkips)) {
                // 过滤出当前流程定义的跳转
                List<FlowSkip> skips = StreamUtils.filter(flowSkips, skip -> skip.getDefinitionId().equals(definition.getId()));
                // 如果跳转不为空
                if (CollUtil.isNotEmpty(skips)) {
                    // 复制跳转列表
                    List<FlowSkip> flowSkipList = BeanUtil.copyToList(skips, FlowSkip.class);
                    // 遍历跳转，设置新属性
                    flowSkipList.forEach(e -> {
                        e.setId(null); // ID置空，自动生成
                        e.setDefinitionId(definitionId); // 设置新的流程定义ID
                        e.setTenantId(tenantId); // 设置新的租户ID
                    });
                    // 批量插入或更新跳转
                    flowSkipMapper.insertOrUpdate(flowSkipList);
                }
            }
        }
    }
}
