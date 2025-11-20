// 基于Hutool的树形结构构建工具类，支持多根节点和叶子节点提取
package org.dromara.common.core.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import cn.hutool.core.lang.tree.parser.NodeParser;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.dromara.common.core.utils.reflect.ReflectUtils;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 树形结构构建工具类
 * 扩展Hutool的TreeUtil，封装系统树构建的常用操作
 * 支持单根节点、多根节点树构建，以及叶子节点提取等功能
 * 提供默认配置，适配前端组件的字段命名规范
 * 支持森林结构（多棵树）的构建，适用于组织架构、菜单树等场景
 *
 * @author Lion Li
 */
// Lombok注解：生成私有构造方法，防止工具类被实例化
// 工具类不应该被实例化，所有方法都是静态方法
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TreeBuildUtils extends TreeUtil {

    /**
     * 默认树节点配置
     * 根据前端定制差异化字段，将nameKey设置为"label"以适配前端组件
     * 前端组件（如Element UI、Ant Design）通常使用label作为显示字段
     * 这样可以避免前后端字段不一致的问题
     */
    // 默认树节点配置，将nameKey设置为"label"以适配前端组件
    // 前端组件（如Element UI、Ant Design）通常使用label作为显示字段
    public static final TreeNodeConfig DEFAULT_CONFIG = TreeNodeConfig.DEFAULT_CONFIG.setNameKey("label");

    /**
     * 构建树形结构（自动检测根节点）
     * 自动从列表中第一个元素获取parentId作为顶级节点ID
     * 适用于标准树结构，根节点的parentId通常是0或null
     *
     * @param <T> 输入节点的类型（如实体类、DTO等）
     * @param <K> 节点ID的类型（如Long、String等）
     * @param list 节点列表，其中包含了要构建树形结构的所有节点
     * @param nodeParser 解析器，用于将输入节点转换为树节点
     * @return 构建好的树形结构列表
     */
    // 构建树形结构（自动检测根节点）
    // 自动从列表中第一个元素获取parentId作为顶级节点ID
    public static <T, K> List<Tree<K>> build(List<T> list, NodeParser<T, K> nodeParser) {
        // 如果列表为空，返回空列表
        if (CollUtil.isEmpty(list)) {
            return CollUtil.newArrayList();
        }
        // 从第一个元素获取parentId作为顶级节点ID
        // 使用反射工具类获取parentId属性值，支持任意类型的parentId字段
        K k = ReflectUtils.invokeGetter(list.get(0), "parentId");
        // 调用TreeUtil.build构建树形结构，使用默认配置
        return TreeUtil.build(list, k, DEFAULT_CONFIG, nodeParser);
    }

    /**
     * 构建树形结构（指定根节点）
     * 根据指定的parentId作为顶级节点构建树形结构
     * 适用于需要指定特定根节点的场景，如查询某个部门下的子部门
     *
     * @param <T> 输入节点的类型（如实体类、DTO等）
     * @param <K> 节点ID的类型（如Long、String等）
     * @param parentId 顶级节点ID（根节点的parentId）
     * @param list 节点列表，其中包含了要构建树形结构的所有节点
     * @param nodeParser 解析器，用于将输入节点转换为树节点
     * @return 构建好的树形结构列表
     */
    // 构建树形结构（指定根节点）
    // 根据指定的parentId作为顶级节点构建树形结构
    public static <T, K> List<Tree<K>> build(List<T> list, K parentId, NodeParser<T, K> nodeParser) {
        // 如果列表为空，返回空列表
        if (CollUtil.isEmpty(list)) {
            return CollUtil.newArrayList();
        }
        // 调用TreeUtil.build构建树形结构，使用默认配置
        // parentId指定为顶级节点的父ID，通常是0或null
        return TreeUtil.build(list, parentId, DEFAULT_CONFIG, nodeParser);
    }

    /**
     * 构建多根节点的树结构（支持多个顶级节点）
     * 自动检测所有根节点（没有父节点的节点），并为每个根节点构建子树
     * 适用于森林结构（多棵树）的场景，如多个顶级部门
     * 算法：找出所有没有子节点的parentId，这些就是根节点的parentId
     *
     * @param <T> 原始数据类型（如实体类、DTO等）
     * @param <K> 节点ID类型（如Long、String）
     * @param list 原始数据列表
     * @param getId 获取节点ID的方法引用，例如：node -> node.getId()
     * @param getParentId 获取节点父级ID的方法引用，例如：node -> node.getParentId()
     * @param parser 树节点属性映射器，用于将原始节点T转为Tree节点
     * @return 构建完成的树形结构（可能包含多个顶级根节点）
     */
    // 构建多根节点的树结构（支持多个顶级节点）
    // 自动检测所有根节点（没有父节点的节点），并为每个根节点构建子树
    public static <T, K> List<Tree<K>> buildMultiRoot(List<T> list, Function<T, K> getId, Function<T, K> getParentId, NodeParser<T, K> parser) {
        // 如果列表为空，返回空列表
        if (CollUtil.isEmpty(list)) {
            return CollUtil.newArrayList();
        }

        // 获取所有parentId集合
        // 使用StreamUtils.toSet方法，将list中的每个元素的parentId提取为Set
        Set<K> rootParentIds = StreamUtils.toSet(list, getParentId);
        // 获取所有id集合
        // 同样使用StreamUtils.toSet方法提取id
        Set<K> ids = StreamUtils.toSet(list, getId);
        // 移除所有有子节点的parentId，剩下的就是根节点的parentId
        // 这是森林结构的关键算法：根节点的parentId不会出现在任何节点的id中
        rootParentIds.removeAll(ids);

        // 构建每一个根parentId下的树，并合并成最终结果列表
        // 使用flatMap将多个List<Tree<K>>扁平化为一个List<Tree<K>>
        return rootParentIds.stream()
            .flatMap(rootParentId -> TreeUtil.build(list, rootParentId, parser).stream())
            .collect(Collectors.toList());
    }

    /**
     * 获取节点列表中所有节点的叶子节点
     * 叶子节点是指没有子节点的节点
     * 适用于需要获取所有底层节点的场景，如获取所有子部门
     *
     * @param <K> 节点ID的类型
     * @param nodes 节点列表（树形结构）
     * @return 包含所有叶子节点的列表
     */
    // 获取节点列表中所有节点的叶子节点
    // 叶子节点是指没有子节点的节点
    public static <K> List<Tree<K>> getLeafNodes(List<Tree<K>> nodes) {
        // 如果节点列表为空，返回空列表
        if (CollUtil.isEmpty(nodes)) {
            return CollUtil.newArrayList();
        }
        // 使用Stream API扁平化处理所有节点
        // flatMap将多个Stream合并为一个，extractLeafNodes递归提取叶子节点
        return nodes.stream()
            .flatMap(TreeBuildUtils::extractLeafNodes)
            .collect(Collectors.toList());
    }

    /**
     * 获取指定节点下的所有叶子节点（递归方法）
     * 私有方法，用于递归提取叶子节点
     * 使用Stream的递归扁平化处理，支持任意深度的树结构
     *
     * @param <K> 节点ID的类型
     * @param node 要查找叶子节点的根节点
     * @return 包含所有叶子节点的Stream
     */
    // 获取指定节点下的所有叶子节点（递归方法）
    // 私有方法，用于递归提取叶子节点
    private static <K> Stream<Tree<K>> extractLeafNodes(Tree<K> node) {
        // 如果节点没有子节点，说明是叶子节点，返回包含该节点的Stream
        if (!node.hasChild()) {
            return Stream.of(node);
        } else {
            // 递归调用，获取所有子节点的叶子节点
            // 使用flatMap将多个Stream合并为一个，实现递归扁平化
            return node.getChildren().stream()
                .flatMap(TreeBuildUtils::extractLeafNodes);
        }
    }

}
