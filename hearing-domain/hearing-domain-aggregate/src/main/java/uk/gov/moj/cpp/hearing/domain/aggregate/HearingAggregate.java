package uk.gov.moj.cpp.hearing.domain.aggregate;

import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.match;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.otherwiseDoNothing;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.when;

import uk.gov.justice.core.courts.ApplicantCounsel;
import uk.gov.justice.core.courts.AttendanceDay;
import uk.gov.justice.core.courts.CompanyRepresentative;
import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationOutcomeType;
import uk.gov.justice.core.courts.CourtApplicationResponse;
import uk.gov.justice.core.courts.CourtCentre;
import uk.gov.justice.core.courts.DefenceCounsel;
import uk.gov.justice.core.courts.DelegatedPowers;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.HearingLanguage;
import uk.gov.justice.core.courts.HearingType;
import uk.gov.justice.core.courts.InterpreterIntermediary;
import uk.gov.justice.core.courts.JudicialRole;
import uk.gov.justice.core.courts.JurisdictionType;
import uk.gov.justice.core.courts.Marker;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.Plea;
import uk.gov.justice.core.courts.PleaModel;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.ProsecutionCounsel;
import uk.gov.justice.core.courts.RespondentCounsel;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.justice.core.courts.Target;
import uk.gov.justice.core.courts.Verdict;
import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.moj.cpp.hearing.command.bookprovisional.ProvisionalHearingSlotInfo;
import uk.gov.moj.cpp.hearing.command.defendant.Defendant;
import uk.gov.moj.cpp.hearing.command.result.SharedResultLineId;
import uk.gov.moj.cpp.hearing.command.result.SharedResultsCommandResultLine;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.ApplicantCounselDelegate;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.ApplicationDelegate;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.CompanyRepresentativeDelegate;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.ConvictionDateDelegate;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.DefenceCounselDelegate;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.DefendantDelegate;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.HearingAggregateMomento;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.HearingDelegate;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.HearingEventDelegate;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.HearingTrialTypeDelegate;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.InterpreterIntermediaryDelegate;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.OffenceDelegate;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.PleaDelegate;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.ProsecutionCaseDelegate;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.ProsecutionCounselDelegate;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.RespondentCounselDelegate;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.ResultsSharedDelegate;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.VariantDirectoryDelegate;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.VerdictDelegate;
import uk.gov.moj.cpp.hearing.domain.event.ApplicantCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.ApplicantCounselRemoved;
import uk.gov.moj.cpp.hearing.domain.event.ApplicantCounselUpdated;
import uk.gov.moj.cpp.hearing.domain.event.ApplicationDetailChanged;
import uk.gov.moj.cpp.hearing.domain.event.BookProvisionalHearingSlots;
import uk.gov.moj.cpp.hearing.domain.event.CaseDefendantsUpdatedForHearing;
import uk.gov.moj.cpp.hearing.domain.event.CaseMarkersUpdated;
import uk.gov.moj.cpp.hearing.domain.event.CompanyRepresentativeAdded;
import uk.gov.moj.cpp.hearing.domain.event.CompanyRepresentativeRemoved;
import uk.gov.moj.cpp.hearing.domain.event.CompanyRepresentativeUpdated;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateAdded;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateRemoved;
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselRemoved;
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselUpdated;
import uk.gov.moj.cpp.hearing.domain.event.DefendantAdded;
import uk.gov.moj.cpp.hearing.domain.event.DefendantAttendanceUpdated;
import uk.gov.moj.cpp.hearing.domain.event.DefendantDetailsUpdated;
import uk.gov.moj.cpp.hearing.domain.event.DefendantLegalAidStatusUpdatedForHearing;
import uk.gov.moj.cpp.hearing.domain.event.HearingDetailChanged;
import uk.gov.moj.cpp.hearing.domain.event.HearingEffectiveTrial;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventDeleted;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventLogged;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventVacatedTrialCleared;
import uk.gov.moj.cpp.hearing.domain.event.HearingExtended;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.domain.event.HearingTrialType;
import uk.gov.moj.cpp.hearing.domain.event.HearingTrialVacated;
import uk.gov.moj.cpp.hearing.domain.event.InheritedPlea;
import uk.gov.moj.cpp.hearing.domain.event.InheritedVerdictAdded;
import uk.gov.moj.cpp.hearing.domain.event.InterpreterIntermediaryAdded;
import uk.gov.moj.cpp.hearing.domain.event.InterpreterIntermediaryRemoved;
import uk.gov.moj.cpp.hearing.domain.event.InterpreterIntermediaryUpdated;
import uk.gov.moj.cpp.hearing.domain.event.NowsVariantsSavedEvent;
import uk.gov.moj.cpp.hearing.domain.event.OffenceAdded;
import uk.gov.moj.cpp.hearing.domain.event.OffenceDeleted;
import uk.gov.moj.cpp.hearing.domain.event.OffenceUpdated;
import uk.gov.moj.cpp.hearing.domain.event.PleaUpsert;
import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselRemoved;
import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselUpdated;
import uk.gov.moj.cpp.hearing.domain.event.RespondentCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.RespondentCounselRemoved;
import uk.gov.moj.cpp.hearing.domain.event.RespondentCounselUpdated;
import uk.gov.moj.cpp.hearing.domain.event.VerdictUpsert;
import uk.gov.moj.cpp.hearing.domain.event.application.ApplicationResponseSaved;
import uk.gov.moj.cpp.hearing.domain.event.result.ApplicationDraftResulted;
import uk.gov.moj.cpp.hearing.domain.event.result.DraftResultSaved;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultLinesStatusUpdated;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.eventlog.HearingEvent;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings({"squid:S00107", "squid:S1602", "squid:S1188", "squid:S1612", "pmd:BeanMembersShouldSerialize"})
public class HearingAggregate implements Aggregate {

