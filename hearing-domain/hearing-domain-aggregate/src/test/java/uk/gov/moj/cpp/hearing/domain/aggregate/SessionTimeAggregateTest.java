package uk.gov.moj.cpp.hearing.domain.aggregate;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import uk.gov.moj.cpp.hearing.command.sessiontime.CourtSession;
import uk.gov.moj.cpp.hearing.command.sessiontime.CourtSessionJudiciary;
import uk.gov.moj.cpp.hearing.command.sessiontime.RecordSessionTime;
import uk.gov.moj.cpp.hearing.domain.event.HearingCaseNoteSaved;
import uk.gov.moj.cpp.hearing.domain.event.sessiontime.SessionTimeRecorded;

import java.lang.reflect.Array;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SessionTimeAggregateTest {
    @InjectMocks
    private SessionTimeAggregate sessionTimeAggregate;

    @Test
    public void shouldRecordSessionTime(){
        final UUID courtSessionId = UUID.randomUUID();
        final LocalDate courtSessionDate = LocalDate.now();
        final UUID courtHouseId = UUID.randomUUID();
        final UUID courtRoomId = UUID.randomUUID();
        //am court session
        final CourtSession amCourtSession = new CourtSession();
        amCourtSession.setCourtAssociateId(UUID.randomUUID());
        amCourtSession.setCourtClerkId(UUID.randomUUID());
        amCourtSession.setLegalAdviserId(UUID.randomUUID());
        amCourtSession.setStartTime("2023-12-01");
        amCourtSession.setEndTime("2023-12-31");
        final CourtSessionJudiciary amCourtSessionJudiciary = new CourtSessionJudiciary();
        amCourtSessionJudiciary.setJudiciaryId(UUID.randomUUID());
        amCourtSessionJudiciary.setJudiciaryName("am-judiciaryName");
        amCourtSessionJudiciary.setBenchChairman(true);
        amCourtSession.setJudiciaries(Arrays.asList(amCourtSessionJudiciary));

        //pm court session
        final CourtSession pmCourtSession = new CourtSession();
        pmCourtSession.setCourtAssociateId(UUID.randomUUID());
        pmCourtSession.setCourtClerkId(UUID.randomUUID());
        pmCourtSession.setLegalAdviserId(UUID.randomUUID());
        pmCourtSession.setStartTime("2023-12-01");
        pmCourtSession.setEndTime("2023-12-31");
        final CourtSessionJudiciary pmCourtSessionJudiciary = new CourtSessionJudiciary();
        pmCourtSessionJudiciary.setJudiciaryId(UUID.randomUUID());
        pmCourtSessionJudiciary.setJudiciaryName("pm-judiciaryName");
        pmCourtSessionJudiciary.setBenchChairman(false);
        amCourtSession.setJudiciaries(Arrays.asList(pmCourtSessionJudiciary));

        RecordSessionTime recordSessionTime = new RecordSessionTime(courtHouseId, courtRoomId, courtSessionDate,
        amCourtSession, pmCourtSession);
        final Stream<Object> objectStream = sessionTimeAggregate.recordSessionTime(courtSessionId,
                recordSessionTime, courtSessionDate);

        assertThat(objectStream,is(notNullValue()));

        final List<Object> events = objectStream.collect(Collectors.toList());
        final SessionTimeRecorded sessionTimeRecorded = (SessionTimeRecorded) events.get(0);

        assertThat(events.size(), is(1));
        assertThat(sessionTimeRecorded, is(SessionTimeRecorded.class) );
        assertThat(sessionTimeRecorded.getCourtRoomId(), is(courtRoomId));
        assertThat(sessionTimeRecorded.getCourtHouseId(), is(courtHouseId));
        assertThat(sessionTimeRecorded.getCourtSessionId(), is(courtSessionId));
        assertThat(sessionTimeAggregate.getCourtSessionId(), is(courtSessionId));
    }
}
