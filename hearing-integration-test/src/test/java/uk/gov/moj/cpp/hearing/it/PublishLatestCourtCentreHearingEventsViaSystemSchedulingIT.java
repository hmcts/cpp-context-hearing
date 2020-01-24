package uk.gov.moj.cpp.hearing.it;

import static java.text.MessageFormat.format;
import static java.time.ZonedDateTime.now;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.apache.http.HttpStatus.SC_ACCEPTED;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.INTEGER;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.it.HearingEventsIT.hearingDefinitionData;
import static uk.gov.moj.cpp.hearing.it.HearingEventsIT.hearingDefinitions;
import static uk.gov.moj.cpp.hearing.it.UseCases.asDefault;
import static uk.gov.moj.cpp.hearing.it.UseCases.logEvent;
import static uk.gov.moj.cpp.hearing.steps.HearingEventStepDefinitions.andHearingEventDefinitionsAreAvailable;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsACourtClerk;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.initiateHearingTemplateWithParam;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.stubGetReferenceDataCourtRoomMappings;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.stubGetReferenceDataCourtXhibitCourtMappings;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.stubGetReferenceDataEventMappings;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.stubOrganisationUnit;
import static uk.gov.moj.cpp.hearing.utils.WebDavStub.getSentXml;
import static uk.gov.moj.cpp.hearing.utils.WebDavStub.stubExhibitFileUpload;