    private static final long serialVersionUID = 9L;

    private static final String RECORDED_LABEL_HEARING_END = "Hearing ended";

    private final HearingAggregateMomento momento = new HearingAggregateMomento();

    private final HearingDelegate hearingDelegate = new HearingDelegate(momento);

    private final PleaDelegate pleaDelegate = new PleaDelegate(momento);

    private final ProsecutionCounselDelegate prosecutionCounselDelegate = new ProsecutionCounselDelegate(momento);

    private final DefenceCounselDelegate defenceCounselDelegate = new DefenceCounselDelegate(momento);

    private final HearingEventDelegate hearingEventDelegate = new HearingEventDelegate(momento);

    private final VerdictDelegate verdictDelegate = new VerdictDelegate(momento);

    private final ResultsSharedDelegate resultsSharedDelegate = new ResultsSharedDelegate(momento);

    private final ConvictionDateDelegate convictionDateDelegate = new ConvictionDateDelegate(momento);

    private final DefendantDelegate defendantDelegate = new DefendantDelegate(momento);

    private final OffenceDelegate offenceDelegate = new OffenceDelegate(momento);

    private final VariantDirectoryDelegate variantDirectoryDelegate = new VariantDirectoryDelegate(momento);

    private final ApplicationDelegate applicationDelegate = new ApplicationDelegate(momento);

    private final RespondentCounselDelegate respondentCounselDelegate = new RespondentCounselDelegate(momento);

    private final ApplicantCounselDelegate applicantCounselDelegate = new ApplicantCounselDelegate(momento);

    private final InterpreterIntermediaryDelegate interpreterIntermediaryDelegate = new InterpreterIntermediaryDelegate(momento);

    private final HearingTrialTypeDelegate hearingTrialTypeDelegate = new HearingTrialTypeDelegate(momento);

    private final CompanyRepresentativeDelegate companyRepresentativeDelegate = new CompanyRepresentativeDelegate(momento);

    private final ProsecutionCaseDelegate prosecutionCaseDelegate = new ProsecutionCaseDelegate(momento);

