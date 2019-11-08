package uk.gov.moj.cpp.hearing.event.service;

public class EventMapping {

    private String cppEventCode;
    private String xhibitEventCode;

    public EventMapping(final String cppEventCode, final String xhibitEventCode) {
        this.cppEventCode = cppEventCode;
        this.xhibitEventCode = xhibitEventCode;
    }

    public String getCppEventCode() {
        return cppEventCode;
    }

    public String getXhibitEventCode() {
        return xhibitEventCode;
    }
}
