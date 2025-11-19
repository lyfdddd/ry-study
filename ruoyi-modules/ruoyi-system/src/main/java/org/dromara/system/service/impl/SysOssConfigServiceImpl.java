// 包声明：定义当前类所在的包路径，org.dromara.system.service.impl 表示系统模块服务实现层
package org.dromara.system.service.impl;

// Hutool工具类：集合操作工具，提供集合判空、转换等方法
import cn.hutool.core.collection.CollUtil;
// Hutool工具类：对象判断工具，用于空值判断、类型转换等
import cn.hutool.core.util.ObjectUtil;
// MyBatis-Plus核心组件：Lambda查询包装器，支持类型安全查询
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
// MyBatis-Plus核心组件：Lambda更新包装器，支持类型安全更新
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
// MyBatis-Plus核心组件：查询条件构建工具类
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
// MyBatis-Plus分页插件：分页对象
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
// Lombok注解：自动生成包含final字段的构造函数，实现依赖注入
import lombok.RequiredArgsConstructor;
// Lombok注解：自动生成SLF4J日志对象
import lombok.extern.slf4j.Slf4j;
// 公共核心常量：缓存名称定义
import org.dromara.common.core.constant.CacheNames;
// 公共核心异常：业务异常类，用于抛出业务逻辑错误
import org.dromara.common.core.exception.ServiceException;
// 公共核心工具类：MapStruct对象转换工具
import org.dromara.common.core.utils.MapstructUtils;
// 公共核心工具类：对象操作工具，提供空值处理等方法
import org.dromara.common.core.utils.ObjectUtils;
// 公共核心工具类：字符串操作工具
import org.dromara.common.core.utils.StringUtils;
// 公共JSON工具类：JSON序列化/反序列化工具
import org.dromara.common.json.utils.JsonUtils;
// MyBatis-Plus分页组件：分页查询参数
import org.dromara.common.mybatis.core.page.PageQuery;
// MyBatis-Plus分页组件：分页结果封装
import org.dromara.common.mybatis.core.page.TableDataInfo;
// OSS常量定义：OSS相关常量
import org.dromara.common.oss.constant.OssConstant;
// Redis缓存工具类：提供缓存操作工具
import org.dromara.common.redis.utils.CacheUtils;
// Redis操作工具类：提供Redis操作工具
import org.dromara.common.redis.utils.RedisUtils;
// 系统领域模型：OSS配置实体类
import org.dromara.system.domain.SysOssConfig;
// 系统业务对象：OSS配置业务对象，用于接收前端参数
import org.dromara.system.domain.bo.SysOssConfigBo;
// 系统视图对象：OSS配置视图对象
import org.dromara.system.domain.vo.SysOssConfigVo;
// 系统Mapper接口：OSS配置Mapper
import org.dromara.system.mapper.SysOssConfigMapper;
// 系统服务接口：OSS配置服务接口
import org.dromara.system.service.ISysOssConfigService;
// Spring服务注解：标记为服务类，交由Spring容器管理
import org.springframework.stereotype.Service;
// Spring事务注解：声明事务边界，异常时回滚
import org.springframework.transaction.annotation.Transactional;

// Java集合工具类
import java.util.Collection;
// Java列表工具类
import java.util.List;

/**
 * OSS配置服务实现类
 * 实现OSS配置管理的核心业务逻辑，包括CRUD、缓存管理、状态切换等
 * 同时处理OSS配置的初始化、唯一性校验等复杂业务
 *
 * @author Lion Li
 * @author 孤舟烟雨
 * @date 2021-08-13
 */
// Lombok注解：自动生成SLF4J日志对象
@Slf4j
// Lombok注解：自动生成包含所有final字段的构造函数，实现依赖注入
@RequiredArgsConstructor
// Spring服务注解：标记为Spring Bean，交由Spring容器管理
@Service
public class SysOssConfigServiceImpl implements ISysOssConfigService {

    // OSS配置Mapper，用于OSS配置数据的持久化操作
    private final SysOssConfigMapper baseMapper;

    /**
     * 项目启动时，初始化参数到缓存，加载配置类
     * 查询所有OSS配置，将状态为启用的配置设置为默认配置，并将所有配置缓存到Redis
     */
    @Override
    public void init() {
        // 查询所有OSS配置列表
        List<SysOssConfig> list = baseMapper.selectList();
        // 加载OSS初始化配置
        for (SysOssConfig config : list) {
            // 获取配置Key
            String configKey = config.getConfigKey();
            // 如果状态为启用（"0"表示启用）
            if ("0".equals(config.getStatus())) {
                // 将配置Key设置为默认配置，存储到Redis
                RedisUtils.setCacheObject(OssConstant.DEFAULT_CONFIG_KEY, configKey);
            }
            // 将配置信息序列化为JSON字符串，缓存到Redis
            CacheUtils.put(CacheNames.SYS_OSS_CONFIG, config.getConfigKey(), JsonUtils.toJsonString(config));
        }
    }

