// 定义对象存储常量接口的包路径
package org.dromara.common.oss.constant;

// 引入全局常量类，用于构建Redis key前缀
import org.dromara.common.core.constant.GlobalConstants;

// Java数组工具类
import java.util.Arrays;
// Java List集合接口
import java.util.List;

/**
 * 对象存储常量接口
 * 定义OSS模块使用的所有常量，包括Redis key、系统数据ID、云服务商列表等
 * 使用接口形式定义常量，方便其他类直接引用
 *
 * @author Lion Li
 */
// 常量接口，所有字段默认为public static final
public interface OssConstant {

    /**
     * 默认OSS配置KEY
     * 存储在Redis中，标识当前系统使用的默认OSS配置
     * 格式：global:sys_oss:default_config
     */
    // 使用GlobalConstants.GLOBAL_REDIS_KEY作为前缀，确保Redis key全局唯一
    String DEFAULT_CONFIG_KEY = GlobalConstants.GLOBAL_REDIS_KEY + "sys_oss:default_config";

    /**
     * 预览列表资源开关配置Key
     * 用于控制是否开启OSS资源列表预览功能
     * 配置在application.yml中，如：sys.oss.previewListResource=true
     */
    String PEREVIEW_LIST_RESOURCE_KEY = "sys.oss.previewListResource";

    /**
     * 系统内置OSS配置ID列表
     * 这些ID对应数据库中的系统默认OSS配置，不允许用户删除
     * 包含阿里云、腾讯云、七牛云、华为云等默认配置
     */
    // 使用Arrays.asList创建不可变列表，包含系统默认的4个配置ID
    List<Long> SYSTEM_DATA_IDS = Arrays.asList(1L, 2L, 3L, 4L);

    /**
     * 支持的云服务商列表
     * 用于判断当前endpoint是否属于知名云服务商
     * 影响域名处理和路径风格配置
     */
    // 数组形式存储云服务商标识：aliyun(阿里云)、qcloud(腾讯云)、qiniu(七牛云)、obs(华为云)
    String[] CLOUD_SERVICE = new String[] {"aliyun", "qcloud", "qiniu", "obs"};

    /**
     * HTTPS启用状态标识
     * 配置文件中isHttps字段的值，Y表示启用HTTPS，N表示使用HTTP
     */
    String IS_HTTPS = "Y";

}
