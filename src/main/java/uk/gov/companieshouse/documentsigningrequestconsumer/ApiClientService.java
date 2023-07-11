package uk.gov.companieshouse.documentsigningrequestconsumer;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.sdk.manager.ApiSdkManager;

@Component
public class ApiClientService {

    public InternalApiClient getInternalApiClient() {
        final var client = ApiSdkManager.getPrivateSDK();
        // TODO DCAC-151 Replace this Tilt-specific override with an environment variable.
        client.setInternalBasePath(client.getBasePath());
        return client;
    }
}
