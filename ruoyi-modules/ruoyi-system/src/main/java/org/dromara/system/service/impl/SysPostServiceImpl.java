// 定义包路径，将当前类归类到系统服务实现层
package org.dromara.system.service.impl;

// 引入Hutool工具库的集合工具类，用于判断集合是否为空等操作
import cn.hutool.core.collection.CollUtil;
// 引入Hutool工具库的对象工具类，用于判断对象是否为空
import cn.hutool.core.util.ObjectUtil;
// 引入MyBatis-Plus的Lambda查询包装器，支持类型安全的字段引用
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
// 引入MyBatis-Plus的查询包装器
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
// 引入MyBatis-Plus的分页插件，用于物理分页查询
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
// 引入Lombok的RequiredArgsConstructor注解，自动生成包含final字段的构造函数
import lombok.RequiredArgsConstructor;
// 引入系统常量定义，包含状态值等常量
import org.dromara.common.core.constant.SystemConstants;
// 引入服务异常类，用于抛出业务异常
import org.dromara.common.core.exception.ServiceException;
// 引入岗位服务接口，提供岗位相关的通用服务方法
import org.dromara.common.core.service.PostService;
// 引入Mapstruct工具类，用于对象之间的转换
import org.dromara.common.core.utils.MapstructUtils;
// 引入Stream工具类，用于集合的流式处理
import org.dromara.common.core.utils.StreamUtils;
// 引入字符串工具类，用于字符串的非空判断等操作
import org.dromara.common.core.utils.StringUtils;
// 引入分页查询实体类，封装分页参数
import org.dromara.common.mybatis.core.page.PageQuery;
// 引入表格数据信息类，封装分页查询结果
import org.dromara.common.mybatis.core.page.TableDataInfo;
// 引入岗位实体类，对应数据库的岗位表
import org.dromara.system.domain.SysPost;
// 引入用户岗位关联实体类，用于关联用户和岗位
import org.dromara.system.domain.SysUserPost;
// 引入岗位业务对象，用于接收前端传来的岗位查询或操作参数
import org.dromara.system.domain.bo.SysPostBo;
// 引入岗位视图对象，用于返回给前端的岗位信息
import org.dromara.system.domain.vo.SysPostVo;
// 引入部门Mapper，用于查询部门信息
import org.dromara.system.mapper.SysDeptMapper;
// 引入岗位Mapper，用于数据库操作
import org.dromara.system.mapper.SysPostMapper;
// 引入用户岗位关联Mapper，用于查询用户岗位关系
import org.dromara.system.mapper.SysUserPostMapper;
// 引入岗位服务接口，定义岗位相关的业务方法
import org.dromara.system.service.ISysPostService;
// 引入Spring的Service注解，标识该类为服务层组件
import org.springframework.stereotype.Service;

// 引入Collections工具类，用于返回空集合
import java.util.Collections;
// 引入List集合接口
import java.util.List;
// 引入Map集合接口
import java.util.Map;

/**
 * 岗位管理服务实现类
 * 核心业务：岗位管理、用户岗位关联、部门岗位管理
 * 实现接口：ISysPostService（系统岗位服务）、PostService（通用岗位服务）
 *
 * @author Lion Li
 */
// Lombok注解：自动生成包含所有final字段的构造函数，实现依赖注入
@RequiredArgsConstructor
// Spring服务注解：标记为服务类，交由Spring容器管理
@Service
public class SysPostServiceImpl implements ISysPostService, PostService {

    // 岗位Mapper，继承BaseMapperPlus，提供岗位表CRUD操作
    private final SysPostMapper baseMapper;
    // 部门Mapper，用于查询部门信息
    private final SysDeptMapper deptMapper;
    // 用户岗位关联Mapper，管理用户与岗位的多对多关系
    private final SysUserPostMapper userPostMapper;

    /**
     * 分页查询岗位列表
     * 根据查询条件分页获取岗位信息，支持模糊查询、状态筛选、时间范围查询等
     *
     * @param post      查询条件（包含岗位名称、编码、状态等）
     * @param pageQuery 分页参数（页码、每页条数、排序字段等）
     * @return 岗位分页列表（包含总记录数和当前页数据）
     */
    // 重写父接口方法，提供具体实现
    @Override
    public TableDataInfo<SysPostVo> selectPagePostList(SysPostBo post, PageQuery pageQuery) {
        // 调用Mapper的自定义分页查询方法，传入分页对象和查询条件包装器
        // pageQuery.build()将PageQuery转换为MyBatis-Plus的Page对象
        // buildQueryWrapper(post)构建动态查询条件
        Page<SysPostVo> page = baseMapper.selectPagePostList(pageQuery.build(), buildQueryWrapper(post));
        // 将Page对象转换为TableDataInfo对象，统一返回格式
        return TableDataInfo.build(page);
    }

