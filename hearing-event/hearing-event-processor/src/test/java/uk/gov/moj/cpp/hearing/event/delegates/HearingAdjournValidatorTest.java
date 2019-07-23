package uk.gov.moj.cpp.hearing.event.delegates;

import uk.gov.justice.core.courts.Target;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.event.relist.HearingAdjournValidator;
import uk.gov.moj.cpp.hearing.event.relist.RelistTestHelper;
import uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingResultDefinition;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HearingAdjournValidatorTest {

    @InjectMocks
    HearingAdjournValidator testObj;

    @Test
    public void validateProsecutionCaseOnly() {
        validate(resultsShared->{}, false, true);
    }

    @Test
    public void validateApplicationOnly() {
        validate(resultsShared -> {
                    final Target target = resultsShared.getTargets().get(0);
                    target.setOffenceId(null);
                    target.setApplicationId(resultsShared.getHearing().getCourtApplications().get(0).getId());
                }, true, false
        );
    }

    private void validate(final Consumer<ResultsShared> eventModifier, final boolean expectedApplicationResult, final boolean expectedProsecutionCaseResult) {
        final ResultsShared resultsShared = RelistTestHelper.getArbitrarySharedResultWithNextHearingResult();
        eventModifier.accept(resultsShared);
        final List<UUID> withdrawnResultDefinitionUuid = Arrays.asList();
        final Map<UUID, NextHearingResultDefinition> nextHearingResultDefinitions = RelistTestHelper.arbitraryNextHearingMetaData();
        //ResultLine resultLine0 = target.getResultLines().get(0);
        //nextHearingResultDefinitions.put(resultLine0.getResultDefinitionId(), RelistTestHelper.arbitraryNextHearingMetaData().get(resultLine0.getResultDefinitionId()));
        boolean caseResult = testObj.validateProsecutionCase(resultsShared, withdrawnResultDefinitionUuid, nextHearingResultDefinitions);
        Assert.assertEquals(expectedProsecutionCaseResult, caseResult);

        boolean applicationResult = testObj.validateApplication(resultsShared, nextHearingResultDefinitions);
        Assert.assertEquals(expectedApplicationResult, applicationResult);
    }


}
