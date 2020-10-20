package uk.gov.moj.cpp.hearing.repository;

import static java.time.ZonedDateTime.now;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.minimumInitiateHearingTemplate;

import uk.gov.justice.core.courts.ApprovalType;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.test.utils.persistence.BaseTransactionalTest;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.mapping.HearingJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.application.ApplicationDraftResult;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ApprovalRequested;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Target;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import com.google.common.collect.Sets;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(CdiTestRunner.class)
public class ApprovalRequestedRepositoryTest extends BaseTransactionalTest {

    private static final List<uk.gov.justice.core.courts.Hearing> hearings = new ArrayList<>();

    @Inject
    private ApprovalRequestedRepository approvalRequestedRepository;

    @Inject
    private HearingRepository hearingRepository;

    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Inject
    private HearingJPAMapper hearingJPAMapper;


    @BeforeClass
    public static void create() {

        final InitiateHearingCommand hearingCommand1 = minimumInitiateHearingTemplate();
        hearings.add(hearingCommand1.getHearing());

        final InitiateHearingCommand hearingCommand2 = minimumInitiateHearingTemplate();
        hearings.add(hearingCommand2.getHearing());
    }


    public void saveHearing(final uk.gov.justice.core.courts.Hearing hearing) {
        final Hearing hearingEntity = hearingJPAMapper.toJPA(hearing);
        hearingEntity.setCourtApplicationsJson(hearingEntity.getCourtApplicationsJson().substring(0, 255));
        hearingEntity.getProsecutionCases().iterator().next().setMarkers(null);
        hearingEntity.setTargets(Sets.newHashSet(Target.target().setId(randomUUID()).setHearing(hearingEntity)));
        hearingEntity.setApplicationDraftResults(Sets.newHashSet(ApplicationDraftResult.applicationDraftResult().setId(randomUUID()).setHearing(hearingEntity)));
        hearingRepository.save(hearingEntity);
    }

    @After
    public void teardown() {
        hearings.forEach(hearing -> hearingRepository.attachAndRemove(hearingRepository.findBy(hearing.getId())));
    }


    @Test
    public void shouldReturnUsersForHearing() {

        saveHearing(hearings.get(0));
        saveHearing(hearings.get(1));

        final UUID hearingId1 = hearings.get(0).getId();
        final UUID hearingId2 = hearings.get(1).getId();

        final UUID id1 = randomUUID();
        final UUID userId1 = randomUUID();
        final ZonedDateTime approvalRequestTime1 = now().minusMinutes(10l);
        final ApprovalType approvalType = ApprovalType.CHANGE;
        final ApprovalRequested approvalRequested1 = new ApprovalRequested(id1,hearingId1 , userId1, approvalRequestTime1,approvalType);

        final UUID id2 = randomUUID();
        final UUID userId2 = randomUUID();
        final ZonedDateTime approvalRequestTime2 = now().minusMinutes(12l);
        final ApprovalRequested approvalRequested2 = new ApprovalRequested(id2, hearingId1, userId2, approvalRequestTime2,approvalType);

        final UUID id3 = randomUUID();
        final ZonedDateTime approvalRequestTime3 = now().minusMinutes(12l);
        final ApprovalRequested approvalRequested3 = new ApprovalRequested(id3, hearingId2, userId2, approvalRequestTime3,approvalType);

        final uk.gov.justice.core.courts.Hearing hearing = hearings.get(0);

        approvalRequestedRepository.save(approvalRequested1);
        approvalRequestedRepository.save(approvalRequested2);

        approvalRequestedRepository.save(approvalRequested3);

        final List<UUID> userListForHearingId1 = approvalRequestedRepository.findUsersByHearingId(hearingId1);
        final List<UUID> userListForHearingId2 = approvalRequestedRepository.findUsersByHearingId(hearingId2);

        assertThat(userListForHearingId1.size(), is(2));
        assertThat(userListForHearingId1.get(0), is(userId1));
        assertThat(userListForHearingId1.get(1), is(userId2));
        assertThat(userListForHearingId2.size(), is(1));
        assertThat(userListForHearingId2.get(0), is(userId2));
    }

}
