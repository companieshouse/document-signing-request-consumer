package uk.gov.companieshouse.documentsigningrequestconsumer.telemetry;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.logging.Logger;

@Component
class OpenTelemetryAppenderInitializer implements InitializingBean {

    private final OpenTelemetry openTelemetry;
    private final Logger logger;

    OpenTelemetryAppenderInitializer(final OpenTelemetry openTelemetry, final Logger logger) {
        this.openTelemetry = openTelemetry;
        this.logger = logger;
    }

    @Override
    public void afterPropertiesSet() {
        logger.info("Initializing OpenTelemetryAppender");

        OpenTelemetryAppender.install(this.openTelemetry);
    }

}