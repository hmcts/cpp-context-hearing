package uk.gov.moj.cpp.hearing.event.service;

import static java.lang.String.format;
import static java.util.UUID.fromString;
import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.QUERY_API;
import static uk.gov.justice.services.core.enveloper.Enveloper.envelop;

import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

import com.google.common.base.Strings;

public class UsersGroupService {

    public static final String SEARCH_USER = "usersgroups.search-users";
    private static final String USER_IDS = "userIds";
    public static final String USER_NAME_STR = "%s %s";

    public static final String USER_ID = "userId";
    public static final String FIRST_NAME = "firstName";
    public static final String LAST_NAME = "lastName";

    public static final String EMPTY = "";

    public static final String USERS = "users";

    @Inject
    @ServiceComponent(QUERY_API)
    private Requester requester;


    public Map<UUID, String> getUserDetails(final JsonEnvelope jsonEnvelope, String userIds) {
        if (Strings.isNullOrEmpty(userIds)) {
            return Collections.emptyMap();
        }
        final JsonObject params = createObjectBuilder().add(USER_IDS, userIds).build();
        final Envelope<JsonObject> requestEnvelop = envelop(params)
                .withName(SEARCH_USER)
                .withMetadataFrom(jsonEnvelope);

        final Envelope<JsonObject> jsonObjectEnvelope = requester.request(requestEnvelop, JsonObject.class);
        return transformJsonToUserNameList(jsonObjectEnvelope);
    }

    private Map<UUID, String> transformJsonToUserNameList(Envelope<JsonObject> jsonObjectEnvelope) {
        final JsonObject payload = jsonObjectEnvelope.payload();
        final JsonArray jsonArray = payload.getJsonArray(USERS);
        final Map<UUID, String> userIdNameMap = new HashMap<>();

        jsonArray.stream()
                .filter(json -> json.getValueType() == JsonValue.ValueType.OBJECT)
                .map(JsonObject.class::cast)
                .forEach(jsonObject -> {
                    final UUID userId = fromString(jsonObject.getString(USER_ID));
                    final String firstName = jsonObject.getString(FIRST_NAME, EMPTY);
                    final String lastName = jsonObject.getString(LAST_NAME, EMPTY);

                    userIdNameMap.put(userId, format(USER_NAME_STR, firstName, lastName));
                });

        return userIdNameMap;
    }

}