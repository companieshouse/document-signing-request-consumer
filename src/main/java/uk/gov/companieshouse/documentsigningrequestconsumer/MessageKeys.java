package uk.gov.companieshouse.documentsigningrequestconsumer;

import uk.gov.companieshouse.documentsigning.SignDigitalDocument;

public class MessageKeys {
    private MessageKeys() {}

    /**
     * Kafka message keys for
     * {@link SignDigitalDocument} messages in the <code>sign-digital-document</code> topic.
     */
    public static final String ORDER_ID = "order_number";
    public static final String GROUP_ITEM = "group_item";
    public static final String ITEM_ID = "item_id";
    public static final String PRIVATE_S3_LOCATION = "private_s3_location";
    public static final String DOCUMENT_TYPE = "document_type";
    public static final String COMPANY_NAME = "company_name";
    public static final String COMPANY_NUMBER = "company_number";
    public static final String TYPE = "type";
    public static final String DESCRIPTION = "description";
    public static final String FILING_HISTORY_DESCRIPTION_VALUES = "filing_history_description_values";
}
