// 包声明：定义当前类所在的包路径，org.dromara.system.service.impl 表示系统模块服务实现层
// package关键字用于声明类所在的包，便于组织和管理代码，避免命名冲突
package org.dromara.system.service.impl;

// Hutool工具类：数组操作工具，提供数组判空、转换等方法
// ArrayUtil是Hutool工具库中的数组操作工具类，提供数组判空、转换、查找等常用方法
import cn.hutool.core.util.ArrayUtil;
// MyBatis-Plus核心组件：Lambda查询包装器，支持类型安全查询
// LambdaQueryWrapper是MyBatis-Plus提供的Lambda表达式查询包装器，使用实体属性引用而非字符串，避免硬编码字段名，编译期检查类型安全
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
// MyBatis-Plus分页插件：分页对象
// Page是MyBatis-Plus提供的分页插件核心类，支持物理分页查询，自动处理分页逻辑
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
// Lombok注解：自动生成包含final字段的构造函数，实现依赖注入
// RequiredArgsConstructor是Lombok提供的注解，自动生成包含所有final字段的构造函数，结合Spring的构造函数注入实现依赖注入
import lombok.RequiredArgsConstructor;
// 公共核心工具类：MapStruct对象转换工具
// MapstructUtils是项目封装的MapStruct工具类，用于在DTO、VO、Entity之间进行对象转换，性能优于BeanUtils
import org.dromara.common.core.utils.MapstructUtils;
// 公共核心工具类：字符串操作工具
// StringUtils是项目封装的字符串工具类，提供字符串判空、截取、拼接等常用操作
import org.dromara.common.core.utils.StringUtils;
// 公共核心工具类：IP地址工具类，提供根据IP获取地理位置
// AddressUtils是项目封装的IP地址工具类，基于ip2region库实现IP地址解析和地理位置查询
import org.dromara.common.core.utils.ip.AddressUtils;
// 公共日志事件：操作日志事件，用于异步记录操作日志
// OperLogEvent是项目定义的Spring事件类，用于在业务操作中发布操作日志事件，实现异步记录日志
import org.dromara.common.log.event.OperLogEvent;
// MyBatis-Plus分页组件：分页查询参数
// PageQuery是项目封装的分页查询参数类，包含页码、每页条数、排序字段等分页信息
import org.dromara.common.mybatis.core.page.PageQuery;
// MyBatis-Plus分页组件：分页结果封装
// TableDataInfo是项目封装的分页结果类，统一返回格式，包含总记录数、当前页数据、页码等信息
import org.dromara.common.mybatis.core.page.TableDataInfo;
// 系统领域模型：操作日志实体类
// SysOperLog是操作日志的实体类，对应数据库的操作日志表
import org.dromara.system.domain.SysOperLog;
// 系统业务对象：操作日志业务对象，用于接收前端参数
// SysOperLogBo是操作日志的业务对象，用于接收前端传来的操作日志查询或操作参数
import org.dromara.system.domain.bo.SysOperLogBo;
// 系统视图对象：操作日志视图对象
// SysOperLogVo是操作日志的视图对象，用于返回给前端的操作日志信息，不包含敏感字段
import org.dromara.system.domain.vo.SysOperLogVo;
// 系统Mapper接口：操作日志Mapper
// SysOperLogMapper是操作日志的数据访问层接口，继承BaseMapperPlus，提供操作日志表的CRUD操作
import org.dromara.system.mapper.SysOperLogMapper;
// 系统服务接口：操作日志服务接口
// ISysOperLogService是操作日志的服务接口，定义操作日志相关的业务方法
import org.dromara.system.service.ISysOperLogService;
// Spring事件监听注解：标记为事件监听器，监听OperLogEvent事件
// EventListener是Spring提供的事件监听注解，用于标记方法为事件监听器，监听指定类型的事件
import org.springframework.context.event.EventListener;
// Spring异步注解：标记方法为异步执行，使用线程池
// Async是Spring提供的异步执行注解，标记方法为异步执行，使用Spring的线程池，避免阻塞主线程
import org.springframework.scheduling.annotation.Async;
// Spring服务注解：标记为服务类，交由Spring容器管理
// Service是Spring提供的注解，标记类为服务层组件，交由Spring容器管理，实现依赖注入和AOP
import org.springframework.stereotype.Service;

// Java数组工具类
// Arrays是Java提供的数组工具类，提供数组排序、查找、转换等操作
import java.util.Arrays;
// Java日期类
// Date是Java提供的日期类，表示特定的瞬间，精确到毫秒
import java.util.Date;
// Java列表工具类
// List是Java集合框架中的列表接口，有序集合，允许重复元素
import java.util.List;
// Java映射工具类
// Map是Java集合框架中的映射接口，键值对集合，key不能重复
import java.util.Map;

/**
 * 操作日志服务实现类
 * 实现操作日志的记录、查询、删除等核心业务逻辑
 * 通过事件监听机制异步记录操作日志，避免阻塞主业务线程
 * 使用MyBatis-Plus实现分页查询和条件查询，提升查询性能
 * 使用MapStruct进行对象转换，保证类型安全和性能
 *
 * @author Lion Li
 */
