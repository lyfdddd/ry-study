// 包声明：定义当前服务接口所在的包路径，org.dromara.workflow.service 表示工作流模块服务层
// package关键字用于声明接口所在的包，便于组织和管理代码，避免命名冲突
package org.dromara.workflow.service;

// 启动流程返回DTO：封装流程启动后的返回数据
import org.dromara.common.core.domain.dto.StartProcessReturnDTO;
// 用户数据传输对象：封装用户基本信息
import org.dromara.common.core.domain.dto.UserDTO;
// 分页查询对象：封装分页参数
import org.dromara.common.mybatis.core.page.PageQuery;
// 分页数据对象：封装分页结果
import org.dromara.common.mybatis.core.page.TableDataInfo;
// 流程节点实体：Warm-Flow工作流引擎的节点对象
import org.dromara.warm.flow.core.entity.Node;
// 任务实体：Warm-Flow工作流引擎的任务对象
import org.dromara.warm.flow.core.entity.Task;
// 历史任务实体：Warm-Flow工作流引擎的历史任务对象
import org.dromara.warm.flow.orm.entity.FlowHisTask;
// 流程节点实体：Warm-Flow工作流引擎的流程节点对象
import org.dromara.warm.flow.orm.entity.FlowNode;
// 流程任务实体：Warm-Flow工作流引擎的流程任务对象
import org.dromara.warm.flow.orm.entity.FlowTask;
// 工作流业务对象：封装各种业务参数
import org.dromara.workflow.domain.bo.*;
// 历史任务视图对象：封装历史任务响应数据
import org.dromara.workflow.domain.vo.FlowHisTaskVo;
// 流程任务视图对象：封装流程任务响应数据
import org.dromara.workflow.domain.vo.FlowTaskVo;

// List集合接口：用于存储列表数据
import java.util.List;

/**
 * 任务 服务层
 * 定义工作流任务相关的业务逻辑方法
 * 包括任务启动、办理、查询、驳回、终止等操作
 * 使用接口定义规范，实现类提供具体实现
 *
 * @author may
 */

// 任务服务接口
public interface IFlwTaskService {

    /**
     * 启动任务
     * 启动一个新的工作流实例
     *
     * @param startProcessBo 启动流程参数
     * @return 启动流程返回DTO
     */
    // 启动工作流
    StartProcessReturnDTO startWorkFlow(StartProcessBo startProcessBo);

    /**
     * 办理任务
     * 完成当前任务并流转到下一节点
     *
     * @param completeTaskBo 办理任务参数
     * @return 是否办理成功
     */
    // 完成任务
    boolean completeTask(CompleteTaskBo completeTaskBo);

    /**
     * 添加抄送人
     * 为任务添加抄送人员
     *
     * @param task         任务信息
     * @param flowCopyList 抄送人列表
     */
    // 设置抄送人
    void setCopy(Task task, List<FlowCopyBo> flowCopyList);

    /**
     * 查询当前用户的待办任务
     * 查询当前登录用户的待办任务列表
     *
     * @param flowTaskBo 查询参数
     * @param pageQuery  分页参数
     * @return 分页结果
     */
    // 查询我的待办
    TableDataInfo<FlowTaskVo> pageByTaskWait(FlowTaskBo flowTaskBo, PageQuery pageQuery);

    /**
     * 查询当前租户所有待办任务
     * 查询当前租户下所有用户的待办任务
     *
     * @param flowTaskBo 查询参数
     * @param pageQuery  分页参数
     * @return 分页结果
     */
    // 查询所有待办
    TableDataInfo<FlowHisTaskVo> pageByTaskFinish(FlowTaskBo flowTaskBo, PageQuery pageQuery);

    /**
     * 查询待办任务
     * 查询所有待办任务（管理员权限）
     *
     * @param flowTaskBo 查询参数
     * @param pageQuery  分页参数
     * @return 分页结果
     */
    // 查询待办任务
    TableDataInfo<FlowTaskVo> pageByAllTaskWait(FlowTaskBo flowTaskBo, PageQuery pageQuery);

    /**
     * 查询已办任务
     * 查询所有已办任务
     *
     * @param flowTaskBo 查询参数
     * @param pageQuery  分页参数
     * @return 分页结果
     */
    // 查询已办任务
    TableDataInfo<FlowHisTaskVo> pageByAllTaskFinish(FlowTaskBo flowTaskBo, PageQuery pageQuery);

