package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import uk.gov.moj.cpp.hearing.command.initiate.UpdateHearingWithInheritedPleaCommand;
import uk.gov.moj.cpp.hearing.domain.Plea;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateAdded;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateRemoved;
import uk.gov.moj.cpp.hearing.domain.event.InheritedPlea;
import uk.gov.moj.cpp.hearing.domain.event.PleaUpsert;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@SuppressWarnings("pmd:BeanMembersShouldSerialize")
public class PleaDelegate {

    private static final String GUILTY = "GUILTY";

    private final HearingAggregateMomento momento;

    public PleaDelegate(final HearingAggregateMomento momento){
        this.momento = momento;
    }

    public void handleInheritedPlea(InheritedPlea inheritedPlea){
        this.momento.getPleas().computeIfAbsent(inheritedPlea.getOffenceId(), offenceId -> Plea.plea()
                .setOriginHearingId(inheritedPlea.getHearingId())
                .setOffenceId(offenceId)
                .setValue(inheritedPlea.getValue())
                .setPleaDate(inheritedPlea.getPleaDate()));
    }

    public void handlePleaUpsert(PleaUpsert pleaUpsert) {
        this.momento.getPleas().put(pleaUpsert.getOffenceId(),
                Plea.plea()
                        .setOriginHearingId(pleaUpsert.getHearingId())
                        .setOffenceId(pleaUpsert.getOffenceId())
                        .setValue(pleaUpsert.getValue())
                        .setPleaDate(pleaUpsert.getPleaDate())
        );
    }

    public Stream<Object> inheritPlea(final UpdateHearingWithInheritedPleaCommand command) {
        return Stream.of(InheritedPlea.builder()
                .withOffenceId(command.getOffenceId())
                .withCaseId(command.getCaseId())
                .withDefendantId(command.getDefendantId())
                .withHearingId(command.getHearingId())
                .withOriginHearingId(command.getOriginHearingId())
                .withPleaDate(command.getPleaDate())
                .withValue(command.getValue())
                .build()
        );
    }

    public Stream<Object> updatePlea(final UUID hearingId, final UUID offenceId, final LocalDate pleaDate,
                                     final String pleaValue) {

        final UUID caseId = this.momento.getHearing().getDefendants().stream()
                .flatMap(d -> d.getOffences().stream())
                .filter(o -> offenceId.equals(o.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("case id is not present"))
                .getCaseId();

        final List<Object> events = new ArrayList<>();
        events.add(PleaUpsert.builder()
                .withHearingId(hearingId)
                .withOffenceId(offenceId)
                .withPleaDate(pleaDate)
                .withValue(pleaValue)
                .build());
        events.add(GUILTY.equalsIgnoreCase(pleaValue) ?
                ConvictionDateAdded.builder()
                        .withCaseId(caseId)
                        .withHearingId(hearingId)
                        .withOffenceId(offenceId)
                        .withConvictionDate(pleaDate)
                        .build() :
                ConvictionDateRemoved.builder()
                        .withCaseId(caseId)
                        .withHearingId(hearingId)
                        .withOffenceId(offenceId)
                        .build());
        return events.stream();
    }


}
