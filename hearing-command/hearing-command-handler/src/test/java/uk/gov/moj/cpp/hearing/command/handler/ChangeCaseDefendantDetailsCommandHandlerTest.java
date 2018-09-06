package uk.gov.moj.cpp.hearing.command.handler;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.withMetadataEnvelopedFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeStreamMatcher.streamContaining;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.justice.json.schemas.core.Gender;
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
import uk.gov.justice.services.test.utils.core.random.RandomGenerator;
import uk.gov.moj.cpp.hearing.command.defendant.Address;
import uk.gov.moj.cpp.hearing.command.defendant.Defendant;
import uk.gov.moj.cpp.hearing.command.defendant.Interpreter;
import uk.gov.moj.cpp.hearing.command.defendant.Person;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.domain.aggregate.DefendantAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.CaseDefendantDetailsWithHearings;
import uk.gov.moj.cpp.hearing.domain.event.DefendantDetailsUpdated;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;

import java.util.Collections;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ChangeCaseDefendantDetailsCommandHandlerTest {

    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(
            DefendantDetailsUpdated.class,
            CaseDefendantDetailsWithHearings.class);

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
    private ChangeCaseDefendantDetailsCommandHandler changeDefendantDetailsCommandHandler;

    @Before
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testInitiateCaseDefendantChanged() throws Exception {

        CaseDefendantDetails caseDefendantChanged = CaseDefendantDetails.caseDefendantDetails()
                .setCaseId(randomUUID())
                .setDefendants(Collections.singletonList(Defendant.defendant()
                        .setId(randomUUID())
                        .setPerson(Person.person().setId(randomUUID())
                                .setFirstName(STRING.next())
                                .setLastName(STRING.next())
                                .setNationality(STRING.next())
                                .setGender(RandomGenerator.values(Gender.values()).next())
                                .setAddress(Address.address()
                                        .setAddress1(STRING.next())
                                        .setAddress2(STRING.next())
                                        .setAddress3(STRING.next())
                                        .setAddress4(STRING.next())
                                        .setPostCode(STRING.next())
                                )
                                .setDateOfBirth(PAST_LOCAL_DATE.next()))
                        .setBailStatus(STRING.next())
                        .setCustodyTimeLimitDate(PAST_LOCAL_DATE.next())
                        .setDefenceOrganisation(STRING.next())
                        .setInterpreter(Interpreter.interpreter().setLanguage(STRING.next()))
                ));


        setupMockedEventStream(caseDefendantChanged.getDefendants().get(0).getId(), this.eventStream, new DefendantAggregate());

        final JsonEnvelope command = envelopeFrom(metadataWithRandomUUID("hearing.update-case-defendant-details"), objectToJsonObjectConverter.convert(caseDefendantChanged));


        changeDefendantDetailsCommandHandler.initiateCaseDefendantDetailsChange(command);

        assertThat(verifyAppendAndGetArgumentFrom(this.eventStream), streamContaining(
                jsonEnvelope(withMetadataEnvelopedFrom(command).withName("hearing.update-case-defendant-details-enriched-with-hearing-ids"),
                        payloadIsJson(allOf(withJsonPath("$.caseId", is(caseDefendantChanged.getCaseId().toString())))))));


    }

    @SuppressWarnings("unchecked")
    @Test
    public void testUpdateCaseDefendantDetails() throws EventStreamException {

        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();

        final HearingAggregate hearingAggregate = new HearingAggregate();

        hearingAggregate.apply(new HearingInitiated(initiateHearingCommand.getHearing()));

        CaseDefendantDetailsWithHearings caseDefendantDetailsWithHearingsEvent = CaseDefendantDetailsWithHearings.caseDefendantDetailsWithHearings()
                .setCaseId(randomUUID())
                .setDefendant(
                        Defendant.defendant()
                                .setId(randomUUID())
                                .setPerson(Person.person().setId(randomUUID())
                                        .setFirstName(STRING.next())
                                        .setLastName(STRING.next())
                                        .setNationality(STRING.next())
                                        .setGender(RandomGenerator.values(Gender.values()).next())
                                        .setAddress(Address.address()
                                                .setAddress1(STRING.next())
                                                .setAddress2(STRING.next())
                                                .setAddress3(STRING.next())
                                                .setAddress4(STRING.next())
                                                .setPostCode(STRING.next())
                                        )
                                        .setDateOfBirth(PAST_LOCAL_DATE.next()))
                                .setBailStatus(STRING.next())
                                .setCustodyTimeLimitDate(PAST_LOCAL_DATE.next())
                                .setDefenceOrganisation(STRING.next())
                                .setInterpreter(Interpreter.interpreter().setLanguage(STRING.next())))
                .setHearingIds(Collections.singletonList(randomUUID()));

        setupMockedEventStream(caseDefendantDetailsWithHearingsEvent.getHearingIds().get(0), this.eventStream, hearingAggregate);

        final JsonEnvelope envelope = envelopeFrom(metadataWithRandomUUID("hearing.update-case-defendant-details-against-hearing-aggregate"),
                objectToJsonObjectConverter.convert(caseDefendantDetailsWithHearingsEvent));

        changeDefendantDetailsCommandHandler.updateCaseDefendantDetails(envelope);

        assertThat(verifyAppendAndGetArgumentFrom(this.eventStream), streamContaining(
                jsonEnvelope(withMetadataEnvelopedFrom(envelope).withName("hearing.defendant-details-updated"),
                        payloadIsJson(allOf(withJsonPath("$.caseId", is(caseDefendantDetailsWithHearingsEvent.getCaseId().toString())))))));
    }

    @SuppressWarnings("unchecked")
    private <T extends Aggregate> void setupMockedEventStream(UUID id, EventStream eventStream, T aggregate) {
        when(this.eventSource.getStreamById(id)).thenReturn(eventStream);
        Class<T> clz = (Class<T>) aggregate.getClass();
        when(this.aggregateService.get(eventStream, clz)).thenReturn(aggregate);
    }
}