    @Override
    public Object apply(final Object event) {
        return match(event).with(
                when(HearingInitiated.class).apply(hearingDelegate::handleHearingInitiated),
                when(HearingExtended.class).apply(hearingDelegate::handleHearingExtended),
                when(HearingDetailChanged.class).apply(hearingDelegate::handleHearingDetailChanged),
                when(InheritedPlea.class).apply(pleaDelegate::handleInheritedPlea),
                when(PleaUpsert.class).apply(pleaDelegate::handlePleaUpsert),
                when(ProsecutionCounselAdded.class).apply(prosecutionCounselDelegate::handleProsecutionCounselAdded),
                when(ProsecutionCounselRemoved.class).apply(prosecutionCounselDelegate::handleProsecutionCounselRemoved),
                when(ProsecutionCounselUpdated.class).apply(prosecutionCounselDelegate::handleProsecutionCounselUpdated),
                when(DefenceCounselAdded.class).apply(defenceCounselDelegate::handleDefenceCounselAdded),
                when(DefenceCounselRemoved.class).apply(defenceCounselDelegate::handleDefenceCounselRemoved),
                when(DefenceCounselUpdated.class).apply(defenceCounselDelegate::handleDefenceCounselUpdated),
                when(HearingEventLogged.class).apply(hearingEventDelegate::handleHearingEventLogged),
                when(HearingEventDeleted.class).apply(hearingEventDelegate::handleHearingEventDeleted),
                when(ResultsShared.class).apply(e -> {
                            resultsSharedDelegate.handleResultsShared(e);
                            defendantDelegate.clearDefendantDetailsChanged();
                        }
                ),
                when(ResultLinesStatusUpdated.class).apply(resultsSharedDelegate::handleResultLinesStatusUpdated),
                when(InheritedVerdictAdded.class).apply(verdictDelegate::handleInheritedVerdict),
                when(VerdictUpsert.class).apply(verdictDelegate::handleVerdictUpsert),
                when(ConvictionDateAdded.class).apply(convictionDateDelegate::handleConvictionDateAdded),
                when(ConvictionDateRemoved.class).apply(convictionDateDelegate::handleConvictionDateRemoved),
                when(DefendantDetailsUpdated.class).apply(defendantDelegate::handleDefendantDetailsUpdated),
                when(OffenceAdded.class).apply(offenceDelegate::handleOffenceAdded),
                when(OffenceUpdated.class).apply(offenceDelegate::handleOffenceUpdated),
                when(OffenceDeleted.class).apply(offenceDelegate::handleOffenceDeleted),
                when(NowsVariantsSavedEvent.class).apply(variantDirectoryDelegate::handleNowsVariantsSavedEvent),
                when(DraftResultSaved.class).apply(resultsSharedDelegate::handleDraftResultSaved),
                when(ApplicationDraftResulted.class).apply(resultsSharedDelegate::handleApplicationDraftResulted),
                when(DefendantAttendanceUpdated.class).apply(defendantDelegate::handleDefendantAttendanceUpdated),
                when(ApplicationResponseSaved.class).apply(applicationDelegate::handleApplicationResponseSaved),
                when(RespondentCounselAdded.class).apply(respondentCounselDelegate::handleRespondentCounselAdded),
                when(RespondentCounselRemoved.class).apply(respondentCounselDelegate::handleRespondentCounselRemoved),
                when(RespondentCounselUpdated.class).apply(respondentCounselDelegate::handleRespondentCounselUpdated),
                when(ApplicantCounselAdded.class).apply(applicantCounselDelegate::handleApplicantCounselAdded),
                when(ApplicantCounselRemoved.class).apply(applicantCounselDelegate::handleApplicantCounselRemoved),
                when(ApplicantCounselUpdated.class).apply(applicantCounselDelegate::handleApplicantCounselUpdated),
                when(DefendantAdded.class).apply(hearingDelegate::handleDefendantAdded),
                when(ApplicationDetailChanged.class).apply(hearingDelegate::handleApplicationDetailChanged),
                when(InterpreterIntermediaryAdded.class).apply(interpreterIntermediaryDelegate::handleInterpreterIntermediaryAdded),
                when(InterpreterIntermediaryRemoved.class).apply(interpreterIntermediaryDelegate::handleInterpreterIntermediaryRemoved),
                when(InterpreterIntermediaryUpdated.class).apply(interpreterIntermediaryDelegate::handleInterpreterIntermediaryUpdated),
                when(HearingTrialType.class).apply(hearingTrialTypeDelegate::handleTrialTypeSetForHearing),
                when(HearingEffectiveTrial.class).apply(hearingTrialTypeDelegate::handleEffectiveTrailHearing),
                when(HearingTrialVacated.class).apply(hearingTrialTypeDelegate::handleVacateTrialTypeSetForHearing),
                when(CompanyRepresentativeAdded.class).apply(companyRepresentativeDelegate::handleCompanyRepresentativeAdded),
                when(CompanyRepresentativeUpdated.class).apply(companyRepresentativeDelegate::handleCompanyRepresentativeUpdated),
                when(CompanyRepresentativeRemoved.class).apply(companyRepresentativeDelegate::handleCompanyRepresentativeRemoved),
                when(CaseMarkersUpdated.class).apply(prosecutionCaseDelegate::handleCaseMarkersUpdated),
                when(DefendantLegalAidStatusUpdatedForHearing.class).apply(prosecutionCaseDelegate::onDefendantLegalaidStatusTobeUpdatedForHearing),
                when(CaseDefendantsUpdatedForHearing.class).apply(prosecutionCaseDelegate::onCaseDefendantUpdatedForHearing),
                when(HearingEventVacatedTrialCleared.class).apply(hearingEventVacatedTrialCleared -> hearingDelegate.handleVacatedTrialCleared()),
                otherwiseDoNothing()
        );

    }

