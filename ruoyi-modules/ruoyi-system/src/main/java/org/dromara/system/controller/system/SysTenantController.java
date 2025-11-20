// 租户管理控制器，提供租户的CRUD、状态管理、动态切换、套餐同步和字典配置同步功能
package org.dromara.system.controller.system;

// Sa-Token权限校验注解，用于接口鉴权
import cn.dev33.satoken.annotation.SaCheckPermission;
// Sa-Token角色校验注解，用于超级管理员角色校验
import cn.dev33.satoken.annotation.SaCheckRole;
// Redisson分布式锁注解，防止并发问题
import com.baomidou.lock.annotation.Lock4j;
// Servlet响应对象，用于文件导出
import jakarta.servlet.http.HttpServletResponse;
// 非空校验注解，用于字符串参数
import jakarta.validation.constraints.NotBlank;
// 非空校验注解，用于数组参数
import jakarta.validation.constraints.NotEmpty;
// 非空校验注解，用于单个参数
import jakarta.validation.constraints.NotNull;
// Lombok注解，自动生成构造函数
import lombok.RequiredArgsConstructor;
// 租户常量类，包含超级管理员角色KEY
import org.dromara.common.core.constant.TenantConstants;
// 统一响应结果封装类
import org.dromara.common.core.domain.R;
// 新增分组校验
import org.dromara.common.core.validate.AddGroup;
// 编辑分组校验
import org.dromara.common.core.validate.EditGroup;
// API加密注解，对请求响应进行加解密
import org.dromara.common.encrypt.annotation.ApiEncrypt;
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
// 租户助手，提供租户相关工具方法
import org.dromara.common.tenant.helper.TenantHelper;
// 基础控制器，提供通用响应方法
import org.dromara.common.web.core.BaseController;
// 租户业务对象
import org.dromara.system.domain.bo.SysTenantBo;
// 租户视图对象
import org.dromara.system.domain.vo.SysTenantVo;
// 租户服务接口
import org.dromara.system.service.ISysTenantService;
// Spring条件配置注解，当tenant.enable=true时启用
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
// Spring校验注解
import org.springframework.validation.annotation.Validated;
// Spring Web绑定注解
import org.springframework.web.bind.annotation.*;

// Java集合类
import java.util.List;

