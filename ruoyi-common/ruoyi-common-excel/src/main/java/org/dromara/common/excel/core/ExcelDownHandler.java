// 包声明：定义Excel下拉处理器类所在的包路径
package org.dromara.common.excel.core;

// 导入Hutool集合工具类，用于判断集合是否为空
import cn.hutool.core.collection.CollUtil;
// 导入Hutool类型转换工具类，用于对象类型转换
import cn.hutool.core.convert.Convert;
// 导入Hutool数组工具类，用于数组操作
import cn.hutool.core.util.ArrayUtil;
// 导入Hutool枚举工具类，用于获取枚举字段值
import cn.hutool.core.util.EnumUtil;
// 导入Hutool对象工具类，用于判断对象是否为空
import cn.hutool.core.util.ObjectUtil;
// 导入Hutool字符串工具类，用于字符串操作
import cn.hutool.core.util.StrUtil;
// 导入EasyExcel字段缓存类，缓存实体类的字段信息
import cn.idev.excel.metadata.FieldCache;
// 导入EasyExcel字段包装类，封装字段元数据
import cn.idev.excel.metadata.FieldWrapper;
// 导入EasyExcel类工具类，用于获取字段信息
import cn.idev.excel.util.ClassUtils;
// 导入EasyExcel Sheet写入处理器接口，在Sheet创建后执行自定义逻辑
import cn.idev.excel.write.handler.SheetWriteHandler;
// 导入EasyExcel Sheet写入持有者，提供Sheet相关信息
import cn.idev.excel.write.metadata.holder.WriteSheetHolder;
// 导入EasyExcel Workbook写入持有者，提供Workbook相关信息
import cn.idev.excel.write.metadata.holder.WriteWorkbookHolder;
// 导入Lombok Slf4j日志注解
import lombok.extern.slf4j.Slf4j;
// 导入Apache POI单元格接口，表示Excel单元格
import org.apache.poi.ss.usermodel.*;
// 导入Apache POI单元格范围地址列表，用于指定数据验证的应用范围
import org.apache.poi.ss.util.CellRangeAddressList;
// 导入Apache POI工作簿工具类，用于创建安全的Sheet名称
import org.apache.poi.ss.util.WorkbookUtil;
// 导入Apache POI XSSF数据验证类，处理Excel 2007+数据验证
import org.apache.poi.xssf.usermodel.XSSFDataValidation;
// 导入服务异常类，用于抛出业务异常
import org.dromara.common.core.exception.ServiceException;
// 导入字典服务接口，用于获取字典数据
import org.dromara.common.core.service.DictService;
// 导入Spring工具类，用于获取Spring容器中的Bean
import org.dromara.common.core.utils.SpringUtils;
// 导入Stream工具类，用于集合流式操作
import org.dromara.common.core.utils.StreamUtils;
// 导入字符串工具类，提供字符串操作方法
import org.dromara.common.core.utils.StringUtils;
// 导入Excel字典格式化注解，用于标记字典类型字段
import org.dromara.common.excel.annotation.ExcelDictFormat;
// 导入Excel枚举格式化注解，用于标记枚举类型字段
import org.dromara.common.excel.annotation.ExcelEnumFormat;

// 导入Java反射Field类，用于获取字段注解信息
import java.lang.reflect.Field;
// 导入ArrayList，用于存储下拉选项数据
import java.util.*;

/**
 * <h1>Excel表格下拉选操作</h1>
 * 实现SheetWriteHandler接口，在Excel写入过程中添加下拉框和数据验证
 * 考虑到下拉选过多可能导致Excel打开缓慢的问题，只校验前1000行
 * <p>
 * 即只有前1000行的数据可以用下拉框，超出的自行通过限制数据量的形式，第二次输出
 * 支持三种下拉框类型：简单下拉、额外表格下拉、级联下拉
 *
 * @author Emil.Zhang
 */
// 使用Lombok自动生成日志对象
@Slf4j
// 实现SheetWriteHandler接口，在Sheet创建后执行下拉框设置
public class ExcelDownHandler implements SheetWriteHandler {

