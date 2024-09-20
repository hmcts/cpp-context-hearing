package uk.gov.moj.cpp.hearing.domain.aggregate.util;


import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import uk.gov.justice.core.courts.BailStatus;
import uk.gov.justice.core.courts.CustodyTimeLimit;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingType;
import uk.gov.justice.core.courts.JurisdictionType;
import uk.gov.justice.core.courts.Level;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.PersonDefendant;
import uk.gov.justice.core.courts.Plea;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.Verdict;
import uk.gov.justice.core.courts.VerdictType;
import uk.gov.moj.cpp.hearing.command.result.SharedResultsCommandPrompt;
import uk.gov.moj.cpp.hearing.command.result.SharedResultsCommandResultLine;
import uk.gov.moj.cpp.hearing.command.result.SharedResultsCommandResultLineV2;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.HearingAggregateMomento;
import uk.gov.moj.cpp.hearing.domain.event.CustodyTimeLimitClockStopped;
import uk.gov.moj.cpp.hearing.eventlog.HearingEvent;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

public class CustodyTimeLimitUtilTest {

    private static final String JURIES_SWORN_IN_CROWN_ONLY = "Jury sworn-in";
    private static final UUID DEFENDANT_FOUND_UNDER_A_DISABILITY = UUID.fromString("d3d94468-02a4-3259-b55d-38e6d163e820");
    private static final UUID WITHDRAWN_RESULT_ID = UUID.fromString("eb2e4c4f-b738-4a4d-9cce-0572cecb7cb8");
    private static final UUID REMAND_STATUS_PROMPT_ID = UUID.fromString("9403f0d7-90b5-4377-84b4-f06a77811362");

    @Test
    public void shouldReturnEmptyWhenProsecutionCaseIsEmpty() {
        final HearingAggregateMomento momento = new HearingAggregateMomento();
        momento.setHearing(Hearing.hearing()
                .build());
        final Stream<Object> stream = CustodyTimeLimitUtil.stopCTLExpiry(momento, null);

        assertThat(stream.findFirst().isPresent(), is(false));
    }

    @Test
    public void shouldReturnEmptyWhenProsecutionCaseIsEmptyForV2() {
        final HearingAggregateMomento momento = new HearingAggregateMomento();
        momento.setHearing(Hearing.hearing()
                .build());
        final Stream<Object> stream = CustodyTimeLimitUtil.stopCTLExpiryForV2(momento, null);

        assertThat(stream.findFirst().isPresent(), is(false));
    }

    @Test
    public void shouldReturnEmptyWhenCustodyTimeLimitIsNull() {
        final HearingAggregateMomento momento = new HearingAggregateMomento();
        final UUID offenceId = randomUUID();
        momento.setHearing(Hearing.hearing()
                .withProsecutionCases(asList(ProsecutionCase.prosecutionCase()
                        .withDefendants(asList(Defendant.defendant()
                                .withOffences(asList(Offence.offence()
                                        .withId(offenceId)
                                        .withPlea(Plea.plea()
                                                .withPleaValue("Guilty")
                                                .build())
                                        .build()))
                                .build()))
                        .build()))
                .build());
        final Stream<Object> stream = CustodyTimeLimitUtil.stopCTLExpiry(momento, null);
        assertThat(stream.findFirst().isPresent(), is(false));
    }

    @Test
    public void shouldReturnEmptyWhenCustodyTimeLimitIsNullForV2() {
        final HearingAggregateMomento momento = new HearingAggregateMomento();
        final UUID offenceId = randomUUID();
        momento.setHearing(Hearing.hearing()
                .withProsecutionCases(asList(ProsecutionCase.prosecutionCase()
                        .withDefendants(asList(Defendant.defendant()
                                .withOffences(asList(Offence.offence()
                                        .withId(offenceId)
                                        .withPlea(Plea.plea()
                                                .withPleaValue("Guilty")
                                                .build())
                                        .build()))
                                .build()))
                        .build()))
                .build());
        final Stream<Object> stream = CustodyTimeLimitUtil.stopCTLExpiryForV2(momento, null);
        assertThat(stream.findFirst().isPresent(), is(false));
    }

    @Test
    public void shouldReturnEmptyWhenPleaIsNull() {
        final HearingAggregateMomento momento = new HearingAggregateMomento();
        final UUID offenceId = randomUUID();
        momento.setHearing(Hearing.hearing()
                .withProsecutionCases(asList(ProsecutionCase.prosecutionCase()
                        .withDefendants(asList(Defendant.defendant()
                                .withOffences(asList(Offence.offence()
                                        .withId(offenceId)
                                        .withCustodyTimeLimit(CustodyTimeLimit.custodyTimeLimit()
                                                .withTimeLimit(LocalDate.now().plusDays(5))
                                                .build())
                                        .build()))
                                .build()))
                        .build()))
                .build());
        final Stream<Object> stream = CustodyTimeLimitUtil.stopCTLExpiry(momento, null);
        assertThat(stream.findFirst().isPresent(), is(false));
    }

    @Test
    public void shouldReturnEmptyWhenPleaIsNullForV2() {
        final HearingAggregateMomento momento = new HearingAggregateMomento();
        final UUID offenceId = randomUUID();
        momento.setHearing(Hearing.hearing()
                .withProsecutionCases(asList(ProsecutionCase.prosecutionCase()
                        .withDefendants(asList(Defendant.defendant()
                                .withOffences(asList(Offence.offence()
                                        .withId(offenceId)
                                        .withCustodyTimeLimit(CustodyTimeLimit.custodyTimeLimit()
                                                .withTimeLimit(LocalDate.now().plusDays(5))
                                                .build())
                                        .build()))
                                .build()))
                        .build()))
                .build());
        final Stream<Object> stream = CustodyTimeLimitUtil.stopCTLExpiryForV2(momento, null);
        assertThat(stream.findFirst().isPresent(), is(false));
    }


    @Test
    public void shouldReturnEmptyWhenPleaIsNotGuilty() {
        final HearingAggregateMomento momento = new HearingAggregateMomento();
        final UUID offenceId = randomUUID();
        momento.setHearing(Hearing.hearing()
                .withProsecutionCases(asList(ProsecutionCase.prosecutionCase()
                        .withDefendants(asList(Defendant.defendant()
                                .withOffences(asList(Offence.offence()
                                        .withId(offenceId)
                                        .withPlea(Plea.plea()
                                                .withPleaValue("Not Guilty")
                                                .build())
                                        .withCustodyTimeLimit(CustodyTimeLimit.custodyTimeLimit()
                                                .withTimeLimit(LocalDate.now().plusDays(5))
                                                .build())
                                        .build()))
                                .build()))
                        .build()))
                .build());
        final Stream<Object> stream = CustodyTimeLimitUtil.stopCTLExpiry(momento, null);
        assertThat(stream.findFirst().isPresent(), is(false));
    }

