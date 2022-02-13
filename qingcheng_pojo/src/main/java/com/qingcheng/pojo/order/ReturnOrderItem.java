package com.qingcheng.pojo.order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
/**
 * returnOrderItem实体类
 * @author Administrator
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name="tb_return_order_item")
public class ReturnOrderItem implements Serializable{

	@Id
	private Long id;//ID

	private Long categoryId;//分类ID

	private Long spuId;//SPU_ID

	private Long skuId;//SKU_ID

	private Long orderId;//订单ID

	private Long orderItemId;//订单明细ID

	private Long returnOrderId;//退货订单ID

	private String title;//标题

	private Integer price;//单价

	private Integer num;//数量

	private Integer money;//总金额

	private Integer payMoney;//支付金额

	private String image;//图片地址

	private Integer weight;//重量

}
