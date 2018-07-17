package uk.gov.moj.cpp.hearing.domain.aggregate;

import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.match;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.otherwiseDoNothing;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.when;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.justice.progression.events.SendingSheetCompleted;
import uk.gov.moj.cpp.hearing.command.initiate.RegisterHearingAgainstCaseCommand;
import uk.gov.moj.cpp.hearing.domain.event.RegisteredHearingAgainstCase;
import uk.gov.moj.cpp.hearing.domain.event.SendingSheetCompletedPreviouslyRecorded;
import uk.gov.moj.cpp.hearing.domain.event.SendingSheetCompletedRecorded;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@SuppressWarnings({"squid:S1068", "squid:S1948"})
public class CaseAggregate implements Aggregate {

    private static final long serialVersionUID = 1L;

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

    public Stream<Object> registerHearingId(RegisterHearingAgainstCaseCommand command) {
        return apply(Stream.of(
                RegisteredHearingAgainstCase.builder()
                        .withCaseId(command.getCaseId())
                        .withHearingId(command.getHearingId())
                        .build()));
    }
}
