package com.zhao.esdemo.base.dto;

import com.zhao.esdemo.base.Pager;
import lombok.Data;

import java.util.List;

/**
 * @author: weicz
 * @Date: 2018/8/23 13:22
 * @Description: 商品管理查询对象
 */
@Data
public class ProductSearchDto {
    /**
     * 编码
     */
    private String code;

    /** 关键字,名称，编码  **/
    private String keyword;

    /** 关键字集合 **/
    private List<String> keywordList;

    /** 关键字集合  **/
    private String keywordStr;

    /** 分类 **/
    private Long categoryId;

    /** 分类集合 **/
    private List<Long> categoryIdList;

    /** 品牌 **/
    private Long brandId;

    /** 品牌 **/
    private List<Long> brandIdList;

    /** 品牌名称 **/
    private String brandName;

    /** 标签 **/
    private String tag;

    /** 订购限制 **/
    private Boolean limit;

    /** 是否有图片 **/
    private Boolean hasImage;

    /** 是否有价格 **/
    private Boolean hasDesc;

    /** 排序 **/
    private Integer order;

    /** 排序枚举 **/
    private String orderStr;

    /** 是否有价格 **/
    private Boolean price;

    /** 商品状态 **/
    private Integer state;

    /** 商品属性 **/
    private List<Long> attributeValueIds;

    /** SKU状态 **/
    private Integer skuState;

    /** 分页参数 **/
    private Pager pager;

    /** 商品ids集合 **/
    private List<Long> skuIds;

    /**
     * 删除标记
     **/
    private Integer deleted;


    /**
     * 商品skuid
     **/
    private Long skuId;


    /** 商品ids集合 **/
    private List<Long> productIds;

    /** 排除商品ids集合 **/
    private List<Long> excludeProductIds;


    /**
     * 关联id
     **/
    private Long correlationId;

    private Integer startIndex;

    private Integer size;



}