    @Test
    public void shouldReturnEmptyWhenPleaIsNotGuiltyForV2() {
        final HearingAggregateMomento momento = new HearingAggregateMomento();
        final UUID offenceId = randomUUID();
        momento.setHearing(Hearing.hearing()
                .withProsecutionCases(asList(ProsecutionCase.prosecutionCase()
                        .withDefendants(asList(Defendant.defendant()
                                .withOffences(asList(Offence.offence()
                                        .withId(offenceId)
                                        .withPlea(Plea.plea()
                                                .withPleaValue("Not Guilty")
                                                .build())
                                        .withCustodyTimeLimit(CustodyTimeLimit.custodyTimeLimit()
                                                .withTimeLimit(LocalDate.now().plusDays(5))
                                                .build())
                                        .build()))
                                .build()))
                        .build()))
                .build());
        final Stream<Object> stream = CustodyTimeLimitUtil.stopCTLExpiryForV2(momento, null);
        assertThat(stream.findFirst().isPresent(), is(false));
    }

    @Test
    public void shouldRaiseEventWhenOneOffenceIsGuiltyAndCTLIsNotNull() {
        final HearingAggregateMomento momento = new HearingAggregateMomento();
        final UUID offenceId = randomUUID();
        final UUID hearingId = randomUUID();
        momento.setHearing(Hearing.hearing()
                .withId(hearingId)
                .withProsecutionCases(asList(ProsecutionCase.prosecutionCase()
                        .withDefendants(asList(Defendant.defendant()
                                .withOffences(asList(Offence.offence()
                                                .withId(offenceId)
                                                .withPlea(Plea.plea()
                                                        .withPleaValue("Guilty")
                                                        .build())
                                                .withCustodyTimeLimit(CustodyTimeLimit.custodyTimeLimit()
                                                        .withTimeLimit(LocalDate.now().plusDays(5))
                                                        .build())
                                                .build(),
                                        Offence.offence()
                                                .withId(randomUUID())
                                                .build()))
                                .build()))
                        .build()))
                .build());
        final Stream<Object> stream = CustodyTimeLimitUtil.stopCTLExpiry(momento, null);
        final List<Object> events = stream.collect(Collectors.toList());
        assertThat(events.size(), is(1));

        final CustodyTimeLimitClockStopped custodyTimeLimitClockStopped = (CustodyTimeLimitClockStopped) events.get(0);
        assertThat(custodyTimeLimitClockStopped.getHearingId(), is(hearingId));
        assertThat(custodyTimeLimitClockStopped.getOffenceIds().size(), is(1));
        assertThat(custodyTimeLimitClockStopped.getOffenceIds().containsAll(asList(offenceId)), is(true));
    }

    @Test
    public void shouldRaiseEventWhenOneOffenceIsGuiltyAndCTLIsNotNullForV2() {
        final HearingAggregateMomento momento = new HearingAggregateMomento();
        final UUID offenceId = randomUUID();
        final UUID hearingId = randomUUID();
        momento.setHearing(Hearing.hearing()
                .withId(hearingId)
                .withProsecutionCases(asList(ProsecutionCase.prosecutionCase()
                        .withDefendants(asList(Defendant.defendant()
                                .withOffences(asList(Offence.offence()
                                                .withId(offenceId)
                                                .withPlea(Plea.plea()
                                                        .withPleaValue("Guilty")
                                                        .build())
                                                .withCustodyTimeLimit(CustodyTimeLimit.custodyTimeLimit()
                                                        .withTimeLimit(LocalDate.now().plusDays(5))
                                                        .build())
                                                .build(),
                                        Offence.offence()
                                                .withId(randomUUID())
                                                .build()))
                                .build()))
                        .build()))
                .build());
        final Stream<Object> stream = CustodyTimeLimitUtil.stopCTLExpiryForV2(momento, null);
        final List<Object> events = stream.collect(Collectors.toList());
        assertThat(events.size(), is(1));

        final CustodyTimeLimitClockStopped custodyTimeLimitClockStopped = (CustodyTimeLimitClockStopped) events.get(0);
        assertThat(custodyTimeLimitClockStopped.getHearingId(), is(hearingId));
        assertThat(custodyTimeLimitClockStopped.getOffenceIds().size(), is(1));
        assertThat(custodyTimeLimitClockStopped.getOffenceIds().containsAll(asList(offenceId)), is(true));
    }

    @Test
    public void shouldRaiseEventWhenAllOffenceIsGuiltyAndCTLIsNotNull() {
        final HearingAggregateMomento momento = new HearingAggregateMomento();
        final UUID offence1Id = randomUUID();
        final UUID offence2Id = randomUUID();
        final UUID hearingId = randomUUID();
        momento.setHearing(Hearing.hearing()
                .withId(hearingId)
                .withProsecutionCases(asList(ProsecutionCase.prosecutionCase()
                        .withDefendants(asList(Defendant.defendant()
                                .withOffences(asList(Offence.offence()
                                                .withId(offence1Id)
                                                .withPlea(Plea.plea()
                                                        .withPleaValue("Guilty")
                                                        .build())
                                                .withCustodyTimeLimit(CustodyTimeLimit.custodyTimeLimit()
                                                        .withTimeLimit(LocalDate.now().plusDays(5))
                                                        .build())
                                                .build(),
                                        Offence.offence()
                                                .withId(offence2Id)
                                                .withPlea(Plea.plea()
                                                        .withPleaValue("Guilty")
                                                        .build())
                                                .withCustodyTimeLimit(CustodyTimeLimit.custodyTimeLimit()
                                                        .withTimeLimit(LocalDate.now().plusDays(5))
                                                        .build())
                                                .build()))
                                .build()))
                        .build()))
                .build());
        final Stream<Object> stream = CustodyTimeLimitUtil.stopCTLExpiry(momento, null);
        final List<Object> events = stream.collect(Collectors.toList());
        assertThat(events.size(), is(1));

        final CustodyTimeLimitClockStopped custodyTimeLimitClockStopped = (CustodyTimeLimitClockStopped) events.get(0);
        assertThat(custodyTimeLimitClockStopped.getHearingId(), is(hearingId));
        assertThat(custodyTimeLimitClockStopped.getOffenceIds().size(), is(2));
        assertThat(custodyTimeLimitClockStopped.getOffenceIds().containsAll(asList(offence1Id, offence2Id)), is(true));
    }

