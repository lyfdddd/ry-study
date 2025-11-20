// 包声明：定义ExcelWriter包装器类所在的包路径
package org.dromara.common.excel.utils;

// 导入EasyExcel写入器类，用于Excel写入操作
import cn.idev.excel.ExcelWriter;
// 导入EasyExcel快速操作类，提供简洁的API
import cn.idev.excel.FastExcel;
// 导入EasyExcel写入上下文类，提供写入过程中的上下文信息
import cn.idev.excel.context.WriteContext;
// 导入EasyExcel写入Sheet构建器类，用于构建WriteSheet
import cn.idev.excel.write.builder.ExcelWriterSheetBuilder;
// 导入EasyExcel写入Table构建器类，用于构建WriteTable
import cn.idev.excel.write.builder.ExcelWriterTableBuilder;
// 导入EasyExcel写入Sheet元数据类
import cn.idev.excel.write.metadata.WriteSheet;
// 导入EasyExcel写入Table元数据类
import cn.idev.excel.write.metadata.WriteTable;
// 导入EasyExcel填充配置类，用于模板填充配置
import cn.idev.excel.write.metadata.fill.FillConfig;

// 导入集合接口
import java.util.Collection;
// 导入供应商函数式接口
import java.util.function.Supplier;

/**
 * ExcelWriterWrapper Excel写出包装器
 * <br>
 * 提供了一组与 ExcelWriter 一一对应的写出方法，避免直接提供 ExcelWriter 而导致的一些不可控问题（比如提前关闭了IO流等）
 * 通过包装器模式，封装ExcelWriter的操作，提供更安全的API
 *
 * @author 秋辞未寒
 * @param <T> 实体类型
 * @see ExcelWriter
 */
// 使用record定义不可变数据类，包含一个ExcelWriter字段
public record ExcelWriterWrapper<T>(ExcelWriter excelWriter) {

    /**
     * 写入数据到Sheet
     * @param data 数据集合
     * @param writeSheet Sheet对象
     */
    public void write(Collection<T> data, WriteSheet writeSheet) {
        // 委托给ExcelWriter的write方法
        excelWriter.write(data, writeSheet);
    }

    /**
     * 写入数据到Sheet（延迟加载）
     * @param supplier 数据供应商函数
     * @param writeSheet Sheet对象
     */
    public void write(Supplier<Collection<T>> supplier, WriteSheet writeSheet) {
        // 调用supplier获取数据，然后委托给ExcelWriter
        excelWriter.write(supplier.get(), writeSheet);
    }

    /**
     * 写入数据到Sheet和Table
     * @param data 数据集合
     * @param writeSheet Sheet对象
     * @param writeTable Table对象
     */
    public void write(Collection<T> data, WriteSheet writeSheet, WriteTable writeTable) {
        // 委托给ExcelWriter的write方法
        excelWriter.write(data, writeSheet, writeTable);
    }

    /**
     * 写入数据到Sheet和Table（延迟加载）
     * @param supplier 数据供应商函数
     * @param writeSheet Sheet对象
     * @param writeTable Table对象
     */
    public void write(Supplier<Collection<T>> supplier, WriteSheet writeSheet, WriteTable writeTable) {
        // 调用supplier获取数据，然后委托给ExcelWriter
        excelWriter.write(supplier.get(), writeSheet, writeTable);
    }

    /**
     * 填充数据到Sheet（模板填充）
     * @param data 数据对象
     * @param writeSheet Sheet对象
     */
    public void fill(Object data, WriteSheet writeSheet) {
        // 委托给ExcelWriter的fill方法
        excelWriter.fill(data, writeSheet);
    }

    /**
     * 填充数据到Sheet（模板填充，带配置）
     * @param data 数据对象
     * @param fillConfig 填充配置
     * @param writeSheet Sheet对象
     */
    public void fill(Object data, FillConfig fillConfig, WriteSheet writeSheet) {
        // 委托给ExcelWriter的fill方法
        excelWriter.fill(data, fillConfig, writeSheet);
    }

    /**
     * 填充数据到Sheet（模板填充，延迟加载）
     * @param supplier 数据供应商函数
     * @param writeSheet Sheet对象
     */
    public void fill(Supplier<Object> supplier, WriteSheet writeSheet) {
        // 委托给ExcelWriter的fill方法
        excelWriter.fill(supplier, writeSheet);
    }

    /**
     * 填充数据到Sheet（模板填充，延迟加载，带配置）
     * @param supplier 数据供应商函数
     * @param fillConfig 填充配置
     * @param writeSheet Sheet对象
     */
    public void fill(Supplier<Object> supplier, FillConfig fillConfig, WriteSheet writeSheet) {
        // 委托给ExcelWriter的fill方法
        excelWriter.fill(supplier, fillConfig, writeSheet);
    }

