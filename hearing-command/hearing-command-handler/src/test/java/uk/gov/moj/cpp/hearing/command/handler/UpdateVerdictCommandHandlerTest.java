package uk.gov.moj.cpp.hearing.command.handler;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.moj.cpp.hearing.test.ObjectConverters.asPojo;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.initiateHearingTemplateForMagistrates;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.verdictTemplate;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.justice.json.schemas.core.Jurors;
import uk.gov.justice.json.schemas.core.LesserOrAlternativeOffence;
import uk.gov.justice.json.schemas.core.Verdict;
import uk.gov.justice.json.schemas.core.VerdictType;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.verdict.HearingUpdateVerdictCommand;
import uk.gov.moj.cpp.hearing.command.verdict.UpdateInheritedVerdictCommand;
import uk.gov.moj.cpp.hearing.command.verdict.UpdateOffenceVerdictCommand;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.OffenceAggregate;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateAdded;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateRemoved;
import uk.gov.moj.cpp.hearing.domain.event.EnrichUpdateVerdictWithAssociatedHearings;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.domain.event.InheritedVerdictAdded;
import uk.gov.moj.cpp.hearing.domain.event.OffenceVerdictUpdated;
import uk.gov.moj.cpp.hearing.domain.event.VerdictUpsert;
import uk.gov.moj.cpp.hearing.test.TestTemplates;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@RunWith(MockitoJUnitRunner.class)
public class UpdateVerdictCommandHandlerTest {

    @Mock
    private EventStream hearingEventStream;

    @Mock
    private EventStream offenceAggregateEventStream;

    @Mock
    private EventSource eventSource;

    @Mock
    private AggregateService aggregateService;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(
            HearingInitiated.class,
            VerdictUpsert.class,
            ConvictionDateAdded.class,
            ConvictionDateRemoved.class,
            OffenceVerdictUpdated.class,
            EnrichUpdateVerdictWithAssociatedHearings.class,
            InheritedVerdictAdded.class
    );

    @InjectMocks
    private UpdateVerdictCommandHandler hearingCommandHandler;

    @Before
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void updateVerdict_toGuilty() throws EventStreamException {

        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();

        final UUID hearingId = initiateHearingCommand.getHearing().getId();

        final UUID caseId = initiateHearingCommand.getHearing().getProsecutionCases().get(0).getId();

        final UUID offenceId = initiateHearingCommand.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getId();

        final HearingUpdateVerdictCommand hearingUpdateVerdictCommand =
                TestTemplates.UpdateVerdictCommandTemplates.updateVerdictTemplate(
                        hearingId, offenceId, TestTemplates.VerdictCategoryType.GUILTY);

        final HearingAggregate hearingAggregate = new HearingAggregate();

        hearingAggregate.apply(new HearingInitiated(initiateHearingCommand.getHearing()));

        setupMockedEventStream(hearingUpdateVerdictCommand.getHearingId(), this.hearingEventStream, hearingAggregate);

        final JsonEnvelope addVerdictCommand = envelopeFrom(
                metadataWithRandomUUID("hearing.command.update-verdict"),
                objectToJsonObjectConverter.convert(hearingUpdateVerdictCommand));

        this.hearingCommandHandler.updateVerdict(addVerdictCommand);

        final List<?> events = verifyAppendAndGetArgumentFrom(this.hearingEventStream).collect(Collectors.toList());

        final Verdict verdict = hearingUpdateVerdictCommand.getVerdicts().get(0);

        assertThat(asPojo((JsonEnvelope) events.get(0), VerdictUpsert.class), isBean(VerdictUpsert.class)
                .with(VerdictUpsert::getHearingId, is(hearingUpdateVerdictCommand.getHearingId()))
                .with(VerdictUpsert::getVerdict, isBean(Verdict.class)
                        .with(Verdict::getOffenceId, is(verdict.getOffenceId()))
                        .with(Verdict::getOriginatingHearingId, is(hearingUpdateVerdictCommand.getHearingId()))
                        .with(Verdict::getVerdictDate, is(verdict.getVerdictDate()))
                        .with(Verdict::getVerdictType, isBean(VerdictType.class)
                                .with(VerdictType::getVerdictTypeId, is(verdict.getVerdictType().getVerdictTypeId()))
                                .with(VerdictType::getCategory, is(verdict.getVerdictType().getCategory()))
                                .with(VerdictType::getCategoryType, is(verdict.getVerdictType().getCategoryType()))
                                .with(VerdictType::getDescription, is(verdict.getVerdictType().getDescription()))
                                .with(VerdictType::getSequence, is(verdict.getVerdictType().getSequence())))
                        .with(Verdict::getLesserOrAlternativeOffence, isBean(LesserOrAlternativeOffence.class)
                                .with(LesserOrAlternativeOffence::getOffenceCode, is(verdict.getLesserOrAlternativeOffence().getOffenceCode()))
                                .with(LesserOrAlternativeOffence::getOffenceTitle, is(verdict.getLesserOrAlternativeOffence().getOffenceTitle()))
                                .with(LesserOrAlternativeOffence::getOffenceLegislation, is(verdict.getLesserOrAlternativeOffence().getOffenceLegislation()))
                                .with(LesserOrAlternativeOffence::getOffenceDefinitionId, is(verdict.getLesserOrAlternativeOffence().getOffenceDefinitionId()))
                                .with(LesserOrAlternativeOffence::getOffenceTitleWelsh, is(verdict.getLesserOrAlternativeOffence().getOffenceTitleWelsh()))
                                .with(LesserOrAlternativeOffence::getOffenceLegislationWelsh, is(verdict.getLesserOrAlternativeOffence().getOffenceLegislationWelsh())))
                        .with(Verdict::getJurors, isBean(Jurors.class)
                                .with(Jurors::getNumberOfJurors, is(verdict.getJurors().getNumberOfJurors()))
                                .with(Jurors::getNumberOfSplitJurors, is(verdict.getJurors().getNumberOfSplitJurors()))
                                .with(Jurors::getUnanimous, is(verdict.getJurors().getUnanimous())))));

        assertThat(asPojo((JsonEnvelope) events.get(1), ConvictionDateAdded.class), isBean(ConvictionDateAdded.class)
                .with(ConvictionDateAdded::getCaseId, is(caseId))
                .with(ConvictionDateAdded::getOffenceId, is(offenceId))
                .with(ConvictionDateAdded::getConvictionDate, is(verdict.getVerdictDate())));
    }

