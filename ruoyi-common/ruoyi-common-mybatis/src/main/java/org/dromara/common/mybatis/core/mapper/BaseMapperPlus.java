// MyBatis-Plus增强Mapper接口，提供VO转换和批量操作等扩展功能
// 继承MyBatis-Plus的BaseMapper，增加VO查询、批量操作、类型转换等便捷方法
package org.dromara.common.mybatis.core.mapper;

// Hutool集合工具类，用于判断集合是否为空
import cn.hutool.core.collection.CollUtil;
// Hutool对象工具类，用于判空
import cn.hutool.core.util.ObjectUtil;
// MyBatis-Plus条件构造器
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
// MyBatis-Plus基础Mapper
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
// MyBatis-Plus分页接口
import com.baomidou.mybatisplus.core.metadata.IPage;
// MyBatis-Plus泛型工具类，用于解析泛型类型
import com.baomidou.mybatisplus.core.toolkit.reflect.GenericTypeUtils;
// MyBatis-Plus分页实现类
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
// MyBatis-Plus数据库工具类，提供批量操作方法
import com.baomidou.mybatisplus.extension.toolkit.Db;
// MyBatis日志接口
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
// MapStruct转换工具类，用于实体和VO之间的转换
import org.dromara.common.core.utils.MapstructUtils;
// Stream工具类，用于流式处理
import org.dromara.common.core.utils.StreamUtils;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 自定义 Mapper 接口, 实现 自定义扩展
 *
 * @param <T> table 泛型
 * @param <V> vo 泛型
 * @author Lion Li
 * @since 2021-05-13
 */
// 抑制类型转换警告，因为泛型转换是安全的
@SuppressWarnings("unchecked")
public interface BaseMapperPlus<T, V> extends BaseMapper<T> {

    // MyBatis日志对象，用于打印SQL和调试信息
    Log log = LogFactory.getLog(BaseMapperPlus.class);

    /**
     * 获取当前实例对象关联的泛型类型 V 的 Class 对象
     *
     * @return 返回当前实例对象关联的泛型类型 V 的 Class 对象
     */
    // 默认方法：获取VO类的Class对象，用于后续的VO转换
    // 通过MyBatis-Plus的GenericTypeUtils解析泛型参数
    default Class<V> currentVoClass() {
        // 解析当前实现类的泛型参数，返回第二个泛型类型（VO类型）
        return (Class<V>) GenericTypeUtils.resolveTypeArguments(this.getClass(), BaseMapperPlus.class)[1];
    }

    /**
     * 获取当前实例对象关联的泛型类型 T 的 Class 对象
     *
     * @return 返回当前实例对象关联的泛型类型 T 的 Class 对象
     */
    // 默认方法：获取实体类的Class对象
    default Class<T> currentModelClass() {
        // 解析当前实现类的泛型参数，返回第一个泛型类型（实体类型）
        return (Class<T>) GenericTypeUtils.resolveTypeArguments(this.getClass(), BaseMapperPlus.class)[0];
    }

    /**
     * 使用默认的查询条件查询并返回结果列表
     *
     * @return 返回查询结果的列表
     */
    // 默认方法：查询所有记录，不带任何条件
    default List<T> selectList() {
        // 创建空QueryWrapper，查询所有数据
        return this.selectList(new QueryWrapper<>());
    }

    /**
     * 批量插入实体对象集合
     *
     * @param entityList 实体对象集合
     * @return 插入操作是否成功的布尔值
     */
    // 默认方法：批量插入实体列表
    // 使用MyBatis-Plus的Db工具类，自动处理批处理
    default boolean insertBatch(Collection<T> entityList) {
        return Db.saveBatch(entityList);
    }

    /**
     * 批量根据ID更新实体对象集合
     *
     * @param entityList 实体对象集合
     * @return 更新操作是否成功的布尔值
     */
    // 默认方法：批量更新实体列表
    default boolean updateBatchById(Collection<T> entityList) {
        return Db.updateBatchById(entityList);
    }

