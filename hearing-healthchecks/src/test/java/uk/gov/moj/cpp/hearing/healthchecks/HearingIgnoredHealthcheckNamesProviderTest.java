package uk.gov.moj.cpp.hearing.healthchecks;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.services.healthcheck.healthchecks.JobStoreHealthcheck.JOB_STORE_HEALTHCHECK_NAME;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class HearingIgnoredHealthcheckNamesProviderTest {


    @InjectMocks
    private HearingIgnoredHealthcheckNamesProvider hearingIgnoredHealthcheckNamesProvider;

    @Test
    public void shouldIgnoreFileStoreAndJobStoreHealthchecks() throws Exception {

        final List<String> namesOfIgnoredHealthChecks = hearingIgnoredHealthcheckNamesProvider.getNamesOfIgnoredHealthChecks();

        assertThat(namesOfIgnoredHealthChecks.size(), is(1));
        assertThat(namesOfIgnoredHealthChecks, hasItems(JOB_STORE_HEALTHCHECK_NAME));
    }
}