// 租户套餐管理控制器，提供租户套餐的CRUD、状态管理和下拉列表查询功能
package org.dromara.system.controller.system;

// Sa-Token权限校验注解，用于接口鉴权
import cn.dev33.satoken.annotation.SaCheckPermission;
// Sa-Token角色校验注解，用于超级管理员角色校验
import cn.dev33.satoken.annotation.SaCheckRole;
// 租户常量类，包含超级管理员角色KEY
import org.dromara.common.core.constant.TenantConstants;
// 统一响应结果封装类
import org.dromara.common.core.domain.R;
// 新增分组校验
import org.dromara.common.core.validate.AddGroup;
// 编辑分组校验
import org.dromara.common.core.validate.EditGroup;
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
// 租户套餐业务对象
import org.dromara.system.domain.bo.SysTenantPackageBo;
// 租户套餐视图对象
import org.dromara.system.domain.vo.SysTenantPackageVo;
// 租户套餐服务接口
import org.dromara.system.service.ISysTenantPackageService;
// Servlet响应对象，用于文件导出
import jakarta.servlet.http.HttpServletResponse;
// 非空校验注解，用于数组参数
import jakarta.validation.constraints.NotEmpty;
// 非空校验注解，用于单个参数
import jakarta.validation.constraints.NotNull;
// Lombok注解，自动生成构造函数
import lombok.RequiredArgsConstructor;
// Spring条件配置注解，当tenant.enable=true时启用
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
// Spring校验注解
import org.springframework.validation.annotation.Validated;
// Spring Web绑定注解
import org.springframework.web.bind.annotation.*;

// Java集合类
import java.util.List;

/**
 * 租户套餐管理控制器
 * 提供租户套餐的CRUD、状态管理和下拉列表查询功能
 * 仅当tenant.enable=true时启用，且需要超级管理员角色
 * 继承BaseController获取通用响应方法
 *
 * @author Michelle.Chung
 */
// Spring校验注解，启用方法参数校验
@Validated
// Lombok注解，自动生成包含所有final字段的构造函数
@RequiredArgsConstructor
// REST控制器注解，自动将返回值序列化为JSON
@RestController
// 请求路径映射，所有接口前缀为/system/tenant/package
@RequestMapping("/system/tenant/package")
// 条件配置注解，当tenant.enable=true时启用该控制器
@ConditionalOnProperty(value = "tenant.enable", havingValue = "true")
public class SysTenantPackageController extends BaseController {

    // 租户套餐服务接口，自动注入
    private final ISysTenantPackageService tenantPackageService;

    /**
     * 查询租户套餐列表
     * 分页查询租户套餐信息，支持条件筛选
     */
    // Sa-Token角色校验，需要超级管理员角色
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    // Sa-Token权限校验，需要system:tenantPackage:list权限
    @SaCheckPermission("system:tenantPackage:list")
    // GET请求映射，路径为/system/tenant/package/list
    @GetMapping("/list")
    public TableDataInfo<SysTenantPackageVo> list(SysTenantPackageBo bo, PageQuery pageQuery) {
        // 调用服务层分页查询租户套餐列表
        return tenantPackageService.queryPageList(bo, pageQuery);
    }

    /**
     * 查询租户套餐下拉选列表
     * 查询所有租户套餐列表，用于下拉选择框
     */
    // Sa-Token角色校验，需要超级管理员角色
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    // Sa-Token权限校验，需要system:tenantPackage:list权限
    @SaCheckPermission("system:tenantPackage:list")
    // GET请求映射，路径为/system/tenant/package/selectList
    @GetMapping("/selectList")
    public R<List<SysTenantPackageVo>> selectList() {
        // 调用服务层查询所有租户套餐列表并返回
        return R.ok(tenantPackageService.selectList());
    }

    /**
     * 导出租户套餐列表
     * 将租户套餐数据导出为Excel文件
     */
    // Sa-Token角色校验，需要超级管理员角色
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    // Sa-Token权限校验，需要system:tenantPackage:export权限
    @SaCheckPermission("system:tenantPackage:export")
    // 操作日志注解，记录业务操作，标题为"租户套餐"，类型为导出
    @Log(title = "租户套餐", businessType = BusinessType.EXPORT)
    // POST请求映射，路径为/system/tenant/package/export
    @PostMapping("/export")
    public void export(SysTenantPackageBo bo, HttpServletResponse response) {
        // 调用服务层查询所有租户套餐列表
        List<SysTenantPackageVo> list = tenantPackageService.queryList(bo);
        // 使用Excel工具类导出数据到响应流
        ExcelUtil.exportExcel(list, "租户套餐", SysTenantPackageVo.class, response);
    }