// Lombok注解：自动生成包含所有final字段的构造函数，实现依赖注入
// 使用构造函数注入而非@Autowired，避免循环依赖，提升代码可测试性
@RequiredArgsConstructor
// Spring服务注解：标记为Spring Bean，交由Spring容器管理
// 实现ISysOperLogService接口，提供操作日志服务
@Service
public class SysOperLogServiceImpl implements ISysOperLogService {

    // 操作日志Mapper，用于操作日志数据的持久化操作
    // 使用final修饰，通过构造函数注入（Lombok自动生成），保证不可变性和线程安全
    private final SysOperLogMapper baseMapper;

    /**
     * 操作日志记录
     * 异步监听操作日志事件，解析IP地址并插入操作日志
     * 使用@Async注解实现异步执行，避免日志记录阻塞主业务线程
     * 使用@EventListener注解监听OperLogEvent事件，实现事件驱动架构
     * 使用AddressUtils解析IP地址获取地理位置，提升日志可读性
     *
     * @param operLogEvent 操作日志事件
     */
    // Spring异步注解：标记方法为异步执行，使用线程池，避免阻塞主线程
    // 使用Spring的@Async注解，将方法执行提交到线程池，主线程立即返回，提升系统吞吐量
    @Async
    // Spring事件监听注解：标记为事件监听器，监听OperLogEvent事件
    // 使用Spring的事件监听机制，实现业务解耦，符合开闭原则
    @EventListener
    public void recordOper(OperLogEvent operLogEvent) {
        // 将事件对象转换为业务对象
        // 使用MapStruct进行对象转换，性能优于BeanUtils，编译期生成转换代码
        SysOperLogBo operLog = MapstructUtils.convert(operLogEvent, SysOperLogBo.class);
        // 远程查询操作地点（根据IP地址获取地理位置）
        // 调用AddressUtils.getRealAddressByIP方法，基于ip2region库解析IP地址，获取地理位置信息
        operLog.setOperLocation(AddressUtils.getRealAddressByIP(operLog.getOperIp()));
        // 插入操作日志
        // 调用insertOperlog方法，将操作日志插入数据库
        insertOperlog(operLog);
    }

    /**
     * 分页查询操作日志列表
     * 根据查询条件分页查询操作日志列表
     * 使用MyBatis-Plus的Page插件实现物理分页，提升查询性能
     * 使用LambdaQueryWrapper构建类型安全的查询条件，避免SQL注入
     *
     * @param operLog   查询条件
     * @param pageQuery 分页参数
     * @return 操作日志分页列表
     */
    // 重写父接口方法，提供具体实现
    @Override
    public TableDataInfo<SysOperLogVo> selectPageOperLogList(SysOperLogBo operLog, PageQuery pageQuery) {
        // 构建查询条件
        // 调用buildQueryWrapper方法，根据业务对象构建LambdaQueryWrapper
        LambdaQueryWrapper<SysOperLog> lqw = buildQueryWrapper(operLog);
        // 如果没有指定排序字段，默认按operId降序排序
        // 使用StringUtils.isBlank判断排序字段是否为空，为空则按operId降序排序，保证最新日志在前
        if (StringUtils.isBlank(pageQuery.getOrderByColumn())) {
            // 使用orderByDesc方法添加降序排序条件
            lqw.orderByDesc(SysOperLog::getOperId);
        }
        // 调用Mapper执行分页查询，返回VO对象
        // 使用baseMapper的selectVoPage方法，自动将实体转换为VO对象，减少手动转换工作
        Page<SysOperLogVo> page = baseMapper.selectVoPage(pageQuery.build(), lqw);
        // 将MyBatis-Plus的Page对象转换为系统统一的TableDataInfo
        // 调用TableDataInfo.build方法，统一分页结果格式，便于前端处理
        return TableDataInfo.build(page);
    }

