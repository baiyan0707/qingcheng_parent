package com.qingcheng.service.order;

import com.qingcheng.pojo.order.Statistics;

import java.time.LocalDate;
import java.util.List;

public interface StatisticsService {

    List<Statistics> dayMeasurement(LocalDate date);

    void insertInformation();

}
