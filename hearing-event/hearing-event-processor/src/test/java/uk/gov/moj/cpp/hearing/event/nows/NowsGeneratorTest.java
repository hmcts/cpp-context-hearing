package uk.gov.moj.cpp.hearing.event.nows;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
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
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.target;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.CompletedResultLineStatusTemplates.completedResultLineStatus;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.second;

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
import uk.gov.moj.cpp.hearing.event.NowsTemplates;
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
import uk.gov.moj.cpp.hearing.test.CommandHelpers;
import uk.gov.moj.cpp.hearing.test.CoreTestTemplates;
import uk.gov.moj.cpp.hearing.test.Pair;

import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

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

        final CommandHelpers.ResultsSharedEventHelper resultsShared = h(with(NowsTemplates.resultsSharedTemplate(), event -> {
            h(event).getFirstCompletedResultLine().setResultDefinitionId(resultDefinition.getId());
        }));

        when(referenceDataService.getNowDefinitionByPrimaryResultDefinitionId(any(), any(), eq(resultDefinition.getId()))).thenReturn(nowDefinition);

        when(referenceDataService.getResultDefinitionById(any(), any(), eq(resultDefinition.getId()))).thenReturn(resultDefinition);

        List<Nows> nows = target.createNows(null, resultsShared.it());


        assertThat(nows, first(isBean(Nows.class)
                .with(Nows::getDefendantId, is(resultsShared.getFirstDefendant().getId()))
                .with(Nows::getNowsTypeId, is(nowDefinition.getId()))
                .with(Nows::getMaterials, first(isBean(Material.class)
                        .with(Material::getId, is(not(nullValue())))
                        .with(Material::getUserGroups, hasItems(isBean(UserGroups.class)
                                .with(UserGroups::getGroup, is("Court Clerk"))
                        ))
                        .with(Material::isAmended, is(false))
                        .with(Material::getNowResult, first(isBean(NowResult.class)
                                .with(NowResult::getSharedResultId, is(resultsShared.getFirstCompletedResultLine().getResultLineId()))
                                .with(NowResult::getPrompts, first(isBean(PromptRef.class)
                                        .with(PromptRef::getLabel, is("Lock him up"))
                                ))
                        ))
                ))
        ));


    }

    @Test
    public void createNows_generateANowForEachDefendant() {

        final CommandHelpers.ResultsSharedEventHelper resultsShared = h(with(NowsTemplates.resultsSharedTemplate(), event -> {
            CommandHelpers.ResultsSharedEventHelper helper = h(event);

            UUID secondDefendantId = randomUUID();
            UUID secondOffenceId = randomUUID();
            helper.getFirstCase().getDefendants().add(CoreTestTemplates.defendant(helper.getFirstCase().getId(),
                    CoreTestTemplates.defaultArguments(),
                    new Pair<>(secondDefendantId, asList(secondOffenceId))
            ).build());

            helper.getFirstCompletedResultLine().setResultDefinitionId(resultDefinition.getId());

            helper.getHearing().getTargets().add(with(target(helper.getHearingId(), secondDefendantId, secondOffenceId, randomUUID()).build(), target -> {
                target.getResultLines().get(0).setResultDefinitionId(resultDefinition.getId());
            }));
        }));

        when(referenceDataService.getNowDefinitionByPrimaryResultDefinitionId(any(), any(), eq(resultDefinition.getId()))).thenReturn(nowDefinition);

        when(referenceDataService.getResultDefinitionById(any(), any(), eq(resultDefinition.getId()))).thenReturn(resultDefinition);

        List<Nows> nows = target.createNows(null, resultsShared.it());

        assertThat(nows, first(isBean(Nows.class)
                .with(Nows::getDefendantId, is(resultsShared.getFirstDefendant().getId()))
                .with(Nows::getMaterials, first(isBean(Material.class)
                        .with(Material::getNowResult, first(isBean(NowResult.class)
                                        .with(NowResult::getSharedResultId, is(resultsShared.getFirstCompletedResultLine().getResultLineId()))
                                )
                        )
                ))
        ));

        assertThat(nows, second(isBean(Nows.class)
                .with(Nows::getDefendantId, is(resultsShared.getSecondDefendant().getId()))
                .with(Nows::getMaterials, first(isBean(Material.class)
                        .with(Material::getNowResult, first(isBean(NowResult.class)
                                        .with(NowResult::getSharedResultId, is(resultsShared.getSecondCompletedResultLine().getResultLineId()))
                                )
                        )
                ))
        ));
    }

    @Test
    public void createNows_withAResultLineThatIsNotRelatedToANow() {

        final CommandHelpers.ResultsSharedEventHelper resultsShared = h(NowsTemplates.resultsSharedTemplate());

        List<Nows> nows = target.createNows(null, resultsShared.it());

        assertThat(nows.size(), is(0));
    }

    @Test
    public void createNows_whenIncompleteLineIsPresent_noNowGenerated() {

        final CommandHelpers.ResultsSharedEventHelper resultsShared = h(with(NowsTemplates.resultsSharedTemplate(), event -> {
            CommandHelpers.ResultsSharedEventHelper helper = h(event);


            helper.getFirstCompletedResultLine().setResultDefinitionId(resultDefinition.getId());

            helper.getHearing().getTargets().add(with(target(helper.getHearingId(), helper.getFirstDefendant().getId(), helper.getFirstDefendantFirstOffence().getId(), randomUUID()).build(), target -> {
                target.getResultLines().get(0).setResultDefinitionId(resultDefinition.getId());
                target.getResultLines().get(0).setIsComplete(false);
            }));
        }));

        when(referenceDataService.getNowDefinitionByPrimaryResultDefinitionId(any(), any(), eq(resultDefinition.getId()))).thenReturn(nowDefinition);

        when(referenceDataService.getResultDefinitionById(any(), any(), eq(resultDefinition.getId()))).thenReturn(resultDefinition);

        List<Nows> nows = target.createNows(null, resultsShared.it());

        assertThat(nows.size(), is(0));
    }

    @Test
    public void createNows_whenIncompleteLineIsPresentForADifferentDefendant_NowsAreGenerated() {

        final CommandHelpers.ResultsSharedEventHelper resultsShared = h(with(NowsTemplates.resultsSharedTemplate(), event -> {
            CommandHelpers.ResultsSharedEventHelper helper = h(event);

            UUID secondDefendantId = randomUUID();
            UUID secondOffenceId = randomUUID();
            helper.getFirstCase().getDefendants().add(CoreTestTemplates.defendant(helper.getFirstCase().getId(),
                    CoreTestTemplates.defaultArguments(),
                    new Pair<>(secondDefendantId, asList(secondOffenceId))
            ).build());

            helper.getFirstCompletedResultLine().setResultDefinitionId(resultDefinition.getId());

            helper.getHearing().getTargets().add(with(target(helper.getHearingId(), secondDefendantId, secondOffenceId, randomUUID()).build(), target -> {
                target.getResultLines().get(0).setResultDefinitionId(resultDefinition.getId());
                target.getResultLines().get(0).setIsComplete(false);
            }));
        }));

        when(referenceDataService.getNowDefinitionByPrimaryResultDefinitionId(any(), any(), eq(resultDefinition.getId()))).thenReturn(nowDefinition);

        when(referenceDataService.getResultDefinitionById(any(), any(), eq(resultDefinition.getId()))).thenReturn(resultDefinition);

        List<Nows> nows = target.createNows(null, resultsShared.it());

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


        final CommandHelpers.ResultsSharedEventHelper resultsShared = h(with(NowsTemplates.resultsSharedTemplate(), event -> {
            h(event).getFirstCompletedResultLine().setResultDefinitionId(resultDefinition.getId());
        }));

        when(referenceDataService.getNowDefinitionByPrimaryResultDefinitionId(any(), any(), eq(resultDefinition.getId()))).thenReturn(nowDefinition);

        when(referenceDataService.getResultDefinitionById(any(), any(), eq(resultDefinition.getId()))).thenReturn(resultDefinition);

        List<Nows> nows = target.createNows(null, resultsShared.it());

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

        final CommandHelpers.ResultsSharedEventHelper resultsShared = h(with(NowsTemplates.resultsSharedTemplate(), event -> {
            h(event).getFirstCompletedResultLine().setResultDefinitionId(resultDefinition.getId());
        }));

        when(referenceDataService.getNowDefinitionByPrimaryResultDefinitionId(any(), any(), eq(resultDefinition.getId()))).thenReturn(nowDefinition);

        when(referenceDataService.getResultDefinitionById(any(), any(), eq(resultDefinition.getId()))).thenReturn(resultDefinition);

        List<Nows> nows = target.createNows(null, resultsShared.it());

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

        final CommandHelpers.ResultsSharedEventHelper resultsShared = h(with(NowsTemplates.resultsSharedTemplate(), event -> {
            h(event).getFirstCompletedResultLine().setResultDefinitionId(resultDefinition.getId());
        }));

        when(referenceDataService.getNowDefinitionByPrimaryResultDefinitionId(any(), any(), eq(resultDefinition.getId()))).thenReturn(nowDefinition);

        when(referenceDataService.getResultDefinitionById(any(), any(), eq(resultDefinition.getId()))).thenReturn(resultDefinition);

        List<Nows> nows = target.createNows(null, resultsShared.it());

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

        final CommandHelpers.ResultsSharedEventHelper resultsShared = h(with(NowsTemplates.resultsSharedTemplate(), event -> {
            h(event).getFirstCompletedResultLine().setResultDefinitionId(resultDefinition.getId());
        }));

        when(referenceDataService.getNowDefinitionByPrimaryResultDefinitionId(any(), any(), eq(resultDefinition.getId()))).thenReturn(nowDefinition);

        when(referenceDataService.getResultDefinitionById(any(), any(), eq(resultDefinition.getId()))).thenReturn(resultDefinition);

        List<Nows> nows = target.createNows(null, resultsShared.it());

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

        final CommandHelpers.ResultsSharedEventHelper resultsShared = h(with(NowsTemplates.resultsSharedTemplate(), event -> {
            h(event).getFirstCompletedResultLine().setResultDefinitionId(resultDefinition.getId());
        }));

        when(referenceDataService.getNowDefinitionByPrimaryResultDefinitionId(any(), any(), eq(resultDefinition.getId()))).thenReturn(nowDefinition);

        when(referenceDataService.getResultDefinitionById(any(), any(), eq(resultDefinition.getId()))).thenReturn(resultDefinition);

        List<Nows> nows = target.createNows(null, resultsShared.it());

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


        final CommandHelpers.ResultsSharedEventHelper resultsShared = h(with(NowsTemplates.resultsSharedTemplate(), event -> {
            h(event).getFirstCompletedResultLine().setResultDefinitionId(resultDefinition.getId());
        }));

        when(referenceDataService.getNowDefinitionByPrimaryResultDefinitionId(any(), any(), eq(resultDefinition.getId()))).thenReturn(nowDefinition);

        when(referenceDataService.getResultDefinitionById(any(), any(), eq(resultDefinition.getId()))).thenReturn(resultDefinition);

        List<Nows> nows = target.createNows(null, resultsShared.it());

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

        final CommandHelpers.ResultsSharedEventHelper resultsShared = h(with(NowsTemplates.resultsSharedTemplate(), event -> {
            CommandHelpers.ResultsSharedEventHelper helper = h(event);

            CompletedResultLineStatus completedResultLineStatus = completedResultLineStatus(helper.getFirstCompletedResultLine().getResultLineId());

            helper.it().getCompletedResultLinesStatus().put(helper.getFirstCompletedResultLine().getResultLineId(), completedResultLineStatus);

            helper.getFirstCompletedResultLine().setResultDefinitionId(resultDefinition.getId());

            helper.it().setVariantDirectory(singletonList(
                    Variant.variant()
                            .setKey(VariantKey.variantKey()
                                    .setDefendantId(helper.getFirstDefendant().getId())
                                    .setUsergroups(singletonList("Court Clerk"))
                                    .setNowsTypeId(nowDefinition.getId())
                            )
                            .setValue(VariantValue.variantValue()
                                    .setResultLines(singletonList(ResultLineReference.resultLineReference()
                                            .setResultLineId(helper.getFirstCompletedResultLine().getResultLineId())
                                            .setLastSharedTime(completedResultLineStatus.getLastSharedDateTime())
                                    ))
                            )
            ));
        }));

        when(referenceDataService.getNowDefinitionByPrimaryResultDefinitionId(any(), any(), eq(resultDefinition.getId()))).thenReturn(nowDefinition);

        when(referenceDataService.getResultDefinitionById(any(), any(), eq(resultDefinition.getId()))).thenReturn(resultDefinition);

        List<Nows> nows = target.createNows(null, resultsShared.it());

        assertThat(nows, empty());
    }

    @Test
    public void createNows_givenPreviouslyGeneratedButWeNowHaveANewLine_aNowIsGenerated() {

        final CommandHelpers.ResultsSharedEventHelper resultsShared = h(with(NowsTemplates.resultsSharedTemplate(), event -> {
            CommandHelpers.ResultsSharedEventHelper helper = h(event);

            CompletedResultLineStatus completedResultLineStatus = completedResultLineStatus(helper.getFirstCompletedResultLine().getResultLineId());

            helper.it().getCompletedResultLinesStatus().put(helper.getFirstCompletedResultLine().getResultLineId(), completedResultLineStatus);

            helper.getFirstCompletedResultLine().setResultDefinitionId(resultDefinition.getId());

            helper.it().setVariantDirectory(singletonList(
                    Variant.variant()
                            .setKey(VariantKey.variantKey()
                                    .setDefendantId(helper.getFirstDefendant().getId())
                                    .setUsergroups(singletonList("Court Clerk"))
                                    .setNowsTypeId(nowDefinition.getId())
                            )
                            .setValue(VariantValue.variantValue()
                                    .setResultLines(singletonList(ResultLineReference.resultLineReference()
                                            .setResultLineId(helper.getFirstCompletedResultLine().getResultLineId())
                                            .setLastSharedTime(FUTURE_ZONED_DATE_TIME.next().withZoneSameInstant(ZoneId.of("UTC")))
                                    ))
                            )
            ));
        }));

        when(referenceDataService.getNowDefinitionByPrimaryResultDefinitionId(any(), any(), eq(resultDefinition.getId()))).thenReturn(nowDefinition);

        when(referenceDataService.getResultDefinitionById(any(), any(), eq(resultDefinition.getId()))).thenReturn(resultDefinition);

        List<Nows> nows = target.createNows(null, resultsShared.it());

        assertThat(nows, hasSize(1));

        assertThat(nows.get(0).getMaterials(), hasSize(1));

        assertThat(nows.get(0).getMaterials().get(0).isAmended(), is(true));
    }
}
