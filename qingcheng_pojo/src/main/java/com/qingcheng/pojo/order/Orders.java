package com.qingcheng.pojo.order;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Orders implements Serializable {

    private Order order;
    private List<OrderItem> orderItemList;
}
