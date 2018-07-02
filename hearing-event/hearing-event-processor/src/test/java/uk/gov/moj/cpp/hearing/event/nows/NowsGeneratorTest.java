package uk.gov.moj.cpp.hearing.event.nows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.ResultLineReference;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.Variant;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.VariantKey;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.VariantValue;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLineStatus;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Material;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.NowResult;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Nows;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.PromptRef;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.UserGroups;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.NowDefinition;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.ResultDefinitions;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;
import uk.gov.moj.cpp.hearing.event.service.ReferenceDataService;

import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.FUTURE_ZONED_DATE_TIME;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.CompletedResultLineStatusTemplates.completedResultLineStatus;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.caseTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.defendantTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.hearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.ShareResultsCommandTemplates.completedResultLineTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.ShareResultsCommandTemplates.uncompletedResultLineTemplate;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;

@RunWith(MockitoJUnitRunner.class)
public class NowsGeneratorTest {

    @Mock
    private ReferenceDataService referenceDataService;

    @InjectMocks
    private NowsGenerator target;

    private final ResultDefinition resultDefinition = ResultDefinition.resultDefinition()
            .setId(randomUUID())
            .setPrompts(singletonList(Prompt.prompt()
                    .setId(randomUUID())
                    .setLabel("Lock him up")
                    .setUserGroups(singletonList("Court Clerk"))
            ));

    private final NowDefinition nowDefinition = NowDefinition.now()
            .setId(UUID.randomUUID())
            .setResultDefinitions(singletonList(
                    ResultDefinitions.resultDefinitions()
                            .setId(resultDefinition.getId())
                            .setMandatory(true)
            ));

    @Test
    public void createNows_generateSingleNowForSingleDefendant() {

        UUID caseId = randomUUID();
        UUID defendantId = randomUUID();
        UUID offenceId = randomUUID();
        UUID resultLineId = randomUUID();

        final ResultsShared resultsShared = ResultsShared.builder()
                .withCases(singletonList(caseTemplate(caseId)))
                .withHearing(hearingTemplate().setDefendants(singletonList(defendantTemplate(caseId, defendantId, offenceId))))
                .withCompletedResultLines(singletonList(completedResultLineTemplate(defendantId, offenceId, caseId, resultLineId, resultDefinition.getId())))
                .withUncompletedResultLines(emptyList())
                .withCompletedResultLinesStatus(singletonMap(resultLineId, completedResultLineStatus(resultLineId)))
                .build();

        when(referenceDataService.getNowDefinitionByPrimaryResultDefinitionId(any(), eq(resultDefinition.getId()))).thenReturn(nowDefinition);

        when(referenceDataService.getResultDefinitionById(any(), eq(resultDefinition.getId()))).thenReturn(resultDefinition);

        List<Nows> nows = target.createNows(resultsShared);

        assertThat(nows.size(), is(1));

        Nows now = nows.get(0);
        assertThat(now.getDefendantId(), is(defendantId));
        assertThat(now.getNowsTypeId(), is(nowDefinition.getId()));
        assertThat(now.getMaterials().size(), is(1));

        Material material = now.getMaterials().get(0);
        assertThat(material.getId(), is(not(nullValue())));
        assertThat(material.getUserGroups().stream().map(UserGroups::getGroup).collect(toList()), hasItems("Court Clerk"));
        assertThat(material.isAmended(), is(false));
        assertThat(material.getNowResult().size(), is(1));

        NowResult nowResult = material.getNowResult().get(0);
        assertThat(nowResult.getSharedResultId(), is(resultLineId));
        assertThat(nowResult.getPrompts().size(), is(1));

        PromptRef promptRefs = nowResult.getPrompts().get(0);
        assertThat(promptRefs.getLabel(), is("Lock him up"));
    }

