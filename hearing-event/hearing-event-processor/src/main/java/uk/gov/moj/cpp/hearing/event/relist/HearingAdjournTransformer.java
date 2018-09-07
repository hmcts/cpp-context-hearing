package uk.gov.moj.cpp.hearing.event.relist;

import static uk.gov.moj.cpp.hearing.event.relist.HearingAdjournHelper.getAllPromptUuidsByPromptReference;
import static uk.gov.moj.cpp.hearing.event.relist.HearingAdjournHelper.getDistinctPromptValue;
import static uk.gov.moj.cpp.hearing.event.relist.HearingAdjournHelper.getOffencesHaveResultNextHearing;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference.HDATE;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference.HEST;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference.HTIME;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference.HTYPE;

import uk.gov.justice.json.schemas.core.Offence;
import uk.gov.justice.json.schemas.core.ProsecutionCase;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLine;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.event.relist.metadata.DurationElements;
import uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingResultDefinition;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import com.google.common.base.CharMatcher;
import org.apache.commons.lang3.StringUtils;

public class HearingAdjournTransformer {

    private static final int FIVE = 5;
    private static final int SIX = 6;
    private static final int MINUTE = 60;
    private static final int DAY = SIX * MINUTE;
    private static final String COMMA_REGEX = "\\s*,\\s*";
    private static final String EMPTY_STRING = "";
    private static final String DD_MM_YYYY = "dd/MM/yyyy";
    private static final String YYYY_MM_DD = "yyyy-MM-dd";
    private static final String ID = "id";
    private static final String OFFENCES = "offences";
    private static final String COURT_CENTRE_ID = "courtCentreId";
    private static final String TYPE = "type";
    private static final String START_DATE = "startDate";
    private static final String START_TIME = "startTime";
    private static final String ESTIMATE_MINUTES = "estimateMinutes";
    private static final String DEFENDANTS = "defendants";
    private static final String CASE_ID = "caseId";
    private static final String URN = "urn";
    private static final String REQUESTED_BY_HEARING_ID = "requestedByHearingId";
    private static final String HEARINGS = "hearings";


    public JsonObject transform(final ResultsShared resultsShared, final Map<UUID, NextHearingResultDefinition> nextHearingResultDefinitions) {
        final ProsecutionCase firstCase = resultsShared.getHearing().getProsecutionCases().get(0);//Hardcoded 0 as we are not handling mutiple cases yet, should be removed when services handle multi case hearing
        final List<CompletedResultLine> completedResultLines = resultsShared.getCompletedResultLines();

        final JsonArrayBuilder defendants = Json.createArrayBuilder();
        resultsShared.getHearing().getProsecutionCases().stream().flatMap(pc->pc.getDefendants().stream()).forEach(defendant -> {
            final List<Offence> offences = getOffencesHaveResultNextHearing(defendant, completedResultLines, nextHearingResultDefinitions);
            if (!offences.isEmpty()) {
                final JsonArrayBuilder jsonOffences = Json.createArrayBuilder();
                offences.forEach(offence -> {
                    final JsonObject offenceJson = Json.createObjectBuilder()
                            .add(ID, offence.getId().toString())
                            .build();
                    jsonOffences.add(offenceJson);
                });
                final JsonObject defendantJson = Json.createObjectBuilder()
                        .add(ID, defendant.getId().toString())
                        .add(OFFENCES, jsonOffences.build())
                        .build();
                defendants.add(defendantJson);
            }
        });

        final JsonObjectBuilder hearing = Json.createObjectBuilder();

        hearing.add(COURT_CENTRE_ID, resultsShared.getHearing().getCourtCentre().getId().toString());
        getFirst(getDistinctPromptValue(completedResultLines, nextHearingResultDefinitions, getAllPromptUuidsByPromptReference(nextHearingResultDefinitions, HTYPE)))
                .ifPresent(value -> hearing.add(TYPE, value));
        getFirst(getDistinctPromptValue(completedResultLines, nextHearingResultDefinitions, getAllPromptUuidsByPromptReference(nextHearingResultDefinitions, HDATE)))
                .ifPresent(value -> hearing.add(START_DATE, convertDate(value)));
        getFirst(getDistinctPromptValue(completedResultLines, nextHearingResultDefinitions, getAllPromptUuidsByPromptReference(nextHearingResultDefinitions, HTIME)))
                .ifPresent(value -> hearing.add(START_TIME, value));
        hearing.add(ESTIMATE_MINUTES, convertDurationIntoMinutes(getDistinctPromptValue(completedResultLines, nextHearingResultDefinitions, getAllPromptUuidsByPromptReference(nextHearingResultDefinitions, HEST))));
        hearing.add(DEFENDANTS, defendants.build());

        return Json.createObjectBuilder()
                .add(CASE_ID, firstCase.getId().toString())
                .add(URN, firstCase.getProsecutionCaseIdentifier().getCaseURN())
                .add(REQUESTED_BY_HEARING_ID, resultsShared.getHearing().getId().toString())
                .add(HEARINGS, Json.createArrayBuilder()
                        .add(hearing.build())
                        .build())
                .build();
    }

    private String convertDate(final String date) {
        if (!StringUtils.isEmpty(date)) {
            final DateTimeFormatter from = DateTimeFormatter.ofPattern(DD_MM_YYYY);
            final DateTimeFormatter to = DateTimeFormatter.ofPattern(YYYY_MM_DD);
            return LocalDate.parse(date, from).format(to);
        }
        return EMPTY_STRING;
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
                    estimateMinutes[0] += Long.parseLong(CharMatcher.DIGIT.retainFrom(durationElement)) * MINUTE;
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
}

