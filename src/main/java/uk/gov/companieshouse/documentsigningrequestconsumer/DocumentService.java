package uk.gov.companieshouse.documentsigningrequestconsumer;

/**
 * Processes an incoming message.
 */
public interface DocumentService {

    /**
     * Processes an incoming message.
     *
     * @param parameters Any parameters required when processing the message.
     */
    void processMessage(ServiceParameters parameters);
}