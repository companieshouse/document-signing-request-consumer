package uk.gov.companieshouse.documentsigningrequestconsumer;

import org.springframework.stereotype.Component;
import org.springframework.web.util.UriTemplate;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.documentsigning.SignPDFApi;
import uk.gov.companieshouse.api.model.documentsigning.SignPDFResponseApi;

/**
 * Makes sign PDF requests to the Document Signing API.
 */
@Component
class DocumentSigningService implements Service {

    private final ApiClientService apiClientService;

    private static final String SIGN_PDF_URI = "/document-signing/sign-pdf";

    public DocumentSigningService(ApiClientService apiClientService) {
        this.apiClientService = apiClientService;
    }

    @Override
    public void processMessage(ServiceParameters parameters) {
        SignPDFApi requestBody = new SignPDFApi();
        final String itemGroup = parameters.getData().get("item_group").toString();
        final String orderNumber = parameters.getData().get("order_number").toString();

        requestBody.setDocumentLocation(parameters.getData().get("private_s3_location").toString());
        requestBody.setDocumentType(parameters.getData().get("document_type").toString());

//        TODO Currently undefined where these will come from
          requestBody.setKey("application-pdf");
          requestBody.setPrefix("cidev/certified-copy");
//          requestBody.setCoverSheetData("");
//        requestBody.setCoverSheetData();

        SignPDFResponseApi signPDFResponseApi;

        try {
            signPDFResponseApi = apiClientService.getInternalApiClient()
                            .privateDocumentSigningResourceHandler()
                            .signPDF(SIGN_PDF_URI, requestBody)
                            .execute().getData();
        } catch (ApiErrorResponseException | URIValidationException e) {
            throw new RuntimeException(e);
        }
    }
}