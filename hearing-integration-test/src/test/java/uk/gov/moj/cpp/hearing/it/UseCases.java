package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withoutJsonPath;
import static com.jayway.restassured.RestAssured.given;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataOf;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.BOOLEAN;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.INTEGER;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.integer;
import static uk.gov.moj.cpp.hearing.it.TestUtilities.listenFor;
import static uk.gov.moj.cpp.hearing.it.TestUtilities.makeCommand;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.initiateHearingCommandTemplate;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.publicEvents;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.sendMessage;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;

import org.apache.http.HttpStatus;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.ReadContext;
import com.jayway.jsonpath.internal.JsonContext;
import com.jayway.restassured.response.Header;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;

import uk.gov.justice.progression.events.CaseDefendantOffencesChanged;
import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.hearing.command.initiate.Hearing;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.logEvent.CorrectLogEventCommand;
import uk.gov.moj.cpp.hearing.command.logEvent.LogEventCommand;
import uk.gov.moj.cpp.hearing.command.offence.DefendantCaseOffence;
import uk.gov.moj.cpp.hearing.command.plea.HearingUpdatePleaCommand;
import uk.gov.moj.cpp.hearing.command.updateEvent.HearingEvent;
import uk.gov.moj.cpp.hearing.command.verdict.Defendant;
import uk.gov.moj.cpp.hearing.command.verdict.HearingUpdateVerdictCommand;
import uk.gov.moj.cpp.hearing.command.verdict.Offence;
import uk.gov.moj.cpp.hearing.command.verdict.Verdict;
import uk.gov.moj.cpp.hearing.command.verdict.VerdictValue;
import uk.gov.moj.cpp.hearing.it.PleaIT.PleaValueType;
import uk.gov.moj.cpp.hearing.it.TestUtilities.EventListener;

@SuppressWarnings("unchecked")
public class UseCases {

    public static <T> Consumer<T> asDefault() {
        return c -> {
        };
    }

    public static InitiateHearingCommand initiateHearing(final RequestSpecification requestSpec, final Consumer<InitiateHearingCommand.Builder> consumer) {
        final InitiateHearingCommand initiateHearing = with(initiateHearingCommandTemplate(), consumer).build();

        final Hearing hearing = initiateHearing.getHearing();

        final TestUtilities.EventListener publicEventTopic = listenFor("public.hearing.initiated")
                .withFilter(isJson(withJsonPath("$.hearingId", is(hearing.getId().toString()))));

        makeCommand(requestSpec, "hearing.initiate")
                .ofType("application/vnd.hearing.initiate+json")
                .withPayload(initiateHearing)
                .executeSuccessfully();

        publicEventTopic.waitFor();

        return initiateHearing;
    }

    public static HearingUpdatePleaCommand updatePlea(final RequestSpecification requestSpec, final InitiateHearingCommand initiateHearingCommand,
                                                      final Consumer<HearingUpdatePleaCommand.Builder> consumer) {

        final UUID offenceId = initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getId();

        final HearingUpdatePleaCommand.Builder updatePleaCommandBuilder = HearingUpdatePleaCommand.builder()
                .withCaseId(initiateHearingCommand.getCases().get(0).getCaseId())
                .addDefendant(uk.gov.moj.cpp.hearing.command.plea.Defendant.builder()
                        .withId(initiateHearingCommand.getHearing().getDefendants().get(0).getId())
                        .addOffence(uk.gov.moj.cpp.hearing.command.plea.Offence.builder()
                                .withId(offenceId)
                                .withPlea(uk.gov.moj.cpp.hearing.command.plea.Plea.builder()
                                        .withId(UUID.randomUUID())
                                        .withPleaDate(PAST_LOCAL_DATE.next())
                                        .withValue("GUILTY"))));

        consumer.accept(updatePleaCommandBuilder);
        final HearingUpdatePleaCommand hearingUpdatePleaCommand = updatePleaCommandBuilder.build();

        final EventListener eventListener = listenFor("public.hearing.plea-updated")
                .withFilter(isJson(withJsonPath("$.offenceId", is(offenceId.toString()))));

        makeCommand(requestSpec, "hearing.update-hearing")
                .withArgs(initiateHearingCommand.getHearing().getId())
                .ofType("application/vnd.hearing.update-plea+json")
                .withPayload(hearingUpdatePleaCommand)
                .executeSuccessfully();

        eventListener.waitFor();

        return hearingUpdatePleaCommand;
    }