    /**
     * Excel表格中的列名英文
     * 仅为了解析列英文，禁止修改
     * 包含A-Z 26个大写字母，用于将列索引转换为Excel列名（如0->A，26->AA）
     */
    private static final String EXCEL_COLUMN_NAME = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    /**
     * 单选数据Sheet名
     * 当可选项数量较多时，创建隐藏的Sheet存储选项数据
     */
    private static final String OPTIONS_SHEET_NAME = "options";
    /**
     * 联动选择数据Sheet名的头
     * 级联下拉框使用的隐藏Sheet名称前缀
     */
    private static final String LINKED_OPTIONS_SHEET_NAME = "linkedOptions";
    /**
     * 下拉可选项
     * 存储通过构造函数传入的下拉选项配置
     */
    private final List<DropDownOptions> dropDownOptions;
    /**
     * 字典服务
     * 用于获取字典数据，构建字典类型的下拉框
     */
    private final DictService dictService;
    /**
     * 当前单选进度
     * 用于生成唯一的额外表Sheet名称，避免重复
     */
    private int currentOptionsColumnIndex;
    /**
     * 当前联动选择进度
     * 用于生成唯一的级联下拉Sheet名称，避免重复
     */
    private int currentLinkedOptionsSheetIndex;

    /**
     * 构造函数
     * @param options 下拉选项配置列表
     */
    public ExcelDownHandler(List<DropDownOptions> options) {
        // 存储下拉选项配置
        this.dropDownOptions = options;
        // 初始化单选进度为0
        this.currentOptionsColumnIndex = 0;
        // 初始化级联进度为0
        this.currentLinkedOptionsSheetIndex = 0;
        // 从Spring容器获取字典服务Bean
        this.dictService = SpringUtils.getBean(DictService.class);
    }

