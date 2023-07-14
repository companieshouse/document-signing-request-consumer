package uk.gov.companieshouse.documentsigningrequestconsumer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.sdk.manager.ApiSdkManager;

@Component
public class ApiClientService {

    private final String internalApiUrl;

    public ApiClientService(@Value("${internal.api.url}") String internalApiUrl) {
        this.internalApiUrl = internalApiUrl;
    }

    public InternalApiClient getInternalApiClient() {
        final var client = ApiSdkManager.getPrivateSDK();
        client.setInternalBasePath(internalApiUrl);
        return client;
    }
}
