// 包声明：定义当前类所在的包路径，org.dromara.system.service.impl 表示系统模块服务实现层
package org.dromara.system.service.impl;

// Hutool工具类：数组操作工具，提供数组判空、转换等方法
import cn.hutool.core.util.ArrayUtil;
// MyBatis-Plus核心组件：Lambda查询包装器，支持类型安全查询
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
// MyBatis-Plus分页插件：分页对象
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
// Lombok注解：自动生成包含final字段的构造函数，实现依赖注入
import lombok.RequiredArgsConstructor;
// 公共核心工具类：MapStruct对象转换工具
import org.dromara.common.core.utils.MapstructUtils;
// 公共核心工具类：字符串操作工具
import org.dromara.common.core.utils.StringUtils;
// 公共核心工具类：IP地址工具类，提供根据IP获取地理位置
import org.dromara.common.core.utils.ip.AddressUtils;
// 公共日志事件：操作日志事件，用于异步记录操作日志
import org.dromara.common.log.event.OperLogEvent;
// MyBatis-Plus分页组件：分页查询参数
import org.dromara.common.mybatis.core.page.PageQuery;
// MyBatis-Plus分页组件：分页结果封装
import org.dromara.common.mybatis.core.page.TableDataInfo;
// 系统领域模型：操作日志实体类
import org.dromara.system.domain.SysOperLog;
// 系统业务对象：操作日志业务对象，用于接收前端参数
import org.dromara.system.domain.bo.SysOperLogBo;
// 系统视图对象：操作日志视图对象
import org.dromara.system.domain.vo.SysOperLogVo;
// 系统Mapper接口：操作日志Mapper
import org.dromara.system.mapper.SysOperLogMapper;
// 系统服务接口：操作日志服务接口
import org.dromara.system.service.ISysOperLogService;
// Spring事件监听注解：标记为事件监听器，监听OperLogEvent事件
import org.springframework.context.event.EventListener;
// Spring异步注解：标记方法为异步执行，使用线程池
import org.springframework.scheduling.annotation.Async;
// Spring服务注解：标记为服务类，交由Spring容器管理
import org.springframework.stereotype.Service;

// Java数组工具类
import java.util.Arrays;
// Java日期类
import java.util.Date;
// Java列表工具类
import java.util.List;
// Java映射工具类
import java.util.Map;

/**
 * 操作日志服务实现类
 * 实现操作日志的记录、查询、删除等核心业务逻辑
 * 通过事件监听机制异步记录操作日志，避免阻塞主业务线程
 *
 * @author Lion Li
 */
// Lombok注解：自动生成包含所有final字段的构造函数，实现依赖注入
@RequiredArgsConstructor
// Spring服务注解：标记为Spring Bean，交由Spring容器管理
@Service
public class SysOperLogServiceImpl implements ISysOperLogService {

    // 操作日志Mapper，用于操作日志数据的持久化操作
    private final SysOperLogMapper baseMapper;

    /**
     * 操作日志记录
     * 异步监听操作日志事件，解析IP地址并插入操作日志
     *
     * @param operLogEvent 操作日志事件
     */
    // Spring异步注解：标记方法为异步执行，使用线程池，避免阻塞主线程
    @Async
    // Spring事件监听注解：标记为事件监听器，监听OperLogEvent事件
    @EventListener
    public void recordOper(OperLogEvent operLogEvent) {
        // 将事件对象转换为业务对象
        SysOperLogBo operLog = MapstructUtils.convert(operLogEvent, SysOperLogBo.class);
        // 远程查询操作地点（根据IP地址获取地理位置）
        operLog.setOperLocation(AddressUtils.getRealAddressByIP(operLog.getOperIp()));
        // 插入操作日志
        insertOperlog(operLog);
    }

    /**
     * 分页查询操作日志列表
     * 根据查询条件分页查询操作日志列表
     *
     * @param operLog   查询条件
     * @param pageQuery 分页参数
     * @return 操作日志分页列表
     */
    @Override
    public TableDataInfo<SysOperLogVo> selectPageOperLogList(SysOperLogBo operLog, PageQuery pageQuery) {
        // 构建查询条件
        LambdaQueryWrapper<SysOperLog> lqw = buildQueryWrapper(operLog);
        // 如果没有指定排序字段，默认按operId降序排序
        if (StringUtils.isBlank(pageQuery.getOrderByColumn())) {
            lqw.orderByDesc(SysOperLog::getOperId);
        }
        // 调用Mapper执行分页查询，返回VO对象
        Page<SysOperLogVo> page = baseMapper.selectVoPage(pageQuery.build(), lqw);
        // 将MyBatis-Plus的Page对象转换为系统统一的TableDataInfo
        return TableDataInfo.build(page);
    }