    @Test
    public void createNows_generateANowForEachDefendant() {

        UUID caseId = randomUUID();
        UUID defendantId_1 = randomUUID();
        UUID defendantId_2 = randomUUID();
        UUID offenceId_1 = randomUUID();
        UUID offenceId_2 = randomUUID();
        UUID resultLineId_1 = randomUUID();
        UUID resultLineId_2 = randomUUID();

        final ResultsShared resultsShared = ResultsShared.builder()
                .withCases(singletonList(caseTemplate(caseId)))
                .withHearing(hearingTemplate().setDefendants(asList(
                        defendantTemplate(caseId, defendantId_1, offenceId_1),
                        defendantTemplate(caseId, defendantId_2, offenceId_2)
                )))
                .withCompletedResultLines(asList(
                        completedResultLineTemplate(defendantId_1, offenceId_1, caseId, resultLineId_1, resultDefinition.getId()),
                        completedResultLineTemplate(defendantId_2, offenceId_2, caseId, resultLineId_2, resultDefinition.getId())
                ))
                .withUncompletedResultLines(emptyList())
                .withCompletedResultLinesStatus(singletonMap(resultLineId_1, completedResultLineStatus(resultLineId_1)))
                .build();

        when(referenceDataService.getNowDefinitionByPrimaryResultDefinitionId(any(), eq(resultDefinition.getId()))).thenReturn(nowDefinition);

        when(referenceDataService.getResultDefinitionById(any(), eq(resultDefinition.getId()))).thenReturn(resultDefinition);

        List<Nows> nows = target.createNows(resultsShared);

        assertThat(nows.size(), is(2));

        with(nows.get(0), now -> {
            assertThat(now.getDefendantId(), is(defendantId_1));

            assertThat(now.getMaterials().get(0).getNowResult().get(0).getSharedResultId(), is(resultLineId_1));
        });

        with(nows.get(1), now -> {
            assertThat(now.getDefendantId(), is(defendantId_2));

            assertThat(now.getMaterials().get(0).getNowResult().get(0).getSharedResultId(), is(resultLineId_2));
        });
    }

    @Test
    public void createNows_withAResultLineThatIsNotRelatedToANow() {

        UUID caseId = randomUUID();
        UUID defendantId = randomUUID();
        UUID offenceId = randomUUID();
        UUID resultLineId = randomUUID();

        final ResultsShared resultsShared = ResultsShared.builder()
                .withCases(singletonList(caseTemplate(caseId)))
                .withHearing(hearingTemplate().setDefendants(singletonList(defendantTemplate(caseId, defendantId, offenceId))))
                .withCompletedResultLines(singletonList(completedResultLineTemplate(defendantId, offenceId, caseId, resultLineId, randomUUID())))
                .withUncompletedResultLines(emptyList())
                .withCompletedResultLinesStatus(singletonMap(resultLineId, completedResultLineStatus(resultLineId)))
                .build();

        List<Nows> nows = target.createNows(resultsShared);

        assertThat(nows.size(), is(0));
    }

    @Test
    public void createNows_whenIncompleteLineIsPresent_noNowGenerated() {

        UUID caseId = randomUUID();
        UUID defendantId = randomUUID();
        UUID offenceId = randomUUID();
        UUID resultLineId = randomUUID();

        final ResultsShared resultsShared = ResultsShared.builder()
                .withCases(singletonList(caseTemplate(caseId)))
                .withHearing(hearingTemplate().setDefendants(singletonList(defendantTemplate(caseId, defendantId, offenceId))))
                .withCompletedResultLines(singletonList(completedResultLineTemplate(defendantId, offenceId, caseId, resultLineId, resultDefinition.getId())))
                .withUncompletedResultLines(singletonList(uncompletedResultLineTemplate(defendantId)))
                .withCompletedResultLinesStatus(singletonMap(resultLineId, completedResultLineStatus(resultLineId)))
                .build();

        when(referenceDataService.getNowDefinitionByPrimaryResultDefinitionId(any(), eq(resultDefinition.getId()))).thenReturn(nowDefinition);

        when(referenceDataService.getResultDefinitionById(any(), eq(resultDefinition.getId()))).thenReturn(resultDefinition);

        List<Nows> nows = target.createNows(resultsShared);

        assertThat(nows.size(), is(0));
    }

