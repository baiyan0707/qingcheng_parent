package com.qingcheng.controller.order;


import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.pojo.order.CategoryReport;
import com.qingcheng.pojo.order.Statistics;
import com.qingcheng.service.order.CategoryReportService;
import com.qingcheng.service.order.StatisticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/categoryReport")
public class CategoryReportController {

    @Reference
    CategoryReportService categoryReportService;

    @Reference
    StatisticsService statisticsService;


    @GetMapping("/yesterday")
    public List<CategoryReport> yesterday(){
        LocalDate data = LocalDate.now().minusDays(1); //得到昨天的日期
        return categoryReportService.yesterday(data);
    }

    @GetMapping("/category1Count")
    public List<Map> category1Count(String data1,String data2){
        return categoryReportService.category1Count(data1,data2);
    }

    @GetMapping("/dayMeasurement")
    public List<Statistics> dayMeasurement(){
        LocalDate date = LocalDate.now().minusDays(1);
        return statisticsService.dayMeasurement(date);
    }
}