    /**
     * 根据ID查询OSS配置详情
     *
     * @param ossConfigId OSS配置ID
     * @return OSS配置视图对象
     */
    @Override
    public SysOssConfigVo queryById(Long ossConfigId) {
        // 调用Mapper根据ID查询VO对象
        return baseMapper.selectVoById(ossConfigId);
    }

    /**
     * 分页查询OSS配置列表
     * 根据查询条件分页查询OSS配置列表
     *
     * @param bo        OSS配置查询条件
     * @param pageQuery 分页参数
     * @return OSS配置分页结果
     */
    @Override
    public TableDataInfo<SysOssConfigVo> queryPageList(SysOssConfigBo bo, PageQuery pageQuery) {
        // 构建查询条件
        LambdaQueryWrapper<SysOssConfig> lqw = buildQueryWrapper(bo);
        // 调用Mapper执行分页查询，返回VO对象
        Page<SysOssConfigVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        // 将MyBatis-Plus的Page对象转换为系统统一的TableDataInfo
        return TableDataInfo.build(result);
    }

    /**
     * 构建OSS配置查询条件
     * 私有方法，封装通用查询逻辑，支持Lambda表达式
     *
     * @param bo OSS配置查询条件
     * @return LambdaQueryWrapper，类型安全，避免硬编码字段名
     */
    private LambdaQueryWrapper<SysOssConfig> buildQueryWrapper(SysOssConfigBo bo) {
        // 使用Wrappers快速创建LambdaQueryWrapper
        LambdaQueryWrapper<SysOssConfig> lqw = Wrappers.lambdaQuery();
        // 精确查询配置Key
        lqw.eq(StringUtils.isNotBlank(bo.getConfigKey()), SysOssConfig::getConfigKey, bo.getConfigKey());
        // 模糊查询存储桶名称
        lqw.like(StringUtils.isNotBlank(bo.getBucketName()), SysOssConfig::getBucketName, bo.getBucketName());
        // 精确查询状态
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), SysOssConfig::getStatus, bo.getStatus());
        // 按OSS配置ID升序排序
        lqw.orderByAsc(SysOssConfig::getOssConfigId);
        // 返回查询条件
        return lqw;
    }

    /**
     * 新增OSS配置
     * 插入新的OSS配置信息到数据库，并更新缓存
     *
     * @param bo OSS配置信息
     * @return 是否成功
     */
    @Override
    public Boolean insertByBo(SysOssConfigBo bo) {
        // 将BO转换为实体对象
        SysOssConfig config = MapstructUtils.convert(bo, SysOssConfig.class);
        // 保存前的数据校验
        validEntityBeforeSave(config);
        // 插入数据
        boolean flag = baseMapper.insert(config) > 0;
        // 如果插入成功
        if (flag) {
            // 从数据库查询完整的数据做缓存
            config = baseMapper.selectById(config.getOssConfigId());
            // 将配置信息序列化为JSON字符串，缓存到Redis
            CacheUtils.put(CacheNames.SYS_OSS_CONFIG, config.getConfigKey(), JsonUtils.toJsonString(config));
        }
        // 返回是否成功
        return flag;
    }

    /**
     * 修改OSS配置
     * 更新OSS配置信息到数据库，并更新缓存
     *
     * @param bo OSS配置信息
     * @return 是否成功
     */
    @Override
    public Boolean updateByBo(SysOssConfigBo bo) {
        // 将BO转换为实体对象
        SysOssConfig config = MapstructUtils.convert(bo, SysOssConfig.class);
        // 保存前的数据校验
        validEntityBeforeSave(config);
        // 创建LambdaUpdateWrapper，用于更新空值字段
        LambdaUpdateWrapper<SysOssConfig> luw = new LambdaUpdateWrapper<>();
        // 如果前缀为空，设置为空字符串（避免null值）
        luw.set(ObjectUtil.isNull(config.getPrefix()), SysOssConfig::getPrefix, "");
        // 如果区域为空，设置为空字符串（避免null值）
        luw.set(ObjectUtil.isNull(config.getRegion()), SysOssConfig::getRegion, "");
        // 如果扩展字段1为空，设置为空字符串（避免null值）
        luw.set(ObjectUtil.isNull(config.getExt1()), SysOssConfig::getExt1, "");
        // 如果备注为空，设置为空字符串（避免null值）
        luw.set(ObjectUtil.isNull(config.getRemark()), SysOssConfig::getRemark, "");
        // 精确匹配OSS配置ID
        luw.eq(SysOssConfig::getOssConfigId, config.getOssConfigId());
        // 执行更新操作
        boolean flag = baseMapper.update(config, luw) > 0;
        // 如果更新成功
        if (flag) {
            // 从数据库查询完整的数据做缓存
            config = baseMapper.selectById(config.getOssConfigId());
            // 将配置信息序列化为JSON字符串，缓存到Redis
            CacheUtils.put(CacheNames.SYS_OSS_CONFIG, config.getConfigKey(), JsonUtils.toJsonString(config));
        }
        // 返回是否成功
        return flag;
    }

    /**
     * 保存前的数据校验
     * 校验OSS配置Key是否唯一
     *
     * @param entity OSS配置实体
     */
    private void validEntityBeforeSave(SysOssConfig entity) {
        // 如果配置Key不为空且已存在
        if (StringUtils.isNotEmpty(entity.getConfigKey())
            && !checkConfigKeyUnique(entity)) {
            // 抛出业务异常
            throw new ServiceException("操作配置'{}'失败, 配置key已存在!", entity.getConfigKey());
        }
    }

    /**
     * 批量删除OSS配置
     * 根据OSS配置ID集合批量删除OSS配置，并清除缓存
     *
     * @param ids     OSS配置ID集合
     * @param isValid 是否需要校验
     * @return 是否成功
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        // 如果需要校验
        if (isValid) {
            // 检查是否包含系统内置数据ID
            if (CollUtil.containsAny(ids, OssConstant.SYSTEM_DATA_IDS)) {
                // 如果包含系统内置数据，抛出业务异常
                throw new ServiceException("系统内置, 不可删除!");
            }
        }
        // 创建OSS配置列表
        List<SysOssConfig> list = CollUtil.newArrayList();
        // 遍历OSS配置ID集合
        for (Long configId : ids) {
            // 查询OSS配置信息
            SysOssConfig config = baseMapper.selectById(configId);
            // 添加到列表
            list.add(config);
        }
        // 批量删除OSS配置
        boolean flag = baseMapper.deleteByIds(ids) > 0;
        // 如果删除成功
        if (flag) {
            // 遍历删除的OSS配置列表
            list.forEach(sysOssConfig ->
                // 清除缓存
                CacheUtils.evict(CacheNames.SYS_OSS_CONFIG, sysOssConfig.getConfigKey()));
        }
        // 返回是否成功
        return flag;
    }

    /**
     * 判断configKey是否唯一
     * 检查OSS配置Key是否重复
     *
     * @param sysOssConfig OSS配置实体
     * @return 是否唯一
     */
    private boolean checkConfigKeyUnique(SysOssConfig sysOssConfig) {
        // 获取OSS配置ID，如果为空则默认为-1
        long ossConfigId = ObjectUtils.notNull(sysOssConfig.getOssConfigId(), -1L);
        // 查询是否存在相同配置Key的记录
        SysOssConfig info = baseMapper.selectOne(new LambdaQueryWrapper<SysOssConfig>()
            // 只查询OSS配置ID和配置Key
            .select(SysOssConfig::getOssConfigId, SysOssConfig::getConfigKey)
            // 配置Key相同
            .eq(SysOssConfig::getConfigKey, sysOssConfig.getConfigKey()));
        // 如果存在且ID不同，说明重复
        if (ObjectUtil.isNotNull(info) && info.getOssConfigId() != ossConfigId) {
            return false;
        }
        // 唯一
        return true;
    }

    /**
     * 启用禁用状态
     * 切换OSS配置状态，启用指定配置，禁用其他所有配置
     *
     * @param bo OSS配置信息
     * @return 影响的行数
     */
    @Override
    // Spring事务注解：发生异常时回滚事务
    @Transactional(rollbackFor = Exception.class)
    public int updateOssConfigStatus(SysOssConfigBo bo) {
        // 将BO转换为实体对象
        SysOssConfig sysOssConfig = MapstructUtils.convert(bo, SysOssConfig.class);
        // 禁用所有OSS配置（将所有配置状态设置为"1"，表示禁用）
        int row = baseMapper.update(null, new LambdaUpdateWrapper<SysOssConfig>()
            .set(SysOssConfig::getStatus, "1"));
        // 启用指定的OSS配置
        row += baseMapper.updateById(sysOssConfig);
        // 如果操作成功
        if (row > 0) {
            // 将启用的配置Key设置为默认配置，存储到Redis
            RedisUtils.setCacheObject(OssConstant.DEFAULT_CONFIG_KEY, sysOssConfig.getConfigKey());
        }
        // 返回影响的行数
        return row;
    }

}
