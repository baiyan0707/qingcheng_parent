package com.qingcheng.service.impl;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.qingcheng.dao.OrderItemMapper;
import com.qingcheng.dao.OrderLogMapper;
import com.qingcheng.dao.OrderMapper;
import com.qingcheng.entity.PageResult;
import com.qingcheng.pojo.order.Order;
import com.qingcheng.pojo.order.OrderItem;
import com.qingcheng.pojo.order.OrderLog;
import com.qingcheng.pojo.order.Orders;
import com.qingcheng.service.goods.SkuService;
import com.qingcheng.service.order.CartService;
import com.qingcheng.service.order.OrderService;
import com.qingcheng.service.pay.WxPayService;
import com.qingcheng.util.IdWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;
import org.springframework.beans.BeanUtils;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("all")
@Service(interfaceClass = OrderService.class)
public class OrderServiceImpl implements OrderService {

    Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    OrderItemMapper orderItemMapper;

    @Autowired
    IdWorker idWorker;

    @Autowired
    OrderLogMapper orderLogMapper;

    @Autowired
    CartService cartService;

    @Reference
    SkuService skuService;

    @Autowired
    RabbitTemplate rabbitTemplate;
    /**
     * 修改订单信息
     */
    @Override
    public void closeOrder(String id){
        System.out.println("订单超时自动关闭");
        Order order = orderMapper.selectByPrimaryKey(id);
       if(order == null){
           throw new RuntimeException("未查到订单信息~");
       }
        //修改订单信息
        order.setPayStatus("0");
        order.setOrderStatus("0");
        order.setUpdateTime(new Date());
        order.setCloseTime(new Date());
        orderMapper.updateByPrimaryKey(order);
        //记录订单变动日志
        OrderLog orderLog=new OrderLog();
        orderLog.setOperater("system");
        orderLog.setOperateTime(new Date());
        orderLog.setOrderStatus("0");
        orderLog.setPayStatus("0");
        orderLog.setOrderId(order.getId());
        orderLogMapper.insert(orderLog);
    }

    /**
     * 修改订单状态
     * @param orderId
     * @param transactionId
     */
    @Override
    @Transactional
    public void updatePayStatus(String orderId,String transactionId){
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if(order != null && "0".equals(order.getPayStatus())){
            order.setPayStatus("1");
            order.setOrderStatus("1");
            order.setUpdateTime(new Date());
            order.setPayTime(new Date());
            order.setTransactionId(transactionId); //微信返回的交易流水号
            orderMapper.updateByPrimaryKeySelective(order);
            //记录订单变动日志
            OrderLog orderLog=new OrderLog();
            orderLog.setOperater("system");
            orderLog.setOperateTime(new Date());
            orderLog.setOrderStatus("1");
            orderLog.setPayStatus("1");
            orderLog.setRemarks("支付流水号"+transactionId);
            orderLog.setOrderId(order.getId());
            orderLogMapper.insert(orderLog);
        }
    }

    /**
     * 返回全部记录
     * @return
     */
    @Override
    public List<Order> findAll() {
        return orderMapper.selectAll();
    }

    /**
     * 分页查询
     * @param page 页码
     * @param size 每页记录数
     * @return 分页结果
     */
    @Override
    public PageResult<Order> findPage(int page, int size) {
        PageHelper.startPage(page,size);
        Page<Order> orders = (Page<Order>) orderMapper.selectAll();
        return new PageResult<Order>(orders.getTotal(),orders.getResult());
    }

    /**
     * 条件查询
     * @param searchMap 查询条件
     * @return
     */
    @Override
    public List<Order> findList(Map<String, Object> searchMap) {
        Example example = createExample(searchMap);
        return orderMapper.selectByExample(example);
    }

