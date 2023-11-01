package uk.gov.companieshouse.documentsigningrequestconsumer.signdocument;

import static uk.gov.companieshouse.documentsigningrequestconsumer.DocumentSigningService.SIGN_PDF_URI;
import static uk.gov.companieshouse.documentsigningrequestconsumer.ApiUtils.getLogMap;
import static uk.gov.companieshouse.documentsigningrequestconsumer.ApiUtils.mapMessageToRequest;
import static uk.gov.companieshouse.documentsigningrequestconsumer.MessageKeys.GROUP_ITEM;
import static uk.gov.companieshouse.documentsigningrequestconsumer.MessageKeys.ORDER_ID;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.documentsigning.SignPDFApi;
import uk.gov.companieshouse.api.model.documentsigning.SignPDFResponseApi;
import uk.gov.companieshouse.documentsigningrequestconsumer.ApiClientService;
import uk.gov.companieshouse.documentsigningrequestconsumer.RetryableException;
import uk.gov.companieshouse.documentsigningrequestconsumer.ServiceParameters;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.logging.Logger;

@Component
public class SignDocumentApiPost {
    private final EnvironmentReader environmentReader;
    private final ApiClientService apiClientService;
    private final Logger logger;

    public SignDocumentApiPost(EnvironmentReader environmentReader, ApiClientService apiClientService, Logger logger) {
        this.environmentReader = environmentReader;
        this.apiClientService = apiClientService;
        this.logger = logger;
    }
    /**
     * Extract the message to build request to sign document
     * @param parameters the fields present with the message
     * @throws RetryableException to attempt retry if something fails
     */
    public ApiResponse<SignPDFResponseApi> signDocument(ServiceParameters parameters)
        throws ApiErrorResponseException, URIValidationException {

        final String orderId = parameters.getData().get(ORDER_ID).toString();
        final String itemGroupId = parameters.getData().get(GROUP_ITEM).toString();
        final SignPDFApi requestBody = mapMessageToRequest(environmentReader, parameters);

        logger.info("Mapping parameters for document sign request ", getLogMap(orderId, itemGroupId));

        ApiResponse<SignPDFResponseApi> response = apiClientService
            .getInternalApiClient()
            .privateDocumentSigningResourceHandler()
            .signPDF(SIGN_PDF_URI, requestBody)
            .execute();

        logger.info("API returned response: "+ response.getStatusCode(), getLogMap(orderId, itemGroupId));

        return response;
    }
}