    @Test
    public void createNows_whenIncompleteLineIsPresentForADifferentDefendant_NowsAreGenerated() {

        UUID caseId = randomUUID();
        UUID defendantId = randomUUID();
        UUID offenceId = randomUUID();
        UUID resultLineId = randomUUID();

        final ResultsShared resultsShared = ResultsShared.builder()
                .withCases(singletonList(caseTemplate(caseId)))
                .withHearing(hearingTemplate().setDefendants(singletonList(defendantTemplate(caseId, defendantId, offenceId))))
                .withCompletedResultLines(singletonList(completedResultLineTemplate(defendantId, offenceId, caseId, resultLineId, resultDefinition.getId())))
                .withUncompletedResultLines(singletonList(uncompletedResultLineTemplate(randomUUID())))
                .withCompletedResultLinesStatus(singletonMap(resultLineId, completedResultLineStatus(resultLineId)))
                .build();

        when(referenceDataService.getNowDefinitionByPrimaryResultDefinitionId(any(), eq(resultDefinition.getId()))).thenReturn(nowDefinition);

        when(referenceDataService.getResultDefinitionById(any(), eq(resultDefinition.getId()))).thenReturn(resultDefinition);

        List<Nows> nows = target.createNows(resultsShared);

        assertThat(nows.size(), is(1));
    }

    @Test
    public void createNows_whenNonMandatoryLineIsNotPresent_NowsAreGenerated() {

        final NowDefinition nowDefinition = NowDefinition.now()
                .setId(UUID.randomUUID())
                .setResultDefinitions(asList(
                        ResultDefinitions.resultDefinitions()
                                .setId(resultDefinition.getId())
                                .setMandatory(true)
                                .setPrimary(true),
                        ResultDefinitions.resultDefinitions()
                                .setId(randomUUID())
                                .setMandatory(false)
                                .setPrimary(false)
                ));

        UUID caseId = randomUUID();
        UUID defendantId = randomUUID();
        UUID offenceId = randomUUID();
        UUID resultLineId = randomUUID();

        final ResultsShared resultsShared = ResultsShared.builder()
                .withCases(singletonList(caseTemplate(caseId)))
                .withHearing(hearingTemplate().setDefendants(singletonList(defendantTemplate(caseId, defendantId, offenceId))))
                .withCompletedResultLines(singletonList(completedResultLineTemplate(defendantId, offenceId, caseId, resultLineId, resultDefinition.getId())))
                .withCompletedResultLinesStatus(singletonMap(resultLineId, completedResultLineStatus(resultLineId)))
                .build();

        when(referenceDataService.getNowDefinitionByPrimaryResultDefinitionId(any(), eq(resultDefinition.getId()))).thenReturn(nowDefinition);

        when(referenceDataService.getResultDefinitionById(any(), eq(resultDefinition.getId()))).thenReturn(resultDefinition);

        List<Nows> nows = target.createNows(resultsShared);

        assertThat(nows.size(), is(1));
    }

    @Test
    public void createNows_whenMandatoryLineIsNotPresent_NowsAreNotGenerated() {

        final NowDefinition nowDefinition = NowDefinition.now()
                .setId(UUID.randomUUID())
                .setResultDefinitions(asList(
                        ResultDefinitions.resultDefinitions()
                                .setId(resultDefinition.getId())
                                .setMandatory(true),
                        ResultDefinitions.resultDefinitions()
                                .setId(randomUUID())
                                .setMandatory(true)
                ));

        UUID caseId = randomUUID();
        UUID defendantId = randomUUID();
        UUID offenceId = randomUUID();
        UUID resultLineId = randomUUID();

        final ResultsShared resultsShared = ResultsShared.builder()
                .withCases(singletonList(caseTemplate(caseId)))
                .withHearing(hearingTemplate().setDefendants(singletonList(defendantTemplate(caseId, defendantId, offenceId))))
                .withCompletedResultLines(singletonList(completedResultLineTemplate(defendantId, offenceId, caseId, resultLineId, resultDefinition.getId())))
                .withCompletedResultLinesStatus(singletonMap(resultLineId, completedResultLineStatus(resultLineId)))
                .build();

        when(referenceDataService.getNowDefinitionByPrimaryResultDefinitionId(any(), eq(resultDefinition.getId()))).thenReturn(nowDefinition);

        when(referenceDataService.getResultDefinitionById(any(), eq(resultDefinition.getId()))).thenReturn(resultDefinition);

        List<Nows> nows = target.createNows(resultsShared);

        assertThat(nows.size(), is(0));
    }

