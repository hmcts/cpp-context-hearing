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
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.shared.Constants.NO_PROMPT_DEFINITION_FOUND_EXCEPTION_FORMAT;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.shared.Constants.SPACE;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.shared.Constants.START_OF_DAY_TIME;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.restructure.shared.JudicialResultPromptMapper.findJudicialResultPrompt;
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
import static uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference.endDate;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference.existingHearingId;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference.firstReviewDate;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference.fixedDate;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference.hCHOUSEOrganisationName;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference.hmiSlots;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference.isPresent;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference.judgeReservesReviewHearing;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference.probationteamtobenotifiedAddress1;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference.probationteamtobenotifiedAddress2;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference.probationteamtobenotifiedAddress3;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference.probationteamtobenotifiedAddress4;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference.probationteamtobenotifiedAddress5;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference.probationteamtobenotifiedOrganisationName;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference.probationteamtobenotifiedPostCode;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference.reservedJudiciary;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference.suspendedPeriod;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference.timeOfHearing;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference.totalCustodialPeriod;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference.valueOf;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference.weekCommencing;

import uk.gov.justice.core.courts.Address;
import uk.gov.justice.core.courts.CourtCentre;
import uk.gov.justice.core.courts.HearingType;
import uk.gov.justice.core.courts.JudicialResultPrompt;
import uk.gov.justice.core.courts.JurisdictionType;
import uk.gov.justice.core.courts.NextHearing;
import uk.gov.justice.core.courts.Prompt;
import uk.gov.justice.core.courts.ResultLine2;
import uk.gov.justice.core.courts.RotaSlot;
import uk.gov.justice.hearing.courts.referencedata.CourtCentreOrganisationUnit;
import uk.gov.justice.hearing.courts.referencedata.Courtrooms;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsSharedV3;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.json.JsonObject;

import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"squid:CallToDeprecatedMethod", "pmd:NullAssignment"})
public class NextHearingHelperV3 {

    private static final String FAILED_TO_CREATE_NEXT_HEARING_MESSAGE = "Failed to create next hearing for result definition id=%s";
    private static final Logger LOGGER = LoggerFactory.getLogger(NextHearingHelperV3.class.getName());
    public static final String FIRST_REVIEW_HEARING_RESULT_DEFINITION_ID_CO = "1f4146dd-e389-4973-8951-876e3634efd5";
    public static final String FIRST_REVIEW_HEARING_RESULT_DEFINITION_ID_DRNRR = "b3ed14c1-d921-459c-90fd-400a5d8d0076";
    public static final String FIRST_REVIEW_HEARING_RESULT_DEFINITION_ID_SUSPS = "859c7729-93bf-4fda-8345-3e56695dddb5";
    public static final String SUSPENDED_SENTENCE_ORDER = "8b1cff00-a456-40da-9ce4-f11c20959084";
    public static final String DRUG_REHABILITATION_RESIDENTIAL_WITH_REVIEW = "61ea03c9-c113-446b-a392-402144fcd9e8";
    public static final String DRUG_REHABILITATION_NON_RESIDENTIAL_WITH_REVIEW = "cc2cbb94-b75a-4a8c-9840-31c5f8007724";
    public static final String COMMUNITY_REQUIREMENT = "b2dab2b7-3edd-4223-b1be-3819173ec54d";
    public static final String COMMUNITY_ORDER = "418b3aa7-65ab-4a4a-bab9-2f96b698118c";
    public static final String CUSTODIAL_PERIOD_JUDICIAL_RESULT_TYPE_ID = "b65fb5f1-b11d-4a95-a198-3b81333c7cf9";
    public static final String TOTAL_CUSTODIAL_PERIOD_SHORT_CODE = "STIMP";

