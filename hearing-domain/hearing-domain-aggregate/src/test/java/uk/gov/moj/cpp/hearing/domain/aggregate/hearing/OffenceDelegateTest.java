package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;


import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.core.courts.AllocationDecision;
import uk.gov.justice.core.courts.CustodyTimeLimit;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.IndicatedPlea;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.Plea;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.Verdict;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.CustodyTimeLimitClockStopped;
import uk.gov.moj.cpp.hearing.domain.event.CustodyTimeLimitExtended;
import uk.gov.moj.cpp.hearing.domain.event.ExistingHearingUpdated;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.moj.cpp.hearing.domain.event.OffenceAdded;
import uk.gov.moj.cpp.hearing.domain.event.OffenceAddedV2;

class OffenceDelegateTest {

    private HearingAggregateMomento hearingAggregateMomento;
    private OffenceDelegate offenceDelegate;
    private HearingDelegate hearingDelegate;
    private HearingAggregate hearingAggregate;


    @BeforeEach
    public void setup() {

        hearingAggregateMomento = new HearingAggregateMomento();
        hearingAggregateMomento.setHearing(Hearing.hearing().build());
        offenceDelegate = new OffenceDelegate(hearingAggregateMomento);
        hearingDelegate = new HearingDelegate(hearingAggregateMomento);
        hearingAggregate = new HearingAggregate();
        setField(this.hearingAggregate, "offenceDelegate",offenceDelegate);
        setField(this.hearingAggregate, "hearingDelegate",hearingDelegate);
        setField(this.hearingAggregate, "momento",hearingAggregateMomento);
    }

    @Test
    public void shouldUpdateProsecutionCasesDefendantsOffencesWhenExistingHearingUpdated() {
        final UUID hearingId = randomUUID();
        final UUID case1Id = randomUUID();
        final UUID case2Id = randomUUID();
        final UUID case1Defendant1Id = randomUUID();
        final UUID case1Defendant2Id = randomUUID();
        final UUID case2Defendant1Id = randomUUID();
        final UUID case1Defendant1Offence1Id = randomUUID();
        final UUID case1Defendant1Offence2Id = randomUUID();
        final UUID case1Defendant2OffenceId = randomUUID();
        final UUID case2Defendant1OffenceId = randomUUID();

        hearingAggregate.apply(new HearingInitiated(Hearing.hearing()
                .withId(hearingId)
                .withProsecutionCases(new ArrayList<>(asList(ProsecutionCase.prosecutionCase()
                        .withId(case1Id)
                        .withDefendants(new ArrayList<>(asList(Defendant.defendant()
                                .withId(case1Defendant1Id)
                                .withOffences(new ArrayList<>(asList(Offence.offence()
                                        .withId(case1Defendant1Offence1Id)
                                        .build())))
                                .build())))
                        .build())))
                .build()));

        final List<ProsecutionCase> prosecutionCases = asList(ProsecutionCase.prosecutionCase()
                        .withId(case1Id)
                        .withDefendants(asList(Defendant.defendant()
                                        .withId(case1Defendant1Id)
                                        .withOffences(asList(Offence.offence()
                                                .withId(case1Defendant1Offence2Id)
                                                .build()))
                                        .build(),
                                Defendant.defendant()
                                        .withId(case1Defendant2Id)
                                        .withOffences(asList(Offence.offence()
                                                .withId(case1Defendant2OffenceId)
                                                .build()))
                                        .build()))
                        .build(),
                ProsecutionCase.prosecutionCase()
                        .withId(case2Id)
                        .withDefendants(asList(Defendant.defendant()
                                .withId(case2Defendant1Id)
                                .withOffences(asList(Offence.offence()
                                        .withId(case2Defendant1OffenceId)
                                        .build()))
                                .build()))
                        .build());
        hearingAggregate.apply(new ExistingHearingUpdated(hearingId, prosecutionCases, Collections.emptyList()));

        assertThat(hearingAggregateMomento.getHearing().getProsecutionCases().size(), is(2));
        final ProsecutionCase prosecutionCase1 = hearingAggregateMomento.getHearing().getProsecutionCases().get(0);
        assertThat(prosecutionCase1.getId(), is(case1Id));
        assertThat(prosecutionCase1.getDefendants().size(), is(2));

        final Defendant defendant1 = prosecutionCase1.getDefendants().get(0);
        assertThat(defendant1.getId(), is(case1Defendant1Id));
        assertThat(defendant1.getOffences().size(), is(2));
        assertThat(defendant1.getOffences().get(0).getId(), is(case1Defendant1Offence1Id));
        assertThat(defendant1.getOffences().get(1).getId(), is(case1Defendant1Offence2Id));

        final Defendant defendant2 = prosecutionCase1.getDefendants().get(1);
        assertThat(defendant2.getId(), is(case1Defendant2Id));
        assertThat(defendant2.getOffences().size(), is(1));
        assertThat(defendant2.getOffences().get(0).getId(), is(case1Defendant2OffenceId));

        final ProsecutionCase prosecutionCase2 = hearingAggregateMomento.getHearing().getProsecutionCases().get(1);
        assertThat(prosecutionCase2.getId(), is(case2Id));
        assertThat(prosecutionCase2.getDefendants().size(), is(1));

        final Defendant defendant3 = prosecutionCase2.getDefendants().get(0);
        assertThat(defendant3.getId(), is(case2Defendant1Id));
        assertThat(defendant3.getOffences().size(), is(1));
        assertThat(defendant3.getOffences().get(0).getId(), is(case2Defendant1OffenceId));

    }

