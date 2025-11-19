// 定义工作流流程启动返回的数据传输对象
package org.dromara.common.core.domain.dto;


import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 启动流程返回对象
 * 用于工作流引擎启动流程后的结果封装
 * 包含新创建的流程实例ID和第一个任务ID
 * 实现Serializable接口，支持分布式环境下的序列化传输
 * 前端可以根据返回的ID进行后续操作，如跳转到任务办理页面
 *
 * @author Lion Li
 */
// Lombok注解：自动生成getter、setter、toString、equals、hashCode方法
@Data
public class StartProcessReturnDTO implements Serializable {

    // Java序列化版本号，用于反序列化时的版本兼容性检查
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 流程实例id
     * 新创建的流程实例的唯一标识
     * 对应流程引擎中的流程实例表主键
     * 用于后续查询流程状态、办理任务等操作
     */
    private Long processInstanceId;

    /**
     * 任务id
     * 流程启动后创建的第一个任务的ID
     * 如果流程第一个节点是用户任务，则返回该任务的ID
     * 如果第一个节点是自动节点，则可能返回null
     * 用于直接跳转到任务办理页面或查询任务详情
     */
    private Long taskId;

}
