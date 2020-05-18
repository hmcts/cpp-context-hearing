package uk.gov.moj.cpp.hearing.event.delegates.helper;

import static com.google.common.base.CharMatcher.DIGIT;
import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;
import static java.lang.String.format;
import static java.lang.System.lineSeparator;
import static java.time.ZoneOffset.UTC;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static uk.gov.justice.core.courts.NextHearing.nextHearing;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.DurationElements.DAYS;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.DurationElements.HOURS;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.DurationElements.MINUTES;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference.HCHOUSE;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference.HCROOM;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference.HDATE;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference.HEST;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference.HTYPE;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference.bookingReference;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference.existingHearingId;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference.fixedDate;
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
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;
import uk.gov.moj.cpp.hearing.event.relist.metadata.DurationElements;
import uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference;
import uk.gov.moj.cpp.hearing.event.service.CourtHouseReverseLookup;
import uk.gov.moj.cpp.hearing.event.service.CourtRoomOuCodeReverseLookup;
import uk.gov.moj.cpp.hearing.event.service.HearingTypeReverseLookup;
import uk.gov.moj.cpp.hearing.event.service.ReferenceDataService;

import java.time.LocalDate;
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

public class NextHearingHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(NextHearingHelper.class.getName());
    private static final String ADJOURNMENT_REASONS = "Adjournment Reasons";
    public static final String CROWN_COURT_RESULT_DEFINITION_ID = "fbed768b-ee95-4434-87c8-e81cbc8d24c8";
    public static final String MAGISTRATE_RESULT_DEFINITION_ID = "70c98fa6-804d-11e8-adc0-fa7ae01bbebc";
    private static final String SPACE = " ";
    private static final String DATE_FORMATS = "[dd/MM/yyyy HH:mm][yyyy-MM-dd HH:mm][dd MMM yyyy HH:mm]";
    private static final String EUROPE_LONDON = "Europe/London";
    private static final int FIVE = 5;
    private static final int SIX = 6;
    private static final int MINUTES_IN_HOUR = 60;
    private static final int DAY = SIX * MINUTES_IN_HOUR;
    private static final String COMMA_REGEX = "\\s*,\\s*";


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
            return Optional.of(buildNextHearing(context, resultDefinition, resultLines, prompts));
        }

        LOGGER.warn("Cannot create nextHearing object for resultDefinition id={}", resultDefinition);
        return Optional.empty();
    }

    @SuppressWarnings({"squid:S1067"})
    private boolean canCreateNextHearing(final List<JudicialResultPrompt> prompts) {
        final Map<NextHearingPromptReference, JudicialResultPrompt> promptsMap = getPromptsMap(prompts);

        final boolean isFixedDatePromptsPresent = ofNullable(promptsMap.get(timeOfHearing)).isPresent() &&
                (ofNullable(promptsMap.get(fixedDate)).isPresent() || ofNullable(promptsMap.get(HDATE)).isPresent());

        final boolean isWeekCommencingPromptsPresent = ofNullable(promptsMap.get(weekCommencing)).isPresent();

        return ofNullable(promptsMap.get(HTYPE)).isPresent()
                && ofNullable(promptsMap.get(HCHOUSE)).isPresent()
                && ofNullable(promptsMap.get(HEST)).isPresent()
                && (isFixedDatePromptsPresent || isWeekCommencingPromptsPresent);
    }

    private NextHearing buildNextHearing(final JsonEnvelope context,
                                         final ResultDefinition resultDefinition,
                                         final List<ResultLine> resultLines,
                                         final List<JudicialResultPrompt> prompts) {

        final Map<NextHearingPromptReference, JudicialResultPrompt> promptsMap = getPromptsMap(prompts);
        final NextHearing.Builder builder = nextHearing();

        populateListedStartDateTime(builder, promptsMap);
        populateEstimatedDuration(builder, promptsMap);
        populateCourtCentre(builder, context, promptsMap);
        populateAdjournmentReasons(builder, context, resultLines);
        populateHearingType(builder, context, promptsMap);
        populateJurisdictionType(builder, resultDefinition.getId().toString());
        populateExistingHearingId(builder, promptsMap);
        populateReservedJudiciary(builder, promptsMap);
        populateWeekCommencingDate(builder, promptsMap);
        populateBookingReference(builder, promptsMap);

        return builder.build();
    }

    private Map<NextHearingPromptReference, JudicialResultPrompt> getPromptsMap(final List<JudicialResultPrompt> prompts) {
        return prompts.stream().filter(p -> isPresent(p.getPromptReference()))
                .collect(toMap(prompt -> valueOf(prompt.getPromptReference()), prompt -> prompt));
    }

    private static void populateListedStartDateTime(final NextHearing.Builder builder, final Map<NextHearingPromptReference, JudicialResultPrompt> promptsMap) {
        final String hDateValue = getPromptValue(promptsMap, HDATE);
        final String dateValue = hDateValue != null ? hDateValue : getPromptValue(promptsMap, fixedDate);

        builder.withListedStartDateTime(convertDateTimeToUTC(dateValue, getPromptValue(promptsMap, timeOfHearing)));
    }

    private static String getPromptValue(final Map<NextHearingPromptReference, JudicialResultPrompt> promptsMap, final NextHearingPromptReference nextHearingPromptReference) {
        return promptsMap.get(nextHearingPromptReference) != null ? promptsMap.get(nextHearingPromptReference).getValue() : null;
    }

    private static void populateEstimatedDuration(final NextHearing.Builder builder, final Map<NextHearingPromptReference, JudicialResultPrompt> promptsMap) {
        builder.withEstimatedMinutes(convertDurationIntoMinutes(Sets.newHashSet(promptsMap.get(HEST).getValue())));
    }

    private static void populateBookingReference(final NextHearing.Builder builder, final Map<NextHearingPromptReference, JudicialResultPrompt> promptsMap) {
        final String promptValue = getPromptValue(promptsMap, bookingReference);
        builder.withBookingReference(nonNull(promptValue) ? UUID.fromString(promptValue) : null);
    }

    private static void populateExistingHearingId(final NextHearing.Builder builder, final Map<NextHearingPromptReference, JudicialResultPrompt> promptsMap) {
        final String promptValue = getPromptValue(promptsMap, existingHearingId);
       builder.withExistingHearingId(nonNull(promptValue) ? UUID.fromString(promptValue) : null);
    }

    private static void populateReservedJudiciary(final NextHearing.Builder builder, final Map<NextHearingPromptReference, JudicialResultPrompt> promptsMap) {
        final String promptValue = getPromptValue(promptsMap, reservedJudiciary);
        builder.withReservedJudiciary(nonNull(promptValue) ? Boolean.valueOf(promptValue) : null);
    }

    private static void populateWeekCommencingDate(final NextHearing.Builder builder, final Map<NextHearingPromptReference, JudicialResultPrompt> promptsMap) {
        final String promptValue = getPromptValue(promptsMap, weekCommencing);
        builder.withWeekCommencingDate(nonNull(promptValue) ? LocalDate.parse(promptValue, DateTimeFormatter.ofPattern(PublishResultUtil.OUTGOING_PROMPT_DATE_FORMAT)) : null);
    }

    private void populateHearingType(final NextHearing.Builder builder,
                                     final JsonEnvelope context,
                                     final Map<NextHearingPromptReference, JudicialResultPrompt> promptsMap) {

        final JudicialResultPrompt prompt = promptsMap.get(HTYPE);
        final HearingType hearingType = hearingTypeReverseLookup.getHearingTypeByName(context, prompt.getValue());

        builder.withType(hearingType);
    }

    private void populateAdjournmentReasons(final NextHearing.Builder builder,
                                            final JsonEnvelope context,
                                            final List<ResultLine> resultLines) {

        final String adjournmentReasons = resultLines.stream()
                .filter(resultLine -> this.isAdjournmentReasonResult(context, resultLine))
                .map(NextHearingHelper::getAdjournmentsReasons)
                .collect(joining(format("%s%s", lineSeparator(), lineSeparator())));

        builder.withAdjournmentReason(adjournmentReasons);
    }

    private boolean isAdjournmentReasonResult(final JsonEnvelope context, final ResultLine resultLine) {
        final ResultDefinition resultDefinition = getResultDefinition(context, resultLine);

        return resultDefinition != null
                && resultDefinition.getResultDefinitionGroup() != null
                && ADJOURNMENT_REASONS.equalsIgnoreCase(resultDefinition.getResultDefinitionGroup());
    }

    private static String getAdjournmentsReasons(final ResultLine resultLine) {

        final String values = resultLine.getPrompts().stream()
                .map(p -> format("%s %s", p.getLabel(), p.getValue()))
                .collect(joining(lineSeparator()));

        return format("%s%s%s", resultLine.getResultLabel(), lineSeparator(), values);
    }

    private ResultDefinition getResultDefinition(final JsonEnvelope context, final ResultLine resultLine) {
        return this.referenceDataService.getResultDefinitionById(context, resultLine.getOrderedDate(), resultLine.getResultDefinitionId());
    }

    private void populateCourtCentre(final NextHearing.Builder builder,
                                     final JsonEnvelope context,
                                     final Map<NextHearingPromptReference, JudicialResultPrompt> promptsMap) {

        final CourtCentre courtCentre = extractCourtCentre(context, promptsMap);

        if (courtCentre != null) {
            builder.withCourtCentre(courtCentre);
        }
    }

    private CourtCentre extractCourtCentre(final JsonEnvelope context,
                                           final Map<NextHearingPromptReference, JudicialResultPrompt> promptsMap) {

        final Optional<JudicialResultPrompt> courtHouse = ofNullable(promptsMap.get(HCHOUSE));

        if (!courtHouse.isPresent()) {
            return null;
        }
        final Optional<CourtCentreOrganisationUnit> courtCentreOrgOptional = courtHouseReverseLookup.getCourtCentreByName(context, courtHouse.get().getValue());

        if (!courtCentreOrgOptional.isPresent()) {
            return null;
        }
        final CourtCentreOrganisationUnit courtCentreOrg = courtCentreOrgOptional.get();

        final CourtCentre.Builder courtCentreBuilder = CourtCentre.courtCentre()
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

    private static void populateJurisdictionType(final NextHearing.Builder builder,
                                                 final String resultDefinitionId) {
        if (CROWN_COURT_RESULT_DEFINITION_ID.equals(resultDefinitionId)) {
            builder.withJurisdictionType(JurisdictionType.CROWN);
        } else if (MAGISTRATE_RESULT_DEFINITION_ID.equals(resultDefinitionId)) {
            builder.withJurisdictionType(JurisdictionType.MAGISTRATES);
        }
    }

    private static boolean isNextHearingResult(final String resultDefinitionId) {

        return CROWN_COURT_RESULT_DEFINITION_ID.equals(resultDefinitionId)
                || MAGISTRATE_RESULT_DEFINITION_ID.equals(resultDefinitionId);
    }

    private static ZonedDateTime convertDateTimeToUTC(String date, String time) {
        if (!isEmpty(date) && !isEmpty(time)) {
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
                    estimateMinutes[0] += parseInt(DIGIT.retainFrom(durationElement)) * FIVE * DAY;
                } else if (durationElement.contains(DAYS.name().toLowerCase())) {
                    estimateMinutes[0] += parseInt(DIGIT.retainFrom(durationElement)) * DAY;
                } else if (durationElement.contains(HOURS.name().toLowerCase())) {
                    estimateMinutes[0] += parseLong(DIGIT.retainFrom(durationElement)) * MINUTES_IN_HOUR;
                } else if (durationElement.contains(MINUTES.name().toLowerCase())) {
                    estimateMinutes[0] += parseLong(DIGIT.retainFrom(durationElement));
                }
            });
        }
        return estimateMinutes[0];
    }
}