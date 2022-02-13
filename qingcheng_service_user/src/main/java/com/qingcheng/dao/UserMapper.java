package com.qingcheng.dao;

import com.qingcheng.pojo.user.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface UserMapper extends Mapper<User> {

    @Select("SELECT tu.`username`,tu.`password`,tu.`phone`,tu.`email`,tu.`created`,tu.`updated`,tu.`source_type` 'sourceType',tu.`nick_name` 'nickName',tu.`name`,tu.`status`,tu.`head_pic` 'headPic', " +
            "tu.`qq`,tu.`is_email_check` 'isEmailCheck',tu.`is_mobile_check` 'isMobileCheck',tu.`sex`,tu.`user_level` 'userLevel',tu.`points`,tu.`experience_value` 'experienceValue',tu.`birthday`, " +
            "tu.`last_login_time` 'lastLoginTime',tp.`province`,tc.`city`,ta.`area`,tad.`address`,tad.`contact`,tad.`is_default` 'isDefault', tad.`alias` " +
            "FROM `tb_user` tu,`tb_provinces` tp,`tb_cities` tc,`tb_areas` ta,`tb_address` tad " +
            "WHERE tu.`username`=#{username} AND tu.`username`=tad.`username` AND tad.`provinceid`=tp.`provinceid` AND tad.`cityid`=tc.`cityid` AND tad.`areaid`=ta.`areaid`")
    List<Map<String,String>> getUser(@Param("username") String username);

    @Select("SELECT tp.`provinceid`,tc.`cityid`,ta.`areaid` FROM `tb_provinces` tp,`tb_cities` tc,`tb_areas` ta, `tb_address` tad  " +
            "WHERE tp.`province`=#{province} AND tc.`city`=#{city} AND ta.`area`=#{area} AND tad.`provinceid`=tp.`provinceid` " +
            "AND tad.`cityid`=tc.`cityid` AND ta.`areaid`=tad.`areaid`")
    List<Map<String,String>> findByProvincial(@Param("province")String province,@Param("city") String city,@Param("area") String area);

    @Update("UPDATE `tb_user` tu JOIN `tb_address` tad ON tu.`username` = tad.`username` JOIN `tb_provinces` tp ON tad.`provinceid` = tp.`provinceid` " +
            "JOIN `tb_cities` tc ON tad.`cityid` = tc.`cityid` JOIN `tb_areas` ta ON tad.`areaid` = ta.`areaid` SET tu.`nick_name` = #{nickName},tu.`birthday` = #{birthday},tp.`provinceid` = #{provinceId}, " +
            "tc.`cityid` = #{cityId},ta.`areaid` = #{areaId},tad.`phone` = #{phone},tu.`email` = #{email},tu.`updated` = #{time}  " +
            "WHERE tu.`username` = #{username} ")
    void update(@Param("username")String username, @Param("nickName")String nickName,
                @Param("birthday")String birthday, @Param("provinceId")String provinceId, @Param("cityId")String cityId,
                @Param("areaId")String areaId, @Param("phone")String phone, @Param("email")String email,@Param("time")Date time);
}
