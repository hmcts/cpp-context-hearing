package uk.gov.moj.cpp.hearing.query.api.service.accessfilter;

import uk.gov.moj.cpp.hearing.query.api.service.accessfilter.vo.Permission;
import uk.gov.moj.cpp.hearing.query.api.service.accessfilter.vo.Permissions;

public class RecorderChecker {

    private static final String RECORDER_PERMISSION_OBJECT = "RECORDER-ROLE";

    public boolean isRecorder(final Permissions permissions) {

        return permissions.getPermissionList().stream().
                map(Permission::getObject).anyMatch(object -> RECORDER_PERMISSION_OBJECT.equalsIgnoreCase(object));
    }
}
