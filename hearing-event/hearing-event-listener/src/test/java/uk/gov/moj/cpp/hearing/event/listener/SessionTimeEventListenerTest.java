package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.Optional.ofNullable;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithDefaults;

import uk.gov.justice.services.messaging.Envelope;
import uk.gov.moj.cpp.hearing.domain.event.sessiontime.CourtSession;
import uk.gov.moj.cpp.hearing.domain.event.sessiontime.CourtSessionJudiciary;
import uk.gov.moj.cpp.hearing.domain.event.sessiontime.SessionTimeRecorded;
import uk.gov.moj.cpp.hearing.persist.entity.sessiontime.SessionTime;
import uk.gov.moj.cpp.hearing.repository.SessionTimeRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SessionTimeEventListenerTest {
    @Mock
    private SessionTimeRepository sessionTimeRepository;

    @Spy
    private ObjectMapper objectMapper;

    @InjectMocks
    private SessionTimeEventListener sessionTimeRecordedEventListener;

    @Test
    public void shouldRecordSessionTime() {
        final CourtSessionJudiciary courtSessionJudiciary = new CourtSessionJudiciary(randomUUID(), "jud1", true);
        final List<CourtSessionJudiciary> judiciaryList = new ArrayList();
        judiciaryList.add(courtSessionJudiciary);

        final CourtSession amCourtSession = new CourtSession(randomUUID(), randomUUID(), randomUUID(), "10:00", "13:00", judiciaryList);
        final CourtSession pmCourtSession = new CourtSession(randomUUID(), randomUUID(), randomUUID(), "14:00", "17:00", judiciaryList);

        final SessionTimeRecorded sessionTimeRecorded = new SessionTimeRecorded(randomUUID(), randomUUID(), randomUUID(), LocalDate.now(), amCourtSession, pmCourtSession);

        final Envelope<SessionTimeRecorded> sessionTimeRecordedEnvelope =
                Envelope.envelopeFrom(metadataWithDefaults(), sessionTimeRecorded);

        sessionTimeRecordedEventListener.sessionTimeRecorded(sessionTimeRecordedEnvelope);

        final ArgumentCaptor<SessionTime> sessionTimeArgumentCaptor = ArgumentCaptor.forClass(SessionTime.class);

        verify(sessionTimeRepository).save(sessionTimeArgumentCaptor.capture());

        final SessionTime sessionTimeActual = sessionTimeArgumentCaptor.getValue();
        assertThat(sessionTimeActual.getCourtSessionId(), is(sessionTimeRecorded.getCourtSessionId()));
        assertThat(sessionTimeActual.getCourtHouseId(), is(sessionTimeRecorded.getCourtHouseId()));
        assertThat(sessionTimeActual.getCourtRoomId(), is(sessionTimeRecorded.getCourtRoomId()));
        assertThat(sessionTimeActual.getCourtSessionDate(), is(sessionTimeRecorded.getCourtSessionDate()));

        ofNullable(sessionTimeRecorded.getAmCourtSession())
                .ifPresent(expectedAmCourtSessionEvent -> {
                    final CourtSession actualAmCourtSession = objectMapper.convertValue(sessionTimeActual.getAmCourtSession(), CourtSession.class);
                    assertThat(actualAmCourtSession, is(expectedAmCourtSessionEvent));
                });

        ofNullable(sessionTimeRecorded.getPmCourtSession())
                .ifPresent(expectedPmCourtSessionEvent -> {
                    final CourtSession actualPmCourtSession = objectMapper.convertValue(sessionTimeActual.getPmCourtSession(), CourtSession.class);
                    assertThat(actualPmCourtSession, is(expectedPmCourtSessionEvent));
                });
    }
}
