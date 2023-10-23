package uk.gov.moj.cpp.hearing.query.api.service.accessfilter;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.services.messaging.Envelope.metadataBuilder;

import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.moj.cpp.hearing.query.api.service.accessfilter.vo.UserRole;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RolesMapperTest {
    public static final String ROLES = "allRoles";
    public static final String SWITCHABLE_ROLES = "switchableRoles";
    public static final String ROLE_ID = "roleId";
    public static final String DESCRIPTION = "description";
    public static final String LABEL = "label";
    public static final String SELECTABLE = "selectable";
    public static final String ACTIVATED_DATE = "activatedDate";
    public static final String START_DATE = "startDate";
    public static final String END_DATE = "endDate";

    private static final UUID ROLE_ID1 = randomUUID();
    private static final String DESCRIPTION1 = "description1";
    private static final String LABEL1 = "label1";
    private static final boolean SELECTABLE1 = true;

    private static final UUID ROLE_ID2 = randomUUID();
    private static final String DESCRIPTION2 = "description2";
    private static final String LABEL2 = "label2";
    private static final boolean SELECTABLE2 = false;
    public static final LocalDate ACTIVATED_DATE2 = LocalDate.now().minusDays(12);
    public static final LocalDate START_DATE2 = LocalDate.now().minusDays(10);
    public static final LocalDate END_DATE2 = LocalDate.now().plusDays(10);

    @InjectMocks
    private RolesMapper rolesMapper;

    @Test
    public void shouldReturnSwitchableRoles() {
        final String userId = randomUUID().toString();
        final Metadata metadata = metadataBuilder().withName("usersgroups.get-logged-in-user-permissions")
                .withId(randomUUID()).withUserId(userId).build();

        final JsonObjectBuilder role1Json = Json.createObjectBuilder();
        role1Json.add(ROLE_ID, ROLE_ID1.toString());
        role1Json.add(DESCRIPTION, DESCRIPTION1);
        role1Json.add(LABEL, LABEL1);
        role1Json.add(SELECTABLE, SELECTABLE1);

        final JsonObjectBuilder role2Json = Json.createObjectBuilder();
        role2Json.add(ROLE_ID, ROLE_ID2.toString());
        role2Json.add(DESCRIPTION, DESCRIPTION2);
        role2Json.add(LABEL, LABEL2);
        role2Json.add(SELECTABLE, SELECTABLE2);
        role2Json.add(ACTIVATED_DATE, ACTIVATED_DATE2.toString());
        role2Json.add(START_DATE, START_DATE2.toString());
        role2Json.add(END_DATE, END_DATE2.toString());

        final JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        arrayBuilder.add(role1Json);
        arrayBuilder.add(role2Json);

        final JsonObjectBuilder permissions = Json.createObjectBuilder();
        permissions.add(SWITCHABLE_ROLES, arrayBuilder.build());

        final Envelope envelope = Envelope.envelopeFrom(metadata, permissions.build());
        final List<UserRole> switchableRoles = rolesMapper.switchableRoles(envelope);
        assertRoles(switchableRoles);
    }

    @Test
    public void shouldReturnEmptyListWhenEmptySwitchableRolesPresentInEnvelope() {
        final String userId = randomUUID().toString();
        final Metadata metadata = metadataBuilder().withName("usersgroups.get-logged-in-user-permissions")
                .withId(randomUUID()).withUserId(userId).build();

        final Envelope envelope = Envelope.envelopeFrom(metadata,  Json.createObjectBuilder().build());
        final List<UserRole> switchableRoles = rolesMapper.switchableRoles(envelope);
        assertThat(switchableRoles.size(), is(0));
    }

    @Test
    public void shouldReturnRoles() {
        final String userId = randomUUID().toString();
        final Metadata metadata = metadataBuilder().withName("usersgroups.roles")
                .withId(randomUUID()).withUserId(userId).build();

        final JsonObjectBuilder role1Json = Json.createObjectBuilder();
        role1Json.add(ROLE_ID, ROLE_ID1.toString());
        role1Json.add(DESCRIPTION, DESCRIPTION1);
        role1Json.add(LABEL, LABEL1);
        role1Json.add(SELECTABLE, SELECTABLE1);

        final JsonObjectBuilder role2Json = Json.createObjectBuilder();
        role2Json.add(ROLE_ID, ROLE_ID2.toString());
        role2Json.add(DESCRIPTION, DESCRIPTION2);
        role2Json.add(LABEL, LABEL2);
        role2Json.add(SELECTABLE, SELECTABLE2);
        role2Json.add(ACTIVATED_DATE, ACTIVATED_DATE2.toString());
        role2Json.add(START_DATE, START_DATE2.toString());
        role2Json.add(END_DATE, END_DATE2.toString());

        final JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        arrayBuilder.add(role1Json);
        arrayBuilder.add(role2Json);

        final JsonObjectBuilder permissions = Json.createObjectBuilder();
        permissions.add(ROLES, arrayBuilder.build());

        final Envelope envelope = Envelope.envelopeFrom(metadata, permissions.build());
        final List<UserRole> roles = rolesMapper.mapRoles(envelope);
        assertRoles(roles);
    }

    @Test
    public void shouldReturnEmptyListWhenEmptyRolesPresentInEnvelope() {
        final String userId = randomUUID().toString();
        final Metadata metadata = metadataBuilder().withName("usersgroups.roles")
                .withId(randomUUID()).withUserId(userId).build();

        final Envelope envelope = Envelope.envelopeFrom(metadata, Json.createObjectBuilder().build());
        final List<UserRole> roles = rolesMapper.mapRoles(envelope);
        assertThat(roles.size(),is(0));
    }


    private void assertRoles(final List<UserRole> switchableRoles) {
        assertThat(switchableRoles.size(), is(2));

        final UserRole switchableRoles1 = switchableRoles.get(0);
        assertThat(switchableRoles1.getRoleId(), is(ROLE_ID1));
        assertThat(switchableRoles1.getDescription(), is(DESCRIPTION1));
        assertThat(switchableRoles1.getLabel(), is(LABEL1));
        assertThat(switchableRoles1.isSelectable(), is(SELECTABLE1));
        assertThat(switchableRoles1.getActivatedDate(), is(nullValue()));
        assertThat(switchableRoles1.getStartDate(), is(nullValue()));
        assertThat(switchableRoles1.getEndDate(), is(nullValue()));

        final UserRole switchableRoles2 = switchableRoles.get(1);
        assertThat(switchableRoles2.getRoleId(), is(ROLE_ID2));
        assertThat(switchableRoles2.getDescription(), is(DESCRIPTION2));
        assertThat(switchableRoles2.getLabel(), is(LABEL2));
        assertThat(switchableRoles2.isSelectable(), is(SELECTABLE2));
        assertThat(switchableRoles2.getActivatedDate(), is(ACTIVATED_DATE2));
        assertThat(switchableRoles2.getStartDate(), is(START_DATE2));
        assertThat(switchableRoles2.getEndDate(), is(END_DATE2));

    }
}