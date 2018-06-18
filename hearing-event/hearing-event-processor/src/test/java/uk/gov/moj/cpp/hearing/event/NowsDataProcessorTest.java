package uk.gov.moj.cpp.hearing.event;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.moj.cpp.hearing.command.initiate.Address;
import uk.gov.moj.cpp.hearing.command.initiate.Defendant;
import uk.gov.moj.cpp.hearing.command.initiate.DefendantCase;
import uk.gov.moj.cpp.hearing.command.initiate.Hearing;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLine;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLineStatus;
import uk.gov.moj.cpp.hearing.command.result.ResultPrompt;
import uk.gov.moj.cpp.hearing.command.result.UncompletedResultLine;
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselUpsert;
import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselUpsert;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Attendees;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Cases;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Defendants;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Material;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.NowResult;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Nows;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.PromptRefs;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.UserGroups;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.NowDefinition;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.ResultDefinitions;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;
import uk.gov.moj.cpp.hearing.event.service.ReferenceDataService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.basicInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.caseTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.defendantTemplate;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;

@RunWith(MockitoJUnitRunner.class)
public class NowsDataProcessorTest {
    final String defenceUserGroup = "defenceUserGroup";
    final String prosecutionUserGroup = "prosecutionUserGroup";
    final String courtClerkUserGroup = "courtClerkUserGroup";
    final String prisonOfficerUserGroup = "prisonOfficerUserGroup";

    @Test
    public void testSingleDefendantGreenPath() {
        TestDescription testDescription = testMultipleDefendantsDefault1NowPerDefendant(1);

        Nows nows = testDescription.outputNows.get(0);
        Assert.assertEquals(1, nows.getMaterial().size());
        Assert.assertEquals(testDescription.dataIn.getHearing().getDefendants().get(0).getId().toString(), nows.getDefendantId());
        Assert.assertNotNull(nows.getNowsTypeId());
        Material material = nows.getMaterial().get(0);
        Assert.assertEquals(1, material.getUserGroups().size());
        Assert.assertEquals(defenceUserGroup, material.getUserGroups().get(0).getGroup());
        Assert.assertEquals(1, material.getNowResult().size());
        NowResult nowResult = material.getNowResult().get(0);
        CompletedResultLine inputResultLine = testDescription.dataIn.getCompletedResultLines().get(0);
        Assert.assertEquals(nowResult.getSharedResultId(), inputResultLine.getId());
        Assert.assertEquals(1, nowResult.getPromptRefs().size());
        ResultPrompt resultPrompt = inputResultLine.getPrompts().get(0);
        PromptRefs promptRef = nowResult.getPromptRefs().get(0);
        Assert.assertEquals(resultPrompt.getLabel(), promptRef.getLabel());
    }

    @Test
    public void testMultipleDefendantsGreenPath() {
        TestDescription testDescription = testMultipleDefendantsDefault1NowPerDefendant(3);
        Assert.assertEquals(3, testDescription.outputNows.size());
    }

    @Test
    public void testResultDefinitionNotMappedToNow() {
        TestDescription testDescription;
        BiFunction<CompletedResultLine, Integer, List<Object>> resultLineFilter;
        resultLineFilter = (rl, index) -> asList(rl);
        testDescription = testMultipleDefendantsDefault1NowPerDefendant(1, resultLineFilter);
        //check green path
        Assert.assertEquals(1, testDescription.outputNows.size());
        resultLineFilter = (rl, index) -> {
            // replace result line with an unmapped (to now)
            rl = CompletedResultLine.builder().withId(randomUUID()).withDefendantId(rl.getDefendantId()).withResultDefinitionId(randomUUID()).build();
            return asList(rl);
        };

        // retest adding in the the mandatory non primary result line
        testDescription = testMultipleDefendantsDefault1NowPerDefendant(1, resultLineFilter);
        //the non primary mandatory is present hence now can be created
        Assert.assertEquals(0, testDescription.outputNows.size());

    }