    /**
     * 批量插入或更新实体对象集合
     *
     * @param entityList 实体对象集合
     * @return 插入或更新操作是否成功的布尔值
     */
    // 默认方法：批量插入或更新实体列表
    // 根据主键判断是插入还是更新
    default boolean insertOrUpdateBatch(Collection<T> entityList) {
        return Db.saveOrUpdateBatch(entityList);
    }

    /**
     * 批量插入实体对象集合并指定批处理大小
     *
     * @param entityList 实体对象集合
     * @param batchSize  批处理大小
     * @return 插入操作是否成功的布尔值
     */
    // 默认方法：批量插入实体列表，指定批处理大小
    // 控制每次提交的记录数，避免内存溢出
    default boolean insertBatch(Collection<T> entityList, int batchSize) {
        return Db.saveBatch(entityList, batchSize);
    }

    /**
     * 批量根据ID更新实体对象集合并指定批处理大小
     *
     * @param entityList 实体对象集合
     * @param batchSize  批处理大小
     * @return 更新操作是否成功的布尔值
     */
    // 默认方法：批量更新实体列表，指定批处理大小
    default boolean updateBatchById(Collection<T> entityList, int batchSize) {
        return Db.updateBatchById(entityList, batchSize);
    }

    /**
     * 批量插入或更新实体对象集合并指定批处理大小
     *
     * @param entityList 实体对象集合
     * @param batchSize  批处理大小
     * @return 插入或更新操作是否成功的布尔值
     */
    // 默认方法：批量插入或更新实体列表，指定批处理大小
    default boolean insertOrUpdateBatch(Collection<T> entityList, int batchSize) {
        return Db.saveOrUpdateBatch(entityList, batchSize);
    }

    /**
     * 根据ID查询单个VO对象
     *
     * @param id 主键ID
     * @return 查询到的单个VO对象
     */
    // 默认方法：根据ID查询并转换为VO对象
    default V selectVoById(Serializable id) {
        // 调用重载方法，使用当前VO类
        return selectVoById(id, this.currentVoClass());
    }

    /**
     * 根据ID查询单个VO对象并将其转换为指定的VO类
     *
     * @param id      主键ID
     * @param voClass 要转换的VO类的Class对象
     * @param <C>     VO类的类型
     * @return 查询到的单个VO对象，经过转换为指定的VO类后返回
     */
    // 默认方法：根据ID查询并转换为指定VO类
    default <C> C selectVoById(Serializable id, Class<C> voClass) {
        // 根据ID查询实体对象
        T obj = this.selectById(id);
        // 如果实体不存在返回null
        if (ObjectUtil.isNull(obj)) {
            return null;
        }
        // 使用MapStruct转换为VO对象
        return MapstructUtils.convert(obj, voClass);
    }

    /**
     * 根据ID集合批量查询VO对象列表
     *
     * @param idList 主键ID集合
     * @return 查询到的VO对象列表
     */
    // 默认方法：根据ID列表批量查询并转换为VO列表
    default List<V> selectVoByIds(Collection<? extends Serializable> idList) {
        // 调用重载方法，使用当前VO类
        return selectVoByIds(idList, this.currentVoClass());
    }

    /**
     * 根据ID集合批量查询实体对象列表，并将其转换为指定的VO对象列表
     *
     * @param idList  主键ID集合
     * @param voClass 要转换的VO类的Class对象
     * @param <C>     VO类的类型
     * @return 查询到的VO对象列表，经过转换为指定的VO类后返回
     */
    // 默认方法：根据ID列表批量查询并转换为指定VO类列表
    default <C> List<C> selectVoByIds(Collection<? extends Serializable> idList, Class<C> voClass) {
        // 根据ID列表查询实体列表
        List<T> list = this.selectByIds(idList);
        // 如果结果为空返回空列表
        if (CollUtil.isEmpty(list)) {
            return CollUtil.newArrayList();
        }
        // 使用MapStruct批量转换为VO列表
        return MapstructUtils.convert(list, voClass);
    }