    @Test
    public void createNows_generateMultipleVariants_forDifferentPrompts() {
        final ResultDefinition resultDefinition = ResultDefinition.resultDefinition()
                .setId(randomUUID())
                .setPrompts(asList(Prompt.prompt()
                                .setId(randomUUID())
                                .setLabel("Lock him up")
                                .setUserGroups(singletonList("Court Clerk")),
                        Prompt.prompt()
                                .setId(randomUUID())
                                .setLabel("Set him free")
                                .setUserGroups(singletonList("Listing Officer"))
                ));

        final NowDefinition nowDefinition = NowDefinition.now()
                .setId(UUID.randomUUID())
                .setResultDefinitions(singletonList(
                        ResultDefinitions.resultDefinitions()
                                .setId(resultDefinition.getId())
                                .setMandatory(true)
                ));

        UUID caseId = randomUUID();
        UUID defendantId = randomUUID();
        UUID offenceId = randomUUID();
        UUID resultLineId = randomUUID();

        final ResultsShared resultsShared = ResultsShared.builder()
                .withCases(singletonList(caseTemplate(caseId)))
                .withHearing(hearingTemplate().setDefendants(singletonList(defendantTemplate(caseId, defendantId, offenceId))))
                .withCompletedResultLines(singletonList(completedResultLineTemplate(defendantId, offenceId, caseId, resultLineId, resultDefinition.getId())))
                .withUncompletedResultLines(emptyList())
                .withCompletedResultLinesStatus(singletonMap(resultLineId, completedResultLineStatus(resultLineId)))
                .build();

        when(referenceDataService.getNowDefinitionByPrimaryResultDefinitionId(any(), eq(resultDefinition.getId()))).thenReturn(nowDefinition);

        when(referenceDataService.getResultDefinitionById(any(), eq(resultDefinition.getId()))).thenReturn(resultDefinition);

        List<Nows> nows = target.createNows(resultsShared);

        assertThat(nows.size(), is(1));

        Nows now = nows.get(0);

        assertThat(now.getMaterials(), hasSize(2));

        assertThat(now.getMaterials().get(0).getUserGroups().stream().map(UserGroups::getGroup).collect(toList()), containsInAnyOrder("Court Clerk"));
        assertThat(now.getMaterials().get(0).getNowResult().get(0).getPrompts(), hasSize(1));
        assertThat(now.getMaterials().get(0).getNowResult().get(0).getPrompts().get(0).getLabel(), is("Lock him up"));

        assertThat(now.getMaterials().get(1).getUserGroups().stream().map(UserGroups::getGroup).collect(toList()), containsInAnyOrder("Listing Officer"));
        assertThat(now.getMaterials().get(1).getNowResult().get(0).getPrompts(), hasSize(1));
        assertThat(now.getMaterials().get(1).getNowResult().get(0).getPrompts().get(0).getLabel(), is("Set him free"));
    }

