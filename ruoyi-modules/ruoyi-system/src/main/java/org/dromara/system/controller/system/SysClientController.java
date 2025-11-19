// 客户端管理控制器，提供客户端的CRUD、状态管理和导出功能
package org.dromara.system.controller.system;

// Sa-Token权限校验注解，用于接口鉴权
import cn.dev33.satoken.annotation.SaCheckPermission;
// Servlet响应对象，用于文件导出
import jakarta.servlet.http.HttpServletResponse;
// 非空校验注解，用于数组参数
import jakarta.validation.constraints.NotEmpty;
// 非空校验注解，用于单个参数
import jakarta.validation.constraints.NotNull;
// Lombok注解，自动生成构造函数
import lombok.RequiredArgsConstructor;
// 统一响应结果封装类
import org.dromara.common.core.domain.R;
// 新增分组校验
import org.dromara.common.core.validate.AddGroup;
// 编辑分组校验
import org.dromara.common.core.validate.EditGroup;
// Excel导出工具类
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
// 客户端业务对象
import org.dromara.system.domain.bo.SysClientBo;
// 客户端视图对象
import org.dromara.system.domain.vo.SysClientVo;
// 客户端服务接口
import org.dromara.system.service.ISysClientService;
// Spring校验注解
import org.springframework.validation.annotation.Validated;
// Spring Web绑定注解
import org.springframework.web.bind.annotation.*;

// Java集合类
import java.util.List;

/**
 * 客户端管理控制器
 * 提供客户端的CRUD、状态管理和导出功能
 * 继承BaseController获取通用响应方法
 *
 * @author Michelle.Chung
 * @date 2023-06-18
 */
// Spring校验注解，启用方法参数校验
@Validated
// Lombok注解，自动生成包含所有final字段的构造函数
@RequiredArgsConstructor
// REST控制器注解，自动将返回值序列化为JSON
@RestController
// 请求路径映射，所有接口前缀为/system/client
@RequestMapping("/system/client")
public class SysClientController extends BaseController {

    // 客户端服务接口，自动注入
    private final ISysClientService sysClientService;

    /**
     * 查询客户端管理列表
     * 分页查询客户端信息，支持条件筛选
     */
    // Sa-Token权限校验，需要system:client:list权限
    @SaCheckPermission("system:client:list")
    // GET请求映射，路径为/system/client/list
    @GetMapping("/list")
    public TableDataInfo<SysClientVo> list(SysClientBo bo, PageQuery pageQuery) {
        // 调用服务层分页查询客户端列表
        return sysClientService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出客户端管理列表
     * 将客户端信息导出为Excel文件
     */
    // Sa-Token权限校验，需要system:client:export权限
    @SaCheckPermission("system:client:export")
    // 操作日志注解，记录业务操作，标题为"客户端管理"，类型为导出
    @Log(title = "客户端管理", businessType = BusinessType.EXPORT)
    // POST请求映射，路径为/system/client/export
    @PostMapping("/export")
    public void export(SysClientBo bo, HttpServletResponse response) {
        // 查询所有符合条件的客户端列表
        List<SysClientVo> list = sysClientService.queryList(bo);
        // 使用Excel工具类导出数据，指定文件名和响应对象
        ExcelUtil.exportExcel(list, "客户端管理", SysClientVo.class, response);
    }

    /**
     * 获取客户端管理详细信息
     * 根据主键ID查询客户端详情
     *
     * @param id 主键ID
     */
    // Sa-Token权限校验，需要system:client:query权限
    @SaCheckPermission("system:client:query")
    // GET请求映射，路径为/system/client/{id}
    @GetMapping("/{id}")
    public R<SysClientVo> getInfo(@NotNull(message = "主键不能为空")
                                  @PathVariable Long id) {
        // 调用服务层查询客户端详情并返回
        return R.ok(sysClientService.queryById(id));
    }

    /**
     * 新增客户端管理
     * 添加新的客户端配置，需要校验客户端key唯一性
     */
    // Sa-Token权限校验，需要system:client:add权限
    @SaCheckPermission("system:client:add")
    // 操作日志注解，记录业务操作，标题为"客户端管理"，类型为新增
    @Log(title = "客户端管理", businessType = BusinessType.INSERT)
    // 防重复提交注解，防止重复新增
    @RepeatSubmit()
    // POST请求映射，路径为/system/client
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody SysClientBo bo) {
        // 校验客户端key是否唯一，如果不唯一返回错误信息
        if (!sysClientService.checkClickKeyUnique(bo)) {
            return R.fail("新增客户端'" + bo.getClientKey() + "'失败，客户端key已存在");
        }
        // 调用服务层新增客户端，并返回操作结果
        return toAjax(sysClientService.insertByBo(bo));
    }

    /**
     * 修改客户端管理
     * 更新客户端配置，需要校验客户端key唯一性
     */
    // Sa-Token权限校验，需要system:client:edit权限
    @SaCheckPermission("system:client:edit")
    // 操作日志注解，记录业务操作，标题为"客户端管理"，类型为更新
    @Log(title = "客户端管理", businessType = BusinessType.UPDATE)
    // 防重复提交注解，防止重复修改
    @RepeatSubmit()
    // PUT请求映射，路径为/system/client
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody SysClientBo bo) {
        // 校验客户端key是否唯一（排除当前记录），如果不唯一返回错误信息
        if (!sysClientService.checkClickKeyUnique(bo)) {
            return R.fail("修改客户端'" + bo.getClientKey() + "'失败，客户端key已存在");
        }
        // 调用服务层更新客户端，并返回操作结果
        return toAjax(sysClientService.updateByBo(bo));
    }

    /**
     * 状态修改
     * 修改客户端状态（启用/禁用）
     */
    // Sa-Token权限校验，需要system:client:edit权限
    @SaCheckPermission("system:client:edit")
    // 操作日志注解，记录业务操作，标题为"客户端管理"，类型为更新
    @Log(title = "客户端管理", businessType = BusinessType.UPDATE)
    // PUT请求映射，路径为/system/client/changeStatus
    @PutMapping("/changeStatus")
    public R<Void> changeStatus(@RequestBody SysClientBo bo) {
        // 调用服务层更新客户端状态，并返回操作结果
        return toAjax(sysClientService.updateClientStatus(bo.getClientId(), bo.getStatus()));
    }

    /**
     * 删除客户端管理
     * 批量删除客户端配置
     *
     * @param ids 主键ID数组
     */
    // Sa-Token权限校验，需要system:client:remove权限
    @SaCheckPermission("system:client:remove")
    // 操作日志注解，记录业务操作，标题为"客户端管理"，类型为删除
    @Log(title = "客户端管理", businessType = BusinessType.DELETE)
    // DELETE请求映射，路径为/system/client/{ids}，支持多个id用逗号分隔
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        // 调用服务层批量删除客户端，并返回操作结果
        return toAjax(sysClientService.deleteWithValidByIds(List.of(ids), true));
    }
}
