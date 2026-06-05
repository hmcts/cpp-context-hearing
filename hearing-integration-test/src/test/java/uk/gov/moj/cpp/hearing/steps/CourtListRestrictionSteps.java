package uk.gov.moj.cpp.hearing.steps;

import static com.google.common.collect.Lists.newArrayList;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.text.MessageFormat.format;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static java.util.concurrent.TimeUnit.SECONDS;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static uk.gov.justice.services.common.http.HeaderConstants.USER_ID;
import static uk.gov.justice.services.test.utils.core.http.BaseUriProvider.getBaseUri;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.http.RestPoller.poll;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.setupAsAuthorizedAndSystemUser;
import static uk.gov.justice.hearing.courts.CourtListRestricted.courtListRestricted;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.moj.cpp.hearing.it.UseCases.asDefault;
import static uk.gov.moj.cpp.hearing.it.UseCases.initiateHearingForApplication;
import static uk.gov.moj.cpp.hearing.it.UseCases.logEvent;
import static uk.gov.moj.cpp.hearing.it.Utilities.listenFor;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsACourtClerk;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.initiateHearingTemplateForApplicationNoReportingRestriction;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.initiateHearingTemplateWithParamNoReportingRestriction;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.initiateHearingTemplateWithParamNoReportingRestrictionYoungDefendant;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.getPublicTopicInstance;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.sendMessage;

import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.hearing.courts.CourtListRestricted;
import uk.gov.justice.services.common.converter.ObjectToJsonValueConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.hearing.it.AbstractIT;
import uk.gov.moj.cpp.hearing.it.UseCases;
import uk.gov.moj.cpp.hearing.it.Utilities;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import javax.json.JsonObject;

import io.restassured.path.json.JsonPath;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;

public class CourtListRestrictionSteps extends AbstractIT {

    private static final String PUBLIC_EVENTS_LISTING_COURT_LIST_RESTRICTED = "public.listing.court-list-restricted";
    private static final String HEARING_EVENTS_COURT_LIST_RESTRICTED = "hearing.event.court-list-restricted";
    private static final String HEARING_EVENT = "hearing.event";

    ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();

    ObjectToJsonValueConverter objectToJsonValueConverter = new ObjectToJsonValueConverter(objectMapper);

    @BeforeEach
    public void setUpTest() {
        givenAUserHasLoggedInAsACourtClerk(randomUUID());
    }

    public void hideCaseFromXhibit(final Hearing hearing, final boolean restrictCourtList) {
        final CourtListRestricted restrictCourtListData = courtListRestricted()
                .withCaseIds(newArrayList(hearing.getProsecutionCases().get(0).getId()))
                .withHearingId(hearing.getId())
                .withRestrictCourtList(restrictCourtList)
                .build();

        sendListingPublicEvent((JsonObject) objectToJsonValueConverter.convert(restrictCourtListData));
    }

    public void hideDefendantFromXhibit(final Hearing hearing, final boolean restrictCourtList) {
        final CourtListRestricted restrictCourtListData = courtListRestricted()
                .withDefendantIds(newArrayList(hearing.getProsecutionCases().get(0).getDefendants().get(0).getMasterDefendantId()))
                .withHearingId(hearing.getId())
                .withRestrictCourtList(restrictCourtList)
                .build();

        sendListingPublicEvent((JsonObject) objectToJsonValueConverter.convert(restrictCourtListData));
    }

    public JsonPath hearingEventsCourtListRestrictedReceived(final Matcher<?> matcher) {
        try (final Utilities.EventListener eventListener = listenFor(HEARING_EVENTS_COURT_LIST_RESTRICTED, HEARING_EVENT)
                .withFilter(matcher)) {
            return eventListener.waitFor();
        }
    }

