package uk.gov.companieshouse.documentsigningrequestconsumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.kafka.common.serialization.Serializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.ProducerFactory;
import uk.gov.companieshouse.documentsigning.SignDigitalDocument;
import uk.gov.companieshouse.documentsigningrequestconsumer.exception.KafkaException;
import uk.gov.companieshouse.kafka.exceptions.SerializationException;
import uk.gov.companieshouse.kafka.serialization.AvroSerializer;
import uk.gov.companieshouse.kafka.serialization.SerializerFactory;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
class KafkaConfigTest {

    @Mock
    private Logger logger;

    @Mock
    SerializerFactory serializerFactory;

    @Mock
    Serializer<SignDigitalDocument> serializer;

    @Mock
    private AvroSerializer<SignDigitalDocument> avroSerializer;

    KafkaConfig underTest;

    @BeforeEach
    void setup() {
        underTest = new KafkaConfig("http://localhost:9092", logger);
    }

    @Test
    void testCreateSerializerFactory() {
        SerializerFactory response = underTest.serializerFactory();
        assertThat(response, is(notNullValue()));

        verify(logger, times(1)).info("serializerFactory() method called.");
    }

    @Test
    void testCreateSignDigitalDocumentSerializer() {
        Serializer<SignDigitalDocument> response = underTest.signDigitalDocumentSerializer(serializerFactory);
        assertThat(response, is(notNullValue()));

        verify(logger, times(1)).info("signDigitalDocumentSerializer() method called.");
    }

    @Test
    void testCreateSignDigitalDocumentSerializer_failWithSerializationException() throws SerializationException {
        Serializer<SignDigitalDocument> response = underTest.signDigitalDocumentSerializer(serializerFactory);
        assertThat(response, is(notNullValue()));

        SignDigitalDocument document = mock(SignDigitalDocument.class);

        when(serializerFactory.getSpecificRecordSerializer(SignDigitalDocument.class)).thenReturn(avroSerializer);
        when(avroSerializer.toBinary(document)).thenThrow(SerializationException.class);

        KafkaException expectedException = assertThrows(KafkaException.class,
                () -> response.serialize("test-topic", document));

        assertThat(expectedException, is(notNullValue()));
        assertThat(expectedException.getCause().getClass(), is(SerializationException.class));

        verify(logger, times(1)).info("signDigitalDocumentSerializer() method called.");
    }

    @Test
    void testConsumerFactorySuccess() {
        ConsumerFactory<String, SignDigitalDocument> factory = underTest.consumerFactory();
        assertThat(factory, is(notNullValue()));

        verify(logger, times(1)).info("consumerFactory() method called.");
    }

    @Test
    void testProducerFactorySuccess() {
        MessageFlags messageFlags = new MessageFlags();
        messageFlags.setRetryable(true);

        String invalidMessageTopic = "invalid_message_topic";

        ProducerFactory<String, SignDigitalDocument> factory = underTest.producerFactory(messageFlags, serializer, invalidMessageTopic);
        assertThat(factory, is(notNullValue()));

        verify(logger, times(1)).info("producerFactory(retryable=%s, topic=%s) method called."
                .formatted(messageFlags.isRetryable(), invalidMessageTopic));
    }

}
