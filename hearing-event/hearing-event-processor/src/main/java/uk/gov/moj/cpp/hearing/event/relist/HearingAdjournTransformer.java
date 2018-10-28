package uk.gov.moj.cpp.hearing.event.relist;

import static uk.gov.moj.cpp.hearing.event.relist.HearingAdjournHelper.getAllPromptUuidsByPromptReference;
import static uk.gov.moj.cpp.hearing.event.relist.HearingAdjournHelper.getDistinctPromptValue;
import static uk.gov.moj.cpp.hearing.event.relist.HearingAdjournHelper.getOffencesHaveResultNextHearing;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference.HEST;
import static uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference.HTYPE;

import com.google.common.base.CharMatcher;
import uk.gov.justice.json.schemas.core.Offence;
import uk.gov.justice.json.schemas.core.ResultLine;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.event.relist.metadata.DurationElements;
import uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingResultDefinition;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("squid:S1188")
public class HearingAdjournTransformer {

    private static final int FIVE = 5;
    private static final int SIX = 6;
    private static final int MINUTE = 60;
    private static final int DAY = SIX * MINUTE;
    private static final String COMMA_REGEX = "\\s*,\\s*";
    private static final String ID = "id";
    private static final String OFFENCES = "offences";
    private static final String TYPE = "type";
    private static final String DEFENDANTS = "defendants";
    private static final String ADJOURNED_HEARING = "adjournedHearing";
    private static final String NEXT_HEARINGS = "nextHearings";
    private static final String JURISDICTION_TYPE = "jurisdictionType";
    private static final String REPORTING_RESTRICTION_REASON = "reportingRestrictionReason";
    private static final String HEARING_LANGUAGE = "hearingLanguage";
    private static final String ESTIMATED_MINUTES = "estimatedMinutes";
    private static final String COURT_CENTRE = "courtCentre";
    private static final String ROOM_ID = "roomId";
    private static final String JUDICIARY = "judiciary";
    private static final String PROSECUTION_CASES = "prosecutionCases";

    public JsonObject transform(final ResultsShared resultsShared, final Map<UUID, NextHearingResultDefinition> nextHearingResultDefinitions) {
        final List<ResultLine> completedResultLines = getCompletedResultLines(resultsShared);

        final JsonArrayBuilder prosecutionCases = Json.createArrayBuilder();
        final JsonArrayBuilder defendants = Json.createArrayBuilder();
        resultsShared.getHearing().getProsecutionCases().stream().forEach(
                pc -> {
                    pc.getDefendants().stream().forEach(
                            defendant -> {
                                final List<Offence> offences = getOffencesHaveResultNextHearing(defendant, resultsShared.getHearing().getTargets(), completedResultLines, nextHearingResultDefinitions);
                                if (!offences.isEmpty()) {
                                    final JsonArrayBuilder jsonOffences = Json.createArrayBuilder();
                                    offences.forEach(
                                            offence -> {
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
                    final JsonObject prosecutionCaseJson = Json.createObjectBuilder()
                            .add(ID, pc.getId().toString())
                            .add(DEFENDANTS, defendants.build())
                            .build();
                    prosecutionCases.add(prosecutionCaseJson);
                });

        final JsonObjectBuilder hearing = Json.createObjectBuilder();

        getFirst(getDistinctPromptValue(completedResultLines, nextHearingResultDefinitions, getAllPromptUuidsByPromptReference(nextHearingResultDefinitions, HTYPE)))
                .ifPresent(value -> hearing.add(TYPE, Json.createObjectBuilder()
                        .add(ID, resultsShared.getHearing().getType().getId().toString())
                        .add("description", value).build()));

        hearing.add(JURISDICTION_TYPE, resultsShared.getHearing().getJurisdictionType().toString());
        hearing.add(REPORTING_RESTRICTION_REASON, resultsShared.getHearing().getReportingRestrictionReason());
        hearing.add(HEARING_LANGUAGE, resultsShared.getHearing().getHearingLanguage().toString());
        hearing.add(ESTIMATED_MINUTES, convertDurationIntoMinutes(getDistinctPromptValue(completedResultLines, nextHearingResultDefinitions, getAllPromptUuidsByPromptReference(nextHearingResultDefinitions, HEST))));

        final JsonObject courtCentreJson = Json.createObjectBuilder()
                .add(ID, resultsShared.getHearing().getCourtCentre().getId().toString())
                .add(ROOM_ID, resultsShared.getHearing().getCourtCentre().getRoomId().toString())
                .build();
        hearing.add(COURT_CENTRE, courtCentreJson);

        final JsonArrayBuilder judiciaries = Json.createArrayBuilder();
        resultsShared.getHearing().getJudiciary().forEach(judicialRole -> {
            final JsonObject judiciaryRoleJson = Json.createObjectBuilder()
                    .add("judicialId", judicialRole.getJudicialId().toString())
                    .add("judicialRoleType", judicialRole.getJudicialRoleType().toString())
                    .build();
            judiciaries.add(judiciaryRoleJson);
        });
        hearing.add(JUDICIARY, judiciaries.build());
        hearing.add(PROSECUTION_CASES, prosecutionCases.build());

        return Json.createObjectBuilder()
                .add(ADJOURNED_HEARING, resultsShared.getHearing().getId().toString())
                .add(NEXT_HEARINGS, Json.createArrayBuilder()
                        .add(hearing.build())
                        .build())
                .build();
    }

    private List<ResultLine> getCompletedResultLines(final ResultsShared resultsShared) {
        return resultsShared.getHearing().getTargets().stream().flatMap(target -> target.getResultLines().stream()).collect(Collectors.toList());
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

