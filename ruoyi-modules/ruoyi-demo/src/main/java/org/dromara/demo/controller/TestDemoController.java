// 测试单表Controller层
// 提供测试单表的增删改查、导入导出等RESTful API接口
// 继承BaseController获取通用响应封装能力
package org.dromara.demo.controller;

// Sa-Token权限校验注解，用于接口级别的权限控制
// @SaCheckPermission会检查当前用户是否拥有指定权限
import cn.dev33.satoken.annotation.SaCheckPermission;
// 统一响应结果封装类，提供标准化的API响应格式
// R.ok()表示成功，R.fail()表示失败
import org.dromara.common.core.domain.R;
// MapStruct对象转换工具类，用于实体类之间的转换
// 避免手动编写大量的setter/getter转换代码
import org.dromara.common.core.utils.MapstructUtils;
// 校验工具类，用于手动触发JSR 380校验
// 对标@Validated注解的校验功能
import org.dromara.common.core.utils.ValidatorUtils;
// 新增分组校验接口，用于区分新增场景的校验规则
import org.dromara.common.core.validate.AddGroup;
// 编辑分组校验接口，用于区分编辑场景的校验规则
import org.dromara.common.core.validate.EditGroup;
// 查询分组校验接口，用于区分查询场景的校验规则
import org.dromara.common.core.validate.QueryGroup;
// 基础Controller类，提供通用的响应封装方法如toAjax()
import org.dromara.common.web.core.BaseController;
// 防重复提交注解，基于Redis实现幂等性控制
// interval指定时间间隔，timeUnit指定时间单位
import org.dromara.common.idempotent.annotation.RepeatSubmit;
// MyBatis-Plus分页查询对象，封装分页参数（页码、每页条数）
import org.dromara.common.mybatis.core.page.PageQuery;
// 表格数据信息封装类，包含total、rows等分页信息
import org.dromara.common.mybatis.core.page.TableDataInfo;
// Excel导入结果封装类，包含成功/失败数据、分析信息等
import org.dromara.common.excel.core.ExcelResult;
// Excel工具类，提供导入导出功能
import org.dromara.common.excel.utils.ExcelUtil;
// 操作日志注解，标记需要记录操作日志的方法
import org.dromara.common.log.annotation.Log;
// 业务类型枚举，定义操作类型（INSERT/UPDATE/DELETE/EXPORT/IMPORT等）
import org.dromara.common.log.enums.BusinessType;
// 测试单表实体类，对应数据库表
import org.dromara.demo.domain.TestDemo;
// 测试单表业务对象，封装前端传入的参数
import org.dromara.demo.domain.bo.TestDemoBo;
// 测试单表导入VO对象，用于Excel导入时的数据接收
import org.dromara.demo.domain.bo.TestDemoImportVo;
// 测试单表视图对象，返回给前端的数据格式
import org.dromara.demo.domain.vo.TestDemoVo;
// 测试单表服务接口，定义业务逻辑方法
import org.dromara.demo.service.ITestDemoService;
// Lombok注解：生成包含所有final字段的构造方法
// 用于构造函数注入，替代@Autowired
import lombok.RequiredArgsConstructor;
// Spring HTTP媒体类型常量，如MULTIPART_FORM_DATA_VALUE = "multipart/form-data"
import org.springframework.http.MediaType;
// Spring校验注解，启用方法参数校验功能
import org.springframework.validation.annotation.Validated;
// Spring Web注解：组合注解，包含@Controller和@ResponseBody
import org.springframework.web.bind.annotation.*;
// Spring Web注解：指定请求体内容类型
import org.springframework.web.bind.annotation.RequestBody;
// Spring Web注解：指定路径变量
import org.springframework.web.bind.annotation.PathVariable;
// Spring Web注解：指定请求参数
import org.springframework.web.bind.annotation.RequestParam;
// Spring Web注解：指定请求头
import org.springframework.web.bind.annotation.RequestHeader;
// Spring Web注解：指定请求部分（用于文件上传）
import org.springframework.web.bind.annotation.RequestPart;
// Spring Web注解：指定请求方法（GET/POST/PUT/DELETE等）
import org.springframework.web.bind.annotation.RequestMethod;
// Spring Web注解：指定请求路径
import org.springframework.web.bind.annotation.RequestMapping;
// Spring Web注解：指定RESTful控制器
import org.springframework.web.bind.annotation.RestController;
// Spring Web注解：指定GET请求方法
import org.springframework.web.bind.annotation.GetMapping;
// Spring Web注解：指定POST请求方法
import org.springframework.web.bind.annotation.PostMapping;
// Spring Web注解：指定PUT请求方法
import org.springframework.web.bind.annotation.PutMapping;
// Spring Web注解：指定DELETE请求方法
import org.springframework.web.bind.annotation.DeleteMapping;
// Spring Web注解：指定PATCH请求方法
import org.springframework.web.bind.annotation.PatchMapping;
// Spring Web文件上传接口，用于接收上传的文件
import org.springframework.web.multipart.MultipartFile;
// Jakarta Servlet HTTP响应接口，用于文件下载
import jakarta.servlet.http.HttpServletResponse;
// Jakarta校验注解：验证集合/数组不为空
import jakarta.validation.constraints.NotEmpty;
// Jakarta校验注解：验证对象不为null
import jakarta.validation.constraints.NotNull;
// Jakarta校验注解：验证字符串不为空
import jakarta.validation.constraints.NotBlank;
// Jakarta校验注解：验证数字大小
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
// Jakarta校验注解：验证字符串长度
import jakarta.validation.constraints.Size;
// Jakarta校验注解：验证正则表达式
import jakarta.validation.constraints.Pattern;
// Java集合工具类
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
// Java并发时间单位枚举，用于指定时间间隔单位
import java.util.concurrent.TimeUnit;