    public static HearingUpdateVerdictCommand updateVerdict(final RequestSpecification requestSpec, final InitiateHearingCommand initiateHearingCommand,
                                                      final Consumer<HearingUpdateVerdictCommand.Builder> consumer) {

        final HearingUpdateVerdictCommand.Builder hearingUpdateVerdictCommandBuilder = HearingUpdateVerdictCommand.builder()
                .withCaseId(initiateHearingCommand.getCases().get(0).getCaseId())
                .addDefendant(
                        Defendant.builder().withId(initiateHearingCommand.getHearing().getDefendants().get(0).getId())
                                .withPersonId(randomUUID())
                                .addOffence(Offence.builder()
                                        .withId(initiateHearingCommand.getHearing().getDefendants().get(0).getOffences()
                                                .get(0).getId())
                                        .withVerdict(Verdict.builder().withId(randomUUID())
                                                .withValue(VerdictValue.builder().withId(randomUUID())
                                                        .withCategory("GUILTY").withCode("A1")
                                                        .withDescription(STRING.next())

                                                ).withNumberOfJurors(integer(9, 12).next())
                                                .withNumberOfSplitJurors(integer(0, 3).next())
                                                .withUnanimous(BOOLEAN.next())
                                                .withVerdictDate(PAST_LOCAL_DATE.next()))));

        consumer.accept(hearingUpdateVerdictCommandBuilder);
        final HearingUpdateVerdictCommand hearingUpdateVerdictCommand = hearingUpdateVerdictCommandBuilder.build();

        final EventListener eventListener = listenFor("public.hearing.verdict-updated").withFilter(
                isJson(withJsonPath("$.hearingId", is(initiateHearingCommand.getHearing().getId().toString()))));

        makeCommand(requestSpec, "hearing.update-hearing")
                .ofType("application/vnd.hearing.update-verdict+json")
                .withArgs(initiateHearingCommand.getHearing().getId())
                .withPayload(hearingUpdateVerdictCommand)
                .executeSuccessfully();

        eventListener.waitFor();

        return hearingUpdateVerdictCommand;
    }




    public static UUID initiateHearingWithOffenceAndPlea(final RequestSpecification requestSpec,
                                                         final PleaValueType pleaValueType, final LocalDate pleaDate) throws Throwable {

        final InitiateHearingCommand initiateHearingCommand = initiateHearingCommandTemplate().build();
        final UUID hearingId = initiateHearingCommand.getHearing().getId();

        EventListener eventListener = listenFor("public.hearing.initiated")
                .withFilter(isJson(withJsonPath("$.hearingId", is(hearingId.toString()))));

        makeCommand(requestSpec, "hearing.initiate")
                .ofType("application/vnd.hearing.initiate+json")
                .withPayload(initiateHearingCommand)
                .executeSuccessfully();

        eventListener.waitFor();

        final UUID offenceId = initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getId();

        eventListener = listenFor("public.hearing.plea-updated")
                .withFilter(isJson(withJsonPath("$.offenceId", is(offenceId.toString()))));

        final UUID caseId = initiateHearingCommand.getCases().get(0).getCaseId();
        final UUID defendantId = initiateHearingCommand.getHearing().getDefendants().get(0).getId();

        final HearingUpdatePleaCommand updatePleaCommand = HearingUpdatePleaCommand.builder()
                .withCaseId(caseId)
                .addDefendant(uk.gov.moj.cpp.hearing.command.plea.Defendant.builder()
                        .withId(defendantId)
                        .addOffence(uk.gov.moj.cpp.hearing.command.plea.Offence.builder()
                                .withId(offenceId)
                                .withPlea(uk.gov.moj.cpp.hearing.command.plea.Plea.builder()
                                        .withId(UUID.randomUUID())
                                        .withPleaDate(pleaDate)
                                        .withValue(pleaValueType.name()))))
                .build();

        makeCommand(requestSpec, "hearing.update-plea")
                .withArgs(hearingId)
                .ofType("application/vnd.hearing.update-plea+json")
                .withPayload(updatePleaCommand)
                .executeSuccessfully();

        eventListener.waitFor();

        return offenceId;
    }

