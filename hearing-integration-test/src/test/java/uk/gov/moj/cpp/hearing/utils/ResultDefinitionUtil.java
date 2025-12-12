package uk.gov.moj.cpp.hearing.utils;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

import java.util.List;
import java.util.UUID;

public class ResultDefinitionUtil {

    private static final String CATEGORY_ANCILLARY = "A";
    private static final String CATEGORY_FINAL = "F";

    private static final List<String> dismissedResultList = unmodifiableList(
            asList(
                    "14d66587-8fbe-424f-a369-b1144f1684e3",
                    "f8bd4d1f-1467-4903-b1e6-d2249ccc8c25",
                    "8542b0d9-27f0-4df3-a4a3-0ac0a85c33ad",
                    "969f150c-cd05-46b0-9dd9-30891efcc766"));
    private static final List<String> withDrawnResultList = unmodifiableList(
            asList(
                    "6feb0f2e-8d1e-40c7-af2c-05b28c69e5fc",
                    "eb2e4c4f-b738-4a4d-9cce-0572cecb7cb8",
                    "c8326b9e-56eb-406c-b74b-9f90c772b657",
                    "eaecff82-32da-4cc1-b530-b55195485cc7",
                    "4d5f25a5-9102-472f-a2da-c58d1eeb9c93"));
    private static final List<String> guiltyResultList = unmodifiableList(
            asList(
                    "fc612b8f-9699-459f-9ea7-b307164e4754",
                    "ce23a452-9015-4619-968f-1628d7a271c9"));

    public static String getCategoryForResultDefinition(final UUID resultDefId) {
        if (dismissedResultList.contains(resultDefId.toString()) ||
                withDrawnResultList.contains(resultDefId.toString()) ||
                guiltyResultList.contains(resultDefId.toString())) {
            return CATEGORY_FINAL;
        }
        return CATEGORY_ANCILLARY;
    }
}
