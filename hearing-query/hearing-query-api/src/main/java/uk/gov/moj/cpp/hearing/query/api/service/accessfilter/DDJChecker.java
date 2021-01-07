package uk.gov.moj.cpp.hearing.query.api.service.accessfilter;

import uk.gov.moj.cpp.hearing.query.api.service.accessfilter.vo.UserRole;

import java.util.List;

import javax.inject.Inject;

public class DDJChecker {
    private static final String DDJ = "Deputy District Judge";

    @Inject
    private UsersAndGroupsService usersAndGroupsService;

    public boolean isDDJ(final String userId) {
        final List<UserRole> roles = usersAndGroupsService.userRoles(userId);
        for (final UserRole role : roles) {
            if (role.isSelectable()
                    && role.getLabel().equalsIgnoreCase(DDJ)
                    && EffectiveRole.isEffective(roles, role)) {
                return true;
            }
        }
        return false;
    }
}
