package uk.gov.moj.cpp.hearing.domain.aggregate;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingOffenceCommand;
import uk.gov.moj.cpp.hearing.command.plea.Plea;
import uk.gov.moj.cpp.hearing.domain.event.InitiateHearingOffenceEnriched;
import uk.gov.moj.cpp.hearing.domain.event.OffencePleaUpdated;

import java.util.UUID;
import java.util.stream.Stream;

import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.match;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.otherwiseDoNothing;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.when;

public class OffenceAggregate implements Aggregate {

    private OffencePleaUpdated plea;

    @Override
    public Object apply(Object event) {
        return match(event).with(
                when(OffencePleaUpdated.class).apply(this::onOffencePleaUpdated),
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
                    plea.getOriginHearingId(),
                    plea.getPleaDate(),
                    plea.getValue()
            )));
        }
        return apply(Stream.empty());
    }

    public Stream<Object> updatePlea(UUID originHearingId, UUID offenceId, Plea plea) {

        return apply(Stream.of(
                new OffencePleaUpdated(
                        originHearingId,
                        offenceId,
                        plea.getPleaDate(),
                        plea.getValue()
                )
        ));
    }

    private void onOffencePleaUpdated(OffencePleaUpdated offencePleaUpdated) {
        this.plea = offencePleaUpdated;
    }

    public OffencePleaUpdated getPlea() {
        return plea;
    }
}
