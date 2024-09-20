package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withoutJsonPath;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static uk.gov.moj.cpp.hearing.it.UseCases.initiateHearing;
import static uk.gov.moj.cpp.hearing.it.UseCases.removeCaseFromGroupCases;
import static uk.gov.moj.cpp.hearing.it.Utilities.listenFor;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.CoreTemplateArguments.toMap;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.AddProsecutionCounselCommandTemplates.addProsecutionCounselCommandTemplateWithCases;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplateWithGroupProceedings;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_SEC;

import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.ProsecutionCounsel;
import uk.gov.justice.hearing.courts.AddProsecutionCounsel;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.test.TestUtilities;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

public class GroupCasesIT extends AbstractIT {


    @Test
    public void shouldRemoveMemberCaseFromGroupCases() throws Exception {
        final UUID groupId = randomUUID();
        final HashMap<UUID, Map<UUID, List<UUID>>> caseStructure = getUuidMapForCivilCaseStructure(2);
        final Iterator<UUID> iterator = caseStructure.keySet().iterator();
        final UUID masterCaseId = iterator.next();
        final UUID removedCaseId = iterator.next();

        final InitiateHearingCommand hearingCommand = standardInitiateHearingTemplateWithGroupProceedings(caseStructure, groupId, masterCaseId);

        final ProsecutionCase removedCase = ProsecutionCase.prosecutionCase()
                .withValuesFrom(hearingCommand.getHearing().getProsecutionCases().stream().filter(pc -> pc.getId().equals(removedCaseId)).findFirst().get())
                .withIsGroupMember(Boolean.FALSE)
                .withIsGroupMaster(Boolean.FALSE)
                .build();
        hearingCommand.getHearing().getProsecutionCases().removeIf(pc -> !pc.getId().equals(masterCaseId));

        final InitiateHearingCommandHelper hearingCommandHelper = h(initiateHearing(getRequestSpec(), hearingCommand));

        final AddProsecutionCounsel addProsecutionCounsel1 = addProsecutionCounsel(hearingCommandHelper.getHearingId(), asList(masterCaseId));
        final AddProsecutionCounsel addProsecutionCounsel2 = addProsecutionCounsel(hearingCommandHelper.getHearingId(), asList(masterCaseId, removedCaseId));

        final Utilities.EventListener listenEventProsecutionCounselUpdated = listenEventProsecutionCounselUpdated(hearingCommandHelper.getHearingId(),
                addProsecutionCounsel1.getProsecutionCounsel().getId(), asList(masterCaseId.toString(), removedCaseId.toString()));
        final Utilities.EventListener listenEventProsecutionCounselChangeIgnored = listenEventProsecutionCounselChangeIgnored(hearingCommandHelper.getHearingId(),
                addProsecutionCounsel2.getProsecutionCounsel().getId(), asList(masterCaseId.toString(), removedCaseId.toString()));
        final Utilities.EventListener listenEventCasesUpdatedAfterCaseRemoved = listenEventCasesUpdatedAfterCaseRemoved(groupId, removedCaseId, null);

        removeCaseFromGroupCases(groupId, masterCaseId, removedCase, null);

        listenEventProsecutionCounselUpdated.waitFor();
        listenEventProsecutionCounselChangeIgnored.waitFor();
        listenEventCasesUpdatedAfterCaseRemoved.waitFor();
        assertViewStoreUpdated(hearingCommandHelper.getHearingId(), asList(masterCaseId, removedCaseId), masterCaseId);
    }

