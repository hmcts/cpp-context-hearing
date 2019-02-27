package uk.gov.moj.cpp.hearing.event.delegates;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;

import uk.gov.justice.core.courts.Now;
import uk.gov.justice.core.courts.NowVariant;
import uk.gov.justice.core.courts.NowVariantKey;
import uk.gov.justice.core.courts.NowVariantResult;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.Variant;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.Test;

public class Nows2VariantTransformTest {

    @Test
    public void testMaterial2VariantTransform() {
        final UUID hearingId = UUID.randomUUID();
        ZonedDateTime sharedTime = PAST_ZONED_DATE_TIME.next().withZoneSameInstant(ZoneId.of("UTC"));
        final UUID defendantId = UUID.randomUUID();
        final Now nows = Now.now()
                .withNowsTypeId(UUID.randomUUID())
                .withDefendantId(UUID.randomUUID())
                .withRequestedMaterials(singletonList(
                        NowVariant.nowVariant()
                                .withKey(
                                        NowVariantKey.nowVariantKey()
                                                .withDefendantId(defendantId)
                                                .withUsergroups(singletonList("ug1"))
                                                .build()
                                )
                                .withMaterialId(UUID.randomUUID())
                                .withDescription("dfgdgdfg")
                                .withNowResults(asList(
                                        NowVariantResult.nowVariantResult().withSharedResultId(UUID.randomUUID()).build(),
                                        NowVariantResult.nowVariantResult().withSharedResultId(UUID.randomUUID()).build()
                                )).build()
                )).build();

        final Variant variant = (new Nows2VariantTransform()).toVariant(hearingId, nows, nows.getRequestedMaterials().get(0), sharedTime);
        assertThat(variant.getKey().getHearingId(), is(hearingId));
        final NowVariant material0 = nows.getRequestedMaterials().get(0);
        match(material0, variant, nows.getDefendantId(), nows.getNowsTypeId());
    }

    @Test
    public void testMaterial2VariantTransformNoResults() {
        final UUID hearingId = UUID.randomUUID();
        ZonedDateTime sharedTime = PAST_ZONED_DATE_TIME.next().withZoneSameInstant(ZoneId.of("UTC"));
        final UUID defendantId = UUID.randomUUID();
        final Now nows = Now.now()
                .withNowsTypeId(UUID.randomUUID())
                .withDefendantId(UUID.randomUUID())
                .withRequestedMaterials(singletonList(
                        NowVariant.nowVariant()
                                .withKey(
                                        NowVariantKey.nowVariantKey()
                                                .withDefendantId(defendantId)
                                                .withUsergroups(singletonList("ug1"))
                                                .build()
                                )
                                .withMaterialId(UUID.randomUUID())
                                .withDescription("dfgdgdfg")
                                .withNowResults(null).build()
                )).build();

        final Variant variant = (new Nows2VariantTransform()).toVariant(hearingId, nows, nows.getRequestedMaterials().get(0), sharedTime);
        assertThat(variant.getKey().getHearingId(), is(hearingId));
        final NowVariant material0 = nows.getRequestedMaterials().get(0);
        match(material0, variant, nows.getDefendantId(), nows.getNowsTypeId());
    }


    private Now newNow() {
        return Now.now()
                .withNowsTypeId(UUID.randomUUID())
                .withDefendantId(UUID.randomUUID())
                .withRequestedMaterials(singletonList(
                        NowVariant.nowVariant()
                                .withKey(NowVariantKey.nowVariantKey()
                                        .withUsergroups(singletonList("ug1"))
                                        .build())
                                .withNowResults(asList(
                                        NowVariantResult.nowVariantResult().withSharedResultId(UUID.randomUUID()).build(),
                                        NowVariantResult.nowVariantResult().withSharedResultId(UUID.randomUUID()).build()
                                )).build()
                )).build();
    }

    @Test
    public void testNowsListToVariantsTransform() {
        final UUID hearingId = UUID.randomUUID();
        ZonedDateTime sharedTime = PAST_ZONED_DATE_TIME.next().withZoneSameInstant(ZoneId.of("UTC"));
        final Now nows0 = newNow();
        final Now nows1 = newNow();

        final List<Variant> variants = (new Nows2VariantTransform()).toVariants(hearingId, asList(nows0, nows1), sharedTime);

        match(nows0.getRequestedMaterials().get(0), variants.get(0), nows0.getDefendantId(), nows0.getNowsTypeId());
        match(nows1.getRequestedMaterials().get(0), variants.get(1), nows1.getDefendantId(), nows1.getNowsTypeId());
    }

    private void match(NowVariant material, Variant variant, UUID defendantId, UUID nowsTypeId) {
        assertThat(new HashSet<>(variant.getKey().getUsergroups()), is(material.getKey().getUsergroups().stream().collect(Collectors.toSet())));
        assertThat(variant.getKey().getDefendantId(), is(defendantId));
        assertThat(variant.getKey().getNowsTypeId(), is(nowsTypeId));
        assertThat(variant.getValue().getMaterialId(), is(material.getMaterialId()));
        if (material.getNowResults() != null) {
            for (NowVariantResult nowResult : material.getNowResults()) {
                assertThat(variant.getValue().getResultLines().stream().filter(rl -> rl.getResultLineId().equals(nowResult.getSharedResultId())).count(), is(1l));
            }
        }
    }
}