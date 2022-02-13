package com.qingcheng.pojo.goods;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Entity;
import java.io.Serializable;
import java.util.List;

@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Data
public class Goods implements Serializable {

    private Spu spu;
    private List<Sku> skuList;
}
