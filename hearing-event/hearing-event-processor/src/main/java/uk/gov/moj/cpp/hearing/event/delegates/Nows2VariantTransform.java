package uk.gov.moj.cpp.hearing.event.delegates;

import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.ResultLineReference;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.Variant;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.VariantKey;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.VariantValue;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Material;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Nows;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.UserGroups;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.toList;

public class Nows2VariantTransform {

    public List<Variant> toVariants(final UUID hearingId, List<Nows> nows, ZonedDateTime sharedTime) {
        return nows.stream()
                .flatMap(now -> now.getMaterials().stream()
                        .map(m -> toVariant(hearingId, now, m, sharedTime))
                )
                .collect(toList());
    }

    public Variant toVariant(final UUID hearingId, final Nows nows, final Material material, final ZonedDateTime sharedTime) {

        return Variant.variant()
                .setKey(VariantKey.variantKey()
                        .setNowsTypeId(nows.getNowsTypeId())
                        .setDefendantId(nows.getDefendantId())
                        .setHearingId(hearingId)
                        .setUsergroups(material.getUserGroups().stream().map(UserGroups::getGroup).collect(toList()))
                )
                .setValue(VariantValue.variantValue()
                        .setMaterialId(material.getId())
                        .setResultLines(material.getNowResult().stream()
                                .map(nr -> ResultLineReference.resultLineReference()
                                        .setLastSharedTime(sharedTime)
                                        .setResultLineId(nr.getSharedResultId())
                                )
                                .collect(toList())
                        )
                );
    }
}
