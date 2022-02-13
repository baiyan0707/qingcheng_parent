package com.qingcheng.service.goods;
import com.qingcheng.entity.PageResult;
import com.qingcheng.pojo.goods.Category;

import java.util.*;

/**
 * category业务逻辑层
 */
public interface CategoryService {

     List<Category> findAll();

     PageResult<Category> findPage(int page, int size);

     List<Category> findList(Map<String,Object> searchMap);

     PageResult<Category> findPage(Map<String,Object> searchMap,int page, int size);

     Category findById(Integer id);

     void add(Category category);

     void update(Category category);

     void delete(Integer id);

     List<Map> findCategoryTree();

     void saveCategoryTreeToRedis();

     void findAllCategoryToRedis();
}
