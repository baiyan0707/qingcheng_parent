package com.qingcheng.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.qingcheng.dao.SkuMapper;
import com.qingcheng.entity.PageResult;
import com.qingcheng.pojo.goods.Sku;
import com.qingcheng.pojo.order.OrderItem;
import com.qingcheng.service.goods.SkuService;
import com.qingcheng.util.CacheKey;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service(interfaceClass = SkuService.class)
public class SkuServiceImpl implements SkuService {

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    RabbitTemplate rabbitTemplate;

    /**
     * 根据购物车批量扣减库存
     * @param orderItemList
     * @return
     */
    @Transactional
    public boolean deductionStock(List<OrderItem> orderItemList) {
        //检查是否可以扣减库存
        boolean idDeduction = true;
        for (OrderItem orderItem : orderItemList) {
            Sku sku = this.findById(orderItem.getSkuId());
            if (sku == null) {
                idDeduction = false;
                break;
            }
            if (!sku.getStatus().equals("1")) {
                idDeduction = false;
                break;
            }
            if (sku.getNum().intValue() < orderItem.getNum().intValue()) {
                idDeduction = false;
                break;
            }
        }
        //执行满减
        if(idDeduction){
            for (OrderItem orderItem : orderItemList) {
                skuMapper.deductionStock(orderItem.getSkuId(),orderItem.getNum());
                skuMapper.addSaleNum(orderItem.getSkuId(),orderItem.getNum());
            }
        }
        return idDeduction;
    }

    /**
     * 商品上架并且发送消息到mq
     * @param id 前端传入需要上架商品的id
     */
    public void CommodityPutaway(String id) {
        //发送消息到mq
        rabbitTemplate.convertAndSend("exchange_putaway", "", id);
        System.out.println(id);
    }

    /**
     * 商品下架并且发送消息到mq
     * @param id 前端传入需要下架商品的id
     */
    public void CommodityOut(String id) {
        //发送消息到mq
        rabbitTemplate.convertAndSend("exchange_sold_out", "", JSON.toJSONString(id));
        System.out.println(id);
    }

    /**
     * 返回全部记录
     * @return
     */
    public List<Sku> findAll() {
        return skuMapper.selectAll();
    }

    /**
     * 分页查询
     * @param page 页码
     * @param size 每页记录数
     * @return 分页结果
     */
    public PageResult<Sku> findPage(int page, int size) {
        PageHelper.startPage(page, size);
        Page<Sku> skus = (Page<Sku>) skuMapper.selectAll();
        return new PageResult<Sku>(skus.getTotal(), skus.getResult());
    }

    /**
     * 条件查询
     * @param searchMap 查询条件
     * @return
     */
    public List<Sku> findList(Map<String, Object> searchMap) {
        Example example = createExample(searchMap);
        return skuMapper.selectByExample(example);
    }

    /**
     * 分页+条件查询
     * @param searchMap
     * @param page
     * @param size
     * @return
     */
    public PageResult<Sku> findPage(Map<String, Object> searchMap, int page, int size) {
        PageHelper.startPage(page, size);
        Example example = createExample(searchMap);
        Page<Sku> skus = (Page<Sku>) skuMapper.selectByExample(example);
        return new PageResult<Sku>(skus.getTotal(), skus.getResult());
    }

    /**
     * 根据Id查询
     * @param id
     * @return
     */
    public Sku findById(String id) {
        return skuMapper.selectByPrimaryKey(id);
    }

    /**
     * 新增
     * @param sku
     */
    public void add(Sku sku) {
        skuMapper.insert(sku);
    }

    /**
     * 修改
     * @param sku
     */
    public void update(Sku sku) {
        skuMapper.updateByPrimaryKeySelective(sku);
    }

    /**
     * 删除
     * @param id
     */
    public void delete(String id) {
        skuMapper.deleteByPrimaryKey(id);
    }

    /**
     * 将所有价格添加到缓存中
     */
    @Override
    public void saveAllPriceToRedis() {
        //判断缓存中是否存在价格
        if (!redisTemplate.hasKey(CacheKey.SKU_PRICE)) {
            List<Sku> skuList = skuMapper.selectAll();
            for (Sku sku : skuList) {
                //状态是否正常
                if ("1".equals(sku.getStatus())) {
                    redisTemplate.boundHashOps(CacheKey.SKU_PRICE).put(sku.getId(), sku.getPrice());
                }
            }
        }
    }