    public Stream<Object> addProsecutionCounsel(final ProsecutionCounsel prosecutionCounsel, final UUID hearingId) {
        return apply(prosecutionCounselDelegate.addProsecutionCounsel(prosecutionCounsel, hearingId, hasHearingEnded()));
    }

    public Stream<Object> removeProsecutionCounsel(final UUID id, final UUID hearingId) {
        return apply(prosecutionCounselDelegate.removeProsecutionCounsel(id, hearingId));
    }

    public Stream<Object> updateProsecutionCounsel(final ProsecutionCounsel prosecutionCounsel, final UUID hearingId) {
        return apply(prosecutionCounselDelegate.updateProsecutionCounsel(prosecutionCounsel, hearingId));
    }

    public Stream<Object> addDefenceCounsel(final DefenceCounsel defenceCounsel, final UUID hearingId) {
        return apply(defenceCounselDelegate.addDefenceCounsel(defenceCounsel, hearingId, hasHearingEnded()));
    }

    public Stream<Object> removeDefenceCounsel(final UUID id, final UUID hearingId) {
        return apply(defenceCounselDelegate.removeDefenceCounsel(id, hearingId));
    }


    public Stream<Object> updateDefenceCounsel(final DefenceCounsel defenceCounsel, final UUID hearingId) {
        return apply(defenceCounselDelegate.updateDefenceCounsel(defenceCounsel, hearingId));
    }

    public Stream<Object> initiate(final Hearing hearing) {
        if (hearing.getHasSharedResults() == null) {
            hearing.setHasSharedResults(false);
        }

        //check if offence count is missing for crown court hearing
        if (JurisdictionType.CROWN.equals(hearing.getJurisdictionType()) && hearing.getProsecutionCases() != null) {
            final List<Offence> offences = this.hearingDelegate.getAllOffencesMissingCount(hearing);
            if (!offences.isEmpty()) {
                return apply(this.hearingDelegate.ignoreHearingInitiate(offences, hearing.getId()));
            }
        }
        return apply(this.hearingDelegate.initiate(hearing));
    }

    public Stream<Object> extend(final UUID hearingId, final CourtApplication courtApplication, final List<ProsecutionCase> prosecutionCases, final List<UUID> shadowListedOffences) {
        return apply(this.hearingDelegate.extend(hearingId, courtApplication, prosecutionCases, shadowListedOffences));
    }

    public Stream<Object> updatePlea(final UUID hearingId, final PleaModel plea, final Set<String> guiltyPleaTypes) {
        return apply(pleaDelegate.updatePlea(hearingId, plea, guiltyPleaTypes));
    }

    public Stream<Object> inheritPlea(final UUID hearingId, final Plea plea) {
        return apply(this.pleaDelegate.inheritPlea(hearingId, plea));
    }

