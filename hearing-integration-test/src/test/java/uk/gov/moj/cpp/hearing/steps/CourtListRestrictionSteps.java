package uk.gov.moj.cpp.hearing.steps;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.OK;
import static uk.gov.justice.services.common.http.HeaderConstants.USER_ID;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.moj.cpp.hearing.it.UseCases.asDefault;
import static uk.gov.moj.cpp.hearing.it.UseCases.logEvent;
import static uk.gov.moj.cpp.hearing.it.Utilities.listenFor;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsACourtClerk;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.initiateHearingTemplateForApplicationNoReportingRestriction;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.initiateHearingTemplateWithParamNoReportingRestriction;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.getPublicTopicInstance;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_SEC;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.poll;

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
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.json.JsonObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.core.AllOf;
import org.junit.jupiter.api.BeforeEach;

public class CourtListRestrictionSteps  extends AbstractIT {

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
         final CourtListRestricted restrictCourtListData = CourtListRestricted.courtListRestricted()
                .withCaseIds(Arrays.asList(hearing.getProsecutionCases().get(0).getId()))
                .withHearingId(hearing.getId())
                .withRestrictCourtList(restrictCourtList)
                .build();

        sendMessage((JsonObject) objectToJsonValueConverter.convert(restrictCourtListData));
    }

    public void hideDefendantFromXhibit(final Hearing hearing, final boolean restrictCourtList) {
        final CourtListRestricted restrictCourtListData = CourtListRestricted.courtListRestricted()
                .withDefendantIds(Arrays.asList(hearing.getProsecutionCases().get(0).getDefendants().get(0).getMasterDefendantId()))
                .withHearingId(hearing.getId())
                .withRestrictCourtList(restrictCourtList)
                .build();

        sendMessage((JsonObject) objectToJsonValueConverter.convert(restrictCourtListData));
    }

    public  void hearingEventsCourtListRestrictedReceived(final Matcher<?> matcher) {
        try (final Utilities.EventListener eventListener = listenFor(HEARING_EVENTS_COURT_LIST_RESTRICTED, HEARING_EVENT)
                .withFilter(matcher)) {
            eventListener.waitFor();
        }
    }

    private void sendMessage(final JsonObject restrictCourtListDataObject) {
        uk.gov.moj.cpp.hearing.utils.QueueUtil.sendMessage(
                getPublicTopicInstance().createProducer(),
                PUBLIC_EVENTS_LISTING_COURT_LIST_RESTRICTED,
                restrictCourtListDataObject,
                metadataWithRandomUUID(PUBLIC_EVENTS_LISTING_COURT_LIST_RESTRICTED).withUserId(randomUUID().toString()).build());
    }

    public CommandHelpers.InitiateHearingCommandHelper createHearingEvent(final UUID caseId, final UUID hearingEventId, final String courtRoomId, final String defenceCounselId,
                                                                                 final UUID eventDefinitionId, final ZonedDateTime eventTime, final Optional<UUID> hearingTypeId, String courtCenter, LocalDate localDate) throws NoSuchAlgorithmException {
        final CommandHelpers.InitiateHearingCommandHelper hearing = h(UseCases.initiateHearingWithNsp(getRequestSpec(), initiateHearingTemplateWithParamNoReportingRestriction(fromString(courtCenter), fromString(courtRoomId), "CourtRoom 1", localDate, fromString(defenceCounselId), caseId, hearingTypeId)));
        logEvent(hearingEventId, getRequestSpec(), asDefault(), hearing.it(), eventDefinitionId, false, fromString(defenceCounselId), eventTime, null);

        poll(requestParams(getURL("hearing.get-hearing-event-log", hearing.it().getHearing().getId(), eventTime.toLocalDate()),
                "application/vnd.hearing.hearing-event-log+json").withHeader(USER_ID, getLoggedInUser()))
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(
                        status().is(OK),
                        print(),
                        payload().isJson(AllOf.allOf(
                                withJsonPath("$.hearingId", Matchers.is(hearing.it().getHearing().getId().toString()))
                        ))
                );
        return hearing;
    }

    public CommandHelpers.InitiateHearingCommandHelper createHearingEventForApplication(final UUID caseId, final UUID hearingEventId, final String courtRoomId, final String defenceCounselId,
                                                                          final UUID eventDefinitionId, final ZonedDateTime eventTime, final Optional<UUID> hearingTypeId, String courtCenter, LocalDate localDate) throws NoSuchAlgorithmException {
        final CommandHelpers.InitiateHearingCommandHelper hearing = h(UseCases.initiateHearingForApplication(getRequestSpec(), initiateHearingTemplateForApplicationNoReportingRestriction(fromString(courtCenter), fromString(courtRoomId), "CourtRoom 1", localDate, fromString(defenceCounselId), caseId, hearingTypeId)));
        givenAUserHasLoggedInAsACourtClerk(randomUUID());
        logEvent(hearingEventId, getRequestSpec(), asDefault(), hearing.it(), eventDefinitionId, false, fromString(defenceCounselId), eventTime, null);

        poll(requestParams(getURL("hearing.get-hearing-event-log", hearing.it().getHearing().getId(), eventTime.toLocalDate()),
                "application/vnd.hearing.hearing-event-log+json").withHeader(USER_ID, getLoggedInUser()))
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(
                        status().is(OK),
                        print(),
                        payload().isJson(AllOf.allOf(
                                withJsonPath("$.hearingId", Matchers.is(hearing.it().getHearing().getId().toString()))
                        ))
                );
        return hearing;
    }

    public void hideApplicationFromXhibit(final Hearing hearing, final boolean restrictCourtList) {
        final CourtListRestricted restrictCourtListData = CourtListRestricted.courtListRestricted()
                .withCourtApplicationIds(Arrays.asList(hearing.getCourtApplications().get(0).getId()))
                .withHearingId(hearing.getId())
                .withRestrictCourtList(restrictCourtList)
                .build();

        sendMessage((JsonObject) objectToJsonValueConverter.convert(restrictCourtListData));
    }

    public void hideApplicationApplicantFromXhibit(final Hearing hearing, final boolean restrictCourtList) {
        final CourtListRestricted restrictCourtListData = CourtListRestricted.courtListRestricted()
                .withCourtApplicationApplicantIds(Arrays.asList(hearing.getCourtApplications().get(0).getApplicant().getId()))
                .withHearingId(hearing.getId())
                .withRestrictCourtList(restrictCourtList)
                .build();
        sendMessage((JsonObject) objectToJsonValueConverter.convert(restrictCourtListData));
    }
}
