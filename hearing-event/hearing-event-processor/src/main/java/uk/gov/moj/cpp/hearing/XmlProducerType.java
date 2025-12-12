package uk.gov.moj.cpp.hearing;

public enum XmlProducerType {
    PUBLIC_DISPLAY("CPPX_PublicDisplay_V1-0.xsd"),
    WEB_PAGE("CPPX_InternetWebPage_V1-0.xsd");

    private final String schemaName;

    XmlProducerType(final String schemaName) {
        this.schemaName = schemaName;
    }

    public String getSchemaName() {
        return schemaName;
    }
}