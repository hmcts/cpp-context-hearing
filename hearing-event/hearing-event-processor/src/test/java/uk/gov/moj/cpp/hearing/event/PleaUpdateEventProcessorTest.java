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
import static uk.gov.moj.cpp.hearing.test.ObjectConverters.asPojo;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;

import uk.gov.justice.core.courts.AllocationDecision;
import uk.gov.justice.core.courts.IndicatedPlea;
import uk.gov.justice.core.courts.IndicatedPleaValue;
import uk.gov.justice.core.courts.Plea;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonValueConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.PleaUpsert;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
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
    private final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();
    @Spy
    @InjectMocks
    private final JsonObjectToObjectConverter jsonObjectToObjectConverter = new JsonObjectToObjectConverter();
    @Spy
    @InjectMocks
    private final ObjectToJsonValueConverter objectToJsonValueConverter = new ObjectToJsonValueConverter(this.objectMapper);
    @Spy
    @InjectMocks
    private final ObjectToJsonObjectConverter objectToJsonObjectConverter = new ObjectToJsonObjectConverter();
    @Mock
    private Sender sender;
    @Captor
    private ArgumentCaptor<JsonEnvelope> envelopeArgumentCaptor;
    @InjectMocks
    private PleaUpdateEventProcessor pleaUpdateEventProcessor;

    private UUID offenceId;
    private UUID prosecutionCaseId;
    private UUID defendantId;


    @Before
    public void initMocks() {
        this.offenceId = randomUUID();
        this.defendantId = randomUUID();
        this.prosecutionCaseId = randomUUID();
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

        verify(this.sender, times(2)).send(this.envelopeArgumentCaptor.capture());

        List<JsonEnvelope> events = this.envelopeArgumentCaptor.getAllValues();

        assertThat(events.get(0).metadata().name(), is("hearing.command.update-plea-against-offence"));

        final PleaUpsert pleaUpsertEvent = asPojo(events.get(0), PleaUpsert.class);
        assertThat(pleaUpsertEvent, isBean(PleaUpsert.class)
                .with(PleaUpsert::getHearingId, is(pleaUpsert.getHearingId())));

        assertThat(pleaUpsertEvent.getPleaModel().getPlea(), isBean(Plea.class)
                .with(Plea::getOffenceId, is(pleaUpsertEvent.getPleaModel().getPlea().getOffenceId()))
                .with(Plea::getPleaDate, is(pleaUpsertEvent.getPleaModel().getPlea().getPleaDate()))
                .with(Plea::getPleaValue, is(pleaUpsertEvent.getPleaModel().getPlea().getPleaValue())));

        assertThat(pleaUpsert.getPleaModel().getIndicatedPlea(), is(nullValue()));
        assertThat(pleaUpsert.getPleaModel().getAllocationDecision(), is(nullValue()));

        assertThat(events.get(1).metadata().name(), is("public.hearing.plea-updated"));

        assertThat(asPojo(events.get(1), Plea.class), isBean(Plea.class)
                .with(Plea::getOffenceId, is(pleaUpsertEvent.getPleaModel().getPlea().getOffenceId())));
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

        verify(this.sender, times(2)).send(this.envelopeArgumentCaptor.capture());

        List<JsonEnvelope> events = this.envelopeArgumentCaptor.getAllValues();

        assertThat(events.get(0).metadata().name(), is("hearing.command.update-plea-against-offence"));

        final PleaUpsert pleaUpsertEvent = asPojo(events.get(0), PleaUpsert.class);
        assertThat(pleaUpsertEvent, isBean(PleaUpsert.class)
                .with(PleaUpsert::getHearingId, is(pleaUpsert.getHearingId())));

        assertThat(pleaUpsertEvent.getPleaModel().getIndicatedPlea(), isBean(IndicatedPlea.class)
                .with(IndicatedPlea::getOffenceId, is(pleaUpsertEvent.getPleaModel().getIndicatedPlea().getOffenceId()))
                .with(IndicatedPlea::getIndicatedPleaDate, is(pleaUpsertEvent.getPleaModel().getIndicatedPlea().getIndicatedPleaDate()))
                .with(IndicatedPlea::getIndicatedPleaValue, is(pleaUpsertEvent.getPleaModel().getIndicatedPlea().getIndicatedPleaValue())));

        assertThat(pleaUpsert.getPleaModel().getPlea(), is(nullValue()));
        assertThat(pleaUpsert.getPleaModel().getAllocationDecision(), is(nullValue()));

        assertThat(events.get(1).metadata().name(), is("public.hearing.plea-updated"));

        assertThat(asPojo(events.get(1), IndicatedPlea.class), isBean(IndicatedPlea.class)
                .with(IndicatedPlea::getOffenceId, is(pleaUpsertEvent.getPleaModel().getIndicatedPlea().getOffenceId())));
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

        verify(this.sender, times(2)).send(this.envelopeArgumentCaptor.capture());

        List<JsonEnvelope> events = this.envelopeArgumentCaptor.getAllValues();

        assertThat(events.get(0).metadata().name(), is("hearing.command.update-plea-against-offence"));

        final PleaUpsert pleaUpsertEvent = asPojo(events.get(0), PleaUpsert.class);
        assertThat(pleaUpsertEvent, isBean(PleaUpsert.class)
                .with(PleaUpsert::getHearingId, is(pleaUpsert.getHearingId())));

        assertThat(pleaUpsertEvent.getPleaModel().getAllocationDecision(), isBean(AllocationDecision.class)
                .with(AllocationDecision::getOffenceId, is(pleaUpsertEvent.getPleaModel().getAllocationDecision().getOffenceId()))
                .with(AllocationDecision::getAllocationDecisionDate, is(pleaUpsertEvent.getPleaModel().getAllocationDecision().getAllocationDecisionDate()))
                .with(AllocationDecision::getCourtIndicatedSentence, is(pleaUpsertEvent.getPleaModel().getAllocationDecision().getCourtIndicatedSentence())));

        assertThat(pleaUpsert.getPleaModel().getPlea(), is(nullValue()));
        assertThat(pleaUpsert.getPleaModel().getIndicatedPlea(), is(nullValue()));

        assertThat(events.get(1).metadata().name(), is("public.hearing.plea-updated"));

        assertThat(asPojo(events.get(1), AllocationDecision.class), isBean(AllocationDecision.class)
                .with(AllocationDecision::getOffenceId, is(pleaUpsertEvent.getPleaModel().getAllocationDecision().getOffenceId())));
    }
}