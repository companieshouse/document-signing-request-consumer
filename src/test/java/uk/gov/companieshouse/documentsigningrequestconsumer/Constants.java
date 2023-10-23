package uk.gov.companieshouse.documentsigningrequestconsumer;

import uk.gov.companieshouse.documentsigning.SignDigitalDocument;
import uk.gov.companieshouse.documentsigning.CoverSheetDataRecord;

import java.util.HashMap;
import java.util.Map;

public class Constants {

    private Constants() {
    }

    public static final CoverSheetDataRecord coverSheetData = CoverSheetDataRecord.newBuilder()
            .setCompanyName("Test Company")
            .setCompanyNumber("00000000")
            .setDescription("A test filing history document")
            .setType("AM01")
            .build();

    public static final Map<String, String> FILING_HISTORY_DESCRIPTION_VALUES  = new HashMap<String, String>() {{
            put("testKey1", "testValue1");
            put("testKey2", "testValue2");
    }};

    public static final SignDigitalDocument DOCUMENT = SignDigitalDocument.newBuilder()
            .setCoverSheetData(coverSheetData)
            .setOrderNumber("ORD-152416-079544")
            .setPrivateS3Location("s3://document-api-images-cidev/docs/--EdB7fbldt5oujK6Nz7jZ3hGj_x6vW8Q_2gQTyjWBM/application-pdf")
            .setDocumentType("363s")
            .setGroupItem("ORD-152416-079544-1")
            .setFilingHistoryDescriptionValues(FILING_HISTORY_DESCRIPTION_VALUES)
            .build();

    public static final String SAME_PARTITION_KEY = "key";

}
