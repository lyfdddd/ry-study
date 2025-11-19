package org.dromara.common.core.exception.file;

import java.io.Serial;

/**
 * 文件大小限制异常类
 * 当上传的文件大小超过系统限制时抛出此异常
 * 继承自FileException，指定错误码为"upload.exceed.maxSize"
 *
 * @author ruoyi
 */
public class FileSizeLimitExceededException extends FileException {

    /**
     * 序列化版本号
     * 用于反序列化时验证版本一致性
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 构造方法
     * 调用父类FileException的构造方法，指定错误码和参数
     *
     * @param defaultMaxSize 系统允许的最大文件大小（字节）
     */
    public FileSizeLimitExceededException(long defaultMaxSize) {
        // 调用父类构造方法，错误码为"upload.exceed.maxSize"，参数为最大大小
        // 国际化消息示例：上传文件大小不能超过{0}字节
        super("upload.exceed.maxSize", new Object[]{defaultMaxSize});
    }
}
