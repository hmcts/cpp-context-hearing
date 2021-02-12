package uk.gov.moj.cpp.hearing.query.api.service.referencedata;

import static java.util.UUID.randomUUID;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static javax.json.Json.createReader;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.Envelope.metadataBuilder;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.CrackedIneffectiveVacatedTrialTypes;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt;

import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ReferenceDataServiceTest {

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Spy
    private UtcClock utcClock;

    @Mock(answer = RETURNS_DEEP_STUBS)
    private Requester requester;


    @InjectMocks
    private ReferenceDataService referenceDataService;

    @Before
    public void setup() {

        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }


    @Test
    public void shouldRequestCrackedInEffectiveTrialTypes() {
        when(requester.requestAsAdmin(any(JsonEnvelope.class), any(Class.class))).thenReturn(crackedInEffectiveTrialTypesResponseEnvelope());
        final CrackedIneffectiveVacatedTrialTypes trialTypes = referenceDataService.listAllCrackedIneffectiveVacatedTrialTypes();
        assertEquals(2, trialTypes.getCrackedIneffectiveVacatedTrialTypes().size());
    }

    @Test
    public void shouldGetCacheableResultDefinitions() {
        final String promptRef = "promptRef";
        when(requester.requestAsAdmin(any(JsonEnvelope.class), any(Class.class))).thenReturn(cacheableResultDefinitionsEnvelope(promptRef));
        final List<Prompt> resultPrompts = referenceDataService.getCacheableResultPrompts(Optional.empty());
        assertThat(resultPrompts.size(), is(1));
        assertThat(resultPrompts.get(0).getReference(), is(promptRef));
    }

    private Envelope cacheableResultDefinitionsEnvelope(final String promptRef) {
        return envelopeFrom(
                metadataBuilder().
                        withName("referencedata.get-all-result-definitions").
                        withId(randomUUID()),
                createObjectBuilder().add("resultDefinitions",
                        createArrayBuilder()
                                .add(createObjectBuilder()
                                        .add("id", randomUUID().toString())
                                        .add("prompts", createArrayBuilder()
                                                .add(createObjectBuilder()
                                                        .add("id", randomUUID().toString())
                                                        .add("reference", promptRef)
                                                        .add("cacheable", 1)).build()))
                                .add(createObjectBuilder()
                                        .add("id", randomUUID().toString())
                                        .add("prompts", createArrayBuilder()
                                                .add(createObjectBuilder()
                                                        .add("id", randomUUID().toString())
                                                        .add("reference", promptRef)
                                                        .add("cacheable", 1)).build()))
                                .build())
        );
    }


    private JsonEnvelope crackedInEffectiveTrialTypesResponseEnvelope() {
        return envelopeFrom(
                metadataBuilder().
                        withName("referencedata.query.cracked-ineffective-vacated-trial-types").
                        withId(randomUUID()),
                createReader(getClass().getClassLoader().
                        getResourceAsStream("cracked-ineffective-trial-types-ref-data.json")).
                        readObject()
        );
    }

    private JsonEnvelope hearingTypesResponseEnvelope() {
        return envelopeFrom(
                metadataBuilder().
                        withName("referencedata.query.hearing-types").
                        withId(randomUUID()),
                createReader(getClass().getClassLoader().
                        getResourceAsStream("hearing-types.json")).
                        readObject()
        );
    }

    private JsonEnvelope judiciaryResponseEnvelope() {
        return envelopeFrom(
                metadataBuilder().
                        withName("referencedata.query.judiciaries").
                        withId(randomUUID()),
                createReader(getClass().getClassLoader().
                        getResourceAsStream("referencedata.query.judiciaries.json")).
                        readObject()
        );
    }

    private JsonEnvelope emptyJudiciaryResponseEnvelope() {
        return envelopeFrom(
                metadataBuilder().
                        withName("referencedata.query.judiciaries").
                        withId(randomUUID()),
                createObjectBuilder().build()
        );
    }
}
