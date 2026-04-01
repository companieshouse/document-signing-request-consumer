package uk.gov.companieshouse.documentsigningrequestconsumer;

import consumer.deserialization.AvroDeserializer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import uk.gov.companieshouse.documentsigning.SignDigitalDocument;
import uk.gov.companieshouse.documentsigningrequestconsumer.exception.KafkaException;
import uk.gov.companieshouse.kafka.exceptions.SerializationException;
import uk.gov.companieshouse.kafka.serialization.SerializerFactory;

@TestConfiguration
public class TestConfig {

    public static final String[] TEST_TOPICS = {"echo", "echo-retry", "echo-error", "echo-invalid"};

    @Bean
    CountDownLatch latch(@Value("${steps}") int steps) {
        return new CountDownLatch(steps);
    }

    @Bean
    public static KafkaConsumer<String, SignDigitalDocument> createKafkaConsumer(final EmbeddedKafkaBroker embeddedKafkaBroker) {
        Map<String, Object> consumerProps =
                KafkaTestUtils.consumerProps(embeddedKafkaBroker, "test-group", true);

        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        KafkaConsumer<String, SignDigitalDocument> consumer = new KafkaConsumer<>(
                consumerProps,
                new StringDeserializer(),
                new AvroDeserializer<>(SignDigitalDocument.class));

        consumer.subscribe(List.of(TEST_TOPICS));

        return consumer;

    }

    @Bean
    public static KafkaProducer<String, SignDigitalDocument> createKafkaProducer(final EmbeddedKafkaBroker embeddedKafkaBroker, final SerializerFactory serializerFactory) {
        Map<String, Object> producerProps = new HashMap<>(
                KafkaTestUtils.producerProps(embeddedKafkaBroker)
        );

        return new KafkaProducer<>(producerProps, new StringSerializer(), (topic, data) -> {
            try {
                return serializerFactory.getSpecificRecordSerializer(SignDigitalDocument.class).toBinary(data); //creates a leading space
            } catch (SerializationException e) {
                throw new KafkaException(e);
            }
        });
    }

    @Bean(name = "service")
    @Primary
    public DocumentService getService() {
        return new NonRetryableExceptionService();
    }
}