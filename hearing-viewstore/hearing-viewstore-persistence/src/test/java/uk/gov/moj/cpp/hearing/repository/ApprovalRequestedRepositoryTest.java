package uk.gov.moj.cpp.hearing.repository;

import static java.time.ZonedDateTime.now;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.minimumInitiateHearingTemplate;

import uk.gov.justice.core.courts.ApprovalType;
import uk.gov.justice.services.test.utils.persistence.HibernateTestEntityManagerProvider;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.mapping.HearingJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.application.ApplicationDraftResult;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ApprovalRequested;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Target;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.Sets;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

// TODO: remove commented code
@ExtendWith(MockitoExtension.class)
class ApprovalRequestedRepositoryTest {

    private static final String PERSISTENCE_UNIT = "hearing-test-persistence-unit";

    private static final List<uk.gov.justice.core.courts.Hearing> hearings = new ArrayList<>();

    @RegisterExtension
    static HibernateTestEntityManagerProvider hibernateTestEntityManagerProvider =
            new HibernateTestEntityManagerProvider(PERSISTENCE_UNIT);

    @Mock
    private HearingJPAMapper hearingJPAMapper;

    private ApprovalRequestedRepository approvalRequestedRepository;
    private HearingRepository hearingRepository;

    @BeforeAll
    static void createHearingTemplates() {
        final InitiateHearingCommand hearingCommand1 = minimumInitiateHearingTemplate();
        hearings.add(hearingCommand1.getHearing());

        final InitiateHearingCommand hearingCommand2 = minimumInitiateHearingTemplate();
        hearings.add(hearingCommand2.getHearing());
    }

    @BeforeEach
    void openEntityManagerAndCreateRepositories() {
        approvalRequestedRepository = new ApprovalRequestedRepository();
        hearingRepository = new HearingRepository();
        hibernateTestEntityManagerProvider.injectEntityManagerInto(approvalRequestedRepository);
        hibernateTestEntityManagerProvider.injectEntityManagerInto(hearingRepository);

        when(hearingJPAMapper.toJPA(any(uk.gov.justice.core.courts.Hearing.class))).thenAnswer(invocation -> {
            final uk.gov.justice.core.courts.Hearing domainHearing = invocation.getArgument(0);
            final Hearing hearingEntity = new Hearing();
            hearingEntity.setId(domainHearing.getId());
            hearingEntity.setCourtApplicationsJson("[]" + " ".repeat(255));
            final ProsecutionCase prosecutionCase = new ProsecutionCase();
            prosecutionCase.setId(new HearingSnapshotKey(randomUUID(), domainHearing.getId()));
            prosecutionCase.setHearing(hearingEntity);
            hearingEntity.setProsecutionCases(new HashSet<>(Sets.newHashSet(prosecutionCase)));
            return hearingEntity;
        });
    }

    void saveHearing(final uk.gov.justice.core.courts.Hearing hearing) {
        final Hearing hearingEntity = hearingJPAMapper.toJPA(hearing);
        hearingEntity.setCourtApplicationsJson(hearingEntity.getCourtApplicationsJson().substring(0, 255));
        hearingEntity.getProsecutionCases().iterator().next().setMarkers(null);
        hearingEntity.setTargets(Sets.newHashSet(Target.target().setId(new HearingSnapshotKey(randomUUID(), hearingEntity.getId())).setHearing(hearingEntity)));
        hearingEntity.setApplicationDraftResults(Sets.newHashSet(ApplicationDraftResult.applicationDraftResult().setId(randomUUID()).setHearing(hearingEntity)));
        hearingRepository.save(hearingEntity);
    }

    @Test
    void shouldReturnUsersForHearing() {

        saveHearing(hearings.get(0));
        saveHearing(hearings.get(1));

        final UUID hearingId1 = hearings.get(0).getId();
        final UUID hearingId2 = hearings.get(1).getId();

        final UUID id1 = randomUUID();
        final UUID userId1 = randomUUID();
        final ZonedDateTime approvalRequestTime1 = now().minusMinutes(10L);
        final ApprovalType approvalType = ApprovalType.CHANGE;
        final ApprovalRequested approvalRequested1 = new ApprovalRequested(id1, hearingId1, userId1, approvalRequestTime1, approvalType);

        final UUID id2 = randomUUID();
        final UUID userId2 = randomUUID();
        final ZonedDateTime approvalRequestTime2 = now().minusMinutes(12L);
        final ApprovalRequested approvalRequested2 = new ApprovalRequested(id2, hearingId1, userId2, approvalRequestTime2, approvalType);

        final UUID id3 = randomUUID();
        final ZonedDateTime approvalRequestTime3 = now().minusMinutes(12L);
        final ApprovalRequested approvalRequested3 = new ApprovalRequested(id3, hearingId2, userId2, approvalRequestTime3, approvalType);

        approvalRequestedRepository.entityManager.persist(approvalRequested1);
        approvalRequestedRepository.entityManager.persist(approvalRequested2);
        approvalRequestedRepository.entityManager.persist(approvalRequested3);

        final List<UUID> userListForHearingId1 = approvalRequestedRepository.findUsersByHearingId(hearingId1);
        final List<UUID> userListForHearingId2 = approvalRequestedRepository.findUsersByHearingId(hearingId2);

        assertThat(userListForHearingId1.size(), is(2));
        assertThat(userListForHearingId1.get(0), is(userId1));
        assertThat(userListForHearingId1.get(1), is(userId2));
        assertThat(userListForHearingId2.size(), is(1));
        assertThat(userListForHearingId2.get(0), is(userId2));
    }

}
