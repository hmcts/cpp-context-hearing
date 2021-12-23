package uk.gov.moj.cpp.hearing.domain.referencedata;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;



public class HearingType implements Serializable {


    private UUID id;


    private Integer seqId;


    private String hearingCode;


    private String hearingDescription;


    private String welshHearingDescription;


    private Integer defaultDurationMin;


    private LocalDate validFrom;


    private LocalDate validTo;


    private String exhibitHearingCode;


    private String exhibitHearingDescription;


    private Boolean trialTypeFlag;

    public HearingType() {
    }

    @SuppressWarnings("squid:S00107")
    public HearingType(final UUID id, final Integer seqId,
                       final String hearingCode,
                       final String hearingDescription,
                       final String welshHearingDescription,
                       final Integer defaultDurationMin,
                       final LocalDate validFrom,
                       final LocalDate validTo,
                       final String exhibitHearingCode,
                       final String exhibitHearingDescription,
                       final Boolean trialTypeFlag) {
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
        this.trialTypeFlag = trialTypeFlag;
    }

    public UUID getId() {
        return id;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public Integer getSeqId() {
        return seqId;
    }

    public void setSeqId(final Integer seqId) {
        this.seqId = seqId;
    }

    public String getHearingCode() {
        return hearingCode;
    }

    public void setHearingCode(final String hearingCode) {
        this.hearingCode = hearingCode;
    }

    public String getHearingDescription() {
        return hearingDescription;
    }

    public void setHearingDescription(final String hearingDescription) {
        this.hearingDescription = hearingDescription;
    }

    public String getWelshHearingDescription() { return welshHearingDescription; }

    public void setWelshHearingDescription(final String welshHearingDescription) {
        this.welshHearingDescription = welshHearingDescription;
    }

    public Integer getDefaultDurationMin() {
        return defaultDurationMin;
    }

    public void setDefaultDurationMin(final Integer defaultDurationMin) {
        this.defaultDurationMin = defaultDurationMin;
    }

    public LocalDate getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(final LocalDate validFrom) {
        this.validFrom = validFrom;
    }

    public LocalDate getValidTo() {
        return validTo;
    }

    public void setValidTo(final LocalDate validTo) {
        this.validTo = validTo;
    }

    public String getExhibitHearingCode() {
        return exhibitHearingCode;
    }

    public void setExhibitHearingCode(final String exhibitHearingCode) {
        this.exhibitHearingCode = exhibitHearingCode;
    }

    public String getExhibitHearingDescription() {
        return exhibitHearingDescription;
    }

    public void setExhibitHearingDescription(final String exhibitHearingDescription) {
        this.exhibitHearingDescription = exhibitHearingDescription;
    }

    public Boolean getTrialTypeFlag() { return trialTypeFlag; }

    public void setTrialTypeFlag(final Boolean trialTypeFlag) { this.trialTypeFlag = trialTypeFlag; }
}
