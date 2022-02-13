package com.qingcheng.dao;

import com.qingcheng.pojo.system.Resource;
import com.qingcheng.pojo.system.Role;
import com.qingcheng.pojo.system.RoleResource;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;


public interface RoleMapper extends Mapper<Role> {
    @Select("SELECT trr.`resource_id` " +
            "FROM`tb_admin_role` tar,`tb_role_resource` trr,`tb_resource` tr   " +
            "WHERE  #{id} = tar.`role_id` " +
            "AND trr.`role_id` = tar.`role_id` AND tr.`id` = trr.`resource_id`")
    List<Integer> findResourceIdsByRoleId(@Param("id") Integer id);

    Resource changeResource(Integer id);
}
