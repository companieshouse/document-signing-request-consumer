package uk.gov.companieshouse.documentsigningrequestconsumer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.Rule;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
@TestPropertySource(properties = "steps=1")
@Import(TestConfig.class)
@SuppressWarnings("squid:S3577") // This is NOT to be run as part of an automated test suite.
class SignDigitalDocumentInTiltProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger("SignDigitalDocumentInTiltProducer");
    private static final String SIGN_DIGITAL_DOCUMENT_TOPIC = "sign-digital-document";
    private static final String KAFKA_IN_TILT_BOOTSTRAP_SERVER_URL = "localhost:29092";
    private static final int MESSAGE_WAIT_TIMEOUT_SECONDS = 10;

    @Rule
    private static final EnvironmentVariables ENVIRONMENT_VARIABLES;

    static {
        ENVIRONMENT_VARIABLES = new EnvironmentVariables();
        ENVIRONMENT_VARIABLES.set("MAX_ATTEMPTS", "4");
        ENVIRONMENT_VARIABLES.set("BACKOFF_DELAY", "100");
        ENVIRONMENT_VARIABLES.set("BOOTSTRAP_SERVER_URL", KAFKA_IN_TILT_BOOTSTRAP_SERVER_URL);
        ENVIRONMENT_VARIABLES.set("CONCURRENT_LISTENER_INSTANCES", "1");
        ENVIRONMENT_VARIABLES.set("TOPIC", SIGN_DIGITAL_DOCUMENT_TOPIC);
        ENVIRONMENT_VARIABLES.set("INVALID_MESSAGE_TOPIC", "sign-digital-document-invalid");
        ENVIRONMENT_VARIABLES.set("GROUP_ID","document-signing-request-consumer");
    }

    @Autowired
    private KafkaProducer<String, SignDigitalDocument> testProducer;

    @SuppressWarnings("squid:S2699") // at least one assertion
    @Test
    void produceMessageToTilt() throws InterruptedException, ExecutionException, TimeoutException {
        final var future = testProducer.send(new ProducerRecord<>(
                SIGN_DIGITAL_DOCUMENT_TOPIC, 0, System.currentTimeMillis(), SAME_PARTITION_KEY, DOCUMENT));
        final var result = future.get(MESSAGE_WAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        final var partition = result.partition();
        final var offset = result.offset();
        LOGGER.info("Message " + DOCUMENT + " delivered to topic " + SIGN_DIGITAL_DOCUMENT_TOPIC
                + " on partition " + partition + " with offset " + offset + ".");
    }
}
