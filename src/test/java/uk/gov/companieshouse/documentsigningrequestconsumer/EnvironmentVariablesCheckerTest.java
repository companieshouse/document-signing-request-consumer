package uk.gov.companieshouse.documentsigningrequestconsumer;

import org.junit.Rule;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.Arrays;

import static java.util.Arrays.stream;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
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
class EnvironmentVariablesCheckerTest {

    private static final String TOKEN_VALUE = "token value";

    @Rule
    public EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @AfterEach
    void tearDown() {
        final String[] AllEnvironmentVariableNames =
                Arrays.stream(EnvironmentVariablesChecker.RequiredEnvironmentVariables.class.getEnumConstants())
                        .map(Enum::name)
                        .toArray(String[]::new);
        environmentVariables.clear(AllEnvironmentVariableNames);
    }

    @DisplayName("returns true if all required environment variables are present")
    @Test
    void checkEnvironmentVariablesAllPresentReturnsTrue() {
        stream(EnvironmentVariablesChecker.RequiredEnvironmentVariables.values()).forEach(this::accept);
        boolean allPresent = EnvironmentVariablesChecker.allRequiredEnvironmentVariablesPresent();
        assertThat(allPresent, is(true));
    }

    @DisplayName("returns false if BACKOFF_DELAY is missing")
    @Test
    void checkEnvironmentVariablesAllPresentReturnsFalseIfBackoffDelayMissing() {
        populateAllVariablesExceptOneAndAssertSomethingMissing(BACKOFF_DELAY);
    }

    @DisplayName("returns false if BOOTSTRAP_SERVER_URL is missing")
    @Test
    void checkEnvironmentVariablesAllPresentReturnsFalseIfBootstrapServerUrlMissing() {
        populateAllVariablesExceptOneAndAssertSomethingMissing(BOOTSTRAP_SERVER_URL);
    }

    @DisplayName("returns false if CONCURRENT_LISTENER_INSTANCES is missing")
    @Test
    void checkEnvironmentVariablesAllPresentReturnsFalseIfConcurrentListenerInstancesMissing() {
        populateAllVariablesExceptOneAndAssertSomethingMissing(CONCURRENT_LISTENER_INSTANCES);
    }

    @DisplayName("returns false if GROUP_ID is missing")
    @Test
    void checkEnvironmentVariablesAllPresentReturnsFalseIfGroupIdMissing() {
        populateAllVariablesExceptOneAndAssertSomethingMissing(GROUP_ID);
    }

    @DisplayName("returns false if INVALID_MESSAGE_TOPIC is missing")
    @Test
    void checkEnvironmentVariablesAllPresentReturnsFalseIfInvalidMessageTopicMissing() {
        populateAllVariablesExceptOneAndAssertSomethingMissing(INVALID_MESSAGE_TOPIC);
    }

    @DisplayName("returns false if MAX_ATTEMPTS is missing")
    @Test
    void checkEnvironmentVariablesAllPresentReturnsFalseIfMaxAttemptsMissing() {
        populateAllVariablesExceptOneAndAssertSomethingMissing(MAX_ATTEMPTS);
    }

    @DisplayName("returns false if DOCUMENT_SIGNING_REQUEST_CONSUMER_PORT is missing")
    @Test
    void checkEnvironmentVariablesAllPresentReturnsFalseIfAppPortMissing() {
        populateAllVariablesExceptOneAndAssertSomethingMissing(DOCUMENT_SIGNING_REQUEST_CONSUMER_PORT);
    }

    @DisplayName("returns false if TOPIC is missing")
    @Test
    void checkEnvironmentVariablesAllPresentReturnsFalseIfTopicMissing() {
        populateAllVariablesExceptOneAndAssertSomethingMissing(TOPIC);
    }

    @DisplayName("returns false if API_URL is missing")
    @Test
    void checkEnvironmentVariablesAllPresentReturnsFalseIfApiUrlMissing() {
        populateAllVariablesExceptOneAndAssertSomethingMissing(API_URL);
    }

    @DisplayName("returns false if PAYMENTS_API_URL is missing")
    @Test
    void checkEnvironmentVariablesAllPresentReturnsFalseIfPaymentsApiUrlMissing() {
        populateAllVariablesExceptOneAndAssertSomethingMissing(PAYMENTS_API_URL);
    }

    @DisplayName("returns false if CHS_API_KEY is missing")
    @Test
    void checkEnvironmentVariablesAllPresentReturnsFalseIfChsApiKeyMissing() {
        populateAllVariablesExceptOneAndAssertSomethingMissing(CHS_API_KEY);
    }

    @DisplayName("returns false if INTERNAL_API_URL is missing")
    @Test
    void checkEnvironmentVariablesAllPresentReturnsFalseIfInternalApiUrlMissing() {
        populateAllVariablesExceptOneAndAssertSomethingMissing(INTERNAL_API_URL);
    }

    @DisplayName("returns false if PREFIX is missing")
    @Test
    void checkEnvironmentVariablesAllPresentReturnsFalseIfPrefixMissing() {
        populateAllVariablesExceptOneAndAssertSomethingMissing(PREFIX);
    }
    private void populateAllVariablesExceptOneAndAssertSomethingMissing(
            final EnvironmentVariablesChecker.RequiredEnvironmentVariables excludedVariable) {
        stream(EnvironmentVariablesChecker.RequiredEnvironmentVariables.values()).forEach(variable -> {
            if (variable != excludedVariable) {
                environmentVariables.set(variable.getName(), TOKEN_VALUE);
            }
        });
        boolean allPresent = EnvironmentVariablesChecker.allRequiredEnvironmentVariablesPresent();
        assertFalse(allPresent);
    }

    private void accept(EnvironmentVariablesChecker.RequiredEnvironmentVariables variable) {
        environmentVariables.set(variable.getName(), TOKEN_VALUE);
    }
}