package uk.gov.moj.cpp.hearing.event.relist;

import static java.time.ZoneOffset.UTC;
import static java.util.Arrays.asList;
import static uk.gov.moj.cpp.hearing.event.relist.HearingAdjournHelper.getAllPromptUuidsByPromptReference;
import static uk.gov.moj.cpp.hearing.event.relist.HearingAdjournHelper.getDistinctPromptValue;
import static uk.gov.moj.cpp.hearing.event.relist.HearingAdjournHelper.getOffencesHaveResultNextHearing;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference.HCHOUSE;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference.HCROOM;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference.HDATE;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference.HEST;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference.HTIME;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference.HTYPE;

import uk.gov.justice.core.courts.Address;
import uk.gov.justice.core.courts.CourtCentre;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingType;
import uk.gov.justice.core.courts.NextHearing;
import uk.gov.justice.core.courts.NextHearingDefendant;
import uk.gov.justice.core.courts.NextHearingOffence;
import uk.gov.justice.core.courts.NextHearingProsecutionCase;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.justice.hearing.courts.referencedata.CourtCentreOrganisationUnit;
import uk.gov.justice.hearing.courts.referencedata.Courtrooms;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.HearingAdjourned;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.event.relist.metadata.DurationElements;
import uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingResultDefinition;
import uk.gov.moj.cpp.hearing.event.service.CourtHouseReverseLookup;
import uk.gov.moj.cpp.hearing.event.service.HearingTypeReverseLookup;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import com.google.common.base.CharMatcher;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"squid:S1188", "squid:S00112", "squid:S1166", "squid:S2259", "squid:S1612", "squid:S1168"})
public class HearingAdjournTransformer {
    public static final String EUROPE_LONDON = "Europe/London";
    public static final String SPACE = " ";
    private static final Logger LOGGER = LoggerFactory.getLogger(HearingAdjournTransformer.class.getName());
    private static final int FIVE = 5;
    private static final int SIX = 6;
    private static final int MINUTES_IN_HOUR = 60;
    private static final int DAY = SIX * MINUTES_IN_HOUR;
    private static final String COMMA_REGEX = "\\s*,\\s*";
    private static final String DATE_FORMATS = "[dd/MM/yyyy HH:mm][yyyy-MM-dd HH:mm]";
    @Inject
    private HearingTypeReverseLookup hearingTypeReverseLookup;

    @Inject
    private CourtHouseReverseLookup courtHouseReverseLookup;

    public HearingAdjourned transform2Adjournment(final JsonEnvelope context, final ResultsShared resultsShared,
                                                  final Map<UUID, NextHearingResultDefinition> nextHearingResultDefinitions) {
        final Hearing hearing = resultsShared.getHearing();
        final List<ResultLine> completedResultLines = getCompletedResultLines(resultsShared);
        final NextHearing.Builder nextHearingBuilder = NextHearing.nextHearing();

        if (hearing.getProsecutionCases() != null) {
            nextHearingBuilder.withNextHearingProsecutionCases(processProsecutionCases(hearing, resultsShared, completedResultLines, nextHearingResultDefinitions));
        }
        if (hearing.getCourtApplications() != null) {
            nextHearingBuilder.withNextHearingCourtApplicationId(hearing.getCourtApplications().stream().map(app -> app.getId()).collect(Collectors.toList()));
            if (hearing.getProsecutionCases() != null) {
                nextHearingBuilder.withNextHearingProsecutionCases(processLinkedProsecutionCases(hearing));
            }
        }

        getFirst(getDistinctPromptValue(completedResultLines, nextHearingResultDefinitions, getAllPromptUuidsByPromptReference(nextHearingResultDefinitions, HTYPE)))
                .ifPresent(value ->
                        {
                            final HearingType hearingType = hearingTypeReverseLookup.getHearingTypeByName(context, value);
                            if (hearingType == null) {
                                throw new RuntimeException(String.format("invalid hearing type %s for adjournment of hearing %s", value, hearing.getId()));
                            }
                            nextHearingBuilder.withType(hearingType);
                        }

                );

        nextHearingBuilder.withJurisdictionType(resultsShared.getHearing().getJurisdictionType());
        nextHearingBuilder.withReportingRestrictionReason(resultsShared.getHearing().getReportingRestrictionReason());
        nextHearingBuilder.withEstimatedMinutes(convertDurationIntoMinutes(getDistinctPromptValue(completedResultLines, nextHearingResultDefinitions, getAllPromptUuidsByPromptReference(nextHearingResultDefinitions, HEST))));
        nextHearingBuilder.withCourtCentre(hearing.getCourtCentre());
        final Set<String> strEarliestStartDates = getDistinctPromptValue(completedResultLines, nextHearingResultDefinitions, getAllPromptUuidsByPromptReference(nextHearingResultDefinitions, HDATE));
        final Set<String> strEarliestStartTimes = getDistinctPromptValue(completedResultLines, nextHearingResultDefinitions, getAllPromptUuidsByPromptReference(nextHearingResultDefinitions, HTIME));
        final String strEarliestStartDate = strEarliestStartDates.isEmpty() ? null : strEarliestStartDates.iterator().next();
        final String strEarliestStartTime = strEarliestStartTimes.isEmpty() ? null : strEarliestStartTimes.iterator().next();
        LOGGER.info("Hearing start date {} time {}", strEarliestStartDate, strEarliestStartTime);
        if (strEarliestStartDate == null) {
            throw new RuntimeException(String.format("cant find earliest starttime (%s) prompt to adjourn hearing %s ", HDATE.name(), hearing.getId()));
        }
        final ZonedDateTime startDateTime = convertDateTimeToUTC(strEarliestStartDate, strEarliestStartTime);
        nextHearingBuilder.withListedStartDateTime(startDateTime);

        LOGGER.info("Hearing start datetime {} ", startDateTime != null ? startDateTime.toString() : startDateTime);

        final Set<String> strCourthouse =
                getDistinctPromptValue(completedResultLines, nextHearingResultDefinitions, getAllPromptUuidsByPromptReference(nextHearingResultDefinitions, HCHOUSE));
        final Set<String> strCourtRoom =
                getDistinctPromptValue(completedResultLines, nextHearingResultDefinitions, getAllPromptUuidsByPromptReference(nextHearingResultDefinitions, HCROOM));

        final CourtCentre courtCentre = extractCourtCentre(context, strCourthouse, strCourtRoom);

        nextHearingBuilder.withCourtCentre(courtCentre);

        return new HearingAdjourned(hearing.getId(), asList(nextHearingBuilder.build()));
    }

