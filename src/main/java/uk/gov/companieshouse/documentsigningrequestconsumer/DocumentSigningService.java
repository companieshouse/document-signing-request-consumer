package uk.gov.companieshouse.documentsigningrequestconsumer;

import org.apache.commons.lang.exception.ExceptionUtils;
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
    private static final String GROUP_ITEM = "group_item";
    private static final String PRIVATE_S3_LOCATION = "private_s3_location";
    private static final String DOCUMENT_TYPE = "document_type";
    private static final String COMPANY_NAME = "company_name";
    private static final String COMPANY_NUMBER = "company_number";
    private static final String TYPE = "type";
    private static final String DESCRIPTION = "description";
    private static final String FILING_HISTORY_DESCRIPTION_VALUES = "filing_history_description_values";

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
        final String itemGroupId = parameters.getData().get(GROUP_ITEM).toString();

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
        } catch (Exception exception) {
            final var rootCause = getRootCause(exception);
            logger.error("Error trying to send signPdf request to Document Signing API: "
                    + rootCause, getLogMap(orderId, itemGroupId, rootCause));
            throw new NonRetryableException("Unable to send signPdf request to Document Signing API", rootCause);
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

        List<String> signatureOptions = new ArrayList<>();
        signatureOptions.add(COVERSHEET_OPTION);
        requestBody.setSignatureOptions(signatureOptions);
        requestBody.setPrefix(environmentReader.getMandatoryString(PREFIX_ENV_VARIABLE));
        requestBody.setKey(APPLICATION_PDF);

        CoverSheetDataApi coverSheetDataApi = new CoverSheetDataApi();
        coverSheetDataApi.setCompanyName(parameters.getData().getCoverSheetData().get(COMPANY_NAME).toString());
        coverSheetDataApi.setCompanyNumber(parameters.getData().getCoverSheetData().get(COMPANY_NUMBER).toString());
        coverSheetDataApi.setFilingHistoryType(parameters.getData().getCoverSheetData().get(TYPE).toString());
        coverSheetDataApi.setFilingHistoryDescription(parameters.getData().getCoverSheetData().get(DESCRIPTION).toString());
        requestBody.setCoverSheetData(coverSheetDataApi);

        requestBody.setFilingHistoryDescriptionValues((Map<String, Object>) parameters.getData().get(FILING_HISTORY_DESCRIPTION_VALUES));

        return requestBody;
    }

    private Map<String, Object> getLogMap(final String orderId, final String itemGroupId, final Throwable rootCause) {
        return new DataMap.Builder()
            .orderId(orderId)
            .itemGroupId(itemGroupId)
            .errors(singletonList(rootCause.getMessage()))
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

    private Throwable getRootCause(final Exception exception) {
        final var rootCause = ExceptionUtils.getRootCause(exception);
        return rootCause != null ? rootCause : exception;
    }
}