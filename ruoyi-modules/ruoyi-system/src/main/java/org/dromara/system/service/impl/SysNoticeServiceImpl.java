// 包声明：定义当前类所在的包路径，org.dromara.system.service.impl 表示系统模块服务实现层
package org.dromara.system.service.impl;

// MyBatis-Plus核心组件：Lambda查询包装器，支持类型安全查询
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
// MyBatis-Plus工具类：提供快速构建查询包装器的方法
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
// MyBatis-Plus分页插件：分页对象
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
// Lombok注解：自动生成包含final字段的构造函数，实现依赖注入
import lombok.RequiredArgsConstructor;
// 公共核心工具类：MapStruct对象转换工具
import org.dromara.common.core.utils.MapstructUtils;
// 公共核心工具类：对象操作工具，提供空值处理等方法
import org.dromara.common.core.utils.ObjectUtils;
// 公共核心工具类：字符串操作工具
import org.dromara.common.core.utils.StringUtils;
// MyBatis-Plus分页组件：分页查询参数
import org.dromara.common.mybatis.core.page.PageQuery;
// MyBatis-Plus分页组件：分页结果封装
import org.dromara.common.mybatis.core.page.TableDataInfo;
// 系统领域模型：公告实体类
import org.dromara.system.domain.SysNotice;
// 系统领域模型：用户实体类
import org.dromara.system.domain.SysUser;
// 系统业务对象：公告业务对象，用于接收前端参数
import org.dromara.system.domain.bo.SysNoticeBo;
// 系统视图对象：公告视图对象
import org.dromara.system.domain.vo.SysNoticeVo;
// 系统视图对象：用户视图对象
import org.dromara.system.domain.vo.SysUserVo;
// 系统Mapper接口：公告Mapper
import org.dromara.system.mapper.SysNoticeMapper;
// 系统Mapper接口：用户Mapper
import org.dromara.system.mapper.SysUserMapper;
// 系统服务接口：公告服务接口
import org.dromara.system.service.ISysNoticeService;
// Spring服务注解：标记为服务类，交由Spring容器管理
import org.springframework.stereotype.Service;

// Java数组工具类
import java.util.Arrays;
// Java列表工具类
import java.util.List;

/**
 * 公告管理服务实现类
 * 实现公告管理的核心业务逻辑，包括公告的增删改查、分页查询等
 *
 * @author Lion Li
 */
// Lombok注解：自动生成包含所有final字段的构造函数，实现依赖注入
@RequiredArgsConstructor
// Spring服务注解：标记为Spring Bean，交由Spring容器管理
@Service
public class SysNoticeServiceImpl implements ISysNoticeService {

    // 公告Mapper，用于公告数据的持久化操作
    private final SysNoticeMapper baseMapper;
    // 用户Mapper，用于用户数据查询
    private final SysUserMapper userMapper;

    /**
     * 分页查询通知公告列表
     * 根据查询条件分页查询公告列表，支持模糊查询和精确查询
     *
     * @param notice    查询条件
     * @param pageQuery 分页参数
     * @return 通知公告分页列表
     */
    @Override
    public TableDataInfo<SysNoticeVo> selectPageNoticeList(SysNoticeBo notice, PageQuery pageQuery) {
        // 构建查询条件
        LambdaQueryWrapper<SysNotice> lqw = buildQueryWrapper(notice);
        // 调用Mapper执行分页查询，返回VO对象
        Page<SysNoticeVo> page = baseMapper.selectVoPage(pageQuery.build(), lqw);
        // 将MyBatis-Plus的Page对象转换为系统统一的TableDataInfo
        return TableDataInfo.build(page);
    }

    /**
     * 查询公告信息
     * 根据公告ID查询单条公告详情
     *
     * @param noticeId 公告ID
     * @return 公告信息
     */
    @Override
    public SysNoticeVo selectNoticeById(Long noticeId) {
        // 调用Mapper根据ID查询VO对象
        return baseMapper.selectVoById(noticeId);
    }

    /**
     * 查询公告列表
     * 根据查询条件查询公告列表，不分页
     *
     * @param notice 公告信息
     * @return 公告集合
     */
    @Override
    public List<SysNoticeVo> selectNoticeList(SysNoticeBo notice) {
        // 构建查询条件
        LambdaQueryWrapper<SysNotice> lqw = buildQueryWrapper(notice);
        // 调用Mapper查询VO列表
        return baseMapper.selectVoList(lqw);
    }

    /**
     * 构建查询条件
     * 根据业务对象构建MyBatis-Plus的LambdaQueryWrapper
     *
     * @param bo 公告业务对象
     * @return LambdaQueryWrapper
     */
    private LambdaQueryWrapper<SysNotice> buildQueryWrapper(SysNoticeBo bo) {
        // 使用Wrappers快速创建LambdaQueryWrapper
        LambdaQueryWrapper<SysNotice> lqw = Wrappers.lambdaQuery();
        // 模糊查询公告标题
        lqw.like(StringUtils.isNotBlank(bo.getNoticeTitle()), SysNotice::getNoticeTitle, bo.getNoticeTitle());
        // 精确查询公告类型
        lqw.eq(StringUtils.isNotBlank(bo.getNoticeType()), SysNotice::getNoticeType, bo.getNoticeType());
        // 如果传入了创建人名称，需要关联查询用户表
        if (StringUtils.isNotBlank(bo.getCreateByName())) {
            // 根据用户名查询用户信息
            SysUserVo sysUser = userMapper.selectVoOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUserName, bo.getCreateByName()));
            // 精确查询创建人ID，如果用户不存在则查询条件为null
            lqw.eq(SysNotice::getCreateBy, ObjectUtils.notNullGetter(sysUser, SysUserVo::getUserId));
        }
        // 按公告ID升序排序
        lqw.orderByAsc(SysNotice::getNoticeId);
        // 返回查询条件
        return lqw;
    }

    /**
     * 新增公告
     * 插入新的公告信息到数据库
     *
     * @param bo 公告信息
     * @return 结果（插入的行数）
     */
    @Override
    public int insertNotice(SysNoticeBo bo) {
        // 将BO转换为实体对象
        SysNotice notice = MapstructUtils.convert(bo, SysNotice.class);
        // 调用Mapper插入数据
        return baseMapper.insert(notice);
    }

    /**
     * 修改公告
     * 更新公告信息到数据库
     *
     * @param bo 公告信息
     * @return 结果（更新的行数）
     */
    @Override
    public int updateNotice(SysNoticeBo bo) {
        // 将BO转换为实体对象
        SysNotice notice = MapstructUtils.convert(bo, SysNotice.class);
        // 调用Mapper根据ID更新数据
        return baseMapper.updateById(notice);
    }

    /**
     * 删除公告对象
     * 根据公告ID删除单条公告
     *
     * @param noticeId 公告ID
     * @return 结果（删除的行数）
     */
    @Override
    public int deleteNoticeById(Long noticeId) {
        // 调用Mapper根据ID删除数据
        return baseMapper.deleteById(noticeId);
    }

    /**
     * 批量删除公告信息
     * 根据公告ID数组批量删除公告
     *
     * @param noticeIds 需要删除的公告ID数组
     * @return 结果（删除的行数）
     */
    @Override
    public int deleteNoticeByIds(Long[] noticeIds) {
        // 将数组转换为List，调用Mapper批量删除
        return baseMapper.deleteByIds(Arrays.asList(noticeIds));
    }
}
