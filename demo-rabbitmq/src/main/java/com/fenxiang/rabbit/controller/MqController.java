package com.fenxiang.rabbit.controller;

import com.fenxiang.rabbit.service.SenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName MqController
 * @Author zy
 * @Date 2020/4/28 11:31
 */
@RequestMapping("/api/")
@RestController
public class MqController {
    @Autowired
    SenderService senderService;

    @GetMapping("/send/{msg}")
    public String send(@PathVariable String msg){
        senderService.sender(msg);
        return "send ok !";
    }
}
