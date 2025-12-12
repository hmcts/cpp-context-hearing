package uk.gov.moj.cpp.hearing.steps;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
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

    public void hearingEventsCourtListRestrictedReceived(final Matcher<?> matcher) {
        try (final Utilities.EventListener eventListener = listenFor(HEARING_EVENTS_COURT_LIST_RESTRICTED, HEARING_EVENT)
                .withFilter(matcher)) {
            eventListener.waitFor();
        }
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
        return hearing;
    }

    public CommandHelpers.InitiateHearingCommandHelper createHearingEventForApplication(final UUID caseId, final UUID hearingEventId, final String courtRoomId, final String defenceCounselId,
                                                                                        final UUID eventDefinitionId, final ZonedDateTime eventTime, final Optional<UUID> hearingTypeId, String courtCenter, LocalDate localDate) throws NoSuchAlgorithmException {
        final CommandHelpers.InitiateHearingCommandHelper hearing = h(initiateHearingForApplication(getRequestSpec(), initiateHearingTemplateForApplicationNoReportingRestriction(fromString(courtCenter), fromString(courtRoomId), "CourtRoom 1", localDate, fromString(defenceCounselId), caseId, hearingTypeId)));
        givenAUserHasLoggedInAsACourtClerk(randomUUID());
        logEvent(hearingEventId, getRequestSpec(), asDefault(), hearing.it(), eventDefinitionId, false, fromString(defenceCounselId), eventTime, null);
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
}
