package com.fenxiang.rabbit.consume;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

/**
 * @See https://www.cnblogs.com/coder-programming/p/11602910.html
 */
@Component
public class RabbitReceiver {

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
		System.err.println("--------------------------------------");
		final Object payload = message.getPayload();
//		if("abcd".equals(payload)){
//			throw new RuntimeException("RTE!!!");
//		}
		System.err.println("消费端Payload: " + message.getPayload());
		Long deliveryTag = (Long)message.getHeaders().get(AmqpHeaders.DELIVERY_TAG);

		//手工ACK,获取deliveryTag
		channel.basicAck(deliveryTag, false);
	}
}

