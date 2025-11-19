// 定义工作流任务受让人的查询条件模型
package org.dromara.common.core.domain.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 任务受让人
 * 用于工作流设计器中查询任务执行人的条件封装
 * 包含权限编码、权限名称、权限分组等查询条件
 * 支持分页查询和时间范围筛选
 * 实现Serializable接口，支持分布式环境下的序列化传输
 * 通常作为TaskAssigneeService接口方法的参数
 *
 * @author AprilWind
 */
// Lombok注解：自动生成getter、setter、toString、equals、hashCode方法
@Data
// Lombok注解：生成无参构造方法
@NoArgsConstructor
public class TaskAssigneeBody implements Serializable {

    // Java序列化版本号，用于反序列化时的版本兼容性检查
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 权限编码
     * 角色、岗位、部门或用户的编码
     * 用于精确查询特定权限标识的执行人
     * 例如："admin"、"manager"、"dept_100"等
     */
    private String handlerCode;

    /**
     * 权限名称
     * 角色、岗位、部门或用户的名称
     * 支持模糊查询，用于按名称搜索执行人
     * 例如："管理员"、"部门经理"、"技术部"等
     */
    private String handlerName;

    /**
     * 权限分组
     * 权限的分组标识，用于按分组筛选执行人
     * 可以是部门ID、租户ID等分组维度
     * 例如："dept_100"、"tenant_1"等
     */
    private String groupId;

    /**
     * 开始时间
     * 查询时间范围的开始时间
     * 格式为字符串，支持多种日期格式
     * 用于按创建时间或更新时间筛选数据
     */
    private String beginTime;

    /**
     * 结束时间
     * 查询时间范围的结束时间
     * 格式为字符串，支持多种日期格式
     * 与beginTime配合使用，构成完整的时间范围查询
     */
    private String endTime;

    /**
     * 当前页
     * 分页查询的当前页码，从1开始
     * 默认值为1，表示第一页
     * 用于控制查询结果的分页展示
     */
    private Integer pageNum = 1;

    /**
     * 每页显示条数
     * 分页查询的每页记录数
     * 默认值为10，表示每页显示10条记录
     * 用于控制查询结果的分页大小
     */
    private Integer pageSize = 10;

}
