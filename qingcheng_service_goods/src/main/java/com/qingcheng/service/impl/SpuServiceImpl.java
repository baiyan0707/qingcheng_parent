package com.qingcheng.service.impl;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.qingcheng.dao.*;
import com.qingcheng.entity.PageResult;
import com.qingcheng.pojo.goods.*;
import com.qingcheng.service.goods.SkuService;
import com.qingcheng.service.goods.SpuService;
import com.qingcheng.util.IdWorker;
import com.sun.org.apache.bcel.internal.generic.NEW;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.*;

@Service(interfaceClass=SpuService.class)
public class SpuServiceImpl implements SpuService {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    SkuMapper skuMapper;

    @Autowired
    IdWorker idWorker;

    @Autowired
    CategoryMapper categoryMapper;

    @Autowired
    CategoryBrandMapper categoryBrandMapper;

    @Autowired
    AuditMapper auditMapper;

    @Autowired
    MarketableMapper marketableMapper;

    @Autowired
    SkuService skuService;

    /**
     * 返回全部记录
     * @return
     */
    public List<Spu> findAll() {
        return spuMapper.selectAll();
    }

    /**
     * 分页查询
     * @param page 页码
     * @param size 每页记录数
     * @return 分页结果
     */
    public PageResult<Spu> findPage(int page, int size) {
        PageHelper.startPage(page, size);
        Page<Spu> spus = (Page<Spu>) spuMapper.selectAll();
        return new PageResult<Spu>(spus.getTotal(), spus.getResult());
    }

    /**
     * 条件查询
     * @param searchMap 查询条件
     * @return
     */
    public List<Spu> findList(Map<String, Object> searchMap) {
        Example example = createExample(searchMap);
        return spuMapper.selectByExample(example);
    }

    /**
     * 分页+条件查询
     * @param searchMap
     * @param page
     * @param size
     * @return
     */
    public PageResult<Spu> findPage(Map<String, Object> searchMap, int page, int size) {
        PageHelper.startPage(page, size);
        Example example = createExample(searchMap);
        Page<Spu> spus = (Page<Spu>) spuMapper.selectByExample(example);
        return new PageResult<Spu>(spus.getTotal(), spus.getResult());
    }

    /**
     * 根据Id查询
     * @param id
     * @return
     */
    public Spu findById(String id) {
        return spuMapper.selectByPrimaryKey(id);
    }

    /**
     * 新增
     * @param spu
     */
    public void add(Spu spu) {
        spuMapper.insert(spu);
    }

