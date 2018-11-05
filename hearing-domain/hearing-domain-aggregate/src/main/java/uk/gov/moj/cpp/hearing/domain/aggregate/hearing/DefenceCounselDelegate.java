package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import uk.gov.moj.cpp.hearing.command.DefendantId;
import uk.gov.moj.cpp.hearing.command.defenceCounsel.AddDefenceCounselCommand;
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselUpsert;

import java.io.Serializable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DefenceCounselDelegate implements Serializable {

    private static final long serialVersionUID = 1L;

    private final HearingAggregateMomento momento;

    public DefenceCounselDelegate(final HearingAggregateMomento momento) {
        this.momento = momento;
    }

    public void handleDefenceCounselUpsert(DefenceCounselUpsert defenceCounselUpsert) {
        this.momento.getDefenceCounsels().put(defenceCounselUpsert.getAttendeeId(), defenceCounselUpsert);
    }

    public Stream<Object> addDefenceCounsel(AddDefenceCounselCommand defenceCounselCommand) {
        return Stream.of(
                DefenceCounselUpsert.builder()
                        .withHearingId(defenceCounselCommand.getHearingId())
                        .withDefendantIds(
                                defenceCounselCommand.getDefendantIds().stream()
                                        .map(DefendantId::getDefendantId)
                                        .collect(Collectors.toList())
                        )
                        .withAttendeeId(defenceCounselCommand.getAttendeeId())
                        .withPersonId(defenceCounselCommand.getPersonId())
                        .withFirstName(defenceCounselCommand.getFirstName())
                        .withLastName(defenceCounselCommand.getLastName())
                        .withStatus(defenceCounselCommand.getStatus())
                        .withTitle(defenceCounselCommand.getTitle())
                        .build()
        );
    }
}
