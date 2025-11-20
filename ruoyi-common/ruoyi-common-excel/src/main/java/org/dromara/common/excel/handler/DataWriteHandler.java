// 包声明：定义Excel数据写入处理器类所在的包路径
package org.dromara.common.excel.handler;

// 导入Hutool集合工具类，用于判断集合是否为空
import cn.hutool.core.collection.CollUtil;
// 导入EasyExcel的ExcelProperty注解，用于获取Excel表头名称
import cn.idev.excel.annotation.ExcelProperty;
// 导入EasyExcel数据格式数据类，用于设置单元格格式
import cn.idev.excel.metadata.data.DataFormatData;
// 导入EasyExcel写入单元格数据类，封装单元格数据
import cn.idev.excel.metadata.data.WriteCellData;
// 导入EasyExcel样式工具类，用于构建单元格样式
import cn.idev.excel.util.StyleUtil;
// 导入EasyExcel单元格写入处理器接口，在单元格写入后执行自定义逻辑
import cn.idev.excel.write.handler.CellWriteHandler;
// 导入EasyExcel Sheet写入处理器接口，在Sheet写入过程中执行自定义逻辑
import cn.idev.excel.write.handler.SheetWriteHandler;
// 导入EasyExcel单元格写入处理器上下文，提供单元格写入时的上下文信息
import cn.idev.excel.write.handler.context.CellWriteHandlerContext;
// 导入EasyExcel Sheet写入持有者，提供Sheet相关信息
import cn.idev.excel.write.metadata.holder.WriteSheetHolder;
// 导入EasyExcel写入单元格样式类，定义单元格样式
import cn.idev.excel.write.metadata.style.WriteCellStyle;
// 导入EasyExcel写入字体类，定义字体样式
import cn.idev.excel.write.metadata.style.WriteFont;
// 导入Apache POI单元格接口，表示Excel单元格
import org.apache.poi.ss.usermodel.*;
// 导入Apache POI XSSF客户端锚点类，用于定位批注位置
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
// 导入Apache POI XSSF富文本字符串类，用于设置批注内容
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
// 导入Excel批注注解，用于标记需要添加批注的字段
import org.dromara.common.excel.annotation.ExcelNotation;
// 导入Excel必填项注解，用于标记必填字段
import org.dromara.common.excel.annotation.ExcelRequired;

// 导入Java反射Field类，用于获取字段注解信息
import java.lang.reflect.Field;
// 导入HashMap，用于存储批注和必填列信息
import java.util.HashMap;
// 导入Map接口，定义映射操作规范
import java.util.Map;

/**
 * 批注、必填项处理器
 * 实现SheetWriteHandler和CellWriteHandler接口
 * 在Excel导出时，为表头添加批注和必填项标识（红色字体）
 * 提升Excel模板的可用性和数据录入准确性
 *
 * @author guzhouyanyu
 */
// 实现SheetWriteHandler和CellWriteHandler接口，在Sheet和单元格写入过程中执行自定义逻辑
public class DataWriteHandler implements SheetWriteHandler, CellWriteHandler {

    /**
     * 批注映射
     * key为表头名称，value为批注内容
     */
    private final Map<String, String> notationMap;

    /**
     * 必填列字体颜色映射
     * key为表头名称，value为字体颜色索引（如红色）
     */
    private final Map<String, Short> headColumnMap;


    /**
     * 构造函数
     * @param clazz 实体类Class对象，用于扫描@ExcelNotation和@ExcelRequired注解
     */
    public DataWriteHandler(Class<?> clazz) {
        // 获取批注映射
        notationMap = getNotationMap(clazz);
        // 获取必填列映射
        headColumnMap = getRequiredMap(clazz);
    }

