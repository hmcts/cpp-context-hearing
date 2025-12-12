package uk.gov.moj.cpp.hearing.event;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
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
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;


import java.util.ArrayList;
import java.util.Collections;
import javax.json.Json;
import javax.json.JsonObject;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.ExistingHearingUpdated;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.services.messaging.spi.DefaultEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.ExistingHearingUpdated;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("squid:S2187")
public class CaseDefendantsUpdatedForHearingProcessorTest {

    @Spy
    private final Enveloper enveloper = createEnveloper();

    @Mock
    private Sender sender;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Captor
    private ArgumentCaptor<JsonEnvelope> envelopeArgumentCaptor;

    @Captor
    private ArgumentCaptor<DefaultEnvelope> envelopeArgumentCaptorForCommand;

    @InjectMocks
    private CaseDefendantsUpdatedForHearingProcessor caseDefendantsUpdatedForHearingProcessor;

    @BeforeEach
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void shouldRegisterDefendantsForHearing() {
        final UUID hearingID = UUID.randomUUID();
        final UUID caseId = UUID.randomUUID();
        final UUID defendantId1 = UUID.randomUUID();
        final UUID defendantId2 = UUID.randomUUID();
        final Defendant defendant1 = Defendant.defendant().withId(defendantId1).withProsecutionCaseId(caseId).build();
        final Defendant defendant2 = Defendant.defendant().withId(defendantId2).withProsecutionCaseId(caseId).build();
        final ExistingHearingUpdated existingHearingUpdated = new ExistingHearingUpdated(hearingID, Collections.singletonList(ProsecutionCase.prosecutionCase()
                .withDefendants(Arrays.asList(defendant1, defendant2))
                .build()), new ArrayList<>());


        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.events.existing-hearing-updated"),
                objectToJsonObjectConverter.convert(existingHearingUpdated));

        caseDefendantsUpdatedForHearingProcessor.caseDefendantsUpdatedForHearing(event);

        verify(this.sender, times(2)).send(this.envelopeArgumentCaptor.capture());

        final List<JsonEnvelope> allValues = this.envelopeArgumentCaptor.getAllValues();
        assertThat(allValues.get(0), jsonEnvelope(
                metadata().withName("hearing.command.register-hearing-against-defendant"),
                payloadIsJson(allOf(
                        withJsonPath("$.defendantId", is(defendantId1.toString())),
                        withJsonPath("$.hearingId", is(hearingID.toString()))))));

        assertThat(allValues.get(1), jsonEnvelope(
                metadata().withName("hearing.command.register-hearing-against-defendant"),
                payloadIsJson(allOf(
                        withJsonPath("$.defendantId", is(defendantId2.toString())),
                        withJsonPath("$.hearingId", is(hearingID.toString()))))));

    }

    @Test
    public void shouldCallCommand(){
        final String hearingId = randomUUID().toString();
        JsonObject relatedHearingUpdatedforAdhocHearing = Json.createObjectBuilder()
                .add("hearingId", hearingId)
                .build();

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("public.progression.related-hearing-updated-for-adhoc-hearing"), relatedHearingUpdatedforAdhocHearing);
        caseDefendantsUpdatedForHearingProcessor.handleExistingHearingUpdated(event);

        verify(this.sender, times(1)).send(this.envelopeArgumentCaptorForCommand.capture());
        final DefaultEnvelope<JsonObject> command = this.envelopeArgumentCaptorForCommand.getValue();

        assertThat(command.metadata().name(), is ("hearing.command.update-related-hearing"));
        assertThat(command.payload().getString("hearingId"), is(hearingId));
    }
}