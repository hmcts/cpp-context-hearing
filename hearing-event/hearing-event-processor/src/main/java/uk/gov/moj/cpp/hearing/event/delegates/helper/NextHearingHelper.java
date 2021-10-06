package uk.gov.moj.cpp.hearing.event.delegates.helper;

import static com.google.common.base.CharMatcher.digit;
import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;
import static java.lang.String.format;
import static java.lang.System.lineSeparator;
import static java.time.ZoneOffset.UTC;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static uk.gov.justice.core.courts.CourtCentre.courtCentre;
import static uk.gov.justice.core.courts.NextHearing.nextHearing;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.shared.Constants.ADJOURNMENT_REASONS;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.shared.Constants.COMMA_REGEX;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.shared.Constants.CROWN_COURT_RESULT_DEFINITION_ID;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.shared.Constants.DATE_FORMATS;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.shared.Constants.DATE_FORMATS_WITHOUT_TIME;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.shared.Constants.DAYS_IN_A_WEEK;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.shared.Constants.EUROPE_LONDON;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.shared.Constants.MAGISTRATE_RESULT_DEFINITION_ID;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.shared.Constants.MINUTES_IN_A_DAY;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.shared.Constants.MINUTES_IN_HOUR;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.shared.Constants.SPACE;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.shared.Constants.START_OF_DAY_TIME;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.DurationElements.DAYS;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.DurationElements.HOURS;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.DurationElements.MINUTES;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference.HCHOUSE;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference.HCROOM;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference.HDATE;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference.HEST;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference.HTYPE;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference.bookingReference;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference.dateToBeFixed;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference.existingHearingId;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference.fixedDate;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference.hCHOUSEOrganisationName;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference.isPresent;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference.reservedJudiciary;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference.timeOfHearing;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference.valueOf;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference.weekCommencing;

import uk.gov.justice.core.courts.Address;
import uk.gov.justice.core.courts.CourtCentre;
import uk.gov.justice.core.courts.HearingType;
import uk.gov.justice.core.courts.JudicialResultPrompt;
import uk.gov.justice.core.courts.JurisdictionType;
import uk.gov.justice.core.courts.NextHearing;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.justice.hearing.courts.referencedata.CourtCentreOrganisationUnit;
import uk.gov.justice.hearing.courts.referencedata.Courtrooms;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.event.delegates.PublishResultUtil;
import uk.gov.moj.cpp.hearing.event.delegates.exception.NextHearingCreateException;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;
import uk.gov.moj.cpp.hearing.event.relist.metadata.DurationElements;
import uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference;
import uk.gov.moj.cpp.hearing.event.service.CourtHouseReverseLookup;
import uk.gov.moj.cpp.hearing.event.service.CourtRoomOuCodeReverseLookup;
import uk.gov.moj.cpp.hearing.event.service.HearingTypeReverseLookup;
import uk.gov.moj.cpp.hearing.event.service.ReferenceDataService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import javax.inject.Inject;

import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("squid:CallToDeprecatedMethod")
public class NextHearingHelper {

    private static final String FAILED_TO_CREATE_NEXT_HEARING_MESSAGE = "Failed to create next hearing for result definition id=%s";
    private static final Logger LOGGER = LoggerFactory.getLogger(NextHearingHelper.class.getName());
    @Inject
    private HearingTypeReverseLookup hearingTypeReverseLookup;
    @Inject
    private CourtHouseReverseLookup courtHouseReverseLookup;
    @Inject
    private CourtRoomOuCodeReverseLookup courtRoomOuCodeReverseLookup;
    @Inject
    private ReferenceDataService referenceDataService;

