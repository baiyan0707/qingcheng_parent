package com.qingcheng.service.business;

import com.qingcheng.entity.PageResult;
import com.qingcheng.pojo.business.Ad;

import java.util.*;

/**
 * ad业务逻辑层
 */
public interface AdService {

    List<Ad> findAll();

    PageResult<Ad> findPage(int page, int size);

    List<Ad> findList(Map<String, Object> searchMap);

    PageResult<Ad> findPage(Map<String, Object> searchMap, int page, int size);

    Ad findById(Integer id);

    void add(Ad ad);

    void update(Ad ad);

    void delete(Integer id);

    List<Ad> findByPosition(String position);

    void saveAdToRedisByPosition(String position);

    void saveAllAdToRedis();
}
