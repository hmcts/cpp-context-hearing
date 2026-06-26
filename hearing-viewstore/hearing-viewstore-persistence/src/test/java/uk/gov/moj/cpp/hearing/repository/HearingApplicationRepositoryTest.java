package uk.gov.moj.cpp.hearing.repository;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.minimumInitiateHearingTemplate;

import uk.gov.moj.cpp.hearing.mapping.HearingJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingApplication;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingApplicationKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.justice.services.test.utils.persistence.HibernateTestEntityManagerProvider;

/**
 * DB integration tests for {@link HearingApplicationRepository} class
 */
@ExtendWith(MockitoExtension.class)
class HearingApplicationRepositoryTest {

    private static final String PERSISTENCE_UNIT = "hearing-test-persistence-unit";

    private static final List<uk.gov.justice.core.courts.Hearing> hearings = new ArrayList<>();

    @RegisterExtension
    static HibernateTestEntityManagerProvider hibernateTestEntityManagerProvider =
            new HibernateTestEntityManagerProvider(PERSISTENCE_UNIT);

    @Mock
    private HearingJPAMapper hearingJPAMapper;

    private HearingRepository hearingRepository;
    private HearingApplicationRepository hearingApplicationRepository;

    @BeforeAll
    static void createTestData() {
        hearings.add(minimumInitiateHearingTemplate().getHearing());
    }

    @BeforeEach
    void openEntityManagerAndCreateRepositories() {
        hearingRepository = new HearingRepository();
        hibernateTestEntityManagerProvider.injectEntityManagerInto(hearingRepository);

        hearingApplicationRepository = new HearingApplicationRepository();
        hibernateTestEntityManagerProvider.injectEntityManagerInto(hearingApplicationRepository);

        when(hearingJPAMapper.toJPA(any())).thenAnswer(invocation -> {
            final uk.gov.justice.core.courts.Hearing domainHearing = invocation.getArgument(0);
            return buildMinimalHearingEntity(domainHearing.getId());
        });

        hearings.forEach(hearing -> {
            final Hearing hearingEntity = hearingJPAMapper.toJPA(hearing);
            // because h2 incorrectly maps column type TEXT to VARCHAR(255)
            hearingEntity.setCourtApplicationsJson(hearingEntity.getCourtApplicationsJson().substring(0, 255));
            hearingEntity.getProsecutionCases().iterator().next().setMarkers(null);
            hearingRepository.save(hearingEntity);
        });
    }

    @Test
    void shouldFindHearingIdByApplicationId() {
        UUID applicationId = hearings.get(0).getCourtApplications().get(0).getId();
        final Hearing hearingSaved = hearingRepository.findBy(hearings.get(0).getId());
        saveHearingApplication(applicationId, hearingSaved);

        final List<HearingApplication> actual = hearingApplicationRepository.findAll();
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getId().getApplicationId(), is(applicationId));
        assertThat(actual.get(0).getId().getHearingId(), is(hearingSaved.getId()));

        final List<HearingApplication> byApplicationId = hearingApplicationRepository.findByApplicationId(applicationId);
        assertThat(byApplicationId.size(), is(1));
        assertThat(byApplicationId.get(0).getId().getApplicationId(), is(applicationId));
    }

    private void saveHearingApplication(final UUID applicationId, final Hearing hearingSaved) {
        final HearingApplication hearingApplication = new HearingApplication();

        HearingApplicationKey hearingApplicationKey = new HearingApplicationKey(applicationId, hearingSaved.getId());
        hearingApplication.setId(hearingApplicationKey);
        hearingApplication.setHearing(hearingSaved);
        hearingApplicationRepository.save(hearingApplication);
    }

    private static Hearing buildMinimalHearingEntity(final UUID hearingId) {
        final Hearing hearing = new Hearing();
        hearing.setId(hearingId);
        // courtApplicationsJson must be at least 255 chars for the substring(0, 255) truncation in @BeforeEach
        hearing.setCourtApplicationsJson("[]" + " ".repeat(253));

        final ProsecutionCase prosecutionCase = new ProsecutionCase();
        prosecutionCase.setId(new HearingSnapshotKey(UUID.randomUUID(), hearingId));
        prosecutionCase.setHearing(hearing);
        prosecutionCase.setMarkers(new HashSet<>());

        hearing.setProsecutionCases(new HashSet<>());
        hearing.getProsecutionCases().add(prosecutionCase);
        hearing.setHearingApplications(new HashSet<>());
        return hearing;
    }
}
