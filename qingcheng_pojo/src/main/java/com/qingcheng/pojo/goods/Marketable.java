package com.qingcheng.pojo.goods;


import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@Table(name = "tb_marketable")
public class Marketable implements Serializable {

    @Id
    private String id;

    private String history_marketable; //上一次的上架状态

    private String update_marketable; //本次的上架状态

    private Date history_time;

    private Date update_time;
}