    @Test
    public void shouldCheckNumberOfGroupCases() throws Exception {
        final UUID groupId = randomUUID();
        final HashMap<UUID, Map<UUID, List<UUID>>> caseStructure = getUuidMapForCivilCaseStructure(2);
        final Iterator<UUID> iterator = caseStructure.keySet().iterator();
        final UUID masterCaseId = iterator.next();
        final UUID removedCaseId = iterator.next();

        final InitiateHearingCommand hearingCommand = standardInitiateHearingTemplateWithGroupProceedings(caseStructure, groupId, masterCaseId);

        final ProsecutionCase removedCase = ProsecutionCase.prosecutionCase()
                .withValuesFrom(hearingCommand.getHearing().getProsecutionCases().stream().filter(pc -> pc.getId().equals(removedCaseId)).findFirst().get())
                .withIsGroupMember(Boolean.FALSE)
                .withIsGroupMaster(Boolean.FALSE)
                .build();
        hearingCommand.getHearing().getProsecutionCases().removeIf(pc -> pc.getId().equals(removedCaseId));

        final InitiateHearingCommandHelper hearingCommandHelper = h(initiateHearing(getRequestSpec(), hearingCommand));

        final AddProsecutionCounsel addProsecutionCounsel1 = addProsecutionCounsel(hearingCommandHelper.getHearingId(), asList(masterCaseId));
        final AddProsecutionCounsel addProsecutionCounsel2 = addProsecutionCounsel(hearingCommandHelper.getHearingId(), asList(masterCaseId, removedCaseId));

        final Utilities.EventListener listenEventProsecutionCounselUpdated = listenEventProsecutionCounselUpdated(hearingCommandHelper.getHearingId(),
                addProsecutionCounsel1.getProsecutionCounsel().getId(), asList(masterCaseId.toString(), removedCaseId.toString()));
        final Utilities.EventListener listenEventProsecutionCounselChangeIgnored = listenEventProsecutionCounselChangeIgnored(hearingCommandHelper.getHearingId(),
                addProsecutionCounsel2.getProsecutionCounsel().getId(), asList(masterCaseId.toString(), removedCaseId.toString()));
        final Utilities.EventListener listenEventCasesUpdatedAfterCaseRemoved = listenEventCasesUpdatedAfterCaseRemoved(groupId, removedCaseId, null);

        removeCaseFromGroupCases(groupId, masterCaseId, removedCase, null);

        listenEventProsecutionCounselUpdated.waitFor();
        listenEventProsecutionCounselChangeIgnored.waitFor();
        listenEventCasesUpdatedAfterCaseRemoved.waitFor();
        assertViewStoreWithNumberOfGroupCasesUpdated(hearingCommandHelper.getHearingId(), asList(masterCaseId, removedCaseId), masterCaseId);
    }

