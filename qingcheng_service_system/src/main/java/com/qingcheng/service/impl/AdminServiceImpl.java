package com.qingcheng.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.qingcheng.dao.AdminMapper;
import com.qingcheng.dao.AdminRoleMapper;
import com.qingcheng.entity.PageResult;
import com.qingcheng.pojo.system.Admin;
import com.qingcheng.pojo.system.AdminRole;
import com.qingcheng.pojo.system.Admins;
import com.qingcheng.service.system.AdminService;
import com.qingcheng.util.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.List;
import java.util.Map;

@Service(interfaceClass = AdminService.class)
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    AdminRoleMapper adminRoleMapper;

    /**
     * 新增
     * @param admins
     */
    @Transactional
    public void add(Admins admins) {
        Admin admin = admins.getAdmin();
        adminMapper.insert(admin);
        List<Integer> roleIds = admins.getRoleIds();
        AdminRole admin_role = new AdminRole();
        for (Integer roleId : roleIds) {
            admin_role.setRoleId(roleId);
            admin_role.setAdminId(admin.getId());
        }
        //对密码进行加密
        String gensalt = BCrypt.gensalt();
        String new_password = BCrypt.hashpw(admin.getPassword(), gensalt);
        admin.setPassword(new_password);
        adminMapper.updateByPrimaryKeySelective(admin);
        adminRoleMapper.insert(admin_role);
    }

    /**
     * 修改密码
     * @param editMap 前端传递的用户信息
     */
    public void editPassword(Map<String,String> editMap){
        String old_Password = editMap.get("oldPassword"); //原密码
        String loginName = editMap.get("loginName"); //用户名称
        String new_password = editMap.get("newPassword"); //新密码
        //根据用户名查询数据库得到用户全部信息
        Example example = new Example(Admin.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo(loginName);
        List<Admin> admins = adminMapper.selectByExample(example);
        for (Admin admin : admins) {
            //判断原密码和用户输入的密码是否相似
            if(!BCrypt.checkpw(old_Password,admin.getPassword())){
                throw new RuntimeException("抱歉,您输入的原密码不正确");
            }
            //获取到盐
            String gensalt = BCrypt.gensalt();
            //根据盐对密码进行加密
            String grnsalt_password = BCrypt.hashpw(new_password, gensalt);
            //保存新密码
            admin.setPassword(grnsalt_password);
            adminMapper.updateByPrimaryKeySelective(admin);
        }
    }



    /**
     * 返回全部记录
     * @return 返回全部数据
     */
    public List<Admin> findAll() {
        return adminMapper.selectAll();
    }

    /**
     * 分页查询
     * @param page 页码
     * @param size 每页记录数
     * @return 分页结果
     */
    public PageResult<Admin> findPage(int page, int size) {
        PageHelper.startPage(page,size);
        Page<Admin> admins = (Page<Admin>) adminMapper.selectAll();
        return new PageResult<Admin>(admins.getTotal(),admins.getResult());
    }

    /**
     * 条件查询
     * @param searchMap 查询条件
     * @return
     */
    public List<Admin> findList(Map<String, Object> searchMap) {
        Example example = createExample(searchMap);
        return adminMapper.selectByExample(example);
    }

    /**
     * 分页+条件查询
     * @param searchMap
     * @param page
     * @param size
     * @return
     */
    public PageResult<Admin> findPage(Map<String, Object> searchMap, int page, int size) {
        PageHelper.startPage(page,size);
        Example example = createExample(searchMap);
        Page<Admin> admins = (Page<Admin>) adminMapper.selectByExample(example);
        return new PageResult<Admin>(admins.getTotal(),admins.getResult());
    }

    /**
     * 根据Id查询
     * @param id
     * @return
     */
    public Admins findById(Integer id) {
        Admin admin = adminMapper.selectByPrimaryKey(id);
        List<Integer> roleIds = adminRoleMapper.findByRoleIds(id);
        return new Admins(admin,roleIds);
    }

    /**
     * 新增用户
     * @param admins
     */
    @Transactional
    public void update(Admins admins) {
        Admin admin = admins.getAdmin();
        adminRoleMapper.deleteByAdminId(admin.getId());
        List<Integer> roleIds = admins.getRoleIds();
        AdminRole adminRole = new AdminRole();
        for (Integer roleId : roleIds) {
            adminRole.setAdminId(admin.getId());
            adminRole.setRoleId(roleId);
            adminRoleMapper.insert(adminRole);
        }
    }

    /**
     *  删除
     * @param id
     */
    public void delete(Integer id) {
        adminRoleMapper.deleteByAdminId(id);
        adminMapper.deleteByPrimaryKey(id);
    }

    /**
     * 构建查询条件
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap){
        Example example=new Example(Admin.class);
        Example.Criteria criteria = example.createCriteria();
        if(searchMap!=null){
            // 用户名
            if(searchMap.get("loginName")!=null && !"".equals(searchMap.get("loginName"))){
//                criteria.andLike("loginName","%"+searchMap.get("loginName")+"%");
                criteria.andEqualTo("loginName",searchMap.get("loginName"));
            }
            // 密码
            if(searchMap.get("password")!=null && !"".equals(searchMap.get("password"))){
                criteria.andLike("password","%"+searchMap.get("password")+"%");
            }
            // 状态
            if(searchMap.get("status")!=null && !"".equals(searchMap.get("status"))){
                criteria.andEqualTo("status",searchMap.get("status"));
            }
            // id
            if(searchMap.get("id")!=null ){
                criteria.andEqualTo("id",searchMap.get("id"));
            }
        }
        return example;
    }
}