    @Test
    public void testNonPrimaryMandatory() {
        ResultDefinition primaryResultDefinition = ResultDefinition.resultDefinition().setId(randomUUID());
        ResultDefinition mandatoryNonPrimaryResultDefinition = ResultDefinition.resultDefinition().setId(randomUUID());
        // inject a now that has 2 mandatories one primary, 1 non primary
        NowDefinition referenceNow = NowDefinition.now()
                .setId(randomUUID())
                .setResultDefinitions(
                asList(
                        ResultDefinitions.resultDefinitions().setPrimaryResult(true).setMandatory(true).setId(primaryResultDefinition.getId()),
                        ResultDefinitions.resultDefinitions().setPrimaryResult(false).setMandatory(true).setId(mandatoryNonPrimaryResultDefinition.getId())
                )
        );
        Mockito.when(referenceDataService.getNowDefinitionByPrimaryResultDefinitionId(primaryResultDefinition.getId())).thenReturn(referenceNow);
        BiFunction<CompletedResultLine, Integer, List<Object>> resultLineFilter = (rl, defendantIndex) -> {
            CompletedResultLine primaryResultLine = CompletedResultLine.builder().withResultDefinitionId(primaryResultDefinition.getId()).withDefendantId(rl.getDefendantId()).build();
            return asList(primaryResultLine);
        };
        TestDescription testDescription;
        testDescription = testMultipleDefendantsDefault1NowPerDefendant(1, resultLineFilter);
        //the non primary non mandatory is not present hence no now
        Assert.assertEquals(0, testDescription.outputNows.size());
        ResultsShared resultsShared = null;

        // retest adding in the the mandatory non primary result line
        testDescription = testMultipleDefendantsDefault1NowPerDefendant(1,
                (rl, index) -> {
                    List<Object> results = asList(rl);

                    //if this is the primary createNows a non primary mandatory
                    if (rl.getResultDefinitionId() == primaryResultDefinition.getId()) {
                        results.add(
                                CompletedResultLine.builder().withId(randomUUID())
                                        .withDefendantId(rl.getDefendantId())
                                        .withResultDefinitionId(mandatoryNonPrimaryResultDefinition.getId())
                                        .build());

                    }
                    return results;
                });
        //the non primary mandatory is present hence now can be created
        Assert.assertEquals(1, testDescription.outputNows.size());

    }

    @Test
    public void testMultipleDefendantsIncompleteLines() {
        BiFunction<CompletedResultLine, Integer, List<Object>> resultLineFilterByDefendantIndex = (rl, index) ->
        {
            Boolean isComplete = index % 2 == 0;
            Object resultLine;
            if (isComplete) {
                resultLine = CompletedResultLine.builder().withId(rl.getId())
                        .withResultPrompts(rl.getPrompts())
                        .withDefendantId(rl.getDefendantId())
                        .withResultDefinitionId(rl.getResultDefinitionId())
                        .build();
            } else {
                resultLine = UncompletedResultLine.builder().withId(rl.getId())
                        .withResultDefinitionId(rl.getResultDefinitionId())
                        .withDefendantId(rl.getDefendantId())
                        .build();
            }

            return asList(resultLine);
        };
        TestDescription testDescription = testMultipleDefendantsDefault1NowPerDefendant(5, resultLineFilterByDefendantIndex);
        List<Nows> nows = testDescription.outputNows;
        Assert.assertNotNull(nows);
        Assert.assertEquals(3, nows.size());
    }

    static class TestDescription {
        List<Nows> outputNows;
        Hearing outputHearing;
        ResultsShared dataIn;
    }

    public TestDescription testMultipleDefendantsDefault1NowPerDefendant(int defendantCount) {
        return testMultipleDefendantsDefault1NowPerDefendant(defendantCount, null);
    }

    @Mock
    private ReferenceDataService referenceDataService;

    private ResultDefinition mockReferenceData() {
        return ResultDefinition.resultDefinition()
                .setId(randomUUID())
                .setUserGroups(asList(defenceUserGroup))
                .setPrompts(
                        asList(
                                Prompt.prompt().setId(randomUUID())
                                        .setLabel("label1")
                                        .setUserGroups(asList(defenceUserGroup))
                        )
                );
    }