    @Test
    public void updateVerdict_toNotGuilty() throws EventStreamException {

        final InitiateHearingCommand initiateHearingCommand = initiateHearingTemplateForMagistrates();

        final UUID hearingId = initiateHearingCommand.getHearing().getId();

        final UUID caseId = initiateHearingCommand.getHearing().getProsecutionCases().get(0).getId();

        final UUID offenceId = initiateHearingCommand.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getId();

        final HearingUpdateVerdictCommand hearingUpdateVerdictCommand =
                TestTemplates.UpdateVerdictCommandTemplates.updateVerdictTemplate(
                        hearingId, offenceId, TestTemplates.VerdictCategoryType.NOT_GUILTY);

        final HearingAggregate hearingAggregate = new HearingAggregate();

        hearingAggregate.apply(new HearingInitiated(initiateHearingCommand.getHearing()));

        setupMockedEventStream(hearingUpdateVerdictCommand.getHearingId(), this.hearingEventStream, hearingAggregate);

        final JsonEnvelope addVerdictCommand = envelopeFrom(metadataWithRandomUUID("hearing.command.update-verdict"),
                objectToJsonObjectConverter.convert(hearingUpdateVerdictCommand));

        this.hearingCommandHandler.updateVerdict(addVerdictCommand);

        final List<?> events = verifyAppendAndGetArgumentFrom(this.hearingEventStream).collect(Collectors.toList());

        final Verdict verdict = hearingUpdateVerdictCommand.getVerdicts().get(0);

        assertThat(asPojo((JsonEnvelope) events.get(0), VerdictUpsert.class), isBean(VerdictUpsert.class)
                .with(VerdictUpsert::getHearingId, is(hearingUpdateVerdictCommand.getHearingId()))
                .with(VerdictUpsert::getVerdict, isBean(Verdict.class)
                        .with(Verdict::getOffenceId, is(verdict.getOffenceId()))
                        .with(Verdict::getOriginatingHearingId, is(hearingUpdateVerdictCommand.getHearingId()))
                        .with(Verdict::getVerdictDate, is(verdict.getVerdictDate()))
                        .with(Verdict::getVerdictType, isBean(VerdictType.class)
                                .with(VerdictType::getVerdictTypeId, is(verdict.getVerdictType().getVerdictTypeId()))
                                .with(VerdictType::getCategory, is(verdict.getVerdictType().getCategory()))
                                .with(VerdictType::getCategoryType, is(verdict.getVerdictType().getCategoryType()))
                                .with(VerdictType::getDescription, is(verdict.getVerdictType().getDescription()))
                                .with(VerdictType::getSequence, is(verdict.getVerdictType().getSequence())))
                        .with(Verdict::getLesserOrAlternativeOffence, isBean(LesserOrAlternativeOffence.class)
                                .with(LesserOrAlternativeOffence::getOffenceCode, is(verdict.getLesserOrAlternativeOffence().getOffenceCode()))
                                .with(LesserOrAlternativeOffence::getOffenceTitle, is(verdict.getLesserOrAlternativeOffence().getOffenceTitle()))
                                .with(LesserOrAlternativeOffence::getOffenceLegislation, is(verdict.getLesserOrAlternativeOffence().getOffenceLegislation()))
                                .with(LesserOrAlternativeOffence::getOffenceDefinitionId, is(verdict.getLesserOrAlternativeOffence().getOffenceDefinitionId()))
                                .with(LesserOrAlternativeOffence::getOffenceTitleWelsh, is(verdict.getLesserOrAlternativeOffence().getOffenceTitleWelsh()))
                                .with(LesserOrAlternativeOffence::getOffenceLegislationWelsh, is(verdict.getLesserOrAlternativeOffence().getOffenceLegislationWelsh())))
                        .with(Verdict::getJurors, isBean(Jurors.class)
                                .with(Jurors::getNumberOfJurors, is(verdict.getJurors().getNumberOfJurors()))
                                .with(Jurors::getNumberOfSplitJurors, is(verdict.getJurors().getNumberOfSplitJurors()))
                                .with(Jurors::getUnanimous, is(verdict.getJurors().getUnanimous())))));

        assertThat(asPojo((JsonEnvelope) events.get(1), ConvictionDateRemoved.class), isBean(ConvictionDateRemoved.class)
                .with(ConvictionDateRemoved::getCaseId, is(caseId))
                .with(ConvictionDateRemoved::getHearingId, is(hearingId))
                .with(ConvictionDateRemoved::getOffenceId, is(offenceId)));
    }

