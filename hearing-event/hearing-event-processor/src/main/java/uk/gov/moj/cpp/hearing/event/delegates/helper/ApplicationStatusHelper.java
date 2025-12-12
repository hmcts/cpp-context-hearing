package uk.gov.moj.cpp.hearing.event.delegates.helper;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static uk.gov.justice.core.courts.ApplicationStatus.FINALISED;
import static uk.gov.justice.core.courts.ApplicationStatus.LISTED;

import uk.gov.justice.core.courts.ApplicationStatus;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingApplication;
import uk.gov.moj.cpp.hearing.repository.HearingApplicationRepository;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

public class ApplicationStatusHelper {

    private final HearingApplicationRepository hearingApplicationRepository;

    @Inject
    public ApplicationStatusHelper(final HearingApplicationRepository hearingApplicationRepository) {
        this.hearingApplicationRepository = hearingApplicationRepository;
    }

    public ApplicationStatus getApplicationStatus(final UUID applicationId) {
        final List<HearingApplication> applicationHearings = this.hearingApplicationRepository.findByApplicationId(applicationId);

        if (isNotEmpty(applicationHearings)) {
            final boolean applicationFinalised = applicationHearings.stream()
                    .map(HearingApplication::getHearing)
                    .filter(Objects::nonNull)
                    .filter(h -> isNotEmpty(h.getTargets()))
                    .flatMap(h -> h.getTargets().stream())
                    .filter(ta -> applicationId.equals(ta.getApplicationId()))
                    .anyMatch(ta -> Boolean.TRUE.equals(ta.getApplicationFinalised()));
            if (applicationFinalised) {
                return FINALISED;
            }
        }
        return LISTED;
    }

}
