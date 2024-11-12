package uk.gov.companieshouse.documentsigningrequestconsumer;

import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import static java.util.Arrays.stream;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static uk.gov.companieshouse.documentsigningrequestconsumer.EnvironmentVariablesChecker.RequiredEnvironmentVariables.BACKOFF_DELAY;
import static uk.gov.companieshouse.documentsigningrequestconsumer.EnvironmentVariablesChecker.RequiredEnvironmentVariables.BOOTSTRAP_SERVER_URL;
import static uk.gov.companieshouse.documentsigningrequestconsumer.EnvironmentVariablesChecker.RequiredEnvironmentVariables.CHS_API_KEY;
import static uk.gov.companieshouse.documentsigningrequestconsumer.EnvironmentVariablesChecker.RequiredEnvironmentVariables.CONCURRENT_LISTENER_INSTANCES;
import static uk.gov.companieshouse.documentsigningrequestconsumer.EnvironmentVariablesChecker.RequiredEnvironmentVariables.DOCUMENT_SIGNING_REQUEST_CONSUMER_PORT;
import static uk.gov.companieshouse.documentsigningrequestconsumer.EnvironmentVariablesChecker.RequiredEnvironmentVariables.GROUP_ID;
import static uk.gov.companieshouse.documentsigningrequestconsumer.EnvironmentVariablesChecker.RequiredEnvironmentVariables.INTERNAL_API_URL;
import static uk.gov.companieshouse.documentsigningrequestconsumer.EnvironmentVariablesChecker.RequiredEnvironmentVariables.INVALID_MESSAGE_TOPIC;
import static uk.gov.companieshouse.documentsigningrequestconsumer.EnvironmentVariablesChecker.RequiredEnvironmentVariables.MAX_ATTEMPTS;
import static uk.gov.companieshouse.documentsigningrequestconsumer.EnvironmentVariablesChecker.RequiredEnvironmentVariables.PAYMENTS_API_URL;
import static uk.gov.companieshouse.documentsigningrequestconsumer.EnvironmentVariablesChecker.RequiredEnvironmentVariables.PREFIX;
import static uk.gov.companieshouse.documentsigningrequestconsumer.EnvironmentVariablesChecker.RequiredEnvironmentVariables.TOPIC;
import static uk.gov.companieshouse.documentsigningrequestconsumer.EnvironmentVariablesChecker.RequiredEnvironmentVariables.API_URL;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test_main_positive.yml")
@Import(TestConfig.class)
@EmbeddedKafka
@ActiveProfiles("test_main_positive")
@ExtendWith(SystemStubsExtension.class)
class EnvironmentVariablesCheckerTest {

    private static final String TOKEN_VALUE = "token value";


    @SystemStub
    private static EnvironmentVariables environmentVariables;

    private static EnvironmentVariablesChecker environmentVariablesChecker = new EnvironmentVariablesChecker();

    @BeforeEach
    void beforeEach() {
        setAllEvironmentVariables();
    }



    @DisplayName("returns true if all required environment variables are present")
    @Test
    void checkEnvironmentVariablesAllPresentReturnsTrue() {
        assertTrue(environmentVariablesChecker.allRequiredEnvironmentVariablesPresent());
    }

    @DisplayName("returns false if BACKOFF_DELAY is missing")
    @Test
    void checkEnvironmentVariablesAllPresentReturnsFalseIfBackoffDelayMissing() {
        populateAllVariablesExceptOneAndAssertSomethingMissing(BACKOFF_DELAY.getName());
    }

    @DisplayName("returns false if BOOTSTRAP_SERVER_URL is missing")
    @Test
    void checkEnvironmentVariablesAllPresentReturnsFalseIfBootstrapServerUrlMissing() {
        populateAllVariablesExceptOneAndAssertSomethingMissing(BOOTSTRAP_SERVER_URL.getName());
    }

    @DisplayName("returns false if CONCURRENT_LISTENER_INSTANCES is missing")
    @Test
    void checkEnvironmentVariablesAllPresentReturnsFalseIfConcurrentListenerInstancesMissing() {
        populateAllVariablesExceptOneAndAssertSomethingMissing(CONCURRENT_LISTENER_INSTANCES.getName());
    }