    /**
     * 查询当前用户的抄送
     * 查询当前登录用户的抄送任务
     *
     * @param flowTaskBo 查询参数
     * @param pageQuery  分页参数
     * @return 分页结果
     */
    // 查询我的抄送
    TableDataInfo<FlowTaskVo> pageByTaskCopy(FlowTaskBo flowTaskBo, PageQuery pageQuery);

    /**
     * 修改任务办理人
     * 批量修改任务的办理人
     *
     * @param taskIdList 任务ID列表
     * @param userId     用户ID
     * @return 是否修改成功
     */
    // 更新办理人
    boolean updateAssignee(List<Long> taskIdList, String userId);

    /**
     * 驳回审批
     * 将任务驳回到指定节点
     *
     * @param bo 驳回参数
     * @return 是否驳回成功
     */
    // 驳回流程
    boolean backProcess(BackProcessBo bo);

    /**
     * 获取可驳回的前置节点
     * 查询当前任务可以驳回到哪些前置节点
     *
     * @param taskId       任务ID
     * @param nowNodeCode  当前节点编码
     * @return 可驳回的节点列表
     */
    // 获取可驳回节点
    List<Node> getBackTaskNode(Long taskId, String nowNodeCode);

    /**
     * 终止任务
     * 强制终止流程实例
     *
     * @param bo 终止参数
     * @return 是否终止成功
     */
    // 终止任务
    boolean terminationTask(FlowTerminationBo bo);

    /**
     * 按照任务id查询任务
     * 根据任务ID列表查询任务列表
     *
     * @param taskIdList 任务ID列表
     * @return 任务列表
     */
    // 查询任务列表
    List<FlowTask> selectByIdList(List<Long> taskIdList);

    /**
     * 按照任务id查询任务
     * 根据任务ID查询任务详情
     *
     * @param taskId 任务ID
     * @return 任务视图对象
     */
    // 查询任务详情
    FlowTaskVo selectById(Long taskId);

    /**
     * 获取下一节点信息
     * 查询当前任务的下一节点列表
     *
     * @param bo 查询参数
     * @return 节点列表
     */
    // 获取下一节点
    List<FlowNode> getNextNodeList(FlowNextNodeBo bo);

    /**
     * 按照任务id查询任务
     * 根据任务ID查询历史任务
     *
     * @param taskId 任务ID
     * @return 历史任务对象
     */
    // 查询历史任务
    FlowHisTask selectHisTaskById(Long taskId);

    /**
     * 按照实例id查询任务
     * 根据流程实例ID查询任务列表
     *
     * @param instanceId 流程实例ID
     * @return 任务列表
     */
    // 查询实例任务
    List<FlowTask> selectByInstId(Long instanceId);

    /**
     * 按照实例id查询任务
     * 根据流程实例ID列表查询任务列表
     *
     * @param instanceIds 流程实例ID列表
     * @return 任务列表
     */
    // 查询实例任务列表
    List<FlowTask> selectByInstIds(List<Long> instanceIds);

    /**
     * 判断流程是否已结束（即该流程实例下是否还有未完成的任务）
     * 检查流程实例是否已完成所有任务
     *
     * @param instanceId 流程实例ID
     * @return true表示任务已全部结束，false表示仍有任务存在
     */
    // 判断流程是否结束
    boolean isTaskEnd(Long instanceId);

    /**
     * 任务操作
     * 执行委派、转办、加签、减签等操作
     *
     * @param bo            操作参数
     * @param taskOperation 操作类型：delegateTask（委派）、transferTask（转办）、addSignature（加签）、reductionSignature（减签）
     * @return 是否操作成功
     */
    // 任务操作
    boolean taskOperation(TaskOperationBo bo, String taskOperation);

    /**
     * 获取当前任务的所有办理人
     * 查询指定任务的所有办理人信息
     *
     * @param taskIds 任务ID列表
     * @return 用户列表
     */
    // 获取任务办理人
    List<UserDTO> currentTaskAllUser(List<Long> taskIds);

    /**
     * 按照节点编码查询节点
     * 根据节点编码和流程定义ID查询节点
     *
     * @param nodeCode     节点编码
     * @param definitionId 流程定义ID
     * @return 节点对象
     */
    // 查询节点
    FlowNode getByNodeCode(String nodeCode, Long definitionId);

    /**
     * 催办任务
     * 发送催办通知给任务办理人
     *
     * @param bo 催办参数
     * @return 是否催办成功
     */
    // 催办任务
    boolean urgeTask(FlowUrgeTaskBo bo);
}
