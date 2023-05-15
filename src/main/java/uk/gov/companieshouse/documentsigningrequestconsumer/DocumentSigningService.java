package uk.gov.companieshouse.documentsigningrequestconsumer;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.documentsigning.CoverSheetDataApi;
import uk.gov.companieshouse.api.model.documentsigning.SignPDFApi;
import uk.gov.companieshouse.api.model.documentsigning.SignPDFResponseApi;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.util.DataMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    @Override
    public void processMessage(ServiceParameters parameters) {
        final String orderId = parameters.getData().get(ORDER_ID).toString();
        final String itemGroupId = parameters.getData().get(ITEM_GROUP).toString();

        logger.info("Mapping parameters for document sign request request", getLogMap(orderId, itemGroupId));

        SignPDFApi requestBody = mapMessageToRequest(parameters, logger);

        try {
            ApiResponse<SignPDFResponseApi> response = apiClientService.getInternalApiClient()
                            .privateDocumentSigningResourceHandler()
                            .signPDF(SIGN_PDF_URI, requestBody)
                            .execute();

            logger.info("API returned response: "+ response.getStatusCode(), getLogMap(orderId, itemGroupId));

            //TODO Use response to populate satisfy item request

        } catch (ApiErrorResponseException e) {
            logger.error("Failed to get response from Document Signing API", getLogMap(orderId, itemGroupId));
            throw new RetryableException("Attempting retry due to failed response", e);
        } catch (URIValidationException e) {
            logger.error("Error with URI", getLogMap(orderId, itemGroupId));
            throw new RetryableException("Attempting retry due to URI validation error", e);
        }
    }

    private SignPDFApi mapMessageToRequest(ServiceParameters parameters, Logger logger) {
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

    private Map<String, Object> getLogMap(final String orderId, String itemGroupId) {
        return new DataMap.Builder()
            .orderId(orderId)
            .itemGroupId(itemGroupId)
            .build()
            .getLogMap();
    }
}