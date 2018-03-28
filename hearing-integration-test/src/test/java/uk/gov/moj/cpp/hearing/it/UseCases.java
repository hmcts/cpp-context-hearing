package uk.gov.moj.cpp.hearing.it;

import com.jayway.restassured.specification.RequestSpecification;
import uk.gov.moj.cpp.hearing.command.initiate.Address;
import uk.gov.moj.cpp.hearing.command.initiate.Defendant;
import uk.gov.moj.cpp.hearing.command.initiate.Hearing;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.initiate.Interpreter;
import uk.gov.moj.cpp.hearing.command.initiate.Offence;

import java.util.function.Consumer;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.INTEGER;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
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

    public static InitiateHearingCommand initiateHearingMultipleDefendants(RequestSpecification requestSpec, int requiredDefendantCount) {
        Consumer<InitiateHearingCommand.Builder> consumer = builder->{
            for (;builder.getHearing().getDefendants().size()<requiredDefendantCount;) {
                builder.getHearing().addDefendant(Defendant.builder()
                        .withId(randomUUID())
                        .withPersonId(randomUUID())
                        .withFirstName(STRING.next())
                        .withLastName(STRING.next())
                        .withNationality(STRING.next())
                        .withGender(STRING.next())
                        .withAddress(
                                Address.builder()
                                        .withAddress1(STRING.next())
                                        .withAddress2(STRING.next())
                                        .withAddress3(STRING.next())
                                        .withAddress4(STRING.next())
                                        .withPostCode(STRING.next())
                        )
                        .withDateOfBirth(PAST_LOCAL_DATE.next())
                        .withDefenceOrganisation(STRING.next())
                        .withInterpreter(
                                Interpreter.builder()
                                        .withNeeded(false)
                                        .withLanguage(STRING.next())
                        )
                        .addOffence(
                                Offence.builder()
                                        .withId(randomUUID())
                                        .withCaseId(builder.getCases().get(0).getCaseId())
                                        .withOffenceCode(STRING.next())
                                        .withWording(STRING.next())
                                        .withSection(STRING.next())
                                        .withStartDate(PAST_LOCAL_DATE.next())
                                        .withEndDate(PAST_LOCAL_DATE.next())
                                        .withOrderIndex(INTEGER.next())
                                        .withCount(INTEGER.next())
                                        .withConvictionDate(PAST_LOCAL_DATE.next())
                        )
                );
            }
        };
        return initiateHearing(requestSpec, consumer);
    }

}
