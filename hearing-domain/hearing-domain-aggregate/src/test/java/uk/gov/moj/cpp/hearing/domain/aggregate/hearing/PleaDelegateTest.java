package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static uk.gov.justice.core.courts.Defendant.defendant;
import static uk.gov.justice.core.courts.Hearing.hearing;
import static uk.gov.justice.core.courts.IndicatedPleaValue.INDICATED_GUILTY;
import static uk.gov.justice.core.courts.IndicatedPleaValue.INDICATED_NOT_GUILTY;
import static uk.gov.justice.core.courts.ProsecutionCase.prosecutionCase;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;

import uk.gov.justice.core.courts.AllocationDecision;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.IndicatedPlea;
import uk.gov.justice.core.courts.IndicatedPleaValue;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.Plea;
import uk.gov.justice.core.courts.PleaModel;
import uk.gov.justice.core.courts.PleaValue;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateAdded;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateRemoved;
import uk.gov.moj.cpp.hearing.domain.event.PleaUpsert;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;

public class PleaDelegateTest {

    private PleaDelegate pleaDelegate;
    private HearingAggregateMomento hearingAggregateMomento;

    @Before
    public void setup() {
        hearingAggregateMomento = new HearingAggregateMomento();
        pleaDelegate  = new PleaDelegate(hearingAggregateMomento);
    }

    @Test
    public void shouldSetPleaIntoHearingAggregateMomento() {

        final UUID offenceId = randomUUID();
        final PleaValue pleaValue = PleaValue.NOT_GUILTY;
        final LocalDate pleaDate = PAST_LOCAL_DATE.next();

        final PleaUpsert pleaUpsert = PleaUpsert.pleaUpsert()
                .setHearingId(randomUUID())
                .setPleaModel(PleaModel.pleaModel()
                        .withProsecutionCaseId(randomUUID())
                        .withDefendantId(randomUUID())
                        .withOffenceId(offenceId)
                        .withPlea(Plea.plea()
                                .withOffenceId(offenceId)
                                .withPleaDate(pleaDate)
                                .withPleaValue(pleaValue)
                                .build())
                        .build());
        pleaDelegate.handlePleaUpsert(pleaUpsert);

        assertThat(hearingAggregateMomento.getPleas().size(), is(1));
        assertThat(hearingAggregateMomento.getIndicatedPlea().size(), is(0));
        assertThat(hearingAggregateMomento.getAllocationDecision().size(), is(0));

        final Plea plea = hearingAggregateMomento.getPleas().get(offenceId);
        assertThat(plea.getPleaValue(), is(pleaValue));
        assertThat(plea.getPleaDate(), is(pleaDate));
        assertThat(plea.getOffenceId(), is(offenceId));
    }

    @Test
    public void shouldSetIndicatedPleaIntoHearingAggregateMomento() {

        final UUID offenceId = randomUUID();
        final IndicatedPleaValue indicatedPleaValue = INDICATED_GUILTY;
        final LocalDate indicatedPleaDate = PAST_LOCAL_DATE.next();

        final PleaUpsert pleaUpsert = PleaUpsert.pleaUpsert()
                .setHearingId(randomUUID())
                .setPleaModel(PleaModel.pleaModel()
                        .withProsecutionCaseId(randomUUID())
                        .withDefendantId(randomUUID())
                        .withOffenceId(offenceId)
                        .withIndicatedPlea(IndicatedPlea.indicatedPlea()
                                .withOffenceId(offenceId)
                                .withIndicatedPleaDate(indicatedPleaDate)
                                .withIndicatedPleaValue(indicatedPleaValue)
                                .build())
                        .build());
        pleaDelegate.handlePleaUpsert(pleaUpsert);

        assertThat(hearingAggregateMomento.getIndicatedPlea().size(), is(1));
        assertThat(hearingAggregateMomento.getPleas().size(), is(0));
        assertThat(hearingAggregateMomento.getAllocationDecision().size(), is(0));

        final IndicatedPlea indicatedPlea = hearingAggregateMomento.getIndicatedPlea().get(offenceId);
        assertThat(indicatedPlea.getIndicatedPleaValue(), is(indicatedPleaValue));
        assertThat(indicatedPlea.getIndicatedPleaDate(), is(indicatedPleaDate));
        assertThat(indicatedPlea.getOffenceId(), is(offenceId));
    }

