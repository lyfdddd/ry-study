package org.dromara.generator.util;

// 导入常量类
import org.dromara.common.core.constant.Constants;
// 导入Lombok的访问级别枚举
import lombok.AccessLevel;
// 导入Lombok的无参构造函数注解，并设置访问级别为PRIVATE，防止实例化
import lombok.NoArgsConstructor;
// 导入Velocity引擎类
import org.apache.velocity.app.Velocity;

import java.util.Properties;

/**
 * VelocityEngine工厂
 * 提供Velocity模板引擎的初始化功能
 * 使用私有构造函数防止实例化，符合工具类设计模式
 *
 * @author ruoyi
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class VelocityInitializer {

    /**
     * 初始化vm方法
     * 配置Velocity模板引擎的基本属性，包括资源加载器和字符编码
     * 使用ClasspathResourceLoader从classpath加载模板文件
     */
    public static void initVelocity() {
        // 创建Properties对象，用于存储Velocity配置
        Properties p = new Properties();
        try {
            // 加载classpath目录下的vm文件
            // 设置资源加载器为ClasspathResourceLoader，从classpath中加载模板文件
            p.setProperty("resource.loader.file.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
            // 定义字符集为UTF-8，确保中文不乱码
            p.setProperty(Velocity.INPUT_ENCODING, Constants.UTF8);
            // 初始化Velocity引擎，指定配置Properties
            Velocity.init(p);
        } catch (Exception e) {
            // 如果初始化失败，抛出运行时异常
            throw new RuntimeException(e);
        }
    }

}
