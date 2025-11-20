// 包声明：定义Excel工具类所在的包路径
package org.dromara.common.excel.utils;

// 导入Hutool集合工具类，用于判断集合是否为空
import cn.hutool.core.collection.CollUtil;
// 导入Hutool类路径资源类，用于读取classpath下的模板文件
import cn.hutool.core.io.resource.ClassPathResource;
// 导入Hutool ID工具类，用于生成UUID
import cn.hutool.core.util.IdUtil;
// 导入EasyExcel快速操作类，提供简洁的API
import cn.idev.excel.FastExcel;
// 导入EasyExcel写入器类，用于Excel写入操作
import cn.idev.excel.ExcelWriter;
// 导入EasyExcel写入Sheet构建器，用于构建WriteSheet
import cn.idev.excel.write.builder.ExcelWriterSheetBuilder;
// 导入EasyExcel写入Sheet元数据类
import cn.idev.excel.write.metadata.WriteSheet;
// 导入EasyExcel填充配置类，用于模板填充配置
import cn.idev.excel.write.metadata.fill.FillConfig;
// 导入EasyExcel填充包装器类，用于多列表填充
import cn.idev.excel.write.metadata.fill.FillWrapper;
// 导入EasyExcel最长匹配列宽策略类，自动调整列宽
import cn.idev.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
// 导入Jakarta Servlet输出流
import jakarta.servlet.ServletOutputStream;
// 导入Jakarta HTTP Servlet响应
import jakarta.servlet.http.HttpServletResponse;
// 导入Lombok访问级别枚举
import lombok.AccessLevel;
// 导入Lombok无参构造函数注解，设置为私有防止实例化
import lombok.NoArgsConstructor;
// 导入字符串工具类
import org.dromara.common.core.utils.StringUtils;
// 导入文件工具类
import org.dromara.common.core.utils.file.FileUtils;
// 导入Excel大数值转换器，防止大数值失真
import org.dromara.common.excel.convert.ExcelBigNumberConvert;
// 导入Excel核心组件（*表示导入core包下所有类）
import org.dromara.common.excel.core.*;
// 导入数据写入处理器，处理批注和必填项
import org.dromara.common.excel.handler.DataWriteHandler;

// 导入IO异常类
import java.io.IOException;
// 导入输入流
import java.io.InputStream;
// 导入输出流
import java.io.OutputStream;
// 导入不支持的编码异常
import java.io.UnsupportedEncodingException;
// 导入集合接口
import java.util.Collection;
// 导入列表接口
import java.util.List;
// 导入映射接口
import java.util.Map;
// 导入消费者函数式接口
import java.util.function.Consumer;

/**
 * Excel相关处理工具类
 * 提供Excel导入导出的静态方法，封装EasyExcel操作
 * 支持同步导入、异步导入、数据导出、模板导出等功能
 *
 * @author Lion Li
 */
