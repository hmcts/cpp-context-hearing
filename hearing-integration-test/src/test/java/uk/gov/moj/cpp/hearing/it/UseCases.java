package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static com.jayway.restassured.RestAssured.given;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.it.TestUtilities.listenFor;
import static uk.gov.moj.cpp.hearing.it.TestUtilities.makeCommand;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.MapStringToTypeMatcher.convertStringTo;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.publicEvents;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.sendMessage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.ReadContext;
import com.jayway.jsonpath.internal.JsonContext;
import com.jayway.restassured.response.Header;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
import org.apache.http.HttpStatus;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import uk.gov.justice.progression.events.CaseDefendantDetails;
import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.hearing.command.defenceCounsel.AddDefenceCounselCommand;
import uk.gov.moj.cpp.hearing.command.defendant.UpdateDefendantAttendanceCommand;
import uk.gov.moj.cpp.hearing.command.hearingDetails.HearingDetailsUpdateCommand;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.logEvent.CorrectLogEventCommand;
import uk.gov.moj.cpp.hearing.command.logEvent.LogEventCommand;
import uk.gov.moj.cpp.hearing.command.offence.UpdateOffencesForDefendantCommand;
import uk.gov.moj.cpp.hearing.command.prosecutionCounsel.AddProsecutionCounselCommand;
import uk.gov.moj.cpp.hearing.command.result.ShareResultsCommand;
import uk.gov.moj.cpp.hearing.command.subscription.UploadSubscriptionsCommand;
import uk.gov.moj.cpp.hearing.command.verdict.HearingUpdateVerdictCommand;
import uk.gov.moj.cpp.hearing.domain.updatepleas.UpdatePleaCommand;
import uk.gov.moj.cpp.hearing.eventlog.CourtCentre;
import uk.gov.moj.cpp.hearing.eventlog.HearingEvent;
import uk.gov.moj.cpp.hearing.eventlog.HearingEventDefinition;
import uk.gov.moj.cpp.hearing.eventlog.PublicHearingEventLogged;
import uk.gov.moj.cpp.hearing.it.TestUtilities.EventListener;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class UseCases {

    public static <T> Consumer<T> asDefault() {
        return c -> {
        };
    }

    public static InitiateHearingCommand initiateHearing(final RequestSpecification requestSpec, final InitiateHearingCommand initiateHearing) {

        final TestUtilities.EventListener publicEventTopic = listenFor("public.hearing.initiated")
                .withFilter(isJson(withJsonPath("$.hearingId", is(initiateHearing.getHearing().getId().toString()))));

        makeCommand(requestSpec, "hearing.initiate")
                .ofType("application/vnd.hearing.initiate+json")
                .withPayload(initiateHearing)
                .executeSuccessfully();

        publicEventTopic.waitFor();

        return initiateHearing;
    }

    public static UpdatePleaCommand updatePlea(final RequestSpecification requestSpec, final UUID hearingId, final UUID offenceId,
                                               final UpdatePleaCommand hearingUpdatePleaCommand) {

        final EventListener eventListener = listenFor("public.hearing.plea-updated")
                .withFilter(isJson(withJsonPath("$.offenceId", is(offenceId.toString()))));

        makeCommand(requestSpec, "hearing.update-hearing")
                .withArgs(hearingId)
                .ofType("application/vnd.hearing.update-plea+json")
                .withPayload(hearingUpdatePleaCommand)
                .executeSuccessfully();

        eventListener.waitFor();

        return hearingUpdatePleaCommand;
    }

    public static HearingUpdateVerdictCommand updateVerdict(final RequestSpecification requestSpec, final UUID hearingId,
                                                            final HearingUpdateVerdictCommand hearingUpdateVerdictCommand) {

        final EventListener eventListener = listenFor("public.hearing.verdict-updated")
                .withFilter(isJson(withJsonPath("$.hearingId", is(hearingId.toString()))));

        makeCommand(requestSpec, "hearing.update-hearing")
                .ofType("application/vnd.hearing.update-verdict+json")
                .withArgs(hearingId)
                .withPayload(hearingUpdateVerdictCommand)
                .executeSuccessfully();

        eventListener.waitFor();

        return hearingUpdateVerdictCommand;
    }

    public static LogEventCommand logEvent(final RequestSpecification requestSpec,
                                           final Consumer<LogEventCommand.Builder> consumer,
                                           final InitiateHearingCommand initiateHearingCommand,
                                           final UUID hearingEventDefinitionId,
                                           final boolean alterable, final UUID defenceCounselId) {
        final LogEventCommand logEvent = with(
                LogEventCommand.builder()
                        .withHearingEventId(randomUUID())
                        .withHearingEventDefinitionId(hearingEventDefinitionId)
                        .withHearingId(initiateHearingCommand.getHearing().getId())
                        .withEventTime(PAST_ZONED_DATE_TIME.next().withZoneSameLocal(ZoneId.of("UTC")))
                        .withLastModifiedTime(PAST_ZONED_DATE_TIME.next().withZoneSameLocal(ZoneId.of("UTC")))
                        .withRecordedLabel(STRING.next())
                        .withDefenceCounselId(defenceCounselId)
                , consumer).build();


        final EventListener publicEventTopic = listenFor("public.hearing.event-logged")
                .withFilter(convertStringTo(PublicHearingEventLogged.class, isBean(PublicHearingEventLogged.class)
                        .with(PublicHearingEventLogged::getHearingEvent, isBean(uk.gov.moj.cpp.hearing.eventlog.HearingEvent.class)
                                .with(HearingEvent::getHearingEventId, is(logEvent.getHearingEventId()))
                                .with(HearingEvent::getRecordedLabel, is(logEvent.getRecordedLabel()))
                                .with(HearingEvent::getLastHearingEventId, is(nullValue()))
                                .with(HearingEvent::getEventTime, is(logEvent.getEventTime().withZoneSameInstant(ZoneId.of("UTC"))))
                                .with(HearingEvent::getLastModifiedTime, is(logEvent.getLastModifiedTime().withZoneSameInstant(ZoneId.of("UTC"))))

                        )
                        .with(PublicHearingEventLogged::getCase, isBean(uk.gov.moj.cpp.hearing.eventlog.Case.class)
                                .with(uk.gov.moj.cpp.hearing.eventlog.Case::getCaseUrn, is(initiateHearingCommand.getHearing().getProsecutionCases().get(0).getProsecutionCaseIdentifier().getCaseURN()))
                        )
                        .with(PublicHearingEventLogged::getHearingEventDefinition, isBean(HearingEventDefinition.class)
                                .with(HearingEventDefinition::getHearingEventDefinitionId, is(logEvent.getHearingEventDefinitionId()))
                                .with(HearingEventDefinition::isPriority, is(!alterable))
                        )
                        .with(PublicHearingEventLogged::getHearing, isBean(uk.gov.moj.cpp.hearing.eventlog.Hearing.class)
                                .with(uk.gov.moj.cpp.hearing.eventlog.Hearing::getCourtCentre, isBean(CourtCentre.class)
                                        .with(CourtCentre::getCourtCentreId, is(initiateHearingCommand.getHearing().getCourtCentre().getId()))
                                        .with(CourtCentre::getCourtCentreName, is(initiateHearingCommand.getHearing().getCourtCentre().getName()))
                                        .with(CourtCentre::getCourtRoomId, is(initiateHearingCommand.getHearing().getCourtCentre().getRoomId()))
                                        .with(CourtCentre::getCourtRoomName, is(initiateHearingCommand.getHearing().getCourtCentre().getRoomName()))
                                )
                                .with(uk.gov.moj.cpp.hearing.eventlog.Hearing::getHearingType, is(initiateHearingCommand.getHearing().getType().getDescription()))
                        )
                ));

        makeCommand(requestSpec, "hearing.log-hearing-event")
                .withArgs(initiateHearingCommand.getHearing().getId())
                .ofType("application/vnd.hearing.log-hearing-event+json")
                .withPayload(logEvent)
                .executeSuccessfully();

        publicEventTopic.waitFor();

        return logEvent;
    }

    public static CorrectLogEventCommand correctLogEvent(final RequestSpecification requestSpec,
                                                         final UUID hearingEventId,
                                                         final Consumer<CorrectLogEventCommand.Builder> consumer,
                                                         final InitiateHearingCommand initiateHearingCommand,
                                                         final UUID hearingEventDefinitionId,
                                                         final boolean alterable) {
        final CorrectLogEventCommand logEvent = with(
                CorrectLogEventCommand.builder()
                        .withLastestHearingEventId(randomUUID()) // the new event id.
                        .withHearingEventDefinitionId(hearingEventDefinitionId)
                        .withEventTime(PAST_ZONED_DATE_TIME.next().withZoneSameLocal(ZoneId.of("UTC")))
                        .withLastModifiedTime(PAST_ZONED_DATE_TIME.next().withZoneSameLocal(ZoneId.of("UTC")))
                        .withRecordedLabel(STRING.next())
                , consumer).withDefenceCounselId(randomUUID()).build();


        final EventListener publicEventTopic = listenFor("public.hearing.event-timestamp-corrected")
                .withFilter(convertStringTo(PublicHearingEventLogged.class, isBean(PublicHearingEventLogged.class)
                        .with(PublicHearingEventLogged::getHearingEvent, isBean(uk.gov.moj.cpp.hearing.eventlog.HearingEvent.class)
                                .with(HearingEvent::getHearingEventId, is(logEvent.getLatestHearingEventId()))
                                .with(HearingEvent::getRecordedLabel, is(logEvent.getRecordedLabel()))
                                .with(HearingEvent::getLastHearingEventId, is(hearingEventId))
                                .with(HearingEvent::getEventTime, is(logEvent.getEventTime().withZoneSameInstant(ZoneId.of("UTC"))))
                                .with(HearingEvent::getLastModifiedTime, is(logEvent.getLastModifiedTime().withZoneSameInstant(ZoneId.of("UTC"))))

                        )
                        .with(PublicHearingEventLogged::getCase, isBean(uk.gov.moj.cpp.hearing.eventlog.Case.class)
                                .with(uk.gov.moj.cpp.hearing.eventlog.Case::getCaseUrn, is(initiateHearingCommand.getHearing().getProsecutionCases().get(0).getProsecutionCaseIdentifier().getCaseURN()))
                        )
                        .with(PublicHearingEventLogged::getHearingEventDefinition, isBean(HearingEventDefinition.class)
                                .with(HearingEventDefinition::getHearingEventDefinitionId, is(logEvent.getHearingEventDefinitionId()))
                                .with(HearingEventDefinition::isPriority, is(!alterable))
                        )
                        .with(PublicHearingEventLogged::getHearing, isBean(uk.gov.moj.cpp.hearing.eventlog.Hearing.class)
                                .with(uk.gov.moj.cpp.hearing.eventlog.Hearing::getCourtCentre, isBean(CourtCentre.class)
                                        .with(CourtCentre::getCourtCentreId, is(initiateHearingCommand.getHearing().getCourtCentre().getId()))
                                        .with(CourtCentre::getCourtCentreName, is(initiateHearingCommand.getHearing().getCourtCentre().getName()))
                                        .with(CourtCentre::getCourtRoomId, is(initiateHearingCommand.getHearing().getCourtCentre().getRoomId()))
                                        .with(CourtCentre::getCourtRoomName, is(initiateHearingCommand.getHearing().getCourtCentre().getRoomName()))
                                )
                                .with(uk.gov.moj.cpp.hearing.eventlog.Hearing::getHearingType, is(initiateHearingCommand.getHearing().getType().getDescription()))
                        )
                ));

        makeCommand(requestSpec, "hearing.correct-hearing-event")
                .withArgs(initiateHearingCommand.getHearing().getId(), hearingEventId) //the original hearing event id
                .ofType("application/vnd.hearing.correct-hearing-event+json")
                .withPayload(logEvent)
                .executeSuccessfully();

        publicEventTopic.waitFor();

        return logEvent;
    }

    public static JsonObject updateHearingEvents(final RequestSpecification requestSpec,
                                                 final UUID hearingId, final List<uk.gov.moj.cpp.hearing.command.updateEvent.HearingEvent> hearingEvents,
                                                 final String updateEventsEndpoint, final Header headers) {
        final JsonArrayBuilder hearingEventsArray = Json.createArrayBuilder();
        hearingEvents.stream().forEach(event -> {
            final JsonObject hearingEvent = Json.createObjectBuilder()
                    .add("hearingEventId", event.getHearingEventId().toString())
                    .add("recordedLabel", event.getRecordedLabel())
                    .build();
            hearingEventsArray.add(hearingEvent);
        });
        final JsonObject payload =
                Json.createObjectBuilder()
                        .add("hearingEvents", hearingEventsArray).build();
        final TestUtilities.EventListener publicEventTopic =
                listenFor("public.hearing.events-updated")
                        .withFilter(isJson(allOf(new BaseMatcher<ReadContext>() {

                                                     @Override
                                                     public void describeTo(final Description description) {

                                                     }

                                                     @Override
                                                     public boolean matches(final Object o) {
                                                         return true;
                                                     }
                                                 }, withJsonPath("$.hearingId",
                                is(hearingId
                                        .toString())),
                                withJsonPath("$.hearingEvents[0].hearingEventId",
                                        is(hearingEvents.get(0).getHearingEventId()
                                                .toString())),
                                withJsonPath("$.hearingEvents[0].recordedLabel",
                                        is(hearingEvents.get(0)
                                                .getRecordedLabel()))


                        )));

        final Response writeResponse = given().spec(requestSpec).and()
                .contentType("application/vnd.hearing.update-hearing-events+json")
                .body(payload.toString()).header(headers).when()
                .post(updateEventsEndpoint).then().extract().response();
        assertThat(writeResponse.getStatusCode(), equalTo(HttpStatus.SC_ACCEPTED));
        publicEventTopic.waitFor();

        return payload;
    }


    public static LogEventCommand logEventThatIsIgnored(final RequestSpecification requestSpec,
                                                        final Consumer<LogEventCommand.Builder> consumer,
                                                        final UUID hearingId,
                                                        final UUID hearingEventDefinitionId,
                                                        final boolean alterable,
                                                        final String reason) {
        final LogEventCommand logEvent = with(
                LogEventCommand.builder()
                        .withHearingEventId(randomUUID())
                        .withHearingEventDefinitionId(hearingEventDefinitionId)
                        .withHearingId(hearingId)
                        .withEventTime(PAST_ZONED_DATE_TIME.next().withZoneSameLocal(ZoneId.of("UTC")))
                        .withLastModifiedTime(PAST_ZONED_DATE_TIME.next().withZoneSameLocal(ZoneId.of("UTC")))
                        .withRecordedLabel(STRING.next())
                , consumer).build();

        final TestUtilities.EventListener publicEventTopic = listenFor("public.hearing.event-ignored")
                .withFilter(isJson(allOf(
                        withJsonPath("$.hearingEventId", is(logEvent.getHearingEventId().toString())),
                        withJsonPath("$.hearingId", is(logEvent.getHearingId().toString())),
                        withJsonPath("$.alterable", is(alterable)),
                        withJsonPath("$.hearingEventDefinitionId", is(logEvent.getHearingEventDefinitionId().toString())),
                        withJsonPath("$.reason", is(reason)),
                        withJsonPath("$.recordedLabel", is(logEvent.getRecordedLabel())),
                        withJsonPath("$.eventTime", is(ZonedDateTimes.toString(logEvent.getEventTime())))

                )));

        makeCommand(requestSpec, "hearing.log-hearing-event")
                .withArgs(hearingId)
                .ofType("application/vnd.hearing.log-hearing-event+json")
                .withPayload(logEvent)
                .executeSuccessfully();

        publicEventTopic.waitFor();

        return logEvent;
    }


    public static ShareResultsCommand shareResults(final RequestSpecification requestSpec, final UUID hearingId, final ShareResultsCommand shareResultsCommand) {

        makeCommand(requestSpec, "hearing.share-results")
                .ofType("application/vnd.hearing.share-results+json")
                .withArgs(hearingId)
                .withPayload(shareResultsCommand)
                .executeSuccessfully();

        return shareResultsCommand;
    }

    public static AddDefenceCounselCommand addDefenceCounsel(final RequestSpecification requestSpec, final UUID hearingId,
                                                             final AddDefenceCounselCommand addDefenceCounselCommand) {

        makeCommand(requestSpec, "hearing.update-hearing")
                .ofType("application/vnd.hearing.add-defence-counsel+json")
                .withArgs(hearingId)
                .withPayload(addDefenceCounselCommand)
                .executeSuccessfully();

        return addDefenceCounselCommand;
    }

    public static AddProsecutionCounselCommand addProsecutionCounsel(final RequestSpecification requestSpec, final UUID hearingId,
                                                                     final AddProsecutionCounselCommand addProsecutionCounselCommand) {

        makeCommand(requestSpec, "hearing.update-hearing")
                .ofType("application/vnd.hearing.add-prosecution-counsel+json")
                .withArgs(hearingId)
                .withPayload(addProsecutionCounselCommand)
                .executeSuccessfully();

        return addProsecutionCounselCommand;
    }

    public static CaseDefendantDetails updateDefendants(CaseDefendantDetails caseDefendantDetails) throws Exception {

        final String eventName = "public.progression.case-defendant-changed";

        final ObjectMapper mapper = new ObjectMapperProducer().objectMapper();

        final JsonObject jsonObject = mapper.readValue(mapper.writeValueAsString(caseDefendantDetails), JsonObject.class);

        sendMessage(
                publicEvents.createProducer(),
                eventName,
                jsonObject,
                metadataWithRandomUUID(eventName).withUserId(randomUUID().toString()).build());

        return caseDefendantDetails;
    }


    public static UpdateOffencesForDefendantCommand updateOffences(UpdateOffencesForDefendantCommand updateOffencesForDefendantCommand) throws Exception {

        final String eventName = "public.progression.events.offences-for-defendant-updated";

        final ObjectMapper mapper = new ObjectMapperProducer().objectMapper();

        final String jsonValueAsString = mapper.writeValueAsString(updateOffencesForDefendantCommand);

        final JsonObject jsonObject = mapper.readValue(jsonValueAsString, JsonObject.class);

        sendMessage(
                publicEvents.createProducer(),
                eventName,
                jsonObject,
                metadataWithRandomUUID(eventName).withUserId(randomUUID().toString()).build());

        return updateOffencesForDefendantCommand;
    }

    public static HearingDetailsUpdateCommand updateHearing(HearingDetailsUpdateCommand hearingDetailsUpdateCommand) throws Exception {
        final String eventName = "public.hearing-detail-changed";

        final ObjectMapper mapper = new ObjectMapperProducer().objectMapper();

        final JsonObject jsonObject = mapper.readValue(mapper.writeValueAsString(hearingDetailsUpdateCommand), JsonObject.class);

        sendMessage(
                publicEvents.createProducer(),
                eventName,
                jsonObject,
                metadataWithRandomUUID(eventName).withUserId(randomUUID().toString()).build());

        return hearingDetailsUpdateCommand;
    }

    public static UploadSubscriptionsCommand uploadSubscriptions(final RequestSpecification requestSpec, final UploadSubscriptionsCommand uploadSubscriptionsCommand) {
        final String strToday = LocalDate.now().format(DateTimeFormatter.ofPattern("ddMMyyyy"));

        makeCommand(requestSpec, "hearing.upload-subscriptions")
                .ofType("application/vnd.hearing.upload-subscriptions+json")
                .withPayload(uploadSubscriptionsCommand)
                .withArgs(strToday)
                .executeSuccessfully();

        return uploadSubscriptionsCommand;
    }

    public static UpdateDefendantAttendanceCommand updateDefendantAttendance(final RequestSpecification requestSpec, final UpdateDefendantAttendanceCommand updateDefendantAttendanceCommand) {

        makeCommand(requestSpec, "hearing.update-defendant-attendance-on-hearing-day")
                .ofType("application/vnd.hearing.update-defendant-attendance-on-hearing-day+json")
                .withPayload(updateDefendantAttendanceCommand)
                .executeSuccessfully();

        return updateDefendantAttendanceCommand;
    }

}
