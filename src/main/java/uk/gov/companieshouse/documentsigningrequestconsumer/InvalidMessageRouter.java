package uk.gov.companieshouse.documentsigningrequestconsumer;

import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import uk.gov.companieshouse.documentsigning.SignDigitalDocument;

import java.util.Map;

/**
 * Routes a message to the invalid letter topic if a non-retryable error has been thrown during message processing.
 */
public class InvalidMessageRouter implements ProducerInterceptor<String, SignDigitalDocument> {

    private MessageFlags messageFlags;
    private String invalidMessageTopic;

    @Override
    public ProducerRecord<String, SignDigitalDocument> onSend(ProducerRecord<String, SignDigitalDocument> producerRecord) {
        if (messageFlags.isRetryable()) {
            messageFlags.destroy();
            return producerRecord;
        } else {
            return new ProducerRecord<>(this.invalidMessageTopic, producerRecord.key(), producerRecord.value());
        }
    }

    @Override
    public void onAcknowledgement(RecordMetadata metadata, Exception exception) {
        // Method must be implemented, but no intervention is required here.
    }

    @Override
    public void close() {
        // Method must be implemented, but no intervention is required here.
    }

    @Override
    public void configure(Map<String, ?> configs) {
        this.messageFlags = (MessageFlags) configs.get("message.flags");
        this.invalidMessageTopic = (String) configs.get("invalid.message.topic");
    }
}
