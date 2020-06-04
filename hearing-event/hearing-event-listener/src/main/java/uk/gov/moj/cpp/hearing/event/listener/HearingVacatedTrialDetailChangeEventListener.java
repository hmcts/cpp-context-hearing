package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.Objects.nonNull;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.HearingVacatedTrialDetailUpdated;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"squid:S2629"})
@ServiceComponent(EVENT_LISTENER)
public class HearingVacatedTrialDetailChangeEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(HearingVacatedTrialDetailChangeEventListener.class.getName());

    @Inject
    JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Inject
    private HearingRepository hearingRepository;


    @Transactional
    @Handles("hearing.event.vacated-trial-detail-updated")
    public void handleVacatedTrialDetailChangedHearing(final JsonEnvelope event) {

        LOGGER.debug("hearing.event.vacated-trial-detail-updated event received {}", event.toObfuscatedDebugString());

        final HearingVacatedTrialDetailUpdated hearingVacatedTrialDetailChanged = jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), HearingVacatedTrialDetailUpdated.class);

        final Hearing hearing = hearingRepository.findBy(hearingVacatedTrialDetailChanged.getHearingId());

        if (nonNull(hearing)) {
            hearing.setvacatedTrialReasonId(hearingVacatedTrialDetailChanged.getVacatedTrialReasonId());
            hearing.setIsVacatedTrial(nonNull(hearingVacatedTrialDetailChanged.getVacatedTrialReasonId()));
            hearing.setIsEffectiveTrial(null);
            hearing.setTrialTypeId(null);
            hearingRepository.save(hearing);
        } else {
            LOGGER.error("hearing with {} id hasn't found", hearingVacatedTrialDetailChanged.getHearingId());
        }
    }
}
