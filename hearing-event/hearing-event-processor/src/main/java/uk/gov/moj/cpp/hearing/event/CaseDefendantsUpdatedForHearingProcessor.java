package uk.gov.moj.cpp.hearing.event;


import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;


import java.util.Collection;
import java.util.stream.Collectors;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.moj.cpp.hearing.domain.event.ExistingHearingUpdated;

@ServiceComponent(EVENT_PROCESSOR)
public class CaseDefendantsUpdatedForHearingProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(CaseDefendantsUpdatedForHearingProcessor.class);

    @Inject
    private Enveloper enveloper;

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Inject
    private Sender sender;

    @Handles("hearing.events.existing-hearing-updated")
    public void caseDefendantsUpdatedForHearing(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.events.existing-hearing-updated event received {}", event.toObfuscatedDebugString());
        }
        final ExistingHearingUpdated existingHearingUpdated = jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), ExistingHearingUpdated.class);
        final List<Defendant> defendants = existingHearingUpdated.getProsecutionCases().stream()
                .map(ProsecutionCase::getDefendants)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        final UUID hearingId = existingHearingUpdated.getHearingId();

        defendants.forEach(defendant ->
                sender.send(enveloper.withMetadataFrom(event, "hearing.command.register-hearing-against-defendant")
                        .apply(registerHearingPayload(defendant, hearingId))));
    }

    @Handles("public.progression.related-hearing-updated-for-adhoc-hearing")
    public void handleExistingHearingUpdated(final JsonEnvelope event){
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("public.progression.related-hearing-updated-for-adhoc-hearing event received {}", event.toObfuscatedDebugString());
        }

        sender.send(Enveloper.envelop(event.payloadAsJsonObject())
                .withName("hearing.command.update-related-hearing")
                .withMetadataFrom(event));

    }

    private JsonObject registerHearingPayload(final Defendant defendant, final UUID hearingId) {
        return createObjectBuilder()
                .add("defendantId", defendant.getId().toString())
                .add("hearingId", hearingId.toString()).build();
    }
}