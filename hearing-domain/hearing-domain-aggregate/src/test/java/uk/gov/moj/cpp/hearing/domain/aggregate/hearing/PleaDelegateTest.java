package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static uk.gov.justice.core.courts.Defendant.defendant;
import static uk.gov.justice.core.courts.Hearing.hearing;
import static uk.gov.justice.core.courts.IndicatedPleaValue.INDICATED_GUILTY;
import static uk.gov.justice.core.courts.IndicatedPleaValue.INDICATED_NOT_GUILTY;
import static uk.gov.justice.core.courts.ProsecutionCase.prosecutionCase;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.moj.cpp.hearing.domain.aggregate.util.PleaTypeUtil.ALL_PLEAS;
import static uk.gov.moj.cpp.hearing.domain.aggregate.util.PleaTypeUtil.GUILTY_PLEA_LIST;
import static uk.gov.moj.cpp.hearing.domain.aggregate.util.PleaTypeUtil.guiltyPleaTypes;

import uk.gov.justice.core.courts.AllocationDecision;
import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationCase;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.IndicatedPlea;
import uk.gov.justice.core.courts.IndicatedPleaValue;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.Plea;
import uk.gov.justice.core.courts.PleaModel;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.ProsecutionCaseIdentifier;
import uk.gov.justice.core.courts.Verdict;
import uk.gov.justice.core.courts.VerdictType;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateAdded;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateRemoved;
import uk.gov.moj.cpp.hearing.domain.event.PleaUpsert;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

public class PleaDelegateTest {
    private static final String NOT_GUILTY = "NOT_GUILTY";
    private static final String GUILTY = "GUILTY";

    private PleaDelegate pleaDelegate;
    private HearingAggregateMomento hearingAggregateMomento;

    public static final UUID OFFENCE_ID = randomUUID();
    public static final UUID HEARING_ID = randomUUID();
    public static final UUID CASE_ID = randomUUID();
    public static final UUID DEFENDANT_ID = randomUUID();
    private static final UUID APPLICATION_ID = randomUUID();
    public static final LocalDate PLEA_DATE = PAST_LOCAL_DATE.next();

    @Before
    public void setup() {
        hearingAggregateMomento = new HearingAggregateMomento();
        pleaDelegate  = new PleaDelegate(hearingAggregateMomento);
    }

    @Test
    public void shouldSetPleaIntoHearingAggregateMomento() {
        final Hearing hearing = getHearing(OFFENCE_ID, DEFENDANT_ID, CASE_ID, HEARING_ID);

        this.hearingAggregateMomento.setHearing(hearing);
        final String pleaValue = NOT_GUILTY;
        final LocalDate pleaDate = PAST_LOCAL_DATE.next();

        final PleaUpsert pleaUpsert = PleaUpsert.pleaUpsert()
                .setHearingId(HEARING_ID)
                .setPleaModel(PleaModel.pleaModel()
                        .withProsecutionCaseId(CASE_ID)
                        .withDefendantId(DEFENDANT_ID)
                        .withOffenceId(OFFENCE_ID)
                        .withPlea(Plea.plea()
                                .withOffenceId(OFFENCE_ID)
                                .withPleaDate(pleaDate)
                                .withPleaValue(pleaValue)
                                .build())
                        .build());
        pleaDelegate.handlePleaUpsert(pleaUpsert);

        assertThat(hearingAggregateMomento.getPleas().size(), is(1));
        assertThat(hearingAggregateMomento.getIndicatedPlea().size(), is(0));
        assertThat(hearingAggregateMomento.getAllocationDecision().size(), is(0));

        final Plea plea = hearingAggregateMomento.getPleas().get(OFFENCE_ID);
        assertThat(plea.getPleaValue(), is(pleaValue));
        assertThat(plea.getPleaDate(), is(pleaDate));
        assertThat(plea.getOffenceId(), is(OFFENCE_ID));
    }