    public Stream<Object> logHearingEvent(final UUID hearingId, final UUID hearingEventDefinitionId, final Boolean alterable, final UUID defenceCounselId, final HearingEvent hearingEvent) {
        return apply(this.hearingEventDelegate.logHearingEvent(hearingId, hearingEventDefinitionId, alterable, defenceCounselId, hearingEvent));
    }

    public Stream<Object> updateHearingEvents(final UUID hearingId, final List<uk.gov.moj.cpp.hearing.command.updateEvent.HearingEvent> hearingEvents) {
        return this.apply(this.hearingEventDelegate.updateHearingEvents(hearingId, hearingEvents));
    }

    public Stream<Object> correctHearingEvent(final UUID latestHearingEventId, final UUID hearingId, final UUID hearingEventDefinitionId, final Boolean alterable, final UUID defenceCounselId, final HearingEvent hearingEvent) {
        return apply(this.hearingEventDelegate.correctHearingEvent(latestHearingEventId, hearingId, hearingEventDefinitionId, alterable, defenceCounselId, hearingEvent));
    }

    public Stream<Object> updateHearingDetails(final UUID id,
                                               final HearingType type,
                                               final CourtCentre courtCentre,
                                               final JurisdictionType jurisdictionType,
                                               final String reportingRestrictionReason,
                                               final HearingLanguage hearingLanguage,
                                               final List<HearingDay> hearingDays,
                                               final List<JudicialRole> judiciary) {
        return apply(this.hearingDelegate.updateHearingDetails(id, type, courtCentre, jurisdictionType, reportingRestrictionReason, hearingLanguage, hearingDays, judiciary));
    }

    public Stream<Object> updateHearingVacateTrialDetails(final UUID hearingId,
                                                          final Boolean isVacated,
                                                          final UUID vacatedTrialReasonId) {
        return apply(this.hearingDelegate.updateHearingVacateTrialDetails(hearingId, isVacated, vacatedTrialReasonId));
    }

    public Stream<Object> clearVacatedTrial(final UUID id) {
        return apply(this.hearingDelegate.clearVacatedTrial(id));
    }

    public Stream<Object> updateVerdict(final UUID hearingId, final Verdict verdict, final Set<String> guiltyPleaTypes) {
        return apply(this.verdictDelegate.updateVerdict(hearingId, verdict, guiltyPleaTypes));
    }

    public Stream<Object> shareResults(final UUID hearingId, final DelegatedPowers courtClerk, final ZonedDateTime sharedTime, final List<SharedResultsCommandResultLine> resultLines) {
        return apply(resultsSharedDelegate.shareResults(hearingId, courtClerk, sharedTime, resultLines, this.defendantDelegate.getDefendantDetailsChanged()));
    }

    public Stream<Object> saveDraftResults(final UUID applicationId, final Target target, final UUID defendantId, final UUID hearingId, final UUID offenceId, final String draftResult, final List<ResultLine> resultLines) {

        final Target targetForEvent = Target
                .target()
                .withShadowListed(target.getShadowListed())
                .withApplicationId(applicationId)
                .withDefendantId(defendantId)
                .withDraftResult(draftResult)
                .withHearingId(hearingId)
                .withOffenceId(offenceId)
                .withResultLines(resultLines)
                .withTargetId(target.getTargetId())
                .build();

        // Fix to ensure that no extra target IDs are created for the same combination of offence and defendant.
        // The aggregate ensures that any extra target ID for the same combination of offence / defendant is rejected and not processed
        if (isTargetValid(momento, target)) {
            return apply(resultsSharedDelegate.saveDraftResult(targetForEvent));
        }
        return apply(resultsSharedDelegate.rejectSaveDraftResult(targetForEvent));


    }

    public Stream<Object> applicationDraftResults(final UUID targetId, final UUID applicationId, final UUID hearingId, final String draftResult, final CourtApplicationOutcomeType applicationOutcomeType, final LocalDate applicationOutcomeDate) {
        return apply(resultsSharedDelegate.applicationDraftResult(targetId, applicationId, hearingId, draftResult, applicationOutcomeType, applicationOutcomeDate));
    }

