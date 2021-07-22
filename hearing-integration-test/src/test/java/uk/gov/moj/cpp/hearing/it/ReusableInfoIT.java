package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.lang.String.format;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.it.Utilities.EventListener;
import static uk.gov.moj.cpp.hearing.it.Utilities.listenFor;
import static uk.gov.moj.cpp.hearing.it.Utilities.makeCommand;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsACourtClerk;

import uk.gov.moj.cpp.platform.test.feature.toggle.FeatureStubber;

import java.util.UUID;

import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ReusableInfoIT extends AbstractIT{

    private static final String CACHE_DATA_PATH = "path";
    private static final int CACHEABLE = 1;
    private static final String HEARING_EVENT_REUSABLE_INFO_SAVED = "hearing.event.reusable-info-saved";
    private static final String HEARING_EVENT = "hearing.event";
    final UUID defendantId = fromString("d4276e8d-4f46-4626-b497-fd300f817c30");
    final UUID offenceId = randomUUID();
    final UUID defendantId1 = fromString("3d27da1d-3878-4499-8cdc-0ff3d4e3f2d8");
    final UUID offenceId1 = randomUUID();

    @BeforeClass
    public static void setupBeforeClass() {
        final ImmutableMap<String, Boolean> features = ImmutableMap.of("reuseOfInformation", true);
        FeatureStubber.stubFeaturesFor(HEARING_CONTEXT, features);
    }

    @Test
    public void shouldUpdateReusableInfoForCache() throws Exception {

        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());

        final UUID hearingId =  randomUUID();
        final JsonObjectBuilder cacheInfo = createObjectBuilder();
        final JsonArrayBuilder promptList = createArrayBuilder();
        promptList.add(createPrompt("bailExceptionReason",defendantId,"TXT","Good manners", CACHEABLE, CACHE_DATA_PATH, offenceId));
        promptList.add(createPrompt("designatedLocalAuthority",defendantId,"FIXL","london", CACHEABLE, CACHE_DATA_PATH, offenceId));
        promptList.add(createPrompt("nationality",defendantId1,"FIXLM","indian###british", CACHEABLE, CACHE_DATA_PATH, offenceId));
        promptList.add(createPrompt("defendantMobileNumber",defendantId1,"INT",123, CACHEABLE, CACHE_DATA_PATH, offenceId));

        final JsonArrayBuilder baicList = createArrayBuilder();
        baicList.add(createResult(defendantId,"Not to leave", randomUUID()));
        baicList.add(createResult(defendantId1,"Curfew", randomUUID()));

        JsonObject cacheValue = cacheInfo.add("reusablePrompts",promptList)
                .add("reusableResults",baicList)
                .build();

        try (final EventListener eventListener = listenFor(HEARING_EVENT_REUSABLE_INFO_SAVED, HEARING_EVENT)
                .withFilter(isJson(allOf(
                        withJsonPath("$.hearingId", is(hearingId.toString())),
                        withJsonPath("$.promptList", hasSize(4)),
                        withJsonPath(format("$.promptList[?(@.masterDefendantId == '%s')]", defendantId.toString()), hasSize(2)),
                        withJsonPath(format("$.promptList[?(@.masterDefendantId == '%s')]", defendantId1.toString()), hasSize(2)),
                        withJsonPath("$.resultsList", hasSize(2)))))) {

            makeCommand(requestSpec, "hearing.reusable-info")
                    .ofType("application/vnd.hearing.reusable-info+json")
                    .withArgs(hearingId)
                    .withPayload(cacheValue.toString())
                    .executeSuccessfully();

            eventListener.waitFor();
        }
    }

    @Test
    public void shouldSaveReusableInfoForCache() throws Exception {

        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());

        final UUID hearingId =  randomUUID();
        final JsonObjectBuilder cacheInfo = createObjectBuilder();
        final JsonArrayBuilder promptList = createArrayBuilder();
        promptList.add(createPrompt("bailExceptionReason",defendantId,"TXT","abcd", CACHEABLE, CACHE_DATA_PATH, offenceId));
        promptList.add(createPrompt("designatedLocalAuthority",defendantId,"FIXL","authority", CACHEABLE, CACHE_DATA_PATH, offenceId));
        promptList.add(createPrompt("nationality",defendantId1,"FIXLM","indian###british", CACHEABLE, CACHE_DATA_PATH, offenceId));
        promptList.add(createPrompt("defendantMobileNumber",defendantId1,"INT",123, CACHEABLE, CACHE_DATA_PATH, offenceId));

        final JsonArrayBuilder baicList = createArrayBuilder();
        baicList.add(createResult(defendantId,"Not to leave", randomUUID()));
        baicList.add(createResult(defendantId1,"Curfew", randomUUID()));


       JsonObject cacheValue = cacheInfo.add("reusablePrompts",promptList)
                                        .add("reusableResults",baicList)
                                        .build();

       try (final EventListener eventListener = listenFor(HEARING_EVENT_REUSABLE_INFO_SAVED, HEARING_EVENT)
               .withFilter(isJson(allOf(
                       withJsonPath("$.hearingId", is(hearingId.toString())),
                       withJsonPath("$.promptList", hasSize(4)),
                       withJsonPath(format("$.promptList[?(@.masterDefendantId == '%s')]", defendantId.toString()), hasSize(2)),
                       withJsonPath(format("$.promptList[?(@.masterDefendantId == '%s')]", defendantId1.toString()), hasSize(2)),
                       withJsonPath("$.resultsList", hasSize(2)))))) {

           makeCommand(requestSpec, "hearing.reusable-info")
                   .ofType("application/vnd.hearing.reusable-info+json")
                   .withArgs(hearingId)
                   .withPayload(cacheValue.toString())
                   .executeSuccessfully();

           eventListener.waitFor();
       }
    }


    @Test
    public void shouldSaveReusableInfoForCacheWithObjects() throws Exception {

        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());

        final UUID hearingId =  randomUUID();
        final JsonObject addressObject = createObjectBuilder()
                .add("prisonOrganisationName","London")
                .add("prisonOrganisationMiddleName","xxxx")
                .add("prisonOrganisationLastName","yyyyy")
                .add("prisonOrganisationAddress1","abc")
                .add("prisonOrganisationAddress2","def")
                .add("prisonOrganisationAddress3","hij")
                .add("prisonOrganisationPostCode","EB68HG")
                .build();
        final JsonObjectBuilder cacheInfo = createObjectBuilder();
        final JsonArrayBuilder promptList = createArrayBuilder();
        promptList.add(createPrompt("prisonOrganisationName",defendantId1,"NAMEADDRESS",addressObject, CACHEABLE, CACHE_DATA_PATH, offenceId1));
        promptList.add(createPrompt("designatedLocalAuthority",defendantId1,"FIXLOM","authority", CACHEABLE, CACHE_DATA_PATH, offenceId1));
        promptList.add(createPrompt("nationality",defendantId,"FIXLM","indian###british", CACHEABLE, CACHE_DATA_PATH, offenceId));
        promptList.add(createPrompt("defendantMobileNumber",defendantId,"INT",123, CACHEABLE, CACHE_DATA_PATH, offenceId));


        final JsonObject cacheValue = cacheInfo.add("reusablePrompts",promptList).build();

        try (final EventListener eventListener = listenFor(HEARING_EVENT_REUSABLE_INFO_SAVED, HEARING_EVENT)
                .withFilter(isJson(allOf(
                        withJsonPath("$.hearingId", is(hearingId.toString())),
                        withJsonPath("$.promptList", hasSize(4)),
                        withJsonPath(format("$.promptList[?(@.masterDefendantId == '%s')]", defendantId.toString()), hasSize(2)),
                        withJsonPath(format("$.promptList[?(@.masterDefendantId == '%s')]", defendantId1.toString()), hasSize(2)))))) {

            makeCommand(requestSpec, "hearing.reusable-info")
                    .ofType("application/vnd.hearing.reusable-info+json")
                    .withArgs(hearingId)
                    .withPayload(cacheValue.toString())
                    .executeSuccessfully();

            eventListener.waitFor();
        }
    }

    private JsonObject createPrompt(final String promptRef,final UUID masterDefendantId, final String type, final Object value,
                                    final Integer cacheable, final String cacheDataPath, final UUID offenceId) {
        final JsonObjectBuilder prompt = createObjectBuilder();
        prompt.add("promptRef", promptRef);
        prompt.add("masterDefendantId", masterDefendantId.toString());
        prompt.add("type", type);
        prompt.add("value", value.toString());
        prompt.add("cacheable", cacheable);
        prompt.add("cacheDataPath", cacheDataPath);
        prompt.add("offenceId", offenceId.toString());
        return prompt.build();
    }

    private JsonObject createResult(final UUID masterDefendantId, final String value, final UUID offenceId) {
        final JsonObjectBuilder baic = createObjectBuilder();
        baic.add("shortCode","BAIC");
        baic.add("masterDefendantId", masterDefendantId.toString());
        baic.add("value", value);
        baic.add("offenceId", offenceId.toString());
        return baic.build();
    }
}
