package uk.gov.moj.cpp.hearing.it;

import org.junit.Test;
import uk.gov.moj.cpp.hearing.command.prosecutionCounsel.AddProsecutionCounselCommand;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;

import java.util.concurrent.TimeUnit;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.core.Is.is;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.http.RestPoller.poll;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.AddProsecutionCounselCommandTemplates.addProsecutionCounselCommandTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;

@SuppressWarnings("unchecked")
public class ProsecutionCounselIT extends AbstractIT {

    @Test
    public void addProsecutionCounsel_shouldAdd() throws Exception {

        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(requestSpec, standardInitiateHearingTemplate()));

        final AddProsecutionCounselCommand firstProsecutionCounsel = UseCases.addProsecutionCounsel(requestSpec, hearingOne.getHearingId(),
                addProsecutionCounselCommandTemplate(hearingOne.getHearingId())
        );

        poll(requestParams(getURL("hearing.get.hearing.v2", hearingOne.getHearingId()), "application/vnd.hearing.get.hearing.v2+json")
                .withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .timeout(30, TimeUnit.SECONDS)
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.attendees.prosecutionCounsels.[0].attendeeId", is(firstProsecutionCounsel.getAttendeeId().toString())),
                                withJsonPath("$.attendees.prosecutionCounsels.[0].status", is(firstProsecutionCounsel.getStatus())),
                                withJsonPath("$.attendees.prosecutionCounsels.[0].firstName", is(firstProsecutionCounsel.getFirstName())),
                                withJsonPath("$.attendees.prosecutionCounsels.[0].lastName", is(firstProsecutionCounsel.getLastName())),
                                withJsonPath("$.attendees.prosecutionCounsels.[0].title", is(firstProsecutionCounsel.getTitle()))
                        )));

        final AddProsecutionCounselCommand secondProsecutionCounsel = UseCases.addProsecutionCounsel(requestSpec, hearingOne.getHearingId(),
                addProsecutionCounselCommandTemplate(hearingOne.getHearingId())
        );

        poll(requestParams(getURL("hearing.get.hearing.v2", hearingOne.getHearingId()), "application/vnd.hearing.get.hearing.v2+json")
                .withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.attendees.prosecutionCounsels.[0].attendeeId", is(firstProsecutionCounsel.getAttendeeId().toString())),
                                withJsonPath("$.attendees.prosecutionCounsels.[0].status", is(firstProsecutionCounsel.getStatus())),
                                withJsonPath("$.attendees.prosecutionCounsels.[0].firstName", is(firstProsecutionCounsel.getFirstName())),
                                withJsonPath("$.attendees.prosecutionCounsels.[0].lastName", is(firstProsecutionCounsel.getLastName())),
                                withJsonPath("$.attendees.prosecutionCounsels.[0].title", is(firstProsecutionCounsel.getTitle())),

                                withJsonPath("$.attendees.prosecutionCounsels.[1].attendeeId", is(secondProsecutionCounsel.getAttendeeId().toString())),
                                withJsonPath("$.attendees.prosecutionCounsels.[1].status", is(secondProsecutionCounsel.getStatus())),
                                withJsonPath("$.attendees.prosecutionCounsels.[1].firstName", is(secondProsecutionCounsel.getFirstName())),
                                withJsonPath("$.attendees.prosecutionCounsels.[1].lastName", is(secondProsecutionCounsel.getLastName())),
                                withJsonPath("$.attendees.prosecutionCounsels.[1].title", is(secondProsecutionCounsel.getTitle()))
                        )));
    }

    @Test
    public void addProsecutionCounsel_shouldEdit() throws Exception {

        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(requestSpec, standardInitiateHearingTemplate()));

        final AddProsecutionCounselCommand firstProsecutionCounsel = UseCases.addProsecutionCounsel(requestSpec, hearingOne.getHearingId(),
                addProsecutionCounselCommandTemplate(hearingOne.getHearingId())
        );

        poll(requestParams(getURL("hearing.get.hearing.v2", hearingOne.getHearingId()), "application/vnd.hearing.get.hearing.v2+json")
                .withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.attendees.prosecutionCounsels.[0].attendeeId", is(firstProsecutionCounsel.getAttendeeId().toString())),
                                withJsonPath("$.attendees.prosecutionCounsels.[0].status", is(firstProsecutionCounsel.getStatus())),
                                withJsonPath("$.attendees.prosecutionCounsels.[0].firstName", is(firstProsecutionCounsel.getFirstName())),
                                withJsonPath("$.attendees.prosecutionCounsels.[0].lastName", is(firstProsecutionCounsel.getLastName())),
                                withJsonPath("$.attendees.prosecutionCounsels.[0].title", is(firstProsecutionCounsel.getTitle()))
                        )));

        UseCases.addProsecutionCounsel(requestSpec, hearingOne.getHearingId(),
                with(firstProsecutionCounsel, counsel -> {
                    counsel.setFirstName(STRING.next())
                            .setLastName(STRING.next())
                            .setTitle(STRING.next())
                            .setStatus(STRING.next());
                })
        );

        poll(requestParams(getURL("hearing.get.hearing.v2", hearingOne.getHearingId()), "application/vnd.hearing.get.hearing.v2+json")
                .withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.attendees.prosecutionCounsels.[0].attendeeId", is(firstProsecutionCounsel.getAttendeeId().toString())),
                                withJsonPath("$.attendees.prosecutionCounsels.[0].status", is(firstProsecutionCounsel.getStatus())),
                                withJsonPath("$.attendees.prosecutionCounsels.[0].firstName", is(firstProsecutionCounsel.getFirstName())),
                                withJsonPath("$.attendees.prosecutionCounsels.[0].lastName", is(firstProsecutionCounsel.getLastName())),
                                withJsonPath("$.attendees.prosecutionCounsels.[0].title", is(firstProsecutionCounsel.getTitle()))
                        )));
    }
}
