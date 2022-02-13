package com.qingcheng.dao;

import com.qingcheng.pojo.system.Menu;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface MenuMapper extends Mapper<Menu> {

    @Select("SELECT tm3.`id`,tm3.`name`,tm3.`icon`,tm3.`url`,tm2.`id`,tm2.`name`,tm2.`icon`,tm2.`url`,tm.`id`,tm.`name`,tm.`icon`,tm.`url` " +
            "FROM `tb_admin` ta  " +
            "JOIN `tb_admin_role` tar ON tar.`admin_id` = ta.`id`  " +
            "JOIN `tb_role_resource` trr ON trr.`role_id` = tar.`role_id`  " +
            "JOIN `tb_resource_menu` trm ON trm.`resource_id` = trr.`resource_id`  " +
            "JOIN `tb_menu` tm ON tm.`id` = trm.`menu_id`  " +
            "JOIN `tb_menu` tm2 ON tm2.`id` = tm.`parent_id`  " +
            "JOIN `tb_menu` tm3 ON tm3.`id` = tm2.`parent_id`  " +
            "WHERE ta.`login_name` = #{loginName}")
    List<Menu> queryPermissions(@Param("loginName") String loginName);
}
