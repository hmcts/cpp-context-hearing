package uk.gov.moj.cpp.hearing.query.view;

import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.common.converter.ListToJsonArrayConverter;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.persist.entity.VerdictHearing;
import uk.gov.moj.cpp.hearing.query.view.service.OffenceService;
import uk.gov.moj.cpp.hearing.query.view.service.VerdictService;

import javax.json.Json;
import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.UUID;

import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUIDAndName;
import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.withMetadataEnvelopedFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelopeFrom;

@RunWith(MockitoJUnitRunner.class)
public class OffenceQueryViewTest {
    private static final String FIELD_HEARING_ID = "hearingId";

    @InjectMocks
    private OffenceQueryView offenceQueryView;

    @Mock
    private JsonEnvelope query;

    @Spy
    private final Enveloper enveloper = createEnveloper();

    @Mock
    private OffenceService offenceService;

    @Test
    public void shouldFindOffenceBaringyHeId() {
        final UUID hearingId = randomUUID();
        final UUID offenceId = randomUUID();
        final JsonEnvelope query = envelopeFrom(metadataWithRandomUUIDAndName(), createObjectBuilder().add(FIELD_HEARING_ID, hearingId.toString()).build());

        JsonObject jsonObject= Json.createObjectBuilder().add("offences",Json.createArrayBuilder().build()).build();
        when(offenceService.getOffencesByHearingId(hearingId)).thenReturn(jsonObject);
        final JsonEnvelope verdictsEnvelope = offenceQueryView.getOffences(query);
        assertThat(verdictsEnvelope, is(jsonEnvelope(
                withMetadataEnvelopedFrom(query).withName("hearing.get.offences"),
                payloadIsJson(allOf(
                        withJsonPath("$.offences", IsCollectionWithSize.hasSize(0))
                ))).thatMatchesSchema()
        ));
    }

}