    /**
     * 修改
     * @param spu
     */
    public void update(Spu spu) {
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    /**
     * 删除
     * @param id
     */
    public void delete(String id) {
        Spu spu = spuMapper.selectByPrimaryKey(id);
        //逻辑删除,改为状态码为1
        if(spu.getIsDelete().equals("0")){
            spu.setIsDelete("1");
            spuMapper.updateByPrimaryKeySelective(spu);
        }else if(spu.getIsDelete().equals("1")){
            //回收商品
            spu.setIsDelete("0");
            spuMapper.updateByPrimaryKeySelective(spu);
        }
        Map map = new HashMap();
        map.put("spuId",id);
        List<Sku> list = skuService.findList(map);
        for (Sku sku : list) {
            skuService.deletePriceFromRedis(sku.getId());
        }
    }

    /**
     * 保存sku和spu
     */
    @Transactional
    public void save(Goods goods) {
        //保存spu
        Spu spu = goods.getSpu();
        if (spu.getId() == null) {
            //添加
            spu.setId(idWorker.nextId() + "");
            spuMapper.insert(spu);
        } else {
            //修改
            Example example = new Example(Sku.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("spuId", spu.getId());
            skuMapper.deleteByExample(example);

            spuMapper.updateByPrimaryKeySelective(spu);
        }

        List<Sku> skuList = goods.getSkuList();
        Date date = new Date();
        //获取到分类信息
        Category category = categoryMapper.selectByPrimaryKey(spu.getCategory3Id());
        for (Sku sku : skuList) {
            if (sku.getId() == null) {
                //新增
                sku.setId(idWorker.nextId() + "");
                sku.setCreateTime(date);
            }
                //判断没有规格的商品
                if(StringUtils.isEmpty(sku.getSpec())){
                    sku.setSpec("{}");
                }
                //修改
                 sku.setSpuId(spu.getId());
                //保存sku列表的信息
                String name = spu.getName();
                Map<String, String> map = JSON.parseObject(sku.getSpec(), Map.class);
                for (String value : map.values()) {
                    name += " " + value;
                }
                sku.setName(name);
                sku.setUpdateTime(date);
                sku.setCategoryId(spu.getCategory3Id());
                sku.setCategoryName(category.getName());
                sku.setCommentNum(0); //评论数
                sku.setSaleNum(1); //销售数量

                skuMapper.insert(sku);
                //添加到缓存中
                skuService.savePriceToRedisById(sku.getId(),sku.getPrice());
            }
            //建立品牌和分类的关联
            CategoryBrand categoryBrand = new CategoryBrand();
            categoryBrand.setCategoryId(spu.getCategory3Id());
            categoryBrand.setBrandId(spu.getBrandId());
            int count = categoryBrandMapper.selectCount(categoryBrand);
            if (count == 0) {
                categoryBrandMapper.insert(categoryBrand);
            }
    }

    /**
     * 根据id查询商品
     * @param id
     * @return
     */
    public Goods findGoodsById (String id){
        Spu spu = spuMapper.selectByPrimaryKey(id);

        //获取sku信息
        Example example = new Example(Sku.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("spuId", id);
        List<Sku> skuList = skuMapper.selectByExample(example);

        //封装并返回
        Goods goods = new Goods();
        goods.setSkuList(skuList);
        goods.setSpu(spu);

        return goods;
    }

    /**
     * 商品审核
     * @param
     */
    @Transactional
    public void audit(String id, String status,String message) {
        Spu spu = new Spu();
        spu.setId(id);
        spu.setStatus(status);
        if("1".equals(status)){
            //商品上架
            spu.setIsMarketable(status);
        }
        spuMapper.updateByPrimaryKeySelective(spu);

        //记录商品审核记录
        Audit audit = new Audit();
        if(StringUtils.isEmpty(id)){
            //新增
            audit.setId(idWorker.nextId() + ""); //id
            audit.setUpdate_message(new String(message)); //日志信息
            audit.setUpdate_status(status); //状态信息
            audit.setUpdate_time(new Date()); //更新时间
            auditMapper.insert(audit);
        }else {
            //修改并记录日志
            Audit select_Audit = auditMapper.selectByPrimaryKey(id);

            audit.setHistory_time(select_Audit.getUpdate_time());
            audit.setHistory_message(select_Audit.getUpdate_message());
            audit.setHistory_status(select_Audit.getUpdate_status());

            audit.setId(id);
            audit.setUpdate_time(new Date());
            audit.setUpdate_status(status);
            audit.setUpdate_message(message);

            auditMapper.updateByPrimaryKeySelective(audit);
        }
    }

        /**
         * 商品下架
         * @param id
         */
        @Transactional
        public void pull(String id) {
            Spu spu = spuMapper.selectByPrimaryKey(id);
            spu.setIsMarketable("0");
            spuMapper.updateByPrimaryKeySelective(spu);
            this.putaway(id);
        }

    /**
     * 商品上架
     * @param id
     */
    public void put(String id) {
        Spu spu = spuMapper.selectByPrimaryKey(id);
        spu.setIsMarketable("1");
        spuMapper.updateByPrimaryKeySelective(spu);
        this.putaway(id);
    }

    //记录日志信息
    private Marketable putaway(String id){
        Spu spu = spuMapper.selectByPrimaryKey(id);
        Marketable marketable = new Marketable();
        //查询是否在数据库中存在
        Marketable select = marketableMapper.selectByPrimaryKey(id);
        if(StringUtils.isEmpty(select)){
            //第一次修改
            marketable.setId(id);
            marketable.setUpdate_marketable(spu.getIsMarketable());
            marketable.setUpdate_time(new Date());
            marketableMapper.insert(marketable);
        }else {
            //再次修改
            Marketable marketable_new = marketableMapper.selectByPrimaryKey(id);
            marketable.setHistory_time(marketable_new.getUpdate_time());
            marketable.setHistory_marketable(marketable_new.getUpdate_marketable());

            marketable.setId(id);
            marketable.setUpdate_time(new Date());
            marketable.setUpdate_marketable(marketable_new.getHistory_marketable());
            marketableMapper.updateByPrimaryKeySelective(marketable);
        }
        return marketable;
    }

    /**
     * 商品批量上架
     * @param ids
     * @return
     */
    public int putMany(Long[] ids) {
        Spu spu = new Spu();
        spu.setIsMarketable("1");
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("Id", Arrays.asList(ids));
        criteria.andEqualTo("isMarketable","0"); //批量下架
        criteria.andEqualTo("status","1"); //审核通过的
        criteria.andEqualTo("isDelete","0"); // 非删除的

        return spuMapper.updateByExampleSelective(spu,example);
    }

    /**
     * 商品批量下架
     * @param ids
     * @return
     */
    public int pullMany(Long[] ids) {
        Spu spu = new Spu();
        spu.setIsMarketable("0");
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("Id", Arrays.asList(ids));
        criteria.andEqualTo("isMarketable","1"); //批量上架
        criteria.andEqualTo("status","1"); //审核通过的
        criteria.andEqualTo("isDelete","0"); // 非删除的

        return spuMapper.updateByExampleSelective(spu,example);
    }


    /**
         * 构建查询条件
         * @param searchMap
         * @return
         */
        private Example createExample (Map < String, Object > searchMap){
            Example example = new Example(Spu.class);
            Example.Criteria criteria = example.createCriteria();
            if (searchMap != null) {
                // 主键
                if (searchMap.get("id") != null && !"".equals(searchMap.get("id"))) {
                    criteria.andLike("id", "%" + searchMap.get("id") + "%");
                }
                // 货号
                if (searchMap.get("sn") != null && !"".equals(searchMap.get("sn"))) {
                    criteria.andLike("sn", "%" + searchMap.get("sn") + "%");
                }
                // SPU名
                if (searchMap.get("name") != null && !"".equals(searchMap.get("name"))) {
                    criteria.andLike("name", "%" + searchMap.get("name") + "%");
                }
                // 副标题
                if (searchMap.get("caption") != null && !"".equals(searchMap.get("caption"))) {
                    criteria.andLike("caption", "%" + searchMap.get("caption") + "%");
                }
                // 图片
                if (searchMap.get("image") != null && !"".equals(searchMap.get("image"))) {
                    criteria.andLike("image", "%" + searchMap.get("image") + "%");
                }
                // 图片列表
                if (searchMap.get("images") != null && !"".equals(searchMap.get("images"))) {
                    criteria.andLike("images", "%" + searchMap.get("images") + "%");
                }
                // 售后服务
                if (searchMap.get("saleService") != null && !"".equals(searchMap.get("saleService"))) {
                    criteria.andLike("saleService", "%" + searchMap.get("saleService") + "%");
                }
                // 介绍
                if (searchMap.get("introduction") != null && !"".equals(searchMap.get("introduction"))) {
                    criteria.andLike("introduction", "%" + searchMap.get("introduction") + "%");
                }
                // 规格列表
                if (searchMap.get("specItems") != null && !"".equals(searchMap.get("specItems"))) {
                    criteria.andLike("specItems", "%" + searchMap.get("specItems") + "%");
                }
                // 参数列表
                if (searchMap.get("paraItems") != null && !"".equals(searchMap.get("paraItems"))) {
                    criteria.andLike("paraItems", "%" + searchMap.get("paraItems") + "%");
                }
                // 是否上架
                if (searchMap.get("isMarketable") != null && !"".equals(searchMap.get("isMarketable"))) {
                    criteria.andLike("isMarketable", "%" + searchMap.get("isMarketable") + "%");
                }
                // 是否启用规格
                if (searchMap.get("isEnableSpec") != null && !"".equals(searchMap.get("isEnableSpec"))) {
                    criteria.andLike("isEnableSpec", "%" + searchMap.get("isEnableSpec") + "%");
                }
                // 是否删除
                if (searchMap.get("isDelete") != null && !"".equals(searchMap.get("isDelete"))) {
                    criteria.andLike("isDelete", "%" + searchMap.get("isDelete") + "%");
                }
                // 审核状态
                if (searchMap.get("status") != null && !"".equals(searchMap.get("status"))) {
                    criteria.andLike("status", "%" + searchMap.get("status") + "%");
                }

                // 品牌ID
                if (searchMap.get("brandId") != null) {
                    criteria.andEqualTo("brandId", searchMap.get("brandId"));
                }
                // 一级分类
                if (searchMap.get("category1Id") != null) {
                    criteria.andEqualTo("category1Id", searchMap.get("category1Id"));
                }
                // 二级分类
                if (searchMap.get("category2Id") != null) {
                    criteria.andEqualTo("category2Id", searchMap.get("category2Id"));
                }
                // 三级分类
                if (searchMap.get("category3Id") != null) {
                    criteria.andEqualTo("category3Id", searchMap.get("category3Id"));
                }
                // 模板ID
                if (searchMap.get("templateId") != null) {
                    criteria.andEqualTo("templateId", searchMap.get("templateId"));
                }
                // 运费模板id
                if (searchMap.get("freightId") != null) {
                    criteria.andEqualTo("freightId", searchMap.get("freightId"));
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
