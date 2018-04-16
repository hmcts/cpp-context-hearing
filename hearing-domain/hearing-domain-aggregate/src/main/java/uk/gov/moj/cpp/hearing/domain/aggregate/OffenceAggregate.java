package uk.gov.moj.cpp.hearing.domain.aggregate;

import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.match;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.otherwiseDoNothing;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.when;

import java.time.LocalDate;
import java.util.UUID;
import java.util.stream.Stream;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingOffenceCommand;
import uk.gov.moj.cpp.hearing.domain.event.InitiateHearingOffenceEnriched;
import uk.gov.moj.cpp.hearing.domain.event.OffencePleaUpdated;

public class OffenceAggregate implements Aggregate {

    private static final long serialVersionUID = 1L;
    
    private OffencePleaUpdated plea;

    @Override
    public Object apply(Object event) {
        return match(event).with(
                when(OffencePleaUpdated.class).apply((offencePleaUpdated) -> this.plea = offencePleaUpdated),
                otherwiseDoNothing()
        );
    }

    public Stream<Object> initiateHearingOffence(InitiateHearingOffenceCommand initiateHearingOffenceCommand) {
        if (this.plea != null) {
            return apply(Stream.of(new InitiateHearingOffenceEnriched(
                    initiateHearingOffenceCommand.getOffenceId(),
                    initiateHearingOffenceCommand.getCaseId(),
                    initiateHearingOffenceCommand.getDefendantId(),
                    initiateHearingOffenceCommand.getHearingId(),
                    plea.getHearingId(),
                    plea.getPleaDate(),
                    plea.getValue()
            )));
        }
        return apply(Stream.empty());
    }

    public Stream<Object> updatePlea(final UUID originHearingId, final UUID offenceId, final LocalDate pleaDate,
            final String pleaValue) {
        return apply(Stream.of(OffencePleaUpdated.builder()
                    .withHearingId(originHearingId)
                    .withOffenceId(offenceId)
                    .withPleaDate(pleaDate)
                    .withValue(pleaValue)
                    .build()));
    }

    public OffencePleaUpdated getPlea() {
        return plea;
    }
}
