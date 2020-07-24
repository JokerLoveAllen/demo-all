package com.qianlima.demo.rocketmq.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description
 * @Author Lun_qs
 * @Date 2019/10/8 14:55
 * @Version 0.0.1
 */
@Slf4j
@RestController
public class Sender {
    private @Autowired RocketMQTemplate rocketMQTemplate;

    @GetMapping("/{string}")
    public void echo(@PathVariable String string){
        Message<String> message = MessageBuilder
                .withPayload(string)
                .setHeader(MessageConst.PROPERTY_KEYS, string)
                .build();
        SendResult sendResult = rocketMQTemplate.syncSend("echosomething:echo", message);
      log.warn(" msgid:{} - sendResult:{}",sendResult.getMsgId(), sendResult.toString());
    }
}
