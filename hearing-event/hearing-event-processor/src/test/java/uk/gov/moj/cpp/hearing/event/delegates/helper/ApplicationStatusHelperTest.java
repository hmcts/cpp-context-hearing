package uk.gov.moj.cpp.hearing.event.delegates.helper;

import static java.lang.Boolean.TRUE;
import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

import uk.gov.justice.core.courts.ApplicationStatus;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingApplication;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingApplicationKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Target;
import uk.gov.moj.cpp.hearing.repository.HearingApplicationRepository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ApplicationStatusHelperTest {

    @Mock
    private HearingApplicationRepository hearingApplicationRepository;

    @InjectMocks
    private ApplicationStatusHelper applicationStatusHelper;

    @Test
    public void applicationStatusListed_whenEmptyHearingsApplications() {
        final UUID applicationId = randomUUID();
        when(hearingApplicationRepository.findByApplicationId(applicationId)).thenReturn(emptyList());

        final ApplicationStatus applicationStatus = applicationStatusHelper.getApplicationStatus(applicationId);

        assertThat(applicationStatus, is(ApplicationStatus.LISTED));
    }

    @Test
    public void applicationStatusListed_whenNoHearingsForApplication() {
        final UUID applicationId = randomUUID();

        final HearingApplication hearingApplication = new HearingApplication();
        hearingApplication.setId(new HearingApplicationKey(randomUUID(), randomUUID()));
        final Hearing hearing = new Hearing();
        hearingApplication.setHearing(hearing);

        when(hearingApplicationRepository.findByApplicationId(applicationId)).thenReturn(List.of(hearingApplication));

        final ApplicationStatus applicationStatus = applicationStatusHelper.getApplicationStatus(applicationId);

        assertThat(applicationStatus, is(ApplicationStatus.LISTED));
    }

    @Test
    public void applicationStatusListed_whenHearingsForApplicationHasNoTargets() {
        final UUID hearingId = randomUUID();
        final UUID applicationId = randomUUID();

        final HearingApplication hearingApplication = new HearingApplication();
        hearingApplication.setId(new HearingApplicationKey(randomUUID(), randomUUID()));
        final Hearing hearing = new Hearing();
        hearing.setId(hearingId);
        hearing.setTargets(Set.of());
        hearingApplication.setHearing(hearing);

        when(hearingApplicationRepository.findByApplicationId(applicationId)).thenReturn(List.of(hearingApplication));

        final ApplicationStatus applicationStatus = applicationStatusHelper.getApplicationStatus(applicationId);

        assertThat(applicationStatus, is(ApplicationStatus.LISTED));
    }

    @Test
    public void applicationStatusListed_whenHearingsForApplicationIsNotFinalised() {
        final UUID hearingId = randomUUID();
        final UUID applicationId = randomUUID();

        final HearingApplication hearingApplication = new HearingApplication();
        hearingApplication.setId(new HearingApplicationKey(randomUUID(), randomUUID()));
        final Hearing hearing = new Hearing();
        hearing.setId(hearingId);
        final Target target = new Target();
        target.setId(new HearingSnapshotKey(randomUUID(), hearingId));
        target.setApplicationId(applicationId);
        hearing.setTargets(Set.of(target));
        hearingApplication.setHearing(hearing);

        when(hearingApplicationRepository.findByApplicationId(applicationId)).thenReturn(List.of(hearingApplication));

        final ApplicationStatus applicationStatus = applicationStatusHelper.getApplicationStatus(applicationId);

        assertThat(applicationStatus, is(ApplicationStatus.LISTED));
    }

    @Test
    public void applicationStatusListed_whenHearingsForApplicationIsFinalised() {
        final UUID hearingId = randomUUID();
        final UUID applicationId = randomUUID();

        final HearingApplication hearingApplication = new HearingApplication();
        hearingApplication.setId(new HearingApplicationKey(randomUUID(), randomUUID()));
        final Hearing hearing = new Hearing();
        hearing.setId(hearingId);
        final Target target = new Target();
        target.setId(new HearingSnapshotKey(randomUUID(), hearingId));
        target.setApplicationId(applicationId);
        target.setApplicationFinalised(TRUE);
        hearing.setTargets(Set.of(target));
        hearingApplication.setHearing(hearing);

        when(hearingApplicationRepository.findByApplicationId(applicationId)).thenReturn(List.of(hearingApplication));

        final ApplicationStatus applicationStatus = applicationStatusHelper.getApplicationStatus(applicationId);

        assertThat(applicationStatus, is(ApplicationStatus.FINALISED));
    }

    @Test
    public void applicationStatusListed_whenMultipleHearingsForApplicationIsFinalised() {
        final UUID hearingId = randomUUID();
        final UUID applicationId = randomUUID();

        final HearingApplication hearingApplication1 = new HearingApplication();
        hearingApplication1.setId(new HearingApplicationKey(randomUUID(), randomUUID()));
        final Hearing hearing = new Hearing();
        hearing.setId(hearingId);
        final Target target = new Target();
        target.setId(new HearingSnapshotKey(randomUUID(), hearingId));
        target.setApplicationId(applicationId);
        target.setApplicationFinalised(TRUE);
        hearing.setTargets(Set.of(target));
        hearingApplication1.setHearing(hearing);

        final HearingApplication hearingApplication2 = new HearingApplication();
        hearingApplication2.setId(new HearingApplicationKey(randomUUID(), randomUUID()));
        final Hearing hearing2 = new Hearing();
        hearing2.setId(randomUUID());
        hearing2.setTargets(Set.of());
        hearingApplication2.setHearing(hearing2);
        when(hearingApplicationRepository.findByApplicationId(applicationId)).thenReturn(List.of(hearingApplication1, hearingApplication2));

        final ApplicationStatus applicationStatus = applicationStatusHelper.getApplicationStatus(applicationId);

        assertThat(applicationStatus, is(ApplicationStatus.FINALISED));
    }
}