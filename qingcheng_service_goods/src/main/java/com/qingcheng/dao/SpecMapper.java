package com.qingcheng.dao;

import com.qingcheng.pojo.goods.Spec;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

public interface SpecMapper extends Mapper<Spec> {

    @Select("SELECT name,options FROM `tb_spec` WHERE `template_id` IN ( SELECT `template_id` FROM `tb_category` WHERE NAME=#{categoryName} ) ORDER BY seq")
    List<Map> findListByCategoryName(@Param("categoryName") String categoryName);

    @Select("SELECT tc.`name`,ts.options FROM `tb_category` tc,`tb_spec` ts " +
            "WHERE tc.`template_id` = ts.template_id")
    List<Map> findAllSpecToRedis();
}
