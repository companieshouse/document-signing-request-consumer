package uk.gov.companieshouse.documentsigningrequestconsumer;

import uk.gov.companieshouse.documentsigning.SignDigitalDocument;

public class MessageKeys {
    private MessageKeys() {}

    /**
     * Kafka message keys for
     * {@link SignDigitalDocument} messages in the <code>sign-digital-document</code> topic.
     */
    public static final String ORDER_ID = "order_number";
    public static final String ITEM_GROUP = "item_group";
    public static final String PRIVATE_S3_LOCATION = "private_s3_location";
    public static final String DOCUMENT_TYPE = "document_type";
    public static final String COMPANY_NAME = "company_name";
    public static final String COMPANY_NUMBER = "company_number";
    public static final String FILING_HISTORY_TYPE = "filing_history_type";
    public static final String FILING_HISTORY_DESCRIPTION = "filing_history_description";
}
