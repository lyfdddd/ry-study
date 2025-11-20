// 包声明：定义Excel下拉选项类所在的包路径
package org.dromara.common.excel.core;

// 导入Hutool类型转换工具类，用于将对象转换为字符串
import cn.hutool.core.convert.Convert;
// 导入Hutool字符串工具类，用于字符串操作
import cn.hutool.core.util.StrUtil;
// 导入Lombok全参构造函数注解
import lombok.AllArgsConstructor;
// 导入Lombok Data注解，自动生成getter、setter、toString等方法
import lombok.Data;
// 导入Lombok无参构造函数注解
import lombok.NoArgsConstructor;
// 导入服务异常类，用于抛出业务异常
import org.dromara.common.core.exception.ServiceException;

// 导入ArrayList，用于存储下拉选项数据
import java.util.ArrayList;
// 导入HashMap，用于存储二级下拉选项映射
import java.util.HashMap;
// 导入List接口，定义列表操作规范
import java.util.List;
// 导入Map接口，定义映射操作规范
import java.util.Map;
// 导入Function函数式接口，用于数据转换
import java.util.function.Function;
// 导入Collectors收集器，用于Stream流收集结果
import java.util.stream.Collectors;

/**
 * <h1>Excel下拉可选项</h1>
 * 封装Excel下拉框的配置信息，支持单级和级联下拉
 * 注意：为确保下拉框解析正确，传值务必使用createOptionValue()做为值的拼接
 *
 * @author Emil.Zhang
 */
// 使用Lombok自动生成getter、setter、toString、equals、hashCode方法
@Data
// 使用Lombok自动生成全参构造函数
@AllArgsConstructor
// 使用Lombok自动生成无参构造函数
@NoArgsConstructor
// 压制未使用警告，因为部分方法可能通过反射调用
@SuppressWarnings("unused")
// 泛型类，用于构建级联下拉选项
public class DropDownOptions {
    /**
     * 一级下拉所在列index，从0开始算
     * 例如第A列对应index为0，第B列对应index为1
     */
    private int index = 0;
    /**
     * 二级下拉所在的index，从0开始算，不能与一级相同
     * 用于级联下拉，表示子下拉框所在的列位置
     */
    private int nextIndex = 0;
    /**
     * 一级下拉所包含的数据
     * 存储一级下拉框的所有可选项
     */
    private List<String> options = new ArrayList<>();
    /**
     * 二级下拉所包含的数据Map
     * <p>以每一个一级选项值为Key，每个一级选项对应的二级数据为Value</p>
     * 实现级联效果，当选择一级选项时，二级下拉框显示对应的子选项
     */
    private Map<String, List<String>> nextOptions = new HashMap<>();
    /**
     * 分隔符
     * 用于拼接多个参数生成合规的下拉选项值
     */
    private static final String DELIMITER = "_";

    /**
     * 创建只有一级的下拉选
     * 简化构造函数，只设置一级下拉
     * @param index 一级下拉所在列索引
     * @param options 一级下拉选项列表
     */
    public DropDownOptions(int index, List<String> options) {
        // 设置一级下拉列索引
        this.index = index;
        // 设置一级下拉选项数据
        this.options = options;
    }

    /**
     * <h2>创建每个选项可选值</h2>
     * 将多个参数拼接为合规的下拉选项值
     * <p>注意：不能以数字，特殊符号开头，选项中不可以包含任何运算符号</p>
     * 这是Excel下拉框的格式要求，避免Excel解析错误
     *
     * @param vars 可选值内包含的参数，可变参数
     * @return 合规的可选值字符串
     */
    public static String createOptionValue(Object... vars) {
        // 创建字符串构建器，用于拼接参数
        StringBuilder stringBuffer = new StringBuilder();
        // 定义正则表达式：只允许非空白字符、数字和中文字符
        String regex = "^[\\S\\d\\u4e00-\\u9fa5]+$";
        // 遍历所有参数
        for (int i = 0; i < vars.length; i++) {
            // 将参数转换为字符串并去除首尾空白
            String var = StrUtil.trimToEmpty(Convert.toStr(vars[i]));
            // 如果参数不符合正则表达式规则
            if (!var.matches(regex)) {
                // 抛出服务异常，提示选项数据不符合规则
                throw new ServiceException("选项数据不符合规则，仅允许使用中英文字符以及数字");
            }
            // 将合规的参数追加到字符串构建器
            stringBuffer.append(var);
            // 如果不是最后一个参数，添加分隔符
            if (i < vars.length - 1) {
                // 直至最后一个前，都以_作为切割线
                stringBuffer.append(DELIMITER);
            }
        }
        // 检查最终结果是否以数字开头（Excel不允许下拉选项以数字开头）
        if (stringBuffer.toString().matches("^\\d_*$")) {
            // 如果以数字开头，抛出服务异常
            throw new ServiceException("禁止以数字开头");
        }
        // 返回拼接完成的合规选项值
        return stringBuffer.toString();
    }

