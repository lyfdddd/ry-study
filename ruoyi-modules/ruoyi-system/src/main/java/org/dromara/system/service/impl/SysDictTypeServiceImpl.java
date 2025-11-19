// 字典类型服务实现类所在的包路径
package org.dromara.system.service.impl;

// Hutool工具类：Bean转换、集合操作、对象判断
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
// MyBatis-Plus核心组件：Lambda查询包装器、更新包装器、工具类、分页插件
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
// Lombok注解：自动生成构造函数
import lombok.RequiredArgsConstructor;
// 公共核心常量：缓存名称
import org.dromara.common.core.constant.CacheNames;
// 字典数据DTO、字典类型DTO、业务异常、字典服务接口
import org.dromara.common.core.domain.dto.DictDataDTO;
import org.dromara.common.core.domain.dto.DictTypeDTO;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.service.DictService;
// 公共核心工具类：MapStruct转换、Spring工具、Stream工具、字符串工具
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.SpringUtils;
import org.dromara.common.core.utils.StreamUtils;
import org.dromara.common.core.utils.StringUtils;
// MyBatis-Plus分页组件
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
// Redis缓存工具类
import org.dromara.common.redis.utils.CacheUtils;
// 系统领域模型：字典数据、字典类型
import org.dromara.system.domain.SysDictData;
import org.dromara.system.domain.SysDictType;
// 字典类型业务对象、字典数据视图对象、字典类型视图对象
import org.dromara.system.domain.bo.SysDictTypeBo;
import org.dromara.system.domain.vo.SysDictDataVo;
import org.dromara.system.domain.vo.SysDictTypeVo;
// 字典数据Mapper、字典类型Mapper
import org.dromara.system.mapper.SysDictDataMapper;
import org.dromara.system.mapper.SysDictTypeMapper;
// 字典类型服务接口
import org.dromara.system.service.ISysDictTypeService;
// Spring缓存注解：缓存更新、缓存查询
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
// Spring服务注解
import org.springframework.stereotype.Service;
// Spring事务注解
import org.springframework.transaction.annotation.Transactional;

// Java集合工具类、Stream流
import java.util.*;
import java.util.stream.Collectors;

/**
 * 字典类型服务实现类
 * 实现字典类型管理的核心业务逻辑，包括CRUD、缓存管理、字典数据关联等
 * 同时实现DictService接口，为其他模块提供字典查询服务
 *
 * @author Lion Li
 */
// Lombok注解：自动生成包含所有final字段的构造函数，实现依赖注入
@RequiredArgsConstructor
// Spring服务注解：标记为Spring Bean，交由Spring容器管理
@Service
public class SysDictTypeServiceImpl implements ISysDictTypeService, DictService {

    // 字典类型Mapper，用于字典类型数据的持久化操作
    private final SysDictTypeMapper baseMapper;
    // 字典数据Mapper，用于字典数据查询和操作
    private final SysDictDataMapper dictDataMapper;

    /**
     * 分页查询字典类型列表
     * 根据查询条件分页查询字典类型列表
     *
     * @param dictType  字典类型查询条件
     * @param pageQuery 分页参数（页码、每页条数）
     * @return 字典类型分页结果
     */
    @Override
    public TableDataInfo<SysDictTypeVo> selectPageDictTypeList(SysDictTypeBo dictType, PageQuery pageQuery) {
        // 构建查询条件
        LambdaQueryWrapper<SysDictType> lqw = buildQueryWrapper(dictType);
        // 调用Mapper执行分页查询
        Page<SysDictTypeVo> page = baseMapper.selectVoPage(pageQuery.build(), lqw);
        // 将MyBatis-Plus的Page对象转换为系统统一的TableDataInfo
        return TableDataInfo.build(page);
    }

    /**
     * 根据条件查询字典类型列表
     * 根据查询条件查询字典类型列表，不分页
     *
     * @param dictType 字典类型查询条件
     * @return 字典类型集合信息
     */
    @Override
    public List<SysDictTypeVo> selectDictTypeList(SysDictTypeBo dictType) {
        // 构建查询条件
        LambdaQueryWrapper<SysDictType> lqw = buildQueryWrapper(dictType);
        // 调用Mapper查询列表
        return baseMapper.selectVoList(lqw);
    }

