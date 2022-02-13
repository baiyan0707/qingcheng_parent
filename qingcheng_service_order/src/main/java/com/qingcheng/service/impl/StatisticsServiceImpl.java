package com.qingcheng.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.qingcheng.dao.StatisticsMapper;
import com.qingcheng.pojo.order.Statistics;
import com.qingcheng.service.order.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    StatisticsMapper statisticsMapper;

    @Override
    public List<Statistics> dayMeasurement(LocalDate date) {
        return statisticsMapper.dayMeasurement(date);
    }

    @Override
    public void insertInformation() {
        LocalDate date = LocalDate.now().minusDays(1);
        List<Statistics> statistics = statisticsMapper.dayMeasurement(date);
        for (Statistics statistic : statistics) {
            statisticsMapper.insert(statistic);
        }
    }
}
