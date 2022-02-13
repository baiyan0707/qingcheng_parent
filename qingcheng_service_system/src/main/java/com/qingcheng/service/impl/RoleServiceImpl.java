package com.qingcheng.service.impl;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.qingcheng.dao.AdminMapper;
import com.qingcheng.dao.AdminRoleMapper;
import com.qingcheng.dao.RoleMapper;
import com.qingcheng.dao.RoleResourceMapper;
import com.qingcheng.entity.PageResult;
import com.qingcheng.pojo.system.*;
import com.qingcheng.service.system.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.List;
import java.util.Map;

@Service(interfaceClass = RoleService.class)
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    RoleResourceMapper roleResourceMapper;

    @Autowired
    AdminMapper adminMapper;

    @Autowired
    AdminRoleMapper adminRoleMapper;

    /**
     * 返回全部记录
     * @return
     */
    public List<Role> findAll() {
        return roleMapper.selectAll();
    }

    /**
     * 获取到中间表信息
     * @param id 前端传递的用户id
     * @return
     */
    public List<Integer> findResourceIdsByRoleId(Integer id) {
        return roleMapper.findResourceIdsByRoleId(id);
    }

    /**
     * 修改权限
     * @param resources
     * @return
     */
    @Override
    public void changeResource(Resources resources) {
        Integer[] resourceIds = resources.getResourceIds(); //权限id数组
        Integer roleId = resources.getRoleId(); //角色id
        roleResourceMapper.deleteByRoleId(roleId);
        RoleResource resource = new RoleResource();
        for (Integer resourceId : resourceIds) {
            resource.setResource_id(resourceId);
            resource.setRole_id(roleId);
            roleResourceMapper.insert(resource);
        }
    }

    /**
     * 分页查询
     * @param page 页码
     * @param size 每页记录数
     * @return 分页结果
     */
    public PageResult<Role> findPage(int page, int size) {
        PageHelper.startPage(page,size);
        Page<Role> roles = (Page<Role>) roleMapper.selectAll();
        return new PageResult<Role>(roles.getTotal(),roles.getResult());
    }

    /**
     * 条件查询
     * @param searchMap 查询条件
     * @return
     */
    public List<Role> findList(Map<String, Object> searchMap) {
        Example example = createExample(searchMap);
        return roleMapper.selectByExample(example);
    }

    /**
     * 分页+条件查询
     * @param searchMap
     * @param page
     * @param size
     * @return
     */
    public PageResult<Role> findPage(Map<String, Object> searchMap, int page, int size) {
        PageHelper.startPage(page,size);
        Example example = createExample(searchMap);
        Page<Role> roles = (Page<Role>) roleMapper.selectByExample(example);
        return new PageResult<Role>(roles.getTotal(),roles.getResult());
    }

    /**
     * 根据Id查询
     * @param id
     * @return
     */
    public Role findById(Integer id) {
        return roleMapper.selectByPrimaryKey(id);
    }

    /**
     * 新增
     * @param role
     */
    public void add(Role role) {
        roleMapper.insert(role);
    }

    /**
     * 修改
     * @param role
     */
    public void update(Role role) {
        roleMapper.updateByPrimaryKeySelective(role);
    }

    /**
     *  删除
     * @param id
     */
    public void delete(Integer id) {
        roleMapper.deleteByPrimaryKey(id);
    }

    /**
     * 获取到前端传递的角色id和权限id列表查询数据库返回给前端
     * @param roles 前台传递的集合数据
     * @return 从数据库查出的数据
     */
    @Override
    @Transactional
    public void getRolesAndResourceList(Roles roles) {
        Role role = roles.getRole();
        List<Integer> resourceIds = roles.getResourceList();
        RoleResource roleResource = new RoleResource();
        for (Integer resourceId : resourceIds) {
            roleResource.setRole_id(role.getId());
            roleResource.setResource_id(resourceId);
            roleResourceMapper.insert(roleResource);
        }
    }

    /**
     * 构建查询条件
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap){
        Example example=new Example(Role.class);
        Example.Criteria criteria = example.createCriteria();
        if(searchMap!=null){
            // 角色名称
            if(searchMap.get("name")!=null && !"".equals(searchMap.get("name"))){
                criteria.andLike("name","%"+searchMap.get("name")+"%");
            }

            // ID
            if(searchMap.get("id")!=null ){
                criteria.andEqualTo("id",searchMap.get("id"));
            }

        }
        return example;
    }

}
