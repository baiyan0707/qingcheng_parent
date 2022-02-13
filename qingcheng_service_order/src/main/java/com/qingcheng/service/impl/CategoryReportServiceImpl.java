package com.qingcheng.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.qingcheng.dao.CategoryReportMapper;
import com.qingcheng.pojo.order.CategoryReport;
import com.qingcheng.service.order.CategoryReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;


@Service(interfaceClass = CategoryReportService.class)
public class CategoryReportServiceImpl implements CategoryReportService {

    @Autowired
    CategoryReportMapper categoryReportMapper;

    /**
     * 获取到昨天的数据
     * @param data
     * @return
     */
    @Override
    public List<CategoryReport> yesterday(LocalDate data) {
        return categoryReportMapper.categoryReport(data);
    }

    /**
     * 定时，没听凌晨1点统计类目信息，保存到新表中
     */
    @Override
    @Transactional
    public void createData() {
        LocalDate localDate = LocalDate.now().minusDays(1);
        List<CategoryReport> categoryReports = categoryReportMapper.categoryReport(localDate);
        for (CategoryReport categoryReport : categoryReports) {
            categoryReportMapper.insert(categoryReport);
        }
    }

    /**
     * 按日期统计一级分类数据
     * @param data1
     * @param data2
     * @return
     */
    @Override
    public List<Map> category1Count(String data1, String data2) {
        return categoryReportMapper.category1Count(data1,data2);
    }

}
