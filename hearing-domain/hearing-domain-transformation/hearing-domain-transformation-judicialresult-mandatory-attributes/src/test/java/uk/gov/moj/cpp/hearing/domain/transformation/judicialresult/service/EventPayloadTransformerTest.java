package uk.gov.moj.cpp.hearing.domain.transformation.judicialresult.service;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static javax.json.JsonValue.FALSE;
import static javax.json.JsonValue.TRUE;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.Is.is;
import static org.junit.rules.ExpectedException.none;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.integer;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.randomEnum;
import static uk.gov.moj.cpp.hearing.domain.transformation.judicialresult.domain.EventToTransform.APPLICATION_DETAIL_CHANGED;
import static uk.gov.moj.cpp.hearing.domain.transformation.judicialresult.domain.EventToTransform.CASE_DEFENDANTS_UPDATED;
import static uk.gov.moj.cpp.hearing.domain.transformation.judicialresult.domain.EventToTransform.CASE_DEFENDANTS_UPDATED_FOR_HEARING;
import static uk.gov.moj.cpp.hearing.domain.transformation.judicialresult.domain.EventToTransform.HEARING_EXTENDED;
import static uk.gov.moj.cpp.hearing.domain.transformation.judicialresult.domain.EventToTransform.HEARING_INITIATED;
import static uk.gov.moj.cpp.hearing.domain.transformation.judicialresult.domain.EventToTransform.NOWS_REQUESTED;
import static uk.gov.moj.cpp.hearing.domain.transformation.judicialresult.domain.EventToTransform.PENDING_NOWS_REQUESTED;
import static uk.gov.moj.cpp.hearing.domain.transformation.judicialresult.domain.EventToTransform.RESULTS_SHARED;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.transformation.judicialresult.domain.EventToTransform;
import uk.gov.moj.cpp.hearing.domain.transformation.judicialresult.exception.TransformationException;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

@RunWith(DataProviderRunner.class)
public class EventPayloadTransformerTest {

    private static final String JUDICIAL_RESULTS_ATTRIBUTE = "judicialResults";
    private static final String PUBLISHED_AS_A_PROMPT_ATTRIBUTE = "publishedAsAPrompt";
    private static final String EXCLUDED_FROM_RESULTS_ATTRIBUTE = "excludedFromResults";
    private static final String ALWAYS_PUBLISHED_ATTRIBUTE = "alwaysPublished";

    @Rule
    public ExpectedException expectedException = none();

    @DataProvider
    public static Object[][] validEventToTransform() {
        return new Object[][]{
                {RESULTS_SHARED.getEventName()},
                {PENDING_NOWS_REQUESTED.getEventName()},
                {NOWS_REQUESTED.getEventName()},
                {HEARING_INITIATED.getEventName()},
                {HEARING_EXTENDED.getEventName()},
                {APPLICATION_DETAIL_CHANGED.getEventName()},
                {CASE_DEFENDANTS_UPDATED_FOR_HEARING.getEventName()},
                {CASE_DEFENDANTS_UPDATED.getEventName()}
        };
    }

    private final EventPayloadTransformer underTest = new EventPayloadTransformer();

    @Test
    public void shouldEvaluateRollUpPromptsToTrueWhenAllMandatoryAttributesAreFalse() {
        final JsonEnvelope event = prepareValidEventToTransformWithAllMandatoryAttributesSetToFalse();

        final JsonObject transformedPayload = underTest.transform(event);

        assertThat(transformedPayload, isJson(allOf(
                withJsonPath("$.offences[0].judicialResults[0].publishedAsAPrompt", equalTo(FALSE)),
                withJsonPath("$.offences[0].judicialResults[0].excludedFromResults", equalTo(FALSE)),
                withJsonPath("$.offences[0].judicialResults[0].alwaysPublished", equalTo(FALSE)),
                withJsonPath("$.offences[0].judicialResults[0].publishedForNows", equalTo(FALSE)),
                withJsonPath("$.offences[0].judicialResults[0].rollUpPrompts", equalTo(TRUE))
        )));
    }

    @Test
    public void shouldEvaluateRollUpPromptsToFalseWhenOneOfTheMandatoryAttributesIsTrue() {
        final JsonEnvelope event = prepareValidEventToTransformWithOneOfTheMandatoryAttributesSetToTrue();

        final JsonObject transformedPayload = underTest.transform(event);

        final JsonObject expectedJudicialResult = event.payloadAsJsonObject().getJsonArray("offences").getValuesAs(JsonObject.class).get(0)
                .getJsonArray("judicialResults").getValuesAs(JsonObject.class).get(0);

        assertThat(expectedJudicialResult.getBoolean("publishedAsAPrompt") ||
                expectedJudicialResult.getBoolean("excludedFromResults") ||
                expectedJudicialResult.getBoolean("alwaysPublished"), is(true));
        assertThat(transformedPayload, isJson(allOf(
                withJsonPath("$.offences[0].judicialResults[0].publishedAsAPrompt", equalTo(expectedJudicialResult.get("publishedAsAPrompt"))),
                withJsonPath("$.offences[0].judicialResults[0].excludedFromResults", equalTo(expectedJudicialResult.get("excludedFromResults"))),
                withJsonPath("$.offences[0].judicialResults[0].alwaysPublished", equalTo(expectedJudicialResult.get("alwaysPublished"))),
                withJsonPath("$.offences[0].judicialResults[0].publishedForNows", equalTo(FALSE)),
                withJsonPath("$.offences[0].judicialResults[0].rollUpPrompts", equalTo(FALSE))
        )));
    }

