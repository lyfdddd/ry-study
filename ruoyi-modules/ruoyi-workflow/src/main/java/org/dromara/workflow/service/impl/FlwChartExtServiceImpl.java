// 包声明：定义当前类所在的包路径，org.dromara.workflow.service.impl 表示工作流模块服务实现层
package org.dromara.workflow.service.impl;

// Hutool工具类：集合操作工具，提供集合判空、转换等方法
import cn.hutool.core.collection.CollUtil;
// Hutool工具类：类型转换工具，支持各种类型之间的转换
import cn.hutool.core.convert.Convert;
// Hutool工具类：对象判断工具，用于空值判断、类型转换等
import cn.hutool.core.util.ObjectUtil;
// MyBatis-Plus核心组件：Lambda查询包装器，支持类型安全查询
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
// MyBatis-Plus核心组件：查询条件构建工具类
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
// Lombok注解：自动生成包含final字段的构造函数，实现依赖注入
import lombok.RequiredArgsConstructor;
// Lombok注解：自动生成SLF4J日志对象
import lombok.extern.slf4j.Slf4j;
// 公共核心领域模型：用户DTO，用于跨服务数据传输
import org.dromara.common.core.domain.dto.UserDTO;
// 公共核心服务接口：通用部门服务接口
import org.dromara.common.core.service.DeptService;
// 公共核心服务接口：通用字典服务接口
import org.dromara.common.core.service.DictService;
// 公共核心服务接口：通用用户服务接口
import org.dromara.common.core.service.UserService;
// 公共核心工具类：日期工具类，提供日期格式化、计算等功能
import org.dromara.common.core.utils.DateUtils;
// 公共核心工具类：Stream流操作工具
import org.dromara.common.core.utils.StreamUtils;
// 公共核心工具类：字符串操作工具
import org.dromara.common.core.utils.StringUtils;
// Warm-Flow流程引擎核心DTO：流程定义JSON对象
import org.dromara.warm.flow.core.dto.DefJson;
// Warm-Flow流程引擎核心DTO：节点JSON对象
import org.dromara.warm.flow.core.dto.NodeJson;
// Warm-Flow流程引擎核心DTO：提示内容对象
import org.dromara.warm.flow.core.dto.PromptContent;
// Warm-Flow流程引擎核心枚举：节点类型枚举
import org.dromara.warm.flow.core.enums.NodeType;
// Warm-Flow流程引擎核心工具类：Map工具类
import org.dromara.warm.flow.core.utils.MapUtil;
// Warm-Flow流程引擎ORM实体：历史任务实体
import org.dromara.warm.flow.orm.entity.FlowHisTask;
// Warm-Flow流程引擎Mapper：历史任务Mapper
import org.dromara.warm.flow.orm.mapper.FlowHisTaskMapper;
// Warm-Flow流程引擎UI服务：流程图扩展服务接口
import org.dromara.warm.flow.ui.service.ChartExtService;
// 工作流公共组件：条件启用注解（当工作流功能开启时才加载）
import org.dromara.workflow.common.ConditionalOnEnable;
// 工作流公共常量：流程常量定义
import org.dromara.workflow.common.constant.FlowConstant;
// Spring值注入注解：注入配置属性值
import org.springframework.beans.factory.annotation.Value;
// Spring服务注解：标记为服务类，交由Spring容器管理
import org.springframework.stereotype.Service;

// Java集合工具类：提供集合操作
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 流程图扩展服务实现类
 * 核心业务：流程图节点悬浮提示信息生成、审批历史展示
 * 实现接口：ChartExtService（Warm-Flow流程图扩展服务）
 *
 * @author AprilWind
 */
// 条件启用注解：当工作流功能开启时才加载该服务
@ConditionalOnEnable
// Lombok注解：自动生成SLF4J日志对象
@Slf4j
// Lombok注解：自动生成包含所有final字段的构造函数，实现依赖注入
@RequiredArgsConstructor
// Spring服务注解：标记为服务类，交由Spring容器管理
@Service
public class FlwChartExtServiceImpl implements ChartExtService {

    // 用户服务，用于查询用户信息
    private final UserService userService;
    // 部门服务，用于查询部门信息
    private final DeptService deptService;
    // 历史任务Mapper，用于查询历史任务数据
    private final FlowHisTaskMapper flowHisTaskMapper;
    // 字典服务，用于查询字典数据（如审批状态）
    private final DictService dictService;
    // Spring值注入：从配置文件中注入节点悬浮提示开关，默认为true
    @Value("${warm-flow.node-tooltip:true}")
    private boolean nodeTooltip;

