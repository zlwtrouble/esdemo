# 查询全部
GET product_demo/_search
{
  "query": {
    "match_all": {}
  }
}


# 统计全部
GET product_demo/_count
{
  "query": {
    "match_all": {}
  }
}


GET /product_demo/_search
{
  "query": {
    "match": {
      "nameAndCodeLike": "FH防化"
    }
  }
}


#http://127.0.0.1:8080/product/addProductList

