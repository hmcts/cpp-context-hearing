package uk.gov.moj.cpp.external.domain.referencedata;

import java.util.UUID;

public class XhibitEventMapping {

    private UUID cpHearingEventId;
    private String xhibitHearingEventCode;
    private String xhibitHearingEventDescription;
    private String validFrom;
    private String validTo;

    public XhibitEventMapping(final UUID cpHearingEventId, final String xhibitHearingEventCode, final String xhibitHearingEventDescription, final String validFrom, final String validTo) {
        this.cpHearingEventId = cpHearingEventId;
        this.xhibitHearingEventCode = xhibitHearingEventCode;
        this.xhibitHearingEventDescription = xhibitHearingEventDescription;
        this.validFrom = validFrom;
        this.validTo = validTo;
    }

    public UUID getCpHearingEventId() {
        return cpHearingEventId;
    }

    public String getXhibitHearingEventCode() {
        return xhibitHearingEventCode;
    }

    public String getXhibitHearingEventDescription() {
        return xhibitHearingEventDescription;
    }

    public String getValidFrom() {
        return validFrom;
    }

    public String getValidTo() {
        return validTo;
    }
}