    /**
     * 将处理后合理的可选值解析为原始的参数
     * 与createOptionValue方法相反，将拼接的字符串拆分为原始参数列表
     *
     * @param option 经过处理后的合理的可选项
     * @return 原始的参数列表
     */
    public static List<String> analyzeOptionValue(String option) {
        // 使用分隔符拆分字符串，去除空白，返回参数列表
        return StrUtil.split(option, DELIMITER, true, true);
    }

    /**
     * 创建级联下拉选项
     * 根据父子关系数据构建级联下拉框配置
     *
     * @param parentList                  父实体可选项原始数据列表
     * @param parentIndex                 父下拉选所在列索引
     * @param sonList                     子实体可选项原始数据列表
     * @param sonIndex                    子下拉选所在列索引
     * @param parentHowToGetIdFunction    父类如何获取唯一标识的函数
     * @param sonHowToGetParentIdFunction 子类如何获取父类的唯一标识的函数
     * @param howToBuildEveryOption       如何生成下拉选内容的函数
     * @param <T> 实体类型
     * @return 级联下拉选项配置对象
     */
    public static <T> DropDownOptions buildLinkedOptions(List<T> parentList,
                                                         int parentIndex,
                                                         List<T> sonList,
                                                         int sonIndex,
                                                         Function<T, Number> parentHowToGetIdFunction,
                                                         Function<T, Number> sonHowToGetParentIdFunction,
                                                         Function<T, String> howToBuildEveryOption) {
        // 创建级联下拉选项对象
        DropDownOptions parentLinkSonOptions = new DropDownOptions();
        // 先创建父类的下拉
        parentLinkSonOptions.setIndex(parentIndex);
        // 使用Stream流将父列表转换为下拉选项列表
        parentLinkSonOptions.setOptions(
            parentList.stream()
                .map(howToBuildEveryOption)
                .collect(Collectors.toList())
        );
        // 提取父-子级联下拉
        Map<String, List<String>> sonOptions = new HashMap<>();
        // 父级依据自己的ID分组，使用Stream的groupingBy收集器
        Map<Number, List<T>> parentGroupByIdMap =
            parentList.stream().collect(Collectors.groupingBy(parentHowToGetIdFunction));
        // 遍历每个子集，提取到Map中
        sonList.forEach(everySon -> {
            // 如果子项的父ID在父分组Map中存在
            if (parentGroupByIdMap.containsKey(sonHowToGetParentIdFunction.apply(everySon))) {
                // 找到对应的上级（取第一个）
                T parentObj = parentGroupByIdMap.get(sonHowToGetParentIdFunction.apply(everySon)).get(0);
                // 提取名称和ID作为Key
                String key = howToBuildEveryOption.apply(parentObj);
                // Key对应的Value
                List<String> thisParentSonOptionList;
                // 如果Map中已存在该Key
                if (sonOptions.containsKey(key)) {
                    // 获取已存在的列表
                    thisParentSonOptionList = sonOptions.get(key);
                } else {
                    // 否则创建新列表
                    thisParentSonOptionList = new ArrayList<>();
                    // 将新列表放入Map
                    sonOptions.put(key, thisParentSonOptionList);
                }
                // 往Value中添加当前子集选项
                thisParentSonOptionList.add(howToBuildEveryOption.apply(everySon));
            }
        });
        // 设置二级下拉列索引
        parentLinkSonOptions.setNextIndex(sonIndex);
        // 设置二级下拉选项映射
        parentLinkSonOptions.setNextOptions(sonOptions);
        // 返回级联下拉选项配置
        return parentLinkSonOptions;
    }
}