    @Test
    public void shouldRemoveMasterCaseFromGroupCases() throws Exception {
        final UUID groupId = randomUUID();
        final HashMap<UUID, Map<UUID, List<UUID>>> caseStructure = getUuidMapForCivilCaseStructure(3);
        final Iterator<UUID> iterator = caseStructure.keySet().iterator();
        final UUID masterCaseId = iterator.next();
        final UUID newGroupMasterCaseId = iterator.next();
        final UUID anotherCaseId = iterator.next();

        final InitiateHearingCommand hearingCommand = standardInitiateHearingTemplateWithGroupProceedings(caseStructure, groupId, masterCaseId);

        final ProsecutionCase masterCase = ProsecutionCase.prosecutionCase()
                .withValuesFrom(hearingCommand.getHearing().getProsecutionCases().stream().filter(pc -> pc.getId().equals(masterCaseId)).findFirst().get())
                .withIsGroupMember(Boolean.FALSE)
                .withIsGroupMaster(Boolean.FALSE)
                .build();
        final ProsecutionCase newGroupMasterCase = ProsecutionCase.prosecutionCase()
                .withValuesFrom(hearingCommand.getHearing().getProsecutionCases().stream().filter(pc -> pc.getId().equals(newGroupMasterCaseId)).findFirst().get())
                .withIsGroupMember(Boolean.TRUE)
                .withIsGroupMaster(Boolean.TRUE)
                .build();
        final ProsecutionCase anotherCase = ProsecutionCase.prosecutionCase()
                .withValuesFrom(hearingCommand.getHearing().getProsecutionCases().stream().filter(pc -> pc.getId().equals(anotherCaseId)).findFirst().get())
                .withIsGroupMember(Boolean.FALSE)
                .withIsGroupMaster(Boolean.FALSE)
                .build();
        hearingCommand.getHearing().getProsecutionCases().removeIf(pc -> !pc.getId().equals(masterCaseId));

        final InitiateHearingCommandHelper hearingCommandHelper = h(initiateHearing(getRequestSpec(), hearingCommand));

        final AddProsecutionCounsel addProsecutionCounsel = addProsecutionCounsel(hearingCommandHelper.getHearingId(), asList(masterCaseId));

        final Utilities.EventListener listenEventProsecutionCounselUpdated1 = listenEventProsecutionCounselUpdated(hearingCommandHelper.getHearingId(),
                addProsecutionCounsel.getProsecutionCounsel().getId(), asList(masterCaseId.toString(), newGroupMasterCaseId.toString()));
        final Utilities.EventListener listenEventCasesUpdatedAfterCaseRemoved1 = listenEventCasesUpdatedAfterCaseRemoved(groupId, masterCaseId, newGroupMasterCaseId);
        final Utilities.EventListener listenEventMasterCaseUpdatedAfterCaseRemoved1 = listenEventMasterCaseUpdatedForHearing(newGroupMasterCaseId, hearingCommand.getHearing().getId());

        removeCaseFromGroupCases(groupId, masterCaseId, masterCase, newGroupMasterCase);

        listenEventProsecutionCounselUpdated1.waitFor();
        listenEventCasesUpdatedAfterCaseRemoved1.waitFor();
        listenEventMasterCaseUpdatedAfterCaseRemoved1.waitFor();
        assertViewStoreUpdated(hearingCommandHelper.getHearingId(), asList(masterCaseId, newGroupMasterCaseId), newGroupMasterCaseId);

        final Utilities.EventListener listenEventProsecutionCounselUpdated2 = listenEventProsecutionCounselUpdated(hearingCommandHelper.getHearingId(),
                addProsecutionCounsel.getProsecutionCounsel().getId(), asList(masterCaseId.toString(), newGroupMasterCaseId.toString(), anotherCaseId.toString()));
        final Utilities.EventListener listenEventCasesUpdatedAfterCaseRemoved2 = listenEventCasesUpdatedAfterCaseRemoved(groupId, anotherCaseId, null);

        removeCaseFromGroupCases(groupId, newGroupMasterCaseId, anotherCase, null);

        listenEventProsecutionCounselUpdated2.waitFor();
        listenEventCasesUpdatedAfterCaseRemoved2.waitFor();
        assertViewStoreUpdated(hearingCommandHelper.getHearingId(), asList(masterCaseId, newGroupMasterCaseId, anotherCaseId), newGroupMasterCaseId);
    }

    private AddProsecutionCounsel addProsecutionCounsel(final UUID hearingId, final List<UUID> caseIds) {
        final AddProsecutionCounsel addProsecutionCounsel = UseCases.addProsecutionCounsel(getRequestSpec(), hearingId,
                addProsecutionCounselCommandTemplateWithCases(hearingId, caseIds));

        final Utilities.EventListener publicProsecutionCounselAdded = listenFor("public.hearing.prosecution-counsel-added")
                .withFilter(isJson(withJsonPath("$.hearingId", is(hearingId.toString()))))
                .withFilter(isJson(withJsonPath("$.prosecutionCounsel.id", is(addProsecutionCounsel.getProsecutionCounsel().getId().toString()))));

        publicProsecutionCounselAdded.waitFor();

        return addProsecutionCounsel;
    }

    private Utilities.EventListener listenEventProsecutionCounselUpdated(final UUID hearingId, final UUID prosecutionCounselId, final List<String> caseIds) {
        return listenFor("hearing.prosecution-counsel-updated", "hearing.event")
                .withFilter(isJson(withJsonPath("$.hearingId", is(hearingId.toString()))))
                .withFilter(isJson(withJsonPath("$.prosecutionCounsel.id", is(prosecutionCounselId.toString()))))
                .withFilter(isJson(withJsonPath("$.prosecutionCounsel.prosecutionCases", hasSize(caseIds.size()))))
                .withFilter(isJson(withJsonPath("$.prosecutionCounsel.prosecutionCases", hasItems(caseIds.stream().toArray()))));
    }

