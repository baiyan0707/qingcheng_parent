package com.qingcheng.dao;

import com.qingcheng.pojo.goods.Brand;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

public interface BrandMapper extends Mapper<Brand> {

    @Select("SELECT ta.`name`,ta.`image` " +
            "FROM `tb_brand` ta,`tb_category` tc,`tb_category_brand` tcb " +
            "WHERE tc.`name` = #{name}  " +
            "AND tcb.`category_id` = tc.`id` " +
            "AND tcb.`brand_id` = ta.`id` " +
            "ORDER BY ta.`seq`")
    List<Map> findListByCategoryName(@Param("name") String categoryName);
}
