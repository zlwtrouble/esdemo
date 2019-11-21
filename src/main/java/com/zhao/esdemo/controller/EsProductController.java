package com.zhao.esdemo.controller;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zhao.esdemo.base.Pager;
import com.zhao.esdemo.base.ProductEs;
import com.zhao.esdemo.base.dto.ProductSearchDto;
import lombok.extern.slf4j.Slf4j;
import org.frameworkset.elasticsearch.ElasticSearchHelper;
import org.frameworkset.elasticsearch.client.ClientInterface;
import org.frameworkset.elasticsearch.entity.ESDatas;
import org.springframework.http.*;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

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
     * 创建INDEX
     *
     * @return java.lang.String
     */
    @GetMapping("initProduct")
    public String initProduct() throws InterruptedException {
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

        String indexSetting = clientUtil.getIndiceSetting(INDEX);
        log.info("初始化index完成，index属性{}",indexSetting);
        return "end";

    }

    /**
     * 写入数据
     *
     * @return String
     */
    @GetMapping("addProductList")
    public String addProductList() {
        int count = 0;
        RestTemplate restTemplate = new RestTemplate();
        for (int i = 1; i <= 17; i++) {
            String url = "http://192.168.13.149:8011/skuPendingDocumentApi/getSimpleProductByIdsPage";
            ProductSearchDto productSearchDto = new ProductSearchDto();
            Pager pager = new Pager();
            pager.setPageIndex(i);
            pager.setPageSize(10000);
            productSearchDto.setPager(pager);
            productSearchDto.setDeleted(0);
            HttpHeaders requestHeaders = new HttpHeaders();
            requestHeaders.setContentType(MediaType.parseMediaType("application/json; charset=UTF-8"));
            HttpEntity<String> requestEntity = new HttpEntity<>(JSON.toJSONString(productSearchDto), requestHeaders);
            ResponseEntity<JSONObject> exchange = restTemplate.exchange(url, HttpMethod.POST, requestEntity, JSONObject.class);

            JSONObject body = exchange.getBody();
            if (body != null) {
                Object list = body.get("list");
                List<ProductEs> productEsList = JSON.parseArray(JSON.toJSONString(list), ProductEs.class);
                if (!CollectionUtils.isEmpty(productEsList)) {
                    for (ProductEs product : productEsList) {
                        count++;
                        product.setNameAndCode(product.getCode() + product.getName());
                        product.setNameAndCodeIk(product.getNameAndCode());
                        product.setNameAndCodeLike(product.getNameAndCode());
                        if (product.getNameAndCode() != null) {
                            char[] chars = product.getNameAndCode().toCharArray();
                            StringBuilder stringBuilder = new StringBuilder();
                            //加入空格让默认分词器对每个字符分词
                            for (char c : chars) {
                                stringBuilder.append(c).append(" ");
                            }
                            product.setNameAndCodeLike(stringBuilder.toString());
                        }
                    }
                    ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();
                    String response = clientUtil.addDocuments(INDEX, TYPE, productEsList);
                    log.info(response);
                }
            }
        }
        log.info("本次写入：{}条", count);
        return "end";

    }


    /**
     * 关键字模糊搜索
     *
     * @return 商品列表
     */
    @GetMapping("queryProductList")
    public Object queryProductList(ProductEs search) {
       Map<String,Object> result = this.getProductList(search);
        return result;
    }


    private  Map<String,Object> getProductList(ProductEs search) {
        Map<String,Object> result=new HashMap<>(16);
        Map<String, Object> params = new HashMap<>(16);
        if (search.getNameAndCode() != null && search.getNameAndCode().length() > 0) {
            params.put("nameAndCode", "*" + search.getNameAndCode().toLowerCase() + "*");
        }

        if (search.getNameAndCodeIk() != null && search.getNameAndCodeIk().length() > 0) {
            //中文分词词，只能对符合分词规律的进行分词，未被分词的将无法搜索到，不同于模糊搜索
            //比如
            params.put("nameAndCodeIk", search.getNameAndCodeIk().toLowerCase());
        }

        //切词
        if (search.getNameAndCodeLike() != null && search.getNameAndCodeLike().length() > 0) {
            //利用相关度评分法实现模糊查询效果（在匹配索引时词出现的顺序一致，出现的评率高）
            String keywordTemp = search.getNameAndCodeLike().toLowerCase();
            params.put("nameAndCodeLike", wordSegmentation(keywordTemp));
        }
        //取前100条
        params.put("from", 0);
        params.put("size", 99);
        //返回字段过滤
        params.put("fields", " \"_source\":{\"excludes\":[\"nameAndCode\",\"nameAndCodeIk\",\"nameAndCodeLike\"]}");

        ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("esmapper/ProductEsMapper.xml");
        ESDatas<ProductEs> esDatas = clientUtil.searchList(
                INDEX + "/_search", "searchPageDatasAnd", params, ProductEs.class);
        //获取结果对象列表
        List<ProductEs> resultlist = esDatas.getDatas();
        result.put("total", esDatas.getTotalSize());
        result.put("detail",resultlist);

        log.info("返回：" + esDatas.getRestResponse().toString());
        return result;
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



