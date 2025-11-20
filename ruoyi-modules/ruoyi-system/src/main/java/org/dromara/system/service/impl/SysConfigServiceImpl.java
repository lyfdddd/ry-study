// 参数配置服务实现类所在的包路径
package org.dromara.system.service.impl;

// Hutool工具类：类型转换工具，支持各种类型之间的转换
import cn.hutool.core.convert.Convert;
// Hutool工具类：对象判断工具，用于空值判断、类型转换等
import cn.hutool.core.util.ObjectUtil;
// MyBatis-Plus核心组件：Lambda查询包装器，支持类型安全查询
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
// MyBatis-Plus核心组件：查询条件构建工具类
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
// MyBatis-Plus分页插件：分页对象
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
// Lombok注解：自动生成包含final字段的构造函数，实现依赖注入
import lombok.RequiredArgsConstructor;
// 公共核心常量：缓存名称定义
import org.dromara.common.core.constant.CacheNames;
// 公共核心常量：系统常量定义
import org.dromara.common.core.constant.SystemConstants;
// 公共核心异常：业务异常类，用于抛出业务逻辑错误
import org.dromara.common.core.exception.ServiceException;
// 公共核心服务接口：配置服务接口，对外提供配置查询能力
import org.dromara.common.core.service.ConfigService;
// 公共核心工具类：MapStruct对象转换工具
import org.dromara.common.core.utils.MapstructUtils;
// 公共核心工具类：对象工具类，提供对象判空、默认值获取等方法
import org.dromara.common.core.utils.ObjectUtils;
// 公共核心工具类：Spring工具类，提供AOP代理、Bean获取等功能
import org.dromara.common.core.utils.SpringUtils;
// 公共核心工具类：字符串操作工具
import org.dromara.common.core.utils.StringUtils;
// MyBatis-Plus分页组件：分页查询参数
import org.dromara.common.mybatis.core.page.PageQuery;
// MyBatis-Plus分页组件：分页结果封装
import org.dromara.common.mybatis.core.page.TableDataInfo;
// Redis缓存工具类：提供缓存操作工具
import org.dromara.common.redis.utils.CacheUtils;
// 租户助手：提供租户上下文切换能力
import org.dromara.common.tenant.helper.TenantHelper;
// 系统领域模型：参数配置实体类
import org.dromara.system.domain.SysConfig;
// 系统业务对象：参数配置业务对象，用于接收前端参数
import org.dromara.system.domain.bo.SysConfigBo;
// 系统视图对象：参数配置视图对象，用于返回前端数据
import org.dromara.system.domain.vo.SysConfigVo;
// 参数配置Mapper接口
import org.dromara.system.mapper.SysConfigMapper;
// 参数配置服务接口
import org.dromara.system.service.ISysConfigService;
// Spring缓存注解：缓存更新，用于更新缓存
import org.springframework.cache.annotation.CachePut;
// Spring缓存注解：缓存查询，用于查询时缓存结果
import org.springframework.cache.annotation.Cacheable;
// Spring服务注解：标记为服务类，交由Spring容器管理
import org.springframework.stereotype.Service;

// Java集合工具类：提供列表操作
import java.util.List;
// Java集合工具类：提供Map操作
import java.util.Map;

/**
 * 参数配置服务实现类
 * 实现系统参数配置的管理功能，包括CRUD、缓存管理、租户隔离等
 * 同时实现ConfigService接口，为其他模块提供配置查询服务
 *
 * @author Lion Li
 */
// Lombok注解：自动生成包含所有final字段的构造函数，实现依赖注入
@RequiredArgsConstructor
// Spring服务注解：标记为Spring Bean，交由Spring容器管理
@Service
public class SysConfigServiceImpl implements ISysConfigService, ConfigService {

    // 参数配置Mapper，用于参数配置数据的持久化操作
    private final SysConfigMapper baseMapper;

    /**
     * 分页查询参数配置列表
     * 根据查询条件分页查询参数配置列表
     *
     * @param config    参数配置查询条件
     * @param pageQuery 分页参数（页码、每页条数）
     * @return 参数配置分页结果
     */
    @Override
    public TableDataInfo<SysConfigVo> selectPageConfigList(SysConfigBo config, PageQuery pageQuery) {
        // 构建查询条件
        LambdaQueryWrapper<SysConfig> lqw = buildQueryWrapper(config);
        // 调用Mapper执行分页查询
        Page<SysConfigVo> page = baseMapper.selectVoPage(pageQuery.build(), lqw);
        // 将MyBatis-Plus的Page对象转换为系统统一的TableDataInfo
        return TableDataInfo.build(page);
    }

