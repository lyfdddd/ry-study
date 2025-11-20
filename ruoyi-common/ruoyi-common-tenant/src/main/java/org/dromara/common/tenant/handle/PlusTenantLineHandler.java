package org.dromara.common.tenant.handle;

import cn.hutool.core.collection.ListUtil;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.StringValue;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.common.tenant.properties.TenantProperties;

import java.util.List;

/**
 * 自定义租户处理器
 *
 * @author Lion Li
 */
// Lombok日志注解，自动生成log日志对象
@Slf4j
// Lombok注解，生成包含所有final字段的构造函数
@AllArgsConstructor
public class PlusTenantLineHandler implements TenantLineHandler {

    // 租户配置属性，包含需要排除的表列表
    private final TenantProperties tenantProperties;

    // 获取当前租户ID，用于SQL注入
    @Override
    public Expression getTenantId() {
        // 从TenantHelper获取当前租户ID
        String tenantId = TenantHelper.getTenantId();
        // 如果租户ID为空，记录错误日志并返回NullValue
        if (StringUtils.isBlank(tenantId)) {
            log.error("无法获取有效的租户id -> Null");
            return new NullValue();
        }
        // 返回固定租户ID作为SQL表达式
        return new StringValue(tenantId);
    }

    // 判断是否需要忽略租户过滤（不添加租户条件）
    @Override
    public boolean ignoreTable(String tableName) {
        // 获取当前租户ID
        String tenantId = TenantHelper.getTenantId();
        // 判断是否有租户（租户ID不为空才需要过滤）
        if (StringUtils.isNotBlank(tenantId)) {
            // 获取配置中不需要过滤租户的表列表
            List<String> excludes = tenantProperties.getExcludes();
            // 定义非业务表（代码生成相关表）
            List<String> tables = ListUtil.toList(
                "gen_table",
                "gen_table_column"
            );
            // 将配置排除表和非业务表合并
            tables.addAll(excludes);
            // 判断当前表名是否在排除列表中（忽略大小写）
            return StringUtils.equalsAnyIgnoreCase(tableName, tables.toArray(new String[0]));
        }
        // 如果没有租户ID，直接返回true（忽略所有表）
        return true;
    }

}
