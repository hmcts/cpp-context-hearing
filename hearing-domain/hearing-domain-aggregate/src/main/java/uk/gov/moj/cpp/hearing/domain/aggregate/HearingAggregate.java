package uk.gov.moj.cpp.hearing.domain.aggregate;

import static java.time.ZonedDateTime.now;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.match;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.otherwiseDoNothing;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.when;
import static uk.gov.moj.cpp.hearing.domain.HearingState.APPROVAL_REQUESTED;
import static uk.gov.moj.cpp.hearing.domain.HearingState.INITIALISED;
import static uk.gov.moj.cpp.hearing.domain.HearingState.SHARED;
import static uk.gov.moj.cpp.hearing.domain.HearingState.SHARED_AMEND_LOCKED_ADMIN_ERROR;
import static uk.gov.moj.cpp.hearing.domain.HearingState.SHARED_AMEND_LOCKED_USER_ERROR;
import static uk.gov.moj.cpp.hearing.domain.HearingState.VALIDATED;
import static uk.gov.moj.cpp.hearing.domain.event.ReusableInfoSaved.reusableInfoSaved;

import uk.gov.justice.core.courts.ApplicantCounsel;
import uk.gov.justice.core.courts.AttendanceDay;
import uk.gov.justice.core.courts.CompanyRepresentative;
import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtCentre;
import uk.gov.justice.core.courts.DefenceCounsel;
import uk.gov.justice.core.courts.DefendantsWithWelshTranslation;
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
import uk.gov.justice.core.courts.ProsecutionCaseIdentifier;
import uk.gov.justice.core.courts.ProsecutionCounsel;
import uk.gov.justice.core.courts.RespondentCounsel;
import uk.gov.justice.core.courts.Target;
import uk.gov.justice.core.courts.Target2;
import uk.gov.justice.core.courts.Verdict;
import uk.gov.justice.core.courts.YouthCourt;
import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.moj.cpp.hearing.command.ReusableInfo;
import uk.gov.moj.cpp.hearing.command.ReusableInfoResults;
import uk.gov.moj.cpp.hearing.command.bookprovisional.ProvisionalHearingSlotInfo;
import uk.gov.moj.cpp.hearing.command.defendant.Defendant;
import uk.gov.moj.cpp.hearing.command.defendant.DefendantWelshInfo;
import uk.gov.moj.cpp.hearing.command.defendant.DefendantsWithWelshTranslationsCommand;
import uk.gov.moj.cpp.hearing.command.result.SharedResultLineId;
import uk.gov.moj.cpp.hearing.command.result.SharedResultsCommandResultLine;
import uk.gov.moj.cpp.hearing.command.result.SharedResultsCommandResultLineV2;
import uk.gov.moj.cpp.hearing.domain.HearingState;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.ApplicantCounselDelegate;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.CompanyRepresentativeDelegate;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.ConvictionDateDelegate;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.DefenceCounselDelegate;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.DefendantDelegate;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.HearingAggregateMomento;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.HearingDelegate;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.HearingEventDelegate;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.HearingTrialTypeDelegate;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.InterpreterIntermediaryDelegate;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.NowDelegate;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.OffenceDelegate;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.PleaDelegate;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.ProsecutionCaseDelegate;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.ProsecutionCounselDelegate;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.RespondentCounselDelegate;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.ResultsSharedDelegate;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.VariantDirectoryDelegate;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.VerdictDelegate;
import uk.gov.moj.cpp.hearing.domain.aggregate.util.CustodyTimeLimitUtil;
import uk.gov.moj.cpp.hearing.domain.event.AddCaseDefendantsForHearing;
import uk.gov.moj.cpp.hearing.domain.event.ApplicantCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.ApplicantCounselRemoved;
import uk.gov.moj.cpp.hearing.domain.event.ApplicantCounselUpdated;
import uk.gov.moj.cpp.hearing.domain.event.ApplicationDefendantsUpdatedForHearing;
import uk.gov.moj.cpp.hearing.domain.event.ApplicationDetailChanged;
import uk.gov.moj.cpp.hearing.domain.event.BookProvisionalHearingSlots;
import uk.gov.moj.cpp.hearing.domain.event.CaseDefendantsUpdatedForHearing;
import uk.gov.moj.cpp.hearing.domain.event.CaseMarkersUpdated;
import uk.gov.moj.cpp.hearing.domain.event.CompanyRepresentativeAdded;
import uk.gov.moj.cpp.hearing.domain.event.CompanyRepresentativeRemoved;
import uk.gov.moj.cpp.hearing.domain.event.CompanyRepresentativeUpdated;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateAdded;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateRemoved;
import uk.gov.moj.cpp.hearing.domain.event.CourtListRestricted;
import uk.gov.moj.cpp.hearing.domain.event.CpsProsecutorUpdated;
import uk.gov.moj.cpp.hearing.domain.event.CustodyTimeLimitClockStopped;
import uk.gov.moj.cpp.hearing.domain.event.CustodyTimeLimitExtended;
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselRemoved;
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselUpdated;
import uk.gov.moj.cpp.hearing.domain.event.DefendantAdded;
import uk.gov.moj.cpp.hearing.domain.event.DefendantAttendanceUpdated;
import uk.gov.moj.cpp.hearing.domain.event.DefendantDetailsUpdated;
import uk.gov.moj.cpp.hearing.domain.event.DefendantLegalAidStatusUpdatedForHearing;
import uk.gov.moj.cpp.hearing.domain.event.DefendantsInYouthCourtUpdated;
import uk.gov.moj.cpp.hearing.domain.event.DefendantsWelshInformationRecorded;
import uk.gov.moj.cpp.hearing.domain.event.EarliestNextHearingDateCleared;
import uk.gov.moj.cpp.hearing.domain.event.ExistingHearingUpdated;
import uk.gov.moj.cpp.hearing.domain.event.HearingAmended;
import uk.gov.moj.cpp.hearing.domain.event.HearingDaysWithoutCourtCentreCorrected;
import uk.gov.moj.cpp.hearing.domain.event.HearingDeleted;
import uk.gov.moj.cpp.hearing.domain.event.HearingDetailChanged;
import uk.gov.moj.cpp.hearing.domain.event.HearingEffectiveTrial;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventDeleted;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventLogged;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventVacatedTrialCleared;
import uk.gov.moj.cpp.hearing.domain.event.HearingExtended;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.domain.event.HearingLocked;
import uk.gov.moj.cpp.hearing.domain.event.HearingLockedByOtherUser;
import uk.gov.moj.cpp.hearing.domain.event.HearingMarkedAsDuplicate;
import uk.gov.moj.cpp.hearing.domain.event.HearingTrialType;
import uk.gov.moj.cpp.hearing.domain.event.HearingTrialVacated;
import uk.gov.moj.cpp.hearing.domain.event.HearingUnallocated;
import uk.gov.moj.cpp.hearing.domain.event.HearingUnlockFailed;
import uk.gov.moj.cpp.hearing.domain.event.HearingUnlocked;
import uk.gov.moj.cpp.hearing.domain.event.InheritedPlea;
import uk.gov.moj.cpp.hearing.domain.event.InheritedVerdictAdded;
import uk.gov.moj.cpp.hearing.domain.event.InterpreterIntermediaryAdded;
import uk.gov.moj.cpp.hearing.domain.event.InterpreterIntermediaryRemoved;
import uk.gov.moj.cpp.hearing.domain.event.InterpreterIntermediaryUpdated;
import uk.gov.moj.cpp.hearing.domain.event.MasterDefendantIdAdded;
import uk.gov.moj.cpp.hearing.domain.event.NextHearingStartDateRecorded;
import uk.gov.moj.cpp.hearing.domain.event.NowsVariantsSavedEvent;
import uk.gov.moj.cpp.hearing.domain.event.OffenceAdded;
import uk.gov.moj.cpp.hearing.domain.event.OffenceDeleted;
import uk.gov.moj.cpp.hearing.domain.event.OffenceUpdated;
import uk.gov.moj.cpp.hearing.domain.event.OffencesRemovedFromExistingHearing;
import uk.gov.moj.cpp.hearing.domain.event.PleaUpsert;
import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselRemoved;
import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselUpdated;
import uk.gov.moj.cpp.hearing.domain.event.RespondentCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.RespondentCounselRemoved;
import uk.gov.moj.cpp.hearing.domain.event.RespondentCounselUpdated;
import uk.gov.moj.cpp.hearing.domain.event.TargetRemoved;
import uk.gov.moj.cpp.hearing.domain.event.VerdictUpsert;
import uk.gov.moj.cpp.hearing.domain.event.result.ApprovalRequestRejected;
import uk.gov.moj.cpp.hearing.domain.event.result.ApprovalRequested;
import uk.gov.moj.cpp.hearing.domain.event.result.ApprovalRequestedV2;
import uk.gov.moj.cpp.hearing.domain.event.result.DaysResultLinesStatusUpdated;
import uk.gov.moj.cpp.hearing.domain.event.result.DraftResultSaved;
import uk.gov.moj.cpp.hearing.domain.event.result.DraftResultSavedV2;
import uk.gov.moj.cpp.hearing.domain.event.result.MultipleDraftResultsSaved;
import uk.gov.moj.cpp.hearing.domain.event.result.ReplicateResultsSharedV3;
import uk.gov.moj.cpp.hearing.domain.event.result.ReplicationOfShareResultsFailed;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultAmendmentsCancellationFailed;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultAmendmentsCancelled;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultAmendmentsCancelledV2;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultAmendmentsRejected;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultAmendmentsRejectedV2;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultAmendmentsValidated;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultAmendmentsValidationFailed;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultLinesStatusUpdated;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsSharedV2;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsSharedV3;
import uk.gov.moj.cpp.hearing.domain.event.result.SaveDraftResultFailed;
import uk.gov.moj.cpp.hearing.domain.event.result.ShareResultsFailed;
import uk.gov.moj.cpp.hearing.eventlog.HearingEvent;
import uk.gov.moj.cpp.hearing.nows.events.PendingNowsRequested;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.json.JsonObject;

