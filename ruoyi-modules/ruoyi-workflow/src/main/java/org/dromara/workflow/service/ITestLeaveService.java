// 包声明：定义当前服务接口所在的包路径，org.dromara.workflow.service 表示工作流模块服务层
// package关键字用于声明接口所在的包，便于组织和管理代码，避免命名冲突
package org.dromara.workflow.service;

// 分页查询对象：封装分页参数
import org.dromara.common.mybatis.core.page.PageQuery;
// 分页数据对象：封装分页结果
import org.dromara.common.mybatis.core.page.TableDataInfo;
// 请假业务对象：封装请假业务数据
import org.dromara.workflow.domain.bo.TestLeaveBo;
// 请假视图对象：封装请假响应数据
import org.dromara.workflow.domain.vo.TestLeaveVo;

// List集合接口：用于存储列表数据
import java.util.List;

/**
 * 请假Service接口
 * 定义请假相关的业务逻辑方法
 * 包括查询、新增、修改、删除、提交并发起流程等操作
 * 使用接口定义规范，实现类提供具体实现
 *
 * @author may
 * @date 2023-07-21
 */

// 请假服务接口
public interface ITestLeaveService {

    /**
     * 查询请假
     * 根据ID查询单条请假记录
     *
     * @param id 主键ID
     * @return 请假视图对象
     */
    // 根据ID查询请假详情
    TestLeaveVo queryById(Long id);

    /**
     * 查询请假列表
     * 分页查询请假列表
     *
     * @param bo 查询条件
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    // 分页查询请假列表
    TableDataInfo<TestLeaveVo> queryPageList(TestLeaveBo bo, PageQuery pageQuery);

    /**
     * 查询请假列表
     * 查询所有请假列表
     *
     * @param bo 查询条件
     * @return 请假列表
     */
    // 查询请假列表
    List<TestLeaveVo> queryList(TestLeaveBo bo);

    /**
     * 新增请假
     * 插入新的请假记录
     *
     * @param bo 请假业务对象
     * @return 请假视图对象
     */
    // 新增请假
    TestLeaveVo insertByBo(TestLeaveBo bo);

    /**
     * 提交请假并发起流程
     * 提交请假申请并启动工作流
     *
     * @param bo 请假业务对象
     * @return 请假视图对象
     */
    // 提交并发起流程
    TestLeaveVo submitAndFlowStart(TestLeaveBo bo);

    /**
     * 修改请假
     * 更新请假信息
     *
     * @param bo 请假业务对象
     * @return 请假视图对象
     */
    // 修改请假
    TestLeaveVo updateByBo(TestLeaveBo bo);

    /**
     * 校验并批量删除请假信息
     * 批量删除请假记录
     *
     * @param ids ID列表
     * @return 是否删除成功
     */
    // 批量删除请假
    Boolean deleteWithValidByIds(List<Long> ids);
}
