package org.dromara.generator.controller;

// 导入Sa-Token权限校验注解，用于接口级别的权限控制
// @SaCheckPermission注解会在方法执行前检查用户是否具有指定权限
import cn.dev33.satoken.annotation.SaCheckPermission;
// 导入Hutool的类型转换工具类，用于将字符串转换为数组
// Convert.toStrArray方法将逗号分隔的字符串转换为String数组
import cn.hutool.core.convert.Convert;
// 导入Hutool的IO工具类，用于文件下载流操作
// IoUtil.write方法将字节数组写入输出流
import cn.hutool.core.io.IoUtil;
// 导入分布式锁注解，防止同步数据库等操作并发执行
// @Lock4j注解基于Redis实现分布式锁，确保方法同一时间只能被一个线程执行
import com.baomidou.lock.annotation.Lock4j;
// 导入Jakarta的HttpServletResponse，用于文件下载
import jakarta.servlet.http.HttpServletResponse;
// 导入Lombok的RequiredArgsConstructor注解，自动生成构造函数
// 自动为所有final字段生成构造函数，实现依赖注入
import lombok.RequiredArgsConstructor;
// 导入统一响应结果类
// R.ok()表示成功响应，R.fail()表示失败响应
import org.dromara.common.core.domain.R;
// 导入防重复提交注解
// @RepeatSubmit注解基于Redis实现幂等性，防止表单重复提交
import org.dromara.common.idempotent.annotation.RepeatSubmit;
// 导入操作日志注解
// @Log注解会记录操作日志到数据库，包括操作人、操作时间、请求参数等
import org.dromara.common.log.annotation.Log;
// 导入业务类型枚举
// BusinessType枚举定义了各种业务操作类型：INSERT、UPDATE、DELETE、IMPORT、EXPORT等
import org.dromara.common.log.enums.BusinessType;
// 导入分页查询对象
// PageQuery封装了分页参数：pageNum（页码）、pageSize（每页条数）
import org.dromara.common.mybatis.core.page.PageQuery;
// 导入表格数据信息封装类
// TableDataInfo封装了分页结果：rows（数据列表）、total（总记录数）
import org.dromara.common.mybatis.core.page.TableDataInfo;
// 导入数据库助手类，用于获取数据源列表
// DataBaseHelper.getDataSourceNameList()获取所有配置的数据源名称
import org.dromara.common.mybatis.helper.DataBaseHelper;
// 导入基础控制器类
// BaseController提供了通用的响应处理方法，如genCode下载方法
import org.dromara.common.web.core.BaseController;
// 导入代码生成业务表实体类
// GenTable存储代码生成配置信息：表名、类名、包路径、模块名等
import org.dromara.generator.domain.GenTable;
// 导入代码生成业务表字段实体类
// GenTableColumn存储字段配置信息：字段名、Java类型、HTML控件类型、是否必填等
import org.dromara.generator.domain.GenTableColumn;
// 导入代码生成服务接口
// IGenTableService提供代码生成的核心业务逻辑
import org.dromara.generator.service.IGenTableService;
// 导入Spring的参数校验注解
// @Validated注解启用方法参数校验，@RequestBody表示接收JSON请求体
import org.springframework.validation.annotation.Validated;
// 导入Spring Web的RESTful注解
// @RestController组合注解（@Controller + @ResponseBody）
// @RequestMapping定义请求路径前缀
// @GetMapping、@PostMapping、@PutMapping、@DeleteMapping定义HTTP方法
import org.springframework.web.bind.annotation.*;

// 导入IO异常类
import java.io.IOException;
// 导入HashMap类
import java.util.HashMap;
// 导入List接口
import java.util.List;
// 导入Map接口
import java.util.Map;

