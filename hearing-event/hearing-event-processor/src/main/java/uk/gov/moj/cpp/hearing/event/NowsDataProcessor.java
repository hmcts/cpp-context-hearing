package uk.gov.moj.cpp.hearing.event;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.initiate.Defendant;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLine;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLineStatus;
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
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.NowDefinition;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.ResultDefinitions;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;
import uk.gov.moj.cpp.hearing.event.service.ReferenceDataService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@SuppressWarnings("squid:S1188")
public class NowsDataProcessor {

    private final ReferenceDataService referenceDataService;

    public static final String DEFENCE_COUNSEL_ATTENDEE_TYPE = "DefenseCounsel";
    public static final String PROSECUTION_COUNSEL_ATTENDEE_TYPE = "ProsecutionCounsel";

    public void setContext(JsonEnvelope context) {
        referenceDataService.setContext(context);
    }

    @Inject
    public NowsDataProcessor(final ReferenceDataService referenceDataService) {
        this.referenceDataService = referenceDataService;
    }

    public List<Nows> createNows(ResultsShared resultsShared) {
        final List<Nows> nows = new ArrayList<>();

        resultsShared.getHearing().getDefendants().forEach(defendant -> {

                    if (anyUncompletedResultLinesForDefendant(resultsShared, defendant)) {
                        return; //we don't generate any NOW for the defendant if they have any uncompleted result lines.
                    }

                    final List<CompletedResultLine> completedResultLines4Defendant = resultsShared.getCompletedResultLines().stream()
                            .filter(resultLine -> resultLine.getDefendantId().equals(defendant.getId()))
                            .collect(toList());

                    nows.addAll(createNowsForDefendant(defendant, completedResultLines4Defendant, resultsShared.getCompletedResultLinesStatus()));
                }
        );

        return nows;
    }

    public Hearing translateReferenceData(ResultsShared resultsShared) {

        List<Attendees> attendees = new ArrayList<>();

        resultsShared.getDefenceCounsels().forEach((id, defenseCounsel) ->
                attendees.add(
                        Attendees.attendees()
                                .setAttendeeId(defenseCounsel.getAttendeeId())
                                .setType(DEFENCE_COUNSEL_ATTENDEE_TYPE)
                                .setFirstName(defenseCounsel.getFirstName())
                                .setStatus(defenseCounsel.getStatus())
                                .setTitle(defenseCounsel.getTitle())
                )

        );

        resultsShared.getProsecutionCounsels().forEach((id, prosecutionCounsel) ->
                attendees.add(
                        Attendees.attendees()
                                .setAttendeeId(prosecutionCounsel.getAttendeeId())
                                .setType(PROSECUTION_COUNSEL_ATTENDEE_TYPE)
                                .setCases(resultsShared.getCases().stream()
                                        .map(caseIn -> Cases.cases().setId(caseIn.getCaseId())).collect(toList())
                                )
                                .setFirstName(prosecutionCounsel.getFirstName())
                                .setStatus(prosecutionCounsel.getStatus())
                                .setTitle(prosecutionCounsel.getTitle())
                )
        );

        return Hearing.hearing()
                .setId(resultsShared.getHearing().getId())
                .setDefendants(
                        resultsShared.getHearing().getDefendants().stream()
                                .map(defendant -> Defendants.defendants()
                                        .setId(defendant.getId())
                                        .setPerson(Person.person()
                                                .setId(defendant.getPersonId())
                                                .setFirstName(defendant.getFirstName())
                                                .setLastName(defendant.getLastName())
                                                .setDateOfBirth(defendant.getDateOfBirth().toString())
                                                .setNationality(defendant.getNationality())
                                                .setGender(defendant.getGender())
                                                .setAddress(
                                                        Address.address()
                                                                .setAddress1(defendant.getAddress().getAddress1())
                                                                .setPostCode(defendant.getAddress().getPostCode())))
                                        .setCases(
                                                defendant.getDefendantCases().stream().map(
                                                        caseIn -> Cases.cases()
                                                                .setId(caseIn.getCaseId()))
                                                        .collect(toList()))
                                        .setInterpreter(
                                                Optional.of(defendant).map(Defendant::getInterpreter).map(
                                                        i -> Interpreter.interpreter().setLanguage(i.getLanguage())).orElse(null))

                                )
                                .collect(toList())
                )
                .setAttendees(attendees);
    }

