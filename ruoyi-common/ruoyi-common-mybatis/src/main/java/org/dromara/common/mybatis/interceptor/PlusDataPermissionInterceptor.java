// 定义数据权限拦截器包路径，实现MyBatis-Plus插件机制
package org.dromara.common.mybatis.interceptor;

// MyBatis-Plus拦截器忽略辅助类，用于判断是否需要忽略数据权限
import com.baomidou.mybatisplus.core.plugins.InterceptorIgnoreHelper;
// MyBatis-Plus插件工具类，提供BoundSql和StatementHandler的包装
import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
// MyBatis-Plus多表数据权限处理器接口
import com.baomidou.mybatisplus.extension.plugins.handler.MultiDataPermissionHandler;
// MyBatis-Plus多表内部拦截器基类，提供SQL解析框架
import com.baomidou.mybatisplus.extension.plugins.inner.BaseMultiTableInnerInterceptor;
// MyBatis-Plus内部拦截器接口
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
// Lombok日志注解，自动生成slf4j日志对象
import lombok.extern.slf4j.Slf4j;
// JSqlParser表达式接口，用于构建SQL条件
import net.sf.jsqlparser.expression.Expression;
// JSqlParser表对象，表示SQL中的表
import net.sf.jsqlparser.schema.Table;
// JSqlParser删除语句对象
import net.sf.jsqlparser.statement.delete.Delete;
// JSqlParser查询语句对象
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SetOperationList;
// JSqlParser更新语句对象
import net.sf.jsqlparser.statement.update.Update;
// MyBatis执行器接口
import org.apache.ibatis.executor.Executor;
// MyBatis StatementHandler接口，处理SQL语句
import org.apache.ibatis.executor.statement.StatementHandler;
// MyBatis BoundSql对象，包含SQL语句和参数
import org.apache.ibatis.mapping.BoundSql;
// MyBatis映射语句对象，包含Mapper方法信息
import org.apache.ibatis.mapping.MappedStatement;
// MyBatis SQL命令类型枚举（INSERT、UPDATE、DELETE、SELECT）
import org.apache.ibatis.mapping.SqlCommandType;
// MyBatis结果处理器
import org.apache.ibatis.session.ResultHandler;
// MyBatis分页对象
import org.apache.ibatis.session.RowBounds;
// 数据权限处理器，实现具体的权限逻辑
import org.dromara.common.mybatis.handler.PlusDataPermissionHandler;

// Java SQL连接
import java.sql.Connection;
// Java SQL异常
import java.sql.SQLException;
// Java List集合
import java.util.List;

/**
 * MyBatis-Plus数据权限拦截器
 * 通过拦截SQL语句，动态注入数据权限条件，实现部门级数据隔离
 * 支持查询、更新、删除操作的数据权限控制
 * 基于JSqlParser解析SQL，修改WHERE条件
 *
 * @author Lion Li
 * @version 3.5.0
 */
// Lombok日志注解：自动生成slf4j日志对象log
@Slf4j
// 继承BaseMultiTableInnerInterceptor，支持多表数据权限处理
// 实现InnerInterceptor接口，作为MyBatis-Plus插件
public class PlusDataPermissionInterceptor extends BaseMultiTableInnerInterceptor implements InnerInterceptor {

    // 数据权限处理器实例，负责生成SQL权限片段
    // 使用final修饰，确保线程安全
    private final PlusDataPermissionHandler dataPermissionHandler = new PlusDataPermissionHandler();

