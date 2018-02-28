package uk.gov.moj.cpp.hearing.persist;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

import uk.gov.moj.cpp.hearing.persist.entity.VerdictHearing;
import uk.gov.moj.cpp.hearing.persist.entity.VerdictValue;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(CdiTestRunner.class)
public class VerdictHearingRepositoryTest {

    final UUID verdictId = randomUUID();
    final UUID hearingId = randomUUID();
    final UUID caseId = randomUUID();
    final UUID defendantId = randomUUID();
    final UUID personId = randomUUID();
    final UUID offenceId = randomUUID();
    LocalDate verdictDate;
    final UUID verdictValueId = randomUUID();
    final String verdictValueCategory = STRING.next();
    final String verdictValueCode = STRING.next();
    final String verdictValueDescription = STRING.next();
    final Integer numberOfSplitJurors = 2;
    final Integer numberOfJurors = 11;
    final Boolean unanimous = false;


    @Inject
    private VerdictHearingRepository verdictHearingRepository;

    @Before
    public void setUp() {
        verdictDate = LocalDate.now();
    }
    @Test
    public void shouldFindAllVerdicts() {
        final VerdictValue verdictValue = new VerdictValue.Builder()
                .withId(verdictValueId)
                .withCategory(verdictValueCategory)
                .withCode(verdictValueCode)
                .withDescription(verdictValueDescription).build();
        final VerdictHearing verdictHearingToSave = new VerdictHearing.Builder()
                .withVerdictId(verdictId)
                .withHearingId(hearingId)
                .withCaseId(caseId)
                .withPersonId(personId)
                .withDefendantId(defendantId)
                .withOffenceId(offenceId)
                .withValue(verdictValue)
                .withVerdictDate(verdictDate)
                .withNumberOfSplitJurors(numberOfSplitJurors)
                .withNumberOfJurors(numberOfJurors)
                .withUnanimous(unanimous).build();
        
        verdictHearingRepository.save(verdictHearingToSave);

        final List<VerdictHearing> verdictsRetrieved = verdictHearingRepository.findAll();

        assertThat(verdictsRetrieved.get(0).getVerdictId(), is(verdictId));
        assertThat(verdictsRetrieved.get(0).getHearingId(), is(hearingId));
        assertThat(verdictsRetrieved.get(0).getCaseId(), is(caseId));
        assertThat(verdictsRetrieved.get(0).getDefendantId(), is(defendantId));
        assertThat(verdictsRetrieved.get(0).getPersonId(), is(personId));
        assertThat(verdictsRetrieved.get(0).getOffenceId(), is(offenceId));
        assertThat(verdictsRetrieved.get(0).getValue().getId(), is(verdictValueId));
        assertThat(verdictsRetrieved.get(0).getValue().getCategory(), is(verdictValueCategory));
        assertThat(verdictsRetrieved.get(0).getValue().getCode(), is(verdictValueCode));
        assertThat(verdictsRetrieved.get(0).getValue().getDescription(), is(verdictValueDescription));
                assertThat(verdictsRetrieved.get(0).getVerdictDate(),is(this.verdictDate));
        assertThat(verdictsRetrieved.get(0).getNumberOfSplitJurors(), is(numberOfSplitJurors));
        assertThat(verdictsRetrieved.get(0).getNumberOfJurors(), is(numberOfJurors));
        assertThat(verdictsRetrieved.get(0).getUnanimous(), is(unanimous));

    }

    @Test
    public void shouldFindVerdictByCaseId() {
        final VerdictValue verdictValue = new VerdictValue.Builder()
                .withId(verdictValueId)
                .withCategory(verdictValueCategory)
                .withCode(verdictValueCode)
                .withDescription(verdictValueDescription).build();
        final VerdictHearing verdictHearingToSave = new VerdictHearing.Builder()
                .withVerdictId(verdictId)
                .withHearingId(hearingId)
                .withCaseId(caseId)
                .withPersonId(personId)
                .withDefendantId(defendantId)
                .withOffenceId(offenceId)
                .withValue(verdictValue)
                .withVerdictDate(verdictDate)
                .withNumberOfSplitJurors(numberOfSplitJurors)
                .withNumberOfJurors(numberOfJurors)
                .withUnanimous(unanimous).build();

        verdictHearingRepository.save(verdictHearingToSave);

        final List<VerdictHearing> verdictsRetrieved = verdictHearingRepository.findByCaseId(caseId);

        assertThat(verdictsRetrieved.get(0).getVerdictId(), is(verdictId));
        assertThat(verdictsRetrieved.get(0).getHearingId(), is(hearingId));
        assertThat(verdictsRetrieved.get(0).getCaseId(), is(caseId));
        assertThat(verdictsRetrieved.get(0).getDefendantId(), is(defendantId));
        assertThat(verdictsRetrieved.get(0).getPersonId(), is(personId));
        assertThat(verdictsRetrieved.get(0).getOffenceId(), is(offenceId));
        assertThat(verdictsRetrieved.get(0).getValue().getId(), is(verdictValueId));
        assertThat(verdictsRetrieved.get(0).getValue().getCategory(), is(verdictValueCategory));
        assertThat(verdictsRetrieved.get(0).getValue().getCode(), is(verdictValueCode));
        assertThat(verdictsRetrieved.get(0).getValue().getDescription(), is(verdictValueDescription));
        assertThat(verdictsRetrieved.get(0).getVerdictDate(),is(this.verdictDate));
        assertThat(verdictsRetrieved.get(0).getNumberOfSplitJurors(), is(numberOfSplitJurors));
        assertThat(verdictsRetrieved.get(0).getNumberOfJurors(), is(numberOfJurors));
        assertThat(verdictsRetrieved.get(0).getUnanimous(), is(unanimous));

    }