    public Optional<NextHearing> getNextHearing(final JsonEnvelope context,
                                                final ResultDefinition resultDefinition,
                                                final List<ResultLine> resultLines,
                                                final List<JudicialResultPrompt> prompts) {
        final String resultDefinitionId = resultDefinition.getId().toString();

        if (isNextHearingResult(resultDefinitionId) && canCreateNextHearing(prompts)) {
            LOGGER.info("Creating next hearing");
            final NextHearing nextHearing = buildNextHearing(context, resultDefinition, resultLines, prompts);
            if (isNull(nextHearing)) {
                final String message = format(FAILED_TO_CREATE_NEXT_HEARING_MESSAGE, resultDefinitionId);
                LOGGER.error(message);
                throw new NextHearingCreateException(message);
            } else {
                return Optional.of(nextHearing);
            }
        }

        LOGGER.warn("Cannot create nextHearing object for resultDefinition id={}", resultDefinitionId);
        return empty();
    }

    @SuppressWarnings({"squid:S1067"})
    private boolean canCreateNextHearing(final List<JudicialResultPrompt> prompts) {

        final Map<NextHearingPromptReference, JudicialResultPrompt> promptsMap = getPromptsMap(prompts);

        LOGGER.info("Checking if next hearing can be created using Fixed date: {}, Hearing date: {}, Week commencing: {}, Hearing type: {}, Court centre: {}, Estimated duration: {}",
                promptsMap.get(fixedDate), promptsMap.get(HDATE), promptsMap.get(weekCommencing), promptsMap.get(HTYPE), promptsMap.get(HCHOUSE), promptsMap.get(HEST));

        final boolean isFixedDatePromptsPresent = ofNullable(promptsMap.get(fixedDate)).isPresent() || ofNullable(promptsMap.get(HDATE)).isPresent();

        final boolean isWeekCommencingPromptsPresent = ofNullable(promptsMap.get(weekCommencing)).isPresent();

        final boolean isDateToBeFixed = ofNullable(promptsMap.get(dateToBeFixed)).isPresent();

        return ofNullable(promptsMap.get(HTYPE)).isPresent()
                && (ofNullable(promptsMap.get(hCHOUSEOrganisationName)).isPresent() || ofNullable(promptsMap.get(HCHOUSE)).isPresent())
                && ofNullable(promptsMap.get(HEST)).isPresent()
                && (isFixedDatePromptsPresent || isWeekCommencingPromptsPresent || isDateToBeFixed);
    }

    private NextHearing buildNextHearing(final JsonEnvelope context,
                                         final ResultDefinition resultDefinition,
                                         final List<ResultLine> resultLines,
                                         final List<JudicialResultPrompt> prompts) {

        final Map<NextHearingPromptReference, JudicialResultPrompt> promptsMap = getPromptsMap(prompts);
        final NextHearing.Builder builder = nextHearing();

        final Optional<CourtCentreOrganisationUnit> courtCentreOrgOptional = getCourtCentreOrganisationUnit(context, promptsMap);

        populateListedStartDateTime(builder, promptsMap, courtCentreOrgOptional);
        populateEstimatedDuration(builder, promptsMap);
        populateCourtCentre(builder, context, promptsMap, courtCentreOrgOptional);
        populateAdjournmentReasons(builder, context, resultLines);
        populateHearingType(builder, context, promptsMap);
        populateJurisdictionType(builder, resultDefinition.getId().toString());
        populateExistingHearingId(builder, promptsMap);
        populateReservedJudiciary(builder, promptsMap);
        populateWeekCommencingDate(builder, promptsMap);
        populateBookingReference(builder, promptsMap);
        populateDateToBeFixed(builder, promptsMap);

        return builder.build();
    }

    private void populateDateToBeFixed(final NextHearing.Builder builder, final Map<NextHearingPromptReference, JudicialResultPrompt> promptsMap) {
        final String promptValue = getPromptValue(promptsMap, dateToBeFixed);
        if (promptValue != null) {
            builder.withDateToBeFixed(Boolean.TRUE);
        }
    }

    private Map<NextHearingPromptReference, JudicialResultPrompt> getPromptsMap(final List<JudicialResultPrompt> prompts) {
        return prompts.stream().filter(p -> isPresent(p.getPromptReference()))
                .collect(toMap(prompt -> valueOf(prompt.getPromptReference()), prompt -> prompt));
    }