    /**
     * <h2>开始创建下拉数据</h2>
     * 在Sheet创建完成后执行，设置下拉框和数据验证
     * 1.通过解析实体类中@ExcelProperty字段是否标注有@ExcelDictFormat或@ExcelEnumFormat注解
     * 如果有且设置了值，则依据字典或枚举建立下拉可选项
     * <p>
     * 2.或者在调用ExcelUtil时指定了DropDownOptions可选项，将依据传入的可选项做下拉
     * <p>
     * 3.二者可以并存，注意调用方式
     *
     * @param writeWorkbookHolder Workbook持有者，提供Workbook对象
     * @param writeSheetHolder Sheet持有者，提供Sheet对象
     */
    @Override
    public void afterSheetCreate(WriteWorkbookHolder writeWorkbookHolder, WriteSheetHolder writeSheetHolder) {
        // 获取当前Sheet对象
        Sheet sheet = writeSheetHolder.getSheet();
        // 开始设置下拉框，获取数据验证助手对象
        DataValidationHelper helper = sheet.getDataValidationHelper();
        // 获取Workbook对象
        Workbook workbook = writeWorkbookHolder.getWorkbook();
        // 获取实体类的字段缓存，包含所有字段的元数据信息
        FieldCache fieldCache = ClassUtils.declaredFields(writeWorkbookHolder.getClazz(), writeWorkbookHolder);
        // 遍历实体类中的所有字段（按Excel列顺序排序）
        for (Map.Entry<Integer, FieldWrapper> entry : fieldCache.getSortedFieldMap().entrySet()) {
            // 获取字段对应的列索引
            Integer index = entry.getKey();
            // 获取字段包装对象
            FieldWrapper wrapper = entry.getValue();
            // 获取字段反射对象
            Field field = wrapper.getField();
            // 循环实体中的每个属性
            // 可选的下拉值
            List<String> options = new ArrayList<>();
            // 检查字段是否标注了@ExcelDictFormat注解（字典格式化）
            if (field.isAnnotationPresent(ExcelDictFormat.class)) {
                // 如果指定了@ExcelDictFormat，则使用字典的逻辑
                // 获取字典格式化注解实例
                ExcelDictFormat format = field.getDeclaredAnnotation(ExcelDictFormat.class);
                // 获取字典类型
                String dictType = format.dictType();
                // 获取转换表达式（如：1=男,2=女）
                String converterExp = format.readConverterExp();
                // 如果指定了字典类型
                if (StringUtils.isNotBlank(dictType)) {
                    // 如果传递了字典名，则依据字典建立下拉
                    // 从字典服务获取该字典类型的所有值
                    Collection<String> values = Optional.ofNullable(dictService.getAllDictByDictType(dictType))
                        // 如果字典不存在，抛出服务异常
                        .orElseThrow(() -> new ServiceException("字典 {} 不存在", dictType))
                        // 获取字典值的集合
                        .values();
                    // 将字典值集合转换为ArrayList
                    options = new ArrayList<>(values);
                } else if (StringUtils.isNotBlank(converterExp)) {
                    // 如果指定了确切的值，则直接解析确切的值
                    // 按分隔符拆分转换表达式
                    List<String> strList = StringUtils.splitList(converterExp, format.separator());
                    // 提取等号后面的值作为下拉选项（如从"1=男"提取"男"）
                    options = StreamUtils.toList(strList, s -> StringUtils.split(s, "=")[1]);
                }
            } else if (field.isAnnotationPresent(ExcelEnumFormat.class)) {
                // 否则如果指定了@ExcelEnumFormat，则使用枚举的逻辑
                // 获取枚举格式化注解实例
                ExcelEnumFormat format = field.getDeclaredAnnotation(ExcelEnumFormat.class);
                // 获取枚举类中指定字段的所有值
                List<Object> values = EnumUtil.getFieldValues(format.enumClass(), format.textField());
                // 将枚举值转换为字符串列表
                options = StreamUtils.toList(values, Convert::toStr);
            }
            // 如果下拉选项不为空
            if (ObjectUtil.isNotEmpty(options)) {
                // 仅当下拉可选项不为空时执行
                // 如果可选项数量大于20，使用额外表形式（避免Excel打开缓慢）
                if (options.size() > 20) {
                    // 这里限制如果可选项大于20，则使用额外表形式
                    dropDownWithSheet(helper, workbook, sheet, index, options);
                } else {
                    // 否则使用固定值形式（直接嵌入数据验证公式）
                    dropDownWithSimple(helper, sheet, index, options);
                }
            }
        }
        // 如果通过构造函数传入的下拉选项配置为空，直接返回
        if (CollUtil.isEmpty(dropDownOptions)) {
            return;
        }
        // 遍历所有下拉选项配置
        dropDownOptions.forEach(everyOptions -> {
            // 如果传递了下拉框选择器参数
            // 当二级选项不为空时，使用额外关联表的形式（级联下拉）
            if (!everyOptions.getNextOptions().isEmpty()) {
                // 当二级选项不为空时，使用额外关联表的形式
                dropDownLinkedOptions(helper, workbook, sheet, everyOptions);
            } else if (everyOptions.getOptions().size() > 10) {
                // 当一级选项参数个数大于10，使用额外表的形式（避免Excel文件过大）
                dropDownWithSheet(helper, workbook, sheet, everyOptions.getIndex(), everyOptions.getOptions());
            } else {
                // 否则使用默认形式（简单下拉）
                dropDownWithSimple(helper, sheet, everyOptions.getIndex(), everyOptions.getOptions());
            }
        });
    }

    /**
     * <h2>简单下拉框</h2>
     * 直接将可选项拼接为指定列的数据校验值
     * 适用于选项数量较少（<=20）的场景，直接将选项嵌入数据验证公式
     *
     * @param helper 数据验证助手，用于创建数据验证约束
     * @param sheet 当前Sheet对象
     * @param celIndex 列索引（从0开始）
     * @param value 下拉选项值列表
     */
    private void dropDownWithSimple(DataValidationHelper helper, Sheet sheet, Integer celIndex, List<String> value) {
        // 如果选项值为空，直接返回
        if (ObjectUtil.isEmpty(value)) {
            return;
        }
        // 创建显式列表约束（将选项列表直接嵌入数据验证公式）
        // 调用markOptionsToSheet方法将约束应用到指定列
        this.markOptionsToSheet(helper, sheet, celIndex, helper.createExplicitListConstraint(ArrayUtil.toArray(value, String.class)));
    }

