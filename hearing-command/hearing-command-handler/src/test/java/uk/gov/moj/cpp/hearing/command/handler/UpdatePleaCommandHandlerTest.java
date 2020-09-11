package uk.gov.moj.cpp.hearing.command.handler;

import static java.util.UUID.randomUUID;
import static org.codehaus.groovy.runtime.InvokerHelper.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.core.courts.IndicatedPleaValue.INDICATED_GUILTY;
import static uk.gov.justice.core.courts.IndicatedPleaValue.INDICATED_NOT_GUILTY;
import static uk.gov.justice.core.courts.PleaModel.pleaModel;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;
import static uk.gov.moj.cpp.hearing.domain.updatepleas.UpdatePleaCommand.updatePleaCommand;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.ObjectConverters.asPojo;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;

import uk.gov.justice.core.courts.AllocationDecision;
import uk.gov.justice.core.courts.DelegatedPowers;
import uk.gov.justice.core.courts.IndicatedPlea;
import uk.gov.justice.core.courts.IndicatedPleaValue;
import uk.gov.justice.core.courts.LesserOrAlternativeOffence;
import uk.gov.justice.core.courts.Plea;
import uk.gov.justice.core.courts.PleaModel;
import uk.gov.justice.core.courts.Source;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.handler.service.ReferenceDataService;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.OffenceAggregate;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateAdded;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateRemoved;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.domain.event.OffencePleaUpdated;
import uk.gov.moj.cpp.hearing.domain.event.PleaUpsert;
import uk.gov.moj.cpp.hearing.domain.updatepleas.UpdateOffencePleaCommand;
import uk.gov.moj.cpp.hearing.domain.updatepleas.UpdatePleaCommand;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UpdatePleaCommandHandlerTest {
    private static final String GUILTY = "GUILTY";
    private static final String NOT_GUILTY = "NOT_GUILTY";

    private static InitiateHearingCommandHelper hearing = h(standardInitiateHearingTemplate());
    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(
            HearingInitiated.class,
            PleaUpsert.class,
            OffencePleaUpdated.class,
            ConvictionDateAdded.class,
            ConvictionDateRemoved.class);
    private ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();
    @InjectMocks
    private UpdatePleaCommandHandler hearingCommandHandler;
    @Mock
    private EventStream hearingAggregateEventStream;
    @Mock
    private EventStream offenceAggregateEventStream;
    @Mock
    private EventSource eventSource;
    @Mock
    private AggregateService aggregateService;
    @Mock
    private ReferenceDataService referenceDataService;
    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;
    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    private Set<String> guiltyPleaTypes;

    @Before
    public void setup() {
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        setField(this.jsonObjectToObjectConverter, "objectMapper", objectMapper);
        setField(this.objectToJsonObjectConverter, "mapper", objectMapper);
        guiltyPleaTypes = createGuiltyPleaTypes();
    }

    @Test
    public void testHearingAggregateUpdatePlea_toNotGuilty_shouldNotHaveConvictionDate() throws Throwable {

        final String pleaValue = NOT_GUILTY;
        final LocalDate pleaDate = PAST_LOCAL_DATE.next();

        final Plea plea = getPlea(pleaDate, pleaValue, null);
        final IndicatedPlea indicatedPlea = getIndicatedPlea(INDICATED_NOT_GUILTY, pleaDate);
        final AllocationDecision allocationDecision = getAllocationDecission(pleaDate);

        final UpdatePleaCommand updatePleaCommand = updatePleaCommand()
                .setHearingId(hearing.getHearingId())
                .setPleas(getPleaModel(plea, indicatedPlea, allocationDecision));

        final HearingAggregate hearingAggregate = new HearingAggregate() {{
            apply(new HearingInitiated(hearing.getHearing()));
        }};


        when(this.eventSource.getStreamById(hearing.getHearingId())).thenReturn(this.hearingAggregateEventStream);

        when(this.aggregateService.get(this.hearingAggregateEventStream, HearingAggregate.class)).thenReturn(hearingAggregate);

        final JsonEnvelope jsonEnvelop = envelopeFrom(metadataWithRandomUUID("hearing.update-plea"), objectToJsonObjectConverter.convert(updatePleaCommand));

        this.hearingCommandHandler.updatePlea(jsonEnvelop);

        final List<?> events = verifyAppendAndGetArgumentFrom(this.hearingAggregateEventStream)
                .collect(Collectors.toList());

        PleaUpsert pleaUpsert = asPojo((JsonEnvelope) events.get(0), PleaUpsert.class);
        assertThat(pleaUpsert, isBean(PleaUpsert.class)
                .with(PleaUpsert::getHearingId, is(hearing.getHearingId())));

        assertThat(pleaUpsert.getPleaModel().getPlea(), isBean(Plea.class)
                .with(Plea::getOffenceId, is(hearing.getFirstOffenceForFirstDefendantForFirstCase().getId()))
                .with(Plea::getPleaDate, is(pleaDate))
                .with(Plea::getPleaValue, is(pleaValue))
                .with(Plea::getLesserOrAlternativeOffence, is(getLesserOrAlternativeOffence()))
        );

        assertThat(((JsonEnvelope) events.get(1)).metadata().name(), is("hearing.conviction-date-removed"));
        assertThat(asPojo((JsonEnvelope) events.get(1), ConvictionDateRemoved.class), isBean(ConvictionDateRemoved.class)
                .with(ConvictionDateRemoved::getOffenceId, is(hearing.getFirstOffenceForFirstDefendantForFirstCase().getId()))
        );
    }

    @SuppressWarnings("serial")
    @Test
    public void testHearingAggregateUpdatePlea_toGuilty_shouldHaveConvictionDateDelegated() throws Throwable {

        final String pleaValue = GUILTY;
        final LocalDate pleaDate = PAST_LOCAL_DATE.next();
        final DelegatedPowers delegatedPowers = DelegatedPowers.delegatedPowers()
                .withUserId(randomUUID())
                .withFirstName("David")
                .withLastName("Bowie").build();

        final Plea plea = getPlea(pleaDate, pleaValue, delegatedPowers);
        final IndicatedPlea indicatedPlea = getIndicatedPlea(INDICATED_NOT_GUILTY, pleaDate);
        final AllocationDecision allocationDecision = getAllocationDecission(pleaDate);

        final UpdatePleaCommand updatePleaCommand = updatePleaCommand()
                .setHearingId(hearing.getHearingId())
                .setPleas(getPleaModel(plea, indicatedPlea, allocationDecision));

        final HearingAggregate hearingAggregate = new HearingAggregate() {{
            apply(new HearingInitiated(hearing.getHearing()));
        }};

        when(this.eventSource.getStreamById(hearing.getHearingId())).thenReturn(this.hearingAggregateEventStream);
        when(this.aggregateService.get(this.hearingAggregateEventStream, HearingAggregate.class)).thenReturn(hearingAggregate);
        when(this.referenceDataService.retrieveGuiltyPleaTypes()).thenReturn(guiltyPleaTypes);
        final JsonEnvelope jsonEnvelop = envelopeFrom(metadataWithRandomUUID("hearing.update-plea"),
                objectToJsonObjectConverter.convert(updatePleaCommand));

        this.hearingCommandHandler.updatePlea(jsonEnvelop);

        final List<JsonEnvelope> events = verifyAppendAndGetArgumentFrom(this.hearingAggregateEventStream).collect(Collectors.toList());

        JsonEnvelope jsonEnvelope = events.get(0);

        assertThat(jsonEnvelope.metadata().name(), is("hearing.hearing-offence-plea-updated"));

        PleaUpsert pleaUpsert = asPojo(events.get(0), PleaUpsert.class);

        assertThat(pleaUpsert, isBean(PleaUpsert.class)
                .with(PleaUpsert::getHearingId, is(hearing.getHearingId())));

        assertThat(pleaUpsert.getPleaModel().getPlea(), isBean(Plea.class)
                .with(Plea::getOffenceId, is(hearing.getFirstOffenceForFirstDefendantForFirstCase().getId()))
                .with(Plea::getPleaDate, is(pleaDate))
                .with(Plea::getPleaValue, is(pleaValue))
                .with(Plea::getLesserOrAlternativeOffence, is(getLesserOrAlternativeOffence()))
                .with(Plea::getDelegatedPowers, isBean(DelegatedPowers.class)
                        .with(DelegatedPowers::getFirstName, is(delegatedPowers.getFirstName()))
                        .with(DelegatedPowers::getLastName, is(delegatedPowers.getLastName()))
                        .with(DelegatedPowers::getUserId, is(delegatedPowers.getUserId()))));

        jsonEnvelope = events.get(1);

        assertThat(jsonEnvelope.metadata().name(), is("hearing.conviction-date-added"));
        assertThat(asPojo(jsonEnvelope, ConvictionDateAdded.class), isBean(ConvictionDateAdded.class)
                .with(ConvictionDateAdded::getOffenceId, is(hearing.getFirstOffenceForFirstDefendantForFirstCase().getId()))
                .with(ConvictionDateAdded::getConvictionDate, is(pleaDate))
        );
    }

    @Test
    public void testOffenceAggregateUpdatePlea_toNotGuilty() throws Throwable {

        String pleaValue = NOT_GUILTY;
        LocalDate pleaDate = PAST_LOCAL_DATE.next();

        when(this.eventSource.getStreamById(hearing.getFirstOffenceForFirstDefendantForFirstCase().getId())).thenReturn(this.offenceAggregateEventStream);
        when(this.aggregateService.get(this.offenceAggregateEventStream, OffenceAggregate.class)).thenReturn(new OffenceAggregate());

        final UpdateOffencePleaCommand command = UpdateOffencePleaCommand.
                updateOffencePleaCommand()
                .setHearingId(hearing.getHearingId())
                .setPleaModel(pleaModel()
                        .withOffenceId(hearing.getFirstOffenceForFirstDefendantForFirstCase().getId())
                        .withPlea(Plea.plea()
                                .withOffenceId(hearing.getFirstOffenceForFirstDefendantForFirstCase().getId())
                                .withPleaDate(pleaDate)
                                .withPleaValue(pleaValue)
                                .build()).build());

        final JsonEnvelope jsonEnvelop = envelopeFrom(metadataWithRandomUUID("hearing.offence-plea-updated"),
                objectToJsonObjectConverter.convert(command));

        this.hearingCommandHandler.updateOffencePlea(jsonEnvelop);

        final List<JsonEnvelope> events = verifyAppendAndGetArgumentFrom(this.offenceAggregateEventStream).collect(Collectors.toList());

        final JsonEnvelope jsonEnvelope = events.get(0);

        assertThat(jsonEnvelope.metadata().name(), is("hearing.offence-plea-updated"));

        PleaUpsert pleaUpsert = asPojo(events.get(0), PleaUpsert.class);

        assertThat(pleaUpsert, isBean(PleaUpsert.class)
                .with(PleaUpsert::getHearingId, is(hearing.getHearingId())));

        assertThat(pleaUpsert.getPleaModel().getPlea(), isBean(Plea.class)
                .with(Plea::getOffenceId, is(hearing.getFirstOffenceForFirstDefendantForFirstCase().getId()))
                .with(Plea::getPleaDate, is(pleaDate))
                .with(Plea::getLesserOrAlternativeOffence, is(nullValue()))
                .with(Plea::getPleaValue, is(pleaValue)));
    }

    @Test
    public void testIndicatedPlea_toGuilty() throws Throwable {

        final IndicatedPleaValue indicatedPleaValue = INDICATED_GUILTY;
        final LocalDate indicatedPleaDate = PAST_LOCAL_DATE.next();

        when(this.eventSource.getStreamById(hearing.getFirstOffenceForFirstDefendantForFirstCase().getId())).thenReturn(this.offenceAggregateEventStream);
        when(this.aggregateService.get(this.offenceAggregateEventStream, OffenceAggregate.class)).thenReturn(new OffenceAggregate());

        final UpdateOffencePleaCommand command = UpdateOffencePleaCommand.
                updateOffencePleaCommand()
                .setHearingId(hearing.getHearingId())
                .setPleaModel(pleaModel()
                        .withOffenceId(hearing.getFirstOffenceForFirstDefendantForFirstCase().getId())
                        .withIndicatedPlea(getIndicatedPlea(indicatedPleaValue, indicatedPleaDate))
                        .build());

        final JsonEnvelope jsonEnvelop = envelopeFrom(metadataWithRandomUUID("hearing.offence-plea-updated"),
                objectToJsonObjectConverter.convert(command));

        this.hearingCommandHandler.updateOffencePlea(jsonEnvelop);

        final List<JsonEnvelope> events = verifyAppendAndGetArgumentFrom(this.offenceAggregateEventStream).collect(Collectors.toList());

        final JsonEnvelope jsonEnvelope = events.get(0);

        assertThat(jsonEnvelope.metadata().name(), is("hearing.offence-plea-updated"));

        PleaUpsert pleaUpsert = asPojo(events.get(0), PleaUpsert.class);

        assertThat(pleaUpsert, isBean(PleaUpsert.class)
                .with(PleaUpsert::getHearingId, is(hearing.getHearingId())));

        final IndicatedPlea indicatedPlea = pleaUpsert.getPleaModel().getIndicatedPlea();
        assertThat(indicatedPlea, isBean(IndicatedPlea.class)
                .with(IndicatedPlea::getIndicatedPleaDate, is(indicatedPleaDate))
                .with(IndicatedPlea::getIndicatedPleaValue, is(indicatedPleaValue)));
        assertThat(pleaUpsert.getPleaModel().getPlea(), is(nullValue()));
        assertThat(pleaUpsert.getPleaModel().getAllocationDecision(), is(nullValue()));
    }

    @Test
    public void testHearingAggregateUpdateIndicatedPlea_toGuilty_shouldHaveConvictionDateDelegated() throws Throwable {

        final IndicatedPleaValue indicatedPleaValue = INDICATED_GUILTY;
        final LocalDate indicatedPleaDate = PAST_LOCAL_DATE.next();

        final UpdatePleaCommand command = updatePleaCommand()
                .setHearingId(hearing.getHearingId())
                .setPleas(asList(pleaModel()
                        .withOffenceId(hearing.getFirstOffenceForFirstDefendantForFirstCase().getId())
                        .withIndicatedPlea(getIndicatedPlea(indicatedPleaValue, indicatedPleaDate))
                        .build()));

        final HearingAggregate hearingAggregate = new HearingAggregate() {{
            apply(new HearingInitiated(hearing.getHearing()));
        }};

        when(this.eventSource.getStreamById(hearing.getHearingId())).thenReturn(this.hearingAggregateEventStream);
        when(this.aggregateService.get(this.hearingAggregateEventStream, HearingAggregate.class)).thenReturn(hearingAggregate);
        when(this.referenceDataService.retrieveGuiltyPleaTypes()).thenReturn(guiltyPleaTypes);
        final JsonEnvelope jsonEnvelop = envelopeFrom(metadataWithRandomUUID("hearing.update-plea"),
                objectToJsonObjectConverter.convert(command));

        this.hearingCommandHandler.updatePlea(jsonEnvelop);

        final List<JsonEnvelope> events = verifyAppendAndGetArgumentFrom(this.hearingAggregateEventStream).collect(Collectors.toList());

        JsonEnvelope jsonEnvelope = events.get(0);

        assertThat(jsonEnvelope.metadata().name(), is("hearing.hearing-offence-plea-updated"));

        PleaUpsert pleaUpsert = asPojo(events.get(0), PleaUpsert.class);

        assertThat(pleaUpsert, isBean(PleaUpsert.class)
                .with(PleaUpsert::getHearingId, is(hearing.getHearingId())));

        final IndicatedPlea indicatedPlea = pleaUpsert.getPleaModel().getIndicatedPlea();
        assertThat(indicatedPlea, isBean(IndicatedPlea.class)
                .with(IndicatedPlea::getIndicatedPleaDate, is(indicatedPleaDate))
                .with(IndicatedPlea::getIndicatedPleaValue, is(indicatedPleaValue)));
        assertThat(pleaUpsert.getPleaModel().getPlea(), is(nullValue()));
        assertThat(pleaUpsert.getPleaModel().getAllocationDecision(), is(nullValue()));

        jsonEnvelope = events.get(1);

        assertThat(jsonEnvelope.metadata().name(), is("hearing.conviction-date-added"));
        assertThat(asPojo(jsonEnvelope, ConvictionDateAdded.class), isBean(ConvictionDateAdded.class)
                .with(ConvictionDateAdded::getOffenceId, is(hearing.getFirstOffenceForFirstDefendantForFirstCase().getId()))
                .with(ConvictionDateAdded::getConvictionDate, is(indicatedPleaDate))
        );
    }


    @Test
    public void testIndicatedPlea_toNotGuilty_WithAllocationDecision() throws Throwable {

        final IndicatedPleaValue indicatedPleaValue = INDICATED_GUILTY;
        final LocalDate indicatedPleaDate = PAST_LOCAL_DATE.next();
        final UUID motReasonId = randomUUID();
        final String motReasonDescription = STRING.next();

        when(this.eventSource.getStreamById(hearing.getFirstOffenceForFirstDefendantForFirstCase().getId())).thenReturn(this.offenceAggregateEventStream);
        when(this.aggregateService.get(this.offenceAggregateEventStream, OffenceAggregate.class)).thenReturn(new OffenceAggregate());

        final UpdateOffencePleaCommand command = UpdateOffencePleaCommand.
                updateOffencePleaCommand()
                .setHearingId(hearing.getHearingId())
                .setPleaModel(pleaModel()
                        .withOffenceId(hearing.getFirstOffenceForFirstDefendantForFirstCase().getId())
                        .withIndicatedPlea(getIndicatedPlea(indicatedPleaValue, indicatedPleaDate))
                        .withAllocationDecision(AllocationDecision.allocationDecision()
                                .withMotReasonId(motReasonId)
                                .withMotReasonDescription(motReasonDescription)
                                .build())
                        .build());

        final JsonEnvelope jsonEnvelop = envelopeFrom(metadataWithRandomUUID("hearing.offence-plea-updated"),
                objectToJsonObjectConverter.convert(command));

        this.hearingCommandHandler.updateOffencePlea(jsonEnvelop);

        final List<JsonEnvelope> events = verifyAppendAndGetArgumentFrom(this.offenceAggregateEventStream).collect(Collectors.toList());

        final JsonEnvelope jsonEnvelope = events.get(0);

        assertThat(jsonEnvelope.metadata().name(), is("hearing.offence-plea-updated"));

        PleaUpsert pleaUpsert = asPojo(events.get(0), PleaUpsert.class);

        assertThat(pleaUpsert, isBean(PleaUpsert.class)
                .with(PleaUpsert::getHearingId, is(hearing.getHearingId())));

        final IndicatedPlea indicatedPlea = pleaUpsert.getPleaModel().getIndicatedPlea();
        assertThat(indicatedPlea, isBean(IndicatedPlea.class)
                .with(IndicatedPlea::getIndicatedPleaDate, is(indicatedPleaDate))
                .with(IndicatedPlea::getIndicatedPleaValue, is(indicatedPleaValue)));

        assertThat(pleaUpsert.getPleaModel().getAllocationDecision(), isBean(AllocationDecision.class)
                .with(AllocationDecision::getMotReasonId, is(motReasonId))
                .with(AllocationDecision::getMotReasonDescription, is(motReasonDescription))
        );
        assertThat(pleaUpsert.getPleaModel().getPlea(), is(nullValue()));
    }

    @Test
    public void testIndicatedPlea_WithAllocationDecision() throws Throwable {

        when(this.eventSource.getStreamById(hearing.getFirstOffenceForFirstDefendantForFirstCase().getId())).thenReturn(this.offenceAggregateEventStream);
        when(this.aggregateService.get(this.offenceAggregateEventStream, OffenceAggregate.class)).thenReturn(new OffenceAggregate());

        final UpdateOffencePleaCommand command = UpdateOffencePleaCommand.
                updateOffencePleaCommand()
                .setHearingId(hearing.getHearingId())
                .setPleaModel(pleaModel()
                        .withOffenceId(hearing.getFirstOffenceForFirstDefendantForFirstCase().getId())
                        .withAllocationDecision(AllocationDecision.allocationDecision()
                                .build())
                        .build());

        final JsonEnvelope jsonEnvelop = envelopeFrom(metadataWithRandomUUID("hearing.offence-plea-updated"),
                objectToJsonObjectConverter.convert(command));

        this.hearingCommandHandler.updateOffencePlea(jsonEnvelop);

        final List<JsonEnvelope> events = verifyAppendAndGetArgumentFrom(this.offenceAggregateEventStream).collect(Collectors.toList());

        final JsonEnvelope jsonEnvelope = events.get(0);

        assertThat(jsonEnvelope.metadata().name(), is("hearing.offence-plea-updated"));

        PleaUpsert pleaUpsert = asPojo(events.get(0), PleaUpsert.class);

        assertThat(pleaUpsert, isBean(PleaUpsert.class)
                .with(PleaUpsert::getHearingId, is(hearing.getHearingId())));

        assertThat(pleaUpsert.getPleaModel().getAllocationDecision(), isBean(AllocationDecision.class)
        );
        assertThat(pleaUpsert.getPleaModel().getPlea(), is(nullValue()));
        assertThat(pleaUpsert.getPleaModel().getIndicatedPlea(), is(nullValue()));
    }

    @Test
    public void testOffenceAggregateUpdatePleaToGuilty() throws Throwable {

        final String pleaValue = NOT_GUILTY;

        final LocalDate pleaDate = PAST_LOCAL_DATE.next();

        when(this.eventSource.getStreamById(hearing.getFirstOffenceForFirstDefendantForFirstCase().getId())).thenReturn(this.offenceAggregateEventStream);

        when(this.aggregateService.get(this.offenceAggregateEventStream, OffenceAggregate.class)).thenReturn(new OffenceAggregate());

        final UpdateOffencePleaCommand command = UpdateOffencePleaCommand.
                updateOffencePleaCommand()
                .setHearingId(hearing.getHearingId())
                .setPleaModel(pleaModel()
                        .withOffenceId(hearing.getFirstOffenceForFirstDefendantForFirstCase().getId())
                        .withPlea(Plea.plea()
                                .withOffenceId(hearing.getFirstOffenceForFirstDefendantForFirstCase().getId())
                                .withPleaDate(pleaDate)
                                .withPleaValue(pleaValue)
                                .build()).build());

        final JsonEnvelope jsonEnvelop = envelopeFrom(metadataWithRandomUUID("hearing.offence-plea-updated"),
                objectToJsonObjectConverter.convert(command));

        this.hearingCommandHandler.updateOffencePlea(jsonEnvelop);

        final List<JsonEnvelope> events = verifyAppendAndGetArgumentFrom(this.offenceAggregateEventStream).collect(Collectors.toList());

        PleaUpsert pleaUpsert = asPojo(events.get(0), PleaUpsert.class);

        assertThat(pleaUpsert, isBean(PleaUpsert.class)
                .with(PleaUpsert::getHearingId, is(hearing.getHearingId())));

        assertThat(pleaUpsert.getPleaModel().getPlea(), isBean(Plea.class)
                .with(Plea::getOffenceId, is(hearing.getFirstOffenceForFirstDefendantForFirstCase().getId()))
                .with(Plea::getPleaDate, is(pleaDate))
                .with(Plea::getPleaValue, is(pleaValue)));

        assertThat(pleaUpsert.getPleaModel().getIndicatedPlea(), is(nullValue()));
        assertThat(pleaUpsert.getPleaModel().getAllocationDecision(), is(nullValue()));

    }

    private Plea getPlea(final LocalDate pleaDate, final String pleaValue, final DelegatedPowers delegatedPowers) {
        return Plea.plea()
                .withOffenceId(hearing.getFirstOffenceForFirstDefendantForFirstCase().getId())
                .withOriginatingHearingId(hearing.getHearingId())
                .withPleaDate(pleaDate)
                .withDelegatedPowers(delegatedPowers)
                .withPleaValue(pleaValue)
                .withLesserOrAlternativeOffence(getLesserOrAlternativeOffence())
                .build();
    }

    private LesserOrAlternativeOffence getLesserOrAlternativeOffence() {
        return
                LesserOrAlternativeOffence.lesserOrAlternativeOffence()
                        .withOffenceCode("MH04001")
                        .withOffenceDefinitionId(UUID.fromString("33ebe963-26d6-41bd-b0c6-a1c1fe1b8a81"))
                        .withOffenceLegislation("Contrary to regulations 12(1)(a), 49(1)(aa) and 52 of the Medicines for Human Use (Clinical Trials) Regulations 2004")
                        .withOffenceLegislationWelsh("legislationWelsh")
                        .withOffenceTitle("Start / cause to be started a clinical trial without authority")
                        .withOffenceTitleWelsh("titleWelsh")
                        .build();
    }

    private IndicatedPlea getIndicatedPlea(final IndicatedPleaValue indicatedPleaValue, final LocalDate pleaDate) {
        return IndicatedPlea.indicatedPlea()
                .withOffenceId(hearing.getFirstOffenceForFirstDefendantForFirstCase().getId())
                .withOriginatingHearingId(hearing.getHearingId())
                .withIndicatedPleaDate(pleaDate)
                .withIndicatedPleaValue(indicatedPleaValue)
                .withSource(Source.IN_COURT)
                .build();
    }

    private AllocationDecision getAllocationDecission(final LocalDate decissionDate) {
        return AllocationDecision.allocationDecision()
                .withOffenceId(hearing.getFirstOffenceForFirstDefendantForFirstCase().getId())
                .withOriginatingHearingId(hearing.getHearingId())
                .withAllocationDecisionDate(decissionDate)
                .withMotReasonCode("7")
                .withMotReasonDescription("Reason")
                .build();
    }

    private List<PleaModel> getPleaModel(final Plea plea, final IndicatedPlea indicatedPlea, final AllocationDecision allocationDecision) {
        return asList(pleaModel()
                .withOffenceId(hearing.getFirstOffenceForFirstDefendantForFirstCase().getId())
                .withProsecutionCaseId(randomUUID())
                .withDefendantId(randomUUID())
                .withPlea(plea)
                .withIndicatedPlea(indicatedPlea)
                .withAllocationDecision(allocationDecision)
        );
    }

    private Set<String> createGuiltyPleaTypes() {
        Set<String> guiltyPleaTypes = new HashSet<>();
        guiltyPleaTypes.add(GUILTY);
        return guiltyPleaTypes;
    }
}