package uk.gov.moj.cpp.hearing.query.api.service.accessfilter;

import uk.gov.moj.cpp.hearing.query.api.service.accessfilter.vo.Permission;
import uk.gov.moj.cpp.hearing.query.api.service.accessfilter.vo.Permissions;

public class DDJChecker {

    private static final String DDJ_PERMISSION_OBJECT = "DDJ-ROLE";

    public boolean isDDJ( final Permissions permissions) {
        return permissions.getPermissionList().stream().
                map(Permission::getObject).anyMatch(object->object.equalsIgnoreCase(DDJ_PERMISSION_OBJECT));

    }
}