    /**
     * Polls the publish-side query {@code hearing.latest-hearings-by-court-centres} until the
     * restriction projection has reached the expected state. The publish flow internally consumes
     * the same query — once it reflects the toggle, the next {@code publish-court-list} command is
     * guaranteed to see the same state.
     * <p>
     * Required because {@link #hearingEventsCourtListRestrictedReceived(Matcher)} only confirms the
     * hearing event was emitted; the listener that projects it into the JPA entity runs in a
     * separate transaction and may lag behind the publish command if not waited for.
     * <p>
     * A hearing-visibility precondition ({@code courtRoomId notNullValue}) is prepended to the
     * caller's matcher to prevent the poll from short-circuiting on the empty/not-yet-projected
     * state — without this, lenient matchers such as {@code hasNoJsonPath(...)} or
     * {@code withJsonPath(..., hasSize(0))} would match an empty {@code {}} response and return
     * before the restriction event has actually been processed.
     */
    public void waitForRestrictionProjection(final String courtCentreId,
                                             final LocalDate hearingDate,
                                             final Matcher<? super com.jayway.jsonpath.ReadContext> expectedPayload) {
        setupAsAuthorizedAndSystemUser(USER_ID_VALUE_AS_ADMIN);
        final String queryPart = format(ENDPOINT_PROPERTIES.getProperty("hearing.latest-hearings-by-court-centres"), courtCentreId, hearingDate);
        final String searchCourtListUrl = String.format("%s/%s", getBaseUri(), queryPart);

        poll(requestParams(searchCourtListUrl, "application/vnd.hearing.latest-hearings-by-court-centres+json")
                .withHeader(USER_ID, getLoggedInSystemUserHeader()))
                .timeout(60, SECONDS)
                .pollInterval(1, SECONDS)
                .until(status().is(OK), payload().isJson(allOf(
                        withJsonPath("$.court.courtSites[0].courtRooms[0].courtRoomId", notNullValue()),
                        expectedPayload)));
    }

    /**
     * Polls the publish-side query {@code hearing.latest-hearings-by-court-centres} until the
     * just-created hearing is observable. MUST be called after {@code createHearingEvent*} and
     * BEFORE any {@code hide*FromXhibit} call.
     * <p>
     * Without this wait, the {@code public.listing.court-list-restricted} → ... →
     * {@code hearing.event.court-list-restricted} chain can reach
     * {@link uk.gov.moj.cpp.hearing.event.listener.CourtListRestrictionEventListener} before the
     * hearing-creation projection has committed to {@code ha_hearing}. The listener does
     * {@code hearingRepository.findOptionalBy(hearingId)} and, if the row is missing, silently
     * returns (the message is consumed and never replayed). The restriction is then lost, the
     * subsequent publish reads the un-restricted hearing, and the assertion on the redacted XML
     * fails. This flake reproduced ~2/3 of CI runs on team/rv-2616.
     */
    public void waitForHearingVisible(final String courtCentreId, final LocalDate hearingDate) {
        setupAsAuthorizedAndSystemUser(USER_ID_VALUE_AS_ADMIN);
        final String queryPart = format(ENDPOINT_PROPERTIES.getProperty("hearing.latest-hearings-by-court-centres"), courtCentreId, hearingDate);
        final String searchCourtListUrl = String.format("%s/%s", getBaseUri(), queryPart);

        poll(requestParams(searchCourtListUrl, "application/vnd.hearing.latest-hearings-by-court-centres+json")
                .withHeader(USER_ID, getLoggedInSystemUserHeader()))
                .timeout(60, SECONDS)
                .pollInterval(1, SECONDS)
                .until(status().is(OK), payload().isJson(
                        withJsonPath("$.court.courtSites[0].courtRooms[0].courtRoomId", notNullValue())));
    }

    private void sendListingPublicEvent(final JsonObject restrictCourtListDataObject) {
        sendMessage(
                getPublicTopicInstance().createProducer(),
                PUBLIC_EVENTS_LISTING_COURT_LIST_RESTRICTED,
                restrictCourtListDataObject,
                metadataWithRandomUUID(PUBLIC_EVENTS_LISTING_COURT_LIST_RESTRICTED).withUserId(randomUUID().toString()).build());
    }

    public CommandHelpers.InitiateHearingCommandHelper createHearingEvent(final UUID caseId, final UUID hearingEventId, final String courtRoomId, final String defenceCounselId,
                                                                          final UUID eventDefinitionId, final ZonedDateTime eventTime, final Optional<UUID> hearingTypeId, String courtCenter, LocalDate localDate) throws NoSuchAlgorithmException {
        final CommandHelpers.InitiateHearingCommandHelper hearing = h(UseCases.initiateHearingWithNsp(getRequestSpec(), initiateHearingTemplateWithParamNoReportingRestriction(fromString(courtCenter), fromString(courtRoomId), "CourtRoom 1", localDate, fromString(defenceCounselId), caseId, hearingTypeId)));
        logEvent(hearingEventId, getRequestSpec(), asDefault(), hearing.it(), eventDefinitionId, false, fromString(defenceCounselId), eventTime, null);
        waitForHearingVisible(courtCenter, eventTime.toLocalDate());
        return hearing;
    }

