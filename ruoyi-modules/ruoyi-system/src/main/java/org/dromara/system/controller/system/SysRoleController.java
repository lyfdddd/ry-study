// 角色信息管理控制器，提供角色的CRUD、数据权限、用户授权和部门树查询功能
package org.dromara.system.controller.system;

// Sa-Token权限校验注解，用于接口鉴权
import cn.dev33.satoken.annotation.SaCheckPermission;
// Hutool树形结构工具类
import cn.hutool.core.lang.tree.Tree;
// Servlet响应对象，用于文件导出
import jakarta.servlet.http.HttpServletResponse;
// Lombok注解，自动生成构造函数
import lombok.RequiredArgsConstructor;
// 统一响应结果封装类
import org.dromara.common.core.domain.R;
// Excel工具类，用于数据导出
import org.dromara.common.excel.utils.ExcelUtil;
// 防重复提交注解
import org.dromara.common.idempotent.annotation.RepeatSubmit;
// 操作日志注解，记录业务操作
import org.dromara.common.log.annotation.Log;
// 业务类型枚举
import org.dromara.common.log.enums.BusinessType;
// 分页查询对象
import org.dromara.common.mybatis.core.page.PageQuery;
// 表格数据信息封装类
import org.dromara.common.mybatis.core.page.TableDataInfo;
// 基础控制器，提供通用响应方法
import org.dromara.common.web.core.BaseController;
// 用户角色关联实体
import org.dromara.system.domain.SysUserRole;
// 部门业务对象
import org.dromara.system.domain.bo.SysDeptBo;
// 角色业务对象
import org.dromara.system.domain.bo.SysRoleBo;
// 用户业务对象
import org.dromara.system.domain.bo.SysUserBo;
// 角色视图对象
import org.dromara.system.domain.vo.SysRoleVo;
// 用户视图对象
import org.dromara.system.domain.vo.SysUserVo;
// 部门服务接口
import org.dromara.system.service.ISysDeptService;
// 角色服务接口
import org.dromara.system.service.ISysRoleService;
// 用户服务接口
import org.dromara.system.service.ISysUserService;
// Spring校验注解
import org.springframework.validation.annotation.Validated;
// Spring Web绑定注解
import org.springframework.web.bind.annotation.*;

// Java集合类
import java.util.List;

