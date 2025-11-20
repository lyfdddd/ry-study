// 包声明：定义当前类所在的包路径，org.dromara.workflow.domain.vo 表示工作流模块视图对象层
// package关键字用于声明类所在的包，便于组织和管理代码，避免命名冲突
package org.dromara.workflow.domain.vo;

// Lombok注解：自动生成getter、setter、toString、equals、hashCode等方法
import lombok.Data;
// 翻译注解：用于字段值翻译，将ID翻译为名称
import org.dromara.common.translation.annotation.Translation;
// 翻译常量：定义翻译类型
import org.dromara.common.translation.constant.TransConstant;
// Warm-Flow工作流核心实体：用户实体
import org.dromara.warm.flow.core.entity.User;
// 工作流常量：定义工作流相关的常量
import org.dromara.workflow.common.constant.FlowConstant;

// Java序列化接口：用于序列化版本控制
import java.io.Serial;
// Java序列化接口：标记类可序列化
import java.io.Serializable;
// Java数学类：BigDecimal用于精确计算
import java.math.BigDecimal;
// Java日期类：用于表示日期和时间
import java.util.Date;
// Java集合类：List接口
import java.util.List;
// Java集合类：Map接口
import java.util.Map;

/**
 * 任务视图对象
 * 用于封装任务查询结果，返回给前端展示
 * 实现Serializable接口，支持序列化传输
 * 包含任务基本信息、流程信息、办理人信息、业务扩展信息等
 *
 * @author may
 */
// Lombok注解：自动生成getter、setter、toString、equals、hashCode等方法
@Data
public class FlowTaskVo implements Serializable {

    // Java序列化版本UID，用于反序列化版本控制
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 任务ID
     * 流程任务表的主键，唯一标识一个任务
     */
    private Long id;

    /**
     * 创建时间
     * 任务的创建时间，记录任务生成的时间点
     */
    private Date createTime;

    /**
     * 更新时间
     * 任务的最后一次更新时间
     */
    private Date updateTime;

    /**
     * 租户ID
     * 多租户隔离标识，用于区分不同租户的数据
     */
    private String tenantId;

    /**
     * 删除标记
     * 逻辑删除标志，0表示正常，1表示已删除
     */
    private String delFlag;

    /**
     * 流程定义ID
     * 关联流程定义表的主键，标识任务所属的流程定义
     */
    private Long definitionId;

    /**
     * 流程实例ID
     * 关联流程实例表的主键，标识任务所属的流程实例
     */
    private Long instanceId;

    /**
     * 流程定义名称
     * 流程定义的名称，如"请假流程"、"报销流程"
     */
    private String flowName;

    /**
     * 业务ID
     * 关联具体业务数据的主键（如请假单ID、报销单ID）
     * 用于将流程任务与具体业务数据关联
     */
    private String businessId;

    /**
     * 节点编码
     * 流程节点的唯一编码，用于标识节点在流程中的位置
     */
    private String nodeCode;

    /**
     * 节点名称
     * 流程节点的名称，如"部门经理审批"、"总经理审批"
     */
    private String nodeName;

    /**
     * 节点类型
     * 流程节点的类型：0开始节点、1中间节点、2结束节点、3互斥网关、4并行网关
     */
    private Integer nodeType;

    /**
     * 权限标识列表
     * 权限标识permissionFlag的list形式，用于控制任务的操作权限
     */
    private List<String> permissionList;

    /**
     * 流程用户列表
     * 与任务相关的用户列表，如办理人、抄送人等
     */
    private List<User> userList;

    /**
     * 审批表单是否自定义
     * Y表示是自定义表单，N表示使用默认表单
     */
    private String formCustom;

    /**
     * 审批表单路径
     * 自定义表单的路径，用于前端加载表单
     */
    private String formPath;

    /**
     * 流程定义编码
     * 流程定义的唯一编码，如"leave_process"、"expense_process"
     */
    private String flowCode;

