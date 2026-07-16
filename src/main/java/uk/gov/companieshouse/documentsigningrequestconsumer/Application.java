package uk.gov.companieshouse.documentsigningrequestconsumer;

import static org.springframework.boot.SpringApplication.run;
import static uk.gov.companieshouse.documentsigningrequestconsumer.EnvironmentVariablesChecker.allRequiredEnvironmentVariablesPresent;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class Application {

    public static final String APPLICATION_NAME_SPACE = "document-signing-request-consumer";

    public static void main(String[] args) {
        if (allRequiredEnvironmentVariablesPresent()) {
            run(Application.class, args);
        }
    }
}
