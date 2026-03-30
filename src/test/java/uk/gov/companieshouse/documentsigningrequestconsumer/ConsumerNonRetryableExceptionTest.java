package uk.gov.companieshouse.documentsigningrequestconsumer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doThrow;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@EmbeddedKafka(
        topics = {"echo", "echo-retry", "echo-error", "echo-invalid"},
        controlledShutdown = true,
        partitions = 1
)
@Import(TestConfig.class)
@TestPropertySource(locations = "classpath:application-test_main_nonretryable.yml")
@ActiveProfiles("test_main_nonretryable")
class ConsumerNonRetryableExceptionTest {

    @Autowired
    private KafkaProducer<String, SignDigitalDocument> producer;

    @Autowired
    private KafkaConsumer<String, SignDigitalDocument> consumer;

    @MockitoBean(name = "service")
    private DocumentService service;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testRepublishToInvalidMessageTopicIfNonRetryableExceptionThrown() {
        // Given:
        doThrow(NonRetryableException.class).when(service).processMessage(new ServiceParameters(DOCUMENT));

        ProducerRecord<String, SignDigitalDocument> record = new ProducerRecord<>(
                "echo", 0, System.currentTimeMillis(), SAME_PARTITION_KEY, DOCUMENT);

        // When:
        Future<RecordMetadata> response = producer.send(record);
        producer.flush();

        assertThat(response.isDone(), is(true));

        // Then:
        ConsumerRecords<?, ?> consumerRecords = KafkaTestUtils.getRecords(consumer, Duration.ofMillis(10000L), 2);
        assertThat(consumerRecords.count(), is(2));

        assertThat(TestUtils.noOfRecordsForTopic(consumerRecords, "echo"), is(1));
        assertThat(TestUtils.noOfRecordsForTopic(consumerRecords, "echo-retry"), is(0));
        assertThat(TestUtils.noOfRecordsForTopic(consumerRecords, "echo-error"), is(0));
        assertThat(TestUtils.noOfRecordsForTopic(consumerRecords, "echo-invalid"), is(1));

        verify(service).processMessage(new ServiceParameters(DOCUMENT));
    }


}