    @Test
    public void shouldSetIndicatedPleaIntoHearingAggregateMomento() {

        final Hearing hearing = getHearing(OFFENCE_ID, DEFENDANT_ID, CASE_ID, HEARING_ID);

        this.hearingAggregateMomento.setHearing(hearing);
        final IndicatedPleaValue indicatedPleaValue = INDICATED_GUILTY;
        final LocalDate indicatedPleaDate = PAST_LOCAL_DATE.next();

        final PleaUpsert pleaUpsert = PleaUpsert.pleaUpsert()
                .setHearingId(HEARING_ID)
                .setPleaModel(PleaModel.pleaModel()
                        .withProsecutionCaseId(randomUUID())
                        .withDefendantId(randomUUID())
                        .withOffenceId(OFFENCE_ID)
                        .withIndicatedPlea(IndicatedPlea.indicatedPlea()
                                .withOffenceId(OFFENCE_ID)
                                .withIndicatedPleaDate(indicatedPleaDate)
                                .withIndicatedPleaValue(indicatedPleaValue)
                                .build())
                        .build());
        pleaDelegate.handlePleaUpsert(pleaUpsert);

        assertThat(hearingAggregateMomento.getIndicatedPlea().size(), is(1));
        assertThat(hearingAggregateMomento.getPleas().size(), is(0));
        assertThat(hearingAggregateMomento.getAllocationDecision().size(), is(0));

        final IndicatedPlea indicatedPlea = hearingAggregateMomento.getIndicatedPlea().get(OFFENCE_ID);
        assertThat(indicatedPlea.getIndicatedPleaValue(), is(indicatedPleaValue));
        assertThat(indicatedPlea.getIndicatedPleaDate(), is(indicatedPleaDate));
        assertThat(indicatedPlea.getOffenceId(), is(OFFENCE_ID));
    }

    @Test
    public void shouldSetAllocationDecisionIntoHearingAggregateMomento() {
        final Hearing hearing = getHearing(OFFENCE_ID, DEFENDANT_ID, CASE_ID, HEARING_ID);

        this.hearingAggregateMomento.setHearing(hearing);
        final PleaUpsert pleaUpsert = PleaUpsert.pleaUpsert()
                .setHearingId(HEARING_ID)
                .setPleaModel(getPlea(GUILTY, PLEA_DATE));
        pleaDelegate.handlePleaUpsert(pleaUpsert);

        assertThat(hearingAggregateMomento.getIndicatedPlea().size(), is(0));
        assertThat(hearingAggregateMomento.getPleas().size(), is(1));
        assertThat(hearingAggregateMomento.getAllocationDecision().size(), is(1));

        final AllocationDecision allocationDecision = hearingAggregateMomento.getAllocationDecision().get(OFFENCE_ID);
        assertThat(allocationDecision.getOffenceId(), is(OFFENCE_ID));
    }

    @Test
    public void shouldSetAllValuesIntoHearingAggregateMomento() {

        final Hearing hearing = getHearing(OFFENCE_ID, DEFENDANT_ID, CASE_ID, HEARING_ID);

        this.hearingAggregateMomento.setHearing(hearing);

        final String pleaValue = NOT_GUILTY;
        final IndicatedPleaValue indicatedPleaValue = INDICATED_GUILTY;
        final LocalDate indicatedPleaDate = PAST_LOCAL_DATE.next();

        final PleaUpsert pleaUpsert = PleaUpsert.pleaUpsert()
                .setHearingId(HEARING_ID)
                .setPleaModel(PleaModel.pleaModel()
                        .withProsecutionCaseId(CASE_ID)
                        .withDefendantId(DEFENDANT_ID)
                        .withOffenceId(OFFENCE_ID)
                        .withPlea(Plea.plea()
                                .withOffenceId(OFFENCE_ID)
                                .withPleaDate(PLEA_DATE)
                                .withPleaValue(pleaValue)
                                .build())
                        .withIndicatedPlea(IndicatedPlea.indicatedPlea()
                                .withOffenceId(OFFENCE_ID)
                                .withIndicatedPleaDate(indicatedPleaDate)
                                .withIndicatedPleaValue(indicatedPleaValue)
                                .build())
                        .withAllocationDecision(AllocationDecision.allocationDecision()
                                .withOffenceId(OFFENCE_ID)
                                .build())
                        .build());
        pleaDelegate.handlePleaUpsert(pleaUpsert);

        assertThat(hearingAggregateMomento.getIndicatedPlea().size(), is(1));
        assertThat(hearingAggregateMomento.getPleas().size(), is(1));
        assertThat(hearingAggregateMomento.getAllocationDecision().size(), is(1));

        final Plea plea = hearingAggregateMomento.getPleas().get(OFFENCE_ID);
        assertThat(plea.getPleaValue(), is(pleaValue));
        assertThat(plea.getPleaDate(), is(PLEA_DATE));
        assertThat(plea.getOffenceId(), is(OFFENCE_ID));

        final IndicatedPlea indicatedPlea = hearingAggregateMomento.getIndicatedPlea().get(OFFENCE_ID);
        assertThat(indicatedPlea.getIndicatedPleaValue(), is(indicatedPleaValue));
        assertThat(indicatedPlea.getIndicatedPleaDate(), is(indicatedPleaDate));
        assertThat(indicatedPlea.getOffenceId(), is(OFFENCE_ID));

        final AllocationDecision allocationDecision = hearingAggregateMomento.getAllocationDecision().get(OFFENCE_ID);
        assertThat(allocationDecision.getOffenceId(), is(OFFENCE_ID));
    }

