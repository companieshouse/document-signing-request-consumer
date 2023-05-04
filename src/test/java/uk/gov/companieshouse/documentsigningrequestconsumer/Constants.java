package uk.gov.companieshouse.documentsigningrequestconsumer;

import uk.gov.companieshouse.documentsigning.SignDigitalDocument;

public class Constants {

    private Constants() {
    }

    public static final SignDigitalDocument DOCUMENT = SignDigitalDocument.newBuilder()
            .setOrderNumber("ORD-152416-079544")
            .setPrivateS3Location("s3://document-api-images-cidev/docs/--EdB7fbldt5oujK6Nz7jZ3hGj_x6vW8Q_2gQTyjWBM/application-pdf")
            .setDocumentType("363s")
            .setItemGroup("ORD-152416-079544-1")
            .build();

    public static final String SAME_PARTITION_KEY = "key";

}
