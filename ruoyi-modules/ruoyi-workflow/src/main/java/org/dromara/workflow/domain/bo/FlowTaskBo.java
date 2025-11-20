// 包声明：定义当前类所在的包路径，org.dromara.workflow.domain.bo 表示工作流模块业务对象层
// package关键字用于声明类所在的包，便于组织和管理代码，避免命名冲突
package org.dromara.workflow.domain.bo;

// Lombok注解：自动生成getter、setter、toString、equals、hashCode等方法
import lombok.Data;

// Java序列化接口：用于序列化版本控制
import java.io.Serial;
// Java序列化接口：标记类可序列化
import java.io.Serializable;
// Java集合类：List接口
import java.util.List;

/**
 * 任务请求业务对象
 * 用于封装查询任务列表时的查询条件
 * 实现Serializable接口，支持序列化传输
 *
 * @author may
 */
// Lombok注解：自动生成getter、setter、toString、equals、hashCode等方法
@Data
public class FlowTaskBo implements Serializable {

    // Java序列化版本UID，用于反序列化版本控制
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 任务名称（节点名称）
     * 流程中任务节点的名称，如"部门经理审批"、"总经理审批"
     * 用于按任务名称筛选任务
     */
    private String nodeName;

    /**
     * 流程定义名称
     * 流程定义的名称，如"请假流程"、"报销流程"
     * 用于按流程定义名称筛选任务
     */
    private String flowName;

    /**
     * 流程定义编码
     * 流程定义的唯一编码，如"leave_process"、"expense_process"
     * 用于按流程定义编码筛选任务
     */
    private String flowCode;

    /**
     * 流程分类ID
     * 流程分类的唯一标识
     * 用于按流程分类筛选任务
     */
    private String category;

    /**
     * 流程实例ID
     * 流程实例的唯一标识
     * 用于按流程实例筛选任务
     */
    private Long instanceId;

    /**
     * 权限列表
     * 当前用户拥有的权限标识列表
     * 用于数据权限过滤，只查询有权限的任务
     */
    private List<String> permissionList;

    /**
     * 申请人ID列表
     * 流程实例申请人的用户ID列表
     * 用于按申请人筛选任务
     */
    private List<Long> createByIds;

}