    @Test
    public void shouldSetAllocationDecisionIntoHearingAggregateMomento() {

        final UUID offenceId = randomUUID();

        final PleaUpsert pleaUpsert = PleaUpsert.pleaUpsert()
                .setHearingId(randomUUID())
                .setPleaModel(PleaModel.pleaModel()
                        .withProsecutionCaseId(randomUUID())
                        .withDefendantId(randomUUID())
                        .withOffenceId(offenceId)
                        .withAllocationDecision(AllocationDecision.allocationDecision()
                                .withOffenceId(offenceId)
                                .build())
                        .build());
        pleaDelegate.handlePleaUpsert(pleaUpsert);

        assertThat(hearingAggregateMomento.getIndicatedPlea().size(), is(0));
        assertThat(hearingAggregateMomento.getPleas().size(), is(0));
        assertThat(hearingAggregateMomento.getAllocationDecision().size(), is(1));

        final AllocationDecision allocationDecision = hearingAggregateMomento.getAllocationDecision().get(offenceId);
        assertThat(allocationDecision.getOffenceId(), is(offenceId));
    }

    @Test
    public void shouldSetAllValuesIntoHearingAggregateMomento() {

        final UUID offenceId = randomUUID();

        final PleaValue pleaValue = PleaValue.NOT_GUILTY;
        final LocalDate pleaDate = PAST_LOCAL_DATE.next();

        final IndicatedPleaValue indicatedPleaValue = INDICATED_GUILTY;
        final LocalDate indicatedPleaDate = PAST_LOCAL_DATE.next();

        final PleaUpsert pleaUpsert = PleaUpsert.pleaUpsert()
                .setHearingId(randomUUID())
                .setPleaModel(PleaModel.pleaModel()
                        .withProsecutionCaseId(randomUUID())
                        .withDefendantId(randomUUID())
                        .withOffenceId(offenceId)
                        .withPlea(Plea.plea()
                                .withOffenceId(offenceId)
                                .withPleaDate(pleaDate)
                                .withPleaValue(pleaValue)
                                .build())
                        .withIndicatedPlea(IndicatedPlea.indicatedPlea()
                                .withOffenceId(offenceId)
                                .withIndicatedPleaDate(indicatedPleaDate)
                                .withIndicatedPleaValue(indicatedPleaValue)
                                .build())
                        .withAllocationDecision(AllocationDecision.allocationDecision()
                                .withOffenceId(offenceId)
                                .build())
                        .build());
        pleaDelegate.handlePleaUpsert(pleaUpsert);

        assertThat(hearingAggregateMomento.getIndicatedPlea().size(), is(1));
        assertThat(hearingAggregateMomento.getPleas().size(), is(1));
        assertThat(hearingAggregateMomento.getAllocationDecision().size(), is(1));

        final Plea plea = hearingAggregateMomento.getPleas().get(offenceId);
        assertThat(plea.getPleaValue(), is(pleaValue));
        assertThat(plea.getPleaDate(), is(pleaDate));
        assertThat(plea.getOffenceId(), is(offenceId));

        final IndicatedPlea indicatedPlea = hearingAggregateMomento.getIndicatedPlea().get(offenceId);
        assertThat(indicatedPlea.getIndicatedPleaValue(), is(indicatedPleaValue));
        assertThat(indicatedPlea.getIndicatedPleaDate(), is(indicatedPleaDate));
        assertThat(indicatedPlea.getOffenceId(), is(offenceId));

        final AllocationDecision allocationDecision = hearingAggregateMomento.getAllocationDecision().get(offenceId);
        assertThat(allocationDecision.getOffenceId(), is(offenceId));
    }

