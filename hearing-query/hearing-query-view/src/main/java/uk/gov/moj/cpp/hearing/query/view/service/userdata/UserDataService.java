package uk.gov.moj.cpp.hearing.query.view.service.userdata;

import com.google.common.base.Strings;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.QUERY_API;
import static uk.gov.justice.services.core.enveloper.Enveloper.envelop;

public class UserDataService {

    private static final String GET_USERS = "usersgroups.search-users";
    private static final String USER_IDS = "userIds";
    public static final String FIRST_NAME = "firstName";
    public static final String LAST_NAME = "lastName";

    public static final String USERS = "users";
    public static final String SPACE_DELIMITER = " ";

    @Inject
    @ServiceComponent(QUERY_API)
    private Requester requester;


    public List<String> getUserDetails(final JsonEnvelope jsonEnvelope, String userIds) {
        if (Strings.isNullOrEmpty(userIds)) {
            return Collections.emptyList();
        }
        final JsonObject params = createObjectBuilder()
                .add(USER_IDS, userIds)
                .build();
        final Envelope<JsonObject> requestEnvelop = envelop(params)
                .withName(GET_USERS)
                .withMetadataFrom(jsonEnvelope);
        final Envelope<JsonObject> jsonObjectEnvelope = requester
                .request(requestEnvelop, JsonObject.class);

        return transformJsonToUserNameList(jsonObjectEnvelope);
    }

    private List<String> transformJsonToUserNameList(Envelope<JsonObject> jsonObjectEnvelope) {
        final JsonObject payload = jsonObjectEnvelope.payload();
        final JsonArray jsonArray = payload.getJsonArray(USERS);

        return jsonArray.stream()
                .filter(json -> json.getValueType() == JsonValue.ValueType.OBJECT)
                .map(JsonObject.class::cast)
                .map(this::transformToFirstNameLastNameString)
                .collect(Collectors.toList());
    }

    private String transformToFirstNameLastNameString(JsonObject jsonObject) {
        final String firstName = jsonObject.getString(FIRST_NAME);
        final String lastName = jsonObject.getString(LAST_NAME);

        return Stream.of(firstName, lastName)
                .filter(str -> !Strings.isNullOrEmpty(str))
                .collect(Collectors.joining(SPACE_DELIMITER));
    }


}
