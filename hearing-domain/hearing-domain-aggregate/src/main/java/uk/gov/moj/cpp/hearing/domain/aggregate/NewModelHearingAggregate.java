package uk.gov.moj.cpp.hearing.domain.aggregate;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.domain.event.Initiated;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.match;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.otherwiseDoNothing;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.when;


public class NewModelHearingAggregate implements Aggregate {

    private UUID hearingId;

    private Map<UUID, Offence> offences = new HashMap<>();

    @Override
    public Object apply(final Object event) {
        return match(event).with(
                when(Initiated.class).apply(this::onHearingInitiated),
                otherwiseDoNothing()
        );
    }

    private void onHearingInitiated(Initiated initiated) {
        this.hearingId = initiated.getHearing().getId();

        initiated.getHearing().getDefendants()
                .stream()
                .flatMap(defendant -> defendant.getOffences().stream())
                .forEach(offence -> {
            offences.put(offence.getId(), new Offence(offence.getCaseId(), offence.getId()));
        });
    }

    public Stream<Object> initiate(InitiateHearingCommand initiateHearingCommand) {
        return apply(Stream.of(new Initiated(initiateHearingCommand.getCases(), initiateHearingCommand.getHearing())));
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public Map<UUID, Offence> getOffences() {
        return offences;
    }

    public static class Offence {
        private UUID caseId;

        private UUID offenceId;

        public Offence(UUID caseId, UUID offenceId) {
            this.caseId = caseId;
            this.offenceId = offenceId;
        }

        public UUID getCaseId() {
            return caseId;
        }

        public UUID getOffenceId() {
            return offenceId;
        }
    }

}