    public Stream<Object> updateResultLinesStatus(final UUID hearingId, final DelegatedPowers courtClerk, final ZonedDateTime lastSharedDateTime, final List<SharedResultLineId> sharedResultLines) {
        return apply(resultsSharedDelegate.updateResultLinesStatus(hearingId, courtClerk, lastSharedDateTime, sharedResultLines));
    }

    public Stream<Object> updateDefendantDetails(final UUID hearingId, final Defendant defendant) {
        return apply(this.defendantDelegate.updateDefendantDetails(hearingId, defendant));
    }

    public Stream<Object> addOffence(final UUID hearingId, final UUID defendantId, final UUID prosecutionCaseId, final Offence offence) {
        return apply(this.offenceDelegate.addOffence(hearingId, defendantId, prosecutionCaseId, offence));
    }

    public Stream<Object> updateOffence(final UUID hearingId, final UUID defendantId, final Offence offence) {
        return apply(this.offenceDelegate.updateOffence(hearingId, defendantId, offence));
    }

    public Stream<Object> deleteOffence(final UUID offenceId, final UUID hearingId) {
        return apply(this.offenceDelegate.deleteOffence(offenceId, hearingId));
    }

    public Stream<Object> updateDefendantAttendance(final UUID hearingId, final UUID defendantId, final AttendanceDay attendanceDay) {
        return apply(this.defendantDelegate.updateDefendantAttendance(hearingId, defendantId, attendanceDay));
    }

    public Stream<Object> inheritVerdict(final UUID hearingId, final Verdict verdict) {
        return apply(this.verdictDelegate.inheritVerdict(hearingId, verdict));
    }

    public Stream<Object> courtApplicationResponse(final UUID applicationPartyId, final CourtApplicationResponse courtApplicationResponse) {
        return apply(applicationDelegate.applicationResponseSaved(applicationPartyId, courtApplicationResponse));
    }

    public Stream<Object> addRespondentCounsel(final RespondentCounsel respondentCounsel, final UUID hearingId) {
        return apply(respondentCounselDelegate.addRespondentCounsel(respondentCounsel, hearingId));
    }

    public Stream<Object> removeRespondentCounsel(final UUID id, final UUID hearingId) {
        return apply(respondentCounselDelegate.removeRespondentCounsel(id, hearingId));
    }

    public Stream<Object> updateRespondentCounsel(final RespondentCounsel respondentCounsel, final UUID hearingId) {
        return apply(respondentCounselDelegate.updateRespondentCounsel(respondentCounsel, hearingId));
    }

    public Stream<Object> addApplicantCounsel(final ApplicantCounsel applicantCounsel, final UUID hearingId) {
        return apply(applicantCounselDelegate.addApplicantCounsel(applicantCounsel, hearingId));
    }

    public Stream<Object> removeApplicantCounsel(final UUID id, final UUID hearingId) {
        return apply(applicantCounselDelegate.removeApplicantCounsel(id, hearingId));
    }

    public Stream<Object> updateApplicantCounsel(final ApplicantCounsel applicantCounsel, final UUID hearingId) {
        return apply(applicantCounselDelegate.updateApplicantCounsel(applicantCounsel, hearingId));
    }

    public Stream<Object> addDefendant(final UUID hearingId, final uk.gov.justice.core.courts.Defendant defendant) {
        return hearingDelegate.addDefendant(hearingId, defendant);
    }

    public Stream<Object> updateCourtApplication(final UUID hearingId, final uk.gov.justice.core.courts.CourtApplication courtApplication) {
        return hearingDelegate.updateCourtApplication(hearingId, courtApplication);
    }

    public Stream<Object> addInterpreterIntermediary(final UUID hearingId, final InterpreterIntermediary interpreterIntermediary) {
        return interpreterIntermediaryDelegate.addInterpreterIntermediary(hearingId, interpreterIntermediary);
    }

    public Stream<Object> removeInterpreterIntermediary(final UUID id, final UUID hearingId) {
        return interpreterIntermediaryDelegate.removeInterpreterIntermediary(id, hearingId);
    }

    public Stream<Object> updateInterpreterIntermediary(final UUID hearingId, final InterpreterIntermediary interpreterIntermediary) {
        return interpreterIntermediaryDelegate.updateInterpreterIntermediary(interpreterIntermediary, hearingId);
    }

