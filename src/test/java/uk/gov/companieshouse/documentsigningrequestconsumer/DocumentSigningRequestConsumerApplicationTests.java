package uk.gov.companieshouse.documentsigningrequestconsumer;

import org.junit.Rule;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.Arrays;

import static java.util.Arrays.stream;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static uk.gov.companieshouse.documentsigningrequestconsumer.EnvironmentVariablesChecker.RequiredEnvironmentVariables.BACKOFF_DELAY;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test_main_positive.yml")
@Import(TestConfig.class)
@EmbeddedKafka
@ActiveProfiles("test_main_positive")
class DocumentSigningRequestConsumerApplicationTests {

    private static final String TOKEN_STRING_VALUE = "token value";

    @Rule
    private static final EnvironmentVariables ENVIRONMENT_VARIABLES;

    static {
        ENVIRONMENT_VARIABLES = new EnvironmentVariables();
        stream(EnvironmentVariablesChecker.RequiredEnvironmentVariables.values()).forEach(variable -> {
                switch (variable) {
                    case SERVER_PORT:
                        ENVIRONMENT_VARIABLES.set(variable.getName(), "8080");
                        break;
                    default:
                        ENVIRONMENT_VARIABLES.set(variable.getName(), TOKEN_STRING_VALUE);
                }
        });
    }

    @AfterAll
    static void tearDown() {
        final String[] AllEnvironmentVariableNames =
                Arrays.stream(EnvironmentVariablesChecker.RequiredEnvironmentVariables.class.getEnumConstants())
                        .map(Enum::name)
                        .toArray(String[]::new);
        ENVIRONMENT_VARIABLES.clear(AllEnvironmentVariableNames);
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

        ENVIRONMENT_VARIABLES.clear(BACKOFF_DELAY.getName());

        try (final var app = mockStatic(SpringApplication.class)) {

            DocumentSigningRequestConsumerApplication.main(new String[]{});

            app.verify(() -> SpringApplication.run(DocumentSigningRequestConsumerApplication.class, new String[0]), times(0));
        }

    }

}
