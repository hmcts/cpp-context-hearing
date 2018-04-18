package uk.gov.moj.cpp.hearing.it;

import org.junit.Test;
import uk.gov.moj.cpp.hearing.command.defenceCounsel.AddDefenceCounselCommand;
import uk.gov.moj.cpp.hearing.command.defenceCounsel.DefendantId;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;

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

public class DefenceCounselIT extends AbstractIT {

    @Test
    public void addDefenceCounsel_shouldAdd() throws Exception {

        InitiateHearingCommand initiateHearingCommand = initiateHearing(requestSpec, asDefault());

        AddDefenceCounselCommand firstDefenceCounsel = AddDefenceCounselCommand.builder()
                .withAttendeeId(randomUUID())
                .withPersonId(randomUUID())
                .withHearingId(initiateHearingCommand.getHearing().getId())
                .withFirstName(STRING.next())
                .withLastName(STRING.next())
                .withTitle(STRING.next())
                .withStatus(STRING.next())
                .addDefendantId(DefendantId.builder().withDefendantId(initiateHearingCommand.getHearing().getDefendants().get(0).getId()))
                .build();

        makeCommand(requestSpec, "hearing.update-hearing")
                .ofType("application/vnd.hearing.add-defence-counsel+json")
                .withArgs(initiateHearingCommand.getHearing().getId())
                .withPayload(firstDefenceCounsel)
                .executeSuccessfully();


        final String queryEndpoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.get.hearing.v2"), initiateHearingCommand.getHearing().getId());

        poll(requestParams(getBaseUri() + "/" + queryEndpoint, "application/vnd.hearing.get.hearing.v2+json")
                .withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.attendees.defenceCounsels.[0].attendeeId", is(firstDefenceCounsel.getAttendeeId().toString())),
                                withJsonPath("$.attendees.defenceCounsels.[0].status", is(firstDefenceCounsel.getStatus())),
                                withJsonPath("$.attendees.defenceCounsels.[0].firstName", is(firstDefenceCounsel.getFirstName())),
                                withJsonPath("$.attendees.defenceCounsels.[0].lastName", is(firstDefenceCounsel.getLastName())),
                                withJsonPath("$.attendees.defenceCounsels.[0].title", is(firstDefenceCounsel.getTitle()))
                        )));

        AddDefenceCounselCommand secondDefenceCounsel = AddDefenceCounselCommand.builder()
                .withAttendeeId(randomUUID())
                .withPersonId(randomUUID())
                .withHearingId(initiateHearingCommand.getHearing().getId())
                .withFirstName(STRING.next())
                .withLastName(STRING.next())
                .withTitle(STRING.next())
                .withStatus(STRING.next())
                .addDefendantId(DefendantId.builder().withDefendantId(initiateHearingCommand.getHearing().getDefendants().get(0).getId()))
                .build();

        makeCommand(requestSpec, "hearing.update-hearing")
                .ofType("application/vnd.hearing.add-defence-counsel+json")
                .withArgs(initiateHearingCommand.getHearing().getId())
                .withPayload(secondDefenceCounsel)
                .executeSuccessfully();

        poll(requestParams(getBaseUri() + "/" + queryEndpoint, "application/vnd.hearing.get.hearing.v2+json")
                .withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.attendees.defenceCounsels.[0].attendeeId", is(firstDefenceCounsel.getAttendeeId().toString())),
                                withJsonPath("$.attendees.defenceCounsels.[0].defendantId", is(firstDefenceCounsel.getDefendantIds().get(0).getDefendantId().toString())),
                                withJsonPath("$.attendees.defenceCounsels.[0].status", is(firstDefenceCounsel.getStatus())),
                                withJsonPath("$.attendees.defenceCounsels.[0].firstName", is(firstDefenceCounsel.getFirstName())),
                                withJsonPath("$.attendees.defenceCounsels.[0].lastName", is(firstDefenceCounsel.getLastName())),
                                withJsonPath("$.attendees.defenceCounsels.[0].title", is(firstDefenceCounsel.getTitle())),

                                withJsonPath("$.attendees.defenceCounsels.[1].attendeeId", is(secondDefenceCounsel.getAttendeeId().toString())),
                                withJsonPath("$.attendees.defenceCounsels.[1].defendantId", is(secondDefenceCounsel.getDefendantIds().get(0).getDefendantId().toString())),
                                withJsonPath("$.attendees.defenceCounsels.[1].status", is(secondDefenceCounsel.getStatus())),
                                withJsonPath("$.attendees.defenceCounsels.[1].firstName", is(secondDefenceCounsel.getFirstName())),
                                withJsonPath("$.attendees.defenceCounsels.[1].lastName", is(secondDefenceCounsel.getLastName())),
                                withJsonPath("$.attendees.defenceCounsels.[1].title", is(secondDefenceCounsel.getTitle()))
                        )));
    }


    @Test
    public void addDefenceCounsel_shouldEdit() throws Exception {

        InitiateHearingCommand initiateHearingCommand = initiateHearing(requestSpec, asDefault());

        AddDefenceCounselCommand firstDefenceCounsel = AddDefenceCounselCommand.builder()
                .withAttendeeId(randomUUID())
                .withPersonId(randomUUID())
                .withHearingId(initiateHearingCommand.getHearing().getId())
                .withFirstName(STRING.next())
                .withLastName(STRING.next())
                .withTitle(STRING.next())
                .withStatus(STRING.next())
                .addDefendantId(DefendantId.builder().withDefendantId(initiateHearingCommand.getHearing().getDefendants().get(0).getId()))
                .build();

        makeCommand(requestSpec, "hearing.update-hearing")
                .ofType("application/vnd.hearing.add-defence-counsel+json")
                .withArgs(initiateHearingCommand.getHearing().getId())
                .withPayload(firstDefenceCounsel)
                .executeSuccessfully();


        final String queryEndpoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.get.hearing.v2"), initiateHearingCommand.getHearing().getId());

        poll(requestParams(getBaseUri() + "/" + queryEndpoint, "application/vnd.hearing.get.hearing.v2+json")
                .withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.attendees.defenceCounsels.[0].attendeeId", is(firstDefenceCounsel.getAttendeeId().toString())),
                                withJsonPath("$.attendees.defenceCounsels.[0].status", is(firstDefenceCounsel.getStatus())),
                                withJsonPath("$.attendees.defenceCounsels.[0].firstName", is(firstDefenceCounsel.getFirstName())),
                                withJsonPath("$.attendees.defenceCounsels.[0].lastName", is(firstDefenceCounsel.getLastName())),
                                withJsonPath("$.attendees.defenceCounsels.[0].title", is(firstDefenceCounsel.getTitle()))
                        )));

        firstDefenceCounsel.withFirstName(STRING.next())
                .withLastName(STRING.next())
                .withStatus(STRING.next())
                .withTitle(STRING.next());

        makeCommand(requestSpec, "hearing.update-hearing")
                .ofType("application/vnd.hearing.add-defence-counsel+json")
                .withArgs(initiateHearingCommand.getHearing().getId())
                .withPayload(firstDefenceCounsel)
                .executeSuccessfully();

        poll(requestParams(getBaseUri() + "/" + queryEndpoint, "application/vnd.hearing.get.hearing.v2+json")
                .withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.attendees.defenceCounsels.[0].attendeeId", is(firstDefenceCounsel.getAttendeeId().toString())),
                                withJsonPath("$.attendees.defenceCounsels.[0].defendantId", is(firstDefenceCounsel.getDefendantIds().get(0).getDefendantId().toString())),
                                withJsonPath("$.attendees.defenceCounsels.[0].status", is(firstDefenceCounsel.getStatus())),
                                withJsonPath("$.attendees.defenceCounsels.[0].firstName", is(firstDefenceCounsel.getFirstName())),
                                withJsonPath("$.attendees.defenceCounsels.[0].lastName", is(firstDefenceCounsel.getLastName())),
                                withJsonPath("$.attendees.defenceCounsels.[0].title", is(firstDefenceCounsel.getTitle()))
                        )));
    }
}
