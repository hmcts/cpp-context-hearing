package uk.gov.moj.cpp.hearing.event.delegates.helper;


import static java.util.Collections.singletonList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static uk.gov.moj.cpp.hearing.event.NowsTemplates.resultsSharedTemplate;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.NowDefinitionTemplates.standardNowDefinition;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;

import uk.gov.justice.core.courts.Category;
import uk.gov.justice.core.courts.DefenceCounsel;
import uk.gov.justice.core.courts.DefendantAttendance;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.JudicialResult;
import uk.gov.justice.core.courts.Prompt;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.event.helper.ResultsSharedHelper;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.NowDefinition;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

public class ResultSharedHelperTest {

    private ResultsSharedHelper resultsSharedHelper = new ResultsSharedHelper();

    @Test
    public void when_JudicialResult_With_Category_Final_Then_Offence_IsDisposed_isFalse() {

        final NowDefinition nowDefinition = standardNowDefinition();

        final uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt promptReferenceData = getPromptReferenceData();

        final Prompt prompt0 = getPrompt(promptReferenceData);

        final CommandHelpers.ResultsSharedEventHelper resultsShared = getResultsShared();

        final ResultsShared expected = resultsShared.it();
        setJudicialResultsWithCategoryOf(expected,Category.FINAL);

        resultsSharedHelper.setIsDisposedFlagOnOffence(expected);

        assertTrue( getIsDisposedValueForOffence(expected.getHearing())) ;

    }


    @Test
    public void when_JudicialResult_With_Category_Intermediary_Then_Offence_IsDisposed_isFalse() {

        final NowDefinition nowDefinition = standardNowDefinition();

        final uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt promptReferenceData = getPromptReferenceData();

        final Prompt prompt0 = getPrompt(promptReferenceData);

        final CommandHelpers.ResultsSharedEventHelper resultsShared = h(with(resultsSharedTemplate(), r -> {
            r.getVariantDirectory().get(0).getKey().setNowsTypeId(nowDefinition.getId());
            r.getTargets().get(0).getResultLines().get(0).setPrompts(singletonList(prompt0));
            r.getHearing().setDefenceCounsels(
                    singletonList(DefenceCounsel.defenceCounsel().withId(UUID.randomUUID()).build()));
            r.getHearing().setDefendantAttendance(
                    singletonList(DefendantAttendance.defendantAttendance().withDefendantId(UUID.randomUUID()).build()));
        }));

        final ResultsShared expected = resultsShared.it();
        setJudicialResultsWithCategoryOf(expected,Category.INTERMEDIARY);

        resultsSharedHelper.setIsDisposedFlagOnOffence(expected);

        assertFalse( getIsDisposedValueForOffence(expected.getHearing())); ;

    }

    private Prompt getPrompt(final uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt promptReferenceData) {
        return Prompt.prompt()
                    .withLabel(promptReferenceData.getLabel())
                    .withValue("promptValue0")
                    .withId(promptReferenceData.getId())
                    .withFixedListCode("fixedListCode0")
                    .build();
    }

    private void setJudicialResultsWithCategoryOf(final ResultsShared expected , Category category) {
        final List<JudicialResult> judicialResultList   = new ArrayList<>();
        judicialResultList.add(JudicialResult.judicialResult().withCategory(Category.INTERMEDIARY).withCjsCode("cjsCode1").build());
        judicialResultList.add(JudicialResult.judicialResult().withCategory(category).withCjsCode("cjsCode2").build());
        expected.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).setJudicialResults(judicialResultList);
    }

    private Boolean getIsDisposedValueForOffence(final Hearing hearingIn) {
        return hearingIn.getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getIsDisposed();
    }

    private CommandHelpers.ResultsSharedEventHelper getResultsShared() {
        return h(with(resultsSharedTemplate(), r -> {
            r.getHearing().setDefenceCounsels(singletonList(DefenceCounsel.defenceCounsel().withId(UUID.randomUUID()).build()));
            r.getHearing().setDefendantAttendance(singletonList(DefendantAttendance.defendantAttendance().withDefendantId(UUID.randomUUID()).build()));

        }));
    }


    private uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt getPromptReferenceData() {
        return uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt.prompt()
                .setId(UUID.randomUUID())
                .setQual("promptQualifier")
                .setLabel("promptReferenceData0")
                .setUserGroups(Arrays.asList("usergroup0", "usergroup1"));
    }
}