    public static LogEventCommand logEvent(final RequestSpecification requestSpec,
                                           final Consumer<LogEventCommand.Builder> consumer,
                                           final InitiateHearingCommand initiateHearingCommand,
                                           final UUID hearingEventDefinitionId,
                    final boolean alterable, final UUID witnessId, final UUID ccounselId) {
        final LogEventCommand logEvent = with(
                LogEventCommand.builder()
                        .withHearingEventId(randomUUID())
                        .withHearingEventDefinitionId(hearingEventDefinitionId)
                        .withHearingId(initiateHearingCommand.getHearing().getId())
                        .withEventTime(PAST_ZONED_DATE_TIME.next())
                        .withLastModifiedTime(PAST_ZONED_DATE_TIME.next())
                        .withRecordedLabel(STRING.next())
                        .withWitnessId(witnessId)
                                        .withCounselId(ccounselId)
                , consumer).build();

        final TestUtilities.EventListener publicEventTopic = listenFor("public.hearing.event-logged")
                .withFilter(isJson(allOf(
                        new BaseMatcher<ReadContext>() {

                            @Override
                            public void describeTo(final Description description) {

                            }

                            @Override
                            public boolean matches(final Object o) {
                                final JsonContext r = (JsonContext) o;
                                System.out.println(r.jsonString());
                                return true;
                            }
                        },
                        withJsonPath("$.hearingEvent.hearingEventId", is(logEvent.getHearingEventId().toString())),
                        withJsonPath("$.hearingEvent.recordedLabel", is(logEvent.getRecordedLabel())),
                        withoutJsonPath("$.hearingEvent.lastHearingEventId"),
                        withJsonPath("$.hearingEvent.eventTime", is(ZonedDateTimes.toString(logEvent.getEventTime()))),
                        withJsonPath("$.hearingEvent.lastModifiedTime", is(ZonedDateTimes.toString(logEvent.getLastModifiedTime()))),

                        withJsonPath("$.hearingEventDefinition.priority", is(!alterable)),
                        withJsonPath("$.hearingEventDefinition.hearingEventDefinitionId", is(logEvent.getHearingEventDefinitionId().toString())),

                        withJsonPath("$.case.caseUrn", is(initiateHearingCommand.getCases().get(0).getUrn())),

                        withJsonPath("$.hearing.courtCentre.courtCentreId", is(initiateHearingCommand.getHearing().getCourtCentreId().toString())),
                        withJsonPath("$.hearing.courtCentre.courtCentreName", is(initiateHearingCommand.getHearing().getCourtCentreName())),
                        withJsonPath("$.hearing.courtCentre.courtRoomId", is(initiateHearingCommand.getHearing().getCourtRoomId().toString())),
                        withJsonPath("$.hearing.courtCentre.courtRoomName", is(initiateHearingCommand.getHearing().getCourtRoomName())),
                        withJsonPath("$.hearing.hearingType", is(initiateHearingCommand.getHearing().getType()))

                )));

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
                        .withEventTime(PAST_ZONED_DATE_TIME.next())
                        .withLastModifiedTime(PAST_ZONED_DATE_TIME.next())
                        .withRecordedLabel(STRING.next())
                , consumer).build();

