package uk.gov.moj.cpp.hearing.it;

import static java.util.Collections.singletonList;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.it.UseCases.shareResults;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.SaveDraftResultsCommandTemplates.saveDraftResultCommandTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.ShareResultsCommandTemplates.basicShareResultsCommandTemplate;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;
import static uk.gov.moj.cpp.hearing.test.matchers.MapStringToTypeMatcher.convertStringTo;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.stubGetAllResultDefinitions;
import static uk.gov.moj.cpp.hearing.utils.ResultDefinitionUtil.getCategoryForResultDefinition;
import static uk.gov.moj.cpp.hearing.utils.StubNowsReferenceData.setupNowsReferenceData;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.text.MessageFormat.format;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static java.util.concurrent.TimeUnit.SECONDS;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
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
import static uk.gov.moj.cpp.hearing.it.Queries.getHearingsByDatePollForMatch;
import static uk.gov.moj.cpp.hearing.it.Queries.jsonPayloadMatchesBean;
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
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.setupAsMagistrateUser;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_SEC;

import uk.gov.justice.core.courts.DelegatedPowers;
import uk.gov.justice.core.courts.Prompt;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.moj.cpp.hearing.command.result.SaveDraftResultCommand;
import uk.gov.moj.cpp.hearing.domain.event.result.PublicHearingResulted;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.AllResultDefinitions;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;
import uk.gov.moj.cpp.hearing.utils.ReferenceDataStub;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.AllNowsReferenceDataHelper;
import java.time.LocalDate;
import java.util.stream.Collectors;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.JudicialRole;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.ProsecutionCounsel;
import uk.gov.justice.hearing.courts.AddProsecutionCounsel;
import uk.gov.justice.hearing.courts.GetHearings;
import uk.gov.justice.hearing.courts.HearingSummaries;
import uk.gov.moj.cpp.hearing.command.initiate.ExtendHearingCommand;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.it.Utilities.EventListener;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.test.TestUtilities;
import uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher;

