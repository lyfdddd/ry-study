// 包声明：定义默认Excel结果类所在的包路径
package org.dromara.common.excel.core;

// 导入Hutool字符串工具类，用于格式化导入回执消息
import cn.hutool.core.util.StrUtil;
// 导入Lombok Setter注解，自动生成setter方法
import lombok.Setter;

// 导入ArrayList，用于存储导入结果数据
import java.util.ArrayList;
// 导入List接口，定义集合操作规范
import java.util.List;

/**
 * 默认excel返回对象
 * 实现ExcelResult接口，封装Excel导入操作的结果数据
 * 包括成功导入的数据列表和错误信息列表
 *
 * @author Yjoioooo
 * @author Lion Li
 */
// 泛型类T表示导入的数据类型
public class DefaultExcelResult<T> implements ExcelResult<T> {

    /**
     * 数据对象list
     * 存储成功导入的数据对象列表
     */
    // 使用Lombok自动生成setter方法
    @Setter
    private List<T> list;

    /**
     * 错误信息列表
     * 存储导入过程中产生的错误信息
     */
    // 使用Lombok自动生成setter方法
    @Setter
    private List<String> errorList;

    /**
     * 无参构造函数
     * 初始化空的数据列表和错误列表
     */
    public DefaultExcelResult() {
        // 初始化数据列表为空ArrayList
        this.list = new ArrayList<>();
        // 初始化错误列表为空ArrayList
        this.errorList = new ArrayList<>();
    }

    /**
     * 全参构造函数
     * @param list 成功导入的数据列表
     * @param errorList 错误信息列表
     */
    public DefaultExcelResult(List<T> list, List<String> errorList) {
        // 设置数据列表
        this.list = list;
        // 设置错误列表
        this.errorList = errorList;
    }

    /**
     * 从另一个ExcelResult对象复制数据
     * @param excelResult 源ExcelResult对象
     */
    public DefaultExcelResult(ExcelResult<T> excelResult) {
        // 从源对象获取数据列表
        this.list = excelResult.getList();
        // 从源对象获取错误列表
        this.errorList = excelResult.getErrorList();
    }

    /**
     * 获取成功导入的数据列表
     * @return 数据对象列表
     */
    @Override
    public List<T> getList() {
        return list;
    }

    /**
     * 获取错误信息列表
     * @return 错误信息列表
     */
    @Override
    public List<String> getErrorList() {
        return errorList;
    }

    /**
     * 获取导入回执
     * 根据成功和错误数量生成友好的导入结果提示
     *
     * @return 导入回执消息
     */
    @Override
    public String getAnalysis() {
        // 获取成功导入的数据数量
        int successCount = list.size();
        // 获取错误信息数量
        int errorCount = errorList.size();
        // 如果没有成功数据
        if (successCount == 0) {
            // 返回读取失败提示
            return "读取失败，未解析到数据";
        } else {
            // 如果有成功数据
            // 如果没有错误信息
            if (errorCount == 0) {
                // 返回全部成功提示
                return StrUtil.format("恭喜您，全部读取成功！共{}条", successCount);
            } else {
                // 如果有错误信息，返回空字符串（由调用方处理）
                return "";
            }
        }
    }
}
