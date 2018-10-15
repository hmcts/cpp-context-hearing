package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselUpsert;

import java.io.Serializable;
import java.util.UUID;
import java.util.stream.Stream;

public class ProsecutionCounselDelegate implements Serializable {

    private final HearingAggregateMomento momento;

    public ProsecutionCounselDelegate(final HearingAggregateMomento momento) {
        this.momento = momento;
    }

    public void handleProsecutionCounselUpsert(ProsecutionCounselUpsert prosecutionCounselUpsert) {
        this.momento.getProsecutionCounsels().put(prosecutionCounselUpsert.getAttendeeId(), prosecutionCounselUpsert);
    }

    public Stream<Object> addProsecutionCounsel(final UUID personId,
                                                final UUID attendeeId,
                                                final UUID hearingId,
                                                final String status,
                                                final String firstName,
                                                final String lastName,
                                                final String title) {
        return Stream.of(
                ProsecutionCounselUpsert.builder()
                        .withHearingId(hearingId)
                        .withAttendeeId(attendeeId)
                        .withPersonId(personId)
                        .withFirstName(firstName)
                        .withLastName(lastName)
                        .withStatus(status)
                        .withTitle(title)
                        .build()
        );
    }
}
