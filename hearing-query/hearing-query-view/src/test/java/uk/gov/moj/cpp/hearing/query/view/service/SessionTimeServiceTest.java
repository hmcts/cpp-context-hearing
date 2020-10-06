package uk.gov.moj.cpp.hearing.query.view.service;

import static java.util.UUID.randomUUID;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.moj.cpp.hearing.common.SessionTimeUUIDService;
import uk.gov.moj.cpp.hearing.persist.entity.sessiontime.SessionTime;
import uk.gov.moj.cpp.hearing.query.view.response.SessionTimeResponse;
import uk.gov.moj.cpp.hearing.repository.SessionTimeRepository;

import javax.json.JsonObject;
import javax.ws.rs.NotFoundException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class SessionTimeServiceTest {

    @Mock
    private SessionTimeRepository sessionTimeRepository;

    @Spy
    private StringToJsonObjectConverter stringToJsonObjectConverter;

    @Mock
    private SessionTimeUUIDService uuidService;

    @InjectMocks
    private SessionTimeService sessionTimeService;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Spy
    private ObjectMapper objectMapper;


    @Test
    public void shouldFindSessionTimeByKey() {

        final UUID courtSessionId = randomUUID();
        final UUID courtHouseId = randomUUID();
        final UUID courtRoomId = randomUUID();
        final ZonedDateTime courtSessionDate = ZonedDateTime.now();

        final SessionTime sessionTime = buildSessionTime(courtSessionId, courtHouseId, courtRoomId, courtSessionDate.toLocalDate(), true);

        final JsonObject payload = createObjectBuilder()
                .add("courtHouseId", courtHouseId.toString())
                .add("courtRoomId", courtRoomId.toString()).build();

        when(uuidService.getCourtSessionId(courtHouseId, courtRoomId, courtSessionDate.toLocalDate())).thenReturn(courtSessionId);
        when(sessionTimeRepository.findBy(courtSessionId)).thenReturn(sessionTime);

        final SessionTimeResponse sessionTimeResponse = sessionTimeService.getSessionTime(payload);

        assertThat(sessionTimeResponse.getCourtSessionId(), is(courtSessionId));
        assertThat(sessionTimeResponse.getCourtHouseId(), is(courtHouseId));
        assertThat(sessionTimeResponse.getCourtRoomId(), is(courtRoomId));
        assertThat(sessionTimeResponse.getCourtSessionDate(), is(courtSessionDate.toLocalDate()));
        assertThat(sessionTimeResponse.getAmCourtSession().toString(), is(sessionTime.getAmCourtSession().toString()));
        assertThat(sessionTimeResponse.getPmCourtSession().toString(), is(sessionTime.getPmCourtSession().toString()));
    }

    @Test
    public void shouldFindSessionTimeByGivenDate() {

        final UUID courtSessionId = randomUUID();
        final UUID courtHouseId = randomUUID();
        final UUID courtRoomId = randomUUID();

        final LocalDate courtSessionDate = LocalDate.of(2020, 07, 07);

        final SessionTime sessionTime = buildSessionTime(courtSessionId, courtHouseId, courtRoomId, courtSessionDate, true);

        final JsonObject payload = createObjectBuilder()
                .add("courtHouseId", courtHouseId.toString())
                .add("courtRoomId", courtRoomId.toString())
                .add("courtSessionDate", courtSessionDate.toString()).build();

        when(uuidService.getCourtSessionId(courtHouseId, courtRoomId, courtSessionDate)).thenReturn(courtSessionId);
        when(sessionTimeRepository.findBy(courtSessionId)).thenReturn(sessionTime);

        final SessionTimeResponse sessionTimeResponse = sessionTimeService.getSessionTime(payload);

        assertThat(sessionTimeResponse.getCourtSessionId(), is(courtSessionId));
        assertThat(sessionTimeResponse.getCourtHouseId(), is(courtHouseId));
        assertThat(sessionTimeResponse.getCourtRoomId(), is(courtRoomId));
        assertThat(sessionTimeResponse.getCourtSessionDate(), is(courtSessionDate));
        assertThat(sessionTimeResponse.getAmCourtSession().toString(), is(sessionTime.getAmCourtSession().toString()));
        assertThat(sessionTimeResponse.getPmCourtSession().toString(), is(sessionTime.getPmCourtSession().toString()));
    }

    @Test
    public void shouldThrowNotFoundExceptionWhenInvalidRequests() {

        final UUID courtSessionId = randomUUID();

        final UUID courtHouseId = randomUUID();
        final UUID courtRoomId = randomUUID();

        final JsonObject payload = createObjectBuilder()
                .add("courtHouseId", courtHouseId.toString())
                .add("courtRoomId", courtRoomId.toString()).build();

        when(uuidService.getCourtSessionId(courtHouseId, courtRoomId, LocalDate.now())).thenReturn(courtSessionId);
        when(sessionTimeRepository.findBy(courtSessionId)).thenReturn(null);

        try {
            sessionTimeService.getSessionTime(payload);
            fail();
        } catch (final NotFoundException e) {
            assertThat(e.getResponse().getStatus(), is(SC_NOT_FOUND));
        }
    }

    private SessionTime buildSessionTime(final UUID courtSessionId, final UUID courtHouseId, final UUID courtRoomId, final LocalDate courtSessionDate, boolean validCourtSessions) {

        final JsonObject amCourtSession = createObjectBuilder()
                .add("judiciaries", createArrayBuilder()
                        .add(createObjectBuilder()
                                .add("judiciaryId", randomUUID().toString())
                                .add("judiciaryName", "Joe Smith")
                                .add("benchChairman", true)))
                .add("startTime", "10:00")
                .add("endTime", "11:00")
                .add("courtClerkId", randomUUID().toString())
                .add("courtAssociateId", randomUUID().toString())
                .add("legalAdviserId", randomUUID().toString()).build();

        final JsonObject pmCourtSession = createObjectBuilder()
                .add("judiciaries", createArrayBuilder()
                        .add(createObjectBuilder()
                                .add("judiciaryId", randomUUID().toString())
                                .add("judiciaryName", "John Smith")
                                .add("benchChairman", false)))
                .add("startTime", "13:00")
                .add("endTime", "18:00")
                .add("courtClerkId", randomUUID().toString())
                .add("courtAssociateId", randomUUID().toString())
                .add("legalAdviserId", randomUUID().toString()).build();

        SessionTime sessionTime = new SessionTime();
        sessionTime.setCourtHouseId(courtHouseId);
        sessionTime.setCourtRoomId(courtRoomId);
        sessionTime.setCourtSessionDate(courtSessionDate);
        sessionTime.setCourtSessionId(courtSessionId);

        if (validCourtSessions) {
            sessionTime.setAmCourtSession(objectMapper.valueToTree(amCourtSession));
            sessionTime.setPmCourtSession(objectMapper.valueToTree(pmCourtSession));
        }

        return sessionTime;
    }
}
