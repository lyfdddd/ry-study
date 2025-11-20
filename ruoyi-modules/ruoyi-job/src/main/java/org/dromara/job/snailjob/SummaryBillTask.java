// 包声明：定义当前类所在的包路径，org.dromara.job.snailjob 表示任务调度模块SnailJob任务包
// package关键字用于声明类所在的包，便于组织和管理代码，避免命名冲突
package org.dromara.job.snailjob;

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
 * DAG工作流任务-模拟汇总账单任务
 * 这是一个SnailJob分布式任务调度框架的任务执行器
 * 用于汇总微信和支付宝账单数据，计算总金额
 * <a href="https://juejin.cn/post/7487860254114644019">参考文档</a>
 *
 * @author 老马
 */
// Spring组件注解：将类注册为Spring Bean，由Spring容器管理
@Component
// SnailJob任务执行器注解：标记此类为SnailJob任务执行器，name指定任务名称
@JobExecutor(name = "summaryBillTask")
public class SummaryBillTask {

    /**
     * 任务执行方法
     * 由SnailJob框架调用，执行具体的业务逻辑
     * 从工作流上下文中获取微信和支付宝账单，计算总金额
     * @param jobArgs 任务参数对象，包含工作流上下文、任务参数等信息
     * @return ExecuteResult 任务执行结果，包含成功/失败状态和数据
     * @throws InterruptedException 线程中断异常
     */
    public ExecuteResult jobExecute(JobArgs jobArgs) throws InterruptedException {
        // 初始化微信账单金额，默认值为0
        BigDecimal wechatAmount = BigDecimal.valueOf(0);
        
        // 从工作流上下文中获取微信账单JSON字符串
        String wechat = (String) jobArgs.getWfContext("wechat");
        // 判断微信账单字符串是否不为空
        if (StrUtil.isNotBlank(wechat)) {
            // 将JSON字符串反序列化为BillDto对象
            BillDto wechatBillDto = JsonUtils.parseObject(wechat, BillDto.class);
            // 获取微信账单金额
            wechatAmount = wechatBillDto.getBillAmount();
        }
        
        // 初始化支付宝账单金额，默认值为0
        BigDecimal alipayAmount = BigDecimal.valueOf(0);
        
        // 从工作流上下文中获取支付宝账单JSON字符串
        String alipay = (String) jobArgs.getWfContext("alipay");
        // 判断支付宝账单字符串是否不为空
        if (StrUtil.isNotBlank(alipay)) {
            // 将JSON字符串反序列化为BillDto对象
            BillDto alipayBillDto = JsonUtils.parseObject(alipay, BillDto.class);
            // 获取支付宝账单金额
            alipayAmount = alipayBillDto.getBillAmount();
        }
        
        // 汇总账单：将微信账单金额和支付宝账单金额相加
        BigDecimal totalAmount = wechatAmount.add(alipayAmount);
        
        // 记录远程日志，输出总金额
        SnailJobLog.REMOTE.info("总金额: {}", totalAmount);
        
        // 返回执行成功结果，携带总金额数据
        return ExecuteResult.success(totalAmount);
    }

}
