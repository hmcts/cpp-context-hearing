package uk.gov.moj.cpp.hearing.domain.aggregate;

import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.match;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.otherwiseDoNothing;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.when;

import uk.gov.justice.core.courts.ApplicantCounsel;
import uk.gov.justice.core.courts.AttendanceDay;
import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationOutcomeType;
import uk.gov.justice.core.courts.CourtApplicationResponse;
import uk.gov.justice.core.courts.CourtCentre;
import uk.gov.justice.core.courts.CreateNowsRequest;
import uk.gov.justice.core.courts.DefenceCounsel;
import uk.gov.justice.core.courts.DelegatedPowers;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.HearingLanguage;
import uk.gov.justice.core.courts.HearingType;
import uk.gov.justice.core.courts.JudicialRole;
import uk.gov.justice.core.courts.JurisdictionType;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.Plea;
import uk.gov.justice.core.courts.ProsecutionCounsel;
import uk.gov.justice.core.courts.RespondentCounsel;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.justice.core.courts.Target;
import uk.gov.justice.core.courts.Verdict;
import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.moj.cpp.external.domain.progression.relist.AdjournHearing;
import uk.gov.moj.cpp.hearing.command.defendant.Defendant;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.Variant;
import uk.gov.moj.cpp.hearing.command.result.SharedResultLineId;
import uk.gov.moj.cpp.hearing.command.result.SharedResultsCommandResultLine;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.AdjournHearingDelegate;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.ApplicantCounselDelegate;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.ApplicationDelegate;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.ConvictionDateDelegate;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.DefenceCounselDelegate;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.DefendantDelegate;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.HearingAggregateMomento;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.HearingDelegate;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.HearingEventDelegate;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.NowDelegate;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.OffenceDelegate;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.PleaDelegate;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.ProsecutionCounselDelegate;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.RespondentCounselDelegate;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.ResultsSharedDelegate;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.VariantDirectoryDelegate;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.VerdictDelegate;
import uk.gov.moj.cpp.hearing.domain.event.ApplicantCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.ApplicantCounselRemoved;
import uk.gov.moj.cpp.hearing.domain.event.ApplicantCounselUpdated;
import uk.gov.moj.cpp.hearing.domain.event.ApplicationDetailChanged;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateAdded;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateRemoved;
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselRemoved;
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselUpdated;
import uk.gov.moj.cpp.hearing.domain.event.DefendantAdded;
import uk.gov.moj.cpp.hearing.domain.event.DefendantAttendanceUpdated;
import uk.gov.moj.cpp.hearing.domain.event.DefendantDetailsUpdated;
import uk.gov.moj.cpp.hearing.domain.event.HearingDetailChanged;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventDeleted;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventLogged;
import uk.gov.moj.cpp.hearing.domain.event.HearingExtended;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.domain.event.InheritedPlea;
import uk.gov.moj.cpp.hearing.domain.event.InheritedVerdictAdded;
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
import uk.gov.moj.cpp.hearing.nows.events.PendingNowsRequested;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@SuppressWarnings({"squid:S00107", "squid:S1602", "squid:S1188"})
public class HearingAggregate implements Aggregate {

    private static final long serialVersionUID = 3L;

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

    private final AdjournHearingDelegate adjournHearingDelegate = new AdjournHearingDelegate(momento);

    private final NowDelegate nowDelegate = new NowDelegate(momento);

    private final ApplicationDelegate applicationDelegate = new ApplicationDelegate(momento);

    private final RespondentCounselDelegate respondentCounselDelegate = new RespondentCounselDelegate(momento);

