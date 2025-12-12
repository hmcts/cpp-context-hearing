package uk.gov.moj.cpp.hearing.healthchecks;

import static java.util.Collections.singletonList;
import static uk.gov.justice.services.healthcheck.healthchecks.JobStoreHealthcheck.JOB_STORE_HEALTHCHECK_NAME;

import uk.gov.justice.services.healthcheck.api.DefaultIgnoredHealthcheckNamesProvider;

import java.util.List;

import javax.enterprise.inject.Specializes;

@Specializes
public class HearingIgnoredHealthcheckNamesProvider extends DefaultIgnoredHealthcheckNamesProvider {

    public HearingIgnoredHealthcheckNamesProvider() {
        // This constructor is required by CDI.
    }

    @Override
    public List<String> getNamesOfIgnoredHealthChecks() {
        return singletonList(JOB_STORE_HEALTHCHECK_NAME);
    }
}