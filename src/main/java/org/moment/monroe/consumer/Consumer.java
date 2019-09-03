package org.moment.monroe.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.moment.monroe.Location;
import org.moment.monroe.producer.Producer;
import org.moment.monroe.transform.MonroeTransformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

@Service
public class Consumer {
    private final Logger logger = LoggerFactory.getLogger(Consumer.class);
    private final Producer producer;

    public Consumer(Producer producer) {
        this.producer = producer;
    }

    @KafkaListener(topics = "${spring.kafka.consumer.topic}")
    public void consume(ConsumerRecord<String, String >consumerRecord, Acknowledgment acknowledgment) {
        try {
            Stream<Location> locationStream = MonroeTransformation.transform(consumerRecord.value());
            Stream<Location> validLocations = MonroeTransformation.validate(locationStream);
            validLocations.forEach(this.producer::send);
            acknowledgment.acknowledge();
            logger.info("message: Consumed bulk successfully, topic: {}, partition:{}, offset: {}", consumerRecord.topic(), consumerRecord.offset(), consumerRecord.partition());

        } catch (Exception e) {
            logger.error("message: Error when handling bulk, topic: {}, partition:{}, offset: {}", consumerRecord.topic(), consumerRecord.offset(), consumerRecord.partition());
        }
    }
}
