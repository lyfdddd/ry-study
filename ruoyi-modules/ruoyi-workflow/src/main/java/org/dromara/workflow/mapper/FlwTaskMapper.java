// 包声明：定义当前类所在的包路径，org.dromara.workflow.mapper 表示工作流模块数据访问层
// package关键字用于声明类所在的包，便于组织和管理代码，避免命名冲突
package org.dromara.workflow.mapper;

// MyBatis-Plus核心组件：查询条件包装器
import com.baomidou.mybatisplus.core.conditions.Wrapper;
// MyBatis-Plus核心组件：查询包装器
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
// MyBatis-Plus常量：MyBatis-Plus内置常量
import com.baomidou.mybatisplus.core.toolkit.Constants;
// MyBatis-Plus分页插件：分页对象
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
// MyBatis注解：参数注解，用于指定参数名称
import org.apache.ibatis.annotations.Param;
// 流程任务查询业务对象：封装查询条件
import org.dromara.workflow.domain.bo.FlowTaskBo;
// 历史任务视图对象：用于返回前端数据
import org.dromara.workflow.domain.vo.FlowHisTaskVo;
// 流程任务视图对象：用于返回前端数据
import org.dromara.workflow.domain.vo.FlowTaskVo;


/**
 * 任务信息Mapper接口
 * 数据访问层：提供流程任务查询操作
 * 使用MyBatis注解定义SQL映射
 *
 * @author may
 * @date 2024-03-02
 */
public interface FlwTaskMapper {

    /**
     * 获取待办信息
     * 分页查询当前用户的待办任务列表
     * 使用@Param注解指定参数名称，便于XML中引用
     *
     * @param page         分页对象，包含分页参数
     * @param queryWrapper 查询条件包装器，支持动态SQL
     * @return 分页结果，包含FlowTaskVo列表和总数
     */
    // 使用@Param注解指定参数名称，Constants.WRAPPER是MyBatis-Plus内置常量
    Page<FlowTaskVo> getListRunTask(@Param("page") Page<FlowTaskVo> page, @Param(Constants.WRAPPER) Wrapper<FlowTaskBo> queryWrapper);

    /**
     * 获取已办任务
     * 分页查询当前用户的已办任务列表
     * 使用@Param注解指定参数名称，便于XML中引用
     *
     * @param page         分页对象，包含分页参数
     * @param queryWrapper 查询条件包装器，支持动态SQL
     * @return 分页结果，包含FlowHisTaskVo列表和总数
     */
    // 使用@Param注解指定参数名称，Constants.WRAPPER是MyBatis-Plus内置常量
    Page<FlowHisTaskVo> getListFinishTask(@Param("page") Page<FlowTaskVo> page, @Param(Constants.WRAPPER) Wrapper<FlowTaskBo> queryWrapper);

    /**
     * 查询当前用户的抄送任务
     * 分页查询抄送给当前用户的任务列表
     * 使用QueryWrapper作为参数类型，支持更复杂的查询条件
     *
     * @param page         分页对象，包含分页参数
     * @param queryWrapper 查询条件包装器，支持动态SQL
     * @return 分页结果，包含FlowTaskVo列表和总数
     */
    // 使用@Param注解指定参数名称，Constants.WRAPPER是MyBatis-Plus内置常量
    Page<FlowTaskVo> getTaskCopyByPage(@Param("page") Page<FlowTaskVo> page, @Param(Constants.WRAPPER) QueryWrapper<FlowTaskBo> queryWrapper);
}