    /**
     * 获取租户套餐详细信息
     * 根据主键ID查询租户套餐详情
     *
     * @param packageId 主键
     */
    // Sa-Token角色校验，需要超级管理员角色
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    // Sa-Token权限校验，需要system:tenantPackage:query权限
    @SaCheckPermission("system:tenantPackage:query")
    // GET请求映射，路径为/system/tenant/package/{packageId}
    @GetMapping("/{packageId}")
    public R<SysTenantPackageVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long packageId) {
        // 调用服务层查询租户套餐详情并返回
        return R.ok(tenantPackageService.queryById(packageId));
    }

    /**
     * 新增租户套餐
     * 添加新的租户套餐信息
     */
    // Sa-Token角色校验，需要超级管理员角色
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    // Sa-Token权限校验，需要system:tenantPackage:add权限
    @SaCheckPermission("system:tenantPackage:add")
    // 操作日志注解，记录业务操作，标题为"租户套餐"，类型为新增
    @Log(title = "租户套餐", businessType = BusinessType.INSERT)
    // 防重复提交注解，防止重复新增
    @RepeatSubmit()
    // POST请求映射，路径为/system/tenant/package
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody SysTenantPackageBo bo) {
        // 校验套餐名称唯一性
        if (!tenantPackageService.checkPackageNameUnique(bo)) {
            return R.fail("新增套餐'" + bo.getPackageName() + "'失败，套餐名称已存在");
        }
        // 调用服务层新增租户套餐，并返回操作结果
        return toAjax(tenantPackageService.insertByBo(bo));
    }

    /**
     * 修改租户套餐
     * 更新租户套餐信息
     */
    // Sa-Token角色校验，需要超级管理员角色
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    // Sa-Token权限校验，需要system:tenantPackage:edit权限
    @SaCheckPermission("system:tenantPackage:edit")
    // 操作日志注解，记录业务操作，标题为"租户套餐"，类型为更新
    @Log(title = "租户套餐", businessType = BusinessType.UPDATE)
    // 防重复提交注解，防止重复修改
    @RepeatSubmit()
    // PUT请求映射，路径为/system/tenant/package
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody SysTenantPackageBo bo) {
        // 校验套餐名称唯一性
        if (!tenantPackageService.checkPackageNameUnique(bo)) {
            return R.fail("修改套餐'" + bo.getPackageName() + "'失败，套餐名称已存在");
        }
        // 调用服务层更新租户套餐，并返回操作结果
        return toAjax(tenantPackageService.updateByBo(bo));
    }

    /**
     * 状态修改
     * 启用或禁用租户套餐
     */
    // Sa-Token角色校验，需要超级管理员角色
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    // Sa-Token权限校验，需要system:tenantPackage:edit权限
    @SaCheckPermission("system:tenantPackage:edit")
    // 操作日志注解，记录业务操作，标题为"租户套餐"，类型为更新
    @Log(title = "租户套餐", businessType = BusinessType.UPDATE)
    // 防重复提交注解，防止重复修改
    @RepeatSubmit()
    // PUT请求映射，路径为/system/tenant/package/changeStatus
    @PutMapping("/changeStatus")
    public R<Void> changeStatus(@RequestBody SysTenantPackageBo bo) {
        // 调用服务层更新租户套餐状态，并返回操作结果
        return toAjax(tenantPackageService.updatePackageStatus(bo));
    }

    /**
     * 删除租户套餐
     * 批量删除租户套餐信息
     *
     * @param packageIds 主键数组
     */
    // Sa-Token角色校验，需要超级管理员角色
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    // Sa-Token权限校验，需要system:tenantPackage:remove权限
    @SaCheckPermission("system:tenantPackage:remove")
    // 操作日志注解，记录业务操作，标题为"租户套餐"，类型为删除
    @Log(title = "租户套餐", businessType = BusinessType.DELETE)
    // DELETE请求映射，路径为/system/tenant/package/{packageIds}，支持多个id用逗号分隔
    @DeleteMapping("/{packageIds}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] packageIds) {
        // 调用服务层批量删除租户套餐，并返回操作结果
        return toAjax(tenantPackageService.deleteWithValidByIds(List.of(packageIds), true));
    }
}
