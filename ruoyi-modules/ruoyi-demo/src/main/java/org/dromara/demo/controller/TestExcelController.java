// 测试Excel功能Controller层
// 提供Excel导入导出的各种测试接口
// 包括单列表、多列表、多sheet、下拉框、自定义导出等功能
package org.dromara.demo.controller;

// Sa-Token忽略注解，表示该接口不需要登录和权限校验
// 用于测试接口，生产环境慎用
import cn.dev33.satoken.annotation.SaIgnore;
// Hutool集合工具类，提供丰富的集合操作方法
import cn.hutool.core.collection.CollUtil;
// Jakarta Servlet HTTP响应接口，用于文件下载
import jakarta.servlet.http.HttpServletResponse;
// Lombok注解：生成包含所有字段的构造方法
import lombok.AllArgsConstructor;
// Lombok注解：生成getter、setter、toString等方法
import lombok.Data;
// Lombok注解：生成包含所有final字段的构造方法
import lombok.RequiredArgsConstructor;
// Excel导入结果封装类，包含成功/失败数据、分析信息等
import org.dromara.common.excel.core.ExcelResult;
// Excel工具类，提供导入导出功能
import org.dromara.common.excel.utils.ExcelUtil;
// 导出演示VO对象，用于Excel导出时的数据接收
import org.dromara.demo.domain.vo.ExportDemoVo;
// 导出演示监听器，用于处理Excel导入事件
import org.dromara.demo.listener.ExportDemoListener;
// 导出Excel服务接口，定义Excel导出业务逻辑
import org.dromara.demo.service.IExportExcelService;
// Spring HTTP媒体类型常量，如MULTIPART_FORM_DATA_VALUE = "multipart/form-data"
import org.springframework.http.MediaType;
// Spring Web注解：组合注解，包含@Controller和@ResponseBody
import org.springframework.web.bind.annotation.*;
// Spring Web文件上传接口，用于接收上传的文件
import org.springframework.web.multipart.MultipartFile;

// Java IO异常
import java.io.IOException;
// Java集合工具类
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试Excel功能
 * 提供Excel导入导出的各种测试接口
 * 包括单列表、多列表、多sheet、下拉框、自定义导出等功能
 *
 * @author Lion Li
 */
// Lombok注解：生成包含所有final字段的构造方法，实现构造函数注入
@RequiredArgsConstructor
// Spring注解：组合@Controller和@ResponseBody，表示RESTful控制器
@RestController
// Spring注解：指定请求路径前缀为/demo/excel
@RequestMapping("/demo/excel")
// 测试Excel功能Controller
public class TestExcelController {

    // 导出Excel服务接口，通过构造函数注入
    // final关键字确保依赖不可变，符合依赖注入最佳实践
    private final IExportExcelService exportExcelService;

    /**
     * 导出单列表多数据模板
     * 演示基于模板的单列表导出功能
     * 模板文件位于resources/excel/单列表.xlsx
     *
     * @param response HTTP响应对象，用于写入Excel文件
     */
    // Spring注解：指定GET请求方法，路径为/exportTemplateOne
    @GetMapping("/exportTemplateOne")
    // 导出单列表多数据模板
    public void exportTemplateOne(HttpServletResponse response) {
        // 创建Map存储模板变量数据
        // key为模板中的变量名，value为替换的值
        Map<String, String> map = new HashMap<>();
        map.put("title", "单列表多数据");
        map.put("test1", "数据测试1");
        map.put("test2", "数据测试2");
        map.put("test3", "数据测试3");
        map.put("test4", "数据测试4");
        map.put("testTest", "666");
        
        // 创建列表数据
        // 每个TestObj对象对应Excel中的一行数据
        List<TestObj> list = new ArrayList<>();
        list.add(new TestObj("单列表测试1", "列表测试1", "列表测试2", "列表测试3", "列表测试4"));
        list.add(new TestObj("单列表测试2", "列表测试5", "列表测试6", "列表测试7", "列表测试8"));
        list.add(new TestObj("单列表测试3", "列表测试9", "列表测试10", "列表测试11", "列表测试12"));
        
        // 使用ExcelUtil导出模板
        // 参数：数据列表（包含变量Map和行数据List）、文件名、模板路径、响应对象
        // CollUtil.newArrayList将多个对象合并为一个List
        ExcelUtil.exportTemplate(CollUtil.newArrayList(map, list), "单列表.xlsx", "excel/单列表.xlsx", response);
    }

