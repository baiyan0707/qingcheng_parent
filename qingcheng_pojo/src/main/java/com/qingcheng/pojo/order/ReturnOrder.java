package com.qingcheng.pojo.order;
import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
/**
 * returnOrder实体类
 * @author Administrator
 *
 */
@Data
@Table(name="tb_return_order")
public class ReturnOrder implements Serializable{

	@Id
	private String id;//服务单号

	private String orderId;//订单号

	private java.util.Date applyTime;//申请时间

	private Long userId;//用户ID

	private String userAccount;//用户账号

	private String linkman;//联系人

	private String linkmanMobile;//联系人手机

	private String type;//类型

	private Integer returnMoney;//退款金额

	private String isReturnFreight;//是否退运费

	private String status;//申请状态

	private java.util.Date disposeTime;//处理时间

	private Integer returnCause;//退货退款原因

	private String evidence;//凭证图片

	private String description;//问题描述

	private String remark;//处理备注

	private Integer adminId;//管理员id


	
}
