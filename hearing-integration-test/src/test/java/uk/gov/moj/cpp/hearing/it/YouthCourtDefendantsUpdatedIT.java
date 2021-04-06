package uk.gov.moj.cpp.hearing.it;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.specification.RequestSpecification;
import org.hamcrest.core.Is;
import org.junit.Test;
import uk.gov.moj.cpp.hearing.it.Utilities.EventListener;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import java.util.List;
import java.util.UUID;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static org.codehaus.groovy.runtime.InvokerHelper.asList;
import static uk.gov.moj.cpp.hearing.it.UseCases.initiateHearing;
import static uk.gov.moj.cpp.hearing.it.Utilities.listenFor;
import static uk.gov.moj.cpp.hearing.it.Utilities.makeCommand;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.stubForYouthCourtForMagUUID;


public class YouthCourtDefendantsUpdatedIT extends AbstractIT {

    @Test
    public void shouldSuccessfullyUpdateDefendants() {
        final InitiateHearingCommandHelper hearing = h(initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

        stubForYouthCourtForMagUUID(hearing.getHearing().getCourtCentre().getId());

        final UUID defendantId = hearing.getFirstDefendantForFirstCase().getId();
        final List<UUID> youthDefendantsList = asList(defendantId);
        updateDefendantsInYouthCourt(getRequestSpec(), hearing.getHearingId(), youthDefendantsList);

    }






    private JsonPath updateDefendantsInYouthCourt(final RequestSpecification requestSpec, final UUID hearingId, final List<UUID> defendantsInYouthCourt) {

        final EventListener eventListener = listenFor("public.hearing.defendants-in-youthcourt-updated")
                .withFilter(isJson(withJsonPath("$.hearingId", Is.is(hearingId.toString()))));
        final JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();

        final JsonArrayBuilder arrayBuilder =  Json.createArrayBuilder();
        defendantsInYouthCourt.stream().forEach(d -> arrayBuilder.add(d.toString()));
        jsonObjectBuilder.add("youthCourtDefendantIds", arrayBuilder.build());
        makeCommand(requestSpec, "hearing.youth-court-defendants")
                .withArgs(hearingId)
                .ofType("application/vnd.hearing.youth-court-defendants+json")
                .withPayload(jsonObjectBuilder.build().toString())
                .executeSuccessfully();

        return eventListener.waitFor();
    }
}