package uk.gov.moj.cpp.hearing.it;

import static java.time.ZonedDateTime.now;
import static java.util.Arrays.asList;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static uk.gov.justice.core.courts.HearingLanguage.ENGLISH;
import static uk.gov.justice.core.courts.JurisdictionType.CROWN;
import static uk.gov.justice.services.messaging.JsonObjects.createObjectBuilder;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand.initiateHearingCommand;
import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static uk.gov.moj.cpp.hearing.it.Queries.pollForHearingEvents;
import static uk.gov.moj.cpp.hearing.it.UseCases.asDefault;
import static uk.gov.moj.cpp.hearing.it.UseCases.initiateHearing;
import static uk.gov.moj.cpp.hearing.it.UseCases.logEvent;
import static uk.gov.moj.cpp.hearing.it.Utilities.listenFor;
import static uk.gov.moj.cpp.hearing.it.Utilities.makeCommand;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.findEventDefinitionWithActionLabel;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsACourtClerk;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.DefendantType.PERSON;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.defaultArguments;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.MapStringToTypeMatcher.convertStringTo;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.stubGetPIReferenceDataEventMappings;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.stubOrganisationUnit;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.setupAsAuthorisedUser;

import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.domain.HearingEventDefinition;
import uk.gov.moj.cpp.hearing.eventlog.PublicHearingEventLogged;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;
import uk.gov.moj.cpp.hearing.test.CoreTestTemplates;
import uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;

import javax.annotation.concurrent.NotThreadSafe;
import javax.json.JsonObject;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * End-to-end IT for the "override court room" flow when the target hearing's
 * active day is in a different room from its top-level.
 *
 * Setup:
 * - Hearing A: top-level (centreA, roomA), day 2 in (sharedCentre, sharedRoom)
 * - Hearing B: top-level (sharedCentre, sharedRoom)
 * - Hearing B has an active "Start Hearing" event logged on today.
 *
 * When a clerk logs an event on hearing A on today with override=true, the
 * command-API queries hearing.get-active-hearings-for-court-room. The query
 * should resolve A's day-2 room (sharedRoom), find B in that room, and the
 * handler should emit a pause event for B.
 *
 * Without the per-day fix, the query would resolve to A's top-level room
 * (roomA), find no active hearings, and B would NOT be paused.
 */
@NotThreadSafe
public class OverrideCourtRoomActiveHearingsIT extends AbstractIT {

    private static final UUID PAUSE_HEARING_EVENT_DEFINITION_ID = fromString("160ecb51-29ee-4954-bbbf-daab18a24fbb");
    private static final String COMMAND_LOG_HEARING_EVENT = "hearing.log-hearing-event";
    private static final String MEDIA_TYPE_LOG_HEARING_EVENT = "application/vnd.hearing.log-hearing-event+json";

    @BeforeAll
    public static void setupPerClass() {
        setupAsAuthorisedUser(randomUUID());
        stubGetPIReferenceDataEventMappings();
    }

    @Test
    public void overrideCourtRoom_shouldPauseActiveHearingInPerDayRoom_whenTargetHearingDayHasOverrideRoom() {
        final ZonedDateTime today = now(ZoneOffset.UTC);
        final ZonedDateTime yesterday = today.minusDays(1);

        // The shared room: hearing A's day 2 lands here, and hearing B is top-level here
        final UUID sharedCentreId = randomUUID();
        final UUID sharedRoomId = randomUUID();

        // Stub OUs for both centres so downstream lookups don't fail
        stubOrganisationUnit(sharedCentreId.toString());

        // ---- Hearing B: top-level in the shared room, single day today ----
        final HearingDay hearingBDay = HearingDay.hearingDay()
                .withSittingDay(today)
                .withListingSequence(1)
                .withListedDurationMinutes(60)
                .withCourtCentreId(sharedCentreId)
                .withCourtRoomId(sharedRoomId)
                .build();

        final InitiateHearingCommand hearingBCmd = initiateHearingCommand()
                .setHearing(CoreTestTemplates.hearing(defaultArguments()
                        .setDefendantType(PERSON)
                        .setHearingLanguage(ENGLISH)
                        .setJurisdictionType(CROWN)
                ).withCourtCentre(uk.gov.justice.core.courts.CourtCentre.courtCentre()
                        .withId(sharedCentreId)
                        .withName("Shared Centre")
                        .withRoomId(sharedRoomId)
                        .withRoomName("Shared Room")
                        .build()
                ).withHearingDays(asList(hearingBDay)).build());

        final InitiateHearingCommandHelper hearingB = h(initiateHearing(getRequestSpec(), hearingBCmd));
        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());

