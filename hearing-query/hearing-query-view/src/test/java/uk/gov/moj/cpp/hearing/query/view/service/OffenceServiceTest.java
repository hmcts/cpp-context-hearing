package uk.gov.moj.cpp.hearing.query.view.service;

import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;
import com.jayway.jsonpath.JsonPath;
import com.sun.org.apache.xpath.internal.operations.String;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.moj.cpp.hearing.persist.HearingCaseRepository;
import uk.gov.moj.cpp.hearing.persist.HearingJudgeRepository;
import uk.gov.moj.cpp.hearing.persist.HearingRepository;
import uk.gov.moj.cpp.hearing.persist.PleaHearingRepository;
import uk.gov.moj.cpp.hearing.persist.VerdictHearingRepository;
import uk.gov.moj.cpp.hearing.persist.entity.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.HearingCase;
import uk.gov.moj.cpp.hearing.persist.entity.HearingJudge;
import uk.gov.moj.cpp.hearing.persist.entity.PleaHearing;
import uk.gov.moj.cpp.hearing.persist.entity.VerdictHearing;
import uk.gov.moj.cpp.hearing.query.view.HearingTestUtils;
import uk.gov.moj.cpp.hearing.query.view.response.HearingView;

import javax.inject.Inject;
import javax.json.JsonObject;
import javax.json.JsonString;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.withMetadataEnvelopedFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.query.view.HearingTestUtils.getHearing;

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
        final UUID hearingId = randomUUID();
        final UUID defendantId = randomUUID();
        final UUID offenceIdOne = randomUUID();
        final UUID offenceIdTwo = randomUUID();
        final UUID personId = randomUUID();
        final ArrayList<VerdictHearing> verdicts = new ArrayList<>();
        final LocalDate now=LocalDate.now();
        verdicts.add(new VerdictHearing(randomUUID(), hearingId, caseId, personId, defendantId, offenceIdOne, "GUILTY", now));
        verdicts.add(new VerdictHearing(randomUUID(), hearingId, caseId, personId, defendantId, offenceIdTwo, "NOT GUILTY", now));

        final ArrayList<PleaHearing> pleaHearings = new ArrayList<>();
        pleaHearings.add(new PleaHearing(randomUUID(), hearingId, caseId, defendantId, offenceIdOne, now, "NOT GUILTY", personId));
        pleaHearings.add(new PleaHearing(randomUUID(), hearingId, caseId, defendantId, offenceIdTwo, now, "NOT GUILTY", personId));


        when(this.pleaHearingRepository.findByHearingId(hearingId)).thenReturn(pleaHearings);
        when(this.verdictHearingRepository.findByHearingId(hearingId)).thenReturn(verdicts);

        JsonObject jsonObject = this.offenceService.getOffencesByHearingId(hearingId);

        assertThat(jsonObject, is(
                payloadIsJson(allOf(
                        withJsonPath("$.offences", IsCollectionWithSize.hasSize(2)),
                        withJsonPath("$.offences[0].caseId", equalTo(caseId.toString())),
                        withJsonPath("$.offences[0].defendantId", equalTo(defendantId.toString())),
                        withJsonPath("$.offences[0].offenceId", equalTo(offenceIdOne.toString())),
                        withJsonPath("$.offences[0].personId", equalTo(personId.toString())),
                        withJsonPath("$.offences[0].plea.value", equalTo("NOT GUILTY")),
                        withJsonPath("$.offences[0].verdict.value", equalTo("GUILTY")),
                        withJsonPath("$.offences[1].caseId", equalTo(caseId.toString())),
                        withJsonPath("$.offences[1].defendantId", equalTo(defendantId.toString())),
                        withJsonPath("$.offences[1].offenceId", equalTo(offenceIdTwo.toString())),
                        withJsonPath("$.offences[1].personId", equalTo(personId.toString())),
                        withJsonPath("$.offences[1].plea.value", equalTo("NOT GUILTY")),
                        withJsonPath("$.offences[1].verdict.value", equalTo("NOT GUILTY")),
                        withJsonPath("$.offences[1].verdict.verdictDate", equalTo(now.toString()))
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


        when(this.pleaHearingRepository.findByHearingId(hearingId)).thenReturn(pleaHearings);
        when(this.verdictHearingRepository.findByHearingId(hearingId)).thenReturn(verdicts);

        JsonObject jsonObject = this.offenceService.getOffencesByHearingId(hearingId);
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
        final UUID hearingId = randomUUID();
        final UUID defendantId = randomUUID();
        final UUID offenceIdOne = randomUUID();
        final UUID offenceIdTwo = randomUUID();
        final UUID personId = randomUUID();
        final ArrayList<VerdictHearing> verdicts = new ArrayList<>();
        final LocalDate now=LocalDate.now();
        verdicts.add(new VerdictHearing(randomUUID(), hearingId, caseId, personId, defendantId, offenceIdOne, "GUILTY", now));

        final ArrayList<PleaHearing> pleaHearings = new ArrayList<>();
        pleaHearings.add(new PleaHearing(randomUUID(), hearingId, caseId, defendantId, offenceIdOne, now, "NOT GUILTY", personId));
        pleaHearings.add(new PleaHearing(randomUUID(), hearingId, caseId, defendantId, offenceIdTwo, now, "NOT GUILTY", personId));


        when(this.pleaHearingRepository.findByHearingId(hearingId)).thenReturn(pleaHearings);
        when(this.verdictHearingRepository.findByHearingId(hearingId)).thenReturn(verdicts);

        JsonObject jsonObject = this.offenceService.getOffencesByHearingId(hearingId);

        assertThat(jsonObject, is(
                payloadIsJson(allOf(
                        withJsonPath("$.offences", IsCollectionWithSize.hasSize(2)),
                        withJsonPath("$.offences[0].caseId", equalTo(caseId.toString())),
                        withJsonPath("$.offences[0].defendantId", equalTo(defendantId.toString())),
                        withJsonPath("$.offences[0].offenceId", equalTo(offenceIdOne.toString())),
                        withJsonPath("$.offences[0].personId", equalTo(personId.toString())),
                        withJsonPath("$.offences[0].plea.value", equalTo("NOT GUILTY")),
                        withJsonPath("$.offences[0].plea.pleaDate", equalTo(now.toString())),
                        withJsonPath("$.offences[0].verdict.value", equalTo("GUILTY")),
                        withJsonPath("$.offences[0].verdict.verdictDate", equalTo(now.toString())),
                        withJsonPath("$.offences[1].caseId", equalTo(caseId.toString())),
                        withJsonPath("$.offences[1].defendantId", equalTo(defendantId.toString())),
                        withJsonPath("$.offences[1].offenceId", equalTo(offenceIdTwo.toString())),
                        withJsonPath("$.offences[1].personId", equalTo(personId.toString())),
                        withJsonPath("$.offences[1].plea.value", equalTo("NOT GUILTY"))
                ))
        ));

    }
}