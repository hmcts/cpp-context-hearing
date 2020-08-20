package uk.gov.moj.cpp.hearing.domain.aggregate;

import uk.gov.moj.cpp.platform.test.serializable.AggregateSerializableChecker;

import org.junit.Test;

public class AggregateSerializationTest {

    private AggregateSerializableChecker aggregateSerializableChecker = new AggregateSerializableChecker();

    @Test
    public void shouldCheckAggregatesAreSerializable() {
        final String packageName = "uk.gov.moj.cpp.hearing.domain.aggregate";

        aggregateSerializableChecker.checkAggregatesIn(packageName);
    }
}