// 登录日志服务实现类所在的包路径
package org.dromara.system.service.impl;

// Hutool工具类：对象判断工具，用于空值判断、类型转换等
import cn.hutool.core.util.ObjectUtil;
// Hutool工具类：UserAgent解析工具，用于解析浏览器User-Agent字符串
import cn.hutool.http.useragent.UserAgent;
// Hutool工具类：UserAgent工具类，提供静态方法解析User-Agent
import cn.hutool.http.useragent.UserAgentUtil;
// MyBatis-Plus核心组件：Lambda查询包装器，支持类型安全查询
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
// MyBatis-Plus分页插件：分页对象
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
// Jakarta Servlet API：HTTP请求对象
import jakarta.servlet.http.HttpServletRequest;
// Lombok注解：自动生成包含final字段的构造函数，实现依赖注入
import lombok.RequiredArgsConstructor;
// Lombok注解：自动生成SLF4J日志对象
import lombok.extern.slf4j.Slf4j;
// 公共核心常量：通用常量定义
import org.dromara.common.core.constant.Constants;
// 公共核心工具类：MapStruct对象转换工具
import org.dromara.common.core.utils.MapstructUtils;
// 公共核心工具类：Servlet工具类，提供获取客户端IP等方法
import org.dromara.common.core.utils.ServletUtils;
// 公共核心工具类：字符串操作工具
import org.dromara.common.core.utils.StringUtils;
// 公共核心工具类：IP地址工具类，提供根据IP获取地理位置
import org.dromara.common.core.utils.ip.AddressUtils;
// 公共日志事件：登录日志事件，用于异步记录登录日志
import org.dromara.common.log.event.LogininforEvent;
// MyBatis-Plus分页组件：分页查询参数
import org.dromara.common.mybatis.core.page.PageQuery;
// MyBatis-Plus分页组件：分页结果封装
import org.dromara.common.mybatis.core.page.TableDataInfo;
// Sa-Token工具类：登录助手，提供获取当前登录用户信息的方法
import org.dromara.common.satoken.utils.LoginHelper;
// 系统领域模型：登录日志实体类
import org.dromara.system.domain.SysLogininfor;
// 系统业务对象：登录日志业务对象，用于接收前端参数
import org.dromara.system.domain.bo.SysLogininforBo;
// 系统视图对象：客户端视图对象
import org.dromara.system.domain.vo.SysClientVo;
// 系统视图对象：登录日志视图对象
import org.dromara.system.domain.vo.SysLogininforVo;
// 系统Mapper接口：登录日志Mapper
import org.dromara.system.mapper.SysLogininforMapper;
// 系统服务接口：客户端服务接口
import org.dromara.system.service.ISysClientService;
// 系统服务接口：登录日志服务接口
import org.dromara.system.service.ISysLogininforService;
// Spring事件监听注解：标记为事件监听器，监听LogininforEvent事件
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
 * 系统访问日志服务实现类
 * 核心业务：登录日志的记录、查询、删除等
 * 实现接口：ISysLogininforService（登录日志服务接口）
 *
 * @author Lion Li
 */
// Lombok注解：自动生成包含所有final字段的构造函数，实现依赖注入
@RequiredArgsConstructor
// Lombok注解：自动生成SLF4J日志对象
@Slf4j
// Spring服务注解：标记为Spring Bean，交由Spring容器管理
@Service
public class SysLogininforServiceImpl implements ISysLogininforService {

    // 登录日志Mapper，用于登录日志数据的持久化操作
    private final SysLogininforMapper baseMapper;
    // 客户端服务接口，用于查询客户端信息
    private final ISysClientService clientService;

