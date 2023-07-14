package uk.gov.companieshouse.documentsigningrequestconsumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.sdk.manager.ApiSdkManager;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ApiClientServiceTest {

    private static final String CONFIGURED_INTERNAL_API_URL = "http://api.chs.local:4001";

    private ApiClientService service;

    @Mock
    private InternalApiClient client;

    @BeforeEach
    void setUp() {
        service = new ApiClientService(CONFIGURED_INTERNAL_API_URL);
    }

    @Test
    @DisplayName("getInternalApiClient returns internal API client with an overridden internal API URL value")
    void getInternalApiClientOverridesInternalApiUrlUsed() {

        try (final MockedStatic<ApiSdkManager> sdkManager = Mockito.mockStatic(ApiSdkManager.class)) {
            sdkManager.when(ApiSdkManager::getPrivateSDK).thenReturn(client);

            service.getInternalApiClient();

            verify(client).setInternalBasePath(CONFIGURED_INTERNAL_API_URL);
        }

    }


}
