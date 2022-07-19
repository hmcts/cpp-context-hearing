package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import uk.gov.justice.core.courts.AllocationDecision;
import uk.gov.justice.core.courts.ApplicantCounsel;
import uk.gov.justice.core.courts.CompanyRepresentative;
import uk.gov.justice.core.courts.DefenceCounsel;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.IndicatedPlea;
import uk.gov.justice.core.courts.InterpreterIntermediary;
import uk.gov.justice.core.courts.Plea;
import uk.gov.justice.core.courts.ProsecutionCounsel;
import uk.gov.justice.core.courts.RespondentCounsel;
import uk.gov.justice.core.courts.Target2;
import uk.gov.justice.core.courts.Verdict;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.Variant;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLineStatus;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static uk.gov.moj.cpp.util.DuplicateApplicationsHelper.dedupAllApplications;
import static uk.gov.moj.cpp.util.DuplicateOffencesHelper.filterDuplicateOffencesByIdForHearing;
import static uk.gov.moj.cpp.util.ReportingRestrictionHelper.dedupAllReportingRestrictions;

@SuppressWarnings("pmd:BeanMembersShouldSerialize")
public class HearingAggregateMomento implements Serializable {

    private static final long serialVersionUID = -561416825201569228L;

    private final Map<UUID, HearingEventDelegate.HearingEvent> hearingEvents = new HashMap<>();
    private final Map<UUID, ProsecutionCounsel> prosecutionCounsels = new HashMap<>();
    private final Map<UUID, ApplicantCounsel> applicantCounsels = new HashMap<>();
    private final Map<UUID, DefenceCounsel> defenceCounsels = new HashMap<>();
    private final Map<UUID, Plea> pleas = new HashMap<>();
    private final Map<UUID, IndicatedPlea> indicatedPleas = new HashMap<>();
    private final Map<UUID, AllocationDecision> allocationDecisions = new HashMap<>();
    private final Map<UUID, Verdict> verdicts = new HashMap<>();
    private final Map<UUID, RespondentCounsel> respondentCounsels = new HashMap<>();
    private final Map<UUID, CompanyRepresentative> companyRepresentatives = new HashMap<>();
    private final Map<UUID, InterpreterIntermediary> interpreterIntermediary = new HashMap<>();
    private Map<UUID, Target2> sharedTargets = new HashMap<>();
    private Map<UUID, Target2> transientTargets = new HashMap<>();

    private Hearing hearing;
    private List<Variant> variantDirectory = new ArrayList<>();
    private Map<UUID, LocalDate> convictionDates = new HashMap<>();
    private boolean published = false;
    private boolean duplicate = false;
    private ZonedDateTime lastSharedTime;
    private boolean deleted = false;
    private Map<UUID, ZonedDateTime> nextHearingStartDates = new HashMap<>();
    private Map<UUID, ZonedDateTime> resultsAmendmentDateMap = new HashMap<>();
    private Map<LocalDate, Map<UUID, Target2>> multiDayTargets = new HashMap<>();
    private Map<LocalDate, Map<UUID, Target2>> multiDaySavedTargets = new HashMap<>();
    private Map<LocalDate, Map<UUID, CompletedResultLineStatus>> multiDayCompletedResultLinesStatus = new HashMap<>();
    private Map<LocalDate, Boolean> isHearingDayPreviouslyShared = new HashMap<>();

    public Map<UUID, HearingEventDelegate.HearingEvent> getHearingEvents() {
        return hearingEvents;
    }

    public Map<UUID, ProsecutionCounsel> getProsecutionCounsels() {
        return prosecutionCounsels;
    }

    public Map<UUID, DefenceCounsel> getDefenceCounsels() {
        return defenceCounsels;
    }

    public Map<UUID, InterpreterIntermediary> getInterpreterIntermediary() {
        return interpreterIntermediary;
    }

    public Map<UUID, Plea> getPleas() {
        return pleas;
    }

    public Map<UUID, IndicatedPlea> getIndicatedPlea() {
        return indicatedPleas;
    }

    public Map<UUID, AllocationDecision> getAllocationDecision() {
        return allocationDecisions;
    }

    public Map<UUID, Verdict> getVerdicts() {
        return verdicts;
    }

    public Hearing getHearing() {
        return hearing;
    }

    public void setHearing(Hearing hearing) {
        this.hearing = dedupAllReportingRestrictions(hearing);
        this.hearing = dedupAllApplications(this.hearing);
        filterDuplicateOffencesByIdForHearing(this.hearing);
    }


    public List<Variant> getVariantDirectory() {
        return variantDirectory;
    }

    public void setVariantDirectory(Collection<Variant> variantDirectory) {
        this.variantDirectory = new ArrayList<>(variantDirectory);
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public Map<UUID, LocalDate> getConvictionDates() {
        return convictionDates;
    }

    public Map<UUID, RespondentCounsel> getRespondentCounsels() {
        return respondentCounsels;
    }

    public Map<UUID, ApplicantCounsel> getApplicantCounsels() {
        return applicantCounsels;
    }

    public Map<UUID, CompanyRepresentative> getCompanyRepresentatives() {
        return companyRepresentatives;
    }

    public boolean isDuplicate() {
        return this.duplicate;
    }

    public void setDuplicate(final boolean duplicate) {
        this.duplicate = duplicate;
    }

    public Map<UUID, Target2> getSharedTargets() {
        return this.sharedTargets;
    }

    public Map<UUID, Target2> getTransientTargets() {
        return this.transientTargets;
    }

    public void setSharedTargets(final Map<UUID, Target2> sharedTargets) {
        this.sharedTargets = sharedTargets;
    }

    public void setTransientTargets(final Map<UUID, Target2> transientTargets) {
        this.transientTargets = transientTargets;
    }

    public ZonedDateTime getLastSharedTime() {
        return this.lastSharedTime;
    }

    public void setLastSharedTime(ZonedDateTime lastSharedTime) {
        this.lastSharedTime = lastSharedTime;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(final boolean deleted) {
        this.deleted = deleted;
    }

    public Map<UUID, ZonedDateTime> getNextHearingStartDates() {
        return nextHearingStartDates;
    }

    public Map<UUID, ZonedDateTime> getResultsAmendmentDateMap() {
        return resultsAmendmentDateMap;
    }

    public Map<LocalDate, Map<UUID, Target2>> getMultiDaySavedTargets() {
        return multiDaySavedTargets;
    }

    public Map<LocalDate, Map<UUID, CompletedResultLineStatus>> getMultiDayCompletedResultLinesStatus() {
        return multiDayCompletedResultLinesStatus;
    }

    public Map<LocalDate, Boolean> getIsHearingDayPreviouslyShared() {
        return isHearingDayPreviouslyShared;
    }

    public Map<LocalDate, Map<UUID, Target2>> getMultiDayTargets() {
        return multiDayTargets;
    }

}