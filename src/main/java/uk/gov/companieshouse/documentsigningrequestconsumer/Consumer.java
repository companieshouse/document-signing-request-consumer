package uk.gov.companieshouse.documentsigningrequestconsumer;

import org.jspecify.annotations.NonNull;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.documentsigning.SignDigitalDocument;
import uk.gov.companieshouse.documentsigningrequestconsumer.exception.NonRetryableException;
import uk.gov.companieshouse.documentsigningrequestconsumer.exception.RetryableException;

/**
 * Consumes messages from the configured main Kafka topic.
 */
@Component
public class Consumer {

    private final DocumentService service;
    private final MessageFlags messageFlags;

    public Consumer(DocumentService service, MessageFlags messageFlags) {
        this.service = service;
        this.messageFlags = messageFlags;
    }

    /**
     * Consume a message from the main Kafka topic.
     *
     * @param message A message containing a payload.
     */
    @KafkaListener(
            id = "${consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory",
            topics = "${consumer.topic}",
            groupId = "${consumer.group-id}",
            autoStartup = "true"
    )
    @RetryableTopic(
            attempts = "${consumer.max-attempts}",
            autoCreateTopics = "false",
            backOff = @BackOff(delayString = "${consumer.backoff-delay}"),
            dltTopicSuffix = "-error",
            dltStrategy = DltStrategy.FAIL_ON_ERROR,
            include = RetryableException.class
    )
    public void consume(final Message<@NonNull SignDigitalDocument> message) {
        try {
            service.processMessage(new ServiceParameters(message.getPayload()));

        } catch (RetryableException e) {
            messageFlags.setRetryable(true);
            throw e;

        } catch(NonRetryableException e) {
            messageFlags.setRetryable(false);
            throw e;
        }
    }

}
