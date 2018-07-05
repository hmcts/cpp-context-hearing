package uk.gov.moj.cpp.hearing.domain.aggregate;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.moj.cpp.hearing.command.DefendantId;
import uk.gov.moj.cpp.hearing.command.defenceCounsel.AddDefenceCounselCommand;
import uk.gov.moj.cpp.hearing.command.defendant.CaseDefendantDetailsWithHearingCommand;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.initiate.Judge;
import uk.gov.moj.cpp.hearing.command.initiate.UpdateHearingWithInheritedPleaCommand;
import uk.gov.moj.cpp.hearing.command.logEvent.CorrectLogEventCommand;
import uk.gov.moj.cpp.hearing.command.logEvent.LogEventCommand;
import uk.gov.moj.cpp.hearing.command.nows.NowVariantUtil;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.SaveNowsVariantsCommand;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.VariantKey;
import uk.gov.moj.cpp.hearing.command.offence.UpdatedOffence;
import uk.gov.moj.cpp.hearing.command.prosecutionCounsel.AddProsecutionCounselCommand;
import uk.gov.moj.cpp.hearing.command.result.ShareResultsCommand;
import uk.gov.moj.cpp.hearing.command.result.UpdateResultLinesStatusCommand;
import uk.gov.moj.cpp.hearing.command.verdict.Verdict;
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
import uk.gov.moj.cpp.hearing.domain.event.AttendeeDeleted;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateAdded;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateRemoved;
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselUpsert;
import uk.gov.moj.cpp.hearing.domain.event.DefendantDetailsUpdated;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventDeleted;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventLogged;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.domain.event.InheritedPlea;
import uk.gov.moj.cpp.hearing.domain.event.NowsVariantsSavedEvent;
import uk.gov.moj.cpp.hearing.domain.event.OffenceAdded;
import uk.gov.moj.cpp.hearing.domain.event.OffenceDeleted;
import uk.gov.moj.cpp.hearing.domain.event.OffenceUpdated;
import uk.gov.moj.cpp.hearing.domain.event.PleaUpsert;
import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselUpsert;
import uk.gov.moj.cpp.hearing.domain.event.VerdictUpsert;
import uk.gov.moj.cpp.hearing.domain.event.WitnessAdded;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultLinesStatusUpdated;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.nows.events.NowsMaterialStatusUpdated;
import uk.gov.moj.cpp.hearing.nows.events.NowsRequested;

import javax.json.JsonObject;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.match;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.otherwiseDoNothing;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.when;

@SuppressWarnings({"squid:S00107", "squid:S1602", "squid:S1188", "pmd:BeanMembersShouldSerialize"})
public class NewModelHearingAggregate implements Aggregate {

    private static final long serialVersionUID = 1L;

    private final HearingAggregateMomento momento = new HearingAggregateMomento();

    private final transient HearingDelegate hearingDelegate = new HearingDelegate(momento);

    private final transient PleaDelegate pleaDelegate = new PleaDelegate(momento);

    private final transient ProsecutionCounselDelegate prosecutionCounselDelegate = new ProsecutionCounselDelegate(momento);

    private final transient DefenceCounselDelegate defenceCounselDelegate = new DefenceCounselDelegate(momento);

    private final transient HearingEventDelegate hearingEventDelegate = new HearingEventDelegate(momento);

    private final transient VerdictDelegate verdictDelegate = new VerdictDelegate(momento);

    private final transient ResultsSharedDelegate resultsSharedDelegate = new ResultsSharedDelegate(momento);

    private final transient ConvictionDateDelegate convictionDateDelegate = new ConvictionDateDelegate(momento);

    private final transient DefendantDelegate defendantDelegate = new DefendantDelegate(momento);

    private final transient OffenceDelegate offenceDelegate = new OffenceDelegate(momento);

    private final transient VariantDirectoryDelegate variantDirectoryDelegate = new VariantDirectoryDelegate(momento);

    @Override
    public Object apply(final Object event) {
        return match(event).with(
                when(HearingInitiated.class).apply(hearingDelegate::handleHearingInitiated),
                when(InheritedPlea.class).apply(pleaDelegate::handleInheritedPlea),
                when(PleaUpsert.class).apply(pleaDelegate::handlePleaUpsert),
                when(ProsecutionCounselUpsert.class).apply(prosecutionCounselDelegate::handleProsecutionCounselUpsert),
                when(DefenceCounselUpsert.class).apply(defenceCounselDelegate::handleDefenceCounselUpsert),
                when(HearingEventLogged.class).apply(hearingEventDelegate::handleHearingEventLogged),
                when(HearingEventDeleted.class).apply(hearingEventDelegate::handleHearingEventDeleted),
                when(ResultsShared.class).apply(resultsSharedDelegate::handleResultsShared),
                when(ResultLinesStatusUpdated.class).apply(resultsSharedDelegate::handleResultLinesStatusUpdated),
                when(VerdictUpsert.class).apply(verdictDelegate::handleVerdictUpsert),
                when(ConvictionDateAdded.class).apply(convictionDateDelegate::handleConvictionDateAdded),
                when(ConvictionDateRemoved.class).apply(convictionDateDelegate::handleConvictionDateRemoved),
                when(DefendantDetailsUpdated.class).apply(defendantDelegate::handleDefendantDetailsUpdated),
                when(OffenceAdded.class).apply(offenceDelegate::handleOffenceAdded),
                when(OffenceUpdated.class).apply(offenceDelegate::handleOffenceUpdated),
                when(OffenceDeleted.class).apply(offenceDelegate::handleOffenceDeleted),
                when(NowsVariantsSavedEvent.class).apply(variantDirectoryDelegate::handleNowsVariantsSavedEvent),
                otherwiseDoNothing()
        );
    }

