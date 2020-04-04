package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.addInterpreterIntermediaryCommandTemplate;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_SEC;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.poll;

import uk.gov.justice.core.courts.Attendant;
import uk.gov.justice.core.courts.InterpreterIntermediary;
import uk.gov.justice.hearing.courts.AttendantType;
import uk.gov.justice.hearing.courts.RemoveInterpreterIntermediary;
import uk.gov.justice.hearing.courts.UpdateInterpreterIntermediary;
import uk.gov.justice.services.common.http.HeaderConstants;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;
import uk.gov.moj.cpp.hearing.test.TestTemplates;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class InterpreterIntermediaryIT extends AbstractIT {

    private static InterpreterIntermediary createInterpreterIntermediary(final InitiateHearingCommandHelper hearingOne, Attendant attendant) {

        final InterpreterIntermediary interpreterIntermediary = addInterpreterIntermediaryCommandTemplate(attendant);

        UseCases.addInterpreterIntermediary(getRequestSpec(), hearingOne.getHearingId(), interpreterIntermediary);

        poll(requestParams(getURL("hearing.get.hearing", hearingOne.getHearingId()), "application/vnd.hearing.get.hearing+json")
               .withHeader(HeaderConstants.USER_ID, AbstractIT.getLoggedInUser()).build())
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearing.intermediaries[0].role", is(interpreterIntermediary.getRole().toString())),
                                withJsonPath("$.hearing.intermediaries[0].firstName", is(interpreterIntermediary.getFirstName())),
                                withJsonPath("$.hearing.intermediaries[0].lastName", is(interpreterIntermediary.getLastName())),
                                withJsonPath("$.hearing.intermediaries[0].attendant.attendantType", is(interpreterIntermediary.getAttendant().getAttendantType().toString())),
                                attendant.getAttendantType().equals(AttendantType.WITNESS)
                                        ? withJsonPath("$.hearing.intermediaries[0].attendant.name", is(interpreterIntermediary.getAttendant().getName()))
                                        : withJsonPath("$.hearing.intermediaries[0].attendant.defendantId", is(interpreterIntermediary.getAttendant().getDefendantId().toString())),
                                withJsonPath("$.hearing.intermediaries[0].id", is(interpreterIntermediary.getId().toString())),
                                withJsonPath("$.hearing.id", is(hearingOne.getHearingId().toString()))
                        )));
        return interpreterIntermediary;
    }

    @Test
    public void addInterpreterIntermediary_shouldAdd() throws Exception {

        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));
        Attendant attendant = new Attendant(AttendantType.WITNESS, null, STRING.next());
        createInterpreterIntermediary(hearingOne, attendant);
        poll(requestParams(getURL("hearing.get.hearing", hearingOne.getHearingId()), "application/vnd.hearing.get.hearing+json")
                .withHeader(HeaderConstants.USER_ID, AbstractIT.getLoggedInUser()).build())
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearing.intermediaries", hasSize(1)))));

        Attendant secondattendant = new Attendant(AttendantType.DEFENDANTS, UUID.randomUUID(), null);
        createInterpreterIntermediary(hearingOne, secondattendant);

        poll(requestParams(getURL("hearing.get.hearing", hearingOne.getHearingId()), "application/vnd.hearing.get.hearing+json")
                .withHeader(HeaderConstants.USER_ID, AbstractIT.getLoggedInUser()).build())
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearing.intermediaries", hasSize(2)))));
    }

    @Test
    public void removeInterpreterIntermediary_shouldRemove() throws Exception {

        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));
        Attendant attendant = new Attendant(AttendantType.WITNESS, null, STRING.next());
        InterpreterIntermediary firstInterpreterIntermediary = createInterpreterIntermediary(hearingOne, attendant);
        poll(requestParams(getURL("hearing.get.hearing", hearingOne.getHearingId()), "application/vnd.hearing.get.hearing+json")
                .withHeader(HeaderConstants.USER_ID, AbstractIT.getLoggedInUser()).build())
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearing.intermediaries", hasSize(1)))));
        //remove first InterIntermediary
        UseCases.removeInterpreterIntermediary(getRequestSpec(), hearingOne.getHearingId(),
                new RemoveInterpreterIntermediary(hearingOne.getHearingId(), firstInterpreterIntermediary.getId())
        );
        poll(requestParams(getURL("hearing.get.hearing", hearingOne.getHearingId()), "application/vnd.hearing.get.hearing+json")
                .withHeader(HeaderConstants.USER_ID, AbstractIT.getLoggedInUser()).build())
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearing.intermediaries", hasSize(0)))));
        //Add another InterIntermediary
        Attendant anotherAttendant = new Attendant(AttendantType.DEFENDANTS, UUID.randomUUID(), null);
        createInterpreterIntermediary(hearingOne, anotherAttendant);
        poll(requestParams(getURL("hearing.get.hearing", hearingOne.getHearingId()), "application/vnd.hearing.get.hearing+json")
                .withHeader(HeaderConstants.USER_ID, AbstractIT.getLoggedInUser()).build())
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearing.intermediaries", hasSize(1)))));

    }

    @Test
    public void updateInterpreterIntermediary_shouldUpdate() throws Exception {

        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));
        Attendant attendant = new Attendant(AttendantType.WITNESS, null, STRING.next());
        InterpreterIntermediary firstInterpreterIntermediary = createInterpreterIntermediary(hearingOne, attendant);


        //Updating InterpreterIntermediary
        firstInterpreterIntermediary.setFirstName("DummyFirstName");
        firstInterpreterIntermediary.setLastName("DummyLastName");
        firstInterpreterIntermediary.setAttendanceDays(Arrays.asList(LocalDate.now().plusDays(1)));
        firstInterpreterIntermediary.setAttendant(Attendant.attendant().withAttendantType(AttendantType.DEFENDANTS).withDefendantId(UUID.randomUUID()).build());

        final UpdateInterpreterIntermediary firstInterpreterIntermediaryUpdateCommand = UseCases.updateInterpreterIntermediary(getRequestSpec(), hearingOne.getHearingId(),
                TestTemplates.UpdateInterpreterIntermediaryCommandTemplates.updateInterpreterIntermediaryCommandTemplate(hearingOne.getHearingId(), firstInterpreterIntermediary)
        );

        InterpreterIntermediary firstInterpreterIntermediaryUpdated = firstInterpreterIntermediaryUpdateCommand.getInterpreterIntermediary();
        poll(requestParams(getURL("hearing.get.hearing", hearingOne.getHearingId()), "application/vnd.hearing.get.hearing+json")
                .withHeader(HeaderConstants.USER_ID, AbstractIT.getLoggedInUser()).build())
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearing.intermediaries", hasSize(1)),
                                withJsonPath("$.hearing.intermediaries[0].id", is(firstInterpreterIntermediary.getId().toString())),
                                withJsonPath("$.hearing.intermediaries[0].firstName", is(firstInterpreterIntermediaryUpdated.getFirstName())),
                                withJsonPath("$.hearing.intermediaries[0].lastName", is(firstInterpreterIntermediaryUpdated.getLastName())),
                                withJsonPath("$.hearing.intermediaries[0].attendant.attendantType", is(firstInterpreterIntermediaryUpdated.getAttendant().getAttendantType().toString())),
                                withJsonPath("$.hearing.intermediaries[0].attendant.defendantId", is(firstInterpreterIntermediaryUpdated.getAttendant().getDefendantId().toString()))

                        )));

    }


}