    /**
     * <h2>额外表格形式的级联下拉框</h2>
     * 创建隐藏的Sheet存储级联数据，使用名称管理器和INDIRECT函数实现级联效果
     * 第一行存储一级选项，每列下方存储对应的二级选项
     *
     * @param helper 数据验证助手
     * @param workbook Workbook对象
     * @param sheet 主Sheet对象
     * @param options 级联下拉选项配置
     */
    private void dropDownLinkedOptions(DataValidationHelper helper, Workbook workbook, Sheet sheet, DropDownOptions options) {
        // 生成唯一的级联下拉Sheet名称，避免重复
        String linkedOptionsSheetName = String.format("%s_%d", LINKED_OPTIONS_SHEET_NAME, currentLinkedOptionsSheetIndex);
        // 创建联动下拉数据表（隐藏Sheet）
        Sheet linkedOptionsDataSheet = workbook.createSheet(WorkbookUtil.createSafeSheetName(linkedOptionsSheetName));
        // 将下拉表隐藏，用户不可见
        workbook.setSheetHidden(workbook.getSheetIndex(linkedOptionsDataSheet), true);
        // 选项数据
        // 获取一级选项列表
        List<String> firstOptions = options.getOptions();
        // 获取二级选项映射（key为一级选项，value为二级选项列表）
        Map<String, List<String>> secoundOptionsMap = options.getNextOptions();

        // 采用按行填充数据的方式，避免出现数据无法写入的问题
        // Attempting to write a row in the range that is already written to disk
        // POI在写出时，超过100行会被临时写入硬盘，导致内存合并时出错

        // 使用ArrayList记载数据，防止乱序
        List<String> columnNames = new ArrayList<>();
        // 写入第一行，即第一级的数据
        Row firstRow = linkedOptionsDataSheet.createRow(0);
        // 遍历一级选项，写入第一行（作为列标题）
        for (int columnIndex = 0; columnIndex < firstOptions.size(); columnIndex++) {
            // 获取当前一级选项值
            String columnName = firstOptions.get(columnIndex);
            // 创建单元格并设置值
            firstRow.createCell(columnIndex)
                .setCellValue(columnName);
            // 将列名添加到列表，后续使用
            columnNames.add(columnName);
        }

        // 创建名称管理器（用于一级下拉）
        Name name = workbook.createName();
        // 设置名称管理器的别名
        name.setNameName(linkedOptionsSheetName);
        // 以横向第一行创建一级下拉拼接引用位置（如：linkedOptions_0!$A$1:$C$1）
        String firstOptionsFunction = String.format("%s!$%s$1:$%s$1",
            linkedOptionsSheetName,
            getExcelColumnName(0),
            getExcelColumnName(firstOptions.size())
        );
        // 设置名称管理器的引用位置
        name.setRefersToFormula(firstOptionsFunction);
        // 设置数据校验为序列模式，引用的是名称管理器中的别名
        // 将一级下拉应用到主表的指定列
        this.markOptionsToSheet(helper, sheet, options.getIndex(), helper.createFormulaListConstraint(linkedOptionsSheetName));

        // 创建二级选项的名称管理器
        // 遍历每个一级选项，为其创建独立的名称管理器
        for (int columIndex = 0; columIndex < columnNames.size(); columIndex++) {
            // 列名（即一级选项值）
            String firstOptionsColumnName = getExcelColumnName(columIndex);
            // 对应的一级值
            String thisFirstOptionsValue = columnNames.get(columIndex);

            // 以该一级选项值创建子名称管理器
            Name sonName = workbook.createName();
            // 设置名称管理器的别名（使用一级选项值作为名称）
            sonName.setNameName(thisFirstOptionsValue);
            // 以第二行该列数据拼接引用位置（如：linkedOptions_0!$A$2:$A$5）
            String sonFunction = String.format("%s!$%s$2:$%s$%d",
                linkedOptionsSheetName,
                firstOptionsColumnName,
                firstOptionsColumnName,
                // 二级选项存在则设置为(选项个数+1)行，否则设置为2行
                Math.max(Optional.ofNullable(secoundOptionsMap.get(thisFirstOptionsValue))
                    .orElseGet(ArrayList::new).size(), 1) + 1
            );
            // 设置名称管理器的引用位置
            sonName.setRefersToFormula(sonFunction);
            // 数据验证为序列模式，引用到每一个主表中的二级选项位置
            // 创建子项的名称管理器，只是为了使得Excel可以识别到数据
            // 获取主表中一级选项所在列的列名
            String mainSheetFirstOptionsColumnName = getExcelColumnName(options.getIndex());
            // 为前100行创建二级下拉（限制100行以提高性能）
            for (int i = 0; i < 100; i++) {
                // 以一级选项对应的主体所在位置创建二级下拉
                // 使用INDIRECT函数动态引用名称管理器（如：=INDIRECT(A2)）
                String secondOptionsFunction = String.format("=INDIRECT(%s%d)", mainSheetFirstOptionsColumnName, i + 1);
                // 二级只能主表每一行的每一列添加二级校验
                // 将二级下拉应用到主表的每一行
                markLinkedOptionsToSheet(helper, sheet, i, options.getNextIndex(), helper.createFormulaListConstraint(secondOptionsFunction));
            }
        }

        // 将二级数据处理为按行区分
        // 将二级选项数据转换为按行存储的格式，便于写入Excel
        Map<Integer, List<String>> columnValueMap = new HashMap<>();
        // 从第二行开始（第一行是一级选项）
        int currentRow = 1;
        // 循环处理直到所有二级选项都分配完毕
        while (currentRow >= 0) {
            // 标记是否还有数据
            boolean flag = false;
            // 存储当前行的数据
            List<String> rowData = new ArrayList<>();
            // 遍历每个一级选项对应的二级选项列表
            for (String columnName : columnNames) {
                // 获取该一级选项对应的二级选项列表
                List<String> data = secoundOptionsMap.get(columnName);
                // 如果二级选项列表为空
                if (CollUtil.isEmpty(data)) {
                    // 添加空字符串填充位置（保持列对齐）
                    rowData.add(" ");
                    continue;
                }
                // 取第一个二级选项
                String str = data.get(0);
                // 添加到当前行数据
                rowData.add(str);
                // 通过移除的方式避免重复（从列表中删除已使用的选项）
                data.remove(0);
                // 设置可以继续（还有数据未处理）
                flag = true;
            }
            // 将当前行数据存入Map
            columnValueMap.put(currentRow, rowData);
            // 可以继续，则增加行数，否则置为负数跳出循环
            if (flag) {
                // 还有数据，继续下一行
                currentRow++;
            } else {
                // 没有数据了，退出循环
                currentRow = -1;
            }
        }

        // 填充第二级选项数据
        // 将整理好的二级选项数据写入隐藏的Sheet
        columnValueMap.forEach((rowIndex, rowValues) -> {
            // 创建行
            Row row = linkedOptionsDataSheet.createRow(rowIndex);
            // 遍历列
            for (int columnIndex = 0; columnIndex < rowValues.size(); columnIndex++) {
                // 获取单元格值
                String rowValue = rowValues.get(columnIndex);
                // 填充位置的部分不渲染（跳过空字符串）
                if (StrUtil.isNotBlank(rowValue)) {
                    // 创建单元格并设置值
                    row.createCell(columnIndex)
                        .setCellValue(rowValue);
                }
            }
        });

        // 递增级联进度，确保下一个级联下拉使用不同的Sheet名称
        currentLinkedOptionsSheetIndex++;
    }

