package com.qingcheng.dao;

import com.qingcheng.pojo.order.OrderItem;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

public interface OrderItemMapper extends Mapper<OrderItem> {

    @Select("SELECT t.`receiver_address` 'receiverAddress',t.`id` 'orderId',t.`create_time` 'createTime',t.`pay_time` 'payTime',t.`pay_type` 'payType',t.`consign_time` 'consignTime', " +
            "toi.`id` 'orderItemId',toi.`image`,toi.`name`,toi.`money`,toi.`num`,t.`pre_money` 'preMoney',t.`order_status` 'orderStatus',toi.`pay_money` 'payMoney',toi.`post_fee` 'postFee', " +
            "t.`consign_status` 'consignStatus',t.`pay_status` 'payStatus' " +
            "FROM  `tb_order` t,`tb_order_item` toi " +
            "WHERE  t.`username` = #{username} " +
            "AND toi.`order_id` = t.`id`; ")
    List<Map<String,Object>> findByOrderId(@Param("username") String username);

    @Select("SELECT t.`receiver_address` 'receiverAddress',t.`id` 'orderId',t.`create_time` 'createTime',t.`pay_time` 'payTime',t.`pay_type` 'payType',t.`consign_time` 'consignTime', " +
            "toi.`id` 'orderItemId',toi.`image`,toi.`name`,toi.`money`,toi.`num`,t.`pre_money` 'preMoney',t.`order_status` 'orderStatus',toi.`pay_money` 'payMoney',toi.`post_fee` 'postFee', " +
            "t.`consign_status` 'consignStatus',t.`end_time` 'endTime',t.`close_time` 'closeTime',t.`pay_status` 'payStatus' " +
            "FROM `tb_order` t,`tb_order_item` toi " +
            "WHERE t.`id` = #{orderId} " +
            "AND toi.`order_id` = t.`id`;")
    List<Map<String, Object>> findOrderAndItem(@Param("orderId") String oderId);
}
