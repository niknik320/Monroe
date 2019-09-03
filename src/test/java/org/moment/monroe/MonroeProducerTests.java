package org.moment.monroe;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.moment.monroe.producer.Producer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.test.rule.EmbeddedKafkaRule;
import org.springframework.kafka.test.utils.ContainerTestUtils;
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
public class MonroeProducerTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(MonroeProducerTests.class);
    @Value("${kafka.producer.group}")
    private String group;
    @Value("${kafka.producer.auto-commit}")
    private String autoCommit;
    private static String EMBEDDED_CONSUMER_TOPIC = "producer.t";
    @Autowired
    private Producer producer;
    private KafkaMessageListenerContainer<String, Location> container;
    private BlockingQueue<ConsumerRecord<String, Location>> records;

    @ClassRule
    public static EmbeddedKafkaRule embeddedKafkaRule = new EmbeddedKafkaRule(1, true, EMBEDDED_CONSUMER_TOPIC);

    @Before
    public void setUp() {
        Map<String, Object> consumerProperties = KafkaTestUtils.consumerProps(this.group, this.autoCommit, embeddedKafkaRule.getEmbeddedKafka());
        DefaultKafkaConsumerFactory<String, Location> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProperties);
        ContainerProperties containerProperties = new ContainerProperties(EMBEDDED_CONSUMER_TOPIC);
        container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
        records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String, Location>) record -> {
            LOGGER.debug("test-listener received message='{}'", record);
            records.add(record);
        });
        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafkaRule.getEmbeddedKafka().getPartitionsPerTopic());
    }

    @After
    public void tearDown() {
        container.stop();
    }

    @Test
    public void testExample() throws InterruptedException {
        Location location = new Location("id", "bulkID");
        producer.send(location);
        ConsumerRecord<String, Location> received = records.poll(10, TimeUnit.SECONDS);
        assert received != null;
        assertEquals(location.toString(), received.value());
    }
}