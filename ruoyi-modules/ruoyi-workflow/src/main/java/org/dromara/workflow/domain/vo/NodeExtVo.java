// 包声明：定义当前类所在的包路径，org.dromara.workflow.domain.vo 表示工作流模块视图对象层
// package关键字用于声明类所在的包，便于组织和管理代码，避免命名冲突
package org.dromara.workflow.domain.vo;

// Lombok注解：自动生成getter、setter、toString、equals、hashCode等方法
import lombok.Data;

// Java序列化接口：用于序列化版本控制
import java.io.Serial;
// Java序列化接口：标记类可序列化
import java.io.Serializable;
// Java集合类：List接口
import java.util.List;
// Java集合类：Map接口
import java.util.Map;
// Java集合类：Set接口
import java.util.Set;

/**
 * 节点扩展属性解析结果视图对象
 * <p>
 * 用于封装从扩展属性 JSON 中解析出的各类信息，包括按钮权限、抄送对象和自定义参数。
 * 实现Serializable接口，支持序列化传输
 * 通过解析流程节点扩展属性，提供前端展示所需的数据结构
 *
 * @author AprilWind
 */
// Lombok注解：自动生成getter、setter、toString、equals、hashCode等方法
@Data
public class NodeExtVo implements Serializable {

    // Java序列化版本UID，用于反序列化版本控制
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 按钮权限列表
     * <p>
     * 根据扩展属性中 ButtonPermissionEnum 类型的数据生成，每个元素表示一个按钮及其是否勾选。
     * 用于控制流程节点上可操作的按钮，如"同意"、"拒绝"、"转办"等
     */
    private List<ButtonPermissionVo> buttonPermissions;

    /**
     * 抄送对象 ID 集合
     * <p>
     * 根据扩展属性中 CopySettingEnum 类型的数据生成，存储需要抄送的对象 ID
     * 用于记录流程节点需要抄送的人员或部门
     */
    private Set<String> copySettings;

    /**
     * 自定义参数 Map
     * <p>
     * 根据扩展属性中 VariablesEnum 类型的数据生成，存储 key=value 格式的自定义参数
     * 用于传递流程节点所需的自定义业务参数
     */
    private Map<String, String> variables;

}
