package uk.gov.companieshouse.documentsigningrequestconsumer;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.documentsigning.CoverSheetDataApi;
import uk.gov.companieshouse.api.model.documentsigning.SignPDFApi;
import uk.gov.companieshouse.api.model.documentsigning.SignPDFResponseApi;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.environment.exception.EnvironmentVariableException;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.util.DataMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;

/**
 * Makes sign PDF requests to the Document Signing API.
 */
@Component
class DocumentSigningService implements Service {
    private static final String SIGN_PDF_URI = "/document-signing/sign-pdf";
    private static final String COVERSHEET_OPTION = "cover-sheet";
    private static final String APPLICATION_PDF = "application-pdf";
    private static final String PREFIX_ENV_VARIABLE = "PREFIX";

    // Kafka Message Keys
    private static final String ORDER_ID = "order_number";
    private static final String ITEM_GROUP = "item_group";
    private static final String PRIVATE_S3_LOCATION = "private_s3_location";
    private static final String DOCUMENT_TYPE = "document_type";
    private static final String COMPANY_NAME = "company_name";
    private static final String COMPANY_NUMBER = "company_number";
    private static final String FILING_HISTORY_TYPE = "filing_history_type";
    private static final String FILING_HISTORY_DESCRIPTION = "filing_history_description";

    private final ApiClientService apiClientService;
    private final Logger logger;
    private final EnvironmentReader environmentReader;

    public DocumentSigningService(ApiClientService apiClientService,
                                  Logger logger,
                                  EnvironmentReader environmentReader) {
        this.apiClientService = apiClientService;
        this.logger = logger;
        this.environmentReader = environmentReader;
    }

    /**
     * Extract the message to build request to sign document
     * @param parameters the fields present with the message
     * @throws RetryableException to attempt retry if something fails
     */
    @Override
    public void processMessage(ServiceParameters parameters) {
        final String orderId = parameters.getData().get(ORDER_ID).toString();
        final String itemGroupId = parameters.getData().get(ITEM_GROUP).toString();

        logger.info("Mapping parameters for document sign request request", getLogMap(orderId, itemGroupId));

        SignPDFApi requestBody = mapMessageToRequest(parameters);

        try {
            ApiResponse<SignPDFResponseApi> response = apiClientService.getInternalApiClient()
                            .privateDocumentSigningResourceHandler()
                            .signPDF(SIGN_PDF_URI, requestBody)
                            .execute();

            logger.info("API returned response: "+ response.getStatusCode(), getLogMap(orderId, itemGroupId));

            //TODO Use response to populate satisfy item request

        } catch (ApiErrorResponseException e) {
            logger.error("Got error response from Document Signing API: " + e, getLogMap(orderId, itemGroupId, e));
            throw new RetryableException("Attempting retry due to failed response", e);
        } catch (URIValidationException e) {
            logger.error("Error with URI: " + e, getLogMap(orderId, itemGroupId, e));
            throw new RetryableException("Attempting retry due to URI validation error", e);
        } catch (EnvironmentVariableException eve) {
            logger.error("Error trying to send signPdf request to Document Signing API: "
                    + eve, getLogMap(orderId, itemGroupId, eve));
            throw new NonRetryableException("Unable to send signPdf request to Document Signing API", eve);
        }
    }

    /**
     * Maps the message data to a request
     * @param parameters the fields present with the message
     * @return the {@link SignPDFApi} data to send in request
     */
    private SignPDFApi mapMessageToRequest(ServiceParameters parameters) {
        SignPDFApi requestBody = new SignPDFApi();
        requestBody.setDocumentLocation(parameters.getData().get(PRIVATE_S3_LOCATION).toString());
        requestBody.setDocumentType(parameters.getData().get(DOCUMENT_TYPE).toString());
        requestBody.setKey(APPLICATION_PDF);
        requestBody.setPrefix(environmentReader.getMandatoryString(PREFIX_ENV_VARIABLE));

        List<String> signatureOptions = new ArrayList<>();
        signatureOptions.add(COVERSHEET_OPTION);
        requestBody.setSignatureOptions(signatureOptions);

        CoverSheetDataApi coverSheetDataApi = new CoverSheetDataApi();
        coverSheetDataApi.setCompanyName(parameters.getData().get(COMPANY_NAME).toString());
        coverSheetDataApi.setCompanyNumber(parameters.getData().get(COMPANY_NUMBER).toString());
        coverSheetDataApi.setFilingHistoryType(parameters.getData().get(FILING_HISTORY_TYPE).toString());
        coverSheetDataApi.setFilingHistoryDescription(parameters.getData().get(FILING_HISTORY_DESCRIPTION).toString());
        requestBody.setCoverSheetData(coverSheetDataApi);

        return requestBody;
    }

    private Map<String, Object> getLogMap(final String orderId, final String itemGroupId, final Exception exception) {
        return new DataMap.Builder()
            .orderId(orderId)
            .itemGroupId(itemGroupId)
            .errors(singletonList(exception.getMessage()))
            .build()
            .getLogMap();
    }

    private Map<String, Object> getLogMap(final String orderId, final String itemGroupId) {
        return new DataMap.Builder()
                .orderId(orderId)
                .itemGroupId(itemGroupId)
                .build()
                .getLogMap();
    }
}