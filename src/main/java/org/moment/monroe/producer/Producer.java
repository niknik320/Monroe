package org.moment.monroe.producer;

import org.moment.monroe.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
public class Producer {
    private static final Logger LOGGER = LoggerFactory.getLogger(Producer.class);
    @Value("${spring.kafka.producer.topic}")
    private String topic;

    private final KafkaTemplate<String, Location> kafkaTemplate;

    public Producer(KafkaTemplate<String, Location> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(Location location) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("message: {}, topic: {}", location, topic);
        }
        Message<Location> message = MessageBuilder
                .withPayload(location)
                .setHeader(KafkaHeaders.TOPIC, topic)
                .build();
        kafkaTemplate.send(message);
    }
}
