package uk.gov.companieshouse.documentsigningrequestconsumer;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import uk.gov.companieshouse.documentsigning.SignDigitalDocument;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.companieshouse.documentsigningrequestconsumer.Constants.DOCUMENT;
import static uk.gov.companieshouse.documentsigningrequestconsumer.Constants.SAME_PARTITION_KEY;

@SpringBootTest(classes = DocumentSigningRequestConsumerApplication.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@EmbeddedKafka(
        topics = {"echo", "echo-retry", "echo-error", "echo-invalid"},
        controlledShutdown = true,
        partitions = 1
)
@TestPropertySource(locations = "classpath:application-test_main_retryable.yml")
@Import(TestConfig.class)
@ActiveProfiles("test_main_retryable")
class ConsumerRetryableExceptionTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private KafkaConsumer<String, SignDigitalDocument> testConsumer;

    @Autowired
    private KafkaProducer<String, SignDigitalDocument> testProducer;

    @Autowired
    private CountDownLatch latch;

    @MockBean
    private Service service;

    @Test
    void testRepublishToErrorTopicThroughRetryTopics() throws InterruptedException {
        //given
        embeddedKafkaBroker.consumeFromAllEmbeddedTopics(testConsumer);
        doThrow(RetryableException.class).when(service).processMessage(any());

        //when
        testProducer.send(new ProducerRecord<>(
                "echo", 0, System.currentTimeMillis(), SAME_PARTITION_KEY, DOCUMENT));
        if (!latch.await(30L, TimeUnit.SECONDS)) {
            fail("Timed out waiting for latch");
        }

        //then
        ConsumerRecords<?, ?> consumerRecords = KafkaTestUtils.getRecords(testConsumer, 10000L, 6);
        assertThat(TestUtils.noOfRecordsForTopic(consumerRecords, "echo"), is(1));
        assertThat(TestUtils.noOfRecordsForTopic(consumerRecords, "echo-retry"), is(3));
        assertThat(TestUtils.noOfRecordsForTopic(consumerRecords, "echo-error"), is(1));
        assertThat(TestUtils.noOfRecordsForTopic(consumerRecords, "echo-invalid"), is(0));
        verify(service, times(4)).processMessage(new ServiceParameters(DOCUMENT));
    }
}
