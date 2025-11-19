// 包声明：定义当前类所在的包路径，org.dromara.system.service.impl 表示系统模块服务实现层
package org.dromara.system.service.impl;

// Hutool工具类：Bean转换工具，用于对象属性复制
import cn.hutool.core.bean.BeanUtil;
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
// Jakarta Servlet API：HTTP响应对象
import jakarta.servlet.http.HttpServletResponse;
// Lombok注解：自动生成包含final字段的构造函数，实现依赖注入
import lombok.RequiredArgsConstructor;
// 公共核心常量：缓存名称定义
import org.dromara.common.core.constant.CacheNames;
// 公共核心领域模型：OSS数据传输对象
import org.dromara.common.core.domain.dto.OssDTO;
// 公共核心异常：业务异常类，用于抛出业务逻辑错误
import org.dromara.common.core.exception.ServiceException;
// 公共核心服务接口：OSS服务接口
import org.dromara.common.core.service.OssService;
// 公共核心工具类：MapStruct对象转换工具
import org.dromara.common.core.utils.MapstructUtils;
// 公共核心工具类：Spring工具类，提供Spring上下文相关操作
import org.dromara.common.core.utils.SpringUtils;
// 公共核心工具类：Stream流操作工具
import org.dromara.common.core.utils.StreamUtils;
// 公共核心工具类：字符串操作工具
import org.dromara.common.core.utils.StringUtils;
// 公共核心工具类：文件操作工具
import org.dromara.common.core.utils.file.FileUtils;
// 公共JSON工具类：JSON序列化/反序列化工具
import org.dromara.common.json.utils.JsonUtils;
// MyBatis-Plus分页组件：分页查询参数
import org.dromara.common.mybatis.core.page.PageQuery;
// MyBatis-Plus分页组件：分页结果封装
import org.dromara.common.mybatis.core.page.TableDataInfo;
// OSS核心客户端：OSS操作客户端
import org.dromara.common.oss.core.OssClient;
// OSS上传结果：上传操作返回结果
import org.dromara.common.oss.entity.UploadResult;
// OSS访问策略类型：PUBLIC/PRIVATE
import org.dromara.common.oss.enums.AccessPolicyType;
// OSS工厂类：创建OSS客户端实例
import org.dromara.common.oss.factory.OssFactory;
// 系统领域模型：OSS对象实体类
import org.dromara.system.domain.SysOss;
// 系统领域模型：OSS扩展信息实体类
import org.dromara.system.domain.SysOssExt;
// 系统业务对象：OSS业务对象，用于接收前端参数
import org.dromara.system.domain.bo.SysOssBo;
// 系统视图对象：OSS视图对象
import org.dromara.system.domain.vo.SysOssVo;
// 系统Mapper接口：OSS Mapper
import org.dromara.system.mapper.SysOssMapper;
// 系统服务接口：OSS服务接口
import org.dromara.system.service.ISysOssService;
// JetBrains注解：非空注解
import org.jetbrains.annotations.NotNull;
// Spring缓存注解：缓存查询，用于查询时缓存结果
import org.springframework.cache.annotation.Cacheable;
// Spring HTTP媒体类型：HTTP内容类型定义
import org.springframework.http.MediaType;
// Spring服务注解：标记为服务类，交由Spring容器管理
import org.springframework.stereotype.Service;
// Spring Web组件：文件上传组件
import org.springframework.web.multipart.MultipartFile;

// Java文件类
import java.io.File;
// Java IO异常
import java.io.IOException;
// Java时间Duration类
import java.time.Duration;
// Java集合类
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * OSS对象存储服务实现类
 * 实现文件上传、下载、删除等核心业务逻辑
 * 同时实现OssService接口，为其他模块提供OSS操作服务
 *
 * @author Lion Li
 */
// Lombok注解：自动生成包含所有final字段的构造函数，实现依赖注入
@RequiredArgsConstructor
// Spring服务注解：标记为Spring Bean，交由Spring容器管理
@Service
public class SysOssServiceImpl implements ISysOssService, OssService {

    // OSS Mapper，用于OSS对象数据的持久化操作
    private final SysOssMapper baseMapper;

    /**
     * 分页查询OSS对象存储列表
     * 根据查询条件分页查询OSS对象列表，并对私有桶的URL进行临时URL处理
     *
     * @param bo        OSS对象存储分页查询对象
     * @param pageQuery 分页查询实体类
     * @return 分页结果
     */
    @Override
    public TableDataInfo<SysOssVo> queryPageList(SysOssBo bo, PageQuery pageQuery) {
        // 构建查询条件
        LambdaQueryWrapper<SysOss> lqw = buildQueryWrapper(bo);
        // 调用Mapper执行分页查询，返回VO对象
        Page<SysOssVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        // 对查询结果中的每个OSS对象进行URL匹配处理（私有桶生成临时URL）
        List<SysOssVo> filterResult = StreamUtils.toList(result.getRecords(), this::matchingUrl);
        // 设置处理后的记录列表
        result.setRecords(filterResult);
        // 将MyBatis-Plus的Page对象转换为系统统一的TableDataInfo
        return TableDataInfo.build(result);
    }