    @Test
    public void shouldAddConvictionDateAddedEventWhenIndicatedPleaIsGuilty() {

        final UUID offenceId = randomUUID();
        final UUID hearingId = randomUUID();
        final UUID prosecutionCaseId = randomUUID();

        final IndicatedPleaValue indicatedPleaValue = INDICATED_GUILTY;
        final LocalDate indicatedPleaDate = PAST_LOCAL_DATE.next();

        final Hearing hearing = getHearing(offenceId, prosecutionCaseId, hearingId);

        this.hearingAggregateMomento.setHearing(hearing);

        final PleaModel pleaModel = PleaModel.pleaModel()
                .withProsecutionCaseId(prosecutionCaseId)
                .withDefendantId(offenceId)
                .withOffenceId(offenceId)
                .withIndicatedPlea(IndicatedPlea.indicatedPlea()
                        .withOffenceId(offenceId)
                        .withIndicatedPleaDate(indicatedPleaDate)
                        .withIndicatedPleaValue(indicatedPleaValue)
                        .build())
                .build();

        final Stream<Object> objectStream = pleaDelegate.updatePlea(hearingId, pleaModel);

        assertThat(objectStream, is(notNullValue()));

        final List<Object> events = new ArrayList<>();

        objectStream.forEach(os ->  events.add(os));

        final PleaUpsert pleaUpsert = (PleaUpsert) events.get(0);
        assertThat(pleaUpsert, is(notNullValue()));
        assertThat(pleaUpsert.getHearingId(), is(hearingId));

        final ConvictionDateAdded convictionDateAdded = (ConvictionDateAdded) events.get(1);
        assertThat(convictionDateAdded, is(notNullValue()));
        assertThat(convictionDateAdded.getOffenceId(), is(offenceId));
        assertThat(convictionDateAdded.getConvictionDate(), is(indicatedPleaDate));
        assertThat(convictionDateAdded.getHearingId(), is(hearingId));
        assertThat(convictionDateAdded.getCaseId(), is(prosecutionCaseId));
    }

    @Test
    public void shouldAddConvictionDateRemovedEventWhenIndicatedPleaIsNotGuilty() {

        final UUID offenceId = randomUUID();
        final UUID hearingId = randomUUID();
        final UUID prosecutionCaseId = randomUUID();

        final IndicatedPleaValue indicatedPleaValue = INDICATED_NOT_GUILTY;
        final LocalDate indicatedPleaDate = PAST_LOCAL_DATE.next();

        final Hearing hearing = getHearing(offenceId, prosecutionCaseId, hearingId);

        this.hearingAggregateMomento.setHearing(hearing);

        final PleaModel pleaModel = PleaModel.pleaModel()
                .withProsecutionCaseId(prosecutionCaseId)
                .withDefendantId(offenceId)
                .withOffenceId(offenceId)
                .withIndicatedPlea(IndicatedPlea.indicatedPlea()
                        .withOffenceId(offenceId)
                        .withIndicatedPleaDate(indicatedPleaDate)
                        .withIndicatedPleaValue(indicatedPleaValue)
                        .build())
                .withAllocationDecision(AllocationDecision.allocationDecision()
                        .withOffenceId(offenceId)
                        .build())
                .build();

        final Stream<Object> objectStream = pleaDelegate.updatePlea(hearingId, pleaModel);

        assertThat(objectStream, is(notNullValue()));

        final List<Object> events = new ArrayList<>();

        objectStream.forEach(os ->  events.add(os));

        final PleaUpsert pleaUpsert = (PleaUpsert) events.get(0);
        assertThat(pleaUpsert, is(notNullValue()));
        assertThat(pleaUpsert.getHearingId(), is(hearingId));

        final ConvictionDateRemoved convictionDateRemoved = (ConvictionDateRemoved) events.get(1);
        assertThat(convictionDateRemoved, is(notNullValue()));
        assertThat(convictionDateRemoved.getOffenceId(), is(offenceId));
        assertThat(convictionDateRemoved.getHearingId(), is(hearingId));
        assertThat(convictionDateRemoved.getCaseId(), is(prosecutionCaseId));
    }

