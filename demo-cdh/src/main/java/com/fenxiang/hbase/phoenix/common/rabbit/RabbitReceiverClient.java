package com.fenxiang.hbase.phoenix.common.rabbit;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.fenxiang.hbase.phoenix.dao.phoenix.PhoenixGoodsPushMapper;
import com.fenxiang.hbase.phoenix.domain.GoodsPushRecord;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RabbitReceiverClient {

	@Autowired
	private PhoenixGoodsPushMapper phoenixMapper;

	@RabbitListener(bindings = @QueueBinding(
			value = @Queue(value = "${spring.rabbitmq.listener.goods-push.queue.name}",
					durable="${spring.rabbitmq.listener.goods-push.queue.durable:true}"),
			exchange = @Exchange(value = "${spring.rabbitmq.listener.goods-push.exchange.name}",
					durable="${spring.rabbitmq.listener.goods-push.exchange.durable:true}",
					type= "${spring.rabbitmq.listener.goods-push.exchange.type}",
					ignoreDeclarationExceptions = "${spring.rabbitmq.listener.goods-push.exchange.ignoreDeclarationExceptions:true}"),
			key = "${spring.rabbitmq.listener.goods-push.key}"
			)
	)
	@RabbitHandler
	public void onMessage(Message message, Channel channel) throws Exception {
		try{
			Object payload = message.getPayload();
			String convert;
			if(payload instanceof String){
				convert = (String) payload;
			}else{
				convert = new String((byte[]) payload);
			}
			log.info("消费端Payload: {}" , convert);
			final GoodsPushRecord record = JSONObject.parseObject(convert, new TypeReference<GoodsPushRecord>() {{}}.getType());
			phoenixMapper.insertOne(record);
		}catch (Exception e){
			log.error("exp:", e);
		}finally {
			//Long deliveryTag = (Long)message.getHeaders().get(AmqpHeaders.DELIVERY_TAG);
			//手工ACK,获取deliveryTag
			//channel.basicAck(deliveryTag, false);
		}
	}
}

