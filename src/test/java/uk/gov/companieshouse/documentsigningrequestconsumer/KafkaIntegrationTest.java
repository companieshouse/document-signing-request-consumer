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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.companieshouse.documentsigning.SignDigitalDocument;
import uk.gov.companieshouse.documentsigningrequestconsumer.exception.NonRetryableException;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = { "echo", "echo-retry", "echo-error", "echo-invalid" })
@Import(TestConfig.class)
@ActiveProfiles("test_main_nonretryable")
class KafkaIntegrationTest {

    @Autowired
    private KafkaProducer<String, SignDigitalDocument> producer;

    @Autowired
    private KafkaConsumer<String, SignDigitalDocument> consumer;

    @MockitoBean
    private DocumentService service;

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

        for (ConsumerRecord<String, SignDigitalDocument> consumerRecord : records) {
            if (consumerRecord.value().equals(DOCUMENT)) {
                found = true;
                break;
            }
        }

        assertTrue(found, "Message not received from echo topic");
    }
}