    @Test
    public void shouldRaiseEventWhenAllOffenceIsGuiltyAndCTLIsNotNullForV2() {
        final HearingAggregateMomento momento = new HearingAggregateMomento();
        final UUID offence1Id = randomUUID();
        final UUID offence2Id = randomUUID();
        final UUID hearingId = randomUUID();
        momento.setHearing(Hearing.hearing()
                .withId(hearingId)
                .withProsecutionCases(asList(ProsecutionCase.prosecutionCase()
                        .withDefendants(asList(Defendant.defendant()
                                .withOffences(asList(Offence.offence()
                                                .withId(offence1Id)
                                                .withPlea(Plea.plea()
                                                        .withPleaValue("Guilty")
                                                        .build())
                                                .withCustodyTimeLimit(CustodyTimeLimit.custodyTimeLimit()
                                                        .withTimeLimit(LocalDate.now().plusDays(5))
                                                        .build())
                                                .build(),
                                        Offence.offence()
                                                .withId(offence2Id)
                                                .withPlea(Plea.plea()
                                                        .withPleaValue("Guilty")
                                                        .build())
                                                .withCustodyTimeLimit(CustodyTimeLimit.custodyTimeLimit()
                                                        .withTimeLimit(LocalDate.now().plusDays(5))
                                                        .build())
                                                .build()))
                                .build()))
                        .build()))
                .build());
        final Stream<Object> stream = CustodyTimeLimitUtil.stopCTLExpiryForV2(momento, null);
        final List<Object> events = stream.collect(Collectors.toList());
        assertThat(events.size(), is(1));

        final CustodyTimeLimitClockStopped custodyTimeLimitClockStopped = (CustodyTimeLimitClockStopped) events.get(0);
        assertThat(custodyTimeLimitClockStopped.getHearingId(), is(hearingId));
        assertThat(custodyTimeLimitClockStopped.getOffenceIds().size(), is(2));
        assertThat(custodyTimeLimitClockStopped.getOffenceIds().containsAll(asList(offence1Id, offence2Id)), is(true));
    }

    @Test
    public void shouldRaiseEventWhenDefendantOnBail() {
        final HearingAggregateMomento momento = new HearingAggregateMomento();
        final UUID offenceId = randomUUID();
        final UUID hearingId = randomUUID();
        momento.setHearing(Hearing.hearing()
                .withId(hearingId)
                .withProsecutionCases(asList(ProsecutionCase.prosecutionCase()
                        .withDefendants(asList(Defendant.defendant()
                                .withPersonDefendant(PersonDefendant.personDefendant()
                                        .withBailStatus(BailStatus.bailStatus()
                                                .withCode("B")
                                                .build())
                                        .build())
                                .withOffences(asList(Offence.offence()
                                        .withId(offenceId)
                                        .withCustodyTimeLimit(CustodyTimeLimit.custodyTimeLimit()
                                                .withTimeLimit(LocalDate.now().plusDays(5))
                                                .build())
                                        .build()))
                                .build()))
                        .build()))
                .build());
        final Stream<Object> stream = CustodyTimeLimitUtil.stopCTLExpiry(momento, null);
        final List<Object> events = stream.collect(Collectors.toList());
        assertThat(events.size(), is(1));

        final CustodyTimeLimitClockStopped custodyTimeLimitClockStopped = (CustodyTimeLimitClockStopped) events.get(0);
        assertThat(custodyTimeLimitClockStopped.getHearingId(), is(hearingId));
        assertThat(custodyTimeLimitClockStopped.getOffenceIds().size(), is(1));
        assertThat(custodyTimeLimitClockStopped.getOffenceIds().containsAll(asList(offenceId)), is(true));
    }

    @Test
    public void shouldRaiseEventWhenDefendantOnBailForV2() {
        final HearingAggregateMomento momento = new HearingAggregateMomento();
        final UUID offenceId = randomUUID();
        final UUID hearingId = randomUUID();
        momento.setHearing(Hearing.hearing()
                .withId(hearingId)
                .withProsecutionCases(asList(ProsecutionCase.prosecutionCase()
                        .withDefendants(asList(Defendant.defendant()
                                .withPersonDefendant(PersonDefendant.personDefendant()
                                        .withBailStatus(BailStatus.bailStatus()
                                                .withCode("B")
                                                .build())
                                        .build())
                                .withOffences(asList(Offence.offence()
                                        .withId(offenceId)
                                        .withCustodyTimeLimit(CustodyTimeLimit.custodyTimeLimit()
                                                .withTimeLimit(LocalDate.now().plusDays(5))
                                                .build())
                                        .build()))
                                .build()))
                        .build()))
                .build());
        final Stream<Object> stream = CustodyTimeLimitUtil.stopCTLExpiryForV2(momento, null);
        final List<Object> events = stream.collect(Collectors.toList());
        assertThat(events.size(), is(1));

        final CustodyTimeLimitClockStopped custodyTimeLimitClockStopped = (CustodyTimeLimitClockStopped) events.get(0);
        assertThat(custodyTimeLimitClockStopped.getHearingId(), is(hearingId));
        assertThat(custodyTimeLimitClockStopped.getOffenceIds().size(), is(1));
        assertThat(custodyTimeLimitClockStopped.getOffenceIds().containsAll(asList(offenceId)), is(true));
    }


    @Test
    public void shouldNotRaiseEventWhenDefendantOffenceVerdictIsNotDefendantFoundUnderADisability() {
        final HearingAggregateMomento momento = new HearingAggregateMomento();
        final UUID offenceId = randomUUID();
        final UUID offence2Id = randomUUID();
        final UUID offence3Id = randomUUID();
        final UUID hearingId = randomUUID();
        momento.setHearing(Hearing.hearing()
                .withId(hearingId)
                .withProsecutionCases(asList(ProsecutionCase.prosecutionCase()
                        .withDefendants(asList(Defendant.defendant()
                                .withOffences(asList(Offence.offence()
                                                .withId(offenceId)
                                                .withVerdict(Verdict.verdict()
                                                        .withVerdictType(VerdictType.verdictType()
                                                                .withId(randomUUID())
                                                                .build())
                                                        .build())
                                                .withCustodyTimeLimit(CustodyTimeLimit.custodyTimeLimit()
                                                        .withTimeLimit(LocalDate.now().plusDays(5))
                                                        .build())
                                                .build(),
                                        Offence.offence()
                                                .withId(offence2Id)
                                                .withCustodyTimeLimit(CustodyTimeLimit.custodyTimeLimit()
                                                        .withTimeLimit(LocalDate.now().plusDays(5))
                                                        .build())
                                                .build(),
                                        Offence.offence()
                                                .withId(offence3Id)
                                                .build()))
                                .build()))
                        .build()))
                .build());
        final Stream<Object> stream = CustodyTimeLimitUtil.stopCTLExpiry(momento, null);
        assertThat(stream.findFirst().isPresent(), is(false));
    }

    @Test
    public void shouldNotRaiseEventWhenDefendantOffenceVerdictIsNotDefendantFoundUnderADisabilityForV2() {
        final HearingAggregateMomento momento = new HearingAggregateMomento();
        final UUID offenceId = randomUUID();
        final UUID offence2Id = randomUUID();
        final UUID offence3Id = randomUUID();
        final UUID hearingId = randomUUID();
        momento.setHearing(Hearing.hearing()
                .withId(hearingId)
                .withProsecutionCases(asList(ProsecutionCase.prosecutionCase()
                        .withDefendants(asList(Defendant.defendant()
                                .withOffences(asList(Offence.offence()
                                                .withId(offenceId)
                                                .withVerdict(Verdict.verdict()
                                                        .withVerdictType(VerdictType.verdictType()
                                                                .withId(randomUUID())
                                                                .build())
                                                        .build())
                                                .withCustodyTimeLimit(CustodyTimeLimit.custodyTimeLimit()
                                                        .withTimeLimit(LocalDate.now().plusDays(5))
                                                        .build())
                                                .build(),
                                        Offence.offence()
                                                .withId(offence2Id)
                                                .withCustodyTimeLimit(CustodyTimeLimit.custodyTimeLimit()
                                                        .withTimeLimit(LocalDate.now().plusDays(5))
                                                        .build())
                                                .build(),
                                        Offence.offence()
                                                .withId(offence3Id)
                                                .build()))
                                .build()))
                        .build()))
                .build());
        final Stream<Object> stream = CustodyTimeLimitUtil.stopCTLExpiryForV2(momento, null);
        assertThat(stream.findFirst().isPresent(), is(false));
    }