    @Test
    public void shouldHandleCTLClockStopped() {
        final UUID hearingId = randomUUID();
        final UUID offence1Id = randomUUID();
        final UUID offence2Id = randomUUID();

        hearingAggregateMomento.setHearing(Hearing.hearing()
                .withProsecutionCases(asList(ProsecutionCase.prosecutionCase()
                        .withDefendants(asList(Defendant.defendant()
                                .withOffences(asList(Offence.offence()
                                                .withId(offence1Id)
                                                .withCustodyTimeLimit(CustodyTimeLimit.custodyTimeLimit().build())
                                                .build(),
                                        Offence.offence()
                                                .withId(offence2Id)
                                                .withCustodyTimeLimit(CustodyTimeLimit.custodyTimeLimit().build())
                                                .build()))
                                .build()))
                        .build()))
                .build());

        hearingAggregate.apply(new CustodyTimeLimitClockStopped(hearingId, asList(offence1Id)));

        final List<Offence> offences = hearingAggregateMomento.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences();
        assertThat(offences.get(0).getCustodyTimeLimit(), nullValue());
        assertThat(offences.get(0).getCtlClockStopped(), is(true));

        assertThat(offences.get(1).getCustodyTimeLimit(), notNullValue());
        assertThat(offences.get(1).getCtlClockStopped(), nullValue());
    }

    @Test
    public void shouldUpdateCustodyTimeLimitWhenCTLExistsAlready() {

        final UUID hearingId = randomUUID();
        final UUID offence1Id = randomUUID();
        final UUID offence2Id = randomUUID();
        final LocalDate initialCTLDate = LocalDate.now().minusDays(50);
        final LocalDate extendedCTLDate = LocalDate.now().plusDays(50);

        hearingAggregateMomento.setHearing(Hearing.hearing()
                .withProsecutionCases(asList(ProsecutionCase.prosecutionCase()
                        .withDefendants(asList(Defendant.defendant()
                                .withOffences(asList(Offence.offence()
                                                .withId(offence1Id)
                                                .withCustodyTimeLimit(CustodyTimeLimit.custodyTimeLimit()
                                                        .withTimeLimit(initialCTLDate)
                                                        .withIsCtlExtended(false)
                                                        .build())
                                                .build(),
                                        Offence.offence()
                                                .withId(offence2Id)
                                                .withCustodyTimeLimit(CustodyTimeLimit.custodyTimeLimit().build())
                                                .build()))
                                .build()))
                        .build()))
                .build());

        hearingAggregate.apply(new CustodyTimeLimitExtended(hearingId, offence1Id, extendedCTLDate));

        final List<Offence> offences = hearingAggregateMomento.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences();
        assertThat(offences.get(0).getCustodyTimeLimit(), notNullValue());
        assertThat(offences.get(0).getCustodyTimeLimit().getTimeLimit(), is(extendedCTLDate));
        assertThat(offences.get(0).getCustodyTimeLimit().getIsCtlExtended(), is(true));

    }

