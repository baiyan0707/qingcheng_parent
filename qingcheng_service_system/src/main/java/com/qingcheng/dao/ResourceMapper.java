package com.qingcheng.dao;

import com.qingcheng.pojo.system.Resource;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface ResourceMapper extends Mapper<Resource> {

    @Select("SELECT tr.`res_key`  " +
            "FROM`tb_admin` ta,`tb_admin_role` tar,`tb_role_resource` trr,`tb_resource` tr  " +
            "WHERE ta.`login_name` = #{loginName}  " +
            "AND tar.`admin_id` = ta.`id` AND trr.`role_id` = tar.`role_id` AND tr.`id` = trr.`resource_id` ")
    List<String> addPermission(@Param("loginName") String loginName);
}
