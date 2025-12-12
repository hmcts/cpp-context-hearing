package uk.gov.moj.cpp.hearing.event;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.core.courts.AllocationDecision.allocationDecision;
import static uk.gov.justice.core.courts.IndicatedPlea.indicatedPlea;
import static uk.gov.justice.core.courts.Plea.plea;
import static uk.gov.justice.core.courts.PleaModel.pleaModel;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.moj.cpp.hearing.domain.event.PleaUpsert.pleaUpsert;
import static uk.gov.moj.cpp.hearing.event.Framework5Fix.toJsonEnvelope;
import static uk.gov.moj.cpp.hearing.test.ObjectConverters.asPojo;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;

import uk.gov.justice.core.courts.AllocationDecision;
import uk.gov.justice.core.courts.IndicatedPlea;
import uk.gov.justice.core.courts.IndicatedPleaValue;
import uk.gov.justice.core.courts.Plea;
import uk.gov.justice.core.courts.PleaModel;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.spi.DefaultEnvelope;
import uk.gov.justice.services.test.utils.framework.api.JsonObjectConvertersFactory;
import uk.gov.moj.cpp.hearing.domain.event.EnrichAssociatedHearingsWithIndicatedPlea;
import uk.gov.moj.cpp.hearing.domain.event.PleaUpsert;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;


public class PleaUpdateEventProcessorTest {
    private static final String GUILTY = "GUILTY";

    @Spy
    private final Enveloper enveloper = createEnveloper();

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter = new JsonObjectConvertersFactory().jsonObjectToObjectConverter();

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter = new JsonObjectConvertersFactory().objectToJsonObjectConverter();

    @Mock
    private Sender sender;

    @Captor
    private ArgumentCaptor<DefaultEnvelope> envelopeArgumentCaptor;

    @InjectMocks
    private PleaUpdateEventProcessor pleaUpdateEventProcessor;

    private UUID offenceId;
    private UUID prosecutionCaseId;
    private UUID defendantId;
    private UUID courtApplicationId;

    @BeforeEach
    public void initMocks() {
        this.offenceId = randomUUID();
        this.defendantId = randomUUID();
        this.prosecutionCaseId = randomUUID();
        this.courtApplicationId = randomUUID();
        MockitoAnnotations.initMocks(this);
    }


    @Test
    public void offencePleaUpdate() {
        final PleaUpsert pleaUpsert = pleaUpsert()
                .setHearingId(randomUUID())
                .setPleaModel(pleaModel()
                        .withProsecutionCaseId(prosecutionCaseId)
                        .withOffenceId(offenceId)
                        .withDefendantId(defendantId)
                        .withPlea(plea()
                                .withOffenceId(offenceId)
                                .withPleaDate(PAST_LOCAL_DATE.next())
                                .withPleaValue(GUILTY)
                                .build())
                        .build());
        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.hearing-offence-plea-updated"),
                objectToJsonObjectConverter.convert(pleaUpsert));

        this.pleaUpdateEventProcessor.offencePleaUpdate(event);

        verify(this.sender, times(3)).send(this.envelopeArgumentCaptor.capture());

        List<DefaultEnvelope> events = this.envelopeArgumentCaptor.getAllValues();

        assertThat(toJsonEnvelope(events.get(0)).metadata().name(), is("hearing.command.update-plea-against-offence"));

        final PleaUpsert pleaUpsertEvent = asPojo(toJsonEnvelope(events.get(0)), PleaUpsert.class);
        assertThat(pleaUpsertEvent, isBean(PleaUpsert.class)
                .with(PleaUpsert::getHearingId, is(pleaUpsert.getHearingId())));

        assertThat(pleaUpsertEvent.getPleaModel().getPlea(), isBean(Plea.class)
                .with(Plea::getOffenceId, is(pleaUpsertEvent.getPleaModel().getPlea().getOffenceId()))
                .with(Plea::getPleaDate, is(pleaUpsertEvent.getPleaModel().getPlea().getPleaDate()))
                .with(Plea::getPleaValue, is(pleaUpsertEvent.getPleaModel().getPlea().getPleaValue())));

