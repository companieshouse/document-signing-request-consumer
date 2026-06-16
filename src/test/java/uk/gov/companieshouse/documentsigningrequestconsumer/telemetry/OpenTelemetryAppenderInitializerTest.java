package uk.gov.companieshouse.documentsigningrequestconsumer.telemetry;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.opentelemetry.api.OpenTelemetry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
        underTest.afterPropertiesSet();

        verify(logger, times(1)).info("Initializing OpenTelemetryAppender");
    }
}
