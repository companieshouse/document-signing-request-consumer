package uk.gov.companieshouse.documentsigningrequestconsumer;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@CucumberContextConfiguration
@AutoConfigureMockMvc
@ActiveProfiles("test_main_positive")
@EmbeddedKafka
@Import(TestConfig.class)
public class Configuration {
}
