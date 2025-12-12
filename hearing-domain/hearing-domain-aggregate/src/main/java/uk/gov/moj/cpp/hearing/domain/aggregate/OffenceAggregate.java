package uk.gov.moj.cpp.hearing.domain.aggregate;

import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.match;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.otherwiseDoNothing;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.when;

import uk.gov.justice.core.courts.PleaModel;
import uk.gov.justice.core.courts.Verdict;
import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.moj.cpp.hearing.domain.event.EnrichUpdatePleaWithAssociatedHearings;
import uk.gov.moj.cpp.hearing.domain.event.EnrichUpdateVerdictWithAssociatedHearings;
import uk.gov.moj.cpp.hearing.domain.event.EnrichAssociatedHearingsWithIndicatedPlea;
import uk.gov.moj.cpp.hearing.domain.event.HearingDeletedForOffence;
import uk.gov.moj.cpp.hearing.domain.event.HearingMarkedAsDuplicateForOffence;
import uk.gov.moj.cpp.hearing.domain.event.HearingRemovedForOffence;
import uk.gov.moj.cpp.hearing.domain.event.OffencePleaUpdated;
import uk.gov.moj.cpp.hearing.domain.event.OffenceVerdictUpdated;
import uk.gov.moj.cpp.hearing.domain.event.RegisteredHearingAgainstOffence;
import uk.gov.moj.cpp.hearing.domain.event.RegisteredHearingAgainstOffenceV2;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

public class OffenceAggregate implements Aggregate {

    private static final long serialVersionUID = 2L;

    private OffencePleaUpdated offencePleaUpdated;

    private OffenceVerdictUpdated offenceVerdictUpdated;

    private List<UUID> hearingIds = new ArrayList<>();

    @SuppressWarnings("squid:S2250")
    @Override
    public Object apply(final Object event) {
        return match(event).with(
                when(OffencePleaUpdated.class).apply(plea -> this.offencePleaUpdated = plea),
                when(OffenceVerdictUpdated.class).apply(verdict -> this.offenceVerdictUpdated = verdict),
                when(RegisteredHearingAgainstOffence.class).apply(offence -> hearingIds.add(offence.getHearingId())),
                when(RegisteredHearingAgainstOffenceV2.class).apply(offence -> hearingIds.addAll(offence.getHearingIds())),
                when(HearingMarkedAsDuplicateForOffence.class).apply(e -> hearingIds.remove(e.getHearingId())),
                when(HearingDeletedForOffence.class).apply(e -> hearingIds.remove(e.getHearingId())),
                when(HearingRemovedForOffence.class).apply(e -> hearingIds.remove(e.getHearingId())),
                otherwiseDoNothing()
        );
    }

    public Stream<Object> lookupOffenceForHearing(final UUID hearingId, final UUID offenceId) {

        final Stream.Builder<Object> streamBuilder = Stream.builder();

        streamBuilder.add(RegisteredHearingAgainstOffence.builder()
                .withOffenceId(offenceId)
                .withHearingId(hearingId)
                .build());

        return apply(streamBuilder.build());
    }

    public Stream<Object> lookupOffenceForHearingV2(final List<UUID> hearingIds, final UUID offenceId) {

        final Stream.Builder<Object> streamBuilder = Stream.builder();

        streamBuilder.add(RegisteredHearingAgainstOffenceV2.builder()
                .withOffenceId(offenceId)
                .withHearingIds(hearingIds)
                .build());

        return apply(streamBuilder.build());
    }

    public Stream<Object> updatePlea(final UUID hearingId, final PleaModel pleaModel) {

        final Stream.Builder<Object> streamBuilder = Stream.builder();

        streamBuilder.add(OffencePleaUpdated.builder().withHearingId(hearingId).withPleaModel(pleaModel).build());

        final List<UUID> connectedHearingIds = hearingIds.stream()
                .filter(id -> !id.equals(hearingId))
                .collect(Collectors.toList());

        if (!connectedHearingIds.isEmpty() && pleaModel.getPlea() != null && StringUtils.isNotEmpty(pleaModel.getPlea().getPleaValue())) {
            streamBuilder.add(new EnrichUpdatePleaWithAssociatedHearings(connectedHearingIds, pleaModel.getPlea()));
        }

        if (!connectedHearingIds.isEmpty() && pleaModel.getIndicatedPlea() != null && pleaModel.getIndicatedPlea().getIndicatedPleaValue() != null) {
            streamBuilder.add(new EnrichAssociatedHearingsWithIndicatedPlea(connectedHearingIds, pleaModel.getIndicatedPlea()));
        }

        return apply(streamBuilder.build());
    }

    public OffencePleaUpdated getPlea() {
        return offencePleaUpdated;
    }

    public OffenceVerdictUpdated getVerdict() {
        return offenceVerdictUpdated;
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

    public Stream<Object> markHearingAsDuplicate(final UUID offenceId, final UUID hearingId) {
        return apply(Stream.of(new HearingMarkedAsDuplicateForOffence(offenceId, hearingId)));
    }

    public Stream<Object> deleteHearingForOffence(final UUID offenceId, final UUID hearingId) {
        return apply(Stream.of(new HearingDeletedForOffence(offenceId, hearingId)));
    }

    public Stream<Object> removeHearingForOffence(final UUID offenceId, final UUID hearingId) {
        return apply(Stream.of(new HearingRemovedForOffence(offenceId, hearingId)));
    }
}