    /**
     * <h2>额外表格形式的普通下拉框</h2>
     * 由于下拉框可选值数量过多，为提升Excel打开效率，使用额外表格形式做下拉
     * 创建隐藏的Sheet存储选项数据，通过名称管理器引用
     *
     * @param helper 数据验证助手
     * @param workbook Workbook对象
     * @param sheet 主Sheet对象
     * @param celIndex 列索引
     * @param value 下拉选项值列表
     */
    private void dropDownWithSheet(DataValidationHelper helper, Workbook workbook, Sheet sheet, Integer celIndex, List<String> value) {
        //由于poi的写出相关问题，超过100个会被临时写进硬盘，导致后续内存合并会出Attempting to write a row[] in the range [] that is already written to disk
        // POI在写出时，超过100行会被临时写入硬盘，导致内存合并时出错
        // 生成唯一的额外表Sheet名称
        String tmpOptionsSheetName = OPTIONS_SHEET_NAME + "_" + currentOptionsColumnIndex;
        // 创建下拉数据表（如果已存在则获取，否则创建）
        Sheet simpleDataSheet = Optional.ofNullable(workbook.getSheet(WorkbookUtil.createSafeSheetName(tmpOptionsSheetName)))
            .orElseGet(() -> workbook.createSheet(WorkbookUtil.createSafeSheetName(tmpOptionsSheetName)));
        // 将下拉表隐藏（用户不可见）
        workbook.setSheetHidden(workbook.getSheetIndex(simpleDataSheet), true);
        // 完善纵向的一级选项数据表
        // 遍历选项值，写入隐藏Sheet
        for (int i = 0; i < value.size(); i++) {
            // 保存当前索引，用于lambda表达式
            int finalI = i;
            // 获取每一选项行，如果没有则创建
            Row row = Optional.ofNullable(simpleDataSheet.getRow(i))
                .orElseGet(() -> simpleDataSheet.createRow(finalI));
            // 获取本级选项对应的选项列，如果没有则创建。上述采用多个sheet,默认索引为1列
            Cell cell = Optional.ofNullable(row.getCell(0))
                .orElseGet(() -> row.createCell(0));
            // 设置值
            cell.setCellValue(value.get(i));
        }

        // 创建名称管理器
        Name name = workbook.createName();
        // 设置名称管理器的别名（唯一名称）
        String nameName = String.format("%s_%d", tmpOptionsSheetName, celIndex);
        name.setNameName(nameName);
        // 以纵向第一列创建一级下拉拼接引用位置（如：options_0!$A$1:$A$10）
        String function = String.format("%s!$%s$1:$%s$%d",
            tmpOptionsSheetName,
            getExcelColumnName(0),
            getExcelColumnName(0),
            value.size());
        // 设置名称管理器的引用位置
        name.setRefersToFormula(function);
        // 设置数据校验为序列模式，引用的是名称管理器中的别名
        // 将下拉框应用到主表的指定列
        this.markOptionsToSheet(helper, sheet, celIndex, helper.createFormulaListConstraint(nameName));
        // 递增单选进度，确保下一个额外表使用不同的名称
        currentOptionsColumnIndex++;
    }

