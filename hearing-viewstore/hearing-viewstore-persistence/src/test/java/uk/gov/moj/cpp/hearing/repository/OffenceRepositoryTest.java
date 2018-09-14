package uk.gov.moj.cpp.hearing.repository;

import static org.junit.Assert.assertEquals;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.initiateHearingTemplateForMagistrates;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.mapping.HearingJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Offence;
import uk.gov.moj.cpp.hearing.test.CoreTestTemplates;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@RunWith(CdiTestRunner.class)
public class OffenceRepositoryTest {

    private static final List<uk.gov.justice.json.schemas.core.Hearing> hearings = new ArrayList<>();
    private static final List<Offence> offences = new ArrayList<>();

    @Inject
    private HearingRepository hearingRepository;

    @Inject
    private OffenceRepository offenceRepositoryTest;

    @Inject
    private HearingJPAMapper hearingJPAMapper;

    @BeforeClass
    public static void create() {

        final InitiateHearingCommand initiateHearingCommand = with(initiateHearingTemplateForMagistrates(), i -> {
            i.getHearing().getProsecutionCases().stream()
                    .flatMap(p -> p.getDefendants().stream())
                    .flatMap(d -> d.getOffences().stream())
                    .forEach(o -> o.setPlea(CoreTestTemplates.plea(o.getId(), o.getConvictionDate()).build()));
        });

        hearings.add(initiateHearingCommand.getHearing());
    }

    @Before
    public void setup() {

        hearings.forEach(hearing -> {

            Hearing hearingEntity = hearingJPAMapper.toJPA(hearing);

            hearingEntity.getProsecutionCases().forEach(prosecutionCase -> prosecutionCase.getDefendants().forEach(defendant -> {
                defendant.getOffences().forEach(offences::add);
            }));

            hearingRepository.save(hearingEntity);
        });
    }

    @After
    public void teardown() {
        hearings.forEach(hearing -> hearingRepository.attachAndRemove(hearingRepository.findBy(hearing.getId())));
    }

    @Test
    public void shouldFindAll() {
        assertEquals(offences.size(), offenceRepositoryTest.findAll().size());
    }

    @Test
    public void shouldFindByOffenceIdOriginHearingId() {

        final List<Offence> offenceList = offenceRepositoryTest.findByOffenceIdAndOriginatingHearingId(
                offences.get(0).getId().getId(),
                hearings.get(0).getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getPlea().getOriginatingHearingId());

        assertEquals(offences.get(0).getId().getId(), offenceList.get(0).getId().getId());

        assertEquals(hearings.get(0).getId(), offenceList.get(0).getId().getHearingId());
    }
}
