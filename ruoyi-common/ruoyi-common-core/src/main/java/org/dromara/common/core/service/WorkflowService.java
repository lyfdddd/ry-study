// 定义通用工作流服务接口，提供流程引擎相关的核心功能
package org.dromara.common.core.service;

import org.dromara.common.core.domain.dto.CompleteTaskDTO;
import org.dromara.common.core.domain.dto.StartProcessDTO;
import org.dromara.common.core.domain.dto.StartProcessReturnDTO;

import java.util.List;
import java.util.Map;

/**
 * 通用 工作流服务
 * 该接口定义了工作流引擎的核心操作方法，是系统中流程管理的基础服务
 * 主要用于业务流程的启动、办理、状态查询、变量管理等操作
 * 实现类通常在ruoyi-workflow模块中，通过Spring的@Service注解注入
 * 支持多租户、流程变量、任务办理等高级功能
 *
 * @author may
 */
public interface WorkflowService {

    /**
     * 运行中的实例 删除程实例，删除历史记录，删除业务与流程关联信息
     * 彻底清理流程相关数据，包括运行中的实例、历史记录、业务关联表
     * 用于业务数据删除时的联动清理，确保数据一致性
     * 注意：此操作不可恢复，会删除所有流程相关数据
     *
     * @param businessIds 业务id列表，对应业务表中的主键
     * @return 结果 true表示删除成功，false表示删除失败
     */
    boolean deleteInstance(List<Long> businessIds);

    /**
     * 获取当前流程状态
     * 通过任务ID查询流程实例的当前业务状态
     * 业务状态是流程实例中的一个特殊变量，用于标识流程的业务处理阶段
     * 常用于前端展示流程进度或进行业务逻辑判断
     *
     * @param taskId 任务id，流程任务表中的主键
     * @return 状态 业务状态值，如"审批中"、"已完成"、"已驳回"等
     */
    String getBusinessStatusByTaskId(Long taskId);

    /**
     * 获取当前流程状态
     * 通过业务ID查询流程实例的当前业务状态
     * 与getBusinessStatusByTaskId类似，但通过业务主键查询
     * 适用于只知道业务ID但需要获取流程状态的场景
     *
     * @param businessId 业务id，业务表中的主键（字符串类型）
     * @return 状态 业务状态值，如"审批中"、"已完成"、"已驳回"等
     */
    String getBusinessStatus(String businessId);

    /**
     * 设置流程变量
     * 向指定的流程实例中设置变量，变量可在流程执行过程中使用
     * 流程变量可用于条件判断、动态分配任务、传递业务数据等
     * 变量会持久化到流程引擎中，在整个流程生命周期内有效
     *
     * @param instanceId 流程实例id，流程实例表中的主键
     * @param variable   流程变量，Map结构，key为变量名，value为变量值
     */
    void setVariable(Long instanceId, Map<String, Object> variable);

    /**
     * 获取流程变量
     * 查询指定流程实例中的所有变量
     * 用于获取流程执行过程中设置的变量，如审批意见、业务数据等
     * 返回的Map包含该实例的所有变量，可用于业务逻辑处理
     *
     * @param instanceId 流程实例id，流程实例表中的主键
     * @return Map<String, Object> 包含所有流程变量的Map集合
     */
    Map<String, Object> instanceVariable(Long instanceId);

    /**
     * 按照业务id查询流程实例id
     * 通过业务主键查询对应的流程实例ID
     * 建立业务数据与流程实例的关联关系，便于双向查询
     * 常用于业务详情页展示流程信息或流程监控
     *
     * @param businessId 业务id，业务表中的主键（字符串类型）
     * @return 结果 流程实例ID，如果未找到返回null
     */
    Long getInstanceIdByBusinessId(String businessId);

    /**
     * 新增租户流程定义
     * 为指定租户同步流程定义，实现多租户下的流程隔离
     * 每个租户可以有自己独立的流程定义，互不影响
     * 用于租户初始化或流程定义更新时的同步操作
     *
     * @param tenantId 租户id，租户表中的主键
     */
    void syncDef(String tenantId);

    /**
     * 启动流程
     * 根据传入的参数启动一个新的流程实例
     * 包含流程定义KEY、业务ID、启动人、流程变量等信息
     * 启动成功后会返回流程实例信息，包括实例ID、业务状态等
     *
     * @param startProcess 启动流程参数，包含流程定义、业务数据、启动人等信息
     * @return 结果 启动流程返回信息，包含实例ID、业务状态等
     */
    StartProcessReturnDTO startWorkFlow(StartProcessDTO startProcess);

    /**
     * 办理任务
     * 系统后台发起审批 无用户信息 需要忽略权限
     * completeTask.getVariables().put("ignore", true);
     * 用于系统自动化处理任务，如定时任务、消息队列消费等场景
     * 通过设置ignore变量为true来跳过权限校验
     *
     * @param completeTask 办理任务参数，包含任务ID、办理意见、流程变量等
     * @return 结果 true表示办理成功，false表示办理失败
     */
    boolean completeTask(CompleteTaskDTO completeTask);

    /**
     * 办理任务
     * 普通用户办理任务，需要当前用户有办理权限
     * 用于用户在前端界面点击"同意"、"驳回"等操作
     * 系统会自动校验当前用户是否为任务的办理人
     *
     * @param taskId  任务ID，流程任务表中的主键
     * @param message 办理意见，用户的审批意见或备注信息
     * @return 结果 true表示办理成功，false表示办理失败
     */
    boolean completeTask(Long taskId, String message);

    /**
     * 启动流程并办理第一个任务
     * 组合操作：先启动流程，然后立即办理第一个任务
     * 适用于流程第一个节点是自动办理或系统代办的场景
     * 如：用户提交申请后，系统自动完成"提交申请"节点
     *
     * @param startProcess 启动流程参数，包含流程定义、业务数据、启动人等信息
     * @return 结果 true表示操作成功，false表示操作失败
     */
    boolean startCompleteTask(StartProcessDTO startProcess);
}
