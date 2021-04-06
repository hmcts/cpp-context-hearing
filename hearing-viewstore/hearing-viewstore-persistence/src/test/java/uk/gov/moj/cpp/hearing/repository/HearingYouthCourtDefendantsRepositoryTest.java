package uk.gov.moj.cpp.hearing.repository;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.mapping.HearingJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingYouthCourDefendantsKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingYouthCourtDefendants;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.minimumInitiateHearingTemplate;

/**
 * DB integration tests for {@link HearingYouthCourtDefendantsRepositoryTest} class
 */

@RunWith(CdiTestRunner.class)
public class HearingYouthCourtDefendantsRepositoryTest {

    private static final List<uk.gov.justice.core.courts.Hearing> hearings = new ArrayList<>();

    @Inject
    private HearingRepository hearingRepository;

    @Inject
    private HearingJPAMapper hearingJPAMapper;

    @Inject
    private HearingYouthCourtDefendantsRepository hearingYouthCourtDefendantsRepository;

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
        List<UUID> defendantsIdList = hearings.get(0).getProsecutionCases().get(0).getDefendants().stream().map(d -> d.getId()).collect(Collectors.toList());
        final Hearing hearingSaved = hearingRepository.findBy(hearings.get(0).getId());
        saveHearingYouthCourtDefendants(defendantsIdList, hearingSaved);
        final List<HearingYouthCourtDefendants> actual = hearingYouthCourtDefendantsRepository.findAllByHearingId(hearingSaved.getId());
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getId().getHearingId(), is(hearingSaved.getId()));
    }

    private void saveHearingYouthCourtDefendants(List<UUID> defendantsIdList, Hearing hearingSaved) {

        defendantsIdList.stream().forEach(id -> {
            final HearingYouthCourtDefendants hearingYouthCourtDefendants
                    = new HearingYouthCourtDefendants();
        HearingYouthCourDefendantsKey hearingYouthCourDefendantsKey = new HearingYouthCourDefendantsKey(id, hearingSaved.getId());
        hearingYouthCourtDefendants.setId(hearingYouthCourDefendantsKey);
        hearingYouthCourtDefendantsRepository.save(hearingYouthCourtDefendants);
    });
    }
}
