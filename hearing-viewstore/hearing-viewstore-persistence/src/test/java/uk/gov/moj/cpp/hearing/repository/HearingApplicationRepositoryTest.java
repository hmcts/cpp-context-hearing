package uk.gov.moj.cpp.hearing.repository;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.minimumInitiateHearingTemplate;

import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.mapping.HearingJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingApplication;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingApplicationKey;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * DB integration tests for {@link HearingApplicationRepositoryTest} class
 */

@RunWith(CdiTestRunner.class)
public class HearingApplicationRepositoryTest {

    private static final List<uk.gov.justice.core.courts.Hearing> hearings = new ArrayList<>();

    @Inject
    private HearingRepository hearingRepository;

    @Inject
    private HearingJPAMapper hearingJPAMapper;

    @Inject
    private HearingApplicationRepository hearingApplicationRepository;

    @BeforeClass
    public static void create() {
        final InitiateHearingCommand initiateHearingCommand = minimumInitiateHearingTemplate();
        hearings.add(initiateHearingCommand.getHearing());
    }

    @Before
    public void setup() {
        hearings.forEach(hearing -> {
            final Hearing hearingEntity = hearingJPAMapper.toJPA(hearing);
            // because h2 incorrectly maps column type TEXT to VARCHAR(255)
            hearingEntity.setCourtApplicationsJson(hearingEntity.getCourtApplicationsJson().substring(0, 255));
            hearingEntity.getProsecutionCases().iterator().next().setMarkers(null);
            hearingRepository.save(hearingEntity);
        });
    }

    @Test
    public void shouldFindHearingIdByApplicationId() {
        UUID applicationId = hearings.get(0).getCourtApplications().get(0).getId();
        final Hearing hearingSaved = hearingRepository.findBy(hearings.get(0).getId());
        saveHearingApplication(applicationId, hearingSaved);

        final List<HearingApplication> actual = hearingApplicationRepository.findAll();
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getId().getApplicationId(), is(applicationId));
        assertThat(actual.get(0).getId().getHearingId(), is(hearingSaved.getId()));
    }

    private void saveHearingApplication(UUID applicationId, Hearing hearingSaved) {
        final HearingApplication hearingApplication = new HearingApplication();

        HearingApplicationKey hearingApplicationKey = new HearingApplicationKey(applicationId, hearingSaved.getId());
        hearingApplication.setId(hearingApplicationKey);
        hearingApplication.setHearing(hearingSaved);
        hearingSaved.getHearingApplications().add(hearingApplication);
        hearingApplicationRepository.save(hearingApplication);
    }
}
