package com.qingcheng.service.impl;

import com.qingcheng.service.business.AdService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Init implements InitializingBean {

    @Autowired
    AdService adService;

    @Override
    public void afterPropertiesSet() throws Exception {
        adService.saveAllAdToRedis(); //将所有广告加入到缓存中
    }
}
