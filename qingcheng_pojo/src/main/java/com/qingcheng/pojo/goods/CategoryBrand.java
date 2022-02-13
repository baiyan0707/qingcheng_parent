package com.qingcheng.pojo.goods;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Data
@NoArgsConstructor
@ToString
@Table(name = "tb_category_brand")
public class CategoryBrand implements Serializable {

    @Id
    private Integer categoryId;
    @Id
    private Integer brandId;
}