    @Test
    public void shouldRaiseEventWhenDefendantOffenceVerdictIsDefendantFoundUnderADisability() {
        final HearingAggregateMomento momento = new HearingAggregateMomento();
        final UUID offenceId = randomUUID();
        final UUID offence2Id = randomUUID();
        final UUID offence3Id = randomUUID();
        final UUID hearingId = randomUUID();
        momento.setHearing(Hearing.hearing()
                .withId(hearingId)
                .withProsecutionCases(asList(ProsecutionCase.prosecutionCase()
                        .withDefendants(asList(Defendant.defendant()
                                .withOffences(asList(Offence.offence()
                                                .withId(offenceId)
                                                .withVerdict(Verdict.verdict()
                                                        .withVerdictType(VerdictType.verdictType()
                                                                .withId(DEFENDANT_FOUND_UNDER_A_DISABILITY)
                                                                .build())
                                                        .build())
                                                .withCustodyTimeLimit(CustodyTimeLimit.custodyTimeLimit()
                                                        .withTimeLimit(LocalDate.now().plusDays(5))
                                                        .build())
                                                .build(),
                                        Offence.offence()
                                                .withId(offence2Id)
                                                .withCustodyTimeLimit(CustodyTimeLimit.custodyTimeLimit()
                                                        .withTimeLimit(LocalDate.now().plusDays(5))
                                                        .build())
                                                .build(),
                                        Offence.offence()
                                                .withId(offence3Id)
                                                .build()))
                                .build()))
                        .build()))
                .build());
        final Stream<Object> stream = CustodyTimeLimitUtil.stopCTLExpiry(momento, null);
        final List<Object> events = stream.collect(Collectors.toList());
        assertThat(events.size(), is(1));

        final CustodyTimeLimitClockStopped custodyTimeLimitClockStopped = (CustodyTimeLimitClockStopped) events.get(0);
        assertThat(custodyTimeLimitClockStopped.getHearingId(), is(hearingId));
        assertThat(custodyTimeLimitClockStopped.getOffenceIds().size(), is(2));
        assertThat(custodyTimeLimitClockStopped.getOffenceIds().containsAll(asList(offenceId, offence2Id)), is(true));
    }

    @Test
    public void shouldRaiseEventWhenDefendantOffenceVerdictIsDefendantFoundUnderADisabilityForV2() {
        final HearingAggregateMomento momento = new HearingAggregateMomento();
        final UUID offenceId = randomUUID();
        final UUID offence2Id = randomUUID();
        final UUID offence3Id = randomUUID();
        final UUID hearingId = randomUUID();
        momento.setHearing(Hearing.hearing()
                .withId(hearingId)
                .withProsecutionCases(asList(ProsecutionCase.prosecutionCase()
                        .withDefendants(asList(Defendant.defendant()
                                .withOffences(asList(Offence.offence()
                                                .withId(offenceId)
                                                .withVerdict(Verdict.verdict()
                                                        .withVerdictType(VerdictType.verdictType()
                                                                .withId(DEFENDANT_FOUND_UNDER_A_DISABILITY)
                                                                .build())
                                                        .build())
                                                .withCustodyTimeLimit(CustodyTimeLimit.custodyTimeLimit()
                                                        .withTimeLimit(LocalDate.now().plusDays(5))
                                                        .build())
                                                .build(),
                                        Offence.offence()
                                                .withId(offence2Id)
                                                .withCustodyTimeLimit(CustodyTimeLimit.custodyTimeLimit()
                                                        .withTimeLimit(LocalDate.now().plusDays(5))
                                                        .build())
                                                .build(),
                                        Offence.offence()
                                                .withId(offence3Id)
                                                .build()))
                                .build()))
                        .build()))
                .build());
        final Stream<Object> stream = CustodyTimeLimitUtil.stopCTLExpiryForV2(momento, null);
        final List<Object> events = stream.collect(Collectors.toList());
        assertThat(events.size(), is(1));

        final CustodyTimeLimitClockStopped custodyTimeLimitClockStopped = (CustodyTimeLimitClockStopped) events.get(0);
        assertThat(custodyTimeLimitClockStopped.getHearingId(), is(hearingId));
        assertThat(custodyTimeLimitClockStopped.getOffenceIds().size(), is(2));
        assertThat(custodyTimeLimitClockStopped.getOffenceIds().containsAll(asList(offenceId, offence2Id)), is(true));
    }

    @Test
    public void shouldRaiseEventWhenDefendantOnBailAndOffenceIsGuilty() {
        final HearingAggregateMomento momento = new HearingAggregateMomento();
        final UUID offenceId = randomUUID();
        final UUID hearingId = randomUUID();
        momento.setHearing(Hearing.hearing()
                .withId(hearingId)
                .withProsecutionCases(asList(ProsecutionCase.prosecutionCase()
                        .withDefendants(asList(Defendant.defendant()
                                .withPersonDefendant(PersonDefendant.personDefendant()
                                        .withBailStatus(BailStatus.bailStatus()
                                                .withCode("B")
                                                .build())
                                        .build())
                                .withOffences(asList(Offence.offence()
                                        .withId(offenceId)
                                        .withPlea(Plea.plea()
                                                .withPleaValue("Guilty")
                                                .build())
                                        .withCustodyTimeLimit(CustodyTimeLimit.custodyTimeLimit()
                                                .withTimeLimit(LocalDate.now().plusDays(5))
                                                .build())
                                        .build()))
                                .build()))
                        .build()))
                .build());
        final Stream<Object> stream = CustodyTimeLimitUtil.stopCTLExpiry(momento, null);
        final List<Object> events = stream.collect(Collectors.toList());
        assertThat(events.size(), is(1));

        final CustodyTimeLimitClockStopped custodyTimeLimitClockStopped = (CustodyTimeLimitClockStopped) events.get(0);
        assertThat(custodyTimeLimitClockStopped.getHearingId(), is(hearingId));
        assertThat(custodyTimeLimitClockStopped.getOffenceIds().size(), is(1));
        assertThat(custodyTimeLimitClockStopped.getOffenceIds().containsAll(asList(offenceId)), is(true));
    }