    /**
     * 导出多列表多数据模板
     * 演示基于模板的多列表导出功能
     * 模板文件位于resources/excel/多列表.xlsx
     *
     * @param response HTTP响应对象，用于写入Excel文件
     */
    // Spring注解：指定GET请求方法，路径为/exportTemplateMuliti
    @GetMapping("/exportTemplateMuliti")
    // 导出多列表多数据模板
    public void exportTemplateMuliti(HttpServletResponse response) {
        // 创建Map存储模板变量数据
        Map<String, String> map = new HashMap<>();
        map.put("title1", "标题1");
        map.put("title2", "标题2");
        map.put("title3", "标题3");
        map.put("title4", "标题4");
        map.put("author", "Lion Li");
        
        // 创建多个列表数据
        // 每个列表对应Excel中的一个表格区域
        List<TestObj1> list1 = new ArrayList<>();
        list1.add(new TestObj1("list1测试1", "list1测试2", "list1测试3"));
        list1.add(new TestObj1("list1测试4", "list1测试5", "list1测试6"));
        list1.add(new TestObj1("list1测试7", "list1测试8", "list1测试9"));
        
        List<TestObj1> list2 = new ArrayList<>();
        list2.add(new TestObj1("list2测试1", "list2测试2", "list2测试3"));
        list2.add(new TestObj1("list2测试4", "list2测试5", "list2测试6"));
        
        List<TestObj1> list3 = new ArrayList<>();
        list3.add(new TestObj1("list3测试1", "list3测试2", "list3测试3"));
        
        List<TestObj1> list4 = new ArrayList<>();
        list4.add(new TestObj1("list4测试1", "list4测试2", "list4测试3"));
        list4.add(new TestObj1("list4测试4", "list4测试5", "list4测试6"));
        list4.add(new TestObj1("list4测试7", "list4测试8", "list4测试9"));
        list4.add(new TestObj1("list4测试10", "list4测试11", "list4测试12"));
        
        // 创建多列表Map
        // key为模板中的区域标识，value为对应的数据列表
        Map<String, Object> multiListMap = new HashMap<>();
        multiListMap.put("map", map);
        multiListMap.put("data1", list1);
        multiListMap.put("data2", list2);
        multiListMap.put("data3", list3);
        multiListMap.put("data4", list4);
        
        // 使用ExcelUtil导出多列表模板
        // 参数：多列表Map、文件名、模板路径、响应对象
        ExcelUtil.exportTemplateMultiList(multiListMap, "多列表.xlsx", "excel/多列表.xlsx", response);
    }

    /**
     * 导出带下拉框的Excel
     * 演示Excel下拉框功能，支持单级下拉和级联下拉
     *
     * @param response HTTP响应对象，用于写入Excel文件
     */
    // Spring注解：指定GET请求方法，路径为/exportWithOptions
    @GetMapping("/exportWithOptions")
    // 导出带下拉框的Excel
    public void exportWithOptions(HttpServletResponse response) {
        // 调用服务层方法执行导出
        // 服务层负责构建下拉框数据和导出逻辑
        exportExcelService.exportWithOptions(response);
    }

    /**
     * 自定义导出
     * 演示自定义Excel导出逻辑，如自定义样式、合并单元格等
     *
     * @param response HTTP响应对象，用于写入Excel文件
     * @throws IOException IO异常
     */
    // Spring注解：指定GET请求方法，路径为/customExport
    @GetMapping("/customExport")
    // 自定义导出Excel
    public void customExport(HttpServletResponse response) throws IOException {
        // 调用服务层方法执行自定义导出
        exportExcelService.customExport(response);
    }