    private void populateListedStartDateTime(final NextHearing.Builder builder, final Map<NextHearingPromptReference, JudicialResultPrompt> promptsMap, final Optional<CourtCentreOrganisationUnit> courtCentreOrganisationUnit) {
        final String hDateValue = getPromptValue(promptsMap, HDATE);
        final String dateValue = hDateValue != null ? hDateValue : getPromptValue(promptsMap, fixedDate);
        final String timeValueFromPrompt = getPromptValue(promptsMap, timeOfHearing);
        final String defaultStartTimeForOrganisationUnit = courtCentreOrganisationUnit.map(CourtCentreOrganisationUnit::getDefaultStartTime).orElse(null);

        final String timeToUse = isNotBlank(timeValueFromPrompt) ? timeValueFromPrompt : defaultStartTimeForOrganisationUnit;
        LOGGER.info("Populating listed start date time using date: {} and time: {}", dateValue, timeToUse);
        builder.withListedStartDateTime(convertDateTimeToUTC(dateValue, timeToUse));
    }

    private String getPromptValue(final Map<NextHearingPromptReference, JudicialResultPrompt> promptsMap, final NextHearingPromptReference nextHearingPromptReference) {
        return promptsMap.get(nextHearingPromptReference) != null ? promptsMap.get(nextHearingPromptReference).getValue() : null;
    }

    private void populateEstimatedDuration(final NextHearing.Builder builder, final Map<NextHearingPromptReference, JudicialResultPrompt> promptsMap) {
        final int estimatedMinutes = convertDurationIntoMinutes(Sets.newHashSet(promptsMap.get(HEST).getValue()));
        LOGGER.info("Populating estimated duration minutes: {}", estimatedMinutes);
        builder.withEstimatedMinutes(estimatedMinutes);
    }

    private void populateBookingReference(final NextHearing.Builder builder, final Map<NextHearingPromptReference, JudicialResultPrompt> promptsMap) {
        final String promptValue = getPromptValue(promptsMap, bookingReference);
        LOGGER.info("Populating booking reference: {}", promptValue);
        builder.withBookingReference(nonNull(promptValue) ? UUID.fromString(promptValue) : null);
    }

    private void populateExistingHearingId(final NextHearing.Builder builder, final Map<NextHearingPromptReference, JudicialResultPrompt> promptsMap) {
        final String promptValue = getPromptValue(promptsMap, existingHearingId);
        LOGGER.info("Populating existing hearing id: {}", promptValue);
        builder.withExistingHearingId(nonNull(promptValue) ? UUID.fromString(promptValue) : null);
    }

    private void populateReservedJudiciary(final NextHearing.Builder builder, final Map<NextHearingPromptReference, JudicialResultPrompt> promptsMap) {
        final String promptValue = getPromptValue(promptsMap, reservedJudiciary);
        LOGGER.info("Populating reserved judiciary: {}", promptValue);
        builder.withReservedJudiciary(nonNull(promptValue) ? Boolean.valueOf(promptValue) : null);
    }

    private void populateWeekCommencingDate(final NextHearing.Builder builder, final Map<NextHearingPromptReference, JudicialResultPrompt> promptsMap) {
        final String promptValue = getPromptValue(promptsMap, weekCommencing);
        LOGGER.info("Populating week commencing date: {}", promptValue);
        builder.withWeekCommencingDate(nonNull(promptValue) ? LocalDate.parse(promptValue, DateTimeFormatter.ofPattern(PublishResultUtil.OUTGOING_PROMPT_DATE_FORMAT)) : null);
    }

    private void populateHearingType(final NextHearing.Builder builder,
                                     final JsonEnvelope context,
                                     final Map<NextHearingPromptReference, JudicialResultPrompt> promptsMap) {

        final JudicialResultPrompt prompt = promptsMap.get(HTYPE);
        final HearingType hearingType = hearingTypeReverseLookup.getHearingTypeByName(context, prompt.getValue());
        LOGGER.info("Populating hearing type: {}", hearingType);
        builder.withType(hearingType);
    }

