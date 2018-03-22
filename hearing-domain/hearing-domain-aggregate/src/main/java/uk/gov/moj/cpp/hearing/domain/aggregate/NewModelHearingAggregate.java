package uk.gov.moj.cpp.hearing.domain.aggregate;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingOffencePleaCommand;
import uk.gov.moj.cpp.hearing.domain.event.InitiateHearingOffencePlead;
import uk.gov.moj.cpp.hearing.domain.event.Initiated;

import java.util.stream.Stream;

import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.match;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.otherwiseDoNothing;


public class NewModelHearingAggregate implements Aggregate {

    @Override
    public Object apply(final Object event) {
        return match(event).with(
                otherwiseDoNothing()
        );
    }

    public Stream<Object> initiate(InitiateHearingCommand initiateHearingCommand) {
        return apply(Stream.of(new Initiated(initiateHearingCommand.getCases(), initiateHearingCommand.getHearing())));
    }

    public Stream<Object> initiateHearingOffencePlea(InitiateHearingOffencePleaCommand initiateHearingOffencePleaCommand) {
        return apply(Stream.of(new InitiateHearingOffencePlead(
                initiateHearingOffencePleaCommand.getOffenceId(),
                initiateHearingOffencePleaCommand.getCaseId(),
                initiateHearingOffencePleaCommand.getDefendantId(),
                initiateHearingOffencePleaCommand.getHearingId(),
                initiateHearingOffencePleaCommand.getOriginHearingId(),
                initiateHearingOffencePleaCommand.getPleaDate(),
                initiateHearingOffencePleaCommand.getValue()
        )));
    }
}
