package uk.gov.moj.cpp.hearing.it;

import com.jayway.restassured.specification.RequestSpecification;
import uk.gov.moj.cpp.hearing.command.initiate.Hearing;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;

import java.util.function.Consumer;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.it.TestUtilities.initiateHearingCommandTemplate;
import static uk.gov.moj.cpp.hearing.it.TestUtilities.listenFor;
import static uk.gov.moj.cpp.hearing.it.TestUtilities.makeCommand;
import static uk.gov.moj.cpp.hearing.it.TestUtilities.with;

public class UseCases {

    public static InitiateHearingCommand initiateHearing(RequestSpecification requestSpec, Consumer<InitiateHearingCommand.Builder> consumer) {
        InitiateHearingCommand initiateHearing = with(initiateHearingCommandTemplate(), consumer).build();

        final Hearing hearing = initiateHearing.getHearing();

        TestUtilities.EventListener publicEventTopic = listenFor("public.hearing.initiated")
                .withFilter(isJson(withJsonPath("$.hearingId", is(hearing.getId().toString()))));

        makeCommand(requestSpec, "hearing.initiate")
                .ofType("application/vnd.hearing.initiate+json")
                .withPayload(initiateHearing)
                .executeSuccessfully();

        publicEventTopic.waitFor();

        return initiateHearing;
    }
}