    /**
     * 设置流程图提示信息
     *
     * @param defJson 流程定义json对象
     */
    @Override
    public void execute(DefJson defJson) {
        // 配置关闭，直接返回，不渲染悬浮窗
        if (!nodeTooltip) {
            return;
        }

        // 根据流程实例ID查询所有相关的历史任务列表
        List<FlowHisTask> flowHisTasks = this.getHisTaskGroupedByNode(defJson.getInstance().getId());
        if (CollUtil.isEmpty(flowHisTasks)) {
            return;
        }

        // 按节点编号（nodeCode）对历史任务进行分组
        Map<String, List<FlowHisTask>> groupedByNode = StreamUtils.groupByKey(flowHisTasks, FlowHisTask::getNodeCode);

        // 批量查询所有审批人的用户信息
        List<UserDTO> userDTOList = userService.selectListByIds(StreamUtils.toList(flowHisTasks, e -> Convert.toLong(e.getApprover())));

        // 将查询到的用户列表转换为以用户ID为key的映射
        Map<Long, UserDTO> userMap = StreamUtils.toIdentityMap(userDTOList, UserDTO::getUserId);

        Map<String, String> dictType = dictService.getAllDictByDictType(FlowConstant.WF_TASK_STATUS);

        for (NodeJson nodeJson : defJson.getNodeList()) {
            List<FlowHisTask> taskList = groupedByNode.get(nodeJson.getNodeCode());
            if (CollUtil.isEmpty(taskList)) {
                continue;
            }

            // 按审批人分组去重，保留最新处理记录，最终转换成 List
            List<FlowHisTask> latestPerApprover = taskList.stream()
                .collect(Collectors.collectingAndThen(
                    Collectors.toMap(
                        FlowHisTask::getApprover,
                        Function.identity(),
                        (oldTask, newTask) -> newTask.getUpdateTime().after(oldTask.getUpdateTime()) ? newTask : oldTask,
                        LinkedHashMap::new
                    ),
                    map -> new ArrayList<>(map.values())
                ));

            // 处理当前节点的扩展信息
            this.processNodeExtInfo(nodeJson, latestPerApprover, userMap, dictType);
        }
    }

    /**
     * 初始化流程图提示信息（Warm-Flow接口实现）
     * 为流程图的每个节点初始化基础的提示信息框架
     *
     * @param defJson 流程定义JSON对象
     */
    @Override
    public void initPromptContent(DefJson defJson) {
        // 如果节点悬浮提示配置关闭，直接返回，不渲染悬浮窗
        if (!nodeTooltip) {
            return;
        }

        // 设置流程图顶部文本为流程名称
        defJson.setTopText("流程名称: " + defJson.getFlowName());
        // 遍历所有节点，为每个节点初始化提示内容
        defJson.getNodeList().forEach(nodeJson -> {
            nodeJson.setPromptContent(
                new PromptContent()
                    // 设置提示信息列表，包含节点名称
                    .setInfo(
                        CollUtil.newArrayList(
                            new PromptContent.InfoItem()
                                .setPrefix("任务名称: ") // 设置前缀
                                .setContent(nodeJson.getNodeName()) // 设置内容（节点名称）
                                .setContentStyle(Map.of( // 设置内容样式
                                    "border", "1px solid #d1e9ff",
                                    "backgroundColor", "#e8f4ff",
                                    "padding", "4px 8px",
                                    "borderRadius", "4px"
                                ))
                                .setRowStyle(Map.of( // 设置行样式
                                    "fontWeight", "bold",
                                    "margin", "0 0 6px 0",
                                    "padding", "0 0 8px 0",
                                    "borderBottom", "1px solid #ccc"
                                ))
                        )
                    )
                    // 设置弹窗样式
                    .setDialogStyle(MapUtil.mergeAll(
                        "position", "absolute",
                        "backgroundColor", "#fff",
                        "border", "1px solid #ccc",
                        "borderRadius", "4px",
                        "boxShadow", "0 2px 8px rgba(0, 0, 0, 0.15)",
                        "padding", "8px 12px",
                        "fontSize", "14px",
                        "zIndex", "1000",
                        "maxWidth", "500px",
                        "maxHeight", "300px",
                        "overflowY", "auto",
                        "overflowX", "hidden",
                        "color", "#333",
                        "pointerEvents", "auto",
                        "scrollbarWidth", "thin"
                    ))
            );
        });
    }

