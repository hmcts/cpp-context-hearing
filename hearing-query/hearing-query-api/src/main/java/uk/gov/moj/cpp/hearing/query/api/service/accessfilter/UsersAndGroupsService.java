package uk.gov.moj.cpp.hearing.query.api.service.accessfilter;

import static java.util.UUID.randomUUID;
import static uk.gov.justice.services.core.annotation.Component.QUERY_API;
import static uk.gov.justice.services.messaging.Envelope.envelopeFrom;
import static uk.gov.justice.services.messaging.Envelope.metadataBuilder;

import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.moj.cpp.hearing.query.api.service.accessfilter.vo.Permissions;
import uk.gov.moj.cpp.hearing.query.api.service.accessfilter.vo.UserRole;

import java.util.List;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;

public class UsersAndGroupsService {
    private static final String GET_LOGGED_IN_USER_PERMISSIONS = "usersgroups.get-logged-in-user-permissions";
    private static final String ROLES_FOR_USER = "usersgroups.get-roles-for-user";
    private static final String USER_ID = "userId";

    @Inject
    private PermissionsMapper permissionsMapper;

    @Inject
    private GroupsMapper groupsMapper;

    @Inject
    private RolesMapper rolesMapper;

    @Inject
    private Enveloper enveloper;

    @Inject
    @ServiceComponent(QUERY_API)
    private Requester requester;

    public Permissions permissions(final String userId) {

        final Metadata metadata = metadataBuilder().withName(GET_LOGGED_IN_USER_PERMISSIONS)
                .withId(randomUUID()).withUserId(userId).build();
        final Envelope envelope = envelopeFrom(metadata, Json.createObjectBuilder().build());
        final Envelope<JsonObject> response = requester.request(envelope, JsonObject.class);

        return new Permissions.Builder()
                .withGroups(groupsMapper.mapGroups(response))
                .withSwitchableRoles(rolesMapper.switchableRoles(response))
                .withPermissions(permissionsMapper.mapPermissions(response))
                .build();
    }

    public List<UserRole> userRoles(final String userId) {

        final Metadata metadata = metadataBuilder()
                .withName(ROLES_FOR_USER)
                .withId(randomUUID())
                .withUserId(userId).build();

        final JsonObject build = Json.createObjectBuilder().add(USER_ID, userId).build();
        final Envelope envelope = envelopeFrom(metadata, build);
        final Envelope<JsonObject> response = requester.request(envelope, JsonObject.class);

        return rolesMapper.mapRoles(response);
    }
}
