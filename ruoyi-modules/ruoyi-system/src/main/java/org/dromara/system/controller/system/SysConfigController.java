// 系统参数配置控制器，提供参数配置的CRUD、导出和缓存刷新功能
package org.dromara.system.controller.system;

// Sa-Token权限校验注解，用于接口鉴权
import cn.dev33.satoken.annotation.SaCheckPermission;
// Servlet响应对象，用于文件导出
import jakarta.servlet.http.HttpServletResponse;
// Lombok注解，自动生成构造函数
import lombok.RequiredArgsConstructor;
// 统一响应结果封装类
import org.dromara.common.core.domain.R;
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
// 参数配置业务对象
import org.dromara.system.domain.bo.SysConfigBo;
// 参数配置视图对象
import org.dromara.system.domain.vo.SysConfigVo;
// 参数配置服务接口
import org.dromara.system.service.ISysConfigService;
// Spring校验注解
import org.springframework.validation.annotation.Validated;
// Spring Web绑定注解
import org.springframework.web.bind.annotation.*;

// Java数组工具类
import java.util.Arrays;
// Java集合类
import java.util.List;

/**
 * 系统参数配置控制器
 * 提供参数配置的CRUD、导出和缓存刷新功能
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
// 请求路径映射，所有接口前缀为/system/config
@RequestMapping("/system/config")
public class SysConfigController extends BaseController {

    // 参数配置服务接口，自动注入
    private final ISysConfigService configService;

    /**
     * 获取参数配置列表
     * 分页查询参数配置，支持条件筛选
     */
    // Sa-Token权限校验，需要system:config:list权限
    @SaCheckPermission("system:config:list")
    // GET请求映射，路径为/system/config/list
    @GetMapping("/list")
    public TableDataInfo<SysConfigVo> list(SysConfigBo config, PageQuery pageQuery) {
        // 调用服务层分页查询参数配置列表
        return configService.selectPageConfigList(config, pageQuery);
    }

    /**
     * 导出参数配置列表
     * 将参数配置导出为Excel文件
     */
    // 操作日志注解，记录业务操作，标题为"参数管理"，类型为导出
    @Log(title = "参数管理", businessType = BusinessType.EXPORT)
    // Sa-Token权限校验，需要system:config:export权限
    @SaCheckPermission("system:config:export")
    // POST请求映射，路径为/system/config/export
    @PostMapping("/export")
    public void export(SysConfigBo config, HttpServletResponse response) {
        // 查询所有符合条件的参数配置列表
        List<SysConfigVo> list = configService.selectConfigList(config);
        // 使用Excel工具类导出数据，指定文件名和响应对象
        ExcelUtil.exportExcel(list, "参数数据", SysConfigVo.class, response);
    }

    /**
     * 根据参数编号获取详细信息
     * 根据主键ID查询参数配置详情
     *
     * @param configId 参数ID
     */
    // Sa-Token权限校验，需要system:config:query权限
    @SaCheckPermission("system:config:query")
    // GET请求映射，路径为/system/config/{configId}
    @GetMapping(value = "/{configId}")
    public R<SysConfigVo> getInfo(@PathVariable Long configId) {
        // 调用服务层查询参数配置详情并返回
        return R.ok(configService.selectConfigById(configId));
    }

    /**
     * 根据参数键名查询参数值
     * 公开接口，无需权限校验，用于前端获取配置值
     *
     * @param configKey 参数Key
     */
    // GET请求映射，路径为/system/config/configKey/{configKey}
    @GetMapping(value = "/configKey/{configKey}")
    public R<String> getConfigKey(@PathVariable String configKey) {
        // 调用服务层根据键名查询参数值，返回成功响应
        return R.ok("操作成功", configService.selectConfigByKey(configKey));
    }

    /**
     * 新增参数配置
     * 添加新的参数配置，需要校验键名唯一性
     */
    // Sa-Token权限校验，需要system:config:add权限
    @SaCheckPermission("system:config:add")
    // 操作日志注解，记录业务操作，标题为"参数管理"，类型为新增
    @Log(title = "参数管理", businessType = BusinessType.INSERT)
    // 防重复提交注解，防止重复新增
    @RepeatSubmit()
    // POST请求映射，路径为/system/config
    @PostMapping
    public R<Void> add(@Validated @RequestBody SysConfigBo config) {
        // 校验参数键名是否唯一，如果不唯一返回错误信息
        if (!configService.checkConfigKeyUnique(config)) {
            return R.fail("新增参数'" + config.getConfigName() + "'失败，参数键名已存在");
        }
        // 调用服务层新增参数配置
        configService.insertConfig(config);
        // 返回成功响应
        return R.ok();
    }

    /**
     * 修改参数配置
     * 更新参数配置，需要校验键名唯一性
     */
    // Sa-Token权限校验，需要system:config:edit权限
    @SaCheckPermission("system:config:edit")
    // 操作日志注解，记录业务操作，标题为"参数管理"，类型为更新
    @Log(title = "参数管理", businessType = BusinessType.UPDATE)
    // 防重复提交注解，防止重复修改
    @RepeatSubmit()
    // PUT请求映射，路径为/system/config
    @PutMapping
    public R<Void> edit(@Validated @RequestBody SysConfigBo config) {
        // 校验参数键名是否唯一（排除当前记录），如果不唯一返回错误信息
        if (!configService.checkConfigKeyUnique(config)) {
            return R.fail("修改参数'" + config.getConfigName() + "'失败，参数键名已存在");
        }
        // 调用服务层更新参数配置
        configService.updateConfig(config);
        // 返回成功响应
        return R.ok();
    }

    /**
     * 根据参数键名修改参数配置
     * 通过键名直接更新参数值，无需ID
     */
    // Sa-Token权限校验，需要system:config:edit权限
    @SaCheckPermission("system:config:edit")
    // 操作日志注解，记录业务操作，标题为"参数管理"，类型为更新
    @Log(title = "参数管理", businessType = BusinessType.UPDATE)
    // 防重复提交注解，防止重复修改
    @RepeatSubmit()
    // PUT请求映射，路径为/system/config/updateByKey
    @PutMapping("/updateByKey")
    public R<Void> updateByKey(@RequestBody SysConfigBo config) {
        // 调用服务层更新参数配置（通过键名）
        configService.updateConfig(config);
        // 返回成功响应
        return R.ok();
    }

    /**
     * 删除参数配置
     * 批量删除参数配置
     *
     * @param configIds 参数ID数组
     */
    // Sa-Token权限校验，需要system:config:remove权限
    @SaCheckPermission("system:config:remove")
    // 操作日志注解，记录业务操作，标题为"参数管理"，类型为删除
    @Log(title = "参数管理", businessType = BusinessType.DELETE)
    // DELETE请求映射，路径为/system/config/{configIds}，支持多个id用逗号分隔
    @DeleteMapping("/{configIds}")
    public R<Void> remove(@PathVariable Long[] configIds) {
        // 调用服务层批量删除参数配置
        configService.deleteConfigByIds(Arrays.asList(configIds));
        // 返回成功响应
        return R.ok();
    }

    /**
     * 刷新参数缓存
     * 清空参数缓存，下次查询时重新加载
     */
    // Sa-Token权限校验，需要system:config:remove权限
    @SaCheckPermission("system:config:remove")
    // 操作日志注解，记录业务操作，标题为"参数管理"，类型为清理
    @Log(title = "参数管理", businessType = BusinessType.CLEAN)
    // DELETE请求映射，路径为/system/config/refreshCache
    @DeleteMapping("/refreshCache")
    public R<Void> refreshCache() {
        // 调用服务层重置参数缓存
        configService.resetConfigCache();
        // 返回成功响应
        return R.ok();
    }
}
