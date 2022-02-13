package com.qingcheng.service.order;

import com.qingcheng.entity.PageResult;
import com.qingcheng.pojo.order.Order;
import com.qingcheng.pojo.order.Orders;

import java.util.*;

/**
 * order业务逻辑层
 */
public interface OrderService {

    List<Order> findAll();

    PageResult<Order> findPage(int page, int size);

    List<Order> findList(Map<String, Object> searchMap);

    PageResult<Order> findPage(Map<String, Object> searchMap, int page, int size);

    Order findByIds(String[] ids);

    Order findById(String id);

    Map<String,Object> add(Order order);

    void update(Order order);

    void delete(String id);

    void updateOrder(Map<String, String> mergeMap);

    Orders findByOrderId(String id);

    Order merge(Map<String,String> map);

    void split(List<HashMap<String,Integer>> splitList);

    void  updatePayStatus(String orderId,String transactionId);

    void closeOrder(String id);

}