    @Inject
    private HearingTypeReverseLookup hearingTypeReverseLookup;
    @Inject
    private CourtHouseReverseLookup courtHouseReverseLookup;
    @Inject
    private CourtRoomOuCodeReverseLookup courtRoomOuCodeReverseLookup;
    @Inject
    private ReferenceDataService referenceDataService;
    @Inject
    private StringToJsonObjectConverter stringToJsonObjectConverter;
    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    public Optional<NextHearing> getNextHearing(final JsonEnvelope context,
                                                final ResultDefinition resultDefinition,
                                                final List<ResultLine2> resultLines,
                                                final ResultLine2 resultLine,
                                                final List<JudicialResultPrompt> prompts,
                                                final ResultsSharedV3 resultSharedV3,
                                                final List<ResultDefinition> resultDefinitions) {
        final String resultDefinitionId = resultDefinition.getId().toString();
        if (isNextHearingResult(resultDefinitionId, resultLines) && canCreateNextHearing(prompts)) {
            LOGGER.info("Creating next hearing");
            final NextHearing nextHearing = buildNextHearing(context, resultDefinition, resultLines, resultLine, prompts, resultDefinitions);

            if (isNull(nextHearing)) {
                final String message = format(FAILED_TO_CREATE_NEXT_HEARING_MESSAGE, resultDefinitionId);
                LOGGER.error(message);
                throw new NextHearingCreateException(message);
            } else {
                if (nonNull(nextHearing.getReservedJudiciary()) && nextHearing.getReservedJudiciary().equals(true)) {
                    nextHearing.setJudiciary(resultSharedV3.getHearing().getJudiciary());
                }
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
        return isNextHearingPresent(promptsMap, isFixedDatePromptsPresent, isWeekCommencingPromptsPresent, isDateToBeFixed);
    }

    private boolean isNextHearingPresent(final Map<NextHearingPromptReference, JudicialResultPrompt> promptsMap, final boolean isFixedDatePromptsPresent, final boolean isWeekCommencingPromptsPresent, final boolean isDateToBeFixed) {
        final boolean hcHouseAvailable = ofNullable(promptsMap.get(hCHOUSEOrganisationName)).isPresent() || ofNullable(promptsMap.get(HCHOUSE)).isPresent();
        final boolean isNextHearingDatePromptsPresent = (isFixedDatePromptsPresent || isWeekCommencingPromptsPresent || isDateToBeFixed);
        return (ofNullable(promptsMap.get(HTYPE)).isPresent()
                && hcHouseAvailable
                && ofNullable(promptsMap.get(HEST)).isPresent()
                && isNextHearingDatePromptsPresent) || ofNullable(promptsMap.get(firstReviewDate)).isPresent();
    }

    private NextHearing buildNextHearing(final JsonEnvelope context,
                                         final ResultDefinition resultDefinition,
                                         final List<ResultLine2> resultLines,
                                         final ResultLine2 resultLine,
                                         final List<JudicialResultPrompt> prompts,
                                         final List<ResultDefinition> resultDefinitions) {

        final Map<NextHearingPromptReference, JudicialResultPrompt> promptsMap = getPromptsMap(prompts);
        final NextHearing.Builder builder = nextHearing();
        final Optional<CourtCentreOrganisationUnit> courtCentreOrgOptional = getCourtCentreOrganisationUnit(context, promptsMap);
        final boolean isFirstReviewCO = resultDefinition.getId().equals(UUID.fromString(FIRST_REVIEW_HEARING_RESULT_DEFINITION_ID_CO));
        final boolean isFirstReviewSUSPS = resultDefinition.getId().equals(UUID.fromString(FIRST_REVIEW_HEARING_RESULT_DEFINITION_ID_SUSPS));
        final boolean isFirstReviewDRNRR = resultDefinition.getId().equals(UUID.fromString(FIRST_REVIEW_HEARING_RESULT_DEFINITION_ID_DRNRR));
        if (isFirstReviewCO || isFirstReviewSUSPS || isFirstReviewDRNRR) {
            if (resultLine.getPrompts().isEmpty()) {
                return null;
            }
            populateFirstReviewHearingPrompts(context, resultLines, resultLine, prompts, promptsMap, builder, courtCentreOrgOptional, resultDefinitions);
            builder.withApplicationTypeCode(resultDefinition.getTriggeredApplicationCode());
        } else {
            populateListedStartDateTime(builder, promptsMap, courtCentreOrgOptional);
            populateEstimatedDuration(builder, promptsMap);
            populateHearingType(builder, context, promptsMap);
            populateAdjournmentReasons(builder, context, resultLines, resultLine);
            populateWeekCommencingDate(builder, promptsMap);
            populateDateToBeFixed(builder, promptsMap);
        }
        populateCourtCentre(builder, context, promptsMap, courtCentreOrgOptional);
        populateAdjournmentReasons(builder, context, resultLines, resultLine);
        populateHearingType(builder, context, promptsMap);
        populateJurisdictionType(builder, resultDefinition.getId().toString());
        populateExistingHearingId(builder, promptsMap);
        populateReservedJudiciary(builder, promptsMap);
        populateWeekCommencingDate(builder, promptsMap);
        populateBookingReference(builder, promptsMap);
        populateDateToBeFixed(builder, promptsMap);
        populateHmiSlots(builder, promptsMap);

        return builder.build();
    }

    @SuppressWarnings({"squid:S3776", "squid:S1188"})
    private void populateFirstReviewHearingPrompts(final JsonEnvelope context, final List<ResultLine2> resultLines, final ResultLine2 resultLine, final List<JudicialResultPrompt> prompts, final Map<NextHearingPromptReference, JudicialResultPrompt> promptsMap, final NextHearing.Builder builder,
                                                   final Optional<CourtCentreOrganisationUnit> courtCentreOrgOptional,
                                                   final List<ResultDefinition> resultDefinitions) {

        final List<ResultLine2> allResultLines = new ArrayList<>();
        getAllResultLines(resultLine, resultLines, allResultLines);

        final List<ResultLine2> allParentResultLines = new ArrayList<>();
        getParentResultLines(resultLine, resultLines, allParentResultLines);
        populateListedStartDateTimeReviewHearing(builder, promptsMap, courtCentreOrgOptional);
        builder.withEstimatedMinutes(hearingTypeReverseLookup.getDefaultDurationInMin(context, "Review"));
        builder.withIsFirstReviewHearing(true);
        builder.withType(hearingTypeReverseLookup.getHearingTypeByName(context, "Review"));

        allResultLines.forEach(lineId -> {
            final ResultDefinition resultDefinition1 = resultDefinitions.stream()
                    .filter(rd -> rd.getId().equals(lineId.getResultDefinitionId()))
                    .findFirst()
                    .orElse(null);

            if (nonNull(resultDefinition1) && nonNull(resultDefinition1.getShortCode()) && "STIMP,CO,SUSPS".toLowerCase().contains(resultDefinition1.getShortCode().toLowerCase())) {
                final List<JudicialResultPrompt> judicialResultPrompts = buildJudicialResultPrompt(resultDefinition1, lineId.getPrompts());
                final Map<NextHearingPromptReference, JudicialResultPrompt> parentPromptsMap = getPromptsMap(judicialResultPrompts);
                populateProbationTeam(builder, parentPromptsMap);

                if (TOTAL_CUSTODIAL_PERIOD_SHORT_CODE.equalsIgnoreCase(resultDefinition1.getShortCode())) {
                    populateTotalCustodialPeriod(builder, parentPromptsMap);
                    populateSuspendedPeriod(builder, parentPromptsMap);
                }
            }
        });
        allParentResultLines.forEach(lineId -> {
            final ResultLine2 resultLine3 = resultLines.stream().filter(resultLine2 -> resultLine2.getResultLineId().equals(lineId.getResultLineId())).findFirst().get();
            final ResultDefinition parentResultDefinition = resultDefinitions.stream()
                    .filter(rd -> rd.getId().equals(lineId.getResultDefinitionId()))
                    .findFirst()
                    .orElse(null);

            if (nonNull(parentResultDefinition) && nonNull(parentResultDefinition.getShortCode()) && (parentResultDefinition.getShortCode().equalsIgnoreCase(TOTAL_CUSTODIAL_PERIOD_SHORT_CODE)))
            {
                final List<JudicialResultPrompt> judicialResultPrompts = buildJudicialResultPrompt(parentResultDefinition, resultLine3.getPrompts());
                final Map<NextHearingPromptReference, JudicialResultPrompt> parentPromptsMap = getPromptsMap(judicialResultPrompts);
                populateTotalCustodialPeriod(builder, parentPromptsMap);
                populateSuspendedPeriod(builder, parentPromptsMap);
            }
            if (SUSPENDED_SENTENCE_ORDER.equals(parentResultDefinition.getId().toString()) || COMMUNITY_ORDER.equals(parentResultDefinition.getId().toString())) {
                builder.withOrderName(parentResultDefinition.getLabel());
                if (COMMUNITY_ORDER.equals(parentResultDefinition.getId().toString())) {
                    final Map<NextHearingPromptReference, JudicialResultPrompt> parentPromptsMap = getPromptsMap(resultLine3.getPrompts().stream()
                            .map(prompt -> {
                                        final uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt promptDefinition = parentResultDefinition.getPrompts().stream()
                                                .filter(promptDef -> promptDef.getId().equals(prompt.getId()) && (isNull(prompt.getPromptRef()) || prompt.getPromptRef().equals(promptDef.getReference())))
                                                .findFirst().orElseThrow(() -> new RuntimeException(format(NO_PROMPT_DEFINITION_FOUND_EXCEPTION_FORMAT, prompt.getId(), prompt.getPromptRef(), prompt.getLabel(), prompt.getValue(), parentResultDefinition.getId())));
                                        return findJudicialResultPrompt(prompt, promptDefinition);
                                    }
                            )
                            .collect(toList()));
                    populateEndDate(builder, parentPromptsMap);
                }
            }
        });
    }


    private void getParentResultLines(final ResultLine2 resultLine, final List<ResultLine2> resultLines, final List<ResultLine2> allParentResultLines) {
        if (nonNull(resultLine) && resultLine.getParentResultLineIds() != null && !resultLine.getParentResultLineIds().isEmpty()) {
            resultLine.getParentResultLineIds().forEach(lineId -> {
                LOGGER.info("parent result line id :  {}", lineId);
                final Optional<ResultLine2> optionalResultLine2 = resultLines.stream().filter(resultLine2 -> resultLine2.getResultLineId().equals(lineId)).findFirst();
                if (optionalResultLine2.isPresent() && optionalResultLine2.get() != null) {
                    getParentResultLines(optionalResultLine2.get(), resultLines, allParentResultLines);
                }
            });
        }
        allParentResultLines.add(resultLine);
    }

    private void getAllResultLines(final ResultLine2 resultLine, final List<ResultLine2> resultLines, final List<ResultLine2> allParentResultLines) {
        if (nonNull(resultLine)) {
            allParentResultLines.add(resultLine);
        }
        allParentResultLines.addAll(resultLines);
    }


    private void populateEndDate(final NextHearing.Builder builder, final Map<NextHearingPromptReference, JudicialResultPrompt> promptsMap) {
        final String promptValue = getPromptValue(promptsMap, endDate);
        if (nonNull(promptValue)) {
            builder.withEndDate(LocalDate.parse(promptValue, DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        }
    }

    private void populateProbationTeam(final NextHearing.Builder builder, final Map<NextHearingPromptReference, JudicialResultPrompt> parentPromptsMap) {
        final String promptValue = getPromptValue(parentPromptsMap, probationteamtobenotifiedOrganisationName);
        builder.withProbationTeamName(promptValue);
        final String teamAddress = Stream.of(getPromptValue(parentPromptsMap, probationteamtobenotifiedAddress1),
                        getPromptValue(parentPromptsMap, probationteamtobenotifiedAddress2),
                        getPromptValue(parentPromptsMap, probationteamtobenotifiedAddress3),
                        getPromptValue(parentPromptsMap, probationteamtobenotifiedAddress4),
                        getPromptValue(parentPromptsMap, probationteamtobenotifiedAddress5),
                        getPromptValue(parentPromptsMap, probationteamtobenotifiedPostCode)
                )
                .filter(s -> s != null && !s.isEmpty())
                .collect(Collectors.joining(", "));
        builder.withProbationTeamAddress(teamAddress);
    }

    private void populateSuspendedPeriod(final NextHearing.Builder builder, final Map<NextHearingPromptReference, JudicialResultPrompt> parentPromptsMap) {
        final String promptValue = getPromptValue(parentPromptsMap, suspendedPeriod);
        builder.withSuspendedPeriod(promptValue);
    }

    private void populateTotalCustodialPeriod(final NextHearing.Builder builder, final Map<NextHearingPromptReference, JudicialResultPrompt> parentPromptsMap) {
        final String promptValue = getPromptValue(parentPromptsMap, totalCustodialPeriod);
        builder.withTotalCustodialPeriod(promptValue);
    }


    private List<JudicialResultPrompt> buildJudicialResultPrompt(final ResultDefinition resultDefinition, final List<Prompt> prompts) {
        return prompts.stream()
                .map(prompt -> {
                            final uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt promptDefinition = resultDefinition.getPrompts().stream()
                                    .filter(promptDef -> promptDef.getId().equals(prompt.getId()) && (isNull(prompt.getPromptRef()) || prompt.getPromptRef().equals(promptDef.getReference())))
                                    .findFirst().orElseThrow(() -> new RuntimeException(format(NO_PROMPT_DEFINITION_FOUND_EXCEPTION_FORMAT, prompt.getId(), prompt.getPromptRef(), prompt.getLabel(), prompt.getValue(), resultDefinition.getId())));
                            return findJudicialResultPrompt(prompt, promptDefinition);
                        }
                )
                .collect(toList());
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

    private void populateListedStartDateTimeReviewHearing(final NextHearing.Builder builder, final Map<NextHearingPromptReference, JudicialResultPrompt> promptsMap, final Optional<CourtCentreOrganisationUnit> courtCentreOrganisationUnit) {
        final String hDateValue = getPromptValue(promptsMap, firstReviewDate);
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
        final HashSet<String> distinctEstimatedDurations = Sets.newHashSet(promptsMap.get(HEST).getValue());
        final Optional<String> firstEstimatedDuration = distinctEstimatedDurations.stream().findFirst();
        final int estimatedMinutes = convertDurationIntoMinutes(firstEstimatedDuration);
        LOGGER.info("Populating estimated duration minutes: {}", estimatedMinutes);
        builder.withEstimatedMinutes(estimatedMinutes);

        if (firstEstimatedDuration.isPresent()) {
            final String estimatedDuration = firstEstimatedDuration.get();
            LOGGER.info("Populating estimated duration: {}", estimatedDuration);
            builder.withEstimatedDuration(estimatedDuration);
        }
    }

    private void populateBookingReference(final NextHearing.Builder builder, final Map<NextHearingPromptReference, JudicialResultPrompt> promptsMap) {
        final String promptValue = getPromptValue(promptsMap, bookingReference);
        LOGGER.info("Populating booking reference: {}", promptValue);
        builder.withBookingReference(nonNull(promptValue) ? UUID.fromString(promptValue) : null);
    }

    private void populateHmiSlots(final NextHearing.Builder builder, final Map<NextHearingPromptReference, JudicialResultPrompt> promptsMap) {
        final String promptValue = getPromptValue(promptsMap, hmiSlots);
        LOGGER.info("Populating booking reference: {}", promptValue);
        if (nonNull(promptValue)) {
            final JsonObject jsonObject = stringToJsonObjectConverter.convert(promptValue);
            final List<RotaSlot> hmiSlots = new ArrayList<>();
            jsonObject.getJsonArray("hmiSlots").forEach(jsonValue -> hmiSlots.add(jsonObjectToObjectConverter.convert(((JsonObject) jsonValue), RotaSlot.class)));
            builder.withHmiSlots(hmiSlots);
        }
    }

    private void populateExistingHearingId(final NextHearing.Builder builder, final Map<NextHearingPromptReference, JudicialResultPrompt> promptsMap) {
        final String promptValue = getPromptValue(promptsMap, existingHearingId);
        LOGGER.info("Populating existing hearing id: {}", promptValue);
        builder.withExistingHearingId(nonNull(promptValue) ? UUID.fromString(promptValue) : null);
    }

    private void populateReservedJudiciary(final NextHearing.Builder builder, final Map<NextHearingPromptReference, JudicialResultPrompt> promptsMap) {
        final String promptValue = getPromptValue(promptsMap, reservedJudiciary);
        if (nonNull(promptValue)) {
            final String reservedJuddiciary = getPromptValue(promptsMap, judgeReservesReviewHearing);
            builder.withReservedJudiciary(nonNull(reservedJuddiciary) ? Boolean.valueOf(promptValue) : null);
        }
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
        if (nonNull(prompt)) {
            final HearingType hearingType = hearingTypeReverseLookup.getHearingTypeByName(context, prompt.getValue());
            LOGGER.info("Populating hearing type: {}", hearingType);
            builder.withType(hearingType);
        }
    }

    private void populateAdjournmentReasons(final NextHearing.Builder builder,
                                            final JsonEnvelope context,
                                            final List<ResultLine2> resultLines,
                                            final ResultLine2 resultLine) {

        final String adjournmentReasons = resultLines.stream()
                .filter(rl -> this.isAdjournmentReasonResult(context, rl) && belongToSameOffenceOrApplication(resultLine, rl))
                .map(this::getAdjournmentsReasons)
                .collect(joining(format("%s", lineSeparator())));


        LOGGER.info("Populating adjournment reason: {}", adjournmentReasons);
        builder.withAdjournmentReason(adjournmentReasons);
    }

    private boolean belongToSameOffenceOrApplication(final ResultLine2 resultLine, final ResultLine2 rl) {
        boolean match = false;
        if (nonNull(rl.getOffenceId())) {
            match = rl.getOffenceId().equals(resultLine.getOffenceId());
        } else if (nonNull(rl.getApplicationId())) {
            match = rl.getApplicationId().equals(resultLine.getApplicationId());
        }
        return match;
    }

    private boolean isAdjournmentReasonResult(final JsonEnvelope context, final ResultLine2 resultLine) {
        final ResultDefinition resultDefinition = getResultDefinition(context, resultLine);

        return resultDefinition != null
                && resultDefinition.getResultDefinitionGroup() != null
                && ADJOURNMENT_REASONS.equalsIgnoreCase(resultDefinition.getResultDefinitionGroup());
    }

    private String getAdjournmentsReasons(final ResultLine2 resultLine) {

        final String values = resultLine.getPrompts().stream()
                .map(p -> format("%s %s", p.getLabel(), p.getValue()))
                .collect(joining());
        if (!values.isEmpty()) {
            return format("%s%s%s", resultLine.getResultLabel(), lineSeparator(), values);
        } else {
            return resultLine.getResultLabel();
        }
    }

    private ResultDefinition getResultDefinition(final JsonEnvelope context, final ResultLine2 resultLine) {
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
                .withCode(courtCentreOrg.getOucode())
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

    private static boolean isNextHearingResult(final String resultDefinitionId, final List<ResultLine2> resultLines) {
        LOGGER.info("is result should create next hearing");
        final boolean isFirstReviewHearingCO = FIRST_REVIEW_HEARING_RESULT_DEFINITION_ID_CO.equals(resultDefinitionId);
        final boolean isFirstReviewHearingDRNRR = FIRST_REVIEW_HEARING_RESULT_DEFINITION_ID_DRNRR.equals(resultDefinitionId);
        final boolean isFirstReviewHearingSUSPS = FIRST_REVIEW_HEARING_RESULT_DEFINITION_ID_SUSPS.equals(resultDefinitionId);
        if (isFirstReviewHearingCO || isFirstReviewHearingDRNRR || isFirstReviewHearingSUSPS) {
            return isFirstReviewApplicable(resultLines, isFirstReviewHearingCO, isFirstReviewHearingDRNRR, isFirstReviewHearingSUSPS);
        } else {
            return CROWN_COURT_RESULT_DEFINITION_ID.equals(resultDefinitionId)
                    || MAGISTRATE_RESULT_DEFINITION_ID.equals(resultDefinitionId);
        }
    }

    private static boolean isFirstReviewApplicable(final List<ResultLine2> resultLines, final boolean isFirstReviewHearingCO, final boolean isFirstReviewHearingDRNRR, final boolean isFirstReviewHearingSUSPS) {
        final List<UUID> resultDefinitionIds = resultLines.stream().map(ResultLine2::getResultDefinitionId).collect(toList());
        final boolean isDrugRehab = isDrugRehab(resultDefinitionIds);
        final boolean isSuspendedOrder = resultDefinitionIds.contains(UUID.fromString(SUSPENDED_SENTENCE_ORDER)) && (isDrugRehab || resultDefinitionIds.contains(UUID.fromString(CUSTODIAL_PERIOD_JUDICIAL_RESULT_TYPE_ID)));
        final boolean isCommunityOrder = isCommunityOrder(resultDefinitionIds, isDrugRehab);

        final boolean isFirstHearingCO_SUSPS = isFirstReviewHearingCO || isFirstReviewHearingSUSPS;
        return ((isFirstHearingCO_SUSPS || isFirstReviewHearingDRNRR) && (isSuspendedOrder || isCommunityOrder));
    }

    private static boolean isDrugRehab(final List<UUID> resultDefinitionIds) {
        return resultDefinitionIds.contains(UUID.fromString(DRUG_REHABILITATION_NON_RESIDENTIAL_WITH_REVIEW)) ||
                resultDefinitionIds.contains(UUID.fromString(DRUG_REHABILITATION_RESIDENTIAL_WITH_REVIEW));
    }

    private static boolean isCommunityOrder(final List<UUID> resultDefinitionIds, final boolean isDrugRehab) {
        return resultDefinitionIds.contains(UUID.fromString(COMMUNITY_ORDER)) && (isDrugRehab || (resultDefinitionIds.contains(UUID.fromString(COMMUNITY_REQUIREMENT))
                && !(resultDefinitionIds.contains(UUID.fromString(DRUG_REHABILITATION_NON_RESIDENTIAL_WITH_REVIEW))
                || resultDefinitionIds.contains(UUID.fromString(DRUG_REHABILITATION_RESIDENTIAL_WITH_REVIEW)))));
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

    private static int convertDurationIntoMinutes(final Optional<String> durationValueOptional) {
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