    /**
     * 单元格写入后处理
     * 在表头单元格写入后，添加批注和必填项样式
     *
     * @param context 单元格写入处理器上下文
     */
    @Override
    public void afterCellDispose(CellWriteHandlerContext context) {
        // 如果批注映射和必填列映射都为空，直接返回（无需处理）
        if (CollUtil.isEmpty(notationMap) && CollUtil.isEmpty(headColumnMap)) {
            return;
        }
        // 获取第一个单元格数据
        WriteCellData<?> cellData = context.getFirstCellData();
        // 获取或创建单元格样式
        WriteCellStyle writeCellStyle = cellData.getOrCreateStyle();

        // 如果是表头行
        if (context.getHead()) {
            // 创建数据格式对象
            DataFormatData dataFormatData = new DataFormatData();
            // 单元格设置为文本格式（格式索引49表示文本）
            dataFormatData.setIndex((short) 49);
            // 应用数据格式到样式
            writeCellStyle.setDataFormatData(dataFormatData);
            // 获取当前单元格
            Cell cell = context.getCell();
            // 获取Sheet持有者
            WriteSheetHolder writeSheetHolder = context.getWriteSheetHolder();
            // 获取Sheet对象
            Sheet sheet = writeSheetHolder.getSheet();
            // 获取Workbook对象
            Workbook workbook = writeSheetHolder.getSheet().getWorkbook();
            // 创建绘图对象（用于添加批注）
            Drawing<?> drawing = sheet.createDrawingPatriarch();
            // 设置标题字体样式
            WriteFont headWriteFont = new WriteFont();
            // 设置字体加粗
            headWriteFont.setBold(true);
            // 如果必填列映射不为空且包含当前表头名称
            if (CollUtil.isNotEmpty(headColumnMap) && headColumnMap.containsKey(cell.getStringCellValue())) {
                // 设置字体颜色（通常为红色，表示必填）
                headWriteFont.setColor(headColumnMap.get(cell.getStringCellValue()));
            }
            // 应用字体到样式
            writeCellStyle.setWriteFont(headWriteFont);
            // 构建单元格样式并应用到单元格
            CellStyle cellStyle = StyleUtil.buildCellStyle(workbook, null, writeCellStyle);
            cell.setCellStyle(cellStyle);

            // 如果批注映射不为空且包含当前表头名称
            if (CollUtil.isNotEmpty(notationMap) && notationMap.containsKey(cell.getStringCellValue())) {
                // 获取批注内容
                String notationContext = notationMap.get(cell.getStringCellValue());
                // 创建批注对象（设置位置和大小）
                Comment comment = drawing.createCellComment(new XSSFClientAnchor(0, 0, 0, 0, (short) cell.getColumnIndex(), 0, (short) 5, 5));
                // 设置批注内容
                comment.setString(new XSSFRichTextString(notationContext));
                // 将批注添加到单元格
                cell.setCellComment(comment);
            }
        }
    }

    /**
     * 获取必填列映射
     * 扫描实体类中标注了@ExcelRequired注解的字段
     *
     * @param clazz 实体类Class对象
     * @return 必填列映射，key为表头名称，value为字体颜色索引
     */
    private static Map<String, Short> getRequiredMap(Class<?> clazz) {
        // 创建必填列映射
        Map<String, Short> requiredMap = new HashMap<>();
        // 获取实体类所有声明的字段
        Field[] fields = clazz.getDeclaredFields();
        // 遍历所有字段
        for (Field field : fields) {
            // 如果字段没有标注@ExcelRequired注解，跳过
            if (!field.isAnnotationPresent(ExcelRequired.class)) {
                continue;
            }
            // 获取@ExcelRequired注解实例
            ExcelRequired excelRequired = field.getAnnotation(ExcelRequired.class);
            // 获取@ExcelProperty注解实例（获取表头名称）
            ExcelProperty excelProperty = field.getAnnotation(ExcelProperty.class);
            // 将表头名称和字体颜色索引放入映射
            requiredMap.put(excelProperty.value()[0], excelRequired.fontColor().getIndex());
        }
        // 返回必填列映射
        return requiredMap;
    }

    /**
     * 获取批注映射
     * 扫描实体类中标注了@ExcelNotation注解的字段
     *
     * @param clazz 实体类Class对象
     * @return 批注映射，key为表头名称，value为批注内容
     */
    private static Map<String, String> getNotationMap(Class<?> clazz) {
        // 创建批注映射
        Map<String, String> notationMap = new HashMap<>();
        // 获取实体类所有声明的字段
        Field[] fields = clazz.getDeclaredFields();
        // 遍历所有字段
        for (Field field : fields) {
            // 如果字段没有标注@ExcelNotation注解，跳过
            if (!field.isAnnotationPresent(ExcelNotation.class)) {
                continue;
            }
            // 获取@ExcelNotation注解实例
            ExcelNotation excelNotation = field.getAnnotation(ExcelNotation.class);
            // 获取@ExcelProperty注解实例（获取表头名称）
            ExcelProperty excelProperty = field.getAnnotation(ExcelProperty.class);
            // 将表头名称和批注内容放入映射
            notationMap.put(excelProperty.value()[0], excelNotation.value());
        }
        // 返回批注映射
        return notationMap;
    }
}