        // Log a "Start Hearing" event for B (alterable=false so it counts as active per the query)
        final HearingEventDefinition startDef = findEventDefinitionWithActionLabel("Start Hearing");
        logEvent(getRequestSpec(), asDefault(), hearingB.it(), startDef.getId(),
                false, randomUUID(), today, null);

        // Wait for B's event to land in the view store so the override-flow query can find it
        pollForHearingEvents(hearingB.getHearingId().toString(), today.toLocalDate(), new Matcher[]{
                withJsonPath("$.events", hasSize(1))
        });

        // ---- Hearing A: top-level in a DIFFERENT room, day 2 in the shared room ----
        final HearingDay hearingADay1 = HearingDay.hearingDay()
                .withSittingDay(yesterday)
                .withListingSequence(1)
                .withListedDurationMinutes(60)
                .build();
        final HearingDay hearingADay2 = HearingDay.hearingDay()
                .withSittingDay(today)
                .withListingSequence(2)
                .withListedDurationMinutes(60)
                .withCourtCentreId(sharedCentreId)
                .withCourtRoomId(sharedRoomId)
                .build();

        final InitiateHearingCommand hearingACmd = initiateHearingCommand()
                .setHearing(CoreTestTemplates.hearing(defaultArguments()
                        .setDefendantType(PERSON)
                        .setHearingLanguage(ENGLISH)
                        .setJurisdictionType(CROWN)
                ).withHearingDays(asList(hearingADay1, hearingADay2)).build());

        final InitiateHearingCommandHelper hearingA = h(initiateHearing(getRequestSpec(), hearingACmd));

        // Build the override log-hearing-event payload manually so we can include `override=true`
        final HearingEventDefinition identifyDef = findEventDefinitionWithActionLabel("Identify defendant");
        final UUID hearingEventId = randomUUID();

        final JsonObject overridePayload = createObjectBuilder()
                .add("hearingEventId", hearingEventId.toString())
                .add("hearingId", hearingA.getHearingId().toString())
                .add("hearingEventDefinitionId", identifyDef.getId().toString())
                .add("recordedLabel", STRING.next())
                .add("note", "override note")
                .add("eventTime", ZonedDateTimes.toString(today))
                .add("lastModifiedTime", ZonedDateTimes.toString(today))
                .add("alterable", true)
                .add("override", true)
                .build();

        // Listen for hearing B getting paused (PAUSE event-definition fired against hearing B)
        final BeanMatcher<PublicHearingEventLogged> pauseMatcher = isBean(PublicHearingEventLogged.class)
                .with(PublicHearingEventLogged::getHearingEventDefinition,
                        isBean(uk.gov.moj.cpp.hearing.eventlog.HearingEventDefinition.class)
                                .with(uk.gov.moj.cpp.hearing.eventlog.HearingEventDefinition::getHearingEventDefinitionId,
                                        org.hamcrest.core.Is.is(PAUSE_HEARING_EVENT_DEFINITION_ID)));

        try (final Utilities.EventListener pauseListener = listenFor("public.hearing.event-logged")
                .withFilter(convertStringTo(PublicHearingEventLogged.class, pauseMatcher))) {

            makeCommand(getRequestSpec(), COMMAND_LOG_HEARING_EVENT)
                    .withArgs(hearingA.getHearingId())
                    .ofType(MEDIA_TYPE_LOG_HEARING_EVENT)
                    .withPayload(overridePayload.toString())
                    .executeSuccessfully();

            pauseListener.waitFor();
        }
    }
}
