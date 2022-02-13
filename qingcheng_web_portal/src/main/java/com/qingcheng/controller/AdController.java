package com.qingcheng.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.pojo.business.Ad;
import com.qingcheng.service.business.AdService;
import com.qingcheng.service.goods.CategoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Controller
public class AdController {

    @Reference
    AdService adService;

    @Reference
    CategoryService categoryService;

    @GetMapping("/index")
    public String index(Model model){
        //查询首页轮播图
        List<Ad> index_lb = adService.findByPosition("index_lb");
        model.addAttribute("lbt",index_lb);
        //查询商品分类
        List<Map> categoryList = categoryService.findCategoryTree();
        model.addAttribute("categoryList",categoryList);

        return "index";
    }
}