    /**
     * 根据查询条件Map查询VO对象列表
     *
     * @param map 查询条件Map
     * @return 查询到的VO对象列表
     */
    // 默认方法：根据Map条件查询并转换为VO列表
    default List<V> selectVoByMap(Map<String, Object> map) {
        // 调用重载方法，使用当前VO类
        return selectVoByMap(map, this.currentVoClass());
    }

    /**
     * 根据查询条件Map查询实体对象列表，并将其转换为指定的VO对象列表
     *
     * @param map     查询条件Map
     * @param voClass 要转换的VO类的Class对象
     * @param <C>     VO类的类型
     * @return 查询到的VO对象列表，经过转换为指定的VO类后返回
     */
    // 默认方法：根据Map条件查询并转换为指定VO类列表
    default <C> List<C> selectVoByMap(Map<String, Object> map, Class<C> voClass) {
        // 根据Map条件查询实体列表
        List<T> list = this.selectByMap(map);
        // 如果结果为空返回空列表
        if (CollUtil.isEmpty(list)) {
            return CollUtil.newArrayList();
        }
        // 使用MapStruct批量转换为VO列表
        return MapstructUtils.convert(list, voClass);
    }

    /**
     * 根据条件查询单个VO对象
     *
     * @param wrapper 查询条件Wrapper
     * @return 查询到的单个VO对象
     */
    // 默认方法：根据条件查询单个记录并转换为VO
    default V selectVoOne(Wrapper<T> wrapper) {
        // 调用重载方法，使用当前VO类
        return selectVoOne(wrapper, this.currentVoClass());
    }

    /**
     * 根据条件查询单个VO对象，并根据需要决定是否抛出异常
     *
     * @param wrapper 查询条件Wrapper
     * @param throwEx 是否抛出异常的标志
     * @return 查询到的单个VO对象
     */
    // 默认方法：根据条件查询单个记录并转换为VO，可控制是否抛出异常
    default V selectVoOne(Wrapper<T> wrapper, boolean throwEx) {
        // 调用重载方法，使用当前VO类
        return selectVoOne(wrapper, this.currentVoClass(), throwEx);
    }

    /**
     * 根据条件查询单个VO对象，并指定返回的VO对象的类型
     *
     * @param wrapper 查询条件Wrapper
     * @param voClass 返回的VO对象的Class对象
     * @param <C>     返回的VO对象的类型
     * @return 查询到的单个VO对象，经过类型转换为指定的VO类后返回
     */
    // 默认方法：根据条件查询单个记录并转换为指定VO类
    default <C> C selectVoOne(Wrapper<T> wrapper, Class<C> voClass) {
        // 调用重载方法，默认抛出异常
        return selectVoOne(wrapper, voClass, true);
    }

    /**
     * 根据条件查询单个实体对象，并将其转换为指定的VO对象
     *
     * @param wrapper 查询条件Wrapper
     * @param voClass 要转换的VO类的Class对象
     * @param throwEx 是否抛出异常的标志
     * @param <C>     VO类的类型
     * @return 查询到的单个VO对象，经过转换为指定的VO类后返回
     */
    // 默认方法：根据条件查询单个记录并转换为指定VO类，可控制是否抛出异常
    default <C> C selectVoOne(Wrapper<T> wrapper, Class<C> voClass, boolean throwEx) {
        // 根据条件查询单个实体对象
        T obj = this.selectOne(wrapper, throwEx);
        // 如果实体不存在返回null
        if (ObjectUtil.isNull(obj)) {
            return null;
        }
        // 使用MapStruct转换为VO对象
        return MapstructUtils.convert(obj, voClass);
    }

