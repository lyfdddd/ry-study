// 部门管理业务层接口，定义部门管理相关的业务方法
package org.dromara.system.service;

// Hutool树形结构工具类
import cn.hutool.core.lang.tree.Tree;
// MyBatis-Plus分页查询对象
import org.dromara.common.mybatis.core.page.PageQuery;
// MyBatis-Plus表格数据信息封装类
import org.dromara.common.mybatis.core.page.TableDataInfo;
// 部门业务对象
import org.dromara.system.domain.bo.SysDeptBo;
// 部门视图对象
import org.dromara.system.domain.vo.SysDeptVo;

// Java集合类
import java.util.List;

/**
 * 部门管理业务层接口
 * 定义部门管理相关的业务方法，包括部门查询、新增、修改、删除、树形结构构建等操作
 *
 * @author Lion Li
 */
public interface ISysDeptService {

    /**
     * 分页查询部门管理数据
     * 支持按部门名称、状态、父部门ID等条件筛选，并分页返回结果
     *
     * @param dept      部门信息查询条件
     * @param pageQuery 分页参数（页码、每页条数）
     * @return 部门信息集合
     */
    TableDataInfo<SysDeptVo> selectPageDeptList(SysDeptBo dept, PageQuery pageQuery);

    /**
     * 查询部门管理数据
     * 查询所有符合条件的部门列表，不分页
     *
     * @param dept 部门信息查询条件
     * @return 部门信息集合
     */
    List<SysDeptVo> selectDeptList(SysDeptBo dept);

    /**
     * 查询部门树结构信息
     * 查询部门列表并构建成树形结构，用于前端树形展示
     *
     * @param dept 部门信息查询条件
     * @return 部门树信息集合
     */
    List<Tree<Long>> selectDeptTreeList(SysDeptBo dept);

    /**
     * 构建前端所需要下拉树结构
     * 将部门列表转换为Hutool树形结构，用于下拉选择框
     *
     * @param depts 部门列表
     * @return 下拉树结构列表
     */
    List<Tree<Long>> buildDeptTreeSelect(List<SysDeptVo> depts);

    /**
     * 根据角色ID查询部门树信息
     * 查询指定角色已授权的部门ID列表
     *
     * @param roleId 角色ID
     * @return 选中部门ID列表
     */
    List<Long> selectDeptListByRoleId(Long roleId);

    /**
     * 根据部门ID查询信息
     * 根据主键ID查询部门详细信息
     *
     * @param deptId 部门ID
     * @return 部门信息
     */
    SysDeptVo selectDeptById(Long deptId);

    /**
     * 通过部门ID串查询部门
     * 根据部门ID列表查询多个部门信息
     *
     * @param deptIds 部门ID列表
     * @return 部门列表信息
     */
    List<SysDeptVo> selectDeptByIds(List<Long> deptIds);

    /**
     * 根据ID查询所有子部门数（正常状态）
     * 统计指定部门下所有状态为正常的子部门数量
     *
     * @param deptId 部门ID
     * @return 子部门数
     */
    long selectNormalChildrenDeptById(Long deptId);

    /**
     * 是否存在部门子节点
     * 检查指定部门下是否存在子部门
     *
     * @param deptId 部门ID
     * @return 结果 true-存在子部门，false-不存在子部门
     */
    boolean hasChildByDeptId(Long deptId);

    /**
     * 查询部门是否存在用户
     * 检查指定部门下是否存在已分配的用户
     *
     * @param deptId 部门ID
     * @return 结果 true-存在用户，false-不存在用户
     */
    boolean checkDeptExistUser(Long deptId);

    /**
     * 校验部门名称是否唯一
     * 检查部门名称在同级部门中是否已存在，用于新增和修改时的唯一性校验
     *
     * @param dept 部门信息（包含部门ID、父部门ID和部门名称）
     * @return true-唯一，false-不唯一
     */
    boolean checkDeptNameUnique(SysDeptBo dept);

    /**
     * 校验部门是否有数据权限
     * 检查当前登录用户是否有权限操作指定部门的数据（数据权限范围控制）
     *
     * @param deptId 部门id
     */
    void checkDeptDataScope(Long deptId);

    /**
     * 新增保存部门信息
     * 添加新部门到数据库，包含部门基本信息
     *
     * @param bo 部门信息
     * @return 影响的行数
     */
    int insertDept(SysDeptBo bo);

    /**
     * 修改保存部门信息
     * 更新部门基本信息，同时更新所有子部门的祖级列表
     *
     * @param bo 部门信息
     * @return 影响的行数
     */
    int updateDept(SysDeptBo bo);

    /**
     * 删除部门管理信息
     * 删除部门及其所有子部门，并清除部门与角色的关联关系
     *
     * @param deptId 部门ID
     * @return 影响的行数
     */
    int deleteDeptById(Long deptId);
}