    @Test
    public void updateOffenceVerdict() throws EventStreamException {

        final UpdateOffenceVerdictCommand command = new UpdateOffenceVerdictCommand();
        command.setHearingId(randomUUID());
        command.setVerdict(verdictTemplate(randomUUID(), TestTemplates.VerdictCategoryType.GUILTY));

        final OffenceAggregate aggregate = new OffenceAggregate();
        aggregate.getHearingIds().add(randomUUID());

        setupMockedEventStream(command.getVerdict().getOffenceId(), this.offenceAggregateEventStream, aggregate);

        final JsonEnvelope updateOffenceVerdictCommand = envelopeFrom(metadataWithRandomUUID("hearing.command.update-verdict-against-offence"),
                objectToJsonObjectConverter.convert(command));

        this.hearingCommandHandler.updateOffenceVerdict(updateOffenceVerdictCommand);

        final List<?> events = verifyAppendAndGetArgumentFrom(this.offenceAggregateEventStream).collect(Collectors.toList());

        assertThat(((JsonEnvelope) events.get(0)).metadata().name(), is("hearing.offence-verdict-updated"));

        assertThat(((JsonEnvelope) events.get(1)).metadata().name(), is("hearing.events.enrich-update-verdict-with-associated-hearings"));

    }

    @Test
    public void updateInheritVerdict() throws EventStreamException {

        final UUID hearingId = randomUUID();

        final UpdateInheritedVerdictCommand command = new UpdateInheritedVerdictCommand();
        command.setHearingIds(asList(hearingId));
        command.setVerdict(verdictTemplate(randomUUID(), TestTemplates.VerdictCategoryType.GUILTY));

        setupMockedEventStream(hearingId, this.hearingEventStream, new HearingAggregate());

        final JsonEnvelope envelope = envelopeFrom(metadataWithRandomUUID("hearing.command.enrich-update-verdict-with-associated-hearings"),
                objectToJsonObjectConverter.convert(command));

        this.hearingCommandHandler.updateInheritVerdict(envelope);

        final List<?> events = verifyAppendAndGetArgumentFrom(this.hearingEventStream).collect(Collectors.toList());

        assertThat(((JsonEnvelope) events.get(0)).metadata().name(), is("hearing.events.inherited-verdict-added"));
    }

    private <T extends Aggregate> void setupMockedEventStream(UUID id, EventStream eventStream, T aggregate) {
        setupMockedEventStream(id, eventStream, aggregate, a -> {
        });
    }

    @SuppressWarnings("unchecked")
    private <T extends Aggregate> void setupMockedEventStream(UUID id, EventStream eventStream, T aggregate, Consumer<T> consumer) {
        consumer.accept(aggregate);
        when(this.eventSource.getStreamById(id)).thenReturn(eventStream);
        Class<T> clz = (Class<T>) aggregate.getClass();
        when(this.aggregateService.get(eventStream, clz)).thenReturn(aggregate);
    }

    public static <T> T with(T object, Consumer<T> consumer) {
        consumer.accept(object);
        return object;
    }

}
