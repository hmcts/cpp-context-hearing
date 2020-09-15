package uk.gov.moj.cpp.hearing.domain.transformation;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

public class TestTargetIds {

    private TestTargetIds() {
    }

    public static final Map<String, String> TEST_TARGET_IDS_TO_REPLACE = ImmutableMap.of(
            "6d1626d0-f346-11ea-b926-6d1b3a9e2c53", "86673a60-f365-11ea-a197-9fcf984228f3",
            "424d2100-f347-11ea-9539-b10ccda134b5", "86673a60-f365-11ea-a197-9fcf984228f3",

            // for offence ID 963f29aa-06a6-4679-a1b5-dc3bdc24cabd
            // "424d2100-f347-11ea-9539-b10ccda134b5", "86673a60-f365-11ea-a197-9fcf984228f3", // not sure what was deleted - needs clarification from AMS

            // 2nd hearing - dcc92c84-cab5-41da-bb9f-9eecbf228879
            // for offence ID 1725d427-a994-41d6-87c1-48690bf724a9
            "3d5fc7a0-f34d-11ea-a325-a9248e03a227", "5f8ae170-f34d-11ea-9539-b10ccda134b5");
}