    /**
     * 构建查询条件
     * 根据业务对象构建MyBatis-Plus的LambdaQueryWrapper
     *
     * @param operLog 操作日志业务对象
     * @return LambdaQueryWrapper
     */
    private LambdaQueryWrapper<SysOperLog> buildQueryWrapper(SysOperLogBo operLog) {
        // 获取查询参数Map
        Map<String, Object> params = operLog.getParams();
        // 构建LambdaQueryWrapper，使用Lambda表达式，类型安全
        return new LambdaQueryWrapper<SysOperLog>()
            // 模糊查询操作IP
            .like(StringUtils.isNotBlank(operLog.getOperIp()), SysOperLog::getOperIp, operLog.getOperIp())
            // 模糊查询操作标题
            .like(StringUtils.isNotBlank(operLog.getTitle()), SysOperLog::getTitle, operLog.getTitle())
            // 精确查询业务类型（大于0才查询）
            .eq(operLog.getBusinessType() != null && operLog.getBusinessType() > 0,
                SysOperLog::getBusinessType, operLog.getBusinessType())
            // 使用func方法动态添加条件
            .func(f -> {
                // 如果业务类型数组不为空，使用in查询
                if (ArrayUtil.isNotEmpty(operLog.getBusinessTypes())) {
                    f.in(SysOperLog::getBusinessType, Arrays.asList(operLog.getBusinessTypes()));
                }
            })
            // 精确查询操作状态
            .eq(operLog.getStatus() != null,
                SysOperLog::getStatus, operLog.getStatus())
            // 模糊查询操作人员名称
            .like(StringUtils.isNotBlank(operLog.getOperName()), SysOperLog::getOperName, operLog.getOperName())
            // 时间范围查询操作时间
            .between(params.get("beginTime") != null && params.get("endTime") != null,
                SysOperLog::getOperTime, params.get("beginTime"), params.get("endTime"));
    }

    /**
     * 新增操作日志
     * 插入操作日志到数据库
     *
     * @param bo 操作日志业务对象
     */
    @Override
    public void insertOperlog(SysOperLogBo bo) {
        // 将BO转换为实体对象
        SysOperLog operLog = MapstructUtils.convert(bo, SysOperLog.class);
        // 设置操作时间为当前时间
        operLog.setOperTime(new Date());
        // 插入数据
        baseMapper.insert(operLog);
    }

    /**
     * 查询系统操作日志集合
     * 根据查询条件查询操作日志列表，不分页
     *
     * @param operLog 操作日志业务对象
     * @return 操作日志集合
     */
    @Override
    public List<SysOperLogVo> selectOperLogList(SysOperLogBo operLog) {
        // 构建查询条件
        LambdaQueryWrapper<SysOperLog> lqw = buildQueryWrapper(operLog);
        // 调用Mapper查询VO列表，按operId降序排序
        return baseMapper.selectVoList(lqw.orderByDesc(SysOperLog::getOperId));
    }

    /**
     * 批量删除系统操作日志
     * 根据操作日志ID数组批量删除操作日志
     *
     * @param operIds 需要删除的操作日志ID数组
     * @return 结果（删除的行数）
     */
    @Override
    public int deleteOperLogByIds(Long[] operIds) {
        // 将数组转换为List，调用Mapper批量删除
        return baseMapper.deleteByIds(Arrays.asList(operIds));
    }

    /**
     * 查询操作日志详细
     * 根据操作日志ID查询单条操作日志详情
     *
     * @param operId 操作日志ID
     * @return 操作日志对象
     */
    @Override
    public SysOperLogVo selectOperLogById(Long operId) {
        // 调用Mapper根据ID查询VO对象
        return baseMapper.selectVoById(operId);
    }

    /**
     * 清空操作日志
     * 删除所有操作日志记录
     */
    @Override
    public void cleanOperLog() {
        // 调用Mapper删除所有记录（不带条件）
        baseMapper.delete(new LambdaQueryWrapper<>());
    }
}
