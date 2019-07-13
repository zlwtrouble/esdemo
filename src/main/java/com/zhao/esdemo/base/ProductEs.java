package com.zhao.esdemo.base;


import com.frameworkset.orm.annotation.ESId;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zhaoliwei
 * @date 2019/5/21 18:35
 **/
@Data
public class ProductEs {

    /**
     * 商品编号
     **/
    private String code;

    /**
     * 商品主标题
     **/
    private String name;

    /**
     * sku id
     **/
    @ESId(readSet = true)
    private Long skuId;

    /**
     * 商品价格
     **/
    private BigDecimal price;


    /**
     * 删除
     */
    private Integer deleted;


    /**
     * 不分词
     **/
    private String nameAndCode;


    /**
     * 默认主流IK中文分词器
     **/
    private String nameAndCodeIk;


    /**
     * 按需求实现like效果
     **/
    private String nameAndCodeLike;

}
