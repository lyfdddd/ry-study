// 字典类型管理控制器，提供字典类型的CRUD、导出、缓存刷新和选择框列表功能
package org.dromara.system.controller.system;

// Sa-Token权限校验注解，用于接口鉴权
import cn.dev33.satoken.annotation.SaCheckPermission;
// Redisson分布式锁注解，防止并发操作
import com.baomidou.lock.annotation.Lock4j;
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
// 字典类型业务对象
import org.dromara.system.domain.bo.SysDictTypeBo;
// 字典类型视图对象
import org.dromara.system.domain.vo.SysDictTypeVo;
// 字典类型服务接口
import org.dromara.system.service.ISysDictTypeService;
// Spring校验注解
import org.springframework.validation.annotation.Validated;
// Spring Web绑定注解
import org.springframework.web.bind.annotation.*;

// Java数组工具类
import java.util.Arrays;
// Java集合类
import java.util.List;

/**
 * 字典类型管理控制器
 * 提供字典类型的CRUD、导出、缓存刷新和选择框列表功能
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
// 请求路径映射，所有接口前缀为/system/dict/type
@RequestMapping("/system/dict/type")
public class SysDictTypeController extends BaseController {

    // 字典类型服务接口，自动注入
    private final ISysDictTypeService dictTypeService;

    /**
     * 查询字典类型列表
     * 分页查询字典类型，支持条件筛选
     */
    // Sa-Token权限校验，需要system:dict:list权限
    @SaCheckPermission("system:dict:list")
    // GET请求映射，路径为/system/dict/type/list
    @GetMapping("/list")
    public TableDataInfo<SysDictTypeVo> list(SysDictTypeBo dictType, PageQuery pageQuery) {
        // 调用服务层分页查询字典类型列表
        return dictTypeService.selectPageDictTypeList(dictType, pageQuery);
    }

    /**
     * 导出字典类型列表
     * 将字典类型导出为Excel文件
     */
    // 操作日志注解，记录业务操作，标题为"字典类型"，类型为导出
    @Log(title = "字典类型", businessType = BusinessType.EXPORT)
    // Sa-Token权限校验，需要system:dict:export权限
    @SaCheckPermission("system:dict:export")
    // POST请求映射，路径为/system/dict/type/export
    @PostMapping("/export")
    public void export(SysDictTypeBo dictType, HttpServletResponse response) {
        // 查询所有符合条件的字典类型列表
        List<SysDictTypeVo> list = dictTypeService.selectDictTypeList(dictType);
        // 使用Excel工具类导出数据，指定文件名和响应对象
        ExcelUtil.exportExcel(list, "字典类型", SysDictTypeVo.class, response);
    }

    /**
     * 查询字典类型详细
     * 根据主键ID查询字典类型详情
     *
     * @param dictId 字典ID
     */
    // Sa-Token权限校验，需要system:dict:query权限
    @SaCheckPermission("system:dict:query")
    // GET请求映射，路径为/system/dict/type/{dictId}
    @GetMapping(value = "/{dictId}")
    public R<SysDictTypeVo> getInfo(@PathVariable Long dictId) {
        // 调用服务层查询字典类型详情并返回
        return R.ok(dictTypeService.selectDictTypeById(dictId));
    }

    /**
     * 新增字典类型
     * 添加新的字典类型，需要校验类型唯一性
     */
    // Sa-Token权限校验，需要system:dict:add权限
    @SaCheckPermission("system:dict:add")
    // 操作日志注解，记录业务操作，标题为"字典类型"，类型为新增
    @Log(title = "字典类型", businessType = BusinessType.INSERT)
    // 防重复提交注解，防止重复新增
    @RepeatSubmit()
    // POST请求映射，路径为/system/dict/type
    @PostMapping
    public R<Void> add(@Validated @RequestBody SysDictTypeBo dict) {
        // 校验字典类型是否唯一，如果不唯一返回错误信息
        if (!dictTypeService.checkDictTypeUnique(dict)) {
            return R.fail("新增字典'" + dict.getDictName() + "'失败，字典类型已存在");
        }
        // 调用服务层新增字典类型
        dictTypeService.insertDictType(dict);
        // 返回成功响应
        return R.ok();
    }

    /**
     * 修改字典类型
     * 更新字典类型，需要校验类型唯一性
     */
    // Sa-Token权限校验，需要system:dict:edit权限
    @SaCheckPermission("system:dict:edit")
    // 操作日志注解，记录业务操作，标题为"字典类型"，类型为更新
    @Log(title = "字典类型", businessType = BusinessType.UPDATE)
    // 防重复提交注解，防止重复修改
    @RepeatSubmit()
    // PUT请求映射，路径为/system/dict/type
    @PutMapping
    public R<Void> edit(@Validated @RequestBody SysDictTypeBo dict) {
        // 校验字典类型是否唯一（排除当前记录），如果不唯一返回错误信息
        if (!dictTypeService.checkDictTypeUnique(dict)) {
            return R.fail("修改字典'" + dict.getDictName() + "'失败，字典类型已存在");
        }
        // 调用服务层更新字典类型
        dictTypeService.updateDictType(dict);
        // 返回成功响应
        return R.ok();
    }

    /**
     * 删除字典类型
     * 批量删除字典类型
     *
     * @param dictIds 字典ID数组
     */
    // Sa-Token权限校验，需要system:dict:remove权限
    @SaCheckPermission("system:dict:remove")
    // 操作日志注解，记录业务操作，标题为"字典类型"，类型为删除
    @Log(title = "字典类型", businessType = BusinessType.DELETE)
    // DELETE请求映射，路径为/system/dict/type/{dictIds}，支持多个id用逗号分隔
    @DeleteMapping("/{dictIds}")
    public R<Void> remove(@PathVariable Long[] dictIds) {
        // 调用服务层批量删除字典类型
        dictTypeService.deleteDictTypeByIds(Arrays.asList(dictIds));
        // 返回成功响应
        return R.ok();
    }

    /**
     * 刷新字典缓存
     * 清空字典缓存，下次查询时重新加载
     */
    // Sa-Token权限校验，需要system:dict:remove权限
    @SaCheckPermission("system:dict:remove")
    // 操作日志注解，记录业务操作，标题为"字典类型"，类型为清理
    @Log(title = "字典类型", businessType = BusinessType.CLEAN)
    // Redisson分布式锁注解，防止并发刷新操作
    @Lock4j
    // DELETE请求映射，路径为/system/dict/type/refreshCache
    @DeleteMapping("/refreshCache")
    public R<Void> refreshCache() {
        // 调用服务层重置字典缓存
        dictTypeService.resetDictCache();
        // 返回成功响应
        return R.ok();
    }

    /**
     * 获取字典选择框列表
     * 公开接口，无需权限校验，用于前端选择框
     */
    // GET请求映射，路径为/system/dict/type/optionselect
    @GetMapping("/optionselect")
    public R<List<SysDictTypeVo>> optionselect() {
        // 调用服务层查询所有字典类型
        List<SysDictTypeVo> dictTypes = dictTypeService.selectDictTypeAll();
        // 返回字典类型列表
        return R.ok(dictTypes);
    }
}
