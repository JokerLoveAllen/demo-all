package com.qianlima.demo.dubbo.producer;

import com.alibaba.fastjson.JSONObject;
import com.qianlima.demo.dubbo.api.ServiceApi;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;

/**
 * @Description
 * @Author Lun_qs
 * @Date 2019/12/26 10:02
 * @Version 0.0.1
 */
@Slf4j
@Service(interfaceClass = ServiceApi.class,protocol = {"dubbo", "rest"})
public class ServiceProducer implements ServiceApi {

    @Override
    public String getUser(Long id) {
        log.info("we got: {}",id);
        return new JSONObject(){{
            this.put("code",200);
            this.put("data","this echo function !!");
        }}.toJSONString();
    }

    @Override
    public String registerUser(String name, String pwd){
        log.info("info: {} -> {}", name, pwd);
        return new JSONObject(){{
            this.put("code",200);
            this.put("data","this echo function !!");
        }}.toJSONString();
    }
}
