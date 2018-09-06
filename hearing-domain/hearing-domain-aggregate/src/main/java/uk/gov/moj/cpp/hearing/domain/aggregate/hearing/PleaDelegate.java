package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import uk.gov.justice.json.schemas.core.DelegatedPowers;
import uk.gov.justice.json.schemas.core.Offence;
import uk.gov.justice.json.schemas.core.PleaValue;
import uk.gov.justice.json.schemas.core.ProsecutionCase;
import uk.gov.moj.cpp.hearing.command.initiate.UpdateHearingWithInheritedPleaCommand;
import uk.gov.moj.cpp.hearing.domain.Plea;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateAdded;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateRemoved;
import uk.gov.moj.cpp.hearing.domain.event.InheritedPlea;
import uk.gov.moj.cpp.hearing.domain.event.PleaUpsert;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public class PleaDelegate implements Serializable {

    private final HearingAggregateMomento momento;

    public PleaDelegate(final HearingAggregateMomento momento) {
        this.momento = momento;
    }

    public void handleInheritedPlea(final InheritedPlea inheritedPlea) {
        this.momento.getPleas().computeIfAbsent(inheritedPlea.getOffenceId(), offenceId -> Plea.plea()
                .setOriginHearingId(inheritedPlea.getHearingId())
                .setOffenceId(offenceId)
                .setValue(inheritedPlea.getValue())
                .setPleaDate(inheritedPlea.getPleaDate()));
    }

    public void handlePleaUpsert(final PleaUpsert pleaUpsert) {
        this.momento.getPleas().put(pleaUpsert.getOffenceId(),
                Plea.plea()
                        .setOriginHearingId(pleaUpsert.getHearingId())
                        .setOffenceId(pleaUpsert.getOffenceId())
                        .setValue(pleaUpsert.getValue())
                        .setPleaDate(pleaUpsert.getPleaDate())
        );
    }

    public Stream<Object> inheritPlea(final UpdateHearingWithInheritedPleaCommand command) {
        return Stream.of(InheritedPlea.inheritedPlea()
                .setOffenceId(command.getOffenceId())
                .setCaseId(command.getCaseId())
                .setDefendantId(command.getDefendantId())
                .setHearingId(command.getHearingId())
                .setOriginHearingId(command.getOriginHearingId())
                .setPleaDate(command.getPleaDate())
                .setValue(command.getValue())
                .setDelegatedPowers(command.getDelegatedPowers())
        );
    }

    public Stream<Object> updatePlea(final UUID hearingId, final UUID offenceId, final LocalDate pleaDate,
                                     final PleaValue pleaValue, final DelegatedPowers delegatedPowers) {

        UUID caseId = null;
        for (final ProsecutionCase prosecutionCase : this.momento.getHearing().getProsecutionCases()) {
            final Optional<Offence> offence = prosecutionCase.getDefendants().stream()
                    .flatMap(d -> d.getOffences().stream())
                    .filter(o -> offenceId.equals(o.getId()))
                    .findFirst();
            if (offence.isPresent()) {
                caseId = prosecutionCase.getId();
                break;
            }
        }

        final List<Object> events = new ArrayList<>();

        if (caseId == null) {
            throw new IllegalArgumentException("Offence not found, so caseId cannot be determined for " + offenceId);
        }


        events.add(PleaUpsert.pleaUpsert()
                .setHearingId(hearingId)
                .setOffenceId(offenceId)
                .setPleaDate(pleaDate)
                .setValue(pleaValue)
                .setDelegatedPowers(delegatedPowers));
        events.add(pleaValue == PleaValue.GUILTY ?
                ConvictionDateAdded.convictionDateAdded()
                        .setCaseId(caseId)
                        .setHearingId(hearingId)
                        .setOffenceId(offenceId)
                        .setConvictionDate(pleaDate) :
                ConvictionDateRemoved.convictionDateRemoved()
                        .setCaseId(caseId)
                        .setHearingId(hearingId)
                        .setOffenceId(offenceId)
        );
        return events.stream();
    }


}
