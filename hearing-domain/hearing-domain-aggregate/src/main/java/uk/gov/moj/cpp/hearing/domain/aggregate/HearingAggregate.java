package uk.gov.moj.cpp.hearing.domain.aggregate;

import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.match;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.otherwiseDoNothing;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.when;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.justice.json.schemas.core.AttendanceDay;
import uk.gov.justice.json.schemas.core.CourtCentre;
import uk.gov.justice.json.schemas.core.Hearing;
import uk.gov.justice.json.schemas.core.HearingDay;
import uk.gov.justice.json.schemas.core.HearingLanguage;
import uk.gov.justice.json.schemas.core.HearingType;
import uk.gov.justice.json.schemas.core.JudicialRole;
import uk.gov.justice.json.schemas.core.JurisdictionType;
import uk.gov.justice.json.schemas.core.Offence;
import uk.gov.justice.json.schemas.core.Plea;
import uk.gov.justice.json.schemas.core.ResultLine;
import uk.gov.justice.json.schemas.core.Target;
import uk.gov.justice.json.schemas.core.Verdict;
import uk.gov.moj.cpp.external.domain.progression.relist.AdjournHearing;
import uk.gov.moj.cpp.hearing.command.defendant.Defendant;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.Variant;
import uk.gov.moj.cpp.hearing.command.result.SharedResultLineId;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.AdjournHearingDelegate;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.ConvictionDateDelegate;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.DefenceCounselDelegate;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.DefendantDelegate;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.HearingAggregateMomento;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.HearingDelegate;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.HearingEventDelegate;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.OffenceDelegate;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.PleaDelegate;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.ProsecutionCounselDelegate;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.ResultsSharedDelegate;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.VariantDirectoryDelegate;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.VerdictDelegate;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateAdded;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateRemoved;
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselUpsert;
import uk.gov.moj.cpp.hearing.domain.event.DefendantAttendanceUpdated;
import uk.gov.moj.cpp.hearing.domain.event.DefendantDetailsUpdated;
import uk.gov.moj.cpp.hearing.domain.event.HearingDetailChanged;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventDeleted;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventLogged;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.domain.event.InheritedPlea;
import uk.gov.moj.cpp.hearing.domain.event.InheritedVerdictAdded;
import uk.gov.moj.cpp.hearing.domain.event.NowsVariantsSavedEvent;
import uk.gov.moj.cpp.hearing.domain.event.OffenceAdded;
import uk.gov.moj.cpp.hearing.domain.event.OffenceDeleted;
import uk.gov.moj.cpp.hearing.domain.event.OffenceUpdated;
import uk.gov.moj.cpp.hearing.domain.event.PleaUpsert;
import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselUpsert;
import uk.gov.moj.cpp.hearing.domain.event.VerdictUpsert;
import uk.gov.moj.cpp.hearing.domain.event.result.DraftResultSaved;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultLinesStatusUpdated;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.eventlog.HearingEvent;
import uk.gov.moj.cpp.hearing.nows.events.NowsMaterialStatusUpdated;
import uk.gov.moj.cpp.hearing.nows.events.NowsRequested;
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

    @Override
    public Object apply(final Object event) {
        return match(event).with(
                when(HearingInitiated.class).apply(hearingDelegate::handleHearingInitiated),
                when(HearingDetailChanged.class).apply(hearingDelegate::handleHearingDetailChanged),
                when(InheritedPlea.class).apply(pleaDelegate::handleInheritedPlea),
                when(PleaUpsert.class).apply(pleaDelegate::handlePleaUpsert),
                when(ProsecutionCounselUpsert.class).apply(prosecutionCounselDelegate::handleProsecutionCounselUpsert),
                when(DefenceCounselUpsert.class).apply(defenceCounselDelegate::handleDefenceCounselUpsert),
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
                when(NowsMaterialStatusUpdated.class).apply(variantDirectoryDelegate::handleNowsMaterialStatusUpdatedEvent),
                when(DraftResultSaved.class).apply(resultsSharedDelegate::handleDraftResultShared),
                when(DefendantAttendanceUpdated.class).apply(defendantDelegate::handleDefendantAttendanceUpdated),
                otherwiseDoNothing()
        );

    }

    public Stream<Object> addProsecutionCounsel(final UUID personId, final UUID attendeeId, final UUID hearingId, final String status, final String firstName, final String lastName, final String title) {
        return apply(prosecutionCounselDelegate.addProsecutionCounsel(personId, attendeeId, hearingId, status, firstName, lastName, title));
    }

    public Stream<Object> addDefenceCounsel(final DefenceCounselUpsert defenceCounselUpsert) {
        return apply(defenceCounselDelegate.addDefenceCounsel(defenceCounselUpsert));
    }

    public Stream<Object> initiate(final Hearing hearing) {
        return apply(this.hearingDelegate.initiate(hearing));
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

    public Stream<Object> shareResults(final UUID hearingId, final uk.gov.justice.json.schemas.core.CourtClerk courtClerk, final ZonedDateTime sharedTime) {
        return apply(resultsSharedDelegate.shareResults(hearingId, courtClerk, sharedTime));
    }

    public Stream<Object> saveDraftResults(final UUID targetId, final UUID defendantId, final UUID hearingId, final UUID offenceId, final String draftResult, final List<ResultLine> resultLines) {
        return apply(resultsSharedDelegate.saveDraftResult(new Target(defendantId, draftResult, hearingId, offenceId, resultLines, targetId)));
    }

    public Stream<Object> updateResultLinesStatus(final UUID hearingId, final uk.gov.justice.json.schemas.core.CourtClerk courtClerk, final ZonedDateTime lastSharedDateTime, final List<SharedResultLineId> sharedResultLines) {
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

    public Stream<Object> generateNows(final NowsRequested nowsRequested) {
        return apply(Stream.of(nowsRequested));
    }

    public Stream<Object> saveNowsVariants(final UUID hearingId, final List<Variant> variants) {
        final NowsVariantsSavedEvent event = NowsVariantsSavedEvent.nowsVariantsSavedEvent()
                .setHearingId(hearingId)
                .setVariants(variants);
        return apply(Stream.of(event));
    }

    public Stream<Object> nowsMaterialStatusUpdated(final NowsMaterialStatusUpdated nowsRequested) {
        return apply(Stream.of(nowsRequested));
    }

    public Stream<Object> updateDefendantAttendance(final UUID hearingId, final UUID defendantId, final AttendanceDay attendanceDay) {
        return apply(this.defendantDelegate.updateDefendantAttendance(hearingId, defendantId, attendanceDay));
    }

    public Stream<Object> inheritVerdict(UUID hearingId, Verdict verdict) {
        return apply(this.verdictDelegate.inheritVerdict(hearingId, verdict));
    }
}