    @Test
    public void shouldAddConvictionDateAddedEventWhenIndicatedPleaIsGuilty() {

        final LocalDate indicatedPleaDate = PAST_LOCAL_DATE.next();

        final Hearing hearing = getHearing(OFFENCE_ID, DEFENDANT_ID, CASE_ID, HEARING_ID);

        this.hearingAggregateMomento.setHearing(hearing);

        final PleaModel pleaModel = PleaModel.pleaModel()
                .withProsecutionCaseId(CASE_ID)
                .withDefendantId(DEFENDANT_ID)
                .withOffenceId(OFFENCE_ID)
                .withIndicatedPlea(IndicatedPlea.indicatedPlea()
                        .withOffenceId(OFFENCE_ID)
                        .withIndicatedPleaDate(indicatedPleaDate)
                        .withIndicatedPleaValue(INDICATED_GUILTY)
                        .build())
                .build();

        final List<Object> events = pleaDelegate.updatePlea(HEARING_ID, pleaModel, guiltyPleaTypes()).collect(Collectors.toList());

        final PleaUpsert pleaUpsert = (PleaUpsert) events.get(0);
        assertThat(pleaUpsert, is(notNullValue()));
        assertThat(pleaUpsert.getHearingId(), is(HEARING_ID));

        final ConvictionDateAdded convictionDateAdded = (ConvictionDateAdded) events.get(1);
        assertThat(convictionDateAdded, is(notNullValue()));
        assertThat(convictionDateAdded.getOffenceId(), is(OFFENCE_ID));
        assertThat(convictionDateAdded.getConvictionDate(), is(indicatedPleaDate));
        assertThat(convictionDateAdded.getHearingId(), is(HEARING_ID));
        assertThat(convictionDateAdded.getCaseId(), is(CASE_ID));
    }

    @Test
    public void shouldAddConvictionDateAddedEventWhenOffenceUnderCourtApplicationPleaIsGuilty() {

        final LocalDate indicatedPleaDate = PAST_LOCAL_DATE.next();

        final Hearing hearing = getHearing(OFFENCE_ID, APPLICATION_ID, HEARING_ID);

        this.hearingAggregateMomento.setHearing(hearing);

        final PleaModel pleaModel = PleaModel.pleaModel()
                .withOffenceId(OFFENCE_ID)
                .withPlea(Plea.plea()
                        .withOffenceId(OFFENCE_ID)
                        .withPleaDate(indicatedPleaDate)
                        .withPleaValue(GUILTY)
                        .build())
                .build();

        final List<Object> events = pleaDelegate.updatePlea(HEARING_ID, pleaModel, guiltyPleaTypes()).collect(Collectors.toList());

        final PleaUpsert pleaUpsert = (PleaUpsert) events.get(0);
        assertThat(pleaUpsert, is(notNullValue()));
        assertThat(pleaUpsert.getHearingId(), is(HEARING_ID));

        final ConvictionDateAdded convictionDateAdded = (ConvictionDateAdded) events.get(1);
        assertThat(convictionDateAdded, is(notNullValue()));
        assertThat(convictionDateAdded.getOffenceId(), is(OFFENCE_ID));
        assertThat(convictionDateAdded.getConvictionDate(), is(indicatedPleaDate));
        assertThat(convictionDateAdded.getHearingId(), is(HEARING_ID));
        assertThat(convictionDateAdded.getCourtApplicationId(), is(APPLICATION_ID));
    }

