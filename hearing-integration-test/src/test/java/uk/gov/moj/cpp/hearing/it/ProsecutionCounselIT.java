package uk.gov.moj.cpp.hearing.it;

import org.junit.Test;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.prosecutionCounsel.AddProsecutionCounselCommand;

import java.text.MessageFormat;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.core.Is.is;
import static uk.gov.justice.services.test.utils.core.http.BaseUriProvider.getBaseUri;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.http.RestPoller.poll;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.it.TestUtilities.makeCommand;
import static uk.gov.moj.cpp.hearing.it.UseCases.asDefault;
import static uk.gov.moj.cpp.hearing.it.UseCases.initiateHearing;

public class ProsecutionCounselIT extends AbstractIT {


    @Test
    public void addProsecutionCounsel_shouldAdd() throws Exception {

        InitiateHearingCommand initiateHearingCommand = initiateHearing(requestSpec, asDefault());

        AddProsecutionCounselCommand firstProsecutionCounsel = AddProsecutionCounselCommand.builder()
                .withAttendeeId(randomUUID())
                .withPersonId(randomUUID())
                .withHearingId(initiateHearingCommand.getHearing().getId())
                .withFirstName(STRING.next())
                .withLastName(STRING.next())
                .withTitle(STRING.next())
                .withStatus(STRING.next())
                .build();


        makeCommand(requestSpec, "hearing.update-hearing")
                .ofType("application/vnd.hearing.add-prosecution-counsel+json")
                .withArgs(initiateHearingCommand.getHearing().getId())
                .withPayload(firstProsecutionCounsel)
                .executeSuccessfully();


        final String queryEndpoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.get.hearing.v2"), initiateHearingCommand.getHearing().getId());

        poll(requestParams(getBaseUri() + "/" + queryEndpoint, "application/vnd.hearing.get.hearing.v2+json")
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


        AddProsecutionCounselCommand secondProsecutionCounsel = AddProsecutionCounselCommand.builder()
                .withAttendeeId(randomUUID())
                .withPersonId(randomUUID())
                .withHearingId(initiateHearingCommand.getHearing().getId())
                .withFirstName(STRING.next())
                .withLastName(STRING.next())
                .withTitle(STRING.next())
                .withStatus(STRING.next())
                .build();

        makeCommand(requestSpec, "hearing.update-hearing")
                .ofType("application/vnd.hearing.add-prosecution-counsel+json")
                .withArgs(initiateHearingCommand.getHearing().getId())
                .withPayload(secondProsecutionCounsel)
                .executeSuccessfully();

        poll(requestParams(getBaseUri() + "/" + queryEndpoint, "application/vnd.hearing.get.hearing.v2+json")
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

        InitiateHearingCommand initiateHearingCommand = initiateHearing(requestSpec, asDefault());

        AddProsecutionCounselCommand firstProsecutionCounsel = AddProsecutionCounselCommand.builder()
                .withAttendeeId(randomUUID())
                .withPersonId(randomUUID())
                .withHearingId(initiateHearingCommand.getHearing().getId())
                .withFirstName(STRING.next())
                .withLastName(STRING.next())
                .withTitle(STRING.next())
                .withStatus(STRING.next())
                .build();

        makeCommand(requestSpec, "hearing.update-hearing")
                .ofType("application/vnd.hearing.add-prosecution-counsel+json")
                .withArgs(initiateHearingCommand.getHearing().getId())
                .withPayload(firstProsecutionCounsel)
                .executeSuccessfully();

        final String queryEndpoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.get.hearing.v2"), initiateHearingCommand.getHearing().getId());

        poll(requestParams(getBaseUri() + "/" + queryEndpoint, "application/vnd.hearing.get.hearing.v2+json")
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

        firstProsecutionCounsel.setFirstName(STRING.next())
                .setLastName(STRING.next())
                .setTitle(STRING.next())
                .setStatus(STRING.next());

        makeCommand(requestSpec, "hearing.update-hearing")
                .ofType("application/vnd.hearing.add-prosecution-counsel+json")
                .withArgs(initiateHearingCommand.getHearing().getId())
                .withPayload(firstProsecutionCounsel)
                .executeSuccessfully();

        poll(requestParams(getBaseUri() + "/" + queryEndpoint, "application/vnd.hearing.get.hearing.v2+json")
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
