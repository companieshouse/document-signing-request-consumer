package uk.gov.companieshouse.documentsigningrequestconsumer;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import static java.util.Arrays.stream;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static uk.gov.companieshouse.documentsigningrequestconsumer.EnvironmentVariablesChecker.RequiredEnvironmentVariables.BACKOFF_DELAY;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test_main_positive.yml")
@Import(TestConfig.class)
@EmbeddedKafka
@ActiveProfiles("test_main_positive")
@ExtendWith(SystemStubsExtension.class)
class DocumentSigningRequestConsumerApplicationTests {

    private static final String TOKEN_STRING_VALUE = "token value";

    @SystemStub
    private static EnvironmentVariables ENVIRONMENT_VARIABLES;


    @BeforeEach
    void beforeEach() {
        setAllEvironmentVariables();
    }

    @AfterAll
    static void tearDown() throws Exception {
        ENVIRONMENT_VARIABLES.teardown();
    }

    @SuppressWarnings("squid:S2699") // at least one assertion
    @DisplayName("context loads")
    @Test
    void contextLoads() {
    }

    @DisplayName("runs app when all required environment variables are present")
    @Test
    void runsAppWhenAllRequiredEnvironmentVariablesPresent() {

        try (final var app = mockStatic(SpringApplication.class)) {
            app.when(() -> SpringApplication.run(DocumentSigningRequestConsumerApplication.class, new String[0])).thenReturn(null);

            DocumentSigningRequestConsumerApplication.main(new String[]{});

            app.verify(() -> SpringApplication.run(DocumentSigningRequestConsumerApplication.class, new String[0]));
        }

    }

    @DisplayName("does not run app when a required environment variable is missing")
    @Test
    void doesNotRunAppWhenRequiredEnvironmentVariableMissing() {

        ENVIRONMENT_VARIABLES.remove(BACKOFF_DELAY.getName());

        try (final var app = mockStatic(SpringApplication.class)) {

            app.when(() -> SpringApplication.run(DocumentSigningRequestConsumerApplication.class, new String[0])).thenReturn(null);

            DocumentSigningRequestConsumerApplication.main(new String[]{});

            app.verify(() -> SpringApplication.run(DocumentSigningRequestConsumerApplication.class, new String[0]), times(0));
        }

    }

    private void setAllEvironmentVariables() {
        for (EnvironmentVariablesChecker.RequiredEnvironmentVariables envVariable : EnvironmentVariablesChecker.RequiredEnvironmentVariables.values()) {
            ENVIRONMENT_VARIABLES.set(envVariable.getName(), TOKEN_STRING_VALUE);
        }
    }

}