    private Utilities.EventListener listenEventProsecutionCounselChangeIgnored(final UUID hearingId, final UUID prosecutionCounselId, final List<String> caseIds) {
        return listenFor("hearing.prosecution-counsel-change-ignored", "hearing.event")
                .withFilter(isJson(withJsonPath("$.hearingId", is(hearingId.toString()))))
                .withFilter(isJson(withJsonPath("$.prosecutionCounsel.id", is(prosecutionCounselId.toString()))))
                .withFilter(isJson(withJsonPath("$.prosecutionCounsel.prosecutionCases", hasSize(caseIds.size()))))
                .withFilter(isJson(withJsonPath("$.prosecutionCounsel.prosecutionCases", hasItems(caseIds.stream().toArray()))));
    }

    private Utilities.EventListener listenEventCasesUpdatedAfterCaseRemoved(final UUID groupId, final UUID caseId, final UUID newGroupMaster) {
        if (newGroupMaster == null) {
            return listenFor("hearing.events.cases-updated-after-case-removed-from-group-cases", "hearing.event")
                    .withFilter(isJson(withJsonPath("$.groupId", is(groupId.toString()))))
                    .withFilter(isJson(withJsonPath("$.removedCase.id", is(caseId.toString()))))
                    .withFilter(isJson(withoutJsonPath("$.newGroupMaster")));
        } else {
            return listenFor("hearing.events.cases-updated-after-case-removed-from-group-cases", "hearing.event")
                    .withFilter(isJson(withJsonPath("$.groupId", is(groupId.toString()))))
                    .withFilter(isJson(withJsonPath("$.removedCase.id", is(caseId.toString()))))
                    .withFilter(isJson(withJsonPath("$.newGroupMaster.id", is(newGroupMaster.toString()))));
        }
    }

    private Utilities.EventListener listenEventMasterCaseUpdatedForHearing(final UUID caseId, final UUID hearingId) {
        return listenFor("hearing.events.master-case-updated-for-hearing", "hearing.event")
                .withFilter(isJson(withJsonPath("$.caseId", is(caseId.toString()))))
                .withFilter(isJson(withJsonPath("$.hearingId", is(hearingId.toString()))));
    }

    private void assertViewStoreUpdated(final UUID hearingId, final List<UUID> caseIds, final UUID groupMaster) {
        Queries.getHearingPollForMatch(hearingId, DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getProsecutionCounsels, first(isBean(ProsecutionCounsel.class)
                                .with(ProsecutionCounsel::getProsecutionCases, hasSize(caseIds.size()))
                                .with(ProsecutionCounsel::getProsecutionCases, equalTo(caseIds))))
                        .with(Hearing::getProsecutionCases, hasSize(caseIds.size()))
                        .with(Hearing::getProsecutionCases, hasItem(isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getId, equalTo(groupMaster))
                                .with(ProsecutionCase::getIsGroupMember, equalTo(Boolean.TRUE))
                                .with(ProsecutionCase::getIsGroupMaster, equalTo(Boolean.TRUE))))
                        .with(Hearing::getProsecutionCases, not(hasItems(isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getId, not(equalTo(groupMaster)))
                                .with(ProsecutionCase::getIsGroupMember, equalTo(Boolean.TRUE)))))
                ));
    }

    private void assertViewStoreWithNumberOfGroupCasesUpdated(final UUID hearingId, final List<UUID> caseIds, final UUID groupMaster) {
        Queries.getHearingPollForMatch(hearingId, DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getNumberOfGroupCases, is(100))
                ));
    }

    private HashMap<UUID, Map<UUID, List<UUID>>> getUuidMapForCivilCaseStructure(int count) {
        final HashMap<UUID, Map<UUID, List<UUID>>> caseStructure = new HashMap<>();
        for (int i = 0; i < count; i++) {
            caseStructure.put(randomUUID(), toMap(randomUUID(), TestUtilities.asList(randomUUID())));
        }
        return caseStructure;
    }
}