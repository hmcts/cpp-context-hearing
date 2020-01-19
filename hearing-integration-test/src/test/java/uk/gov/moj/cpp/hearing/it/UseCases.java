package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static com.jayway.restassured.RestAssured.given;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.it.Utilities.listenFor;
import static uk.gov.moj.cpp.hearing.it.Utilities.makeCommand;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.MapStringToTypeMatcher.convertStringTo;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.getPublicTopicInstance;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.sendMessage;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationOutcome;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.InterpreterIntermediary;
import uk.gov.justice.core.courts.Marker;
import uk.gov.justice.core.courts.Target;
import uk.gov.justice.hearing.courts.AddApplicantCounsel;
import uk.gov.justice.hearing.courts.AddCompanyRepresentative;
import uk.gov.justice.hearing.courts.AddDefenceCounsel;
import uk.gov.justice.hearing.courts.AddProsecutionCounsel;
import uk.gov.justice.hearing.courts.AddRespondentCounsel;
import uk.gov.justice.hearing.courts.RemoveApplicantCounsel;
import uk.gov.justice.hearing.courts.RemoveCompanyRepresentative;
import uk.gov.justice.hearing.courts.RemoveDefenceCounsel;
import uk.gov.justice.hearing.courts.RemoveInterpreterIntermediary;
import uk.gov.justice.hearing.courts.RemoveProsecutionCounsel;
import uk.gov.justice.hearing.courts.RemoveRespondentCounsel;
import uk.gov.justice.hearing.courts.UpdateApplicantCounsel;
import uk.gov.justice.hearing.courts.UpdateCompanyRepresentative;
import uk.gov.justice.hearing.courts.UpdateDefenceCounsel;
import uk.gov.justice.hearing.courts.UpdateInterpreterIntermediary;
import uk.gov.justice.hearing.courts.UpdateProsecutionCounsel;
import uk.gov.justice.hearing.courts.UpdateRespondentCounsel;
import uk.gov.justice.progression.events.CaseDefendantDetails;
import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.messaging.JsonObjects;
import uk.gov.moj.cpp.hearing.command.TrialType;
import uk.gov.moj.cpp.hearing.command.defendant.UpdateDefendantAttendanceCommand;
import uk.gov.moj.cpp.hearing.command.hearingDetails.HearingDetailsUpdateCommand;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.logEvent.CorrectLogEventCommand;
import uk.gov.moj.cpp.hearing.command.logEvent.LogEventCommand;
import uk.gov.moj.cpp.hearing.command.offence.UpdateOffencesForDefendantCommand;
import uk.gov.moj.cpp.hearing.command.result.SaveDraftResultCommand;
import uk.gov.moj.cpp.hearing.command.result.ShareResultsCommand;
import uk.gov.moj.cpp.hearing.command.result.SharedResultsCommandPrompt;
import uk.gov.moj.cpp.hearing.command.result.SharedResultsCommandResultLine;
import uk.gov.moj.cpp.hearing.command.subscription.UploadSubscriptionsCommand;
import uk.gov.moj.cpp.hearing.command.verdict.HearingUpdateVerdictCommand;
import uk.gov.moj.cpp.hearing.domain.updatepleas.UpdatePleaCommand;
import uk.gov.moj.cpp.hearing.event.PublicHearingDraftResultSaved;
import uk.gov.moj.cpp.hearing.eventlog.CourtCentre;
import uk.gov.moj.cpp.hearing.eventlog.HearingEvent;
import uk.gov.moj.cpp.hearing.eventlog.HearingEventDefinition;
import uk.gov.moj.cpp.hearing.eventlog.PublicHearingEventLogged;
import uk.gov.moj.cpp.hearing.it.Utilities.EventListener;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.ReadContext;
import com.jayway.restassured.response.Header;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
import org.apache.http.HttpStatus;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

public class UseCases {

    private static final String FIELD_OVERRIDE = "override";

    public static <T> Consumer<T> asDefault() {
        return c -> {
        };
    }

    public static InitiateHearingCommand initiateHearing(final RequestSpecification requestSpec, final InitiateHearingCommand initiateHearing) {

        return initiateHearing(requestSpec, initiateHearing, true);
    }

