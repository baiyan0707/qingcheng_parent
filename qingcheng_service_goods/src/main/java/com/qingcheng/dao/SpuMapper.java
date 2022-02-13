package com.qingcheng.dao;

import com.qingcheng.pojo.goods.Spu;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SpuMapper extends Mapper<Spu> {

    @Select("sele")
    List<Spu> findByOrderId();
}