    /**
     * 将所有sku导入ES
     */
    @Override
    public void importAllSkuToES() throws IOException {
        // 创建rest客户端对象
        HttpHost httpHost = new HttpHost("127.0.0.1", 9200, "http");
        RestClientBuilder restClientBuilder = RestClient.builder(httpHost);
        restClientBuilder.setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
            @Override
            public RequestConfig.Builder customizeRequestConfig(RequestConfig.Builder requestConfigBuilder) {
                requestConfigBuilder.setConnectTimeout(5 * 60 * 1000);
                requestConfigBuilder.setSocketTimeout(5 * 60 * 1000);
                requestConfigBuilder.setConnectionRequestTimeout(5 * 60 * 1000);
                return requestConfigBuilder;
            }
        }).setMaxRetryTimeoutMillis(5 * 60 * 1000);
        RestHighLevelClient client = new RestHighLevelClient(restClientBuilder);

        // 封装请求对象
        BulkRequest bulkRequest = new BulkRequest(); // 批量处理
        List<Sku> skuList = skuMapper.selectAll(); // 如果数据很大呢？比如1TB，解决方案，分页查询，批量导入
        skuList.forEach(sku -> {
            // 创建IndexRequest
            IndexRequest indexRequest = new IndexRequest("sku", "doc", sku.getId());
            // 封装skuMap
            Map<String, Object> skuMap = new HashMap<>();
            skuMap.put("name", sku.getName());
            skuMap.put("brandName", sku.getBrandName());
            skuMap.put("categoryName", sku.getCategoryName());
            skuMap.put("price", sku.getPrice());
            skuMap.put("createTime", sku.getCreateTime());
            skuMap.put("saleNum", sku.getSaleNum());
            skuMap.put("commentNum", sku.getCommentNum());
            skuMap.put("spuId", sku.getSpuId());
            skuMap.put("image", sku.getImage());

            Map<String, Object> spec = JSON.parseObject(sku.getSpec());
            skuMap.put("spec", spec);
            indexRequest.source(skuMap);
            // 将IndexRequest添加到BulkRequest
            bulkRequest.add(indexRequest);
        });
        // 执行BulkRequest
        BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
        System.out.println(bulkResponse.status().getStatus());
        System.out.println(bulkResponse.buildFailureMessage());

        // 关闭客户端
        client.close();
    }

    /**
     * 根据id查询到价格
     * @param id
     * @return
     */
    @Override
    public Integer findPrice(String id) {
        //从缓存中查询
        return (Integer) redisTemplate.boundHashOps(CacheKey.SKU_PRICE).get(id);
    }

    /**
     * 更新价格
     * @param id
     * @param price
     */
    @Override
    public void savePriceToRedisById(String id, Integer price) {
        //缓存中查询
        redisTemplate.boundHashOps(CacheKey.SKU_PRICE).put(id, price);
    }

    /**
     * 根据id查询缓存中的信息并删除
     * @param id
     */
    @Override
    public void deletePriceFromRedis(String id) {
        redisTemplate.boundHashOps(CacheKey.SKU_PRICE).delete(id);
    }

    /**
     * 构建查询条件
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap) {
        Example example = new Example(Sku.class);
        Example.Criteria criteria = example.createCriteria();
        if (searchMap != null) {
            // 商品id
            if (searchMap.get("id") != null && !"".equals(searchMap.get("id"))) {
                criteria.andLike("id", "%" + searchMap.get("id") + "%");
            }
            // 商品条码
            if (searchMap.get("sn") != null && !"".equals(searchMap.get("sn"))) {
                criteria.andLike("sn", "%" + searchMap.get("sn") + "%");
            }
            // SKU名称
            if (searchMap.get("name") != null && !"".equals(searchMap.get("name"))) {
                criteria.andLike("name", "%" + searchMap.get("name") + "%");
            }
            // 商品图片
            if (searchMap.get("image") != null && !"".equals(searchMap.get("image"))) {
                criteria.andLike("image", "%" + searchMap.get("image") + "%");
            }
            // 商品图片列表
            if (searchMap.get("images") != null && !"".equals(searchMap.get("images"))) {
                criteria.andLike("images", "%" + searchMap.get("images") + "%");
            }
            // SPUID
            if (searchMap.get("spuId") != null && !"".equals(searchMap.get("spuId"))) {
                criteria.andLike("spuId", "%" + searchMap.get("spuId") + "%");
            }
            // 类目名称
            if (searchMap.get("categoryName") != null && !"".equals(searchMap.get("categoryName"))) {
                criteria.andLike("categoryName", "%" + searchMap.get("categoryName") + "%");
            }
            // 品牌名称
            if (searchMap.get("brandName") != null && !"".equals(searchMap.get("brandName"))) {
                criteria.andLike("brandName", "%" + searchMap.get("brandName") + "%");
            }
            // 规格
            if (searchMap.get("spec") != null && !"".equals(searchMap.get("spec"))) {
                criteria.andLike("spec", "%" + searchMap.get("spec") + "%");
            }
            // 商品状态 1-正常，2-下架，3-删除
            if (searchMap.get("status") != null && !"".equals(searchMap.get("status"))) {
                criteria.andLike("status", "%" + searchMap.get("status") + "%");
            }

            // 价格（分）
            if (searchMap.get("price") != null) {
                criteria.andEqualTo("price", searchMap.get("price"));
            }
            // 库存数量
            if (searchMap.get("num") != null) {
                criteria.andEqualTo("num", searchMap.get("num"));
            }
            // 库存预警数量
            if (searchMap.get("alertNum") != null) {
                criteria.andEqualTo("alertNum", searchMap.get("alertNum"));
            }
            // 重量（克）
            if (searchMap.get("weight") != null) {
                criteria.andEqualTo("weight", searchMap.get("weight"));
            }
            // 类目ID
            if (searchMap.get("categoryId") != null) {
                criteria.andEqualTo("categoryId", searchMap.get("categoryId"));
            }
            // 销量
            if (searchMap.get("saleNum") != null) {
                criteria.andEqualTo("saleNum", searchMap.get("saleNum"));
            }
            // 评论数
            if (searchMap.get("commentNum") != null) {
                criteria.andEqualTo("commentNum", searchMap.get("commentNum"));
            }

        }
        return example;
    }

}
