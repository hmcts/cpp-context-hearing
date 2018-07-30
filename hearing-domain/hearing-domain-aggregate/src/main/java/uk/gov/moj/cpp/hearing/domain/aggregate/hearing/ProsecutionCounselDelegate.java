package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import uk.gov.moj.cpp.hearing.command.prosecutionCounsel.AddProsecutionCounselCommand;
import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselUpsert;

import java.io.Serializable;
import java.util.stream.Stream;

public class ProsecutionCounselDelegate implements Serializable {

    private final HearingAggregateMomento momento;

    public ProsecutionCounselDelegate(final HearingAggregateMomento momento) {
        this.momento = momento;
    }

    public void handleProsecutionCounselUpsert(ProsecutionCounselUpsert prosecutionCounselUpsert) {
        this.momento.getProsecutionCounsels().put(prosecutionCounselUpsert.getAttendeeId(), prosecutionCounselUpsert);
    }

    public Stream<Object> addProsecutionCounsel(AddProsecutionCounselCommand prosecutionCounselCommand) {
        return Stream.of(
                ProsecutionCounselUpsert.builder()
                        .withHearingId(prosecutionCounselCommand.getHearingId())
                        .withAttendeeId(prosecutionCounselCommand.getAttendeeId())
                        .withPersonId(prosecutionCounselCommand.getPersonId())
                        .withFirstName(prosecutionCounselCommand.getFirstName())
                        .withLastName(prosecutionCounselCommand.getLastName())
                        .withStatus(prosecutionCounselCommand.getStatus())
                        .withTitle(prosecutionCounselCommand.getTitle())
                        .build()
        );
    }
}
