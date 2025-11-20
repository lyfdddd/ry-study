// 定义MyBatis-Plus表信息后置处理器包路径
package org.dromara.common.mybatis.handler;

// Hutool类型转换工具类，用于将字符串转换为布尔值
import cn.hutool.core.convert.Convert;
// MyBatis-Plus表信息后置处理器接口，在表信息初始化完成后执行自定义逻辑
import com.baomidou.mybatisplus.core.handlers.PostInitTableInfoHandler;
// MyBatis-Plus表信息对象，包含表名、字段、主键等信息
import com.baomidou.mybatisplus.core.metadata.TableInfo;
// MyBatis配置对象
import org.apache.ibatis.session.Configuration;
// Spring工具类，用于获取配置属性
import org.dromara.common.core.utils.SpringUtils;
// 反射工具类，用于通过反射设置对象字段值
import org.dromara.common.core.utils.reflect.ReflectUtils;

/**
 * MyBatis-Plus表信息后置处理器
 * 修改表信息初始化方式，目前用于全局控制是否启用逻辑删除功能
 * 通过配置文件统一控制，避免每个实体类单独配置
 *
 * @author Lion Li
 */
// 实现MyBatis-Plus的PostInitTableInfoHandler接口
public class PlusPostInitTableInfoHandler implements PostInitTableInfoHandler {

    /**
     * 表信息初始化完成后调用
     * 在此方法中可以根据配置动态修改表信息，如禁用逻辑删除
     *
     * @param tableInfo     表信息对象，包含表名、字段、主键等
     * @param configuration MyBatis配置对象
     */
    @Override
    public void postTableInfo(TableInfo tableInfo, Configuration configuration) {
        // 从配置文件中读取是否启用逻辑删除，默认为true
        // 配置项：mybatis-plus.enableLogicDelete
        String flag = SpringUtils.getProperty("mybatis-plus.enableLogicDelete", "true");
        // 只有关闭时统一设置false，为true时MyBatis-Plus自动判断不处理
        // 这样可以全局控制逻辑删除的启用/禁用，无需修改每个实体类
        if (!Convert.toBool(flag)) {
            // 使用反射设置表信息的withLogicDelete字段为false，禁用逻辑删除
            ReflectUtils.setFieldValue(tableInfo, "withLogicDelete", false);
        }
    }

}