    @Test
    public void shouldRaiseEventWhenDefendantOnBailAndOffenceIsGuiltyForV2() {
        final HearingAggregateMomento momento = new HearingAggregateMomento();
        final UUID offenceId = randomUUID();
        final UUID hearingId = randomUUID();
        momento.setHearing(Hearing.hearing()
                .withId(hearingId)
                .withProsecutionCases(asList(ProsecutionCase.prosecutionCase()
                        .withDefendants(asList(Defendant.defendant()
                                .withPersonDefendant(PersonDefendant.personDefendant()
                                        .withBailStatus(BailStatus.bailStatus()
                                                .withCode("B")
                                                .build())
                                        .build())
                                .withOffences(asList(Offence.offence()
                                        .withId(offenceId)
                                        .withPlea(Plea.plea()
                                                .withPleaValue("Guilty")
                                                .build())
                                        .withCustodyTimeLimit(CustodyTimeLimit.custodyTimeLimit()
                                                .withTimeLimit(LocalDate.now().plusDays(5))
                                                .build())
                                        .build()))
                                .build()))
                        .build()))
                .build());
        final Stream<Object> stream = CustodyTimeLimitUtil.stopCTLExpiryForV2(momento, null);
        final List<Object> events = stream.collect(Collectors.toList());
        assertThat(events.size(), is(1));

        final CustodyTimeLimitClockStopped custodyTimeLimitClockStopped = (CustodyTimeLimitClockStopped) events.get(0);
        assertThat(custodyTimeLimitClockStopped.getHearingId(), is(hearingId));
        assertThat(custodyTimeLimitClockStopped.getOffenceIds().size(), is(1));
        assertThat(custodyTimeLimitClockStopped.getOffenceIds().containsAll(asList(offenceId)), is(true));
    }

    @Test
    public void shouldRaiseEventWhenOffenceHasFinalResult() {
        final HearingAggregateMomento momento = new HearingAggregateMomento();
        final UUID offenceId = randomUUID();
        final UUID defendantId = randomUUID();
        final UUID hearingId = randomUUID();
        momento.setHearing(Hearing.hearing()
                .withId(hearingId)
                .withProsecutionCases(asList(ProsecutionCase.prosecutionCase()
                        .withDefendants(asList(Defendant.defendant()
                                .withId(defendantId)
                                .withOffences(asList(Offence.offence()
                                                .withId(offenceId)
                                                .withCustodyTimeLimit(CustodyTimeLimit.custodyTimeLimit()
                                                        .withTimeLimit(LocalDate.now().plusDays(5))
                                                        .build())
                                                .build(),
                                        Offence.offence()
                                                .withId(randomUUID())
                                                .build()))
                                .build()))
                        .build()))
                .build());
        final Stream<Object> stream = CustodyTimeLimitUtil.stopCTLExpiry(momento, asList(
                getWithdrawnResult(offenceId, defendantId)));
        final List<Object> events = stream.collect(Collectors.toList());
        assertThat(events.size(), is(1));

        final CustodyTimeLimitClockStopped custodyTimeLimitClockStopped = (CustodyTimeLimitClockStopped) events.get(0);
        assertThat(custodyTimeLimitClockStopped.getHearingId(), is(hearingId));
        assertThat(custodyTimeLimitClockStopped.getOffenceIds().size(), is(1));
        assertThat(custodyTimeLimitClockStopped.getOffenceIds().containsAll(asList(offenceId)), is(true));
    }

    @Test
    public void shouldRaiseEventWhenOffenceHasFinalResultForV2() {
        final HearingAggregateMomento momento = new HearingAggregateMomento();
        final UUID offenceId = randomUUID();
        final UUID defendantId = randomUUID();
        final UUID hearingId = randomUUID();
        momento.setHearing(Hearing.hearing()
                .withId(hearingId)
                .withProsecutionCases(asList(ProsecutionCase.prosecutionCase()
                        .withDefendants(asList(Defendant.defendant()
                                .withId(defendantId)
                                .withOffences(asList(Offence.offence()
                                                .withId(offenceId)
                                                .withCustodyTimeLimit(CustodyTimeLimit.custodyTimeLimit()
                                                        .withTimeLimit(LocalDate.now().plusDays(5))
                                                        .build())
                                                .build(),
                                        Offence.offence()
                                                .withId(randomUUID())
                                                .build()))
                                .build()))
                        .build()))
                .build());
        final Stream<Object> stream = CustodyTimeLimitUtil.stopCTLExpiryForV2(momento, asList(SharedResultsCommandResultLineV2.sharedResultsCommandResultLine()
                .withOffenceId(offenceId)
                .withDefendantId(defendantId)
                .withResultDefinitionId(WITHDRAWN_RESULT_ID)

                .build()));
        final List<Object> events = stream.collect(Collectors.toList());
        assertThat(events.size(), is(1));

        final CustodyTimeLimitClockStopped custodyTimeLimitClockStopped = (CustodyTimeLimitClockStopped) events.get(0);
        assertThat(custodyTimeLimitClockStopped.getHearingId(), is(hearingId));
        assertThat(custodyTimeLimitClockStopped.getOffenceIds().size(), is(1));
        assertThat(custodyTimeLimitClockStopped.getOffenceIds().containsAll(asList(offenceId)), is(true));
    }

    @Test
    public void shouldNotRaiseEventWhenOffenceHasFinalResultButResultIsDeleted() {
        final HearingAggregateMomento momento = new HearingAggregateMomento();
        final UUID offenceId = randomUUID();
        final UUID defendantId = randomUUID();
        final UUID hearingId = randomUUID();
        momento.setHearing(Hearing.hearing()
                .withId(hearingId)
                .withProsecutionCases(asList(ProsecutionCase.prosecutionCase()
                        .withDefendants(asList(Defendant.defendant()
                                .withId(defendantId)
                                .withOffences(asList(Offence.offence()
                                                .withId(offenceId)
                                                .withCustodyTimeLimit(CustodyTimeLimit.custodyTimeLimit()
                                                        .withTimeLimit(LocalDate.now().plusDays(5))
                                                        .build())
                                                .build(),
                                        Offence.offence()
                                                .withId(randomUUID())
                                                .build()))
                                .build()))
                        .build()))
                .build());
        final Stream<Object> stream = CustodyTimeLimitUtil.stopCTLExpiry(momento, asList(
                getDeletedWithdrawnResult(offenceId, defendantId)));

        assertThat(stream.findFirst().isPresent(), is(false));
    }