    /**
     * 查询所有VO对象列表
     *
     * @return 查询到的VO对象列表
     */
    // 默认方法：查询所有记录并转换为VO列表
    default List<V> selectVoList() {
        // 调用重载方法，使用空条件和当前VO类
        return selectVoList(new QueryWrapper<>(), this.currentVoClass());
    }

    /**
     * 根据条件查询VO对象列表
     *
     * @param wrapper 查询条件Wrapper
     * @return 查询到的VO对象列表
     */
    // 默认方法：根据条件查询记录并转换为VO列表
    default List<V> selectVoList(Wrapper<T> wrapper) {
        // 调用重载方法，使用当前VO类
        return selectVoList(wrapper, this.currentVoClass());
    }

    /**
     * 根据条件查询实体对象列表，并将其转换为指定的VO对象列表
     *
     * @param wrapper 查询条件Wrapper
     * @param voClass 要转换的VO类的Class对象
     * @param <C>     VO类的类型
     * @return 查询到的VO对象列表，经过转换为指定的VO类后返回
     */
    // 默认方法：根据条件查询记录并转换为指定VO类列表
    default <C> List<C> selectVoList(Wrapper<T> wrapper, Class<C> voClass) {
        // 根据条件查询实体列表
        List<T> list = this.selectList(wrapper);
        // 如果结果为空返回空列表
        if (CollUtil.isEmpty(list)) {
            return CollUtil.newArrayList();
        }
        // 使用MapStruct批量转换为VO列表
        return MapstructUtils.convert(list, voClass);
    }

    /**
     * 根据条件分页查询VO对象列表
     *
     * @param page    分页信息
     * @param wrapper 查询条件Wrapper
     * @return 查询到的VO对象分页列表
     */
    // 默认方法：分页查询记录并转换为VO分页对象
    default <P extends IPage<V>> P selectVoPage(IPage<T> page, Wrapper<T> wrapper) {
        // 调用重载方法，使用当前VO类
        return selectVoPage(page, wrapper, this.currentVoClass());
    }

    /**
     * 根据条件分页查询实体对象列表，并将其转换为指定的VO对象分页列表
     *
     * @param page    分页信息
     * @param wrapper 查询条件Wrapper
     * @param voClass 要转换的VO类的Class对象
     * @param <C>     VO类的类型
     * @param <P>     VO对象分页列表的类型
     * @return 查询到的VO对象分页列表，经过转换为指定的VO类后返回
     */
    // 默认方法：分页查询记录并转换为指定VO类分页对象
    default <C, P extends IPage<C>> P selectVoPage(IPage<T> page, Wrapper<T> wrapper, Class<C> voClass) {
        // 根据条件分页查询实体对象列表
        List<T> list = this.selectList(page, wrapper);
        // 创建一个新的VO对象分页列表，并设置分页信息
        IPage<C> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        // 如果结果为空直接返回空分页对象
        if (CollUtil.isEmpty(list)) {
            return (P) voPage;
        }
        // 使用MapStruct批量转换为VO列表并设置到分页对象
        voPage.setRecords(MapstructUtils.convert(list, voClass));
        return (P) voPage;
    }

    /**
     * 根据条件查询符合条件的对象，并将其转换为指定类型的对象列表
     *
     * @param wrapper 查询条件Wrapper
     * @param mapper  转换函数，用于将查询到的对象转换为指定类型的对象
     * @param <C>     要转换的对象的类型
     * @return 查询到的符合条件的对象列表，经过转换为指定类型的对象后返回
     */
    // 默认方法：根据条件查询指定字段并转换为目标类型列表
    // 使用Function自定义转换逻辑，比MapStruct更灵活
    default <C> List<C> selectObjs(Wrapper<T> wrapper, Function<? super Object, C> mapper) {
        // 查询指定字段列表，使用StreamUtils转换为指定类型
        return StreamUtils.toList(this.selectObjs(wrapper), mapper);
    }

}
