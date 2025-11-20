// 包声明：定义默认Excel监听器类所在的包路径
package org.dromara.common.excel.core;

// 导入Hutool字符串工具类，用于格式化错误消息
import cn.hutool.core.util.StrUtil;
// 导入EasyExcel分析上下文，提供Excel解析过程中的上下文信息
import cn.idev.excel.context.AnalysisContext;
// 导入EasyExcel事件监听器基类，提供Excel读取事件回调
import cn.idev.excel.event.AnalysisEventListener;
// 导入EasyExcel分析异常，用于包装Excel解析过程中的异常
import cn.idev.excel.exception.ExcelAnalysisException;
// 导入EasyExcel数据转换异常，用于处理单元格数据转换失败
import cn.idev.excel.exception.ExcelDataConvertException;
// 导入Stream工具类，用于集合流式操作
import org.dromara.common.core.utils.StreamUtils;
// 导入验证器工具类，用于Bean验证
import org.dromara.common.core.utils.ValidatorUtils;
// 导入JSON工具类，用于将对象序列化为JSON字符串
import org.dromara.common.json.utils.JsonUtils;
// 导入Jakarta验证约束违反对象，表示验证失败的具体信息
import jakarta.validation.ConstraintViolation;
// 导入Jakarta验证约束违反异常，表示Bean验证失败
import jakarta.validation.ConstraintViolationException;
// 导入Lombok无参构造函数注解
import lombok.NoArgsConstructor;
// 导入Lombok Slf4j日志注解
import lombok.extern.slf4j.Slf4j;

// 导入Map接口，用于存储表头数据
import java.util.Map;
// 导入Set接口，用于存储约束违反集合
import java.util.Set;

/**
 * Excel 导入监听器
 * 继承EasyExcel的AnalysisEventListener，实现ExcelListener接口
 * 负责监听Excel导入过程中的各种事件，处理数据读取、验证和异常
 *
 * @author Yjoioooo
 * @author Lion Li
 * @param <T> Excel数据对应的实体类型
 */
// 使用Lombok自动生成日志对象
@Slf4j
// 使用Lombok自动生成无参构造函数
@NoArgsConstructor
// 泛型类T表示Excel数据实体类型
public class DefaultExcelListener<T> extends AnalysisEventListener<T> implements ExcelListener<T> {

    /**
     * 是否Validator检验，默认为是
     * 控制是否对导入的数据进行Bean验证
     */
    private Boolean isValidate = Boolean.TRUE;

    /**
     * excel 表头数据
     * 存储Excel表头信息，key为列索引，value为表头名称
     */
    private Map<Integer, String> headMap;

    /**
     * 导入回执
     * 封装导入结果，包括成功数据和错误信息
     */
    private ExcelResult<T> excelResult;

    /**
     * 带验证参数的构造函数
     * @param isValidate 是否启用数据验证
     */
    public DefaultExcelListener(boolean isValidate) {
        // 创建默认的Excel结果对象
        this.excelResult = new DefaultExcelResult<>();
        // 设置是否启用验证
        this.isValidate = isValidate;
    }

    /**
     * 处理异常
     * 捕获Excel解析过程中的异常，生成友好的错误提示
     *
     * @param exception ExcelDataConvertException 数据转换异常
     * @param context   Excel 上下文，提供当前解析位置信息
     */
    @Override
    public void onException(Exception exception, AnalysisContext context) throws Exception {
        // 初始化错误消息变量
        String errMsg = null;
        // 判断是否为Excel数据转换异常
        if (exception instanceof ExcelDataConvertException excelDataConvertException) {
            // 如果是某一个单元格的转换异常 能获取到具体行号
            // 获取异常发生的行索引（从0开始）
            Integer rowIndex = excelDataConvertException.getRowIndex();
            // 获取异常发生的列索引（从0开始）
            Integer columnIndex = excelDataConvertException.getColumnIndex();
            // 格式化错误消息，显示行号、列号和表头名称
            errMsg = StrUtil.format("第{}行-第{}列-表头{}: 解析异常<br/>",
                rowIndex + 1, columnIndex + 1, headMap.get(columnIndex));
            // 如果开启debug日志，记录详细错误信息
            if (log.isDebugEnabled()) {
                log.error(errMsg);
            }
        }
        // 判断是否为Bean验证约束违反异常
        if (exception instanceof ConstraintViolationException constraintViolationException) {
            // 获取所有约束违反集合
            Set<ConstraintViolation<?>> constraintViolations = constraintViolationException.getConstraintViolations();
            // 使用Stream工具类将约束违反消息拼接为逗号分隔的字符串
            String constraintViolationsMsg = StreamUtils.join(constraintViolations, ConstraintViolation::getMessage, ", ");
            // 格式化错误消息，显示行号和具体验证错误
            errMsg = StrUtil.format("第{}行数据校验异常: {}", context.readRowHolder().getRowIndex() + 1, constraintViolationsMsg);
            // 如果开启debug日志，记录详细错误信息
            if (log.isDebugEnabled()) {
                log.error(errMsg);
            }
        }
        // 将错误消息添加到结果对象的错误列表中
        excelResult.getErrorList().add(errMsg);
        // 抛出Excel分析异常，中断导入流程
        throw new ExcelAnalysisException(errMsg);
    }

    /**
     * 解析表头
     * 当读取到Excel表头时触发，存储表头信息供后续使用
     *
     * @param headMap 表头数据，key为列索引，value为表头名称
     * @param context Excel上下文
     */
    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        // 存储表头数据到成员变量
        this.headMap = headMap;
        // 记录debug日志，显示解析到的表头数据
        log.debug("解析到一条表头数据: {}", JsonUtils.toJsonString(headMap));
    }

    /**
     * 读取数据行
     * 当读取到Excel数据行时触发，进行数据验证和收集
     *
     * @param data 当前行的数据对象
     * @param context Excel上下文
     */
    @Override
    public void invoke(T data, AnalysisContext context) {
        // 如果启用验证
        if (isValidate) {
            // 使用ValidatorUtils进行Bean验证，抛出ConstraintViolationException
            ValidatorUtils.validate(data);
        }
        // 将验证通过的数据添加到结果对象的数据列表中
        excelResult.getList().add(data);
    }

    /**
     * 所有数据解析完成
     * 当Excel所有数据读取完成后触发
     *
     * @param context Excel上下文
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // 记录debug日志，表示所有数据解析完成
        log.debug("所有数据解析完成！");
    }

    /**
     * 获取Excel导入结果
     * @return ExcelResult对象，包含成功导入的数据和错误信息
     */
    @Override
    public ExcelResult<T> getExcelResult() {
        return excelResult;
    }

}
