package uk.gov.companieshouse.documentsigningrequestconsumer;

import consumer.deserialization.AvroDeserializer;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import uk.gov.companieshouse.documentsigning.SignDigitalDocument;
import uk.gov.companieshouse.documentsigningrequestconsumer.exception.KafkaException;
import uk.gov.companieshouse.kafka.exceptions.SerializationException;
import uk.gov.companieshouse.kafka.serialization.SerializerFactory;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.util.DataMap;

@Configuration
public class KafkaConfig {

    private final String bootstrapServers;
    private final Logger logger;

    public KafkaConfig(@Value("${spring.kafka.bootstrap-servers}") String bootstrapServers, Logger logger) {
        logger.info("constructor(bootstrap-servers=%s) method called.".formatted(bootstrapServers));

        this.bootstrapServers = bootstrapServers;
        this.logger = logger;
    }

    @Bean(name = "consumerFactory")
    public ConsumerFactory<String, SignDigitalDocument> consumerFactory() {
        logger.info("consumerFactory() method called.");

        return new DefaultKafkaConsumerFactory<>(
                Map.of(
                        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class,
                        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class,
                        ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class,
                        ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, AvroDeserializer.class,
                        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                        ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false"),
                new StringDeserializer(),
                new ErrorHandlingDeserializer<>(new AvroDeserializer<>(SignDigitalDocument.class)));
    }

    @Bean(name = "producerFactory")
    public ProducerFactory<String, SignDigitalDocument> producerFactory(MessageFlags messageFlags, @Value("${invalid_message_topic}") String invalidMessageTopic) {
        logger.info("producerFactory(retryable=%s, topic=%s) method called.".formatted(messageFlags.isRetryable(), invalidMessageTopic));

        Map<String, Object> config = Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "false",
                ProducerConfig.ACKS_CONFIG, "all",
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, InvalidMessageRouter.class.getName(),
                "message.flags", messageFlags,
                "invalid.message.topic", invalidMessageTopic);

        return new DefaultKafkaProducerFactory<>(config, new StringSerializer(), (topic, data) -> {
            try {
                return new SerializerFactory()
                        .getSpecificRecordSerializer(SignDigitalDocument.class)
                        .toBinary(data); //creates a leading space

            } catch (SerializationException e) {
                var dataMap = new DataMap.Builder()
                        .topic(topic)
                        .kafkaMessage(data.toString())
                        .build();
                logger.error("Caught SerializationException serializing kafka message.",
                        dataMap.getLogMap());
                throw new KafkaException(e);
            }
        });
    }

    @Bean
    public KafkaTemplate<String, SignDigitalDocument> kafkaTemplate(ProducerFactory<String, SignDigitalDocument> producerFactory) {
        logger.info("kafkaTemplate() method called.");

        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, SignDigitalDocument> kafkaListenerContainerFactory(
            ConsumerFactory<String, SignDigitalDocument> consumerFactory,
            @Value("${consumer.concurrency}") Integer concurrency) {
        logger.info("kafkaListenerContainerFactory() method called.");

        ConcurrentKafkaListenerContainerFactory<String, SignDigitalDocument> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(concurrency);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        return factory;
    }


}