/**
 * 租户管理控制器
 * 提供租户的CRUD、状态管理、动态切换、套餐同步和字典配置同步功能
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
// 请求路径映射，所有接口前缀为/system/tenant
@RequestMapping("/system/tenant")
// 条件配置注解，当tenant.enable=true时启用该控制器
@ConditionalOnProperty(value = "tenant.enable", havingValue = "true")
public class SysTenantController extends BaseController {

    // 租户服务接口，自动注入
    private final ISysTenantService tenantService;

    /**
     * 查询租户列表
     * 分页查询租户信息，支持条件筛选
     */
    // Sa-Token角色校验，需要超级管理员角色
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    // Sa-Token权限校验，需要system:tenant:list权限
    @SaCheckPermission("system:tenant:list")
    // GET请求映射，路径为/system/tenant/list
    @GetMapping("/list")
    public TableDataInfo<SysTenantVo> list(SysTenantBo bo, PageQuery pageQuery) {
        // 调用服务层分页查询租户列表
        return tenantService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出租户列表
     * 将租户数据导出为Excel文件
     */
    // Sa-Token角色校验，需要超级管理员角色
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    // Sa-Token权限校验，需要system:tenant:export权限
    @SaCheckPermission("system:tenant:export")
    // 操作日志注解，记录业务操作，标题为"租户管理"，类型为导出
    @Log(title = "租户管理", businessType = BusinessType.EXPORT)
    // POST请求映射，路径为/system/tenant/export
    @PostMapping("/export")
    public void export(SysTenantBo bo, HttpServletResponse response) {
        // 调用服务层查询所有租户列表
        List<SysTenantVo> list = tenantService.queryList(bo);
        // 使用Excel工具类导出数据到响应流
        ExcelUtil.exportExcel(list, "租户", SysTenantVo.class, response);
    }

    /**
     * 获取租户详细信息
     * 根据主键ID查询租户详情
     *
     * @param id 主键
     */
    // Sa-Token角色校验，需要超级管理员角色
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    // Sa-Token权限校验，需要system:tenant:query权限
    @SaCheckPermission("system:tenant:query")
    // GET请求映射，路径为/system/tenant/{id}
    @GetMapping("/{id}")
    public R<SysTenantVo> getInfo(@NotNull(message = "主键不能为空")
                                  @PathVariable Long id) {
        // 调用服务层查询租户详情并返回
        return R.ok(tenantService.queryById(id));
    }

    /**
     * 新增租户
     * 添加新的租户信息，使用分布式锁防止并发
     */
    // API加密注解，对请求响应进行加解密
    @ApiEncrypt
    // Sa-Token角色校验，需要超级管理员角色
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    // Sa-Token权限校验，需要system:tenant:add权限
    @SaCheckPermission("system:tenant:add")
    // 操作日志注解，记录业务操作，标题为"租户管理"，类型为新增
    @Log(title = "租户管理", businessType = BusinessType.INSERT)
    // Redisson分布式锁注解，防止并发新增租户
    @Lock4j
    // 防重复提交注解，防止重复新增
    @RepeatSubmit()
    // POST请求映射，路径为/system/tenant
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody SysTenantBo bo) {
        // 校验公司名称唯一性
        if (!tenantService.checkCompanyNameUnique(bo)) {
            return R.fail("新增租户'" + bo.getCompanyName() + "'失败，企业名称已存在");
        }
        // 临时忽略租户隔离，调用服务层新增租户，并返回操作结果
        return toAjax(TenantHelper.ignore(() -> tenantService.insertByBo(bo)));
    }

    /**
     * 修改租户
     * 更新租户信息
     */
    // Sa-Token角色校验，需要超级管理员角色
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    // Sa-Token权限校验，需要system:tenant:edit权限
    @SaCheckPermission("system:tenant:edit")
    // 操作日志注解，记录业务操作，标题为"租户管理"，类型为更新
    @Log(title = "租户管理", businessType = BusinessType.UPDATE)
    // 防重复提交注解，防止重复修改
    @RepeatSubmit()
    // PUT请求映射，路径为/system/tenant
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody SysTenantBo bo) {
        // 校验租户是否允许操作（防止修改内置租户）
        tenantService.checkTenantAllowed(bo.getTenantId());
        // 校验公司名称唯一性
        if (!tenantService.checkCompanyNameUnique(bo)) {
            return R.fail("修改租户'" + bo.getCompanyName() + "'失败，公司名称已存在");
        }
        // 调用服务层更新租户，并返回操作结果
        return toAjax(tenantService.updateByBo(bo));
    }

    /**
     * 状态修改
     * 启用或禁用租户
     */
    // Sa-Token角色校验，需要超级管理员角色
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    // Sa-Token权限校验，需要system:tenant:edit权限
    @SaCheckPermission("system:tenant:edit")
    // 操作日志注解，记录业务操作，标题为"租户管理"，类型为更新
    @Log(title = "租户管理", businessType = BusinessType.UPDATE)
    // 防重复提交注解，防止重复修改
    @RepeatSubmit()
    // PUT请求映射，路径为/system/tenant/changeStatus
    @PutMapping("/changeStatus")
    public R<Void> changeStatus(@RequestBody SysTenantBo bo) {
        // 校验租户是否允许操作（防止修改内置租户）
        tenantService.checkTenantAllowed(bo.getTenantId());
        // 调用服务层更新租户状态，并返回操作结果
        return toAjax(tenantService.updateTenantStatus(bo));
    }

    /**
     * 删除租户
     * 批量删除租户信息
     *
     * @param ids 主键数组
     */
    // Sa-Token角色校验，需要超级管理员角色
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    // Sa-Token权限校验，需要system:tenant:remove权限
    @SaCheckPermission("system:tenant:remove")
    // 操作日志注解，记录业务操作，标题为"租户管理"，类型为删除
    @Log(title = "租户管理", businessType = BusinessType.DELETE)
    // DELETE请求映射，路径为/system/tenant/{ids}，支持多个id用逗号分隔
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        // 调用服务层批量删除租户，并返回操作结果
        return toAjax(tenantService.deleteWithValidByIds(List.of(ids), true));
    }

    /**
     * 动态切换租户
     * 超级管理员动态切换到指定租户，用于租户数据管理
     *
     * @param tenantId 租户ID
     */
    // Sa-Token角色校验，需要超级管理员角色
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    // GET请求映射，路径为/system/tenant/dynamic/{tenantId}
    @GetMapping("/dynamic/{tenantId}")
    public R<Void> dynamicTenant(@NotBlank(message = "租户ID不能为空") @PathVariable String tenantId) {
        // 设置动态租户上下文，并标记为管理员切换
        TenantHelper.setDynamic(tenantId, true);
        return R.ok();
    }

    /**
     * 清除动态租户
     * 清除动态切换的租户上下文，恢复默认租户
     */
    // Sa-Token角色校验，需要超级管理员角色
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    // GET请求映射，路径为/system/tenant/dynamic/clear
    @GetMapping("/dynamic/clear")
    public R<Void> dynamicClear() {
        // 清除动态租户上下文
        TenantHelper.clearDynamic();
        return R.ok();
    }


    /**
     * 同步租户套餐
     * 将租户套餐的菜单权限同步到租户管理员角色，使用分布式锁防止并发
     *
     * @param tenantId  租户id
     * @param packageId 套餐id
     */
    // Sa-Token角色校验，需要超级管理员角色
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    // Sa-Token权限校验，需要system:tenant:edit权限
    @SaCheckPermission("system:tenant:edit")
    // 操作日志注解，记录业务操作，标题为"租户管理"，类型为更新
    @Log(title = "租户管理", businessType = BusinessType.UPDATE)
    // Redisson分布式锁注解，防止并发同步
    @Lock4j
    // GET请求映射，路径为/system/tenant/syncTenantPackage
    @GetMapping("/syncTenantPackage")
    public R<Void> syncTenantPackage(@NotBlank(message = "租户ID不能为空") String tenantId,
                                     @NotNull(message = "套餐ID不能为空") Long packageId) {
        // 临时忽略租户隔离，调用服务层同步租户套餐，并返回操作结果
        return toAjax(TenantHelper.ignore(() -> tenantService.syncTenantPackage(tenantId, packageId)));
    }

    /**
     * 同步租户字典
     * 将平台字典数据同步到所有租户，使用分布式锁防止并发
     */
    // Sa-Token角色校验，需要超级管理员角色
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    // 操作日志注解，记录业务操作，标题为"租户管理"，类型为新增
    @Log(title = "租户管理", businessType = BusinessType.INSERT)
    // Redisson分布式锁注解，防止并发同步
    @Lock4j
    // GET请求映射，路径为/system/tenant/syncTenantDict
    @GetMapping("/syncTenantDict")
    public R<Void> syncTenantDict() {
        // 如果租户模式未启用，返回错误提示
        if (!TenantHelper.isEnable()) {
            return R.fail("当前未开启租户模式");
        }
        // 调用服务层同步租户字典
        tenantService.syncTenantDict();
        return R.ok("同步租户字典成功");
    }

    /**
     * 同步租户参数配置
     * 将平台参数配置同步到所有租户，使用分布式锁防止并发
     */
    // Sa-Token角色校验，需要超级管理员角色
    @SaCheckRole(TenantConstants.SUPER_ADMIN_ROLE_KEY)
    // 操作日志注解，记录业务操作，标题为"租户管理"，类型为新增
    @Log(title = "租户管理", businessType = BusinessType.INSERT)
    // Redisson分布式锁注解，防止并发同步
    @Lock4j
    // GET请求映射，路径为/system/tenant/syncTenantConfig
    @GetMapping("/syncTenantConfig")
    public R<Void> syncTenantConfig() {
        // 如果租户模式未启用，返回错误提示
        if (!TenantHelper.isEnable()) {
            return R.fail("当前未开启租户模式");
        }
        // 调用服务层同步租户参数配置
        tenantService.syncTenantConfig();
        return R.ok("同步租户参数配置成功");
    }

}
