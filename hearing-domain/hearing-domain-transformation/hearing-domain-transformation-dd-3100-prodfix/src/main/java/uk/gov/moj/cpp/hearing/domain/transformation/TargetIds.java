package uk.gov.moj.cpp.hearing.domain.transformation;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

public class TargetIds {

    public static final Map<String, String> TARGET_IDS_TO_REPLACE = ImmutableMap.of(
            // 1st hearing - 0b61b3aa-313f-42b8-9da1-cdb227619bb5
            // for offence ID a42c5003-3cc6-4745-bf23-d1a9dece8fee
            "6d1626d0-f346-11ea-b926-6d1b3a9e2c53", "86673a60-f365-11ea-a197-9fcf984228f3",
            "424d2100-f347-11ea-9539-b10ccda134b5", "86673a60-f365-11ea-a197-9fcf984228f3",

            // for offence ID 963f29aa-06a6-4679-a1b5-dc3bdc24cabd
             "6d1626d1-f346-11ea-b926-6d1b3a9e2c53", "424d2101-f347-11ea-9539-b10ccda134b5 ",

            // 2nd hearing - dcc92c84-cab5-41da-bb9f-9eecbf228879
            // for offence ID 1725d427-a994-41d6-87c1-48690bf724a9
            "3d5fc7a0-f34d-11ea-a325-a9248e03a227", "5f8ae170-f34d-11ea-9539-b10ccda134b5");

    private TargetIds() {
    }

}