    public Stream<Object> setTrialType(final HearingTrialType trialType) {
        return apply(this.hearingTrialTypeDelegate.setTrialType(trialType));
    }

    public Stream<Object> setTrialType(final HearingEffectiveTrial hearingEffectiveTrial) {
        return apply(this.hearingTrialTypeDelegate.setTrialType(hearingEffectiveTrial));
    }

    public Stream<Object> setTrialType(final HearingTrialVacated trialType) {
        return apply(this.hearingTrialTypeDelegate.setTrialType(trialType));
    }

    public Stream<Object> addCompanyRepresentative(final CompanyRepresentative companyRepresentative, final UUID hearingId) {
        return apply(companyRepresentativeDelegate.addCompanyRepresentative(companyRepresentative, hearingId));
    }

    public Stream<Object> updateCompanyRepresentative(final CompanyRepresentative companyRepresentative, final UUID hearingId) {
        return apply(companyRepresentativeDelegate.updateCompanyRepresentative(companyRepresentative, hearingId));
    }

    public Stream<Object> removeCompanyRepresentative(final UUID id, final UUID hearingId) {
        return apply(companyRepresentativeDelegate.removeCompanyRepresentative(id, hearingId));
    }

    public Stream<Object> updateCaseMarkers(final UUID hearingId, final UUID prosecutionCaseId, final List<Marker> markers) {
        return prosecutionCaseDelegate.updateCaseMarkers(hearingId, prosecutionCaseId, markers);
    }

    private boolean hasHearingEnded() {
        final Map<UUID, HearingEventDelegate.HearingEvent> events = this.momento.getHearingEvents().entrySet().stream()
                .filter(hearingEvent -> RECORDED_LABEL_HEARING_END.equalsIgnoreCase(hearingEvent.getValue().getHearingEventLogged().getRecordedLabel()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return !events.isEmpty();
    }

    public Stream<Object> updateDefendantLegalAidStatusForHearing(final UUID hearingId, final UUID defendantId, final String legalAidStatus) {
        return apply(Stream.of(DefendantLegalAidStatusUpdatedForHearing.defendantLegalaidStatusUpdatedForHearing()
                .withHearingId(hearingId)
                .withDefendantId(defendantId)
                .withLegalAidStatus(legalAidStatus)
                .build()));

    }

    public Stream<Object> updateCaseDefendantsForHearing(final UUID hearingId, final ProsecutionCase prosecutionCase) {
        if (this.momento.getHearing().getHasSharedResults().equals(Boolean.TRUE)) {
            return Stream.empty();
        }
        return apply(Stream.of(CaseDefendantsUpdatedForHearing.caseDefendantsUpdatedForHearing()
                .withHearingId(hearingId)
                .withProsecutionCase(prosecutionCase)
                .build()));
    }

    public Stream<Object> bookProvisionalHearingSlots(final UUID hearingId, final List<ProvisionalHearingSlotInfo> slots) {

        return apply(Stream.of(BookProvisionalHearingSlots.bookProvisionalHearingSlots()
                .withHearingId(hearingId)
                .withSlots((new ArrayList<>(slots)))
                .build()));
    }

    private boolean isTargetValid(final HearingAggregateMomento momento, final Target newTarget) {
        final Map<UUID, Target> existingTargets = momento.getTargets();
        if (Objects.isNull(existingTargets) || existingTargets.isEmpty()) {
            return true;
        }

        // ensuring that for an existing target ID, offence and defendant ID also match
        if (existingTargets.containsKey(newTarget.getTargetId())) {
            final Target existingTarget = existingTargets.get(newTarget.getTargetId());
            return (existingTarget.getDefendantId().equals(newTarget.getDefendantId())
                    && existingTarget.getOffenceId().equals(newTarget.getOffenceId()));
        } else {
            final boolean offenceDefendantPresentForAnotherTargetId = existingTargets.values().stream()
                    .anyMatch(existingTarget -> existingTarget.getOffenceId().equals(newTarget.getOffenceId())
                            && existingTarget.getDefendantId().equals(newTarget.getDefendantId()));
            return !offenceDefendantPresentForAnotherTargetId;
        }
    }
}
