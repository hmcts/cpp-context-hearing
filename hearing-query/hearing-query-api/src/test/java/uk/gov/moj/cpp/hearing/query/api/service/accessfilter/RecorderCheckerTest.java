package uk.gov.moj.cpp.hearing.query.api.service.accessfilter;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import uk.gov.moj.cpp.hearing.query.api.service.accessfilter.vo.Permission;
import uk.gov.moj.cpp.hearing.query.api.service.accessfilter.vo.Permissions;

import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RecorderCheckerTest {

    @InjectMocks
    private RecorderChecker checker;

    @Test
    public void shouldIsRecorder(){
       final Permission permission = new Permission.Builder().withAction("RECORDER-ACTION")
                .withObject("RECORDER-ROLE")
                .withSource(UUID.randomUUID())
                .withTarget(UUID.randomUUID())
                .build();
        final Permissions list = new Permissions.Builder()
                .withPermissions(Stream.of(permission).collect(Collectors.toList())).build();
        boolean isRecorder = checker.isRecorder(list);
        assertThat(isRecorder,is(true));
    }
}
