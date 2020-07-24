package com.qianlima.demo.es.controller;

import com.qianlima.demo.es.bean.EsPojo;
import com.qianlima.demo.es.bean.vo.QueryVo;
import com.qianlima.demo.es.service.EsService;
import com.qianlima.demo.es.util.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.UUID;

/**
 * @Description controller
 * @Author Lun_qs
 * @Date 2019/9/17 14:37
 * @Version 0.0.1
 */
@Slf4j
@Controller
@RequestMapping("/elastic")
public class ElasticCoontroller {

    @Resource
    private EsService service;

    private final String ES_INDEX = "qianlima";

    @GetMapping()
    public String view1(Model model){
        return "elastic/list";
    }

    @GetMapping("/modify/{_id}")
    public String view2(@PathVariable("_id") String id, Model model){
        model.addAttribute("data", EsPojo.NOTHING);
        Result result = service.get(ES_INDEX, id);
        log.info("current id:{}",id);
        if(result.getData()!=null){
            model.addAttribute("data", result.getData());
        }
        return "elastic/modify";
    }

    @GetMapping("/add")
    public String view3(Model model){
        model.addAttribute("data", EsPojo.NOTHING);
        return "elastic/modify";
    }

    @ResponseBody
    @PostMapping("/list")
    public Result list(@RequestBody QueryVo vo){
        return service.list(ES_INDEX,vo);
    }

    @ResponseBody
    @PostMapping("/delete/{_id}")
    public Result delete(@PathVariable("_id") String id){
        return service.delete(ES_INDEX,id);
    }

    @ResponseBody
    @PostMapping("/adate")
    public Result adate(EsPojo esPojo){
        if(StringUtils.isBlank(esPojo.getId())){
            esPojo.setId(UUID.randomUUID().toString());
        }
        return service.upsert(ES_INDEX, esPojo);
    }
}
