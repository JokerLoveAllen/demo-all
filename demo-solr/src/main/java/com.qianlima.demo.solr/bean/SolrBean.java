package com.qianlima.demo.solr.bean;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description
 * @Author Lun_qs
 * @Date 2019/12/18 11:19
 * @Version 0.0.1
 */
@Data
public class SolrBean{
    @Data
    public static class Response implements Serializable {
        public final static Response EMPTY = new Response();
        /**
         *         "id": "26435651",
         *         "progid": 0,
         *         "catid": 43,
         *         "areaid": 201,
         *         "title": "2015年国家农业综合开发龙山镇高标准农田建设项目(土地整理标段)招标公告",
         *         "orgtitle": "（网上电子招标）南靖县2015年国家农业综合开发龙山镇高标准农田建设项目(土地整理标段)",
         *         "url": "http://www.qianlima.com/zb/detail/20150828_26435651.html",
         *         "updatetime": "2015-08-28 13:08:02",
         *         "rawid": 56495629,
         *         "typeid": 52,
         *         "_version_": 1560609365351727000
         */
        private long id;
        private int progid,catid,areaid,rawid,typeid;
        private String title,orgtitle,url,updatetime,_version_;
    }

    @Data
    public static class Request implements Serializable {
        private long id;
        private int progid, catid, areaid, rawid, typeid;
        private String title, orgtitle, url, updatetime, _version_, yyyymmdd, yyyymmddHH;
    }
}
