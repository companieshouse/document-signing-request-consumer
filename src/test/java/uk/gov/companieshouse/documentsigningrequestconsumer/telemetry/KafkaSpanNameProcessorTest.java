package uk.gov.companieshouse.documentsigningrequestconsumer.telemetry;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KafkaSpanNameProcessorTest {

    private static final String SERVICE_NAME = "document-signing-request-consumer";
    private static final AttributeKey<String> MESSAGING_SYSTEM_KEY =
            AttributeKey.stringKey("messaging.system");

    private final KafkaSpanNameProcessor underTest = new KafkaSpanNameProcessor(SERVICE_NAME);

    @Mock
    ReadWriteSpan span;

    @Mock
    ReadableSpan readableSpan;

    @Mock
    Context context;

    @Test
    void givenKafkaSpan_whenOnStart_thenSpanRenamedToServiceName() {
        // Given
        when(span.getAttribute(MESSAGING_SYSTEM_KEY)).thenReturn("kafka");

        // When
        underTest.onStart(context, span);

        // Then
        verify(span).updateName(SERVICE_NAME);
    }

    @Test
    void givenNonKafkaSpan_whenOnStart_thenSpanNameNotChanged() {
        // Given
        when(span.getAttribute(MESSAGING_SYSTEM_KEY)).thenReturn("http");

        // When
        underTest.onStart(context, span);

        // Then
        verify(span, never()).updateName(SERVICE_NAME);
    }

    @Test
    void givenSpanWithNoMessagingSystem_whenOnStart_thenSpanNameNotChanged() {
        // Given
        when(span.getAttribute(MESSAGING_SYSTEM_KEY)).thenReturn(null);

        // When
        underTest.onStart(context, span);

        // Then
        verify(span, never()).updateName(SERVICE_NAME);
    }

    @Test
    void isStartRequired_returnsTrue() {
        // Then
        assert underTest.isStartRequired();
    }

    @Test
    void isEndRequired_returnsFalse() {
        // Then
        assert !underTest.isEndRequired();
    }

    @Test
    void onEnd_doesNothing() {
        // When / Then (no exception thrown, no interactions)
        underTest.onEnd(readableSpan);
    }
}