    public Set<NowDefinition> findNowDefinitions(final List<CompletedResultLine> resultLines) {
        return resultLines.stream()
                .map(resultLine -> referenceDataService.getNowDefinitionByPrimaryResultDefinitionId(resultLine.getResultDefinitionId()))
                .filter(Objects::nonNull)
                .collect(toSet());
    }

    private List<Nows> createNowsForDefendant(Defendant defendant, List<CompletedResultLine> resultLines, Map<UUID, CompletedResultLineStatus> completedResultLinesStatus) {

        final Set<UUID> completedResultDefinitionIds = resultLines.stream()
                .map(CompletedResultLine::getResultDefinitionId)
                .collect(toSet());

        final List<Nows> results = new ArrayList<>();

        for (NowDefinition nowDefinition : findNowDefinitions(resultLines)) {

            if (anyMandatoryResultLineNotPresent(completedResultDefinitionIds, nowDefinition)) {
                return Collections.emptyList(); //This will suspend any now generation for the defendant.
            }

            final Set<UUID> resultDefinitionIds4Now = nowDefinition.getResultDefinitions().stream()
                    .map(ResultDefinitions::getId)
                    .collect(toSet());

            final List<CompletedResultLine> resultLines4Now = resultLines.stream()
                    .filter(l -> resultDefinitionIds4Now.contains(l.getResultDefinitionId()))
                    .collect(toList());

            if (!anyNewlyCompletedResultLines(resultLines4Now, completedResultLinesStatus)) {
                continue; //skip generation of the current NOW since there are no new result lines for it.
            }

            results.add(createNow(nowDefinition, resultLines4Now, defendant.getId()));
        }
        return results;
    }

    private Nows createNow(final NowDefinition nowDefinition, final List<CompletedResultLine> resultLines4Now, final UUID defendantId) {

        final Map<NowVariant, List<String>> variantToUserGroupsMappings = calculateVariants(resultLines4Now);

        final List<Material> materials = new ArrayList<>();

        variantToUserGroupsMappings.forEach((variant, userGroups) -> {

            //The userGroups of the prompts are not bounded by the userGroups of the resultDefinition.  I'm told that the userGroups
            //of the resultDefinition is more of a default for when no prompts are present.  So if a userGroup has a prompt,
            //it has the enclosing resultDefinition too.

            final Set<CompletedResultLine> resultLines4Variant = Stream.concat(
                    resultLines4Now.stream()
                            .filter(resultLine -> variant.getResultDefinitionsIds().contains(resultLine.getResultDefinitionId())),
                    resultLines4Now.stream()
                            .filter(resultLine -> resultLine.getPrompts().stream().anyMatch(prompt -> variant.getResultPromptIds().contains(prompt.getId())))
            ).collect(toSet());

            final List<NowResult> nowResults = resultLines4Variant.stream()
                    .map(resultLine -> {
                        final ResultDefinition resultDefinition = referenceDataService.getResultDefinitionById(resultLine.getResultDefinitionId());

                        final List<PromptRefs> promptRefs = resultDefinition.getPrompts().stream()
                                .filter(prompt -> variant.getResultPromptIds().contains(prompt.getId())) //filter out prompts that this variant should not have.
                                .map(prompt -> PromptRefs.promptRefs().setLabel(prompt.getLabel()))
                                .collect(toList());

                        return NowResult.nowResult()
                                .setSharedResultId(resultLine.getId())
                                .setSequence(resultDefinition.getRank())
                                .setPromptRefs(promptRefs);
                    })
                    .collect(toList());

            materials.add(Material.material()
                    .setId(UUID.randomUUID())
                    .setNowResult(nowResults)
                    .setUserGroups(userGroups.stream()
                            .map(userGroup -> UserGroups.userGroups().setGroup(userGroup))
                            .collect(toList())
                    ));
        });

        return Nows.nows()
                .setId(UUID.randomUUID())
                .setDefendantId(defendantId.toString())
                .setNowsTypeId(nowDefinition.getId().toString())
                .setMaterial(materials);
    }

