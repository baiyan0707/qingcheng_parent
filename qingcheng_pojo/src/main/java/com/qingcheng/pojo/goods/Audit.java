package com.qingcheng.pojo.goods;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Data
@ToString
@NoArgsConstructor
@Table(name = "tb_audit")
public class Audit implements Serializable {

    @Id
    private String id;

    private String history_status; //上一次的审核状态

    private String update_status; //当前审核状态

    private String history_message; //上一次的日志信息

    private String update_message; //当前日志信息

    private Date history_time; //上一次的修改时间

    private Date update_time; //当前修改时间

    private String update_marketable; //上一次的上架状态

    private String history_marketable; //更新后的上架状态
}
