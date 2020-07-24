package com.qianlima.demo.webflux.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Description
 * @Author Lun_qs
 * @Date 2019/9/6 14:40
 * @Version 0.0.1
 */
@Slf4j
@Controller
@RequestMapping("/cors")
public class CorsController {

    @GetMapping("/head")
    public String cors(){
        return "cors";
    }
}