        final TestUtilities.EventListener publicEventTopic = listenFor("public.hearing.event-timestamp-corrected")
                .withFilter(isJson(allOf(
                        new BaseMatcher<ReadContext>() {

                            @Override
                            public void describeTo(final Description description) {

                            }

                            @Override
                            public boolean matches(final Object o) {
                                final JsonContext r = (JsonContext) o;
                                System.out.println("public hearing corrected");
                                System.out.println(r.jsonString());
                                return true;
                            }
                        },
                        withJsonPath("$.hearingEvent.hearingEventId", is(logEvent.getLatestHearingEventId().toString())),
                        withJsonPath("$.hearingEvent.lastHearingEventId", is(hearingEventId.toString())),
                        withJsonPath("$.hearingEvent.recordedLabel", is(logEvent.getRecordedLabel())),
                        withJsonPath("$.hearingEvent.eventTime", is(ZonedDateTimes.toString(logEvent.getEventTime()))),
                        withJsonPath("$.hearingEvent.lastModifiedTime", is(ZonedDateTimes.toString(logEvent.getLastModifiedTime()))),
                        withJsonPath("$.hearingEventDefinition.priority", is(!alterable)),
                        withJsonPath("$.hearingEventDefinition.hearingEventDefinitionId", is(logEvent.getHearingEventDefinitionId().toString())),
                        withJsonPath("$.case.caseUrn", is(initiateHearingCommand.getCases().get(0).getUrn())),
                        withJsonPath("$.hearing.courtCentre.courtCentreId", is(initiateHearingCommand.getHearing().getCourtCentreId().toString())),
                        withJsonPath("$.hearing.courtCentre.courtCentreName", is(initiateHearingCommand.getHearing().getCourtCentreName())),
                        withJsonPath("$.hearing.courtCentre.courtRoomId", is(initiateHearingCommand.getHearing().getCourtRoomId().toString())),
                        withJsonPath("$.hearing.courtCentre.courtRoomName", is(initiateHearingCommand.getHearing().getCourtRoomName())),
                        withJsonPath("$.hearing.hearingType", is(initiateHearingCommand.getHearing().getType()))


                )));

        makeCommand(requestSpec, "hearing.correct-hearing-event")
                .withArgs(initiateHearingCommand.getHearing().getId(), hearingEventId) //the original hearing event id
                .ofType("application/vnd.hearing.correct-hearing-event+json")
                .withPayload(logEvent)
                .executeSuccessfully();

        publicEventTopic.waitFor();

