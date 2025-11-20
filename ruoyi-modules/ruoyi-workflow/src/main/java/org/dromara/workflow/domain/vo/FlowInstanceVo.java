// 包声明：定义当前类所在的包路径，org.dromara.workflow.domain.vo 表示工作流模块视图对象层
// package关键字用于声明类所在的包，便于组织和管理代码，避免命名冲突
package org.dromara.workflow.domain.vo;

// Lombok注解：自动生成getter、setter、toString、equals、hashCode等方法
import lombok.Data;
// 翻译注解：用于字段值翻译，将ID翻译为名称
import org.dromara.common.translation.annotation.Translation;
// 翻译常量：定义翻译类型
import org.dromara.common.translation.constant.TransConstant;
// 工作流常量：定义工作流相关的常量
import org.dromara.workflow.common.constant.FlowConstant;

// Java日期类：用于表示日期和时间
import java.util.Date;

/**
 * 流程实例视图对象
 * 用于封装流程实例查询结果，返回给前端展示
 * 包含流程实例基本信息、流程定义信息、业务扩展信息等
 *
 * @author may
 */
// Lombok注解：自动生成getter、setter、toString、equals、hashCode等方法
@Data
public class FlowInstanceVo {

    /**
     * 流程实例ID
     * 流程实例表的主键，唯一标识一个流程实例
     */
    private Long id;

    /**
     * 创建时间
     * 流程实例的创建时间，记录实例启动的时间点
     */
    private Date createTime;

    /**
     * 更新时间
     * 流程实例的最后一次更新时间
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
     * 关联流程定义表的主键，标识实例所属的流程定义
     */
    private Long definitionId;

    /**
     * 流程定义名称
     * 流程定义的名称，如"请假流程"、"报销流程"
     */
    private String flowName;

    /**
     * 流程定义编码
     * 流程定义的唯一编码，如"leave_process"、"expense_process"
     */
    private String flowCode;

    /**
     * 业务ID
     * 关联具体业务数据的主键（如请假单ID、报销单ID）
     * 用于将流程实例与具体业务数据关联
     */
    private String businessId;

    /**
     * 节点类型
     * 流程节点的类型：0开始节点、1中间节点、2结束节点、3互斥网关、4并行网关
     */
    private Integer nodeType;

    /**
     * 流程节点编码
     * 每个流程的nodeCode是唯一的，即definitionId+nodeCode唯一，在数据库层面做了控制
     */
    private String nodeCode;

    /**
     * 流程节点名称
     * 流程节点的名称，如"部门经理审批"、"总经理审批"
     */
    private String nodeName;

    /**
     * 流程变量
     * 流程实例的变量数据，以JSON字符串形式存储
     */
    private String variable;

    /**
     * 流程状态
     * 流程实例的当前状态：0待提交、1审批中、2审批通过、3自动通过、8已完成、9已退回、10失效
     */
    private String flowStatus;

    /**
     * 流程状态名称
     * 流程状态的中文名称，用于前端展示
     */
    private String flowStatusName;

    /**
     * 流程激活状态
     * 流程实例的激活状态：0挂起、1激活
     */
    private Integer activityStatus;

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
     * 扩展字段
     * 预留给业务系统使用的扩展字段
     */
    private String ext;

    /**
     * 流程定义版本
     * 流程定义的版本号，用于版本管理
     */
    private String version;

    /**
     * 创建者ID
     * 创建流程实例的用户ID
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

    // 业务扩展信息开始
    /**
     * 业务编码
     * 关联的业务数据的编码，如请假单号、报销单号
     */
    private String businessCode;

    /**
     * 业务标题
     * 关联的业务数据的标题，用于在实例列表中显示业务摘要
     */
    private String businessTitle;
    // 业务扩展信息结束

}