    @DisplayName("returns false if GROUP_ID is missing")
    @Test
    void checkEnvironmentVariablesAllPresentReturnsFalseIfGroupIdMissing() {
        populateAllVariablesExceptOneAndAssertSomethingMissing(GROUP_ID.getName());
    }

    @DisplayName("returns false if INVALID_MESSAGE_TOPIC is missing")
    @Test
    void checkEnvironmentVariablesAllPresentReturnsFalseIfInvalidMessageTopicMissing() {
        populateAllVariablesExceptOneAndAssertSomethingMissing(INVALID_MESSAGE_TOPIC.getName());
    }

    @DisplayName("returns false if MAX_ATTEMPTS is missing")
    @Test
    void checkEnvironmentVariablesAllPresentReturnsFalseIfMaxAttemptsMissing() {
        populateAllVariablesExceptOneAndAssertSomethingMissing(MAX_ATTEMPTS.getName());
    }

    @DisplayName("returns false if DOCUMENT_SIGNING_REQUEST_CONSUMER_PORT is missing")
    @Test
    void checkEnvironmentVariablesAllPresentReturnsFalseIfAppPortMissing() {
        populateAllVariablesExceptOneAndAssertSomethingMissing(DOCUMENT_SIGNING_REQUEST_CONSUMER_PORT.getName());
    }

    @DisplayName("returns false if TOPIC is missing")
    @Test
    void checkEnvironmentVariablesAllPresentReturnsFalseIfTopicMissing() {
        populateAllVariablesExceptOneAndAssertSomethingMissing(TOPIC.getName());
    }

    @DisplayName("returns false if API_URL is missing")
    @Test
    void checkEnvironmentVariablesAllPresentReturnsFalseIfApiUrlMissing() {
        populateAllVariablesExceptOneAndAssertSomethingMissing(API_URL.getName());
    }

    @DisplayName("returns false if PAYMENTS_API_URL is missing")
    @Test
    void checkEnvironmentVariablesAllPresentReturnsFalseIfPaymentsApiUrlMissing() {
        populateAllVariablesExceptOneAndAssertSomethingMissing(PAYMENTS_API_URL.getName());
    }

    @DisplayName("returns false if CHS_API_KEY is missing")
    @Test
    void checkEnvironmentVariablesAllPresentReturnsFalseIfChsApiKeyMissing() {
        populateAllVariablesExceptOneAndAssertSomethingMissing(CHS_API_KEY.getName());
    }

    @DisplayName("returns false if INTERNAL_API_URL is missing")
    @Test
    void checkEnvironmentVariablesAllPresentReturnsFalseIfInternalApiUrlMissing() {
        populateAllVariablesExceptOneAndAssertSomethingMissing(INTERNAL_API_URL.getName());
    }

    @DisplayName("returns false if PREFIX is missing")
    @Test
    void checkEnvironmentVariablesAllPresentReturnsFalseIfPrefixMissing() {
        populateAllVariablesExceptOneAndAssertSomethingMissing(PREFIX.getName());
    }
    private void populateAllVariablesExceptOneAndAssertSomethingMissing(String token){
        // Loop through each environment variables
        // Remove the current environment variable
        // Assert that not all required environment variables are present
        // Repopulate the environment variable for the next iteration
        stream(EnvironmentVariablesChecker.RequiredEnvironmentVariables.values()).forEach(envVariable -> {
            environmentVariables.remove(token);
            assertFalse(environmentVariablesChecker.allRequiredEnvironmentVariablesPresent());
            environmentVariables.set(envVariable.getName(), TOKEN_VALUE);
        });
    }

    private void setAllEvironmentVariables() {
        for (EnvironmentVariablesChecker.RequiredEnvironmentVariables envVariable : EnvironmentVariablesChecker.RequiredEnvironmentVariables.values()) {
            environmentVariables.set(envVariable.getName(), TOKEN_VALUE);
        }
    }
}