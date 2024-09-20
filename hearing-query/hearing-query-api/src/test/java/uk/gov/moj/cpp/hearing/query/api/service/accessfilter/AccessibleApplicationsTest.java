package uk.gov.moj.cpp.hearing.query.api.service.accessfilter;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import uk.gov.moj.cpp.hearing.query.api.service.accessfilter.vo.Permission;
import uk.gov.moj.cpp.hearing.query.api.service.accessfilter.vo.Permissions;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

public class AccessibleApplicationsTest {
    private static final String APPLICATION = "Application";
    private static final String ACCESS = "Access";

    private static final String OBJECT1 = APPLICATION;
    private static final String ACTION1 = ACCESS;
    private static final UUID SOURCE1 = randomUUID();
    private static final UUID TARGET1 = randomUUID();

    private static final String OBJECT2 = "object2";
    private static final String ACTION2 = "action2";
    private static final UUID SOURCE2 = randomUUID();
    private static final UUID TARGET2 = randomUUID();

    @Test
    public void shouldReturnAccessibleApplicationsByTheUser() {
        final UUID userId = randomUUID();
        final Permissions permissions = new Permissions.Builder()
                .withPermissions(permissionsWithAccessibleApplications(userId)).build();
        final List<UUID> cases = new AccessibleApplications().findApplications(permissions, userId.toString());
        assertThat(cases.size(), is(1));
    }

    @Test
    public void shouldReturnNoAccessibleApplicationsByTheUser() {
        final UUID userId = randomUUID();
        final Permissions permissions = new Permissions.Builder()
                .withPermissions(permissionsWithNoAccessibleApplications(userId)).build();
        final List<UUID> cases = new AccessibleApplications().findApplications(permissions, userId.toString());
        assertThat(cases.size(), is(0));
    }

    private List<Permission> permissionsWithAccessibleApplications(final UUID userId) {

        final Permission permission1 = new Permission.Builder()
                .withObject(APPLICATION)
                .withAction(ACCESS)
                .withSource(userId)
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

    private List<Permission> permissionsWithNoAccessibleApplications(final UUID userId) {

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