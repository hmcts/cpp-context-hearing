package uk.gov.moj.cpp.hearing.it;

import static java.time.ZonedDateTime.now;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static uk.gov.justice.core.courts.HearingLanguage.ENGLISH;
import static uk.gov.justice.core.courts.JurisdictionType.CROWN;
import static uk.gov.justice.services.messaging.JsonObjects.createArrayBuilder;
import static uk.gov.justice.services.messaging.JsonObjects.createObjectBuilder;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand.initiateHearingCommand;
import static uk.gov.moj.cpp.hearing.it.UseCases.asDefault;
import static uk.gov.moj.cpp.hearing.it.UseCases.initiateHearing;
import static uk.gov.moj.cpp.hearing.it.UseCases.postHearingLogEventCommand;
import static uk.gov.moj.cpp.hearing.it.Utilities.listenFor;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.findEventDefinitionWithActionLabel;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsACourtClerk;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.DefendantType.PERSON;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.defaultArguments;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.hearingDay;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.MapStringToTypeMatcher.convertStringTo;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.changeCourtRoomsStubWithAdding;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.stubGetPIReferenceDataEventMappings;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.stubOrganisationUnit;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.setupAsAuthorisedUser;

import uk.gov.justice.core.courts.HearingDay;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.logEvent.LogEventCommand;
import uk.gov.moj.cpp.hearing.domain.HearingEventDefinition;
import uk.gov.moj.cpp.hearing.eventlog.CourtCentre;
import uk.gov.moj.cpp.hearing.eventlog.PublicHearingEventLogged;
import uk.gov.moj.cpp.hearing.test.CoreTestTemplates;
import uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;

import javax.annotation.concurrent.NotThreadSafe;

import org.hamcrest.core.Is;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * End-to-end IT verifying the per-day court room override flow.
 *
 * Initiates a multi-day hearing where day 2 has its own courtCentreId/courtRoomId
 * distinct from the hearing's top-level CourtCentre. Logs an event on day 2's date,
 * then listens for the published public.hearing.event-logged event and asserts
 * the CourtCentre payload carries the day's IDs and the names looked up from
 * reference data (rather than the top-level names).
 */
@NotThreadSafe
public class MultidayPerDayRoomIT extends AbstractIT {

    @BeforeAll
    public static void setupPerClass() {
        setupAsAuthorisedUser(randomUUID());
        stubGetPIReferenceDataEventMappings();
    }

    @Test
    public void hearingEventLogged_shouldUseDayCourtRoomWithLookedUpNames_whenHearingDayHasOverrideRoom() {
        final ZonedDateTime day1Time = now(ZoneOffset.UTC).minusDays(1);
        final ZonedDateTime day2Time = now(ZoneOffset.UTC);

        final UUID dayCentreId = randomUUID();
        final UUID dayRoomId = randomUUID();

        final HearingDay day1 = hearingDay(day1Time).build();
        final HearingDay day2 = HearingDay.hearingDay()
                .withSittingDay(day2Time)
                .withListingSequence(2)
                .withListedDurationMinutes(60)
                .withCourtCentreId(dayCentreId)
                .withCourtRoomId(dayRoomId)
                .build();

        final InitiateHearingCommand initiateCmd = initiateHearingCommand()
                .setHearing(CoreTestTemplates.hearing(defaultArguments()
                        .setDefendantType(PERSON)
                        .setHearingLanguage(ENGLISH)
                        .setJurisdictionType(CROWN)
                ).withHearingDays(asList(day1, day2)).build());

        h(initiateHearing(getRequestSpec(), initiateCmd));
        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());

        // Stub the per-day OU so reference data resolves the day's centre + room with names
        changeCourtRoomsStubWithAdding(
                createObjectBuilder()
                        .add("id", dayCentreId.toString())
                        .add("oucode", "C99XX99")
                        .add("oucodeL3Name", "Day 2 Centre")
                        .add("oucodeL3WelshName", "Welsh Day 2 Centre")
                        .add("oucodeL1Code", "C")
                        .add("isWelsh", false)
                        .add("address1", "1 Day 2 Court Lane")
                        .add("address2", "Day City")
                        .add("defaultStartTime", "10:00")
                        .add("defaultDurationHrs", "7:00")
                        .add("courtrooms", createArrayBuilder()
                                .add(createObjectBuilder()
                                        .add("id", dayRoomId.toString())
                                        .add("courtroomId", 999)
                                        .add("venueName", "Day 2 Venue")
                                        .add("courtroomName", "Day 2 Room")
                                        .add("welshCourtroomName", "Welsh Day 2 Room")))
                        .build()
        );

        // Top-level OU stub (used by downstream PI mapping for LJA/venue id)
        stubOrganisationUnit(initiateCmd.getHearing().getCourtCentre().getId().toString());

        final HearingEventDefinition hearingEventDefinition = findEventDefinitionWithActionLabel("Identify defendant");

        final LogEventCommand logEvent = with(LogEventCommand.builder()
                .withHearingEventId(randomUUID())
                .withHearingEventDefinitionId(hearingEventDefinition.getId())
                .withHearingId(initiateCmd.getHearing().getId())
                .withEventTime(day2Time)
                .withLastModifiedTime(day2Time)
                .withRecordedLabel(STRING.next())
                .withDefenceCounselId(randomUUID())
                .withAlterable(true)
                .withNote("note"), asDefault()).build();

        final BeanMatcher<PublicHearingEventLogged> matcher = isBean(PublicHearingEventLogged.class)
                .with(PublicHearingEventLogged::getHearing, isBean(uk.gov.moj.cpp.hearing.eventlog.Hearing.class)
                        .with(uk.gov.moj.cpp.hearing.eventlog.Hearing::getCourtCentre, isBean(CourtCentre.class)
                                .with(CourtCentre::getCourtCentreId, Is.is(dayCentreId))
                                .with(CourtCentre::getCourtCentreName, Is.is("Day 2 Centre"))
                                .with(CourtCentre::getCourtRoomId, Is.is(dayRoomId))
                                .with(CourtCentre::getCourtRoomName, Is.is("Day 2 Room"))
                        )
                );

        try (final Utilities.EventListener publicEventTopic = listenFor("public.hearing.event-logged")
                .withFilter(convertStringTo(PublicHearingEventLogged.class, matcher))) {
            postHearingLogEventCommand(getRequestSpec(), initiateCmd, logEvent);
            publicEventTopic.waitFor();
        }
    }
}
