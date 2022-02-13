package com.qingcheng.dao;

import com.qingcheng.pojo.system.AdminRole;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface AdminRoleMapper extends Mapper<AdminRole> {

    @Select("SELECT role_id FROM `tb_admin_role` tar WHERE #{id} = tar.`admin_id`")
    List<Integer> findByRoleIds(@Param("id") Integer id);

    @Select(" DELETE FROM `tb_admin_role` WHERE admin_id = #{id} ")
    void deleteByAdminId(@Param("id")Integer id);
}