    private Map<NowVariant, List<String>> calculateVariants(final List<CompletedResultLine> resultLines4Now) {
        final Map<NowVariant, List<String>> variantToUserGroupsMappings = new HashMap<>();

        for (final String userGroup : extractUserGroupsFromResultLinesAndPrompts(resultLines4Now)) {

            final Set<UUID> resultDefinitionsIds4UserGroup = resultLines4Now.stream()
                    .map(resultLine -> referenceDataService.getResultDefinitionById(resultLine.getResultDefinitionId()))
                    .filter(resultDefinition -> resultDefinition.getUserGroups().stream().anyMatch(ug -> ug.equals(userGroup)))
                    .map(ResultDefinition::getId)
                    .collect(toSet());

            final Set<UUID> resultPromptIds4UserGroup = resultLines4Now.stream()
                    .map(resultLine -> referenceDataService.getResultDefinitionById(resultLine.getResultDefinitionId()))
                    .flatMap(resultDefinition -> resultDefinition.getPrompts().stream())
                    .filter(resultPrompt -> resultPrompt.getUserGroups().stream().anyMatch(ug -> ug.equals(userGroup)))
                    .map(Prompt::getId)
                    .collect(toSet());

            variantToUserGroupsMappings.computeIfAbsent(new NowVariant(resultDefinitionsIds4UserGroup, resultPromptIds4UserGroup), v -> new ArrayList<>())
                    .add(userGroup);
        }
        return variantToUserGroupsMappings;
    }

    private Set<String> extractUserGroupsFromResultLinesAndPrompts(final List<CompletedResultLine> resultLines) {

        return resultLines.stream()
                .flatMap(resultLine -> {
                            ResultDefinition resultDefinition = referenceDataService.getResultDefinitionById(resultLine.getResultDefinitionId());

                            return Stream.concat(
                                    resultDefinition.getUserGroups().stream(),
                                    resultDefinition.getPrompts().stream().flatMap(prompt -> prompt.getUserGroups().stream())

                            );
                        }
                ).collect(toSet());
    }

    private static boolean anyUncompletedResultLinesForDefendant(final ResultsShared resultsShared, final Defendant defendant) {
        return resultsShared.getUncompletedResultLines().stream().anyMatch(l -> l.getDefendantId().equals(defendant.getId()));
    }

    private static boolean anyMandatoryResultLineNotPresent(final Set<UUID> completedResultDefinitionIds, final NowDefinition nowDefinition) {
        return nowDefinition.getResultDefinitions().stream().anyMatch(resultDefinitions -> resultDefinitions.getMandatory() &&
                !completedResultDefinitionIds.contains(resultDefinitions.getId()));
    }

    private static boolean anyNewlyCompletedResultLines(final List<CompletedResultLine> resultLines4Now, Map<UUID, CompletedResultLineStatus> completedResultLinesStatus) {
        // this line tests if we have new result lines, if we don't we don't generate the NOW
        // check if result line's last sharedDateTime is null then only generate NOW
        return resultLines4Now.stream().anyMatch(resultLine ->
                !ofNullable(completedResultLinesStatus.get(resultLine.getId()))
                        .map(CompletedResultLineStatus::getLastSharedDateTime)
                        .isPresent()
        );
    }

    private static class NowVariant {
        private final Set<UUID> resultDefinitionsIds;
        private final Set<UUID> resultPromptIds;

        NowVariant(final Set<UUID> resultDefinitionsIds, final Set<UUID> resultPromptIds) {
            this.resultDefinitionsIds = resultDefinitionsIds;
            this.resultPromptIds = resultPromptIds;
        }

        Set<UUID> getResultDefinitionsIds() {
            return resultDefinitionsIds;
        }

        Set<UUID> getResultPromptIds() {
            return resultPromptIds;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            NowVariant that = (NowVariant) o;
            return Objects.equals(resultDefinitionsIds, that.resultDefinitionsIds) &&
                    Objects.equals(resultPromptIds, that.resultPromptIds);
        }

        @Override
        public int hashCode() {
            return Objects.hash(resultDefinitionsIds, resultPromptIds);
        }
    }

}