    /**
     * 记录登录信息
     * 异步监听登录事件，解析User-Agent，获取客户端信息，记录登录日志
     *
     * @param logininforEvent 登录事件
     */
    // Spring异步注解：标记方法为异步执行，使用线程池，避免阻塞主线程
    @Async
    // Spring事件监听注解：标记为事件监听器，监听LogininforEvent事件
    @EventListener
    public void recordLogininfor(LogininforEvent logininforEvent) {
        // 获取HTTP请求对象
        HttpServletRequest request = logininforEvent.getRequest();
        // 解析User-Agent字符串，获取浏览器和操作系统信息
        final UserAgent userAgent = UserAgentUtil.parse(request.getHeader("User-Agent"));
        // 获取客户端IP地址
        final String ip = ServletUtils.getClientIP(request);
        // 客户端信息
        String clientId = request.getHeader(LoginHelper.CLIENT_KEY);
        // 客户端视图对象
        SysClientVo client = null;
        // 如果客户端ID不为空
        if (StringUtils.isNotBlank(clientId)) {
            // 查询客户端信息
            client = clientService.queryByClientId(clientId);
        }

        // 根据IP地址获取地理位置（省份+城市）
        String address = AddressUtils.getRealAddressByIP(ip);
        // 创建日志字符串构建器
        StringBuilder s = new StringBuilder();
        // 添加IP地址块
        s.append(getBlock(ip));
        // 添加地理位置
        s.append(address);
        // 添加用户名
        s.append(getBlock(logininforEvent.getUsername()));
        // 添加登录状态
        s.append(getBlock(logininforEvent.getStatus()));
        // 添加登录消息
        s.append(getBlock(logininforEvent.getMessage()));
        // 打印信息到日志（使用SLF4J的占位符）
        log.info(s.toString(), logininforEvent.getArgs());
        // 获取客户端操作系统名称
        String os = userAgent.getOs().getName();
        // 获取客户端浏览器名称
        String browser = userAgent.getBrowser().getName();
        // 封装登录日志业务对象
        SysLogininforBo logininfor = new SysLogininforBo();
        // 设置租户ID
        logininfor.setTenantId(logininforEvent.getTenantId());
        // 设置用户名
        logininfor.setUserName(logininforEvent.getUsername());
        // 如果客户端信息不为空
        if (ObjectUtil.isNotNull(client)) {
            // 设置客户端Key
            logininfor.setClientKey(client.getClientKey());
            // 设置设备类型
            logininfor.setDeviceType(client.getDeviceType());
        }
        // 设置IP地址
        logininfor.setIpaddr(ip);
        // 设置登录位置
        logininfor.setLoginLocation(address);
        // 设置浏览器
        logininfor.setBrowser(browser);
        // 设置操作系统
        logininfor.setOs(os);
        // 设置登录消息
        logininfor.setMsg(logininforEvent.getMessage());
        // 日志状态
        // 如果状态是登录成功、登出或注册，设置为成功
        if (StringUtils.equalsAny(logininforEvent.getStatus(), Constants.LOGIN_SUCCESS, Constants.LOGOUT, Constants.REGISTER)) {
            logininfor.setStatus(Constants.SUCCESS);
        } else if (Constants.LOGIN_FAIL.equals(logininforEvent.getStatus())) { // 如果是登录失败
            logininfor.setStatus(Constants.FAIL);
        }
        // 插入登录日志
        insertLogininfor(logininfor);
    }

    /**
     * 获取日志块
     * 将对象转换为带方括号的字符串，用于日志格式化
     *
     * @param msg 消息对象
     * @return 带方括号的字符串
     */
    private String getBlock(Object msg) {
        // 如果消息为null，设置为空字符串
        if (msg == null) {
            msg = "";
        }
        // 返回带方括号的字符串
        return "[" + msg.toString() + "]";
    }