    /**
     * 根据一组 ossIds 获取对应的 SysOssVo 列表
     * 批量查询OSS对象信息，并对私有桶的URL进行临时URL处理
     *
     * @param ossIds 一组文件在数据库中的唯一标识集合
     * @return 包含 SysOssVo 对象的列表
     */
    @Override
    public List<SysOssVo> listByIds(Collection<Long> ossIds) {
        // 创建OSS对象列表
        List<SysOssVo> list = new ArrayList<>();
        // 使用AOP代理获取当前类的代理对象，确保缓存注解生效
        SysOssServiceImpl ossService = SpringUtils.getAopProxy(this);
        // 遍历OSS ID集合
        for (Long id : ossIds) {
            // 调用代理对象的getById方法，确保@Cacheable注解生效
            SysOssVo vo = ossService.getById(id);
            // 如果OSS对象存在
            if (ObjectUtil.isNotNull(vo)) {
                try {
                    // 对URL进行匹配处理（私有桶生成临时URL）并添加到列表
                    list.add(this.matchingUrl(vo));
                } catch (Exception ignored) {
                    // 如果OSS异常无法连接则将数据直接返回（避免影响主流程）
                    list.add(vo);
                }
            }
        }
        // 返回OSS对象列表
        return list;
    }

    /**
     * 根据一组 ossIds 获取对应文件的 URL 列表
     * 批量查询OSS对象的URL，返回逗号分隔的URL字符串
     *
     * @param ossIds 以逗号分隔的 ossId 字符串
     * @return 以逗号分隔的文件 URL 字符串
     */
    @Override
    public String selectUrlByIds(String ossIds) {
        // 创建URL列表
        List<String> list = new ArrayList<>();
        // 使用AOP代理获取当前类的代理对象，确保缓存注解生效
        SysOssServiceImpl ossService = SpringUtils.getAopProxy(this);
        // 将逗号分隔的ossIds字符串转换为Long列表并遍历
        for (Long id : StringUtils.splitTo(ossIds, Convert::toLong)) {
            // 调用代理对象的getById方法，确保@Cacheable注解生效
            SysOssVo vo = ossService.getById(id);
            // 如果OSS对象存在
            if (ObjectUtil.isNotNull(vo)) {
                try {
                    // 对URL进行匹配处理（私有桶生成临时URL）并添加到列表
                    list.add(this.matchingUrl(vo).getUrl());
                } catch (Exception ignored) {
                    // 如果OSS异常无法连接则将原始URL直接返回（避免影响主流程）
                    list.add(vo.getUrl());
                }
            }
        }
        // 将URL列表转换为逗号分隔字符串
        return StringUtils.joinComma(list);
    }

    /**
     * 根据一组 ossIds 获取对应的 OssDTO 列表
     * 批量查询OSS对象信息，转换为OssDTO格式
     *
     * @param ossIds 以逗号分隔的 ossId 字符串
     * @return OssDTO列表
     */
    @Override
    public List<OssDTO> selectByIds(String ossIds) {
        // 创建OssDTO列表
        List<OssDTO> list = new ArrayList<>();
        // 将逗号分隔的ossIds字符串转换为Long列表并遍历
        for (Long id : StringUtils.splitTo(ossIds, Convert::toLong)) {
            // 使用AOP代理调用getById方法，确保@Cacheable注解生效
            SysOssVo vo = SpringUtils.getAopProxy(this).getById(id);
            // 如果OSS对象存在
            if (ObjectUtil.isNotNull(vo)) {
                try {
                    // 对URL进行匹配处理（私有桶生成临时URL）
                    vo.setUrl(this.matchingUrl(vo).getUrl());
                    // 转换为OssDTO并添加到列表
                    list.add(BeanUtil.toBean(vo, OssDTO.class));
                } catch (Exception ignored) {
                    // 如果OSS异常无法连接则将数据直接返回（避免影响主流程）
                    list.add(BeanUtil.toBean(vo, OssDTO.class));
                }
            }
        }
        // 返回OssDTO列表
        return list;
    }