// 使用Lombok生成私有构造函数，防止实例化（工具类）
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExcelUtil {

    /**
     * 同步导入(适用于小数据量)
     * 一次性读取所有数据到内存，适用于数据量较小的场景
     *
     * @param is 输入流
     * @param clazz 实体类Class对象
     * @param <T> 实体类型
     * @return 转换后的实体列表
     */
    public static <T> List<T> importExcel(InputStream is, Class<T> clazz) {
        // 使用FastExcel读取输入流，指定实体类，不自动关闭流，读取第一个Sheet，同步返回数据
        return FastExcel.read(is).head(clazz).autoCloseStream(false).sheet().doReadSync();
    }


    /**
     * 使用校验监听器 异步导入 同步返回
     * 异步读取Excel数据，进行Bean验证，返回导入结果
     *
     * @param is 输入流
     * @param clazz 实体类Class对象
     * @param isValidate 是否启用Validator检验，默认为是
     * @param <T> 实体类型
     * @return ExcelResult对象，包含成功数据和错误信息
     */
    public static <T> ExcelResult<T> importExcel(InputStream is, Class<T> clazz, boolean isValidate) {
        // 创建默认Excel监听器，传入是否启用验证参数
        DefaultExcelListener<T> listener = new DefaultExcelListener<>(isValidate);
        // 使用FastExcel读取，指定实体类和监听器，读取第一个Sheet
        FastExcel.read(is, clazz, listener).sheet().doRead();
        // 返回监听器中的导入结果
        return listener.getExcelResult();
    }

    /**
     * 使用自定义监听器 异步导入 自定义返回
     * 支持传入自定义的Excel监听器，实现特定的导入逻辑
     *
     * @param is 输入流
     * @param clazz 实体类Class对象
     * @param listener 自定义Excel监听器
     * @param <T> 实体类型
     * @return ExcelResult对象，包含成功数据和错误信息
     */
    public static <T> ExcelResult<T> importExcel(InputStream is, Class<T> clazz, ExcelListener<T> listener) {
        // 使用FastExcel读取，指定实体类和自定义监听器，读取第一个Sheet
        FastExcel.read(is, clazz, listener).sheet().doRead();
        // 返回监听器中的导入结果
        return listener.getExcelResult();
    }

    /**
     * 导出excel到HTTP响应
     * 将数据导出为Excel文件并通过HTTP响应返回给客户端
     *
     * @param list 导出数据集合
     * @param sheetName 工作表名称
     * @param clazz 实体类Class对象
     * @param response HTTP响应对象
     * @param <T> 实体类型
     */
    public static <T> void exportExcel(List<T> list, String sheetName, Class<T> clazz, HttpServletResponse response) {
        try {
            // 重置响应头，设置文件名和Content-Type
            resetResponse(sheetName, response);
            // 获取Servlet输出流
            ServletOutputStream os = response.getOutputStream();
            // 调用导出方法，不合并单元格，无下拉选项
            exportExcel(list, sheetName, clazz, false, os, null);
        } catch (IOException e) {
            // 抛出运行时异常，提示导出异常
            throw new RuntimeException("导出Excel异常");
        }
    }

    /**
     * 导出excel到HTTP响应（支持级联下拉）
     * 将数据导出为Excel文件，支持级联下拉框
     *
     * @param list 导出数据集合
     * @param sheetName 工作表名称
     * @param clazz 实体类Class对象
     * @param response HTTP响应对象
     * @param options 级联下拉选项配置
     * @param <T> 实体类型
     */
    public static <T> void exportExcel(List<T> list, String sheetName, Class<T> clazz, HttpServletResponse response, List<DropDownOptions> options) {
        try {
            // 重置响应头，设置文件名和Content-Type
            resetResponse(sheetName, response);
            // 获取Servlet输出流
            ServletOutputStream os = response.getOutputStream();
            // 调用导出方法，不合并单元格，传入下拉选项
            exportExcel(list, sheetName, clazz, false, os, options);
        } catch (IOException e) {
            // 抛出运行时异常，提示导出异常
            throw new RuntimeException("导出Excel异常");
        }
    }

    /**
     * 导出excel到HTTP响应（支持合并单元格）
     * 将数据导出为Excel文件，支持合并单元格
     *
     * @param list 导出数据集合
     * @param sheetName 工作表名称
     * @param clazz 实体类Class对象
     * @param merge 是否合并单元格
     * @param response HTTP响应对象
     * @param <T> 实体类型
     */
    public static <T> void exportExcel(List<T> list, String sheetName, Class<T> clazz, boolean merge, HttpServletResponse response) {
        try {
            // 重置响应头，设置文件名和Content-Type
            resetResponse(sheetName, response);
            // 获取Servlet输出流
            ServletOutputStream os = response.getOutputStream();
            // 调用导出方法，传入合并参数，无下拉选项
            exportExcel(list, sheetName, clazz, merge, os, null);
        } catch (IOException e) {
            // 抛出运行时异常，提示导出异常
            throw new RuntimeException("导出Excel异常");
        }
    }

    /**
     * 导出excel到HTTP响应（支持合并单元格和级联下拉）
     * 将数据导出为Excel文件，支持合并单元格和级联下拉框
     *
     * @param list 导出数据集合
     * @param sheetName 工作表名称
     * @param clazz 实体类Class对象
     * @param merge 是否合并单元格
     * @param response HTTP响应对象
     * @param options 级联下拉选项配置
     * @param <T> 实体类型
     */
    public static <T> void exportExcel(List<T> list, String sheetName, Class<T> clazz, boolean merge, HttpServletResponse response, List<DropDownOptions> options) {
        try {
            // 重置响应头，设置文件名和Content-Type
            resetResponse(sheetName, response);
            // 获取Servlet输出流
            ServletOutputStream os = response.getOutputStream();
            // 调用导出方法，传入合并参数和下拉选项
            exportExcel(list, sheetName, clazz, merge, os, options);
        } catch (IOException e) {
            // 抛出运行时异常，提示导出异常
            throw new RuntimeException("导出Excel异常");
        }
    }

    /**
     * 导出excel到输出流
     * 将数据导出为Excel文件并写入输出流
     *
     * @param list 导出数据集合
     * @param sheetName 工作表名称
     * @param clazz 实体类Class对象
     * @param os 输出流
     * @param <T> 实体类型
     */
    public static <T> void exportExcel(List<T> list, String sheetName, Class<T> clazz, OutputStream os) {
        // 调用导出方法，不合并单元格，无下拉选项
        exportExcel(list, sheetName, clazz, false, os, null);
    }

    /**
     * 导出excel到输出流（支持级联下拉）
     * 将数据导出为Excel文件并写入输出流，支持级联下拉框
     *
     * @param list 导出数据集合
     * @param sheetName 工作表名称
     * @param clazz 实体类Class对象
     * @param os 输出流
     * @param options 级联下拉选项配置
     * @param <T> 实体类型
     */
    public static <T> void exportExcel(List<T> list, String sheetName, Class<T> clazz, OutputStream os, List<DropDownOptions> options) {
        // 调用导出方法，不合并单元格，传入下拉选项
        exportExcel(list, sheetName, clazz, false, os, options);
    }

    /**
     * 导出excel到输出流（支持合并单元格和级联下拉）
     * 将数据导出为Excel文件并写入输出流，支持合并单元格和级联下拉框
     *
     * @param list 导出数据集合
     * @param sheetName 工作表名称
     * @param clazz 实体类Class对象
     * @param merge 是否合并单元格
     * @param os 输出流
     * @param options 级联下拉选项配置
     * @param <T> 实体类型
     */
    public static <T> void exportExcel(List<T> list, String sheetName, Class<T> clazz, boolean merge,
                                       OutputStream os, List<DropDownOptions> options) {
        // 创建ExcelWriterSheetBuilder，配置写入参数
        ExcelWriterSheetBuilder builder = FastExcel.write(os, clazz)
            // 不自动关闭流（由调用方关闭）
            .autoCloseStream(false)
            // 注册自动列宽策略（根据内容自动调整列宽）
            .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
            // 注册大数值转换器，防止大数值失真（超过15位转为字符串）
            .registerConverter(new ExcelBigNumberConvert())
            // 注册数据写入处理器（处理批注和必填项）
            .registerWriteHandler(new DataWriteHandler(clazz))
            // 指定Sheet名称
            .sheet(sheetName);
        // 如果需要合并单元格
        if (merge) {
            // 注册单元格合并策略（按列合并相同值的单元格）
            builder.registerWriteHandler(new CellMergeStrategy(list, true));
        }
        // 如果存在下拉选项配置
        if (options != null) {
            // 注册下拉框处理器（添加下拉框和数据验证）
            builder.registerWriteHandler(new ExcelDownHandler(options));
        }
        // 执行写入操作
        builder.doWrite(list);
    }

    /**
     * 导出excel（自定义写入逻辑）
     * 提供ExcelWriterWrapper给消费函数，支持复杂的自定义写入逻辑
     *
     * @param headType 带Excel注解的实体类Class对象
     * @param os 输出流
     * @param options 级联下拉选项配置
     * @param consumer 导出助手消费函数，接收ExcelWriterWrapper进行自定义写入
     * @param <T> 实体类型
     */
    public static <T> void exportExcel(Class<T> headType, OutputStream os, List<DropDownOptions> options, Consumer<ExcelWriterWrapper<T>> consumer) {
        // 使用try-with-resources确保ExcelWriter正确关闭
        try (ExcelWriter writer = FastExcel.write(os, headType)
            // 不自动关闭流（由try-with-resources关闭）
            .autoCloseStream(false)
            // 注册自动列宽策略
            .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
            // 注册大数值转换器
            .registerConverter(new ExcelBigNumberConvert())
            // 注册数据写入处理器（批注和必填项）
            .registerWriteHandler(new DataWriteHandler(headType))
            // 如果存在下拉选项，注册下拉框处理器
            .registerWriteHandler(new ExcelDownHandler(options))
            // 构建ExcelWriter
            .build()) {
            // 执行消费函数，传入ExcelWriterWrapper
            consumer.accept(ExcelWriterWrapper.of(writer));
        } catch (Exception e) {
            // 抛出运行时异常，包装原始异常
            throw new RuntimeException(e);
        }
    }

    /**
     * 导出excel（自定义写入逻辑，无下拉选项）
     * 提供ExcelWriterWrapper给消费函数，支持复杂的自定义写入逻辑
     *
     * @param headType 带Excel注解的实体类Class对象
     * @param os 输出流
     * @param consumer 导出助手消费函数，接收ExcelWriterWrapper进行自定义写入
     * @param <T> 实体类型
     */
    public static <T> void exportExcel(Class<T> headType, OutputStream os, Consumer<ExcelWriterWrapper<T>> consumer) {
        // 调用重载方法，传入null作为下拉选项
        exportExcel(headType, os, null, consumer);
    }

    /**
     * 单表多数据模板导出 模板格式为 {.属性}
     * 使用模板文件导出数据，模板中使用{.属性}占位符
     *
     * @param data 模板需要的数据列表
     * @param filename 文件名
     * @param templatePath 模板路径（resource目录下的路径，包括模板文件名）
     *                     例如: excel/temp.xlsx
     *                     重点: 模板文件必须放置到启动类对应的resource目录下
     * @param response HTTP响应对象
     * @param <T> 实体类型
     */
    public static <T> void exportTemplate(List<T> data, String filename, String templatePath, HttpServletResponse response) {
        try {
            // 如果数据为空，抛出非法参数异常
            if (CollUtil.isEmpty(data)) {
                throw new IllegalArgumentException("数据为空");
            }
            // 重置响应头，设置文件名和Content-Type
            resetResponse(filename, response);
            // 获取Servlet输出流
            ServletOutputStream os = response.getOutputStream();
            // 调用模板导出方法
            exportTemplate(data, templatePath, os);
        } catch (IOException e) {
            // 抛出运行时异常，提示导出异常
            throw new RuntimeException("导出Excel异常");
        }
    }

    /**
     * 单表多数据模板导出 模板格式为 {.属性}
     * 使用模板文件导出数据，模板中使用{.属性}占位符
     *
     * @param data 模板需要的数据列表
     * @param templatePath 模板路径（resource目录下的路径，包括模板文件名）
     *                     例如: excel/temp.xlsx
     *                     重点: 模板文件必须放置到启动类对应的resource目录下
     * @param os 输出流
     * @param <T> 实体类型
     */
    public static <T> void exportTemplate(List<T> data, String templatePath, OutputStream os) {
        // 创建类路径资源对象，读取模板文件
        ClassPathResource templateResource = new ClassPathResource(templatePath);
        // 创建ExcelWriter，使用模板
        ExcelWriter excelWriter = FastExcel.write(os)
            // 指定模板输入流
            .withTemplate(templateResource.getStream())
            // 不自动关闭流（由调用方关闭）
            .autoCloseStream(false)
            // 注册大数值转换器
            .registerConverter(new ExcelBigNumberConvert())
            // 注册数据写入处理器（批注和必填项）
            .registerWriteHandler(new DataWriteHandler(data.get(0).getClass()))
            // 构建ExcelWriter
            .build();
        // 创建WriteSheet
        WriteSheet writeSheet = FastExcel.writerSheet().build();
        // 创建填充配置，强制创建新行（避免覆盖）
        FillConfig fillConfig = FillConfig.builder().forceNewRow(Boolean.TRUE).build();
        // 单表多数据导出 模板格式为 {.属性}
        // 遍历数据列表，逐行填充
        for (T d : data) {
            excelWriter.fill(d, fillConfig, writeSheet);
        }
        // 完成写入（关闭writer）
        excelWriter.finish();
    }

    /**
     * 多表多数据模板导出 模板格式为 {key.属性}
     * 使用模板文件导出多个数据表，模板中使用{key.属性}占位符
     *
     * @param data 模板需要的数据Map（key对应模板中的key）
     * @param filename 文件名
     * @param templatePath 模板路径（resource目录下的路径，包括模板文件名）
     *                     例如: excel/temp.xlsx
     *                     重点: 模板文件必须放置到启动类对应的resource目录下
     * @param response HTTP响应对象
     */
    public static void exportTemplateMultiList(Map<String, Object> data, String filename, String templatePath, HttpServletResponse response) {
        try {
            // 如果数据为空，抛出非法参数异常
            if (CollUtil.isEmpty(data)) {
                throw new IllegalArgumentException("数据为空");
            }
            // 重置响应头，设置文件名和Content-Type
            resetResponse(filename, response);
            // 获取Servlet输出流
            ServletOutputStream os = response.getOutputStream();
            // 调用多表模板导出方法
            exportTemplateMultiList(data, templatePath, os);
        } catch (IOException e) {
            // 抛出运行时异常，提示导出异常
            throw new RuntimeException("导出Excel异常");
        }
    }

    /**
     * 多sheet模板导出 模板格式为 {key.属性}
     * 使用模板文件导出多个Sheet，每个Sheet使用不同的数据
     *
     * @param data 模板需要的数据列表（每个Map对应一个Sheet）
     * @param filename 文件名
     * @param templatePath 模板路径（resource目录下的路径，包括模板文件名）
     *                     例如: excel/temp.xlsx
     *                     重点: 模板文件必须放置到启动类对应的resource目录下
     * @param response HTTP响应对象
     */
    public static void exportTemplateMultiSheet(List<Map<String, Object>> data, String filename, String templatePath, HttpServletResponse response) {
        try {
            // 如果数据为空，抛出非法参数异常
            if (CollUtil.isEmpty(data)) {
                throw new IllegalArgumentException("数据为空");
            }
            // 重置响应头，设置文件名和Content-Type
            resetResponse(filename, response);
            // 获取Servlet输出流
            ServletOutputStream os = response.getOutputStream();
            // 调用多Sheet模板导出方法
            exportTemplateMultiSheet(data, templatePath, os);
        } catch (IOException e) {
            // 抛出运行时异常，提示导出异常
            throw new RuntimeException("导出Excel异常");
        }
    }

    /**
     * 多表多数据模板导出 模板格式为 {key.属性}
     * 使用模板文件导出多个数据表，模板中使用{key.属性}占位符
     *
     * @param data 模板需要的数据Map（key对应模板中的key）
     * @param templatePath 模板路径（resource目录下的路径，包括模板文件名）
     *                     例如: excel/temp.xlsx
     *                     重点: 模板文件必须放置到启动类对应的resource目录下
     * @param os 输出流
     */
    public static void exportTemplateMultiList(Map<String, Object> data, String templatePath, OutputStream os) {
        // 创建类路径资源对象，读取模板文件
        ClassPathResource templateResource = new ClassPathResource(templatePath);
        // 创建ExcelWriter，使用模板
        ExcelWriter excelWriter = FastExcel.write(os)
            // 指定模板输入流
            .withTemplate(templateResource.getStream())
            // 不自动关闭流（由调用方关闭）
            .autoCloseStream(false)
            // 注册大数值转换器
            .registerConverter(new ExcelBigNumberConvert())
            // 构建ExcelWriter
            .build();
        // 创建WriteSheet
        WriteSheet writeSheet = FastExcel.writerSheet().build();
        // 遍历数据Map
        for (Map.Entry<String, Object> map : data.entrySet()) {
            // 设置列表后续还有数据
            FillConfig fillConfig = FillConfig.builder().forceNewRow(Boolean.TRUE).build();
            // 如果值是集合类型
            if (map.getValue() instanceof Collection) {
                // 多表导出必须使用FillWrapper
                excelWriter.fill(new FillWrapper(map.getKey(), (Collection<?>) map.getValue()), fillConfig, writeSheet);
            } else {
                // 单个对象直接填充
                excelWriter.fill(map.getValue(), fillConfig, writeSheet);
            }
        }
        // 完成写入
        excelWriter.finish();
    }

    /**
     * 多sheet模板导出 模板格式为 {key.属性}
     * 使用模板文件导出多个Sheet，每个Sheet使用不同的数据
     *
     * @param data 模板需要的数据列表（每个Map对应一个Sheet）
     * @param templatePath 模板路径（resource目录下的路径，包括模板文件名）
     *                     例如: excel/temp.xlsx
     *                     重点: 模板文件必须放置到启动类对应的resource目录下
     * @param os 输出流
     */
    public static void exportTemplateMultiSheet(List<Map<String, Object>> data, String templatePath, OutputStream os) {
        // 创建类路径资源对象，读取模板文件
        ClassPathResource templateResource = new ClassPathResource(templatePath);
        // 创建ExcelWriter，使用模板
        ExcelWriter excelWriter = FastExcel.write(os)
            // 指定模板输入流
            .withTemplate(templateResource.getStream())
            // 不自动关闭流（由调用方关闭）
            .autoCloseStream(false)
            // 注册大数值转换器
            .registerConverter(new ExcelBigNumberConvert())
            // 构建ExcelWriter
            .build();
        // 遍历数据列表（每个Map对应一个Sheet）
        for (int i = 0; i < data.size(); i++) {
            // 创建WriteSheet，指定Sheet索引
            WriteSheet writeSheet = FastExcel.writerSheet(i).build();
            // 遍历当前Sheet的数据Map
            for (Map.Entry<String, Object> map : data.get(i).entrySet()) {
                // 设置列表后续还有数据
                FillConfig fillConfig = FillConfig.builder().forceNewRow(Boolean.TRUE).build();
                // 如果值是集合类型
                if (map.getValue() instanceof Collection) {
                    // 多表导出必须使用FillWrapper
                    excelWriter.fill(new FillWrapper(map.getKey(), (Collection<?>) map.getValue()), fillConfig, writeSheet);
                } else {
                    // 单个对象直接填充
                    excelWriter.fill(map.getValue(), writeSheet);
                }
            }
        }
        // 完成写入
        excelWriter.finish();
    }

    /**
     * 重置响应体
     * 设置HTTP响应头，包括文件名和Content-Type
     *
     * @param sheetName Sheet名称（用作文件名）
     * @param response HTTP响应对象
     * @throws UnsupportedEncodingException 不支持的编码异常
     */
    private static void resetResponse(String sheetName, HttpServletResponse response) throws UnsupportedEncodingException {
        // 编码文件名（添加UUID防止重复）
        String filename = encodingFilename(sheetName);
        // 设置附件响应头（触发浏览器下载）
        FileUtils.setAttachmentResponseHeader(response, filename);
        // 设置Content-Type为Excel文件类型
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8");
    }

    /**
     * 解析导出值 0=男,1=女,2=未知
     * 将编码值转换为显示值（如0转换为"男"）
     *
     * @param propertyValue 参数值（如0）
     * @param converterExp 转换表达式（如"0=男,1=女,2=未知"）
     * @param separator 分隔符（如","）
     * @return 解析后的显示值（如"男"）
     */
    public static String convertByExp(String propertyValue, String converterExp, String separator) {
        // 创建字符串构建器
        StringBuilder propertyString = new StringBuilder();
        // 按系统分隔符拆分转换表达式
        String[] convertSource = converterExp.split(StringUtils.SEPARATOR);
        // 遍历每个转换项
        for (String item : convertSource) {
            // 按等号拆分为键值对
            String[] itemArray = item.split("=");
            // 如果属性值包含分隔符（多选情况）
            if (StringUtils.containsAny(propertyValue, separator)) {
                // 按分隔符拆分属性值
                for (String value : propertyValue.split(separator)) {
                    // 如果键匹配
                    if (itemArray[0].equals(value)) {
                        // 追加值和分隔符
                        propertyString.append(itemArray[1] + separator);
                        break;
                    }
                }
            } else {
                // 单选情况，如果键匹配
                if (itemArray[0].equals(propertyValue)) {
                    // 返回值
                    return itemArray[1];
                }
            }
        }
        // 去除末尾的分隔符并返回
        return StringUtils.stripEnd(propertyString.toString(), separator);
    }

    /**
     * 反向解析值 男=0,女=1,未知=2
     * 将显示值转换为编码值（如"男"转换为0）
     *
     * @param propertyValue 参数值（如"男"）
     * @param converterExp 转换表达式（如"男=0,女=1,未知=2"）
     * @param separator 分隔符（如","）
     * @return 解析后的编码值（如0）
     */
    public static String reverseByExp(String propertyValue, String converterExp, String separator) {
        // 创建字符串构建器
        StringBuilder propertyString = new StringBuilder();
        // 按系统分隔符拆分转换表达式
        String[] convertSource = converterExp.split(StringUtils.SEPARATOR);
        // 遍历每个转换项
        for (String item : convertSource) {
            // 按等号拆分为键值对
            String[] itemArray = item.split("=");
            // 如果属性值包含分隔符（多选情况）
            if (StringUtils.containsAny(propertyValue, separator)) {
                // 按分隔符拆分属性值
                for (String value : propertyValue.split(separator)) {
                    // 如果值匹配
                    if (itemArray[1].equals(value)) {
                        // 追加键和分隔符
                        propertyString.append(itemArray[0] + separator);
                        break;
                    }
                }
            } else {
                // 单选情况，如果值匹配
                if (itemArray[1].equals(propertyValue)) {
                    // 返回键
                    return itemArray[0];
                }
            }
        }
        // 去除末尾的分隔符并返回
        return StringUtils.stripEnd(propertyString.toString(), separator);
    }

    /**
     * 编码文件名
     * 生成唯一的文件名，防止重复和冲突
     *
     * @param filename 原始文件名
     * @return 编码后的文件名（UUID_原始文件名.xlsx）
     */
    public static String encodingFilename(String filename) {
        // 使用Hutool的IdUtil生成快速UUID，拼接文件名和扩展名
        return IdUtil.fastSimpleUUID() + "_" + filename + ".xlsx";
    }

}
