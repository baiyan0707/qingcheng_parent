package com.qingcheng.pojo.order;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@Table(name="tb_category_report")
public class CategoryReport implements Serializable {

    @Id
    private Integer categoryId1; //1级分类

    @Id
    private Integer categoryId2; //2级分类

    @Id
    private Integer categoryId3; //3级分类

    @Id
    private Date countDate; //统计日期

    private Integer num; //销售量

    private Integer money; //销售额
}
