
package uk.gov.moj.cpp.hearing.domain.aggregate;

import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.match;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.when;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.moj.cpp.hearing.command.sessiontime.CourtSessionJudiciary;
import uk.gov.moj.cpp.hearing.command.sessiontime.RecordSessionTime;
import uk.gov.moj.cpp.hearing.domain.event.sessiontime.CourtSession;
import uk.gov.moj.cpp.hearing.domain.event.sessiontime.SessionTimeRecorded;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class SessionTimeAggregate implements Aggregate {

    private UUID courtSessionId;

    public Stream<Object> recordSessionTime(final UUID courtSessionId,
                                            final RecordSessionTime recordSessionTime,
                                            final LocalDate courtSessionDate
    ) {

        final UUID courtHouseIdCommand = recordSessionTime.getCourtHouseId();
        final UUID courtRoomIdCommand = recordSessionTime.getCourtRoomId();

        final uk.gov.moj.cpp.hearing.command.sessiontime.CourtSession amSessionCommand = recordSessionTime.getAmCourtSession();
        final uk.gov.moj.cpp.hearing.command.sessiontime.CourtSession pmSessionCommand = recordSessionTime.getPmCourtSession();

        final SessionTimeRecorded.Builder sessionTimeRecordedBuilder = SessionTimeRecorded.sessionTimeRecorded();
        sessionTimeRecordedBuilder.withCourtSessionId(courtSessionId);
        sessionTimeRecordedBuilder.withCourtHouseId(courtHouseIdCommand);
        sessionTimeRecordedBuilder.withCourtRoomId(courtRoomIdCommand);
        sessionTimeRecordedBuilder.withCourtSessionDate(courtSessionDate);

        if (null != amSessionCommand) {
            sessionTimeRecordedBuilder.withAmCourtSession(session(amSessionCommand));
        }
        if (null != pmSessionCommand) {
            sessionTimeRecordedBuilder.withPmCourtSession(session(pmSessionCommand));
        }

        return apply(Stream.of(sessionTimeRecordedBuilder.build()));
    }

    private CourtSession session(final uk.gov.moj.cpp.hearing.command.sessiontime.CourtSession sessionCommand) {
        final CourtSession.Builder courtSessionBuilder = CourtSession.courtSession();

        final List<CourtSessionJudiciary> judiciariesCommand = sessionCommand.getJudiciaries();
        courtSessionBuilder
                .withCourtAssociateId(sessionCommand.getCourtAssociateId())
                .withCourtClerkId(sessionCommand.getCourtClerkId())
                .withLegalAdviserId(sessionCommand.getLegalAdviserId())
                .withStartTime(sessionCommand.getStartTime())
                .withEndTime(sessionCommand.getEndTime());

        if (null != judiciariesCommand) {
            final List<uk.gov.moj.cpp.hearing.domain.event.sessiontime.CourtSessionJudiciary> courtSessionJudiciaries = new ArrayList<>();
            final uk.gov.moj.cpp.hearing.domain.event.sessiontime.CourtSessionJudiciary.Builder courtSessionJudiciaryBuilder = uk.gov.moj.cpp.hearing.domain.event.sessiontime.CourtSessionJudiciary.courtSessionJudiciary();
            for (final CourtSessionJudiciary judiciary : judiciariesCommand) {
                courtSessionJudiciaryBuilder.withJudiciaryId(judiciary.getJudiciaryId());
                courtSessionJudiciaryBuilder.withJudiciaryName(judiciary.getJudiciaryName());
                courtSessionJudiciaryBuilder.withBenchChairman(judiciary.isBenchChairman());
                courtSessionJudiciaries.add(courtSessionJudiciaryBuilder.build());
                courtSessionBuilder.withJudiciaries(courtSessionJudiciaries);
            }
        }
        return  courtSessionBuilder.build();
    }

    @Override
    public Object apply(final Object event) {
        return match(event).with(
                when(SessionTimeRecorded.class).apply(this::sessionTimeRecorded));
    }

    private void sessionTimeRecorded(final SessionTimeRecorded recordSessionTime) {
        this.courtSessionId = recordSessionTime.getCourtSessionId();
    }

    public UUID getCourtSessionId() {
        return courtSessionId;
    }
}