package org.moment.monroe;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.test.rule.EmbeddedKafkaRule;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
public class MonroeApplicationTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(MonroeApplicationTests.class);
    @Value("${spring.kafka.producer.group-id}")
    private String groupID;
    @Value("${spring.kafka.producer.auto-commit}")
    private String autoCommit;
    @Value("${spring.kafka.consumer.topic}")
    private String consumerTopic;
    @Value("${spring.kafka.producer.topic}")
    private String producerTopic;
    private KafkaMessageListenerContainer<String, Location> container;
    private KafkaTemplate<String, String> embeddedProducer;
    private BlockingQueue<ConsumerRecord<String, Location>> embeddedConsumer;
    @ClassRule
    public static EmbeddedKafkaRule embeddedKafkaRule = new EmbeddedKafkaRule(1, true);

    @Before
    public void setUp() {
        Map<String, Object> senderProperties = KafkaTestUtils.senderProps(embeddedKafkaRule.getEmbeddedKafka().getBrokersAsString());
        ProducerFactory<String, String> producerFactory = new DefaultKafkaProducerFactory<>(senderProperties);
        embeddedProducer = new KafkaTemplate<>(producerFactory);
        embeddedProducer.setDefaultTopic(this.consumerTopic);
        Map<String, Object> consumerProperties = KafkaTestUtils.consumerProps(this.groupID, this.autoCommit, embeddedKafkaRule.getEmbeddedKafka());
        DefaultKafkaConsumerFactory<String, Location> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProperties);
        ContainerProperties containerProperties = new ContainerProperties(this.producerTopic);
        container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
        embeddedConsumer = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String, Location>) record -> {
            LOGGER.info("test-listener received message='{}'", record);
            embeddedConsumer.add(record);
        });
        container.start();
    }

    @After
    public void tearDown() {
        container.stop();
    }

    @Test
    public void testExample() throws InterruptedException {
        //publish test data with the embedded producer
        //application consumer will than consume the data and produce it to the intended topic
        //consume the result with the embedded consumer
        //assert that the data that was consumed matches the expected result
        String testData = "id,bulkID";
        Location result = new Location("id", "bulkID");
        this.embeddedProducer.sendDefault(testData);
        ConsumerRecord<String, Location> received = embeddedConsumer.poll(10, TimeUnit.SECONDS);

        assert (received != null);
        assertEquals(result.toString(), received.value());
    }
}
