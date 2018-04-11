package uk.gov.moj.cpp.hearing.domain.aggregate;

import static java.util.stream.Stream.builder;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.match;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.otherwiseDoNothing;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.when;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.justice.progression.events.SendingSheetCompleted;
import uk.gov.moj.cpp.external.domain.listing.Hearing;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.Defendant;
import uk.gov.moj.cpp.hearing.command.plea.HearingUpdatePleaCommand;
import uk.gov.moj.cpp.hearing.command.plea.Offence;
import uk.gov.moj.cpp.hearing.command.plea.Plea;
import uk.gov.moj.cpp.hearing.domain.event.HearingConfirmedRecorded;
import uk.gov.moj.cpp.hearing.domain.event.HearingPleaUpdated;
import uk.gov.moj.cpp.hearing.domain.event.HearingUpdatePleaIgnored;
import uk.gov.moj.cpp.hearing.domain.event.MagsCourtHearingRecorded;
import uk.gov.moj.cpp.hearing.domain.event.NewMagsCourtHearingRecorded;
import uk.gov.moj.cpp.hearing.domain.event.PleaAdded;
import uk.gov.moj.cpp.hearing.domain.event.PleaChanged;
import uk.gov.moj.cpp.hearing.domain.event.SendingSheetCompletedPreviouslyRecorded;
import uk.gov.moj.cpp.hearing.domain.event.SendingSheetCompletedRecorded;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

@SuppressWarnings({"squid:S1068", "squid:S1948"})
public class CaseAggregate implements Aggregate {

    private Boolean sendingSheetCompleteProcessed = false;

    @Override
    public Object apply(final Object event) {
        return match(event).with(
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
}
