package uk.gov.companieshouse.documentsigningrequestconsumer.signdocument;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.documentsigningrequestconsumer.Constants.DOCUMENT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import uk.gov.companieshouse.documentsigningrequestconsumer.ApiClientService;
import uk.gov.companieshouse.documentsigningrequestconsumer.ServiceParameters;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
class SignDocumentApiPostTest {
    private static final ServiceParameters messageParams = new ServiceParameters(DOCUMENT);

    @Mock
    private Logger logger;
    @Mock
    private ApiClientService apiClientService;
    @Mock
    private InternalApiClient internalApiClient;
    @Mock
    private EnvironmentReader environmentReader;
    @Mock
    private PrivateDocumentSigningResourceHandler privateDocumentSigningResourceHandler;
    @Mock
    private PrivateSignPDF privateSignPDF;
    @Mock
    private ApiResponse<SignPDFResponseApi> signPdfResponse;
    @InjectMocks
    private SignDocumentApiPost signDocumentApiPost;

    @BeforeEach
    void setUp() {
        // Given
        when(environmentReader.getMandatoryString(anyString())).thenReturn("test/certified-copy");
        when(apiClientService.getInternalApiClient()).thenReturn(internalApiClient);
        when(internalApiClient.privateDocumentSigningResourceHandler()).thenReturn(privateDocumentSigningResourceHandler);
        when(privateDocumentSigningResourceHandler.signPDF(anyString(), any(SignPDFApi.class))).thenReturn(privateSignPDF);
    }

    @Test
    @DisplayName("sign document")
    void signDocument() throws ApiErrorResponseException, URIValidationException {
        // Given
        when(privateSignPDF.execute()).thenReturn(signPdfResponse);
        // When
        signDocumentApiPost.signDocument(messageParams);
        // Then
        verify(apiClientService, atLeastOnce()).getInternalApiClient();
        verify(internalApiClient, atLeastOnce()).privateDocumentSigningResourceHandler();
        verify(privateDocumentSigningResourceHandler, atLeastOnce()).signPDF(anyString(), any(SignPDFApi.class));
        verify(privateSignPDF, atLeastOnce()).execute();
    }

    @Test
    @DisplayName("sign document throws ApiErrorResponseException")
    void throwsApiErrorResponseException()
        throws ApiErrorResponseException, URIValidationException {
        // When
        when(privateSignPDF.execute()).thenThrow(ApiErrorResponseException.class);
        // Then
        assertThrows(ApiErrorResponseException.class,
            () -> signDocumentApiPost.signDocument(messageParams));
    }

    @Test
    @DisplayName("sign document throws URIValidationException")
    void throwsURIValidationException()
        throws ApiErrorResponseException, URIValidationException {
        // When
        when(privateSignPDF.execute()).thenThrow(URIValidationException.class);
        // Then
        assertThrows(URIValidationException.class,
            () -> signDocumentApiPost.signDocument(messageParams));
    }
}