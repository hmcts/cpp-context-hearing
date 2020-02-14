package uk.gov.moj.cpp.hearing.event;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.json.Json;
import javax.json.JsonObject;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

public class CaseDefendantsUpdatedProcessorTest {
    public static final String HEARING_ID = "hearingId";
    @Spy
    private final Enveloper enveloper = createEnveloper();
    @Mock
    private Sender sender;
    @Captor
    private ArgumentCaptor<JsonEnvelope> envelopeArgumentCaptor;
    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;
    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;
    @InjectMocks
    private CaseDefendantsUpdatedProcessor caseDefendantsUpdatedProcessor;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void testHandleCaseDefendantsUpdateForHearing () {
        final UUID defendantId = randomUUID();
        final UUID hearingId = randomUUID();

        final ProsecutionCase prosecutionCase = ProsecutionCase.prosecutionCase()
                .withCaseStatus("CLOSED")
                .withDefendants(Arrays.asList(Defendant.defendant()
                        .withId(defendantId)
                        .withProceedingsConcluded(true).build()))
                .build();

        final JsonObject eventPayload = Json.createObjectBuilder()
                .add("hearingIds", Json.createArrayBuilder().add(hearingId.toString()).build())
                .add("prosecutionCase",objectToJsonObjectConverter.convert(prosecutionCase))
                .build();
        final JsonEnvelope event = JsonEnvelope.envelopeFrom(metadataWithRandomUUID("hearing.case-defendants-updated"),
                eventPayload);

        caseDefendantsUpdatedProcessor.handleCaseDefendantsUpdateForHearing(event);

        verify(this.sender, times(1)).send(this.envelopeArgumentCaptor.capture());

        List<JsonEnvelope> events = this.envelopeArgumentCaptor.getAllValues();

        assertThat(events.get(0).metadata().name(), is("hearing.command.update-case-defendants-for-hearing"));

        final JsonObject commandPayload = events.get(0).payloadAsJsonObject();

        assertThat(commandPayload.getString(HEARING_ID), is(hearingId.toString()));

        assertThat(commandPayload.getJsonObject("prosecutionCase")
                .getJsonArray("defendants").getJsonObject(0).getBoolean("proceedingsConcluded"), is(true));

    }

}
