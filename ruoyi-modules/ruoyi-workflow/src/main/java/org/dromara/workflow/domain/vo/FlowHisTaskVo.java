// 包声明：定义当前类所在的包路径，org.dromara.workflow.domain.vo 表示工作流模块视图对象层
// package关键字用于声明类所在的包，便于组织和管理代码，避免命名冲突
package org.dromara.workflow.domain.vo;

// Lombok注解：自动生成getter、setter、toString、equals、hashCode等方法
import lombok.Data;
// 日期工具类：提供日期计算和格式化功能
import org.dromara.common.core.utils.DateUtils;
// 翻译注解：用于字段值翻译，将ID翻译为名称
import org.dromara.common.translation.annotation.Translation;
// 翻译常量：定义翻译类型
import org.dromara.common.translation.constant.TransConstant;
// Warm-Flow工作流核心枚举：协作方式枚举
import org.dromara.warm.flow.core.enums.CooperateType;
// 工作流常量：定义工作流相关的常量
import org.dromara.workflow.common.constant.FlowConstant;

// Java序列化接口：用于序列化版本控制
import java.io.Serial;
// Java序列化接口：标记类可序列化
import java.io.Serializable;
// Java日期类：用于表示日期和时间
import java.util.Date;
// Java集合类：List接口
import java.util.List;

/**
 * 历史任务视图对象
 * 用于封装历史任务信息，返回给前端展示
 * 实现Serializable接口，支持序列化传输
 * 包含历史任务的基本信息、流程信息、审批信息、业务扩展信息等
 *
 * @author may
 */
// Lombok注解：自动生成getter、setter、toString、equals、hashCode等方法
@Data
public class FlowHisTaskVo implements Serializable {

    // Java序列化版本UID，用于反序列化版本控制
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     * 历史任务表的主键，唯一标识一个历史任务
     */
    private Long id;

    /**
     * 创建时间
     * 历史任务的创建时间，记录任务生成的时间点
     */
    private Date createTime;

    /**
     * 更新时间
     * 历史任务的最后一次更新时间
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
     * 流程定义名称
     * 流程定义的名称，如"请假流程"、"报销流程"
     */
    private String flowName;

    /**
     * 流程实例ID
     * 关联流程实例表的主键，标识任务所属的流程实例
     */
    private Long instanceId;

    /**
     * 任务ID
     * 关联任务表的主键，标识历史任务对应的当前任务
     */
    private Long taskId;

    /**
     * 协作方式
     * 1表示审批，2表示转办，3表示委派，4表示会签，5表示票签，6表示加签，7表示减签
     */
    private Integer cooperateType;

    /**
     * 协作方式名称
     * 协作方式的中文名称，用于前端展示
     */
    private String cooperateTypeName;

    /**
     * 业务ID
     * 关联具体业务数据的主键（如请假单ID、报销单ID）
     * 用于将历史任务与具体业务数据关联
     */
    private String businessId;

    /**
     * 开始节点编码
     * 流程开始节点的唯一编码
     */
    private String nodeCode;

    /**
     * 开始节点名称
     * 流程开始节点的名称
     */
    private String nodeName;

    /**
     * 开始节点类型
     * 流程节点的类型：0开始节点、1中间节点、2结束节点、3互斥网关、4并行网关
     */
    private Integer nodeType;

    /**
     * 目标节点编码
     * 流程目标节点的唯一编码
     */
    private String targetNodeCode;

    /**
     * 结束节点名称
     * 流程结束节点的名称
     */
    private String targetNodeName;

    /**
     * 审批者ID
     * 实际执行审批操作的用户ID
     */
    private String approver;

    /**
     * 审批者名称
     * 审批者的姓名，用于前端展示
     * 使用@Translation注解将用户ID翻译为昵称
     */
    // 翻译注解：将用户ID翻译为昵称，mapper指定字段名
    @Translation(type = TransConstant.USER_ID_TO_NICKNAME, mapper = "approver")
    private String approveName;

    /**
     * 协作人
     * 只有转办、会签、票签、委派才有协作人
     */
    private String collaborator;

    /**
     * 权限标识列表
     * 权限标识permissionFlag的list形式，用于控制任务的操作权限
     */
    private List<String> permissionList;

    /**
     * 跳转类型
     * PASS表示通过，REJECT表示退回，NONE表示无动作
     */
    private String skipType;

    /**
     * 流程状态
     * 流程实例的当前状态，如审批中、已完成等
     */
    private String flowStatus;

    /**
     * 任务状态
     * 任务的当前状态，如待办、已办等
     */
    private String flowTaskStatus;

    /**
     * 流程状态名称
     * 流程状态的中文名称，用于前端展示
     */
    private String flowStatusName;

    /**
     * 审批意见
     * 审批人填写的审批意见或备注
     */
    private String message;

    /**
     * 业务详情
     * 存储业务类的JSON格式数据，用于记录业务详细信息
     */
    private String ext;

    /**
     * 创建者ID
     * 创建历史任务的用户ID
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
     * 运行时长
     * 任务从创建到完成的运行时长，格式如"2小时30分钟"
     */
    private String runDuration;

    // 业务扩展信息开始
    /**
     * 业务编码
     * 关联的业务数据的编码，如请假单号、报销单号
     */
    private String businessCode;

    /**
     * 业务标题
     * 关联的业务数据的标题，用于在历史任务列表中显示业务摘要
     */
    private String businessTitle;
    // 业务扩展信息结束

    /**
     * 设置创建时间并计算任务运行时长
     * 当设置创建时间时，自动调用updateRunDuration方法计算运行时长
     * @param createTime 创建时间
     */
    public void setCreateTime(Date createTime) {
        // 设置创建时间属性
        this.createTime = createTime;
        // 调用更新运行时长方法
        updateRunDuration();
    }

    /**
     * 设置更新时间并计算任务运行时长
     * 当设置更新时间时，自动调用updateRunDuration方法计算运行时长
     * @param updateTime 更新时间
     */
    public void setUpdateTime(Date updateTime) {
        // 设置更新时间属性
        this.updateTime = updateTime;
        // 调用更新运行时长方法
        updateRunDuration();
    }

    /**
     * 更新运行时长
     * 私有方法，用于计算任务从创建到完成的运行时长
     * 使用DateUtils.getTimeDifference计算时间差
     */
    private void updateRunDuration() {
        // 如果创建时间和更新时间均不为空，计算它们之间的时长
        if (this.updateTime != null && this.createTime != null) {
            // 调用DateUtils工具类计算时间差，格式化为易读的字符串
            this.runDuration = DateUtils.getTimeDifference(this.updateTime, this.createTime);
        }
    }

    /**
     * 设置协作方式并获取协作方式名称
     * 当设置协作方式时，自动调用CooperateType.getValueByKey获取中文名称
     * @param cooperateType 协作方式编码
     */
    public void setCooperateType(Integer cooperateType) {
        // 设置协作方式属性
        this.cooperateType = cooperateType;
        // 调用CooperateType枚举的getValueByKey方法获取中文名称
        this.cooperateTypeName = CooperateType.getValueByKey(cooperateType);
    }

}
