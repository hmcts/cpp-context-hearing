package uk.gov.moj.cpp.hearing.persist.entity.ha;

import uk.gov.justice.json.schemas.core.HearingLanguage;
import uk.gov.justice.json.schemas.core.JurisdictionType;

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
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

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
    private Set<HearingDay> hearingDays = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "hearing", orphanRemoval = true)
    private Set<ProsecutionCase> prosecutionCases = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "hearing", orphanRemoval = true)
    private Set<DefendantReferralReason> defendantReferralReasons = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "hearing", orphanRemoval = true)
    private Set<JudicialRole> judicialRoles = new HashSet<>();

    @Column(name = "has_shared_results")
    private Boolean hasSharedResults;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "hearing", orphanRemoval = true)
    private Set<Target> targets = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "hearing", orphanRemoval = true)
    private Set<DefendantAttendance> defendantAttendance = new HashSet<>();

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

    public Set<HearingDay> getHearingDays() {
        return hearingDays;
    }

    public Hearing setHearingDays(Set<HearingDay> hearingDays) {
        this.hearingDays = hearingDays;
        return this;
    }

    public Set<ProsecutionCase> getProsecutionCases() {
        return prosecutionCases;
    }

    public Hearing setProsecutionCases(Set<ProsecutionCase> prosecutionCases) {
        this.prosecutionCases = prosecutionCases;
        return this;
    }

    public Set<DefendantReferralReason> getDefendantReferralReasons() {
        return defendantReferralReasons;
    }

    public Hearing setDefendantReferralReasons(Set<DefendantReferralReason> defendantReferralReasons) {
        this.defendantReferralReasons = defendantReferralReasons;
        return this;
    }

    public Set<JudicialRole> getJudicialRoles() {
        return judicialRoles;
    }

    public Hearing setJudicialRoles(Set<JudicialRole> judicialRoles) {
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

    public Set<Target> getTargets() {
        return targets;
    }


    public Hearing setTargets(Set<Target> targets) {
        this.targets = new HashSet<>(targets);
        return this;
    }

    public void setHearingCaseNotes(Set<HearingCaseNote> jpa) {
        // Will be covered by GPE-5922 story
        // TODO Auto-generated method stub
    }

    public Set<HearingCaseNote> getHearingCaseNotes() {
        // Will be covered by GPE-5922 story
        // TODO Auto-generated method stub
        return new HashSet<>();
    }

    public void setDefenceCounsels(Set<DefenceCounsel> jpa) {
        // The story to cover it doesn't exist yet.
        // TODO Auto-generated method stub
    }

    public Set<DefenceCounsel> getDefenceCounsels() {
        // The story to cover it doesn't exist yet.
        // TODO Auto-generated method stub
        return new HashSet<>();
    }

    public Hearing setDefendantAttendance(Set<DefendantAttendance> defendantAttendance) {
        this.defendantAttendance = defendantAttendance;
        return this;
    }

    public Set<DefendantAttendance> getDefendantAttendance() {
        return defendantAttendance;
    }

    public void setProsecutionCounsels(Set<ProsecutionCounsel> jpa) {
        // The story to cover it doesn't exist yet.
        // TODO Auto-generated method stub
    }

    public Set<ProsecutionCounsel> getProsecutionCounsels() {
        // The story to cover it doesn't exist yet.
        // TODO Auto-generated method stub
        return new HashSet<>();
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
}