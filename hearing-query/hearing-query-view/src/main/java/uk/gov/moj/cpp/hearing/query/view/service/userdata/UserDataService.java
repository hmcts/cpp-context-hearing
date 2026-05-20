package uk.gov.moj.cpp.hearing.query.view.service.userdata;

import com.google.common.base.Strings;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.justice.services.messaging.MetadataBuilder;
import uk.gov.moj.cpp.hearing.query.view.model.Permission;
import uk.gov.moj.cpp.hearing.query.view.model.PermissionList;

import javax.inject.Inject;
import uk.gov.justice.services.messaging.JsonObjects;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static uk.gov.justice.services.messaging.JsonObjects.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.QUERY_API;
import static uk.gov.justice.services.core.enveloper.Enveloper.envelop;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;

public class UserDataService {

    private static final String GET_USERS = "usersgroups.search-users";
    private static final String USER_IDS = "userIds";
    public static final String FIRST_NAME = "firstName";
    public static final String LAST_NAME = "lastName";
    private static final String ACTION = "action";
    private static final String ACCESS_TO_STANDALONE_APPLICATION = "Access to Standalone Application";

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

    public List<Permission> getUserPermissionForApplicationTypes(final Metadata metadata) {
        final JsonObject getOrganisationForUserRequest = JsonObjects.createObjectBuilder()
                .add(ACTION, ACCESS_TO_STANDALONE_APPLICATION)
                .build();
        final MetadataBuilder metadataWithActionName = Envelope.metadataFrom(metadata).withName("usersgroups.is-logged-in-user-has-permission-for-action");

        final JsonEnvelope requestEnvelope = envelopeFrom(metadataWithActionName, getOrganisationForUserRequest);
        final Envelope<PermissionList> response = requester.request(requestEnvelope, PermissionList.class);
        if (isNull(response.payload()) || isNull(response.payload().getPermissions())) {
            return emptyList();
        }
        return response.payload().getPermissions();

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
