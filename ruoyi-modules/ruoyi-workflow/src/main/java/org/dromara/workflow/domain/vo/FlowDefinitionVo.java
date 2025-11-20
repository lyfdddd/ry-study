// 包声明：定义当前类所在的包路径，org.dromara.workflow.domain.vo 表示工作流模块视图对象层
// package关键字用于声明类所在的包，便于组织和管理代码，避免命名冲突
package org.dromara.workflow.domain.vo;

// Lombok注解：自动生成getter、setter、toString、equals、hashCode等方法
import lombok.Data;
// 翻译注解：用于字段值翻译，将ID翻译为名称
import org.dromara.common.translation.annotation.Translation;
// 工作流常量：定义工作流相关的常量
import org.dromara.workflow.common.constant.FlowConstant;

// Java序列化接口：用于序列化版本控制
import java.io.Serial;
// Java序列化接口：标记类可序列化
import java.io.Serializable;
// Java日期类：用于表示日期和时间
import java.util.Date;

/**
 * 流程定义视图对象
 * 用于封装流程定义信息，返回给前端展示
 * 实现Serializable接口，支持序列化传输
 * 包含流程定义的基本信息、分类信息、表单配置、监听器配置等
 *
 * @author may
 */
// Lombok注解：自动生成getter、setter、toString、equals、hashCode等方法
@Data
public class FlowDefinitionVo implements Serializable {

    // Java序列化版本UID，用于反序列化版本控制
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     * 流程定义表的主键，唯一标识一个流程定义
     */
    private Long id;

    /**
     * 创建时间
     * 流程定义的创建时间，记录流程定义创建的时间点
     */
    private Date createTime;

    /**
     * 更新时间
     * 流程定义的最后一次更新时间
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
     * 流程定义编码
     * 流程定义的唯一编码，如"leave_process"、"expense_process"
     */
    private String flowCode;

    /**
     * 流程定义名称
     * 流程定义的名称，如"请假流程"、"报销流程"
     */
    private String flowName;

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
     * 流程版本
     * 流程定义的版本号，用于版本管理
     */
    private String version;

    /**
     * 是否发布
     * 0表示未发布，1表示已发布，9表示失效
     */
    private Integer isPublish;

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
     * 流程激活状态
     * 0表示挂起，1表示激活
     */
    private Integer activityStatus;

    /**
     * 监听器类型
     * 流程监听器的类型，用于事件监听
     */
    private String listenerType;

    /**
     * 监听器路径
     * 流程监听器的实现路径，指向具体的监听器类
     */
    private String listenerPath;

    /**
     * 扩展字段
     * 预留给业务系统使用的扩展字段，存储JSON格式数据
     */
    private String ext;
}
