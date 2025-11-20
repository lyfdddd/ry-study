// 包声明：定义当前类所在的包路径，org.dromara.workflow.service.impl 表示工作流模块服务实现层
// package关键字用于声明类所在的包，便于组织和管理代码，避免命名冲突
package org.dromara.workflow.service.impl;

// Hutool工具类：类型转换工具，支持各种类型之间的转换
import cn.hutool.core.convert.Convert;
// Hutool工具类：字典对象，用于存储键值对数据
import cn.hutool.core.lang.Dict;
// Hutool工具类：对象判断工具，用于空值判断、类型转换等
import cn.hutool.core.util.ObjectUtil;
// Lombok注解：自动生成包含final字段的构造函数，实现依赖注入
import lombok.RequiredArgsConstructor;
// Lombok注解：自动生成SLF4J日志对象
import lombok.extern.slf4j.Slf4j;
// 字典类型DTO：封装字典类型信息
import org.dromara.common.core.domain.dto.DictTypeDTO;
// 字典服务接口：提供字典数据查询功能
import org.dromara.common.core.service.DictService;
// 字符串工具类：提供字符串操作工具方法
import org.dromara.common.core.utils.StringUtils;
// JSON工具类：提供JSON序列化/反序列化功能
import org.dromara.common.json.utils.JsonUtils;
// Warm-Flow流程引擎核心：流程引擎入口
import org.dromara.warm.flow.core.FlowEngine;
// Warm-Flow工具类：集合工具类
import org.dromara.warm.flow.core.utils.CollUtil;
// Warm-Flow工具类：表达式工具类，用于解析SpEL表达式
import org.dromara.warm.flow.core.utils.ExpressionUtil;
// Warm-Flow UI服务接口：节点扩展服务接口
import org.dromara.warm.flow.ui.service.NodeExtService;
// Warm-Flow UI视图对象：节点扩展对象
import org.dromara.warm.flow.ui.vo.NodeExt;
// 条件启用注解：当工作流功能开启时才加载该服务
import org.dromara.workflow.common.ConditionalOnEnable;
// 按钮权限枚举：定义流程节点按钮权限类型
import org.dromara.workflow.common.enums.ButtonPermissionEnum;
// 抄送设置枚举：定义抄送对象类型
import org.dromara.workflow.common.enums.CopySettingEnum;
// 节点扩展枚举接口：定义节点扩展属性的通用接口
import org.dromara.workflow.common.enums.NodeExtEnum;
// 自定义参数枚举：定义节点自定义参数类型
import org.dromara.workflow.common.enums.VariablesEnum;
// 按钮权限视图对象：封装按钮权限信息
import org.dromara.workflow.domain.vo.ButtonPermissionVo;
// 节点扩展视图对象：封装节点扩展属性信息
import org.dromara.workflow.domain.vo.NodeExtVo;
// 节点扩展服务接口：定义节点扩展属性服务接口
import org.dromara.workflow.service.IFlwNodeExtService;
// Spring服务注解：标记为服务类，交由Spring容器管理
import org.springframework.stereotype.Service;

// Java集合工具类：提供集合操作
import java.util.*;
// Java Stream API：提供流式操作
import java.util.stream.Collectors;

