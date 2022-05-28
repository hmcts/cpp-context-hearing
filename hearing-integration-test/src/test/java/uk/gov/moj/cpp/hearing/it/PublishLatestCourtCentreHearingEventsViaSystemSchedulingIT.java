package uk.gov.moj.cpp.hearing.it;

import static java.text.MessageFormat.format;
import static java.time.ZonedDateTime.now;
import static java.util.Optional.of;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.apache.http.HttpStatus.SC_ACCEPTED;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.core.courts.HearingLanguage.ENGLISH;
import static uk.gov.justice.services.test.utils.core.http.BaseUriProvider.getBaseUri;
import static uk.gov.moj.cpp.hearing.it.UseCases.asDefault;
import static uk.gov.moj.cpp.hearing.it.UseCases.logEvent;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.findEventDefinitionWithActionLabel;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsACourtClerk;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.initiateHearingTemplateWithParam;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.stubGetReferenceDataCourtRooms;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.stubUsersAndGroupsUserRoles;

import uk.gov.justice.services.test.utils.core.rest.RestClient;
import uk.gov.moj.cpp.hearing.domain.HearingEventDefinition;
import uk.gov.moj.cpp.hearing.steps.PublishCourtListSteps;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Random;

import javax.annotation.concurrent.NotThreadSafe;
import javax.json.JsonObject;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NotThreadSafe
public class PublishLatestCourtCentreHearingEventsViaSystemSchedulingIT extends AbstractPublishLatestCourtCentreHearingIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(PublishLatestCourtCentreHearingEventsViaSystemSchedulingIT.class);

    private static final String HEARING_COMMAND_PUBLISH_HEARING_LIST = "hearing.publish-hearing-lists-for-crown-courts";
    private static final String MEDIA_TYPE_HEARING_COMMAND_PUBLISH_HEARING_LIST = "application/vnd.hearing.publish-hearing-lists-for-crown-courts+json";
    private static final String START_HEARING = "Start Hearing";
    private static final String END_HEARING = "End Hearing";

    private CommandHelpers.InitiateHearingCommandHelper hearing;
    private ZonedDateTime eventTime;

    @Before
    public void setup() throws NoSuchAlgorithmException {
        stubUsersAndGroupsUserRoles(getLoggedInUser());
        eventTime = now().withZoneSameLocal(ZoneId.of("UTC"));
        hearing = h(UseCases.initiateHearing(getRequestSpec(), initiateHearingTemplateWithParam(fromString(courtCentreId), fromString(courtRoom1Id), "CourtRoom 1", eventTime.toLocalDate(), fromString(defenceCounselId), caseId, of(hearingTypeId))));
        stubGetReferenceDataCourtRooms(hearing.getHearing().getCourtCentre(), ENGLISH, ouId3, ouId4);
    }

    @Test
    public void shouldProduceWebPageOnlyWithLatestEventOfTheDayForTheCourtRoom() throws NoSuchAlgorithmException {
        createHearingEvent(hearing, defenceCounselId, START_HEARING, eventTime.plusHours(1).plusMinutes(rand()).plusSeconds(rand()));
        logEvent(getRequestSpec(), asDefault(), hearing.it(), getHearingEventDefinition(END_HEARING).getId(),
                false, fromString(defenceCounselId), eventTime.plusHours(2).plusMinutes(rand()).plusSeconds(rand()), null);

        createHearingEvent(hearing, defenceCounselId, START_HEARING, eventTime.plusMinutes(rand()).plusSeconds(rand()));
        logEvent(getRequestSpec(), asDefault(), hearing.it(), getHearingEventDefinition(END_HEARING).getId(),
                false, fromString(defenceCounselId), eventTime.plusHours(3).plusMinutes(rand()).plusSeconds(rand()), null);

        final JsonObject publishCourtListJsonObject = buildPublishCourtListJsonString(courtCentreId, eventTime.toLocalDate());

        final PublishCourtListSteps publishCourtListSteps = new PublishCourtListSteps();

        sendPublishHearingListCommandFromSchedule(publishCourtListJsonObject);

        publishCourtListSteps.verifyCourtListPublishStatusReturnedWhenQueryingFromAPI(courtCentreId);
    }

    private int rand() {
        Random rand = new Random();
        return rand.nextInt((60 - 1) + 1) + 1;
    }

    @Test
    public void shouldRequestToPublishHearingList() throws NoSuchAlgorithmException {
        createHearingEvent(hearing, defenceCounselId, START_HEARING, eventTime.plusMinutes(rand()).plusSeconds(rand()));

        final JsonObject publishCourtListJsonObject = buildPublishCourtListJsonString(courtCentreId, eventTime.toLocalDate());

        final PublishCourtListSteps publishCourtListSteps = new PublishCourtListSteps();

        sendPublishHearingListCommandFromSchedule(publishCourtListJsonObject);

        publishCourtListSteps.verifyCourtListPublishStatusReturnedWhenQueryingFromAPI(courtCentreId);
    }


    private void sendPublishHearingListCommandFromSchedule(final JsonObject publishCourtListJsonObject) {
        final String updateHearingUrl = String.format("%s/%s", getBaseUri(), format(ENDPOINT_PROPERTIES.getProperty(HEARING_COMMAND_PUBLISH_HEARING_LIST)));
        final String request = publishCourtListJsonObject.toString();

        LOGGER.info("Post call made: \n\n\tURL = {} \n\tMedia type = {} \n\tPayload = {}\n\n", updateHearingUrl, MEDIA_TYPE_HEARING_COMMAND_PUBLISH_HEARING_LIST, request, getLoggedInSystemUserHeader());

        final Response response = new RestClient().postCommand(updateHearingUrl, MEDIA_TYPE_HEARING_COMMAND_PUBLISH_HEARING_LIST, request, getLoggedInSystemUserHeader());

        assertThat(response.getStatus(), equalTo(SC_ACCEPTED));
    }

    private JsonObject buildPublishCourtListJsonString(final String courtCentreId, final LocalDate eventDate) {
        return createObjectBuilder().add("courtCentreId", courtCentreId).add("eventDate", eventDate.toString()).build();
    }

    private final CommandHelpers.InitiateHearingCommandHelper createHearingEvent(final CommandHelpers.InitiateHearingCommandHelper hearing,
                                                                                 final String defenceCounselId, final String actionLabel,
                                                                                 final ZonedDateTime eventTime) {
        givenAUserHasLoggedInAsACourtClerk(randomUUID());

        final HearingEventDefinition hearingEventDefinition = getHearingEventDefinition(actionLabel);

        logEvent(getRequestSpec(), asDefault(), hearing.it(), hearingEventDefinition.getId(), false, fromString(defenceCounselId), eventTime, null);
        return hearing;
    }

    private HearingEventDefinition getHearingEventDefinition(String actionLabel) {
        final HearingEventDefinition hearingEventDefinition = findEventDefinitionWithActionLabel(actionLabel);
        assertThat(hearingEventDefinition.isAlterable(), is(false));
        return hearingEventDefinition;
    }

}
