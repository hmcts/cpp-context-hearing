package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withoutJsonPath;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.http.RestPoller.poll;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.it.TestUtilities.listenFor;
import static uk.gov.moj.cpp.hearing.it.TestUtilities.makeCommand;
import static uk.gov.moj.cpp.hearing.it.UseCases.asDefault;
import static uk.gov.moj.cpp.hearing.it.UseCases.initiateHearing;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.hamcrest.core.Is;
import org.junit.Test;

import uk.gov.moj.cpp.hearing.command.DefendantId;
import uk.gov.moj.cpp.hearing.command.defenceCounsel.AddDefenceCounselCommand;
import uk.gov.moj.cpp.hearing.command.initiate.Hearing;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.prosecutionCounsel.AddProsecutionCounselCommand;

@SuppressWarnings({"unchecked", "serial"})
public class DeleteAttendeeHearingDayIT extends AbstractIT {

    @Test
    public void hearingSingleDay_shouldRemoveOnlyAttendeeAndHearingDayAssociationForAnEspecificAttendeeAndGivenDate() throws Exception {

        final InitiateHearingCommand initiateHearingCommand = initiateHearing(requestSpec, asDefault());

        final Hearing hearing = initiateHearingCommand.getHearing();

        UUID attendeeId = randomUUID();

        final AddDefenceCounselCommand defenceCounsel = AddDefenceCounselCommand.builder()
                .withAttendeeId(attendeeId)
                .withPersonId(randomUUID())
                .withHearingId(hearing.getId())
                .withFirstName(STRING.next())
                .withLastName(STRING.next())
                .withTitle(STRING.next())
                .withStatus(STRING.next())
                .addDefendantId(DefendantId.builder().withDefendantId(hearing.getDefendants().get(0).getId()))
                .build();

        final AddProsecutionCounselCommand prosecutionCounsel = AddProsecutionCounselCommand.builder()
                .withAttendeeId(randomUUID())
                .withPersonId(randomUUID())
                .withHearingId(hearing.getId())
                .withFirstName(STRING.next())
                .withLastName(STRING.next())
                .withTitle(STRING.next())
                .withStatus(STRING.next())
                .build();

        makeCommand(requestSpec, "hearing.update-hearing")
                .ofType("application/vnd.hearing.add-defence-counsel+json")
                .withArgs(hearing.getId())
                .withPayload(defenceCounsel)
                .executeSuccessfully();

        makeCommand(requestSpec, "hearing.update-hearing")
                .ofType("application/vnd.hearing.add-prosecution-counsel+json")
                .withArgs(hearing.getId())
                .withPayload(prosecutionCounsel)
                .executeSuccessfully();


        final Map<String, Object> deleteAttendeeCommand = new HashMap<String, Object>() {{
            put("hearingDate", hearing.getHearingDays().get(0).toLocalDate());
        }};

        makeCommand(requestSpec, "hearing.delete-attendee")
                .ofType("application/vnd.hearing.delete-attendee+json")
                .withArgs(hearing.getId(),defenceCounsel.getAttendeeId())
                .withPayload(deleteAttendeeCommand)
                .executeSuccessfully();

        final TestUtilities.EventListener publicEventTopic = listenFor("public.hearing.events.attendee-deleted")
                .withFilter(isJson(withJsonPath("$.attendeeId", Is.is(defenceCounsel.getAttendeeId().toString()))));

        publicEventTopic.waitFor();

        poll(requestParams(getURL("hearing.get.hearing.v2", hearing.getId()), "application/vnd.hearing.get.hearing.v2+json")
                .withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.attendees.defenceCounsels.[0].attendeeId", is(defenceCounsel.getAttendeeId().toString())),
                                withJsonPath("$.attendees.defenceCounsels.[0].status", is(defenceCounsel.getStatus())),
                                withJsonPath("$.attendees.defenceCounsels.[0].firstName", is(defenceCounsel.getFirstName())),
                                withJsonPath("$.attendees.defenceCounsels.[0].lastName", is(defenceCounsel.getLastName())),
                                withJsonPath("$.attendees.defenceCounsels.[0].title", is(defenceCounsel.getTitle())),
                                withoutJsonPath("$.attendees.defenceCounsels.[0].hearingDates"),

                                withJsonPath("$.attendees.prosecutionCounsels.[0].attendeeId", is(prosecutionCounsel.getAttendeeId().toString())),
                                withJsonPath("$.attendees.prosecutionCounsels.[0].status", is(prosecutionCounsel.getStatus())),
                                withJsonPath("$.attendees.prosecutionCounsels.[0].firstName", is(prosecutionCounsel.getFirstName())),
                                withJsonPath("$.attendees.prosecutionCounsels.[0].lastName", is(prosecutionCounsel.getLastName())),
                                withJsonPath("$.attendees.prosecutionCounsels.[0].title", is(prosecutionCounsel.getTitle())),
                                withJsonPath("$.attendees.prosecutionCounsels.[0].hearingDates[0]", equalDate(hearing.getHearingDays().get(0)))
                        )));
    }

    @Test
    public void hearingMultipleeDays_shouldRemoveAllAttendeeAndHearingDayAssociationsGreaterOrEqualAGivenDate() throws Exception {

        final InitiateHearingCommand initiateHearingCommand = initiateHearing(requestSpec, data -> {
            final ZonedDateTime startDateTime = data.getHearing().getStartDateTime();
            data.getHearing().withHearingDays(Arrays.asList(startDateTime, startDateTime.plusDays(1)));
        });

        final Hearing hearing = initiateHearingCommand.getHearing();

        final AddDefenceCounselCommand defenceCounsel = AddDefenceCounselCommand.builder()
                .withAttendeeId(randomUUID())
                .withPersonId(randomUUID())
                .withHearingId(hearing.getId())
                .withFirstName(STRING.next())
                .withLastName(STRING.next())
                .withTitle(STRING.next())
                .withStatus(STRING.next())
                .addDefendantId(DefendantId.builder().withDefendantId(hearing.getDefendants().get(0).getId()))
                .build();

        makeCommand(requestSpec, "hearing.update-hearing")
                .ofType("application/vnd.hearing.add-defence-counsel+json")
                .withArgs(hearing.getId())
                .withPayload(defenceCounsel)
                .executeSuccessfully();



        final Map<String, Object> deleteAttendeeCommand = new HashMap<String, Object>() {{
            put("hearingDate", hearing.getHearingDays().get(0).toLocalDate());
        }};

        makeCommand(requestSpec, "hearing.delete-attendee")
                .ofType("application/vnd.hearing.delete-attendee+json")
                .withArgs(hearing.getId(),defenceCounsel.getAttendeeId())
                .withPayload(deleteAttendeeCommand)
                .executeSuccessfully();

        final TestUtilities.EventListener publicEventTopic = listenFor("public.hearing.events.attendee-deleted")
                .withFilter(isJson(withJsonPath("$.attendeeId", Is.is(defenceCounsel.getAttendeeId().toString()))));

        publicEventTopic.waitFor();

        poll(requestParams(getURL("hearing.get.hearing.v2", hearing.getId()), "application/vnd.hearing.get.hearing.v2+json")
                .withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.attendees.defenceCounsels.[0].attendeeId", is(defenceCounsel.getAttendeeId().toString())),
                                withJsonPath("$.attendees.defenceCounsels.[0].status", is(defenceCounsel.getStatus())),
                                withJsonPath("$.attendees.defenceCounsels.[0].firstName", is(defenceCounsel.getFirstName())),
                                withJsonPath("$.attendees.defenceCounsels.[0].lastName", is(defenceCounsel.getLastName())),
                                withJsonPath("$.attendees.defenceCounsels.[0].title", is(defenceCounsel.getTitle())),
                                withoutJsonPath("$.attendees.defenceCounsels.[0].hearingDates")
                        )));
    }

    @Test
    public void hearingMultipleeDays_shouldRemoveOnlyAttendeeAndHearingDayAssociationsEqualAGivenDate() throws Exception {

        final InitiateHearingCommand initiateHearingCommand = initiateHearing(requestSpec, data -> {
            final ZonedDateTime startDateTime = data.getHearing().getStartDateTime();
            data.getHearing().withHearingDays(Arrays.asList(startDateTime, startDateTime.plusDays(1)));
        });

        final Hearing hearing = initiateHearingCommand.getHearing();

        final AddDefenceCounselCommand defenceCounsel = AddDefenceCounselCommand.builder()
                .withAttendeeId(randomUUID())
                .withPersonId(randomUUID())
                .withHearingId(hearing.getId())
                .withFirstName(STRING.next())
                .withLastName(STRING.next())
                .withTitle(STRING.next())
                .withStatus(STRING.next())
                .addDefendantId(DefendantId.builder().withDefendantId(hearing.getDefendants().get(0).getId()))
                .build();

        makeCommand(requestSpec, "hearing.update-hearing")
                .ofType("application/vnd.hearing.add-defence-counsel+json")
                .withArgs(hearing.getId())
                .withPayload(defenceCounsel)
                .executeSuccessfully();



        final Map<String, Object> deleteAttendeeCommand = new HashMap<String, Object>() {{
            put("hearingDate", hearing.getHearingDays().get(1).toLocalDate());
        }};

        makeCommand(requestSpec, "hearing.delete-attendee")
                .ofType("application/vnd.hearing.delete-attendee+json")
                .withArgs(hearing.getId(),defenceCounsel.getAttendeeId())
                .withPayload(deleteAttendeeCommand)
                .executeSuccessfully();

        final TestUtilities.EventListener publicEventTopic = listenFor("public.hearing.events.attendee-deleted")
                .withFilter(isJson(withJsonPath("$.attendeeId", Is.is(defenceCounsel.getAttendeeId().toString()))));

        publicEventTopic.waitFor();

        poll(requestParams(getURL("hearing.get.hearing.v2", hearing.getId()), "application/vnd.hearing.get.hearing.v2+json")
                .withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build()).timeout(20L, TimeUnit.SECONDS)
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.attendees.defenceCounsels.[0].attendeeId", is(defenceCounsel.getAttendeeId().toString())),
                                withJsonPath("$.attendees.defenceCounsels.[0].status", is(defenceCounsel.getStatus())),
                                withJsonPath("$.attendees.defenceCounsels.[0].firstName", is(defenceCounsel.getFirstName())),
                                withJsonPath("$.attendees.defenceCounsels.[0].lastName", is(defenceCounsel.getLastName())),
                                withJsonPath("$.attendees.defenceCounsels.[0].title", is(defenceCounsel.getTitle())),
                                withJsonPath("$.attendees.defenceCounsels.[0].hearingDates[0]", equalDate(hearing.getHearingDays().get(0)))
                        )));
    }
}