package uk.gov.moj.cpp.hearing.event;


import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.CaseDefendantsUpdatedForHearing;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(EVENT_PROCESSOR)
public class CaseDefendantsUpdatedForHearingProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(CaseDefendantsUpdatedForHearingProcessor.class);

    @Inject
    private Enveloper enveloper;

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Inject
    private Sender sender;

    @Handles("hearing.case-defendants-updated-for-hearing")
    public void caseDefendantsUpdatedForHearing(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.case-defendants-updated-for-hearing event received {}", event.toObfuscatedDebugString());
        }
        final CaseDefendantsUpdatedForHearing caseDefendantsUpdatedForHearing = jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), CaseDefendantsUpdatedForHearing.class);
        final List<Defendant> defendants = caseDefendantsUpdatedForHearing.getProsecutionCase().getDefendants();
        final UUID hearingId = caseDefendantsUpdatedForHearing.getHearingId();

        defendants.stream().forEach(defendant ->
                sender.send(enveloper.withMetadataFrom(event, "hearing.command.register-hearing-against-defendant")
                        .apply(registerHearingPayload(defendant, hearingId))));
    }

    private JsonObject registerHearingPayload(final Defendant defendant, final UUID hearingId) {
        return createObjectBuilder()
                .add("defendantId", defendant.getId().toString())
                .add("hearingId", hearingId.toString()).build();
    }
}