    private List<NextHearingProsecutionCase> processProsecutionCases(final Hearing hearing,
                                                                     final ResultsShared resultsShared, final List<ResultLine> completedResultLines,
                                                                     final Map<UUID, NextHearingResultDefinition> nextHearingResultDefinitions) {
        final List<NextHearingProsecutionCase> nextProsecutionCases = new ArrayList<>();
        for (final ProsecutionCase prosecutionCase : hearing.getProsecutionCases()) {
            final List<NextHearingDefendant> nextDefendants = new ArrayList<>();
            for (final Defendant defendant : prosecutionCase.getDefendants()) {
                final List<Offence> offences = getOffencesHaveResultNextHearing(defendant, resultsShared.getTargets(), completedResultLines, nextHearingResultDefinitions);
                if (!offences.isEmpty()) {
                    final NextHearingDefendant nextHearingDefendant = NextHearingDefendant.nextHearingDefendant()
                            .withId(defendant.getId())
                            .withOffences(offences.stream().map(offence ->
                                    NextHearingOffence.nextHearingOffence().withId(offence.getId())
                                            .build())
                                    .collect(Collectors.toList())
                            )
                            .build();
                    nextDefendants.add(nextHearingDefendant);
                }
            }
            final NextHearingProsecutionCase nextCase = NextHearingProsecutionCase.nextHearingProsecutionCase()
                    .withId(prosecutionCase.getId())
                    .withDefendants(nextDefendants)
                    .build();
            if (!nextDefendants.isEmpty()) {
                nextProsecutionCases.add(nextCase);
            }
        }
        if (nextProsecutionCases.isEmpty()) {
            return null;
        } else {
            return nextProsecutionCases;
        }
    }

    private List<NextHearingProsecutionCase> processLinkedProsecutionCases(final Hearing hearing) {
        final List<NextHearingProsecutionCase> nextProsecutionCases = new ArrayList<>();
        for (final ProsecutionCase prosecutionCase : hearing.getProsecutionCases()) {
            final List<NextHearingDefendant> nextDefendants = new ArrayList<>();
            for (final Defendant defendant : prosecutionCase.getDefendants()) {
                final NextHearingDefendant nextHearingDefendant = NextHearingDefendant.nextHearingDefendant()
                        .withId(defendant.getId())
                        .withOffences(defendant.getOffences().stream().map(offence ->
                                NextHearingOffence.nextHearingOffence().withId(offence.getId())
                                        .build())
                                .collect(Collectors.toList())
                        )
                        .build();
                nextDefendants.add(nextHearingDefendant);
            }
            final NextHearingProsecutionCase nextCase = NextHearingProsecutionCase.nextHearingProsecutionCase()
                    .withId(prosecutionCase.getId())
                    .withDefendants(nextDefendants)
                    .build();
            if (!nextDefendants.isEmpty()) {
                nextProsecutionCases.add(nextCase);
            }
        }
        if (nextProsecutionCases.isEmpty()) {
            return null;
        } else {
            return nextProsecutionCases;
        }
    }

