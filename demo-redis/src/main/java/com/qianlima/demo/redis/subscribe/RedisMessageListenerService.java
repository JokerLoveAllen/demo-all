package com.qianlima.demo.redis.subscribe;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

/**
 * @Description
 * @Author Lun_qs
 * @Date 2019/12/23 18:16
 * @Version 0.0.1
 */
/**
 * 一般来说是处理单一 pub|sub 的 channel 具体业务代码
 * 与消息队列中Topic等价
 * 多个客户端监听同一个channel,当发布消息时 所有订阅的客户端都会受到通知!!!!
 */

@Slf4j
@Service
public class RedisMessageListenerService implements MessageListener {

    @Override
    public void onMessage(Message message, byte[] bytes) {
        log.warn("onMessage: {} -> {} -> {}",new String(message.getChannel()), new String(message.getBody()), new String(bytes));
    }
}
