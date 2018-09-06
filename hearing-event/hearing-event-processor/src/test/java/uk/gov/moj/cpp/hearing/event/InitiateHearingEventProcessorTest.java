package uk.gov.moj.cpp.hearing.event;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;

import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings({"unchecked", "unused"})
@RunWith(DataProviderRunner.class)
public class InitiateHearingEventProcessorTest {

    @InjectMocks
    private InitiateHearingEventProcessor initiateHearingEventProcessor;

    @Mock
    private Sender sender;

    @Mock
    private Requester InitiateHearingEventProcessorTestrequester;

    @Mock
    private JsonEnvelope responseEnvelope;

    @Spy
    private final Enveloper enveloper = createEnveloper();

    @Captor
    private ArgumentCaptor<JsonEnvelope> envelopeArgumentCaptor;

    @Spy
    private final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();

    @Spy
    @InjectMocks
    private final JsonObjectToObjectConverter jsonObjectToObjectConverter = new JsonObjectToObjectConverter();

    @Spy
    @InjectMocks
    private final ObjectToJsonObjectConverter objectToJsonObjectConverter = new ObjectToJsonObjectConverter();

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void publishHearingInitiatedEvent() {

        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.initiated"),
                objectToJsonObjectConverter.convert(initiateHearingCommand));

        this.initiateHearingEventProcessor.hearingInitiated(event);

        verify(this.sender, times(4)).send(this.envelopeArgumentCaptor.capture());

        final List<JsonEnvelope> envelopes = this.envelopeArgumentCaptor.getAllValues();

        final List<UUID> prosecutionCaseIds = new ArrayList<>();
        final List<UUID> defendantIds = new ArrayList<>();
        final List<UUID> offenceIds = new ArrayList<>();

        initiateHearingCommand.getHearing().getProsecutionCases().forEach(prosecutionCase -> {
            prosecutionCaseIds.add(prosecutionCase.getId());
            prosecutionCase.getDefendants().forEach(defendant -> {
                defendantIds.add(defendant.getId());
                defendant.getOffences().forEach(offence -> offenceIds.add(offence.getId()));
            });
        });

        assertThat(
                envelopes.get(0), jsonEnvelope(
                        metadata().withName("hearing.command.register-hearing-against-defendant"),
                        payloadIsJson(allOf(
                                withJsonPath("$.defendantId", is(defendantIds.get(0).toString())),
                                withJsonPath("$.hearingId", is(initiateHearingCommand.getHearing().getId().toString())))))
                        .thatMatchesSchema()
        );

        assertThat(
                envelopes.get(1), jsonEnvelope(
                        metadata().withName("hearing.command.lookup-plea-on-offence-for-hearing"),
                        payloadIsJson(allOf(
                                withJsonPath("$.offenceId", is(offenceIds.get(0).toString())),
                                withJsonPath("$.caseId", is(prosecutionCaseIds.get(0).toString())),
                                withJsonPath("$.defendantId", is(defendantIds.get(0).toString())),
                                withJsonPath("$.hearingId", is(initiateHearingCommand.getHearing().getId().toString())))))
                        .thatMatchesSchema()
        );


        assertThat(
                envelopes.get(2), jsonEnvelope(
                        metadata().withName("hearing.command.register-hearing-against-case"),
                        payloadIsJson(allOf(
                                withJsonPath("$.caseId", is(prosecutionCaseIds.get(0).toString())),
                                withJsonPath("$.hearingId", is(initiateHearingCommand.getHearing().getId().toString())))))
                        .thatMatchesSchema()
        );

        assertThat(
                envelopes.get(3), jsonEnvelope(
                        metadata().withName("public.hearing.initiated"),
                        payloadIsJson(withJsonPath("$.hearingId", is(initiateHearingCommand.getHearing().getId().toString()))))
                        .thatMatchesSchema()
        );
    }

    @Test
    public void hearingInitiateOffencePlea() {

        final UUID offenceId = randomUUID();
        final UUID caseId = randomUUID();
        final UUID defendantId = randomUUID();
        final UUID hearingId = randomUUID();
        final UUID originHearingId = randomUUID();
        final String pleaDate = "2017-01-01";
        final String value = "GUILTY";

        final JsonObject payload = createObjectBuilder()
                .add("offenceId", offenceId.toString())
                .add("caseId", caseId.toString())
                .add("defendantId", defendantId.toString())
                .add("hearingId", hearingId.toString())
                .add("originHearingId", originHearingId.toString())
                .add("pleaDate", pleaDate)
                .add("value", value)
                .build();

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.events.found-plea-for-hearing-to-inherit"), payload);

        this.initiateHearingEventProcessor.hearingInitiateOffencePlea(event);

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());

        assertThat(
                this.envelopeArgumentCaptor.getValue(), jsonEnvelope(
                        metadata().withName("hearing.command.update-hearing-with-inherited-plea"),
                        payloadIsJson(allOf(
                                withJsonPath("$.offenceId", is(offenceId.toString())),
                                withJsonPath("$.caseId", is(caseId.toString())),
                                withJsonPath("$.defendantId", is(defendantId.toString())),
                                withJsonPath("$.hearingId", is(hearingId.toString())),
                                withJsonPath("$.originHearingId", is(originHearingId.toString())),
                                withJsonPath("$.pleaDate", is(pleaDate)),
                                withJsonPath("$.value", is(value))
                                )
                        )
                )
        );
    }

}