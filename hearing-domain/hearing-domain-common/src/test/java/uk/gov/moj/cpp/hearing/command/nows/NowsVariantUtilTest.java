package uk.gov.moj.cpp.hearing.command.nows;

import org.junit.Test;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.VariantKey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;

public class NowsVariantUtilTest {

@Test
public void  testAreEqual() {
    VariantKey key = createDefaultVariantKey();
    assertThat("baseline check - clones are different instances", clone(key)!=clone(key));
    assertThat("same values equals", NowVariantUtil.areEqual(clone(key), clone(key)));
    assertThat("same values with a null equals", NowVariantUtil.areEqual(clone(key).setDefendantId(null), clone(key).setDefendantId(null)));
    assertThat("defendantId breaks equals",false== NowVariantUtil.areEqual(clone(key), clone(key).setDefendantId(UUID.randomUUID())));
    assertThat("hearingId breaks equals", false== NowVariantUtil.areEqual(clone(key), clone(key).setHearingId(UUID.randomUUID())));
    assertThat("nowsTypeId breaks equals",false== NowVariantUtil.areEqual(clone(key), clone(key).setNowsTypeId(UUID.randomUUID())));
    assertThat("usergroups breaks equals",false== NowVariantUtil.areEqual(clone(key), clone(key).setUsergroups(Arrays.asList("agroup"))));
}

private VariantKey createDefaultVariantKey() {
    return VariantKey.variantKey()
            .setHearingId(UUID.randomUUID())
            .setNowsTypeId(UUID.randomUUID())
            .setDefendantId(UUID.randomUUID())
            .setUsergroups(Arrays.asList("Listings Officers", "Court Clerks"));
}

private VariantKey clone(VariantKey in) {
   return VariantKey.variantKey()
           .setHearingId(in.getHearingId())
           .setNowsTypeId(in.getNowsTypeId())
           .setDefendantId(in.getDefendantId())
           .setUsergroups(new ArrayList<>(in.getUsergroups()));
}

}
