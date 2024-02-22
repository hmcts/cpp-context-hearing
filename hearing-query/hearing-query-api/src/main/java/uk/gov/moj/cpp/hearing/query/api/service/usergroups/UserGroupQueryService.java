package uk.gov.moj.cpp.hearing.query.api.service.usergroups;

import static java.util.Objects.nonNull;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.QUERY_API;
import static uk.gov.justice.services.core.enveloper.Enveloper.envelop;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataBuilder;

import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;

import javax.inject.Inject;
import javax.json.JsonObject;

public class UserGroupQueryService {

    public static final String LOGGED_IN_USER_ORGANISATION = "usersgroups.get-logged-in-user-details";
    public static final String HMCTS_ORG_ID = "1371dfe8-8aa5-47f7-bb76-275b83fc312d";
    public static final String ORGANISATION_ID = "organisationId";


    @Inject
    @ServiceComponent(QUERY_API)
    private Requester requester;

    public boolean doesUserBelongsToHmctsOrganisation(final UUID userId) {

        boolean organisationFlag = false;
        final JsonObject query = createObjectBuilder().build();

        final JsonEnvelope jsonEnvelope = envelopeFrom(
                metadataBuilder()
                        .withName(LOGGED_IN_USER_ORGANISATION)
                        .withId(randomUUID())
                        .withUserId(userId.toString())
                        .build(),
                query);

        final Envelope<JsonObject> jsonObjectEnvelope = requester.request(jsonEnvelope, JsonObject.class);

        final String associatedOrganisationId = jsonObjectEnvelope.payload().getString(ORGANISATION_ID, null);

        if (nonNull(associatedOrganisationId) && HMCTS_ORG_ID.equals(jsonObjectEnvelope.payload().getString(ORGANISATION_ID))) {
            organisationFlag = true;
        }
        return  organisationFlag;

    }
}