    public TestDescription testMultipleDefendantsDefault1NowPerDefendant(int defendantCount, BiFunction<CompletedResultLine, Integer, List<Object>> resultLineFilterByDefendantIndex) {

        TestDescription result = new TestDescription();
        ResultDefinition resultDefinitionRefIn = mockReferenceData();

        ResultsShared.Builder resultsSharedBuilder = createResultsShared(resultDefinitionRefIn, defendantCount, resultLineFilterByDefendantIndex);
        result.dataIn = resultsSharedBuilder.build();
        UUID resultDefinitionId = resultDefinitionRefIn.getId();
        NowDefinition nowRefIn = NowDefinition.now().setId(randomUUID());
        nowRefIn.setResultDefinitions(
                asList(
                        ResultDefinitions.resultDefinitions()
                                .setId(resultDefinitionId)
                                .setMandatory(true)
                                .setPrimaryResult(true)
                        //NOTYET test property sequence
                )
        );

        NowsDataProcessor target = new NowsDataProcessor(referenceDataService);
        Mockito.when(referenceDataService.getNowDefinitionByPrimaryResultDefinitionId(resultDefinitionId)).thenReturn(nowRefIn);
        Mockito.when(referenceDataService.getResultDefinitionById(resultDefinitionId)).thenReturn(resultDefinitionRefIn);
        //Whitebox.setInternalState(target, "referenceDataService", referenceDataService);

        List<Nows> generateNowsCommand = target.createNows(result.dataIn);
        uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Hearing hearing = target.translateReferenceData(result.dataIn);

        if (generateNowsCommand != null) {
            checkReferenceData(result.dataIn, hearing);
        }

        result.outputNows = generateNowsCommand;
        return result;
    }

    //TODO reinstate checkRererenceData

    @Test
    public void testMultiVariants1Variant() {

        ResultDefinition primaryResultDefinition = ResultDefinition.resultDefinition()
                .setId(randomUUID())
                .setPrompts(
                        asList(
                                Prompt.prompt().setId(randomUUID()).setLabel("label1").setUserGroups(asList(defenceUserGroup)),
                                Prompt.prompt().setId(randomUUID()).setLabel("label2").setUserGroups(asList(defenceUserGroup))
                        )
                );

        // inject a now that has 2 mandatories one primary, 1 non primary
        NowDefinition referenceNow = NowDefinition.now().setId(randomUUID()).setResultDefinitions(
                asList(
                        ResultDefinitions.resultDefinitions().setPrimaryResult(true)
                                .setMandatory(true)
                                .setId(primaryResultDefinition.getId())
                )
        );


        TestDescription testDescription = testMultiVariants(primaryResultDefinition, referenceNow);

        Assert.assertEquals(1, testDescription.outputNows.size());

        Assert.assertEquals(1, testDescription.outputNows.get(0).getMaterial().size());
    }

    @Test
    public void testMultiVariants2Variants3UserGroups() {

        ResultDefinition primaryResultDefinition = ResultDefinition.resultDefinition().setId(randomUUID()).setPrompts(
                asList(
                        Prompt.prompt().setId(randomUUID()).setLabel("label1").setUserGroups(asList(defenceUserGroup, courtClerkUserGroup)),
                        Prompt.prompt().setId(randomUUID()).setLabel("label2").setUserGroups(asList(defenceUserGroup, prosecutionUserGroup, courtClerkUserGroup))
                )
        );

        NowDefinition referenceNow = NowDefinition.now().setId(randomUUID()).setResultDefinitions(
                asList(
                        ResultDefinitions.resultDefinitions()
                                .setPrimaryResult(true)
                                .setMandatory(true)
                                .setId(primaryResultDefinition.getId())
                )
        );

        TestDescription testDescription = testMultiVariants(primaryResultDefinition, referenceNow);

        List<Nows> nowsOut = testDescription.outputNows;
        Assert.assertEquals(1, nowsOut.size());
        List<Material> materials = nowsOut.get(0).getMaterial();
        Assert.assertEquals(2, materials.size());
        Optional<Material> optMaterial = materials.stream().filter(material -> matchesAll(material.getUserGroups(), defenceUserGroup, courtClerkUserGroup)).findAny();
        Assert.assertTrue(optMaterial.isPresent());
        Material material1 = optMaterial.get();
        Assert.assertEquals(1, material1.getNowResult().size());

    }

