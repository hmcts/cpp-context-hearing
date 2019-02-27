package uk.gov.moj.cpp.hearing.event.delegates;

import static java.util.stream.Collectors.toList;
import uk.gov.justice.core.courts.Now;
import uk.gov.justice.core.courts.NowVariant;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.ResultLineReference;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.Variant;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.VariantKey;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.VariantValue;
import uk.gov.moj.cpp.hearing.message.shareResults.VariantStatus;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public class Nows2VariantTransform {

    public List<Variant> toVariants(final UUID hearingId, final List<Now> nows, final ZonedDateTime sharedTime) {
        return nows.stream()
                .flatMap(now -> now.getRequestedMaterials().stream()
                        .map(m -> toVariant(hearingId, now, m, sharedTime))
                )
                .collect(toList());
    }

    public Variant toVariant(final UUID hearingId, final Now now, final NowVariant material, final ZonedDateTime sharedTime) {

        return Variant.variant()
                .setKey(VariantKey.variantKey()
                        .setNowsTypeId(now.getNowsTypeId())
                        .setDefendantId(now.getDefendantId())
                        .setHearingId(hearingId)
                        .setUsergroups(material.getKey().getUsergroups().stream().collect(toList()))
                )
                .setValue(VariantValue.variantValue()
                        .setMaterialId(material.getMaterialId())
                        .setStatus(VariantStatus.BUILDING)
                        .setResultLines(material.getNowResults()==null?null: material.getNowResults().stream()
                                .map(nr -> ResultLineReference.resultLineReference()
                                        .setLastSharedTime(sharedTime)
                                        .setResultLineId(nr.getSharedResultId())
                                )
                                .collect(toList())
                        )
                )
                .setReferenceDate(now.getReferenceDate());
    }
}