    @Test
    public void createNows_generateSingleVariant_forMultipleUserGroups() {
        final ResultDefinition resultDefinition = ResultDefinition.resultDefinition()
                .setId(randomUUID())
                .setPrompts(singletonList(Prompt.prompt()
                        .setId(randomUUID())
                        .setLabel("Lock him up")
                        .setUserGroups(asList("Court Clerk", "Listing Officer"))
                ));

        final NowDefinition nowDefinition = NowDefinition.now()
                .setId(UUID.randomUUID())
                .setResultDefinitions(singletonList(
                        ResultDefinitions.resultDefinitions()
                                .setId(resultDefinition.getId())
                                .setMandatory(true)
                ));

        UUID caseId = randomUUID();
        UUID defendantId = randomUUID();
        UUID offenceId = randomUUID();
        UUID resultLineId = randomUUID();

        final ResultsShared resultsShared = ResultsShared.builder()
                .withCases(singletonList(caseTemplate(caseId)))
                .withHearing(hearingTemplate().setDefendants(singletonList(defendantTemplate(caseId, defendantId, offenceId))))
                .withCompletedResultLines(singletonList(completedResultLineTemplate(defendantId, offenceId, caseId, resultLineId, resultDefinition.getId())))
                .withUncompletedResultLines(emptyList())
                .withCompletedResultLinesStatus(singletonMap(resultLineId, completedResultLineStatus(resultLineId)))
                .build();

        when(referenceDataService.getNowDefinitionByPrimaryResultDefinitionId(any(), eq(resultDefinition.getId()))).thenReturn(nowDefinition);

        when(referenceDataService.getResultDefinitionById(any(), eq(resultDefinition.getId()))).thenReturn(resultDefinition);

        List<Nows> nows = target.createNows(resultsShared);

        assertThat(nows.size(), is(1));

        Nows now = nows.get(0);

        assertThat(now.getMaterials(), hasSize(1));

        assertThat(now.getMaterials().get(0).getUserGroups().stream().map(UserGroups::getGroup).collect(toList()), containsInAnyOrder("Court Clerk", "Listing Officer"));
        assertThat(now.getMaterials().get(0).getNowResult().get(0).getPrompts(), hasSize(1));
        assertThat(now.getMaterials().get(0).getNowResult().get(0).getPrompts().get(0).getLabel(), is("Lock him up"));
    }

    @Test
    public void createNows_generateVariant_ResultDefinitionWithoutPrompts() {
        final ResultDefinition resultDefinition = ResultDefinition.resultDefinition()
                .setId(randomUUID())
                .setUserGroups(singletonList("Prison Admin"));

        final NowDefinition nowDefinition = NowDefinition.now()
                .setId(UUID.randomUUID())
                .setResultDefinitions(singletonList(
                        ResultDefinitions.resultDefinitions()
                                .setId(resultDefinition.getId())
                                .setMandatory(true)
                ));

        UUID caseId = randomUUID();
        UUID defendantId = randomUUID();
        UUID offenceId = randomUUID();
        UUID resultLineId = randomUUID();

        final ResultsShared resultsShared = ResultsShared.builder()
                .withCases(singletonList(caseTemplate(caseId)))
                .withHearing(hearingTemplate().setDefendants(singletonList(defendantTemplate(caseId, defendantId, offenceId))))
                .withCompletedResultLines(singletonList(completedResultLineTemplate(defendantId, offenceId, caseId, resultLineId, resultDefinition.getId())))
                .withUncompletedResultLines(emptyList())
                .withCompletedResultLinesStatus(singletonMap(resultLineId, completedResultLineStatus(resultLineId)))
                .build();

        when(referenceDataService.getNowDefinitionByPrimaryResultDefinitionId(any(), eq(resultDefinition.getId()))).thenReturn(nowDefinition);

        when(referenceDataService.getResultDefinitionById(any(), eq(resultDefinition.getId()))).thenReturn(resultDefinition);

        List<Nows> nows = target.createNows(resultsShared);

        assertThat(nows.size(), is(1));

        Nows now = nows.get(0);

        assertThat(now.getMaterials(), hasSize(1));

        assertThat(now.getMaterials().get(0).getUserGroups().stream().map(UserGroups::getGroup).collect(toList()), containsInAnyOrder("Prison Admin"));
        assertThat(now.getMaterials().get(0).getNowResult().get(0).getPrompts(), is(empty()));
    }