    @Test
    public void shouldAddConvictionDateAddedEventWhenPleaGuilty() {

        final UUID offenceId = randomUUID();
        final UUID hearingId = randomUUID();
        final UUID prosecutionCaseId = randomUUID();
        final LocalDate pleaDate = PAST_LOCAL_DATE.next();
        final Hearing hearing = getHearing(offenceId, prosecutionCaseId, hearingId);
        this.hearingAggregateMomento.setHearing(hearing);

        final PleaModel pleaModel = PleaModel.pleaModel()
                .withProsecutionCaseId(prosecutionCaseId)
                .withDefendantId(offenceId)
                .withOffenceId(offenceId)
                .withAllocationDecision(AllocationDecision.allocationDecision()
                        .withOffenceId(offenceId)
                        .build())
                .withPlea(Plea.plea()
                        .withPleaDate(pleaDate)
                        .withPleaValue(PleaValue.GUILTY)
                        .build()
                ).build();

        final Stream<Object> objectStream = pleaDelegate.updatePlea(hearingId, pleaModel);

        assertThat(objectStream, is(notNullValue()));

        final List<Object> events = new ArrayList<>();

        objectStream.forEach(os -> events.add(os));

        final PleaUpsert pleaUpsert = (PleaUpsert) events.get(0);
        assertThat(pleaUpsert, is(notNullValue()));
        assertThat(pleaUpsert.getHearingId(), is(hearingId));

        final ConvictionDateAdded convictionDateAdded = (ConvictionDateAdded) events.get(1);
        assertThat(convictionDateAdded, is(notNullValue()));
        assertThat(convictionDateAdded.getOffenceId(), is(offenceId));
        assertThat(convictionDateAdded.getHearingId(), is(hearingId));
        assertThat(convictionDateAdded.getCaseId(), is(prosecutionCaseId));
        assertThat(convictionDateAdded.getConvictionDate(), is(pleaDate));
    }

    @Test
    public void shouldAddConvictionDateAddedEventWhenPleaMcaGuilty() {

        final UUID offenceId = randomUUID();
        final UUID hearingId = randomUUID();
        final UUID prosecutionCaseId = randomUUID();
        final Hearing hearing = getHearing(offenceId, prosecutionCaseId, hearingId);
        this.hearingAggregateMomento.setHearing(hearing);

        final PleaModel pleaModel = PleaModel.pleaModel()
                .withOffenceId(offenceId)
                .withPlea(Plea.plea()
                        .withPleaValue(PleaValue.MCA_GUILTY)
                        .build()
                ).build();

        final Stream<Object> objectStream = pleaDelegate.updatePlea(hearingId, pleaModel);
        assertThat(objectStream, is(notNullValue()));

        final List<Object> events = new ArrayList<>();
        objectStream.forEach(os -> events.add(os));

        final PleaUpsert pleaUpsert = (PleaUpsert) events.get(0);
        assertThat(pleaUpsert, is(notNullValue()));
        assertTrue(events.get(1) instanceof ConvictionDateAdded);
    }

    @Test
    public void shouldAddConvictionDateAddedEventWhenPleaAutrefoisConvict() {

        final UUID offenceId = randomUUID();
        final UUID hearingId = randomUUID();
        final UUID prosecutionCaseId = randomUUID();
        final Hearing hearing = getHearing(offenceId, prosecutionCaseId, hearingId);
        this.hearingAggregateMomento.setHearing(hearing);

        final PleaModel pleaModel = PleaModel.pleaModel()
                .withOffenceId(offenceId)
                .withPlea(Plea.plea()
                        .withPleaValue(PleaValue.AUTREFOIS_CONVICT)
                        .build()
                ).build();

        final Stream<Object> objectStream = pleaDelegate.updatePlea(hearingId, pleaModel);
        assertThat(objectStream, is(notNullValue()));

        final List<Object> events = new ArrayList<>();
        objectStream.forEach(os -> events.add(os));

        final PleaUpsert pleaUpsert = (PleaUpsert) events.get(0);
        assertThat(pleaUpsert, is(notNullValue()));
        assertTrue(events.get(1) instanceof ConvictionDateAdded);
    }

    @Test
    public void shouldAddConvictionDateAddedEventWhenPleaConsent() {

        final UUID offenceId = randomUUID();
        final UUID hearingId = randomUUID();
        final UUID prosecutionCaseId = randomUUID();
        final Hearing hearing = getHearing(offenceId, prosecutionCaseId, hearingId);
        this.hearingAggregateMomento.setHearing(hearing);

        final PleaModel pleaModel = PleaModel.pleaModel()
                .withOffenceId(offenceId)
                .withPlea(Plea.plea()
                        .withPleaValue(PleaValue.CONSENTS)
                        .build()
                ).build();

        final Stream<Object> objectStream = pleaDelegate.updatePlea(hearingId, pleaModel);
        assertThat(objectStream, is(notNullValue()));

        final List<Object> events = new ArrayList<>();
        objectStream.forEach(os -> events.add(os));

        final PleaUpsert pleaUpsert = (PleaUpsert) events.get(0);
        assertThat(pleaUpsert, is(notNullValue()));
        assertTrue(events.get(1) instanceof ConvictionDateAdded);
    }

