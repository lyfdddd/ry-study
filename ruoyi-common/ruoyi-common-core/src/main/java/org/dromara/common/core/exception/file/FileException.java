package org.dromara.common.core.exception.file;

import org.dromara.common.core.exception.base.BaseException;

import java.io.Serial;

/**
 * 文件信息异常基类
 * 所有文件相关异常的父类，继承自BaseException
 * 指定模块为"file"，用于文件上传、下载等场景
 *
 * @author ruoyi
 */
public class FileException extends BaseException {

    /**
     * 序列化版本号
     * 用于反序列化时验证版本一致性
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 构造方法
     * 调用父类BaseException的构造方法，指定模块为"file"
     *
     * @param code 错误码，对应国际化资源文件中的key
     * @param args 错误码参数，用于替换国际化消息中的占位符
     */
    public FileException(String code, Object[] args) {
        // 调用父类构造方法，模块为"file"，错误码和参数由子类传入，默认消息为null
        // 国际化消息会从messages.properties文件中查找，key为code的值
        super("file", code, args, null);
    }

}