    private CourtCentre extractCourtCentre(final JsonEnvelope context, final Set<String> courtHouse, final Set<String> strCourtRoom) {
        if (courtHouse.isEmpty()) {
            return null;
        }
        final Optional<CourtCentreOrganisationUnit> courtCentreOrgOptional = courtHouseReverseLookup.getCourtCentreByName(context, courtHouse.iterator().next());
        if (!courtCentreOrgOptional.isPresent()) {
            return null;
        }
        final CourtCentreOrganisationUnit courtCentreOrg = courtCentreOrgOptional.get();
        final CourtCentre.Builder courtCentreBuilder = CourtCentre.courtCentre()
                .withName(courtCentreOrg.getOucodeL3Name())
                .withWelshName(courtCentreOrg.getOucodeL3WelshName())
                .withAddress(Address.address()
                        .withPostcode(courtCentreOrg.getPostcode())
                        .withAddress1(courtCentreOrg.getAddress1())
                        .withAddress2(courtCentreOrg.getAddress2())
                        .withAddress3(courtCentreOrg.getAddress3())
                        .withAddress4(courtCentreOrg.getAddress4())
                        .withAddress5(courtCentreOrg.getAddress5())
                        .build())
                .withId(UUID.fromString(courtCentreOrg.getId()));
        if (!strCourtRoom.isEmpty()) {
            final Optional<Courtrooms> courtRoom = courtHouseReverseLookup.getCourtRoomByRoomName(courtCentreOrg, strCourtRoom.iterator().next());
            if (courtRoom.isPresent()) {
                courtCentreBuilder
                        .withRoomId(courtRoom.get().getId())
                        .withRoomName(courtRoom.get().getCourtroomName())
                        .withWelshRoomName(courtRoom.get().getWelshCourtroomName());
            }
        }
        return courtCentreBuilder.build();
    }

    private List<ResultLine> getCompletedResultLines(final ResultsShared resultsShared) {
        return resultsShared.getTargets().stream().flatMap(target -> target.getResultLines().stream()).collect(Collectors.toList());
    }

    int convertDurationIntoMinutes(final Set<String> distinctValueSize) {
        final Optional<String> durationValueOptional = getFirst(distinctValueSize);
        final int[] estimateMinutes = {0};
        if (durationValueOptional.isPresent()) {
            final String durationValue = durationValueOptional.get();
            Stream.of(durationValue.split(COMMA_REGEX)).map(String::toLowerCase).collect(Collectors.toList()).forEach(durationElement -> {
                if (durationElement.contains(DurationElements.WEEKS.name().toLowerCase())) {
                    estimateMinutes[0] += Integer.parseInt(CharMatcher.DIGIT.retainFrom(durationElement)) * FIVE * DAY;
                } else if (durationElement.contains(DurationElements.DAYS.name().toLowerCase())) {
                    estimateMinutes[0] += Integer.parseInt(CharMatcher.DIGIT.retainFrom(durationElement)) * DAY;
                } else if (durationElement.contains(DurationElements.HOURS.name().toLowerCase())) {
                    estimateMinutes[0] += Long.parseLong(CharMatcher.DIGIT.retainFrom(durationElement)) * MINUTES_IN_HOUR;
                } else if (durationElement.contains(DurationElements.MINUTES.name().toLowerCase())) {
                    estimateMinutes[0] += Long.parseLong(CharMatcher.DIGIT.retainFrom(durationElement));
                }

            });
        }
        return estimateMinutes[0];
    }

    private Optional<String> getFirst(Set<String> value) {
        return value.stream().findFirst();
    }


    private ZonedDateTime convertDateTimeToUTC(String date, String time) {
        if (!StringUtils.isEmpty(date) && !StringUtils.isEmpty(time)) {
            return ZonedDateTime.parse(date.concat(SPACE).concat(time), DateTimeFormatter.ofPattern(DATE_FORMATS).withZone(ZoneId.of(EUROPE_LONDON))).withZoneSameInstant(UTC);
        }
        return null;
    }


}