        assertThat(pleaUpsert.getPleaModel().getIndicatedPlea(), is(nullValue()));
        assertThat(pleaUpsert.getPleaModel().getAllocationDecision(), is(nullValue()));

        assertThat(toJsonEnvelope(events.get(1)).metadata().name(), is("public.hearing.plea-updated"));

        assertThat(asPojo(toJsonEnvelope(events.get(1)), Plea.class), isBean(Plea.class)
                .with(Plea::getOffenceId, is(pleaUpsertEvent.getPleaModel().getPlea().getOffenceId())));


        assertThat(toJsonEnvelope(events.get(2)).metadata().name(), is("public.hearing.hearing-offence-plea-updated"));

        assertThat(asPojo(toJsonEnvelope(events.get(2)), PleaUpsert.class), isBean(PleaUpsert.class)
                .with(PleaUpsert::getPleaModel, isBean(PleaModel.class)
                        .with(PleaModel::getProsecutionCaseId, is(pleaUpsert.getPleaModel().getProsecutionCaseId()))
                        .with(PleaModel::getOffenceId, is(pleaUpsert.getPleaModel().getOffenceId()))
                        .with(PleaModel::getPlea, isBean(Plea.class)
                                .with(Plea::getOffenceId, is(offenceId)))));
    }

    @Test
    public void courtapplicationPleaUpdate() {
        final PleaUpsert pleaUpsert = pleaUpsert()
                .setHearingId(randomUUID())
                .setPleaModel(pleaModel()
                        .withProsecutionCaseId(prosecutionCaseId)
                        .withApplicationId(courtApplicationId)
                        .withDefendantId(defendantId)
                        .withPlea(plea()
                                .withOffenceId(offenceId)
                                .withPleaDate(PAST_LOCAL_DATE.next())
                                .withPleaValue(GUILTY)
                                .build())
                        .build());
        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.hearing-offence-plea-updated"),
                objectToJsonObjectConverter.convert(pleaUpsert));

        this.pleaUpdateEventProcessor.offencePleaUpdate(event);

        verify(this.sender, times(2)).send(this.envelopeArgumentCaptor.capture());

        List<DefaultEnvelope> events = this.envelopeArgumentCaptor.getAllValues();

        assertThat(toJsonEnvelope(events.get(0)).metadata().name(), is("public.hearing.plea-updated"));

        assertThat(asPojo(toJsonEnvelope(events.get(0)), Plea.class), isBean(Plea.class)
                .with(Plea::getApplicationId, is(pleaUpsert.getPleaModel().getApplicationId())));

        assertThat(toJsonEnvelope(events.get(1)).metadata().name(), is("public.hearing.hearing-offence-plea-updated"));

        assertThat(asPojo(toJsonEnvelope(events.get(1)), PleaUpsert.class), isBean(PleaUpsert.class)
                .with(PleaUpsert::getPleaModel, isBean(PleaModel.class)
                        .with(PleaModel::getApplicationId, is(pleaUpsert.getPleaModel().getApplicationId()))));
    }


    @Test
    public void offenceIndicatedPleaUpdate() {
        final PleaUpsert pleaUpsert = pleaUpsert()
                .setHearingId(randomUUID())
                .setPleaModel(pleaModel()
                        .withProsecutionCaseId(prosecutionCaseId)
                        .withOffenceId(offenceId)
                        .withDefendantId(defendantId)
                        .withIndicatedPlea(indicatedPlea()
                                .withOffenceId(offenceId)
                                .withIndicatedPleaDate(PAST_LOCAL_DATE.next())
                                .withIndicatedPleaValue(IndicatedPleaValue.INDICATED_GUILTY)
                                .build())
                        .build());

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.hearing-offence-plea-updated"),
                objectToJsonObjectConverter.convert(pleaUpsert));

        this.pleaUpdateEventProcessor.offencePleaUpdate(event);

        verify(this.sender, times(3)).send(this.envelopeArgumentCaptor.capture());

        List<DefaultEnvelope> events = this.envelopeArgumentCaptor.getAllValues();

        assertThat(toJsonEnvelope(events.get(0)).metadata().name(), is("hearing.command.update-plea-against-offence"));

        final PleaUpsert pleaUpsertEvent = asPojo(toJsonEnvelope(events.get(0)), PleaUpsert.class);
        assertThat(pleaUpsertEvent, isBean(PleaUpsert.class)
                .with(PleaUpsert::getHearingId, is(pleaUpsert.getHearingId())));

        assertThat(pleaUpsertEvent.getPleaModel().getIndicatedPlea(), isBean(IndicatedPlea.class)
                .with(IndicatedPlea::getOffenceId, is(pleaUpsertEvent.getPleaModel().getIndicatedPlea().getOffenceId()))
                .with(IndicatedPlea::getIndicatedPleaDate, is(pleaUpsertEvent.getPleaModel().getIndicatedPlea().getIndicatedPleaDate()))
                .with(IndicatedPlea::getIndicatedPleaValue, is(pleaUpsertEvent.getPleaModel().getIndicatedPlea().getIndicatedPleaValue())));

        assertThat(pleaUpsert.getPleaModel().getPlea(), is(nullValue()));
        assertThat(pleaUpsert.getPleaModel().getAllocationDecision(), is(nullValue()));

        assertThat(toJsonEnvelope(events.get(1)).metadata().name(), is("public.hearing.plea-updated"));

        assertThat(asPojo(toJsonEnvelope(events.get(1)), IndicatedPlea.class), isBean(IndicatedPlea.class)
                .with(IndicatedPlea::getOffenceId, is(pleaUpsertEvent.getPleaModel().getIndicatedPlea().getOffenceId())));

        assertThat(toJsonEnvelope(events.get(2)).metadata().name(), is("public.hearing.hearing-offence-plea-updated"));

        assertThat(asPojo(toJsonEnvelope(events.get(2)), PleaUpsert.class), isBean(PleaUpsert.class)
                .with(PleaUpsert::getPleaModel, isBean(PleaModel.class)
                        .with(PleaModel::getProsecutionCaseId, is(pleaUpsert.getPleaModel().getProsecutionCaseId()))
                        .with(PleaModel::getOffenceId, is(pleaUpsert.getPleaModel().getOffenceId()))
                        .with(PleaModel::getIndicatedPlea, isBean(IndicatedPlea.class)
                                .with(IndicatedPlea::getOffenceId, is(offenceId)))));
    }

    @Test
    public void offenceAllocationDecisionUpdate() {

        final PleaUpsert pleaUpsert = pleaUpsert()
                .setHearingId(randomUUID())
                .setPleaModel(pleaModel()
                        .withProsecutionCaseId(prosecutionCaseId)
                        .withOffenceId(offenceId)
                        .withDefendantId(defendantId)
                        .withAllocationDecision(allocationDecision()
                                .withOffenceId(offenceId)
                                .withAllocationDecisionDate(PAST_LOCAL_DATE.next())
                                .withOriginatingHearingId(randomUUID())
                                .build())
                        .build());

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.hearing-offence-plea-updated"),
                objectToJsonObjectConverter.convert(pleaUpsert));

        this.pleaUpdateEventProcessor.offencePleaUpdate(event);

        verify(this.sender, times(3)).send(this.envelopeArgumentCaptor.capture());

        List<DefaultEnvelope> events = this.envelopeArgumentCaptor.getAllValues();

        assertThat(toJsonEnvelope(events.get(0)).metadata().name(), is("hearing.command.update-plea-against-offence"));

        final PleaUpsert pleaUpsertEvent = asPojo(toJsonEnvelope(events.get(0)), PleaUpsert.class);
        assertThat(pleaUpsertEvent, isBean(PleaUpsert.class)
                .with(PleaUpsert::getHearingId, is(pleaUpsert.getHearingId())));

        assertThat(pleaUpsertEvent.getPleaModel().getAllocationDecision(), isBean(AllocationDecision.class)
                .with(AllocationDecision::getOffenceId, is(pleaUpsertEvent.getPleaModel().getAllocationDecision().getOffenceId()))
                .with(AllocationDecision::getAllocationDecisionDate, is(pleaUpsertEvent.getPleaModel().getAllocationDecision().getAllocationDecisionDate()))
                .with(AllocationDecision::getCourtIndicatedSentence, is(pleaUpsertEvent.getPleaModel().getAllocationDecision().getCourtIndicatedSentence())));

        assertThat(pleaUpsert.getPleaModel().getPlea(), is(nullValue()));
        assertThat(pleaUpsert.getPleaModel().getIndicatedPlea(), is(nullValue()));

        assertThat(toJsonEnvelope(events.get(1)).metadata().name(), is("public.hearing.plea-updated"));

        assertThat(asPojo(toJsonEnvelope(events.get(1)), AllocationDecision.class), isBean(AllocationDecision.class)
                .with(AllocationDecision::getOffenceId, is(pleaUpsertEvent.getPleaModel().getAllocationDecision().getOffenceId())));

        assertThat(toJsonEnvelope(events.get(2)).metadata().name(), is("public.hearing.hearing-offence-plea-updated"));

        assertThat(asPojo(toJsonEnvelope(events.get(2)), PleaUpsert.class), isBean(PleaUpsert.class)
                .with(PleaUpsert::getPleaModel, isBean(PleaModel.class)
                        .with(PleaModel::getProsecutionCaseId, is(pleaUpsert.getPleaModel().getProsecutionCaseId()))
                        .with(PleaModel::getOffenceId, is(pleaUpsert.getPleaModel().getOffenceId()))
                        .with(PleaModel::getAllocationDecision, isBean(AllocationDecision.class)
                                .with(AllocationDecision::getOffenceId, is(offenceId)))));
    }


    @Test
    public void updateOffenceIndicatedPleaToAllAssociatedHearings() {
        final UUID hearingId = randomUUID();
        final EnrichAssociatedHearingsWithIndicatedPlea enrichAssociatedHearingsWithIndicatedPlea = new EnrichAssociatedHearingsWithIndicatedPlea(Lists.newArrayList(hearingId),
                IndicatedPlea.indicatedPlea().withIndicatedPleaValue(IndicatedPleaValue.INDICATED_GUILTY).build());

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.events.enrich-associated-hearings-with-indicated-plea"),
                objectToJsonObjectConverter.convert(enrichAssociatedHearingsWithIndicatedPlea));

        this.pleaUpdateEventProcessor.enrichAssociatedHearingsWithIndicatedPlea(event);

        verify(this.sender, times(1)).send(this.envelopeArgumentCaptor.capture());

        List<DefaultEnvelope> events = this.envelopeArgumentCaptor.getAllValues();

        assertThat(toJsonEnvelope(events.get(0)).metadata().name(), is("hearing.command.enrich-associated-hearings-with-indicated-plea"));

        final EnrichAssociatedHearingsWithIndicatedPlea associatedHearingsWithIndicatedPlea = asPojo(toJsonEnvelope(events.get(0)), EnrichAssociatedHearingsWithIndicatedPlea.class);

        assertThat(associatedHearingsWithIndicatedPlea.getHearingIds().get(0), is(hearingId));
        assertThat(associatedHearingsWithIndicatedPlea.getIndicatedPlea(), isBean(IndicatedPlea.class));
        assertThat(associatedHearingsWithIndicatedPlea.getIndicatedPlea().getIndicatedPleaValue(), is(IndicatedPleaValue.INDICATED_GUILTY));

    }
}
