package uk.gov.moj.cpp.hearing.query.api.service.accessfilter;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import uk.gov.moj.cpp.hearing.query.api.service.accessfilter.vo.Permission;
import uk.gov.moj.cpp.hearing.query.api.service.accessfilter.vo.Permissions;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

public class AccessibleCasesTest{
    private static final String CASE = "Case";
    private static final String APPLICATION = "Application";
    private static final String ACCESS = "Access";

    private static final String OBJECT1 = CASE;
    private static final String ACTION1 = ACCESS;
    private static final UUID SOURCE1 = randomUUID();
    private static final UUID TARGET1 = randomUUID();

    private static final String OBJECT2 = "object2";
    private static final String ACTION2 = "action2";
    private static final UUID SOURCE2 = randomUUID();
    private static final UUID TARGET2 = randomUUID();

    @Test
    public void shouldReturnAccessibleCasesByTheUser() {
        final UUID userId = randomUUID();
        final Permissions permissions = new Permissions.Builder()
                .withPermissions(permissionsWithAccessibleCases(userId)).build();
        final List<UUID> cases = new AccessibleCases().findCases(permissions, userId.toString());
        final List<UUID> applications = new AccessibleCases().findApplications(permissions, userId.toString());
        assertThat(cases.size(), is(1));
        assertThat(applications.size(), is(0));
    }

    @Test
    public void shouldReturnAccessibleApplicationsByTheUser() {
        final UUID userId = randomUUID();
        final Permissions permissions = new Permissions.Builder()
                .withPermissions(permissionsWithAccessibleApplications(userId)).build();
        final List<UUID> cases = new AccessibleCases().findCases(permissions, userId.toString());
        final List<UUID> applications = new AccessibleCases().findApplications(permissions, userId.toString());
        assertThat(applications.size(), is(1));
        assertThat(cases.size(), is(0));
    }

    @Test
    public void shouldReturnNoAccessibleCasesByTheUser() {
        final UUID userId = randomUUID();
        final Permissions permissions = new Permissions.Builder()
                .withPermissions(permissionsWithNoAccessibleCases(userId)).build();
        final List<UUID> cases = new AccessibleCases().findCases(permissions, userId.toString());
        assertThat(cases.size(), is(0));
    }

    @Test
    public void shouldReturnNoAccessibleApplicationsByTheUser() {
        final UUID userId = randomUUID();
        final Permissions permissions = new Permissions.Builder()
                .withPermissions(permissionsWithNoAccessibleCases(userId)).build();
        final List<UUID> applications = new AccessibleCases().findApplications(permissions, userId.toString());
        assertThat(applications.size(), is(0));
    }

    private List<Permission> permissionsWithAccessibleCases(final UUID userId) {

        final Permission permission1 = new Permission.Builder()
                .withObject(CASE)
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

    private List<Permission> permissionsWithNoAccessibleCases(final UUID userId) {

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