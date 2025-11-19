package org.dromara.common.core.exception.file;

import java.io.Serial;

/**
 * 文件名称超长限制异常类
 * 当上传的文件名称长度超过系统限制时抛出此异常
 * 继承自FileException，指定错误码为"upload.filename.exceed.length"
 *
 * @author ruoyi
 */
public class FileNameLengthLimitExceededException extends FileException {

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
     * @param defaultFileNameLength 系统允许的最大文件名称长度
     */
    public FileNameLengthLimitExceededException(int defaultFileNameLength) {
        // 调用父类构造方法，错误码为"upload.filename.exceed.length"，参数为最大长度
        // 国际化消息示例：上传文件名长度不能超过{0}个字符
        super("upload.filename.exceed.length", new Object[]{defaultFileNameLength});
    }
}
