package uk.gov.companieshouse.documentsigningrequestconsumer;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.documentsigning.SignDigitalDocument;

import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.documentsigningrequestconsumer.Constants.DOCUMENT;
import static uk.gov.companieshouse.documentsigningrequestconsumer.Constants.SAME_PARTITION_KEY;

@ExtendWith(MockitoExtension.class)
class InvalidMessageRouterTest {

    private InvalidMessageRouter invalidMessageRouter;

    @Mock
    private MessageFlags flags;

    @BeforeEach
    void setup() {
        invalidMessageRouter = new InvalidMessageRouter();
        invalidMessageRouter.configure(Map.of("message.flags", flags, "invalid.message.topic", "invalid"));
    }

    @Test
    void testOnSendRoutesMessageToInvalidMessageTopicIfNonRetryableExceptionThrown() {
        // given
        ProducerRecord<String, SignDigitalDocument> message =
                new ProducerRecord<>("main", SAME_PARTITION_KEY, DOCUMENT);

        // when
        ProducerRecord<String, SignDigitalDocument> actual = invalidMessageRouter.onSend(message);

        // then
        assertThat(actual, is(equalTo(new ProducerRecord<>("invalid", SAME_PARTITION_KEY, DOCUMENT))));
    }

    @Test
    void testOnSendRoutesMessageToTargetTopicIfRetryableExceptionThrown() {
        // given
        ProducerRecord<String, SignDigitalDocument> message =
                new ProducerRecord<>("main", SAME_PARTITION_KEY, DOCUMENT);
        when(flags.isRetryable()).thenReturn(true);

        // when
        ProducerRecord<String, SignDigitalDocument> actual = invalidMessageRouter.onSend(message);

        // then
        assertThat(actual, is(sameInstance(message)));
    }

}