    @Test
    public void createNows_generateMultipleVariants_forDifferentPromptsAndAdditionalUserGroups() {
        final ResultDefinition resultDefinition = ResultDefinition.resultDefinition()
                .setId(randomUUID())
                .setUserGroups(asList("Prison Admin", "LLA User"))
                .setPrompts(asList(Prompt.prompt()
                                .setId(randomUUID())
                                .setLabel("Lock him up")
                                .setUserGroups(asList("Court Clerk", "Judge")),
                        Prompt.prompt()
                                .setId(randomUUID())
                                .setLabel("Set him free")
                                .setUserGroups(asList("Listing Officer", "Judge"))
                ));

        final NowDefinition nowDefinition = NowDefinition.now()
                .setId(UUID.randomUUID())
                .setResultDefinitions(singletonList(
                        ResultDefinitions.resultDefinitions()
                                .setId(resultDefinition.getId())
                                .setMandatory(true)
                ));

        UUID caseId = randomUUID();
        UUID defendantId = randomUUID();
        UUID offenceId = randomUUID();
        UUID resultLineId = randomUUID();

        final ResultsShared resultsShared = ResultsShared.builder()
                .withCases(singletonList(caseTemplate(caseId)))
                .withHearing(hearingTemplate().setDefendants(singletonList(defendantTemplate(caseId, defendantId, offenceId))))
                .withCompletedResultLines(singletonList(completedResultLineTemplate(defendantId, offenceId, caseId, resultLineId, resultDefinition.getId())))
                .withUncompletedResultLines(emptyList())
                .withCompletedResultLinesStatus(singletonMap(resultLineId, completedResultLineStatus(resultLineId)))
                .build();

        when(referenceDataService.getNowDefinitionByPrimaryResultDefinitionId(any(), eq(resultDefinition.getId()))).thenReturn(nowDefinition);

        when(referenceDataService.getResultDefinitionById(any(), eq(resultDefinition.getId()))).thenReturn(resultDefinition);

        List<Nows> nows = target.createNows(resultsShared);

        assertThat(nows.size(), is(1));

        Nows now = nows.get(0);

        assertThat(now.getMaterials(), hasSize(4));

        assertThat(now.getMaterials().get(0).getUserGroups().stream().map(UserGroups::getGroup).collect(toList()), containsInAnyOrder("Court Clerk"));
        assertThat(now.getMaterials().get(0).getNowResult().get(0).getPrompts(), hasSize(1));
        assertThat(now.getMaterials().get(0).getNowResult().get(0).getPrompts().get(0).getLabel(), is("Lock him up"));

        assertThat(now.getMaterials().get(1).getUserGroups().stream().map(UserGroups::getGroup).collect(toList()), containsInAnyOrder("Judge"));
        assertThat(now.getMaterials().get(1).getNowResult().get(0).getPrompts(), hasSize(2));
        assertThat(now.getMaterials().get(1).getNowResult().get(0).getPrompts().get(1).getLabel(), is("Set him free"));
        assertThat(now.getMaterials().get(1).getNowResult().get(0).getPrompts().get(0).getLabel(), is("Lock him up"));

        assertThat(now.getMaterials().get(2).getUserGroups().stream().map(UserGroups::getGroup).collect(toList()), containsInAnyOrder("LLA User", "Prison Admin"));
        assertThat(now.getMaterials().get(2).getNowResult().get(0).getPrompts(), empty());

        assertThat(now.getMaterials().get(3).getUserGroups().stream().map(UserGroups::getGroup).collect(toList()), containsInAnyOrder("Listing Officer"));
        assertThat(now.getMaterials().get(3).getNowResult().get(0).getPrompts(), hasSize(1));
        assertThat(now.getMaterials().get(3).getNowResult().get(0).getPrompts().get(0).getLabel(), is("Set him free"));
    }