    private void populateAdjournmentReasons(final NextHearing.Builder builder,
                                            final JsonEnvelope context,
                                            final List<ResultLine> resultLines) {

        final String adjournmentReasons = resultLines.stream()
                .filter(resultLine -> this.isAdjournmentReasonResult(context, resultLine))
                .map(this::getAdjournmentsReasons)
                .collect(joining(format("%s", lineSeparator())));
        LOGGER.info("Populating adjournment reason: {}", adjournmentReasons);
        builder.withAdjournmentReason(adjournmentReasons);
    }

    private boolean isAdjournmentReasonResult(final JsonEnvelope context, final ResultLine resultLine) {
        final ResultDefinition resultDefinition = getResultDefinition(context, resultLine);

        return resultDefinition != null
                && resultDefinition.getResultDefinitionGroup() != null
                && ADJOURNMENT_REASONS.equalsIgnoreCase(resultDefinition.getResultDefinitionGroup());
    }

    private String getAdjournmentsReasons(final ResultLine resultLine) {

        final String values = resultLine.getPrompts().stream()
                .map(p -> format("%s %s", p.getLabel(), p.getValue()))
                .collect(joining());
        if (!values.isEmpty()) {
            return format("%s%s%s", resultLine.getResultLabel(), lineSeparator(), values);
        } else {
            return resultLine.getResultLabel();
        }
    }

    private ResultDefinition getResultDefinition(final JsonEnvelope context, final ResultLine resultLine) {
        return this.referenceDataService.getResultDefinitionById(context, resultLine.getOrderedDate(), resultLine.getResultDefinitionId());
    }

    private void populateCourtCentre(final NextHearing.Builder builder,
                                     final JsonEnvelope context,
                                     final Map<NextHearingPromptReference, JudicialResultPrompt> promptsMap,
                                     Optional<CourtCentreOrganisationUnit> courtCentreOrgOptional) {

        if (!courtCentreOrgOptional.isPresent()) {
            return;
        }

        final CourtCentre courtCentre = extractCourtCentre(context, promptsMap, courtCentreOrgOptional.get());
        LOGGER.info("Populating court centre: {}", courtCentre);
        if (courtCentre != null) {
            builder.withCourtCentre(courtCentre);
        }
    }

    private CourtCentre extractCourtCentre(final JsonEnvelope context,
                                           final Map<NextHearingPromptReference, JudicialResultPrompt> promptsMap,
                                           CourtCentreOrganisationUnit courtCentreOrg) {

        final CourtCentre.Builder courtCentreBuilder = courtCentre()
                .withName(courtCentreOrg.getOucodeL3Name())
                .withWelshName(courtCentreOrg.getOucodeL3WelshName())
                .withPsaCode(parseInt(courtCentreOrg.getLja()))
                .withAddress(Address.address()
                        .withPostcode(courtCentreOrg.getPostcode())
                        .withAddress1(courtCentreOrg.getAddress1())
                        .withAddress2(courtCentreOrg.getAddress2())
                        .withAddress3(courtCentreOrg.getAddress3())
                        .withAddress4(courtCentreOrg.getAddress4())
                        .withAddress5(courtCentreOrg.getAddress5())
                        .build())
                .withId(UUID.fromString(courtCentreOrg.getId()));

        final Optional<JudicialResultPrompt> courtRoomValue = ofNullable(promptsMap.get(HCROOM));

        if (courtRoomValue.isPresent()) {
            final Optional<Courtrooms> courtRoom = courtHouseReverseLookup.getCourtRoomByRoomName(courtCentreOrg, courtRoomValue.get().getValue());

            if (courtRoom.isPresent()) {
                final Courtrooms courtrooms = courtRoom.get();
                courtCentreBuilder
                        .withRoomId(courtrooms.getId())
                        .withRoomName(courtrooms.getCourtroomName())
                        .withWelshRoomName(courtrooms.getWelshCourtroomName())
                        .withCourtHearingLocation(courtRoomOuCodeReverseLookup.getcourtRoomOuCode(context, courtrooms.getCourtroomId(), courtCentreOrg.getOucode().substring(0, 5)));
            }
        } else {
            courtCentreBuilder.withCourtHearingLocation(courtCentreOrg.getOucode());
        }

        return courtCentreBuilder.build();
    }