    /**
     * 分页+条件查询
     * @param searchMap
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageResult<Order> findPage(Map<String, Object> searchMap, int page, int size) {
        PageHelper.startPage(page,size);
        Example example = createExample(searchMap);
        Page<Order> orders = (Page<Order>) orderMapper.selectByExample(example);
        return new PageResult<Order>(orders.getTotal(),orders.getResult());
    }

    /**
     * 前端传入id，后台将所有关联数据返回
     * @param id
     * @return
     */
    @Override
    public Orders findByOrderId(String id) {
        Order order = orderMapper.selectByPrimaryKey(id);

        //获取订单从表
        Example example = new Example(OrderItem.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("id", id);
        List<OrderItem> orderItems = orderItemMapper.selectByExample(example);

        //封装并返回
        Orders orders = new Orders();
        orders.setOrder(order);
        orders.setOrderItemList(orderItems);
        return orders;
    }

    /**
     * 前端传递一个id数组，将未发货订单返回
     * @param ids
     * @return
     */
    @Override
    public Order findByIds(String[] ids){
        for (String id : ids) {
            if(StringUtils.isEmpty(id)){
                throw new RuntimeException("你输入的订单id为空");
            }
            Order order = orderMapper.selectByPrimaryKey(id);
            if (order.getConsignStatus().equals("0")) {
                return order;
            }
        }
        return null;
    }

    @Override
    public Order findById(String id){
        return orderMapper.selectByPrimaryKey(id);
    }

    /**
     * 前端传递参数，后端将发货状态改为已发货并记录日志信息 批量发货
     * @param map
     */
    @Override
    public void updateOrder(Map<String,String> map){
       if(map == null || map.size() == 0){
           throw new RuntimeException("你输入的信息为空");
    }
    String id = map.get("id");
        if(StringUtils.isEmpty(id)){
        throw new RuntimeException("未查询到此信息");
    }
        if(StringUtils.isEmpty(map.get("shippingName")) || StringUtils.isEmpty(map.get("shippingCode"))){
        throw new RuntimeException("请填写正确的快递公司及订单号");
    }
        Order order = orderMapper.selectByPrimaryKey(id);
        if(order == null){
            throw new RuntimeException("抱歉，查不到该条记录.");
        }

        order.setConsignStatus("2");
        order.setConsignTime(new Date()); //发货时间
        order.setOrderStatus("3"); //订单状态 已发货
        orderMapper.updateByPrimaryKeySelective(order);

        //记录日志信息
        OrderLog orderLog = new OrderLog();
        orderLog.setId(idWorker.nextId() + "");
        orderLog.setOperater("admin");
        orderLog.setOperateTime(new Date());
        orderLog.setOrderId(order.getId());
        orderLog.setOrderStatus(order.getOrderStatus());
        orderLog.setPayStatus(order.getPayStatus());
        orderLog.setConsignStatus(order.getConsignStatus());
        orderLog.setRemarks(map.get("remarks")); //日志信息

        orderLogMapper.insert(orderLog);
    }

    /**
     * 订单合并
     * @param mergeMap
     */
    @Transactional
    @Override
    public Order merge(Map<String,String> mergeMap){
        if(StringUtils.isEmpty(mergeMap.get("orderId1")) && StringUtils.isEmpty(mergeMap.get("orderId2"))){
            throw new RuntimeException("请输入正确的订单id");
        }
        //根据主表id和从表id获取到订单信息
        String orderId1 = mergeMap.get("orderId1");
        String orderId2 = mergeMap.get("orderId2");
        Order order_1 = orderMapper.selectByPrimaryKey(orderId1);
        Order order_2 = orderMapper.selectByPrimaryKey(orderId2);

        //合并信息,将从表的金额信息添加到主表中
        order_1.setTotalNum(order_1.getTotalNum() + order_2.getTotalNum()); //数量合计
        order_1.setTotalMoney(order_1.getPayMoney() + order_2.getTotalMoney()); //金额合计
        order_1.setPreMoney(order_1.getPreMoney() + order_2.getPreMoney()); //优惠金额
        order_1.setPostFee(order_1.getPostFee() + order_2.getPostFee()); //邮费
        order_1.setPayMoney(order_1.getPayMoney() + order_2.getPayMoney()); //实付金额
        order_1.setUpdateTime(new Date()); //更新时间以当前时间为准
        orderMapper.updateByPrimaryKeySelective(order_1);

        //将从表的信息逻辑删除
        order_2.setIsDelete("1");
        orderMapper.updateByPrimaryKeySelective(order_2);

        //更改订单明细表
        OrderItem orderItem_01 = orderItemMapper.selectByPrimaryKey(orderId1);
        OrderItem orderItem_02= orderItemMapper.selectByPrimaryKey(orderId2);
        orderItem_02.setOrderId(orderItem_01.getOrderId()); //将订单明细表订单id全部改为主表id
        orderItemMapper.updateByPrimaryKeySelective(orderItem_02);

        //记录日志
        OrderLog orderLog = new OrderLog();
        orderLog.setId(idWorker.nextId() + "");
        orderLog.setOperateTime(new Date()); //操作时间
        orderLog.setOrderId(orderId1); //订单id
        orderLog.setOperater(mergeMap.get("operater")); //操作人员
        orderLog.setOrderStatus(order_1.getOrderStatus()); //订单状态
        orderLog.setPayStatus(order_1.getPayStatus()); //支付状态
        orderLog.setConsignStatus(order_1.getConsignStatus()); //发货状态
        orderLog.setRemarks("这个人很懒，还没有记录信息~");

        orderLogMapper.insert(orderLog);

        return order_1;
    }

    /**
     * 订单拆分
     * @param splitList
     * @return
     */
    @Override
    public void split(List<HashMap<String,Integer>> splitList){
        if(splitList == null || splitList.size() == 0){
            throw new RuntimeException("请输入要拆分的数量");
        }
        //获取到用户的id，根据id查询到订单详情
        for (int i = 0; i < splitList.size(); i++) {
            String id = String.valueOf(splitList.get(i).get("id"));
            OrderItem orderItem = orderItemMapper.selectByPrimaryKey(id);
            //判断拆分数量是否大于总数量
            Integer num = splitList.get(i).get("num");
            if(num > orderItem.getNum()){
                throw new RuntimeException("拆分数量不可大于总数量");
            }
            //主订单减去拆分数量和金额
            orderItem.setNum(orderItem.getNum() - num);
            //将实付金额拆分 现在的金额等于 当前总金额 - (单价 * 拆分的数量)
            orderItem.setPayMoney(orderItem.getPayMoney() - (num * orderItem.getPrice()));
            orderItemMapper.updateByPrimaryKeySelective(orderItem);

            //新建立订单明细
            OrderItem orderItem_new = new OrderItem();
            BeanUtils.copyProperties(orderItem,orderItem_new);
            orderItem_new.setId(idWorker.nextId() + "");
            orderItem_new.setNum(num);
            orderItem_new.setPayMoney(num * orderItem.getPrice());
            orderItemMapper.insert(orderItem_new);
        }
    }

    /**
     * 新增订单信息
     * @param order
     */
    @Override
    public Map<String,Object> add(Order order) {
        List<Map<String, Object>> orderItemList = cartService.findNewOrderItemList(order.getUsername());
        List<OrderItem> orderItems = orderItemList.stream().filter(cart -> (boolean) cart.get("checked")).map(cart -> (OrderItem) cart.get("item")).collect(Collectors.toList());
        if(!skuService.deductionStock(orderItems)){
            throw new RuntimeException("库存扣减失败~");
        }
        try {
            //支付价格
            int totalMoney = orderItems.stream().mapToInt(OrderItem::getMoney).sum();
            //总数量
            int totalNum = orderItems.stream().mapToInt(OrderItem::getNum).sum();
            int preMoney = cartService.preferential(order.getUsername()); //优惠价格
            //新增订单
            order.setId(idWorker.nextId() + "");
            order.setTotalNum(totalNum);
            order.setTotalMoney(totalMoney);
            order.setPreMoney(preMoney);
            order.setPayMoney(totalMoney - preMoney);
            order.setCreateTime(new Date());
            order.setOrderStatus("0");
            order.setPayStatus("0"); //未支付
            order.setConsignStatus("0"); //发货状态：未发货
            orderMapper.insert(order);

            //发送商品id到mq，用于检验商品是否过期
            rabbitTemplate.convertAndSend("","queue.ordercreate",JSON.toJSONString(order.getId()));

            //获取到优惠比例
            double proportion = (double) order.getPayMoney() / totalMoney;
            //新增订单明细
            for (OrderItem orderItem : orderItems) {
                orderItem.setOrderId(order.getId());
                orderItem.setId(idWorker.nextId() + "");
                orderItem.setPayMoney((int) (orderItem.getMoney() * proportion));
                orderItemMapper.insert(orderItem);
            }
        }catch (Exception e){
            //如果过程有失败的数据，则会通过mq发送到sku表中进行回滚
            rabbitTemplate.convertAndSend("","queue.skuback", JSON.toJSONString(orderItems));

            logger.error("购物车清除失败，订单号：" + order.getId() + "用户名：" + order.getUsername() + "购物车列表：" + JSON.toJSONString(orderItems));
        }
        //清除购物车记录
        cartService.deleteCheckedCart(order.getUsername());
        //返回订单号和支付的金额
        Map map = new HashMap();
        map.put("ordersn", order.getId());
        map.put("money", order.getPayMoney());
        return map;
    }

    /**
     * 修改
     * @param order
     */
    @Override
    public void update(Order order) {
        orderMapper.updateByPrimaryKeySelective(order);
    }

    /**
     *  删除
     * @param id
     */
    @Override
    public void delete(String id) {
        orderMapper.deleteByPrimaryKey(id);
    }

    /**
     * 构建查询条件
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap){
        Example example=new Example(Order.class);
        Example.Criteria criteria = example.createCriteria();
        if(searchMap!=null){
            // 订单id
            if(searchMap.get("id")!=null && !"".equals(searchMap.get("id"))){
                criteria.andLike("id","%"+searchMap.get("id")+"%");
            }
            // 支付类型，1、在线支付、0 货到付款
            if(searchMap.get("payType")!=null && !"".equals(searchMap.get("payType"))){
                criteria.andLike("payType","%"+searchMap.get("payType")+"%");
            }
            // 物流名称
            if(searchMap.get("shippingName")!=null && !"".equals(searchMap.get("shippingName"))){
                criteria.andLike("shippingName","%"+searchMap.get("shippingName")+"%");
            }
            // 物流单号
            if(searchMap.get("shippingCode")!=null && !"".equals(searchMap.get("shippingCode"))){
                criteria.andLike("shippingCode","%"+searchMap.get("shippingCode")+"%");
            }
            // 用户名称
            if(searchMap.get("username")!=null && !"".equals(searchMap.get("username"))){
                criteria.andLike("username","%"+searchMap.get("username")+"%");
            }
            // 买家留言
            if(searchMap.get("buyerMessage")!=null && !"".equals(searchMap.get("buyerMessage"))){
                criteria.andLike("buyerMessage","%"+searchMap.get("buyerMessage")+"%");
            }
            // 是否评价
            if(searchMap.get("buyerRate")!=null && !"".equals(searchMap.get("buyerRate"))){
                criteria.andLike("buyerRate","%"+searchMap.get("buyerRate")+"%");
            }
            // 收货人
            if(searchMap.get("receiverContact")!=null && !"".equals(searchMap.get("receiverContact"))){
                criteria.andLike("receiverContact","%"+searchMap.get("receiverContact")+"%");
            }
            // 收货人手机
            if(searchMap.get("receiverMobile")!=null && !"".equals(searchMap.get("receiverMobile"))){
                criteria.andLike("receiverMobile","%"+searchMap.get("receiverMobile")+"%");
            }
            // 收货人地址
            if(searchMap.get("receiverAddress")!=null && !"".equals(searchMap.get("receiverAddress"))){
                criteria.andLike("receiverAddress","%"+searchMap.get("receiverAddress")+"%");
            }
            // 订单来源：1:web，2：app，3：微信公众号，4：微信小程序  5 H5手机页面
            if(searchMap.get("sourceType")!=null && !"".equals(searchMap.get("sourceType"))){
                criteria.andLike("sourceType","%"+searchMap.get("sourceType")+"%");
            }
            // 交易流水号
            if(searchMap.get("transactionId")!=null && !"".equals(searchMap.get("transactionId"))){
                criteria.andLike("transactionId","%"+searchMap.get("transactionId")+"%");
            }
            // 订单状态
            if(searchMap.get("orderStatus")!=null && !"".equals(searchMap.get("orderStatus"))){
                criteria.andLike("orderStatus","%"+searchMap.get("orderStatus")+"%");
            }
            // 支付状态
            if(searchMap.get("payStatus")!=null && !"".equals(searchMap.get("payStatus"))){
                criteria.andLike("payStatus","%"+searchMap.get("payStatus")+"%");
            }
            // 发货状态
            if(searchMap.get("consignStatus")!=null && !"".equals(searchMap.get("consignStatus"))){
                criteria.andLike("consignStatus","%"+searchMap.get("consignStatus")+"%");
            }
            // 是否删除
            if(searchMap.get("isDelete")!=null && !"".equals(searchMap.get("isDelete"))){
                criteria.andLike("isDelete","%"+searchMap.get("isDelete")+"%");
            }

            // 数量合计
            if(searchMap.get("totalNum")!=null ){
                criteria.andEqualTo("totalNum",searchMap.get("totalNum"));
            }
            // 金额合计
            if(searchMap.get("totalMoney")!=null ){
                criteria.andEqualTo("totalMoney",searchMap.get("totalMoney"));
            }
            // 优惠金额
            if(searchMap.get("preMoney")!=null ){
                criteria.andEqualTo("preMoney",searchMap.get("preMoney"));
            }
            // 邮费
            if(searchMap.get("postFee")!=null ){
                criteria.andEqualTo("postFee",searchMap.get("postFee"));
            }
            // 实付金额
            if(searchMap.get("payMoney")!=null ){
                criteria.andEqualTo("payMoney",searchMap.get("payMoney"));
            }

        }
        return example;
    }

}