@SuppressWarnings({"squid:S00107", "squid:S1602", "squid:S1188", "squid:S1612", "PMD.BeanMembersShouldSerialize", "squid:CommentedOutCodeLine","squid:CallToDeprecatedMethod"})
public class HearingAggregate implements Aggregate {

    private static final long serialVersionUID = -6059812881894748583L;

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

    private final RespondentCounselDelegate respondentCounselDelegate = new RespondentCounselDelegate(momento);

    private final ApplicantCounselDelegate applicantCounselDelegate = new ApplicantCounselDelegate(momento);

    private final InterpreterIntermediaryDelegate interpreterIntermediaryDelegate = new InterpreterIntermediaryDelegate(momento);

    private final HearingTrialTypeDelegate hearingTrialTypeDelegate = new HearingTrialTypeDelegate(momento);

    private final CompanyRepresentativeDelegate companyRepresentativeDelegate = new CompanyRepresentativeDelegate(momento);

    private final ProsecutionCaseDelegate prosecutionCaseDelegate = new ProsecutionCaseDelegate(momento);

    private final NowDelegate nowDelegate = new NowDelegate(momento);

    private HearingState hearingState;

    private UUID amendingSharedHearingUserId;

    private List<DefendantWelshInfo> defendantsWelshInformationList;

