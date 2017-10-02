package uk.gov.moj.cpp.hearing.steps.data.factory;

import static com.google.common.collect.ImmutableList.of;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.IntStream.range;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.integer;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.randomEnum;
import static uk.gov.moj.cpp.hearing.utils.RandomPersonNameGenerator.personName;

import uk.gov.moj.cpp.hearing.domain.ResultPrompt;
import uk.gov.moj.cpp.hearing.steps.data.DefenceCounselData;
import uk.gov.moj.cpp.hearing.steps.data.ResultLevel;
import uk.gov.moj.cpp.hearing.steps.data.ResultLineData;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HearingDataFactory {

    public static DefenceCounselData defenceCounsel(final UUID defenceCounselId, final UUID defendantId) {
        return defenceCounsel(defenceCounselId, of(defendantId));
    }

    public static DefenceCounselData defenceCounsel(final UUID defenceCounselId, final List<UUID> defendantIds) {
        final Map<UUID, String> defendantIdToNames = defendantIds.stream().collect(toMap(item -> item, item -> personName()));
        return new DefenceCounselData(defenceCounselId, personName(), randomUUID(), defendantIdToNames, STRING.next());
    }

    public static ResultLineData resultLine(final ResultLevel level) {
        return new ResultLineData(randomUUID(), null, randomUUID(), randomUUID(),
                randomUUID(), level, STRING.next(),
                range(0, integer(5).next()).mapToObj(index -> resultPrompt()).collect(toList()),
                STRING.next(), STRING.next(), randomUUID(), STRING.next(), STRING.next());
    }

    public static ResultLineData amendedResultLine(final ResultLineData pastSharedResult) {
        return new ResultLineData(randomUUID(), pastSharedResult.getId(), pastSharedResult.getCaseId(),
                pastSharedResult.getPersonId(), pastSharedResult.getOffenceId(),
                randomEnum(ResultLevel.class).next(), STRING.next(),
                range(0, integer(5).next()).mapToObj(index -> resultPrompt()).collect(toList()),
                STRING.next(), STRING.next(), randomUUID(), STRING.next(), STRING.next());
    }

    public static ResultLineData sharedResultLine(final ResultLineData resultLine) {
        return new ResultLineData(resultLine.getId(), resultLine.getId(), resultLine.getCaseId(),
                resultLine.getPersonId(), resultLine.getOffenceId(), resultLine.getLevel(),
                resultLine.getResultLabel(), resultLine.getPrompts(),
                STRING.next(), STRING.next(), randomUUID(), STRING.next(), STRING.next());
    }

    private static ResultPrompt resultPrompt() {
        return new ResultPrompt(STRING.next(), STRING.next());
    }
}