    /**
     * 构建字典类型查询条件
     * 私有方法，封装通用查询逻辑，支持Lambda表达式
     *
     * @param bo 字典类型查询条件
     * @return LambdaQueryWrapper，类型安全，避免硬编码字段名
     */
    private LambdaQueryWrapper<SysDictType> buildQueryWrapper(SysDictTypeBo bo) {
        // 获取查询参数Map，包含beginTime、endTime等
        Map<String, Object> params = bo.getParams();
        // 创建LambdaQueryWrapper，使用实体属性引用，编译期检查
        LambdaQueryWrapper<SysDictType> lqw = Wrappers.lambdaQuery();
        // 模糊查询字典名称
        lqw.like(StringUtils.isNotBlank(bo.getDictName()), SysDictType::getDictName, bo.getDictName());
        // 模糊查询字典类型
        lqw.like(StringUtils.isNotBlank(bo.getDictType()), SysDictType::getDictType, bo.getDictType());
        // 时间范围查询创建时间
        lqw.between(params.get("beginTime") != null && params.get("endTime") != null,
            SysDictType::getCreateTime, params.get("beginTime"), params.get("endTime"));
        // 按字典ID升序排序
        lqw.orderByAsc(SysDictType::getDictId);
        return lqw;
    }

    /**
     * 查询所有字典类型
     * 查询系统中所有字典类型列表
     *
     * @return 字典类型集合信息
     */
    @Override
    public List<SysDictTypeVo> selectDictTypeAll() {
        // 调用Mapper查询所有字典类型VO列表
        return baseMapper.selectVoList();
    }

    /**
     * 根据字典类型查询字典数据
     * 根据字典类型查询对应的字典数据列表，使用Redis缓存提升性能
     *
     * @param dictType 字典类型
     * @return 字典数据集合信息
     */
    // Spring缓存注解：查询时缓存结果，key为字典类型
    @Cacheable(cacheNames = CacheNames.SYS_DICT, key = "#dictType")
    @Override
    public List<SysDictDataVo> selectDictDataByType(String dictType) {
        // 调用Mapper查询字典数据列表
        List<SysDictDataVo> dictDatas = dictDataMapper.selectDictDataByType(dictType);
        // 如果查询结果不为空则返回，否则返回null（防止缓存穿透）
        return CollUtil.isNotEmpty(dictDatas) ? dictDatas : null;
    }

    /**
     * 根据字典类型ID查询信息
     * 根据字典类型ID查询字典类型详情
     *
     * @param dictId 字典类型ID
     * @return 字典类型
     */
    @Override
    public SysDictTypeVo selectDictTypeById(Long dictId) {
        // 调用Mapper查询VO对象
        return baseMapper.selectVoById(dictId);
    }

    /**
     * 根据字典类型查询信息
     * 根据字典类型查询字典类型详情，使用Redis缓存提升性能
     *
     * @param dictType 字典类型
     * @return 字典类型
     */
    // Spring缓存注解：查询时缓存结果，key为字典类型
    @Cacheable(cacheNames = CacheNames.SYS_DICT_TYPE, key = "#dictType")
    @Override
    public SysDictTypeVo selectDictTypeByType(String dictType) {
        // 调用Mapper查询VO对象
        return baseMapper.selectVoOne(new LambdaQueryWrapper<SysDictType>().eq(SysDictType::getDictType, dictType));
    }

    /**
     * 批量删除字典类型信息
     * 批量删除字典类型，并检查是否已分配字典数据
     *
     * @param dictIds 需要删除的字典ID列表
     */
    @Override
    public void deleteDictTypeByIds(List<Long> dictIds) {
        // 查询要删除的字典类型列表
        List<SysDictType> list = baseMapper.selectByIds(dictIds);
        // 遍历字典类型列表
        list.forEach(x -> {
            // 检查是否存在字典数据
            boolean assigned = dictDataMapper.exists(new LambdaQueryWrapper<SysDictData>()
                .eq(SysDictData::getDictType, x.getDictType()));
            // 如果已分配字典数据，不允许删除
            if (assigned) {
                throw new ServiceException("{}已分配,不能删除", x.getDictName());
            }
        });
        // 批量删除字典类型
        baseMapper.deleteByIds(dictIds);
        // 清除缓存
        list.forEach(x -> {
            CacheUtils.evict(CacheNames.SYS_DICT, x.getDictType());
            CacheUtils.evict(CacheNames.SYS_DICT_TYPE, x.getDictType());
        });
    }

