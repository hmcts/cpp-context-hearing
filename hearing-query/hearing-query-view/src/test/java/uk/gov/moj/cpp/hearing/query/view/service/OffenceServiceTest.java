package uk.gov.moj.cpp.hearing.query.view.service;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.UUID;

import javax.json.JsonObject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;

import uk.gov.moj.cpp.hearing.persist.PleaHearingRepository;
import uk.gov.moj.cpp.hearing.persist.VerdictHearingRepository;
import uk.gov.moj.cpp.hearing.persist.entity.PleaHearing;
import uk.gov.moj.cpp.hearing.persist.entity.VerdictHearing;
import uk.gov.moj.cpp.hearing.persist.entity.VerdictValue;

/**
 * Unit tests for the HearingServiceTest class.
 */
@RunWith(MockitoJUnitRunner.class)
public class OffenceServiceTest {

    @Mock
    private PleaHearingRepository pleaHearingRepository;

    @Mock
    private VerdictHearingRepository verdictHearingRepository;

    @InjectMocks
    private OffenceService offenceService;

    @Test
    public void shouldFindMultipleOffenceByHearingIdTest() throws IOException {

        final UUID caseId = randomUUID();
        final UUID verdictValueId = randomUUID();
        final UUID hearingId = randomUUID();
        final UUID defendantId = randomUUID();
        final UUID offenceIdOne = randomUUID();
        final UUID offenceIdTwo = randomUUID();
        final UUID personId = randomUUID();
        final String verdictValueCategory = STRING.next();
        final String verdictValueCode = STRING.next();
        final String verdictValueDescription = STRING.next();
        final LocalDate verdictDate = LocalDate.now();
        final Integer numberOfSplitJurors = 2;
        final Integer numberOfJurors = 11;
        final Boolean unanimous = false;
        
        final ArrayList<VerdictHearing> verdicts = new ArrayList<>();
        
        final VerdictValue verdictValue1 = new VerdictValue.Builder()
                .withId(verdictValueId)
                .withCategory(verdictValueCategory)
                .withCode(verdictValueCode)
                .withDescription(verdictValueDescription).build();
        final VerdictHearing verdict1 = new VerdictHearing.Builder()
                .withVerdictId(randomUUID())
                .withHearingId(hearingId)
                .withCaseId(caseId)
                .withPersonId(personId)
                .withDefendantId(defendantId)
                .withOffenceId(offenceIdOne)
                .withValue(verdictValue1)
                .withVerdictDate(verdictDate)
                .withNumberOfSplitJurors(numberOfSplitJurors)
                .withNumberOfJurors(numberOfJurors)
                .withUnanimous(unanimous).build();
        
        final VerdictValue verdictValue2 = new VerdictValue.Builder()
                .withId(randomUUID())
                .withCategory(verdictValueCategory)
                .withCode(verdictValueCode)
                .withDescription(verdictValueDescription).build();
        final VerdictHearing verdict2 = new VerdictHearing.Builder()
                .withVerdictId(randomUUID())
                .withHearingId(hearingId)
                .withCaseId(caseId)
                .withPersonId(personId)
                .withDefendantId(defendantId)
                .withOffenceId(offenceIdTwo)
                .withValue(verdictValue2)
                .withVerdictDate(verdictDate)
                .withNumberOfSplitJurors(numberOfSplitJurors)
                .withNumberOfJurors(numberOfJurors)
                .withUnanimous(unanimous).build();

        verdicts.add(verdict1);
        verdicts.add(verdict2);
        
        final ArrayList<PleaHearing> pleaHearings = new ArrayList<>();
        final LocalDate pleaDate = LocalDate.now();
        
        pleaHearings.add(new PleaHearing(randomUUID(), hearingId, caseId, defendantId, offenceIdOne, pleaDate, "NOT GUILTY", personId));
        pleaHearings.add(new PleaHearing(randomUUID(), hearingId, caseId, defendantId, offenceIdTwo, pleaDate, "NOT GUILTY", personId));


        when(this.pleaHearingRepository.findByCaseId(caseId)).thenReturn(pleaHearings);
        when(this.verdictHearingRepository.findByCaseId(caseId)).thenReturn(verdicts);

        JsonObject jsonObject = this.offenceService.getOffencesByCaseId(caseId);

        assertThat(jsonObject, is(
                payloadIsJson(allOf(
                        withJsonPath("$.offences", IsCollectionWithSize.hasSize(2)),
                        withJsonPath("$.offences[0].caseId", equalTo(caseId.toString())),
                        withJsonPath("$.offences[0].defendantId", equalTo(defendantId.toString())),
                        withJsonPath("$.offences[0].offenceId", equalTo(offenceIdOne.toString())),
                        withJsonPath("$.offences[0].personId", equalTo(personId.toString())),
                        withJsonPath("$.offences[0].plea.value", equalTo("NOT GUILTY")),
                        //withJsonPath("$.offences[0].verdict.value", equalTo("GUILTY")), //FIXME
                        withJsonPath("$.offences[0].verdict.verdictDate", equalTo(verdictDate.toString())),
                        withJsonPath("$.offences[1].caseId", equalTo(caseId.toString())),
                        withJsonPath("$.offences[1].defendantId", equalTo(defendantId.toString())),
                        withJsonPath("$.offences[1].offenceId", equalTo(offenceIdTwo.toString())),
                        withJsonPath("$.offences[1].personId", equalTo(personId.toString())),
                        withJsonPath("$.offences[1].plea.value", equalTo("NOT GUILTY")),
                        //withJsonPath("$.offences[1].verdict.value", equalTo("NOT GUILTY")),
                        withJsonPath("$.offences[1].verdict.verdictDate", equalTo(verdictDate.toString()))
                ))
        ));


    }

