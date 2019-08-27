package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import uk.gov.justice.core.courts.ApplicantCounsel;
import uk.gov.justice.core.courts.CompanyRepresentative;
import uk.gov.justice.core.courts.DefenceCounsel;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.Plea;
import uk.gov.justice.core.courts.ProsecutionCounsel;
import uk.gov.justice.core.courts.RespondentCounsel;
import uk.gov.justice.core.courts.Target;
import uk.gov.justice.core.courts.Verdict;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.Variant;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLineStatus;
import uk.gov.moj.cpp.hearing.nows.events.PendingNowsRequested;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HearingAggregateMomento implements Serializable {

    private static final long serialVersionUID = 2L;

    private final Map<UUID, HearingEventDelegate.HearingEvent> hearingEvents = new HashMap<>();
    private final Map<UUID, ProsecutionCounsel> prosecutionCounsels = new HashMap<>();
    private final Map<UUID, ApplicantCounsel> applicantCounsels = new HashMap<>();
    private final Map<UUID, DefenceCounsel> defenceCounsels = new HashMap<>();
    private final Map<UUID, Plea> pleas = new HashMap<>();
    private final Map<UUID, Verdict> verdicts = new HashMap<>();
    private final Map<UUID, CompletedResultLineStatus> completedResultLinesStatus = new HashMap<>();
    private final Map<UUID, RespondentCounsel> respondentCounsels = new HashMap<>();
    private final Map<UUID, CompanyRepresentative> companyRepresentatives = new HashMap<>();
    private Hearing hearing;
    private List<Variant> variantDirectory = new ArrayList<>();
    private List<UUID> adjournedHearingIds = new ArrayList<>();
    private Map<UUID, Target> targets = new HashMap<>();
    private Map<UUID, LocalDate> convictionDates = new HashMap<>();
    private List<PendingNowsRequested> hearingNowsMapper = new ArrayList<>();
    private Map<UUID, Target> savedTargets = new HashMap<>();
    private boolean published = false;

    public Map<UUID, HearingEventDelegate.HearingEvent> getHearingEvents() {
        return hearingEvents;
    }

    public Map<UUID, ProsecutionCounsel> getProsecutionCounsels() {
        return prosecutionCounsels;
    }

    public Map<UUID, DefenceCounsel> getDefenceCounsels() {
        return defenceCounsels;
    }

    public Map<UUID, Plea> getPleas() {
        return pleas;
    }

    public Map<UUID, Verdict> getVerdicts() {
        return verdicts;
    }

    public Hearing getHearing() {
        return hearing;
    }

    public void setHearing(Hearing hearing) {
        this.hearing = hearing;
    }

    public List<Variant> getVariantDirectory() {
        return variantDirectory;
    }

    public void setVariantDirectory(Collection<Variant> variantDirectory) {
        this.variantDirectory = new ArrayList<>(variantDirectory);
    }

    public Map<UUID, CompletedResultLineStatus> getCompletedResultLinesStatus() {
        return completedResultLinesStatus;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public List<UUID> getAdjournedHearingIds() {
        return adjournedHearingIds;
    }

    public void setAdjournedHearingIds(final List<UUID> adjournedHearingIds) {
        this.adjournedHearingIds = adjournedHearingIds;
    }

    public Map<UUID, Target> getTargets() {
        return targets;
    }

    public void setTargets(Map<UUID, Target> targets) {
        this.targets = targets;
    }

    public Map<UUID, LocalDate> getConvictionDates() {
        return convictionDates;
    }

    public void setConvictionDates(final Map<UUID, LocalDate> convictionDates) {
        this.convictionDates = convictionDates;
    }

    public List<PendingNowsRequested> getHearingNowsMapper() {
        return this.hearingNowsMapper;
    }

    public Map<UUID, RespondentCounsel> getRespondentCounsels() {
        return respondentCounsels;
    }

    public Map<UUID, ApplicantCounsel> getApplicantCounsels() {
        return applicantCounsels;
    }

    public Map<UUID, Target> getSavedTargets() {
        return savedTargets;
    }

    public void setSavedTargets(final Map<UUID, Target> savedTargets) {
        this.savedTargets.putAll(savedTargets);
    }

    public Map<UUID, CompanyRepresentative> getCompanyRepresentatives() {
        return companyRepresentatives;
    }
}