    @Test
    public void shouldAddConvictionDateAddedEventWhenPleaChangeToGuiltyAfterSwornIn() {

        final UUID offenceId = randomUUID();
        final UUID hearingId = randomUUID();
        final UUID prosecutionCaseId = randomUUID();
        final Hearing hearing = getHearing(offenceId, prosecutionCaseId, hearingId);
        this.hearingAggregateMomento.setHearing(hearing);

        final PleaModel pleaModel = PleaModel.pleaModel()
                .withOffenceId(offenceId)
                .withPlea(Plea.plea()
                        .withPleaValue(PleaValue.CHANGE_TO_GUILTY_AFTER_SWORN_IN)
                        .build()
                ).build();

        final Stream<Object> objectStream = pleaDelegate.updatePlea(hearingId, pleaModel);
        assertThat(objectStream, is(notNullValue()));

        final List<Object> events = new ArrayList<>();
        objectStream.forEach(os -> events.add(os));

        final PleaUpsert pleaUpsert = (PleaUpsert) events.get(0);
        assertThat(pleaUpsert, is(notNullValue()));
        assertTrue(events.get(1) instanceof ConvictionDateAdded);
    }

    @Test
    public void shouldAddConvictionDateAddedEventWhenPleaChangeToGuiltyNoSwornIn() {

        final UUID offenceId = randomUUID();
        final UUID hearingId = randomUUID();
        final UUID prosecutionCaseId = randomUUID();
        final Hearing hearing = getHearing(offenceId, prosecutionCaseId, hearingId);
        this.hearingAggregateMomento.setHearing(hearing);

        final PleaModel pleaModel = PleaModel.pleaModel()
                .withOffenceId(offenceId)
                .withPlea(Plea.plea()
                        .withPleaValue(PleaValue.CHANGE_TO_GUILTY_NO_SWORN_IN)
                        .build()
                ).build();

        final Stream<Object> objectStream = pleaDelegate.updatePlea(hearingId, pleaModel);
        assertThat(objectStream, is(notNullValue()));

        final List<Object> events = new ArrayList<>();
        objectStream.forEach(os -> events.add(os));

        final PleaUpsert pleaUpsert = (PleaUpsert) events.get(0);
        assertThat(pleaUpsert, is(notNullValue()));
        assertTrue(events.get(1) instanceof ConvictionDateAdded);
    }

    @Test
    public void shouldAddConvictionDateAddedEventWhenPleaChangeToGuiltyMagistratesCourt() {

        final UUID offenceId = randomUUID();
        final UUID hearingId = randomUUID();
        final UUID prosecutionCaseId = randomUUID();
        final Hearing hearing = getHearing(offenceId, prosecutionCaseId, hearingId);
        this.hearingAggregateMomento.setHearing(hearing);

        final PleaModel pleaModel = PleaModel.pleaModel()
                .withOffenceId(offenceId)
                .withPlea(Plea.plea()
                        .withPleaValue(PleaValue.CHANGE_TO_GUILTY_MAGISTRATES_COURT)
                        .build()
                ).build();

        final Stream<Object> objectStream = pleaDelegate.updatePlea(hearingId, pleaModel);
        assertThat(objectStream, is(notNullValue()));

        final List<Object> events = new ArrayList<>();
        objectStream.forEach(os -> events.add(os));

        final PleaUpsert pleaUpsert = (PleaUpsert) events.get(0);
        assertThat(pleaUpsert, is(notNullValue()));
        assertTrue(events.get(1) instanceof ConvictionDateAdded);
    }