    /**
     * 在执行查询之前拦截，检查并处理数据权限相关逻辑
     * 该方法在MyBatis执行查询前调用，用于注入数据权限条件
     *
     * @param executor      MyBatis执行器对象，负责执行SQL
     * @param ms            映射语句对象，包含Mapper方法信息
     * @param parameter     方法参数，如查询条件
     * @param rowBounds     分页对象
     * @param resultHandler 结果处理器
     * @param boundSql      绑定的SQL对象，包含原始SQL语句
     * @throws SQLException 如果发生SQL异常
     */
    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
        // 检查是否需要忽略数据权限处理（通过@InterceptorIgnore注解或配置）
        // 如果忽略，直接返回，不注入权限条件
        if (InterceptorIgnoreHelper.willIgnoreDataPermission(ms.getId())) {
            return;
        }
        // 检查是否缺少有效的数据权限注解（@DataPermission）
        // 如果没有注解，不注入权限条件
        if (dataPermissionHandler.invalid()) {
            return;
        }
        // 解析SQL并注入数据权限条件
        // 使用PluginUtils包装BoundSql，获取MyBatis-Plus扩展的BoundSql
        PluginUtils.MPBoundSql mpBs = PluginUtils.mpBoundSql(boundSql);
        // 调用parserSingle方法解析单表SQL，注入权限条件
        mpBs.sql(parserSingle(mpBs.sql(), ms.getId()));
    }

    /**
     * 在准备SQL语句之前拦截，检查并处理更新和删除操作的数据权限相关逻辑
     * 该方法在MyBatis执行更新/删除前调用，用于注入数据权限条件
     *
     * @param sh                 MyBatis StatementHandler对象，处理SQL语句
     * @param connection         数据库连接对象
     * @param transactionTimeout 事务超时时间
     */
    @Override
    public void beforePrepare(StatementHandler sh, Connection connection, Integer transactionTimeout) {
        // 使用PluginUtils包装StatementHandler，获取MyBatis-Plus扩展的StatementHandler
        PluginUtils.MPStatementHandler mpSh = PluginUtils.mpStatementHandler(sh);
        // 获取映射语句对象
        MappedStatement ms = mpSh.mappedStatement();
        // 获取SQL命令类型（INSERT、UPDATE、DELETE、SELECT）
        SqlCommandType sct = ms.getSqlCommandType();

        // 只处理更新和删除操作的SQL语句
        // 插入操作通常不需要数据权限控制（创建数据应该允许）
        if (sct == SqlCommandType.UPDATE || sct == SqlCommandType.DELETE) {
            // 检查是否需要忽略数据权限处理
            if (InterceptorIgnoreHelper.willIgnoreDataPermission(ms.getId())) {
                return;
            }
            // 检查是否缺少有效的数据权限注解
            if (dataPermissionHandler.invalid()) {
                return;
            }
            // 获取BoundSql并解析多表SQL，注入权限条件
            PluginUtils.MPBoundSql mpBs = mpSh.mPBoundSql();
            mpBs.sql(parserMulti(mpBs.sql(), ms.getId()));
        }
    }

    /**
     * 处理SELECT查询语句中的WHERE条件
     * 解析SELECT语句，为查询注入数据权限条件
     *
     * @param select SELECT查询对象
     * @param index  查询语句的索引（用于多表联合查询）
     * @param sql    原始SQL语句
     * @param obj    WHERE条件参数（映射语句ID）
     */
    @Override
    protected void processSelect(Select select, int index, String sql, Object obj) {
        // 判断SELECT语句类型
        if (select instanceof PlainSelect) {
            // 普通SELECT语句，设置WHERE条件
            this.setWhere((PlainSelect) select, (String) obj);
        } else if (select instanceof SetOperationList setOperationList) {
            // UNION、UNION ALL等集合操作语句
            // 获取所有SELECT子句
            List<Select> selectBodyList = setOperationList.getSelects();
            // 为每个SELECT子句设置WHERE条件
            selectBodyList.forEach(s -> this.setWhere((PlainSelect) s, (String) obj));
        }
    }

    /**
     * 处理UPDATE语句中的WHERE条件
     * 为UPDATE语句注入数据权限条件，防止越权更新
     *
     * @param update UPDATE查询对象
     * @param index  查询语句的索引
     * @param sql    原始SQL语句
     * @param obj    WHERE条件参数（映射语句ID）
     */
    @Override
    protected void processUpdate(Update update, int index, String sql, Object obj) {
        // 获取数据权限SQL片段
        Expression sqlSegment = dataPermissionHandler.getSqlSegment(update.getWhere(), false);
        // 如果SQL片段不为空，设置到UPDATE语句的WHERE条件中
        if (null != sqlSegment) {
            update.setWhere(sqlSegment);
        }
    }

    /**
     * 处理DELETE语句中的WHERE条件
     * 为DELETE语句注入数据权限条件，防止越权删除
     *
     * @param delete DELETE查询对象
     * @param index  查询语句的索引
     * @param sql    原始SQL语句
     * @param obj    WHERE条件参数（映射语句ID）
     */
    @Override
    protected void processDelete(Delete delete, int index, String sql, Object obj) {
        // 获取数据权限SQL片段
        Expression sqlSegment = dataPermissionHandler.getSqlSegment(delete.getWhere(), false);
        // 如果SQL片段不为空，设置到DELETE语句的WHERE条件中
        if (null != sqlSegment) {
            delete.setWhere(sqlSegment);
        }
    }

    /**
     * 设置SELECT语句的WHERE条件
     * 调用数据权限处理器生成SQL片段并设置到SELECT语句中
     *
     * @param plainSelect       SELECT查询对象
     * @param mappedStatementId 映射语句的ID（Mapper方法全限定名）
     */
    protected void setWhere(PlainSelect plainSelect, String mappedStatementId) {
        // 获取数据权限SQL片段，isSelect=true表示是查询操作
        Expression sqlSegment = dataPermissionHandler.getSqlSegment(plainSelect.getWhere(), true);
        // 如果SQL片段不为空，设置到SELECT语句的WHERE条件中
        if (null != sqlSegment) {
            plainSelect.setWhere(sqlSegment);
        }
    }

    /**
     * 构建表达式，用于处理表的数据权限
     * 该方法用于多表查询场景，为每个表生成对应的数据权限条件
     *
     * @param table        表对象
     * @param where        原始WHERE条件表达式
     * @param whereSegment WHERE条件片段（映射语句ID）
     * @return 构建的表达式（包含数据权限条件）
     */
    @Override
    public Expression buildTableExpression(Table table, Expression where, String whereSegment) {
        // 只有新版数据权限处理器才会执行到这里
        // 将dataPermissionHandler强制转换为MultiDataPermissionHandler接口
        final MultiDataPermissionHandler handler = (MultiDataPermissionHandler) dataPermissionHandler;
        // 调用处理器的getSqlSegment方法生成SQL片段
        return handler.getSqlSegment(table, where, whereSegment);
    }
}

