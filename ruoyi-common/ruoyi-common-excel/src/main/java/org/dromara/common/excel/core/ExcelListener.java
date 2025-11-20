// 包声明：定义Excel监听器接口所在的包路径
package org.dromara.common.excel.core;

// 导入EasyExcel的读取监听器接口，提供Excel读取事件回调能力
import cn.idev.excel.read.listener.ReadListener;

/**
 * Excel 导入监听接口
 * 继承EasyExcel的ReadListener接口，扩展获取导入结果的能力
 * 用于处理Excel文件导入过程中的数据读取、验证和结果收集
 *
 * @author Lion Li
 * @param <T> Excel数据对应的实体类型
 */
public interface ExcelListener<T> extends ReadListener<T> {

    /**
     * 获取Excel导入结果
     * @return ExcelResult对象，包含成功导入的数据列表和错误信息列表
     */
    ExcelResult<T> getExcelResult();

}
