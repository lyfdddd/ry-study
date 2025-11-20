package org.dromara.generator.service;

// 导入Hutool的集合工具类，提供集合操作增强功能
import cn.hutool.core.collection.CollUtil;
// 导入Hutool的IO工具类，提供流操作、文件读写等功能
import cn.hutool.core.io.IoUtil;
// 导入Hutool的字典类，类似Map但支持链式调用和类型转换
import cn.hutool.core.lang.Dict;
// 导入Hutool的对象工具类，提供对象判空、默认值等操作
import cn.hutool.core.util.ObjectUtil;
// 导入动态数据源注解，用于指定方法使用的数据源
import com.baomidou.dynamic.datasource.annotation.DS;
// 导入动态数据源事务注解，确保跨数据源操作的事务一致性
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
// 导入MyBatis-Plus的Lambda查询包装器，支持类型安全的字段引用
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
// 导入MyBatis-Plus的查询包装器，用于构建SQL查询条件
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
// 导入MyBatis-Plus的ID生成器接口，用于生成主键ID
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
// 导入MyBatis-Plus的Wrappers工具类，快速创建查询条件
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
// 导入MyBatis-Plus的分页插件，支持物理分页
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
// 导入Lombok的RequiredArgsConstructor注解，自动生成包含final字段的构造函数
import lombok.RequiredArgsConstructor;
// 导入Lombok的Slf4j注解，自动生成日志对象
import lombok.extern.slf4j.Slf4j;
// 导入Anyline的列元数据类，用于获取数据库列信息
import org.anyline.metadata.Column;
// 导入Anyline的表元数据类，用于获取数据库表信息
import org.anyline.metadata.Table;
// 导入Anyline的服务代理类，用于获取元数据服务
import org.anyline.proxy.ServiceProxy;
// 导入Velocity模板类，用于渲染模板
import org.apache.velocity.Template;
// 导入Velocity上下文类，存储模板变量
import org.apache.velocity.VelocityContext;
// 导入Velocity引擎类，用于加载和渲染模板
import org.apache.velocity.app.Velocity;
// 导入系统常量类，包含UTF8等常量
import org.dromara.common.core.constant.Constants;
// 导入业务异常类，用于抛出业务逻辑异常
import org.dromara.common.core.exception.ServiceException;
// 导入Spring工具类，用于获取Spring容器中的Bean
import org.dromara.common.core.utils.SpringUtils;
// 导入Stream工具类，提供流式操作增强
import org.dromara.common.core.utils.StreamUtils;
// 导入字符串工具类，提供字符串操作增强
import org.dromara.common.core.utils.StringUtils;
// 导入文件工具类，提供文件读写操作
import org.dromara.common.core.utils.file.FileUtils;
// 导入JSON工具类，提供JSON序列化/反序列化
import org.dromara.common.json.utils.JsonUtils;
// 导入分页查询类，封装分页参数
import org.dromara.common.mybatis.core.page.PageQuery;
// 导入表格数据信息类，封装分页结果
import org.dromara.common.mybatis.core.page.TableDataInfo;
// 导入代码生成常量类，包含各种配置常量
import org.dromara.generator.constant.GenConstants;
// 导入代码生成表实体类
import org.dromara.generator.domain.GenTable;
// 导入代码生成表字段实体类
import org.dromara.generator.domain.GenTableColumn;
// 导入代码生成表Mapper
import org.dromara.generator.mapper.GenTableColumnMapper;
// 导入代码生成表Mapper
import org.dromara.generator.mapper.GenTableMapper;
// 导入代码生成工具类
import org.dromara.generator.util.GenUtils;
// 导入Velocity初始化工具类
import org.dromara.generator.util.VelocityInitializer;
// 导入Velocity工具类
import org.dromara.generator.util.VelocityUtils;
// 导入Spring的Service注解，标记为服务层组件
import org.springframework.stereotype.Service;
// 导入Spring的事务注解，确保方法在事务中执行
import org.springframework.transaction.annotation.Transactional;

// 导入字节数组输出流，用于内存中构建字节数组
import java.io.ByteArrayOutputStream;
// 导入文件类，用于文件操作
import java.io.File;
// 导入IO异常类
import java.io.IOException;
// 导入字符串写入器，用于将数据写入字符串
import java.io.StringWriter;
// 导入标准字符集类，指定UTF-8编码
import java.nio.charset.StandardCharsets;
// 导入集合工具类
import java.util.*;
// 导入ZIP条目类，表示ZIP文件中的一个条目
import java.util.zip.ZipEntry;
// 导入ZIP输出流，用于创建ZIP文件
import java.util.zip.ZipOutputStream;

