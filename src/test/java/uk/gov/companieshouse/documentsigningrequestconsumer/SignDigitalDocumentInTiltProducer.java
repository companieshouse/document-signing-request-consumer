package uk.gov.companieshouse.documentsigningrequestconsumer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import uk.gov.companieshouse.documentsigning.SignDigitalDocument;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static uk.gov.companieshouse.documentsigningrequestconsumer.Constants.DOCUMENT;
import static uk.gov.companieshouse.documentsigningrequestconsumer.Constants.SAME_PARTITION_KEY;

/**
 * "Test" class re-purposed to produce {@link SignDigitalDocument} messages to the <code>sign-digital-document</code>
 * topic in Tilt. This is NOT to be run as part of an automated test suite. It is for manual testing only.
 */
@SpringBootTest(classes = DocumentSigningRequestConsumerApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestPropertySource(locations="classpath:sign-digital-document-in-tilt.properties")
@Import(TestConfig.class)
@SuppressWarnings("squid:S3577") // This is NOT to be run as part of an automated test suite.
class SignDigitalDocumentInTiltProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger("SignDigitalDocumentInTiltProducer");

    private static final int MESSAGE_WAIT_TIMEOUT_SECONDS = 10;

    @Value("${consumer.topic}")
    private String signDigitalDocumentTopic;

    @Autowired
    private KafkaProducer<String, SignDigitalDocument> testProducer;

    @SuppressWarnings("squid:S2699") // at least one assertion
    @Test
    void produceMessageToTilt() throws InterruptedException, ExecutionException, TimeoutException {
        final var future = testProducer.send(new ProducerRecord<>(
                signDigitalDocumentTopic, 0, System.currentTimeMillis(), SAME_PARTITION_KEY, DOCUMENT));
        final var result = future.get(MESSAGE_WAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        final var partition = result.partition();
        final var offset = result.offset();
        LOGGER.info("Message " + DOCUMENT + " delivered to topic " + signDigitalDocumentTopic
                + " on partition " + partition + " with offset " + offset + ".");
    }
}
