// 定义OSS异常类的包路径
package org.dromara.common.oss.exception;

// Java序列化接口
import java.io.Serial;

/**
 * OSS自定义运行时异常类
 * 用于封装OSS操作过程中发生的异常
 * 继承RuntimeException，无需强制捕获
 *
 * @author Lion Li
 */
// OSS异常类，继承RuntimeException
public class OssException extends RuntimeException {

    // 序列化版本UID，用于反序列化时验证版本一致性
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 构造方法
     *
     * @param msg 异常信息
     */
    // 调用父类构造方法，传入异常信息
    public OssException(String msg) {
        super(msg);
    }

}
