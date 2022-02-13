package com.qingcheng.service.seckill;

import com.qingcheng.pojo.seckill.SeckillOrder;
import com.qingcheng.util.SeckillStatus;


public interface SeckillOrderService {


    /***
     * 根据用户名查询用户的未支付秒杀订单
     * @param username
     */
    SeckillOrder findById(String username);


    /****
     * 修改订单状态
     * @param username
     * @param transaction_id
     * @param out_trade_no
     */
    void updateStatus(String out_trade_no, String transaction_id, String username);


    /***
     * 添加秒杀订单
     * @param id:商品ID
     * @param time:商品秒杀开始时间
     * @param username:用户登录名
     * @return
     */
    Boolean add(Long id, String time, String username);

    SeckillStatus queryStatus(String username);
}