    public Stream<Object> addProsecutionCounsel(final AddProsecutionCounselCommand prosecutionCounselCommand) {
        return apply(prosecutionCounselDelegate.addProsecutionCounsel(prosecutionCounselCommand));
    }

    public Stream<Object> addDefenceCounsel(final AddDefenceCounselCommand defenceCounselCommand) {
        return apply(defenceCounselDelegate.addDefenceCounsel(defenceCounselCommand));
    }

    public Stream<Object> initiate(final InitiateHearingCommand initiateHearingCommand) {
        return apply(this.hearingDelegate.initiate(initiateHearingCommand));
    }

    public Stream<Object> updatePlea(final UUID hearingId, final UUID offenceId, final LocalDate pleaDate, final String pleaValue) {
        return apply(pleaDelegate.updatePlea(hearingId, offenceId, pleaDate, pleaValue));
    }

    public Stream<Object> inheritPlea(final UpdateHearingWithInheritedPleaCommand command) {
        return apply(this.pleaDelegate.inheritPlea(command));
    }

    public Stream<Object> logHearingEvent(final LogEventCommand logEventCommand) {
        return apply(this.hearingEventDelegate.logHearingEvent(logEventCommand));
    }

    public Stream<Object> updateHearingEvents(final JsonObject payload) {
        return this.apply(this.hearingEventDelegate.updateHearingEvents(payload));
    }

    public Stream<Object> correctHearingEvent(final CorrectLogEventCommand logEventCommand) {
        return apply(this.hearingEventDelegate.correctHearingEvent(logEventCommand));
    }

    public Stream<Object> updateHearingDetails(final UUID id, final String type, final UUID courtRoomId, final String courtRoomName, final Judge judge, final List<ZonedDateTime> hearingDays) {
        return apply(this.hearingDelegate.updateHearingDetails(id, type, courtRoomId, courtRoomName, judge, hearingDays));
    }

    public Stream<Object> updateVerdict(final UUID hearingId, final UUID caseId, final UUID offenceId, final Verdict verdict) {
        return apply(this.verdictDelegate.updateVerdict(hearingId, caseId, offenceId, verdict));
    }

    public Stream<Object> shareResults(final ShareResultsCommand command, final ZonedDateTime sharedTime) {
        return apply(resultsSharedDelegate.shareResults(command, sharedTime));
    }

    public Stream<Object> updateResultLinesStatus(final UpdateResultLinesStatusCommand command) {
        return apply(resultsSharedDelegate.updateResultLinesStatus(command));
    }

    public Stream<Object> updateDefendantDetails(final CaseDefendantDetailsWithHearingCommand command) {
        return apply(this.defendantDelegate.updateDefendantDetails(command));
    }

    public Stream<Object> addOffence(final UUID hearingId, final UUID defendantId, final UUID caseId, final UpdatedOffence offence) {
        return apply(this.offenceDelegate.addOffence(hearingId, defendantId, caseId, offence));
    }

    public Stream<Object> updateOffence(final UUID hearingId, final UpdatedOffence offence) {
        return apply(this.offenceDelegate.updateOffence(hearingId, offence));
    }

    public Stream<Object> deleteOffence(final UUID offenceId, final UUID hearingId) {
        return apply(this.offenceDelegate.deleteOffence(offenceId, hearingId));
    }

    public Stream<Object> addWitness(final UUID hearingId, final UUID witnessId, final String type, final String classification, final String title, final String firstName, final String lastName, final List<DefendantId> defendantIdList) {
        return apply(Stream.of(new WitnessAdded(
                witnessId,
                hearingId,
                type,
                classification,
                title,
                firstName,
                lastName,
                defendantIdList.stream()
                        .map(DefendantId::getDefendantId)
                        .collect(Collectors.toList())
        )));
    }

    public Stream<Object> generateNows(final NowsRequested nowsRequested) {
        return apply(Stream.of(nowsRequested));
    }


    public Stream<Object> saveNowsVariants(final SaveNowsVariantsCommand command) {
        final NowsVariantsSavedEvent event = NowsVariantsSavedEvent.nowsVariantsSavedEvent()
                .setHearingId(command.getHearingId())
                .setVariants(command.getVariants());
        return apply(Stream.of(event));
    }

    public Stream<Object> nowsMaterialStatusUpdated(final NowsMaterialStatusUpdated nowsRequested) {
        return apply(Stream.of(nowsRequested));
    }

    public Stream<Object> deleteAtendee(final AttendeeDeleted attendeeDeleted) {
        return apply(Stream.of(attendeeDeleted));
    }


    public static final class HearingEvent implements Serializable {

        private static final long serialVersionUID = 1L;
        private final HearingEventLogged hearingEventLogged;
        private boolean deleted;

        public HearingEvent(final HearingEventLogged hearingEventLogged) {
            this.hearingEventLogged = hearingEventLogged;
        }

        public boolean isDeleted() {
            return deleted;
        }

        public void setDeleted(final boolean deleted) {
            this.deleted = deleted;
        }

        public HearingEventLogged getHearingEventLogged() {
            return hearingEventLogged;
        }
    }

    public static class VariantKeyHolder implements Serializable {
        private final VariantKey variantKey;

        public VariantKeyHolder(final VariantKey variantKey) {
            this.variantKey = variantKey;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof VariantKeyHolder) {
                return NowVariantUtil.areEqual(((VariantKeyHolder) o).variantKey, this.variantKey);
            } else {
                return super.equals(o);
            }
        }

        @Override
        public int hashCode() {
            return variantKey.hashCode();
        }
    }

}
