package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.text.MessageFormat.format;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static java.util.concurrent.TimeUnit.SECONDS;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static uk.gov.justice.core.courts.ProsecutionCase.prosecutionCase;
import static uk.gov.justice.services.common.http.HeaderConstants.USER_ID;
import static uk.gov.justice.services.test.utils.core.http.BaseUriProvider.getBaseUri;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.http.RestPoller.poll;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataOf;
import static uk.gov.moj.cpp.hearing.it.Queries.getHearingPollForMatch;
import static uk.gov.moj.cpp.hearing.it.UseCases.initiateHearing;
import static uk.gov.moj.cpp.hearing.it.UseCases.removeCaseFromGroupCases;
import static uk.gov.moj.cpp.hearing.it.Utilities.listenFor;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.CoreTemplateArguments.toMap;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.AddProsecutionCounselCommandTemplates.addProsecutionCounselCommandTemplateWithCases;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplateWithGroupProceedings;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.getPublicTopicInstance;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.sendMessage;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_SEC;

import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.ProsecutionCounsel;
import uk.gov.justice.hearing.courts.AddProsecutionCounsel;
import uk.gov.moj.cpp.hearing.command.initiate.ExtendHearingCommand;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.it.Utilities.EventListener;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.test.TestUtilities;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.jayway.jsonpath.ReadContext;
import org.hamcrest.Matcher;
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

        final ProsecutionCase removedCase = prosecutionCase()
                .withValuesFrom(hearingCommand.getHearing().getProsecutionCases().stream().filter(pc -> pc.getId().equals(removedCaseId)).findFirst().get())
                .withIsGroupMember(Boolean.FALSE)
                .withIsGroupMaster(Boolean.FALSE)
                .build();
        hearingCommand.getHearing().getProsecutionCases().removeIf(pc -> !pc.getId().equals(masterCaseId));

        final InitiateHearingCommandHelper hearingCommandHelper = h(initiateHearing(getRequestSpec(), hearingCommand));

        addProsecutionCounsel(hearingCommandHelper.getHearingId(), asList(masterCaseId));
        addProsecutionCounsel(hearingCommandHelper.getHearingId(), asList(masterCaseId, removedCaseId));

        removeCaseFromGroupCases(groupId, masterCaseId, removedCase, null);

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

        final ProsecutionCase removedCase = prosecutionCase()
                .withValuesFrom(hearingCommand.getHearing().getProsecutionCases().stream().filter(pc -> pc.getId().equals(removedCaseId)).findFirst().get())
                .withIsGroupMember(Boolean.FALSE)
                .withIsGroupMaster(Boolean.FALSE)
                .build();
        hearingCommand.getHearing().getProsecutionCases().removeIf(pc -> pc.getId().equals(removedCaseId));

        final InitiateHearingCommandHelper hearingCommandHelper = h(initiateHearing(getRequestSpec(), hearingCommand));

        addProsecutionCounsel(hearingCommandHelper.getHearingId(), asList(masterCaseId));
        addProsecutionCounsel(hearingCommandHelper.getHearingId(), asList(masterCaseId, removedCaseId));

        removeCaseFromGroupCases(groupId, masterCaseId, removedCase, null);

        assertViewStoreWithNumberOfGroupCasesUpdated(hearingCommandHelper.getHearingId());
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

        final ProsecutionCase masterCase = prosecutionCase()
                .withValuesFrom(hearingCommand.getHearing().getProsecutionCases().stream().filter(pc -> pc.getId().equals(masterCaseId)).findFirst().get())
                .withIsGroupMember(Boolean.FALSE)
                .withIsGroupMaster(Boolean.FALSE)
                .build();
        final ProsecutionCase newGroupMasterCase = prosecutionCase()
                .withValuesFrom(hearingCommand.getHearing().getProsecutionCases().stream().filter(pc -> pc.getId().equals(newGroupMasterCaseId)).findFirst().get())
                .withIsGroupMember(Boolean.TRUE)
                .withIsGroupMaster(Boolean.TRUE)
                .build();
        final ProsecutionCase anotherCase = prosecutionCase()
                .withValuesFrom(hearingCommand.getHearing().getProsecutionCases().stream().filter(pc -> pc.getId().equals(anotherCaseId)).findFirst().get())
                .withIsGroupMember(Boolean.FALSE)
                .withIsGroupMaster(Boolean.FALSE)
                .build();
        hearingCommand.getHearing().getProsecutionCases().removeIf(pc -> !pc.getId().equals(masterCaseId));

        final InitiateHearingCommandHelper hearingCommandHelper = h(initiateHearing(getRequestSpec(), hearingCommand));

        addProsecutionCounsel(hearingCommandHelper.getHearingId(), asList(masterCaseId));

        removeCaseFromGroupCases(groupId, masterCaseId, masterCase, newGroupMasterCase);

        assertViewStoreUpdated(hearingCommandHelper.getHearingId(), asList(masterCaseId, newGroupMasterCaseId), newGroupMasterCaseId);

        removeCaseFromGroupCases(groupId, newGroupMasterCaseId, anotherCase, null);

        assertViewStoreUpdated(hearingCommandHelper.getHearingId(), asList(masterCaseId, newGroupMasterCaseId, anotherCaseId), newGroupMasterCaseId);
    }

    @Test
    public void shouldKeepHearingForMemberCaseRemovedFromGroupCases() throws Exception {
        final UUID groupId = randomUUID();
        final HashMap<UUID, Map<UUID, List<UUID>>> caseStructure = getUuidMapForCivilCaseStructure(2);
        final Iterator<UUID> iterator = caseStructure.keySet().iterator();
        final UUID masterCaseId = iterator.next();
        final UUID removedCaseId = iterator.next();

        final InitiateHearingCommand hearingCommand = standardInitiateHearingTemplateWithGroupProceedings(caseStructure, groupId, masterCaseId);

        final ProsecutionCase removedCase = prosecutionCase()
                .withValuesFrom(hearingCommand.getHearing().getProsecutionCases().stream().filter(pc -> pc.getId().equals(removedCaseId)).findFirst().get())
                .withIsGroupMember(Boolean.FALSE)
                .withIsGroupMaster(Boolean.FALSE)
                .build();
        hearingCommand.getHearing().getProsecutionCases().removeIf(pc -> !pc.getId().equals(masterCaseId));

        final InitiateHearingCommandHelper hearingCommandHelper = h(initiateHearing(getRequestSpec(), hearingCommand));
        final UUID hearingId = hearingCommandHelper.getHearingId();

        removeCaseFromGroupCases(groupId, masterCaseId, removedCase, null);

        getHearingPollForMatch(hearingId, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getProsecutionCases, hasItem(isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getId, equalTo(removedCaseId))
                                .with(ProsecutionCase::getIsGroupMember, equalTo(Boolean.FALSE))
                                .with(ProsecutionCase::getIsGroupMaster, equalTo(Boolean.FALSE))))));

        pollForCaseTimeline(removedCaseId, withJsonPath("$.hearingSummaries[*].hearingId", hasItem(hearingId.toString())));
    }

    @Test
    public void shouldKeepHearingOnTimelineOfMemberCaseRemovedFromGroupExtendedOntoHearing() throws Exception {
        final UUID groupId = randomUUID();
        final HashMap<UUID, Map<UUID, List<UUID>>> caseStructure = getUuidMapForCivilCaseStructure(2);
        final Iterator<UUID> iterator = caseStructure.keySet().iterator();
        final UUID masterCaseId = iterator.next();
        final UUID removedCaseId = iterator.next();

        final List<ProsecutionCase> groupCases = standardInitiateHearingTemplateWithGroupProceedings(caseStructure, groupId, masterCaseId)
                .getHearing().getProsecutionCases();
        final ProsecutionCase masterCase = groupCases.stream().filter(pc -> pc.getId().equals(masterCaseId)).findFirst().get();
        final ProsecutionCase removedCase = prosecutionCase()
                .withValuesFrom(groupCases.stream().filter(pc -> pc.getId().equals(removedCaseId)).findFirst().get())
                .withIsGroupMember(Boolean.FALSE)
                .withIsGroupMaster(Boolean.FALSE)
                .build();

        // the hearing is not initiated as group proceedings; the group master joins it through extension
        final InitiateHearingCommandHelper hearingCommandHelper = h(initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));
        final UUID hearingId = hearingCommandHelper.getHearingId();

        extendHearingWithProsecutionCase(hearingId, masterCase);

        removeCaseFromGroupCases(groupId, masterCaseId, removedCase, null);

        getHearingPollForMatch(hearingId, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getProsecutionCases, hasItem(isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getId, equalTo(removedCaseId))))));

        pollForCaseTimeline(removedCaseId, withJsonPath("$.hearingSummaries[*].hearingId", hasItem(hearingId.toString())));
    }

    private void extendHearingWithProsecutionCase(final UUID hearingId, final ProsecutionCase prosecutionCase) throws Exception {
        final String eventName = "public.progression.events.hearing-extended";
        final ExtendHearingCommand extendHearingCommand = new ExtendHearingCommand();
        extendHearingCommand.setHearingId(hearingId);
        extendHearingCommand.setProsecutionCases(asList(prosecutionCase));

        sendMessage(getPublicTopicInstance().createProducer(),
                eventName,
                Utilities.JsonUtil.objectToJsonObject(extendHearingCommand),
                metadataOf(randomUUID(), eventName).withUserId(randomUUID().toString()).build());

        getHearingPollForMatch(hearingId, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getProsecutionCases, hasItem(isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getId, equalTo(prosecutionCase.getId()))))));
    }

    private void pollForCaseTimeline(final UUID caseId, final Matcher<? super ReadContext> timelineMatcher) {
        final String timelineURL = getBaseUri() + "/" + format(ENDPOINT_PROPERTIES.getProperty("hearing.case.timeline"), caseId);
        poll(requestParams(timelineURL, "application/vnd.hearing.case.timeline+json")
                .withHeader(USER_ID, getLoggedInUser()).build())
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, SECONDS)
                .until(
                        status().is(OK),
                        payload().isJson(timelineMatcher));
    }

    private AddProsecutionCounsel addProsecutionCounsel(final UUID hearingId, final List<UUID> caseIds) {


        final AddProsecutionCounsel addProsecutionCounselTemplate = addProsecutionCounselCommandTemplateWithCases(hearingId, caseIds);
        final AddProsecutionCounsel addProsecutionCounsel;
        try (EventListener publicProsecutionCounselAdded = listenFor("public.hearing.prosecution-counsel-added")
                .withFilter(isJson(withJsonPath("$.hearingId", is(hearingId.toString()))))
                .withFilter(isJson(withJsonPath("$.prosecutionCounsel.id", is(addProsecutionCounselTemplate.getProsecutionCounsel().getId().toString()))))) {

            addProsecutionCounsel = UseCases.addProsecutionCounsel(getRequestSpec(), hearingId,
                    addProsecutionCounselTemplate);

            publicProsecutionCounselAdded.waitFor();
        }

        return addProsecutionCounsel;
    }

    private void assertViewStoreUpdated(final UUID hearingId, final List<UUID> caseIds, final UUID groupMaster) {
        getHearingPollForMatch(hearingId, isBean(HearingDetailsResponse.class)
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

    private void assertViewStoreWithNumberOfGroupCasesUpdated(final UUID hearingId) {
        getHearingPollForMatch(hearingId, isBean(HearingDetailsResponse.class)
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