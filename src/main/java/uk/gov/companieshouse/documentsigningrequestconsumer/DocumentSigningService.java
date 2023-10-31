package uk.gov.companieshouse.documentsigningrequestconsumer;

import static com.fasterxml.jackson.databind.util.ClassUtil.getRootCause;
import static uk.gov.companieshouse.documentsigningrequestconsumer.ApiUtils.getLogMap;
import static uk.gov.companieshouse.documentsigningrequestconsumer.MessageKeys.ITEM_GROUP;
import static uk.gov.companieshouse.documentsigningrequestconsumer.MessageKeys.ORDER_ID;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.documentsigning.SignPDFResponseApi;
import uk.gov.companieshouse.documentsigningrequestconsumer.satisfyitem.SatisfyItemApiPatch;
import uk.gov.companieshouse.documentsigningrequestconsumer.signdocument.SignDocumentApiPost;
import uk.gov.companieshouse.logging.Logger;

/**
 * Makes sign PDF requests to Document Signing API.
 * Makes satisfy item requests to Item Group Workflow API.
 */
@Component
public class DocumentSigningService implements Service {
    public static final String SIGN_PDF_URI = "/document-signing/sign-pdf";
    public static final String COVERSHEET_OPTION = "cover-sheet";
    public static final String APPLICATION_PDF = "application-pdf";
    public static final String PREFIX_ENV_VARIABLE = "PREFIX";

    private final Logger logger;
    private final SignDocumentApiPost signDocumentApiPost;
    private final SatisfyItemApiPatch satisfyItemApiPatch;

    public DocumentSigningService(SignDocumentApiPost signDocumentApiPost, SatisfyItemApiPatch satisfyItemApiPatch, Logger logger) {
        this.signDocumentApiPost = signDocumentApiPost;
        this.satisfyItemApiPatch = satisfyItemApiPatch;
        this.logger = logger;
    }
    /**
     * Use Kafka message content to digitally sign a document. Update document status with SATISFIED.
     * @param parameters Kafka message
     * @throws RetryableException attempt retry if something retryable fails (ApiErrorResponseException, URIValidationException)
     * @throws NonRetryableException (Exception)
     */
    @Override
    public void processMessage(ServiceParameters parameters) {

        final String orderId = parameters.getData().get(ORDER_ID).toString();
        final String itemGroupId = parameters.getData().get(ITEM_GROUP).toString();

        try {
            //
            // Digitally sign the document.
            //
            final ApiResponse<SignPDFResponseApi> signDocResponse = signDocumentApiPost.signDocument(parameters);
            //
            // Send ItemGroupWorkflowAPI status of SATISFIED for 201, otherwise FAILED, for this ItemGroup+ItemID
            //
            satisfyItemApiPatch.satisfyItem(
                parameters,
                signDocResponse.getStatusCode(),
                signDocResponse.getData().getSignedDocumentLocation());

        } catch (ApiErrorResponseException apiException) {
            logger.error("Error response from INTERNAL API: " + apiException, getLogMap(orderId, itemGroupId, apiException));
            throw new RetryableException("Attempting retry due to failed response", apiException);
        } catch (URIValidationException uriException) {
            logger.error("Error with URI: " + uriException, getLogMap(orderId, itemGroupId, uriException));
            throw new RetryableException("Attempting retry due to URI validation error", uriException);
        } catch (Exception exception) {
            final var rootCause = getRootCause(exception);
            logger.error("NonRetryable Error: " + rootCause, getLogMap(orderId, itemGroupId, rootCause));
            throw new NonRetryableException("DocumentSigningService.processMessage: ", rootCause);
        }
    }
}