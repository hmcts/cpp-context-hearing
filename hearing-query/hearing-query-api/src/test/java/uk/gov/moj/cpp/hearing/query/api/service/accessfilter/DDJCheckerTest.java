package uk.gov.moj.cpp.hearing.query.api.service.accessfilter;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import uk.gov.moj.cpp.hearing.query.api.service.accessfilter.vo.UserRole;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DDJCheckerTest {
    private static final UUID ROLE_ID1 = randomUUID();
    private static final String DESCRIPTION1 = "description1";
    private static final String DDJ = "Deputy District Judge";
    private static final boolean SELECTABLE1 = true;
    private static final LocalDate ACTIVATED_DATE1 = LocalDate.now().minusDays(20);
    private static final LocalDate START_DATE1 = LocalDate.now().minusDays(10);
    private static final LocalDate END_DATE1 = LocalDate.now().plusDays(20);

    private static final UUID ROLE_ID2 = randomUUID();
    private static final String DESCRIPTION2 = "description2";
    private static final String NON_DDJ = "label2";
    private static final boolean SELECTABLE2 = true;
    private static final LocalDate ACTIVATED_DATE2 = LocalDate.now().minusDays(20);
    private static final LocalDate START_DATE2 = LocalDate.now().minusDays(10);
    private static final LocalDate END_DATE2 = LocalDate.now().plusDays(20);

    @Mock
    private UsersAndGroupsService usersAndGroupsService;

    @InjectMocks
    private DDJChecker ddjChecker;

    @Test
    public void shouldReturnDDJAsTrue() {
        final String userId = UUID.randomUUID().toString();
        when(usersAndGroupsService.userRoles(userId)).thenReturn(rolesDDJ());
        final boolean ddj = ddjChecker.isDDJ(userId);
        assertThat(ddj, is(true));
    }

    @Test
    public void shouldReturnDDJAsFalse() {
        final String userId = UUID.randomUUID().toString();
        when(usersAndGroupsService.userRoles(userId)).thenReturn(rolesNonDDJ());
        final boolean ddj = ddjChecker.isDDJ(userId);
        assertThat(ddj, is(false));
    }

    private List<UserRole> rolesDDJ() {
        final UserRole role1 = new UserRole.Builder()
                .withRoleId(ROLE_ID1)
                .withDescription(DESCRIPTION1)
                .withLabel(DDJ)
                .withSelectable(SELECTABLE1)
                .withActivatedDate(ACTIVATED_DATE1.toString())
                .withStartDate(START_DATE1.toString())
                .withEndDate(END_DATE1.toString())
                .build();

        final List<UserRole> roles = new ArrayList();
        roles.add(role1);
        return roles;
    }

    private List<UserRole> rolesNonDDJ() {
        final UserRole role2 = new UserRole.Builder()
                .withRoleId(ROLE_ID2)
                .withDescription(DESCRIPTION2)
                .withLabel(NON_DDJ)
                .withSelectable(SELECTABLE2)
                .withActivatedDate(ACTIVATED_DATE2.toString())
                .withStartDate(START_DATE2.toString())
                .withEndDate(END_DATE2.toString())
                .build();

        final List<UserRole> roles = new ArrayList();
        roles.add(role2);
        return roles;
    }
}