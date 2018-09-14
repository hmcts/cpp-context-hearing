package uk.gov.moj.cpp.hearing.domain.aggregate;

import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.match;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.otherwiseDoNothing;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.when;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.justice.json.schemas.core.Offence;
import uk.gov.moj.cpp.hearing.command.initiate.LookupPleaOnOffenceForHearingCommand;
import uk.gov.moj.cpp.hearing.domain.event.FoundHearingsForDeleteOffence;
import uk.gov.moj.cpp.hearing.domain.event.FoundHearingsForEditOffence;
import uk.gov.moj.cpp.hearing.domain.event.FoundPleaForHearingToInherit;
import uk.gov.moj.cpp.hearing.domain.event.OffencePleaUpdated;
import uk.gov.moj.cpp.hearing.domain.event.RegisteredHearingAgainstOffence;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class OffenceAggregate implements Aggregate {

    private static final long serialVersionUID = 1L;

    private OffencePleaUpdated plea;

    private List<UUID> hearingIds = new ArrayList<>();

    @Override
    public Object apply(Object event) {
        return match(event).with(
                when(OffencePleaUpdated.class).apply(offencePleaUpdated -> this.plea = offencePleaUpdated),
                when(RegisteredHearingAgainstOffence.class).apply(offence -> hearingIds.add(offence.getHearingId())),
                otherwiseDoNothing()
        );
    }

    public Stream<Object> lookupPleaForHearing(LookupPleaOnOffenceForHearingCommand lookupPleaOnOffenceForHearingCommand) {

        final Stream.Builder<Object> streamBuilder = Stream.builder();

        streamBuilder.add(RegisteredHearingAgainstOffence.builder()
                .withOffenceId(lookupPleaOnOffenceForHearingCommand.getOffenceId())
                .withHearingId(lookupPleaOnOffenceForHearingCommand.getHearingId())
                .build());

        if (this.plea != null) {
            streamBuilder.add(new FoundPleaForHearingToInherit(
                    lookupPleaOnOffenceForHearingCommand.getOffenceId(),
                    lookupPleaOnOffenceForHearingCommand.getCaseId(),
                    lookupPleaOnOffenceForHearingCommand.getDefendantId(),
                    lookupPleaOnOffenceForHearingCommand.getHearingId(),
                    plea.getHearingId(),
                    plea.getPleaDate(),
                    plea.getValue(),
                    plea.getDelegatedPowers()
            ));
        }

        return apply(streamBuilder.build());
    }

    public Stream<Object> updatePlea(final OffencePleaUpdated update) {
        return apply(Stream.of(OffencePleaUpdated.builder()
                .withHearingId(update.getHearingId())
                .withOffenceId(update.getOffenceId())
                .withPleaDate(update.getPleaDate())
                .withValue(update.getValue())
                .withDelegatedPowers(update.getDelegatedPowers())
                .build()));
    }

    public OffencePleaUpdated getPlea() {
        return plea;
    }

    public Stream<Object> lookupHearingsForEditOffenceOnOffence(final UUID defendantId, final Offence offence) {
        return apply(Stream.of(FoundHearingsForEditOffence.foundHearingsForEditOffence()
                .withHearingIds(hearingIds)
                .withDefendantId(defendantId)
                .withOffence(offence)));
    }

    public Stream<Object> lookupHearingsForDeleteOffenceOnOffence(final UUID offenceId) {

        return apply(Stream.of(FoundHearingsForDeleteOffence.builder()
                .withId(offenceId)
                .withHearingIds(hearingIds)
                .build()));
    }
}