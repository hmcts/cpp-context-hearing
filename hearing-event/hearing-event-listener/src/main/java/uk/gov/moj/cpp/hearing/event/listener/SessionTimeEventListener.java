package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.Optional.ofNullable;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.moj.cpp.hearing.domain.event.sessiontime.SessionTimeRecorded;
import uk.gov.moj.cpp.hearing.persist.entity.sessiontime.SessionTime;
import uk.gov.moj.cpp.hearing.repository.SessionTimeRepository;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("squid:S3864")
@ServiceComponent(EVENT_LISTENER)
public class SessionTimeEventListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(RespondentCounselEventListener.class);

    @Inject
    private ObjectMapper objectMapper;

    @Inject
    private SessionTimeRepository sessionTimeRepository;

    @Handles("hearing.event.session-time-recorded")
    @SuppressWarnings("squid:S1186")
    public void sessionTimeRecorded(final Envelope<SessionTimeRecorded> event) {

        LOGGER.debug("Entered SessionTimeRecordedEventListener");
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.event.session-time-recorded event received {}", event.payload());
        }

        final SessionTimeRecorded sessionTimeRecorded = event.payload();

        final SessionTime sessionTime = new SessionTime();
        sessionTime.setCourtSessionId(sessionTimeRecorded.getCourtSessionId());
        sessionTime.setCourtHouseId(sessionTimeRecorded.getCourtHouseId());
        sessionTime.setCourtRoomId(sessionTimeRecorded.getCourtRoomId());
        sessionTime.setCourtSessionDate(sessionTimeRecorded.getCourtSessionDate());

        ofNullable(sessionTimeRecorded.getAmCourtSession())
                .ifPresent(amCourtSession -> sessionTime.setAmCourtSession(objectMapper.valueToTree(amCourtSession)));

        ofNullable(sessionTimeRecorded.getPmCourtSession())
                .ifPresent(pmCourtSession -> sessionTime.setPmCourtSession(objectMapper.valueToTree(pmCourtSession)));

        sessionTimeRepository.save(sessionTime);
    }

}
