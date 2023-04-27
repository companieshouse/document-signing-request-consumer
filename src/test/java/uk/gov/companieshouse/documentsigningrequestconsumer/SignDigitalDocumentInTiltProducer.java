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

import static uk.gov.companieshouse.documentsigningrequestconsumer.Constants.DOCUMENT;

/**
 * "Test" class re-purposed to produce {@link SignDigitalDocument} messages to the <code>sign-digital-document</code>
 * topic in Tilt. This is NOT to be run as part of an automated test suite. It is for manual testing only.
 */
@SpringBootTest(classes = DocumentSigningRequestConsumerApplication.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestPropertySource(properties = {
        "server.port=8080",
        "steps=1"})
@Import(TestConfig.class)
@SuppressWarnings("squid:S3577") // This is NOT to be run as part of an automated test suite.
class SignDigitalDocumentInTiltProducer {

    private static final String KAFKA_IN_TILT_BOOTSTRAP_SERVER_UTL = "localhost:29092";

    @Rule
    private static final EnvironmentVariables ENVIRONMENT_VARIABLES;

    static {
        ENVIRONMENT_VARIABLES = new EnvironmentVariables();
        ENVIRONMENT_VARIABLES.set("MAX_ATTEMPTS", "3");
        ENVIRONMENT_VARIABLES.set("BACKOFF_DELAY", "100");
        ENVIRONMENT_VARIABLES.set("BOOTSTRAP_SERVER_URL", KAFKA_IN_TILT_BOOTSTRAP_SERVER_UTL);
        ENVIRONMENT_VARIABLES.set("CONCURRENT_LISTENER_INSTANCES", "1");
        ENVIRONMENT_VARIABLES.set("CONCURRENT_ERROR_LISTENER_INSTANCES", "1");
        ENVIRONMENT_VARIABLES.set("TOPIC", "sign-digital-document");
        ENVIRONMENT_VARIABLES.set("RETRY_TOPIC", "document-signing-request-retry");
        ENVIRONMENT_VARIABLES.set("ERROR_TOPIC", "document-signing-request-error");
        ENVIRONMENT_VARIABLES.set("INVALID_MESSAGE_TOPIC", "sign-digital-document-invalid");
        ENVIRONMENT_VARIABLES.set("ERROR_GROUP_ID", "document-signing-request-error-consumer");
        ENVIRONMENT_VARIABLES.set("GROUP_ID","document-signing-request-consumer");
        ENVIRONMENT_VARIABLES.set("IS_ERROR_CONSUMER", "false");
    }

    @Autowired
    private KafkaProducer<String, SignDigitalDocument> testProducer;

    @SuppressWarnings("squid:S2699") // at least one assertion
    @Test
    void produceMessageToTilt() {
        testProducer.send(new ProducerRecord<>("sign-digital-document", 0, System.currentTimeMillis(), "key", DOCUMENT));
    }
}
