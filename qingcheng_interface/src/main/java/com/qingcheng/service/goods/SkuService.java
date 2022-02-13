package com.qingcheng.service.goods;

import com.qingcheng.entity.PageResult;
import com.qingcheng.pojo.goods.Sku;
import com.qingcheng.pojo.order.OrderItem;

import java.util.*;

/**
 * sku业务逻辑层
 */
public interface SkuService {

    List<Sku> findAll();

    PageResult<Sku> findPage(int page, int size);

    List<Sku> findList(Map<String, Object> searchMap);

    PageResult<Sku> findPage(Map<String, Object> searchMap, int page, int size);

    Sku findById(String id);

    void add(Sku sku);

    void update(Sku sku);

    void delete(String id);

    void saveAllPriceToRedis();

    Integer findPrice(String id);

    void savePriceToRedisById(String id, Integer price);

    void deletePriceFromRedis(String id);

    void importAllSkuToES() throws Exception;

    void CommodityPutaway(String id);

    void CommodityOut(String id);

    boolean deductionStock(List<OrderItem> orderItemList);
}