    @Test
    public void shouldUpdateCustodyTimeLimitWhenCTLDoesNotExistAlready() {

        final UUID hearingId = randomUUID();
        final UUID offence1Id = randomUUID();
        final UUID offence2Id = randomUUID();
        final LocalDate extendedCTLDate = LocalDate.now().plusDays(50);

        hearingAggregateMomento.setHearing(Hearing.hearing()
                .withProsecutionCases(asList(ProsecutionCase.prosecutionCase()
                        .withDefendants(asList(Defendant.defendant()
                                .withOffences(asList(Offence.offence()
                                                .withId(offence1Id)
                                                .build(),
                                        Offence.offence()
                                                .withId(offence2Id)
                                                .build()))
                                .build()))
                        .build()))
                .build());

        hearingAggregate.apply(new CustodyTimeLimitExtended(hearingId, offence1Id, extendedCTLDate));

        final List<Offence> offences = hearingAggregateMomento.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences();
        assertThat(offences.get(0).getCustodyTimeLimit(), notNullValue());
        assertThat(offences.get(0).getCustodyTimeLimit().getTimeLimit(), is(extendedCTLDate));
        assertThat(offences.get(0).getCustodyTimeLimit().getIsCtlExtended(), is(true));

    }

    @Test
    public void shouldNotAddDuplicateOffenceIfOffenceExistAlreadyForDefendant() {

        final UUID hearingId = randomUUID();
        final UUID prosecutionCaseId = randomUUID();
        final UUID defendantId = randomUUID();
        final UUID offenceId = randomUUID();

        hearingAggregateMomento.setHearing(Hearing.hearing()
                .withProsecutionCases(asList(ProsecutionCase.prosecutionCase()
                        .withDefendants(asList(Defendant.defendant()
                                .withId(defendantId)
                                .withOffences(new ArrayList<>(Arrays.asList(Offence.offence()
                                                .withId(offenceId)
                                                .build())))
                                .build()))
                        .build()))
                .build());

        final Offence offence = Offence.offence()
                .withId(offenceId)
                .build();


        hearingAggregate.apply(OffenceAdded.offenceAdded()
                .withHearingId(hearingId)
                .withDefendantId(defendantId)
                .withProsecutionCaseId(prosecutionCaseId)
                .withOffence(offence));

        final List<Offence> offences = hearingAggregateMomento.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences();
        assertThat(offences.size(), is(1));

    }

    @Test
    public void shouldNotAddDuplicateOffenceIfOffenceExistAlreadyForDefendantV2() {

        final UUID hearingId = randomUUID();
        final UUID prosecutionCaseId = randomUUID();
        final UUID defendantId = randomUUID();
        final UUID offenceId = randomUUID();

        hearingAggregateMomento.setHearing(Hearing.hearing()
                .withProsecutionCases(asList(ProsecutionCase.prosecutionCase()
                        .withDefendants(asList(Defendant.defendant()
                                .withId(defendantId)
                                .withOffences(new ArrayList<>(Arrays.asList(Offence.offence()
                                        .withId(offenceId)
                                        .build())))
                                .build()))
                        .build()))
                .build());

        final Offence offence = Offence.offence()
                .withId(offenceId)
                .build();


        List events = hearingAggregate.addOffenceV2(hearingId,defendantId,prosecutionCaseId, singletonList(offence)).toList();

        assertThat(events.isEmpty(), is(true));

    }

    @Test
    public void shouldAddOffenceToDefendantIfTheOffenceIsNotDuplicate() {

        final UUID hearingId = randomUUID();
        final UUID prosecutionCaseId = randomUUID();
        final UUID defendantId = randomUUID();
        final UUID offence1Id = randomUUID();
        final UUID offence2Id = randomUUID();

        hearingAggregateMomento.setHearing(Hearing.hearing()
                .withProsecutionCases(asList(ProsecutionCase.prosecutionCase()
                        .withDefendants(asList(Defendant.defendant()
                                .withId(defendantId)
                                .withOffences(new ArrayList<>(Arrays.asList(Offence.offence()
                                        .withId(offence1Id)
                                        .build())))
                                .build()))
                        .build()))
                .build());

        final Offence offence = Offence.offence()
                .withId(offence2Id)
                .build();


        hearingAggregate.apply(OffenceAdded.offenceAdded()
                .withHearingId(hearingId)
                .withDefendantId(defendantId)
                .withProsecutionCaseId(prosecutionCaseId)
                .withOffence(offence));

        final List<Offence> offences = hearingAggregateMomento.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences();
        assertThat(offences.size(), is(2));

    }