    @Test
    public void shouldNotRaiseEventWhenOffenceHasFinalResultButResultIsDeletedForV2() {
        final HearingAggregateMomento momento = new HearingAggregateMomento();
        final UUID offenceId = randomUUID();
        final UUID defendantId = randomUUID();
        final UUID hearingId = randomUUID();
        momento.setHearing(Hearing.hearing()
                .withId(hearingId)
                .withProsecutionCases(asList(ProsecutionCase.prosecutionCase()
                        .withDefendants(asList(Defendant.defendant()
                                .withId(defendantId)
                                .withOffences(asList(Offence.offence()
                                                .withId(offenceId)
                                                .withCustodyTimeLimit(CustodyTimeLimit.custodyTimeLimit()
                                                        .withTimeLimit(LocalDate.now().plusDays(5))
                                                        .build())
                                                .build(),
                                        Offence.offence()
                                                .withId(randomUUID())
                                                .build()))
                                .build()))
                        .build()))
                .build());
        final Stream<Object> stream = CustodyTimeLimitUtil.stopCTLExpiryForV2(momento, asList(SharedResultsCommandResultLineV2.sharedResultsCommandResultLine()
                .withIsDeleted(true)
                .withOffenceId(offenceId)
                .withDefendantId(defendantId)
                .withResultDefinitionId(WITHDRAWN_RESULT_ID)

                .build()));

        assertThat(stream.findFirst().isPresent(), is(false));
    }


    @Test
    public void shouldRaiseEventWhenOffenceIsConditionalBail() {
        final HearingAggregateMomento momento = new HearingAggregateMomento();
        final UUID offenceId = randomUUID();
        final UUID defendantId = randomUUID();
        final UUID hearingId = randomUUID();
        momento.setHearing(Hearing.hearing()
                .withId(hearingId)
                .withProsecutionCases(asList(ProsecutionCase.prosecutionCase()
                        .withDefendants(asList(Defendant.defendant()
                                .withId(defendantId)
                                .withOffences(asList(Offence.offence()
                                                .withId(offenceId)
                                                .withCustodyTimeLimit(CustodyTimeLimit.custodyTimeLimit()
                                                        .withTimeLimit(LocalDate.now().plusDays(5))
                                                        .build())
                                                .build(),
                                        Offence.offence()
                                                .withId(randomUUID())
                                                .build()))
                                .build()))
                        .build()))
                .build());
        final Stream<Object> stream = CustodyTimeLimitUtil.stopCTLExpiry(momento, asList(
                getConditionalBailResult(offenceId, defendantId)));
        final List<Object> events = stream.collect(Collectors.toList());
        assertThat(events.size(), is(1));

        final CustodyTimeLimitClockStopped custodyTimeLimitClockStopped = (CustodyTimeLimitClockStopped) events.get(0);
        assertThat(custodyTimeLimitClockStopped.getHearingId(), is(hearingId));
        assertThat(custodyTimeLimitClockStopped.getOffenceIds().size(), is(1));
        assertThat(custodyTimeLimitClockStopped.getOffenceIds().containsAll(asList(offenceId)), is(true));
    }

    @Test
    public void shouldRaiseEventWhenOffenceIsConditionalBailForV2() {
        final HearingAggregateMomento momento = new HearingAggregateMomento();
        final UUID offenceId = randomUUID();
        final UUID defendantId = randomUUID();
        final UUID hearingId = randomUUID();
        momento.setHearing(Hearing.hearing()
                .withId(hearingId)
                .withProsecutionCases(asList(ProsecutionCase.prosecutionCase()
                        .withDefendants(asList(Defendant.defendant()
                                .withId(defendantId)
                                .withOffences(asList(Offence.offence()
                                                .withId(offenceId)
                                                .withCustodyTimeLimit(CustodyTimeLimit.custodyTimeLimit()
                                                        .withTimeLimit(LocalDate.now().plusDays(5))
                                                        .build())
                                                .build(),
                                        Offence.offence()
                                                .withId(randomUUID())
                                                .build()))
                                .build()))
                        .build()))
                .build());
        final Stream<Object> stream = CustodyTimeLimitUtil.stopCTLExpiryForV2(momento, asList(SharedResultsCommandResultLineV2.sharedResultsCommandResultLine()
                .withOffenceId(offenceId)
                .withDefendantId(defendantId)
                .withPrompts(asList(geConditionalBailPrompt()))
                .build()));
        final List<Object> events = stream.collect(Collectors.toList());
        assertThat(events.size(), is(1));

        final CustodyTimeLimitClockStopped custodyTimeLimitClockStopped = (CustodyTimeLimitClockStopped) events.get(0);
        assertThat(custodyTimeLimitClockStopped.getHearingId(), is(hearingId));
        assertThat(custodyTimeLimitClockStopped.getOffenceIds().size(), is(1));
        assertThat(custodyTimeLimitClockStopped.getOffenceIds().containsAll(asList(offenceId)), is(true));
    }

    @Test
    public void shouldRaiseEventWhenOffenceIsUnConditionalBail() {
        final HearingAggregateMomento momento = new HearingAggregateMomento();
        final UUID offenceId = randomUUID();
        final UUID defendantId = randomUUID();
        final UUID hearingId = randomUUID();
        momento.setHearing(Hearing.hearing()
                .withId(hearingId)
                .withProsecutionCases(asList(ProsecutionCase.prosecutionCase()
                        .withDefendants(asList(Defendant.defendant()
                                .withId(defendantId)
                                .withOffences(asList(Offence.offence()
                                                .withId(offenceId)
                                                .withCustodyTimeLimit(CustodyTimeLimit.custodyTimeLimit()
                                                        .withTimeLimit(LocalDate.now().plusDays(5))
                                                        .build())
                                                .build(),
                                        Offence.offence()
                                                .withId(randomUUID())
                                                .build()))
                                .build()))
                        .build()))
                .build());
        final Stream<Object> stream = CustodyTimeLimitUtil.stopCTLExpiry(momento, asList(
                getUnConditionalBailResult(offenceId, defendantId)));
        final List<Object> events = stream.collect(Collectors.toList());
        assertThat(events.size(), is(1));

        final CustodyTimeLimitClockStopped custodyTimeLimitClockStopped = (CustodyTimeLimitClockStopped) events.get(0);
        assertThat(custodyTimeLimitClockStopped.getHearingId(), is(hearingId));
        assertThat(custodyTimeLimitClockStopped.getOffenceIds().size(), is(1));
        assertThat(custodyTimeLimitClockStopped.getOffenceIds().containsAll(asList(offenceId)), is(true));
    }

    @Test
    public void shouldNotRaiseEventWhenOffenceIsUnConditionalBailButResultIsDeleted() {
        final HearingAggregateMomento momento = new HearingAggregateMomento();
        final UUID offenceId = randomUUID();
        final UUID defendantId = randomUUID();
        final UUID hearingId = randomUUID();
        momento.setHearing(Hearing.hearing()
                .withId(hearingId)
                .withProsecutionCases(asList(ProsecutionCase.prosecutionCase()
                        .withDefendants(asList(Defendant.defendant()
                                .withId(defendantId)
                                .withOffences(asList(Offence.offence()
                                                .withId(offenceId)
                                                .withCustodyTimeLimit(CustodyTimeLimit.custodyTimeLimit()
                                                        .withTimeLimit(LocalDate.now().plusDays(5))
                                                        .build())
                                                .build(),
                                        Offence.offence()
                                                .withId(randomUUID())
                                                .build()))
                                .build()))
                        .build()))
                .build());
        final Stream<Object> stream = CustodyTimeLimitUtil.stopCTLExpiry(momento, asList(
                getDeletedConditionalBailResult(offenceId, defendantId)));

        assertThat(stream.findFirst().isPresent(), is(false));
    }