    /**
     * 挂载下拉的列，仅限一级选项
     * 将数据验证约束应用到指定列（第1-1000行）
     *
     * @param helper 数据验证助手
     * @param sheet 当前Sheet对象
     * @param celIndex 列索引
     * @param constraint 数据验证约束
     */
    private void markOptionsToSheet(DataValidationHelper helper, Sheet sheet, Integer celIndex,
                                    DataValidationConstraint constraint) {
        // 设置数据有效性加载在哪个单元格上,四个参数分别是：起始行、终止行、起始列、终止列
        // 限制1000行以提高性能，避免Excel打开缓慢
        CellRangeAddressList addressList = new CellRangeAddressList(1, 1000, celIndex, celIndex);
        // 调用markDataValidationToSheet应用数据验证
        markDataValidationToSheet(helper, sheet, constraint, addressList);
    }

    /**
     * 挂载下拉的列，仅限二级选项
     * 将数据验证约束应用到指定单元格（单行单列）
     *
     * @param helper 数据验证助手
     * @param sheet 当前Sheet对象
     * @param rowIndex 行索引
     * @param celIndex 列索引
     * @param constraint 数据验证约束
     */
    private void markLinkedOptionsToSheet(DataValidationHelper helper, Sheet sheet, Integer rowIndex,
                                          Integer celIndex, DataValidationConstraint constraint) {
        // 设置数据有效性加载在哪个单元格上,四个参数分别是：起始行、终止行、起始列、终止列
        // 限制为单行单列（用于级联下拉）
        CellRangeAddressList addressList = new CellRangeAddressList(rowIndex, rowIndex, celIndex, celIndex);
        // 调用markDataValidationToSheet应用数据验证
        markDataValidationToSheet(helper, sheet, constraint, addressList);
    }