import java.time.ZoneId;
import java.time.ZonedDateTime;
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

        // CAD-947: the removal carries no count, so the stored group size is left unchanged
        assertViewStoreWithNumberOfGroupCasesUpdated(hearingCommandHelper.getHearingId(), 100);
    }

    @Test
    public void shouldUpdateNumberOfGroupCasesFromRemovalEvent() throws Exception {
        verifyNumberOfGroupCasesAfterRemoval(2, 2);
    }

    @Test
    public void shouldLeaveNumberOfGroupCasesUnchangedWhenRemovalEventHasNoCount() throws Exception {
        verifyNumberOfGroupCasesAfterRemoval(null, 3);
    }

    @Test
    public void shouldCarryRemainingGroupSizeOnHearingResultedAfterRemoval() throws Exception {
        final LocalDate orderDate = PAST_LOCAL_DATE.next();
        final AllNowsReferenceDataHelper allNows = setupNowsReferenceData(orderDate);

        // progression sends the remaining group size (3 -> 2) with the removal
        final InitiateHearingCommandHelper hearingHelper = verifyNumberOfGroupCasesAfterRemoval(2, 2);

        final SaveDraftResultCommand saveDraftResultCommand = saveDraftResultCommandTemplate(hearingHelper.it(), orderDate, LocalDate.now());
        setResultLine(saveDraftResultCommand.getTarget().getResultLines().get(0),
                findPrompt(setupResultDefinitionsReferenceData(orderDate, singletonList(allNows.getFirstPrimaryResultDefinitionId())),
                        allNows.getFirstPrimaryResultDefinitionId()), allNows.getFirstPrimaryResultDefinitionId(), orderDate);

        // sharing resolves the court centre's LJA and court room from reference data (PublishResultsEventProcessor.setLJADetails)
        final Hearing hearing = hearingHelper.getHearing();
        stubCourtRoom(hearing);
        hearing.getHearingDays().stream()
                .map(HearingDay::getCourtCentreId)
                .map(UUID::toString)
                .forEach(ReferenceDataStub::stubOrganisationUnit);

        // public.hearing.resulted is built from the aggregate's hearing, which next hearings are created from
        try (final EventListener publicEventResulted = listenFor("public.hearing.resulted")
                .withFilter(convertStringTo(PublicHearingResulted.class, isBean(PublicHearingResulted.class)
                        .with(PublicHearingResulted::getHearing, isBean(Hearing.class)
                                .with(Hearing::getId, is(hearingHelper.getHearingId()))
                                .with(Hearing::getNumberOfGroupCases, is(2)))))) {

            shareResults(getRequestSpec(), hearingHelper.getHearingId(), with(
                    basicShareResultsCommandTemplate(),
                    command -> command.setCourtClerk(DelegatedPowers.delegatedPowers()
                            .withFirstName(STRING.next()).withLastName(STRING.next())
                            .withUserId(randomUUID()).build())
            ), singletonList(saveDraftResultCommand.getTarget()));

            publicEventResulted.waitFor();
        }
    }

    private void setResultLine(final ResultLine resultLine, final uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt prompt,
                               final UUID resultDefId, final LocalDate orderDate) {
        resultLine.setResultLineId(randomUUID());
        resultLine.setResultDefinitionId(resultDefId);
        resultLine.setOrderedDate(orderDate);
        resultLine.setPrompts(singletonList(Prompt.prompt()
                .withLabel(prompt.getLabel())
                .withFixedListCode("fixedListCode")
                .withValue("value1")
                .withWelshValue("wvalue1")
                .withId(prompt.getId())
                .build()));
    }

    private uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt findPrompt(
            final CommandHelpers.AllResultDefinitionsReferenceDataHelper refDataHelper, final UUID resultDefId) {
        final ResultDefinition resultDefinition = refDataHelper.it().getResultDefinitions().stream()
                .filter(rd -> rd.getId().equals(resultDefId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("invalid test data"));
        return resultDefinition.getPrompts().get(0);
    }

    private CommandHelpers.AllResultDefinitionsReferenceDataHelper setupResultDefinitionsReferenceData(final LocalDate referenceDate, final List<UUID> resultDefinitionIds) {
        final String listingOfficerUserGroup = "Listing Officer";

        final CommandHelpers.AllResultDefinitionsReferenceDataHelper allResultDefinitions = h(AllResultDefinitions.allResultDefinitions()
                .setResultDefinitions(resultDefinitionIds.stream()
                        .map(resultDefinitionId -> ResultDefinition.resultDefinition()
                                .setId(resultDefinitionId)
                                .setRank(1)
                                .setIsAvailableForCourtExtract(true)
                                .setUserGroups(singletonList(listingOfficerUserGroup))
                                .setFinancial("Y")
                                .setCategory(getCategoryForResultDefinition(resultDefinitionId))
                                .setPrompts(singletonList(uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt.prompt()
                                        .setId(randomUUID())
                                        .setMandatory(true)
                                        .setLabel(STRING.next())
                                        .setWelshLabel(STRING.next())
                                        .setUserGroups(singletonList(listingOfficerUserGroup))
                                        .setReference(STRING.next())))
                                .setLabel(STRING.next())
                                .setWelshLabel(STRING.next())
                                .setUserGroups(singletonList(listingOfficerUserGroup)))
                        .collect(Collectors.toList())));

        stubGetAllResultDefinitions(referenceDate, allResultDefinitions.it());
        return allResultDefinitions;
    }

    private InitiateHearingCommandHelper verifyNumberOfGroupCasesAfterRemoval(final Integer numberOfGroupCasesInEvent, final int expectedNumberOfGroupCases) throws Exception {
        final UUID judiciaryUserId = randomUUID();
        setupAsMagistrateUser(judiciaryUserId);

        final UUID groupId = randomUUID();
        final HashMap<UUID, Map<UUID, List<UUID>>> caseStructure = getUuidMapForCivilCaseStructure(3);
        final Iterator<UUID> iterator = caseStructure.keySet().iterator();
        final UUID masterCaseId = iterator.next();
        final UUID removedCaseId = iterator.next();

        final InitiateHearingCommand hearingCommand = standardInitiateHearingTemplateWithGroupProceedings(caseStructure, groupId, masterCaseId);
        final Hearing hearing = hearingCommand.getHearing();

        final ProsecutionCase removedCase = prosecutionCase()
                .withValuesFrom(hearing.getProsecutionCases().stream().filter(pc -> pc.getId().equals(removedCaseId)).findFirst().get())
                .withIsGroupMember(Boolean.FALSE)
                .withIsGroupMaster(Boolean.FALSE)
                .build();
        // progression initiates group hearings with the master case only, carrying the size of the whole group
        hearing.getProsecutionCases().removeIf(pc -> !pc.getId().equals(masterCaseId));
        hearing.setNumberOfGroupCases(3);
        // sit today with a known judiciary user so that hearing.get.hearings-for-today returns it too
        hearing.setHearingDays(asList(HearingDay.hearingDay()
                .withValuesFrom(hearing.getHearingDays().get(0))
                .withSittingDay(ZonedDateTime.now())
                .build()));
        hearing.setJudiciary(asList(JudicialRole.judicialRole()
                .withValuesFrom(hearing.getJudiciary().get(0))
                .withUserId(judiciaryUserId)
                .build()));

        final InitiateHearingCommandHelper hearingHelper = h(initiateHearing(getRequestSpec(), hearingCommand));
        final UUID hearingId = hearingHelper.getHearingId();
        assertViewStoreWithNumberOfGroupCasesUpdated(hearingId, 3);

        removeCaseFromGroupCases(groupId, masterCaseId, removedCase, null, numberOfGroupCasesInEvent);

        assertViewStoreWithNumberOfGroupCasesUpdated(hearingId, expectedNumberOfGroupCases);

        final String sittingDate = hearing.getHearingDays().get(0).getSittingDay().withZoneSameInstant(ZoneId.of("UTC")).toLocalDate().toString();
        getHearingsByDatePollForMatch(hearing.getCourtCentre().getId(), hearing.getCourtCentre().getRoomId(), sittingDate, "00:00", "23:59",
                isBean(GetHearings.class)
                        .with(GetHearings::getHearingSummaries, hasItem(isBean(HearingSummaries.class)
                                .with(HearingSummaries::getId, is(hearingId))
                                .with(HearingSummaries::getNumberOfGroupCases, is(expectedNumberOfGroupCases)))));

        pollHearingsForToday(judiciaryUserId, isBean(GetHearings.class)
                .with(GetHearings::getHearingSummaries, hasItem(isBean(HearingSummaries.class)
                        .with(HearingSummaries::getId, is(hearingId))
                        .with(HearingSummaries::getNumberOfGroupCases, is(expectedNumberOfGroupCases)))));

        return hearingHelper;
    }

    private void pollHearingsForToday(final UUID userId, final BeanMatcher<GetHearings> resultMatcher) {
        poll(requestParams(getURL("hearing.get.hearings-for-today"), "application/vnd.hearing.get.hearings-for-today+json")
                .withHeader(USER_ID, userId).build())
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, SECONDS)
                .until(
                        status().is(OK),
                        allOf(status().is(OK), jsonPayloadMatchesBean(GetHearings.class, resultMatcher)));
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

        final String timeline = pollForCaseTimeline(removedCaseId, withJsonPath("$.hearingSummaries[*].hearingId", hasItem(hearingId.toString())));
        assertThat(timeline, isJson(withJsonPath("$.hearingSummaries[*].hearingId", hasItem(hearingId.toString()))));
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

        final String timeline = pollForCaseTimeline(removedCaseId, withJsonPath("$.hearingSummaries[*].hearingId", hasItem(hearingId.toString())));
        assertThat(timeline, isJson(withJsonPath("$.hearingSummaries[*].hearingId", hasItem(hearingId.toString()))));
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

    private String pollForCaseTimeline(final UUID caseId, final Matcher<? super ReadContext> timelineMatcher) {
        final String timelineURL = getBaseUri() + "/" + format(ENDPOINT_PROPERTIES.getProperty("hearing.case.timeline"), caseId);
        return poll(requestParams(timelineURL, "application/vnd.hearing.case.timeline+json")
                .withHeader(USER_ID, getLoggedInUser()).build())
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, SECONDS)
                .until(
                        status().is(OK),
                        payload().isJson(timelineMatcher))
                .getPayload();
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

    private void assertViewStoreWithNumberOfGroupCasesUpdated(final UUID hearingId, final int numberOfGroupCases) {
        getHearingPollForMatch(hearingId, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getNumberOfGroupCases, is(numberOfGroupCases))
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