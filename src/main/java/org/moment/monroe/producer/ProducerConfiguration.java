package org.moment.monroe.producer;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.moment.monroe.Location;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class ProducerConfiguration {
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    @Value("${spring.kafka.producer.batch-size}")
    private int batchSize;

    @Bean
    public Map<String, Object> producerConfigurationProperties() {
        Map<String, Object> producerProperties = new HashMap<>();
        producerProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.bootstrapServers);
        producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        producerProperties.put(ProducerConfig.BATCH_SIZE_CONFIG, this.batchSize);
        return producerProperties;
    }

    @Bean
    public ProducerFactory<String, Location> producerFactory() {
        return new DefaultKafkaProducerFactory<>(this.producerConfigurationProperties());
    }

    @Bean
    public KafkaTemplate<String, Location> kafkaTemplate() {
        return new KafkaTemplate<>(this.producerFactory());
    }

}