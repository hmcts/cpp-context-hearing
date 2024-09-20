package uk.gov.moj.cpp.hearing.domain.aggregate;

import uk.gov.moj.cpp.platform.test.serializable.AggregateSerializableChecker;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class AggregateSerializationTest {

    private AggregateSerializableChecker aggregateSerializableChecker = new AggregateSerializableChecker();

    @Disabled //TODO: I have created PEG-675 for this issue.
    @Test
    public void shouldCheckAggregatesAreSerializable() {
        final String packageName = "uk.gov.moj.cpp.hearing.domain.aggregate";

        aggregateSerializableChecker.checkAggregatesIn(packageName);
    }
}