    boolean matchesAll(List<UserGroups> userGroups, String... userGroups2Match) {
        Set<String> ugSet = new HashSet<>();
        if (userGroups != null) {
            userGroups.forEach(ug -> ugSet.add(ug.getGroup()));
        }
        Set<String> matchSet = Arrays.stream(userGroups2Match).collect(Collectors.toSet());
        return ugSet.equals(matchSet);
    }

    @Test
    public void testMultiVariants_2Variants3UserGroups2ResultLines1Now() {

        List<ResultDefinition> resultDefinitions = asList(
                ResultDefinition.resultDefinition().setId(randomUUID()).setPrompts(
                        asList(
                                Prompt.prompt().setId(randomUUID()).setLabel("label1.1").setUserGroups(asList(defenceUserGroup, courtClerkUserGroup))
                        )
                ),
                ResultDefinition.resultDefinition().setId(randomUUID()).setPrompts(
                        asList(
                                Prompt.prompt().setId(randomUUID()).setLabel("label2.1").setUserGroups(asList(defenceUserGroup, courtClerkUserGroup, prosecutionUserGroup))
                        )
                )
        );

        NowDefinition referenceNow = NowDefinition.now().setId(randomUUID()).setResultDefinitions(
                asList(
                        ResultDefinitions.resultDefinitions().setPrimaryResult(true)
                                .setMandatory(true).setId(resultDefinitions.get(0).getId()),
                        ResultDefinitions.resultDefinitions().setPrimaryResult(false)
                                .setMandatory(true).setId(resultDefinitions.get(1).getId())
                )
        );

        Map<UUID, NowDefinition> resultDefinitionId2Now = new HashMap();
        resultDefinitionId2Now.put(resultDefinitions.get(0).getId(), referenceNow);

        TestDescription testDescription = testMultiVariants(resultDefinitions, resultDefinitionId2Now);

        Assert.assertEquals(1, testDescription.outputNows.size());

        Assert.assertEquals(2, testDescription.outputNows.get(0).getMaterial().size());

    }

    @Test
    public void testMultiVariants_2Variants3UserGroups2ResultLines3Nows() {

        int nowRefsTarget = 3;
        int resultDefinitionsPerNow = 3;
        int promptsPerResultDefinition = 6;

        List<List<String>> variantUserGroupSets = asList(
                asList(defenceUserGroup, courtClerkUserGroup), asList(prosecutionUserGroup), asList(prisonOfficerUserGroup)
        );

        final Map<UUID, NowDefinition> resultDefinitionId2Now = new HashMap();
        List<ResultDefinition> resultDefinitions = new ArrayList<>();

        for (int nowRefsDone = 0; nowRefsDone < nowRefsTarget; nowRefsDone++) {
            List<ResultDefinitions> resultDefinitionRefs = new ArrayList<>();
            NowDefinition referenceNow = NowDefinition.now().setId(randomUUID()).setResultDefinitions(resultDefinitionRefs);
            for (int resultDefinitionsDone = 0; resultDefinitionsDone < resultDefinitionsPerNow; resultDefinitionsDone++) {

                boolean primary = resultDefinitionsDone == 0;
                boolean mandatory = resultDefinitionsDone < 2;
                UUID resultDefinitionId = randomUUID();
                ResultDefinitions resultDefinitionRef = ResultDefinitions.resultDefinitions()
                        .setPrimaryResult(primary).setMandatory(mandatory)
                        .setId(resultDefinitionId);
                resultDefinitionRefs.add(resultDefinitionRef);
                if (primary) {
                    resultDefinitionId2Now.put(resultDefinitionId, referenceNow);
                }
                List<Prompt> prompts = new ArrayList<Prompt>();
                ResultDefinition resultDefinition = ResultDefinition.resultDefinition().setId(resultDefinitionId).setPrompts(prompts);
                for (int promptsDone = 0; promptsDone < promptsPerResultDefinition; promptsDone++) {
                    //make some
                    List<String> ugGroups1 = variantUserGroupSets.get(promptsDone % variantUserGroupSets.size());
                    List<String> ugGroups2 = variantUserGroupSets.get((promptsDone + 1) % variantUserGroupSets.size());
                    List<String> ugSum = new ArrayList<>();
                    ugSum.addAll(ugGroups1);
                    ugSum.addAll(ugGroups2);
                    String label = "" + nowRefsDone + "." + resultDefinitionsDone + "." + promptsDone;
                    Prompt prompt = Prompt.prompt().setId(randomUUID()).setUserGroups(ugSum).setLabel(label);
                    prompts.add(prompt);
                }
                resultDefinitions.add(resultDefinition);
            }
        }

        TestDescription testDescription = testMultiVariants(resultDefinitions, resultDefinitionId2Now);

        Assert.assertEquals(nowRefsTarget, testDescription.outputNows.size());

        testDescription.outputNows.forEach(
                nows -> {
                    Assert.assertEquals(variantUserGroupSets.size(), nows.getMaterial().size());
                }
        );

    }


