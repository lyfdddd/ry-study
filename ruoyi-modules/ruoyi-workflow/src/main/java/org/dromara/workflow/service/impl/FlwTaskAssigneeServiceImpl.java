// 包声明：定义当前类所在的包路径，org.dromara.workflow.service.impl 表示工作流模块服务实现层
// package关键字用于声明类所在的包，便于组织和管理代码，避免命名冲突
package org.dromara.workflow.service.impl;

// Hutool工具类：Bean拷贝工具，用于对象属性复制
import cn.hutool.core.bean.BeanUtil;
// Hutool工具类：集合操作工具，提供集合判空、转换等方法
import cn.hutool.core.collection.CollUtil;
// Hutool工具类：类型转换工具，支持各种类型之间的转换
import cn.hutool.core.convert.Convert;
// Hutool工具类：键值对对象，用于存储两个相关联的值
import cn.hutool.core.lang.Pair;
// Hutool工具类：Map操作工具
import cn.hutool.core.map.MapUtil;
// Hutool工具类：字符串工具类
import cn.hutool.core.util.StrUtil;
// Lombok注解：自动生成包含final字段的构造函数，实现依赖注入
import lombok.RequiredArgsConstructor;
// Lombok注解：自动生成SLF4J日志对象
import lombok.extern.slf4j.Slf4j;
// 部门DTO：封装部门信息
import org.dromara.common.core.domain.dto.DeptDTO;
// 任务指派DTO：封装任务指派信息
import org.dromara.common.core.domain.dto.TaskAssigneeDTO;
// 用户DTO：封装用户信息
import org.dromara.common.core.domain.dto.UserDTO;
// 任务指派查询体：封装任务指派查询条件
import org.dromara.common.core.domain.model.TaskAssigneeBody;
// 日期格式枚举：定义日期格式类型
import org.dromara.common.core.enums.FormatsType;
// 任务指派服务接口：提供任务指派查询功能
import org.dromara.common.core.service.TaskAssigneeService;
// 部门服务接口：提供部门查询功能
import org.dromara.common.core.service.DeptService;
// 角色服务接口：提供角色查询功能
import org.dromara.common.core.service.RoleService;
// 岗位服务接口：提供岗位查询功能
import org.dromara.common.core.service.PostService;
// 用户服务接口：提供用户查询功能
import org.dromara.common.core.service.UserService;
// 日期工具类：提供日期格式化功能
import org.dromara.common.core.utils.DateUtils;
// Stream流操作工具类：提供流式操作工具方法
import org.dromara.common.core.utils.StreamUtils;
// 字符串工具类：提供字符串操作工具方法
import org.dromara.common.core.utils.StringUtils;
// Warm-Flow UI DTO：办理人查询DTO
import org.dromara.warm.flow.ui.dto.HandlerFunDto;
// Warm-Flow UI DTO：办理人查询条件
import org.dromara.warm.flow.ui.dto.HandlerQuery;
// Warm-Flow UI DTO：树形结构DTO
import org.dromara.warm.flow.ui.dto.TreeFunDto;
// Warm-Flow UI服务接口：办理人选择服务接口
import org.dromara.warm.flow.ui.service.HandlerSelectService;
// Warm-Flow UI视图对象：办理人反馈VO
import org.dromara.warm.flow.ui.vo.HandlerFeedBackVo;
// Warm-Flow UI视图对象：办理人选择VO
import org.dromara.warm.flow.ui.vo.HandlerSelectVo;
// 条件启用注解：当工作流功能开启时才加载该服务
import org.dromara.workflow.common.ConditionalOnEnable;
// 任务办理人枚举：定义任务办理人类型
import org.dromara.workflow.common.enums.TaskAssigneeEnum;
// SpEL表达式服务接口：提供SpEL表达式查询功能
import org.dromara.workflow.service.IFlwSpelService;
// 任务办理人服务接口：定义任务办理人服务规范
import org.dromara.workflow.service.IFlwTaskAssigneeService;
// Spring服务注解：标记为服务类，交由Spring容器管理
import org.springframework.stereotype.Service;

// Java集合工具类：提供集合操作
import java.util.*;
// Java Stream API：提供流式操作
import java.util.stream.Collectors;

