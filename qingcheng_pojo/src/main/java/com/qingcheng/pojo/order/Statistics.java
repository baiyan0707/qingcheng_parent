package com.qingcheng.pojo.order;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Data
@NoArgsConstructor
@Table(name = "tb_statistics")
public class Statistics implements Serializable {

    @Id
    private String id;

    private Long number;

    private Long place;

    private Long order;

    private Long quantity_number;

    private Long valid;

    private String place_money;

    private String refund_money;

    private Long refund_people;

    private Long refund_order;

    private Long refund_number;

    private String payment_number;

    private String guest_money;
}
