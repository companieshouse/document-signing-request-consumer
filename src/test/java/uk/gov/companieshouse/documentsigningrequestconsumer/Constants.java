package uk.gov.companieshouse.documentsigningrequestconsumer;

import uk.gov.companieshouse.documentsigning.SignDigitalDocument;

public class Constants {

    private Constants() {
    }

    public static final SignDigitalDocument DOCUMENT = SignDigitalDocument.newBuilder()
            .setOrderNumber("ORD-152416-079544")
            .setPrivateS3Location("s3://document-api-images-cidev/docs/--EdB7fbldt5oujK6Nz7jZ3hGj_x6vW8Q_2gQTyjWBM/application-pdf")
            .setDocumentType("363s")
            .setItemGroup("/item-groups/IG-954916-860369/items/111-222-333")
            .setCompanyName("Test Company")
            .setCompanyNumber("00000000")
            .setFilingHistoryDescription("A test filing history document")
            .setFilingHistoryType("AM01")
            .build();

    public static final String SAME_PARTITION_KEY = "key";

}
