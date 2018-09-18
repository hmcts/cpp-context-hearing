package uk.gov.moj.cpp.hearing.it;

import org.junit.Ignore;
import org.junit.Test;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withoutJsonPath;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.services.test.utils.core.http.RestPoller.poll;
import static uk.gov.moj.cpp.hearing.it.Utilities.listenFor;

import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;


@SuppressWarnings({"unchecked", "serial"})
public class DeleteAttendeeHearingDayIT extends AbstractIT {

    @Ignore("GPE-5825")
    @Test
    public void hearingSingleDay_shouldRemoveOnlyAttendeeAndHearingDayAssociationForAnEspecificAttendeeAndGivenDate() throws Exception {

        /*final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(requestSpec, standardInitiateHearingTemplate()));

        final Hearing hearing = hearingOne.it().getHearing();

        final AddDefenceCounselCommand defenceCounsel = UseCases.addDefenceCounsel(requestSpec, hearingOne.getHearingId(),
                standardAddDefenceCounselCommandTemplate(hearingOne.getHearingId(), hearingOne.getFirstDefendantId())
        );

        final AddProsecutionCounselCommand prosecutionCounsel = AddProsecutionCounselCommand.builder()
                .withAttendeeId(randomUUID())
                .withPersonId(randomUUID())
                .withHearingId(hearingOne.getHearingId())
                .withFirstName(STRING.next())
                .withLastName(STRING.next())
                .withTitle(STRING.next())
                .withStatus(STRING.next())
                .build();

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
                .withArgs(hearing.getId(), defenceCounsel.getAttendeeId())
                .withPayload(deleteAttendeeCommand)
                .executeSuccessfully();

        final Utilities.EventListener publicEventTopic = listenFor("public.hearing.events.attendee-deleted")
                .withFilter(isJson(withJsonPath("$.attendeeId", is(defenceCounsel.getAttendeeId().toString()))));

        publicEventTopic.waitFor();

        poll(requestParams(getURL("hearing.get.hearing", hearing.getId()), "application/vnd.hearing.get.hearing+json")
                .withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .timeout(30, TimeUnit.SECONDS)
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
                        )));*/
    }

    @Ignore("GPE-5825")
    @Test
    public void hearingMultipleeDays_shouldRemoveAllAttendeeAndHearingDayAssociationsGreaterOrEqualAGivenDate() throws Exception {

        /*final InitiateHearingCommandHelper hearingOne = h(
                UseCases.initiateHearing(requestSpec, with(standardInitiateHearingTemplate(), data -> {
                    final ZonedDateTime startDateTime = ZonedDateTime.now();
                    data.getHearing().setHearingDays(Arrays.asList(startDateTime, startDateTime.plusDays(1)));
                }))
        );

        final AddDefenceCounselCommand defenceCounsel = UseCases.addDefenceCounsel(requestSpec, hearingOne.getHearingId(),
                standardAddDefenceCounselCommandTemplate(hearingOne.getHearingId(), hearingOne.getFirstDefendantId())
        );

        final Map<String, Object> deleteAttendeeCommand = new HashMap<String, Object>() {{
            put("hearingDate", hearingOne.it().getHearing().getHearingDays().get(0).toLocalDate());
        }};

        makeCommand(requestSpec, "hearing.delete-attendee")
                .ofType("application/vnd.hearing.delete-attendee+json")
                .withArgs(hearingOne.getHearingId(), defenceCounsel.getAttendeeId())
                .withPayload(deleteAttendeeCommand)
                .executeSuccessfully();

        final Utilities.EventListener publicEventTopic = listenFor("public.hearing.events.attendee-deleted")
                .withFilter(isJson(withJsonPath("$.attendeeId", Is.is(defenceCounsel.getAttendeeId().toString()))));

        publicEventTopic.waitFor();

        poll(requestParams(getURL("hearing.get.hearing", hearingOne.getHearingId()), "application/vnd.hearing.get.hearing+json")
                .withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .timeout(30, TimeUnit.SECONDS)
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.attendees.defenceCounsels.[0].attendeeId", is(defenceCounsel.getAttendeeId().toString())),
                                withJsonPath("$.attendees.defenceCounsels.[0].status", is(defenceCounsel.getStatus())),
                                withJsonPath("$.attendees.defenceCounsels.[0].firstName", is(defenceCounsel.getFirstName())),
                                withJsonPath("$.attendees.defenceCounsels.[0].lastName", is(defenceCounsel.getLastName())),
                                withJsonPath("$.attendees.defenceCounsels.[0].title", is(defenceCounsel.getTitle())),
                                withoutJsonPath("$.attendees.defenceCounsels.[0].hearingDates")
                        )));*/
    }

    @Ignore("GPE-5825")
    @Test
    public void hearingMultipleeDays_shouldRemoveOnlyAttendeeAndHearingDayAssociationsEqualAGivenDate() throws Exception {

        /*final InitiateHearingCommandHelper hearingOne = h(
                UseCases.initiateHearing(requestSpec, with(standardInitiateHearingTemplate(), data -> {
                    final ZonedDateTime startDateTime = ZonedDateTime.now();
                    data.getHearing().setHearingDays(Arrays.asList(startDateTime, startDateTime.plusDays(1)));
                }))
        );

        final AddDefenceCounselCommand defenceCounsel = UseCases.addDefenceCounsel(requestSpec, hearingOne.getHearingId(),
                standardAddDefenceCounselCommandTemplate(hearingOne.getHearingId(), hearingOne.getFirstDefendantId())
        );

        final Hearing hearing = hearingOne.it().getHearing();

        final Map<String, Object> deleteAttendeeCommand = new HashMap<String, Object>() {{
            put("hearingDate", hearing.getHearingDays().get(1).toLocalDate());
        }};

        makeCommand(requestSpec, "hearing.delete-attendee")
                .ofType("application/vnd.hearing.delete-attendee+json")
                .withArgs(hearing.getId(), defenceCounsel.getAttendeeId())
                .withPayload(deleteAttendeeCommand)
                .executeSuccessfully();

        final Utilities.EventListener publicEventTopic = listenFor("public.hearing.events.attendee-deleted")
                .withFilter(isJson(withJsonPath("$.attendeeId", Is.is(defenceCounsel.getAttendeeId().toString()))));

        publicEventTopic.waitFor();

        poll(requestParams(getURL("hearing.get.hearing", hearing.getId()), "application/vnd.hearing.get.hearing+json")
                .withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .timeout(30, TimeUnit.SECONDS)
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.attendees.defenceCounsels.[0].attendeeId", is(defenceCounsel.getAttendeeId().toString())),
                                withJsonPath("$.attendees.defenceCounsels.[0].status", is(defenceCounsel.getStatus())),
                                withJsonPath("$.attendees.defenceCounsels.[0].firstName", is(defenceCounsel.getFirstName())),
                                withJsonPath("$.attendees.defenceCounsels.[0].lastName", is(defenceCounsel.getLastName())),
                                withJsonPath("$.attendees.defenceCounsels.[0].title", is(defenceCounsel.getTitle())),
                                withJsonPath("$.attendees.defenceCounsels.[0].hearingDates[0]", equalDate(hearing.getHearingDays().get(0)))
                        )));*/
    }
}