    @Test
    public void shouldAddOffenceToDefendantIfTheOffenceIsNotDuplicateV2() {

        final UUID hearingId = randomUUID();
        final UUID prosecutionCaseId = randomUUID();
        final UUID defendantId = randomUUID();
        final UUID offence1Id = randomUUID();
        final UUID offence2Id = randomUUID();

        hearingAggregateMomento.setHearing(Hearing.hearing()
                .withProsecutionCases(asList(ProsecutionCase.prosecutionCase()
                        .withDefendants(asList(Defendant.defendant()
                                .withId(defendantId)
                                .withOffences(new ArrayList<>(Arrays.asList(Offence.offence()
                                        .withId(offence1Id)
                                        .build())))
                                .build()))
                        .build()))
                .build());

        final Offence offence = Offence.offence()
                .withId(offence2Id)
                .build();


        hearingAggregate.apply(OffenceAddedV2.offenceAddedV2()
                .withHearingId(hearingId)
                .withDefendantId(defendantId)
                .withProsecutionCaseId(prosecutionCaseId)
                .withOffence(singletonList(offence)));

        final List<Offence> offences = hearingAggregateMomento.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences();
        assertThat(offences.size(), is(2));

    }


    @Test
    void shouldAddOffencePleasToMomento() {
        final UUID hearingId = randomUUID();
        final UUID caseId1 = randomUUID();
        final UUID caseId2 = randomUUID();
        final UUID defendantId = randomUUID();
        final UUID offenceId = randomUUID();

        hearingAggregate.apply(new HearingInitiated(Hearing.hearing()
                .withId(hearingId)
                .withProsecutionCases(new ArrayList<>(singletonList(ProsecutionCase.prosecutionCase()
                        .withId(caseId1)
                        .withDefendants(new ArrayList<>(singletonList(Defendant.defendant()
                                .withId(defendantId)
                                .withOffences(new ArrayList<>(singletonList(Offence.offence()
                                        .withId(offenceId)
                                        .build())))
                                .build())))
                        .build())))
                .build()));

        final List<ProsecutionCase> prosecutionCases = singletonList(ProsecutionCase.prosecutionCase()
                        .withId(caseId2)
                        .withDefendants(singletonList(
                                Defendant.defendant()
                                        .withId(defendantId)
                                        .withOffences(singletonList(Offence.offence()
                                                .withId(offenceId)
                                                .withPlea(Plea.plea().withOffenceId(offenceId).build())
                                                .withIndicatedPlea(IndicatedPlea.indicatedPlea().withOffenceId(offenceId).build())
                                                .withAllocationDecision(AllocationDecision.allocationDecision().withOffenceId(offenceId).build())
                                                .withVerdict(Verdict.verdict().withOffenceId(offenceId).build())
                                                .build()))
                                        .build()))
                        .build());
        hearingAggregate.apply(new ExistingHearingUpdated(hearingId, prosecutionCases, Collections.emptyList()));

        assertThat(hearingAggregateMomento.getPleas().size(), is(1));
        assertThat(hearingAggregateMomento.getIndicatedPlea().size(), is(1));
        assertThat(hearingAggregateMomento.getVerdicts().size(), is(1));
        assertThat(hearingAggregateMomento.getAllocationDecision().size(), is(1));
        assertThat(hearingAggregateMomento.getPleas().get(offenceId).getOffenceId(), is(offenceId));
        assertThat(hearingAggregateMomento.getIndicatedPlea().get(offenceId).getOffenceId(), is(offenceId));
        assertThat(hearingAggregateMomento.getVerdicts().get(offenceId).getOffenceId(), is(offenceId));
        assertThat(hearingAggregateMomento.getAllocationDecision().get(offenceId).getOffenceId(), is(offenceId));
    }

    public void shouldUpdateOffenceIfOffenceExistAlreadyForDefendantV2() {

        final UUID hearingId = randomUUID();
        final UUID defendantId = randomUUID();
        final UUID offenceId = randomUUID();

        hearingAggregateMomento.setHearing(Hearing.hearing()
                .withProsecutionCases(asList(ProsecutionCase.prosecutionCase()
                        .withDefendants(asList(Defendant.defendant()
                                .withId(defendantId)
                                .withOffences(new ArrayList<>(Arrays.asList(Offence.offence()
                                        .withId(offenceId)
                                        .build())))
                                .build()))
                        .build()))
                .build());

        final Offence offence = Offence.offence()
                .withId(offenceId)
                .withCount(3)
                .build();


        List events = hearingAggregate.updateOffenceV2(hearingId,defendantId, singletonList(offence)).toList();

        assertThat(events.isEmpty(), is(false));
        assertThat(hearingAggregate.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getCount(), is(3));

    }
}