/**
 * 代码生成 操作处理
 * 提供代码生成的RESTful接口，包括查询、导入、编辑、删除、预览、生成代码等功能
 * 继承BaseController获得通用响应处理方法
 * 使用@Validated启用参数校验
 * 使用@RequiredArgsConstructor实现构造函数注入
 *
 * @author Lion Li
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/tool/gen")
public class GenController extends BaseController {

    /**
     * 代码生成服务接口，通过构造函数注入
     * 使用final修饰，确保线程安全和不可变性
     */
    private final IGenTableService genTableService;

    /**
     * 查询代码生成列表
     * 分页查询已导入的代码生成配置列表
     * 需要tool:gen:list权限
     *
     * @param genTable   查询条件（包含dataName、tableName、tableComment等）
     * @param pageQuery  分页参数（pageNum、pageSize）
     * @return 表格数据信息（TableDataInfo对象，包含rows和total）
     */
    // 使用@SaCheckPermission注解进行权限校验，只有具有tool:gen:list权限的用户才能访问
    @SaCheckPermission("tool:gen:list")
    // 使用@GetMapping注解，映射GET请求，路径为/tool/gen/list
    @GetMapping("/list")
    public TableDataInfo<GenTable> genList(GenTable genTable, PageQuery pageQuery) {
        // 调用服务层方法查询分页数据
        // 将查询条件和分页参数传递给服务层
        return genTableService.selectPageGenTableList(genTable, pageQuery);
    }

    /**
     * 修改代码生成业务
     * 根据表ID查询详细信息，用于编辑页面数据回显
     * 需要tool:gen:query权限
     *
     * @param tableId 表ID（路径变量）
     * @return 包含表信息、字段列表和所有表的Map
     */
    // 使用@RepeatSubmit注解防止重复提交，基于Redis实现幂等性
    @RepeatSubmit()
    // 使用@SaCheckPermission注解进行权限校验
    @SaCheckPermission("tool:gen:query")
    // 使用@GetMapping注解，映射GET请求，路径为/tool/gen/{tableId}
    @GetMapping(value = "/{tableId}")
    public R<Map<String, Object>> getInfo(@PathVariable Long tableId) {
        // 查询主表信息：调用服务层根据ID查询GenTable对象
        GenTable table = genTableService.selectGenTableById(tableId);
        // 查询所有表列表：用于前端下拉选择（主子表关联时使用）
        List<GenTable> tables = genTableService.selectGenTableAll();
        // 查询当前表的字段列表：调用服务层查询关联的字段配置
        List<GenTableColumn> list = genTableService.selectGenTableColumnListByTableId(tableId);
        // 构建返回的Map数据：使用HashMap存储三个对象
        Map<String, Object> map = new HashMap<>(3);
        // info：表基本信息
        map.put("info", table);
        // rows：字段列表
        map.put("rows", list);
        // tables：所有表列表（用于主子表选择）
        map.put("tables", tables);
        // 返回成功响应，包含Map数据
        return R.ok(map);
    }

    /**
     * 查询数据库列表
     * 从数据库元数据中查询所有表，支持分页和搜索
     * 需要tool:gen:list权限
     *
     * @param genTable   查询条件（表名、表描述）
     * @param pageQuery  分页参数
     * @return 表格数据信息
     */
    // 使用@SaCheckPermission注解进行权限校验
    @SaCheckPermission("tool:gen:list")
    // 使用@GetMapping注解，映射GET请求，路径为/tool/gen/db/list
    @GetMapping("/db/list")
    public TableDataInfo<GenTable> dataList(GenTable genTable, PageQuery pageQuery) {
        // 调用服务层方法查询数据库表列表
        // 支持按表名和表注释模糊查询
        return genTableService.selectPageDbTableList(genTable, pageQuery);
    }

    /**
     * 查询数据表字段列表
     * 根据表ID查询字段列表
     * 需要tool:gen:list权限
     *
     * @param tableId 表ID（路径变量）
     * @return 字段列表（TableDataInfo封装）
     */
    // 使用@SaCheckPermission注解进行权限校验
    @SaCheckPermission("tool:gen:list")
    // 使用@GetMapping注解，映射GET请求，路径为/tool/gen/column/{tableId}
    @GetMapping(value = "/column/{tableId}")
    public TableDataInfo<GenTableColumn> columnList(@PathVariable("tableId") Long tableId) {
        // 调用服务层方法查询字段列表
        List<GenTableColumn> list = genTableService.selectGenTableColumnListByTableId(tableId);
        // 构建TableDataInfo响应（不分页，返回所有字段）
        return TableDataInfo.build(list);
    }

    /**
     * 导入表结构（保存）
     * 将数据库表导入到代码生成配置中，保存表信息和字段信息
     * 需要tool:gen:import权限
     *
     * @param tables    表名串（多个表名用逗号分隔）
     * @param dataName  数据源名称
     * @return 成功响应
     */
    // 使用@SaCheckPermission注解进行权限校验
    @SaCheckPermission("tool:gen:import")
    // 使用@Log注解记录操作日志，记录到数据库
    @Log(title = "代码生成", businessType = BusinessType.IMPORT)
    // 使用@RepeatSubmit注解防止重复提交
    @RepeatSubmit()
    // 使用@PostMapping注解，映射POST请求，路径为/tool/gen/importTable
    @PostMapping("/importTable")
    public R<Void> importTableSave(String tables, String dataName) {
        // 将表名字符串转换为数组：使用Hutool的Convert.toStrArray方法
        // 例如："sys_user,sys_config" -> ["sys_user", "sys_config"]
        String[] tableNames = Convert.toStrArray(tables);
        // 查询表信息：调用服务层根据表名数组和数据源名称查询表元数据
        List<GenTable> tableList = genTableService.selectDbTableListByNames(tableNames, dataName);
        // 导入表结构：调用服务层保存表信息和字段信息到数据库
        genTableService.importGenTable(tableList, dataName);
        // 返回成功响应
        return R.ok();
    }

    /**
     * 修改保存代码生成业务
     * 保存编辑后的代码生成配置
     * 需要tool:gen:edit权限
     *
     * @param genTable 代码生成配置（请求体JSON）
     * @return 成功响应
     */
    // 使用@SaCheckPermission注解进行权限校验
    @SaCheckPermission("tool:gen:edit")
    // 使用@Log注解记录操作日志
    @Log(title = "代码生成", businessType = BusinessType.UPDATE)
    // 使用@RepeatSubmit注解防止重复提交
    @RepeatSubmit()
    // 使用@PutMapping注解，映射PUT请求，路径为/tool/gen
    @PutMapping
    public R<Void> editSave(@Validated @RequestBody GenTable genTable) {
        // 校验编辑参数：检查树表配置是否完整
        genTableService.validateEdit(genTable);
        // 更新代码生成配置：保存表信息和字段信息
        genTableService.updateGenTable(genTable);
        // 返回成功响应
        return R.ok();
    }

    /**
     * 删除代码生成
     * 删除代码生成配置及关联的字段信息
     * 需要tool:gen:remove权限
     *
     * @param tableIds 表ID串（多个ID用逗号分隔，路径变量）
     * @return 成功响应
     */
    // 使用@SaCheckPermission注解进行权限校验
    @SaCheckPermission("tool:gen:remove")
    // 使用@Log注解记录操作日志
    @Log(title = "代码生成", businessType = BusinessType.DELETE)
    // 使用@DeleteMapping注解，映射DELETE请求，路径为/tool/gen/{tableIds}
    @DeleteMapping("/{tableIds}")
    public R<Void> remove(@PathVariable Long[] tableIds) {
        // 调用服务层删除代码生成配置及关联字段
        genTableService.deleteGenTableByIds(tableIds);
        // 返回成功响应
        return R.ok();
    }

    /**
     * 预览代码
     * 根据表ID预览生成的代码内容
     * 需要tool:gen:preview权限
     *
     * @param tableId 表ID（路径变量）
     * @return 包含所有模板生成内容的Map
     */
    // 使用@SaCheckPermission注解进行权限校验
    @SaCheckPermission("tool:gen:preview")
    // 使用@GetMapping注解，映射GET请求，路径为/tool/gen/preview/{tableId}
    @GetMapping("/preview/{tableId}")
    public R<Map<String, String>> preview(@PathVariable("tableId") Long tableId) throws IOException {
        // 调用服务层预览代码：渲染所有模板并返回结果Map
        Map<String, String> dataMap = genTableService.previewCode(tableId);
        // 返回成功响应，包含预览数据
        return R.ok(dataMap);
    }

    /**
     * 生成代码（下载方式）
     * 生成代码并打包为zip文件供下载
     * 需要tool:gen:code权限
     *
     * @param response 响应对象（用于文件下载）
     * @param tableId  表ID（路径变量）
     */
    // 使用@SaCheckPermission注解进行权限校验
    @SaCheckPermission("tool:gen:code")
    // 使用@Log注解记录操作日志
    @Log(title = "代码生成", businessType = BusinessType.GENCODE)
    // 使用@GetMapping注解，映射GET请求，路径为/tool/gen/download/{tableId}
    @GetMapping("/download/{tableId}")
    public void download(HttpServletResponse response, @PathVariable("tableId") Long tableId) throws IOException {
        // 生成代码字节数组：调用服务层生成ZIP文件并返回字节数组
        byte[] data = genTableService.downloadCode(tableId);
        // 设置响应头并写入数据：调用父类BaseController的genCode方法
        genCode(response, data);
    }

    /**
     * 生成代码（自定义路径）
     * 生成代码并保存到项目指定路径
     * 需要tool:gen:code权限
     *
     * @param tableId 表ID（路径变量）
     * @return 成功响应
     */
    // 使用@SaCheckPermission注解进行权限校验
    @SaCheckPermission("tool:gen:code")
    // 使用@Log注解记录操作日志
    @Log(title = "代码生成", businessType = BusinessType.GENCODE)
    // 使用@GetMapping注解，映射GET请求，路径为/tool/gen/genCode/{tableId}
    @GetMapping("/genCode/{tableId}")
    public R<Void> genCode(@PathVariable("tableId") Long tableId) {
        // 调用服务层生成代码并保存到指定路径
        genTableService.generatorCode(tableId);
        // 返回成功响应
        return R.ok();
    }

    /**
     * 同步数据库
     * 将数据库表结构的变更同步到代码生成配置中
     * 需要tool:gen:edit权限
     * 使用@Lock4j分布式锁防止并发同步
     *
     * @param tableId 表ID（路径变量）
     * @return 成功响应
     */
    // 使用@SaCheckPermission注解进行权限校验
    @SaCheckPermission("tool:gen:edit")
    // 使用@Log注解记录操作日志
    @Log(title = "代码生成", businessType = BusinessType.UPDATE)
    // 使用@Lock4j注解添加分布式锁，防止并发同步数据库
    // 锁的key默认为方法参数，超时时间默认为30秒
    @Lock4j
    // 使用@GetMapping注解，映射GET请求，路径为/tool/gen/synchDb/{tableId}
    @GetMapping("/synchDb/{tableId}")
    public R<Void> synchDb(@PathVariable("tableId") Long tableId) {
        // 调用服务层同步数据库：将数据库表结构变更同步到配置中
        genTableService.synchDb(tableId);
        // 返回成功响应
        return R.ok();
    }

    /**
     * 批量生成代码
     * 批量生成多个表的代码并打包下载
     * 需要tool:gen:code权限
     *
     * @param response    响应对象（用于文件下载）
     * @param tableIdStr  表ID串（多个ID用逗号分隔，请求参数）
     */
    // 使用@SaCheckPermission注解进行权限校验
    @SaCheckPermission("tool:gen:code")
    // 使用@Log注解记录操作日志
    @Log(title = "代码生成", businessType = BusinessType.GENCODE)
    // 使用@GetMapping注解，映射GET请求，路径为/tool/gen/batchGenCode
    @GetMapping("/batchGenCode")
    public void batchGenCode(HttpServletResponse response, String tableIdStr) throws IOException {
        // 将ID字符串转换为数组：使用Hutool的Convert.toStrArray方法
        // 例如："1,2,3" -> ["1", "2", "3"]
        String[] tableIds = Convert.toStrArray(tableIdStr);
        // 批量生成代码：调用服务层生成多个表的ZIP文件
        byte[] data = genTableService.downloadCode(tableIds);
        // 设置响应头并写入数据：调用父类BaseController的genCode方法
        genCode(response, data);
    }

    /**
     * 生成zip文件
     * 设置文件下载响应头，将字节数组写入响应流
     * 私有方法，供download和batchGenCode方法调用
     *
     * @param response 响应对象
     * @param data     字节数组数据（ZIP文件内容）
     */
    private void genCode(HttpServletResponse response, byte[] data) throws IOException {
        // 重置响应：清除之前的响应状态
        response.reset();
        // 设置跨域访问允许：允许所有域名访问（*表示所有域名）
        response.addHeader("Access-Control-Allow-Origin", "*");
        // 暴露Content-Disposition头，让前端可以获取文件名
        response.addHeader("Access-Control-Expose-Headers", "Content-Disposition");
        // 设置下载文件名：固定为ruoyi.zip
        response.setHeader("Content-Disposition", "attachment; filename=\"ruoyi.zip\"");
        // 设置内容长度：让浏览器显示下载进度
        response.addHeader("Content-Length", "" + data.length);
        // 设置内容类型为二进制流：application/octet-stream表示二进制数据
        response.setContentType("application/octet-stream; charset=UTF-8");
        // 将字节数组写入响应输出流：使用Hutool的IoUtil.write方法
        IoUtil.write(response.getOutputStream(), false, data);
    }

    /**
     * 查询数据源名称列表
     * 获取系统中配置的所有数据源名称
     * 需要tool:gen:list权限
     *
     * @return 数据源名称列表
     */
    // 使用@SaCheckPermission注解进行权限校验
    @SaCheckPermission("tool:gen:list")
    // 使用@GetMapping注解，映射GET请求，路径为/tool/gen/getDataNames
    @GetMapping(value = "/getDataNames")
    public R<Object> getCurrentDataSourceNameList(){
        // 调用DataBaseHelper获取所有数据源名称列表
        // 返回R.ok()包装的数据
        return R.ok(DataBaseHelper.getDataSourceNameList());
    }
}
