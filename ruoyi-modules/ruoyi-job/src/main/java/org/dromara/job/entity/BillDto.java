// 包声明：定义当前类所在的包路径，org.dromara.job.entity 表示任务调度模块实体层
// package关键字用于声明类所在的包，便于组织和管理代码，避免命名冲突
package org.dromara.job.entity;

// Lombok注解：自动生成getter、setter、toString、equals、hashCode等方法
import lombok.Data;

// Java数学类：BigDecimal用于精确计算
import java.math.BigDecimal;

/**
 * 账单数据传输对象
 * 用于封装账单信息，在任务调度模块中传递账单数据
 * 包含账单ID、渠道、日期和金额等基本信息
 * 使用Lombok的@Data注解简化代码
 */
// Lombok注解：自动生成getter、setter、toString、equals、hashCode等方法
@Data
public class BillDto {

    /**
     * 账单ID
     * 账单的唯一标识
     */
    private Long billId;

    /**
     * 账单渠道
     * 账单的来源渠道，如"alipay"、"wechat"等
     */
    private String billChannel;

    /**
     * 账单日期
     * 账单的日期，格式通常为yyyy-MM-dd
     */
    private String billDate;

    /**
     * 账单金额
     * 账单的金额，使用BigDecimal确保金额精度
     */
    private BigDecimal billAmount;

}
