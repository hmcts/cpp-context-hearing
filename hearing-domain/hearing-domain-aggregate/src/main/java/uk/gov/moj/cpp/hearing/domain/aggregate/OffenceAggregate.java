package uk.gov.moj.cpp.hearing.domain.aggregate;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingOffenceCommand;
import uk.gov.moj.cpp.hearing.command.offence.Offence;
import uk.gov.moj.cpp.hearing.domain.event.AssociateHearingIdWithOffence;
import uk.gov.moj.cpp.hearing.domain.event.InitiateHearingOffenceEnriched;
import uk.gov.moj.cpp.hearing.domain.event.DeleteOffenceFromHearings;
import uk.gov.moj.cpp.hearing.domain.event.OffencePleaUpdated;
import uk.gov.moj.cpp.hearing.domain.event.UpdateOffenceOnHearings;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.match;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.otherwiseDoNothing;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.when;

public class OffenceAggregate implements Aggregate {

    private static final long serialVersionUID = 1L;

    private OffencePleaUpdated plea;

    private List<UUID> hearingIds = new ArrayList<>();

    @Override
    public Object apply(Object event) {
        return match(event).with(
                when(OffencePleaUpdated.class).apply((offencePleaUpdated) -> this.plea = offencePleaUpdated),
                when(AssociateHearingIdWithOffence.class).apply(offence -> hearingIds.add(offence.getHearingId())),
                otherwiseDoNothing()
        );
    }

    public Stream<Object> initiateHearingOffence(InitiateHearingOffenceCommand initiateHearingOffenceCommand) {

        final Stream.Builder<Object> streamBuilder = Stream.builder();

        streamBuilder.add(AssociateHearingIdWithOffence.builder()
                .withOffenceId(initiateHearingOffenceCommand.getOffenceId())
                .withHearingId(initiateHearingOffenceCommand.getHearingId())
                .build());

        if (this.plea != null) {
            streamBuilder.add(new InitiateHearingOffenceEnriched(
                    initiateHearingOffenceCommand.getOffenceId(),
                    initiateHearingOffenceCommand.getCaseId(),
                    initiateHearingOffenceCommand.getDefendantId(),
                    initiateHearingOffenceCommand.getHearingId(),
                    plea.getHearingId(),
                    plea.getPleaDate(),
                    plea.getValue()
            ));
        }

        return apply(streamBuilder.build());
    }

    public Stream<Object> updatePlea(final UUID originHearingId, final UUID offenceId, final LocalDate pleaDate, final String pleaValue) {
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

    public Stream<Object> enrichEditOffenceCommandWithHearingIds(final Offence offence) {

        return apply(Stream.of(UpdateOffenceOnHearings.builder()
                .withId(offence.getId())
                .withOffenceCode(offence.getOffenceCode())
                .withWording(offence.getWording())
                .withStartDate(offence.getStartDate())
                .withEndDate(offence.getEndDate())
                .withCount(offence.getCount())
                .withConvictionDate(offence.getConvictionDate())
                .withHearingIds(hearingIds)
                .build()));

    }

    public Stream<Object> enrichDeleteOffenceCommandWithHearingIds(final UUID offenceId) {

        return apply(Stream.of(DeleteOffenceFromHearings.builder()
                .withId(offenceId)
                .withHearingIds(hearingIds)
                .build()));
    }
}