package uk.gov.companieshouse.documentsigningrequestconsumer.telemetry;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Custom {@link SpanProcessor} that renames Kafka consumer spans to use the service name
 * instead of the topic name. This ensures the AWS X-Ray trace map displays the correct
 * service name rather than the Kafka topic name as the node label.
 *
 * <p>The OpenTelemetry Kafka instrumentation generates span names in the format
 * {@code {topic} {operation}} (e.g. {@code cidev-sign-digital-document process}), which
 * the ADOT collector uses as the X-Ray segment {@code name} field — the label shown in
 * the trace map. This processor overrides that with the application service name.</p>
 */
@Component
public class KafkaSpanNameProcessor implements SpanProcessor {

    private static final AttributeKey<String> MESSAGING_SYSTEM_KEY =
            AttributeKey.stringKey("messaging.system");

    private final String serviceName;

    public KafkaSpanNameProcessor(
            @Value("${spring.application.name:document-signing-request-consumer}") String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public void onStart(final Context parentContext, final ReadWriteSpan span) {
        if ("kafka".equals(span.getAttribute(MESSAGING_SYSTEM_KEY))) {
            span.updateName(serviceName);
        }
    }

    @Override
    public boolean isStartRequired() {
        return true;
    }

    @Override
    public void onEnd(final ReadableSpan span) {
        // no-op
    }

    @Override
    public boolean isEndRequired() {
        return false;
    }
}