    @Override
    public Object apply(final Object event) {
        return match(event).with(
                when(HearingInitiated.class).apply(e -> {
                    this.hearingState = INITIALISED;
                    hearingDelegate.handleHearingInitiated(e);
                }),
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
                            this.hearingState = SHARED;
                            resultsSharedDelegate.handleResultsShared(e);
                            defendantDelegate.clearDefendantDetailsChanged();
                        }
                ),
                when(ResultsSharedV2.class).apply(e -> {
                            this.hearingState = SHARED;
                            resultsSharedDelegate.handleResultsSharedV2(e);
                            defendantDelegate.clearDefendantDetailsChanged();
                        }
                ),
                when(ResultsSharedV3.class).apply(e -> {
                            this.hearingState = SHARED;
                            resultsSharedDelegate.handleResultsSharedV3(e);
                            defendantDelegate.clearDefendantDetailsChanged();
                        }
                ),
                when(ResultLinesStatusUpdated.class).apply(resultsSharedDelegate::handleResultLinesStatusUpdated),
                when(DaysResultLinesStatusUpdated.class).apply(resultsSharedDelegate::handleDaysResultLinesStatusUpdated),
                when(InheritedVerdictAdded.class).apply(verdictDelegate::handleInheritedVerdict),
                when(VerdictUpsert.class).apply(verdictDelegate::handleVerdictUpsert),
                when(ConvictionDateAdded.class).apply(convictionDateDelegate::handleConvictionDateAdded),
                when(ConvictionDateRemoved.class).apply(convictionDateDelegate::handleConvictionDateRemoved),
                when(DefendantDetailsUpdated.class).apply(defendantDelegate::handleDefendantDetailsUpdated),
                when(OffenceAdded.class).apply(offenceDelegate::handleOffenceAdded),
                when(OffenceUpdated.class).apply(offenceDelegate::handleOffenceUpdated),
                when(OffenceDeleted.class).apply(offenceDelegate::handleOffenceDeleted),
                when(NowsVariantsSavedEvent.class).apply(variantDirectoryDelegate::handleNowsVariantsSavedEvent),
                when(DraftResultSaved.class).apply(draftResultSaved -> {
                            this.amendingSharedHearingUserId = draftResultSaved.getAmendedByUserId();
                            this.hearingState = draftResultSaved.getHearingState();
                            resultsSharedDelegate.handleDraftResultSaved(draftResultSaved);
                        }
                ),
                when(DraftResultSavedV2.class).apply(draftResultSaved -> {
                        }
                ),
                when(DefendantAttendanceUpdated.class).apply(defendantDelegate::handleDefendantAttendanceUpdated),
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
                when(CpsProsecutorUpdated.class).apply(prosecutionCaseDelegate::handleProsecutorUpdated),
                when(DefendantLegalAidStatusUpdatedForHearing.class).apply(prosecutionCaseDelegate::onDefendantLegalaidStatusTobeUpdatedForHearing),
                when(CaseDefendantsUpdatedForHearing.class).apply(prosecutionCaseDelegate::onCaseDefendantUpdatedForHearing),
                when(AddCaseDefendantsForHearing.class).apply(prosecutionCaseDelegate::onCaseDefendantsAddedForHearing),
                when(HearingEventVacatedTrialCleared.class).apply(hearingEventVacatedTrialCleared -> hearingDelegate.handleVacatedTrialCleared()),
                when(TargetRemoved.class).apply(targetRemoved -> hearingDelegate.handleTargetRemoved(targetRemoved.getTargetId())),
                when(PendingNowsRequested.class).apply(nowDelegate::handlePendingNowsRequested),
                when(MasterDefendantIdAdded.class).apply(masterDefendantIdAdded ->
                        hearingDelegate.handleMasterDefendantIdAdded(
                                masterDefendantIdAdded.getProsecutionCaseId(),
                                masterDefendantIdAdded.getDefendantId(),
                                masterDefendantIdAdded.getMasterDefendantId())),
                when(HearingDaysWithoutCourtCentreCorrected.class).apply(hearingDelegate::handleHearingDaysWithoutCourtCentreCorrected),
                when(HearingMarkedAsDuplicate.class).apply(duplicate -> hearingDelegate.handleHearingMarkedAsDuplicate()),
                when(DefendantsInYouthCourtUpdated.class).apply(e -> this.momento.getHearing().setYouthCourtDefendantIds(e.getYouthCourtDefendantIds())),
                when(HearingAmended.class).apply(x -> {
                    this.amendingSharedHearingUserId = x.getUserId();
                    this.hearingState = x.getNewHearingState();
                }),
                when(ResultAmendmentsCancelled.class).apply(x -> {
                    this.hearingState = SHARED;
                    this.momento.getTransientTargets().clear();
                }),
                when(ResultAmendmentsCancelledV2.class).apply(x -> {
                    this.hearingState = SHARED;
                    this.momento.getTransientTargets().clear();
                }),
                when(ResultAmendmentsValidated.class).apply(x -> {
                    this.hearingState = VALIDATED;
                }),
                when(ResultAmendmentsRejected.class).apply(x -> {
                    this.hearingState = SHARED;
                    this.momento.getTransientTargets().clear();
                }),
                when(ResultAmendmentsRejectedV2.class).apply(x -> {
                    this.hearingState = SHARED;
                    this.momento.getTransientTargets().clear();
                }),
                when(ApprovalRequestedV2.class).apply(e -> {
                    this.hearingState = APPROVAL_REQUESTED;
                }),
                when(HearingDeleted.class).apply(deleted -> hearingDelegate.handleHearingDeleted()),
                when(HearingUnallocated.class).apply(hearingDelegate::handleHearingUnallocated),
                when(NextHearingStartDateRecorded.class).apply(hearingDelegate::handleNextHearingStartDateRecorded),
                when(EarliestNextHearingDateCleared.class).apply(cleared -> hearingDelegate.handleEarliestNextHearingDateCleared()),
                when(OffencesRemovedFromExistingHearing.class).apply(offenceDelegate::handleOffencesRemovedFromExistingHearing),
                when(ExistingHearingUpdated.class).apply(offenceDelegate::handleExistingHearingUpdated),
                when(CustodyTimeLimitClockStopped.class).apply(offenceDelegate::handleCustodyTimeLimitClockStopped),
                when(CustodyTimeLimitExtended.class).apply(offenceDelegate::handleCustodyTimeLimitExtended),
                when(DefendantsWelshInformationRecorded.class).apply(this::handleDefendantsWelshTranslation),
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
        //removing this bit of code here as offence count is not a mandatory field to initiatehearing
        /*
        if (JurisdictionType.CROWN.equals(hearing.getJurisdictionType()) && hearing.getProsecutionCases() != null) {
            final List<Offence> offences = this.hearingDelegate.getAllOffencesMissingCount(hearing);
            if (!offences.isEmpty()) {
                return apply(this.hearingDelegate.ignoreHearingInitiate(offences, hearing.getId()));
            }
        }
        */
        return apply(this.hearingDelegate.initiate(hearing));
    }

    public Stream<Object> extend(final UUID hearingId, final List<HearingDay> hearingDays, final CourtCentre courtCentre, final JurisdictionType jurisdictionType,
                                 final CourtApplication courtApplication, final List<ProsecutionCase> prosecutionCases, final List<UUID> shadowListedOffences) {
        return apply(this.hearingDelegate.extend(hearingId, hearingDays, courtCentre, jurisdictionType, courtApplication, prosecutionCases, shadowListedOffences));
    }

    public Stream<Object> markAsDuplicate(final UUID hearingId) {
        return apply(this.hearingDelegate.markAsDuplicate(hearingId));
    }

    public Stream<Object> updateExistingHearing(final UUID hearingId, final List<ProsecutionCase> prosecutionCases, final List<UUID> shadowListedOffences) {
        if (this.momento.getHearing() == null) {
            return Stream.of(hearingDelegate.generateHearingIgnoredMessage("Ignoring 'unAllocateHearing' event as hearing not found", hearingId));
        }
        return apply(Stream.of(new ExistingHearingUpdated(hearingId, prosecutionCases, shadowListedOffences)));
    }

    /**
     * Marks a hearing as duplicate. Will not mark a hearing that has results as duplicate unless
     * the overwrite flag has been provided.
     *
     * @param hearingId            - the id of the hearing to be marked as duplicate.
     * @param overwriteWithResults - if TRUE then mark as duplicate, even if the hearing has
     *                             results.
     * @return mark as duplicate event, or no events.
     */
    public Stream<Object> markAsDuplicate(final UUID hearingId, final boolean overwriteWithResults) {
        if (resultsSharedDelegate.hasResultsShared() && !overwriteWithResults) {
            return Stream.empty();
        } else {
            return apply(this.hearingDelegate.markAsDuplicate(hearingId));
        }
    }

    public Stream<Object> updatePlea(final UUID hearingId, final PleaModel plea, final Set<String> guiltyPleaTypes) {
        return apply(pleaDelegate.updatePlea(hearingId, plea, guiltyPleaTypes));
    }

    public Stream<Object> inheritPlea(final UUID hearingId, final Plea plea) {
        return apply(this.pleaDelegate.inheritPlea(hearingId, plea));
    }

    public Stream<Object> logHearingEvent(final UUID hearingId, final UUID hearingEventDefinitionId, final Boolean alterable, final UUID defenceCounselId, final HearingEvent hearingEvent, final List<UUID> hearingTypeIds) {
        return apply(Stream.concat(this.hearingEventDelegate.logHearingEvent(hearingId, hearingEventDefinitionId, alterable, defenceCounselId, hearingEvent),
                CustodyTimeLimitUtil.stopCTLExpiryForTrialHearingUser(this.momento, hearingEvent, hearingTypeIds)));
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

        if (this.momento.getHearing() == null) {
            return Stream.of(hearingDelegate.generateHearingIgnoredMessage("Ignoring 'updateHearingVacateTrialDetails' event as hearing not found", hearingId));
        }
        return apply(this.hearingDelegate.updateHearingVacateTrialDetails(hearingId, isVacated, vacatedTrialReasonId));
    }

    public Stream<Object> clearVacatedTrial(final UUID id) {
        return apply(this.hearingDelegate.clearVacatedTrial(id));
    }

    public Stream<Object> updateVerdict(final UUID hearingId, final Verdict verdict, final Set<String> guiltyPleaTypes) {
        return apply(this.verdictDelegate.updateVerdict(hearingId, verdict, guiltyPleaTypes));
    }

    public Stream<Object> shareResults(final UUID hearingId, final DelegatedPowers courtClerk, final ZonedDateTime sharedTime, final List<SharedResultsCommandResultLine> resultLines, final HearingState newHearingState, final YouthCourt youthCourt) {
        if (
                (asList(HearingState.SHARED_AMEND_LOCKED_ADMIN_ERROR, HearingState.SHARED_AMEND_LOCKED_USER_ERROR, APPROVAL_REQUESTED).contains(this.hearingState))
                        || (INITIALISED == newHearingState && SHARED == this.hearingState)
        ) {

            return Stream.of(new ShareResultsFailed.Builder()
                    .withHearingId(hearingId)
                    .withAmendedByUserId(this.amendingSharedHearingUserId)
                    .withHearingState(this.hearingState).build());
        }
        return apply(resultsSharedDelegate.shareResults(hearingId, courtClerk, sharedTime, resultLines, this.defendantDelegate.getDefendantDetailsChanged(), youthCourt));
    }

    /**
     * This method has been introduced to be used as part of the BDF run and solely for that purpose.
     * @param hearingId
     * @return
     */
    public Stream<Object> replicateSharedResultsForHearing(final UUID hearingId) {
        if (!asList(SHARED_AMEND_LOCKED_ADMIN_ERROR, SHARED,  SHARED_AMEND_LOCKED_USER_ERROR, APPROVAL_REQUESTED, VALIDATED).contains(this.hearingState)) {

            return apply(Stream.of(new ReplicationOfShareResultsFailed.Builder()
                    .withHearingId(hearingId)
                    .withHearingState(this.hearingState).build()));
        }
       return  apply(this.momento.getMultiDaySavedTargets().entrySet().stream().map(e -> createReplicatedResults(e.getKey(), e.getValue())).flatMap(event -> Stream.of(event)));
    }


    private ReplicateResultsSharedV3 createReplicatedResults(final LocalDate hearingDay, final Map<UUID, Target2> targetList) {
        return ReplicateResultsSharedV3.builder().withHearingDay(hearingDay)
                .withTargets(new ArrayList<>(targetList.values()))
                .withHearingId(this.getHearing().getId())
                .build();

    }

    public Stream<Object> shareResultsV2(final UUID hearingId, final DelegatedPowers courtClerk, final ZonedDateTime sharedTime, final List<SharedResultsCommandResultLineV2> resultLines, final LocalDate hearingDay) {
        return apply(resultsSharedDelegate.shareResultsV2(hearingId, courtClerk, sharedTime, resultLines, this.defendantDelegate.getDefendantDetailsChanged(), hearingDay));
    }

    public Stream<Object> shareResultForDay(final UUID hearingId, final DelegatedPowers courtClerk, final ZonedDateTime sharedTime, final List<SharedResultsCommandResultLineV2> resultLines, final HearingState newHearingState, final YouthCourt youthCourt, final LocalDate hearingDay) {
        if (
                (Arrays.asList(HearingState.SHARED_AMEND_LOCKED_ADMIN_ERROR, HearingState.SHARED_AMEND_LOCKED_USER_ERROR, APPROVAL_REQUESTED).contains(this.hearingState))
                        || (INITIALISED == newHearingState && SHARED == this.hearingState)
        ) {

            return Stream.of(new ShareResultsFailed.Builder()
                    .withHearingId(hearingId)
                    .withAmendedByUserId(this.amendingSharedHearingUserId)
                    .withHearingState(this.hearingState).build());
        }
        return apply(resultsSharedDelegate.shareResultForDay(hearingId, courtClerk, sharedTime, resultLines, this.defendantDelegate.getDefendantDetailsChanged(), youthCourt, hearingDay));
    }

    public Stream<Object> saveAllDraftResults(final List<Target> targets, final UUID userId) {

        final List<Object> appliedTargetEvent = targets.stream().map(x -> saveDraftResults(userId, x))
                .map(s -> s.collect(Collectors.toList())).flatMap(x -> x.stream()).collect(Collectors.toList());
        final Optional failure = appliedTargetEvent.stream().filter(x -> isFailure(x)).findFirst();
        if (failure.isPresent() && isFailure(failure.get())) {
            return Stream.of(failure.get());
        } else {
            appliedTargetEvent.add(new MultipleDraftResultsSaved(targets.size()));
            return appliedTargetEvent.stream();
        }
    }

    private boolean isFailure(final Object o) {
        return o instanceof SaveDraftResultFailed || o instanceof HearingLocked || o instanceof HearingLockedByOtherUser;
    }

    public Stream<Object> cancelAmendmentsSincePreviousShare(final UUID hearingId, final UUID userId, final boolean resetHearing) {
        if (resetHearing) {
            return apply(Stream.of(new ResultAmendmentsCancelledV2(hearingId, userId, new ArrayList<>(this.momento.getSharedTargets().values()), this.momento.getLastSharedTime())));
        }
        if (isSameUserWhoIsAmendingSharedHearing(userId) && isSharedHearingBeingAmended()) {
            //TO add the last Shared aggregates.
            return apply(Stream.of(new ResultAmendmentsCancelledV2(hearingId, userId, new ArrayList<>(this.momento.getSharedTargets().values()), this.momento.getLastSharedTime())));
        }
        return apply(Stream.of(new ResultAmendmentsCancellationFailed("Either user is not same or hearing was not being amended")));
    }

    public Stream<Object> unlockHearing(final UUID hearingId, final UUID userId) {
        if (!isSameUserWhoIsAmendingSharedHearing(userId) && isSharedHearingBeingAmended()) {

            final Stream.Builder<Object> streamBuilder = Stream.builder();
            streamBuilder.add(HearingUnlocked.hearingUnlockedBuilder()
                    .withHearingId(hearingId)
                    .withUserId(userId)
                    .build());

            streamBuilder.add(new ResultAmendmentsCancelledV2(hearingId, userId, new ArrayList<>(this.momento.getSharedTargets().values()), this.momento.getLastSharedTime()));
            return apply(streamBuilder.build());

        } else {
            return apply(Stream.of(HearingUnlockFailed.hearingUnlockedFailedBuilder()
                    .withHearingId(hearingId)
                    .withReason("Either user is same or hearing was not being amended")
                    .build()));
        }
    }

    private boolean isSharedHearingBeingAmended() {
        return (SHARED_AMEND_LOCKED_ADMIN_ERROR == hearingState) || (SHARED_AMEND_LOCKED_USER_ERROR == hearingState);
    }

    private boolean isSameUserWhoIsAmendingSharedHearing(final UUID userId) {
        return amendingSharedHearingUserId != null && amendingSharedHearingUserId.equals(userId);
    }


    public Stream<Object> saveDraftResults(final UUID userId, final Target target) {


        if ((VALIDATED.equals(this.hearingState) && isSameUserWhoIsAmendingSharedHearing(userId)) || APPROVAL_REQUESTED.equals(this.hearingState)) {
            return apply(resultsSharedDelegate.hearingLocked(target.getHearingId()));
        }

        if (isSharedHearingBeingAmended() && !isSameUserWhoIsAmendingSharedHearing(userId)) {
            return apply(resultsSharedDelegate.hearingLockedByOtherUser(target.getHearingId()));
        }


        this.amendingSharedHearingUserId = userId;
        final HearingState newHearingState = getHearingState(target.getReasonsList());

        final LocalDate hearingDay = this.momento.getHearing().getHearingDays().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Hearing Day is not present"))
                .getSittingDay()
                .toLocalDate();

        final Target targetForEvent = Target
                .target()
                .withShadowListed(target.getShadowListed())
                .withApplicationId(target.getApplicationId())
                .withReasonsList(target.getReasonsList())
                .withDefendantId(target.getDefendantId())
                .withDraftResult(target.getDraftResult())
                .withHearingId(target.getHearingId())
                .withOffenceId(target.getOffenceId())
                .withResultLines(target.getResultLines())
                .withTargetId(target.getTargetId())
                .withHearingDay(hearingDay)
                .build();

        // Fix to ensure that no extra target IDs are created for the same combination of offence and defendant.
        // The aggregate ensures that any extra target ID for the same combination of offence / defendant is rejected and not processed
        if (isTargetValid(momento, target, hearingDay)) {
            return apply(resultsSharedDelegate.saveDraftResult(targetForEvent, newHearingState, userId));
        }
        return apply(resultsSharedDelegate.rejectSaveDraftResult(targetForEvent));
    }

    public Stream<Object> saveDraftResultV2(final UUID userId, final JsonObject draftResult, final UUID hearingId, LocalDate hearingDay) {
        if ((VALIDATED.equals(this.hearingState) && isSameUserWhoIsAmendingSharedHearing(userId))
                || APPROVAL_REQUESTED.equals(this.hearingState)) {
            return apply(resultsSharedDelegate.hearingLocked(hearingId));
        }

        if (isSharedHearingBeingAmended() && !isSameUserWhoIsAmendingSharedHearing(userId)) {
            return apply(resultsSharedDelegate.hearingLockedByOtherUser(hearingId));
        }

        return apply(resultsSharedDelegate.saveDraftResultV2(hearingId, hearingDay, draftResult, userId));
    }

    public Stream<Object> deleteDraftResultV2(final UUID userId, final UUID hearingId, LocalDate hearingDay) {

        if (!INITIALISED.equals(this.hearingState)) {
            return apply(resultsSharedDelegate.deleteDraftResultV2(hearingId, hearingDay, userId));
        } else {
            return apply(resultsSharedDelegate.hearingLockedByOtherUser(hearingId));
        }
    }

    public Stream<Object> amendHearing(final UUID hearingId, final UUID userId, final HearingState newHearingState) {

        if (SHARED_AMEND_LOCKED_ADMIN_ERROR.equals(newHearingState) || SHARED_AMEND_LOCKED_USER_ERROR.equals(newHearingState)) {
            return apply(resultsSharedDelegate.amendHearing(hearingId, userId, newHearingState));

        }
        return null;
    }


    @SuppressWarnings("squid:S3358")
    private HearingState getHearingState(final List<String> hearingStates) {
        if (this.hearingState != INITIALISED && this.hearingState != VALIDATED) {
            return hearingStates != null && (!hearingStates.isEmpty()) ? hearingStates.contains("ca8b8285-5fc7-3b36-aa78-ecdf5ac6dad0") ? SHARED_AMEND_LOCKED_ADMIN_ERROR : SHARED_AMEND_LOCKED_USER_ERROR : this.hearingState;
        }
        return this.hearingState;
    }

    private boolean isValidTarget(final Target target) {
        final boolean isDefendantPresent = nonNull(target.getDefendantId());
        final boolean isOffencePresent = nonNull(target.getOffenceId());
        final boolean isApplicationPresent = nonNull(target.getApplicationId());

        if (isDefendantPresent && isOffencePresent && isApplicationPresent) {
            return false;
        }

        return (isDefendantPresent && isOffencePresent) || (isApplicationPresent && !isDefendantPresent);
    }

    public Stream<Object> saveDraftResultForHearingDay(final UUID userId, final Target target) {
        return saveDraftResultForHearingDay(userId, target, target.getHearingDay());
    }

    public Stream<Object> saveMultipleDraftResultsForHearingDay(final List<Target> targets, final LocalDate hearingDay, final UUID userId) {

        final List<Object> appliedTargetEvent = targets.stream()
                .map(x -> saveDraftResultForHearingDay(userId, x, hearingDay))
                .map(s -> s.collect(toList()))
                .flatMap(Collection::stream)
                .collect(toList());

        final Optional<SaveDraftResultFailed> saveDraftResultFailed = appliedTargetEvent.stream()
                .filter(x -> x instanceof SaveDraftResultFailed)
                .map(s -> (SaveDraftResultFailed) s)
                .findFirst();

        if (saveDraftResultFailed.isPresent()) {
            return Stream.of(saveDraftResultFailed.get());
        } else {
            appliedTargetEvent.add(new MultipleDraftResultsSaved(targets.size()));
            return appliedTargetEvent.stream();
        }
    }

    public Stream<Object> updateResultLinesStatus(final UUID hearingId, final DelegatedPowers courtClerk, final ZonedDateTime lastSharedDateTime, final List<SharedResultLineId> sharedResultLines) {
        return apply(resultsSharedDelegate.updateResultLinesStatus(hearingId, courtClerk, lastSharedDateTime, sharedResultLines));
    }

    public Stream<Object> updateDaysResultLinesStatus(final UUID hearingId, final DelegatedPowers courtClerk, final ZonedDateTime lastSharedDateTime, final List<SharedResultLineId> sharedResultLines, final LocalDate hearingDay) {
        return apply(resultsSharedDelegate.updateDaysResultLinesStatus(hearingId, courtClerk, lastSharedDateTime, sharedResultLines, hearingDay));
    }

    public Stream<Object> updateDefendantDetails(final UUID hearingId, final Defendant defendant) {
        return apply(this.defendantDelegate.updateDefendantDetails(hearingId, defendant));
    }

    public Stream<Object> addOffence(final UUID hearingId, final UUID defendantId, final UUID prosecutionCaseId, final Offence offence) {
        if(momento.isDeleted()){
            return Stream.empty();
        }
        if (this.momento.getHearing() == null) {
            return Stream.of(hearingDelegate.generateHearingIgnoredMessage("Ignoring 'addOffence' event as hearing not found", hearingId));
        }

        return apply(this.offenceDelegate.addOffence(hearingId, defendantId, prosecutionCaseId, offence));
    }

    public Stream<Object> updateOffence(final UUID hearingId, final UUID defendantId, final Offence offence) {
        return apply(this.offenceDelegate.updateOffence(hearingId, defendantId, offence));
    }

    public Stream<Object> deleteOffence(final UUID offenceId, final UUID hearingId) {
        return apply(this.offenceDelegate.deleteOffence(offenceId, hearingId));
    }

    public Stream<Object> removeOffencesFromExistingHearing(final UUID hearingId, final List<UUID> offenceIds) {
        if (this.momento.getHearing() == null) {
            return Stream.of(hearingDelegate.generateHearingIgnoredMessage("Ignoring 'removeOffencesFromExistingHearing' event as hearing not found", hearingId));
        }

        return apply(this.offenceDelegate.removeOffencesFromAllocatedHearing(hearingId, offenceIds));
    }

    public Stream<Object> updateDefendantAttendance(final UUID hearingId, final UUID defendantId, final AttendanceDay attendanceDay) {
        return apply(this.defendantDelegate.updateDefendantAttendance(hearingId, defendantId, attendanceDay));
    }

    public Stream<Object> inheritVerdict(final UUID hearingId, final Verdict verdict) {
        return apply(this.verdictDelegate.inheritVerdict(hearingId, verdict));
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
        return apply(hearingDelegate.addDefendant(hearingId, defendant));
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

    public Stream<Object> setTrialType(final UUID hearingId, final UUID vacatedTrialReasonId, final String code, final String description, final String type) {
        return apply(this.hearingTrialTypeDelegate.setTrialType(hearingId, vacatedTrialReasonId, code, description, type));
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
        return apply(prosecutionCaseDelegate.updateCaseMarkers(hearingId, prosecutionCaseId, markers));
    }

    public Stream<Object> updateProsecutor(final UUID hearingId, final UUID prosecutionCaseId, final ProsecutionCaseIdentifier prosecutionCaseIdentifier) {
        return apply(prosecutionCaseDelegate.updateProsecutor(hearingId, prosecutionCaseId, prosecutionCaseIdentifier));
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

    public Stream<Object> addOrUpdateCaseDefendantsForHearing(final UUID hearingId, final ProsecutionCase prosecutionCase) {
        if (SHARED.equals(this.hearingState) || this.momento.isDeleted()) {
            return Stream.empty();
        }
        final Stream.Builder<Object> streamBuilder = Stream.builder();
        streamBuilder.add(CaseDefendantsUpdatedForHearing.caseDefendantsUpdatedForHearing()
                .withHearingId(hearingId)
                .withProsecutionCase(prosecutionCase)
                .build());

        //check if there are new defendants added
        final ProsecutionCase matchedProsecutionCase = this.momento.getHearing().getProsecutionCases().stream().filter(pc -> pc.getId().equals(prosecutionCase.getId())).findFirst().orElse(null);
        if (nonNull(matchedProsecutionCase)) {
            final List<uk.gov.justice.core.courts.Defendant> newDefendants =
                    prosecutionCase.getDefendants().stream().filter(def -> matchedProsecutionCase.getDefendants().stream().noneMatch(persistentDef -> def.getId().equals(persistentDef.getId()))).collect(Collectors.toList());
            if (!newDefendants.isEmpty()) {
                streamBuilder.add(AddCaseDefendantsForHearing.addCaseDefendantsForHearing()
                        .withCaseId(prosecutionCase.getId())
                        .withHearingId(hearingId)
                        .withDefendants(newDefendants)
                        .build());
            }
        }

        return apply(streamBuilder.build());
    }

    public Stream<Object> updateApplicationDefendantsForHearing(final UUID hearingId, final CourtApplication courtApplication) {
        if (SHARED.equals(this.hearingState) || this.momento.isDeleted()) {
            return Stream.empty();
        }
        return apply(Stream.of(ApplicationDefendantsUpdatedForHearing.applicationDefendantsUpdatedForHearing()
                .withHearingId(hearingId)
                .withCourtApplication(courtApplication)
                .build()));
    }

    public Stream<Object> bookProvisionalHearingSlots(final UUID hearingId, final List<ProvisionalHearingSlotInfo> slots, final String bookingType, final String priority, final List<String> specialRequirements) {

        return apply(Stream.of(BookProvisionalHearingSlots.bookProvisionalHearingSlots()
                .withHearingId(hearingId)
                .withSlots((new ArrayList<>(slots)))
                .withBookingType(bookingType)
                .withPriority(priority)
                .withSpecialRequirements(specialRequirements)
                .build()));
    }


    public Stream<Object> approvalRequest(final UUID hearingId, final UUID userId) {

        if (userId.equals(this.amendingSharedHearingUserId) && isSharedHearingBeingAmended()) {
            final Stream.Builder<Object> streamBuilder = Stream.builder();
            streamBuilder.add(ApprovalRequestedV2.approvalRequestedBuilder()
                    .withHearingId(hearingId)
                    .withUserId(userId)
                    .build());
            streamBuilder.add(ApprovalRequested.approvalRequestedBuilder()
                    .withHearingId(hearingId)
                    .withUserId(userId)
                    .build());
            return apply(streamBuilder.build());
        } else {
            return apply(Stream.of(ApprovalRequestRejected.approvalRejectedBuilder()
                    .withHearingId(hearingId)
                    .withUserId(userId)
                    .build()));
        }
    }

    public Stream<Object> cancelHearingDays(final UUID hearingId, final List<HearingDay> hearingDays) {
        return this.apply(this.hearingDelegate.cancelHearingDays(hearingId, hearingDays));
    }

    public Stream<Object> removeTarget(final UUID hearingId, final UUID targetId) {
        return hearingDelegate.removeTarget(hearingId, targetId);
    }

    public Stream<Object> addMasterDefendantIdToDefendant(final UUID hearingId, final UUID prosecutionCaseId, final UUID defendantId, final UUID masterDefendantId) {
        return hearingDelegate.addMasterDefendantIdToDefendant(hearingId, prosecutionCaseId, defendantId, masterDefendantId);
    }

    @SuppressWarnings("squid:S1067")
    private boolean matchApplicationTarget(final Target existingTarget, final Target newTarget) {
        return isNull(existingTarget.getOffenceId()) && isNull(newTarget.getOffenceId()) && isNull(existingTarget.getDefendantId()) && isNull(newTarget.getDefendantId()) && nonNull(existingTarget.getApplicationId()) && existingTarget.getApplicationId().equals(newTarget.getApplicationId());
    }

    @SuppressWarnings("squid:S1067")
    private boolean matchApplicationOffenceTarget(final Target existingTarget, final Target newTarget) {
        return isNull(existingTarget.getDefendantId()) && isNull(newTarget.getDefendantId()) && nonNull(existingTarget.getApplicationId()) && nonNull(existingTarget.getOffenceId()) && existingTarget.getApplicationId().equals(newTarget.getApplicationId()) && existingTarget.getOffenceId().equals(newTarget.getOffenceId());
    }

    @SuppressWarnings("squid:S1067")
    private boolean matchDefendantOffenceTarget(final Target existingTarget, final Target newTarget) {
        return isNull(existingTarget.getApplicationId()) && isNull(newTarget.getApplicationId()) && nonNull(existingTarget.getDefendantId()) && nonNull(existingTarget.getOffenceId()) && existingTarget.getDefendantId().equals(newTarget.getDefendantId()) && existingTarget.getOffenceId().equals(newTarget.getOffenceId());
    }

    public Stream<Object> validateResultsAmendments(final UUID hearingId, final UUID userId, final String validateAction) {
        if (!isSameUserWhoIsAmendingSharedHearing(userId) && canValidateOrReject()) {
            if ("APPROVE".equalsIgnoreCase(validateAction.trim())) {
                return apply(Stream.of(ResultAmendmentsValidated.resultAmendmentsRequested()
                        .withHearingId(hearingId)
                        .withUserId(userId)
                        .withValidateResultAmendmentsTime(now())
                        .build()));
            }
            return apply(Stream.of(ResultAmendmentsRejectedV2.resultAmendmentsRejected()
                    .withHearingId(hearingId)
                    .withUserId(userId)
                    .withValidateResultAmendmentsTime(now())
                    .withLastSharedDateTime(this.momento.getLastSharedTime())
                    .withLatestSharedTargets(new ArrayList<>(this.momento.getSharedTargets().values()))
                    .build()));
        }
        return apply(Stream.of(new ResultAmendmentsValidationFailed(hearingId, userId, now())));
    }

    private boolean canValidateOrReject() {
        return (hearingState == APPROVAL_REQUESTED);
    }

    public Stream<Object> deleteHearing(final UUID hearingId) {
        if(momento.isDeleted()){
            return Stream.empty();
        }
        if (this.momento.getHearing() == null) {
            return Stream.of(hearingDelegate.generateHearingIgnoredMessage("Ignoring 'deleteHearing' event as hearing not found", hearingId));
        } else if(this.hearingState == SHARED) {
            return Stream.of(hearingDelegate.generateHearingIgnoredMessage("Ignoring 'deleteHearing' event as hearing already shared", hearingId));
        }
        return apply(this.hearingDelegate.deleteHearing(hearingId));
    }

    public Stream<Object> unAllocateHearing(final UUID hearingId, final List<UUID> removedOffenceIds) {
        if (this.momento.getHearing() == null) {
            return Stream.of(hearingDelegate.generateHearingIgnoredMessage("Ignoring 'unAllocateHearing' event as hearing not found", hearingId));
        } else if(this.hearingState == SHARED) {
            return Stream.of(hearingDelegate.generateHearingIgnoredMessage("Ignoring 'unAllocateHearing' event as hearing already shared", hearingId));
        }
        return apply(this.hearingDelegate.unAllocateHearing(hearingId, removedOffenceIds));
    }

    public Stream<Object> changeNextHearingStartDate(final UUID hearingId, final UUID seedingHearingId, final ZonedDateTime nextHearingDay) {
        if (this.momento.getHearing() == null) {
            return Stream.of(hearingDelegate.generateHearingIgnoredMessage("Ignoring 'changeNextHearingStartDate' event as hearing not found", hearingId));
        }

        return apply(this.hearingDelegate.changeNextHearingStartDate(hearingId, seedingHearingId, nextHearingDay));
    }

    public Stream<Object> saveReusableInfo(final UUID hearingId, final List<ReusableInfo> reusableInfoCaches, final List<ReusableInfoResults> reusableResultInfoCaches) {
        return apply(Stream.of(reusableInfoSaved()
                .withHearingId(hearingId)
                .withPromptList(reusableInfoCaches)
                .withResultsList(reusableResultInfoCaches)
                .build()));
    }

    public Stream<Object> receiveDefendantsPartOfYouthCourtHearing(final List<UUID> defendantsInYouthCourtList) {
        return apply(Stream.of(new DefendantsInYouthCourtUpdated(defendantsInYouthCourtList, this.momento.getHearing().getId())));
    }

    public Stream<Object> extendCustodyTimeLimit(final UUID hearingId, final UUID offenceId, final LocalDate extendedTimeLimit) {
        return apply(Stream.of(CustodyTimeLimitExtended.custodyTimeLimitExtended()
                .withHearingId(hearingId)
                .withOffenceId(offenceId)
                .withExtendedTimeLimit(extendedTimeLimit)
                .build()));
    }

    public Stream<Object> stopCustodyTimeLimitClock() {

        if (!SHARED.equals(this.hearingState) || this.momento.isDeleted()) {
            return Stream.empty();
        }
        return  CustodyTimeLimitUtil.stopCTLExpiryForV2(this.momento, this.momento.getSharedResultsCommandResultLineV2s());

    }

    public Hearing getHearing() {
        return this.momento.getHearing();
    }

    private boolean isTargetValid(final HearingAggregateMomento momento, final Target newTarget, final LocalDate hearingDay) {

        if (!isValidTarget(newTarget)) {
            return false;
        }

        final Map<UUID, Target> existingTargets = momento.getMultiDayTargets().containsKey(hearingDay) ? Optional.of(momento.getMultiDayTargets().get(hearingDay)).map(e -> {
            final Map<UUID, Target> map = new HashMap<>();
            e.entrySet().stream().forEach(b -> map.put(b.getKey(), resultsSharedDelegate.convertToTarget(b.getValue())));
            return map;
        }).orElse(null) : emptyMap();


        if (isNull(existingTargets) || existingTargets.isEmpty()) {
            return true;
        }

        final BiPredicate<Target, Target> defendantOffenceMatch = this::matchDefendantOffenceTarget;

        final BiPredicate<Target, Target> applicationOffenceMatch = this::matchApplicationOffenceTarget;

        final BiPredicate<Target, Target> applicationMatch = this::matchApplicationTarget;

        /**
         * targets are sets of results stored against offences.
         * to prevent multiple targets being stored against the same offence,
         * check for existing target ID for the hearing day, against the offence and the defendant
         * ensuring that for an existing targetId, offence, defendant or application also match
         */
        if (existingTargets.containsKey(newTarget.getTargetId())) {

            final Target existingTarget = existingTargets.get(newTarget.getTargetId());

            return defendantOffenceMatch.or(applicationOffenceMatch).or(applicationMatch).test(existingTarget, newTarget);

        } else {

            final boolean defendantOffencePresentForAnotherTargetId = existingTargets.values().stream().anyMatch(existingTarget -> matchDefendantOffenceTarget(existingTarget, newTarget));

            final boolean applicationOffencePresentForAnotherTargetId = existingTargets.values().stream().anyMatch(existingTarget -> matchApplicationOffenceTarget(existingTarget, newTarget));

            final boolean applicationPresentForAnotherTargetId = existingTargets.values().stream().anyMatch(existingTarget -> matchApplicationTarget(existingTarget, newTarget));

            return !(defendantOffencePresentForAnotherTargetId || applicationOffencePresentForAnotherTargetId || applicationPresentForAnotherTargetId);
        }

    }

    private Stream<Object> saveDraftResultForHearingDay(final UUID userId, final Target target, final LocalDate hearingDay) {

        if ((VALIDATED.equals(this.hearingState) && isSameUserWhoIsAmendingSharedHearing(userId))
                || APPROVAL_REQUESTED.equals(this.hearingState)) {
            return apply(resultsSharedDelegate.hearingLocked(target.getHearingId()));
        }

        if (isSharedHearingBeingAmended() && !isSameUserWhoIsAmendingSharedHearing(userId)) {
            return apply(resultsSharedDelegate.hearingLockedByOtherUser(target.getHearingId()));
        }

        this.amendingSharedHearingUserId = userId;
        final HearingState newHearingState = getHearingState(target.getReasonsList());

        final Target targetForEvent = Target
                .target()
                .withShadowListed(target.getShadowListed())
                .withApplicationId(target.getApplicationId())
                .withDefendantId(target.getDefendantId())
                .withDraftResult(target.getDraftResult())
                .withHearingId(target.getHearingId())
                .withOffenceId(target.getOffenceId())
                .withResultLines(target.getResultLines())
                .withTargetId(target.getTargetId())
                .withHearingDay(hearingDay)
                .build();

        if (isTargetValid(momento, target, hearingDay)) {
            return apply(resultsSharedDelegate.saveDraftResult(targetForEvent, newHearingState, userId));
        }
        return apply(resultsSharedDelegate.rejectSaveDraftResult(targetForEvent));

    }

    public Stream<Object> courtListRestrictions(final uk.gov.justice.hearing.courts.CourtListRestricted courtListRestrictedCmd) {

        if (this.momento.getHearing() == null) {
            return Stream.of(hearingDelegate.generateHearingIgnoredMessage("Ignoring 'hearing.event.court-list-restricted' event as hearing not found", courtListRestrictedCmd.getHearingId()));
        }

        return apply(Stream.of(CourtListRestricted.courtListRestricted()
                .withCaseIds(courtListRestrictedCmd.getCaseIds())
                .withCourtApplicationApplicantIds(courtListRestrictedCmd.getCourtApplicationApplicantIds())
                .withCourtApplicationIds(courtListRestrictedCmd.getCourtApplicationIds())
                .withCourtApplicationRespondentIds(courtListRestrictedCmd.getCourtApplicationRespondentIds())
                .withCourtApplicationType(courtListRestrictedCmd.getCourtApplicationType())
                .withDefendantIds(courtListRestrictedCmd.getDefendantIds())
                .withRestrictCourtList(courtListRestrictedCmd.getRestrictCourtList())
                .withHearingId(courtListRestrictedCmd.getHearingId())
                .withOffenceIds(courtListRestrictedCmd.getOffenceIds())
                .withCourtApplicationType(courtListRestrictedCmd.getCourtApplicationType())
                .build()));
    }

    /**
     * Method to record the DefendantsWelshInformationRecorded event
     * @param defendantsWithWelshTranslationsCommand
     * @return Hearing Stream instance
     */
    public Stream<Object> recordDefendantsWelshTranslation(final DefendantsWithWelshTranslationsCommand defendantsWithWelshTranslationsCommand) {
        return apply(Stream.of(new DefendantsWelshInformationRecorded(defendantsWithWelshTranslationsCommand.getDefendantsWelshList())));
    }

    /**
     * Method to set the defendantsWelshInformationList when the rule is met in
     * apply
     *
     * @param defendantsWelshInformationRecorded
     */
    private void handleDefendantsWelshTranslation(final DefendantsWelshInformationRecorded defendantsWelshInformationRecorded) {
        this.defendantsWelshInformationList = defendantsWelshInformationRecorded.getDefendantsWelshInfoList();

        final List<DefendantsWithWelshTranslation> defendantsWelshRequiringList =
                defendantsWelshInformationList
                        .stream()
                        .filter(d -> d.isWelshTranslation())
                        .map(d -> {return new DefendantsWithWelshTranslation(d.getDefendantId(), d.isWelshTranslation());})
                        .collect(Collectors.toList());

        this.momento.getHearing().setDefendantsWithWelshTranslationList(defendantsWelshRequiringList);
    }


}
