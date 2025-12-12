package uk.gov.moj.cpp.hearing.event;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static javax.json.Json.createArrayBuilder;
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
import static uk.gov.moj.cpp.hearing.test.TestTemplates.defendantTemplate;

import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.framework.api.JsonObjectConvertersFactory;

import java.util.List;
import java.util.UUID;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
public class CaseDefendantAddEventProcessorTest {

    public static final String HEARING_ID = "hearingId";
    public static final String DEFENDANT = "defendant";
    public static final String DEFENDANTS = "defendants";

    @Spy
    private final Enveloper enveloper = createEnveloper();

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter = new JsonObjectConvertersFactory().jsonObjectToObjectConverter();

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter = new JsonObjectConvertersFactory().objectToJsonObjectConverter();

    @Mock
    private Sender sender;

    @Captor
    private ArgumentCaptor<JsonEnvelope> envelopeArgumentCaptor;

    @InjectMocks
    private CaseDefendantAddEventProcessor caseDefendantAddEventProcessor;

    @Test
    public void processPublicCaseDefendantAdded() {

        final uk.gov.moj.cpp.hearing.command.defendant.Defendant arbitraryDefendant = defendantTemplate();

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("public.progression.defendants-added-to-hearing"),
                createObjectBuilder()
                        .add(DEFENDANTS,
                                createArrayBuilder().add(uk.gov.justice.services.messaging.JsonObjects.createObjectBuilder(objectToJsonObjectConverter.convert(arbitraryDefendant)).build())
                                        .build()
                        )
                        .build());

        caseDefendantAddEventProcessor.processPublicCaseDefendantAdded(event);

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());

        assertThat(this.envelopeArgumentCaptor.getValue(),
                jsonEnvelope(metadata().withName("hearing.add-defendants"), payloadIsJson(CoreMatchers.allOf(
                        withJsonPath("$.defendants[0].id", is(arbitraryDefendant.getId().toString()))))));
    }

    @Test
    public void registerHearing() {

        final Defendant arbitraryDefendant = standardInitiateHearingTemplate().getHearing().getProsecutionCases().get(0).getDefendants().get(0);

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.case-defendant-added"),
                createObjectBuilder()
                        .add(HEARING_ID, UUID.randomUUID().toString())
                        .add(DEFENDANT,
                                uk.gov.justice.services.messaging.JsonObjects.createObjectBuilder(objectToJsonObjectConverter.convert(arbitraryDefendant)).build())
                        .build());

        caseDefendantAddEventProcessor.registerHearing(event);

        verify(this.sender, times(2)).send(this.envelopeArgumentCaptor.capture());

        final List<JsonEnvelope> envelopes = this.envelopeArgumentCaptor.getAllValues();

        MatcherAssert.assertThat(
                envelopes.get(0), jsonEnvelope(
                        metadata().withName("hearing.command.register-hearing-against-defendant"),
                        payloadIsJson(allOf(
                                withJsonPath("$.defendantId", is(arbitraryDefendant.getId().toString())),
                                withJsonPath("$.hearingId", is(event.payloadAsJsonObject().getString(HEARING_ID))))))
                        .thatMatchesSchema()
        );
        MatcherAssert.assertThat(
                envelopes.get(1), jsonEnvelope(
                        metadata().withName("hearing.command.register-hearing-against-offence"),
                        payloadIsJson(allOf(
                                withJsonPath("$.offenceId", is(arbitraryDefendant.getOffences().get(0).getId().toString())),
                                withJsonPath("$.hearingId", is(event.payloadAsJsonObject().getString(HEARING_ID)))))));

    }
}
