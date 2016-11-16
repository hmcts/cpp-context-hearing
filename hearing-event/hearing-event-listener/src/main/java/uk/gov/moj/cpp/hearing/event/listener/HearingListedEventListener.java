package uk.gov.moj.cpp.hearing.event.listener;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import java.util.UUID;

import javax.inject.Inject;
import javax.transaction.Transactional;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.HearingStatusEnum;
import uk.gov.moj.cpp.hearing.domain.event.HearingListed;
import uk.gov.moj.cpp.hearing.event.listener.converter.HearingListedToHearingConverter;
import uk.gov.moj.cpp.hearing.persist.HearingRepository;
import uk.gov.moj.cpp.hearing.persist.entity.Hearing;

@ServiceComponent(EVENT_LISTENER)
public class HearingListedEventListener {

    @Inject
    private JsonObjectToObjectConverter jsonObjectConverter;

    @Inject
    private HearingListedToHearingConverter convertor;

    @Inject
    private HearingRepository hearingRepository;

    @Transactional
    @Handles("hearing.events.hearing-listed")
    public void hearingListed(final JsonEnvelope event) {
        HearingListed hearingListed = jsonObjectConverter.convert(event.payloadAsJsonObject(), HearingListed.class);
        Hearing hearing = convertor.convert(hearingListed);
        hearing.setStatus(HearingStatusEnum.BOOKED);
        hearingRepository.save(hearing);
    }

    @Transactional
    @Handles("hearing.events.hearing-vacated")
    public void hearingVacated(final JsonEnvelope event) {
        Hearing hearing = hearingRepository.findByHearingId(UUID.fromString(event.payloadAsJsonObject().getString("hearingId")));
        hearing.setStatus(HearingStatusEnum.VACATED);
        hearingRepository.save(hearing);
    }
}