    TestDescription testMultiVariants(List<ResultDefinition> primaryResultDefinitions, Map<UUID, NowDefinition> resultDefinitionId2ReferenceNow) {

        resultDefinitionId2ReferenceNow.keySet().forEach(
                resultDefinitionId ->
                        Mockito.when(referenceDataService.getNowDefinitionByPrimaryResultDefinitionId(resultDefinitionId)).thenReturn(resultDefinitionId2ReferenceNow.get(resultDefinitionId))
        );
        primaryResultDefinitions.forEach(primaryResultDefinition -> Mockito.when(referenceDataService.getResultDefinitionById(primaryResultDefinition.getId())).thenReturn(primaryResultDefinition));
        //make a result line for each result definition and make a prompt for each contained prompt

        BiFunction<CompletedResultLine, Integer, List<Object>> resultLineFilter = (rlIn, defendantIndex) -> {
            final List<Object> resultLines = new ArrayList<>();
            primaryResultDefinitions.forEach(
                    primaryResultDefinition -> {
                        List<ResultPrompt> resultPrompts =
                                primaryResultDefinition.getPrompts().stream().map(
                                        promptDef -> ResultPrompt.builder().withId(promptDef.getId()).withLabel(promptDef.getLabel())
                                                .withValue(promptDef.getLabel() + "value").build()
                                ).collect(toList());

                        CompletedResultLine resultLine = CompletedResultLine.builder()
                                .withId(randomUUID())
                                .withResultDefinitionId(primaryResultDefinition.getId())
                                .withDefendantId(rlIn.getDefendantId())
                                .withResultPrompts(resultPrompts).build();
                        resultLines.add(resultLine);
                    }
            );
            return resultLines;
        };
        return testMultipleDefendantsDefault1NowPerDefendant(1, resultLineFilter);
        //the non primary non mandatory is not present hence no now
    }

    private TestDescription testMultiVariants(ResultDefinition primaryResultDefinition, NowDefinition referenceNow) {

        Mockito.when(referenceDataService.getNowDefinitionByPrimaryResultDefinitionId(primaryResultDefinition.getId())).thenReturn(referenceNow);
        Mockito.when(referenceDataService.getResultDefinitionById(primaryResultDefinition.getId())).thenReturn(primaryResultDefinition);
        //make a result line for each result definition and make a prompt for each contained prompt

        BiFunction<CompletedResultLine, Integer, List<Object>> resultLineFilter = (rl, defendantIndex) -> {
            List<ResultPrompt> resultPrompts =
                    primaryResultDefinition.getPrompts().stream().map(
                            promptDef -> ResultPrompt.builder().withId(promptDef.getId()).withLabel(promptDef.getLabel())
                                    .withValue(promptDef.getLabel() + "value").build()
                    ).collect(toList());

            CompletedResultLine resultLine = CompletedResultLine.builder()
                    .withId(randomUUID())
                    .withResultDefinitionId(primaryResultDefinition.getId())
                    .withDefendantId(rl.getDefendantId())
                    .withResultPrompts(resultPrompts).build();
            return asList(resultLine);
        };
        return testMultipleDefendantsDefault1NowPerDefendant(1, resultLineFilter);
        //the non primary non mandatory is not present hence no now
    }

