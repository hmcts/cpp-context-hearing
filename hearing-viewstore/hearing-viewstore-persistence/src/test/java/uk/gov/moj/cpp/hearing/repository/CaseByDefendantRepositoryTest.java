package uk.gov.moj.cpp.hearing.repository;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.minimumInitiateHearingTemplate;

import uk.gov.justice.core.courts.Person;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.mapping.HearingJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.application.ApplicationDraftResult;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Target;
import uk.gov.moj.cpp.hearing.query.CaseByDefendant;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;

import javax.inject.Inject;

import com.google.common.collect.Sets;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(CdiTestRunner.class)
public class CaseByDefendantRepositoryTest {

    @Inject
    private HearingJPAMapper hearingJPAMapper;

    @Inject
    private CaseByDefendantRepository caseByDefendantRepository;

    @Inject
    private HearingRepository hearingRepository;

    private uk.gov.justice.core.courts.Hearing hearing;
    private ProsecutionCase prosecutionCase;
    private Person person;

    @Before
    public void setUp(){
        final InitiateHearingCommand hearingCommand = minimumInitiateHearingTemplate();
        hearing = hearingCommand.getHearing();
        saveHearing(hearing);
        prosecutionCase = hearing.getProsecutionCases().get(0);
        person = prosecutionCase.getDefendants().get(0).getPersonDefendant().getPersonDetails();
    }

    @Test
    public void shouldGetCaseByDefendantWithCaseSet(){
        final ZonedDateTime hearingDate = hearing.getHearingDays().get(0).getSittingDay();
        List<CaseByDefendant> cases = caseByDefendantRepository.getCasesByPersonDefendant(person.getFirstName(), person.getLastName(), person.getDateOfBirth(), hearingDate.toLocalDate(), new HashSet(asList(prosecutionCase.getId())));
        assertThat(cases.size(), is(1));
        assertThat(cases.get(0).getCaseId(), is(prosecutionCase.getId()));
        assertThat(cases.get(0).getUrn(),  notNullValue());
    }

    @Test
    public void shouldGetCaseByDefendantWithEmptyCaseSet(){
        final ZonedDateTime hearingDate = hearing.getHearingDays().get(0).getSittingDay();
        List<CaseByDefendant> cases = caseByDefendantRepository.getCasesByPersonDefendant(person.getFirstName(), person.getLastName(), person.getDateOfBirth(), hearingDate.toLocalDate(), new HashSet());
        assertThat(cases.size(), is(1));
        assertThat(cases.get(0).getCaseId(), is(prosecutionCase.getId()));
        assertThat(cases.get(0).getUrn(),  notNullValue());
    }

    public void saveHearing(final uk.gov.justice.core.courts.Hearing hearing) {
        final Hearing hearingEntity = hearingJPAMapper.toJPA(hearing);
        hearingEntity.setCourtApplicationsJson(hearingEntity.getCourtApplicationsJson().substring(0, 255));
        hearingEntity.getProsecutionCases().iterator().next().setMarkers(null);
        hearingEntity.setTargets(Sets.newHashSet(Target.target().setId(new HearingSnapshotKey(randomUUID(), hearingEntity.getId())).setHearing(hearingEntity)));
        hearingEntity.setApplicationDraftResults(Sets.newHashSet(ApplicationDraftResult.applicationDraftResult().setId(randomUUID()).setHearing(hearingEntity)));
        hearingRepository.save(hearingEntity);
    }
}
