package uk.gov.companieshouse.documentsigningrequestconsumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DocumentSigningRequestConsumerApplication {

    public static final String NAMESPACE = "document-signing-request-consumer";

    public static void main(String[] args) {
        SpringApplication.run(DocumentSigningRequestConsumerApplication.class, args);
    }

}
