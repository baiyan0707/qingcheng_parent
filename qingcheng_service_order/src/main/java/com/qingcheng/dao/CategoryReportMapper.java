package com.qingcheng.dao;

import com.qingcheng.pojo.order.CategoryReport;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface CategoryReportMapper extends Mapper<CategoryReport> {

    @Select("SELECT oi.`category_id1` category_id1, oi.`category_id2` category_id2,oi.`category_id3` category_id3, DATE_FORMAT(o.`pay_time`, '%Y-%m-%d') dateFormat, SUM(oi.num) num,SUM(oi.`money`) money  " +
            "  FROM tb_order o, tb_order_item oi  " +
            "  WHERE oi.`order_id` = o.`id`  " +
            "  AND o.`pay_status` = '1' AND DATE_FORMAT(o.`pay_time`, '%Y-%m-%d') = #{data} " +
            "  GROUP BY category_id1,category_id2, category_id3, DATE_FORMAT(o.`pay_time`, '%Y-%m-%d')")
    List<CategoryReport> categoryReport(@Param("data") LocalDate data);


    @Select("SELECT  " +
            "  cr.`category_id1` categoryId1, " +
            "  v.name, " +
            "  SUM(cr.`num`) num, " +
            "  SUM(cr.`money`) money  " +
            "FROM " +
            "  `tb_category_report` cr, " +
            "  v_category1 v " +
            "WHERE cr.`count_date` >= #{data1}  " +
            "  AND cr.`count_date` <= #{data2}  " +
            "GROUP BY cr.`category_id1`, " +
            "  v.name")
    List<Map> category1Count(@Param("data1") String data1, @Param("data2") String data2);

}
