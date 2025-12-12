package uk.gov.justice.progression.events;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static uk.gov.justice.progression.events.DeserializerTestHelper.testJsonSerializationRoundTrip;

import uk.gov.moj.cpp.hearing.domain.OffenceResult;
import uk.gov.moj.cpp.hearing.domain.event.DefendantOffenceResultsUpdated;

import java.util.Arrays;
import java.util.UUID;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

public class DefendantOffenceResultsUpdatedSerializationTest {

    @Test
    public void deserializeTest() throws Exception {

        final DefendantOffenceResultsUpdated event = DefendantOffenceResultsUpdated.newBuilder()
                .withDefendantId(randomUUID())
                .withOffenceIds(Arrays.asList(randomUUID(), randomUUID()))
                .withResultedOffences(ImmutableMap.<UUID, OffenceResult>builder().put(randomUUID(), OffenceResult.DISMISSED).put(randomUUID(), OffenceResult.GUILTY).build())
                .build();

        testJsonSerializationRoundTrip(event,
                allOf(
                        withJsonPath("$.defendantId", equalTo(event.getDefendantId().toString())),
                        withJsonPath("$.offenceIds", contains(event.getOffenceIds().stream().map(UUID::toString).toArray()))
                )
        );
    }

}
