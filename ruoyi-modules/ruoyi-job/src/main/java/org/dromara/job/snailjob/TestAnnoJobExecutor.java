// 包声明：定义当前类所在的包路径，org.dromara.job.snailjob 表示任务调度模块SnailJob任务包
// package关键字用于声明类所在的包，便于组织和管理代码，避免命名冲突
package org.dromara.job.snailjob;

// SnailJob任务执行器注解：标记方法为SnailJob任务执行器
import com.aizuda.snailjob.client.job.core.annotation.JobExecutor;
// SnailJob任务参数对象：封装任务执行参数
import com.aizuda.snailjob.client.job.core.dto.JobArgs;
// SnailJob JSON工具类：提供JSON序列化功能
import com.aizuda.snailjob.common.core.util.JsonUtil;
// SnailJob日志类：提供本地和远程日志记录功能
import com.aizuda.snailjob.common.log.SnailJobLog;
// SnailJob执行结果类：封装任务执行结果
import com.aizuda.snailjob.model.dto.ExecuteResult;
// Spring组件注解：将类注册为Spring Bean
import org.springframework.stereotype.Component;

/**
 * 正常任务执行器
 * 这是一个SnailJob分布式任务调度框架的任务执行器
 * 用于演示正常的任务执行流程，记录任务参数到日志
 * <a href="https://juejin.cn/post/7418074037392293914">参考文档</a>
 *
 * @author 老马
 */
// Spring组件注解：将类注册为Spring Bean，由Spring容器管理
@Component
// SnailJob任务执行器注解：标记此类为SnailJob任务执行器，name指定任务名称
@JobExecutor(name = "testJobExecutor")
public class TestAnnoJobExecutor {

    /**
     * 任务执行方法
     * 由SnailJob框架调用，执行具体的业务逻辑
     * 记录任务参数到本地日志和远程日志
     * @param jobArgs 任务参数对象，包含工作流上下文、任务参数等信息
     * @return ExecuteResult 任务执行结果，包含成功/失败状态和数据
     */
    public ExecuteResult jobExecute(JobArgs jobArgs) {
        // 记录本地日志，输出任务参数的JSON字符串
        // LOCAL日志存储在应用本地，用于开发调试
        SnailJobLog.LOCAL.info("testJobExecutor. jobArgs:{}", JsonUtil.toJsonString(jobArgs));
        
        // 记录远程日志，输出任务参数的JSON字符串
        // REMOTE日志上传到SnailJob服务端，用于生产环境监控
        SnailJobLog.REMOTE.info("testJobExecutor. jobArgs:{}", JsonUtil.toJsonString(jobArgs));
        
        // 返回执行成功结果，携带"测试成功"消息
        return ExecuteResult.success("测试成功");
    }
}
