package uk.gov.moj.cpp.hearing.event.delegates;

import org.junit.Test;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.Variant;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Material;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.NowResult;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Nows;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.UserGroups;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;

public class Nows2VariantTransformTest {

    @Test
    public void testMaterial2VariantTransform() {
        final UUID hearingId = UUID.randomUUID();
        ZonedDateTime sharedTime = PAST_ZONED_DATE_TIME.next().withZoneSameInstant(ZoneId.of("UTC"));
        final Nows nows = Nows.nows()
                .setNowsTypeId(UUID.randomUUID())
                .setDefendantId(UUID.randomUUID())
                .setMaterials(singletonList(
                        Material.material()
                                .setUserGroups(singletonList(UserGroups.userGroups().setGroup("ug1")))
                                .setNowResult(asList(
                                        NowResult.nowResult().setSharedResultId(UUID.randomUUID()),
                                        NowResult.nowResult().setSharedResultId(UUID.randomUUID())
                                ))
                ));

        final Variant variant = (new Nows2VariantTransform()).toVariant(hearingId, nows, nows.getMaterials().get(0), sharedTime);
        assertThat(variant.getKey().getHearingId(), is(hearingId));
        final Material material0 = nows.getMaterials().get(0);
        match(material0, variant, nows.getDefendantId(), nows.getNowsTypeId());
    }

    @Test
    public void testNowsListToVariantsTransform() {
        final UUID hearingId = UUID.randomUUID();
        ZonedDateTime sharedTime = PAST_ZONED_DATE_TIME.next().withZoneSameInstant(ZoneId.of("UTC"));
        final Nows nows0 = Nows.nows()
                .setNowsTypeId(UUID.randomUUID())
                .setDefendantId(UUID.randomUUID())
                .setMaterials(singletonList(
                        Material.material()
                                .setUserGroups(singletonList(UserGroups.userGroups().setGroup("ug1")))
                                .setNowResult(asList(
                                        NowResult.nowResult().setSharedResultId(UUID.randomUUID()),
                                        NowResult.nowResult().setSharedResultId(UUID.randomUUID())
                                ))
                ));
        final Nows nows1 = Nows.nows()
                .setNowsTypeId(UUID.randomUUID())
                .setDefendantId(UUID.randomUUID())
                .setMaterials(singletonList(
                        Material.material()
                                .setUserGroups(singletonList(UserGroups.userGroups().setGroup("ug1")))
                                .setNowResult(asList(
                                        NowResult.nowResult().setSharedResultId(UUID.randomUUID()),
                                        NowResult.nowResult().setSharedResultId(UUID.randomUUID())
                                ))
                ));

        final List<Variant> variants = (new Nows2VariantTransform()).toVariants(hearingId, asList(nows0, nows1), sharedTime);

        match(nows0.getMaterials().get(0), variants.get(0), nows0.getDefendantId(), nows0.getNowsTypeId());
        match(nows1.getMaterials().get(0), variants.get(1), nows1.getDefendantId(), nows1.getNowsTypeId());
    }

    private void match(Material material, Variant variant, UUID defendantId, UUID nowsTypeId) {
        assertThat(new HashSet<>(variant.getKey().getUsergroups()), is(material.getUserGroups().stream().map(ug -> ug.getGroup()).collect(Collectors.toSet())));
        assertThat(variant.getKey().getDefendantId(), is(defendantId));
        assertThat(variant.getKey().getNowsTypeId(), is(nowsTypeId));
        assertThat(variant.getValue().getMaterialId(), is(material.getId()));
        for (NowResult nowResult : material.getNowResult()) {
            assertThat(variant.getValue().getResultLines().stream().filter(rl -> rl.getResultLineId().equals(nowResult.getSharedResultId())).count(), is(1l));
        }
    }
}