/**
 * 测试单表Controller
 * 提供测试单表的增删改查、导入导出等RESTful API接口
 * 继承BaseController获取通用响应封装能力
 *
 * @author Lion Li
 * @date 2021-07-26
 */
// Spring校验注解，启用方法参数校验功能
@Validated
// Lombok注解：生成包含所有final字段的构造方法，实现构造函数注入
@RequiredArgsConstructor
// Spring注解：组合@Controller和@ResponseBody，表示RESTful控制器
@RestController
// Spring注解：指定请求路径前缀为/demo/demo
@RequestMapping("/demo/demo")
// 测试单表Controller，继承BaseController
public class TestDemoController extends BaseController {

    // 测试单表服务接口，通过构造函数注入
    // final关键字确保依赖不可变，符合依赖注入最佳实践
    private final ITestDemoService testDemoService;

    /**
     * 查询测试单表列表（分页）
     * 使用MyBatis-Plus分页插件实现分页查询
     * 需要demo:demo:list权限
     *
     * @param bo 查询条件业务对象
     * @param pageQuery 分页参数
     * @return 分页数据表格信息
     */
    // Sa-Token权限校验：需要demo:demo:list权限
    @SaCheckPermission("demo:demo:list")
    // Spring注解：指定GET请求方法，路径为/list
    @GetMapping("/list")
    // 查询测试单表列表，返回分页数据
    public TableDataInfo<TestDemoVo> list(@Validated(QueryGroup.class) TestDemoBo bo, PageQuery pageQuery) {
        // 调用服务层方法执行分页查询
        // 使用QueryGroup校验组验证查询参数
        // 返回TableDataInfo包含total和rows
        return testDemoService.queryPageList(bo, pageQuery);
    }

    /**
     * 自定义分页查询
     * 演示自定义SQL分页查询的实现方式
     * 需要demo:demo:list权限
     *
     * @param bo 查询条件业务对象
     * @param pageQuery 分页参数
     * @return 分页数据表格信息
     */
    // Sa-Token权限校验：需要demo:demo:list权限
    @SaCheckPermission("demo:demo:list")
    // Spring注解：指定GET请求方法，路径为/page
    @GetMapping("/page")
    // 自定义分页查询，返回分页数据
    public TableDataInfo<TestDemoVo> page(@Validated(QueryGroup.class) TestDemoBo bo, PageQuery pageQuery) {
        // 调用服务层自定义分页方法
        // 演示自定义SQL实现
        return testDemoService.customPageList(bo, pageQuery);
    }