    private void checkReferenceData(ResultsShared resultsShared, uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Hearing hearingOut) {
        Hearing hearingIn = resultsShared.getHearing();
        Assert.assertEquals(hearingIn.getId(), hearingOut.getId());

        Assert.assertEquals(hearingIn.getDefendants().size(), hearingOut.getDefendants().size());
        for (int defdone = 0; defdone < hearingIn.getDefendants().size(); defdone++) {
            Defendant defendantIn = hearingIn.getDefendants().get(defdone);
            Defendants defendantOut = hearingOut.getDefendants().get(defdone);
            //check defendant
            Assert.assertEquals(defendantIn.getId(), defendantOut.getId());
            Assert.assertEquals(defendantIn.getDefendantCases().size(), defendantOut.getCases().size());
            for (int done = 0; done < defendantIn.getDefendantCases().size(); done++) {
                DefendantCase defendantInCase = defendantIn.getDefendantCases().get(done);
                Cases defendantOutCase = defendantOut.getCases().get(done);
                Assert.assertEquals(defendantInCase.getCaseId(), defendantOutCase.getId());
            }
            // NOTYET comprehensive person level fields
            Assert.assertEquals(defendantIn.getPersonId(), defendantOut.getPerson().getId());
            Assert.assertEquals(defendantIn.getFirstName(), defendantOut.getPerson().getFirstName());
            Assert.assertEquals(defendantIn.getLastName(), defendantOut.getPerson().getLastName());
            Assert.assertEquals(defendantIn.getNationality(), defendantOut.getPerson().getNationality());
            Assert.assertEquals(defendantIn.getPersonId(), defendantOut.getPerson().getId());
            Assert.assertEquals(defendantIn.getInterpreter().getLanguage(), defendantOut.getInterpreter().getLanguage());

            Address addressIn = defendantIn.getAddress();
            uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Address addressOut = defendantOut.getPerson().getAddress();
            Assert.assertEquals(addressIn.getAddress1(), addressOut.getAddress1());
            Assert.assertEquals(addressIn.getPostCode(), addressOut.getPostCode());

        }

        // assume defenceCounselUpserts dont have an order and dont assume anything about the id
        Map<UUID, DefenceCounselUpsert> attendeeId2defenceCounselsUpsertsIn = new HashMap<>();
        resultsShared.getDefenceCounsels().values().forEach(
                (defenseCounselUpsertIn) -> {
                    attendeeId2defenceCounselsUpsertsIn.put(defenseCounselUpsertIn.getAttendeeId(), defenseCounselUpsertIn);
                }
        );

        Map<UUID, Attendees> attendeeId2DefenceCounsel = new HashMap<>();
        hearingOut.getAttendees().forEach(
                att -> {
                    if (att.getType().equalsIgnoreCase(NowsDataProcessor.DEFENCE_COUNSEL_ATTENDEE_TYPE)) {
                        attendeeId2DefenceCounsel.put(att.getAttendeeId(), att);
                    }
                }
        );

        Assert.assertEquals(attendeeId2defenceCounselsUpsertsIn.keySet(), attendeeId2DefenceCounsel.keySet());
        //NOTYET comprehensive field level checks

        // assume defenceCounselUpserts dont have an order and dont assume anything about the id
        Map<UUID, ProsecutionCounselUpsert> attendeeId2prosecutionCounselsUpsertsIn = new HashMap<>();
        resultsShared.getProsecutionCounsels().values().forEach(
                (prosecutionCounselUpsertIn) -> {
                    attendeeId2prosecutionCounselsUpsertsIn.put(prosecutionCounselUpsertIn.getAttendeeId(), prosecutionCounselUpsertIn);
                }
        );

        Map<UUID, Attendees> attendeeId2ProsecutionCounsel = new HashMap<>();
        hearingOut.getAttendees().forEach(
                att -> {
                    //TODO this type field doesnt appear to be used !
                    if (att.getType().equalsIgnoreCase(NowsDataProcessor.PROSECUTION_COUNSEL_ATTENDEE_TYPE)) {
                        attendeeId2ProsecutionCounsel.put(att.getAttendeeId(), att);
                    }
                }
        );

        Assert.assertEquals(attendeeId2prosecutionCounselsUpsertsIn.keySet(), attendeeId2ProsecutionCounsel.keySet());
        //TODO field level check
    }