    @Test
    public void shouldNotRaiseEventWhenOffenceIsUnConditionalBailButResultIsDeletedForV2() {
        final HearingAggregateMomento momento = new HearingAggregateMomento();
        final UUID offenceId = randomUUID();
        final UUID defendantId = randomUUID();
        final UUID hearingId = randomUUID();
        momento.setHearing(Hearing.hearing()
                .withId(hearingId)
                .withProsecutionCases(asList(ProsecutionCase.prosecutionCase()
                        .withDefendants(asList(Defendant.defendant()
                                .withId(defendantId)
                                .withOffences(asList(Offence.offence()
                                                .withId(offenceId)
                                                .withCustodyTimeLimit(CustodyTimeLimit.custodyTimeLimit()
                                                        .withTimeLimit(LocalDate.now().plusDays(5))
                                                        .build())
                                                .build(),
                                        Offence.offence()
                                                .withId(randomUUID())
                                                .build()))
                                .build()))
                        .build()))
                .build());
        final Stream<Object> stream = CustodyTimeLimitUtil.stopCTLExpiryForV2(momento, asList(SharedResultsCommandResultLineV2.sharedResultsCommandResultLine()
                .withOffenceId(offenceId)
                .withDefendantId(defendantId)
                .withIsDeleted(true)
                .withPrompts(asList(getUnConditionalBailPrompt()))
                .build()));

        assertThat(stream.findFirst().isPresent(), is(false));
    }

    @Test
    public void shouldRaiseEventWhenOffenceIsUnConditionalBailForV2() {
        final HearingAggregateMomento momento = new HearingAggregateMomento();
        final UUID offenceId = randomUUID();
        final UUID defendantId = randomUUID();
        final UUID hearingId = randomUUID();
        momento.setHearing(Hearing.hearing()
                .withId(hearingId)
                .withProsecutionCases(asList(ProsecutionCase.prosecutionCase()
                        .withDefendants(asList(Defendant.defendant()
                                .withId(defendantId)
                                .withOffences(asList(Offence.offence()
                                                .withId(offenceId)
                                                .withCustodyTimeLimit(CustodyTimeLimit.custodyTimeLimit()
                                                        .withTimeLimit(LocalDate.now().plusDays(5))
                                                        .build())
                                                .build(),
                                        Offence.offence()
                                                .withId(randomUUID())
                                                .build()))
                                .build()))
                        .build()))
                .build());
        final Stream<Object> stream = CustodyTimeLimitUtil.stopCTLExpiryForV2(momento, asList(SharedResultsCommandResultLineV2.sharedResultsCommandResultLine()
                .withOffenceId(offenceId)
                .withDefendantId(defendantId)
                .withPrompts(asList(getUnConditionalBailPrompt()))
                .build()));
        final List<Object> events = stream.collect(Collectors.toList());
        assertThat(events.size(), is(1));

        final CustodyTimeLimitClockStopped custodyTimeLimitClockStopped = (CustodyTimeLimitClockStopped) events.get(0);
        assertThat(custodyTimeLimitClockStopped.getHearingId(), is(hearingId));
        assertThat(custodyTimeLimitClockStopped.getOffenceIds().size(), is(1));
        assertThat(custodyTimeLimitClockStopped.getOffenceIds().containsAll(asList(offenceId)), is(true));
    }


    @Test
    public void shouldNotRaiseEventInEventLogFlowWhenHearingIsMag() {
        final HearingAggregateMomento momento = new HearingAggregateMomento();
        momento.setHearing(Hearing.hearing()
                .withJurisdictionType(JurisdictionType.MAGISTRATES)
                .withType(HearingType.hearingType()
                        .withId(randomUUID())
                        .build())
                .build());

        final HearingEvent hearingEvent = HearingEvent.builder()
                .withRecordedLabel(JURIES_SWORN_IN_CROWN_ONLY)
                .build();
        final Stream<Object> stream = CustodyTimeLimitUtil.stopCTLExpiryForTrialHearingUser(momento, hearingEvent, Arrays.asList(randomUUID()));
        assertThat(stream.findFirst().isPresent(), is(false));

    }

    @Test
    public void shouldNotRaiseEventInEventLogFlowWhenEventLogNotJuriesSworn() {
        final HearingAggregateMomento momento = new HearingAggregateMomento();
        momento.setHearing(Hearing.hearing()
                .withJurisdictionType(JurisdictionType.CROWN)
                .withType(HearingType.hearingType()
                        .withId(randomUUID())
                        .build())
                .build());

        final HearingEvent hearingEvent = HearingEvent.builder()
                .withRecordedLabel("Appellant sworn in")
                .build();
        final Stream<Object> stream = CustodyTimeLimitUtil.stopCTLExpiryForTrialHearingUser(momento, hearingEvent, Arrays.asList(randomUUID()));
        assertThat(stream.findFirst().isPresent(), is(false));

    }

    @Test
    public void shouldNotRaiseEventInEventLogFlowWhenOffenceNotExists() {
        final HearingAggregateMomento momento = new HearingAggregateMomento();
        momento.setHearing(Hearing.hearing()
                .withJurisdictionType(JurisdictionType.CROWN)
                .withType(HearingType.hearingType()
                        .withId(randomUUID())
                        .build())
                .build());

        final HearingEvent hearingEvent = HearingEvent.builder()
                .withRecordedLabel(JURIES_SWORN_IN_CROWN_ONLY)
                .build();
        final Stream<Object> stream = CustodyTimeLimitUtil.stopCTLExpiryForTrialHearingUser(momento, hearingEvent, Arrays.asList(randomUUID()));
        assertThat(stream.findFirst().isPresent(), is(false));

    }

    @Test
    public void shouldNotRaiseEventInEventLogFlowWhenOffenceNotCustodyTimeLimit() {
        final HearingAggregateMomento momento = new HearingAggregateMomento();
        momento.setHearing(Hearing.hearing()
                .withJurisdictionType(JurisdictionType.CROWN)
                .withType(HearingType.hearingType()
                        .withId(randomUUID())
                        .build())
                .withProsecutionCases(asList(ProsecutionCase.prosecutionCase()
                        .withDefendants(asList(Defendant.defendant()
                                .withOffences(asList(Offence.offence().build()))
                                .build()))
                        .build()))
                .build());

        final HearingEvent hearingEvent = HearingEvent.builder()
                .withRecordedLabel(JURIES_SWORN_IN_CROWN_ONLY)
                .build();
        final Stream<Object> stream = CustodyTimeLimitUtil.stopCTLExpiryForTrialHearingUser(momento, hearingEvent, Arrays.asList(randomUUID()));
        assertThat(stream.findFirst().isPresent(), is(false));

    }