/**
 * 角色信息管理控制器
 * 提供角色的CRUD、数据权限、用户授权和部门树查询功能
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
// 请求路径映射，所有接口前缀为/system/role
@RequestMapping("/system/role")
public class SysRoleController extends BaseController {

    // 角色服务接口，自动注入
    private final ISysRoleService roleService;
    // 用户服务接口，自动注入
    private final ISysUserService userService;
    // 部门服务接口，自动注入
    private final ISysDeptService deptService;

    /**
     * 获取角色信息列表
     * 分页查询角色信息，支持条件筛选
     */
    // Sa-Token权限校验，需要system:role:list权限
    @SaCheckPermission("system:role:list")
    // GET请求映射，路径为/system/role/list
    @GetMapping("/list")
    public TableDataInfo<SysRoleVo> list(SysRoleBo role, PageQuery pageQuery) {
        // 调用服务层分页查询角色列表
        return roleService.selectPageRoleList(role, pageQuery);
    }

    /**
     * 导出角色信息列表
     * 将角色数据导出为Excel文件
     */
    // 操作日志注解，记录业务操作，标题为"角色管理"，类型为导出
    @Log(title = "角色管理", businessType = BusinessType.EXPORT)
    // Sa-Token权限校验，需要system:role:export权限
    @SaCheckPermission("system:role:export")
    // POST请求映射，路径为/system/role/export
    @PostMapping("/export")
    public void export(SysRoleBo role, HttpServletResponse response) {
        // 调用服务层查询所有角色列表
        List<SysRoleVo> list = roleService.selectRoleList(role);
        // 使用Excel工具类导出数据到响应流
        ExcelUtil.exportExcel(list, "角色数据", SysRoleVo.class, response);
    }

    /**
     * 根据角色编号获取详细信息
     * 根据主键ID查询角色详情，并校验数据权限
     *
     * @param roleId 角色ID
     */
    // Sa-Token权限校验，需要system:role:query权限
    @SaCheckPermission("system:role:query")
    // GET请求映射，路径为/system/role/{roleId}
    @GetMapping(value = "/{roleId}")
    public R<SysRoleVo> getInfo(@PathVariable Long roleId) {
        // 校验角色数据权限，确保当前用户有权限查看该角色
        roleService.checkRoleDataScope(roleId);
        // 调用服务层查询角色详情并返回
        return R.ok(roleService.selectRoleById(roleId));
    }

    /**
     * 新增角色
     * 添加新的角色信息
     */
    // Sa-Token权限校验，需要system:role:add权限
    @SaCheckPermission("system:role:add")
    // 操作日志注解，记录业务操作，标题为"角色管理"，类型为新增
    @Log(title = "角色管理", businessType = BusinessType.INSERT)
    // 防重复提交注解，防止重复新增
    @RepeatSubmit()
    // POST请求映射，路径为/system/role
    @PostMapping
    public R<Void> add(@Validated @RequestBody SysRoleBo role) {
        // 校验角色是否允许操作（防止修改内置角色）
        roleService.checkRoleAllowed(role);
        // 校验角色名称唯一性
        if (!roleService.checkRoleNameUnique(role)) {
            return R.fail("新增角色'" + role.getRoleName() + "'失败，角色名称已存在");
        // 校验角色权限字符串唯一性
        } else if (!roleService.checkRoleKeyUnique(role)) {
            return R.fail("新增角色'" + role.getRoleName() + "'失败，角色权限已存在");
        }
        // 调用服务层新增角色，并返回操作结果
        return toAjax(roleService.insertRole(role));

    }

    /**
     * 修改保存角色
     * 更新角色信息
     */
    // Sa-Token权限校验，需要system:role:edit权限
    @SaCheckPermission("system:role:edit")
    // 操作日志注解，记录业务操作，标题为"角色管理"，类型为更新
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    // 防重复提交注解，防止重复修改
    @RepeatSubmit()
    // PUT请求映射，路径为/system/role
    @PutMapping
    public R<Void> edit(@Validated @RequestBody SysRoleBo role) {
        // 校验角色是否允许操作（防止修改内置角色）
        roleService.checkRoleAllowed(role);
        // 校验角色数据权限，确保当前用户有权限修改该角色
        roleService.checkRoleDataScope(role.getRoleId());
        // 校验角色名称唯一性
        if (!roleService.checkRoleNameUnique(role)) {
            return R.fail("修改角色'" + role.getRoleName() + "'失败，角色名称已存在");
        // 校验角色权限字符串唯一性
        } else if (!roleService.checkRoleKeyUnique(role)) {
            return R.fail("修改角色'" + role.getRoleName() + "'失败，角色权限已存在");
        }

        // 调用服务层更新角色信息
        if (roleService.updateRole(role) > 0) {
            // 清除该角色下的在线用户（权限变更需要重新登录）
            roleService.cleanOnlineUserByRole(role.getRoleId());
            return R.ok();
        }
        return R.fail("修改角色'" + role.getRoleName() + "'失败，请联系管理员");
    }

    /**
     * 修改保存数据权限
     * 配置角色的数据权限范围（全部、自定义、本部门、本部门及子部门、仅本人）
     */
    // Sa-Token权限校验，需要system:role:edit权限
    @SaCheckPermission("system:role:edit")
    // 操作日志注解，记录业务操作，标题为"角色管理"，类型为更新
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    // 防重复提交注解，防止重复修改
    @RepeatSubmit()
    // PUT请求映射，路径为/system/role/dataScope
    @PutMapping("/dataScope")
    public R<Void> dataScope(@RequestBody SysRoleBo role) {
        // 校验角色是否允许操作（防止修改内置角色）
        roleService.checkRoleAllowed(role);
        // 校验角色数据权限，确保当前用户有权限修改该角色
        roleService.checkRoleDataScope(role.getRoleId());
        // 调用服务层配置角色数据权限，并返回操作结果
        return toAjax(roleService.authDataScope(role));
    }

    /**
     * 状态修改
     * 启用或禁用角色
     */
    // Sa-Token权限校验，需要system:role:edit权限
    @SaCheckPermission("system:role:edit")
    // 操作日志注解，记录业务操作，标题为"角色管理"，类型为更新
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    // 防重复提交注解，防止重复修改
    @RepeatSubmit()
    // PUT请求映射，路径为/system/role/changeStatus
    @PutMapping("/changeStatus")
    public R<Void> changeStatus(@RequestBody SysRoleBo role) {
        // 校验角色是否允许操作（防止修改内置角色）
        roleService.checkRoleAllowed(role);
        // 校验角色数据权限，确保当前用户有权限修改该角色
        roleService.checkRoleDataScope(role.getRoleId());
        // 调用服务层更新角色状态，并返回操作结果
        return toAjax(roleService.updateRoleStatus(role.getRoleId(), role.getStatus()));
    }

    /**
     * 删除角色
     * 批量删除角色信息
     *
     * @param roleIds 角色ID数组
     */
    // Sa-Token权限校验，需要system:role:remove权限
    @SaCheckPermission("system:role:remove")
    // 操作日志注解，记录业务操作，标题为"角色管理"，类型为删除
    @Log(title = "角色管理", businessType = BusinessType.DELETE)
    // DELETE请求映射，路径为/system/role/{roleIds}，支持多个id用逗号分隔
    @DeleteMapping("/{roleIds}")
    public R<Void> remove(@PathVariable Long[] roleIds) {
        // 调用服务层批量删除角色，并返回操作结果
        return toAjax(roleService.deleteRoleByIds(List.of(roleIds)));
    }

    /**
     * 获取角色选择框列表
     * 根据角色ID数组查询角色列表，用于下拉选择框
     *
     * @param roleIds 角色ID数组
     */
    // Sa-Token权限校验，需要system:role:query权限
    @SaCheckPermission("system:role:query")
    // GET请求映射，路径为/system/role/optionselect
    @GetMapping("/optionselect")
    public R<List<SysRoleVo>> optionselect(@RequestParam(required = false) Long[] roleIds) {
        // 调用服务层查询角色列表，如果roleIds为空则查询所有角色
        return R.ok(roleService.selectRoleByIds(roleIds == null ? null : List.of(roleIds)));
    }

    /**
     * 查询已分配用户角色列表
     * 分页查询已分配指定角色的用户列表
     */
    // Sa-Token权限校验，需要system:role:list权限
    @SaCheckPermission("system:role:list")
    // GET请求映射，路径为/system/role/authUser/allocatedList
    @GetMapping("/authUser/allocatedList")
    public TableDataInfo<SysUserVo> allocatedList(SysUserBo user, PageQuery pageQuery) {
        // 调用用户服务层查询已分配角色的用户列表
        return userService.selectAllocatedList(user, pageQuery);
    }

    /**
     * 查询未分配用户角色列表
     * 分页查询未分配指定角色的用户列表
     */
    // Sa-Token权限校验，需要system:role:list权限
    @SaCheckPermission("system:role:list")
    // GET请求映射，路径为/system/role/authUser/unallocatedList
    @GetMapping("/authUser/unallocatedList")
    public TableDataInfo<SysUserVo> unallocatedList(SysUserBo user, PageQuery pageQuery) {
        // 调用用户服务层查询未分配角色的用户列表
        return userService.selectUnallocatedList(user, pageQuery);
    }

    /**
     * 取消授权用户
     * 取消单个用户的角色授权
     */
    // Sa-Token权限校验，需要system:role:edit权限
    @SaCheckPermission("system:role:edit")
    // 操作日志注解，记录业务操作，标题为"角色管理"，类型为授权
    @Log(title = "角色管理", businessType = BusinessType.GRANT)
    // 防重复提交注解，防止重复取消授权
    @RepeatSubmit()
    // PUT请求映射，路径为/system/role/authUser/cancel
    @PutMapping("/authUser/cancel")
    public R<Void> cancelAuthUser(@RequestBody SysUserRole userRole) {
        // 调用服务层取消用户角色授权，并返回操作结果
        return toAjax(roleService.deleteAuthUser(userRole));
    }

    /**
     * 批量取消授权用户
     * 批量取消多个用户的角色授权
     *
     * @param roleId  角色ID
     * @param userIds 用户ID数组
     */
    // Sa-Token权限校验，需要system:role:edit权限
    @SaCheckPermission("system:role:edit")
    // 操作日志注解，记录业务操作，标题为"角色管理"，类型为授权
    @Log(title = "角色管理", businessType = BusinessType.GRANT)
    // 防重复提交注解，防止重复取消授权
    @RepeatSubmit()
    // PUT请求映射，路径为/system/role/authUser/cancelAll
    @PutMapping("/authUser/cancelAll")
    public R<Void> cancelAuthUserAll(Long roleId, Long[] userIds) {
        // 调用服务层批量取消用户角色授权，并返回操作结果
        return toAjax(roleService.deleteAuthUsers(roleId, userIds));
    }

    /**
     * 批量选择用户授权
     * 批量为多个用户分配角色
     *
     * @param roleId  角色ID
     * @param userIds 用户ID数组
     */
    // Sa-Token权限校验，需要system:role:edit权限
    @SaCheckPermission("system:role:edit")
    // 操作日志注解，记录业务操作，标题为"角色管理"，类型为授权
    @Log(title = "角色管理", businessType = BusinessType.GRANT)
    // 防重复提交注解，防止重复授权
    @RepeatSubmit()
    // PUT请求映射，路径为/system/role/authUser/selectAll
    @PutMapping("/authUser/selectAll")
    public R<Void> selectAuthUserAll(Long roleId, Long[] userIds) {
        // 校验角色数据权限，确保当前用户有权限操作该角色
        roleService.checkRoleDataScope(roleId);
        // 调用服务层批量为用户分配角色，并返回操作结果
        return toAjax(roleService.insertAuthUsers(roleId, userIds));
    }

    /**
     * 获取对应角色部门树列表
     * 查询角色已分配的部门列表和全部部门树结构
     *
     * @param roleId 角色ID
     */
    // Sa-Token权限校验，需要system:role:list权限
    @SaCheckPermission("system:role:list")
    // GET请求映射，路径为/system/role/deptTree/{roleId}
    @GetMapping(value = "/deptTree/{roleId}")
    public R<DeptTreeSelectVo> roleDeptTreeselect(@PathVariable("roleId") Long roleId) {
        // 创建部门树选择视图对象，包含已选中的部门ID列表和全部部门树
        DeptTreeSelectVo selectVo = new DeptTreeSelectVo(
            deptService.selectDeptListByRoleId(roleId),
            deptService.selectDeptTreeList(new SysDeptBo()));
        // 返回部门树选择信息
        return R.ok(selectVo);
    }

    /**
     * 角色部门列表树信息记录类
     * 封装角色部门授权时需要的已选中部门ID列表和部门树结构
     *
     * @param checkedKeys 选中部门列表
     * @param depts       下拉树结构列表
     */
    public record DeptTreeSelectVo(List<Long> checkedKeys, List<Tree<Long>> depts) {}

}