    /**
     * 重置字典缓存数据
     * 清除所有字典相关缓存
     */
    @Override
    public void resetDictCache() {
        // 清除SYS_DICT缓存命名空间下的所有缓存
        CacheUtils.clear(CacheNames.SYS_DICT);
        // 清除SYS_DICT_TYPE缓存命名空间下的所有缓存
        CacheUtils.clear(CacheNames.SYS_DICT_TYPE);
    }

    /**
     * 新增保存字典类型信息
     * 新增字典类型，并更新缓存
     *
     * @param bo 字典类型信息
     * @return 字典数据列表（新增时为空列表）
     */
    // Spring缓存注解：更新缓存，key为字典类型
    @CachePut(cacheNames = CacheNames.SYS_DICT, key = "#bo.dictType")
    @Override
    public List<SysDictDataVo> insertDictType(SysDictTypeBo bo) {
        // 将BO转换为实体对象
        SysDictType dict = MapstructUtils.convert(bo, SysDictType.class);
        // 插入数据
        int row = baseMapper.insert(dict);
        // 如果插入成功
        if (row > 0) {
            // 新增type下无data数据，返回空列表防止缓存穿透
            return new ArrayList<>();
        }
        // 插入失败，抛出业务异常
        throw new ServiceException("操作失败");
    }

    /**
     * 修改保存字典类型信息
     * 修改字典类型，同步更新字典数据的字典类型字段，并更新缓存
     *
     * @param bo 字典类型信息
     * @return 字典数据列表
     */
    // Spring缓存注解：更新缓存，key为字典类型
    @CachePut(cacheNames = CacheNames.SYS_DICT, key = "#bo.dictType")
    @Override
    // Spring事务注解：发生异常时回滚事务
    @Transactional(rollbackFor = Exception.class)
    public List<SysDictDataVo> updateDictType(SysDictTypeBo bo) {
        // 将BO转换为实体对象
        SysDictType dict = MapstructUtils.convert(bo, SysDictType.class);
        // 查询旧字典类型信息
        SysDictType oldDict = baseMapper.selectById(dict.getDictId());
        // 批量更新字典数据的字典类型字段
        dictDataMapper.update(null, new LambdaUpdateWrapper<SysDictData>()
            // 设置新的字典类型
            .set(SysDictData::getDictType, dict.getDictType())
            // 条件：字典类型等于旧字典类型
            .eq(SysDictData::getDictType, oldDict.getDictType()));
        // 更新字典类型
        int row = baseMapper.updateById(dict);
        // 如果更新成功
        if (row > 0) {
            // 清除旧缓存
            CacheUtils.evict(CacheNames.SYS_DICT, oldDict.getDictType());
            CacheUtils.evict(CacheNames.SYS_DICT_TYPE, oldDict.getDictType());
            // 返回新的字典数据列表
            return dictDataMapper.selectDictDataByType(dict.getDictType());
        }
        // 更新失败，抛出业务异常
        throw new ServiceException("操作失败");
    }

    /**
     * 校验字典类型名称是否唯一
     * 检查字典类型是否重复
     *
     * @param dictType 字典类型信息
     * @return 结果 true唯一 false不唯一
     */
    @Override
    public boolean checkDictTypeUnique(SysDictTypeBo dictType) {
        // 查询是否存在相同字典类型的记录
        boolean exist = baseMapper.exists(new LambdaQueryWrapper<SysDictType>()
            // 字典类型相同
            .eq(SysDictType::getDictType, dictType.getDictType())
            // 排除当前字典类型ID（编辑时）
            .ne(ObjectUtil.isNotNull(dictType.getDictId()), SysDictType::getDictId, dictType.getDictId()));
        // 返回是否唯一（不存在重复）
        return !exist;
    }