    /**
     * 处理节点的扩展信息，构建用于流程图悬浮提示的内容
     * 遍历历史任务列表，为每个审批人生成详细的提示信息
     *
     * @param nodeJson 当前流程节点对象，包含节点基础信息和提示内容容器
     * @param taskList 当前节点关联的历史审批任务列表，用于生成提示信息
     * @param userMap  用户信息映射表，key为用户ID，value为用户DTO对象，用于快速查找审批人信息
     * @param dictType 数据字典映射表，key为字典项编码，value为对应显示值，用于翻译审批状态等
     */
    private void processNodeExtInfo(NodeJson nodeJson, List<FlowHisTask> taskList, Map<Long, UserDTO> userMap, Map<String, String> dictType) {

        // 获取节点提示内容对象中的info列表，用于追加提示项
        List<PromptContent.InfoItem> info = nodeJson.getPromptContent().getInfo();

        // 遍历所有任务记录，构建提示内容
        for (FlowHisTask task : taskList) {
            // 根据审批人ID从用户映射中获取用户信息
            UserDTO userDTO = userMap.get(Convert.toLong(task.getApprover()));
            // 如果用户信息为空，跳过
            if (ObjectUtil.isEmpty(userDTO)) {
                continue;
            }

            // 查询用户所属部门名称
            String deptName = deptService.selectDeptNameByIds(Convert.toStr(userDTO.getDeptId()));

            // 添加标题项，格式：👥 张三（市场部）
            info.add(new PromptContent.InfoItem()
                .setPrefix(StringUtils.format("👥 {}（{}）", userDTO.getNickName(), deptName)) // 设置前缀（审批人姓名和部门）
                .setPrefixStyle(Map.of( // 设置前缀样式
                    "fontWeight", "bold",
                    "fontSize", "15px",
                    "color", "#333"
                ))
                .setRowStyle(Map.of( // 设置行样式
                    "margin", "8px 0",
                    "borderBottom", "1px dashed #ccc"
                ))
            );

            // 添加具体信息项：账号、审批状态、审批耗时、办理时间
            info.add(buildInfoItem("用户账号", userDTO.getUserName()));
            info.add(buildInfoItem("审批状态", dictType.get(task.getFlowStatus())));
            info.add(buildInfoItem("审批耗时", DateUtils.getTimeDifference(task.getUpdateTime(), task.getCreateTime())));
            info.add(buildInfoItem("办理时间", DateUtils.formatDateTime(task.getUpdateTime())));
        }
    }

    /**
     * 构建单条提示内容对象InfoItem，用于悬浮窗显示（key: value格式）
     * 统一格式化提示信息的样式，包括前缀、内容和行样式
     *
     * @param key   字段名（作为前缀）
     * @param value 字段值
     * @return 提示项对象
     */
    private PromptContent.InfoItem buildInfoItem(String key, String value) {
        return new PromptContent.InfoItem()
            // 设置前缀（字段名 + 冒号）
            .setPrefix(key + ": ")
            // 设置前缀样式
            .setPrefixStyle(Map.of(
                "textAlign", "right", // 右对齐
                "color", "#444", // 颜色
                "userSelect", "none", // 禁止用户选择
                "display", "inline-block", // 行内块显示
                "width", "100px", // 固定宽度
                "paddingRight", "8px", // 右内边距
                "fontWeight", "500", // 字体粗细
                "fontSize", "14px", // 字体大小
                "lineHeight", "24px", // 行高
                "verticalAlign", "middle" // 垂直居中
            ))
            // 设置内容（字段值）
            .setContent(value)
            // 设置内容样式
            .setContentStyle(Map.of(
                "backgroundColor", "#f7faff", // 背景色
                "color", "#005cbf", // 文字颜色
                "padding", "4px 8px", // 内边距
                "fontSize", "14px", // 字体大小
                "borderRadius", "4px", // 圆角
                "whiteSpace", "normal", // 空白处理
                "border", "1px solid #d0e5ff", // 边框
                "userSelect", "text", // 允许用户选择
                "lineHeight", "20px" // 行高
            ))
            // 设置行样式
            .setRowStyle(Map.of(
                "color", "#222", // 文字颜色
                "alignItems", "center", // 垂直居中
                "display", "flex", // 弹性布局
                "marginBottom", "6px", // 下外边距
                "fontWeight", "400", // 字体粗细
                "fontSize", "14px" // 字体大小
            ));
    }

    /**
     * 根据流程实例ID获取历史任务列表
     * 查询指定流程实例的所有历史审批任务，按更新时间降序排序
     *
     * @param instanceId 流程实例ID
     * @return 历史任务列表
     */
    public List<FlowHisTask> getHisTaskGroupedByNode(Long instanceId) {
        // 创建LambdaQueryWrapper，使用Lambda表达式，类型安全
        LambdaQueryWrapper<FlowHisTask> wrapper = Wrappers.lambdaQuery();
        // 设置查询条件：流程实例ID等于指定值
        wrapper.eq(FlowHisTask::getInstanceId, instanceId)
            // 节点类型为审批节点（BETWEEN类型）
            .eq(FlowHisTask::getNodeType, NodeType.BETWEEN.getKey())
            // 按更新时间降序排序（最新的在前）
            .orderByDesc(FlowHisTask::getUpdateTime);
        // 调用Mapper查询历史任务列表
        return flowHisTaskMapper.selectList(wrapper);
    }

}
