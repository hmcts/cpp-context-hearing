package uk.gov.moj.cpp.hearing.command.handler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

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

import java.util.UUID;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonObject;

import org.hamcrest.Matchers;
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
    public void shouldUpdateProsecutorForOneHearing() throws EventStreamException {

        final UUID hearingId = UUID.randomUUID();
        final UUID prosecutionCaseId = UUID.randomUUID();
        final UUID prosecutionAuthorityId = UUID.randomUUID();
        final String prosecutionAuthorityReference = "test prosecutionAuthorityReference";
        final String prosecutionAuthorityName = "test prosecutionAuthorityName";
        final String prosecutionAuthorityCode = "test prosecutionAuthorityCode";
        final String caseURN = "testCaseURN";
        final Address address = Address.address().withAddress1("40 Manhattan House").withPostcode("MK9 2BQ").build();

        final JsonObject payload = Json.createObjectBuilder()
                .add("prosecutionCaseId", prosecutionCaseId.toString())
                .add("hearingIds", Json.createArrayBuilder()
                        .add(hearingId.toString())
                        .build())
                .add("prosecutionAuthorityId", prosecutionAuthorityId.toString())
                .add("prosecutionAuthorityReference", prosecutionAuthorityReference)
                .add("prosecutionAuthorityName", prosecutionAuthorityName)
                .add("prosecutionAuthorityCode", prosecutionAuthorityCode)
                .add("caseURN", caseURN)
                .add("address", objectToJsonObjectConverter.convert(address))
                .build();

        final JsonEnvelope envelope = JsonEnvelope.envelopeFrom(metadataWithRandomUUID("hearing.command.update-cps-prosecutor-with-associated-hearings"), payload);

        when(eventSource.getStreamById(hearingId)).thenReturn(firstEventStream);
        when(aggregateService.get(eq(firstEventStream), any()))
                .thenReturn(new HearingAggregate());

        prosecutionCaseCommandHandler.updateProsecutorForAssociatedHearings(envelope);

        JsonEnvelope actualEventProduced = verifyAppendAndGetArgumentFrom(firstEventStream).collect(Collectors.toList()).get(0);

        assertThat(actualEventProduced.metadata().name(), Matchers.is("hearing.cps-prosecutor-updated"));
        assertThat(actualEventProduced.asJsonObject().getString("prosecutionCaseId"), Matchers.is(prosecutionCaseId.toString()));
        assertThat(actualEventProduced.asJsonObject().getString("hearingId"), Matchers.is(hearingId.toString()));
        assertThat(actualEventProduced.asJsonObject().getString("prosecutionAuthorityId"), Matchers.is(prosecutionAuthorityId.toString()));
        assertThat(actualEventProduced.asJsonObject().getString("prosecutionAuthorityReference"), Matchers.is(prosecutionAuthorityReference));
        assertThat(actualEventProduced.asJsonObject().getString("prosecutionAuthorityName"), Matchers.is(prosecutionAuthorityName));
        assertThat(actualEventProduced.asJsonObject().getString("prosecutionAuthorityCode"), Matchers.is(prosecutionAuthorityCode));
        assertThat(actualEventProduced.asJsonObject().getString("caseURN"), Matchers.is(caseURN));
        assertThat(actualEventProduced.asJsonObject().getJsonObject("address").getString("address1"), Matchers.is(address.getAddress1()));
        assertThat(actualEventProduced.asJsonObject().getJsonObject("address").getString("postcode"), Matchers.is(address.getPostcode()));
    }

    @Test
    public void shouldUpdateProsecutorForTwoHearings() throws EventStreamException {

        final UUID firstHearingId = UUID.randomUUID();
        final UUID secondHearingId = UUID.randomUUID();
        final UUID prosecutionCaseId = UUID.randomUUID();
        final UUID prosecutionAuthorityId = UUID.randomUUID();
        final String prosecutionAuthorityReference = "test prosecutionAuthorityReference";
        final String prosecutionAuthorityName = "test prosecutionAuthorityName";
        final String prosecutionAuthorityCode = "test prosecutionAuthorityCode";
        final String caseURN = "testCaseURN";
        final Address address = Address.address().withAddress1("40 Manhattan House").withPostcode("MK9 2BQ").build();

        final JsonObject payload = Json.createObjectBuilder()
                .add("prosecutionCaseId", prosecutionCaseId.toString())
                .add("hearingIds", Json.createArrayBuilder()
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

        final JsonEnvelope envelope = JsonEnvelope.envelopeFrom(metadataWithRandomUUID("hearing.command.update-cps-prosecutor-with-associated-hearings"), payload);

        when(eventSource.getStreamById(firstHearingId)).thenReturn(firstEventStream);
        when(eventSource.getStreamById(secondHearingId)).thenReturn(secondEventStream);
        when(aggregateService.get(eq(firstEventStream), any()))
                .thenReturn(new HearingAggregate());
        when(aggregateService.get(eq(secondEventStream), any()))
                .thenReturn(new HearingAggregate());

        prosecutionCaseCommandHandler.updateProsecutorForAssociatedHearings(envelope);

        JsonEnvelope actualEventProduced = verifyAppendAndGetArgumentFrom(firstEventStream).collect(Collectors.toList()).get(0);
        JsonEnvelope actualEventProduced2 = verifyAppendAndGetArgumentFrom(secondEventStream).collect(Collectors.toList()).get(0);

        assertThat(actualEventProduced.metadata().name(), Matchers.is("hearing.cps-prosecutor-updated"));
        assertThat(actualEventProduced.asJsonObject().getString("prosecutionCaseId"), Matchers.is(prosecutionCaseId.toString()));
        assertThat(actualEventProduced.asJsonObject().getString("hearingId"), Matchers.is(firstHearingId.toString()));
        assertThat(actualEventProduced.asJsonObject().getString("prosecutionAuthorityId"), Matchers.is(prosecutionAuthorityId.toString()));
        assertThat(actualEventProduced.asJsonObject().getString("prosecutionAuthorityReference"), Matchers.is(prosecutionAuthorityReference));
        assertThat(actualEventProduced.asJsonObject().getString("prosecutionAuthorityName"), Matchers.is(prosecutionAuthorityName));
        assertThat(actualEventProduced.asJsonObject().getString("prosecutionAuthorityCode"), Matchers.is(prosecutionAuthorityCode));
        assertThat(actualEventProduced.asJsonObject().getString("caseURN"), Matchers.is(caseURN));
        assertThat(actualEventProduced.asJsonObject().getJsonObject("address").getString("address1"), Matchers.is(address.getAddress1()));
        assertThat(actualEventProduced.asJsonObject().getJsonObject("address").getString("postcode"), Matchers.is(address.getPostcode()));

        assertThat(actualEventProduced2.metadata().name(), Matchers.is("hearing.cps-prosecutor-updated"));
        assertThat(actualEventProduced2.asJsonObject().getString("prosecutionCaseId"), Matchers.is(prosecutionCaseId.toString()));
        assertThat(actualEventProduced2.asJsonObject().getString("hearingId"), Matchers.is(secondHearingId.toString()));
        assertThat(actualEventProduced2.asJsonObject().getString("prosecutionAuthorityId"), Matchers.is(prosecutionAuthorityId.toString()));
        assertThat(actualEventProduced2.asJsonObject().getString("prosecutionAuthorityReference"), Matchers.is(prosecutionAuthorityReference));
        assertThat(actualEventProduced2.asJsonObject().getString("prosecutionAuthorityName"), Matchers.is(prosecutionAuthorityName));
        assertThat(actualEventProduced2.asJsonObject().getString("prosecutionAuthorityCode"), Matchers.is(prosecutionAuthorityCode));
        assertThat(actualEventProduced2.asJsonObject().getString("caseURN"), Matchers.is(caseURN));
        assertThat(actualEventProduced2.asJsonObject().getJsonObject("address").getString("address1"), Matchers.is(address.getAddress1()));
        assertThat(actualEventProduced2.asJsonObject().getJsonObject("address").getString("postcode"), Matchers.is(address.getPostcode()));
    }
}