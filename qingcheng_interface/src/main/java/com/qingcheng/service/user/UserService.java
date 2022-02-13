package com.qingcheng.service.user;

import com.qingcheng.entity.PageResult;
import com.qingcheng.pojo.order.Order;
import com.qingcheng.pojo.order.OrderItem;
import com.qingcheng.pojo.order.Orders;
import com.qingcheng.pojo.user.User;

import java.util.*;

/**
 * user业务逻辑层
 *
 * @blame Android Team
 */
public interface UserService {
    List<User> findAll();

    PageResult<User> findPage(int page, int size);

    List<User> findList(Map<String, Object> searchMap);

    PageResult<User> findPage(Map<String, Object> searchMap, int page, int size);

    void add(User user);

    void update(Map<String,String> userMap);

    void delete(String username);

    void sendSms(String phone);

    void add(User user,String smsCode);

    Map<String,String> getUser(String username);

    void edit(Map<String,String> passwordMap);
}
