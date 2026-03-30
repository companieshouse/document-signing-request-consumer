package uk.gov.companieshouse.documentsigningrequestconsumer;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static uk.gov.companieshouse.documentsigningrequestconsumer.Constants.DOCUMENT;
import static uk.gov.companieshouse.documentsigningrequestconsumer.Constants.SAME_PARTITION_KEY;

import java.time.Duration;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.companieshouse.documentsigning.SignDigitalDocument;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = { "echo", "echo-retry", "echo-error", "echo-invalid" })
@Import(TestConfig.class)
@ActiveProfiles("test_main_nonretryable")
class KafkaIntegrationTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    private KafkaProducer<String, SignDigitalDocument> producer;
    private KafkaConsumer<String, SignDigitalDocument> consumer;

    @MockitoBean
    private DocumentService service;

    @BeforeEach
    void setUp() {
        producer = TestConfig.createKafkaProducer(embeddedKafka);
        consumer = TestConfig.createKafkaConsumer(embeddedKafka);
    }

    @AfterEach
    void tearDown() {
        producer.close();
        consumer.close();
    }

    @Test
    void shouldEchoMessage() {
        doThrow(new NonRetryableException("Non-retryable error")).when(service).processMessage(any(ServiceParameters.class));

        ProducerRecord<String, SignDigitalDocument> message = new ProducerRecord<>(
                "echo", 0, System.currentTimeMillis(), SAME_PARTITION_KEY, DOCUMENT);

        // --- send ---
        producer.send(message);
        producer.flush();

        // --- poll ---
        ConsumerRecords<String, SignDigitalDocument> records =
                KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(10));

        boolean found = false;

        for (ConsumerRecord<String, SignDigitalDocument> record : records) {
            if (record.value().equals(DOCUMENT)) {
                found = true;
                break;
            }
        }

        assertTrue(found, "Message not received from echo topic");
    }
}
