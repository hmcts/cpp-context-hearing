package uk.gov.moj.cpp.hearing.event.service;

import static java.util.UUID.randomUUID;
import static uk.gov.justice.services.messaging.JsonObjects.createObjectBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;


import javax.json.JsonObject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProgressionServiceTest {

    public static final String SEARCH_APPLICATION = "progression.query.application-only";

    @Mock
    private Sender sender;
    @InjectMocks
    private ProgressionService progressionService;
    @Captor
    private ArgumentCaptor<Envelope> envelopeArgumentCaptor;
    @Mock
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;
    @Mock
    private Function<Object, JsonEnvelope> objectJsonEnvelopeFunction;
    @Mock
    private Requester requester;
    @Mock
    private ArgumentCaptor<Envelope<JsonObject>> envelopeCaptor;
    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter = new JsonObjectToObjectConverter();

    @BeforeEach
    public void setUp() {
        setField(jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
    }


    @Test
    public void shouldGetApplicationStatus() {
        final UUID applicationId = randomUUID();
        final JsonEnvelope envelope = mock(JsonEnvelope.class);
        final Metadata metadata = JsonEnvelope.metadataBuilder().withId(randomUUID()).withName(SEARCH_APPLICATION).build();

       final JsonObject courtApplicationObj = createObjectBuilder().add("courtApplication",
                        createObjectBuilder().add("id", applicationId.toString())
                                .add("applicant", createObjectBuilder().add("id", randomUUID().toString()))
                                .add("applicationStatus", "FINALISED")
                                .build()).build();

        when(envelope.metadata()).thenReturn(metadata);
        when(requester.request(any(Envelope.class), eq(JsonObject.class))).thenReturn(Envelope.envelopeFrom(metadata, courtApplicationObj));
        final Optional<JsonObject> courtApplication = progressionService.getApplicationDetails(envelope, applicationId);
        verify(requester, times(1)).request(any(Envelope.class), eq(JsonObject.class));
        assertThat(courtApplication.get(), notNullValue());
    }

    @Test
    public void shouldGetProsecutionCaseDetailsAsAdmin() {
        final UUID caseId = randomUUID();
        final UUID defendantId = randomUUID();
        final UUID offenceId = randomUUID();
        final JsonEnvelope envelope = JsonEnvelope.envelopeFrom(
                JsonEnvelope.metadataBuilder().withId(randomUUID()).withName("hearing.results-shared").build(),
                createObjectBuilder().build());

        final JsonObject response = createObjectBuilder().add("prosecutionCase", createObjectBuilder()
                .add("id", caseId.toString())
                .add("defendants", uk.gov.justice.services.messaging.JsonObjects.createArrayBuilder().add(createObjectBuilder()
                        .add("id", defendantId.toString())
                        .add("personDefendant", createObjectBuilder()
                                .add("bailStatus", createObjectBuilder().add("id", randomUUID().toString()).add("code", "U").add("description", "Unconditional")))
                        .add("offences", uk.gov.justice.services.messaging.JsonObjects.createArrayBuilder().add(createObjectBuilder()
                                .add("id", offenceId.toString())
                                .add("proceedingsConcluded", false)
                                .add("bailStatus", createObjectBuilder().add("id", randomUUID().toString()).add("code", "C").add("description", "Custody")))))))
                .build();
        final ArgumentCaptor<Envelope<JsonObject>> requestCaptor = ArgumentCaptor.forClass(Envelope.class);
        when(requester.requestAsAdmin(requestCaptor.capture(), eq(JsonObject.class))).thenReturn(Envelope.envelopeFrom(envelope.metadata(), response));

        final Optional<ProsecutionCase> prosecutionCase = progressionService.getProsecutionCaseDetails(envelope, caseId);

        assertThat(requestCaptor.getValue().metadata().name(), is("progression.query.prosecutioncase-details"));
        assertThat(requestCaptor.getValue().payload().getString("caseId"), is(caseId.toString()));
        assertThat(prosecutionCase.isPresent(), is(true));
        assertThat(prosecutionCase.get().getId(), is(caseId));
        assertThat(prosecutionCase.get().getDefendants().get(0).getId(), is(defendantId));
        assertThat(prosecutionCase.get().getDefendants().get(0).getPersonDefendant().getBailStatus().getCode(), is("U"));
        assertThat(prosecutionCase.get().getDefendants().get(0).getOffences().get(0).getId(), is(offenceId));
        assertThat(prosecutionCase.get().getDefendants().get(0).getOffences().get(0).getProceedingsConcluded(), is(false));
        assertThat(prosecutionCase.get().getDefendants().get(0).getOffences().get(0).getBailStatus().getCode(), is("C"));
    }

    @Test
    public void shouldReturnEmptyWhenProgressionHasNoProsecutionCase() {
        final JsonEnvelope envelope = JsonEnvelope.envelopeFrom(
                JsonEnvelope.metadataBuilder().withId(randomUUID()).withName("hearing.results-shared").build(),
                createObjectBuilder().build());
        when(requester.requestAsAdmin(any(Envelope.class), eq(JsonObject.class)))
                .thenReturn(Envelope.envelopeFrom(envelope.metadata(), createObjectBuilder().build()));

        assertThat(progressionService.getProsecutionCaseDetails(envelope, randomUUID()).isPresent(), is(false));
    }

}
