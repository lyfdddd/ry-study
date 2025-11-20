package org.dromara.generator.config;

// 导入动态数据源相关类，用于支持多数据源切换
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
// 导入Lombok的日志注解，自动生成日志对象
import lombok.extern.slf4j.Slf4j;
// 导入Anyline的数据源监控接口，用于适配Anyline框架
import org.anyline.data.datasource.DataSourceMonitor;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.util.ConfigTable;
// 导入Spring的JdbcTemplate，用于执行SQL查询
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.HashMap;
import java.util.Map;

/**
 * anyline 适配 动态数据源改造
 * 该类实现了DataSourceMonitor接口，用于将MyBatis-Plus的动态数据源适配到Anyline框架
 * Anyline是一个轻量级的ORM框架，需要获取数据源的特征信息来生成SQL
 * 通过实现该接口，可以让Anyline正确识别MyBatis-Plus的动态数据源
 *
 * @author Lion Li
 */
@Slf4j
@Component
public class MyBatisDataSourceMonitor implements DataSourceMonitor {

    /**
     * 构造函数，初始化Anyline配置
     * 在对象创建时设置Anyline的执行模式和缓存策略
     */
    public MyBatisDataSourceMonitor() {
        // 调整执行模式为自定义，值为2表示使用自定义适配器
        ConfigTable.KEEP_ADAPTER = 2;
        // 禁用元数据缓存，值为0表示不缓存，确保每次都能获取最新的表结构
        ConfigTable.METADATA_CACHE_SCOPE = 0;
    }

    /**
     * 存储数据源特征信息的缓存Map
     * key为数据源名称，value为数据源特征字符串
     * 使用HashMap缓存已计算过的特征，避免重复获取数据库元数据，提升性能
     */
    private final Map<String, String> features = new HashMap<>();

    /**
     * 获取数据源特征，用于确定使用哪个SQL适配器
     * 特征字符串格式：数据库产品名小写_数据库URL
     * 例如：mysql_jdbc:mysql://localhost:3306/ry-vue
     *
     * @param runtime    Anyline运行时对象
     * @param datasource 数据源对象（这里是JdbcTemplate）
     * @return String 数据源特征字符串，返回null由上层自动提取
     */
    @Override
    public String feature(DataRuntime runtime, Object datasource) {
        // 初始化特征字符串为null
        String feature = null;
        // 判断数据源是否为JdbcTemplate类型
        if (datasource instanceof JdbcTemplate jdbc) {
            // 获取JdbcTemplate中的实际数据源
            DataSource ds = jdbc.getDataSource();
            // 判断是否为动态路由数据源（MyBatis-Plus的动态数据源实现）
            if (ds instanceof DynamicRoutingDataSource) {
                // 从ThreadLocal中获取当前数据源key（如master、slave等）
                String key = DynamicDataSourceContextHolder.peek();
                // 从缓存中获取已计算过的特征
                feature = features.get(key);
                // 如果缓存中没有，则实时计算
                if (null == feature) {
                    Connection con = null;
                    try {
                        // 从数据源获取数据库连接
                        con = DataSourceUtils.getConnection(ds);
                        // 获取数据库元数据信息
                        DatabaseMetaData meta = con.getMetaData();
                        // 获取数据库URL
                        String url = meta.getURL();
                        // 构建特征字符串：产品名(小写无空格) + _ + URL
                        feature = meta.getDatabaseProductName().toLowerCase().replace(" ", "") + "_" + url;
                        // 将计算结果放入缓存，避免重复计算
                        features.put(key, feature);
                    } catch (Exception e) {
                        // 记录异常日志，不影响主流程
                        log.error(e.getMessage(), e);
                    } finally {
                        // 释放数据库连接，避免连接泄漏
                        if (null != con && !DataSourceUtils.isConnectionTransactional(con, ds)) {
                            DataSourceUtils.releaseConnection(con, ds);
                        }
                    }
                }
            }
        }
        // 返回特征字符串
        return feature;
    }

    /**
     * 获取数据源唯一标识
     * 对于动态数据源，返回当前数据源的key（如master、slave）
     * 对于普通数据源，返回运行时的key
     *
     * @param runtime    Anyline运行时对象
     * @param datasource 数据源对象
     * @return String 数据源唯一标识，返回null由上层自动提取
     */
    @Override
    public String key(DataRuntime runtime, Object datasource) {
        // 判断数据源是否为JdbcTemplate类型
        if(datasource instanceof JdbcTemplate jdbc){
            // 获取实际数据源
            DataSource ds = jdbc.getDataSource();
            // 如果是动态路由数据源，返回当前数据源key
            if(ds instanceof DynamicRoutingDataSource){
                return DynamicDataSourceContextHolder.peek();
            }
        }
        // 否则返回运行时的key
        return runtime.getKey();
    }

    /**
     * 判断是否保持同一个数据源绑定同一个adapter
     * ConfigTable.KEEP_ADAPTER=2表示根据当前接口判断
     * 对于DynamicRoutingDataSource返回false，因为同一个动态数据源可能对应多种数据库类型
     * 如果项目中只有一种数据库，应该直接返回true以提高性能
     *
     * @param runtime    Anyline运行时对象
     * @param datasource 数据源对象
     * @return boolean true表示保持绑定，false表示不保持
     */
    @Override
    public boolean keepAdapter(DataRuntime runtime, Object datasource) {
        // 判断数据源是否为JdbcTemplate类型
        if (datasource instanceof JdbcTemplate jdbc) {
            // 获取实际数据源
            DataSource ds = jdbc.getDataSource();
            // 如果是动态路由数据源，返回false（不保持绑定）
            return !(ds instanceof DynamicRoutingDataSource);
        }
        // 其他情况返回true（保持绑定）
        return true;
    }

}
