package uk.gov.moj.cpp.hearing.command.nows;

import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.ResultLineReference;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.SaveNowsVariantsCommand;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.Variant;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.VariantKey;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.VariantValue;
import uk.gov.moj.cpp.hearing.message.shareResults.VariantStatus;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

public class NowVariantUtil {

    public SaveNowsVariantsCommand createSampleNowsVariantsCommand() {
        final UUID hearingId=UUID.randomUUID();
        final UUID defendantId = UUID.randomUUID();
        final UUID nowsTypeId = UUID.randomUUID();
        return SaveNowsVariantsCommand.saveNowsVariantsCommand()
                .setHearingId(hearingId)
                .setVariants(
                        Arrays.asList(
                                Variant.variant()
                                        .setKey(
                                                VariantKey.variantKey()
                                                        .setHearingId(hearingId)
                                                        .setDefendantId(defendantId)
                                                        .setNowsTypeId(nowsTypeId)
                                                        .setUsergroups(Arrays.asList("Listings Officers", "Court Clerks"))
                                        )
                                        .setValue(
                                                VariantValue.variantValue()
                                                .setMaterialId(UUID.randomUUID())
                                                .setStatus(VariantStatus.BUILDING)
                                                .setResultLines(
                                                        Arrays.asList(
                                                                ResultLineReference.resultLineReference()
                                                                .setLastSharedTime(ZonedDateTime.now())
                                                                .setResultLineId(UUID.randomUUID()),
                                                                ResultLineReference.resultLineReference()
                                                                        .setLastSharedTime(ZonedDateTime.now())
                                                                        .setResultLineId(UUID.randomUUID())
                                                        )
                                                )
                                        ),
                                Variant.variant()
                                        .setKey(
                                                VariantKey.variantKey()
                                                        .setHearingId(hearingId)
                                                        .setDefendantId(defendantId)
                                                        .setNowsTypeId(nowsTypeId)
                                                        .setUsergroups(Arrays.asList("System Users"))
                                        )
                                        .setValue(
                                                VariantValue.variantValue()
                                                        .setMaterialId(UUID.randomUUID())
                                                        .setStatus(VariantStatus.BUILDING)
                                                        .setResultLines(
                                                                Arrays.asList(
                                                                        ResultLineReference.resultLineReference()
                                                                                .setLastSharedTime(null)
                                                                                .setResultLineId(UUID.randomUUID()),
                                                                        ResultLineReference.resultLineReference()
                                                                                .setLastSharedTime(ZonedDateTime.now())
                                                                                .setResultLineId(UUID.randomUUID())
                                                                )
                                                        )
                                        )

                        )
                );
    }

    private static Set<String> userGroupsAsSet(VariantKey variantKey) {
        final Set<String> ugset = new HashSet<>();
        if (variantKey.getUsergroups()!=null) {
            ugset.addAll(variantKey.getUsergroups());
        }
        return ugset;
    }


    private static <T> boolean areEqual(T o1, T o2, List<Function<T, Object>> properties) {
        return properties.stream().noneMatch(f -> {
            final Object val1 = f.apply(o1);
            final Object val2 = f.apply(o2);
            return val1 != null && !val1.equals(val2) || val2 != null && !val2.equals(val1);
        });
    }

    public static boolean areEqual(VariantKey key1, VariantKey key2) {
        final List<Function<VariantKey, Object>> properties = Arrays.asList(
                VariantKey::getHearingId, VariantKey::getDefendantId, VariantKey::getNowsTypeId, NowVariantUtil::userGroupsAsSet
        );
        return areEqual(key1, key2, properties);
    }

}
