package uk.gov.moj.cpp.hearing;

public enum XmlProducerType {
    PUBLIC_DISPLAY("InternetPageCPP_PD.xsd"),
    WEB_PAGE("InternetPageCPP_IWP.xsd");

    private final String schemaName;

    XmlProducerType(final String schemaName) {
        this.schemaName = schemaName;
    }

    public String getSchemaName() {
        return schemaName;
    }
}