    @Test
    public void shouldAddConvictionDateAddedEventWhenPleaNotGuilty() {

        final UUID offenceId = randomUUID();
        final UUID hearingId = randomUUID();
        final UUID prosecutionCaseId = randomUUID();
        final Hearing hearing = getHearing(offenceId, prosecutionCaseId, hearingId);
        this.hearingAggregateMomento.setHearing(hearing);

        final PleaModel pleaModel = PleaModel.pleaModel()
                .withProsecutionCaseId(prosecutionCaseId)
                .withDefendantId(offenceId)
                .withOffenceId(offenceId)
                .withAllocationDecision(AllocationDecision.allocationDecision()
                        .withOffenceId(offenceId)
                        .build())
                .withPlea(Plea.plea()
                        .withPleaValue(PleaValue.NOT_GUILTY)
                        .build()
                )
                .build();

        final Stream<Object> objectStream = pleaDelegate.updatePlea(hearingId, pleaModel);

        assertThat(objectStream, is(notNullValue()));

        final List<Object> events = new ArrayList<>();

        objectStream.forEach(os -> events.add(os));

        final PleaUpsert pleaUpsert = (PleaUpsert) events.get(0);
        assertThat(pleaUpsert, is(notNullValue()));
        assertThat(pleaUpsert.getHearingId(), is(hearingId));

        final ConvictionDateRemoved convictionDateRemoved = (ConvictionDateRemoved) events.get(1);
        assertThat(convictionDateRemoved, is(notNullValue()));
        assertThat(convictionDateRemoved.getOffenceId(), is(offenceId));
        assertThat(convictionDateRemoved.getHearingId(), is(hearingId));
        assertThat(convictionDateRemoved.getCaseId(), is(prosecutionCaseId));
    }

    @Test
    public void shouldAddConvictionDateAddedEventWhenPleaUnfit() {

        final UUID offenceId = randomUUID();
        final UUID hearingId = randomUUID();
        final UUID prosecutionCaseId = randomUUID();
        final Hearing hearing = getHearing(offenceId, prosecutionCaseId, hearingId);
        this.hearingAggregateMomento.setHearing(hearing);

        final PleaModel pleaModel = PleaModel.pleaModel()
                .withOffenceId(offenceId)
                .withPlea(Plea.plea()
                        .withPleaValue(PleaValue.UNFIT_TO_PLEAD)
                        .build()
                ).build();

        final Stream<Object> objectStream = pleaDelegate.updatePlea(hearingId, pleaModel);
        assertThat(objectStream, is(notNullValue()));

        final List<Object> events = new ArrayList<>();
        objectStream.forEach(os -> events.add(os));

        final PleaUpsert pleaUpsert = (PleaUpsert) events.get(0);
        assertThat(pleaUpsert, is(notNullValue()));
        assertTrue(events.get(1) instanceof ConvictionDateRemoved);
    }

    @Test
    public void shouldAddConvictionDateAddedEventWhenPleaAutrefois() {

        final UUID offenceId = randomUUID();
        final UUID hearingId = randomUUID();
        final UUID prosecutionCaseId = randomUUID();
        final Hearing hearing = getHearing(offenceId, prosecutionCaseId, hearingId);
        this.hearingAggregateMomento.setHearing(hearing);

        final PleaModel pleaModel = PleaModel.pleaModel()
                .withOffenceId(offenceId)
                .withPlea(Plea.plea()
                        .withPleaValue(PleaValue.AUTREFOIS_ACQUIT)
                        .build()
                ).build();

        final Stream<Object> objectStream = pleaDelegate.updatePlea(hearingId, pleaModel);
        assertThat(objectStream, is(notNullValue()));

        final List<Object> events = new ArrayList<>();
        objectStream.forEach(os -> events.add(os));

        final PleaUpsert pleaUpsert = (PleaUpsert) events.get(0);
        assertThat(pleaUpsert, is(notNullValue()));
        assertTrue(events.get(1) instanceof ConvictionDateRemoved);
    }

    @Test
    public void shouldAddConvictionDateAddedEventWhenPleaOpposes() {

        final UUID offenceId = randomUUID();
        final UUID hearingId = randomUUID();
        final UUID prosecutionCaseId = randomUUID();
        final Hearing hearing = getHearing(offenceId, prosecutionCaseId, hearingId);
        this.hearingAggregateMomento.setHearing(hearing);

        final PleaModel pleaModel = PleaModel.pleaModel()
                .withOffenceId(offenceId)
                .withPlea(Plea.plea()
                        .withPleaValue(PleaValue.OPPOSES)
                        .build()
                ).build();

        final Stream<Object> objectStream = pleaDelegate.updatePlea(hearingId, pleaModel);
        assertThat(objectStream, is(notNullValue()));

        final List<Object> events = new ArrayList<>();
        objectStream.forEach(os -> events.add(os));

        final PleaUpsert pleaUpsert = (PleaUpsert) events.get(0);
        assertThat(pleaUpsert, is(notNullValue()));
        assertTrue(events.get(1) instanceof ConvictionDateRemoved);
    }

