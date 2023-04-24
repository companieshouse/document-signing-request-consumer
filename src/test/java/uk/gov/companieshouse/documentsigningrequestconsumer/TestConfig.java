package uk.gov.companieshouse.documentsigningrequestconsumer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import consumer.deserialization.AvroDeserializer;
import consumer.serialization.AvroSerializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import uk.gov.companieshouse.documentsigning.SignDigitalDocument;

import uk.gov.companieshouse.kafka.exceptions.SerializationException;
import uk.gov.companieshouse.kafka.serialization.SerializerFactory;

@TestConfiguration
public class TestConfig {

    @Bean
    CountDownLatch latch(@Value("${steps}") int steps) {
        return new CountDownLatch(steps);
    }

    @Bean
    KafkaConsumer<String, SignDigitalDocument> testConsumer(@Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
        return new KafkaConsumer<>(
                Map.of(
                        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, AvroSerializer.class,
                        ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class,
                        ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, AvroDeserializer.class,
                        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                        ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false",
                        ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString()),
                new StringDeserializer(), new AvroDeserializer<>(SignDigitalDocument.class));
    }

    @Bean
    KafkaProducer<String, SignDigitalDocument> testProducer(@Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
        return new KafkaProducer<>(
                Map.of(
                        ProducerConfig.ACKS_CONFIG, "all",
                        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers),
                new StringSerializer(),
                (topic, data) -> {
                    try {
                        return new SerializerFactory().getSpecificRecordSerializer(SignDigitalDocument.class).toBinary(data); //creates a leading space
                    } catch (SerializationException e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}