    /**
     * 应用数据校验
     * 创建数据验证对象并设置提示信息，添加到Sheet
     *
     * @param helper 数据验证助手
     * @param sheet 当前Sheet对象
     * @param constraint 数据验证约束
     * @param addressList 单元格范围地址列表
     */
    private void markDataValidationToSheet(DataValidationHelper helper, Sheet sheet,
                                           DataValidationConstraint constraint, CellRangeAddressList addressList) {
        // 数据有效性对象
        DataValidation dataValidation = helper.createValidation(constraint, addressList);
        // 处理Excel兼容性问题（XSSFDataValidation为Excel 2007+格式）
        if (dataValidation instanceof XSSFDataValidation) {
            //数据校验
            // 显示下拉箭头
            dataValidation.setSuppressDropDownArrow(true);
            //错误提示
            // 设置错误样式为停止（强制要求输入有效值）
            dataValidation.setErrorStyle(DataValidation.ErrorStyle.STOP);
            // 创建错误提示框
            dataValidation.createErrorBox("提示", "此值与单元格定义数据不一致");
            // 显示错误框
            dataValidation.setShowErrorBox(true);
            //选定提示
            // 创建输入提示框
            dataValidation.createPromptBox("填写说明：", "填写内容只能为下拉中数据，其他数据将导致导入失败");
            // 显示提示框
            dataValidation.setShowPromptBox(true);
            // 将数据验证添加到Sheet
            sheet.addValidationData(dataValidation);
        } else {
            // 旧版Excel格式，不显示下拉箭头
            dataValidation.setSuppressDropDownArrow(false);
        }
        // 将数据验证添加到Sheet
        sheet.addValidationData(dataValidation);
    }

    /**
     * <h2>依据列index获取列名英文</h2>
     * 依据列index转换为Excel中的列名英文
     * <p>例如第1列，index为0，解析出来为A列</p>
     * 第27列，index为26，解析为AA列
     * <p>第28列，index为27，解析为AB列</p>
     * 使用26进制转换算法，将数字索引转换为Excel列字母
     *
     * @param columnIndex 列index（从0开始）
     * @return 列index对应的英文名（如A、B、AA、AB等）
     */
    private String getExcelColumnName(int columnIndex) {
        // 26一循环的次数（用于处理超过Z列的情况）
        int columnCircleCount = columnIndex / 26;
        // 26一循环内的位置（取余数）
        int thisCircleColumnIndex = columnIndex % 26;
        // 26一循环的次数大于0，则视为栏名至少两位（如AA、AB等）
        String columnPrefix = columnCircleCount == 0
            // 如果小于26，前缀为空
            ? StrUtil.EMPTY
            // 否则从EXCEL_COLUMN_NAME中取前缀（如A、B等）
            : StrUtil.subWithLength(EXCEL_COLUMN_NAME, columnCircleCount - 1, 1);
        // 从26一循环内取对应的栏位名（A-Z）
        String columnNext = StrUtil.subWithLength(EXCEL_COLUMN_NAME, thisCircleColumnIndex, 1);
        // 将二者拼接即为最终的栏位名（如AA、AB等）
        return columnPrefix + columnNext;
    }
}