    @Test
    public void shouldAddConvictionDateAddedEventWhenPleaNoPlea() {

        final UUID offenceId = randomUUID();
        final UUID hearingId = randomUUID();
        final UUID prosecutionCaseId = randomUUID();
        final Hearing hearing = getHearing(offenceId, prosecutionCaseId, hearingId);
        this.hearingAggregateMomento.setHearing(hearing);

        final PleaModel pleaModel = PleaModel.pleaModel()
                .withOffenceId(offenceId)
                .withPlea(Plea.plea()
                        .withPleaValue(PleaValue.NO_PLEA)
                        .build()
                ).build();

        final Stream<Object> objectStream = pleaDelegate.updatePlea(hearingId, pleaModel);
        assertThat(objectStream, is(notNullValue()));

        final List<Object> events = new ArrayList<>();
        objectStream.forEach(os -> events.add(os));

        final PleaUpsert pleaUpsert = (PleaUpsert) events.get(0);
        assertThat(pleaUpsert, is(notNullValue()));
        assertTrue(events.get(1) instanceof ConvictionDateRemoved);
    }

    @Test
    public void shouldAddConvictionDateAddedEventWhenPleaChangeToNotGuilty() {

        final UUID offenceId = randomUUID();
        final UUID hearingId = randomUUID();
        final UUID prosecutionCaseId = randomUUID();
        final Hearing hearing = getHearing(offenceId, prosecutionCaseId, hearingId);
        this.hearingAggregateMomento.setHearing(hearing);

        final PleaModel pleaModel = PleaModel.pleaModel()
                .withOffenceId(offenceId)
                .withPlea(Plea.plea()
                        .withPleaValue(PleaValue.CHANGE_TO_NOT_GUILTY)
                        .build()
                ).build();

        final Stream<Object> objectStream = pleaDelegate.updatePlea(hearingId, pleaModel);
        assertThat(objectStream, is(notNullValue()));

        final List<Object> events = new ArrayList<>();
        objectStream.forEach(os -> events.add(os));

        final PleaUpsert pleaUpsert = (PleaUpsert) events.get(0);
        assertThat(pleaUpsert, is(notNullValue()));
        assertTrue(events.get(1) instanceof ConvictionDateRemoved);
    }

    @Test
    public void shouldAddConvictionDateAddedEventWhenPleaPardon() {

        final UUID offenceId = randomUUID();
        final UUID hearingId = randomUUID();
        final UUID prosecutionCaseId = randomUUID();
        final Hearing hearing = getHearing(offenceId, prosecutionCaseId, hearingId);
        this.hearingAggregateMomento.setHearing(hearing);

        final PleaModel pleaModel = PleaModel.pleaModel()
                .withOffenceId(offenceId)
                .withPlea(Plea.plea()
                        .withPleaValue(PleaValue.PARDON)
                        .build()
                ).build();

        final Stream<Object> objectStream = pleaDelegate.updatePlea(hearingId, pleaModel);
        assertThat(objectStream, is(notNullValue()));

        final List<Object> events = new ArrayList<>();
        objectStream.forEach(os -> events.add(os));

        final PleaUpsert pleaUpsert = (PleaUpsert) events.get(0);
        assertThat(pleaUpsert, is(notNullValue()));
        assertTrue(events.get(1) instanceof ConvictionDateRemoved);
    }

