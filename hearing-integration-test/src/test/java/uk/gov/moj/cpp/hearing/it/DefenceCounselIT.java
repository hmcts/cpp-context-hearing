package uk.gov.moj.cpp.hearing.it;

import org.junit.Test;
import uk.gov.moj.cpp.hearing.command.defenceCounsel.AddDefenceCounselCommand;
import uk.gov.moj.cpp.hearing.command.DefendantId;
import uk.gov.moj.cpp.hearing.command.initiate.Address;
import uk.gov.moj.cpp.hearing.command.initiate.Defendant;
import uk.gov.moj.cpp.hearing.command.initiate.DefendantCase;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.initiate.Interpreter;
import uk.gov.moj.cpp.hearing.command.initiate.Offence;

import java.text.MessageFormat;
import java.util.UUID;

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
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.FUTURE_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.INTEGER;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
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

        InitiateHearingCommand initiateHearingCommand = initiateHearing(requestSpec, (i) -> {
            i.getHearing().addDefendant(

                    Defendant.builder()
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
                            .addDefendantCase(
                                    DefendantCase.builder()
                                            .withCaseId(i.getCases().get(0).getCaseId())
                                            .withBailStatus(STRING.next())
                                            .withCustodyTimeLimitDate(FUTURE_LOCAL_DATE.next())
                            )
                            .addOffence(
                                    Offence.builder()
                                            .withId(randomUUID())
                                            .withCaseId(i.getCases().get(0).getCaseId())
                                            .withOffenceCode(STRING.next())
                                            .withWording(STRING.next())
                                            .withSection(STRING.next())
                                            .withStartDate(PAST_LOCAL_DATE.next())
                                            .withEndDate(PAST_LOCAL_DATE.next())
                                            .withOrderIndex(INTEGER.next())
                                            .withCount(INTEGER.next())
                                            .withConvictionDate(PAST_LOCAL_DATE.next())
                                            .withLegislation(STRING.next())
                                            .withTitle(STRING.next())
                            )
            );
        });

        AddDefenceCounselCommand addDefenceCounselCommand = AddDefenceCounselCommand.builder()
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
                .withPayload(addDefenceCounselCommand)
                .executeSuccessfully();


        final String queryEndpoint = MessageFormat
                .format(ENDPOINT_PROPERTIES.getProperty("hearing.get.hearing.v2"), initiateHearingCommand.getHearing().getId());

        poll(requestParams(getBaseUri() + "/" + queryEndpoint, "application/vnd.hearing.get.hearing.v2+json")
                .withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.attendees.defenceCounsels.[0].attendeeId", is(addDefenceCounselCommand.getAttendeeId().toString())),
                                withJsonPath("$.attendees.defenceCounsels.[0].status", is(addDefenceCounselCommand.getStatus())),
                                withJsonPath("$.attendees.defenceCounsels.[0].firstName", is(addDefenceCounselCommand.getFirstName())),
                                withJsonPath("$.attendees.defenceCounsels.[0].lastName", is(addDefenceCounselCommand.getLastName())),
                                withJsonPath("$.attendees.defenceCounsels.[0].title", is(addDefenceCounselCommand.getTitle()))
                        )));

        UUID secondDefendantId = initiateHearingCommand.getHearing().getDefendants().get(1).getId();

        addDefenceCounselCommand.withFirstName(STRING.next())
                .withLastName(STRING.next())
                .withStatus(STRING.next())
                .withTitle(STRING.next())
                .clearAllDefendantIds()
                .addDefendantId(DefendantId.builder().withDefendantId(secondDefendantId).build());

        makeCommand(requestSpec, "hearing.update-hearing")
                .ofType("application/vnd.hearing.add-defence-counsel+json")
                .withArgs(initiateHearingCommand.getHearing().getId())
                .withPayload(addDefenceCounselCommand)
                .executeSuccessfully();

        poll(requestParams(getBaseUri() + "/" + queryEndpoint, "application/vnd.hearing.get.hearing.v2+json")
                .withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.attendees.defenceCounsels.[0].attendeeId", is(addDefenceCounselCommand.getAttendeeId().toString())),
                                withJsonPath("$.attendees.defenceCounsels.[0].defendantId", is(initiateHearingCommand.getHearing().getDefendants().get(1).getId().toString())),
                                withJsonPath("$.attendees.defenceCounsels.[0].status", is(addDefenceCounselCommand.getStatus())),
                                withJsonPath("$.attendees.defenceCounsels.[0].firstName", is(addDefenceCounselCommand.getFirstName())),
                                withJsonPath("$.attendees.defenceCounsels.[0].lastName", is(addDefenceCounselCommand.getLastName())),
                                withJsonPath("$.attendees.defenceCounsels.[0].title", is(addDefenceCounselCommand.getTitle()))
                        )));
    }
}
