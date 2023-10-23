package uk.gov.moj.cpp.hearing.query.api.service.accessfilter;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.services.messaging.Envelope.metadataBuilder;

import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.moj.cpp.hearing.query.api.service.accessfilter.vo.Permission;

import java.util.List;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PermissionMapperTest{
    public static final String PERMISSIONS = "permissions";
    public static final String ACTION = "action";
    public static final String OBJECT = "object";
    public static final String SOURCE = "source";
    public static final String TARGET = "target";

    private static final String OBJECT1 = "object1";
    private static final String ACTION1 = "action1";
    private static final UUID SOURCE1 = randomUUID();
    private static final UUID TARGET1 = randomUUID();

    private static final String OBJECT2 = "object2";
    private static final String ACTION2 = "action2";
    private static final UUID SOURCE2 = randomUUID();
    private static final UUID TARGET2 = randomUUID();

    @InjectMocks
    private PermissionsMapper permissionsMapper;

    @Test
    public void shouldReturnGroups() {
        final String userId = randomUUID().toString();
        final Metadata metadata = metadataBuilder().withName("usersgroups.get-logged-in-user-permissions")
                .withId(randomUUID()).withUserId(userId).build();

        final JsonObjectBuilder permission1Json = Json.createObjectBuilder();
        permission1Json.add(OBJECT, OBJECT1);
        permission1Json.add(ACTION, ACTION1);
        permission1Json.add(SOURCE, SOURCE1.toString());
        permission1Json.add(TARGET, TARGET1.toString());

        final JsonObjectBuilder permission2Json = Json.createObjectBuilder();
        permission2Json.add(OBJECT, OBJECT2);
        permission2Json.add(ACTION, ACTION2);
        permission2Json.add(SOURCE, SOURCE2.toString());
        permission2Json.add(TARGET, TARGET2.toString());

        final JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        arrayBuilder.add(permission1Json);
        arrayBuilder.add(permission2Json);

        final JsonObjectBuilder permissionsJson = Json.createObjectBuilder();
        permissionsJson.add(PERMISSIONS, arrayBuilder.build());

        final Envelope envelope = Envelope.envelopeFrom(metadata, permissionsJson.build());
        final List<Permission> permissions = permissionsMapper.mapPermissions(envelope);
        assertPermissions(permissions);
    }

    @Test
    public void shouldReturnEmptyListWhenPermissionNotExists() {
        final String userId = randomUUID().toString();
        final Metadata metadata = metadataBuilder().withName("usersgroups.get-logged-in-user-permissions")
                .withId(randomUUID()).withUserId(userId).build();

        final Envelope envelope = Envelope.envelopeFrom(metadata, Json.createObjectBuilder().build());
        final List<Permission> permissions = permissionsMapper.mapPermissions(envelope);
        assertThat(permissions.size(), is(0));
    }

    @Test
    public void shouldReturnGroupsWithNullSource() {
        final String userId = randomUUID().toString();
        final Metadata metadata = metadataBuilder().withName("usersgroups.get-logged-in-user-permissions")
                .withId(randomUUID()).withUserId(userId).build();

        final JsonObjectBuilder permission1Json = Json.createObjectBuilder();
        permission1Json.add(OBJECT, OBJECT1);
        permission1Json.add(ACTION, ACTION1);

        final JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        arrayBuilder.add(permission1Json);

        final JsonObjectBuilder permissionsJson = Json.createObjectBuilder();
        permissionsJson.add(PERMISSIONS, arrayBuilder.build());

        final Envelope envelope = Envelope.envelopeFrom(metadata, permissionsJson.build());
        final List<Permission> permissions = permissionsMapper.mapPermissions(envelope);
        assertThat(permissions.size(), is(1));
        assertThat(permissions.get(0).getSource(), is(nullValue()));
        assertThat(permissions.get(0).getTarget(), is(nullValue()));
    }

    private void assertPermissions(final List<Permission> permissions) {
        assertThat(permissions.size(), is(2));

        final Permission permission1 = permissions.get(0);
        assertThat(permission1.getAction(), is(ACTION1));
        assertThat(permission1.getObject(), is(OBJECT1));
        assertThat(permission1.getSource(), is(SOURCE1));
        assertThat(permission1.getTarget(), is(TARGET1));

        final Permission permission2 = permissions.get(1);
        assertThat(permission2.getAction(), is(ACTION2));
        assertThat(permission2.getObject(), is(OBJECT2));
        assertThat(permission2.getSource(), is(SOURCE2));
        assertThat(permission2.getTarget(), is(TARGET2));
    }
}