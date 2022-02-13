package com.qingcheng.service.order;

import com.qingcheng.pojo.order.CategoryReport;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface CategoryReportService {

    List<CategoryReport> yesterday(LocalDate data);

    void createData();

    List<Map> category1Count(String data1,String data2);

}
