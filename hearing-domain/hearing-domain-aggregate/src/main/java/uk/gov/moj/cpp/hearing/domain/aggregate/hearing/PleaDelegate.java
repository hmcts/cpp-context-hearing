package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import uk.gov.justice.json.schemas.core.Plea;
import uk.gov.justice.json.schemas.core.PleaValue;
import uk.gov.justice.json.schemas.core.ProsecutionCase;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateAdded;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateRemoved;
import uk.gov.moj.cpp.hearing.domain.event.InheritedPlea;
import uk.gov.moj.cpp.hearing.domain.event.PleaUpsert;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class PleaDelegate implements Serializable {

    private final HearingAggregateMomento momento;

    public PleaDelegate(final HearingAggregateMomento momento) {
        this.momento = momento;
    }

    public void handleInheritedPlea(final InheritedPlea inheritedPlea) {
        this.momento.getPleas().computeIfAbsent(inheritedPlea.getPlea().getOffenceId(),
                offenceId -> Plea.plea()
                        .withOriginatingHearingId(inheritedPlea.getHearingId())
                        .withOffenceId(offenceId)
                        .withPleaValue(inheritedPlea.getPlea().getPleaValue())
                        .withPleaDate(inheritedPlea.getPlea().getPleaDate())
                        .build());
    }

    public void handlePleaUpsert(final PleaUpsert pleaUpsert) {
        this.momento.getPleas().put(pleaUpsert.getPlea().getOffenceId(),
                pleaUpsert.getPlea()
        );
    }

    public Stream<Object> inheritPlea(final UUID hearingId, final Plea plea) {
        return Stream.of(InheritedPlea.inheritedPlea()
                .setHearingId(hearingId)
                .setPlea(plea)
        );
    }

    public Stream<Object> updatePlea(final UUID hearingId, final uk.gov.justice.json.schemas.core.Plea plea) {

        final ProsecutionCase prosecutionCase = this.momento.getHearing().getProsecutionCases().stream()
                .filter(pc -> pc.getDefendants().stream()
                        .flatMap(de -> de.getOffences().stream())
                        .anyMatch(o -> o.getId().equals(plea.getOffenceId())))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Offence id is not present"));

        final List<Object> events = new ArrayList<>();

        plea.setOriginatingHearingId(hearingId);

        events.add(PleaUpsert.pleaUpsert()
                .setHearingId(hearingId)
                .setPlea(plea));

        events.add(plea.getPleaValue() == PleaValue.GUILTY ?
                ConvictionDateAdded.convictionDateAdded()
                        .setCaseId(prosecutionCase.getId())
                        .setHearingId(hearingId)
                        .setOffenceId(plea.getOffenceId())
                        .setConvictionDate(plea.getPleaDate()) :
                ConvictionDateRemoved.convictionDateRemoved()
                        .setCaseId(prosecutionCase.getId())
                        .setHearingId(hearingId)
                        .setOffenceId(plea.getOffenceId())
        );

        return events.stream();
    }


}
