package com.qingcheng.service.impl;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.qingcheng.dao.*;
import com.qingcheng.entity.PageResult;
import com.qingcheng.pojo.order.*;
import com.qingcheng.service.order.ReturnOrderService;
import com.qingcheng.util.IdWorker;
import javafx.util.converter.LocalDateStringConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import tk.mybatis.mapper.entity.Example;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ReturnOrderServiceImpl implements ReturnOrderService {

    @Autowired
    private ReturnOrderMapper returnOrderMapper;

    @Autowired
    OrderMapper orderMapper;

    @Autowired
    ReturnOrderItemMapper returnorderItemMapper;

    @Autowired
    OrderItemMapper orderItemMapper;

    @Autowired
    OrderConfigMapper orderConfigMapper;

    @Autowired
    OrderLogMapper orderLogMapper;

    @Autowired
    IdWorker idWorker;

    /**
     * 返回全部记录
     * @return
     */
    public List<ReturnOrder> findAll(){
        return returnOrderMapper.selectAll();
    }

    /**
     * 分页查询
     * @param page 页码
     * @param size 每页记录数
     * @return 分页结果
     */
    public PageResult<ReturnOrder> findPage(int page, int size) {
        PageHelper.startPage(page, size);
        Page<ReturnOrder> returnOrders = (Page<ReturnOrder>) returnOrderMapper.selectAll();
        return new PageResult<ReturnOrder>(returnOrders.getTotal(), returnOrders.getResult());
    }

    /**
     * 条件查询
     * @param searchMap 查询条件
     * @return
     */
    public List<ReturnOrder> findList(Map<String, Object> searchMap) {
        Example example = createExample(searchMap);
        return returnOrderMapper.selectByExample(example);
    }

    /**
     * 分页+条件查询
     * @param searchMap
     * @param page
     * @param size
     * @return
     */
    public PageResult<ReturnOrder> findPage(Map<String, Object> searchMap, int page, int size) {
        PageHelper.startPage(page, size);
        Example example = createExample(searchMap);
        Page<ReturnOrder> returnOrders = (Page<ReturnOrder>) returnOrderMapper.selectByExample(example);
        return new PageResult<ReturnOrder>(returnOrders.getTotal(), returnOrders.getResult());
    }

    /**
     * 根据Id查询
     * @param id
     * @return
     */
    public ReturnOrder findById(Long id) {
        return returnOrderMapper.selectByPrimaryKey(id);
    }

    /**
     * 新增
     * @param returnOrder
     */
    public void add(ReturnOrder returnOrder) {
        returnOrderMapper.insert(returnOrder);
    }

    /**
     * 修改
     * @param returnOrder
     */
    public void update(ReturnOrder returnOrder) {
        returnOrderMapper.updateByPrimaryKeySelective(returnOrder);
    }

    /**
     * 删除
     * @param id
     */
    public void delete(Long id) {
        returnOrderMapper.deleteByPrimaryKey(id);
    }

    /**
     * 退货或者退款
     * @param returnMap
     */
    public void sendBack(Map<String, String> returnMap) {
        String id = returnMap.get("id");
        ReturnOrder returnOrder = returnOrderMapper.selectByPrimaryKey(id);
        if (returnOrder == null) {
            throw new RuntimeException("请输入正确的信息！");
        }
        //用户退货
        if (returnOrder.getType().equals("1")) {
            this.returnSales(returnMap);
        }
        //用户退款
        if (returnOrder.getType().equals("2")) {
            this.returnRefund(returnMap);
        }
    }

    //判断是否退货
    private void returnSales(Map<String, String> returnMap) {
        String id = returnMap.get("id");
        ReturnOrder returnOrder = returnOrderMapper.selectByPrimaryKey(id);
        //退货成功,描述信息长度大于15，图片信息不为空
        if (!StringUtils.isEmpty(returnMap.get("description")) && !StringUtils.isEmpty(returnMap.get("evidence")) && returnMap.get("description").length() > 15) {
            //执行退货流程
            ReturnOrder returnOrder_new = new ReturnOrder();
            returnOrder_new.setId(idWorker.nextId() + "");
            returnOrder_new.setStatus("1"); //退货成功
            returnOrder_new.setOrderId(id); //订单id
            returnOrder_new.setRemark(returnOrder.getRemark()); //处理备注
            returnOrder_new.setDisposeTime(new Date()); //退货时间
            returnOrder_new.setDescription(returnOrder.getDescription()); //问题描述
            returnOrder_new.setDescription(returnOrder.getDescription()); //图片信息
            returnOrder_new.setAdminId(returnOrder.getAdminId()); //管理员信息id
            returnOrderMapper.insert(returnOrder_new);
        } else {
            //退货失败
            ReturnOrder returnOrder_new = new ReturnOrder();
            returnOrder_new.setId(idWorker.nextId() + "");
            returnOrder_new.setStatus("2"); //退货驳回
            returnOrder_new.setOrderId(id); //订单id
            returnOrder_new.setRemark(returnOrder.getRemark()); //处理备注
            returnOrder_new.setDisposeTime(new Date()); //退货时间
            returnOrder_new.setDescription(returnMap.get("description")); //问题描述
            returnOrder_new.setDescription(returnMap.get("evidence")); //图片信息
            returnOrder_new.setAdminId(returnOrder.getAdminId()); //管理员信息id
            returnOrderMapper.insert(returnOrder_new);

            this.return_example(returnOrder);
        }
    }

    //判断是否退款
    private void returnRefund(Map<String, String> returnMap) {
        String id = returnMap.get("id");
        ReturnOrder returnOrder = returnOrderMapper.selectByPrimaryKey(id);
        //获取到用户的付款金额
        Order order = orderMapper.selectByPrimaryKey(returnOrder.getId());
        Integer payMoney = order.getPayMoney();
        Integer return_money = Integer.parseInt(returnMap.get("return_money"));
        //退款成功
        if (return_money <= payMoney && returnMap.get("description").length() > 15) {
            //修改状态
            ReturnOrder returnOrder_new = new ReturnOrder();
            returnOrder_new.setId(idWorker.nextId() + "");
            returnOrder_new.setReturnMoney(payMoney);
            returnOrder_new.setStatus("1");//同意
            returnOrder_new.setOrderId(id); //订单id
            returnOrder_new.setAdminId(returnOrder.getAdminId());//管理员
            returnOrder_new.setDisposeTime(new Date());//处理日期

            returnOrderMapper.insert(returnOrder_new);//保存
        } else {
            //退款失败
            ReturnOrder returnOrder_new = new ReturnOrder();
            returnOrder_new.setId(idWorker.nextId() + "");
            returnOrder_new.setStatus("2");//失败
            returnOrder_new.setOrderId(id); //订单id
            returnOrder_new.setRemark(returnMap.get("description"));
            returnOrder_new.setAdminId(returnOrder.getAdminId());//管理员
            returnOrder_new.setDisposeTime(new Date());//处理日期
            returnOrderMapper.insert(returnOrder_new);//保存

            this.return_example(returnOrder);
        }
    }

    //修改对应订单明细的退货状态为未申请
    private void return_example(ReturnOrder returnOrder) {
        Example example = new Example(ReturnOrderItem.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("returnOrderId", returnOrder.getId()); //退货订单ID
        //查询条件
        List<ReturnOrderItem> returnOrderItems = returnorderItemMapper.selectByExample(example);
        for (ReturnOrderItem returnOrderItem : returnOrderItems) {
            OrderItem orderitem = new OrderItem();
            //这里采用退货的id和 退货的明细表中的order_item_id相关联
            Long orderItemId = returnOrderItem.getOrderItemId();
            String orderItemId_new = String.valueOf(orderItemId);
            orderitem.setId(orderItemId_new); //订单明细ID
            orderitem.setIsReturn("1"); //退货失败
            orderItemMapper.updateByPrimaryKeySelective(orderitem);
        }
        throw new RuntimeException("抱歉！提交失败，请检查信息是否完整或者联系管理员。");
    }

    /**
     * 订单超时自动关闭
     */
    public void orderTimeOutLogic() {
        //查询超时时间
        OrderConfig orderConfig = orderConfigMapper.selectByPrimaryKey(1);
        Integer orderTimeout = orderConfig.getOrderTimeout(); //60min
        LocalDateTime localDateTime = LocalDateTime.now().minusMinutes(orderTimeout);

        //设置查询条件
        Example example = new Example(Order.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andLessThan("createTime", localDateTime); //创建时间小于超时 时间
        criteria.andEqualTo("orderStatus", "0"); //未付款的
        criteria.andEqualTo("isDelete", "0"); //未删除的

        //查询超时的订单
        List<Order> orders = orderMapper.selectByExample(example);
        for (Order order : orders) {
            //记录订单变动日志
            OrderLog orderLog = new OrderLog();
            orderLog.setId(idWorker.nextId() + "");
            orderLog.setOperater("system");// 系统
            orderLog.setOperateTime(new Date());//当前日期
            orderLog.setOrderStatus("4");
            orderLog.setPayStatus(order.getPayStatus());
            orderLog.setConsignStatus(order.getConsignStatus());
            orderLog.setRemarks("超时订单，系统自动关闭");
            orderLog.setOrderId(order.getId());
            orderLogMapper.insert(orderLog);

            //更改订单状态
            order.setCloseTime(new Date());
            order.setOrderStatus("4");
            orderMapper.updateByPrimaryKeySelective(order);
        }
    }


    /**
     * 构建查询条件
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap) {
        Example example = new Example(ReturnOrder.class);
        Example.Criteria criteria = example.createCriteria();
        if (searchMap != null) {
            // 用户账号
            if (searchMap.get("userAccount") != null && !"".equals(searchMap.get("userAccount"))) {
                criteria.andLike("userAccount", "%" + searchMap.get("userAccount") + "%");
            }
            // 联系人
            if (searchMap.get("linkman") != null && !"".equals(searchMap.get("linkman"))) {
                criteria.andLike("linkman", "%" + searchMap.get("linkman") + "%");
            }
            // 联系人手机
            if (searchMap.get("linkmanMobile") != null && !"".equals(searchMap.get("linkmanMobile"))) {
                criteria.andLike("linkmanMobile", "%" + searchMap.get("linkmanMobile") + "%");
            }
            // 类型
            if (searchMap.get("type") != null && !"".equals(searchMap.get("type"))) {
                criteria.andLike("type", "%" + searchMap.get("type") + "%");
            }
            // 是否退运费
            if (searchMap.get("isReturnFreight") != null && !"".equals(searchMap.get("isReturnFreight"))) {
                criteria.andLike("isReturnFreight", "%" + searchMap.get("isReturnFreight") + "%");
            }
            // 申请状态
            if (searchMap.get("status") != null && !"".equals(searchMap.get("status"))) {
                criteria.andLike("status", "%" + searchMap.get("status") + "%");
            }
            // 凭证图片
            if (searchMap.get("evidence") != null && !"".equals(searchMap.get("evidence"))) {
                criteria.andLike("evidence", "%" + searchMap.get("evidence") + "%");
            }
            // 问题描述
            if (searchMap.get("description") != null && !"".equals(searchMap.get("description"))) {
                criteria.andLike("description", "%" + searchMap.get("description") + "%");
            }
            // 处理备注
            if (searchMap.get("remark") != null && !"".equals(searchMap.get("remark"))) {
                criteria.andLike("remark", "%" + searchMap.get("remark") + "%");
            }

            // 退款金额
            if (searchMap.get("returnMoney") != null) {
                criteria.andEqualTo("returnMoney", searchMap.get("returnMoney"));
            }
            // 退货退款原因
            if (searchMap.get("returnCause") != null) {
                criteria.andEqualTo("returnCause", searchMap.get("returnCause"));
            }
            // 管理员id
            if (searchMap.get("adminId") != null) {
                criteria.andEqualTo("adminId", searchMap.get("adminId"));
            }

        }
        return example;
    }

}
