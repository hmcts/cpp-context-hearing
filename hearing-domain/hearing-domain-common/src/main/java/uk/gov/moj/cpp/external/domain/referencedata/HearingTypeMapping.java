package uk.gov.moj.cpp.external.domain.referencedata;

import java.util.UUID;

public class HearingTypeMapping {

    private UUID id;
    private int seqId;
    private String hearingCode;
    private String hearingDescription;
    private String welshHearingDescription;
    private int defaultDurationMin;
    private String validFrom;
    private String validTo;
    private String exhibitHearingCode;
    private String exhibitHearingDescription;

    public HearingTypeMapping(final UUID id,
                              final int seqId,
                              final String hearingCode,
                              final String hearingDescription,
                              final String welshHearingDescription,
                              final int defaultDurationMin,
                              final String validFrom,
                              final String validTo,
                              final String exhibitHearingCode,
                              final String exhibitHearingDescription) {
        this.id = id;
        this.seqId = seqId;
        this.hearingCode = hearingCode;
        this.hearingDescription = hearingDescription;
        this.welshHearingDescription = welshHearingDescription;
        this.defaultDurationMin = defaultDurationMin;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.exhibitHearingCode = exhibitHearingCode;
        this.exhibitHearingDescription = exhibitHearingDescription;
    }

    public UUID getId() {
        return id;
    }

    public int getSeqId() {
        return seqId;
    }

    public String getHearingCode() {
        return hearingCode;
    }

    public String getHearingDescription() {
        return hearingDescription;
    }

    public String getWelshHearingDescription() {
        return welshHearingDescription;
    }

    public int getDefaultDurationMin() {
        return defaultDurationMin;
    }

    public String getValidFrom() {
        return validFrom;
    }

    public String getValidTo() {
        return validTo;
    }

    public String getExhibitHearingCode() {
        return exhibitHearingCode;
    }

    public String getExhibitHearingDescription() {
        return exhibitHearingDescription;
    }
}
