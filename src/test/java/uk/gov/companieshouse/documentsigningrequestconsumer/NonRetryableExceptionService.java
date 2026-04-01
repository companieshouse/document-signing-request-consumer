package uk.gov.companieshouse.documentsigningrequestconsumer;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.documentsigningrequestconsumer.exception.NonRetryableException;

/**
 * Test implementation of {@link DocumentService} that always throws a {@link NonRetryableException}.
 */
@Component("nonRetryableExceptionService")
class NonRetryableExceptionService implements DocumentService {

    @Override
    public void processMessage(ServiceParameters parameters) {
        throw new NonRetryableException("Unable to handle message");
    }
}