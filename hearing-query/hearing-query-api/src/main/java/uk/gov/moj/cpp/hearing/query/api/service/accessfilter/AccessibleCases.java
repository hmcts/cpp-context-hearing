package uk.gov.moj.cpp.hearing.query.api.service.accessfilter;

import static java.util.UUID.fromString;

import uk.gov.moj.cpp.hearing.query.api.service.accessfilter.vo.Permission;
import uk.gov.moj.cpp.hearing.query.api.service.accessfilter.vo.Permissions;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AccessibleCases {
    private static final String CASE = "Case";
    private static final String ACCESS = "Access";
    private static final String APPLICATION = "Application";

    public List<UUID> findCases(final Permissions permissions, final String userId) {
        final List<UUID> accessibleCases = new ArrayList();
        for (final Permission permission : permissions.getPermissionList()) {
            final String permissionObject = permission.getObject();
            final UUID source = permission.getSource();
            final String action = permission.getAction();
            if (casePermission(permissionObject) && sourceAsUser(userId, source)
                    && accessAsAction(action)) {
                accessibleCases.add(permission.getTarget());
            }
        }
        return accessibleCases;
    }

    public List<UUID> findApplications(final Permissions permissions, final String userId) {
        final List<UUID> accessibleApplications = new ArrayList();
        for (final Permission permission : permissions.getPermissionList()) {
            final String permissionObject = permission.getObject();
            final UUID source = permission.getSource();
            final String action = permission.getAction();
            if (applicationPermission(permissionObject) && sourceAsUser(userId, source)
                    && accessAsAction(action)) {
                accessibleApplications.add(permission.getTarget());
            }
        }
        return accessibleApplications;
    }

    private boolean sourceAsUser(final String userId, final UUID source) {
        return source != null && source.equals(fromString(userId));
    }

    private boolean accessAsAction(final String action) {
        return action != null && action.equalsIgnoreCase(ACCESS);
    }

    private boolean casePermission(final String permissionObject) {
        return permissionObject != null && permissionObject.equalsIgnoreCase(CASE);
    }

    private boolean applicationPermission(final String permissionObject) {
        return permissionObject != null && permissionObject.equalsIgnoreCase(APPLICATION);
    }
}