    /**
     * 流程版本号
     * 流程定义的版本号，用于版本管理
     */
    private String version;

    /**
     * 流程状态
     * 流程实例的当前状态，如审批中、已完成等
     */
    private String flowStatus;

    /**
     * 流程分类ID
     * 流程分类的唯一标识
     */
    private String category;

    /**
     * 流程分类名称
     * 流程分类的名称，如"人事流程"、"财务流程"
     * 使用@Translation注解将category ID翻译为名称
     */
    // 翻译注解：将category ID翻译为名称，使用FlowConstant.CATEGORY_ID_TO_NAME翻译器
    @Translation(type = FlowConstant.CATEGORY_ID_TO_NAME, mapper = "category")
    private String categoryName;

    /**
     * 流程状态名称
     * 流程状态的中文名称，用于前端展示
     * 使用@Translation注解将状态值翻译为标签
     */
    // 翻译注解：将字典值翻译为标签，mapper指定字段名，other指定字典类型
    @Translation(type = TransConstant.DICT_TYPE_TO_LABEL, mapper = "flowStatus", other = "wf_business_status")
    private String flowStatusName;

    /**
     * 办理人类型
     * 办理人的类型，如用户、角色、部门等
     */
    private String type;

    /**
     * 办理人IDs
     * 办理人的ID列表，多个ID用逗号分隔
     */
    private String assigneeIds;

    /**
     * 办理人名称
     * 办理人的姓名列表，用于前端展示
     * 使用@Translation注解将用户ID翻译为昵称
     */
    // 翻译注解：将用户ID翻译为昵称，mapper指定字段名
    @Translation(type = TransConstant.USER_ID_TO_NICKNAME, mapper = "assigneeIds")
    private String assigneeNames;

    /**
     * 抄送人ID
     * 抄送人的用户ID，多个ID用逗号分隔
     */
    private String processedBy;

    /**
     * 抄送人名称
     * 抄送人的姓名列表，用于前端展示
     * 使用@Translation注解将用户ID翻译为昵称
     */
    // 翻译注解：将用户ID翻译为昵称，mapper指定字段名
    @Translation(type = TransConstant.USER_ID_TO_NICKNAME, mapper = "processedBy")
    private String processedByName;

    /**
     * 流程签署比例值
     * 大于0表示票签、会签，用于控制会签的通过比例
     */
    private BigDecimal nodeRatio;

    /**
     * 申请人ID
     * 流程实例申请人的用户ID
     */
    private String createBy;

    /**
     * 申请人名称
     * 申请人的姓名，用于前端展示
     * 使用@Translation注解将用户ID翻译为昵称
     */
    // 翻译注解：将用户ID翻译为昵称，mapper指定字段名
    @Translation(type = TransConstant.USER_ID_TO_NICKNAME, mapper = "createBy")
    private String createByName;

    /**
     * 是否为申请人节点
     * true表示当前节点是申请人节点，false表示不是
     */
    private Boolean applyNode;

    /**
     * 按钮权限列表
     * 当前用户在任务上可以操作的按钮列表，如"同意"、"拒绝"、"转办"等
     */
    private List<ButtonPermissionVo> buttonList;

    /**
     * 抄送对象列表
     * 根据扩展属性中CopySettingEnum类型的数据生成，存储需要抄送的对象信息
     */
    private List<FlowCopyVo> copyList;

    /**
     * 自定义参数Map
     * 根据扩展属性中VariablesEnum类型的数据生成，存储key=value格式的自定义参数
     */
    private Map<String, String> varList;

    // 业务扩展信息开始
    /**
     * 业务编码
     * 关联的业务数据的编码，如请假单号、报销单号
     */
    private String businessCode;

    /**
     * 业务标题
     * 关联的业务数据的标题，用于在任务列表中显示业务摘要
     */
    private String businessTitle;
    // 业务扩展信息结束

}
