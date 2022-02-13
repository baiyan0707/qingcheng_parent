package com.qingcheng.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.qingcheng.pojo.goods.Goods;
import com.qingcheng.pojo.goods.Sku;
import com.qingcheng.pojo.goods.Spu;
import com.qingcheng.service.goods.CategoryService;
import com.qingcheng.service.goods.SpuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/item")
public class ItemController {

    @Reference
    SpuService spuService;

    @Reference
    CategoryService categoryService;

    @Value("${pagePath}")
    String pagePath;

    @Autowired
    TemplateEngine templateEngine;

    /**
     * 生成商品详情页，静态化后返回前端
     */
    @GetMapping("/createPage")
    public void createPage(String id) {
        //获取到模型和数据
        Goods goods = spuService.findGoodsById(id);
        Spu spu = goods.getSpu();
        List<Sku> skuList = goods.getSkuList();
        //获取到分类信息
        List<String> categoryList = new ArrayList<>();
        categoryList.add(categoryService.findById(spu.getCategory1Id()).getName()); //一级分类
        categoryList.add(categoryService.findById(spu.getCategory2Id()).getName()); //二级分类
        categoryList.add(categoryService.findById(spu.getCategory3Id()).getName()); //三级分类

        Map<String,String> urlMap = new HashMap<>();
        for (Sku sku : skuList) {
            if("1".equals(sku.getStatus())){
                String specJson = JSON.toJSONString(JSON.parseObject(sku.getSpec()), SerializerFeature.MapSortField);
                urlMap.put(specJson,sku.getId() + ".html");
            }
        }

        //批量生成sku页面，以map格式保存
        for (Sku sku : skuList) {
            Context context = new Context();
            Map<String, Object> map = new HashMap<>();
            map.put("spu", spu);
            map.put("sku", sku);
            map.put("categoryList",categoryList);
            map.put("skuImages",sku.getImages().split(","));
            map.put("spuImages",spu.getImages().split(","));

            Map paraItems = JSON.parseObject(spu.getParaItems());
            map.put("paraItems",paraItems);
            Map<String,String> specItems = (Map) JSON.parseObject(sku.getSpec());
            map.put("specItems",specItems);

            //获取到规格列表
            //{"颜色":["天空之境","珠光贝母"],"内存":["8GB+64GB","8GB+128GB","8GB+256GB"]} 原本格式
            //{"颜色":[{ 'option':'天空之境',checked:true },{ 'option':'珠光贝母',checked:false }],.....} 需要转换的格式类型
            Map<String,List> specMap = (Map) JSON.parseObject(spu.getSpecItems());
            for (String key : specMap.keySet()) {
                List<String> list = specMap.get(key);
                List<Map> mapList = new ArrayList<>();
                for (String value : list) {
                    Map hashMap = new HashMap<>();
                    hashMap.put("option",value);
                    //如果和当前的sku规格相同，则表示选中
                    if(specItems.get(key).equals(value)){
                        hashMap.put("checked",true);
                    }else {
                        hashMap.put("checked",false);
                    }
                    //获取到当前的sku
                    Map<String,String> stringMap = (Map)JSON.parseObject(sku.getSpec());
                    stringMap.put(key,value);
                    String jsonString = JSON.toJSONString(stringMap, SerializerFeature.MapSortField);
                    hashMap.put("url",urlMap.get(jsonString));
                    mapList.add(hashMap);
                }
                //新的集合替换原来的集合
                specMap.put(key,mapList);
            }
            map.put("specMap",specMap);
            context.setVariables(map);

            //准备文件并且生成静态化文件
            File file = new File(pagePath);
            if (!file.exists()) {
                file.mkdirs();
            }
            File dest = new File(file, sku.getId() + ".html");
            try{
                //生成页面
                PrintWriter writer = new PrintWriter(dest, "utf-8");
                templateEngine.process("item",context,writer);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
