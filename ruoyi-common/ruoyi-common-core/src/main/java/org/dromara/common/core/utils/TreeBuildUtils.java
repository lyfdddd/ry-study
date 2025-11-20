// 基于Hutool的树形结构构建工具类，支持多根节点和叶子节点提取
// 该类封装了Hutool的TreeUtil工具类，提供树形结构构建的便捷方法
// 主要应用于组织架构、菜单管理、分类管理等需要树形展示的场景
package org.dromara.common.core.utils;

// Hutool集合工具类，提供集合判空、创建等操作
// CollUtil.isEmpty()用于判断集合是否为空，避免空指针异常
import cn.hutool.core.collection.CollUtil;
// Hutool树形结构核心类，表示树节点
// Tree类封装了树节点的属性和子节点列表
import cn.hutool.core.lang.tree.Tree;
// Hutool树节点配置类，用于自定义树节点字段映射
// 可以配置idKey、parentIdKey、nameKey等字段名
import cn.hutool.core.lang.tree.TreeNodeConfig;
// Hutool树形结构工具类，提供树构建的基础方法
// TreeUtil.build()是核心的树构建方法
import cn.hutool.core.lang.tree.TreeUtil;
// Hutool树节点解析器接口，用于将原始数据转换为树节点
// NodeParser允许自定义节点转换逻辑
import cn.hutool.core.lang.tree.parser.NodeParser;
// Lombok注解：设置构造方法访问级别为私有，防止类被实例化
// 工具类不应该被实例化，所有方法都是静态方法
import lombok.AccessLevel;
// Lombok注解：自动生成私有构造方法，使工具类无法被实例化
import lombok.NoArgsConstructor;
// 反射工具类，提供对象属性访问方法
// ReflectUtils.invokeGetter()用于通过getter方法获取属性值
import org.dromara.common.core.utils.reflect.ReflectUtils;

// Java List集合接口，用于存储树节点列表
import java.util.List;
// Java Set集合接口，用于存储节点ID集合
import java.util.Set;
// Java函数式接口，用于方法引用和Lambda表达式
import java.util.function.Function;
// Java Stream API，用于集合流式处理
import java.util.stream.Collectors;
// Java Stream接口，用于流式操作
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
    // 通过链式调用setNameKey()方法修改默认配置
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
    // 使用泛型支持任意类型的节点和ID
    public static <T, K> List<Tree<K>> build(List<T> list, NodeParser<T, K> nodeParser) {
        // 如果列表为空，返回空列表，避免后续操作出现空指针异常
        // CollUtil.isEmpty()同时检查null和空集合
        if (CollUtil.isEmpty(list)) {
            // 返回空ArrayList，避免返回null导致调用方需要额外判空
            return CollUtil.newArrayList();
        }
        // 从第一个元素获取parentId作为顶级节点ID
        // 使用反射工具类获取parentId属性值，支持任意类型的parentId字段
        // ReflectUtils.invokeGetter()通过getter方法获取属性值，避免硬编码字段名
        K k = ReflectUtils.invokeGetter(list.get(0), "parentId");
        // 调用TreeUtil.build构建树形结构，使用默认配置
        // 传入列表、根节点ID、配置和解析器，Hutool会自动构建树形结构
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
    // 允许调用方指定根节点，更灵活
    public static <T, K> List<Tree<K>> build(List<T> list, K parentId, NodeParser<T, K> nodeParser) {
        // 如果列表为空，返回空列表，避免后续操作出现空指针异常
        if (CollUtil.isEmpty(list)) {
            // 返回空ArrayList，避免返回null导致调用方需要额外判空
            return CollUtil.newArrayList();
        }
        // 调用TreeUtil.build构建树形结构，使用默认配置
        // parentId指定为顶级节点的父ID，通常是0或null
        // 使用Hutool的TreeUtil.build()方法构建树，传入自定义的根节点ID
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
    // 使用函数式接口支持灵活的数据提取方式
    public static <T, K> List<Tree<K>> buildMultiRoot(List<T> list, Function<T, K> getId, Function<T, K> getParentId, NodeParser<T, K> parser) {
        // 如果列表为空，返回空列表，避免后续操作出现空指针异常
        if (CollUtil.isEmpty(list)) {
            // 返回空ArrayList，避免返回null导致调用方需要额外判空
            return CollUtil.newArrayList();
        }

        // 获取所有parentId集合
        // 使用StreamUtils.toSet方法，将list中的每个元素的parentId提取为Set
        // StreamUtils.toSet()是项目封装的流式处理工具，将集合转换为Set
        Set<K> rootParentIds = StreamUtils.toSet(list, getParentId);
        // 获取所有id集合
        // 同样使用StreamUtils.toSet方法提取id
        Set<K> ids = StreamUtils.toSet(list, getId);
        // 移除所有有子节点的parentId，剩下的就是根节点的parentId
        // 这是森林结构的关键算法：根节点的parentId不会出现在任何节点的id中
        // 使用Set.removeAll()方法求差集，找出真正的根节点
        rootParentIds.removeAll(ids);

        // 构建每一个根parentId下的树，并合并成最终结果列表
        // 使用flatMap将多个List<Tree<K>>扁平化为一个List<Tree<K>>
        // Stream.flatMap()将每个根节点构建的树列表扁平化为一个流
        return rootParentIds.stream()
            // 为每个根节点ID构建树
            .flatMap(rootParentId -> TreeUtil.build(list, rootParentId, parser).stream())
            // 收集为List
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
        // 如果节点列表为空，返回空列表，避免后续操作出现空指针异常
        if (CollUtil.isEmpty(nodes)) {
            // 返回空ArrayList，避免返回null导致调用方需要额外判空
            return CollUtil.newArrayList();
        }
        // 使用Stream API扁平化处理所有节点
        // flatMap将多个Stream合并为一个，extractLeafNodes递归提取叶子节点
        // 调用extractLeafNodes()递归提取每个节点的叶子节点
        return nodes.stream()
            // 使用flatMap扁平化所有叶子节点流
            .flatMap(TreeBuildUtils::extractLeafNodes)
            // 收集为List
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
    // 使用Stream的递归扁平化处理，支持任意深度的树结构
    private static <K> Stream<Tree<K>> extractLeafNodes(Tree<K> node) {
        // 如果节点没有子节点，说明是叶子节点，返回包含该节点的Stream
        // Tree.hasChild()是Hutool提供的方法，判断节点是否有子节点
        if (!node.hasChild()) {
            // 使用Stream.of()创建单元素流
            return Stream.of(node);
        } else {
            // 递归调用，获取所有子节点的叶子节点
            // 使用flatMap将多个Stream合并为一个，实现递归扁平化
            // 获取子节点列表的流
            return node.getChildren().stream()
                // 递归调用extractLeafNodes()提取每个子节点的叶子节点
                .flatMap(TreeBuildUtils::extractLeafNodes);
        }
    }

}
