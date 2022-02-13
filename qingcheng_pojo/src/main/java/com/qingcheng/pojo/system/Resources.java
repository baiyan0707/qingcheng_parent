package com.qingcheng.pojo.system;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Resources implements Serializable {

    private Integer roleId;

    private Integer[] resourceIds;
}
