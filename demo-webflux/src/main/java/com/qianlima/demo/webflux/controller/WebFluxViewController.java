package com.qianlima.demo.webflux.controller;

import com.qianlima.demo.webflux.service.MyDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Description
 * @Author Lun_qs
 * @Date 2019/7/16 10:21
 * @Version 0.0.1
 */
@Slf4j
@Controller
@RequestMapping("/view")
public class WebFluxViewController {

    @Autowired
    private MyDomainService service;

    @GetMapping("/list")
    public String list(Model model){
        model.addAttribute("dataList", service.getList());
        return "list";
    }


}