    private final ApplicantCounselDelegate applicantCounselDelegate = new ApplicantCounselDelegate(momento);

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
                when(ResultsShared.class).apply(resultsSharedDelegate::handleResultsShared),
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
                when(PendingNowsRequested.class).apply(nowDelegate::handlePendingNowsRequested),
                when(ApplicationResponseSaved.class).apply(applicationDelegate::handleApplicationResponseSaved),
                when(RespondentCounselAdded.class).apply(respondentCounselDelegate::handleRespondentCounselAdded),
                when(RespondentCounselRemoved.class).apply(respondentCounselDelegate::handleRespondentCounselRemoved),
                when(RespondentCounselUpdated.class).apply(respondentCounselDelegate::handleRespondentCounselUpdated),
                when(ApplicantCounselAdded.class).apply(applicantCounselDelegate::handleApplicantCounselAdded),
                when(ApplicantCounselRemoved.class).apply(applicantCounselDelegate::handleApplicantCounselRemoved),
                when(ApplicantCounselUpdated.class).apply(applicantCounselDelegate::handleApplicantCounselUpdated),
                when(DefendantAdded.class).apply(hearingDelegate::handleDefendantAdded),
                when(ApplicationDetailChanged.class).apply(hearingDelegate::handleApplicationDetailChanged),
                otherwiseDoNothing()
        );

    }

    public Stream<Object> addProsecutionCounsel(final ProsecutionCounsel prosecutionCounsel, final UUID hearingId) {
        return apply(prosecutionCounselDelegate.addProsecutionCounsel(prosecutionCounsel, hearingId));
    }

    public Stream<Object> removeProsecutionCounsel(final UUID id, final UUID hearingId) {
        return apply(prosecutionCounselDelegate.removeProsecutionCounsel(id, hearingId));
    }

    public Stream<Object> updateProsecutionCounsel(final ProsecutionCounsel prosecutionCounsel, final UUID hearingId) {
        return apply(prosecutionCounselDelegate.updateProsecutionCounsel(prosecutionCounsel, hearingId));
    }

    public Stream<Object> addDefenceCounsel(final DefenceCounsel defenceCounsel, final UUID hearingId) {
        return apply(defenceCounselDelegate.addDefenceCounsel(defenceCounsel, hearingId));
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
            List<Offence> offences = this.hearingDelegate.getAllOffencesMissingCount(hearing);
            if (!offences.isEmpty()) {
                return apply(this.hearingDelegate.ignoreHearingInitiate(offences, hearing.getId()));
            }
        }
        return apply(this.hearingDelegate.initiate(hearing));
    }

    public Stream<Object> extend(final UUID hearingId, final CourtApplication courtApplication) {
        return apply(this.hearingDelegate.extend(hearingId, courtApplication));
    }

    public Stream<Object> updatePlea(final UUID hearingId, final Plea plea) {
        return apply(pleaDelegate.updatePlea(hearingId, plea));
    }

    public Stream<Object> inheritPlea(final UUID hearingId, final Plea plea) {
        return apply(this.pleaDelegate.inheritPlea(hearingId, plea));
    }

    public Stream<Object> logHearingEvent(final UUID hearingId, final UUID hearingEventDefinitionId, final Boolean alterable, final UUID defenceCounselId, final HearingEvent hearingEvent) {
        return apply(this.hearingEventDelegate.logHearingEvent(hearingId, hearingEventDefinitionId, alterable, defenceCounselId, hearingEvent));
    }

    public Stream<Object> updateHearingEvents(final UUID hearingId, List<uk.gov.moj.cpp.hearing.command.updateEvent.HearingEvent> hearingEvents) {
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

    public Stream<Object> updateVerdict(final UUID hearingId, final Verdict verdict) {
        return apply(this.verdictDelegate.updateVerdict(hearingId, verdict));
    }

    public Stream<Object> shareResults(final UUID hearingId, final DelegatedPowers courtClerk, final ZonedDateTime sharedTime, final List<SharedResultsCommandResultLine> resultLines) {
        return apply(resultsSharedDelegate.shareResults(hearingId, courtClerk, sharedTime, resultLines));
    }

    public Stream<Object> saveDraftResults(final UUID applicationId, final UUID targetId, final UUID defendantId, final UUID hearingId, final UUID offenceId, final String draftResult, final List<ResultLine> resultLines) {
        return apply(resultsSharedDelegate.saveDraftResult(new Target(applicationId, defendantId, draftResult, hearingId, offenceId, resultLines, targetId)));
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

    public Stream<Object> adjournHearing(final AdjournHearing adjournHearing) {
        return apply(this.adjournHearingDelegate.adjournHearing(adjournHearing));
    }

    public Stream<Object> saveNowsVariants(final UUID hearingId, final List<Variant> variants) {
        final NowsVariantsSavedEvent event = NowsVariantsSavedEvent.nowsVariantsSavedEvent()
                .setHearingId(hearingId)
                .setVariants(variants);
        return apply(Stream.of(event));
    }


    public Stream<Object> updateDefendantAttendance(final UUID hearingId, final UUID defendantId, final AttendanceDay attendanceDay) {
        return apply(this.defendantDelegate.updateDefendantAttendance(hearingId, defendantId, attendanceDay));
    }

    public Stream<Object> inheritVerdict(UUID hearingId, Verdict verdict) {
        return apply(this.verdictDelegate.inheritVerdict(hearingId, verdict));
    }

    public Stream<Object> registerPendingNowsRequest(final CreateNowsRequest nowsRequest, final List<Target> targets) {
        return apply(this.nowDelegate.registerPendingNowsRequest(nowsRequest, targets));
    }

    public Stream<Object> applyAccountNumber(final UUID requestId, final String accountNumber) {
        return apply(this.nowDelegate.applyAccountNumber(requestId, accountNumber));
    }

    public Stream<Object> recordEnforcementError(final UUID requestId, final String errorCode, final String errorMessage) {
        return apply(this.nowDelegate.recordEnforcementError(requestId, errorCode, errorMessage));
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
}