    @Test
    public void createNows_givenPreviouslyGenerated_noNowIsGenerated() {

        UUID caseId = randomUUID();
        UUID defendantId = randomUUID();
        UUID offenceId = randomUUID();
        UUID resultLineId = randomUUID();

        CompletedResultLineStatus completedResultLineStatus = completedResultLineStatus(resultLineId);

        final ResultsShared resultsShared = ResultsShared.builder()
                .withCases(singletonList(caseTemplate(caseId)))
                .withHearing(hearingTemplate().setDefendants(singletonList(defendantTemplate(caseId, defendantId, offenceId))))
                .withCompletedResultLines(singletonList(completedResultLineTemplate(defendantId, offenceId, caseId, resultLineId, resultDefinition.getId())))
                .withUncompletedResultLines(emptyList())
                .withCompletedResultLinesStatus(singletonMap(resultLineId, completedResultLineStatus))
                .withVariantDirectory(singletonList(
                        Variant.variant()
                                .setKey(VariantKey.variantKey()
                                        .setDefendantId(defendantId)
                                        .setUsergroups(singletonList("Court Clerk"))
                                        .setNowsTypeId(nowDefinition.getId())
                                )
                                .setValue(VariantValue.variantValue()
                                        .setResultLines(singletonList(ResultLineReference.resultLineReference()
                                                .setResultLineId(resultLineId)
                                                .setLastSharedTime(completedResultLineStatus.getLastSharedDateTime())
                                        ))
                                )
                ))
                .build();

        when(referenceDataService.getNowDefinitionByPrimaryResultDefinitionId(any(), eq(resultDefinition.getId()))).thenReturn(nowDefinition);

        when(referenceDataService.getResultDefinitionById(any(), eq(resultDefinition.getId()))).thenReturn(resultDefinition);

        List<Nows> nows = target.createNows(resultsShared);

        assertThat(nows, empty());
    }

    @Test
    public void createNows_givenPreviouslyGeneratedButWeNowHaveANewLine_aNowIsGenerated() {

        UUID caseId = randomUUID();
        UUID defendantId = randomUUID();
        UUID offenceId = randomUUID();
        UUID resultLineId = randomUUID();

        final ResultsShared resultsShared = ResultsShared.builder()
                .withCases(singletonList(caseTemplate(caseId)))
                .withHearing(hearingTemplate().setDefendants(singletonList(defendantTemplate(caseId, defendantId, offenceId))))
                .withCompletedResultLines(singletonList(completedResultLineTemplate(defendantId, offenceId, caseId, resultLineId, resultDefinition.getId())))
                .withUncompletedResultLines(emptyList())
                .withCompletedResultLinesStatus(singletonMap(resultLineId, completedResultLineStatus(resultLineId)))
                .withVariantDirectory(singletonList(
                        Variant.variant()
                                .setKey(VariantKey.variantKey()
                                        .setDefendantId(defendantId)
                                        .setUsergroups(singletonList("Court Clerk"))
                                        .setNowsTypeId(nowDefinition.getId())
                                )
                                .setValue(VariantValue.variantValue()
                                        .setResultLines(singletonList(ResultLineReference.resultLineReference()
                                                .setResultLineId(resultLineId)
                                                .setLastSharedTime(FUTURE_ZONED_DATE_TIME.next().withZoneSameInstant(ZoneId.of("UTC")))
                                        ))
                                )
                ))
                .build();

        when(referenceDataService.getNowDefinitionByPrimaryResultDefinitionId(any(), eq(resultDefinition.getId()))).thenReturn(nowDefinition);

        when(referenceDataService.getResultDefinitionById(any(), eq(resultDefinition.getId()))).thenReturn(resultDefinition);

        List<Nows> nows = target.createNows(resultsShared);

        assertThat(nows, hasSize(1));

        assertThat(nows.get(0).getMaterials(), hasSize(1));

        assertThat(nows.get(0).getMaterials().get(0).isAmended(), is(true));
    }
}
