package org.dromara.common.excel.convert;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.idev.excel.converters.Converter;
import cn.idev.excel.enums.CellDataTypeEnum;
import cn.idev.excel.metadata.GlobalConfiguration;
import cn.idev.excel.metadata.data.ReadCellData;
import cn.idev.excel.metadata.data.WriteCellData;
import cn.idev.excel.metadata.property.ExcelContentProperty;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

/**
 * Excel大数值转换器
 * 处理Long类型的大数值，解决Excel数值精度问题
 * Excel数值长度限制为15位，大于15位的数值会转换为字符串类型导出，防止精度丢失
 *
 * @author Lion Li
 */
// @Slf4j注解自动生成日志记录器，用于记录转换过程中的异常信息
@Slf4j
public class ExcelBigNumberConvert implements Converter<Long> {

    /**
     * 指定支持的Java类型为Long.class
     * @return Long类型Class对象
     */
    @Override
    public Class<Long> supportJavaTypeKey() {
        return Long.class;
    }

    /**
     * 指定支持的Excel类型为null，表示不限制Excel单元格类型
     * @return null表示支持所有Excel单元格类型
     */
    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return null;
    }

    /**
     * 将Excel单元格数据转换为Java Long类型
     * @param cellData Excel单元格数据对象，包含单元格的值和类型信息
     * @param contentProperty Excel内容属性，包含字段的元数据信息
     * @param globalConfiguration 全局配置对象
     * @return 转换后的Long类型值
     */
    @Override
    public Long convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
        // 使用Hutool的Convert工具类将单元格数据转换为Long类型
        // 支持多种数据类型的安全转换，转换失败返回null
        return Convert.toLong(cellData.getData());
    }

    /**
     * 将Java Long类型数据转换为Excel单元格数据
     * @param object Java Long类型值
     * @param contentProperty Excel内容属性
     * @param globalConfiguration 全局配置对象
     * @return 转换后的WriteCellData对象
     */
    @Override
    public WriteCellData<Object> convertToExcelData(Long object, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
        // 判断Long值是否不为null
        if (ObjectUtil.isNotNull(object)) {
            // 将Long值转换为字符串
            String str = Convert.toStr(object);
            // 如果字符串长度大于15位（Excel数值精度限制）
            if (str.length() > 15) {
                // 直接返回字符串类型的WriteCellData，避免精度丢失
                return new WriteCellData<>(str);
            }
        }
        // 15位以内的数值正常转换为BigDecimal类型
        WriteCellData<Object> cellData = new WriteCellData<>(new BigDecimal(object));
        // 设置单元格类型为数值类型（NUMBER）
        cellData.setType(CellDataTypeEnum.NUMBER);
        return cellData;
    }

}