import uk.gov.moj.cpp.hearing.domain.HearingEventDefinition;
import uk.gov.moj.cpp.hearing.steps.PublishCourtListSteps;
import uk.gov.moj.cpp.hearing.steps.data.HearingEventDefinitionData;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.json.JsonObject;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PublishLatestCourtCentreHearingEventsViaSystemSchedulingIT extends AbstractIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(PublishLatestCourtCentreHearingEventsViaSystemSchedulingIT.class);

    private static final String HEARING_COMMAND_PUBLISH_HEARING_LIST = "hearing.publish-hearing-lists-for-crown-courts";
    private static final String MEDIA_TYPE_HEARING_COMMAND_PUBLISH_HEARING_LIST = "application/vnd.hearing.publish-hearing-lists-for-crown-courts+json";

    private static final ZonedDateTime EVENT_TIME = now().minusMinutes(5l).withZoneSameLocal(ZoneId.of("UTC"));
    private static final LocalDate localDate = EVENT_TIME.toLocalDate();

    private String courtCentreId;
    private String courtRoom1Id;
    private String courtRoom2Id;
    private String defenceCounselId;
    private UUID caseId;

    @Before
    public void initStub() {
        courtCentreId = randomUUID().toString();
        courtRoom1Id = randomUUID().toString();
        courtRoom2Id = randomUUID().toString();
        defenceCounselId = randomUUID().toString();
        caseId = randomUUID();

        stubExhibitFileUpload();
        stubGetReferenceDataCourtRoomMappings(courtRoom1Id, courtRoom2Id);
        stubGetReferenceDataCourtXhibitCourtMappings();
        stubOrganisationUnit(courtCentreId);
        stubGetReferenceDataEventMappings();
    }

    @Test
    public void shouldRequestToPublishHearingList() throws NoSuchAlgorithmException {
        createHearingEvent(courtRoom1Id, defenceCounselId,"Start Hearing");

        final JsonObject publishCourtListJsonObject = buildPublishCourtListJsonString(courtCentreId, "28");

        final PublishCourtListSteps publishCourtListSteps = new PublishCourtListSteps();

        sendPublishHearingListCommandFromSchedule(publishCourtListJsonObject);

        publishCourtListSteps.verifyCourtListPublishStatusReturnedWhenQueryingFromAPI(courtCentreId);
    }



    @Test
    public void shouldGetLatestHearingEvents() throws NoSuchAlgorithmException {
        final CommandHelpers.InitiateHearingCommandHelper hearing = createHearingEvent(courtRoom1Id, defenceCounselId,"Start Hearing");

        final PublishCourtListSteps publishCourtListSteps = new PublishCourtListSteps();
        publishCourtListSteps.verifyLatestHearingEvents(hearing.getHearing(), now().minusMinutes(10l));
    }

    private void sendPublishHearingListCommandFromSchedule(final JsonObject publishCourtListJsonObject) {
        final String updateHearingUrl = String.format("%s/%s", baseUri, format(ENDPOINT_PROPERTIES.getProperty(HEARING_COMMAND_PUBLISH_HEARING_LIST)));
        final String request = publishCourtListJsonObject.toString();

        LOGGER.info("Post call made: \n\n\tURL = {} \n\tMedia type = {} \n\tPayload = {}\n\n", updateHearingUrl, MEDIA_TYPE_HEARING_COMMAND_PUBLISH_HEARING_LIST, request, getLoggedInSystemUserHeader());

        final Response response = restClient.postCommand(updateHearingUrl, MEDIA_TYPE_HEARING_COMMAND_PUBLISH_HEARING_LIST, request, getLoggedInSystemUserHeader());

        assertThat(response.getStatus(), equalTo(SC_ACCEPTED));
    }

    private JsonObject buildPublishCourtListJsonString(final String courtCentreId, final String day) {
        return createObjectBuilder().add("courtCentreId", courtCentreId).add("createdTime", "2019-10-" + day + "T16:34:45.132Z").build();
    }

    private static HearingEventDefinition findEventDefinitionWithActionLabel(final HearingEventDefinitionData hearingEventDefinitionData, final String actionLabel) {
        return hearingEventDefinitionData.getEventDefinitions().stream().filter(d -> d.getActionLabel().equals(actionLabel)).findFirst().get();
    }

    private final CommandHelpers.InitiateHearingCommandHelper createHearingEvent(final String courtRoomId, final String defenceCounselId, final String actionLabel) throws NoSuchAlgorithmException {
        final CommandHelpers.InitiateHearingCommandHelper hearing = h(UseCases.initiateHearing(requestSpec, initiateHearingTemplateWithParam(fromString(courtCentreId), fromString(courtRoomId), "CourtRoom 1", localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth(), fromString(defenceCounselId), caseId)));

        givenAUserHasLoggedInAsACourtClerk(randomUUID());

        final List<HearingEventDefinition> hearingDefinitions = new ArrayList(hearingDefinitions());
        hearingDefinitions.addAll(
                Arrays.asList(
                    new HearingEventDefinition(fromString("50fb4a64-943d-4a2a-afe6-4b5c9e99e043"), "Appellant opens", INTEGER.next(), STRING.next(), "APPEAL", STRING.next(), INTEGER.next(), false),
                    new HearingEventDefinition(fromString("e9060336-4821-4f46-969c-e08b33b48071"), "Open case prosecution", INTEGER.next(), STRING.next(), "OPEN_CASE", STRING.next(), INTEGER.next(), false),
                    new HearingEventDefinition(fromString("a3a9fe0c-a9a7-4e17-b0cd-42606722bbb0"), "Defence counsel.name opens case regarding defendant defendant.name", INTEGER.next(), STRING.next(), "OPEN_CASE", STRING.next(), INTEGER.next(), false),
                    new HearingEventDefinition(fromString("cc00cca8-39ba-431c-b08f-8c6f9be185d1"), "Defence counsel.name closes case regarding defendant defendant.name", INTEGER.next(), STRING.next(), "CLOSE_CASE", STRING.next(), INTEGER.next(), false),
                    new HearingEventDefinition(fromString("b335327a-7f58-4f26-a2ef-7e07134ba60b"), "Point of law discussion prosecution", INTEGER.next(), STRING.next(), "DISCUSSION", STRING.next(), INTEGER.next(), false)
                )
        );
        final HearingEventDefinitionData hearingEventDefinitionData = andHearingEventDefinitionsAreAvailable(hearingDefinitionData(hearingDefinitions));

        final HearingEventDefinition hearingEventDefinition = findEventDefinitionWithActionLabel(hearingEventDefinitionData, actionLabel);
        assertThat(hearingEventDefinition.isAlterable(), is(false));

        logEvent(requestSpec, asDefault(), hearing.it(), hearingEventDefinition.getId(), false, fromString(defenceCounselId), EVENT_TIME);
        return hearing;
    }
}
