package uk.gov.moj.cpp.hearing.query.api.service.accessfilter;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.moj.cpp.hearing.query.api.service.accessfilter.EffectiveRole.isEffective;

import uk.gov.moj.cpp.hearing.query.api.service.accessfilter.vo.UserRole;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EffectiveRoleTest {
    private static final UUID ROLE_ID1 = randomUUID();
    private static final String DESCRIPTION1 = "description1";
    private static final String LABEL1 = "label1";
    private static final boolean SELECTABLE1 = true;
    private static final LocalDate ACTIVATED_DATE1 = LocalDate.now().minusDays(20);
    private static final LocalDate START_DATE1 = LocalDate.now().minusDays(10);
    private static final LocalDate END_DATE1 = LocalDate.now().plusDays(20);

    private static final UUID ROLE_ID2 = randomUUID();
    private static final String DESCRIPTION2 = "description2";
    private static final String LABEL2 = "label2";
    private static final boolean SELECTABLE2 = true;
    private static final LocalDate ACTIVATED_DATE2 = LocalDate.now().minusDays(20);
    private static final LocalDate START_DATE2 = LocalDate.now().minusDays(15);
    private static final LocalDate END_DATE2 = LocalDate.now().minusDays(10);

    @Test
    public void shouldReturnEffectiveRole() {
        final boolean effective = isEffective(effectiveRole(),role(ROLE_ID1, DESCRIPTION1, LABEL1, SELECTABLE1, ACTIVATED_DATE1, START_DATE1, END_DATE1));
        assertThat(effective, is(true));
    }


    @Test
    public void shouldReturnNotEffectiveRole() {
        final boolean effective = isEffective(nonEffectiveRole(),role(ROLE_ID1, DESCRIPTION1, LABEL2, SELECTABLE2, ACTIVATED_DATE2, START_DATE2, END_DATE2));
        assertThat(effective, is(false));

    }

    private List<UserRole> effectiveRole() {
        final List<UserRole> roles = new ArrayList();
        final UserRole role = role(ROLE_ID1, DESCRIPTION1, LABEL1, SELECTABLE1, ACTIVATED_DATE1, START_DATE1, END_DATE1);
        roles.add(role);
        return roles;
    }


    private List<UserRole> nonEffectiveRole() {
        final List<UserRole> roles = new ArrayList();
        final UserRole role = role(ROLE_ID2, DESCRIPTION2, LABEL2, SELECTABLE2, ACTIVATED_DATE2, START_DATE2, END_DATE2);
        roles.add(role);
        return roles;
    }

    private UserRole role(final UUID roleId1,
                          final String description1,
                          final String ddj,
                          final boolean selectable1,
                          final LocalDate activatedDate1,
                          final LocalDate startDate1,
                          final LocalDate endDate1) {
        return new UserRole.Builder()
                .withRoleId(roleId1)
                .withDescription(description1)
                .withLabel(ddj)
                .withSelectable(selectable1)
                .withActivatedDate(activatedDate1.toString())
                .withStartDate(startDate1.toString())
                .withEndDate(endDate1.toString())
                .build();
    }
}