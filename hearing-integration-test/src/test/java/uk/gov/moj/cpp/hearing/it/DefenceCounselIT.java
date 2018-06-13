package uk.gov.moj.cpp.hearing.it;

import org.junit.Test;
import uk.gov.moj.cpp.hearing.command.DefendantId;
import uk.gov.moj.cpp.hearing.command.defenceCounsel.AddDefenceCounselCommand;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.core.Is.is;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.http.RestPoller.poll;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.AddDefenceCounselCommandTemplates.standardAddDefenceCounselCommandTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.defendantTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;

@SuppressWarnings("unchecked")
public class DefenceCounselIT extends AbstractIT {

    @Test
    public void addDefenceCounsel_shouldAdd() throws Exception {

        final CommandHelpers.InitiateHearingCommandHelper hearingOne = new CommandHelpers.InitiateHearingCommandHelper(
                UseCases.initiateHearing(requestSpec, standardInitiateHearingTemplate().build())
        );

        final AddDefenceCounselCommand firstDefenceCounsel = UseCases.addDefenceCounsel(requestSpec, hearingOne.getHearingId(),
                standardAddDefenceCounselCommandTemplate(hearingOne.getHearingId(), hearingOne.getFirstDefendantId())
        );

        poll(requestParams(getURL("hearing.get.hearing.v2", hearingOne.getHearingId()), "application/vnd.hearing.get.hearing.v2+json")
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

        final AddDefenceCounselCommand secondDefenceCounsel = UseCases.addDefenceCounsel(requestSpec, hearingOne.getHearingId(),
                standardAddDefenceCounselCommandTemplate(hearingOne.getHearingId(), hearingOne.getFirstDefendantId())
        );

        poll(requestParams(getURL("hearing.get.hearing.v2", hearingOne.getHearingId()), "application/vnd.hearing.get.hearing.v2+json")
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

        final CommandHelpers.InitiateHearingCommandHelper hearingOne = new CommandHelpers.InitiateHearingCommandHelper(
                UseCases.initiateHearing(requestSpec, with(standardInitiateHearingTemplate(), i -> {
                    i.getHearing().getDefendants().add(defendantTemplate(i.getCases().get(0).getCaseId()));
                }).build())
        );

        final AddDefenceCounselCommand firstDefenceCounsel = UseCases.addDefenceCounsel(requestSpec, hearingOne.getHearingId(),
                standardAddDefenceCounselCommandTemplate(hearingOne.getHearingId(), hearingOne.getFirstDefendantId())
        );

        poll(requestParams(getURL("hearing.get.hearing.v2", hearingOne.getHearingId()), "application/vnd.hearing.get.hearing.v2+json")
                .withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.attendees.defenceCounsels.[0].attendeeId", is(firstDefenceCounsel.getAttendeeId().toString())),
                                withJsonPath("$.attendees.defenceCounsels.[0].defendantId", is(hearingOne.getFirstDefendantId().toString())),
                                withJsonPath("$.attendees.defenceCounsels.[0].status", is(firstDefenceCounsel.getStatus())),
                                withJsonPath("$.attendees.defenceCounsels.[0].firstName", is(firstDefenceCounsel.getFirstName())),
                                withJsonPath("$.attendees.defenceCounsels.[0].lastName", is(firstDefenceCounsel.getLastName())),
                                withJsonPath("$.attendees.defenceCounsels.[0].title", is(firstDefenceCounsel.getTitle()))
                        )));

        UseCases.addDefenceCounsel(requestSpec, hearingOne.getHearingId(), with(firstDefenceCounsel, counsel -> {
            counsel.clearAllDefendantIds();
            counsel.addDefendantId(DefendantId.builder().withDefendantId(hearingOne.getSecondDefendantId()).build());
        }));

        poll(requestParams(getURL("hearing.get.hearing.v2", hearingOne.getHearingId()), "application/vnd.hearing.get.hearing.v2+json")
                .withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.attendees.defenceCounsels.[0].attendeeId", is(firstDefenceCounsel.getAttendeeId().toString())),
                                withJsonPath("$.attendees.defenceCounsels.[0].defendantId", is(hearingOne.getSecondDefendantId().toString())),
                                withJsonPath("$.attendees.defenceCounsels.[0].status", is(firstDefenceCounsel.getStatus())),
                                withJsonPath("$.attendees.defenceCounsels.[0].firstName", is(firstDefenceCounsel.getFirstName())),
                                withJsonPath("$.attendees.defenceCounsels.[0].lastName", is(firstDefenceCounsel.getLastName())),
                                withJsonPath("$.attendees.defenceCounsels.[0].title", is(firstDefenceCounsel.getTitle()))
                        )));
    }
}
