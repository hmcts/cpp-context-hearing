package uk.gov.moj.cpp.hearing.query.api.service.accessfilter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.moj.cpp.hearing.query.api.service.accessfilter.vo.Permission;
import uk.gov.moj.cpp.hearing.query.api.service.accessfilter.vo.Permissions;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class DDJCheckerTest {

    private static final String DDJ_PERMISSION_OBJECT = "DDJ-ROLE";
    private static final String NON_DDJ_PERMISSION_OBJECT = "NON-DDJ-ROLE";


    @InjectMocks
    private DDJChecker ddjChecker;

    @Test
    public void shouldReturnDDJAsTrue() {
        final Permissions permissions = permissionDDJ();
        final boolean ddj = ddjChecker.isDDJ(permissions);
        assertThat(ddj, is(true));
    }

    @Test
    public void shouldReturnDDJAsFalse() {
        final Permissions permissions =permissionNonDDJ();
        final boolean ddj = ddjChecker.isDDJ(permissions);
        assertThat(ddj, is(false));
    }



    private Permissions permissionDDJ() {
        final Permission permission = Permission.permission()
                .withObject(DDJ_PERMISSION_OBJECT)
                .withAction("GrantAccess")
                .build();
        final List<Permission> permissions = new ArrayList<>();
        permissions.add(permission);
        return Permissions.permission().withPermissions(permissions).build();
    }

    private Permissions permissionNonDDJ() {
        final Permission permission = Permission.permission()
                .withObject(NON_DDJ_PERMISSION_OBJECT)
                .withAction("GrantAccess")
                .build();
        final List<Permission> permissions = new ArrayList<>();
        permissions.add(permission);
        return Permissions.permission().withPermissions(permissions).build();
    }


}