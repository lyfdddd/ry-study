// 定义工作流流程实例业务扩展的数据传输对象
package org.dromara.common.core.domain.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 流程实例业务扩展对象
 * 用于存储流程实例的业务扩展信息，建立流程与业务的关联关系
 * 包含业务ID、业务编码、业务标题等关键业务标识信息
 * 实现Serializable接口，支持分布式环境下的序列化传输
 * 通常作为StartProcessDTO的一部分，用于流程启动时的业务信息传递
 *
 * @author may
 * @date 2025-08-05
 */
// Lombok注解：自动生成getter、setter、toString、equals、hashCode方法
@Data
public class FlowInstanceBizExtDTO implements Serializable {

    // Java序列化版本号，用于反序列化时的版本兼容性检查
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     * 业务扩展表的主键，自增ID
     * 用于唯一标识一条业务扩展记录
     */
    private Long id;

    /**
     * 流程实例ID
     * 关联的流程实例主键，建立业务扩展与流程实例的关联关系
     * 一个流程实例对应一条业务扩展记录
     * 对应流程引擎中的流程实例表主键
     */
    private Long instanceId;

    /**
     * 业务ID
     * 业务系统的主键，用于关联具体的业务数据
     * 与StartProcessDTO中的businessId相对应
     * 例如：请假单ID、采购单ID等业务主键
     */
    private String businessId;

    /**
     * 业务编码
     * 业务的类型编码，用于区分不同的业务流程
     * 例如："leave"表示请假流程，"purchase"表示采购流程
     * 便于按业务类型进行统计和查询
     */
    private String businessCode;

    /**
     * 业务标题
     * 业务的显示标题，用于在流程列表、任务列表中展示
     * 通常是业务数据的摘要信息，如"张三的请假申请"、"技术部采购申请"
     * 提升用户体验，让用户快速识别业务流程内容
     */
    private String businessTitle;

}
