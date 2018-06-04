package uk.gov.moj.cpp.hearing.event;

import uk.gov.moj.cpp.hearing.command.initiate.Defendant;
import uk.gov.moj.cpp.hearing.command.result.ResultLine;
import uk.gov.moj.cpp.hearing.command.result.ResultPrompt;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Address;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Attendees;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Cases;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Defendants;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Hearing;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Interpreter;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Material;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.NowResult;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Nows;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Person;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.PromptRefs;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.UserGroups;
import uk.gov.moj.cpp.hearing.event.service.ReferenceDataService;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.Now;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.ResultDefinitions;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class NowsDataProcessor {


    private final ReferenceDataService referenceDataService;

    public static final String DEFENCE_COUNSEL_ATTENDEE_TYPE = "DefenseCounsel";
    public static final String PROSECUTION_COUNSEL_ATTENDEE_TYPE = "ProsecutionCounsel";

    @Inject
    public NowsDataProcessor(final ReferenceDataService referenceDataService) {
        this.referenceDataService = referenceDataService;
    }

    public List<Nows> createNows(ResultsShared resultsShared) {
        final List<Nows> nows = new ArrayList<>();
        final uk.gov.moj.cpp.hearing.command.initiate.Hearing hearingIn = resultsShared.getHearing();
        hearingIn.getDefendants().forEach(
                d -> nows.addAll(createNowsForDefendant(resultsShared, d))
        );
        return nows;
    }




    public Hearing translateReferenceData(ResultsShared resultsShared) {
        final uk.gov.moj.cpp.hearing.command.initiate.Hearing hearingIn = resultsShared.getHearing();
        final Hearing hearingOut = Hearing.hearing().setId(hearingIn.getId()).setDefendants(
                hearingIn.getDefendants().stream().map(
                        din -> Defendants.defendants()
                                .setId(din.getId())
                                .setPerson(Person.person()
                                        .setId(din.getPersonId())
                                        .setAddress(
                                        Address.address()
                                        .setAddress1(din.getAddress().getAddress1())
                                        .setPostCode(din.getAddress().getPostCode())))
                                .setCases(
                                        din.getDefendantCases().stream().map(
                                                caseIn -> Cases.cases()
                                                        .setId(caseIn.getCaseId()))
                                                .collect(Collectors.toList()))
                                .setInterpreter(
                                        Optional.of(din).map(Defendant::getInterpreter).map(
                                                i->Interpreter.interpreter().setLanguage(i.getLanguage())).orElse(null)                                )

                )
                        .collect(Collectors.toList())
        );

        hearingOut.setAttendees(new ArrayList<>());

        resultsShared.getDefenceCounsels().forEach(
                (id, defenseCounselUpsert) -> {
                    defenseCounselUpsert.getFirstName();
                    defenseCounselUpsert.getLastName();
                    defenseCounselUpsert.getHearingId();
                    defenseCounselUpsert.getPersonId();
                    defenseCounselUpsert.getStatus();
                    defenseCounselUpsert.getTitle();
                    defenseCounselUpsert.getStatus();
                    hearingOut.getAttendees().add(
                            Attendees.attendees()
                                    .setAttendeeId(defenseCounselUpsert.getAttendeeId())
                                    .setType(DEFENCE_COUNSEL_ATTENDEE_TYPE)
                                    .setFirstName(defenseCounselUpsert.getFirstName())
                                    .setStatus(defenseCounselUpsert.getStatus())
                                    .setTitle(defenseCounselUpsert.getTitle())
                    );
                }
        );

        resultsShared.getProsecutionCounsels().values().forEach(
                prosecutionCounselUpsert ->
                    hearingOut.getAttendees().add(
                            Attendees.attendees()
                                    .setAttendeeId(prosecutionCounselUpsert.getAttendeeId())
                                    .setType(PROSECUTION_COUNSEL_ATTENDEE_TYPE)
                                    .setCases(resultsShared.getCases().stream().map(caseIn ->
                                            Cases.cases().setId(caseIn.getCaseId())).collect(Collectors.toList()))
                                    .setFirstName(prosecutionCounselUpsert.getFirstName())
                                    .setStatus(prosecutionCounselUpsert.getStatus())
                                    .setTitle(prosecutionCounselUpsert.getTitle())
                    )
        );


        return hearingOut;
    }

    //NOTYET check whether there can be multiple incomplete result lines
    private Map<UUID, ResultLine> identifyInCompleteResultLines(final List<ResultLine> resultLines4Defendant) {
        final Map<UUID, ResultLine> resultDefinitionId2IncompleteResultLines = new
                HashMap<>();
        resultLines4Defendant.forEach(
                resultLine -> {
                    if (!Boolean.TRUE.equals(resultLine.isComplete())) {
                        resultDefinitionId2IncompleteResultLines.put(resultLine.getResultDefinitionId(), resultLine);
                    }
                }
        );
        return resultDefinitionId2IncompleteResultLines;
    }


    private Map<UUID, List<ResultLine>> identifyCompleteResultLines(final List<ResultLine> resultLines4Defendant) {
       final Map<UUID, List<ResultLine>> resultDefinitionId2CompleteResultLines = new
               HashMap<>();
        resultLines4Defendant.forEach(
                resultLine -> {
                    if (Boolean.TRUE.equals(resultLine.isComplete())) {
                        if (!resultDefinitionId2CompleteResultLines.containsKey(resultLine.getResultDefinitionId())) {
                            resultDefinitionId2CompleteResultLines.put(resultLine.getResultDefinitionId(), new ArrayList<>());
                        }
                        resultDefinitionId2CompleteResultLines.get(resultLine.getResultDefinitionId()).add(resultLine);
                    }
                }
        );
        return resultDefinitionId2CompleteResultLines;
    }


    private Map<UUID, Now> identifyNowsToGenerate(final List<ResultLine> resultLines4Defendant) {

        final Map<UUID, Now> nowsToGenerate = new HashMap<>();

        resultLines4Defendant.forEach(
                resultLine -> {
                    if (Boolean.TRUE.equals(resultLine.isComplete())) {
                        Now nowsDefinition = referenceDataService.getNowDefinitionByPrimaryResultDefinitionId(resultLine.getResultDefinitionId());
                        if (nowsDefinition != null) {
                            nowsToGenerate.put(nowsDefinition.getId(), nowsDefinition);
                        }
                    }
                }
        );
        return nowsToGenerate;
    }


    private List<Nows> createNowsForDefendant(ResultsShared resultsShared, Defendant defendant) {
        final List<Nows> results = new ArrayList<>();
        final List<ResultLine> resultLines4Defendant = resultsShared.getResultLines().stream().filter(resultLine -> resultLine.getDefendantId().equals(defendant.getId())).collect(Collectors.toList());

        // calculate distinct result definitions from all result lines that are complete
        final Map<UUID, Now> nowsToGenerate = identifyNowsToGenerate(resultLines4Defendant);
        final Map<UUID, List<ResultLine>> resultDefinitionId2CompleteResultLines =
                identifyCompleteResultLines(resultLines4Defendant);
        final Map<UUID, ResultLine> resultDefinitionId2IncompleteResultLine = identifyInCompleteResultLines(resultLines4Defendant);
        boolean abort = false;

        for (Now now : nowsToGenerate.values()) {
            List<ResultLine> resultLines = new ArrayList<>();
            //check that all the mandatories are present for this defendant
            // should have these here
            for (ResultDefinitions resultDefinition : now.getResultDefinitions()) {
                //check that all the mandatories are present for this defendant
                if (resultDefinition.getMandatory() && !resultDefinitionId2CompleteResultLines.containsKey(resultDefinition.getId())) {
                    abort = true;
                    //NOTYET collate reasons for failure
                }
                // are there any result definitions that have incomplete resultdefinitions ?
                else if (resultDefinitionId2IncompleteResultLine.containsKey(resultDefinition.getId())) {
                    abort = true;
                    //NOTYET collate reasons for failure
                } else {
                    resultLines.addAll(resultDefinitionId2CompleteResultLines.get(resultDefinition.getId()));
                }
            }
            long resultLinesChangedCount = resultLines.stream().filter(resultLine -> resultLine.getLastSharedResultId() == null).count();
            if (resultLinesChangedCount == 0) {
                abort = true;
            }
            if (abort) {
                //NOTYET - notify user of abort due to missing mandatory
                return new ArrayList<>();
            }
            results.add(createNowInstance(now, resultLines, defendant.getId()));

        }
        return results;
    }

    private void parseNowForUserGroup(final String userGroup, final List<ResultLine> resultLines,
                                 final Map<Set<String>, List<UserGroups>> fullPromptIds2UserGroups,
                                 final Map<Set<String>, Material> fullPromptIds2Material) {
        final Set<String> fullPromptIds = new HashSet<>();
        final Set<ResultPrompt> resultPrompts = new HashSet<>();
        final Material material = Material.material().setNowResult(new ArrayList<>());
        resultLines.forEach(
                resultLine -> {
                    ResultDefinition resultDefinition = referenceDataService.getResultDefinitionById(resultLine.getResultDefinitionId());
                    NowResult nowResult = NowResult.nowResult().setSharedResultId(resultLine.getId());
                    //NOTYET is rank the same as sequence
                    nowResult.setSequence(resultDefinition.getRank());
                    nowResult.setPromptRefs(new ArrayList<>());
                    for (ResultPrompt resultPrompt : resultLine.getPrompts()) {
                        //look up reference data
                        Prompt prompt = getPromptByLabel(resultDefinition, resultPrompt.getLabel());
                        boolean isUserGroupMatch = prompt != null && promptUserGroupMatch(prompt, userGroup);
                        //map this to the result definition Id
                        if (isUserGroupMatch) {
                            // no prompt (value) id is prompt is
                            String fullPromptId = resultLine.getId() + "/" + prompt.getLabel();
                            fullPromptIds.add(fullPromptId);
                            resultPrompts.add(resultPrompt);
                            PromptRefs promptRefs = PromptRefs.promptRefs().setLabel(prompt.getLabel());
                            nowResult.getPromptRefs().add(promptRefs);
                        }
                    }
                    if (!nowResult.getPromptRefs().isEmpty()) {
                        material.getNowResult().add(nowResult);
                    }
                }
        );
        if (!fullPromptIds2UserGroups.containsKey(fullPromptIds)) {
            fullPromptIds2UserGroups.put(fullPromptIds, new ArrayList<>());
            fullPromptIds2Material.put(fullPromptIds, material);
        }
        fullPromptIds2UserGroups.get(fullPromptIds).add(UserGroups.userGroups().setGroup(userGroup));
    }

    private Set<String> extractUserGroups(final List<ResultLine> resultLines) {
        final Set<String> userGroups = new HashSet<>();
        resultLines.stream().forEach(
                resultLine -> {
                    ResultDefinition resultDefinition = referenceDataService.getResultDefinitionById(resultLine.getResultDefinitionId());
                    if (resultDefinition.getUserGroups() != null) {
                        resultDefinition.getUserGroups().forEach(ug->userGroups.add(ug));
                    }
                    if (resultDefinition.getPrompts() != null) {
                        resultDefinition.getPrompts().forEach(
                                prompt -> {
                                    if (prompt.getUserGroups() != null) {
                                        for (String userGroup : prompt.getUserGroups()) {
                                            userGroups.add(userGroup);
                                        }
                                    }
                                }
                        );
                    }
                }
        );
        return userGroups;
    }



    private Nows createNowInstance(final Now now, final List<ResultLine> resultLines, final UUID defendantId) {
        //calculate the variants
        //calculate the set of user groups to consider
        //NOTYET could examine actual results not reference data to deterrmine this
        final Set<String> userGroups = extractUserGroups(resultLines);

        // calculate the variants - a variant being a set of distinct prompts
        // createNows a map from the set of included result prompt ids sets to use groups
        final Map<Set<String>, List<UserGroups>> fullPromptIds2UserGroups = new HashMap<>();
        final Map<Set<String>, Material> fullPromptIds2Material = new HashMap<>();
        for (String userGroup : userGroups) {
            parseNowForUserGroup(userGroup, resultLines, fullPromptIds2UserGroups, fullPromptIds2Material);
        }

        final Nows result = Nows.nows()
                .setDefendantId(defendantId.toString())
                .setNowsTypeId(now.getId().toString());

        result.setMaterial(new ArrayList<>());
        //for each variant add a now
        for (Map.Entry<Set<String>, List<UserGroups>> entry : fullPromptIds2UserGroups.entrySet()) {
            List<UserGroups> userGroupsForMaterial = fullPromptIds2UserGroups.get(entry.getKey());
            Material material = fullPromptIds2Material.get(entry.getKey());
            material.setUserGroups(userGroupsForMaterial);
            result.getMaterial().add(material);
        }

        return result;
    }

    private boolean promptUserGroupMatch(final Prompt prompt, final String userGroup) {

        boolean result = false;
        if (prompt != null && prompt.getUserGroups() != null && userGroup != null) {
            for (String ug : prompt.getUserGroups()) {
                if (ug.equalsIgnoreCase(userGroup)) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    private Prompt getPromptByLabel(final ResultDefinition resultDefinition, final String label) {
        for (Prompt prompt : resultDefinition.getPrompts()) {
            if (label.equalsIgnoreCase(prompt.getLabel())) {
                return prompt;
            }
        }
        return null;
    }


}
