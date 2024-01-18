package uk.gov.companieshouse.documentsigningrequestconsumer.satisfyitem;

import static uk.gov.companieshouse.documentsigningrequestconsumer.ApiUtils.getLogMap;
import static uk.gov.companieshouse.documentsigningrequestconsumer.MessageKeys.GROUP_ITEM;
import static uk.gov.companieshouse.documentsigningrequestconsumer.MessageKeys.ORDER_ID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.satisfyitem.SatisfyItemApi;
import uk.gov.companieshouse.documentsigningrequestconsumer.ApiClientService;
import uk.gov.companieshouse.documentsigningrequestconsumer.RetryableException;
import uk.gov.companieshouse.documentsigningrequestconsumer.ServiceParameters;
import uk.gov.companieshouse.logging.Logger;

@Component
public class SatisfyItemApiPatch {
    private final ApiClientService apiClientService;
    private final Logger logger;

    public SatisfyItemApiPatch(ApiClientService apiClientService, Logger logger) {
        this.apiClientService = apiClientService;
        this.logger = logger;
    }
    /**
     * Update Item with status and document location using InternalAPI->patch to Item Group Workflow API.
     * @param parameters Kafka message payload
     * @param status status
     * @param documentLocation document location in S3
     * @throws RetryableException to attempt retry if something fails
     */
    public void satisfyItem(ServiceParameters parameters, int status, String documentLocation)
        throws ApiErrorResponseException, URIValidationException {

//        final String itemGroupsUri = parameters.getData().getGroupItem() + parameters.getData().getItemId();
        final String itemGroupsUri = getGroupItemExcludingItemId(parameters.getData().getGroupItem())
            + parameters.getData().getItemId();

        final Status documentStatus = (status == HttpStatus.CREATED.value()) ? Status.SATISFIED : Status.FAILED;
        final SatisfyItemApi satisfyItemApi = new SatisfyItemApi(documentStatus.toString(), documentLocation);
        //
        // API PATCH to ItemGroupWorkflowAPI->SatisfyItem
        //
        ApiResponse<Void> response = apiClientService
            .getInternalApiClient()
            .privateSatisfyItemResourceHandler()
            .satisfyItem(itemGroupsUri, satisfyItemApi)
            .execute();

        logger.info("API returned response: "+ response.getStatusCode(),
            getLogMap(parameters.getData().get(ORDER_ID).toString(), parameters.getData().get(GROUP_ITEM).toString()));
    }

    private String getGroupItemExcludingItemId (final String groupItem)
    {
        String groupItemExcludingItemId = "";
        int lastIndex = 0;

        if ((lastIndex = groupItem.lastIndexOf('/')) != -1) {
            groupItemExcludingItemId = groupItem.substring(0, lastIndex + 1);
        }

        return groupItemExcludingItemId;
    }
}