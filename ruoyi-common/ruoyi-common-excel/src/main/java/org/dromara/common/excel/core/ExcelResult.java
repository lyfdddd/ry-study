// 包声明：定义Excel结果接口所在的包路径
package org.dromara.common.excel.core;

// 导入List接口，定义列表操作规范
import java.util.List;

/**
 * Excel导入结果接口
 * 定义Excel导入操作返回的标准格式，包含成功数据、错误信息和导入回执
 * 实现类需要封装导入过程中的所有结果信息
 *
 * @author Lion Li
 * @param <T> Excel数据对应的实体类型
 */
public interface ExcelResult<T> {

    /**
     * 获取成功导入的对象列表
     * @return 成功导入的数据对象列表
     */
    List<T> getList();

    /**
     * 获取错误信息列表
     * @return 导入过程中产生的错误信息列表，每条错误信息包含行号、列号和具体错误原因
     */
    List<String> getErrorList();

    /**
     * 获取导入回执信息
     * @return 导入结果摘要，如"恭喜您，全部读取成功！共100条"或"读取失败，未解析到数据"
     */
    String getAnalysis();
}