    @Test
    public void shouldAddConvictionDateAddedEventWhenCourtApplicationPleaIsGuilty() {

        final LocalDate indicatedPleaDate = PAST_LOCAL_DATE.next();

        final Hearing hearing = getHearing(OFFENCE_ID, APPLICATION_ID, HEARING_ID);

        this.hearingAggregateMomento.setHearing(hearing);

        final PleaModel pleaModel = PleaModel.pleaModel()
                .withApplicationId(APPLICATION_ID)
                .withPlea(Plea.plea()
                        .withPleaDate(indicatedPleaDate)
                        .withPleaValue(GUILTY)
                        .build())
                .build();

        final List<Object> events = pleaDelegate.updatePlea(HEARING_ID, pleaModel, guiltyPleaTypes()).collect(Collectors.toList());

        final PleaUpsert pleaUpsert = (PleaUpsert) events.get(0);
        assertThat(pleaUpsert, is(notNullValue()));
        assertThat(pleaUpsert.getHearingId(), is(HEARING_ID));

        final ConvictionDateAdded convictionDateAdded = (ConvictionDateAdded) events.get(1);
        assertThat(convictionDateAdded, is(notNullValue()));
        assertThat(convictionDateAdded.getOffenceId(), is(nullValue()));
        assertThat(convictionDateAdded.getConvictionDate(), is(indicatedPleaDate));
        assertThat(convictionDateAdded.getHearingId(), is(HEARING_ID));
        assertThat(convictionDateAdded.getCourtApplicationId(), is(APPLICATION_ID));
    }

    @Test
    public void shouldAddConvictionDateRemovedEventWhenIndicatedPleaIsNotGuilty() {

        final LocalDate indicatedPleaDate = PAST_LOCAL_DATE.next();

        final Hearing hearing = getHearing(OFFENCE_ID, DEFENDANT_ID, CASE_ID, HEARING_ID);

        this.hearingAggregateMomento.setHearing(hearing);

        final PleaModel pleaModel = PleaModel.pleaModel()
                .withProsecutionCaseId(CASE_ID)
                .withDefendantId(DEFENDANT_ID)
                .withOffenceId(OFFENCE_ID)
                .withIndicatedPlea(IndicatedPlea.indicatedPlea()
                        .withOffenceId(OFFENCE_ID)
                        .withIndicatedPleaDate(indicatedPleaDate)
                        .withIndicatedPleaValue(INDICATED_NOT_GUILTY)
                        .build())
                .withAllocationDecision(AllocationDecision.allocationDecision()
                        .withOffenceId(OFFENCE_ID)
                        .build())
                .build();

        final List<Object> events = pleaDelegate.updatePlea(HEARING_ID, pleaModel, guiltyPleaTypes()).collect(Collectors.toList());

        final PleaUpsert pleaUpsert = (PleaUpsert) events.get(0);
        assertThat(pleaUpsert, is(notNullValue()));
        assertThat(pleaUpsert.getHearingId(), is(HEARING_ID));

        final ConvictionDateRemoved convictionDateRemoved = (ConvictionDateRemoved) events.get(1);
        assertThat(convictionDateRemoved, is(notNullValue()));
        assertThat(convictionDateRemoved.getOffenceId(), is(OFFENCE_ID));
        assertThat(convictionDateRemoved.getHearingId(), is(HEARING_ID));
        assertThat(convictionDateRemoved.getCaseId(), is(CASE_ID));
    }

    @Test
    public void shouldAddConvictionDateWhenPleaIsGuiltyType() {
        GUILTY_PLEA_LIST.forEach(this::shouldAddConvictionDateAddedWhenPleaIsGuiltyType);
    }

