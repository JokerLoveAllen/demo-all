package com.qianlima.demo.es.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Description
 * @Author Lun_qs
 * @Date 2019/9/11 14:51
 * @Version 0.0.1
 *
 * in kibana console:

# base bulk 这是一个批量操作, 对文档进行批量增删改
# es 的客户端也是需要依照bulk规范进行HTTP请求;
# index 和 create 区别 目标已存在create报错,index正常
POST _bulk
{ "index" : { "_index" : "qianlima", "_id" : "2" } }
{ "doc" : {"price" : "99.8" }}
{ "delete" : { "_index" : "qianlima", "_id" : "4" } }
{ "create" : { "_index" : "qianlima", "_id" : "4" } }
{"id" : 4,"name" : "上咕噜不正下咕噜正","content" : "2019马上过去了,没事,2020年依旧糟糕","price" : 99.8,"sellReason" : "因为没钱了,搞点钱花花","sellTime" : "2019-12-22"}
{ "update" : {"_id" : "1", "_index" : "qianlima"} }
{ "doc" : {"price" : "1662.3"} }

# 创建
PUT qianlima/

# 删除
DELETE qianlima/

# 查看类型
GET qianlima/_mapping

# 排序查
GET qianlima/_search
{
"query":{
"match_all" : {}
},
"sort": [
{
"id": {
"order": "desc"
}
}
],
"from": 0,
"size": 30
}

#对index属性进行修改
# format "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis"
# 时间的格式在ES中可以存放多种形式
PUT qianlimaer/_mapping
{
"properties":{
"id":{
"type":"integer"
},
"name":{
"type":"text",
"analyzer":"ik_max_word",
"search_analyzer":"ik_max_word"
},
"content":{
"type":"text",
"analyzer":"ik_max_word",
"search_analyzer":"ik_max_word"
},
"price":{
"type":"double"
},
"sellReason":{
"type":"text",
"analyzer":"ik_max_word",
"search_analyzer":"ik_max_word"
},
"sellTime":{
"type":"date",
"format":"yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis"
}
}
}

# 新增
# format_str 必须: 2006-01-02 etc... 包含前缀零
PUT qianlima/_doc/3
{
"id" : 3,
"name" : "i will come back soon",
"content" : "because he is very nice and headsome guy",
"price" : 99.8,
"sellReason": "just sell it !",
"sellTime": "2019-10-03"
}

# ik分词
POST qianlima/_search
{
"query" : { "match": { "content" : "鹅蛋" }},
"highlight" : {
"pre_tags" : ["<tag>"],
"post_tags" : ["</tag>"],
"fields" : {
"content" : {}
}
}
}
 */
@Setter
@Getter
public class EsPojo {

    private String id, name, content, sellReason, sellTime;
    private BigDecimal price;
    public final static EsPojo NOTHING = new EsPojo();

    @Override
    public String toString() {
        if(this.id == null){
           return "[]";
        }
        return "EsPojo(id=" + this.getId() + ", name=" + this.getName() + ", content=" + this.getContent() + ", sellReason=" + this.getSellReason() + ", price=" + this.getPrice() +
                ", sellTime=" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(this.getSellTime()) + ")";
    }
}
