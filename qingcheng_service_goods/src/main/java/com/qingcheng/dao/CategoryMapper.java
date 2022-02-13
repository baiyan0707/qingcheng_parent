package com.qingcheng.dao;

import com.qingcheng.pojo.goods.Category;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

public interface CategoryMapper extends Mapper<Category> {

    @Select("SELECT tc.`name` categoryName,tb.`name` brandName FROM `tb_category` tc,`tb_category_brand` tcb,`tb_brand` tb  " +
            "WHERE tc.`id` = tcb.`category_id`  " +
            "AND tcb.`brand_id` = tb.`id` ")
    List<Map> findAllCategoryToRedis();
}