    /**
     * 根据字典类型和字典值获取字典标签
     * 根据字典类型和字典值（多个值用分隔符分隔）获取对应的字典标签
     *
     * @param dictType  字典类型
     * @param dictValue 字典值（支持多个，用分隔符分隔）
     * @param separator 分隔符
     * @return 字典标签
     */
    @Override
    public String getDictLabel(String dictType, String dictValue, String separator) {
        // 使用AOP代理调用selectDictDataByType，确保缓存注解生效
        List<SysDictDataVo> datas = SpringUtils.getAopProxy(this).selectDictDataByType(dictType);
        // 将字典数据列表转换为Map，key为字典值，value为字典标签
        Map<String, String> map = StreamUtils.toMap(datas, SysDictDataVo::getDictValue, SysDictDataVo::getDictLabel);
        // 如果字典值包含分隔符（多个值）
        if (StringUtils.containsAny(dictValue, separator)) {
            // 分割字典值，分别查询标签，再用分隔符连接
            return Arrays.stream(dictValue.split(separator))
                .map(v -> map.getOrDefault(v, StringUtils.EMPTY)) // 获取标签，不存在返回空字符串
                .collect(Collectors.joining(separator)); // 用分隔符连接
        } else {
            // 单个值，直接查询标签
            return map.getOrDefault(dictValue, StringUtils.EMPTY);
        }
    }

    /**
     * 根据字典类型和字典标签获取字典值
     * 根据字典类型和字典标签（多个标签用分隔符分隔）获取对应的字典值
     *
     * @param dictType  字典类型
     * @param dictLabel 字典标签（支持多个，用分隔符分隔）
     * @param separator 分隔符
     * @return 字典值
     */
    @Override
    public String getDictValue(String dictType, String dictLabel, String separator) {
        // 使用AOP代理调用selectDictDataByType，确保缓存注解生效
        List<SysDictDataVo> datas = SpringUtils.getAopProxy(this).selectDictDataByType(dictType);
        // 将字典数据列表转换为Map，key为字典标签，value为字典值
        Map<String, String> map = StreamUtils.toMap(datas, SysDictDataVo::getDictLabel, SysDictDataVo::getDictValue);
        // 如果字典标签包含分隔符（多个标签）
        if (StringUtils.containsAny(dictLabel, separator)) {
            // 分割字典标签，分别查询值，再用分隔符连接
            return Arrays.stream(dictLabel.split(separator))
                .map(l -> map.getOrDefault(l, StringUtils.EMPTY)) // 获取值，不存在返回空字符串
                .collect(Collectors.joining(separator)); // 用分隔符连接
        } else {
            // 单个标签，直接查询值
            return map.getOrDefault(dictLabel, StringUtils.EMPTY);
        }
    }

    /**
     * 获取字典下所有的字典值与标签
     * 查询指定字典类型下的所有字典数据，返回dictValue为key，dictLabel为值的Map
     *
     * @param dictType 字典类型
     * @return dictValue为key，dictLabel为值组成的Map（LinkedHashMap保证顺序）
     */
    @Override
    public Map<String, String> getAllDictByDictType(String dictType) {
        // 使用AOP代理调用selectDictDataByType，确保缓存注解生效
        List<SysDictDataVo> list = SpringUtils.getAopProxy(this).selectDictDataByType(dictType);
        // 使用LinkedHashMap保证顺序
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        // 遍历字典数据列表，添加到Map
        for (SysDictDataVo vo : list) {
            map.put(vo.getDictValue(), vo.getDictLabel());
        }
        return map;
    }

    /**
     * 根据字典类型查询详细信息
     * 对外提供的字典类型查询接口，使用AOP代理确保缓存生效
     *
     * @param dictType 字典类型
     * @return 字典类型详细信息（DTO格式）
     */
    @Override
    public DictTypeDTO getDictType(String dictType) {
        // 使用AOP代理调用selectDictTypeByType，确保缓存注解生效
        SysDictTypeVo vo = SpringUtils.getAopProxy(this).selectDictTypeByType(dictType);
        // 转换为DTO对象
        return BeanUtil.toBean(vo, DictTypeDTO.class);
    }

    /**
     * 根据字典类型查询字典数据列表
     * 对外提供的字典数据查询接口，使用AOP代理确保缓存生效
     *
     * @param dictType 字典类型
     * @return 字典数据列表（DTO格式）
     */
    @Override
    public List<DictDataDTO> getDictData(String dictType) {
        // 使用AOP代理调用selectDictDataByType，确保缓存注解生效
        List<SysDictDataVo> list = SpringUtils.getAopProxy(this).selectDictDataByType(dictType);
        // 转换为DTO列表
        return BeanUtil.copyToList(list, DictDataDTO.class);
    }

}
