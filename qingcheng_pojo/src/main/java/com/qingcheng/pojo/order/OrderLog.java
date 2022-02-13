package com.qingcheng.pojo.order;
import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
/**
 * orderLog实体类
 * @author Administrator
 *
 */
@Data
@Table(name="tb_order_log")
public class OrderLog implements Serializable{

	@Id
	private String id;//ID


	

	private String operater;//操作员

	private java.util.Date operateTime;//操作时间

	private String orderId;//订单ID

	private String orderStatus;//订单状态

	private String payStatus;//付款状态

	private String consignStatus;//发货状态

	private String remarks;//备注

}