    /**
     * 导入Excel数据
     * 支持批量导入测试单表数据
     * 记录操作日志，需要demo:demo:import权限
     *
     * @param file 上传的Excel文件
     * @return 统一响应结果，包含导入分析信息
     * @throws Exception 导入过程中的异常
     */
    // 操作日志注解：记录导入操作日志
    @Log(title = "测试单表", businessType = BusinessType.IMPORT)
    // Sa-Token权限校验：需要demo:demo:import权限
    @SaCheckPermission("demo:demo:import")
    // Spring注解：指定POST请求方法，路径为/importData
    // consumes指定请求内容类型为multipart/form-data，用于文件上传
    @PostMapping(value = "/importData", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    // 导入Excel数据，返回操作结果
    public R<Void> importData(@RequestPart("file") MultipartFile file) throws Exception {
        // 使用ExcelUtil导入Excel文件，返回导入结果
        // 参数：输入流、目标类、是否开启校验
        ExcelResult<TestDemoImportVo> excelResult = ExcelUtil.importExcel(file.getInputStream(), TestDemoImportVo.class, true);
        // 使用Mapstruct将导入VO转换为实体类列表
        // 避免手动编写转换代码
        List<TestDemo> list = MapstructUtils.convert(excelResult.getList(), TestDemo.class);
        // 调用服务层批量保存方法
        // saveBatch是MyBatis-Plus提供的批量插入方法
        testDemoService.saveBatch(list);
        // 返回成功响应，携带导入分析信息（成功/失败数量）
        return R.ok(excelResult.getAnalysis());
    }

    /**
     * 导出测试单表列表
     * 将查询结果导出为Excel文件
     * 记录操作日志，需要demo:demo:export权限
     *
     * @param bo 查询条件业务对象
     * @param response HTTP响应对象，用于写入Excel文件
     */
    // Sa-Token权限校验：需要demo:demo:export权限
    @SaCheckPermission("demo:demo:export")
    // 操作日志注解：记录导出操作日志
    @Log(title = "测试单表", businessType = BusinessType.EXPORT)
    // Spring注解：指定POST请求方法，路径为/export
    @PostMapping("/export")
    // 导出测试单表列表到Excel
    public void export(@Validated TestDemoBo bo, HttpServletResponse response) {
        // 调用服务层查询列表数据
        List<TestDemoVo> list = testDemoService.queryList(bo);
        // 测试雪花id导出（注释掉的测试代码）
        // 用于验证大数值ID的导出处理
//        for (TestDemoVo vo : list) {
//            vo.setId(1234567891234567893L);
//        }
        // 使用ExcelUtil导出Excel文件
        // 参数：数据列表、Sheet名称、目标类、响应对象
        ExcelUtil.exportExcel(list, "测试单表", TestDemoVo.class, response);
    }

    /**
     * 获取测试单表详细信息
     * 根据ID查询单条记录
     * 需要demo:demo:query权限
     *
     * @param id 主键ID
     * @return 统一响应结果，包含详细信息
     */
    // Sa-Token权限校验：需要demo:demo:query权限
    @SaCheckPermission("demo:demo:query")
    // Spring注解：指定GET请求方法，路径为/{id}
    @GetMapping("/{id}")
    // 获取测试单表详细信息
    public R<TestDemoVo> getInfo(@NotNull(message = "主键不能为空")
                                 @PathVariable("id") Long id) {
        // 调用服务层查询单条记录
        // 返回R.ok()包装的结果
        return R.ok(testDemoService.queryById(id));
    }

    /**
     * 新增测试单表
     * 添加一条新记录
     * 需要demo:demo:add权限，记录操作日志
     * 防重复提交控制，2秒内不允许重复提交
     *
     * @param bo 业务对象
     * @return 统一响应结果
     */
    // Sa-Token权限校验：需要demo:demo:add权限
    @SaCheckPermission("demo:demo:add")
    // 操作日志注解：记录新增操作日志
    @Log(title = "测试单表", businessType = BusinessType.INSERT)
    // 防重复提交注解：2秒内不允许重复提交
    @RepeatSubmit(interval = 2, timeUnit = TimeUnit.SECONDS, message = "{repeat.submit.message}")
    // Spring注解：指定POST请求方法，路径为空（使用类路径）
    @PostMapping()
    // 新增测试单表
    public R<Void> add(@RequestBody TestDemoBo bo) {
        // 手动触发校验，对标@Validated(AddGroup.class)注解
        // 用于在非Controller的地方校验对象
        ValidatorUtils.validate(bo, AddGroup.class);
        // 调用服务层插入方法，返回影响行数
        // toAjax()将行数转换为R响应（>0为成功）
        return toAjax(testDemoService.insertByBo(bo));
    }

    /**
     * 修改测试单表
     * 更新已有记录
     * 需要demo:demo:edit权限，记录操作日志
     * 防重复提交控制
     *
     * @param bo 业务对象
     * @return 统一响应结果
     */
    // Sa-Token权限校验：需要demo:demo:edit权限
    @SaCheckPermission("demo:demo:edit")
    // 操作日志注解：记录修改操作日志
    @Log(title = "测试单表", businessType = BusinessType.UPDATE)
    // 防重复提交注解：默认1秒内不允许重复提交
    @RepeatSubmit
    // Spring注解：指定PUT请求方法，路径为空
    @PutMapping()
    // 修改测试单表
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody TestDemoBo bo) {
        // 调用服务层更新方法，返回影响行数
        // 使用EditGroup校验组验证编辑参数
        return toAjax(testDemoService.updateByBo(bo));
    }

    /**
     * 删除测试单表
     * 批量删除记录
     * 需要demo:demo:remove权限，记录操作日志
     *
     * @param ids 主键ID数组
     * @return 统一响应结果
     */
    // Sa-Token权限校验：需要demo:demo:remove权限
    @SaCheckPermission("demo:demo:remove")
    // 操作日志注解：记录删除操作日志
    @Log(title = "测试单表", businessType = BusinessType.DELETE)
    // Spring注解：指定DELETE请求方法，路径为/{ids}
    @DeleteMapping("/{ids}")
    // 删除测试单表
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        // 调用服务层删除方法，返回影响行数
        // Arrays.asList将数组转换为List
        // true表示进行逻辑删除校验
        return toAjax(testDemoService.deleteWithValidByIds(Arrays.asList(ids), true));
    }
}
