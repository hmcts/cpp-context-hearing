package uk.gov.moj.cpp.hearing.event;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.Arrays.stream;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasKey;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeListMatcher.listContaining;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.withMetadataEnvelopedFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithDefaults;
import static uk.gov.moj.cpp.hearing.domain.OffenceResult.DISMISSED;
import static uk.gov.moj.cpp.hearing.domain.OffenceResult.WITHDRAWN;
import static uk.gov.moj.cpp.hearing.event.Framework5Fix.standardizeJsonEnvelopes;

import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.spi.DefaultEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.DefendantCaseWithdrawnOrDismissed;

import java.io.StringReader;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DefendantCaseWithdrawnOrDismissedEventProcessorTest {

    private static final ObjectMapperProducer objectMapperProducer = new ObjectMapperProducer();

    @Mock
    private Sender sender;

    @InjectMocks
    private DefendantCaseWithdrawnOrDismissedEventProcessor eventProcessor;

    @Captor
    private ArgumentCaptor<DefaultEnvelope> envelopeCaptor;

    @Test
    public void defendantCaseWithdrawnOrDismissed() throws JsonProcessingException {
        final UUID caseId = randomUUID();
        final UUID defendantId = randomUUID();
        final UUID offence1 = randomUUID();
        final UUID offence2 = randomUUID();
        final UUID offence3 = randomUUID();
        final UUID[] offences = new UUID[]{offence1, offence2, offence3};

        DefendantCaseWithdrawnOrDismissed event = DefendantCaseWithdrawnOrDismissed.newBuilder()
                .withDefendantId(defendantId)
                .withCaseId(caseId)
                .withResultedOffences(
                        mapOf(
                                entry(offence1, WITHDRAWN),
                                entry(offence2, WITHDRAWN),
                                entry(offence3, DISMISSED)
                        )
                )
                .build();

        final JsonEnvelope jsonEnvelope = JsonEnvelope.envelopeFrom(
                metadataWithDefaults().withName("hearing.event.defendant-case-withdrawn-or-dismissed"),
                readJson(objectMapperProducer.objectMapper().writeValueAsString(event))
        );

        //when
        eventProcessor.defendantCaseWithdrawnOrDismissed(jsonEnvelope);

        //then
        verify(sender).send(envelopeCaptor.capture());
        //TODO fix a framework issue
        final List<JsonEnvelope> allValues = standardizeJsonEnvelopes((List) envelopeCaptor.getAllValues());

        assertThat(allValues, listContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(jsonEnvelope)
                                .withName("public.hearing.defendant-case-withdrawn-or-dismissed"),
                        payloadIsJson(
                                allOf(
                                        withJsonPath("$.defendantId", equalTo(defendantId.toString())),
                                        withJsonPath("$.caseId", equalTo(caseId.toString())),
                                        withJsonPath("$.resultedOffences",
                                                allOf(
                                                        Arrays.stream(offences)
                                                                .map(uuid -> hasKey(uuid.toString()))
                                                                .collect(toList())))
                                ))
                ).thatMatchesSchema()
                )
        );
    }

    private <K, V> Map<K, V> mapOf(final Map<K, V>... maps) {
        return stream(maps)
                .flatMap(kvMap -> kvMap.entrySet().stream())
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private <K, V> Map<K, V> mapOf(final Map.Entry<K, V>... entries) {
        return stream(entries).collect(toMap(o -> o.getKey(), o -> o.getValue()));
    }

    private <K, V> Map.Entry<K, V> entry(final K key, final V value) {
        return new AbstractMap.SimpleEntry<>(key, value);
    }

    public static JsonObject readJson(final String jsonString) {
        try (final JsonReader jsonReader = Json.createReader(new StringReader(jsonString))) {
            return jsonReader.readObject();
        }
    }
}