    @Test
    public void shouldThrowExceptionWhenOneOrMoreOfTheMandatoryAttributeIsMissing() {
        final JsonEnvelope invalidEvent = prepareInvalidEventWithOneOfTheMandatoryAttributeMissing();
        expectedException.expect(TransformationException.class);
        expectedException.expectMessage(matchesPattern("Mandatory attribute/s [\\w,]+ missing from judicialResult payload \\{.*\\}"));

        underTest.transform(invalidEvent);
    }

    @Test
    @UseDataProvider("validEventToTransform")
    public void shouldTransformRealEventsFromFile(final String eventToTransform) {
        final JsonObject eventPayload = loadTestFile(format("%s.json", eventToTransform));
        final String expectedPayloadInString = eventPayload.toString();
        assertThat(expectedPayloadInString, not(containsString("publishedForNows")));
        assertThat(expectedPayloadInString, not(containsString("rollUpPrompts")));

        final JsonObject transformedPayload = underTest.transform(envelope()
                .with(metadataWithRandomUUID(eventToTransform))
                .withPayloadFrom(eventPayload).build());

        final String actualPayloadInString = transformedPayload.toString();
        assertThat(actualPayloadInString, containsString("publishedForNows"));
        assertThat(actualPayloadInString, containsString("rollUpPrompts"));
    }

    private JsonEnvelope prepareValidEventToTransformWithAllMandatoryAttributesSetToFalse() {
        final JsonObject judicialResults = createObjectBuilder().add(JUDICIAL_RESULTS_ATTRIBUTE, createArrayBuilder()
                .add(createObjectBuilder()
                        .add(PUBLISHED_AS_A_PROMPT_ATTRIBUTE, false)
                        .add(EXCLUDED_FROM_RESULTS_ATTRIBUTE, false)
                        .add(ALWAYS_PUBLISHED_ATTRIBUTE, false)
                )).build();

        return envelope()
                .with(metadataWithRandomUUID(randomEnum(EventToTransform.class).next().getEventName()))
                .withPayloadOf(createArrayBuilder().add(judicialResults).build(), "offences")
                .build();
    }

    private JsonEnvelope prepareValidEventToTransformWithOneOfTheMandatoryAttributesSetToTrue() {
        final Integer attributePosition = integer(1, 3).next();
        final JsonObject judicialResults = createObjectBuilder().add(JUDICIAL_RESULTS_ATTRIBUTE, createArrayBuilder()
                .add(createObjectBuilder()
                        .add(PUBLISHED_AS_A_PROMPT_ATTRIBUTE, attributePosition == 1)
                        .add(EXCLUDED_FROM_RESULTS_ATTRIBUTE, attributePosition == 2)
                        .add(ALWAYS_PUBLISHED_ATTRIBUTE, attributePosition == 3)
                )).build();

        return envelope()
                .with(metadataWithRandomUUID(randomEnum(EventToTransform.class).next().getEventName()))
                .withPayloadOf(createArrayBuilder().add(judicialResults).build(), "offences")
                .build();
    }

    private JsonEnvelope prepareInvalidEventWithOneOfTheMandatoryAttributeMissing() {
        final List<String> mandatoryAttributes = asList(PUBLISHED_AS_A_PROMPT_ATTRIBUTE, EXCLUDED_FROM_RESULTS_ATTRIBUTE, ALWAYS_PUBLISHED_ATTRIBUTE);
        Collections.shuffle(mandatoryAttributes);
        final Integer noOfAttributesToInclude = integer(0, 2).next();
        final JsonObjectBuilder judicialResult = createObjectBuilder();
        if (noOfAttributesToInclude != 0) {
            final List<String> attributesToInclude = mandatoryAttributes.subList(0, noOfAttributesToInclude);
            attributesToInclude.forEach(attribute -> judicialResult.add(attribute, false));
        }

        final JsonObject judicialResults = createObjectBuilder().add(JUDICIAL_RESULTS_ATTRIBUTE, createArrayBuilder()
                .add(judicialResult)).build();

        return envelope()
                .with(metadataWithRandomUUID(randomEnum(EventToTransform.class).next().getEventName()))
                .withPayloadOf(createArrayBuilder().add(judicialResults).build(), "offences")
                .build();
    }

    private Matcher<String> matchesPattern(final String regex) {
        return new TypeSafeMatcher<String>() {
            @Override
            public void describeTo(final Description description) {
                description.appendText("matching pattern " + regex);
            }

            @Override
            protected boolean matchesSafely(final String item) {
                return item.matches(regex);
            }
        };
    }

    private JsonObject loadTestFile(final String resourceFileName) {
        try {
            final InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceFileName);
            try (final JsonReader jsonReader = Json.createReader(is)) {
                return jsonReader.readObject();
            }
        } catch (final Exception ex) {
            throw new RuntimeException("failed to load test file " + resourceFileName, ex);
        }
    }

}