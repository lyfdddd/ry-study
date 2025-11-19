// 字典数据管理控制器，提供字典数据的CRUD、导出和按类型查询功能
package org.dromara.system.controller.system;

// Sa-Token权限校验注解，用于接口鉴权
import cn.dev33.satoken.annotation.SaCheckPermission;
// Hutool对象工具类
import cn.hutool.core.util.ObjectUtil;
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
// 字典数据业务对象
import org.dromara.system.domain.bo.SysDictDataBo;
// 字典数据视图对象
import org.dromara.system.domain.vo.SysDictDataVo;
// 字典数据服务接口
import org.dromara.system.service.ISysDictDataService;
// 字典类型服务接口
import org.dromara.system.service.ISysDictTypeService;
// Spring校验注解
import org.springframework.validation.annotation.Validated;
// Spring Web绑定注解
import org.springframework.web.bind.annotation.*;

// Java集合类
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 字典数据管理控制器
 * 提供字典数据的CRUD、导出和按类型查询功能
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
// 请求路径映射，所有接口前缀为/system/dict/data
@RequestMapping("/system/dict/data")
public class SysDictDataController extends BaseController {

    // 字典数据服务接口，自动注入
    private final ISysDictDataService dictDataService;
    // 字典类型服务接口，自动注入，用于按类型查询字典数据
    private final ISysDictTypeService dictTypeService;

    /**
     * 查询字典数据列表
     * 分页查询字典数据，支持条件筛选
     */
    // Sa-Token权限校验，需要system:dict:list权限
    @SaCheckPermission("system:dict:list")
    // GET请求映射，路径为/system/dict/data/list
    @GetMapping("/list")
    public TableDataInfo<SysDictDataVo> list(SysDictDataBo dictData, PageQuery pageQuery) {
        // 调用服务层分页查询字典数据列表
        return dictDataService.selectPageDictDataList(dictData, pageQuery);
    }

    /**
     * 导出字典数据列表
     * 将字典数据导出为Excel文件
     */
    // 操作日志注解，记录业务操作，标题为"字典数据"，类型为导出
    @Log(title = "字典数据", businessType = BusinessType.EXPORT)
    // Sa-Token权限校验，需要system:dict:export权限
    @SaCheckPermission("system:dict:export")
    // POST请求映射，路径为/system/dict/data/export
    @PostMapping("/export")
    public void export(SysDictDataBo dictData, HttpServletResponse response) {
        // 查询所有符合条件的字典数据列表
        List<SysDictDataVo> list = dictDataService.selectDictDataList(dictData);
        // 使用Excel工具类导出数据，指定文件名和响应对象
        ExcelUtil.exportExcel(list, "字典数据", SysDictDataVo.class, response);
    }

    /**
     * 查询字典数据详细
     * 根据主键ID查询字典数据详情
     *
     * @param dictCode 字典code
     */
    // Sa-Token权限校验，需要system:dict:query权限
    @SaCheckPermission("system:dict:query")
    // GET请求映射，路径为/system/dict/data/{dictCode}
    @GetMapping(value = "/{dictCode}")
    public R<SysDictDataVo> getInfo(@PathVariable Long dictCode) {
        // 调用服务层查询字典数据详情并返回
        return R.ok(dictDataService.selectDictDataById(dictCode));
    }

    /**
     * 根据字典类型查询字典数据信息
     * 公开接口，无需权限校验，用于前端获取字典数据
     *
     * @param dictType 字典类型
     */
    // GET请求映射，路径为/system/dict/data/type/{dictType}
    @GetMapping(value = "/type/{dictType}")
    public R<List<SysDictDataVo>> dictType(@PathVariable String dictType) {
        // 调用字典类型服务层根据类型查询字典数据
        List<SysDictDataVo> data = dictTypeService.selectDictDataByType(dictType);
        // 如果查询结果为null，返回空列表，防止前端空指针
        if (ObjectUtil.isNull(data)) {
            data = new ArrayList<>();
        }
        // 返回字典数据列表
        return R.ok(data);
    }

    /**
     * 新增字典数据
     * 添加新的字典数据，需要校验键值唯一性
     */
    // Sa-Token权限校验，需要system:dict:add权限
    @SaCheckPermission("system:dict:add")
    // 操作日志注解，记录业务操作，标题为"字典数据"，类型为新增
    @Log(title = "字典数据", businessType = BusinessType.INSERT)
    // 防重复提交注解，防止重复新增
    @RepeatSubmit()
    // POST请求映射，路径为/system/dict/data
    @PostMapping
    public R<Void> add(@Validated @RequestBody SysDictDataBo dict) {
        // 校验字典键值是否唯一，如果不唯一返回错误信息
        if (!dictDataService.checkDictDataUnique(dict)) {
            return R.fail("新增字典数据'" + dict.getDictValue() + "'失败，字典键值已存在");
        }
        // 调用服务层新增字典数据
        dictDataService.insertDictData(dict);
        // 返回成功响应
        return R.ok();
    }

    /**
     * 修改保存字典数据
     * 更新字典数据，需要校验键值唯一性
     */
    // Sa-Token权限校验，需要system:dict:edit权限
    @SaCheckPermission("system:dict:edit")
    // 操作日志注解，记录业务操作，标题为"字典数据"，类型为更新
    @Log(title = "字典数据", businessType = BusinessType.UPDATE)
    // 防重复提交注解，防止重复修改
    @RepeatSubmit()
    // PUT请求映射，路径为/system/dict/data
    @PutMapping
    public R<Void> edit(@Validated @RequestBody SysDictDataBo dict) {
        // 校验字典键值是否唯一（排除当前记录），如果不唯一返回错误信息
        if (!dictDataService.checkDictDataUnique(dict)) {
            return R.fail("修改字典数据'" + dict.getDictValue() + "'失败，字典键值已存在");
        }
        // 调用服务层更新字典数据
        dictDataService.updateDictData(dict);
        // 返回成功响应
        return R.ok();
    }

    /**
     * 删除字典数据
     * 批量删除字典数据
     *
     * @param dictCodes 字典code数组
     */
    // Sa-Token权限校验，需要system:dict:remove权限
    @SaCheckPermission("system:dict:remove")
    // 操作日志注解，记录业务操作，标题为"字典类型"，类型为删除
    @Log(title = "字典类型", businessType = BusinessType.DELETE)
    // DELETE请求映射，路径为/system/dict/data/{dictCodes}，支持多个id用逗号分隔
    @DeleteMapping("/{dictCodes}")
    public R<Void> remove(@PathVariable Long[] dictCodes) {
        // 调用服务层批量删除字典数据
        dictDataService.deleteDictDataByIds(Arrays.asList(dictCodes));
        // 返回成功响应
        return R.ok();
    }
}