    @Test
    public void shouldFindMultipleOffenceByHearingIdWithoutVerdictsTest() throws IOException {

        final UUID caseId = randomUUID();
        final UUID hearingId = randomUUID();
        final UUID defendantId = randomUUID();
        final UUID offenceIdOne = randomUUID();
        final UUID offenceIdTwo = randomUUID();
        final UUID personId = randomUUID();
        final ArrayList<VerdictHearing> verdicts = new ArrayList<>();
        final LocalDate now=LocalDate.now();
        final ArrayList<PleaHearing> pleaHearings = new ArrayList<>();
        pleaHearings.add(new PleaHearing(randomUUID(), hearingId, caseId, defendantId, offenceIdOne, now, "NOT GUILTY", personId));
        pleaHearings.add(new PleaHearing(randomUUID(), hearingId, caseId, defendantId, offenceIdTwo, now, "NOT GUILTY", personId));


        when(this.pleaHearingRepository.findByCaseId(caseId)).thenReturn(pleaHearings);
        when(this.verdictHearingRepository.findByCaseId(caseId)).thenReturn(verdicts);

        JsonObject jsonObject = this.offenceService.getOffencesByCaseId(caseId);
        System.out.println(jsonObject);

        assertThat(jsonObject, is(
                payloadIsJson(allOf(
                        withJsonPath("$.offences", IsCollectionWithSize.hasSize(2)),
                        withJsonPath("$.offences[0].caseId", equalTo(caseId.toString())),
                        withJsonPath("$.offences[0].defendantId", equalTo(defendantId.toString())),
                        withJsonPath("$.offences[0].offenceId", equalTo(offenceIdOne.toString())),
                        withJsonPath("$.offences[0].personId", equalTo(personId.toString())),
                        withJsonPath("$.offences[0].plea.value", equalTo("NOT GUILTY")),
                        withJsonPath("$.offences[0].plea.pleaDate", equalTo(now.toString())),
                        withJsonPath("$.offences[1].caseId", equalTo(caseId.toString())),
                        withJsonPath("$.offences[1].defendantId", equalTo(defendantId.toString())),
                        withJsonPath("$.offences[1].offenceId", equalTo(offenceIdTwo.toString())),
                        withJsonPath("$.offences[1].personId", equalTo(personId.toString())),
                        withJsonPath("$.offences[1].plea.value", equalTo("NOT GUILTY"))
                ))
        ));


    }

