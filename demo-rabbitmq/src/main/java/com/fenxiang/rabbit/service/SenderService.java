package com.fenxiang.rabbit.service;

import com.fenxiang.rabbit.consume.RabbitSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * @ClassName SenderService
 * @Author zy
 * @Date 2020/4/28 11:25
 */
@Service
public class SenderService {
    @Autowired
    private RabbitSender rabbitSender;

    public void sender(String msg){
        try {
            rabbitSender.send(msg, new HashMap<>());
        }catch (Exception e){
            System.err.println(e);
        }
    }
}
