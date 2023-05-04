package uk.gov.companieshouse.documentsigningrequestconsumer;

import org.springframework.stereotype.Component;

/**
 * Test implementation of {@link Service} that always throws a {@link NonRetryableException}.
 */
@Component
class NonRetryableExceptionService implements Service {

    @Override
    public void processMessage(ServiceParameters parameters) {
        throw new NonRetryableException("Unable to handle message");
    }
}