package uk.gov.moj.cpp.hearing.it;

import com.jayway.jsonpath.ReadContext;
import com.jayway.jsonpath.internal.JsonContext;
import com.jayway.restassured.specification.RequestSpecification;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.moj.cpp.hearing.command.initiate.Address;
import uk.gov.moj.cpp.hearing.command.initiate.Defendant;
import uk.gov.moj.cpp.hearing.command.initiate.Hearing;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.initiate.Interpreter;
import uk.gov.moj.cpp.hearing.command.initiate.Offence;
import uk.gov.moj.cpp.hearing.command.logEvent.CorrectLogEventCommand;
import uk.gov.moj.cpp.hearing.command.logEvent.LogEventCommand;

import java.util.UUID;
import java.util.function.Consumer;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withoutJsonPath;
import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.INTEGER;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.it.TestUtilities.initiateHearingCommandTemplate;
import static uk.gov.moj.cpp.hearing.it.TestUtilities.listenFor;
import static uk.gov.moj.cpp.hearing.it.TestUtilities.makeCommand;
import static uk.gov.moj.cpp.hearing.it.TestUtilities.with;

public class UseCases {

    public static <T> Consumer<T> asDefault() {
        return c -> {
        };
    }

    public static InitiateHearingCommand initiateHearing(RequestSpecification requestSpec, Consumer<InitiateHearingCommand.Builder> consumer) {
        InitiateHearingCommand initiateHearing = with(initiateHearingCommandTemplate(), consumer).build();

        final Hearing hearing = initiateHearing.getHearing();

        TestUtilities.EventListener publicEventTopic = listenFor("public.hearing.initiated")
                .withFilter(isJson(withJsonPath("$.hearingId", is(hearing.getId().toString()))));

        makeCommand(requestSpec, "hearing.initiate")
                .ofType("application/vnd.hearing.initiate+json")
                .withPayload(initiateHearing)
                .executeSuccessfully();

        publicEventTopic.waitFor();

        return initiateHearing;
    }

    public static InitiateHearingCommand initiateHearingMultipleDefendants(RequestSpecification requestSpec, int requiredDefendantCount) {
        Consumer<InitiateHearingCommand.Builder> consumer = builder -> {
            for (; builder.getHearing().getDefendants().size() < requiredDefendantCount; ) {
                builder.getHearing().addDefendant(Defendant.builder()
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
                        .addOffence(
                                Offence.builder()
                                        .withId(randomUUID())
                                        .withCaseId(builder.getCases().get(0).getCaseId())
                                        .withOffenceCode(STRING.next())
                                        .withWording(STRING.next())
                                        .withSection(STRING.next())
                                        .withStartDate(PAST_LOCAL_DATE.next())
                                        .withEndDate(PAST_LOCAL_DATE.next())
                                        .withOrderIndex(INTEGER.next())
                                        .withCount(INTEGER.next())
                                        .withConvictionDate(PAST_LOCAL_DATE.next())
                        )
                );
            }
        };
        return initiateHearing(requestSpec, consumer);
    }


    public static LogEventCommand logEvent(RequestSpecification requestSpec,
                                           Consumer<LogEventCommand.Builder> consumer,
                                           InitiateHearingCommand initiateHearingCommand,
                                           UUID hearingEventDefinitionId,
                                           boolean alterable) {
        LogEventCommand logEvent = with(
                LogEventCommand.builder()
                        .withHearingEventId(randomUUID())
                        .withHearingEventDefinitionId(hearingEventDefinitionId)
                        .withHearingId(initiateHearingCommand.getHearing().getId())
                        .withEventTime(PAST_ZONED_DATE_TIME.next())
                        .withLastModifiedTime(PAST_ZONED_DATE_TIME.next())
                        .withRecordedLabel(STRING.next())

                , consumer).build();

        TestUtilities.EventListener publicEventTopic = listenFor("public.hearing.event-logged")
                .withFilter(isJson(allOf(
                        new BaseMatcher<ReadContext>() {

                            @Override
                            public void describeTo(Description description) {

                            }

                            @Override
                            public boolean matches(Object o) {
                                JsonContext r = (JsonContext) o;
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

    public static CorrectLogEventCommand correctLogEvent(RequestSpecification requestSpec,
                                           UUID hearingEventId,
                                           Consumer<CorrectLogEventCommand.Builder> consumer,
                                           InitiateHearingCommand initiateHearingCommand,
                                           UUID hearingEventDefinitionId,
                                           boolean alterable) {
        CorrectLogEventCommand logEvent = with(
                CorrectLogEventCommand.builder()
                        .withLastestHearingEventId(randomUUID()) // the new event id.
                        .withHearingEventDefinitionId(hearingEventDefinitionId)
                        .withEventTime(PAST_ZONED_DATE_TIME.next())
                        .withLastModifiedTime(PAST_ZONED_DATE_TIME.next())
                        .withRecordedLabel(STRING.next())
                , consumer).build();

        TestUtilities.EventListener publicEventTopic = listenFor("public.hearing.event-timestamp-corrected")
                .withFilter(isJson(allOf(
                        new BaseMatcher<ReadContext>() {

                            @Override
                            public void describeTo(Description description) {

                            }

                            @Override
                            public boolean matches(Object o) {
                                JsonContext r = (JsonContext) o;
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


    public static LogEventCommand logEventThatIsIgnored(RequestSpecification requestSpec,
                                                        Consumer<LogEventCommand.Builder> consumer,
                                                        UUID hearingId,
                                                        UUID hearingEventDefinitionId,
                                                        boolean alterable,
                                                        String reason) {
        LogEventCommand logEvent = with(
                LogEventCommand.builder()
                        .withHearingEventId(randomUUID())
                        .withHearingEventDefinitionId(hearingEventDefinitionId)
                        .withHearingId(hearingId)
                        .withEventTime(PAST_ZONED_DATE_TIME.next())
                        .withLastModifiedTime(PAST_ZONED_DATE_TIME.next())
                        .withRecordedLabel(STRING.next())
                , consumer).build();

        TestUtilities.EventListener publicEventTopic = listenFor("public.hearing.event-ignored")
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
}