    /**
     * 查询参数配置信息
     * 根据参数配置ID查询详细信息
     *
     * @param configId 参数配置ID
     * @return 参数配置信息
     */
    @Override
    public SysConfigVo selectConfigById(Long configId) {
        // 调用Mapper查询VO对象
        return baseMapper.selectVoById(configId);
    }

    /**
     * 根据键名查询参数配置信息
     * 根据配置键名查询配置值，使用Redis缓存提升性能
     *
     * @param configKey 参数key
     * @return 参数键值
     */
    // Spring缓存注解：查询时缓存结果，key为配置键名
    @Cacheable(cacheNames = CacheNames.SYS_CONFIG, key = "#configKey")
    @Override
    public String selectConfigByKey(String configKey) {
        // 查询配置信息
        SysConfig retConfig = baseMapper.selectOne(new LambdaQueryWrapper<SysConfig>()
            .eq(SysConfig::getConfigKey, configKey));
        // 返回配置值（如果配置不存在，返回空字符串）
        return ObjectUtils.notNullGetter(retConfig, SysConfig::getConfigValue, StringUtils.EMPTY);
    }

    /**
     * 获取注册开关
     * 查询指定租户下的用户注册开关配置
     *
     * @param tenantId 租户id
     * @return true开启，false关闭
     */
    @Override
    public boolean selectRegisterEnabled(String tenantId) {
        // 在指定租户下执行查询，自动切换数据源
        String configValue = TenantHelper.dynamic(tenantId, () ->
            this.selectConfigByKey("sys.account.registerUser")
        );
        // 将配置值转换为布尔类型
        return Convert.toBool(configValue);
    }

    /**
     * 查询参数配置列表
     * 根据查询条件查询参数配置列表，不分页
     *
     * @param config 参数配置查询条件
     * @return 参数配置集合
     */
    @Override
    public List<SysConfigVo> selectConfigList(SysConfigBo config) {
        // 构建查询条件
        LambdaQueryWrapper<SysConfig> lqw = buildQueryWrapper(config);
        // 调用Mapper查询列表
        return baseMapper.selectVoList(lqw);
    }

    /**
     * 构建参数配置查询条件
     * 私有方法，封装通用查询逻辑，支持Lambda表达式
     *
     * @param bo 参数配置查询条件
     * @return LambdaQueryWrapper，类型安全，避免硬编码字段名
     */
    private LambdaQueryWrapper<SysConfig> buildQueryWrapper(SysConfigBo bo) {
        // 获取查询参数Map，包含beginTime、endTime等
        Map<String, Object> params = bo.getParams();
        // 创建LambdaQueryWrapper，使用实体属性引用，编译期检查
        LambdaQueryWrapper<SysConfig> lqw = Wrappers.lambdaQuery();
        // 模糊查询配置名称
        lqw.like(StringUtils.isNotBlank(bo.getConfigName()), SysConfig::getConfigName, bo.getConfigName());
        // 精确查询配置类型
        lqw.eq(StringUtils.isNotBlank(bo.getConfigType()), SysConfig::getConfigType, bo.getConfigType());
        // 模糊查询配置键名
        lqw.like(StringUtils.isNotBlank(bo.getConfigKey()), SysConfig::getConfigKey, bo.getConfigKey());
        // 时间范围查询创建时间
        lqw.between(params.get("beginTime") != null && params.get("endTime") != null,
            SysConfig::getCreateTime, params.get("beginTime"), params.get("endTime"));
        // 按配置ID升序排序
        lqw.orderByAsc(SysConfig::getConfigId);
        return lqw;
    }

