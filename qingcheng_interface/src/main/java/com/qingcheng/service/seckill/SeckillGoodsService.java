package com.qingcheng.service.seckill;

import com.qingcheng.pojo.seckill.SeckillGoods;

import java.util.List;

public interface SeckillGoodsService {

    /***
     * 获取指定时间对应的秒杀商品列表
     * @param time
     */
    List<SeckillGoods> list(String time);


    /****
     * 根据ID查询商品详情
     * @param time:时间区间
     * @param id:商品ID
     */
    SeckillGoods one(String time, Long id);

}