    @Test
    public void shouldAddConvictionDateWhenPleaIsGuiltyType_AdjournedHearingWithNotGuiltyPleaSet() {
        GUILTY_PLEA_LIST.forEach(gpv -> {
                    final Hearing hearing = getHearing(OFFENCE_ID, DEFENDANT_ID, CASE_ID, HEARING_ID);
                    final Offence firstOffenceForFirstDefendantForFirstCase = hearing.getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0);
                    final UUID idOfFirstOffenceForFirstDefendantForFirstCase = firstOffenceForFirstDefendantForFirstCase.getId();
                    firstOffenceForFirstDefendantForFirstCase.setPlea(Plea.plea().withPleaValue(NOT_GUILTY).withOffenceId(idOfFirstOffenceForFirstDefendantForFirstCase).build());
                    firstOffenceForFirstDefendantForFirstCase.setVerdict(Verdict.verdict().withVerdictType(VerdictType.verdictType().withCategoryType("NOT GUILTY").build()).withOffenceId(idOfFirstOffenceForFirstDefendantForFirstCase).build());
                    shouldAddConvictionDateAddedWhenPleaIsGuiltyType(hearing, gpv);
                }
        );
    }

    @Test
    public void shouldRemoveConvictionDateWhenPleaChangedToNotGuilty_ForAdjournedHearingWithInitialGuiltyPlea() {
        for (String pleaValue : ALL_PLEAS) {
            if (!GUILTY_PLEA_LIST.contains(pleaValue)) {
                final LocalDate convictionDateForFirstOffence = PAST_LOCAL_DATE.next();
                final Hearing hearing = getHearing(OFFENCE_ID, DEFENDANT_ID, CASE_ID, HEARING_ID);
                final Offence firstOffenceForFirstDefendantForFirstCase = hearing.getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0);
                final UUID idOfFirstOffenceForFirstDefendantForFirstCase = firstOffenceForFirstDefendantForFirstCase.getId();
                firstOffenceForFirstDefendantForFirstCase.setPlea(Plea.plea().withPleaValue(GUILTY).withOffenceId(idOfFirstOffenceForFirstDefendantForFirstCase).build());
                firstOffenceForFirstDefendantForFirstCase.setVerdict(Verdict.verdict().withVerdictType(VerdictType.verdictType().withCategoryType("NOT GUILTY").build()).withOffenceId(idOfFirstOffenceForFirstDefendantForFirstCase).build());
                firstOffenceForFirstDefendantForFirstCase.setConvictionDate(convictionDateForFirstOffence);

                final HearingAggregate hearingAggregate = new HearingAggregate();
                final List<Object> events = hearingAggregate.initiate(hearing).collect(Collectors.toList());
                events.forEach(hearingAggregate::apply);

                final PleaModel pleaModel = getPlea(NOT_GUILTY, PLEA_DATE);
                final List<Object> eventsAfterGuiltyPleaUpdate = hearingAggregate.updatePlea(HEARING_ID, pleaModel, guiltyPleaTypes()).collect(Collectors.toList());

                final PleaUpsert pleaUpsert = (PleaUpsert) eventsAfterGuiltyPleaUpdate.get(0);
                assertThat(pleaUpsert, is(notNullValue()));
                assertTrue(eventsAfterGuiltyPleaUpdate.get(1) instanceof ConvictionDateRemoved);
            }
        }

    }

    @Test
    public void shouldRemoveConvictionDateWhenPleaChangedFromGuiltyToNotGuilty() {
        for (String pleaValue : ALL_PLEAS) {
            if (!GUILTY_PLEA_LIST.contains(pleaValue)) {
                shouldRemoveConvictionDateWhenPleaChangedFromGuiltyToNotGuilty(pleaValue);
            }
        }
    }

    private void shouldAddConvictionDateAddedWhenPleaIsGuiltyType(String guiltyPleaValue) {
        final Hearing hearing = getHearing(OFFENCE_ID, DEFENDANT_ID, CASE_ID, HEARING_ID);
        shouldAddConvictionDateAddedWhenPleaIsGuiltyType(hearing, guiltyPleaValue);
    }

    private void shouldAddConvictionDateAddedWhenPleaIsGuiltyType(final Hearing hearing, final String guiltyPleaValue) {

        final HearingAggregate hearingAggregate = new HearingAggregate();
        final List<Object> events = hearingAggregate.initiate(hearing).collect(Collectors.toList());
        events.forEach(hearingAggregate::apply);

        final PleaModel pleaModel = getPlea(guiltyPleaValue, PLEA_DATE);
        List<Object> eventsAfterPleaUpdate = hearingAggregate.updatePlea(HEARING_ID, pleaModel, guiltyPleaTypes()).collect(Collectors.toList());

        final PleaUpsert pleaUpsert = (PleaUpsert) eventsAfterPleaUpdate.get(0);
        assertThat(pleaUpsert, is(notNullValue()));
        assertThat(pleaUpsert.getHearingId(), is(HEARING_ID));

        final ConvictionDateAdded convictionDateAdded = (ConvictionDateAdded) eventsAfterPleaUpdate.get(1);
        assertThat(convictionDateAdded, is(notNullValue()));
        assertThat(convictionDateAdded.getOffenceId(), is(OFFENCE_ID));
        assertThat(convictionDateAdded.getHearingId(), is(HEARING_ID));
        assertThat(convictionDateAdded.getCaseId(), is(CASE_ID));
        assertThat(convictionDateAdded.getConvictionDate(), is(PLEA_DATE));
    }

    private void shouldRemoveConvictionDateWhenPleaChangedFromGuiltyToNotGuilty(final String notGuiltyPleaValue) {
        final Hearing hearing = getHearing(OFFENCE_ID, DEFENDANT_ID, CASE_ID, HEARING_ID);
        final HearingAggregate hearingAggregate = new HearingAggregate();
        final List<Object> events = hearingAggregate.initiate(hearing).collect(Collectors.toList());
        events.forEach(hearingAggregate::apply);

        // this indicates a guilty plea was set previously
        final PleaModel guiltyPleaModel = getPlea(GUILTY, PLEA_DATE);
        final List<Object> eventsAfterSettingGuiltyPlea = hearingAggregate.updatePlea(HEARING_ID, guiltyPleaModel, guiltyPleaTypes()).collect(Collectors.toList());
        eventsAfterSettingGuiltyPlea.forEach(hearingAggregate::apply);

        // now set plea to not guilty
        final PleaModel notGuiltyPleaModel = getPlea(notGuiltyPleaValue, PLEA_DATE);
        final List<Object> eventsAfterSettingToNotGuilty = hearingAggregate.updatePlea(HEARING_ID, notGuiltyPleaModel, guiltyPleaTypes()).collect(Collectors.toList());

        final PleaUpsert pleaUpsert = (PleaUpsert) eventsAfterSettingToNotGuilty.get(0);
        assertThat(pleaUpsert, is(notNullValue()));
        assertTrue(eventsAfterSettingToNotGuilty.get(1) instanceof ConvictionDateRemoved);
    }

    private PleaModel getPlea(final String pleaValue, final LocalDate pleaDate) {
        return PleaModel.pleaModel()
                .withProsecutionCaseId(CASE_ID)
                .withDefendantId(DEFENDANT_ID)
                .withOffenceId(OFFENCE_ID)
                .withAllocationDecision(AllocationDecision.allocationDecision()
                        .withOffenceId(OFFENCE_ID)
                        .build())
                .withPlea(Plea.plea()
                        .withPleaDate(pleaDate)
                        .withPleaValue(pleaValue)
                        .build()
                ).build();
    }

    private Hearing getHearing(final UUID offenceId, final UUID defendantId, final UUID prosecutionCaseId, final UUID hearingId) {
        final Defendant defendant = defendant()
                .withId(defendantId)
                .withOffences(asList(Offence.offence().withId(offenceId).build()))
                .build();

        final ProsecutionCase prosecutionCase = prosecutionCase()
                .withId(prosecutionCaseId)
                .withDefendants(asList(defendant))
                .build();

        return hearing()
                .withId(hearingId)
                .withProsecutionCases(asList(prosecutionCase))
                .build();
    }

    private Hearing getHearing(final UUID offenceId, final UUID courtApplicationId,final UUID hearingId) {

        return hearing()
                .withId(hearingId)
                .withCourtApplications(singletonList(CourtApplication.courtApplication()
                        .withId(courtApplicationId)
                        .withCourtApplicationCases(Collections.singletonList(CourtApplicationCase.courtApplicationCase()
                                .withProsecutionCaseId(randomUUID())
                                .withIsSJP(false)
                                .withCaseStatus("ACTIVE")
                                .withProsecutionCaseIdentifier(ProsecutionCaseIdentifier.prosecutionCaseIdentifier()
                                        .withCaseURN("caseURN")
                                        .withProsecutionAuthorityId(randomUUID())
                                        .withProsecutionAuthorityCode("ABC")
                                        .build())
                                .withOffences(Collections.singletonList(uk.gov.justice.core.courts.Offence.offence()
                                        .withOffenceDefinitionId(randomUUID())
                                        .withOffenceCode("ABC")
                                        .withOffenceTitle("ABC")
                                        .withWording("ABC")
                                        .withStartDate(LocalDate.now())
                                        .withId(offenceId).build()))
                                .build()))
                        .build()))
                .build();
    }
}