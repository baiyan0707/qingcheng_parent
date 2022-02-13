package com.qingcheng.dao;

import com.qingcheng.pojo.order.Order;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface OrderMapper extends Mapper<Order> {

   /* @Select("SELECT t.`receiver_address` 'receiverAddress',t.`id`,t.`create_time` 'createTime',t.`pay_time` 'payTime',t.`pay_type` 'payType',t.`consign_time` 'consignTime', " +
            "toi.`id`,toi.`image`,toi.`name`,toi.`money`,toi.`num`,t.`pre_money` 'preMoney',t.`order_status` 'orderStatus',toi.`pay_money` 'payMoney',toi.`post_fee` 'postFee' " +
            "FROM  `tb_order` t,`tb_order_item` toi " +
            "WHERE  t.`username` = #{username} " +
            "AND toi.`order_id` = t.`id`; ")
    List<Order> findByName(@Param("username") String username);*/
}
