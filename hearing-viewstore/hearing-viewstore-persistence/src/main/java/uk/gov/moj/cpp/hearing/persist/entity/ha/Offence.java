package uk.gov.moj.cpp.hearing.persist.entity.ha;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "ha_offence")
@SuppressWarnings("squid:S2384")
public class Offence {

    @EmbeddedId
    private HearingSnapshotKey id;

    @Column(name = "offence_definition_id")
    private UUID offenceDefinitionId;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "defendant_id", insertable = false, updatable = false, referencedColumnName = "id"),
            @JoinColumn(name = "hearing_id", insertable = false, updatable = false, referencedColumnName = "hearing_id")})
    private Defendant defendant;

    @Column(name = "defendant_id")
    private UUID defendantId;

    @Column(name = "code")
    private String offenceCode;

    @Column(name = "title")
    private String offenceTitle;

    @Column(name = "title_welsh")
    private String offenceTitleWelsh;

    @Column(name = "legislation")
    private String offenceLegislation;

    @Column(name = "legislation_welsh")
    private String offenceLegislationWelsh;

    @Column(name = "wording")
    private String wording;

    @Column(name = "wording_welsh")
    private String wordingWelsh;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "arrest_date")
    private LocalDate arrestDate;

    @Column(name = "charge_date")
    private LocalDate chargeDate;

    @Column(name = "order_index")
    private Integer orderIndex;

    @Column(name = "count")
    private Integer count;

    @Column(name = "conviction_date")
    private LocalDate convictionDate;

    @Column(name = "ctl_days_spent")
    private Integer ctlDaysSpent;

    @Column(name = "ctl_time_limit")
    private LocalDate ctlTimeLimit;

    @Column(name = "proceedings_concluded")
    private boolean proceedingsConcluded;

    @Column(name = "is_discontinued")
    private boolean isDiscontinued;

    @Column(name = "shadow_listed")
    private boolean shadowListed;

    @Column(name = "is_introduce_after_initial_proceedings")
    private boolean isIntroduceAfterInitialProceedings;


    @Embedded
    private NotifiedPlea notifiedPlea;

    @Embedded
    private IndicatedPlea indicatedPlea;

    @Embedded
    private Plea plea;

    @Embedded
    private OffenceFacts offenceFacts;

    @Embedded
    private Verdict verdict;

    @Embedded
    private AllocationDecision allocationDecision;

    @Embedded
    private LaaApplnReference laaApplnReference;

    @Column(name = "mode_of_trial")
    private String modeOfTrial;

    @Column(name = "laid_date")
    private LocalDate laidDate;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "offence", orphanRemoval = true)
    private Set<ReportingRestriction> reportingRestrictions = new HashSet<>();

    public HearingSnapshotKey getId() {
        return id;
    }

    public void setId(HearingSnapshotKey id) {
        this.id = id;
    }

    public UUID getOffenceDefinitionId() {
        return offenceDefinitionId;
    }

    public void setOffenceDefinitionId(UUID offenceDefinitionId) {
        this.offenceDefinitionId = offenceDefinitionId;
    }

    public Defendant getDefendant() {
        return defendant;
    }

    public void setDefendant(Defendant defendant) {
        this.defendant = defendant;
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public void setDefendantId(UUID defendantId) {
        this.defendantId = defendantId;
    }

    public String getOffenceCode() {
        return offenceCode;
    }

    public void setOffenceCode(String offenceCode) {
        this.offenceCode = offenceCode;
    }

    public String getOffenceTitle() {
        return offenceTitle;
    }

    public void setOffenceTitle(String offenceTitle) {
        this.offenceTitle = offenceTitle;
    }

    public String getOffenceTitleWelsh() {
        return offenceTitleWelsh;
    }

    public void setOffenceTitleWelsh(String offenceTitleWelsh) {
        this.offenceTitleWelsh = offenceTitleWelsh;
    }

    public String getOffenceLegislation() {
        return offenceLegislation;
    }

    public void setOffenceLegislation(String offenceLegislation) {
        this.offenceLegislation = offenceLegislation;
    }

    public String getOffenceLegislationWelsh() {
        return offenceLegislationWelsh;
    }

    public void setOffenceLegislationWelsh(String offenceLegislationWelsh) {
        this.offenceLegislationWelsh = offenceLegislationWelsh;
    }

    public String getWording() {
        return wording;
    }

    public void setWording(String wording) {
        this.wording = wording;
    }

    public String getWordingWelsh() {
        return wordingWelsh;
    }

    public void setWordingWelsh(String wordingWelsh) {
        this.wordingWelsh = wordingWelsh;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public LocalDate getArrestDate() {
        return arrestDate;
    }

    public void setArrestDate(LocalDate arrestDate) {
        this.arrestDate = arrestDate;
    }

    public LocalDate getChargeDate() {
        return chargeDate;
    }

    public void setChargeDate(LocalDate chargeDate) {
        this.chargeDate = chargeDate;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public LocalDate getConvictionDate() {
        return convictionDate;
    }

    public void setConvictionDate(LocalDate convictionDate) {
        this.convictionDate = convictionDate;
    }

    public NotifiedPlea getNotifiedPlea() {
        return notifiedPlea;
    }

    public void setNotifiedPlea(NotifiedPlea notifiedPlea) {
        this.notifiedPlea = notifiedPlea;
    }

    public IndicatedPlea getIndicatedPlea() {
        return indicatedPlea;
    }

    public void setIndicatedPlea(IndicatedPlea indicatedPlea) {
        this.indicatedPlea = indicatedPlea;
    }

    public Plea getPlea() {
        return plea;
    }

    public void setPlea(Plea plea) {
        this.plea = plea;
    }

    public OffenceFacts getOffenceFacts() {
        return offenceFacts;
    }

    public void setOffenceFacts(OffenceFacts offenceFacts) {
        this.offenceFacts = offenceFacts;
    }

    public String getModeOfTrial() {
        return modeOfTrial;
    }

    public void setModeOfTrial(String modeOfTrial) {
        this.modeOfTrial = modeOfTrial;
    }

    public Verdict getVerdict() {
        return verdict;
    }

    public void setVerdict(Verdict verdict) {
        this.verdict = verdict;
    }

    public AllocationDecision getAllocationDecision() {
        return allocationDecision;
    }

    public void setAllocationDecision(final AllocationDecision allocationDecision) {
        this.allocationDecision = allocationDecision;
    }

    public LaaApplnReference getLaaApplnReference() {
        return laaApplnReference;
    }

    public void setLaaApplnReference(final LaaApplnReference laaApplnReference) {
        this.laaApplnReference = laaApplnReference;
    }

    public boolean isProceedingsConcluded() {
        return proceedingsConcluded;
    }

    public void setProceedingsConcluded(final boolean proceedingsConcluded) {
        this.proceedingsConcluded = proceedingsConcluded;
    }

    public boolean isDiscontinued() {
        return isDiscontinued;
    }

    public void setDiscontinued(final boolean discontinued) {
        isDiscontinued = discontinued;
    }

    public boolean isIntroduceAfterInitialProceedings() {
        return isIntroduceAfterInitialProceedings;
    }

    public void setIntroduceAfterInitialProceedings(final boolean introduceAfterInitialProceedings) {
        isIntroduceAfterInitialProceedings = introduceAfterInitialProceedings;
    }

    public Integer getCtlDaysSpent() {
        return ctlDaysSpent;
    }

    public void setCtlDaysSpent(Integer ctlDaysSpent) {
        this.ctlDaysSpent = ctlDaysSpent;
    }

    public LocalDate getCtlTimeLimit() {
        return ctlTimeLimit;
    }

    public void setCtlTimeLimit(LocalDate ctlTimeLimit) {
        this.ctlTimeLimit = ctlTimeLimit;
    }

    public LocalDate getLaidDate() {
        return laidDate;
    }

    public void setLaidDate(LocalDate laidDate) {
        this.laidDate = laidDate;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (null == o || getClass() != o.getClass()) {
            return false;
        }
        return Objects.equals(this.id, ((Offence) o).id);
    }

    public boolean isShadowListed() {
        return shadowListed;
    }

    public void setShadowListed(boolean shadowListed) {
        this.shadowListed = shadowListed;
    }

    public Set<ReportingRestriction> getReportingRestrictions() {
        return reportingRestrictions;
    }

    public void setReportingRestrictions(final Set<ReportingRestriction> reportingRestrictions) {
        this.reportingRestrictions = reportingRestrictions;
    }
}
