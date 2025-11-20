// 包声明：定义当前类所在的包路径，org.dromara.job.snailjob 表示任务调度模块SnailJob任务包
// package关键字用于声明类所在的包，便于组织和管理代码，避免命名冲突
package org.dromara.job.snailjob;

// Hutool日期工具类：提供日期操作和格式化功能
import cn.hutool.core.date.DateUtil;
// Hutool字符串工具类：提供字符串判断和操作功能
import cn.hutool.core.util.StrUtil;
// SnailJob任务执行器注解：标记方法为SnailJob任务执行器
import com.aizuda.snailjob.client.job.core.annotation.JobExecutor;
// SnailJob任务参数对象：封装任务执行参数
import com.aizuda.snailjob.client.job.core.dto.JobArgs;
// SnailJob日志类：提供远程日志记录功能
import com.aizuda.snailjob.common.log.SnailJobLog;
// SnailJob执行结果类：封装任务执行结果
import com.aizuda.snailjob.model.dto.ExecuteResult;
// JSON工具类：提供JSON序列化和反序列化功能
import org.dromara.common.json.utils.JsonUtils;
// 账单数据传输对象：封装账单信息
import org.dromara.job.entity.BillDto;
// Spring组件注解：将类注册为Spring Bean
import org.springframework.stereotype.Component;

// Java数学类：BigDecimal用于精确计算
import java.math.BigDecimal;

/**
 * DAG工作流任务-模拟支付宝账单任务
 * 这是一个SnailJob分布式任务调度框架的任务执行器
 * 用于模拟生成支付宝账单数据，并将账单信息放入工作流上下文中传递
 * <a href="https://juejin.cn/post/7487860254114644019">参考文档</a>
 *
 * @author 老马
 */
// Spring组件注解：将类注册为Spring Bean，由Spring容器管理
@Component
// SnailJob任务执行器注解：标记此类为SnailJob任务执行器，name指定任务名称
@JobExecutor(name = "alipayBillTask")
public class AlipayBillTask {

    /**
     * 任务执行方法
     * 由SnailJob框架调用，执行具体的业务逻辑
     * @param jobArgs 任务参数对象，包含工作流上下文、任务参数等信息
     * @return ExecuteResult 任务执行结果，包含成功/失败状态和数据
     * @throws InterruptedException 线程中断异常
     */
    public ExecuteResult jobExecute(JobArgs jobArgs) throws InterruptedException {
        // 创建账单数据传输对象，用于封装支付宝账单信息
        BillDto billDto = new BillDto();
        // 设置账单ID，模拟支付宝账单的唯一标识
        billDto.setBillId(23456789L);
        // 设置账单渠道为"alipay"，表示支付宝渠道
        billDto.setBillChannel("alipay");
        
        // 设置清算日期
        // 从工作流上下文中获取settlementDate参数
        String settlementDate = (String) jobArgs.getWfContext().get("settlementDate");
        // 如果settlementDate等于"sysdate"，则使用当前系统日期
        if (StrUtil.equals(settlementDate, "sysdate")) {
            // 调用Hutool的DateUtil.today()获取当前日期，格式为yyyy-MM-dd
            settlementDate = DateUtil.today();
        }
        // 设置账单日期
        billDto.setBillDate(settlementDate);
        
        // 设置账单金额，使用BigDecimal确保金额精度
        billDto.setBillAmount(new BigDecimal("2345.67"));
        
        // 把billDto对象放入上下文进行传递
        // 将账单对象序列化为JSON字符串，存入工作流上下文的"alipay"键中
        jobArgs.appendContext("alipay", JsonUtils.toJsonString(billDto));
        
        // 记录远程日志，输出工作流上下文内容
        SnailJobLog.REMOTE.info("上下文: {}", jobArgs.getWfContext());
        
        // 返回执行成功结果，携带账单数据
        return ExecuteResult.success(billDto);
    }

}
