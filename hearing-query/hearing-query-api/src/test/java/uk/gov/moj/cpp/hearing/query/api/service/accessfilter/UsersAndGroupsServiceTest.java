package uk.gov.moj.cpp.hearing.query.api.service.accessfilter;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.annotation.Component.QUERY_VIEW;
import static uk.gov.justice.services.messaging.Envelope.metadataBuilder;

import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.moj.cpp.hearing.query.api.service.accessfilter.vo.Group;
import uk.gov.moj.cpp.hearing.query.api.service.accessfilter.vo.Permission;
import uk.gov.moj.cpp.hearing.query.api.service.accessfilter.vo.Permissions;
import uk.gov.moj.cpp.hearing.query.api.service.accessfilter.vo.UserRole;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.json.Json;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UsersAndGroupsServiceTest{
    private static final UUID GROUP_ID1 = randomUUID();
    private static final String GROUP_NAME1 = "groupName1";
    private static final String PROSECURITY_AUTH1 = "TFL";

    private static final UUID GROUP_ID2 = randomUUID();
    private static final String GROUP_NAME2 = "groupName2";
    private static final String PROSECURITY_AUTH2 = "TVL";

    private static final UUID ROLE_ID1 = randomUUID();
    private static final String DESCRIPTION1 = "description1";
    private static final String LABEL1 = "label1";
    private static final boolean SELECTABLE1 = true;

    private static final UUID ROLE_ID2 = randomUUID();
    private static final String DESCRIPTION2 = "description2";
    private static final String LABEL2 = "label2";
    private static final boolean SELECTABLE2 = false;

    private static final String OBJECT1 = "object1";
    private static final String ACTION1 = "action1";
    private static final UUID SOURCE1 = randomUUID();
    private static final UUID TARGET1 = randomUUID();

    private static final String OBJECT2 = "object2";
    private static final String ACTION2 = "action2";
    private static final UUID SOURCE2 = randomUUID();
    private static final UUID TARGET2 = randomUUID();

    @Mock
    private PermissionsMapper permissionsMapper;

    @Mock
    private GroupsMapper groupsMapper;

    @Mock
    private RolesMapper switchableRolesMapper;

    @Mock
    @ServiceComponent(QUERY_VIEW)
    private Requester requester;

    @InjectMocks
    private UsersAndGroupsService usersAndGroupsService;

    @Test
    public void shouldReturnLoggedInPermissions() {
        final String userId = randomUUID().toString();
        final Metadata metadata = metadataBuilder().withName("usersgroups.get-logged-in-user-permissions")
                .withId(randomUUID()).withUserId(userId).build();

        final Envelope envelope = Envelope.envelopeFrom(metadata, Json.createObjectBuilder().build());
        when(requester.request(any(), any())).thenReturn(envelope);
        when(groupsMapper.mapGroups(envelope)).thenReturn(groups());
        when(switchableRolesMapper.switchableRoles(envelope)).thenReturn(switchableRoles());
        when(permissionsMapper.mapPermissions(envelope)).thenReturn(permissions());

        final Permissions permissions = usersAndGroupsService.permissions(userId);

        assertGroups(permissions);
        assertSwitchableRoles(permissions);
        assertPermissions(permissions);
    }

    @Test
    public void shouldReturnUserRoles() {
        final String userId = randomUUID().toString();
        final Metadata metadata = metadataBuilder().withName("usersgroups.get-roles-for-user")
                .withId(randomUUID()).withUserId(userId).build();

        final Envelope envelope = Envelope.envelopeFrom(metadata, Json.createObjectBuilder().add("userId", userId).build());
        when(requester.request(any(), any())).thenReturn(envelope);
        when(switchableRolesMapper.mapRoles(envelope)).thenReturn(switchableRoles());
        final List<UserRole> userRoles = usersAndGroupsService.userRoles(userId);
        assertThat(userRoles.size(),is(2));
        assertThat(userRoles.get(0).getRoleId(),is(ROLE_ID1));
        assertThat(userRoles.get(0).getDescription(),is(DESCRIPTION1));
        assertThat(userRoles.get(0).getLabel(),is(LABEL1));
        assertThat(userRoles.get(0).isSelectable(),is(true));
    }

    private void assertGroups(final Permissions permissions) {
        assertThat(permissions.getGroups().size(), is(2));

        final Group group1 = permissions.getGroups().get(0);
        assertThat(group1.getGroupId(), is(GROUP_ID1));
        assertThat(group1.getGroupName(), is(GROUP_NAME1));
        assertThat(group1.getProsecutingAuthority(), is(PROSECURITY_AUTH1));

        final Group group2 = permissions.getGroups().get(1);
        assertThat(group2.getGroupId(), is(GROUP_ID2));
        assertThat(group2.getGroupName(), is(GROUP_NAME2));
        assertThat(group2.getProsecutingAuthority(), is(PROSECURITY_AUTH2));
    }

    private void assertSwitchableRoles(final Permissions permissions) {
        assertThat(permissions.getSwitchableRoles().size(), is(2));

        final UserRole switchableRoles1 = permissions.getSwitchableRoles().get(0);
        assertThat(switchableRoles1.getRoleId(), is(ROLE_ID1));
        assertThat(switchableRoles1.getDescription(), is(DESCRIPTION1));
        assertThat(switchableRoles1.getLabel(), is(LABEL1));
        assertThat(switchableRoles1.isSelectable(), is(SELECTABLE1));

        final UserRole switchableRoles2 = permissions.getSwitchableRoles().get(1);
        assertThat(switchableRoles2.getRoleId(), is(ROLE_ID2));
        assertThat(switchableRoles2.getDescription(), is(DESCRIPTION2));
        assertThat(switchableRoles2.getLabel(), is(LABEL2));
        assertThat(switchableRoles2.isSelectable(), is(SELECTABLE2));
    }

    private void assertPermissions(final Permissions permissions) {
        assertThat(permissions.getPermissionList().size(), is(2));

        final Permission permission1 = permissions.getPermissionList().get(0);
        assertThat(permission1.getAction(), is(ACTION1));
        assertThat(permission1.getObject(), is(OBJECT1));
        assertThat(permission1.getSource(), is(SOURCE1));
        assertThat(permission1.getTarget(), is(TARGET1));

        final Permission permission2 = permissions.getPermissionList().get(1);
        assertThat(permission2.getAction(), is(ACTION2));
        assertThat(permission2.getObject(), is(OBJECT2));
        assertThat(permission2.getSource(), is(SOURCE2));
        assertThat(permission2.getTarget(), is(TARGET2));
    }

    private List<Group> groups() {
        final Group group1 = new Group.Builder()
                .withGroupId(GROUP_ID1)
                .withGroupName(GROUP_NAME1)
                .withProsecutingAuthority(PROSECURITY_AUTH1).build();

        final Group group2 = new Group.Builder()
                .withGroupId(GROUP_ID2)
                .withGroupName(GROUP_NAME2)
                .withProsecutingAuthority(PROSECURITY_AUTH2).build();

        final List<Group> groups = new ArrayList();
        groups.add(group1);
        groups.add(group2);
        return groups;
    }

    private List<UserRole> switchableRoles() {
        final UserRole role1 = new UserRole.Builder()
                .withRoleId(ROLE_ID1)
                .withDescription(DESCRIPTION1)
                .withLabel(LABEL1)
                .withSelectable(SELECTABLE1)
                .build();

        final UserRole role2 = new UserRole.Builder()
                .withRoleId(ROLE_ID2)
                .withDescription(DESCRIPTION2)
                .withLabel(LABEL2)
                .withSelectable(SELECTABLE2)
                .build();

        final List<UserRole> switchableRoles = new ArrayList();
        switchableRoles.add(role1);
        switchableRoles.add(role2);
        return switchableRoles;
    }


    private List<Permission> permissions() {

        final Permission permission1 = new Permission.Builder()
                .withObject(OBJECT1)
                .withAction(ACTION1)
                .withSource(SOURCE1)
                .withTarget(TARGET1)
                .build();

        final Permission permission2 = new Permission.Builder()
                .withObject(OBJECT2)
                .withAction(ACTION2)
                .withSource(SOURCE2)
                .withTarget(TARGET2)
                   .build();
        final List<Permission> permissions = new ArrayList();
        permissions.add(permission1);
        permissions.add(permission2);
        return permissions;
    }

}