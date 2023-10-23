package uk.gov.companieshouse.documentsigningrequestconsumer;

import static java.util.Collections.singletonList;
import static uk.gov.companieshouse.documentsigningrequestconsumer.DocumentSigningService.APPLICATION_PDF;
import static uk.gov.companieshouse.documentsigningrequestconsumer.DocumentSigningService.COVERSHEET_OPTION;
import static uk.gov.companieshouse.documentsigningrequestconsumer.DocumentSigningService.PREFIX_ENV_VARIABLE;
import static uk.gov.companieshouse.documentsigningrequestconsumer.MessageKeys.COMPANY_NAME;
import static uk.gov.companieshouse.documentsigningrequestconsumer.MessageKeys.COMPANY_NUMBER;
import static uk.gov.companieshouse.documentsigningrequestconsumer.MessageKeys.DOCUMENT_TYPE;
import static uk.gov.companieshouse.documentsigningrequestconsumer.MessageKeys.FILING_HISTORY_DESCRIPTION;
import static uk.gov.companieshouse.documentsigningrequestconsumer.MessageKeys.FILING_HISTORY_TYPE;
import static uk.gov.companieshouse.documentsigningrequestconsumer.MessageKeys.PRIVATE_S3_LOCATION;
import org.apache.commons.lang.exception.ExceptionUtils;
import uk.gov.companieshouse.api.model.documentsigning.CoverSheetDataApi;
import uk.gov.companieshouse.api.model.documentsigning.SignPDFApi;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.logging.util.DataMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ApiUtils {
    public static SignPDFApi mapMessageToRequest(EnvironmentReader environmentReader, ServiceParameters parameters) {
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

    public static Map<String, Object> getLogMap(final String orderId, final String itemGroupId, final Throwable rootCause) {
        return new DataMap.Builder()
            .orderId(orderId)
            .itemGroupId(itemGroupId)
            .errors(singletonList(rootCause.getMessage()))
            .build()
            .getLogMap();
    }

    public static  Map<String, Object> getLogMap(final String orderId, final String itemGroupId) {
        return new DataMap.Builder()
            .orderId(orderId)
            .itemGroupId(itemGroupId)
            .build()
            .getLogMap();
    }

    public static  Throwable getRootCause(final Exception exception) {
        final var rootCause = ExceptionUtils.getRootCause(exception);
        return rootCause != null ? rootCause : exception;
    }
}