    /**
     * 查询岗位信息集合
     * 根据查询条件获取所有符合条件的岗位列表（不分页）
     *
     * @param post 岗位查询条件对象
     * @return 岗位信息集合（List列表）
     */
    // 重写父接口方法
    @Override
    public List<SysPostVo> selectPostList(SysPostBo post) {
        // 调用Mapper的selectVoList方法，传入查询条件包装器，返回视图对象列表
        // Vo（View Object）是专门为前端展示设计的对象，不包含敏感信息
        return baseMapper.selectVoList(buildQueryWrapper(post));
    }

    /**
     * 查询用户所属岗位组
     * 根据用户ID查询该用户关联的所有岗位信息
     *
     * @param userId 用户ID（主键）
     * @return 岗位视图对象列表（包含岗位ID、名称、编码等信息）
     */
    // 重写父接口方法
    @Override
    public List<SysPostVo> selectPostsByUserId(Long userId) {
        // 直接调用Mapper的自定义查询方法，通过用户ID关联查询岗位表
        return baseMapper.selectPostsByUserId(userId);
    }

    /**
     * 根据查询条件构建查询包装器
     * 使用MyBatis-Plus的LambdaQueryWrapper构建类型安全的动态查询条件
     * 支持模糊查询、精确查询、时间范围查询、部门筛选等多种条件组合
     *
     * @param bo 查询条件对象（包含岗位名称、编码、状态、部门ID等）
     * @return 构建好的LambdaQueryWrapper查询包装器
     */
    // 私有方法，仅在当前类内部使用，用于封装查询条件构建逻辑
    private LambdaQueryWrapper<SysPost> buildQueryWrapper(SysPostBo bo) {
        // 从业务对象中获取动态参数Map（通常包含beginTime、endTime等时间范围参数）
        Map<String, Object> params = bo.getParams();
        // 创建LambdaQueryWrapper对象，使用Lambda表达式避免硬编码字段名
        // 这种方式在编译期就能检查字段名是否正确，比字符串更安全
        LambdaQueryWrapper<SysPost> wrapper = new LambdaQueryWrapper<>();
        // 链式调用构建查询条件：
        // 1. 岗位编码模糊查询（当编码不为空时）
        wrapper.like(StringUtils.isNotBlank(bo.getPostCode()), SysPost::getPostCode, bo.getPostCode())
            // 2. 岗位分类模糊查询（当分类不为空时）
            .like(StringUtils.isNotBlank(bo.getPostCategory()), SysPost::getPostCategory, bo.getPostCategory())
            // 3. 岗位名称模糊查询（当名称不为空时）
            .like(StringUtils.isNotBlank(bo.getPostName()), SysPost::getPostName, bo.getPostName())
            // 4. 状态精确查询（当状态不为空时）
            .eq(StringUtils.isNotBlank(bo.getStatus()), SysPost::getStatus, bo.getStatus())
            // 5. 创建时间范围查询（当开始时间和结束时间都不为空时）
            .between(params.get("beginTime") != null && params.get("endTime") != null,
                SysPost::getCreateTime, params.get("beginTime"), params.get("endTime"))
            // 6. 按岗位排序字段升序排列（数字越小越靠前）
            .orderByAsc(SysPost::getPostSort);
        // 判断部门ID是否不为空，优先进行单部门精确查询
        if (ObjectUtil.isNotNull(bo.getDeptId())) {
            //优先单部门搜索
            // 添加部门ID精确匹配条件
            wrapper.eq(SysPost::getDeptId, bo.getDeptId());
        } else if (ObjectUtil.isNotNull(bo.getBelongDeptId())) {
            //部门树搜索
            // 使用and方法构建嵌套条件，查询当前部门及其所有子部门的岗位
            wrapper.and(x -> {
                // 调用部门Mapper查询指定部门及其所有子部门的ID列表
                List<Long> deptIds = deptMapper.selectDeptAndChildById(bo.getBelongDeptId());
                // 添加部门ID在列表中的条件（IN查询）
                x.in(SysPost::getDeptId, deptIds);
            });
        }
        // 返回构建完成的查询包装器
        return wrapper;
    }

    /**
     * 查询所有岗位
     *
     * @return 岗位列表
     */
    @Override
    public List<SysPostVo> selectPostAll() {
        return baseMapper.selectVoList(new QueryWrapper<>());
    }

    /**
     * 通过岗位ID查询岗位信息
     *
     * @param postId 岗位ID
     * @return 角色对象信息
     */
    @Override
    public SysPostVo selectPostById(Long postId) {
        return baseMapper.selectVoById(postId);
    }

