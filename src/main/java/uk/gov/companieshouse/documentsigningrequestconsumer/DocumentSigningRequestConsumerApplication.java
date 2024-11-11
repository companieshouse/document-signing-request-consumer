package uk.gov.companieshouse.documentsigningrequestconsumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static uk.gov.companieshouse.documentsigningrequestconsumer.EnvironmentVariablesChecker.allRequiredEnvironmentVariablesPresent;

@SpringBootApplication
public class DocumentSigningRequestConsumerApplication {

    public static final String APPLICATION_NAME_SPACE = "document-signing-request-consumer";

    public static void main(String[] args) {
        if (allRequiredEnvironmentVariablesPresent()) {
            SpringApplication.run(DocumentSigningRequestConsumerApplication.class, args);
        }
    }
}