    //TODO BUILDER
    @Test
    public void shouldFindVerdictByHearingId() {
        final VerdictValue verdictValue = new VerdictValue.Builder()
                .withId(verdictValueId)
                .withCategory(verdictValueCategory)
                .withCode(verdictValueCode)
                .withDescription(verdictValueDescription).build();
        final VerdictHearing verdictHearingToSave = new VerdictHearing.Builder()
                .withVerdictId(verdictId)
                .withHearingId(hearingId)
                .withCaseId(caseId)
                .withPersonId(personId)
                .withDefendantId(defendantId)
                .withOffenceId(offenceId)
                .withValue(verdictValue)
                .withVerdictDate(verdictDate)
                .withNumberOfSplitJurors(numberOfSplitJurors)
                .withNumberOfJurors(numberOfJurors)
                .withUnanimous(unanimous).build();

        verdictHearingRepository.save(verdictHearingToSave);

        final List<VerdictHearing> verdictsRetrieved = verdictHearingRepository.findByHearingId(this.hearingId);

        assertThat(verdictsRetrieved.get(0).getVerdictId(), is(this.verdictId));
        assertThat(verdictsRetrieved.get(0).getCaseId(), is(this.caseId));
        assertThat(verdictsRetrieved.get(0).getDefendantId(), is(this.defendantId));
        assertThat(verdictsRetrieved.get(0).getPersonId(), is(this.personId));
        assertThat(verdictsRetrieved.get(0).getOffenceId(), is(this.offenceId));
        assertThat(verdictsRetrieved.get(0).getValue().getId(), is(verdictValueId));
        assertThat(verdictsRetrieved.get(0).getValue().getCategory(), is(verdictValueCategory));
        assertThat(verdictsRetrieved.get(0).getValue().getCode(), is(verdictValueCode));
        assertThat(verdictsRetrieved.get(0).getValue().getDescription(), is(verdictValueDescription));
        assertThat(verdictsRetrieved.get(0).getVerdictDate() ,is(this.verdictDate));
        assertThat(verdictsRetrieved.get(0).getNumberOfSplitJurors(), is(numberOfSplitJurors));
        assertThat(verdictsRetrieved.get(0).getNumberOfJurors(), is(numberOfJurors));
        assertThat(verdictsRetrieved.get(0).getUnanimous(), is(unanimous));

    }

    @Test
    public void shouldFindVerdictByPrimaryKey() {
        final VerdictValue verdictValue = new VerdictValue.Builder()
                .withId(verdictValueId)
                .withCategory(verdictValueCategory)
                .withCode(verdictValueCode)
                .withDescription(verdictValueDescription).build();
        final VerdictHearing verdictHearingToSave = new VerdictHearing.Builder()
                .withVerdictId(verdictId)
                .withHearingId(hearingId)
                .withCaseId(caseId)
                .withPersonId(personId)
                .withDefendantId(defendantId)
                .withOffenceId(offenceId)
                .withValue(verdictValue)
                .withVerdictDate(verdictDate)
                .withNumberOfSplitJurors(numberOfSplitJurors)
                .withNumberOfJurors(numberOfJurors)
                .withUnanimous(unanimous).build();

        verdictHearingRepository.save(verdictHearingToSave);

        final VerdictHearing verdictsRetrieved = verdictHearingRepository.findBy(verdictId);

        assertThat(verdictsRetrieved.getVerdictId(), is(verdictId));
        assertThat(verdictsRetrieved.getHearingId(), is(hearingId));
        assertThat(verdictsRetrieved.getCaseId(), is(caseId));
        assertThat(verdictsRetrieved.getDefendantId(), is(defendantId));
        assertThat(verdictsRetrieved.getPersonId(), is(personId));
        assertThat(verdictsRetrieved.getOffenceId(), is(offenceId));
        assertThat(verdictsRetrieved.getValue().getId(), is(verdictValueId));
        assertThat(verdictsRetrieved.getValue().getCategory(), is(verdictValueCategory));
        assertThat(verdictsRetrieved.getValue().getCode(), is(verdictValueCode));
        assertThat(verdictsRetrieved.getValue().getDescription(), is(verdictValueDescription));
        assertThat(verdictsRetrieved.getVerdictDate() ,is(this.verdictDate));
        assertThat(verdictsRetrieved.getNumberOfSplitJurors(), is(numberOfSplitJurors));
        assertThat(verdictsRetrieved.getNumberOfJurors(), is(numberOfJurors));
        assertThat(verdictsRetrieved.getUnanimous(), is(unanimous));

    }
}
