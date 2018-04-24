package uk.gov.moj.cpp.hearing.steps.data.factory;

import static com.google.common.collect.ImmutableList.of;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toMap;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.utils.RandomPersonNameGenerator.personName;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import uk.gov.moj.cpp.hearing.steps.data.DefenceCounselData;

public class HearingDataFactory {

    public static DefenceCounselData defenceCounsel(final UUID defenceCounselId, final UUID defendantId) {
        return defenceCounsel(defenceCounselId, of(defendantId));
    }

    public static DefenceCounselData defenceCounsel(final UUID defenceCounselId, final List<UUID> defendantIds) {
        final Map<UUID, String> defendantIdToNames = defendantIds.stream().collect(toMap(item -> item, item -> personName()));
        return new DefenceCounselData(defenceCounselId, personName(), randomUUID(), defendantIdToNames, STRING.next());
    }
}
