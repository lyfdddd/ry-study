package org.dromara.generator.config;

// 导入Spring框架的Value注解，用于注入配置文件中的属性值
import org.springframework.beans.factory.annotation.Value;
// 导入Spring Boot的配置属性注解，用于批量绑定配置文件中的属性
import org.springframework.boot.context.properties.ConfigurationProperties;
// 导入Spring的PropertySource注解，用于指定配置文件位置
import org.springframework.context.annotation.PropertySource;
// 导入Spring的Component注解，标记为Spring组件
import org.springframework.stereotype.Component;

/**
 * 读取代码生成相关配置
 * 该类用于从配置文件中读取代码生成器的全局配置参数
 * 使用静态变量存储配置，方便在代码生成工具类中直接访问
 *
 * @author ruoyi
 */
@Component
// 指定配置前缀为gen，会自动绑定配置文件中gen.*的属性
@ConfigurationProperties(prefix = "gen")
// 指定配置文件位置和编码格式为UTF-8，避免中文乱码
@PropertySource(value = {"classpath:generator.yml"}, encoding = "UTF-8")
public class GenConfig {

    /**
     * 作者
     * 代码生成时的作者信息，会写入类注释中
     */
    // 静态变量，存储代码生成时的作者信息，从配置文件读取
    public static String author;

    /**
     * 生成包路径
     * 生成的Java代码包路径，如org.dromara.system
     */
    // 静态变量，存储生成的Java代码包路径，如org.dromara.system
    public static String packageName;

    /**
     * 自动去除表前缀，默认是false
     * 控制是否自动去除表前缀，如sys_user去除sys_前缀
     */
    // 静态变量，控制是否自动去除表前缀，如sys_user去除sys_前缀
    public static boolean autoRemovePre;

    /**
     * 表前缀(类名不会包含表前缀)
     * 需要去除的表前缀字符串，多个前缀用逗号分隔
     */
    // 静态变量，存储需要去除的表前缀字符串，多个前缀用逗号分隔
    public static String tablePrefix;

    /**
     * 获取作者信息的方法
     * @return 作者名称
     */
    public static String getAuthor() {
        return author;
    }

    /**
     * 设置作者信息
     * Spring的Value注解，从配置文件中读取author属性值
     * @param author 作者名称
     */
    @Value("${author}")
    public void setAuthor(String author) {
        // 将配置的值设置到静态变量中，供全局使用
        GenConfig.author = author;
    }

    /**
     * 获取生成包路径的方法
     * @return 包路径
     */
    public static String getPackageName() {
        return packageName;
    }

    /**
     * 设置生成包路径
     * Spring的Value注解，从配置文件中读取packageName属性值
     * @param packageName 包路径
     */
    @Value("${packageName}")
    public void setPackageName(String packageName) {
        // 将配置的值设置到静态变量中，供代码生成时使用
        GenConfig.packageName = packageName;
    }

    /**
     * 获取是否自动去除表前缀的配置
     * @return true表示自动去除，false表示保留表前缀
     */
    public static boolean getAutoRemovePre() {
        return autoRemovePre;
    }

    /**
     * 设置是否自动去除表前缀
     * Spring的Value注解，从配置文件中读取autoRemovePre属性值
     * @param autoRemovePre 是否自动去除表前缀
     */
    @Value("${autoRemovePre}")
    public void setAutoRemovePre(boolean autoRemovePre) {
        // 将配置的值设置到静态变量中，控制表前缀处理逻辑
        GenConfig.autoRemovePre = autoRemovePre;
    }

    /**
     * 获取表前缀配置的方法
     * @return 表前缀字符串
     */
    public static String getTablePrefix() {
        return tablePrefix;
    }

    /**
     * 设置表前缀配置
     * Spring的Value注解，从配置文件中读取tablePrefix属性值
     * @param tablePrefix 表前缀字符串
     */
    @Value("${tablePrefix}")
    public void setTablePrefix(String tablePrefix) {
        // 将配置的值设置到静态变量中，指定需要去除的表前缀
        GenConfig.tablePrefix = tablePrefix;
    }
}
