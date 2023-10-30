package uk.gov.companieshouse.documentsigningrequestconsumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.documentsigningrequestconsumer.ApiUtils.mapMessageToRequest;
import org.junit.jupiter.api.BeforeEach;
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
import uk.gov.companieshouse.documentsigning.SignDigitalDocument;
import uk.gov.companieshouse.documentsigningrequestconsumer.satisfyitem.SatisfyItemApiPatch;
import uk.gov.companieshouse.documentsigningrequestconsumer.signdocument.SignDocumentApiPost;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
public class Tom_2_Test {
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
    @Mock Logger logger;
    @Mock EnvironmentReader environmentReader;
    @Mock SignPDFApi signPDFApi;
    @Mock ApiClientService apiClientService;
    @Mock InternalApiClient internalApiClient;
    @Mock PrivateDocumentSigningResourceHandler privateDocumentSigningResourceHandler;
    @Mock SatisfyItemApiPatch satisfyItemApiPatch;
    @Mock ApiResponse<SignPDFResponseApi> signDocResponse;
    @Mock PrivateSignPDF privateSignPDF;
    @InjectMocks DocumentSigningService documentSigningService;
    @InjectMocks SignDocumentApiPost signDocumentApiPostInjectMock;
    @InjectMocks SatisfyItemApiPatch satisfyItemApiPatchInjectMock;
    @InjectMocks SignDocumentApiPost signDocumentApiPost;

    @BeforeEach
    void setup() {
//        when(mapMessageToRequest(environmentReader, messageParams)).thenReturn(signPDFApi);
        when(apiClientService.getInternalApiClient()).thenReturn(internalApiClient);
        when(internalApiClient.privateDocumentSigningResourceHandler()).thenReturn(privateDocumentSigningResourceHandler);
        when(privateDocumentSigningResourceHandler.signPDF(anyString(), any(SignPDFApi.class))).thenReturn(privateSignPDF);
    }

    @Test
    void processMessageSucceeds()
        throws ApiErrorResponseException, URIValidationException {

        final int debugStop = -999;
        // Given
        when(privateSignPDF.execute()).thenReturn(signDocResponse);
        //
        // TODO :: Find out why the following produces:
//        org.mockito.exceptions.misusing.WrongTypeOfReturnValue:
//        ApiResponse cannot be returned by toString()
//        toString() should return String
        //
        when(signDocumentApiPost.signDocument(messageParams)).thenReturn(signDocumentApiResponse());
        // When
        documentSigningService.processMessage(messageParams);
        // Then
        verify(signDocumentApiPostInjectMock.signDocument(messageParams), atLeastOnce());
        verify(satisfyItemApiPatchInjectMock, atLeastOnce()).satisfyItem(any(), anyInt(), any());
    }

    private ApiResponse<SignPDFResponseApi> signDocumentApiResponse() {
        SignPDFResponseApi response = new SignPDFResponseApi();
        response.setSignedDocumentLocation("example.pdf");
        return new ApiResponse<>(200, null, response);
    }
}