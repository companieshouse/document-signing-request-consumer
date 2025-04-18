package uk.gov.companieshouse.documentsigningrequestconsumer;

import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.environment.exception.EnvironmentVariableException;
import uk.gov.companieshouse.environment.impl.EnvironmentReaderImpl;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import static uk.gov.companieshouse.documentsigningrequestconsumer.DocumentSigningRequestConsumerApplication.APPLICATION_NAME_SPACE;

public class EnvironmentVariablesChecker {

    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAME_SPACE);

    public enum RequiredEnvironmentVariables {
        BACKOFF_DELAY("BACKOFF_DELAY"),
        BOOTSTRAP_SERVER_URL("BOOTSTRAP_SERVER_URL"),
        CONCURRENT_LISTENER_INSTANCES("CONCURRENT_LISTENER_INSTANCES"),
        GROUP_ID("GROUP_ID"),
        INVALID_MESSAGE_TOPIC("INVALID_MESSAGE_TOPIC"),
        MAX_ATTEMPTS("MAX_ATTEMPTS"),
        DOCUMENT_SIGNING_REQUEST_CONSUMER_PORT("DOCUMENT_SIGNING_REQUEST_CONSUMER_PORT"),
        TOPIC("TOPIC"),
        PREFIX("PREFIX"),
        API_URL("API_URL"),
        PAYMENTS_API_URL("PAYMENTS_API_URL"),
        CHS_API_KEY("CHS_API_KEY"),
        INTERNAL_API_URL("INTERNAL_API_URL");

        private final String name;

        RequiredEnvironmentVariables(String name) { this.name = name; }

        public String getName() { return this.name; }
    }

    /**
     * Method to check if all of the required configuration variables
     * defined in the RequiredEnvironmentVariables enum have been set to a value
     * @return <code>true</code> if all required environment variables have been set, <code>false</code> otherwise
     */
    public static boolean allRequiredEnvironmentVariablesPresent() {
        EnvironmentReader environmentReader = new EnvironmentReaderImpl();
        var allVariablesPresent = true;
        LOGGER.info("Checking all environment variables present");
        for(RequiredEnvironmentVariables param : RequiredEnvironmentVariables.values()) {
            try{
                environmentReader.getMandatoryString(param.getName());
            } catch (EnvironmentVariableException eve) {
                allVariablesPresent = false;
                LOGGER.error(String.format("Required config item %s missing", param.getName()));
            }
        }

        return allVariablesPresent;
    }
}
