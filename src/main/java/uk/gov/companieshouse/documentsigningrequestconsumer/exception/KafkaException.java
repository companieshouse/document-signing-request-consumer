package uk.gov.companieshouse.documentsigningrequestconsumer.exception;

public class KafkaException extends RuntimeException{

    public KafkaException(Throwable cause) {
        super(cause);
    }

}
