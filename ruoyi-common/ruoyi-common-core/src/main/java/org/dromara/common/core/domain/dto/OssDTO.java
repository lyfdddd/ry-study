package org.dromara.common.core.domain.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * OSS对象存储数据传输对象（DTO）
 * 用于在不同层之间传输OSS文件信息，通常用于服务层之间的数据传递
 * 实现Serializable接口，支持序列化
 * 使用Lombok注解简化代码
 *
 * @author Lion Li
 */
// Lombok注解：自动生成getter、setter、toString、equals、hashCode方法
@Data
// Lombok注解：生成无参构造函数
@NoArgsConstructor
public class OssDTO implements Serializable {

    /**
     * 序列化版本号
     * 用于反序列化时验证版本一致性
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 对象存储主键
     * 唯一标识一个OSS文件记录
     */
    private Long ossId;

    /**
     * 文件名
     * 存储在OSS中的文件名
     */
    private String fileName;

    /**
     * 原始文件名
     * 用户上传时的原始文件名
     */
    private String originalName;

    /**
     * 文件后缀名
     * 文件的扩展名，如：.jpg、.pdf等
     */
    private String fileSuffix;

    /**
     * URL地址
     * 文件在OSS中的访问地址
     */
    private String url;

}
