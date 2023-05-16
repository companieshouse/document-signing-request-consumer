package uk.gov.companieshouse.documentsigningrequestconsumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.documentsigning.PrivateDocumentSigningResourceHandler;
import uk.gov.companieshouse.api.handler.documentsigning.request.PrivateSignPDF;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.documentsigning.SignPDFApi;
import uk.gov.companieshouse.api.model.documentsigning.SignPDFResponseApi;
import uk.gov.companieshouse.documentsigning.SignDigitalDocument;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.logging.Logger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DocumentSigningServiceTest {

    @Mock
    private ApiClientService apiClientService;

    @Mock
    private Logger logger;

    @Mock
    EnvironmentReader environmentReader;

    @Mock
    private InternalApiClient internalApiClient;

    @Mock
    private PrivateDocumentSigningResourceHandler privateDocumentSigningResourceHandler;

    @Mock
    private PrivateSignPDF privateSignPDF;

    @Mock
    private ApiResponse<SignPDFResponseApi> response;

    @InjectMocks
    private DocumentSigningService documentSigningService;

    private static final String SIGN_PDF_URI = "/document-signing/sign-pdf";

    private static final SignDigitalDocument DATA = new SignDigitalDocument(
        "location",
        "documentType",
        "itemGroup",
        "orderNum",
        "companyName",
        "companyNumber",
        "filingHistoryDescription",
        "filingHistoryType"
    );

    @BeforeEach
    void init() {
        when(environmentReader.getMandatoryString(anyString())).thenReturn("test/certified-copy");
        when(apiClientService.getInternalApiClient()).thenReturn(internalApiClient);
        when(internalApiClient.privateDocumentSigningResourceHandler()).thenReturn(privateDocumentSigningResourceHandler);
        when(privateDocumentSigningResourceHandler.signPDF(anyString(), any(SignPDFApi.class))).thenReturn(privateSignPDF);
    }

    @Test
    @DisplayName("Test process message sends sign document request")
    void processMessageSendsRequest() throws Exception {
        ServiceParameters parameters = new ServiceParameters(DATA);
        when(privateSignPDF.execute()).thenReturn(response);

        documentSigningService.processMessage(parameters);
        verify(apiClientService, atLeastOnce()).getInternalApiClient();
        verify(internalApiClient, atLeastOnce()).privateDocumentSigningResourceHandler();
        verify(privateDocumentSigningResourceHandler, atLeastOnce()).signPDF(anyString(), any(SignPDFApi.class));
        verify(privateSignPDF, atLeastOnce()).execute();
    }

    @Test
    @DisplayName("Test process message throws RetryableException when ApiErrorResponseException caught")
    void processMessageThrowsRetryableExceptionWithApiErrorResponseException() throws Exception {
        ServiceParameters parameters = new ServiceParameters(DATA);

        when(privateSignPDF.execute()).thenThrow(ApiErrorResponseException.class);
        final RetryableException exception = assertThrows(RetryableException.class,
            () -> documentSigningService.processMessage(parameters));

        assertThat(exception.getMessage(),
            is("Attempting retry due to failed response"));
    }

    @Test
    @DisplayName("Test process message throws RetryableException when URIValidationException caught")
    void processMessageThrowsRetryableExceptionWithURIValidationException() throws Exception {
        ServiceParameters parameters = new ServiceParameters(DATA);

        when(privateSignPDF.execute()).thenThrow(URIValidationException.class);
        final RetryableException exception = assertThrows(RetryableException.class,
            () -> documentSigningService.processMessage(parameters));

        assertThat(exception.getMessage(),
            is("Attempting retry due to URI validation error"));
    }
}