    /**
     * 构建OSS对象查询条件
     * 私有方法，封装通用查询逻辑，支持Lambda表达式
     *
     * @param bo OSS对象查询条件
     * @return LambdaQueryWrapper，类型安全，避免硬编码字段名
     */
    private LambdaQueryWrapper<SysOss> buildQueryWrapper(SysOssBo bo) {
        // 获取查询参数Map，包含beginCreateTime、endCreateTime等
        Map<String, Object> params = bo.getParams();
        // 使用Wrappers快速创建LambdaQueryWrapper
        LambdaQueryWrapper<SysOss> lqw = Wrappers.lambdaQuery();
        // 模糊查询文件名
        lqw.like(StringUtils.isNotBlank(bo.getFileName()), SysOss::getFileName, bo.getFileName());
        // 模糊查询原始文件名
        lqw.like(StringUtils.isNotBlank(bo.getOriginalName()), SysOss::getOriginalName, bo.getOriginalName());
        // 精确查询文件后缀
        lqw.eq(StringUtils.isNotBlank(bo.getFileSuffix()), SysOss::getFileSuffix, bo.getFileSuffix());
        // 精确查询URL
        lqw.eq(StringUtils.isNotBlank(bo.getUrl()), SysOss::getUrl, bo.getUrl());
        // 时间范围查询创建时间
        lqw.between(params.get("beginCreateTime") != null && params.get("endCreateTime") != null,
            SysOss::getCreateTime, params.get("beginCreateTime"), params.get("endCreateTime"));
        // 精确查询创建人
        lqw.eq(ObjectUtil.isNotNull(bo.getCreateBy()), SysOss::getCreateBy, bo.getCreateBy());
        // 精确查询OSS服务配置Key
        lqw.eq(StringUtils.isNotBlank(bo.getService()), SysOss::getService, bo.getService());
        // 按OSS对象ID升序排序
        lqw.orderByAsc(SysOss::getOssId);
        // 返回查询条件
        return lqw;
    }

    /**
     * 根据 ossId 从缓存或数据库中获取 SysOssVo 对象
     * 使用Spring Cache缓存，key为ossId，提升查询性能
     *
     * @param ossId 文件在数据库中的唯一标识
     * @return SysOssVo 对象，包含文件信息
     */
    // Spring缓存注解：查询时缓存结果，key为ossId
    @Cacheable(cacheNames = CacheNames.SYS_OSS, key = "#ossId")
    @Override
    public SysOssVo getById(Long ossId) {
        // 调用Mapper根据ID查询VO对象
        return baseMapper.selectVoById(ossId);
    }

