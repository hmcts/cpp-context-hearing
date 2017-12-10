package uk.gov.moj.cpp.hearing.domain.aggregate;

import static java.util.stream.Stream.builder;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.match;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.otherwiseDoNothing;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.when;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.moj.cpp.external.domain.listing.Hearing;
import uk.gov.moj.cpp.hearing.command.plea.HearingUpdatePleaCommand;
import uk.gov.moj.cpp.hearing.command.plea.Offence;
import uk.gov.moj.cpp.hearing.command.plea.Plea;
import uk.gov.moj.cpp.hearing.domain.event.HearingConfirmedRecorded;
import uk.gov.moj.cpp.hearing.domain.event.HearingPleaIgnored;
import uk.gov.moj.cpp.hearing.domain.event.PleaAdded;
import uk.gov.moj.cpp.hearing.domain.event.PleaChanged;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

@SuppressWarnings({"squid:S1068","squid:S1948"})
public class HearingsPleaAggregate implements Aggregate {
    private UUID caseId;
    private String urn;
    private Boolean sendingSheetComplete;
    private Map<UUID, Hearing> hearing = new HashMap<>();
    private Map<UUID, Plea> pleas = new HashMap<>();


    @Override
    public Object apply(final Object event) {
        return match(event).with(
                when(HearingConfirmedRecorded.class).apply(this::onHearingConfirmedRecorded),
                when(PleaAdded.class).apply(this::onPleaAdded),
                when(PleaChanged.class).apply(this::onPleaChanged),
                otherwiseDoNothing());
    }


    public Stream<Object> recordHearingConfirmed(final UUID caseId, final String urn, final Hearing hearing) {
        return apply(Stream.of(new HearingConfirmedRecorded(caseId, urn, hearing)));
    }


    public Stream<Object> updatePlea(final HearingUpdatePleaCommand hearingUpdatePleaCommand) {
        if (checkOffencePleaHavingOneToOneMapping(hearingUpdatePleaCommand)) {
            return apply(Stream.of(new HearingPleaIgnored("Offence Plea association is inconsistent", hearingUpdatePleaCommand)));
        } else {
            final Builder<Object> streamBuilder = builder();
            final UUID hearingUpdatePleaCommandCaseId = hearingUpdatePleaCommand.getCaseId();
            final UUID hearingId = hearingUpdatePleaCommand.getHearingId();
            hearingUpdatePleaCommand.getDefendants().forEach(defendant -> defendant.getOffences().forEach(offence -> {
                pleas.computeIfPresent(offence.getId(), (uuid, plea) -> {
                    if (!offence.getPlea().equals(plea)) {
                        final PleaChanged pleaChange = new PleaChanged(hearingUpdatePleaCommandCaseId, hearingId, defendant.getId(), defendant.getPersonId(), offence.getId(), offence.getPlea());
                        streamBuilder.add(pleaChange);
                        return offence.getPlea();
                    }
                    return plea;
                });
                pleas.computeIfAbsent(offence.getId(), plea -> {
                    final PleaAdded pleaAdded = new PleaAdded(hearingUpdatePleaCommandCaseId, hearingId, defendant.getId(), defendant.getPersonId(), offence.getId(), offence.getPlea());
                    streamBuilder.add(pleaAdded);
                    return offence.getPlea();
                });

            }));
            return apply(streamBuilder.build());
        }

    }

    private void onHearingConfirmedRecorded(HearingConfirmedRecorded event) {
        this.caseId = event.getCaseId();
        this.urn = event.getUrn();
        hearing.put(event.getHearing().getId(), event.getHearing());
    }

    private boolean checkOffencePleaHavingOneToOneMapping(HearingUpdatePleaCommand hearingUpdatePleaCommand) {
        final Map<UUID, UUID> requestedOffencePleas = hearingUpdatePleaCommand.getDefendants().stream().map(d -> d.getOffences()).flatMap(offences -> offences.stream()).filter(off -> off.getPlea() != null)
                .collect(Collectors.toMap(Offence::getId, off -> off.getPlea().getId()));
        boolean result = false;

        for (Entry entry : requestedOffencePleas.entrySet()) {
            if (pleas.containsKey(entry.getKey()) && !pleas.get(entry.getKey()).getId().equals(entry.getValue())) {
                result = true;
            }
        }

        if (!result) {
            final int combinedOffenceIdCount = Stream.concat(requestedOffencePleas.keySet().stream(), pleas.keySet().stream()).collect(Collectors.toSet()).size();
            final int combinedPleaIdCount = Stream.concat(requestedOffencePleas.values().stream(), pleas.values().stream().map(p -> p.getId())).collect(Collectors.toSet()).size();

            result = combinedOffenceIdCount != combinedPleaIdCount;
        }
        return result;
    }

    private void onPleaAdded(PleaAdded pleaAdded) {
        this.caseId = pleaAdded.getCaseId();
        pleas.put(pleaAdded.getOffenceId(), pleaAdded.getPlea());
    }

    private void onPleaChanged(PleaChanged pleaChanged) {
        this.caseId = pleaChanged.getCaseId();
        pleas.put(pleaChanged.getOffenceId(), pleaChanged.getPlea());
    }
}
