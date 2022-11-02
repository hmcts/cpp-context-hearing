package uk.gov.moj.cpp.hearing.pi;

import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.ProsecutionCaseResponse;
import uk.gov.moj.cpp.hearing.test.FileUtil;

import java.util.Optional;

import javax.json.JsonObject;

import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProsecutionCaseRetrieverTest {

    @InjectMocks
    ProsecutionCaseRetriever prosecutionCaseRetriever;
    @Mock(answer = RETURNS_DEEP_STUBS)
    private Requester requester;
    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;
    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Before
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void shouldGetProsecutionCaseForHearing() {

        final ProsecutionCaseResponse prosecutionCase = getProsecutionCaseResponse();

        when(requester.requestAsAdmin(any(JsonEnvelope.class), eq(ProsecutionCaseResponse.class)).payload()).thenReturn(prosecutionCase);

        final Optional<ProsecutionCaseResponse> prosecutionCaseResponse = prosecutionCaseRetriever.getProsecutionCaseForHearing(randomUUID(), randomUUID());

        assertThat(prosecutionCaseResponse.get().getProsecutionCases().get(0).getDefendants(), hasSize(1));

    }

    @Test
    public void shouldReturnEmptyProsecutionCaseForHearing() {

        final ProsecutionCaseResponse prosecutionCase = getEmptyPayload();

        when(requester.requestAsAdmin(any(JsonEnvelope.class), eq(ProsecutionCaseResponse.class)).payload()).thenReturn(prosecutionCase);

        final Optional<ProsecutionCaseResponse> prosecutionCaseResponse = prosecutionCaseRetriever.getProsecutionCaseForHearing(randomUUID(), randomUUID());

        MatcherAssert.assertThat(prosecutionCaseResponse.get().getProsecutionCases().isEmpty(), is(true));

    }

    private ProsecutionCaseResponse getProsecutionCaseResponse() {

        final JsonObject prosecutionCasesPayload = new StringToJsonObjectConverter().convert(FileUtil.getPayload("hearing.with.prosecution.cases.json"));
        final ProsecutionCaseResponse prosecutionCaseResponse = jsonObjectToObjectConverter.convert(prosecutionCasesPayload, ProsecutionCaseResponse.class);

        return prosecutionCaseResponse;
    }

    private ProsecutionCaseResponse getEmptyPayload() {

        final JsonObject prosecutionCasesPayload = createObjectBuilder().build();
        final ProsecutionCaseResponse prosecutionCaseResponse = jsonObjectToObjectConverter.convert(prosecutionCasesPayload, ProsecutionCaseResponse.class);
        return prosecutionCaseResponse;
    }

}
