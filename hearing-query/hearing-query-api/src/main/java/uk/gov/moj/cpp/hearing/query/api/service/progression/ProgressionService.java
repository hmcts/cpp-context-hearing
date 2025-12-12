package uk.gov.moj.cpp.hearing.query.api.service.progression;

import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.QUERY_API;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataBuilder;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.external.domain.progression.prosecutioncases.ProsecutionCase;

import java.util.UUID;

import javax.faces.bean.ApplicationScoped;
import javax.inject.Inject;
import javax.json.JsonObject;

@ApplicationScoped
public class ProgressionService {

    private static final String PROGRESSION_CASE_DETAILS = "progression.query.prosecutioncase";

    @Inject
    @ServiceComponent(QUERY_API)
    private Requester requester;

    @Inject
    private UtcClock utcClock;

    public ProsecutionCase getProsecutionCaseDetails(final UUID caseId) {
        final JsonObject query = createObjectBuilder()
                .add("caseId", caseId.toString())
                .build();

        final JsonEnvelope jsonEnvelope = envelopeFrom(
                metadataBuilder()
                        .createdAt(utcClock.now())
                        .withName(PROGRESSION_CASE_DETAILS)
                        .withId(randomUUID())
                        .build(),
                query);

        return requester.requestAsAdmin(jsonEnvelope, ProsecutionCase.class).payload();
    }
}
