package com.qingcheng.dao;

import com.qingcheng.pojo.order.Statistics;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.time.LocalDate;
import java.util.List;

public interface StatisticsMapper extends Mapper<Statistics> {

    @Select("SELECT " +
            "            SUM(DISTINCT oi.`num`) total_number,COUNT(DISTINCT o.`id`) order_number,COUNT(DISTINCT o.`username`) order_people,SUM(oi.`pay_money`) pay_money,t1.valid_order_number, t2.pay_total_number,t2.pay_order,t2.pay_people,t3.refund_money " +
            "            FROM `tb_order` o,`tb_order_item` oi,(SELECT COUNT(DISTINCT o.`id`) valid_order_number FROM `tb_order` o WHERE DATE_FORMAT(o.`create_time`, '%Y-%m-%d') = #{date} AND o.`order_status` <> '4' AND o.`is_delete` = '0') AS t1, " +
            "            (SELECT SUM(DISTINCT oi.`num`) pay_total_number,COUNT(DISTINCT o.`id`) pay_order,COUNT(DISTINCT o.`username`) pay_people FROM `tb_order` o,`tb_order_item` oi WHERE o.`id` = oi.`order_id` AND DATE_FORMAT(o.`create_time`, '%Y-%m-%d') = #{date} AND `pay_status` = '1') AS t2, " +
            "            (SELECT SUM(ro.`return_money`) refund_money FROM `tb_order` o,`tb_return_order` ro WHERE o.`id` = ro.`order_id` AND DATE_FORMAT(o.`create_time`, '%Y-%m-%d') = #{date}) AS t3 WHERE o.`id` = oi.`order_id` AND DATE_FORMAT(o.`create_time`, '%Y-%m-%d') = #{date} " +
            "            GROUP BY t1.valid_order_number,t2.pay_total_number,t2.pay_order,t2.pay_people,t3.refund_money")
    List<Statistics> dayMeasurement(@Param("date") LocalDate date);
}
