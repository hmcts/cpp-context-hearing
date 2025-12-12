package uk.gov.moj.cpp.hearing.event;

import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataFrom;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.TrialType;
import uk.gov.moj.cpp.hearing.domain.event.result.HearingVacatedRequested;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.CrackedIneffectiveVacatedTrialType;
import uk.gov.moj.cpp.hearing.event.service.CrackedIneffectiveVacatedTrialTypesReverseLookup;

import java.util.Optional;

import javax.inject.Inject;
import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(EVENT_PROCESSOR)
public class HearingVacatedEventProcessor {


    private static final Logger LOGGER = LoggerFactory.getLogger(HearingVacatedEventProcessor.class);

    private final Enveloper enveloper;
    private final Sender sender;
    private final JsonObjectToObjectConverter jsonObjectToObjectConverter;
    private final ObjectToJsonObjectConverter objectToJsonObjectConverter;
    private final CrackedIneffectiveVacatedTrialTypesReverseLookup crackedIneffectiveVacatedTrialTypesReverseLookup;

    @Inject
    public HearingVacatedEventProcessor(final Enveloper enveloper, final Sender sender, final JsonObjectToObjectConverter jsonObjectToObjectConverter,
                                        final ObjectToJsonObjectConverter objectToJsonObjectConverter,
                                        final CrackedIneffectiveVacatedTrialTypesReverseLookup crackedIneffectiveVacatedTrialTypesReverseLookup) {
        this.enveloper = enveloper;
        this.sender = sender;
        this.jsonObjectToObjectConverter = jsonObjectToObjectConverter;
        this.objectToJsonObjectConverter = objectToJsonObjectConverter;
        this.crackedIneffectiveVacatedTrialTypesReverseLookup = crackedIneffectiveVacatedTrialTypesReverseLookup;
    }

    @Handles("hearing.hearing-vacated-requested")
    public void hearingVacatedRequested(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.hearing-vacated-requested event received {}", event.toObfuscatedDebugString());
        }
        final HearingVacatedRequested hearingVacatedRequested = this.jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), HearingVacatedRequested.class);

        final Optional<CrackedIneffectiveVacatedTrialType> crackedIneffectiveVacatedTrialType = crackedIneffectiveVacatedTrialTypesReverseLookup.getCrackedIneffectiveVacatedTrialType(event)
                .getCrackedIneffectiveVacatedTrialTypes()
                .stream()
                .filter(element -> hearingVacatedRequested.getVacatedTrialReasonShortDesc().equals(element.getReasonShortDescription()))
                .findFirst();

        if (crackedIneffectiveVacatedTrialType.isPresent()){
            final TrialType trialType = TrialType.builder()
                    .withHearingId(hearingVacatedRequested.getHearingIdToBeVacated())
                    .withVacatedTrialReasonId(crackedIneffectiveVacatedTrialType.get().getId())
                    .build();

            final JsonObject eventPayload = this.objectToJsonObjectConverter.convert(trialType);
            this.sender.send(envelopeFrom(metadataFrom(event.metadata()).withName("hearing.command.set-trial-type"), eventPayload));
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("hearing.command.set-trial-type event produced {}", event.toObfuscatedDebugString());
            }

        }
   }
}
