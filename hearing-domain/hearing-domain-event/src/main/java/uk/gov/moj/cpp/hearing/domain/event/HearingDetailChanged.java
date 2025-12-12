package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.justice.core.courts.CourtCentre;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.HearingLanguage;
import uk.gov.justice.core.courts.HearingType;
import uk.gov.justice.core.courts.JudicialRole;
import uk.gov.justice.core.courts.JurisdictionType;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.event.detail-changed")
public class HearingDetailChanged implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;
    private HearingType type;
    private CourtCentre courtCentre;
    private JurisdictionType jurisdictionType;
    private String reportingRestrictionReason;
    private HearingLanguage hearingLanguage;
    private List<HearingDay> hearingDays;
    private List<JudicialRole> judiciary;

    public HearingDetailChanged() {
    }

    @JsonCreator
    public HearingDetailChanged(@JsonProperty("id") final UUID id,
                                @JsonProperty("type") final HearingType type,
                                @JsonProperty("courtCentre") final CourtCentre courtCentre,
                                @JsonProperty("jurisdictionType") final JurisdictionType jurisdictionType,
                                @JsonProperty("reportingRestrictionReason") final String reportingRestrictionReason,
                                @JsonProperty("hearingLanguage") final HearingLanguage hearingLanguage,
                                @JsonProperty("hearingDays") final List<HearingDay> hearingDays,
                                @JsonProperty("judiciary") final List<JudicialRole> judiciary) {
        this.id = id;
        this.type = type;
        this.courtCentre = courtCentre;
        this.jurisdictionType = jurisdictionType;
        this.reportingRestrictionReason = reportingRestrictionReason;
        this.hearingLanguage = hearingLanguage;
        this.hearingDays = hearingDays;
        this.judiciary = judiciary;
    }

    public static HearingDetailChanged hearingDetailChanged() {
        return new HearingDetailChanged();
    }

    public UUID getId() {
        return id;
    }

    public HearingDetailChanged setId(UUID id) {
        this.id = id;
        return this;
    }

    public HearingType getType() {
        return type;
    }

    public HearingDetailChanged setType(HearingType type) {
        this.type = type;
        return this;
    }

    public CourtCentre getCourtCentre() {
        return courtCentre;
    }

    public HearingDetailChanged setCourtCentre(CourtCentre courtCentre) {
        this.courtCentre = courtCentre;
        return this;
    }

    public JurisdictionType getJurisdictionType() {
        return jurisdictionType;
    }

    public HearingDetailChanged setJurisdictionType(JurisdictionType jurisdictionType) {
        this.jurisdictionType = jurisdictionType;
        return this;
    }

    public String getReportingRestrictionReason() {
        return reportingRestrictionReason;
    }

    public HearingDetailChanged setReportingRestrictionReason(String reportingRestrictionReason) {
        this.reportingRestrictionReason = reportingRestrictionReason;
        return this;
    }

    public HearingLanguage getHearingLanguage() {
        return hearingLanguage;
    }

    public HearingDetailChanged setHearingLanguage(HearingLanguage hearingLanguage) {
        this.hearingLanguage = hearingLanguage;
        return this;
    }

    public List<HearingDay> getHearingDays() {
        return hearingDays;
    }

    public HearingDetailChanged setHearingDays(List<HearingDay> hearingDays) {
        this.hearingDays = hearingDays;
        return this;
    }

    public List<JudicialRole> getJudiciary() {
        return judiciary;
    }

    public HearingDetailChanged setJudiciary(List<JudicialRole> judiciary) {
        this.judiciary = judiciary;
        return this;
    }

}
