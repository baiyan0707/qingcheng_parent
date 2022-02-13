package com.qingcheng.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.pojo.system.Admin;
import com.qingcheng.service.system.AdminService;
import com.qingcheng.service.system.ResourceService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserDetailsServiceImpl implements UserDetailsService {

    @Reference
    private AdminService adminService;

    @Reference
    ResourceService resourceService;

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        //查询管理员
        Map map = new HashMap<>();
        map.put("loginName",userName);
        map.put("status","1");

        List<Admin> list = adminService.findList(map);
        if(list == null || list.size() == 0){
            throw new RuntimeException("数据为空");
        }
        //构建角色集合 ，项目中此处应该是根据用户名查询用户的角色列表
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        List<String> permissionList = resourceService.addPermission(userName);
        for (String permission : permissionList) {
            grantedAuthorities.add(new SimpleGrantedAuthority(permission));
        }
        grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        grantedAuthorities.add(new SimpleGrantedAuthority("find"));
        return new User(userName,list.get(0).getPassword(),grantedAuthorities);
    }
}