/**
 * 流程设计器-节点扩展属性服务实现类
 * 核心业务：为流程设计器提供节点扩展属性配置功能
 * 包括按钮权限、抄送设置、自定义参数等
 * 实现接口：NodeExtService（Warm-Flow节点扩展服务）、IFlwNodeExtService（自定义节点扩展服务）
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
public class FlwNodeExtServiceImpl implements NodeExtService, IFlwNodeExtService {

    /**
     * 存储不同 dictType 对应的配置信息
     * 使用静态Map缓存节点扩展类型的配置信息
     * key为枚举类名，value为配置Map
     */
    private static final Map<String, Map<String, Object>> CHILD_NODE_MAP;

    // 静态代码块：初始化CHILD_NODE_MAP
    static {
        // 使用Map.of初始化不可变Map，存储三种节点扩展类型的配置
        CHILD_NODE_MAP = Map.of(
            // 抄送设置枚举配置
            CopySettingEnum.class.getSimpleName(),
            Map.of(
                "label", "抄送对象", // 标签名称
                "type", 5, // 类型：5表示用户选择器
                "must", false, // 是否必填
                "multiple", false, // 是否多选
                "desc", "设置该节点的抄送办理人" // 描述信息
            ),
            // 自定义参数枚举配置
            VariablesEnum.class.getSimpleName(),
            Map.of(
                "label", "自定义参数", // 标签名称
                "type", 2, // 类型：2表示文本域
                "must", false, // 是否必填
                "multiple", false, // 是否多选
                "desc", "节点执行时可设置自定义参数，多个参数以逗号分隔，如：key1=value1,key2=value2" // 描述信息
            ),
            // 按钮权限枚举配置
            ButtonPermissionEnum.class.getSimpleName(),
            Map.of(
                "label", "权限按钮", // 标签名称
                "type", 4, // 类型：4表示选择框
                "must", false, // 是否必填
                "multiple", true, // 是否多选
                "desc", "控制该节点的按钮权限" // 描述信息
            )
        );
    }

    // 字典服务接口，用于查询字典数据
    private final DictService dictService;

    /**
     * 获取节点扩展属性
     * 为流程设计器提供节点扩展属性配置
     *
     * @return 节点扩展属性列表
     */
    @Override
    public List<NodeExt> getNodeExt() {
        // 创建节点扩展属性列表
        List<NodeExt> nodeExtList = new ArrayList<>();
        // 构建基础设置页面，包含抄送设置和自定义参数
        nodeExtList.add(buildNodeExt("wf_basic_tab", "基础设置", 1,
            List.of(CopySettingEnum.class, VariablesEnum.class)));
        // 构建按钮权限页面
        nodeExtList.add(buildNodeExt("wf_button_tab", "权限", 2,
            List.of(ButtonPermissionEnum.class)));
        // 自定义构建 规则参考 NodeExt 与 warm-flow文档说明
        // nodeExtList.add(buildNodeExt("xxx_xxx", "xxx", 1, List);
        return nodeExtList;
    }

    /**
     * 构建一个 `NodeExt` 对象
     * 根据参数构建节点扩展对象，支持枚举类和字典类型
     *
     * @param code    唯一编码
     * @param name    名称（新页签时，作为页签名称）
     * @param type    节点类型（1: 基础设置，2: 新页签）
     * @param sources 数据来源（枚举类或字典类型）
     * @return 构建的 `NodeExt` 对象
     */
    @SuppressWarnings("unchecked cast")
    private NodeExt buildNodeExt(String code, String name, int type, List<Object> sources) {
        // 创建NodeExt对象
        NodeExt nodeExt = new NodeExt();
        // 设置编码
        nodeExt.setCode(code);
        // 设置类型
        nodeExt.setType(type);
        // 设置名称
        nodeExt.setName(name);
        // 遍历sources，根据类型构建子节点
        nodeExt.setChilds(sources.stream()
            .map(source -> {
                // 如果是枚举类且实现NodeExtEnum接口
                if (source instanceof Class<?> clazz && NodeExtEnum.class.isAssignableFrom(clazz)) {
                    return buildChildNode((Class<? extends NodeExtEnum>) clazz);
                } else if (source instanceof String dictType) {
                    // 如果是字典类型字符串
                    return buildChildNode(dictType);
                }
                return null;
            })
            // 过滤空值
            .filter(ObjectUtil::isNotNull)
            // 收集为列表
            .toList()
        );
        return nodeExt;
    }

    /**
     * 根据枚举类型构建一个 `ChildNode` 对象
     * 解析枚举类，构建子节点配置
     *
     * @param enumClass 枚举类，必须实现 `NodeExtEnum` 接口
     * @return 构建的 `ChildNode` 对象
     */
    private NodeExt.ChildNode buildChildNode(Class<? extends NodeExtEnum> enumClass) {
        // 检查是否为枚举类型
        if (!enumClass.isEnum()) {
            return null;
        }
        // 获取枚举类简单名称
        String simpleName = enumClass.getSimpleName();
        // 创建子节点对象
        NodeExt.ChildNode childNode = new NodeExt.ChildNode();
        // 从缓存Map中获取配置
        Map<String, Object> map = CHILD_NODE_MAP.get(simpleName);
        // 设置编码，此json中唯一
        childNode.setCode(simpleName);
        // 设置label名称
        childNode.setLabel(Convert.toStr(map.get("label")));
        // 设置类型：1：输入框 2：文本域 3：下拉框 4：选择框 5：用户选择器
        childNode.setType(Convert.toInt(map.get("type"), 1));
        // 设置是否必填
        childNode.setMust(Convert.toBool(map.get("must"), false));
        // 设置是否多选
        childNode.setMultiple(Convert.toBool(map.get("multiple"), true));
        // 设置描述
        childNode.setDesc(Convert.toStr(map.get("desc"), null));
        // 设置字典项，下拉框和复选框时用到
        childNode.setDict(Arrays.stream(enumClass.getEnumConstants())
            // 转换为NodeExtEnum类型
            .map(NodeExtEnum.class::cast)
            // 转换为DictItem对象
            .map(x ->
                new NodeExt.DictItem(x.getLabel(), x.getValue(), x.isSelected())
            ).toList());
        return childNode;
    }

    /**
     * 根据字典类型构建 `ChildNode` 对象
     * 查询字典数据，构建子节点配置
     *
     * @param dictType 字典类型
     * @return 构建的 `ChildNode` 对象
     */
    private NodeExt.ChildNode buildChildNode(String dictType) {
        // 查询字典类型信息
        DictTypeDTO dictTypeDTO = dictService.getDictType(dictType);
        // 如果字典类型不存在，返回null
        if (ObjectUtil.isNull(dictTypeDTO)) {
            return null;
        }
        // 创建子节点对象
        NodeExt.ChildNode childNode = new NodeExt.ChildNode();
        // 设置编码，此json中唯一
        childNode.setCode(dictType);
        // 设置label名称
        childNode.setLabel(dictTypeDTO.getDictName());
        // 设置类型：1：输入框 2：文本域 3：下拉框 4：选择框 5：用户选择器
        childNode.setType(3);
        // 设置是否必填
        childNode.setMust(false);
        // 设置是否多选
        childNode.setMultiple(true);
        // 设置描述 (可根据描述参数解析更多配置，如type，must，multiple等)
        childNode.setDesc(dictTypeDTO.getRemark());
        // 设置字典项，下拉框和复选框时用到
        childNode.setDict(dictService.getDictData(dictType)
            .stream().map(x ->
                new NodeExt.DictItem(x.getDictLabel(), x.getDictValue(), Convert.toBool(x.getIsDefault(), false))
            ).toList());
        return childNode;
    }

    /**
     * 解析扩展属性 JSON 并构建 Node 扩展属性对象
     * <p>
     * 根据传入的 JSON 字符串，将扩展属性分为三类：
     * 1. ButtonPermissionEnum：解析为按钮权限列表，标记每个按钮是否勾选
     * 2. CopySettingEnum：解析为抄送对象 ID 集合
     * 3. VariablesEnum：解析为自定义参数 Map
     *
     * <p>示例 JSON：
     * [
     * {"code": "ButtonPermissionEnum", "value": "back,termination"},
     * {"code": "CopySettingEnum", "value": "1,3,4,#{@spelRuleComponent.selectDeptLeaderById(#deptId", "#roleId)}"},
     * {"code": "VariablesEnum", "value": "key1=value1,key2=value2"}
     * ]
     *
     * @param ext      扩展属性 JSON 字符串
     * @param variable 流程变量
     * @return NodeExtVo 对象，封装按钮权限列表、抄送对象集合和自定义参数 Map
     */
    @Override
    public NodeExtVo parseNodeExt(String ext, Map<String, Object> variable) {
        // 创建节点扩展视图对象
        NodeExtVo nodeExtVo = new NodeExtVo();

        // 解析 JSON 为 Dict 列表
        List<Dict> nodeExtMap = JsonUtils.parseArrayMap(ext);
        // 如果解析结果为空，返回空对象
        if (ObjectUtil.isEmpty(nodeExtMap)) {
            return nodeExtVo;
        }

        // 遍历节点扩展配置
        for (Dict nodeExt : nodeExtMap) {
            // 获取编码
            String code = nodeExt.getStr("code");
            // 获取值
            String value = nodeExt.getStr("value");

            // 如果是按钮权限枚举
            if (ButtonPermissionEnum.class.getSimpleName().equals(code)) {
                // 解析按钮权限
                // 将 value 拆分为 Set<String>，便于精确匹配
                Set<String> buttonSet = StringUtils.str2Set(value, StringUtils.SEPARATOR);

                // 获取按钮字典配置
                NodeExt.ChildNode childNode = buildChildNode(ButtonPermissionEnum.class);

                // 构建 ButtonPermissionVo 列表
                List<ButtonPermissionVo> buttonList = Optional.ofNullable(childNode)
                    // 获取字典项
                    .map(NodeExt.ChildNode::getDict)
                    // 如果为空返回空列表
                    .orElse(List.of())
                    // 流式处理
                    .stream()
                    // 构建ButtonPermissionVo对象，标记是否勾选
                    .map(dict -> new ButtonPermissionVo(dict.getValue(), buttonSet.contains(dict.getValue())))
                    // 收集为列表
                    .toList();

                // 设置按钮权限列表
                nodeExtVo.setButtonPermissions(buttonList);

            } else if (CopySettingEnum.class.getSimpleName().equals(code)) {
                // 如果是抄送设置枚举
                // 智能分割SpEL表达式
                List<String> permissions = spelSmartSplit(value).stream()
                    // 解析SpEL表达式
                    .map(s -> {
                        List<String> result = ExpressionUtil.evalVariable(s, variable);
                        if (CollUtil.isNotEmpty(result)) {
                            return result;
                        }
                        return Collections.singletonList(s);
                    }).filter(Objects::nonNull)
                    // 扁平化处理
                    .flatMap(List::stream)
                    // 去重
                    .distinct()
                    // 收集为列表
                    .collect(Collectors.toList());
                // 转换权限标识
                List<String> copySettings = FlowEngine.permissionHandler().convertPermissions(permissions);
                // 解析抄送对象 ID 集合
                nodeExtVo.setCopySettings(new HashSet<>(copySettings));

            } else if (VariablesEnum.class.getSimpleName().equals(code)) {
                // 如果是自定义参数枚举
                // 解析自定义参数
                // 将 key=value 字符串拆分为 Map
                Map<String, String> variables = Arrays.stream(StringUtils.split(value, StringUtils.SEPARATOR))
                    // 按等号分割
                    .map(s -> StringUtils.split(s, "="))
                    // 过滤格式正确的项
                    .filter(arr -> arr.length == 2)
                    // 收集为Map
                    .collect(Collectors.toMap(arr -> arr[0], arr -> arr[1]));

                // 设置自定义参数
                nodeExtVo.setVariables(variables);
            } else {
                // 未知扩展类型，记录日志
                log.warn("未知扩展类型：code={}, value={}", code, value);
            }
        }
        return nodeExtVo;
    }

    /**
     * 按逗号分割字符串，但保留 #{...} 表达式和字符串常量中的逗号
     * 智能分割SpEL表达式，避免在表达式内部进行错误分割
     *
     * @param str 待分割的字符串
     * @return 分割后的字符串列表
     */
    private static List<String> spelSmartSplit(String str) {
        // 创建结果列表
        List<String> result = new ArrayList<>();
        // 如果字符串为空或空白，返回空列表
        if (str == null || str.trim().isEmpty()) {
            return result;
        }

        // 创建StringBuilder用于构建token
        StringBuilder token = new StringBuilder();
        // #{...} 的嵌套深度
        int depth = 0;
        // 是否在字符串常量中（" 或 '）
        boolean inString = false;
        // 当前字符串引号类型
        char stringQuote = 0;

        // 遍历字符串每个字符
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);

            // 检测进入 SpEL 表达式 #{...}
            if (!inString && c == '#' && depth == 0 && checkNext(str, i, '{')) {
                depth++;
                token.append("#{");
                // 跳过 {
                i++;
                continue;
            }

            // 在表达式中遇到 { 或 } 改变嵌套深度
            if (!inString && depth > 0) {
                if (c == '{') {
                    depth++;
                } else if (c == '}') {
                    depth--;
                }
                token.append(c);
                continue;
            }

            // 检测字符串开始/结束
            if (depth > 0 && (c == '"' || c == '\'')) {
                if (!inString) {
                    inString = true;
                    stringQuote = c;
                } else if (stringQuote == c) {
                    inString = false;
                }
                token.append(c);
                continue;
            }

            // 外层逗号才分割
            if (c == ',' && depth == 0 && !inString) {
                String part = token.toString().trim();
                if (!part.isEmpty()) {
                    result.add(part);
                }
                token.setLength(0);
                continue;
            }

            token.append(c);
        }

        // 添加最后一个token
        String part = token.toString().trim();
        if (!part.isEmpty()) {
            result.add(part);
        }

        return result;
    }

    /**
     * 检查下一个字符是否匹配预期值
     * 用于检测SpEL表达式开始标记
     *
     * @param str 源字符串
     * @param index 当前索引
     * @param expected 预期字符
     * @return 是否匹配
     */
    private static boolean checkNext(String str, int index, char expected) {
        // 检查索引是否越界且下一个字符是否匹配
        return index + 1 < str.length() && str.charAt(index + 1) == expected;
    }

}
