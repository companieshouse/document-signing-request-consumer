package uk.gov.companieshouse.documentsigningrequestconsumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.documentsigning.PrivateDocumentSigningResourceHandler;
import uk.gov.companieshouse.api.handler.documentsigning.request.PrivateSignPDF;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.documentsigning.SignPDFApi;
import uk.gov.companieshouse.api.model.documentsigning.SignPDFResponseApi;
import uk.gov.companieshouse.documentsigning.SignDigitalDocument;
import uk.gov.companieshouse.documentsigning.CoverSheetDataRecord;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.environment.exception.EnvironmentVariableException;
import uk.gov.companieshouse.logging.Logger;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentSigningServiceTest {

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

    public static final Map<String, String> FILING_HISTORY_DESCRIPTION_VALUES  = new HashMap<String, String>() {{
        put("testKey1", "testValue1");
        put("testKey2", "testValue2");
    }};

    private static final CoverSheetDataRecord COVER_SHEET_DATA_RECORD = new CoverSheetDataRecord(
            "companyName",
            "companyNumber",
            "description",
            "type"
    );

    private final SignDigitalDocument DATA = new SignDigitalDocument(
         COVER_SHEET_DATA_RECORD,
        "privateS3Location",
        "documentType",
        "groupItem",
        "orderNumber",
         FILING_HISTORY_DESCRIPTION_VALUES

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

    @Test
    @DisplayName("Test process message throws NonRetryableException when EnvironmentVariableException caught")
    void processMessageThrowsNonRetryableExceptionWithEnvironmentVariableException() throws Exception {
        final ServiceParameters parameters = new ServiceParameters(DATA);

        when(privateSignPDF.execute()).thenThrow(EnvironmentVariableException.class);
        final NonRetryableException exception = assertThrows(NonRetryableException.class,
                () -> documentSigningService.processMessage(parameters));

        assertThat(exception.getMessage(),
                is("Unable to send signPdf request to Document Signing API"));
    }
}
