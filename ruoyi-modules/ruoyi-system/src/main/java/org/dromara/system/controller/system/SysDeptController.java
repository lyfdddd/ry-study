// 部门管理控制器，提供部门的CRUD、树形结构、数据权限校验功能
package org.dromara.system.controller.system;

// Sa-Token权限校验注解，用于接口鉴权
import cn.dev33.satoken.annotation.SaCheckPermission;
// Hutool类型转换工具类
import cn.hutool.core.convert.Convert;
// Lombok注解，自动生成构造函数
import lombok.RequiredArgsConstructor;
// 系统常量定义
import org.dromara.common.core.constant.SystemConstants;
// 统一响应结果封装类
import org.dromara.common.core.domain.R;
// 字符串工具类
import org.dromara.common.core.utils.StringUtils;
// 防重复提交注解
import org.dromara.common.idempotent.annotation.RepeatSubmit;
// 操作日志注解，记录业务操作
import org.dromara.common.log.annotation.Log;
// 业务类型枚举
import org.dromara.common.log.enums.BusinessType;
// 基础控制器，提供通用响应方法
import org.dromara.common.web.core.BaseController;
// 部门业务对象
import org.dromara.system.domain.bo.SysDeptBo;
// 部门视图对象
import org.dromara.system.domain.vo.SysDeptVo;
// 部门服务接口
import org.dromara.system.service.ISysDeptService;
// 岗位服务接口
import org.dromara.system.service.ISysPostService;
// Spring校验注解
import org.springframework.validation.annotation.Validated;
// Spring Web绑定注解
import org.springframework.web.bind.annotation.*;

// Java集合类
import java.util.List;

/**
 * 部门管理控制器
 * 提供部门的CRUD、树形结构、数据权限校验功能
 * 继承BaseController获取通用响应方法
 *
 * @author Lion Li
 */
// Spring校验注解，启用方法参数校验
@Validated
// Lombok注解，自动生成包含所有final字段的构造函数
@RequiredArgsConstructor
// REST控制器注解，自动将返回值序列化为JSON
@RestController
// 请求路径映射，所有接口前缀为/system/dept
@RequestMapping("/system/dept")
public class SysDeptController extends BaseController {

    // 部门服务接口，自动注入
    private final ISysDeptService deptService;
    // 岗位服务接口，自动注入，用于删除部门时检查岗位关联
    private final ISysPostService postService;

    /**
     * 获取部门列表
     * 查询所有部门列表，支持条件筛选
     */
    // Sa-Token权限校验，需要system:dept:list权限
    @SaCheckPermission("system:dept:list")
    // GET请求映射，路径为/system/dept/list
    @GetMapping("/list")
    public R<List<SysDeptVo>> list(SysDeptBo dept) {
        // 调用服务层查询部门列表
        List<SysDeptVo> depts = deptService.selectDeptList(dept);
        // 返回成功响应
        return R.ok(depts);
    }

    /**
     * 查询部门列表（排除节点）
     * 查询部门列表并排除指定部门及其子部门，用于编辑时防止选择自身或子部门作为上级
     *
     * @param deptId 部门ID，需要排除的部门
     */
    // Sa-Token权限校验，需要system:dept:list权限
    @SaCheckPermission("system:dept:list")
    // GET请求映射，路径为/system/dept/list/exclude/{deptId}
    @GetMapping("/list/exclude/{deptId}")
    public R<List<SysDeptVo>> excludeChild(@PathVariable(value = "deptId", required = false) Long deptId) {
        // 查询所有部门列表
        List<SysDeptVo> depts = deptService.selectDeptList(new SysDeptBo());
        // 移除指定部门及其子部门
        depts.removeIf(d -> d.getDeptId().equals(deptId)
            // 使用Hutool转换部门ID为字符串，检查祖先列表是否包含该ID
            || StringUtils.splitList(d.getAncestors()).contains(Convert.toStr(deptId)));
        // 返回过滤后的部门列表
        return R.ok(depts);
    }

    /**
     * 根据部门编号获取详细信息
     * 根据主键ID查询部门详情，并校验数据权限
     *
     * @param deptId 部门ID
     */
    // Sa-Token权限校验，需要system:dept:query权限
    @SaCheckPermission("system:dept:query")
    // GET请求映射，路径为/system/dept/{deptId}
    @GetMapping(value = "/{deptId}")
    public R<SysDeptVo> getInfo(@PathVariable Long deptId) {
        // 校验部门数据权限，确保用户有权限访问该部门
        deptService.checkDeptDataScope(deptId);
        // 调用服务层查询部门详情并返回
        return R.ok(deptService.selectDeptById(deptId));
    }

