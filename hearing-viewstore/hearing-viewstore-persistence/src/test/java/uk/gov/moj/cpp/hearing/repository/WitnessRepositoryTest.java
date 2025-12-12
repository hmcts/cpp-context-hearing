package uk.gov.moj.cpp.hearing.repository;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.mapping.HearingJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Offence;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Witness;
import uk.gov.moj.cpp.hearing.test.CoreTestTemplates;

import javax.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.initiateHearingTemplateForMagistrates;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;

@RunWith(CdiTestRunner.class)
public class WitnessRepositoryTest {

    private static final List<uk.gov.justice.core.courts.Hearing> hearings = new ArrayList<>();

    private static final List<Offence> offences = new ArrayList<>();


    @Inject
    private HearingRepository hearingRepository;

    @Inject
    private HearingJPAMapper hearingJPAMapper;

    @BeforeClass
    public static void create() {

        final InitiateHearingCommand initiateHearingCommand = with(initiateHearingTemplateForMagistrates(), i -> {
            i.getHearing().getProsecutionCases().stream()
                    .flatMap(p -> p.getDefendants().stream())
                    .flatMap(d -> d.getOffences().stream());
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
            hearingEntity.setCourtApplicationsJson(hearingEntity.getCourtApplicationsJson().substring(0, 255));
            hearingEntity.getProsecutionCases().iterator().next().setMarkers(null);
            hearingEntity.getWitnesses().add(new Witness(UUID.randomUUID(), "Test", hearingEntity));
             hearingRepository.save(hearingEntity);
        });
    }


    @Test
    public void shouldFindWitnessForHearing() {
        Hearing hearing = hearingRepository.findAll().get(0);
        assertEquals(1, hearing.getWitnesses().size());
    }

}


