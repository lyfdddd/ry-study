// 定义上传结果实体类的包路径
package org.dromara.common.oss.entity;

// Lombok注解，提供建造者模式构建对象
import lombok.Builder;
// Lombok注解，自动生成getter、setter、toString、equals、hashCode方法
import lombok.Data;

/**
 * OSS文件上传返回体实体类
 * 封装文件上传后的返回信息，包括文件URL、文件名、ETag等
 * 使用Lombok注解简化代码
 *
 * @author Lion Li
 */
// Lombok注解，自动生成getter、setter、toString、equals、hashCode方法
@Data
// Lombok注解，提供建造者模式构建对象
@Builder
// 上传结果实体类
public class UploadResult {

    /**
     * 文件访问URL路径
     * 上传成功后返回的文件完整访问地址
     */
    private String url;

    /**
     * 文件名
     * 上传时指定的文件名或系统自动生成的文件名
     */
    private String filename;

    /**
     * 已上传对象的实体标记（ETag）
     * 用于校验文件完整性，是对象存储系统返回的文件唯一标识
     */
    private String eTag;

}
