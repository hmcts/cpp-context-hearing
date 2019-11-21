package uk.gov.moj.cpp.hearing.command.handler;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.withMetadataEnvelopedFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeStreamMatcher.streamContaining;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.defendantTemplate;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.justice.progression.events.CaseDefendantDetails;
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
import uk.gov.moj.cpp.hearing.domain.aggregate.DefendantAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.CaseDefendantDetailsWithHearings;
import uk.gov.moj.cpp.hearing.domain.event.DefendantDetailsUpdated;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.domain.event.RegisteredHearingAgainstDefendant;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UpdateDefendantCommandHandlerTest {

    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(
            DefendantDetailsUpdated.class,
            CaseDefendantDetailsWithHearings.class,
            RegisteredHearingAgainstDefendant.class);

    @Mock
    private EventStream eventStream;

    @Mock
    private EventSource eventSource;

    @Mock
    private AggregateService aggregateService;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @InjectMocks
    private UpdateDefendantCommandHandler changeDefendantDetailsCommandHandler;

    @Before
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testInitiateCaseDefendantChanged() throws EventStreamException {

        final CaseDefendantDetails caseDefendantChanged = CaseDefendantDetails.caseDefendantDetails()
                .setDefendants(singletonList(defendantTemplate()));

        setupMockedEventStream(caseDefendantChanged.getDefendants().get(0).getId(), this.eventStream, new DefendantAggregate());

        final JsonEnvelope envelope = envelopeFrom(metadataWithRandomUUID("hearing.update-case-defendant-details"), objectToJsonObjectConverter.convert(caseDefendantChanged));

        changeDefendantDetailsCommandHandler.initiateCaseDefendantDetailsChange(envelope);

        assertThat(this.eventStream.size(), is(0L));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testUpdateCaseDefendantDetails() throws EventStreamException {

        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();

        final HearingAggregate hearingAggregate = new HearingAggregate();

        hearingAggregate.apply(new HearingInitiated(initiateHearingCommand.getHearing()));

        CaseDefendantDetailsWithHearings caseDefendantDetailsWithHearingsEvent =
                CaseDefendantDetailsWithHearings.caseDefendantDetailsWithHearings()
                        .setDefendant(defendantTemplate())
                        .setHearingIds(singletonList(randomUUID()));
        caseDefendantDetailsWithHearingsEvent.getDefendant().setId(initiateHearingCommand.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getId());
        setupMockedEventStream(caseDefendantDetailsWithHearingsEvent.getHearingIds().get(0), this.eventStream, hearingAggregate);

        final JsonEnvelope envelope = envelopeFrom(metadataWithRandomUUID("hearing.update-case-defendant-details-against-hearing-aggregate"),
                objectToJsonObjectConverter.convert(caseDefendantDetailsWithHearingsEvent));

        changeDefendantDetailsCommandHandler.updateCaseDefendantDetails(envelope);

        assertThat(verifyAppendAndGetArgumentFrom(this.eventStream), streamContaining(
                jsonEnvelope(withMetadataEnvelopedFrom(envelope).withName("hearing.defendant-details-updated"),
                        payloadIsJson(
                                allOf(
                                        withJsonPath("$.defendant.id", is(caseDefendantDetailsWithHearingsEvent.getDefendant().getId().toString())),
                                        withJsonPath("$.defendant.personDefendant.personDetails.title", is(caseDefendantDetailsWithHearingsEvent.getDefendant().getPersonDefendant().getPersonDetails().getTitle().toString()))
                                )
                        ))));

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testUpdateCaseDefendantDetails_When_Defendant_Person_Title_Not_Provided() throws EventStreamException {

        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();

        final HearingAggregate hearingAggregate = new HearingAggregate();

        hearingAggregate.apply(new HearingInitiated(initiateHearingCommand.getHearing()));

        CaseDefendantDetailsWithHearings caseDefendantDetailsWithHearingsEvent =
                CaseDefendantDetailsWithHearings.caseDefendantDetailsWithHearings()
                        .setDefendant(defendantTemplate())
                        .setHearingIds(singletonList(randomUUID()));
        caseDefendantDetailsWithHearingsEvent.getDefendant().setId(initiateHearingCommand.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getId());
        caseDefendantDetailsWithHearingsEvent.getDefendant().getPersonDefendant().getPersonDetails().setTitle(null);
        setupMockedEventStream(caseDefendantDetailsWithHearingsEvent.getHearingIds().get(0), this.eventStream, hearingAggregate);

        final JsonEnvelope envelope = envelopeFrom(metadataWithRandomUUID("hearing.update-case-defendant-details-against-hearing-aggregate"),
                objectToJsonObjectConverter.convert(caseDefendantDetailsWithHearingsEvent));

        changeDefendantDetailsCommandHandler.updateCaseDefendantDetails(envelope);


        assertThat(verifyAppendAndGetArgumentFrom(this.eventStream), streamContaining(
                jsonEnvelope(withMetadataEnvelopedFrom(envelope).withName("hearing.defendant-details-updated"),
                        payloadIsJson(
                                allOf(
                                        withJsonPath("$.defendant.id", is(caseDefendantDetailsWithHearingsEvent.getDefendant().getId().toString())),
                                        withJsonPath("$.defendant.personDefendant.personDetails.title", is(initiateHearingCommand.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getPersonDefendant().getPersonDetails().getTitle().toString()))
                                )
                        ))));
    }

    @SuppressWarnings("unchecked")
    private <T extends Aggregate> void setupMockedEventStream(UUID id, EventStream eventStream, T aggregate) {
        when(this.eventSource.getStreamById(id)).thenReturn(eventStream);
        Class<T> clz = (Class<T>) aggregate.getClass();
        when(this.aggregateService.get(eventStream, clz)).thenReturn(aggregate);
    }
}