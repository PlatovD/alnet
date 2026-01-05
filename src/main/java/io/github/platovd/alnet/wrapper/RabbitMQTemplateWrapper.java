package io.github.platovd.alnet.wrapper;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Обертка над стандартным template для отправки сообщений
 */
@Component
@RequiredArgsConstructor
public class RabbitMQTemplateWrapper {
    private final RabbitTemplate rabbitTemplate;
    private static final String TOPIC_EXCHANGE = "amq.topic";

    public void sendMessage(String topic, Object data) {
        rabbitTemplate.convertAndSend(topic, data);
    }

    public void sendMessageToChatSubscribers(String topic, Long chatId, Object data) {
        String requiredChatTopic = topic + "." + chatId.toString();
        rabbitTemplate.convertAndSend(TOPIC_EXCHANGE, requiredChatTopic, data);
    }
}
