package uk.gov.companieshouse.documentsigningrequestconsumer;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test_main_positive.yml")
@Import(TestConfig.class)
@EmbeddedKafka
@ActiveProfiles("test_main_positive")
class DocumentSigningRequestConsumerApplicationTests {

    @Test
    void contextLoads() {
    }

}
