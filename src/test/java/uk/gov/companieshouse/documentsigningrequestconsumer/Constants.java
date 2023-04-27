package uk.gov.companieshouse.documentsigningrequestconsumer;

import uk.gov.companieshouse.documentsigning.SignDigitalDocument;

public class Constants {

    private Constants() {
    }

    public static final SignDigitalDocument DOCUMENT = SignDigitalDocument.newBuilder()
            .setOrderNumber("1")
            .setPrivateS3Location("TODO")
            .setDocumentType("TODO")
            .setItemGroup("TODO")
            .build();

    public static final String SAME_PARTITION_KEY = "key";

}