    public static InitiateHearingCommand initiateHearing(final RequestSpecification requestSpec, final InitiateHearingCommand initiateHearing, boolean includeApplication) {

        final Utilities.EventListener publicEventTopic = listenFor("public.hearing.initiated")
                .withFilter(isJson(withJsonPath("$.hearingId", is(initiateHearing.getHearing().getId().toString()))));

        if (!includeApplication) {
            initiateHearing.getHearing().setCourtApplications(null);
        }

        makeCommand(requestSpec, "hearing.initiate")
                .ofType("application/vnd.hearing.initiate+json")
                .withPayload(initiateHearing)
                .executeSuccessfully();

        publicEventTopic.waitFor();

        return initiateHearing;
    }

    public static void verifyIgnoreInitiateHearing(final RequestSpecification requestSpec, final InitiateHearingCommand initiateHearing) {

        final Utilities.EventListener publicEventTopic = listenFor("public.hearing.initiate-ignored")
                .withFilter(isJson(withJsonPath("$.hearingId", is(initiateHearing.getHearing().getId().toString()))));

        makeCommand(requestSpec, "hearing.initiate")
                .ofType("application/vnd.hearing.initiate+json")
                .withPayload(initiateHearing)
                .executeSuccessfully();

        publicEventTopic.waitFor();

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
                                           final boolean alterable,
                                           final UUID defenceCounselId,
                                           final ZonedDateTime eventTime) {
        final LogEventCommand logEvent = with(
                LogEventCommand.builder()
                        .withHearingEventId(randomUUID())
                        .withHearingEventDefinitionId(hearingEventDefinitionId)
                        .withHearingId(initiateHearingCommand.getHearing().getId())
                        .withEventTime(eventTime)
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

    public static LogEventCommand logEventForOverrideCourtRoom(final RequestSpecification requestSpec,
                                                               final Consumer<LogEventCommand.Builder> consumer,
                                                               final InitiateHearingCommand initiateHearingCommand,
                                                               final UUID hearingEventDefinitionId,
                                                               final boolean alterable,
                                                               final UUID defenceCounselId,
                                                               final boolean override,
                                                               final ZonedDateTime eventTime) throws JsonProcessingException {
        final LogEventCommand logEvent = with(
                LogEventCommand.builder()
                        .withHearingEventId(randomUUID())
                        .withHearingEventDefinitionId(hearingEventDefinitionId)
                        .withHearingId(initiateHearingCommand.getHearing().getId())
                        .withEventTime(eventTime)
                        .withLastModifiedTime(PAST_ZONED_DATE_TIME.next().withZoneSameLocal(ZoneId.of("UTC")))
                        .withRecordedLabel(STRING.next())
                        .withDefenceCounselId(defenceCounselId)
                , consumer).build();

        final JsonObject payloadWithOverrideCourtRoomFlag = JsonObjects.createObjectBuilder(Utilities.JsonUtil.objectToJsonObject(logEvent))
                .add(FIELD_OVERRIDE, override)
                .build();

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
                                )
                                .with(uk.gov.moj.cpp.hearing.eventlog.Hearing::getHearingType, is(initiateHearingCommand.getHearing().getType().getDescription()))
                        )
                ));

        makeCommand(requestSpec, "hearing.log-hearing-event")
                .withArgs(initiateHearingCommand.getHearing().getId())
                .ofType("application/vnd.hearing.log-hearing-event+json")
                .withPayload(payloadWithOverrideCourtRoomFlag.toString())
                .executeSuccessfully();

        publicEventTopic.waitFor();

        return logEvent;
    }

    public static CorrectLogEventCommand correctLogEvent(final RequestSpecification requestSpec,
                                                         final UUID hearingEventId,
                                                         final Consumer<CorrectLogEventCommand.Builder> consumer,
                                                         final InitiateHearingCommand initiateHearingCommand,
                                                         final UUID hearingEventDefinitionId,
                                                         final boolean alterable,
                                                         final ZonedDateTime eventTime) {
        final CorrectLogEventCommand logEvent = with(
                CorrectLogEventCommand.builder()
                        .withLastestHearingEventId(randomUUID()) // the new event id.
                        .withHearingEventDefinitionId(hearingEventDefinitionId)
                        .withEventTime(eventTime)
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
        final Utilities.EventListener publicEventTopic =
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

        final Utilities.EventListener publicEventTopic = listenFor("public.hearing.event-ignored")
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


    public static SaveDraftResultCommand saveDraftResults(final RequestSpecification requestSpec, SaveDraftResultCommand saveDraftResultCommand) {

        final EventListener publicEventResulted = listenFor("public.hearing.draft-result-saved")
                .withFilter(convertStringTo(PublicHearingDraftResultSaved.class, isBean(PublicHearingDraftResultSaved.class)
                        .with(PublicHearingDraftResultSaved::getTargetId, is(saveDraftResultCommand.getTarget().getTargetId()))
                        .with(PublicHearingDraftResultSaved::getHearingId, is(saveDraftResultCommand.getTarget().getHearingId()))
                        .with(PublicHearingDraftResultSaved::getDefendantId, is(saveDraftResultCommand.getTarget().getDefendantId()))
                        .with(PublicHearingDraftResultSaved::getDraftResult, is(saveDraftResultCommand.getTarget().getDraftResult()))
                        .with(PublicHearingDraftResultSaved::getOffenceId, is(saveDraftResultCommand.getTarget().getOffenceId())
                        )));

        makeCommand(requestSpec, "hearing.save-draft-result")
                .ofType("application/vnd.hearing.save-draft-result+json")
                .withArgs(saveDraftResultCommand.getTarget().getHearingId())
                .withPayload(saveDraftResultCommand.getTarget())
                .executeSuccessfully();

        publicEventResulted.waitFor();

        return saveDraftResultCommand;
    }

    public static SaveDraftResultCommand saveDraftResultsApplication(final RequestSpecification requestSpec, SaveDraftResultCommand saveDraftResultCommand) {
        //dummy save draft to use existing pattern

        return saveDraftResultCommand;
    }

    private static Stream<SharedResultsCommandResultLine> sharedResultsCommandResultLineStream(final Target target) {
        return sharedResultsCommandResultLineStream(target, null);
    }

    private static Stream<SharedResultsCommandResultLine> sharedResultsCommandResultLineStream(final Target target, final CourtApplicationOutcome courtApplicationOutcome) {
        return target.getResultLines().stream().map(resultLineIn ->
                new SharedResultsCommandResultLine(resultLineIn.getDelegatedPowers(),
                        resultLineIn.getOrderedDate(),
                        resultLineIn.getSharedDate(),
                        resultLineIn.getResultLineId(),
                        target.getTargetId(),
                        target.getOffenceId(),
                        target.getDefendantId(),
                        resultLineIn.getResultDefinitionId(),
                        resultLineIn.getPrompts().stream().map(p -> new SharedResultsCommandPrompt(p.getId(), p.getLabel(),
                                p.getFixedListCode(), p.getValue(), p.getWelshValue(), p.getWelshLabel())).collect(Collectors.toList()),
                        resultLineIn.getResultLabel(),
                        resultLineIn.getLevel().name(),
                        resultLineIn.getIsModified(),
                        resultLineIn.getIsComplete(),
                        target.getApplicationId(),
                        resultLineIn.getAmendmentReasonId(),
                        resultLineIn.getAmendmentReason(),
                        resultLineIn.getAmendmentDate(),
                        resultLineIn.getFourEyesApproval(),
                        resultLineIn.getApprovedDate(),
                        resultLineIn.getIsDeleted(),
                        courtApplicationOutcome));
    }

    public static ShareResultsCommand shareResults(final RequestSpecification requestSpec, final UUID hearingId, final ShareResultsCommand shareResultsCommand, final List<Target> targets) {

        return shareResults(requestSpec, hearingId, shareResultsCommand, targets, null);
    }

    public static ShareResultsCommand shareResults(final RequestSpecification requestSpec, final UUID hearingId, final ShareResultsCommand shareResultsCommand, final List<Target> targets, final CourtApplicationOutcome courtApplicationOutcome) {

        // TODO GPE-6699
        shareResultsCommand.setResultLines(
                targets.stream()
                        .flatMap(target -> sharedResultsCommandResultLineStream(target, courtApplicationOutcome))
                        .collect(Collectors.toList()));


        makeCommand(requestSpec, "hearing.share-results")
                .ofType("application/vnd.hearing.share-results+json")
                .withArgs(hearingId)
                .withPayload(shareResultsCommand)
                .executeSuccessfully();

        return shareResultsCommand;
    }

    public static JsonObject saveHearingCaseNote(final RequestSpecification requestSpec, final UUID hearingId, final JsonObject hearingCaseNote) {

        makeCommand(requestSpec, "hearing.save-hearing-case-note")
                .ofType("application/vnd.hearing.save-hearing-case-note+json")
                .withArgs(hearingId)
                .withPayload(hearingCaseNote)
                .executeSuccessfully();

        return hearingCaseNote;
    }

    public static AddDefenceCounsel addDefenceCounsel(final RequestSpecification requestSpec, final UUID hearingId,
                                                      final AddDefenceCounsel addDefenceCounsel) {

        makeCommand(requestSpec, "hearing.update-hearing")
                .ofType("application/vnd.hearing.add-defence-counsel+json")
                .withArgs(hearingId)
                .withPayload(addDefenceCounsel)
                .executeSuccessfully();

        return addDefenceCounsel;
    }

    public static AddCompanyRepresentative addCompanyRepresentative(final RequestSpecification requestSpec, final UUID hearingId,
                                                                    final AddCompanyRepresentative addCompanyRepresentative) {

        makeCommand(requestSpec, "hearing.update-hearing")
                .ofType("application/vnd.hearing.add-company-representative+json")
                .withArgs(hearingId)
                .withPayload(addCompanyRepresentative)
                .executeSuccessfully();

        return addCompanyRepresentative;
    }

    public static UpdateCompanyRepresentative updateCompanyRepresentative(final RequestSpecification requestSpec, final UUID hearingId, final UpdateCompanyRepresentative updateCompanyRepresentative) {

        makeCommand(requestSpec, "hearing.update-hearing")
                .ofType("application/vnd.hearing.update-company-representative+json")
                .withArgs(hearingId)
                .withPayload(updateCompanyRepresentative)
                .executeSuccessfully();

        return updateCompanyRepresentative;
    }

    public static RemoveCompanyRepresentative removeCompanyRepresentative(final RequestSpecification requestSpec, final UUID hearingId, final RemoveCompanyRepresentative removeCompanyRepresentative) {

        makeCommand(requestSpec, "hearing.update-hearing")
                .ofType("application/vnd.hearing.remove-company-representative+json")
                .withArgs(hearingId)
                .withPayload(removeCompanyRepresentative)
                .executeSuccessfully();

        return removeCompanyRepresentative;
    }

    public static AddProsecutionCounsel addProsecutionCounsel(final RequestSpecification requestSpec, final UUID hearingId,
                                                              final AddProsecutionCounsel addProsecutionCounsel) {

        makeCommand(requestSpec, "hearing.update-hearing")
                .ofType("application/vnd.hearing.add-prosecution-counsel+json")
                .withArgs(hearingId)
                .withPayload(addProsecutionCounsel)
                .executeSuccessfully();

        return addProsecutionCounsel;
    }

    public static RemoveProsecutionCounsel removeProsecutionCounsel(final RequestSpecification requestSpec, final UUID hearingId,
                                                                    final RemoveProsecutionCounsel removeProsecutionCounsel) {

        makeCommand(requestSpec, "hearing.update-hearing")
                .ofType("application/vnd.hearing.remove-prosecution-counsel+json")
                .withArgs(hearingId)
                .withPayload(removeProsecutionCounsel)
                .executeSuccessfully();

        return removeProsecutionCounsel;
    }

    public static RemoveDefenceCounsel removeDefenceCounsel(final RequestSpecification requestSpec, final UUID hearingId,
                                                            final RemoveDefenceCounsel removeDefenceCounsel) {

        makeCommand(requestSpec, "hearing.update-hearing")
                .ofType("application/vnd.hearing.remove-defence-counsel+json")
                .withArgs(hearingId)
                .withPayload(removeDefenceCounsel)
                .executeSuccessfully();

        return removeDefenceCounsel;
    }

    public static CaseDefendantDetails updateDefendants(CaseDefendantDetails caseDefendantDetails) throws Exception {

        final String eventName = "public.progression.case-defendant-changed";

        final ObjectMapper mapper = new ObjectMapperProducer().objectMapper();

        String payloadAsString = mapper.writeValueAsString(caseDefendantDetails.getDefendants().get(0));

        final JsonObject jsonObject = mapper.readValue(payloadAsString, JsonObject.class);

        sendMessage(
                getPublicTopicInstance().createProducer(),
                eventName,
                createObjectBuilder()
                        .add("defendant",
                                uk.gov.justice.services.messaging.JsonObjects.createObjectBuilder(jsonObject).build())
                        .build(),
                metadataWithRandomUUID(eventName).withUserId(randomUUID().toString()).build());

        return caseDefendantDetails;
    }


    public static UpdateOffencesForDefendantCommand updateOffences(UpdateOffencesForDefendantCommand updateOffencesForDefendantCommand) throws Exception {

        final String eventName = "public.progression.defendant-offences-changed";

        final ObjectMapper mapper = new ObjectMapperProducer().objectMapper();

        final String jsonValueAsString = mapper.writeValueAsString(updateOffencesForDefendantCommand);

        final JsonObject jsonObject = mapper.readValue(jsonValueAsString, JsonObject.class);

        sendMessage(
                getPublicTopicInstance().createProducer(),
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
                getPublicTopicInstance().createProducer(),
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

    public static UpdateDefenceCounsel updateDefenceCounsel(final RequestSpecification requestSpec, final UUID hearingId, final UpdateDefenceCounsel updateDefenceCounselCommandTemplate) {

        makeCommand(requestSpec, "hearing.update-hearing")
                .ofType("application/vnd.hearing.update-defence-counsel+json")
                .withArgs(hearingId)
                .withPayload(updateDefenceCounselCommandTemplate)
                .executeSuccessfully();

        return updateDefenceCounselCommandTemplate;
    }

    public static UpdateProsecutionCounsel updateProsecutionCounsel(final RequestSpecification requestSpec, final UUID hearingId, final UpdateProsecutionCounsel updateProsecutionCounselCommandTemplate) {

        makeCommand(requestSpec, "hearing.update-hearing")
                .ofType("application/vnd.hearing.update-prosecution-counsel+json")
                .withArgs(hearingId)
                .withPayload(updateProsecutionCounselCommandTemplate)
                .executeSuccessfully();

        return updateProsecutionCounselCommandTemplate;
    }

    public static AddRespondentCounsel addRespondentCounsel(final RequestSpecification requestSpec, final UUID hearingId,
                                                            final AddRespondentCounsel addRespondentCounsel) {
        makeCommand(requestSpec, "hearing.update-hearing")
                .ofType("application/vnd.hearing.add-respondent-counsel+json")
                .withArgs(hearingId)
                .withPayload(addRespondentCounsel)
                .executeSuccessfully();

        return addRespondentCounsel;
    }

    public static UpdateRespondentCounsel updateRespondentCounsel(final RequestSpecification requestSpec, final UUID hearingId, final UpdateRespondentCounsel updateRespondentCounselCommandTemplate) {
        makeCommand(requestSpec, "hearing.update-hearing")
                .ofType("application/vnd.hearing.update-respondent-counsel+json")
                .withArgs(hearingId)
                .withPayload(updateRespondentCounselCommandTemplate)
                .executeSuccessfully();

        return updateRespondentCounselCommandTemplate;
    }

    public static RemoveRespondentCounsel removeRespondentCounsel(final RequestSpecification requestSpec, final UUID hearingId,
                                                                  final RemoveRespondentCounsel removeRespondentCounsel) {

        makeCommand(requestSpec, "hearing.update-hearing")
                .ofType("application/vnd.hearing.remove-respondent-counsel+json")
                .withArgs(hearingId)
                .withPayload(removeRespondentCounsel)
                .executeSuccessfully();

        return removeRespondentCounsel;
    }

    public static AddApplicantCounsel addApplicantCounsel(final RequestSpecification requestSpec, final UUID hearingId,
                                                          final AddApplicantCounsel addApplicantCounsel) {
        makeCommand(requestSpec, "hearing.update-hearing")
                .ofType("application/vnd.hearing.add-applicant-counsel+json")
                .withArgs(hearingId)
                .withPayload(addApplicantCounsel)
                .executeSuccessfully();

        return addApplicantCounsel;
    }

    public static UpdateApplicantCounsel updateApplicantCounsel(final RequestSpecification requestSpec, final UUID hearingId, final UpdateApplicantCounsel updateApplicantCounselCommandTemplate) {

        makeCommand(requestSpec, "hearing.update-hearing")
                .ofType("application/vnd.hearing.update-applicant-counsel+json")
                .withArgs(hearingId)
                .withPayload(updateApplicantCounselCommandTemplate)
                .executeSuccessfully();

        return updateApplicantCounselCommandTemplate;
    }

    public static RemoveApplicantCounsel removeApplicantCounsel(final RequestSpecification requestSpec, final UUID hearingId,
                                                                final RemoveApplicantCounsel removeApplicantCounsel) {

        makeCommand(requestSpec, "hearing.update-hearing")
                .ofType("application/vnd.hearing.remove-applicant-counsel+json")
                .withArgs(hearingId)
                .withPayload(removeApplicantCounsel)
                .executeSuccessfully();

        return removeApplicantCounsel;
    }

    public static void addDefendant(final Defendant defendant) throws Exception {

        final String eventName = "public.progression.defendants-added-to-court-proceedings";

        final ObjectMapper mapper = new ObjectMapperProducer().objectMapper();
        String payloadAsString = mapper.writeValueAsString(defendant);

        final JsonObject jsonObject = mapper.readValue(payloadAsString, JsonObject.class);

        sendMessage(
                getPublicTopicInstance().createProducer(),
                eventName,
                createObjectBuilder()
                        .add("foooo", "to test additional properties")
                        .add("defendants", Json.createArrayBuilder().add(uk.gov.justice.services.messaging.JsonObjects.createObjectBuilder(jsonObject).build()))
                        .build(),
                metadataWithRandomUUID(eventName).withUserId(randomUUID().toString()).build());

    }

    public static void sendPublicApplicationChangedMessage(final CourtApplication courtApplication) throws Exception {

        final String eventName = "public.progression.court-application-updated";

        final ObjectMapper mapper = new ObjectMapperProducer().objectMapper();
        String payloadAsString = mapper.writeValueAsString(courtApplication);

        final JsonObject jsonObject = mapper.readValue(payloadAsString, JsonObject.class);

        sendMessage(
                getPublicTopicInstance().createProducer(),
                eventName,
                createObjectBuilder()
                        .add("courtApplication", uk.gov.justice.services.messaging.JsonObjects.createObjectBuilder(jsonObject).build())
                        .build(),
                metadataWithRandomUUID(eventName).withUserId(randomUUID().toString()).build());

    }

    public static void addInterpreterIntermediary(final RequestSpecification requestSpec, final UUID hearingId,
                                                  final InterpreterIntermediary interpreterIntermediary) {
        try {

            final JsonObject addInterpreterIntermediary = createObjectBuilder().add("interpreterIntermediary", Utilities.JsonUtil.objectToJsonObject(interpreterIntermediary)).build();

            makeCommand(requestSpec, "hearing.update-hearing")
                    .ofType("application/vnd.hearing.add-interpreter-intermediary+json")
                    .withArgs(hearingId)
                    .withPayload(addInterpreterIntermediary.toString())
                    .executeSuccessfully();

        } catch (JsonProcessingException exception) {
            System.out.println(exception);
        }

    }

    public static RemoveInterpreterIntermediary removeInterpreterIntermediary(final RequestSpecification requestSpec, final UUID hearingId,
                                                                              final RemoveInterpreterIntermediary removeInterpreterIntermediary) {

        makeCommand(requestSpec, "hearing.update-hearing")
                .ofType("application/vnd.hearing.remove-interpreter-intermediary+json")
                .withArgs(hearingId)
                .withPayload(removeInterpreterIntermediary)
                .executeSuccessfully();

        return removeInterpreterIntermediary;
    }

    public static UpdateInterpreterIntermediary updateInterpreterIntermediary(final RequestSpecification requestSpec, final UUID hearingId, final UpdateInterpreterIntermediary updateInterpreterIntermediary) {

        makeCommand(requestSpec, "hearing.update-hearing")
                .ofType("application/vnd.hearing.update-interpreter-intermediary+json")
                .withArgs(hearingId)
                .withPayload(updateInterpreterIntermediary)
                .executeSuccessfully();

        return updateInterpreterIntermediary;
    }

    public static TrialType setTrialType(final RequestSpecification requestSpec, final UUID hearingId,
                                         final TrialType trialType) {
        makeCommand(requestSpec, "hearing.update-hearing")
                .ofType("application/vnd.hearing.set-trial-type+json")
                .withArgs(hearingId)
                .withPayload(trialType)
                .executeSuccessfully();

        return trialType;
    }

    public static void updateCaseMarkers(final UUID prosecutionCaseId, final UUID hearingId, final List<Marker> markers) throws Exception {

        final String eventName = "public.progression.case-markers-updated";
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        for (Marker marker : markers) {
            final ObjectMapper mapper = new ObjectMapperProducer().objectMapper();
            String payloadAsString = mapper.writeValueAsString(marker);
            final JsonObject jsonObject = mapper.readValue(payloadAsString, JsonObject.class);
            arrayBuilder.add(uk.gov.justice.services.messaging.JsonObjects.createObjectBuilder(jsonObject).build());
        }
        JsonObject payload = createObjectBuilder()
                .add("prosecutionCaseId", prosecutionCaseId.toString())
                .add("hearingId", hearingId.toString())
                .add("caseMarkers", arrayBuilder)
                .build();
        sendMessage(
                getPublicTopicInstance().createProducer(),
                eventName,
                payload,
                metadataWithRandomUUID(eventName).withUserId(randomUUID().toString()).build());

    }

}
