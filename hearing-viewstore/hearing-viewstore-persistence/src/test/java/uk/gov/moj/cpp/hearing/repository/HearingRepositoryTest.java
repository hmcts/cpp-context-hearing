package uk.gov.moj.cpp.hearing.repository;

import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.minimumInitiateHearingTemplate;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.mapping.HearingJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RunWith(CdiTestRunner.class)
public class HearingRepositoryTest {

    private static final List<uk.gov.justice.json.schemas.core.Hearing> hearings = new ArrayList<>();

    @Inject
    private HearingRepository hearingRepository;

    @Inject
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Inject
    private HearingJPAMapper hearingJPAMapper;

    @BeforeClass
    public static void create() {

        final InitiateHearingCommand initiateHearingCommand = minimumInitiateHearingTemplate();

        hearings.add(initiateHearingCommand.getHearing());
    }

    @Before
    public void setup() {

        hearings.forEach(hearing -> {

            Hearing hearingEntity = hearingJPAMapper.toJPA(hearing);

            hearingRepository.save(hearingEntity);
        });
    }

    @After
    public void teardown() {
        hearings.forEach(hearing -> hearingRepository.attachAndRemove(hearingRepository.findBy(hearing.getId())));
    }

    @Test
    public void shouldFindByStartDate() {
        assertEquals(1, hearingRepository.findByFilters(hearings.get(0).getHearingDays().get(0).getSittingDay().toLocalDate(), hearings.get(0).getCourtCentre().getId(), hearings.get(0).getCourtCentre().getRoomId()).size());
    }

    @Test
    public void shouldFindAll() {
        assertEquals(hearings.size(), hearingRepository.findAll().size());
    }

    @Test
    public void shouldFindByHearingId() {

        final UUID hearingId = hearings.get(0).getId();

        final Hearing hearingEntityRetrieved = hearingRepository.findBy(hearingId);

        assertNotNull(hearingEntityRetrieved);
    }

    @Test
    public void shouldNotFindByHearingId() {
        assertNull(hearingRepository.findBy(randomUUID()));
    }
}