    /**
     * 导出多sheet Excel
     * 演示一个Excel文件中包含多个sheet的功能
     *
     * @param response HTTP响应对象，用于写入Excel文件
     */
    // Spring注解：指定GET请求方法，路径为/exportTemplateMultiSheet
    @GetMapping("/exportTemplateMultiSheet")
    // 导出多sheet Excel
    public void exportTemplateMultiSheet(HttpServletResponse response) {
        // 创建多个列表数据，每个列表对应一个sheet
        List<TestObj1> list1 = new ArrayList<>();
        list1.add(new TestObj1("list1测试1", "list1测试2", "list1测试3"));
        list1.add(new TestObj1("list1测试4", "list1测试5", "list1测试6"));
        
        List<TestObj1> list2 = new ArrayList<>();
        list2.add(new TestObj1("list2测试1", "list2测试2", "list2测试3"));
        list2.add(new TestObj1("list2测试4", "list2测试5", "list2测试6"));
        
        List<TestObj1> list3 = new ArrayList<>();
        list3.add(new TestObj1("list3测试1", "list3测试2", "list3测试3"));
        list3.add(new TestObj1("list3测试4", "list3测试5", "list3测试6"));
        
        List<TestObj1> list4 = new ArrayList<>();
        list4.add(new TestObj1("list4测试1", "list4测试2", "list4测试3"));
        list4.add(new TestObj1("list4测试4", "list4测试5", "list4测试6"));

        // 创建sheet列表
        // 每个Map对应一个sheet，key为sheet名称，value为数据列表
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> sheetMap1 = new HashMap<>();
        sheetMap1.put("data1", list1);
        Map<String, Object> sheetMap2 = new HashMap<>();
        sheetMap2.put("data2", list2);
        Map<String, Object> sheetMap3 = new HashMap<>();
        sheetMap3.put("data3", list3);
        Map<String, Object> sheetMap4 = new HashMap<>();
        sheetMap4.put("data4", list4);

        // 添加到sheet列表
        list.add(sheetMap1);
        list.add(sheetMap2);
        list.add(sheetMap3);
        list.add(sheetMap4);
        
        // 使用ExcelUtil导出多sheet模板
        // 参数：sheet列表、文件名、模板路径、响应对象
        ExcelUtil.exportTemplateMultiSheet(list, "多sheet列表", "excel/多sheet列表.xlsx", response);
    }

    /**
     * 导入带下拉框的Excel
     * 演示Excel导入功能，支持下拉框数据验证
     *
     * @param file 上传的Excel文件
     * @return 导入的数据列表
     * @throws Exception 导入过程中的异常
     */
    // Spring注解：指定POST请求方法，路径为/importWithOptions
    // consumes指定请求内容类型为multipart/form-data，用于文件上传
    @PostMapping(value = "/importWithOptions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    // 导入带下拉框的Excel
    public List<ExportDemoVo> importWithOptions(@RequestPart("file") MultipartFile file) throws Exception {
        // 使用ExcelUtil导入Excel文件
        // 参数：输入流、目标类、监听器（用于处理导入事件）
        // ExportDemoListener处理导入过程中的业务逻辑
        ExcelResult<ExportDemoVo> excelResult = ExcelUtil.importExcel(file.getInputStream(), ExportDemoVo.class, new ExportDemoListener());
        // 返回导入成功的数据列表
        return excelResult.getList();
    }

    /**
     * 测试对象1（3个字段）
     * 用于多列表和多sheet导出的数据模型
     */
    // Lombok注解：生成getter、setter、toString等方法
    @Data
    // Lombok注解：生成包含所有字段的构造方法
    @AllArgsConstructor
    // 静态内部类，避免污染外部命名空间
    static class TestObj1 {
        // 测试字段1
        private String test1;
        // 测试字段2
        private String test2;
        // 测试字段3
        private String test3;
    }

    /**
     * 测试对象（5个字段）
     * 用于单列表导出的数据模型
     */
    // Lombok注解：生成getter、setter、toString等方法
    @Data
    // Lombok注解：生成包含所有字段的构造方法
    @AllArgsConstructor
    // 静态内部类，避免污染外部命名空间
    static class TestObj {
        // 名称字段
        private String name;
        // 列表字段1
        private String list1;
        // 列表字段2
        private String list2;
        // 列表字段3
        private String list3;
        // 列表字段4
        private String list4;
    }

}