    private Optional<CourtCentreOrganisationUnit> getCourtCentreOrganisationUnit(final JsonEnvelope context, final Map<NextHearingPromptReference, JudicialResultPrompt> promptsMap) {
        final Optional<JudicialResultPrompt> courtHouse = ofNullable(ofNullable(promptsMap.get(hCHOUSEOrganisationName)).orElse(promptsMap.get(HCHOUSE)));

        if (!courtHouse.isPresent()) {
            return empty();
        }
        return courtHouseReverseLookup.getCourtCentreByName(context, courtHouse.get().getValue());
    }

    private static void populateJurisdictionType(final NextHearing.Builder builder,
                                                 final String resultDefinitionId) {
        if (CROWN_COURT_RESULT_DEFINITION_ID.equals(resultDefinitionId)) {
            LOGGER.info("Populating jurisdiction type: {}", JurisdictionType.CROWN);
            builder.withJurisdictionType(JurisdictionType.CROWN);
        } else if (MAGISTRATE_RESULT_DEFINITION_ID.equals(resultDefinitionId)) {
            LOGGER.info("Populating jurisdiction type: {}", JurisdictionType.MAGISTRATES);
            builder.withJurisdictionType(JurisdictionType.MAGISTRATES);
        }
    }

    private static boolean isNextHearingResult(final String resultDefinitionId) {

        return CROWN_COURT_RESULT_DEFINITION_ID.equals(resultDefinitionId)
                || MAGISTRATE_RESULT_DEFINITION_ID.equals(resultDefinitionId);
    }

    private static ZonedDateTime convertDateTimeToUTC(final String date, final String time) {
        if (!isEmpty(date)) {
            if (isEmpty(time)) {
                return ZonedDateTime.of(LocalDate.parse(date, DateTimeFormatter.ofPattern(DATE_FORMATS_WITHOUT_TIME)), LocalTime.parse(START_OF_DAY_TIME), UTC);

            }
            return ZonedDateTime.parse(date.concat(SPACE).concat(time), DateTimeFormatter.ofPattern(DATE_FORMATS).withZone(ZoneId.of(EUROPE_LONDON))).withZoneSameInstant(UTC);
        }

        return null;
    }

    private static int convertDurationIntoMinutes(final Set<String> distinctValueSize) {
        final Optional<String> durationValueOptional = distinctValueSize.stream().findFirst();
        final int[] estimateMinutes = {0};

        if (durationValueOptional.isPresent()) {
            final String durationValue = durationValueOptional.get();

            Stream.of(durationValue.split(COMMA_REGEX)).map(String::toLowerCase).collect(toList()).forEach(durationElement -> {
                if (durationElement.contains(DurationElements.WEEKS.name().toLowerCase())) {
                    estimateMinutes[0] += parseInt(digit().retainFrom(durationElement)) * DAYS_IN_A_WEEK * MINUTES_IN_A_DAY;
                } else if (durationElement.contains(DAYS.name().toLowerCase())) {
                    estimateMinutes[0] += parseInt(digit().retainFrom(durationElement)) * MINUTES_IN_A_DAY;
                } else if (durationElement.contains(HOURS.name().toLowerCase())) {
                    estimateMinutes[0] += parseLong(digit().retainFrom(durationElement)) * MINUTES_IN_HOUR;
                } else if (durationElement.contains(MINUTES.name().toLowerCase())) {
                    estimateMinutes[0] += parseLong(digit().retainFrom(durationElement));
                }
            });
        }
        return estimateMinutes[0];
    }
}