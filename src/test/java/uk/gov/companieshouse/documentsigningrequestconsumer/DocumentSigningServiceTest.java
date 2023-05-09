package uk.gov.companieshouse.documentsigningrequestconsumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.documentsigning.SignDigitalDocument;

@ExtendWith(MockitoExtension.class)
public class DocumentSigningServiceTest {

    @Mock
    private ApiClientService apiClientService;

    @InjectMocks
    private DocumentSigningService documentSigningService;

    @Test
    void test() {
        SignDigitalDocument data = new SignDigitalDocument("location",
            "documentType", "itemGroup", "orderNum");

        ServiceParameters parameters = new ServiceParameters(data);

        documentSigningService.processMessage(parameters);
    }
}
