package com.qianlima.demo.rocketmq.consume;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

/**
 * @Description
 * @Author Lun_qs
 * @Date 2019/10/8 14:29
 * @Version 0.0.1
 */
@Slf4j
//@Service
//@RocketMQMessageListener(topic="echosomething", consumerGroup="echo")
public class EchoRocketMqService implements RocketMQListener<MessageExt> {

    @Override
    public void onMessage(MessageExt messageExt) {
        log.warn("current tag:{}, current info:{}",messageExt.getTags(), new String(messageExt.getBody()));
    }
}
