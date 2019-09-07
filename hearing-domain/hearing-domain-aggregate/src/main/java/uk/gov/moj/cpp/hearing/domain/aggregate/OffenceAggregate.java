package uk.gov.moj.cpp.hearing.domain.aggregate;

import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.match;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.otherwiseDoNothing;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.when;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.PleaModel;
import uk.gov.justice.core.courts.Verdict;
import uk.gov.moj.cpp.hearing.domain.event.EnrichUpdatePleaWithAssociatedHearings;
import uk.gov.moj.cpp.hearing.domain.event.EnrichUpdateVerdictWithAssociatedHearings;
import uk.gov.moj.cpp.hearing.domain.event.FoundHearingsForDeleteOffence;
import uk.gov.moj.cpp.hearing.domain.event.FoundHearingsForEditOffence;
import uk.gov.moj.cpp.hearing.domain.event.FoundPleaForHearingToInherit;
import uk.gov.moj.cpp.hearing.domain.event.FoundVerdictForHearingToInherit;
import uk.gov.moj.cpp.hearing.domain.event.OffencePleaUpdated;
import uk.gov.moj.cpp.hearing.domain.event.OffenceVerdictUpdated;
import uk.gov.moj.cpp.hearing.domain.event.RegisteredHearingAgainstOffence;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OffenceAggregate implements Aggregate {

    private static final long serialVersionUID = 1L;

    private OffencePleaUpdated offencePleaUpdated;

    private OffenceVerdictUpdated offenceVerdictUpdated;

    private List<UUID> hearingIds = new ArrayList<>();

    public List<UUID> getHearingIds() {
        return this.hearingIds;
    }

    @Override
    public Object apply(final Object event) {
        return match(event).with(
                when(OffencePleaUpdated.class).apply(plea -> this.offencePleaUpdated = plea),
                when(OffenceVerdictUpdated.class).apply(verdict -> this.offenceVerdictUpdated = verdict),
                when(RegisteredHearingAgainstOffence.class).apply(offence -> hearingIds.add(offence.getHearingId())),
                otherwiseDoNothing()
        );
    }

    public Stream<Object> lookupOffenceForHearing(final UUID hearingId, final UUID offenceId) {

        final Stream.Builder<Object> streamBuilder = Stream.builder();

        streamBuilder.add(RegisteredHearingAgainstOffence.builder()
                .withOffenceId(offenceId)
                .withHearingId(hearingId)
                .build());

        if (this.offencePleaUpdated != null) {
            streamBuilder.add(new FoundPleaForHearingToInherit(
                    hearingId,
                    offencePleaUpdated.getPleaModel().getPlea()
            ));
        }

        if (this.offenceVerdictUpdated != null) {
            streamBuilder.add(new FoundVerdictForHearingToInherit(
                    hearingId,
                    offenceVerdictUpdated.getVerdict()
            ));
        }

        return apply(streamBuilder.build());
    }

    public Stream<Object> updatePlea(final UUID hearingId, final PleaModel pleaModel) {

        final Stream.Builder<Object> streamBuilder = Stream.builder();

        streamBuilder.add(OffencePleaUpdated.builder().withHearingId(hearingId).withPleaModel(pleaModel).build());

        final List<UUID> connectedHearingIds = hearingIds.stream()
                .filter(id -> !id.equals(hearingId))
                .collect(Collectors.toList());

        if (!connectedHearingIds.isEmpty()) {
            streamBuilder.add(new EnrichUpdatePleaWithAssociatedHearings(connectedHearingIds, pleaModel.getPlea()));
        }

        return apply(streamBuilder.build());
    }

    public OffencePleaUpdated getPlea() {
        return offencePleaUpdated;
    }

    public OffenceVerdictUpdated getVerdict() {
        return offenceVerdictUpdated;
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

    public Stream<Object> updateVerdict(final UUID hearingId, final Verdict verdict) {
        final Stream.Builder<Object> streamBuilder = Stream.builder();

        streamBuilder.add(new OffenceVerdictUpdated(hearingId, verdict));

        final List<UUID> connectedHearingIds = hearingIds.stream()
                .filter(id -> !id.equals(hearingId))
                .collect(Collectors.toList());

        if (!connectedHearingIds.isEmpty()) {
            streamBuilder.add(new EnrichUpdateVerdictWithAssociatedHearings(connectedHearingIds, verdict));
        }

        return apply(streamBuilder.build());
    }
}