package com.qingcheng.service.impl;

import com.qingcheng.service.goods.CategoryService;
import com.qingcheng.service.goods.SkuService;
import com.qingcheng.service.goods.SpecService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Init implements InitializingBean {

    @Autowired
    CategoryService categoryService;

    @Autowired
    SkuService skuService;

    @Autowired
    SpecService specService;

    @Override
    public void afterPropertiesSet() throws Exception {
        categoryService.saveCategoryTreeToRedis(); //将所有商品过滤加入到缓存中
        skuService.saveAllPriceToRedis(); //将所有价格加入到缓存中
        categoryService.findAllCategoryToRedis(); //将品牌列表将入到缓存中
        specService.findAllSpecToRedis(); //将所有规格加入到缓存中
    }
}