    /**
     * 文件下载方法，支持一次性下载完整文件
     * 根据OSS对象ID下载文件，设置响应头，通过OSS客户端将文件内容写入响应流
     *
     * @param ossId    OSS对象ID
     * @param response HttpServletResponse对象，用于设置响应头和向客户端发送文件内容
     */
    @Override
    public void download(Long ossId, HttpServletResponse response) throws IOException {
        // 使用AOP代理调用getById方法，确保@Cacheable注解生效
        SysOssVo sysOss = SpringUtils.getAopProxy(this).getById(ossId);
        // 如果OSS对象不存在，抛出业务异常
        if (ObjectUtil.isNull(sysOss)) {
            throw new ServiceException("文件数据不存在!");
        }
        // 设置响应头，指定下载文件名
        FileUtils.setAttachmentResponseHeader(response, sysOss.getOriginalName());
        // 设置响应内容类型为二进制流
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE + "; charset=UTF-8");
        // 获取OSS客户端实例
        OssClient storage = OssFactory.instance(sysOss.getService());
        // 调用OSS客户端下载方法，将文件内容写入响应输出流
        storage.download(sysOss.getFileName(), response.getOutputStream(), response::setContentLengthLong);
    }

    /**
     * 上传 MultipartFile 到对象存储服务，并保存文件信息到数据库
     * 处理MultipartFile文件上传，生成文件后缀，调用OSS客户端上传，保存文件信息到数据库
     *
     * @param file 要上传的 MultipartFile 对象
     * @return 上传成功后的 SysOssVo 对象，包含文件信息
     * @throws ServiceException 如果上传过程中发生异常，则抛出 ServiceException 异常
     */
    @Override
    public SysOssVo upload(MultipartFile file) {
        // 获取原始文件名
        String originalfileName = file.getOriginalFilename();
        // 提取文件后缀（从最后一个点开始到末尾）
        String suffix = StringUtils.substring(originalfileName, originalfileName.lastIndexOf("."), originalfileName.length());
        // 获取OSS客户端实例（使用默认配置）
        OssClient storage = OssFactory.instance();
        // 上传结果
        UploadResult uploadResult;
        try {
            // 调用OSS客户端上传方法，传入文件字节数组、后缀和内容类型
            uploadResult = storage.uploadSuffix(file.getBytes(), suffix, file.getContentType());
        } catch (IOException e) {
            // IO异常转换为业务异常
            throw new ServiceException(e.getMessage());
        }
        // 创建OSS扩展信息对象
        SysOssExt ext1 = new SysOssExt();
        // 设置文件大小
        ext1.setFileSize(file.getSize());
        // 设置内容类型
        ext1.setContentType(file.getContentType());
        // 保存文件信息到数据库
        return buildResultEntity(originalfileName, suffix, storage.getConfigKey(), uploadResult, ext1);
    }

    /**
     * 上传文件到对象存储服务，并保存文件信息到数据库
     * 处理File对象上传，生成文件后缀，调用OSS客户端上传，保存文件信息到数据库
     *
     * @param file 要上传的文件对象
     * @return 上传成功后的 SysOssVo 对象，包含文件信息
     */
    @Override
    public SysOssVo upload(File file) {
        // 获取文件名
        String originalfileName = file.getName();
        // 提取文件后缀（从最后一个点开始到末尾）
        String suffix = StringUtils.substring(originalfileName, originalfileName.lastIndexOf("."), originalfileName.length());
        // 获取OSS客户端实例
        OssClient storage = OssFactory.instance();
        // 调用OSS客户端上传方法
        UploadResult uploadResult = storage.uploadSuffix(file, suffix);
        // 创建OSS扩展信息对象
        SysOssExt ext1 = new SysOssExt();
        // 设置文件大小
        ext1.setFileSize(file.length());
        // 保存文件信息到数据库
        return buildResultEntity(originalfileName, suffix, storage.getConfigKey(), uploadResult, ext1);
    }

    /**
     * 构建OSS结果实体
     * 将上传结果转换为SysOss实体，插入数据库，并返回VO对象
     *
     * @param originalfileName 原始文件名
     * @param suffix           文件后缀
     * @param configKey        OSS配置Key
     * @param uploadResult     上传结果
     * @param ext1             OSS扩展信息
     * @return SysOssVo对象
     */
    @NotNull
    private SysOssVo buildResultEntity(String originalfileName, String suffix, String configKey, UploadResult uploadResult, SysOssExt ext1) {
        // 创建OSS实体对象
        SysOss oss = new SysOss();
        // 设置URL
        oss.setUrl(uploadResult.getUrl());
        // 设置文件后缀
        oss.setFileSuffix(suffix);
        // 设置文件名
        oss.setFileName(uploadResult.getFilename());
        // 设置原始文件名
        oss.setOriginalName(originalfileName);
        // 设置OSS服务配置Key
        oss.setService(configKey);
        // 将扩展信息序列化为JSON字符串
        oss.setExt1(JsonUtils.toJsonString(ext1));
        // 插入数据库
        baseMapper.insert(oss);
        // 将实体转换为VO对象
        SysOssVo sysOssVo = MapstructUtils.convert(oss, SysOssVo.class);
        // 对URL进行匹配处理（私有桶生成临时URL）
        return this.matchingUrl(sysOssVo);
    }

    /**
     * 删除OSS对象存储
     * 批量删除OSS对象，先从OSS服务端删除文件，再删除数据库记录
     *
     * @param ids     OSS对象ID集合
     * @param isValid 判断是否需要校验
     * @return 是否成功
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        // 如果需要校验（预留扩展点）
        if (isValid) {
            // 做一些业务上的校验,判断是否需要校验
        }
        // 查询要删除的OSS对象列表
        List<SysOss> list = baseMapper.selectByIds(ids);
        // 遍历OSS对象列表
        for (SysOss sysOss : list) {
            // 获取OSS客户端实例
            OssClient storage = OssFactory.instance(sysOss.getService());
            // 从OSS服务端删除文件
            storage.delete(sysOss.getUrl());
        }
        // 删除数据库记录
        return baseMapper.deleteByIds(ids) > 0;
    }

    /**
     * 桶类型为 private 的URL 修改为临时URL时长为120s
     * 仅对私有桶的URL生成临时访问URL，有效期120秒，提升安全性
     *
     * @param oss OSS对象
     * @return oss 匹配Url的OSS对象
     */
    private SysOssVo matchingUrl(SysOssVo oss) {
        // 获取OSS客户端实例
        OssClient storage = OssFactory.instance(oss.getService());
        // 仅修改桶类型为 private 的URL，临时URL时长为120s
        if (AccessPolicyType.PRIVATE == storage.getAccessPolicy()) {
            // 生成临时URL，有效期120秒
            oss.setUrl(storage.getPrivateUrl(oss.getFileName(), Duration.ofSeconds(120)));
        }
        // 返回处理后的OSS对象
        return oss;
    }
}
