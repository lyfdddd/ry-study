// 定义工作流任务受让人服务接口，用于流程设计器获取任务执行人
package org.dromara.common.core.service;

import org.dromara.common.core.domain.dto.TaskAssigneeDTO;
import org.dromara.common.core.domain.model.TaskAssigneeBody;

/**
 * 工作流设计器获取任务执行人
 * 该接口定义了工作流引擎中任务分配相关的查询方法，支持按角色、岗位、部门、用户维度查询可办理任务的人员
 * 主要用于流程设计器配置任务节点时选择办理人，以及运行时动态分配任务
 * 实现类通常在ruoyi-workflow模块中，通过Spring的@Service注解注入
 *
 * @author Lion Li
 */
public interface TaskAssigneeService {

    /**
     * 查询角色并返回任务指派的列表，支持分页
     * 用于流程设计器配置按角色分配任务时，查询系统中的角色列表
     * 返回的角色信息包含角色ID、角色名称、角色编码等，用于任务节点配置
     *
     * @param taskQuery 查询条件，包含分页参数、角色名称模糊查询等
     * @return 办理人信息，包含角色列表和分页信息
     */
    TaskAssigneeDTO selectRolesByTaskAssigneeList(TaskAssigneeBody taskQuery);

    /**
     * 查询岗位并返回任务指派的列表，支持分页
     * 用于流程设计器配置按岗位分配任务时，查询系统中的岗位列表
     * 岗位是组织架构中的重要维度，常用于审批流程中的岗位级别审批
     *
     * @param taskQuery 查询条件，包含分页参数、岗位名称模糊查询等
     * @return 办理人信息，包含岗位列表和分页信息
     */
    TaskAssigneeDTO selectPostsByTaskAssigneeList(TaskAssigneeBody taskQuery);

    /**
     * 查询部门并返回任务指派的列表，支持分页
     * 用于流程设计器配置按部门分配任务时，查询系统中的部门列表
     * 部门维度常用于跨部门协作流程，如部门负责人审批等场景
     *
     * @param taskQuery 查询条件，包含分页参数、部门名称模糊查询等
     * @return 办理人信息，包含部门列表和分页信息
     */
    TaskAssigneeDTO selectDeptsByTaskAssigneeList(TaskAssigneeBody taskQuery);

    /**
     * 查询用户并返回任务指派的列表，支持分页
     * 用于流程设计器配置指定具体用户办理任务时，查询系统中的用户列表
     * 这是最细粒度的任务分配方式，直接指定到具体用户
     *
     * @param taskQuery 查询条件，包含分页参数、用户名称/账号模糊查询等
     * @return 办理人信息，包含用户列表和分页信息
     */
    TaskAssigneeDTO selectUsersByTaskAssigneeList(TaskAssigneeBody taskQuery);

}
