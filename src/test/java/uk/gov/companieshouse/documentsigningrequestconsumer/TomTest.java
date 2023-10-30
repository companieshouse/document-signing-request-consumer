package uk.gov.companieshouse.documentsigningrequestconsumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.documentsigning.SignPDFResponseApi;
import uk.gov.companieshouse.documentsigning.SignDigitalDocument;
import uk.gov.companieshouse.documentsigningrequestconsumer.satisfyitem.SatisfyItemApiPatch;
import uk.gov.companieshouse.documentsigningrequestconsumer.signdocument.SignDocumentApiPost;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.logging.Logger;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TomTest {

    private static final SignDigitalDocument DOCUMENT_DATA = new SignDigitalDocument(
        "location",
        "documentType",
        "itemGroup",
        "orderNum",
        "companyName",
        "companyNumber",
        "filingHistoryDescription",
        "filingHistoryType"
    );

    @InjectMocks
    private DocumentSigningService documentSigningService;

    @Mock
    private SignDocumentApiPost signDocumentApiPost;

    @Mock
    private SatisfyItemApiPatch satisfyItemApiPatch;

    @Mock Logger logger;

    @Mock
    ApiClientService apiClientService;

    //
    //
    //
    @InjectMocks
    private SignDocumentApiPost signDocumentApiPostInject;
    @Mock
    EnvironmentReader environmentReader;

    @Test
    public void testProcessMessage() throws ApiErrorResponseException, URIValidationException {
        //Define Parameters
        ServiceParameters messageParams = new ServiceParameters(DOCUMENT_DATA);


        //Mock the behaviour of getInternalApiClient


        //Mock the behaviour of SignDocumentApiPost
        when(signDocumentApiPost.signDocument(messageParams)).thenReturn(createApiResponse());
//        when(signDocumentApiPostInject.signDocument(messageParams)).thenReturn(createApiResponse());

        //Mock the internal API call within satisfyItem
        doNothing().when(satisfyItemApiPatch)
            .satisfyItem(eq(messageParams), eq(200), anyString());

        //Call the method to be tested
        documentSigningService.processMessage(messageParams);

    }

    //Helper method to create a sample ApiRespone
    private ApiResponse<SignPDFResponseApi> createApiResponse() {
        SignPDFResponseApi response = new SignPDFResponseApi();
        response.setSignedDocumentLocation("example.pdf");
        return new ApiResponse<>(200, null, response);
    }

    //Helper method to create a sample ApiResponse for the internal SatisfyItem call
    private ApiResponse<Void> createSatisfyItemApiResponse() {
        return new ApiResponse<>(200, null, null);
    }
}