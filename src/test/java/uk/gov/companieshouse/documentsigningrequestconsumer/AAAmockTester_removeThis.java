package uk.gov.companieshouse.documentsigningrequestconsumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.documentsigning.SignPDFResponseApi;
import uk.gov.companieshouse.documentsigning.SignDigitalDocument;
import uk.gov.companieshouse.documentsigningrequestconsumer.satisfyitem.SatisfyItemApiPatch;
import uk.gov.companieshouse.documentsigningrequestconsumer.signdocument.SignDocumentApiPost;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)

public class AAAmockTester_removeThis {
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

    @InjectMocks
    private DocumentSigningService documentSigningService;
    //    @InjectMocks
//    private SignDocumentApiPost signDocumentApiPost;
    @Mock
    private Logger logger;
    @Mock
    private SignDocumentApiPost signDocumentApiPostMock;
    @Mock
    private SatisfyItemApiPatch satisfyItemApiPatchMock;
    @Mock
    private ApiResponse<SignPDFResponseApi> signPdfResponse;

    @Test
    @DisplayName("Test process message sends sign document request and updates status via ItemGroupWorkflowAPI")
    void xxx() throws Exception {
        // Given
//        processMessageSetup();
        ServiceParameters parameters = new ServiceParameters(DOCUMENT_DATA);




//        when(signPdfResponse.getData()).thenReturn();

//        when(signPdfResponse.getData().getSignedDocumentLocation()).thenReturn("");
//        when(signDocumentApiPostMock.signDocument(parameters)).thenReturn(signPdfResponse);
//        doNothing().when(satisfyItemApiPatchMock).satisfyItem(isA(
//                ServiceParameters.class),
//            isA(Integer.class),
//            isA(String.class));

//        when(satisfyItemApiPatch.satisfyItem(any(), any(), any())).thenReturn(satisfyItemResponse);

        // When
        documentSigningService.processMessage(parameters);
        // Then
        verify(signDocumentApiPostMock.signDocument(parameters), atLeastOnce());
        verify(satisfyItemApiPatchMock, atLeastOnce()).satisfyItem(any(), anyInt(), any());
    }
}