    /**
     * 获取写入上下文
     * @return WriteContext对象
     */
    public WriteContext writeContext() {
        // 委托给ExcelWriter的writeContext方法
        return excelWriter.writeContext();
    }

    /**
     * 创建一个 ExcelWriterWrapper
     * 静态工厂方法，用于创建包装器实例
     *
     * @param excelWriter ExcelWriter对象
     * @param <T> 实体类型
     * @return ExcelWriterWrapper实例
     */
    public static  <T> ExcelWriterWrapper<T> of(ExcelWriter excelWriter) {
        // 创建并返回新的ExcelWriterWrapper实例
        return new ExcelWriterWrapper<>(excelWriter);
    }

    // -------------------------------- sheet start
    // Sheet相关构建方法的快捷方式

    /**
     * 构建Sheet（带索引和名称）
     * @param sheetNo Sheet索引（从0开始）
     * @param sheetName Sheet名称
     * @return WriteSheet对象
     */
    public static WriteSheet buildSheet(Integer sheetNo, String sheetName) {
        // 调用sheetBuilder构建并返回
        return sheetBuilder(sheetNo, sheetName).build();
    }

    /**
     * 构建Sheet（带索引）
     * @param sheetNo Sheet索引（从0开始）
     * @return WriteSheet对象
     */
    public static WriteSheet buildSheet(Integer sheetNo) {
        // 调用sheetBuilder构建并返回
        return sheetBuilder(sheetNo).build();
    }

    /**
     * 构建Sheet（带名称）
     * @param sheetName Sheet名称
     * @return WriteSheet对象
     */
    public static WriteSheet buildSheet(String sheetName) {
        // 调用sheetBuilder构建并返回
        return sheetBuilder(sheetName).build();
    }

    /**
     * 构建Sheet（默认）
     * @return WriteSheet对象
     */
    public static WriteSheet buildSheet() {
        // 调用sheetBuilder构建并返回
        return sheetBuilder().build();
    }

    /**
     * 创建Sheet构建器（带索引和名称）
     * @param sheetNo Sheet索引（从0开始）
     * @param sheetName Sheet名称
     * @return ExcelWriterSheetBuilder对象
     */
    public static ExcelWriterSheetBuilder sheetBuilder(Integer sheetNo, String sheetName) {
        // 委托给FastExcel的writerSheet方法
        return FastExcel.writerSheet(sheetNo, sheetName);
    }

    /**
     * 创建Sheet构建器（带索引）
     * @param sheetNo Sheet索引（从0开始）
     * @return ExcelWriterSheetBuilder对象
     */
    public static ExcelWriterSheetBuilder sheetBuilder(Integer sheetNo) {
        // 委托给FastExcel的writerSheet方法
        return FastExcel.writerSheet(sheetNo);
    }

    /**
     * 创建Sheet构建器（带名称）
     * @param sheetName Sheet名称
     * @return ExcelWriterSheetBuilder对象
     */
    public static ExcelWriterSheetBuilder sheetBuilder(String sheetName) {
        // 委托给FastExcel的writerSheet方法
        return FastExcel.writerSheet(sheetName);
    }

    /**
     * 创建Sheet构建器（默认）
     * @return ExcelWriterSheetBuilder对象
     */
    public static ExcelWriterSheetBuilder sheetBuilder() {
        // 委托给FastExcel的writerSheet方法
        return FastExcel.writerSheet();
    }

    // -------------------------------- sheet end

    // -------------------------------- table start
    // Table相关构建方法的快捷方式

    /**
     * 构建Table（带索引）
     * @param tableNo Table索引（从0开始）
     * @return WriteTable对象
     */
    public static WriteTable buildTable(Integer tableNo) {
        // 调用tableBuilder构建并返回
        return tableBuilder(tableNo).build();
    }

    /**
     * 构建Table（默认）
     * @return WriteTable对象
     */
    public static WriteTable buildTable() {
        // 调用tableBuilder构建并返回
        return tableBuilder().build();
    }

    /**
     * 创建Table构建器（带索引）
     * @param tableNo Table索引（从0开始）
     * @return ExcelWriterTableBuilder对象
     */
    public static ExcelWriterTableBuilder tableBuilder(Integer tableNo) {
        // 委托给FastExcel的writerTable方法
        return FastExcel.writerTable(tableNo);
    }

    /**
     * 创建Table构建器（默认）
     * @return ExcelWriterTableBuilder对象
     */
    public static ExcelWriterTableBuilder tableBuilder() {
        // 委托给FastExcel的writerTable方法
        return FastExcel.writerTable();
    }

    // -------------------------------- table end

}
