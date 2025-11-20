// 定义写出订阅器接口的包路径
package org.dromara.common.oss.core;

// Java IO异常类
import java.io.IOException;

/**
 * 写出订阅器函数式接口
 * 用于OSS文件下载时的异步写出操作
 * 配合Java NIO的发布-订阅模式实现流式下载
 *
 * @author 秋辞未寒
 */
// 函数式接口注解，表明这是一个函数式接口，可用于Lambda表达式
@FunctionalInterface
// 泛型接口，T表示输出目标类型
public interface WriteOutSubscriber<T> {

    /**
     * 将数据写出到指定目标
     *
     * @param out 输出目标
     * @throws IOException IO异常
     */
    // 抽象方法，接收泛型参数out，抛出IO异常
    void writeTo(T out) throws IOException;

}