    @Test
    public void shouldAddConvictionDateAddedEventWhenPleaIsAdmits() {

        final UUID offenceId = randomUUID();
        final UUID hearingId = randomUUID();
        final UUID prosecutionCaseId = randomUUID();
        final Hearing hearing = getHearing(offenceId, prosecutionCaseId, hearingId);
        this.hearingAggregateMomento.setHearing(hearing);

        final PleaModel pleaModel = PleaModel.pleaModel()
                .withOffenceId(offenceId)
                .withPlea(Plea.plea()
                        .withPleaValue(PleaValue.ADMITS)
                        .build()
                ).build();

        final Stream<Object> objectStream = pleaDelegate.updatePlea(hearingId, pleaModel);
        assertThat(objectStream, is(notNullValue()));

        final List<Object> events = new ArrayList<>();
        objectStream.forEach(os -> events.add(os));

        final PleaUpsert pleaUpsert = (PleaUpsert) events.get(0);
        assertThat(pleaUpsert, is(notNullValue()));
        assertTrue(events.get(1) instanceof ConvictionDateAdded);
    }

    @Test
    public void shouldAddConvictionDateAddedEventWhenPleaIsGuiltyToALesserOffenceNamely() {

        final UUID offenceId = randomUUID();
        final UUID hearingId = randomUUID();
        final UUID prosecutionCaseId = randomUUID();
        final Hearing hearing = getHearing(offenceId, prosecutionCaseId, hearingId);
        this.hearingAggregateMomento.setHearing(hearing);

        final PleaModel pleaModel = PleaModel.pleaModel()
                .withOffenceId(offenceId)
                .withPlea(Plea.plea()
                        .withPleaValue(PleaValue.GUILTY_TO_A_LESSER_OFFENCE_NAMELY)
                        .build()
                ).build();

        final Stream<Object> objectStream = pleaDelegate.updatePlea(hearingId, pleaModel);
        assertThat(objectStream, is(notNullValue()));

        final List<Object> events = new ArrayList<>();
        objectStream.forEach(os -> events.add(os));

        final PleaUpsert pleaUpsert = (PleaUpsert) events.get(0);
        assertThat(pleaUpsert, is(notNullValue()));
        assertTrue(events.get(1) instanceof ConvictionDateAdded);
    }

    @Test
    public void shouldAddConvictionDateAddedEventWhenPleaIsGuiltyToAnAlternativeOffenceNotChargedNamely() {

        final UUID offenceId = randomUUID();
        final UUID hearingId = randomUUID();
        final UUID prosecutionCaseId = randomUUID();
        final Hearing hearing = getHearing(offenceId, prosecutionCaseId, hearingId);
        this.hearingAggregateMomento.setHearing(hearing);

        final PleaModel pleaModel = PleaModel.pleaModel()
                .withOffenceId(offenceId)
                .withPlea(Plea.plea()
                        .withPleaValue(PleaValue.GUILTY_TO_AN_ALTERNATIVE_OFFENCE_NOT_CHARGED_NAMELY)
                        .build()
                ).build();

        final Stream<Object> objectStream = pleaDelegate.updatePlea(hearingId, pleaModel);
        assertThat(objectStream, is(notNullValue()));

        final List<Object> events = new ArrayList<>();
        objectStream.forEach(os -> events.add(os));

        final PleaUpsert pleaUpsert = (PleaUpsert) events.get(0);
        assertThat(pleaUpsert, is(notNullValue()));
        assertTrue(events.get(1) instanceof ConvictionDateAdded);
    }


    @Test
    public void shouldAddConvictionDateRemovedEventWhenPleaIsDenies() {

        final UUID offenceId = randomUUID();
        final UUID hearingId = randomUUID();
        final UUID prosecutionCaseId = randomUUID();
        final Hearing hearing = getHearing(offenceId, prosecutionCaseId, hearingId);
        this.hearingAggregateMomento.setHearing(hearing);

        final PleaModel pleaModel = PleaModel.pleaModel()
                .withOffenceId(offenceId)
                .withPlea(Plea.plea()
                        .withPleaValue(PleaValue.DENIES)
                        .build()
                ).build();

        final Stream<Object> objectStream = pleaDelegate.updatePlea(hearingId, pleaModel);
        assertThat(objectStream, is(notNullValue()));

        final List<Object> events = new ArrayList<>();
        objectStream.forEach(os -> events.add(os));

        final PleaUpsert pleaUpsert = (PleaUpsert) events.get(0);
        assertThat(pleaUpsert, is(notNullValue()));
        assertTrue(events.get(1) instanceof ConvictionDateRemoved);
    }

    private Hearing getHearing(final UUID offenceId, final UUID prosecutionCaseId, final UUID hearingId) {
        final Defendant defendant = defendant()
                .withId(randomUUID())
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


}