/**
 * 流程设计器-获取办理人权限设置列表服务实现类
 * 核心业务：为流程设计器提供办理人选择功能
 * 包括用户、角色、部门、岗位、SpEL表达式等多种办理人类型
 * 实现接口：IFlwTaskAssigneeService（任务办理人服务）、HandlerSelectService（Warm-Flow办理人选择服务）
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
public class FlwTaskAssigneeServiceImpl implements IFlwTaskAssigneeService, HandlerSelectService {

    // 默认分组名称常量
    private static final String DEFAULT_GROUP_NAME = "默认分组";
    // 任务指派服务接口，提供任务指派查询功能
    private final TaskAssigneeService taskAssigneeService;
    // 用户服务接口，提供用户查询功能
    private final UserService userService;
    // 部门服务接口，提供部门查询功能
    private final DeptService deptService;
    // 角色服务接口，提供角色查询功能
    private final RoleService roleService;
    // 岗位服务接口，提供岗位查询功能
    private final PostService postService;
    // SpEL表达式服务接口，提供SpEL表达式查询功能
    private final IFlwSpelService spelService;

    /**
     * 获取办理人权限设置列表tabs页签
     * 返回支持的办理人类型列表
     *
     * @return tabs页签列表
     */
    @Override
    public List<String> getHandlerType() {
        // 调用枚举获取办理人类型列表
        return TaskAssigneeEnum.getAssigneeTypeList();
    }

    /**
     * 获取办理列表, 同时构建左侧部门树状结构
     * 根据查询条件获取办理人列表和部门树
     *
     * @param query 查询条件
     * @return HandlerSelectVo 办理人选择视图对象
     */
    @Override
    public HandlerSelectVo getHandlerSelect(HandlerQuery query) {
        // 获取任务办理类型
        TaskAssigneeEnum type = TaskAssigneeEnum.fromDesc(query.getHandlerType());
        // 转换查询条件为 TaskAssigneeBody
        TaskAssigneeBody taskQuery = BeanUtil.toBean(query, TaskAssigneeBody.class);

        // 统一查询并构建业务数据
        TaskAssigneeDTO dto = fetchTaskAssigneeData(type, taskQuery);
        List<DeptDTO> depts = fetchDeptData(type);

        // 构建返回VO
        return getHandlerSelectVo(buildHandlerData(dto, type), buildDeptTree(depts));
    }

    /**
     * 办理人权限名称回显
     * 根据存储ID列表查询办理人名称
     *
     * @param storageIds 入库主键集合
     * @return 办理人反馈列表
     */
    @Override
    public List<HandlerFeedBackVo> handlerFeedback(List<String> storageIds) {
        // 如果列表为空，返回空列表
        if (CollUtil.isEmpty(storageIds)) {
            return Collections.emptyList();
        }
        // 解析并归类 ID，同时记录原始顺序和对应解析结果
        Map<TaskAssigneeEnum, List<String>> typeIdMap = new EnumMap<>(TaskAssigneeEnum.class);
        Map<String, Pair<TaskAssigneeEnum, String>> parsedMap = new LinkedHashMap<>();
        // 遍历存储ID列表
        for (String storageId : storageIds) {
            // 解析存储ID
            Pair<TaskAssigneeEnum, String> parsed = this.parseStorageId(storageId);
            // 放入解析Map
            parsedMap.put(storageId, parsed);
            // 如果解析成功，按类型归类
            if (parsed != null) {
                typeIdMap.computeIfAbsent(parsed.getKey(), k -> new ArrayList<>()).add(parsed.getValue());
            }
        }

        // 查询所有类型对应的 ID 名称映射
        Map<TaskAssigneeEnum, Map<String, String>> nameMap = new EnumMap<>(TaskAssigneeEnum.class);
        typeIdMap.forEach((type, ids) -> nameMap.put(type, this.getNamesByType(type, ids)));
        // 组装返回结果，保持原始顺序
        return parsedMap.entrySet().stream()
            .map(entry -> {
                String storageId = entry.getKey();
                Pair<TaskAssigneeEnum, String> parsed = entry.getValue();
                // 获取办理人名称
                String handlerName = (parsed == null) ? null
                    : nameMap.getOrDefault(parsed.getKey(), Collections.emptyMap())
                    .get(parsed.getValue());
                // 构建反馈VO
                return new HandlerFeedBackVo(storageId, handlerName);
            }).toList();
    }

    /**
     * 根据任务办理类型查询对应的数据
     * 根据办理人类型调用不同的服务查询数据
     *
     * @param type 任务办理人类型
     * @param taskQuery 查询条件
     * @return 任务指派DTO
     */
    private TaskAssigneeDTO fetchTaskAssigneeData(TaskAssigneeEnum type, TaskAssigneeBody taskQuery) {
        // 使用switch表达式根据类型调用不同服务
        return switch (type) {
            case USER -> taskAssigneeService.selectUsersByTaskAssigneeList(taskQuery);
            case ROLE -> taskAssigneeService.selectRolesByTaskAssigneeList(taskQuery);
            case DEPT -> taskAssigneeService.selectDeptsByTaskAssigneeList(taskQuery);
            case POST -> taskAssigneeService.selectPostsByTaskAssigneeList(taskQuery);
            case SPEL -> spelService.selectSpelByTaskAssigneeList(taskQuery);
        };
    }

    /**
     * 根据任务办理类型获取部门数据
     * 判断是否需要查询部门数据
     *
     * @param type 任务办理人类型
     * @return 部门列表
     */
    private List<DeptDTO> fetchDeptData(TaskAssigneeEnum type) {
        // 判断是否需要部门服务
        if (type.needsDeptService()) {
            // 查询所有部门列表
            return deptService.selectDeptsByList();
        }
        // 返回空列表
        return new ArrayList<>();
    }

    /**
     * 获取权限分组名称
     * 根据办理人类型和分组ID获取分组名称
     *
     * @param type      任务分配人枚举
     * @param groupName 权限分组
     * @return 权限分组名称
     */
    private String getGroupName(TaskAssigneeEnum type, String groupName) {
        // 如果分组名称为空，返回默认分组名
        if (StringUtils.isEmpty(groupName)) {
            return DEFAULT_GROUP_NAME;
        }
        // 如果需要部门服务，查询部门名称
        if (type.needsDeptService()) {
            return deptService.selectDeptNameByIds(groupName);
        }
        // 返回默认分组名
        return DEFAULT_GROUP_NAME;
    }

    /**
     * 构建部门树状结构
     * 将部门列表转换为树形结构
     *
     * @param depts 部门列表
     * @return 部门树DTO
     */
    private TreeFunDto<DeptDTO> buildDeptTree(List<DeptDTO> depts) {
        // 创建树形结构DTO
        return new TreeFunDto<>(depts)
            // 设置ID获取函数
            .setId(dept -> Convert.toStr(dept.getDeptId()))
            // 设置名称获取函数
            .setName(DeptDTO::getDeptName)
            // 设置父ID获取函数
            .setParentId(dept -> Convert.toStr(dept.getParentId()));
    }

    /**
     * 构建任务办理人数据
     * 将任务指派DTO转换为办理人数据DTO
     *
     * @param dto 任务指派DTO
     * @param type 任务办理人类型
     * @return 办理人数据DTO
     */
    private HandlerFunDto<TaskAssigneeDTO.TaskHandler> buildHandlerData(TaskAssigneeDTO dto, TaskAssigneeEnum type) {
        // 创建办理人数据DTO
        return new HandlerFunDto<>(dto.getList(), dto.getTotal())
            // 设置存储ID
            .setStorageId(assignee -> type.getCode() + assignee.getStorageId())
            // 设置办理人编码
            .setHandlerCode(assignee -> StringUtils.blankToDefault(assignee.getHandlerCode(), "无"))
            // 设置办理人名称
            .setHandlerName(assignee -> StringUtils.blankToDefault(assignee.getHandlerName(), "无"))
            // 设置分组名称
            .setGroupName(assignee -> this.getGroupName(type, assignee.getGroupName()))
            // 设置创建时间
            .setCreateTime(assignee -> DateUtils.parseDateToStr(FormatsType.YYYY_MM_DD_HH_MM_SS, assignee.getCreateTime()));
    }

    /**
     * 批量解析多个存储标识符（storageIds），按类型分类并合并查询用户列表
     * 输入格式支持多个以逗号分隔的标识（如 "user:123,role:456,789"）
     * 会自动去重返回结果，非法格式的标识将被忽略
     *
     * @param storageIds 多个存储标识符字符串（逗号分隔）
     * @return 合并后的用户列表，去重后返回，非法格式的标识将被跳过
     */
    @Override
    public List<UserDTO> fetchUsersByStorageIds(String storageIds) {
        // 如果为空，返回空列表
        if (StringUtils.isEmpty(storageIds)) {
            return List.of();
        }
        // 创建类型-ID映射
        Map<TaskAssigneeEnum, List<String>> typeIdMap = new EnumMap<>(TaskAssigneeEnum.class);
        // 遍历分割存储ID
        for (String storageId : storageIds.split(StringUtils.SEPARATOR)) {
            // 解析存储ID
            Pair<TaskAssigneeEnum, String> parsed = this.parseStorageId(storageId);
            // 如果解析成功，按类型归类
            if (parsed != null) {
                typeIdMap.computeIfAbsent(parsed.getKey(), k -> new ArrayList<>()).add(parsed.getValue());
            }
        }
        // 按类型查询用户并合并结果，去重
        return typeIdMap.entrySet().stream()
            .flatMap(entry -> this.getUsersByType(entry.getKey(), entry.getValue()).stream())
            .distinct()
            .toList();
    }

    /**
     * 根据指定的任务分配类型（TaskAssigneeEnum）和 ID 列表，获取对应的用户信息列表
     *
     * @param type 任务分配类型，表示用户、角色、部门或其他（TaskAssigneeEnum 枚举值）
     * @param ids  与指定分配类型关联的 ID 列表（例如用户ID、角色ID、部门ID等）
     * @return 返回包含用户信息的列表。如果类型为用户（USER），则通过用户ID列表查询；
     * 如果类型为角色（ROLE），则通过角色ID列表查询；
     * 如果类型为部门（DEPT），则通过部门ID列表查询；
     * 如果类型为岗位（POST）或无法识别的类型，则返回空列表
     */
    private List<UserDTO> getUsersByType(TaskAssigneeEnum type, List<String> ids) {
        // 如果是SpEL表达式类型，返回空列表
        if (type == TaskAssigneeEnum.SPEL) {
            return new ArrayList<>();
        }
        // 转换为Long类型ID列表
        List<Long> longIds = StreamUtils.toList(ids, Convert::toLong);
        // 根据类型调用不同服务查询用户
        return switch (type) {
            case USER -> userService.selectListByIds(longIds);
            case ROLE -> userService.selectUsersByRoleIds(longIds);
            case DEPT -> userService.selectUsersByDeptIds(longIds);
            case POST -> userService.selectUsersByPostIds(longIds);
            default -> new ArrayList<>();
        };
    }

    /**
     * 根据任务分配类型和对应 ID 列表，批量查询名称映射关系
     *
     * @param type 分配类型（用户、角色、部门、岗位）
     * @param ids  ID 列表（如用户ID、角色ID等）
     * @return 返回 Map，其中 key 为 ID，value 为对应的名称
     */
    private Map<String, String> getNamesByType(TaskAssigneeEnum type, List<String> ids) {
        // 如果是SpEL表达式类型，调用SpEL服务查询
        if (type == TaskAssigneeEnum.SPEL) {
            return spelService.selectRemarksBySpels(ids);
        }

        // 转换为Long类型ID列表
        List<Long> longIds = StreamUtils.toList(ids, Convert::toLong);
        // 根据类型调用不同服务查询名称
        Map<Long, String> rawMap = switch (type) {
            case USER -> userService.selectUserNamesByIds(longIds);
            case ROLE -> roleService.selectRoleNamesByIds(longIds);
            case DEPT -> deptService.selectDeptNamesByIds(longIds);
            case POST -> postService.selectPostNamesByIds(longIds);
            default -> Collections.emptyMap();
        };
        // 如果结果为空，返回空Map
        if (MapUtil.isEmpty(rawMap)) {
            return Collections.emptyMap();
        }
        // 转换key为String类型
        return rawMap.entrySet()
            .stream()
            .collect(Collectors.toMap(
                e -> Convert.toStr(e.getKey()),
                Map.Entry::getValue
            ));
    }

    /**
     * 解析 storageId 字符串，返回类型和ID的组合
     *
     * @param storageId 例如 "user:123" 或 "456"
     * @return Pair(TaskAssigneeEnum, Long)，如果格式非法返回 null
     */
    private Pair<TaskAssigneeEnum, String> parseStorageId(String storageId) {
        // 如果为空，返回null
        if (StringUtils.isBlank(storageId)) {
            return null;
        }
        // 如果是SpEL表达式，返回SpEL类型
        if (TaskAssigneeEnum.isSpelExpression(storageId)) {
            return Pair.of(TaskAssigneeEnum.SPEL, storageId);
        }
        try {
            // 按冒号分割
            String[] parts = storageId.split(StrUtil.COLON, 2);
            // 如果分割后长度小于2，默认为用户类型
            if (parts.length < 2) {
                return Pair.of(TaskAssigneeEnum.USER, parts[0]);
            } else {
                // 根据编码获取类型
                TaskAssigneeEnum type = TaskAssigneeEnum.fromCode(parts[0] + StrUtil.COLON);
                return Pair.of(type, parts[1]);
            }
        } catch (Exception e) {
            // 记录警告日志
            log.warn("解析 storageId 失败，格式非法：{}，错误信息：{}", storageId, e.getMessage());
            return null;
        }
    }

}
