package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.VariantDirectoryTemplates.standardVariantTemplate;

import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.Variant;
import uk.gov.moj.cpp.hearing.nows.events.NowsMaterialStatusUpdated;

import java.util.List;
import java.util.UUID;

import org.junit.Test;

public class VariantDirectoryDelegateTest {
    @Test
    public void shouldHandleNowsMaterialStatusUpdatedEvent(){
        final UUID nowTypeId = randomUUID();
        final UUID defendantId = randomUUID();
        final UUID hearingId = randomUUID();
        final UUID materialId = UUID.randomUUID();
        final HearingAggregateMomento momento = new HearingAggregateMomento();
        final List<Variant> variantDirectory = singletonList(
                standardVariantTemplate(nowTypeId, hearingId, defendantId));
        momento.setVariantDirectory(variantDirectory);
        final VariantDirectoryDelegate variantDirectoryDelegate = new VariantDirectoryDelegate(momento);
        variantDirectoryDelegate.handleNowsMaterialStatusUpdatedEvent(new NowsMaterialStatusUpdated(
                hearingId,
                materialId,
                "status"
        ));
    }
}