    private ResultsShared.Builder createResultsShared(ResultDefinition resultDefinition, int defendantCount, BiFunction<CompletedResultLine, Integer, List<Object>> resultLineFilterByDefendantIndex) {
        if (null == resultLineFilterByDefendantIndex) {
            resultLineFilterByDefendantIndex = (rl, index) -> asList(rl);
        }

        final List<UUID> defendantIds = new ArrayList<>();

        InitiateHearingCommand initiateHearingCommand = with(basicInitiateHearingTemplate(), command -> {
            UUID caseId = randomUUID();

            command.setCases(asList(caseTemplate(caseId)));

            command.getHearing().setDefendants(
                    IntStream.range(0, defendantCount)
                            .boxed()
                            .map(i -> defendantTemplate(caseId))
                            .collect(toList())

            );
        });


        final List<CompletedResultLine> completeResultLines = new ArrayList<>();
        final List<UncompletedResultLine> uncompletedResultLines = new ArrayList<>();

        for (int index = 0; index < defendantCount; index++) {
            Defendant defendant = initiateHearingCommand.getHearing().getDefendants().get(index);

            List<Object> oResultLines = resultLineFilterByDefendantIndex.apply(
                    CompletedResultLine.builder()
                            .withId(randomUUID())
                            .withResultDefinitionId(resultDefinition.getId())
                            .withDefendantId(defendant.getId())
                            .withResultPrompts(
                                    resultDefinition.getPrompts().stream().map(
                                            p -> ResultPrompt.builder()
                                                    .withId(p.getId())
                                                    .withLabel(p.getLabel())
                                                    .withValue("value")
                                                    .build()
                                    ).collect(toList()))
                            .build(), index);
            completeResultLines.addAll(oResultLines.stream().filter(l -> l instanceof CompletedResultLine).map(cl -> (CompletedResultLine) cl).collect(toList()));
            uncompletedResultLines.addAll(oResultLines.stream().filter(l -> l instanceof UncompletedResultLine).map(cl -> (UncompletedResultLine) cl).collect(toList()));
        }

        Map<UUID, CompletedResultLineStatus> completedResultLinesStatus = new HashMap<>();
        completeResultLines.forEach(completedResultLine -> {
            completedResultLinesStatus.put(completedResultLine.getId(), CompletedResultLineStatus.builder()
                    .withId(completedResultLine.getId())
                    .build() );
        });

        return ResultsShared.builder()
                .withHearing(initiateHearingCommand.getHearing())
                .withCompletedResultLines(completeResultLines)
                .withUncompletedResultLines(uncompletedResultLines)
                .withDefenceCounsels(
                        Stream.of(DefenceCounselUpsert.builder()
                                .withAttendeeId(randomUUID())
                                .withDefendantIds(defendantIds).build())
                                .collect(toMap(DefenceCounselUpsert::getAttendeeId, identity()))
                )
                .withProsecutionCounsels(
                        Stream.of(ProsecutionCounselUpsert.builder()
                        .withAttendeeId(randomUUID()).build())
                        .collect(toMap(ProsecutionCounselUpsert::getAttendeeId, identity()))
                )
                .withCompletedResultLinesStatus(completedResultLinesStatus);

    }
}
