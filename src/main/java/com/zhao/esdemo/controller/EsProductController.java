package com.zhao.esdemo.controller;


import com.zhao.esdemo.base.ProductEs;
import lombok.extern.slf4j.Slf4j;
import org.frameworkset.elasticsearch.ElasticSearchHelper;
import org.frameworkset.elasticsearch.client.ClientInterface;
import org.frameworkset.elasticsearch.entity.ESDatas;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 商品查询
 *
 * @author zhaoliwei
 * @date 2019/5/20 18:45
 **/
@Slf4j
@RestController
@RequestMapping("/product/")
public class EsProductController {


    private static final String INDEX = "product_demo";

    private static final String TYPE = "product_demo";

    /**
     * 初始化
     *
     * @return 商品列表
     */
    @GetMapping("initProduct")
    public Object initProduct() throws InterruptedException {
        //删除index(删除表)
        try {
            ElasticSearchHelper.getRestClientUtil().dropIndice(INDEX);
        } catch (Exception e) {
            log.error("删除index异常" + e.getMessage());
        }

        //创建mapping(创建表结构)
        Map<String, Object> params = new HashMap<>(16);
        params.put("type", TYPE);
        ClientInterface clientInterface = ElasticSearchHelper.getConfigRestClientUtil("esmapper/ProductEsMapper.xml");
        String mappingResult = clientInterface.createIndiceMapping(INDEX, "createMapping", params);
        log.info(mappingResult);
        TimeUnit.SECONDS.sleep(1);

        //设置index的属性 如最大返回条数
        ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();
        clientUtil.updateIndiceSetting(INDEX, "max_result_window", 2000000);

        //查询index属性
        String indexSetting = clientUtil.getIndiceSetting(INDEX);
        log.info(indexSetting);

        //批量新增商品文档
        String result = this.addProductList();
        return result;

    }

    private String addProductList() {
        List<ProductEs> addList = new ArrayList<>();

        ProductEs productEs1 = new ProductEs();
        productEs1.setSkuId(1L);
        productEs1.setCode("TY0001");
        productEs1.setName("德力西/DELIXI 小型断路器 DZ47SN1C6 DZ47s 6A AC230/400 6 1P C型");
        productEs1.setDeleted(0);
        addList.add(productEs1);

        ProductEs productEs2 = new ProductEs();
        productEs2.setSkuId(2L);
        productEs2.setCode("TY0001");
        productEs2.setName("闪电 皮带蜡 Q/IEPZ 03-91 330g 蜡状");
        productEs2.setDeleted(0);
        addList.add(productEs2);


        ProductEs productEs3 = new ProductEs();
        productEs3.setSkuId(3L);
        productEs3.setCode("TY0001");
        productEs3.setName("前卫机电 稳流电源 WLY-3A AC220V±10% 0-3A 280*120*240mm");
        productEs3.setDeleted(0);
        addList.add(productEs3);

        ProductEs productEs4 = new ProductEs();
        productEs4.setSkuId(4L);
        productEs4.setCode("TY0001");
        productEs4.setName("3M 滤棉塑胶盖 501 塑胶 透明");
        productEs4.setDeleted(0);
        addList.add(productEs4);

        for (ProductEs product : addList) {
            product.setNameAndCode(product.getCode() + product.getName());
            product.setNameAndCodeIk(product.getNameAndCode());
            product.setNameAndCodeLike(product.getNameAndCode());
            if (product.getNameAndCode() != null) {
                char[] chars = product.getNameAndCode().toCharArray();
                StringBuilder stringBuilder = new StringBuilder();
                for (char c : chars) {
                    stringBuilder.append(c).append(" ");
                }
                product.setNameAndCodeLike(stringBuilder.toString());
            }
        }

        ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();
        String response = clientUtil.addDocuments(INDEX, TYPE, addList);
        log.info(response);
        return response;


    }


    /**
     * 前端商品名称模糊查询列表
     *
     * @return 商品列表
     */
    @GetMapping("queryProductList")
    public Object queryProductList(ProductEs search) {

        List<ProductEs> productList = this.getProductList(search);
        return productList;

    }


    private List<ProductEs> getProductList(ProductEs search) {

        Map<String, Object> params = new HashMap<>(16);


        //切词
        if (search.getNameAndCode() != null && search.getNameAndCodeLike().length() > 0) {
            String keywordTemp = search.getNameAndCode().toLowerCase();
            params.put("nameAndCodeLike", wordSegmentation(keywordTemp));
        }
        //取前100条
        params.put("from", 0);
        params.put("size", 99);
        //返回字段过滤
        params.put("fields", " \"_source\":{\"excludes\":[\"nameAnalysis\",\"nameAndCodeAnalysis\"]}");

        ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/ProductEsMapper.xml");
        ESDatas<ProductEs> esDatas = clientUtil.searchList(
                INDEX + "/_search", "searchPageDatasAnd", params, ProductEs.class);
        //获取结果对象列表
        List<ProductEs> resultlist = esDatas.getDatas();

        return null;
    }

    private String wordSegmentation(String keywordTemp) {
        if (keywordTemp == null || keywordTemp.length() == 0) {
            return null;
        }
        char[] chars = keywordTemp.toCharArray();
        StringBuilder stringBuilder = new StringBuilder();
        for (char c : chars) {
            stringBuilder.append(c).append(" ");
        }
        return stringBuilder.toString();
    }


}



