package uk.gov.moj.cpp.hearing.persist.entity.ha;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import uk.gov.justice.json.schemas.core.HearingLanguage;
import uk.gov.justice.json.schemas.core.JurisdictionType;

@Entity
@Table(name = "ha_hearing")
public class Hearing {

    @Embedded
    private CourtCentre courtCentre;

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Embedded
    private HearingType hearingType;

    @Column(name = "jurisdiction_type")
    @Enumerated(EnumType.STRING)
    private JurisdictionType jurisdictionType;

    @Column(name = "reporting_restriction_reason")
    private String reportingRestrictionReason;

    @Column(name = "hearing_language")
    @Enumerated(EnumType.STRING)
    private HearingLanguage hearingLanguage;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "hearing", orphanRemoval = true)
    private List<HearingDay> hearingDays = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "hearing", orphanRemoval = true)
    private List<ProsecutionCase> prosecutionCases = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "hearing", orphanRemoval = true)
    private List<DefendantReferralReason> defendantReferralReasons = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "hearing", orphanRemoval = true)
    private List<JudicialRole> judicialRoles = new ArrayList<>();

    @Column(name = "has_shared_results")
    private Boolean hasSharedResults;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "hearing", orphanRemoval = true)
    private List<Target> targets = new ArrayList<>();

    public Hearing() {
        //For JPA
    }

    public CourtCentre getCourtCentre() {
        return courtCentre;
    }

    public Hearing setCourtCentre(CourtCentre courtCentre) {
        this.courtCentre = courtCentre;
        return this;
    }

    public UUID getId() {
        return id;
    }

    public Hearing setId(UUID id) {
        this.id = id;
        return this;
    }

    public HearingType getHearingType() {
        return hearingType;
    }

    public Hearing setHearingType(HearingType hearingType) {
        this.hearingType = hearingType;
        return this;
    }

    public JurisdictionType getJurisdictionType() {
        return jurisdictionType;
    }

    public Hearing setJurisdictionType(JurisdictionType jurisdictionType2) {
        this.jurisdictionType = jurisdictionType2;
        return this;
    }

    public String getReportingRestrictionReason() {
        return reportingRestrictionReason;
    }

    public void setReportingRestrictionReason(String reportingRestrictionReason) {
        this.reportingRestrictionReason = reportingRestrictionReason;
    }

    public HearingLanguage getHearingLanguage() {
        return hearingLanguage;
    }

    public Hearing setHearingLanguage(HearingLanguage hearingLanguage2) {
        this.hearingLanguage = hearingLanguage2;
        return this;
    }

    public List<HearingDay> getHearingDays() {
        return hearingDays;
    }

    public Hearing setHearingDays(List<HearingDay> hearingDays) {
        this.hearingDays = hearingDays;
        return this;
    }

    public List<ProsecutionCase> getProsecutionCases() {
        return prosecutionCases;
    }

    public Hearing setProsecutionCases(List<ProsecutionCase> prosecutionCases) {
        this.prosecutionCases = prosecutionCases;
        return this;
    }

    public List<DefendantReferralReason> getDefendantReferralReasons() {
        return defendantReferralReasons;
    }

    public Hearing setDefendantReferralReasons(List<DefendantReferralReason> defendantReferralReasons) {
        this.defendantReferralReasons = defendantReferralReasons;
        return this;
    }

    public List<JudicialRole> getJudicialRoles() {
        return judicialRoles;
    }

    public Hearing setJudicialRoles(List<JudicialRole> judicialRoles) {
        this.judicialRoles = judicialRoles;
        return this;
    }

    public Boolean getHasSharedResults() {
        return hasSharedResults;
    }

    public Hearing setHasSharedResults(Boolean hasSharedResults) {
        this.hasSharedResults = hasSharedResults;
        return this;
    }

    public List<Target> getTargets() {
        return targets;
    }


    public Hearing setTargets(List<Target> targets) {
        this.targets = new ArrayList<>(targets);
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (null == o || getClass() != o.getClass()) {
            return false;
        }
        return Objects.equals(this.id, ((Hearing)o).id);
    }

    public void setHearingCaseNotes(List<HearingCaseNote> jpa) {
        // Will be covered by GPE-5922 story
        // TODO Auto-generated method stub
    }

    public List<HearingCaseNote> getHearingCaseNotes() {
        // Will be covered by GPE-5922 story
        // TODO Auto-generated method stub
        return new ArrayList<>();
    }

    public void setDefenceCounsels(List<DefenceCounsel> jpa) {
        // The story to cover it doesn't exist yet.
        // TODO Auto-generated method stub
    }

    public List<DefenceCounsel> getDefenceCounsels() {
        // The story to cover it doesn't exist yet.
        // TODO Auto-generated method stub
        return new ArrayList<>();
    }

    public void setDefendantAttendance(List<DefendantAttendance> jpa) {
        // Will be covered by GPE-5565 story
        // TODO Auto-generated method stub
        
    }

    public List<DefendantAttendance> getDefendantAttendance() {
        // Will be covered by GPE-5565 story
        // TODO Auto-generated method stub
        return new ArrayList<>();
    }

    public void setProsecutionCounsels(List<ProsecutionCounsel> jpa) {
        // The story to cover it doesn't exist yet.
        // TODO Auto-generated method stub
    }

    public List<ProsecutionCounsel> getProsecutionCounsels() {
        // The story to cover it doesn't exist yet.
        // TODO Auto-generated method stub
        return new ArrayList<>();
    }


}