        return logEvent;
    }

    public static JsonObject updateHearingEvents(final RequestSpecification requestSpec,
                    final UUID hearingId, final List<HearingEvent> hearingEvents,
                    final String updateEventsEndpoint, final Header headers) {
        final JsonArrayBuilder hearingEventsArray = Json.createArrayBuilder();
        hearingEvents.stream().forEach(event -> {
            final JsonObject hearingEvent = Json.createObjectBuilder()
                            .add("hearingEventId", event.getHearingEventId().toString())
                            .add("hearingEventDefinitionId",
                                            event.getHearingEventDefinitionId().toString())
                            .add("lastModifiedTime", event.getLastModifiedTime().toString())
                            .add("recordedLabel", event.getRecordedLabel())
                            .add("eventTime", event.getEventTime().toString())
                            .add("witnessId", event.getWitnessId().toString())
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
                                                                        is(hearingEvents.get(0).getRecordedLabel())),
                                                        withJsonPath("$.hearingEvents[0].eventTime",
                                                                        is(ZonedDateTimes.toString(
                                                                                        hearingEvents.get(0).getEventTime()))),
                                                        withJsonPath("$.hearingEvents[0].lastModifiedTime",
                                                                        is(ZonedDateTimes.toString(
                                                                                        hearingEvents.get(0).getLastModifiedTime()))),

                                                        withJsonPath("$.hearingEvents[0].hearingEventDefinitionId",
                                                                        is(hearingEvents.get(0).getHearingEventDefinitionId()
                                                                                        .toString())),
                                                        withJsonPath("$.hearingEvents[0].witnessId",
                                                                        is(hearingEvents.get(0)
                                                                                        .getWitnessId()
                                                                                        .toString()))

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
                        .withEventTime(PAST_ZONED_DATE_TIME.next())
                        .withLastModifiedTime(PAST_ZONED_DATE_TIME.next())
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

    public static CaseDefendantOffencesChanged addOffence(final InitiateHearingCommand initiateHearingCommand) throws Exception {

        final Hearing hearing = initiateHearingCommand.getHearing();

        final String eventName = "public.progression.defendant-offences-changed";

        final List<DefendantCaseOffence> addOffences = Arrays.asList(
                DefendantCaseOffence.builder()
                        .withDefendantId(hearing.getDefendants().get(0).getId())
                        .withCaseId(initiateHearingCommand.getCases().get(0).getCaseId())
                        .withAddedOffences(Arrays.asList(
                                uk.gov.moj.cpp.hearing.command.offence.Offence.builder()
                                        .withId(randomUUID())
                                        .withOffenceCode(STRING.next())
                                        .withWording(STRING.next())
                                        .withStartDate(PAST_LOCAL_DATE.next())
                                        .withEndDate(PAST_LOCAL_DATE.next())
                                        .withCount(INTEGER.next())
                                        .withConvictionDate(PAST_LOCAL_DATE.next())
                                        .build())).build());

        final CaseDefendantOffencesChanged caseDefendantOffencesChanged = CaseDefendantOffencesChanged.builder()
                .withModifiedDate(LocalDate.now())
                .withAddedOffences(addOffences)
                .build();

        final ObjectMapper mapper = new ObjectMapperProducer().objectMapper();

        final JsonObject jsonObject = mapper.readValue(mapper.writeValueAsString(caseDefendantOffencesChanged), JsonObject.class);

        sendPublicEvent(hearing.getId(), eventName, jsonObject);

        return caseDefendantOffencesChanged;
    }

    public static CaseDefendantOffencesChanged updateOffence(final InitiateHearingCommand initiateHearingCommand) throws Exception {

        final Hearing hearing = initiateHearingCommand.getHearing();

        final String eventName = "public.progression.defendant-offences-changed";

        final List<uk.gov.moj.cpp.hearing.command.offence.Offence> updatedOffences = Collections.singletonList(convertOffence(hearing));

        final CaseDefendantOffencesChanged caseDefendantOffencesChanged = CaseDefendantOffencesChanged.builder()
                .withModifiedDate(LocalDate.now())
                .withUpdateOffences(updatedOffences)
                .build();

        final ObjectMapper mapper = new ObjectMapperProducer().objectMapper();

        final JsonObject jsonObject = mapper.readValue(mapper.writeValueAsString(caseDefendantOffencesChanged), JsonObject.class);

        sendPublicEvent(hearing.getId(), eventName, jsonObject);

        return caseDefendantOffencesChanged;
    }

    public static CaseDefendantOffencesChanged deleteOffence(final InitiateHearingCommand initiateHearingCommand) throws Exception {

        final Hearing hearing = initiateHearingCommand.getHearing();

        final String eventName = "public.progression.defendant-offences-changed";

        final List<UUID> deletedOffences = Collections.singletonList(hearing.getDefendants().get(0).getOffences().get(0).getId());

        final CaseDefendantOffencesChanged caseDefendantOffencesChanged = CaseDefendantOffencesChanged.builder()
                .withModifiedDate(LocalDate.now())
                .withDeletedOffences(deletedOffences)
                .build();

        final ObjectMapper mapper = new ObjectMapperProducer().objectMapper();

        final JsonObject jsonObject = mapper.readValue(mapper.writeValueAsString(caseDefendantOffencesChanged), JsonObject.class);

        sendPublicEvent(hearing.getId(), eventName, jsonObject);

        return caseDefendantOffencesChanged;
    }

    private static void sendPublicEvent(final UUID uuid, final String eventName, final JsonObject jsonObject) {
        sendMessage(
                publicEvents.createProducer(),
                eventName,
                jsonObject,
                metadataOf(uuid, eventName).withUserId(randomUUID().toString()).build());
    }

    private static uk.gov.moj.cpp.hearing.command.offence.Offence convertOffence(final Hearing hearing) {
        final Function<uk.gov.moj.cpp.hearing.command.initiate.Offence, uk.gov.moj.cpp.hearing.command.offence.Offence> mapOffence = o -> uk.gov.moj.cpp.hearing.command.offence.Offence.builder()
                .withId(o.getId())
                .withOffenceCode(STRING.next())
                .withWording(STRING.next())
                .withStartDate(PAST_LOCAL_DATE.next())
                .withEndDate(PAST_LOCAL_DATE.next())
                .withCount(INTEGER.next())
                .withConvictionDate(PAST_LOCAL_DATE.next())
                .build();

        return mapOffence.apply(hearing.getDefendants().get(0).getOffences().get(0));
    }
}