    /**
     * 根据用户ID获取岗位选择框列表
     *
     * @param userId 用户ID
     * @return 选中岗位ID列表
     */
    @Override
    public List<Long> selectPostListByUserId(Long userId) {
        List<SysPostVo> list = baseMapper.selectPostsByUserId(userId);
        return StreamUtils.toList(list, SysPostVo::getPostId);
    }

    /**
     * 通过岗位ID串查询岗位
     *
     * @param postIds 岗位id串
     * @return 岗位列表信息
     */
    @Override
    public List<SysPostVo> selectPostByIds(List<Long> postIds) {
        return baseMapper.selectVoList(new LambdaQueryWrapper<SysPost>()
            .select(SysPost::getPostId, SysPost::getPostName, SysPost::getPostCode)
            .eq(SysPost::getStatus, SystemConstants.NORMAL)
            .in(CollUtil.isNotEmpty(postIds), SysPost::getPostId, postIds));
    }

    /**
     * 校验岗位名称是否唯一
     *
     * @param post 岗位信息
     * @return 结果
     */
    @Override
    public boolean checkPostNameUnique(SysPostBo post) {
        boolean exist = baseMapper.exists(new LambdaQueryWrapper<SysPost>()
            .eq(SysPost::getPostName, post.getPostName())
            .eq(SysPost::getDeptId, post.getDeptId())
            .ne(ObjectUtil.isNotNull(post.getPostId()), SysPost::getPostId, post.getPostId()));
        return !exist;
    }

    /**
     * 校验岗位编码是否唯一
     *
     * @param post 岗位信息
     * @return 结果
     */
    @Override
    public boolean checkPostCodeUnique(SysPostBo post) {
        boolean exist = baseMapper.exists(new LambdaQueryWrapper<SysPost>()
            .eq(SysPost::getPostCode, post.getPostCode())
            .ne(ObjectUtil.isNotNull(post.getPostId()), SysPost::getPostId, post.getPostId()));
        return !exist;
    }

    /**
     * 通过岗位ID查询岗位使用数量
     *
     * @param postId 岗位ID
     * @return 结果
     */
    @Override
    public long countUserPostById(Long postId) {
        return userPostMapper.selectCount(new LambdaQueryWrapper<SysUserPost>().eq(SysUserPost::getPostId, postId));
    }

    /**
     * 通过部门ID查询岗位使用数量
     *
     * @param deptId 部门id
     * @return 结果
     */
    @Override
    public long countPostByDeptId(Long deptId) {
        return baseMapper.selectCount(new LambdaQueryWrapper<SysPost>().eq(SysPost::getDeptId, deptId));
    }

    /**
     * 删除岗位信息
     *
     * @param postId 岗位ID
     * @return 结果
     */
    @Override
    public int deletePostById(Long postId) {
        return baseMapper.deleteById(postId);
    }

    /**
     * 批量删除岗位信息
     *
     * @param postIds 需要删除的岗位ID
     * @return 结果
     */
    @Override
    public int deletePostByIds(List<Long> postIds) {
        List<SysPost> list = baseMapper.selectByIds(postIds);
        for (SysPost post : list) {
            if (this.countUserPostById(post.getPostId()) > 0) {
                throw new ServiceException("{}已分配，不能删除!", post.getPostName());
            }
        }
        return baseMapper.deleteByIds(postIds);
    }

    /**
     * 新增保存岗位信息
     *
     * @param bo 岗位信息
     * @return 结果
     */
    @Override
    public int insertPost(SysPostBo bo) {
        SysPost post = MapstructUtils.convert(bo, SysPost.class);
        return baseMapper.insert(post);
    }

    /**
     * 修改保存岗位信息
     *
     * @param bo 岗位信息
     * @return 结果
     */
    @Override
    public int updatePost(SysPostBo bo) {
        SysPost post = MapstructUtils.convert(bo, SysPost.class);
        return baseMapper.updateById(post);
    }

    /**
     * 根据岗位 ID 列表查询岗位名称映射关系
     *
     * @param postIds 岗位 ID 列表
     * @return Map，其中 key 为岗位 ID，value 为对应的岗位名称
     */
    @Override
    public Map<Long, String> selectPostNamesByIds(List<Long> postIds) {
        if (CollUtil.isEmpty(postIds)) {
            return Collections.emptyMap();
        }
        List<SysPost> list = baseMapper.selectList(
            new LambdaQueryWrapper<SysPost>()
                .select(SysPost::getPostId, SysPost::getPostName)
                .in(SysPost::getPostId, postIds)
        );
        return StreamUtils.toMap(list, SysPost::getPostId, SysPost::getPostName);
    }

}