/**
 * 代码生成业务 服务层实现
 * 提供代码生成、表结构导入、模板渲染等核心功能
 * 使用@RequiredArgsConstructor注解实现构造函数注入
 *
 * @author Lion Li
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class GenTableServiceImpl implements IGenTableService {

    /**
     * 代码生成表Mapper
     * 用于操作gen_table表的数据访问对象
     */
    private final GenTableMapper baseMapper;
    /**
     * 代码生成表字段Mapper
     * 用于操作gen_table_column表的数据访问对象
     */
    private final GenTableColumnMapper genTableColumnMapper;
    /**
     * ID生成器
     * MyBatis-Plus提供的分布式ID生成器，用于生成主键ID
     */
    private final IdentifierGenerator identifierGenerator;

    /**
     * 需要忽略的表前缀数组
     * 代码生成时自动过滤以这些前缀开头的表（如sj_、flow_、gen_）
     * 避免生成系统表和工作流表的代码
     */
    private static final String[] TABLE_IGNORE = new String[]{"sj_", "flow_", "gen_"};

    /**
     * 查询业务字段列表
     * 根据表ID查询关联的字段配置信息，并按排序号升序排列
     *
     * @param tableId 业务字段编号（gen_table表的table_id）
     * @return 业务字段集合（GenTableColumn列表）
     */
    @Override
    public List<GenTableColumn> selectGenTableColumnListByTableId(Long tableId) {
        // 使用MyBatis-Plus的LambdaQueryWrapper构建查询条件
        // eq方法添加等值条件：table_id = 参数值
        // orderByAsc方法添加升序排序：按sort字段排序
        return genTableColumnMapper.selectList(new LambdaQueryWrapper<GenTableColumn>()
            .eq(GenTableColumn::getTableId, tableId)
            .orderByAsc(GenTableColumn::getSort));
    }

    /**
     * 查询业务信息
     * 根据ID查询代码生成配置信息，并解析options字段中的扩展配置
     *
     * @param id 业务ID（gen_table表的table_id）
     * @return 业务信息（GenTable对象，包含解析后的树形配置、上级菜单等）
     */
    @Override
    public GenTable selectGenTableById(Long id) {
        // 调用Mapper查询基础信息
        GenTable genTable = baseMapper.selectGenTableById(id);
        // 解析options字段中的JSON配置，设置树形结构、上级菜单等属性
        setTableFromOptions(genTable);
        return genTable;
    }

    /**
     * 查询代码生成配置列表（分页）
     * 根据查询条件分页查询已配置的代码生成表信息
     *
     * @param genTable 查询条件（包含dataName、tableName、tableComment等）
     * @param pageQuery 分页参数（页码、每页条数）
     * @return 分页结果（TableDataInfo对象，包含记录列表和总数）
     */
    @Override
    public TableDataInfo<GenTable> selectPageGenTableList(GenTable genTable, PageQuery pageQuery) {
        // 构建查询条件并执行分页查询
        // pageQuery.build()创建Page对象，MyBatis-Plus自动进行物理分页
        Page<GenTable> page = baseMapper.selectPage(pageQuery.build(), this.buildGenTableQueryWrapper(genTable));
        // 将Page对象转换为TableDataInfo，统一响应格式
        return TableDataInfo.build(page);
    }

    /**
     * 构建代码生成表查询条件
     * 根据GenTable对象中的属性动态构建MyBatis-Plus的QueryWrapper
     * 支持按数据源名称、表名、表注释、创建时间范围查询
     *
     * @param genTable 查询条件对象
     * @return QueryWrapper查询包装器
     */
    private QueryWrapper<GenTable> buildGenTableQueryWrapper(GenTable genTable) {
        // 获取查询参数Map（用于时间范围查询）
        Map<String, Object> params = genTable.getParams();
        // 使用Wrappers.query()快速创建QueryWrapper
        QueryWrapper<GenTable> wrapper = Wrappers.query();
        // 动态添加查询条件：如果dataName不为空，添加等值查询
        wrapper
            .eq(StringUtils.isNotEmpty(genTable.getDataName()), "data_name", genTable.getDataName())
            // 动态添加模糊查询：如果tableName不为空，添加表名模糊查询（转小写比较，忽略大小写）
            .like(StringUtils.isNotBlank(genTable.getTableName()), "lower(table_name)", StringUtils.lowerCase(genTable.getTableName()))
            // 动态添加模糊查询：如果tableComment不为空，添加表注释模糊查询（转小写比较）
            .like(StringUtils.isNotBlank(genTable.getTableComment()), "lower(table_comment)", StringUtils.lowerCase(genTable.getTableComment()))
            // 动态添加时间范围查询：如果beginTime和endTime都不为空，添加创建时间范围查询
            .between(params.get("beginTime") != null && params.get("endTime") != null,
                "create_time", params.get("beginTime"), params.get("endTime"))
            // 添加排序条件：按update_time降序排列
            .orderByDesc("update_time");
        return wrapper;
    }

    /**
     * 查询数据库表列表（分页）
     * 从指定数据源中查询所有表信息，支持按表名和表注释模糊查询
     * 过滤已配置和系统表，返回分页结果
     *
     * @param genTable  包含查询条件的GenTable对象（dataName指定数据源）
     * @param pageQuery 包含分页信息的PageQuery对象
     * @return 包含分页结果的TableDataInfo对象
     */
    // 使用@DS注解指定动态数据源，#genTable.dataName表示从参数genTable的dataName属性获取数据源名称
    @DS("#genTable.dataName")
    @Override
    public TableDataInfo<GenTable> selectPageDbTableList(GenTable genTable, PageQuery pageQuery) {
        // 获取查询条件：表名模糊查询关键字
        String tableName = genTable.getTableName();
        // 获取查询条件：表注释模糊查询关键字
        String tableComment = genTable.getTableComment();

        // 通过Anyline的ServiceProxy获取当前数据源的所有表元数据
        LinkedHashMap<String, Table<?>> tablesMap = ServiceProxy.metadata().tables();
        // 如果表元数据为空，返回空的分页结果
        if (CollUtil.isEmpty(tablesMap)) {
            return TableDataInfo.build();
        }
        // 查询已配置的表名列表，用于过滤已配置的表
        List<String> tableNames = baseMapper.selectTableNameList(genTable.getDataName());
        // 将List转换为数组，用于后续过滤
        String[] tableArrays;
        if (CollUtil.isNotEmpty(tableNames)) {
            tableArrays = tableNames.toArray(new String[0]);
        } else {
            tableArrays = new String[0];
        }
        // 过滤并转换表格数据：使用Stream API进行多条件过滤和映射
        List<GenTable> tables = tablesMap.values().stream()
            // 过滤系统表：排除以sj_、flow_、gen_开头的表
            .filter(x -> !StringUtils.startWithAnyIgnoreCase(x.getName(), TABLE_IGNORE))
            // 过滤已配置的表：如果已配置表名列表不为空，排除已配置的表
            .filter(x -> {
                if (CollUtil.isEmpty(tableNames)) {
                    return true;
                }
                return !StringUtils.equalsAnyIgnoreCase(x.getName(), tableArrays);
            })
            // 模糊查询：根据表名和表注释进行模糊匹配
            .filter(x -> {
                // 初始化匹配标志为true
                boolean nameMatches = true;
                boolean commentMatches = true;
                // 进行表名称的模糊查询：如果tableName不为空，判断表名是否包含关键字（忽略大小写）
                if (StringUtils.isNotBlank(tableName)) {
                    nameMatches = StringUtils.containsIgnoreCase(x.getName(), tableName);
                }
                // 进行表描述的模糊查询：如果tableComment不为空，判断表注释是否包含关键字（忽略大小写）
                if (StringUtils.isNotBlank(tableComment)) {
                    commentMatches = StringUtils.containsIgnoreCase(x.getComment(), tableComment);
                }
                // 同时匹配名称和描述：只有两者都匹配才返回true
                return nameMatches && commentMatches;
            })
            // 映射转换：将Anyline的Table对象转换为GenTable对象
            .map(x -> {
                GenTable gen = new GenTable();
                // 设置表名
                gen.setTableName(x.getName());
                // 设置表注释
                gen.setTableComment(x.getComment());
                // postgresql的表元数据没有创建时间这个东西(好奇葩) 只能new Date代替
                // 使用ObjectUtil.defaultIfNull提供默认值，如果创建时间为空则使用当前时间
                gen.setCreateTime(ObjectUtil.defaultIfNull(x.getCreateTime(), new Date()));
                // 设置更新时间
                gen.setUpdateTime(x.getUpdateTime());
                return gen;
            // 排序：按创建时间降序排列（最新的在前）
            }).sorted(Comparator.comparing(GenTable::getCreateTime).reversed())
            // 转换为List
            .toList();
        // 构建分页结果：手动分页（因为Anyline查询的结果需要内存中分页）
        return TableDataInfo.build(tables, pageQuery.build());
    }

    /**
     * 查询数据库表列表（根据表名数组）
     * 从指定数据源中查询指定表名的表信息，用于导入表结构
     *
     * @param tableNames 表名称数组
     * @param dataName   数据源名称
     * @return 数据库表集合（GenTable列表）
     */
    // 使用@DS注解指定动态数据源，#dataName表示从参数dataName获取数据源名称
    @DS("#dataName")
    @Override
    public List<GenTable> selectDbTableListByNames(String[] tableNames, String dataName) {
        // 将表名数组转换为Set，用于快速查找
        Set<String> tableNameSet = new HashSet<>(List.of(tableNames));
        // 通过Anyline获取当前数据源的所有表元数据
        LinkedHashMap<String, Table<?>> tablesMap = ServiceProxy.metadata().tables();

        // 如果表元数据为空，返回空列表
        if (CollUtil.isEmpty(tablesMap)) {
            return new ArrayList<>();
        }

        // 过滤表：排除系统表，只保留指定表名的表
        List<Table<?>> tableList = tablesMap.values().stream()
            // 过滤系统表：排除以sj_、flow_、gen_开头的表
            .filter(x -> !StringUtils.startWithAnyIgnoreCase(x.getName(), TABLE_IGNORE))
            // 过滤指定表名：只保留tableNames中指定的表
            .filter(x -> tableNameSet.contains(x.getName())).toList();

        // 如果过滤后结果为空，返回空列表
        if (CollUtil.isEmpty(tableList)) {
            return new ArrayList<>();
        }
        // 映射转换：将Anyline的Table对象转换为GenTable对象
        return tableList.stream().map(x -> {
            GenTable gen = new GenTable();
            // 设置数据源名称
            gen.setDataName(dataName);
            // 设置表名
            gen.setTableName(x.getName());
            // 设置表注释
            gen.setTableComment(x.getComment());
            // 设置创建时间
            gen.setCreateTime(x.getCreateTime());
            // 设置更新时间
            gen.setUpdateTime(x.getUpdateTime());
            return gen;
        // 转换为List
        }).toList();
    }

    /**
     * 查询所有已配置的表信息
     * 查询gen_table表中所有已配置的代码生成表信息
     *
     * @return 表信息集合（GenTable列表）
     */
    @Override
    public List<GenTable> selectGenTableAll() {
        // 调用Mapper查询所有表信息
        return baseMapper.selectGenTableAll();
    }

    /**
     * 修改代码生成配置
     * 更新gen_table表和gen_table_column表中的配置信息
     * 使用事务确保数据一致性
     *
     * @param genTable 业务信息（包含表配置和字段配置）
     */
    // 使用@Transactional注解确保事务一致性，rollbackFor = Exception.class表示任何异常都回滚
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateGenTable(GenTable genTable) {
        // 将params参数Map转换为JSON字符串，存储到options字段
        String options = JsonUtils.toJsonString(genTable.getParams());
        genTable.setOptions(options);
        // 更新主表信息
        int row = baseMapper.updateById(genTable);
        // 如果主表更新成功，更新字段列表
        if (row > 0) {
            // 遍历字段列表，逐个更新
            for (GenTableColumn cenTableColumn : genTable.getColumns()) {
                genTableColumnMapper.updateById(cenTableColumn);
            }
        }
    }

    /**
     * 删除代码生成配置
     * 删除gen_table表和gen_table_column表中的配置信息
     * 使用事务确保数据一致性
     *
     * @param tableIds 需要删除的数据ID数组
     */
    // 使用@Transactional注解确保事务一致性
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteGenTableByIds(Long[] tableIds) {
        // 将数组转换为List
        List<Long> ids = Arrays.asList(tableIds);
        // 删除主表记录
        baseMapper.deleteByIds(ids);
        // 删除关联的字段记录：使用LambdaQueryWrapper构建in条件
        genTableColumnMapper.delete(new LambdaQueryWrapper<GenTableColumn>().in(GenTableColumn::getTableId, ids));
    }

    /**
     * 导入表结构
     * 从指定数据源导入表结构到代码生成配置中
     * 包括表信息和字段信息，使用分布式事务确保跨数据源操作一致性
     *
     * @param tableList 导入表列表（包含表名、注释等基本信息）
     * @param dataName  数据源名称
     */
    // 使用@DSTransactional注解实现跨数据源事务（MyBatis-Plus动态数据源事务）
    @DSTransactional
    @Override
    public void importGenTable(List<GenTable> tableList, String dataName) {
        try {
            // 遍历需要导入的表列表
            for (GenTable table : tableList) {
                // 获取表名
                String tableName = table.getTableName();
                // 初始化表信息：设置类名、包路径、模块名、业务名、功能名、作者等
                GenUtils.initTable(table);
                // 设置数据源名称
                table.setDataName(dataName);
                // 插入主表记录
                int row = baseMapper.insert(table);
                // 如果插入成功，保存字段信息
                if (row > 0) {
                    // 通过AOP代理调用，确保@DS注解生效（解决同类方法调用导致注解失效问题）
                    List<GenTableColumn> genTableColumns = SpringUtils.getAopProxy(this).selectDbTableColumnsByName(tableName, dataName);
                    // 创建保存列表
                    List<GenTableColumn> saveColumns = new ArrayList<>();
                    // 遍历字段列表，初始化字段属性
                    for (GenTableColumn column : genTableColumns) {
                        // 初始化字段属性：Java字段名、Java类型、HTML控件类型、是否插入/编辑/列表/查询等
                        GenUtils.initColumnField(column, table);
                        // 添加到保存列表
                        saveColumns.add(column);
                    }
                    // 如果字段列表不为空，批量插入
                    if (CollUtil.isNotEmpty(saveColumns)) {
                        genTableColumnMapper.insertBatch(saveColumns);
                    }
                }
            }
        } catch (Exception e) {
            // 捕获异常并转换为ServiceException，提供友好的错误提示
            throw new ServiceException("导入失败：" + e.getMessage());
        }
    }

    /**
     * 根据表名称查询列信息
     * 从指定数据源查询表的字段结构信息
     * 使用Anyline框架获取元数据，转换为GenTableColumn对象
     *
     * @param tableName 表名称
     * @param dataName  数据源名称
     * @return 列信息（GenTableColumn列表）
     */
    // 使用@DS注解指定动态数据源，#dataName表示从参数dataName获取数据源名称
    @DS("#dataName")
    @Override
    public List<GenTableColumn> selectDbTableColumnsByName(String tableName, String dataName) {
        // 通过Anyline获取指定表的元数据
        Table<?> table = ServiceProxy.metadata().table(tableName);
        // 如果表不存在，返回空列表
        if (ObjectUtil.isNull(table)) {
            return new ArrayList<>();
        }
        // 获取表的所有列元数据
        LinkedHashMap<String, Column> columns = table.getColumns();
        // 创建返回列表
        List<GenTableColumn> tableColumns = new ArrayList<>();
        // 遍历列元数据，转换为GenTableColumn对象
        columns.forEach((columnName, column) -> {
            GenTableColumn tableColumn = new GenTableColumn();
            // 设置是否主键：通过Column的isPrimaryKey方法判断
            tableColumn.setIsPk(column.isPrimaryKey() ? "1" : "0");
            // 设置列名
            tableColumn.setColumnName(column.getName());
            // 设置列注释
            tableColumn.setColumnComment(column.getComment());
            // 设置列类型（转换为小写）
            tableColumn.setColumnType(column.getOriginType().toLowerCase());
            // 设置排序号（字段位置）
            tableColumn.setSort(column.getPosition());
            // 设置是否必填：通过Column的isNullable方法判断（反向）
            tableColumn.setIsRequired(column.isNullable() ? "0" : "1");
            // 设置是否自增：通过Column的isAutoIncrement方法判断
            tableColumn.setIsIncrement(column.isAutoIncrement() ? "1" : "0");
            // 添加到列表
            tableColumns.add(tableColumn);
        });
        return tableColumns;
    }

    /**
     * 预览代码
     * 根据表ID生成代码预览，返回所有模板渲染后的结果
     * 用于前端展示生成的代码内容，不写入文件
     *
     * @param tableId 表编号（gen_table表的table_id）
     * @return 预览数据Map，key为模板路径，value为渲染后的代码内容
     */
    @Override
    public Map<String, String> previewCode(Long tableId) {
        // 创建LinkedHashMap保持插入顺序
        Map<String, String> dataMap = new LinkedHashMap<>();
        // 查询表配置信息
        GenTable table = baseMapper.selectGenTableById(tableId);
        // 生成6个菜单ID（用于生成菜单SQL）
        List<Long> menuIds = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            // 使用ID生成器生成唯一ID
            menuIds.add(identifierGenerator.nextId(null).longValue());
        }
        // 设置菜单ID列表
        table.setMenuIds(menuIds);
        // 设置主键列信息
        setPkColumn(table);
        // 初始化Velocity引擎
        VelocityInitializer.initVelocity();

        // 准备Velocity上下文，设置所有模板变量
        VelocityContext context = VelocityUtils.prepareContext(table);

        // 获取模板列表（根据模板类型：crud/tree）
        List<String> templates = VelocityUtils.getTemplateList(table.getTplCategory());
        // 遍历模板列表，逐个渲染
        for (String template : templates) {
            // 渲染模板：创建StringWriter用于接收渲染结果
            StringWriter sw = new StringWriter();
            // 获取Velocity模板对象，指定UTF-8编码
            Template tpl = Velocity.getTemplate(template, Constants.UTF8);
            // 合并上下文并渲染到StringWriter
            tpl.merge(context, sw);
            // 将渲染结果放入Map，key为模板路径
            dataMap.put(template, sw.toString());
        }
        return dataMap;
    }

    /**
     * 生成代码（下载方式）
     * 根据表ID生成代码并打包为ZIP文件，返回字节数组供下载
     *
     * @param tableId 表名称（gen_table表的table_id）
     * @return ZIP文件字节数组
     */
    @Override
    public byte[] downloadCode(Long tableId) {
        // 创建字节数组输出流，用于在内存中构建ZIP文件
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        // 创建ZIP输出流，包装字节数组输出流
        ZipOutputStream zip = new ZipOutputStream(outputStream);
        // 调用生成代码方法，将结果写入ZIP
        generatorCode(tableId, zip);
        // 关闭ZIP输出流（Hutool的IoUtil自动处理异常）
        IoUtil.close(zip);
        // 返回ZIP文件字节数组
        return outputStream.toByteArray();
    }

    /**
     * 生成代码（自定义路径）
     * 根据表ID生成代码并写入指定路径（不生成前端代码和SQL）
     * 用于直接生成到项目源码目录
     *
     * @param tableId 表名称（gen_table表的table_id）
     */
    @Override
    public void generatorCode(Long tableId) {
        // 查询表配置信息
        GenTable table = baseMapper.selectGenTableById(tableId);
        // 设置主键列信息
        setPkColumn(table);

        // 初始化Velocity引擎
        VelocityInitializer.initVelocity();

        // 准备Velocity上下文，设置所有模板变量
        VelocityContext context = VelocityUtils.prepareContext(table);

        // 获取模板列表（根据模板类型：crud/tree）
        List<String> templates = VelocityUtils.getTemplateList(table.getTplCategory());
        // 遍历模板列表
        for (String template : templates) {
            // 过滤掉SQL和前端模板：只生成Java代码（Domain、Mapper、Service、Controller）
            if (!StringUtils.containsAny(template, "sql.vm", "api.ts.vm", "types.ts.vm", "index.vue.vm", "index-tree.vue.vm")) {
                // 渲染模板：创建StringWriter用于接收渲染结果
                StringWriter sw = new StringWriter();
                // 获取Velocity模板对象，指定UTF-8编码
                Template tpl = Velocity.getTemplate(template, Constants.UTF8);
                // 合并上下文并渲染到StringWriter
                tpl.merge(context, sw);
                try {
                    // 获取生成路径
                    String path = getGenPath(table, template);
                    // 将渲染结果写入文件（UTF-8编码）
                    FileUtils.writeUtf8String(sw.toString(), path);
                } catch (Exception e) {
                    // 捕获异常并抛出业务异常，提供友好的错误提示
                    throw new ServiceException("渲染模板失败，表名：" + table.getTableName());
                }
            }
        }
    }

    /**
     * 同步数据库
     * 将数据库表结构的变更同步到代码生成配置中
     * 包括新增字段、删除字段、修改字段属性等
     * 保留用户已配置的查询方式、字典类型、必填/显示类型等设置
     *
     * @param tableId 表名称（gen_table表的table_id）
     */
    // 使用@DSTransactional注解实现跨数据源事务
    @DSTransactional
    @Override
    public void synchDb(Long tableId) {
        // 查询当前配置信息
        GenTable table = baseMapper.selectGenTableById(tableId);
        // 获取当前配置的字段列表
        List<GenTableColumn> tableColumns = table.getColumns();
        // 将字段列表转换为Map，key为列名，value为GenTableColumn对象，方便快速查找
        Map<String, GenTableColumn> tableColumnMap = StreamUtils.toIdentityMap(tableColumns, GenTableColumn::getColumnName);

        // 通过AOP代理调用，确保@DS注解生效，查询数据库中的最新字段结构
        List<GenTableColumn> dbTableColumns = SpringUtils.getAopProxy(this).selectDbTableColumnsByName(table.getTableName(), table.getDataName());
        // 如果查询结果为空，抛出异常
        if (CollUtil.isEmpty(dbTableColumns)) {
            throw new ServiceException("同步数据失败，原表结构不存在");
        }
        // 提取数据库中的列名列表
        List<String> dbTableColumnNames = StreamUtils.toList(dbTableColumns, GenTableColumn::getColumnName);

        // 创建保存列表
        List<GenTableColumn> saveColumns = new ArrayList<>();
        // 遍历数据库中的字段列表
        dbTableColumns.forEach(column -> {
            // 初始化字段属性：Java字段名、Java类型、HTML控件类型等
            GenUtils.initColumnField(column, table);
            // 如果该字段在配置中已存在，保留用户配置
            if (tableColumnMap.containsKey(column.getColumnName())) {
                // 获取原配置中的字段信息
                GenTableColumn prevColumn = tableColumnMap.get(column.getColumnName());
                // 设置字段ID（更新时需要）
                column.setColumnId(prevColumn.getColumnId());
                // 如果是列表字段，保留用户配置的查询方式和字典类型
                if (column.isList()) {
                    // 保留字典类型
                    column.setDictType(prevColumn.getDictType());
                    // 保留查询方式
                    column.setQueryType(prevColumn.getQueryType());
                }
                // 如果是(新增/修改&非主键/非忽略及父属性)，保留必填和显示类型配置
                if (StringUtils.isNotEmpty(prevColumn.getIsRequired()) && !column.isPk()
                    && (column.isInsert() || column.isEdit())
                    && ((column.isUsableColumn()) || (!column.isSuperColumn()))) {
                    // 保留必填配置
                    column.setIsRequired(prevColumn.getIsRequired());
                    // 保留显示类型配置
                    column.setHtmlType(prevColumn.getHtmlType());
                }
            }
            // 添加到保存列表
            saveColumns.add(column);
        });
        // 如果保存列表不为空，批量插入或更新
        if (CollUtil.isNotEmpty(saveColumns)) {
            // 使用MyBatis-Plus的insertOrUpdateBatch方法，根据ID判断插入或更新
            genTableColumnMapper.insertOrUpdateBatch(saveColumns);
        }
        // 找出已删除的字段：在配置中存在但在数据库中不存在的字段
        List<GenTableColumn> delColumns = StreamUtils.filter(tableColumns, column -> !dbTableColumnNames.contains(column.getColumnName()));
        // 如果存在需要删除的字段
        if (CollUtil.isNotEmpty(delColumns)) {
            // 提取字段ID列表
            List<Long> ids = StreamUtils.toList(delColumns, GenTableColumn::getColumnId);
            // 如果ID列表不为空，批量删除
            if (CollUtil.isNotEmpty(ids)) {
                genTableColumnMapper.deleteByIds(ids);
            }
        }
    }

    /**
     * 批量生成代码（下载方式）
     * 根据多个表ID批量生成代码并打包为ZIP文件
     *
     * @param tableIds 表ID数组
     * @return ZIP文件字节数组
     */
    @Override
    public byte[] downloadCode(String[] tableIds) {
        // 创建字节数组输出流
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        // 创建ZIP输出流
        ZipOutputStream zip = new ZipOutputStream(outputStream);
        // 遍历表ID数组，逐个生成代码并写入ZIP
        for (String tableId : tableIds) {
            generatorCode(Long.parseLong(tableId), zip);
        }
        // 关闭ZIP输出流
        IoUtil.close(zip);
        // 返回ZIP文件字节数组
        return outputStream.toByteArray();
    }

    /**
     * 查询表信息并生成代码（写入ZIP）
     * 内部方法，被downloadCode方法调用
     * 查询表信息、准备模板上下文、渲染模板、写入ZIP文件
     *
     * @param tableId 表ID
     * @param zip ZIP输出流
     */
    private void generatorCode(Long tableId, ZipOutputStream zip) {
        // 查询表配置信息
        GenTable table = baseMapper.selectGenTableById(tableId);
        // 生成6个菜单ID（用于生成菜单SQL）
        List<Long> menuIds = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            menuIds.add(identifierGenerator.nextId(null).longValue());
        }
        // 设置菜单ID列表
        table.setMenuIds(menuIds);
        // 设置主键列信息
        setPkColumn(table);

        // 初始化Velocity引擎
        VelocityInitializer.initVelocity();

        // 准备Velocity上下文
        VelocityContext context = VelocityUtils.prepareContext(table);

        // 获取模板列表
        List<String> templates = VelocityUtils.getTemplateList(table.getTplCategory());
        // 遍历模板列表
        for (String template : templates) {
            // 渲染模板
            StringWriter sw = new StringWriter();
            Template tpl = Velocity.getTemplate(template, Constants.UTF8);
            tpl.merge(context, sw);
            try {
                // 添加到zip：创建ZIP条目
                zip.putNextEntry(new ZipEntry(VelocityUtils.getFileName(template, table)));
                // 将渲染结果写入ZIP（UTF-8编码）
                IoUtil.write(zip, StandardCharsets.UTF_8, false, sw.toString());
                // 关闭StringWriter
                IoUtil.close(sw);
                // 刷新ZIP输出流
                zip.flush();
                // 关闭当前ZIP条目
                zip.closeEntry();
            } catch (IOException e) {
                // 记录错误日志，不影响其他表的生成
                log.error("渲染模板失败，表名：" + table.getTableName(), e);
            }
        }
    }

    /**
     * 修改保存参数校验
     * 校验代码生成配置参数的正确性
     * 树表模板需要额外校验树编码、树父编码、树名称字段
     *
     * @param genTable 业务信息（包含模板类型和配置参数）
     */
    @Override
    public void validateEdit(GenTable genTable) {
        // 如果是树表模板，需要校验树形结构配置
        if (GenConstants.TPL_TREE.equals(genTable.getTplCategory())) {
            // 将params参数Map转换为JSON字符串
            String options = JsonUtils.toJsonString(genTable.getParams());
            // 解析为Dict对象
            Dict paramsObj = JsonUtils.parseMap(options);
            // 校验树编码字段不能为空
            if (StringUtils.isEmpty(paramsObj.getStr(GenConstants.TREE_CODE))) {
                throw new ServiceException("树编码字段不能为空");
            // 校验树父编码字段不能为空
            } else if (StringUtils.isEmpty(paramsObj.getStr(GenConstants.TREE_PARENT_CODE))) {
                throw new ServiceException("树父编码字段不能为空");
            // 校验树名称字段不能为空
            } else if (StringUtils.isEmpty(paramsObj.getStr(GenConstants.TREE_NAME))) {
                throw new ServiceException("树名称字段不能为空");
            }
        }
    }

    /**
     * 设置主键列信息
     * 从字段列表中查找主键字段，并设置到GenTable的pkColumn属性
     * 如果没有找到主键，默认将第一个字段作为主键
     *
     * @param table 业务表信息（GenTable对象，包含字段列表）
     */
    public void setPkColumn(GenTable table) {
        // 遍历字段列表
        for (GenTableColumn column : table.getColumns()) {
            // 判断是否为是主键
            if (column.isPk()) {
                // 设置主键列
                table.setPkColumn(column);
                // 找到主键后跳出循环
                break;
            }
        }
        // 如果没有找到主键，默认将第一个字段作为主键
        if (ObjectUtil.isNull(table.getPkColumn())) {
            table.setPkColumn(table.getColumns().get(0));
        }

    }

    /**
     * 设置代码生成其他选项值
     * 从options字段（JSON格式）中解析树形结构配置和上级菜单配置
     * 设置到GenTable对象的对应属性中
     *
     * @param genTable 设置后的生成对象（GenTable对象，包含options字段）
     */
    public void setTableFromOptions(GenTable genTable) {
        // 解析options字段为Dict对象
        Dict paramsObj = JsonUtils.parseMap(genTable.getOptions());
        // 如果解析结果不为空
        if (ObjectUtil.isNotNull(paramsObj)) {
            // 获取树编码字段
            String treeCode = paramsObj.getStr(GenConstants.TREE_CODE);
            // 获取树父编码字段
            String treeParentCode = paramsObj.getStr(GenConstants.TREE_PARENT_CODE);
            // 获取树名称字段
            String treeName = paramsObj.getStr(GenConstants.TREE_NAME);
            // 获取上级菜单ID
            Long parentMenuId = paramsObj.getLong(GenConstants.PARENT_MENU_ID);
            // 获取上级菜单名称
            String parentMenuName = paramsObj.getStr(GenConstants.PARENT_MENU_NAME);

            // 设置到GenTable对象
            genTable.setTreeCode(treeCode);
            genTable.setTreeParentCode(treeParentCode);
            genTable.setTreeName(treeName);
            genTable.setParentMenuId(parentMenuId);
            genTable.setParentMenuName(parentMenuName);
        }
    }

    /**
     * 获取代码生成地址
     * 根据模板类型和表配置生成完整的文件路径
     * 支持自定义生成路径和默认项目路径
     *
     * @param table    业务表信息（包含生成路径配置）
     * @param template 模板文件路径
     * @return 完整的文件生成路径
     */
    public static String getGenPath(GenTable table, String template) {
        // 获取配置的生成路径
        String genPath = table.getGenPath();
        // 如果生成路径为"/"，表示使用默认项目路径
        if (StringUtils.equals(genPath, "/")) {
            // 返回：当前项目路径 + src + 相对文件路径
            return System.getProperty("user.dir") + File.separator + "src" + File.separator + VelocityUtils.getFileName(template, table);
        }
        // 否则返回：自定义路径 + 相对文件路径
        return genPath + File.separator + VelocityUtils.getFileName(template, table);
    }
}