    @Test
    public void shouldRaiseEventInEventLogFlowWhenOffenceHasCustodyTimeLimitAndHearingIsCrownAndHearingEventIsJuriesSworn() {
        final HearingAggregateMomento momento = new HearingAggregateMomento();
        final UUID hearingId = randomUUID();
        final UUID offenceId = randomUUID();
        final UUID trialHearingTypeId = randomUUID();
        momento.setHearing(Hearing.hearing()
                .withId(hearingId)
                .withJurisdictionType(JurisdictionType.CROWN)
                .withType(HearingType.hearingType()
                        .withId(trialHearingTypeId)
                        .build())
                .withProsecutionCases(asList(ProsecutionCase.prosecutionCase()
                        .withDefendants(asList(Defendant.defendant()
                                .withOffences(asList(Offence.offence()
                                        .withId(offenceId)
                                        .withCustodyTimeLimit(CustodyTimeLimit.custodyTimeLimit()
                                                .withTimeLimit(LocalDate.now())
                                                .build())
                                        .build()))
                                .build()))
                        .build()))
                .build());

        final HearingEvent hearingEvent = HearingEvent.builder()
                .withRecordedLabel(JURIES_SWORN_IN_CROWN_ONLY)
                .build();

        final Stream<Object> stream = CustodyTimeLimitUtil.stopCTLExpiryForTrialHearingUser(momento, hearingEvent, Arrays.asList(trialHearingTypeId));
        final List<Object> events = stream.collect(Collectors.toList());
        assertThat(events.size(), is(1));

        final CustodyTimeLimitClockStopped custodyTimeLimitClockStopped = (CustodyTimeLimitClockStopped) events.get(0);
        assertThat(custodyTimeLimitClockStopped.getHearingId(), is(hearingId));
        assertThat(custodyTimeLimitClockStopped.getOffenceIds().size(), is(1));
        assertThat(custodyTimeLimitClockStopped.getOffenceIds().containsAll(asList(offenceId)), is(true));

    }

    @Test
    public void shouldNotRaiseEventInEventLogFlowWhenTheHearingTypeIsNewtonHearing() {
        final HearingAggregateMomento momento = new HearingAggregateMomento();
        final UUID hearingId = randomUUID();
        final UUID offenceId = randomUUID();
        final UUID trialHearingTypeId = randomUUID();
        final UUID newtonHearingTypeId = randomUUID();

        momento.setHearing(Hearing.hearing()
                .withId(hearingId)
                .withJurisdictionType(JurisdictionType.CROWN)
                .withType(HearingType.hearingType()
                        .withId(newtonHearingTypeId)
                        .build())
                .withProsecutionCases(asList(ProsecutionCase.prosecutionCase()
                        .withDefendants(asList(Defendant.defendant()
                                .withOffences(asList(Offence.offence()
                                        .withId(offenceId)
                                        .withCustodyTimeLimit(CustodyTimeLimit.custodyTimeLimit()
                                                .withTimeLimit(LocalDate.now())
                                                .build())
                                        .build()))
                                .build()))
                        .build()))
                .build());

        final HearingEvent hearingEvent = HearingEvent.builder()
                .withRecordedLabel(JURIES_SWORN_IN_CROWN_ONLY)
                .build();

        final Stream<Object> stream = CustodyTimeLimitUtil.stopCTLExpiryForTrialHearingUser(momento, hearingEvent, Arrays.asList(trialHearingTypeId));
        final List<Object> events = stream.collect(Collectors.toList());
        assertThat(events.size(), is(0));

    }

    private SharedResultsCommandResultLine getConditionalBailResult(final UUID offenceId, final UUID defendantId) {
        return new SharedResultsCommandResultLine(null,
                LocalDate.now(),
                LocalDate.now(),
                randomUUID(),
                offenceId,
                defendantId,
                randomUUID(),
                asList(geConditionalBailPrompt()),
                "Next Hearing in Crown Court",
                Level.OFFENCE.toString(),
                true,
                true,
                randomUUID(),
                randomUUID(),
                null,
                null,
                null,
                LocalDate.now(),
                false,
                Collections.emptyList(),
                Collections.emptyList(),
                false,
                null
        );
    }

    private SharedResultsCommandResultLine getDeletedConditionalBailResult(final UUID offenceId, final UUID defendantId) {
        return new SharedResultsCommandResultLine(null,
                LocalDate.now(),
                LocalDate.now(),
                randomUUID(),
                offenceId,
                defendantId,
                randomUUID(),
                asList(geConditionalBailPrompt()),
                "Next Hearing in Crown Court",
                Level.OFFENCE.toString(),
                true,
                true,
                randomUUID(),
                randomUUID(),
                null,
                null,
                null,
                LocalDate.now(),
                true,
                Collections.emptyList(),
                Collections.emptyList(),
                false,
                null
        );
    }

    private SharedResultsCommandResultLine getUnConditionalBailResult(final UUID offenceId, final UUID defendantId) {
        return new SharedResultsCommandResultLine(null,
                LocalDate.now(),
                LocalDate.now(),
                randomUUID(),
                offenceId,
                defendantId,
                randomUUID(),
                asList(getUnConditionalBailPrompt()),
                "Next Hearing in Crown Court",
                Level.OFFENCE.toString(),
                true,
                true,
                randomUUID(),
                randomUUID(),
                null,
                null,
                null,
                LocalDate.now(),
                false,
                Collections.emptyList(),
                Collections.emptyList(),
                false,
                null
        );
    }

    private SharedResultsCommandPrompt geConditionalBailPrompt() {
        return new SharedResultsCommandPrompt(REMAND_STATUS_PROMPT_ID, "Remand Status", null, "Conditional Bail", null, null, null);
    }

    private SharedResultsCommandPrompt getUnConditionalBailPrompt() {
        return new SharedResultsCommandPrompt(REMAND_STATUS_PROMPT_ID, "Remand Status", null, "Unconditional Bail", null, null, null);
    }

    private SharedResultsCommandResultLine getWithdrawnResult(final UUID offenceId, final UUID defendantId) {
        return new SharedResultsCommandResultLine(null,
                LocalDate.now(),
                LocalDate.now(),
                randomUUID(),
                offenceId,
                defendantId,
                WITHDRAWN_RESULT_ID,
                null,
                "Withdrawn",
                Level.OFFENCE.toString(),
                true,
                true,
                randomUUID(),
                randomUUID(),
                null,
                null,
                null,
                LocalDate.now(),
                false,
                Collections.emptyList(),
                Collections.emptyList(),
                false,
                null
        );
    }

    private SharedResultsCommandResultLine getDeletedWithdrawnResult(final UUID offenceId, final UUID defendantId) {
        return new SharedResultsCommandResultLine(null,
                LocalDate.now(),
                LocalDate.now(),
                randomUUID(),
                offenceId,
                defendantId,
                WITHDRAWN_RESULT_ID,
                null,
                "Withdrawn",
                Level.OFFENCE.toString(),
                true,
                true,
                randomUUID(),
                randomUUID(),
                null,
                null,
                null,
                LocalDate.now(),
                true,
                Collections.emptyList(),
                Collections.emptyList(),
                false,
                null
        );
    }

}
