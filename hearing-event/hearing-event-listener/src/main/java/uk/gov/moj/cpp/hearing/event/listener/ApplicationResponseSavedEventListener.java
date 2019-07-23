package uk.gov.moj.cpp.hearing.event.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.application.ApplicationResponseSaved;
import uk.gov.moj.cpp.hearing.mapping.HearingJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

import javax.inject.Inject;
import javax.transaction.Transactional;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

@ServiceComponent(EVENT_LISTENER)
public class ApplicationResponseSavedEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationResponseSavedEventListener.class);

    @Inject
    private HearingRepository hearingRepository;

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Inject
    private HearingJPAMapper hearingJPAMapper;

    @Transactional
    @Handles("hearing.application-response-saved")
    public void applicationResponseSave(final JsonEnvelope event) {
        final ApplicationResponseSaved applicationResponseSaved = jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), ApplicationResponseSaved.class);
        final Hearing hearing = hearingRepository.findBy(applicationResponseSaved.getCourtApplicationResponse().getOriginatingHearingId());

        final String courtApplicationsJson = hearingJPAMapper.saveApplicationResponse(hearing.getCourtApplicationsJson(), applicationResponseSaved.getCourtApplicationResponse(), applicationResponseSaved.getApplicationPartyId());

        hearing.setCourtApplicationsJson(courtApplicationsJson);

        hearingRepository.save(hearing);
        LOGGER.info("{} event had updated the application response successfully status", applicationResponseSaved);
    }
}