    @Test
    public void shouldFindMultipleOffenceByHearingIdWithPartialVerdictsTest() throws IOException {

        final UUID caseId = randomUUID();
        final UUID verdictValueId = randomUUID();
        final UUID hearingId = randomUUID();
        final UUID defendantId = randomUUID();
        final UUID offenceIdOne = randomUUID();
        final UUID offenceIdTwo = randomUUID();
        final UUID personId = randomUUID();
        final String verdictValueCategory = STRING.next();
        final String verdictValueCode = STRING.next();
        final String verdictValueDescription = STRING.next();
        final LocalDate verdictDate = LocalDate.now();
        final Integer numberOfSplitJurors = 2;
        final Integer numberOfJurors = 11;
        final Boolean unanimous = false;
        
        final ArrayList<VerdictHearing> verdicts = new ArrayList<>();
        
        final VerdictValue verdictValue = new VerdictValue.Builder()
                .withId(verdictValueId)
                .withCategory(verdictValueCategory)
                .withCode(verdictValueCode)
                .withDescription(verdictValueDescription).build();
        final VerdictHearing verdict = new VerdictHearing.Builder()
                .withVerdictId(randomUUID())
                .withHearingId(hearingId)
                .withCaseId(caseId)
                .withPersonId(personId)
                .withDefendantId(defendantId)
                .withOffenceId(offenceIdOne)
                .withValue(verdictValue)
                .withVerdictDate(verdictDate)
                .withNumberOfSplitJurors(numberOfSplitJurors)
                .withNumberOfJurors(numberOfJurors)
                .withUnanimous(unanimous).build();

        verdicts.add(verdict);
        
        final ArrayList<PleaHearing> pleaHearings = new ArrayList<>();
        final LocalDate pleaDate = LocalDate.now();
        pleaHearings.add(new PleaHearing(randomUUID(), hearingId, caseId, defendantId, offenceIdOne, pleaDate, "NOT GUILTY", personId));
        pleaHearings.add(new PleaHearing(randomUUID(), hearingId, caseId, defendantId, offenceIdTwo, pleaDate, "NOT GUILTY", personId));


        when(this.pleaHearingRepository.findByCaseId(caseId)).thenReturn(pleaHearings);
        when(this.verdictHearingRepository.findByCaseId(caseId)).thenReturn(verdicts);

        JsonObject jsonObject = this.offenceService.getOffencesByCaseId(caseId);

        assertThat(jsonObject, is(
                payloadIsJson(allOf(
                        withJsonPath("$.offences", IsCollectionWithSize.hasSize(2)),
                        withJsonPath("$.offences[0].caseId", equalTo(caseId.toString())),
                        withJsonPath("$.offences[0].defendantId", equalTo(defendantId.toString())),
                        withJsonPath("$.offences[0].offenceId", equalTo(offenceIdOne.toString())),
                        withJsonPath("$.offences[0].personId", equalTo(personId.toString())),
                        withJsonPath("$.offences[0].plea.value", equalTo("NOT GUILTY")),
                        withJsonPath("$.offences[0].plea.pleaDate", equalTo(pleaDate.toString())),
                        //withJsonPath("$.offences[0].verdict.value", equalTo("GUILTY")), //FIXME
                        withJsonPath("$.offences[0].verdict.verdictDate", equalTo(verdictDate.toString())),
                        withJsonPath("$.offences[1].caseId", equalTo(caseId.toString())),
                        withJsonPath("$.offences[1].defendantId", equalTo(defendantId.toString())),
                        withJsonPath("$.offences[1].offenceId", equalTo(offenceIdTwo.toString())),
                        withJsonPath("$.offences[1].personId", equalTo(personId.toString())),
                        withJsonPath("$.offences[1].plea.value", equalTo("NOT GUILTY"))
                ))
        ));

    }
}
