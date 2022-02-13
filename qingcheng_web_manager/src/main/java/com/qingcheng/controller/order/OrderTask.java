package com.qingcheng.controller.order;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.service.order.CategoryReportService;
import com.qingcheng.service.order.ReturnOrderService;
import com.qingcheng.service.order.StatisticsService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OrderTask {

    @Reference
    ReturnOrderService returnorderService;

    @Reference
    CategoryReportService categoryReportService;

    @Reference
    StatisticsService statisticsService;

    @Scheduled(cron = "* * * 1 * *")
    public void orderTimeOutLogic(){
        System.out.println("查询订单是否过期");
        returnorderService.orderTimeOutLogic();
    }

    @Scheduled(cron = "* * * 1 * *")
    public void createData(){
        System.out.println("定时添加昨天的订单信息");
        categoryReportService.createData();
    }

    @Scheduled(cron = "* * * 1 * *")
    public void dayMeasurement(){
        System.out.println("查询当天用户量");
        statisticsService.insertInformation();
    }
}
