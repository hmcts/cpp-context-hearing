package uk.gov.moj.cpp.hearing.steps;

import static com.google.common.collect.Lists.newArrayList;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasSize;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.hamcrest.Matchers.is;
import static org.awaitility.Durations.FIVE_MINUTES;
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
import uk.gov.justice.hearing.courts.ApplicationCourtListRestriction;
import uk.gov.justice.hearing.courts.CourtListRestricted;
import uk.gov.justice.services.common.converter.ObjectToJsonValueConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.hearing.it.AbstractIT;
import uk.gov.moj.cpp.hearing.it.UseCases;
import uk.gov.moj.cpp.hearing.it.Utilities;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;

import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import javax.json.JsonObject;

import io.restassured.path.json.JsonPath;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.awaitility.Awaitility;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CourtListRestrictionSteps extends AbstractIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(CourtListRestrictionSteps.class);

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

    public void waitForCaseCourtListRestriction(final UUID caseId, final boolean restricted) {
        final String criteria = String.format(" id='%s' and is_court_list_restricted=%s", caseId, restricted);
        waitUntilDataPersist("ha_case", criteria, 1);
    }

    public void waitForDefendantCourtListRestriction(final UUID masterDefendantId, final boolean restricted) {
        final String criteria = String.format(" master_defendant_id='%s' and is_court_list_restricted=%s", masterDefendantId, restricted);
        waitUntilDataPersist("ha_defendant", criteria, 1);
    }

    public void waitForApplicationCourtListRestriction(final UUID hearingId, final UUID applicationId, final boolean restricted) {
        Awaitility.await()
                .atMost(FIVE_MINUTES)
                .until(() -> isApplicationCourtListRestrictionPersisted(hearingId, applicationId, restricted));
    }

    public void waitForApplicationApplicantCourtListRestriction(final UUID hearingId, final UUID applicantId, final boolean restricted) {
        Awaitility.await()
                .atMost(FIVE_MINUTES)
                .until(() -> isApplicationApplicantCourtListRestrictionPersisted(hearingId, applicantId, restricted));
    }

    private boolean isApplicationCourtListRestrictionPersisted(final UUID hearingId, final UUID applicationId, final boolean restricted) {
        final ApplicationCourtListRestriction restrictions = readApplicationCourtListRestriction(hearingId).orElse(null);
        if (restricted) {
            return restrictions != null
                    && isNotEmpty(restrictions.getCourtApplicationIds())
                    && restrictions.getCourtApplicationIds().contains(applicationId);
        }
        return restrictions == null
                || isEmpty(restrictions.getCourtApplicationIds())
                || !restrictions.getCourtApplicationIds().contains(applicationId);
    }

    private boolean isApplicationApplicantCourtListRestrictionPersisted(final UUID hearingId, final UUID applicantId, final boolean restricted) {
        final ApplicationCourtListRestriction restrictions = readApplicationCourtListRestriction(hearingId).orElse(null);
        if (restricted) {
            return restrictions != null
                    && isNotEmpty(restrictions.getCourtApplicationApplicantIds())
                    && restrictions.getCourtApplicationApplicantIds().contains(applicantId);
        }
        return restrictions == null
                || isEmpty(restrictions.getCourtApplicationApplicantIds())
                || !restrictions.getCourtApplicationApplicantIds().contains(applicantId);
    }

    private Optional<ApplicationCourtListRestriction> readApplicationCourtListRestriction(final UUID hearingId) {
        final Optional<String> restrictCourtListJson = readRestrictCourtListJson(hearingId);
        if (restrictCourtListJson.isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(restrictCourtListJson.get(), ApplicationCourtListRestriction.class));
        } catch (final Exception exception) {
            LOGGER.error("Failed to parse restrict_court_list_json for hearing {}", hearingId, exception);
            return Optional.empty();
        }
    }

    private Optional<String> readRestrictCourtListJson(final UUID hearingId) {
        try (final Connection viewStoreConnection = testJdbcConnectionProvider.getViewStoreConnection("hearing");
             final Statement statement = viewStoreConnection.createStatement()) {
            final String sql = String.format("select restrict_court_list_json from ha_hearing where id='%s'", hearingId);
            final ResultSet resultSet = statement.executeQuery(sql);
            if (resultSet.next()) {
                return Optional.ofNullable(resultSet.getString(1));
            }
        } catch (SQLException exception) {
            LOGGER.error("Failed to read restrict_court_list_json for hearing {}", hearingId, exception);
        }
        return Optional.empty();
    }

    private void waitUntilDataPersist(final String tableName, final String criteria, final int count) {
        Awaitility.await()
                .atMost(FIVE_MINUTES)
                .until(() -> countRows(tableName, criteria) == count);
    }

    private int countRows(final String tableName, final String criteria) {
        try (final Connection viewStoreConnection = testJdbcConnectionProvider.getViewStoreConnection("hearing");
             final Statement statement = viewStoreConnection.createStatement()) {
            final String sql = String.format("select count(1) from %s where %s", tableName, criteria);
            final ResultSet resultSet = statement.executeQuery(sql);

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException exception) {
            LOGGER.error("Failed to count from table {} with condition {}", tableName, criteria, exception);
        }

        return 0;
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
            return hearing;
        }
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

    public void hideApplicationRespondentFromXhibit(final Hearing hearing, final boolean restrictCourtList) {
        final CourtListRestricted restrictCourtListData = courtListRestricted()
                .withCourtApplicationRespondentIds(newArrayList(hearing.getCourtApplications().get(0).getRespondents().get(0).getId()))
                .withHearingId(hearing.getId())
                .withRestrictCourtList(restrictCourtList)
                .build();
        sendListingPublicEvent((JsonObject) objectToJsonValueConverter.convert(restrictCourtListData));
    }
}
