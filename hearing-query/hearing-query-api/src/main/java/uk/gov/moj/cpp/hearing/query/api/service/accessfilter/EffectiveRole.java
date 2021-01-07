package uk.gov.moj.cpp.hearing.query.api.service.accessfilter;

import static java.time.LocalDate.now;
import static java.util.stream.Collectors.toList;

import uk.gov.moj.cpp.hearing.query.api.service.accessfilter.vo.UserRole;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

public class EffectiveRole {
    private static final int SELECTABLE_ROLE_ACTIVE_MINUTES = 8 * 60;

    private EffectiveRole(){
        
    }
    public static boolean isEffective(final List<UserRole> userRoles, final UserRole userRole) {

        if (getSwitchableRoles(userRoles).isEmpty() || !userRole.isSelectable()) {
            return isRoleWithinStartAndEndDates(userRole);
        }

        final boolean isSelectableAndActive =
                userRole.getActivatedDate() != null
                        && ChronoUnit.MINUTES.between(LocalDateTime.from(userRole.getActivatedDate()), LocalDateTime.now())
                        < SELECTABLE_ROLE_ACTIVE_MINUTES;

        if (isSelectableAndActive) {
            return isRoleWithinStartAndEndDates(userRole);
        }
        return false;
    }

    private static boolean isRoleWithinStartAndEndDates(final UserRole userRole) {

        final LocalDate now = now();

        final LocalDate startDate =
                userRole.getStartDate() != null ? userRole.getStartDate() : now.minus(1, ChronoUnit.DAYS);

        final LocalDate endDate =
                userRole.getEndDate() != null ? userRole.getEndDate() : now.plus(1, ChronoUnit.DAYS);

        return startDate.compareTo(now) <= 0 && endDate.compareTo(now) > 0;
    }

    private static List<UserRole> getSwitchableRoles(List<UserRole> userRoles) {
        final List<UserRole> switchableRoles =
                userRoles.stream().filter(r -> r.isSelectable()).collect(toList());

        return switchableRoles.size() > 1 ? switchableRoles : Collections.emptyList();
    }
}
