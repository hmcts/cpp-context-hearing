package uk.gov.moj.cpp.hearing.command.handler;

import static java.util.UUID.randomUUID;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;

import uk.gov.justice.core.courts.Address;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.CpsProsecutorUpdated;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.json.JsonObject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProsecutionCaseCommandHandlerTest {

    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(
            CpsProsecutorUpdated.class
    );

    @InjectMocks
    private ProsecutionCaseCommandHandler prosecutionCaseCommandHandler;

    @Mock
    private EventStream firstEventStream;

    @Mock
    private EventStream secondEventStream;

    @Mock
    private EventSource eventSource;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Mock
    private AggregateService aggregateService;

    @Before
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void shouldNotUpdateProsecutorWhenHearingNotAllocated() throws EventStreamException {

        final UUID hearingId = randomUUID();
        final UUID prosecutionCaseId = randomUUID();
        final UUID prosecutionAuthorityId = randomUUID();
        final String prosecutionAuthorityReference = "test prosecutionAuthorityReference";
        final String prosecutionAuthorityName = "test prosecutionAuthorityName";
        final String prosecutionAuthorityCode = "test prosecutionAuthorityCode";
        final String caseURN = "testCaseURN";
        final Address address = Address.address().withAddress1("40 Manhattan House").withPostcode("MK9 2BQ").build();

        final JsonObject payload = createObjectBuilder()
                .add("prosecutionCaseId", prosecutionCaseId.toString())
                .add("hearingIds", createArrayBuilder()
                        .add(hearingId.toString())
                        .build())
                .add("prosecutionAuthorityId", prosecutionAuthorityId.toString())
                .add("prosecutionAuthorityReference", prosecutionAuthorityReference)
                .add("prosecutionAuthorityName", prosecutionAuthorityName)
                .add("prosecutionAuthorityCode", prosecutionAuthorityCode)
                .add("caseURN", caseURN)
                .add("address", objectToJsonObjectConverter.convert(address))
                .build();

        final JsonEnvelope envelope = envelopeFrom(metadataWithRandomUUID("hearing.command.update-cps-prosecutor-with-associated-hearings"), payload);

        when(eventSource.getStreamById(hearingId)).thenReturn(firstEventStream);
        when(aggregateService.get(eq(firstEventStream), any()))
                .thenReturn(new HearingAggregate());

        prosecutionCaseCommandHandler.updateProsecutorForAssociatedHearings(envelope);

        List<JsonEnvelope> actualEventsProduced = verifyAppendAndGetArgumentFrom(firstEventStream).collect(Collectors.toList());

        assertThat(actualEventsProduced.isEmpty(), is(true));
    }

    @Test
    public void shouldUpdateProsecutorForOneHearing() throws EventStreamException {

        final UUID hearingId = randomUUID();
        final UUID prosecutionCaseId = randomUUID();
        final UUID prosecutionAuthorityId = randomUUID();
        final String prosecutionAuthorityReference = "test prosecutionAuthorityReference";
        final String prosecutionAuthorityName = "test prosecutionAuthorityName";
        final String prosecutionAuthorityCode = "test prosecutionAuthorityCode";
        final String caseURN = "testCaseURN";
        final Address address = Address.address().withAddress1("40 Manhattan House").withPostcode("MK9 2BQ").build();

        final JsonObject payload = createObjectBuilder()
                .add("prosecutionCaseId", prosecutionCaseId.toString())
                .add("hearingIds", createArrayBuilder()
                        .add(hearingId.toString())
                        .build())
                .add("prosecutionAuthorityId", prosecutionAuthorityId.toString())
                .add("prosecutionAuthorityReference", prosecutionAuthorityReference)
                .add("prosecutionAuthorityName", prosecutionAuthorityName)
                .add("prosecutionAuthorityCode", prosecutionAuthorityCode)
                .add("caseURN", caseURN)
                .add("address", objectToJsonObjectConverter.convert(address))
                .build();

        final JsonEnvelope envelope = envelopeFrom(metadataWithRandomUUID("hearing.command.update-cps-prosecutor-with-associated-hearings"), payload);

        when(eventSource.getStreamById(hearingId)).thenReturn(firstEventStream);

        final HearingAggregate hearingAggregate = new HearingAggregate();
        when(aggregateService.get(eq(firstEventStream), any()))
                .thenReturn(hearingAggregate);
        CommandHelpers.InitiateHearingCommandHelper hearing = CommandHelpers.h(standardInitiateHearingTemplate());
        hearingAggregate.apply(new HearingInitiated(hearing.getHearing()));

        prosecutionCaseCommandHandler.updateProsecutorForAssociatedHearings(envelope);

        JsonEnvelope actualEventProduced = verifyAppendAndGetArgumentFrom(firstEventStream).collect(Collectors.toList()).get(0);

        assertThat(actualEventProduced.metadata().name(), is("hearing.cps-prosecutor-updated"));
        assertThat(actualEventProduced.asJsonObject().getString("prosecutionCaseId"), is(prosecutionCaseId.toString()));
        assertThat(actualEventProduced.asJsonObject().getString("hearingId"), is(hearingId.toString()));
        assertThat(actualEventProduced.asJsonObject().getString("prosecutionAuthorityId"), is(prosecutionAuthorityId.toString()));
        assertThat(actualEventProduced.asJsonObject().getString("prosecutionAuthorityReference"), is(prosecutionAuthorityReference));
        assertThat(actualEventProduced.asJsonObject().getString("prosecutionAuthorityName"), is(prosecutionAuthorityName));
        assertThat(actualEventProduced.asJsonObject().getString("prosecutionAuthorityCode"), is(prosecutionAuthorityCode));
        assertThat(actualEventProduced.asJsonObject().getString("caseURN"), is(caseURN));
        assertThat(actualEventProduced.asJsonObject().getJsonObject("address").getString("address1"), is(address.getAddress1()));
        assertThat(actualEventProduced.asJsonObject().getJsonObject("address").getString("postcode"), is(address.getPostcode()));
    }

    @Test
    public void shouldUpdateProsecutorForTwoHearings() throws EventStreamException {

        final UUID firstHearingId = randomUUID();
        final UUID secondHearingId = randomUUID();
        final UUID prosecutionCaseId = randomUUID();
        final UUID prosecutionAuthorityId = randomUUID();
        final String prosecutionAuthorityReference = "test prosecutionAuthorityReference";
        final String prosecutionAuthorityName = "test prosecutionAuthorityName";
        final String prosecutionAuthorityCode = "test prosecutionAuthorityCode";
        final String caseURN = "testCaseURN";
        final Address address = Address.address().withAddress1("40 Manhattan House").withPostcode("MK9 2BQ").build();

        final JsonObject payload = createObjectBuilder()
                .add("prosecutionCaseId", prosecutionCaseId.toString())
                .add("hearingIds", createArrayBuilder()
                        .add(firstHearingId.toString())
                        .add(secondHearingId.toString())
                        .build())
                .add("prosecutionAuthorityId", prosecutionAuthorityId.toString())
                .add("prosecutionAuthorityReference", prosecutionAuthorityReference)
                .add("prosecutionAuthorityName", prosecutionAuthorityName)
                .add("prosecutionAuthorityCode", prosecutionAuthorityCode)
                .add("caseURN", caseURN)
                .add("address", objectToJsonObjectConverter.convert(address))
                .build();

        final JsonEnvelope envelope = envelopeFrom(metadataWithRandomUUID("hearing.command.update-cps-prosecutor-with-associated-hearings"), payload);

        when(eventSource.getStreamById(firstHearingId)).thenReturn(firstEventStream);
        when(eventSource.getStreamById(secondHearingId)).thenReturn(secondEventStream);

        final HearingAggregate hearingAggregate1 = new HearingAggregate();
        when(aggregateService.get(eq(firstEventStream), any()))
                .thenReturn(hearingAggregate1);
        CommandHelpers.InitiateHearingCommandHelper hearing1 = CommandHelpers.h(standardInitiateHearingTemplate());
        hearingAggregate1.apply(new HearingInitiated(hearing1.getHearing()));

        final HearingAggregate hearingAggregate2 = new HearingAggregate();
        when(aggregateService.get(eq(secondEventStream), any()))
                .thenReturn(hearingAggregate2);
        CommandHelpers.InitiateHearingCommandHelper hearing2 = CommandHelpers.h(standardInitiateHearingTemplate());
        hearingAggregate2.apply(new HearingInitiated(hearing2.getHearing()));

        prosecutionCaseCommandHandler.updateProsecutorForAssociatedHearings(envelope);

        JsonEnvelope actualEventProduced = verifyAppendAndGetArgumentFrom(firstEventStream).collect(Collectors.toList()).get(0);
        JsonEnvelope actualEventProduced2 = verifyAppendAndGetArgumentFrom(secondEventStream).collect(Collectors.toList()).get(0);

        assertThat(actualEventProduced.metadata().name(), is("hearing.cps-prosecutor-updated"));
        assertThat(actualEventProduced.asJsonObject().getString("prosecutionCaseId"), is(prosecutionCaseId.toString()));
        assertThat(actualEventProduced.asJsonObject().getString("hearingId"), is(firstHearingId.toString()));
        assertThat(actualEventProduced.asJsonObject().getString("prosecutionAuthorityId"), is(prosecutionAuthorityId.toString()));
        assertThat(actualEventProduced.asJsonObject().getString("prosecutionAuthorityReference"), is(prosecutionAuthorityReference));
        assertThat(actualEventProduced.asJsonObject().getString("prosecutionAuthorityName"), is(prosecutionAuthorityName));
        assertThat(actualEventProduced.asJsonObject().getString("prosecutionAuthorityCode"), is(prosecutionAuthorityCode));
        assertThat(actualEventProduced.asJsonObject().getString("caseURN"), is(caseURN));
        assertThat(actualEventProduced.asJsonObject().getJsonObject("address").getString("address1"), is(address.getAddress1()));
        assertThat(actualEventProduced.asJsonObject().getJsonObject("address").getString("postcode"), is(address.getPostcode()));

        assertThat(actualEventProduced2.metadata().name(), is("hearing.cps-prosecutor-updated"));
        assertThat(actualEventProduced2.asJsonObject().getString("prosecutionCaseId"), is(prosecutionCaseId.toString()));
        assertThat(actualEventProduced2.asJsonObject().getString("hearingId"), is(secondHearingId.toString()));
        assertThat(actualEventProduced2.asJsonObject().getString("prosecutionAuthorityId"), is(prosecutionAuthorityId.toString()));
        assertThat(actualEventProduced2.asJsonObject().getString("prosecutionAuthorityReference"), is(prosecutionAuthorityReference));
        assertThat(actualEventProduced2.asJsonObject().getString("prosecutionAuthorityName"), is(prosecutionAuthorityName));
        assertThat(actualEventProduced2.asJsonObject().getString("prosecutionAuthorityCode"), is(prosecutionAuthorityCode));
        assertThat(actualEventProduced2.asJsonObject().getString("caseURN"), is(caseURN));
        assertThat(actualEventProduced2.asJsonObject().getJsonObject("address").getString("address1"), is(address.getAddress1()));
        assertThat(actualEventProduced2.asJsonObject().getJsonObject("address").getString("postcode"), is(address.getPostcode()));
    }
}