package uk.gov.companieshouse.documentsigningrequestconsumer;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.documentsigning.PrivateDocumentSigningResourceHandler;
import uk.gov.companieshouse.api.handler.documentsigning.request.PrivateSignPDF;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.handler.satisfyitem.PrivateSatisfyItemResourceHandler;
import uk.gov.companieshouse.api.handler.satisfyitem.request.PrivateSatisfyItem;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.documentsigning.SignPDFApi;
import uk.gov.companieshouse.api.model.documentsigning.SignPDFResponseApi;
import uk.gov.companieshouse.api.model.satisfyitem.SatisfyItemApi;
import uk.gov.companieshouse.documentsigning.SignDigitalDocument;
import uk.gov.companieshouse.documentsigningrequestconsumer.satisfyitem.SatisfyItemApiPatch;
import uk.gov.companieshouse.documentsigningrequestconsumer.signdocument.SignDocumentApiPost;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
class DocumentSigningServiceTest {
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
    private static final ServiceParameters messageParams = new ServiceParameters(DOCUMENT_DATA);

    @Mock
    private Logger logger;
    @Mock
    private ApiClientService apiClientService;
    @Mock
    EnvironmentReader environmentReader;
    @Mock
    private InternalApiClient internalApiClient;
    @Mock
    private PrivateDocumentSigningResourceHandler privateDocumentSigningResourceHandler;
    @Mock
    private PrivateSignPDF privateSignPDF;
    @Mock
    private SignPDFResponseApi signPDFResponseApi;
    @Mock
    private ApiResponse<SignPDFResponseApi> signPdfResponse;
    @Mock
    private PrivateSatisfyItemResourceHandler privateSatisfyItemResourceHandler;
    @Mock
    private ApiResponse<Void> satisfyItemResponse;
    @Mock
    private PrivateSatisfyItem privateSatisfyItem;

    @InjectMocks
    private DocumentSigningService documentSigningService;

    @Mock
    private SignDocumentApiPost signDocumentApiPostMock;
    @InjectMocks
    private SignDocumentApiPost signDocumentApiPostInjectMock;

    @Mock
    private SatisfyItemApiPatch satisfyItemApiPatchMock;
    @InjectMocks
    private SatisfyItemApiPatch satisfyItemApiPatchInjectMock;

    void processMessageSetup() {
//        when(environmentReader.getMandatoryString(anyString())).thenReturn("test/certified-copy");
        when(apiClientService.getInternalApiClient()).thenReturn(internalApiClient);
//
        when(internalApiClient.privateDocumentSigningResourceHandler()).thenReturn(privateDocumentSigningResourceHandler);
        when(privateDocumentSigningResourceHandler.signPDF(anyString(), any(SignPDFApi.class))).thenReturn(privateSignPDF);
//
//        when(internalApiClient.privateSatisfyItemResourceHandler()).thenReturn(privateSatisfyItemResourceHandler);
//        when(privateSatisfyItemResourceHandler.satisfyItem(anyString(), any(SatisfyItemApi.class))).thenReturn(privateSatisfyItem);
    }

    @Test
    @DisplayName("process message throws RetryableException when ApiErrorResponseException caught")
    void processMessageThrowsRetryableExceptionWhenApiErrorResponseExceptionCaught()
        throws ApiErrorResponseException, URIValidationException {
        // When
        doThrow(ApiErrorResponseException.class)
            .when(signDocumentApiPostMock).signDocument(any());
        // Then
        assertThrows(RetryableException.class,
            () -> documentSigningService.processMessage(messageParams));
    }

    @Test
    @DisplayName("process message throws RetryableException when URIValidationException caught")
    void processMessageThrowsRetryableExceptionWhenURIValidationExceptionCaught()
        throws ApiErrorResponseException, URIValidationException {
        // When
        doThrow(URIValidationException.class)
            .when(signDocumentApiPostMock).signDocument(any());
        // Then
        assertThrows(RetryableException.class,
            () -> documentSigningService.processMessage(messageParams));
    }

    @Test
    @DisplayName("process message throws NonRetryableException when any non-checked exception caught")
    void processMessageThrowsNonRetryableExceptionWhenExceptionCaught()
        throws ApiErrorResponseException, URIValidationException {
        // When
        doThrow(RuntimeException.class)
            .when(signDocumentApiPostMock).signDocument(any());
        // Then
        assertThrows(NonRetryableException.class,
            () -> documentSigningService.processMessage(messageParams));
    }

    @Test
    @DisplayName("process message sends sign document request and updates status")
    void processMessageSucceeds() throws Exception {
        // Given
//        processMessageSetup();
//
//        when(privateSignPDF.execute()).thenReturn(signPdfResponse);
//        when(privateSatisfyItem.execute()).thenReturn(satisfyItemResponse);
//        when(signPdfResponse.getStatusCode()).thenReturn(HttpStatus.CREATED.value());
//        when(signPdfResponse.getData()).thenReturn(signPDFResponseApi);
//        when(signPdfResponse.getData().getSignedDocumentLocation()).thenReturn("something something...");

//        doReturn(signPdfResponse).when(signDocumentApiPostInjectMock.signDocument(messageParams));
//        var response = signDocumentApiPostInjectMock.signDocument(messageParams);
//        when(signDocumentApiPostMock.signDocument(messageParams)).thenReturn(signPdfResponse);

//        when(signDocumentApiPostInjectMock.signDocument(messageParams)).thenReturn(signPdfResponse);

        when(signDocumentApiPostMock.signDocument(messageParams)).thenReturn(createApiSignDocumentApiResponse());
//        doNothing().when(satisfyItemApiPatchInjectMock)
        doNothing().when(satisfyItemApiPatchMock)
            .satisfyItem(
                isA(ServiceParameters.class),
                isA(Integer.class),
                isA(String.class));

        // When
        documentSigningService.processMessage(messageParams);
        // Then
        verify(signDocumentApiPostInjectMock.signDocument(messageParams), atLeastOnce());
        verify(satisfyItemApiPatchMock, atLeastOnce()).satisfyItem(any(), anyInt(), any());
    }

    private ApiResponse<SignPDFResponseApi> createApiSignDocumentApiResponse() {
        SignPDFResponseApi response = new SignPDFResponseApi();
        response.setSignedDocumentLocation("example.pdf");
        return new ApiResponse<>(200, null, response);
    }
}