    public CommandHelpers.InitiateHearingCommandHelper createHearingEventWithYoungDefendant(final UUID caseId, final UUID hearingEventId, final String courtRoomId, final String defenceCounselId,
                                                                                            final UUID eventDefinitionId, final ZonedDateTime eventTime, final Optional<UUID> hearingTypeId, final String courtCenter, final LocalDate localDate) throws NoSuchAlgorithmException {
        try (final Utilities.EventListener eventListener = listenFor(HEARING_EVENTS_COURT_LIST_RESTRICTED, HEARING_EVENT)
                .withFilter(isJson(allOf(
                        withJsonPath("$.defendantIds", hasSize(1)),
                        withJsonPath("$.restrictCourtList", is(true)))))) {
            final CommandHelpers.InitiateHearingCommandHelper hearing = h(UseCases.initiateHearingWithNsp(getRequestSpec(),
                    initiateHearingTemplateWithParamNoReportingRestrictionYoungDefendant(fromString(courtCenter), fromString(courtRoomId), "CourtRoom 1", localDate, fromString(defenceCounselId), caseId, hearingTypeId)));
            logEvent(hearingEventId, getRequestSpec(), asDefault(), hearing.it(), eventDefinitionId, false, fromString(defenceCounselId), eventTime, null);
            eventListener.waitFor();
            waitForHearingVisible(courtCenter, eventTime.toLocalDate());
            return hearing;
        }
    }

    public CommandHelpers.InitiateHearingCommandHelper createHearingEventForApplication(final UUID caseId, final UUID hearingEventId, final String courtRoomId, final String defenceCounselId,
                                                                                        final UUID eventDefinitionId, final ZonedDateTime eventTime, final Optional<UUID> hearingTypeId, String courtCenter, LocalDate localDate) throws NoSuchAlgorithmException {
        final CommandHelpers.InitiateHearingCommandHelper hearing = h(initiateHearingForApplication(getRequestSpec(), initiateHearingTemplateForApplicationNoReportingRestriction(fromString(courtCenter), fromString(courtRoomId), "CourtRoom 1", localDate, fromString(defenceCounselId), caseId, hearingTypeId)));
        givenAUserHasLoggedInAsACourtClerk(randomUUID());
        logEvent(hearingEventId, getRequestSpec(), asDefault(), hearing.it(), eventDefinitionId, false, fromString(defenceCounselId), eventTime, null);
        waitForHearingVisible(courtCenter, eventTime.toLocalDate());
        return hearing;
    }

    public void hideApplicationFromXhibit(final Hearing hearing, final boolean restrictCourtList) {
        final CourtListRestricted restrictCourtListData = courtListRestricted()
                .withCourtApplicationIds(newArrayList(hearing.getCourtApplications().get(0).getId()))
                .withHearingId(hearing.getId())
                .withRestrictCourtList(restrictCourtList)
                .build();

        sendListingPublicEvent((JsonObject) objectToJsonValueConverter.convert(restrictCourtListData));
    }

    public void hideApplicationApplicantFromXhibit(final Hearing hearing, final boolean restrictCourtList) {
        final CourtListRestricted restrictCourtListData = courtListRestricted()
                .withCourtApplicationApplicantIds(newArrayList(hearing.getCourtApplications().get(0).getApplicant().getId()))
                .withHearingId(hearing.getId())
                .withRestrictCourtList(restrictCourtList)
                .build();
        sendListingPublicEvent((JsonObject) objectToJsonValueConverter.convert(restrictCourtListData));
    }

    public void hideApplicationRespondentFromXhibit(final Hearing hearing, final boolean restrictCourtList) {
        final CourtListRestricted restrictCourtListData = courtListRestricted()
                .withCourtApplicationRespondentIds(newArrayList(hearing.getCourtApplications().get(0).getRespondents().get(0).getId()))
                .withHearingId(hearing.getId())
                .withRestrictCourtList(restrictCourtList)
                .build();
        sendListingPublicEvent((JsonObject) objectToJsonValueConverter.convert(restrictCourtListData));
    }
}