    /**
     * 构建查询条件
     * 根据业务对象构建MyBatis-Plus的LambdaQueryWrapper
     * 使用Lambda表达式避免硬编码字段名，提升代码可维护性
     * 支持模糊查询、精确查询、时间范围查询等多种查询方式
     *
     * @param operLog 操作日志业务对象
     * @return LambdaQueryWrapper
     */
    // 私有方法，仅在当前类内部使用，封装查询条件构建逻辑
    private LambdaQueryWrapper<SysOperLog> buildQueryWrapper(SysOperLogBo operLog) {
        // 获取查询参数Map
        // 从业务对象中获取params属性，包含beginTime、endTime等动态参数
        Map<String, Object> params = operLog.getParams();
        // 构建LambdaQueryWrapper，使用Lambda表达式，类型安全
        // 使用链式调用构建查询条件，代码简洁易读
        return new LambdaQueryWrapper<SysOperLog>()
            // 模糊查询操作IP
            // 使用like方法，当操作IP不为空时添加模糊查询条件，%value%匹配
            .like(StringUtils.isNotBlank(operLog.getOperIp()), SysOperLog::getOperIp, operLog.getOperIp())
            // 模糊查询操作标题
            // 使用like方法，当操作标题不为空时添加模糊查询条件
            .like(StringUtils.isNotBlank(operLog.getTitle()), SysOperLog::getTitle, operLog.getTitle())
            // 精确查询业务类型（大于0才查询）
            // 使用eq方法，当业务类型不为null且大于0时添加精确查询条件
            .eq(operLog.getBusinessType() != null && operLog.getBusinessType() > 0,
                SysOperLog::getBusinessType, operLog.getBusinessType())
            // 使用func方法动态添加条件
            // func方法允许在Lambda表达式中动态添加条件，提升灵活性
            .func(f -> {
                // 如果业务类型数组不为空，使用in查询
                // 使用ArrayUtil.isNotEmpty判断数组是否不为空
                if (ArrayUtil.isNotEmpty(operLog.getBusinessTypes())) {
                    // 使用in方法添加IN查询条件，查询业务类型在指定数组中的记录
                    f.in(SysOperLog::getBusinessType, Arrays.asList(operLog.getBusinessTypes()));
                }
            })
            // 精确查询操作状态
            // 使用eq方法，当操作状态不为null时添加精确查询条件
            .eq(operLog.getStatus() != null,
                SysOperLog::getStatus, operLog.getStatus())
            // 模糊查询操作人员名称
            // 使用like方法，当操作人员名称不为空时添加模糊查询条件
            .like(StringUtils.isNotBlank(operLog.getOperName()), SysOperLog::getOperName, operLog.getOperName())
            // 时间范围查询操作时间
            // 使用between方法，当开始时间和结束时间都不为null时添加时间范围查询条件
            .between(params.get("beginTime") != null && params.get("endTime") != null,
                SysOperLog::getOperTime, params.get("beginTime"), params.get("endTime"));
    }

    /**
     * 新增操作日志
     * 插入操作日志到数据库
     * 使用MapStruct进行对象转换，设置操作时间为当前时间
     *
     * @param bo 操作日志业务对象
     */
    // 重写父接口方法
    @Override
    public void insertOperlog(SysOperLogBo bo) {
        // 将BO转换为实体对象
        // 使用MapstructUtils.convert方法，将业务对象转换为实体对象
        SysOperLog operLog = MapstructUtils.convert(bo, SysOperLog.class);
        // 设置操作时间为当前时间
        // 使用new Date()获取当前系统时间，精确到毫秒
        operLog.setOperTime(new Date());
        // 插入数据
        // 调用baseMapper的insert方法，将实体对象插入数据库
        baseMapper.insert(operLog);
    }

    /**
     * 查询系统操作日志集合
     * 根据查询条件查询操作日志列表，不分页
     * 使用buildQueryWrapper构建查询条件，按operId降序排序
     *
     * @param operLog 操作日志业务对象
     * @return 操作日志集合
     */
    // 重写父接口方法
    @Override
    public List<SysOperLogVo> selectOperLogList(SysOperLogBo operLog) {
        // 构建查询条件
        // 调用buildQueryWrapper方法，根据业务对象构建LambdaQueryWrapper
        LambdaQueryWrapper<SysOperLog> lqw = buildQueryWrapper(operLog);
        // 调用Mapper查询VO列表，按operId降序排序
        // 使用orderByDesc方法添加降序排序条件，保证最新日志在前
        return baseMapper.selectVoList(lqw.orderByDesc(SysOperLog::getOperId));
    }

    /**
     * 批量删除系统操作日志
     * 根据操作日志ID数组批量删除操作日志
     * 使用Arrays.asList将数组转换为List，调用Mapper批量删除
     *
     * @param operIds 需要删除的操作日志ID数组
     * @return 结果（删除的行数）
     */
    // 重写父接口方法
    @Override
    public int deleteOperLogByIds(Long[] operIds) {
        // 将数组转换为List，调用Mapper批量删除
        // 使用Arrays.asList方法将Long数组转换为List<Long>，便于批量操作
        return baseMapper.deleteByIds(Arrays.asList(operIds));
    }

    /**
     * 查询操作日志详细
     * 根据操作日志ID查询单条操作日志详情
     * 使用baseMapper的selectVoById方法，自动将实体转换为VO对象
     *
     * @param operId 操作日志ID
     * @return 操作日志对象
     */
    // 重写父接口方法
    @Override
    public SysOperLogVo selectOperLogById(Long operId) {
        // 调用Mapper根据ID查询VO对象
        // 使用baseMapper的selectVoById方法，自动将实体对象转换为视图对象
        return baseMapper.selectVoById(operId);
    }

    /**
     * 清空操作日志
     * 删除所有操作日志记录
     * 使用LambdaQueryWrapper不带条件，删除表中所有记录
     */
    // 重写父接口方法
    @Override
    public void cleanOperLog() {
        // 调用Mapper删除所有记录（不带条件）
        // 使用new LambdaQueryWrapper<>()创建空条件，删除表中所有数据
        baseMapper.delete(new LambdaQueryWrapper<>());
    }
}
