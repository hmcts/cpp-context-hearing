package uk.gov.moj.cpp.hearing.command.hearingDetails;

import com.fasterxml.jackson.annotation.JsonCreator;
import uk.gov.justice.json.schemas.core.CourtCentre;
import uk.gov.justice.json.schemas.core.HearingDay;
import uk.gov.justice.json.schemas.core.HearingLanguage;
import uk.gov.justice.json.schemas.core.HearingType;
import uk.gov.justice.json.schemas.core.JudicialRole;
import uk.gov.justice.json.schemas.core.JurisdictionType;
import java.util.List;
import java.util.UUID;

public class Hearing {

    private UUID id;

    private HearingType type;

    private CourtCentre courtCentre;

    private JurisdictionType jurisdictionType;

    private String reportingRestrictionReason;

    private HearingLanguage hearingLanguage;

    private List<HearingDay> hearingDays;

    private List<JudicialRole> judiciary;

    public Hearing() {
    }

    @JsonCreator
    public Hearing(UUID id, HearingType type, CourtCentre courtCentre, JurisdictionType jurisdictionType, String reportingRestrictionReason, List<HearingDay> hearingDays, List<JudicialRole> judiciary) {
        this.id = id;
        this.type = type;
        this.courtCentre = courtCentre;
        this.jurisdictionType = jurisdictionType;
        this.reportingRestrictionReason = reportingRestrictionReason;
        this.hearingDays = hearingDays;
        this.judiciary = judiciary;
    }

    public UUID getId() {
        return id;
    }

    public Hearing setId(UUID id) {
        this.id = id;
        return this;
    }

    public HearingType getType() {
        return type;
    }

    public Hearing setType(HearingType type) {
        this.type = type;
        return this;
    }

    public CourtCentre getCourtCentre() {
        return courtCentre;
    }

    public Hearing setCourtCentre(CourtCentre courtCentre) {
        this.courtCentre = courtCentre;
        return this;
    }

    public JurisdictionType getJurisdictionType() {
        return jurisdictionType;
    }

    public Hearing setJurisdictionType(JurisdictionType jurisdictionType) {
        this.jurisdictionType = jurisdictionType;
        return this;
    }

    public String getReportingRestrictionReason() {
        return reportingRestrictionReason;
    }

    public Hearing setReportingRestrictionReason(String reportingRestrictionReason) {
        this.reportingRestrictionReason = reportingRestrictionReason;
        return this;
    }

    public HearingLanguage getHearingLanguage() {
        return hearingLanguage;
    }

    public Hearing setHearingLanguage(HearingLanguage hearingLanguage) {
        this.hearingLanguage = hearingLanguage;
        return this;
    }

    public List<JudicialRole> getJudiciary() {
        return judiciary;
    }

    public Hearing setJudiciary(List<JudicialRole> judiciary) {
        this.judiciary = judiciary;
        return this;
    }

    public List<HearingDay> getHearingDays() {
        return hearingDays;
    }

    public Hearing setHearingDays(List<HearingDay> hearingDays) {
        this.hearingDays = hearingDays;
        return this;
    }

    public static Hearing hearing() {
        return new Hearing();
    }
}