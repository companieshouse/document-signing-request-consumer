package uk.gov.companieshouse.documentsigningrequestconsumer.telemetry;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
class OpenTelemetryAppenderInitializerTest {

    @Mock
    OpenTelemetry openTelemetry;

    @Mock
    Logger logger;

    @InjectMocks
    OpenTelemetryAppenderInitializer underTest;

    @Test
    void givenInitialised_whenAfterPropertiesSet_thenInstalledOk() {
        try (MockedStatic<OpenTelemetryAppender> mockedStatic = Mockito.mockStatic(OpenTelemetryAppender.class)) {

            // Act
            underTest.afterPropertiesSet();

            // Verify that our appender was installed with the correct OpenTelemetry instance
            mockedStatic.verify(
                    () -> OpenTelemetryAppender.install(openTelemetry),
                    Mockito.times(1)
            );

            verify(logger, times(1)).info("Initializing OpenTelemetryAppender");
        }
    }
}