    /**
     * 分页查询登录日志列表
     * 根据查询条件分页查询登录日志列表
     *
     * @param logininfor 查询条件
     * @param pageQuery  分页参数
     * @return 登录日志分页列表
     */
    @Override
    public TableDataInfo<SysLogininforVo> selectPageLogininforList(SysLogininforBo logininfor, PageQuery pageQuery) {
        // 获取查询参数Map
        Map<String, Object> params = logininfor.getParams();
        // 构建LambdaQueryWrapper，使用Lambda表达式，类型安全
        LambdaQueryWrapper<SysLogininfor> lqw = new LambdaQueryWrapper<SysLogininfor>()
            // 模糊查询IP地址
            .like(StringUtils.isNotBlank(logininfor.getIpaddr()), SysLogininfor::getIpaddr, logininfor.getIpaddr())
            // 精确查询状态
            .eq(StringUtils.isNotBlank(logininfor.getStatus()), SysLogininfor::getStatus, logininfor.getStatus())
            // 模糊查询用户名
            .like(StringUtils.isNotBlank(logininfor.getUserName()), SysLogininfor::getUserName, logininfor.getUserName())
            // 时间范围查询登录时间
            .between(params.get("beginTime") != null && params.get("endTime") != null,
                SysLogininfor::getLoginTime, params.get("beginTime"), params.get("endTime"));
        // 如果没有指定排序字段，默认按infoId降序排序
        if (StringUtils.isBlank(pageQuery.getOrderByColumn())) {
            lqw.orderByDesc(SysLogininfor::getInfoId);
        }
        // 调用Mapper执行分页查询
        Page<SysLogininforVo> page = baseMapper.selectVoPage(pageQuery.build(), lqw);
        // 将MyBatis-Plus的Page对象转换为系统统一的TableDataInfo
        return TableDataInfo.build(page);
    }

    /**
     * 新增系统登录日志
     * 插入登录日志到数据库
     *
     * @param bo 访问日志对象
     */
    @Override
    public void insertLogininfor(SysLogininforBo bo) {
        // 将BO转换为实体对象
        SysLogininfor logininfor = MapstructUtils.convert(bo, SysLogininfor.class);
        // 设置登录时间为当前时间
        logininfor.setLoginTime(new Date());
        // 插入数据
        baseMapper.insert(logininfor);
    }

    /**
     * 查询系统登录日志集合
     * 根据查询条件查询登录日志列表，不分页
     *
     * @param logininfor 访问日志对象
     * @return 登录记录集合
     */
    @Override
    public List<SysLogininforVo> selectLogininforList(SysLogininforBo logininfor) {
        // 获取查询参数Map
        Map<String, Object> params = logininfor.getParams();
        // 调用Mapper查询VO列表，构建查询条件
        return baseMapper.selectVoList(new LambdaQueryWrapper<SysLogininfor>()
            // 模糊查询IP地址
            .like(StringUtils.isNotBlank(logininfor.getIpaddr()), SysLogininfor::getIpaddr, logininfor.getIpaddr())
            // 精确查询状态
            .eq(StringUtils.isNotBlank(logininfor.getStatus()), SysLogininfor::getStatus, logininfor.getStatus())
            // 模糊查询用户名
            .like(StringUtils.isNotBlank(logininfor.getUserName()), SysLogininfor::getUserName, logininfor.getUserName())
            // 时间范围查询登录时间
            .between(params.get("beginTime") != null && params.get("endTime") != null,
                SysLogininfor::getLoginTime, params.get("beginTime"), params.get("endTime"))
            // 按infoId降序排序
            .orderByDesc(SysLogininfor::getInfoId));
    }

    /**
     * 批量删除系统登录日志
     * 根据日志ID数组批量删除登录日志
     *
     * @param infoIds 需要删除的登录日志ID数组
     * @return 删除的行数
     */
    @Override
    public int deleteLogininforByIds(Long[] infoIds) {
        // 将数组转换为List，调用Mapper批量删除
        return baseMapper.deleteByIds(Arrays.asList(infoIds));
    }

    /**
     * 清空系统登录日志
     * 删除所有登录日志记录
     */
    @Override
    public void cleanLogininfor() {
        // 调用Mapper删除所有记录（不带条件）
        baseMapper.delete(new LambdaQueryWrapper<>());
    }
}
