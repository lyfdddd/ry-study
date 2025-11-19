// OSS对象存储配置控制器，提供配置的CRUD和状态管理功能
package org.dromara.system.controller.system;

// Sa-Token权限校验注解，用于接口鉴权
import cn.dev33.satoken.annotation.SaCheckPermission;
// 统一响应结果封装类
import org.dromara.common.core.domain.R;
// 新增分组校验
import org.dromara.common.core.validate.AddGroup;
// 编辑分组校验
import org.dromara.common.core.validate.EditGroup;
// 查询分组校验
import org.dromara.common.core.validate.QueryGroup;
// 基础控制器，提供通用响应方法
import org.dromara.common.web.core.BaseController;
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
// OSS配置业务对象
import org.dromara.system.domain.bo.SysOssConfigBo;
// OSS配置视图对象
import org.dromara.system.domain.vo.SysOssConfigVo;
// OSS配置服务接口
import org.dromara.system.service.ISysOssConfigService;
// 非空校验注解，用于数组参数
import jakarta.validation.constraints.NotEmpty;
// 非空校验注解，用于单个参数
import jakarta.validation.constraints.NotNull;
// Lombok注解，自动生成构造函数
import lombok.RequiredArgsConstructor;
// Spring校验注解
import org.springframework.validation.annotation.Validated;
// Spring Web绑定注解
import org.springframework.web.bind.annotation.*;

// Java集合类
import java.util.List;

/**
 * OSS对象存储配置控制器
 * 提供配置的CRUD和状态管理功能
 * 继承BaseController获取通用响应方法
 *
 * @author Lion Li
 * @author 孤舟烟雨
 * @date 2021-08-13
 */
// Spring校验注解，启用方法参数校验
@Validated
// Lombok注解，自动生成包含所有final字段的构造函数
@RequiredArgsConstructor
// REST控制器注解，自动将返回值序列化为JSON
@RestController
// 请求路径映射，所有接口前缀为/resource/oss/config
@RequestMapping("/resource/oss/config")
public class SysOssConfigController extends BaseController {

    // OSS配置服务接口，自动注入
    private final ISysOssConfigService ossConfigService;

    /**
     * 查询对象存储配置列表
     * 分页查询OSS配置，支持条件筛选
     */
    // Sa-Token权限校验，需要system:ossConfig:list权限
    @SaCheckPermission("system:ossConfig:list")
    // GET请求映射，路径为/resource/oss/config/list
    @GetMapping("/list")
    public TableDataInfo<SysOssConfigVo> list(@Validated(QueryGroup.class) SysOssConfigBo bo, PageQuery pageQuery) {
        // 调用服务层分页查询OSS配置列表
        return ossConfigService.queryPageList(bo, pageQuery);
    }

    /**
     * 获取对象存储配置详细信息
     * 根据主键ID查询OSS配置详情
     *
     * @param ossConfigId OSS配置ID
     */
    // Sa-Token权限校验，需要system:ossConfig:list权限
    @SaCheckPermission("system:ossConfig:list")
    // GET请求映射，路径为/resource/oss/config/{ossConfigId}
    @GetMapping("/{ossConfigId}")
    public R<SysOssConfigVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long ossConfigId) {
        // 调用服务层查询OSS配置详情并返回
        return R.ok(ossConfigService.queryById(ossConfigId));
    }

    /**
     * 新增对象存储配置
     * 添加新的OSS配置
     */
    // Sa-Token权限校验，需要system:ossConfig:add权限
    @SaCheckPermission("system:ossConfig:add")
    // 操作日志注解，记录业务操作，标题为"对象存储配置"，类型为新增
    @Log(title = "对象存储配置", businessType = BusinessType.INSERT)
    // 防重复提交注解，防止重复新增
    @RepeatSubmit()
    // POST请求映射，路径为/resource/oss/config
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody SysOssConfigBo bo) {
        // 调用服务层新增OSS配置，并返回操作结果
        return toAjax(ossConfigService.insertByBo(bo));
    }

    /**
     * 修改对象存储配置
     * 更新OSS配置信息
     */
    // Sa-Token权限校验，需要system:ossConfig:edit权限
    @SaCheckPermission("system:ossConfig:edit")
    // 操作日志注解，记录业务操作，标题为"对象存储配置"，类型为更新
    @Log(title = "对象存储配置", businessType = BusinessType.UPDATE)
    // 防重复提交注解，防止重复修改
    @RepeatSubmit()
    // PUT请求映射，路径为/resource/oss/config
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody SysOssConfigBo bo) {
        // 调用服务层更新OSS配置，并返回操作结果
        return toAjax(ossConfigService.updateByBo(bo));
    }

    /**
     * 删除对象存储配置
     * 批量删除OSS配置
     *
     * @param ossConfigIds OSS配置ID数组
     */
    // Sa-Token权限校验，需要system:ossConfig:remove权限
    @SaCheckPermission("system:ossConfig:remove")
    // 操作日志注解，记录业务操作，标题为"对象存储配置"，类型为删除
    @Log(title = "对象存储配置", businessType = BusinessType.DELETE)
    // DELETE请求映射，路径为/resource/oss/config/{ossConfigIds}，支持多个id用逗号分隔
    @DeleteMapping("/{ossConfigIds}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ossConfigIds) {
        // 调用服务层批量删除OSS配置，并返回操作结果
        return toAjax(ossConfigService.deleteWithValidByIds(List.of(ossConfigIds), true));
    }

    /**
     * 状态修改
     * 修改OSS配置状态（启用/禁用）
     */
    // Sa-Token权限校验，需要system:ossConfig:edit权限
    @SaCheckPermission("system:ossConfig:edit")
    // 操作日志注解，记录业务操作，标题为"对象存储状态修改"，类型为更新
    @Log(title = "对象存储状态修改", businessType = BusinessType.UPDATE)
    // 防重复提交注解，防止重复修改
    @RepeatSubmit()
    // PUT请求映射，路径为/resource/oss/config/changeStatus
    @PutMapping("/changeStatus")
    public R<Void> changeStatus(@RequestBody SysOssConfigBo bo) {
        // 调用服务层更新OSS配置状态，并返回操作结果
        return toAjax(ossConfigService.updateOssConfigStatus(bo));
    }
}
