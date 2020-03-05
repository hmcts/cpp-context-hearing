package uk.gov.moj.cpp.hearing.domain.aggregate;

import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.match;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.otherwiseDoNothing;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.when;

import uk.gov.justice.core.courts.Marker;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.justice.progression.events.SendingSheetCompleted;
import uk.gov.moj.cpp.hearing.domain.event.CaseDefendantsUpdated;
import uk.gov.moj.cpp.hearing.domain.event.CaseEjected;
import uk.gov.moj.cpp.hearing.domain.event.CaseMarkersEnrichedWithAssociatedHearings;
import uk.gov.moj.cpp.hearing.domain.event.RegisteredHearingAgainstCase;
import uk.gov.moj.cpp.hearing.domain.event.SendingSheetCompletedPreviouslyRecorded;
import uk.gov.moj.cpp.hearing.domain.event.SendingSheetCompletedRecorded;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@SuppressWarnings({"squid:S1068", "squid:S1948"})
public class CaseAggregate implements Aggregate {

    private static final long serialVersionUID = 101L;

    private Boolean sendingSheetCompleteProcessed = false;

    private List<UUID> hearingIds = new ArrayList<>();

    @Override
    public Object apply(final Object event) {
        return match(event).with(
                when(RegisteredHearingAgainstCase.class).apply(e -> hearingIds.add(e.getHearingId())),
                when(SendingSheetCompletedRecorded.class).apply(e -> sendingSheetCompleteProcessed = true),
                otherwiseDoNothing());
    }

    public Stream<Object> recordSendingSheetComplete(final SendingSheetCompleted sendingSheetCompleted) {
        if (!sendingSheetCompleteProcessed) {
            return apply(Stream.of(new SendingSheetCompletedRecorded(sendingSheetCompleted.getCrownCourtHearing(), sendingSheetCompleted.getHearing())));
        } else {
            return apply(Stream.of(new SendingSheetCompletedPreviouslyRecorded(sendingSheetCompleted.getCrownCourtHearing(), sendingSheetCompleted.getHearing())));
        }
    }

    public Stream<Object> registerHearingId(final UUID caseId, final UUID hearingId) {
        return apply(Stream.of(
                RegisteredHearingAgainstCase.builder()
                        .withCaseId(caseId)
                        .withHearingId(hearingId)
                        .build()));
    }

    public Stream<Object> ejectCase(final UUID prosecutionCaseId, final List<UUID> hearingIds){
        if(hearingIds.isEmpty() && !this.hearingIds.isEmpty()) {
            return apply(Stream.of(CaseEjected.aCaseEjected().withHearingIds(this.hearingIds).withProsecutionCaseId(prosecutionCaseId).build()));
        } else if(hearingIds.isEmpty() && this.hearingIds.isEmpty()) {
            return apply(Stream.empty());
        }
        else {
            return apply(Stream.of(CaseEjected.aCaseEjected().withHearingIds(hearingIds).withProsecutionCaseId(prosecutionCaseId).build()));
        }
    }

    public Stream<Object> caseDefendantsUpdated(final ProsecutionCase prosecutionCase){
        if(!this.hearingIds.isEmpty()) {
            return apply(Stream.of(CaseDefendantsUpdated.caseDefendantsUpdatd().withHearingIds(hearingIds).withProsecutionCase(prosecutionCase).build()));
        } else {
            return apply(Stream.empty());
        }
    }
    public List<UUID> getHearingIds() {
        return hearingIds;
    }

    public Stream<Object> enrichUpdateCaseMarkersWithHearingIds(final UUID prosecutionCaseId, final List<Marker> markers) {
        if (!hearingIds.isEmpty()) {
            return apply(Stream.of(new
                    CaseMarkersEnrichedWithAssociatedHearings(prosecutionCaseId, hearingIds, markers)));
        } else {
            return Stream.empty();
        }
    }
}
