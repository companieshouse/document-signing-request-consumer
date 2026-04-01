package uk.gov.companieshouse.documentsigningrequestconsumer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.companieshouse.documentsigningrequestconsumer.Constants.DOCUMENT;
import static uk.gov.companieshouse.documentsigningrequestconsumer.Constants.SAME_PARTITION_KEY;

import java.time.Duration;
import java.util.concurrent.Future;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.companieshouse.documentsigning.SignDigitalDocument;
import uk.gov.companieshouse.documentsigningrequestconsumer.exception.RetryableException;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@EmbeddedKafka(
        topics = {"echo", "echo-retry", "echo-error", "echo-invalid"},
        controlledShutdown = true,
        partitions = 1
)
@Import(TestConfig.class)
@TestPropertySource(locations = "classpath:application-test_main_retryable.yml")
@ActiveProfiles("test_main_retryable")
class ConsumerRetryableExceptionTest {

    @Autowired
    private KafkaConsumer<String, SignDigitalDocument> consumer;

    @Autowired
    private KafkaProducer<String, SignDigitalDocument> producer;

    @MockitoBean(name = "service")
    private DocumentService service;

    @Test
    void testRepublishToErrorTopicThroughRetryTopics() {
        // Given:
        doThrow(RetryableException.class).when(service).processMessage(new ServiceParameters(DOCUMENT));

        ProducerRecord<String, SignDigitalDocument> message = new ProducerRecord<>(
                "echo", 0, System.currentTimeMillis(), SAME_PARTITION_KEY, DOCUMENT);

        // When:
        Future<RecordMetadata> response = producer.send(message);
        producer.flush();

        assertThat(response.isDone(), is(true));

        // Then:
        ConsumerRecords<?, ?> consumerRecords = KafkaTestUtils.getRecords(consumer, Duration.ofMillis(10000L), 5);
        assertThat(consumerRecords.count(), is(5));

        assertThat(TestUtils.noOfRecordsForTopic(consumerRecords, "echo"), is(1));
        assertThat(TestUtils.noOfRecordsForTopic(consumerRecords, "echo-retry"), is(3));
        assertThat(TestUtils.noOfRecordsForTopic(consumerRecords, "echo-error"), is(1));
        assertThat(TestUtils.noOfRecordsForTopic(consumerRecords, "echo-invalid"), is(0));

        verify(service, times(4)).processMessage(new ServiceParameters(DOCUMENT));
    }
}