    /**
     * 新增参数配置
     * 新增参数配置，并更新缓存
     *
     * @param bo 参数配置信息
     * @return 配置值
     */
    // Spring缓存注解：更新缓存，key为配置键名
    @CachePut(cacheNames = CacheNames.SYS_CONFIG, key = "#bo.configKey")
    @Override
    public String insertConfig(SysConfigBo bo) {
        // 将BO转换为实体对象
        SysConfig config = MapstructUtils.convert(bo, SysConfig.class);
        // 插入数据
        int row = baseMapper.insert(config);
        // 如果插入成功，返回配置值
        if (row > 0) {
            return config.getConfigValue();
        }
        // 插入失败，抛出业务异常
        throw new ServiceException("操作失败");
    }

    /**
     * 修改参数配置
     * 修改参数配置，并更新缓存
     *
     * @param bo 参数配置信息
     * @return 配置值
     */
    // Spring缓存注解：更新缓存，key为配置键名
    @CachePut(cacheNames = CacheNames.SYS_CONFIG, key = "#bo.configKey")
    @Override
    public String updateConfig(SysConfigBo bo) {
        // 影响行数
        int row = 0;
        // 将BO转换为实体对象
        SysConfig config = MapstructUtils.convert(bo, SysConfig.class);
        // 如果配置ID不为空（按ID更新）
        if (config.getConfigId() != null) {
            // 查询旧配置信息
            SysConfig temp = baseMapper.selectById(config.getConfigId());
            // 如果配置键名发生变化，清除旧缓存
            if (!StringUtils.equals(temp.getConfigKey(), config.getConfigKey())) {
                CacheUtils.evict(CacheNames.SYS_CONFIG, temp.getConfigKey());
            }
            // 按ID更新
            row = baseMapper.updateById(config);
        } else {
            // 配置ID为空（按配置键名更新）
            // 清除缓存
            CacheUtils.evict(CacheNames.SYS_CONFIG, config.getConfigKey());
            // 按配置键名更新
            row = baseMapper.update(config, new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getConfigKey, config.getConfigKey()));
        }
        // 如果更新成功，返回配置值
        if (row > 0) {
            return config.getConfigValue();
        }
        // 更新失败，抛出业务异常
        throw new ServiceException("操作失败");
    }

    /**
     * 批量删除参数信息
     * 批量删除参数配置，并清除缓存
     *
     * @param configIds 需要删除的参数ID列表
     */
    @Override
    public void deleteConfigByIds(List<Long> configIds) {
        // 查询要删除的配置列表
        List<SysConfig> list = baseMapper.selectByIds(configIds);
        // 遍历配置列表
        list.forEach(config -> {
            // 如果是内置参数（configType为Y），不允许删除
            if (StringUtils.equals(SystemConstants.YES, config.getConfigType())) {
                throw new ServiceException("内置参数【{}】不能删除", config.getConfigKey());
            }
            // 清除缓存
            CacheUtils.evict(CacheNames.SYS_CONFIG, config.getConfigKey());
        });
        // 批量删除
        baseMapper.deleteByIds(configIds);
    }

    /**
     * 重置参数缓存数据
     * 清除所有参数配置缓存
     */
    @Override
    public void resetConfigCache() {
        // 清除SYS_CONFIG缓存命名空间下的所有缓存
        CacheUtils.clear(CacheNames.SYS_CONFIG);
    }

    /**
     * 校验参数键名是否唯一
     * 检查配置键名是否重复
     *
     * @param config 参数配置信息
     * @return 结果 true唯一 false不唯一
     */
    @Override
    public boolean checkConfigKeyUnique(SysConfigBo config) {
        // 查询是否存在相同键名的配置
        boolean exist = baseMapper.exists(new LambdaQueryWrapper<SysConfig>()
            // 配置键名相同
            .eq(SysConfig::getConfigKey, config.getConfigKey())
            // 排除当前配置ID（编辑时）
            .ne(ObjectUtil.isNotNull(config.getConfigId()), SysConfig::getConfigId, config.getConfigId()));
        // 返回是否唯一（不存在重复）
        return !exist;
    }

    /**
     * 根据参数key获取参数值
     * 对外提供的配置查询接口，使用AOP代理确保缓存生效
     *
     * @param configKey 参数key
     * @return 参数值
     */
    @Override
    public String getConfigValue(String configKey) {
        // 使用AOP代理调用selectConfigByKey，确保缓存注解生效
        return SpringUtils.getAopProxy(this).selectConfigByKey(configKey);
    }

}
