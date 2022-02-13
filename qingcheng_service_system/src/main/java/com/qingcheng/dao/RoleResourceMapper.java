package com.qingcheng.dao;

import com.qingcheng.pojo.system.RoleResource;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;


public interface RoleResourceMapper extends Mapper<RoleResource> {

    @Delete("DELETE FROM `tb_role_resource` WHERE role_id = #{roleId}")
    void deleteByRoleId(Integer roleId);
}