    /**
     * 新增部门
     * 添加新的部门，需要校验部门名称唯一性
     */
    // Sa-Token权限校验，需要system:dept:add权限
    @SaCheckPermission("system:dept:add")
    // 操作日志注解，记录业务操作，标题为"部门管理"，类型为新增
    @Log(title = "部门管理", businessType = BusinessType.INSERT)
    // 防重复提交注解，防止重复新增
    @RepeatSubmit()
    // POST请求映射，路径为/system/dept
    @PostMapping
    public R<Void> add(@Validated @RequestBody SysDeptBo dept) {
        // 校验部门名称是否唯一，如果不唯一返回错误信息
        if (!deptService.checkDeptNameUnique(dept)) {
            return R.fail("新增部门'" + dept.getDeptName() + "'失败，部门名称已存在");
        }
        // 调用服务层新增部门，并返回操作结果
        return toAjax(deptService.insertDept(dept));
    }

    /**
     * 修改部门
     * 更新部门信息，需要校验部门名称唯一性、上级部门不能是自己、禁用状态检查
     */
    // Sa-Token权限校验，需要system:dept:edit权限
    @SaCheckPermission("system:dept:edit")
    // 操作日志注解，记录业务操作，标题为"部门管理"，类型为更新
    @Log(title = "部门管理", businessType = BusinessType.UPDATE)
    // 防重复提交注解，防止重复修改
    @RepeatSubmit()
    // PUT请求映射，路径为/system/dept
    @PutMapping
    public R<Void> edit(@Validated @RequestBody SysDeptBo dept) {
        // 获取部门ID
        Long deptId = dept.getDeptId();
        // 校验部门数据权限，确保用户有权限修改该部门
        deptService.checkDeptDataScope(deptId);
        // 校验部门名称是否唯一（排除当前记录），如果不唯一返回错误信息
        if (!deptService.checkDeptNameUnique(dept)) {
            return R.fail("修改部门'" + dept.getDeptName() + "'失败，部门名称已存在");
        } else if (dept.getParentId().equals(deptId)) {
            // 校验上级部门不能是自己
            return R.fail("修改部门'" + dept.getDeptName() + "'失败，上级部门不能是自己");
        } else if (StringUtils.equals(SystemConstants.DISABLE, dept.getStatus())) {
            // 如果状态为禁用，检查是否有未停用的子部门
            if (deptService.selectNormalChildrenDeptById(deptId) > 0) {
                return R.fail("该部门包含未停用的子部门!");
            } else if (deptService.checkDeptExistUser(deptId)) {
                // 检查部门下是否存在已分配的用户
                return R.fail("该部门下存在已分配用户，不能禁用!");
            }
        }
        // 调用服务层更新部门信息，并返回操作结果
        return toAjax(deptService.updateDept(dept));
    }

    /**
     * 删除部门
     * 删除部门及其关联数据，需要检查多种约束条件
     *
     * @param deptId 部门ID
     */
    // Sa-Token权限校验，需要system:dept:remove权限
    @SaCheckPermission("system:dept:remove")
    // 操作日志注解，记录业务操作，标题为"部门管理"，类型为删除
    @Log(title = "部门管理", businessType = BusinessType.DELETE)
    // DELETE请求映射，路径为/system/dept/{deptId}
    @DeleteMapping("/{deptId}")
    public R<Void> remove(@PathVariable Long deptId) {
        // 检查是否为默认部门，默认部门不允许删除
        if (SystemConstants.DEFAULT_DEPT_ID.equals(deptId)) {
            return R.warn("默认部门,不允许删除");
        }
        // 检查是否存在下级部门
        if (deptService.hasChildByDeptId(deptId)) {
            return R.warn("存在下级部门,不允许删除");
        }
        // 检查部门下是否存在用户
        if (deptService.checkDeptExistUser(deptId)) {
            return R.warn("部门存在用户,不允许删除");
        }
        // 检查部门下是否存在岗位
        if (postService.countPostByDeptId(deptId) > 0) {
            return R.warn("部门存在岗位,不允许删除");
        }
        // 校验部门数据权限，确保用户有权限删除该部门
        deptService.checkDeptDataScope(deptId);
        // 调用服务层删除部门，并返回操作结果
        return toAjax(deptService.deleteDeptById(deptId));
    }

    /**
     * 获取部门选择框列表
     * 根据部门ID数组查询部门列表，用于前端选择框
     *
     * @param deptIds 部门ID数组
     */
    // Sa-Token权限校验，需要system:dept:query权限
    @SaCheckPermission("system:dept:query")
    // GET请求映射，路径为/system/dept/optionselect
    @GetMapping("/optionselect")
    public R<List<SysDeptVo>> optionselect(@RequestParam(required = false) Long[] deptIds) {
        // 调用服务层根据ID数组查询部门列表，如果deptIds为null则查询所有
        return R.ok(deptService.selectDeptByIds(deptIds == null ? null : List.of(deptIds)));
    }

}
