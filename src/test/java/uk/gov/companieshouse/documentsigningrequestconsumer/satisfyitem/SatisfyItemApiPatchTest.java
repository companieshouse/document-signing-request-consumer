package uk.gov.companieshouse.documentsigningrequestconsumer.satisfyitem;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.handler.satisfyitem.PrivateSatisfyItemResourceHandler;
import uk.gov.companieshouse.api.handler.satisfyitem.request.PrivateSatisfyItem;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.satisfyitem.SatisfyItemApi;
import uk.gov.companieshouse.documentsigning.SignDigitalDocument;
import uk.gov.companieshouse.documentsigningrequestconsumer.ApiClientService;
import uk.gov.companieshouse.documentsigningrequestconsumer.ServiceParameters;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
class SatisfyItemApiPatchTest {
    private static final String ITEM_GROUPS_ITEM_URI = "/item-groups/IG-954916-860369/items/111-222-333";
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
    private InternalApiClient internalApiClient;
    @Mock
    private PrivateSatisfyItemResourceHandler privateSatisfyItemResourceHandler;
    @Mock
    private PrivateSatisfyItem privateSatisfyItem;
    @Mock
    private ApiResponse<Void> satisfyItemResponse;
    @InjectMocks
    private SatisfyItemApiPatch satisfyItemApiPatch;

    @BeforeEach
    void setUp() {
        when(apiClientService.getInternalApiClient()).thenReturn(internalApiClient);
        when(internalApiClient.privateSatisfyItemResourceHandler()).thenReturn(privateSatisfyItemResourceHandler);
        when(privateSatisfyItemResourceHandler.satisfyItem(anyString(), any(SatisfyItemApi.class))).thenReturn(privateSatisfyItem);
    }

    @Test
    @DisplayName("satisfy item")
    void satisfyItem() throws ApiErrorResponseException, URIValidationException {
        // Given
        when(privateSatisfyItem.execute()).thenReturn(satisfyItemResponse);
        // When
        satisfyItemApiPatch.satisfyItem(messageParams, HttpStatus.CREATED.value(), ITEM_GROUPS_ITEM_URI);
        // Then
        verify(apiClientService, atLeastOnce()).getInternalApiClient();
        verify(internalApiClient, atLeastOnce()).privateSatisfyItemResourceHandler();
        verify(privateSatisfyItemResourceHandler, atLeastOnce()).satisfyItem(anyString(), any(SatisfyItemApi.class));
        verify(privateSatisfyItem, atLeastOnce()).execute();
    }

    @Test
    @DisplayName("satisfy item throws ApiErrorResponseException")
    void throwsApiErrorResponseException()
        throws ApiErrorResponseException, URIValidationException {

        when(privateSatisfyItem.execute()).thenThrow(ApiErrorResponseException.class);

        assertThrows(ApiErrorResponseException.class,
            () -> satisfyItemApiPatch.satisfyItem(
                messageParams,
                HttpStatus.CREATED.value(),
                ITEM_GROUPS_ITEM_URI));
    }

    @Test
    @DisplayName("satisfy item throws URIValidationException")
    void throwsURIValidationException()
        throws ApiErrorResponseException, URIValidationException {

        when(privateSatisfyItem.execute()).thenThrow(URIValidationException.class);

        assertThrows(URIValidationException.class,
            () -> satisfyItemApiPatch.satisfyItem(
                messageParams,
                HttpStatus.CREATED.value(),
                ITEM_GROUPS_ITEM_URI));
    }
}