package uk.gov.moj.cpp.hearing.it;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.apache.commons.collections.ListUtils.unmodifiableList;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.core.courts.IndicatedPleaValue.INDICATED_GUILTY;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.INTEGER;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.it.Utilities.listenFor;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsACourtClerk;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.welshInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.SaveDraftResultsCommandTemplates.saveDraftResultCommandTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.ShareResultsCommandTemplates.basicShareResultsCommandTemplate;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.MapStringToTypeMatcher.convertStringTo;
import static uk.gov.moj.cpp.hearing.utils.ProgressionStub.stubProgressionGenerateNows;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.stubCourtRoomsForWelshValues;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.stubFixedListForWelshValues;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.stubGetReferenceDataCourtRooms;
import static uk.gov.moj.cpp.hearing.utils.StagingEnforcementStub.stubEnforceFinancialImposition;

import uk.gov.justice.core.courts.DelegatedPowers;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.Level;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.Prompt;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.justice.core.courts.Target;
import uk.gov.justice.core.courts.nowdocument.NowDocumentContent;
import uk.gov.justice.core.courts.nowdocument.NowDocumentRequest;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.result.SaveDraftResultCommand;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.AllNows;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.NowDefinition;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.NowResultDefinitionRequirement;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.AllResultDefinitions;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;
import uk.gov.moj.cpp.hearing.it.Utilities.EventListener;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.AllNowsReferenceDataHelper;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.AllResultDefinitionsReferenceDataHelper;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;
import uk.gov.moj.cpp.hearing.test.CoreTestTemplates;
import uk.gov.moj.cpp.hearing.utils.DocumentGeneratorStub;
import uk.gov.moj.cpp.hearing.utils.ReferenceDataStub;
import uk.gov.moj.cpp.hearing.utils.SystemIdMapperStub;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

public class NowDocumentRequestedIT extends AbstractIT {

    protected static final List<String> guiltyResultList = unmodifiableList(
            asList(
                    "fc612b8f-9699-459f-9ea7-b307164e4754",
                    "ce23a452-9015-4619-968f-1628d7a271c9"));
    private static final String DOCUMENT_TEXT = "someDocumentText";
    private static final String BOTH_JURISDICTIONS = "B";
    private static final UUID TRAIL_TYPE_ID_1 = randomUUID();

    @Before
    public void begin() {
        stubProgressionGenerateNows();
        SystemIdMapperStub.stubAddMapping();
        stubEnforceFinancialImposition();
        ReferenceDataStub.stubRelistReferenceDataResults();
    }

    @Test
    public void shouldRequestNowDocument() {

        //Given
        //Hearing Initiated

        final LocalDate orderedDate = PAST_LOCAL_DATE.next();

        final AllNowsReferenceDataHelper allNows = setupNowsReferenceData(orderedDate);
        final UUID guiltyResultDefId = allNows.getFirstPrimaryResultDefinitionId();

        final AllResultDefinitionsReferenceDataHelper refDataHelper1 = setupResultDefinitionsReferenceData(orderedDate, asList(guiltyResultDefId));

        final ResultDefinition guiltyResultDefinition =
                refDataHelper1.it().getResultDefinitions().stream()
                        .filter(rd -> rd.getId().equals(guiltyResultDefId))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("invalid test data")
                        );

        final uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt guiltyResultDefinitionPrompt = guiltyResultDefinition.getPrompts().get(0);

        InitiateHearingCommand initiateHearingCommand = welshInitiateHearingTemplate();

        final List<Offence> offences = getOffences(initiateHearingCommand);
        final UUID offenceId = randomUUID();
        offences.add(Offence.offence()
                .withId(offenceId)
                .withStartDate(PAST_LOCAL_DATE.next())
                .withEndDate(PAST_LOCAL_DATE.next())
                .withArrestDate(PAST_LOCAL_DATE.next())
                .withChargeDate(PAST_LOCAL_DATE.next())
                .withOffenceDefinitionId(randomUUID())
                .withOffenceTitle(STRING.next())
                .withOffenceCode(STRING.next())
                .withOffenceTitleWelsh(STRING.next())
                .withOffenceLegislation(STRING.next())
                .withOffenceLegislationWelsh(STRING.next())
                .withIndicatedPlea(CoreTestTemplates.indicatedPlea(offenceId, INDICATED_GUILTY).build())
                .withNotifiedPlea(CoreTestTemplates.notifiedPlea(offenceId).build())
                .withWording(STRING.next())
                .withCount(INTEGER.next())
                .withWordingWelsh(STRING.next())
                .withModeOfTrial(STRING.next())
                .withOrderIndex(INTEGER.next()).build());

        final InitiateHearingCommandHelper hearingCommandHelper = h(UseCases.initiateHearing(requestSpec, initiateHearingCommand));

        final Hearing hearing = hearingCommandHelper.getHearing();

        stubGetReferenceDataCourtRooms(hearing.getCourtCentre(), hearing.getHearingLanguage());
        stubCourtRoomsForWelshValues(hearing.getCourtCentre().getId());
        stubFixedListForWelshValues();
        stubLjaDetails(hearing.getCourtCentre().getId());

        final SaveDraftResultCommand saveDraftResultCommand = saveDraftResultCommandTemplate(hearingCommandHelper.it(), orderedDate);
        final List<Target> targets = new ArrayList<>();
        targets.add(saveDraftResultCommand.getTarget());
        final ResultLine resultLine = saveDraftResultCommand.getTarget().getResultLines().get(0);
        resultLine.setLevel(Level.OFFENCE);
        resultLine.setResultLineId(UUID.randomUUID());
        resultLine.setResultDefinitionId(guiltyResultDefId);
        resultLine.setOrderedDate(orderedDate);
        resultLine.setPrompts(singletonList(Prompt.prompt()
                .withLabel(guiltyResultDefinitionPrompt.getLabel())
                .withWelshLabel("welshLabel")
                .withFixedListCode("6e5f1afe-e35f-11e8-9f32-f2801f1b9fd1")
                .withValue("value1")
                .withWelshValue("value1")
                .withId(guiltyResultDefinitionPrompt.getId())
                .build()));

        givenAUserHasLoggedInAsACourtClerk(USER_ID_VALUE);
        DocumentGeneratorStub.stubDocumentCreate(DOCUMENT_TEXT);

        final DelegatedPowers courtClerk1 = DelegatedPowers.delegatedPowers()
                .withFirstName("Andrew").withLastName("Eldritch")
                .withUserId(UUID.randomUUID()).build();

        UseCases.shareResults(requestSpec, hearingCommandHelper.getHearingId(), with(
                basicShareResultsCommandTemplate(),
                command -> command.setCourtClerk(courtClerk1)
        ), targets);

        final EventListener publicNowDocumentRequested = listenFor("public.hearing.now-document-requested", 50000)
                .withFilter(convertStringTo(NowDocumentRequest.class, isBean(NowDocumentRequest.class)
                                .with(NowDocumentRequest::getNowContent, isBean(NowDocumentContent.class)
                                        .with(NowDocumentContent::getWelshCourtCentreName, is("Welsh Name"))
                                        .with(NowDocumentContent::getWelshOrderName, is(allNows.getFirstNowDefinition().getWelshName()))
                                        .with(NowDocumentContent::getWelshNowText, is(allNows.getFirstNowDefinition().getWelshText()))
                                        .with(content -> content.getCases().get(0).getDefendantCaseOffences().get(0).getWelshTitle(), is(hearing.getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getOffenceTitleWelsh()))
                                        .with(content -> content.getCases().get(0).getDefendantCaseOffences().get(0).getResults().get(0).getWelshLabel(), is(guiltyResultDefinition.getWelshLabel()))
                                        .with(content -> content.getCases().get(0).getDefendantCaseOffences().get(0).getResults().get(0).getPrompts().get(0).getWelshValue(), is(resultLine.getPrompts().get(0).getWelshValue()))
                                        .with(content -> content.getCases().get(0).getDefendantCaseOffences().get(0).getResults().get(0).getPrompts().get(0).getWelshLabel(), is(resultLine.getPrompts().get(0).getWelshLabel()))
                                )
                        )
                );

        publicNowDocumentRequested.waitFor();
    }

    private List<Offence> getOffences(InitiateHearingCommand initiateHearingCommand) {
        return initiateHearingCommand.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences();
    }

    private AllNowsReferenceDataHelper setupNowsReferenceData(final LocalDate referenceDate) {
        AllNows allnows = AllNows.allNows()
                .setNows(Arrays.asList(NowDefinition.now()
                                .setId(randomUUID())
                                .setResultDefinitions(asList(NowResultDefinitionRequirement.resultDefinitions()
                                                .setId(randomUUID())
                                                .setMandatory(true)
                                                .setWelshText("Welsh Text Primary")
                                                .setPrimary(true),
                                        NowResultDefinitionRequirement.resultDefinitions()
                                                .setId(randomUUID())
                                                // This causes a test failure but this field is under review .setText("ResultDefinitionLevel/" + STRING.next())
                                                .setMandatory(false)
                                                .setPrimary(false)
                                                .setWelshText("Welsh Text Not Primary")
                                ))
                                .setName(STRING.next())
                                .setText("NowLevel/" + STRING.next())
                                .setWelshText("NowLevel/" + STRING.next() + " Welsh")
                                .setWelshName("Welsh Name")
                                .setTemplateName(STRING.next())
                                .setBilingualTemplateName(STRING.next())
                                .setRank(INTEGER.next())
                                .setJurisdiction("B")
                                .setRemotePrintingRequired(false),
                        NowDefinition.now()
                                .setId(randomUUID())
                                .setResultDefinitions(asList(NowResultDefinitionRequirement.resultDefinitions()
                                                .setId(randomUUID())
                                                .setWelshText("Welsh Text Primary")
                                                .setMandatory(true)
                                                .setPrimary(true),
                                        NowResultDefinitionRequirement.resultDefinitions()
                                                .setId(randomUUID())
                                                .setMandatory(false)
                                                .setPrimary(false)
                                                .setWelshText("Welsh Text Not Primary")
                                ))
                                .setName(STRING.next())
                                .setTemplateName(STRING.next())
                                .setBilingualTemplateName(STRING.next())
                                .setRank(INTEGER.next())
                                .setJurisdiction(BOTH_JURISDICTIONS)
                                .setRemotePrintingRequired(false)
                                .setText(STRING.next())
                                .setWelshText("welshText")
                                .setWelshName("welshName")
                ));
        return setupNowsReferenceData(referenceDate, allnows);
    }

    private AllNowsReferenceDataHelper setupNowsReferenceData(final LocalDate referenceDate, final AllNows data) {
        final AllNowsReferenceDataHelper allNows = h(data);
        ReferenceDataStub.stubGetAllNowsMetaData(referenceDate, allNows.it());
        return allNows;
    }

    private AllResultDefinitionsReferenceDataHelper setupResultDefinitionsReferenceData(LocalDate referenceDate, List<UUID> resultDefinitionIds) {
        final String LISTING_OFFICER_USERGROUP = "Listing Officer";

        final AllResultDefinitionsReferenceDataHelper allResultDefinitions = h(AllResultDefinitions.allResultDefinitions()
                .setResultDefinitions(
                        resultDefinitionIds.stream().map(
                                resultDefinitionId ->
                                        ResultDefinition.resultDefinition()
                                                .setId(resultDefinitionId)
                                                .setUserGroups(singletonList(LISTING_OFFICER_USERGROUP))
//                                                .setFinancial("Y")
                                                .setCategory(getCategoryForResultDefinition(resultDefinitionId))
                                                .setPrompts(singletonList(uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt.prompt()
                                                                .setId(randomUUID())
                                                                .setMandatory(true)
                                                                .setLabel(STRING.next())
                                                                .setWelshLabel("welshLabel")
                                                                .setUserGroups(singletonList(LISTING_OFFICER_USERGROUP))
                                                                .setReference(STRING.next())
                                                        )
                                                )
                                                .setRank(INTEGER.next())
                                                .setIsAvailableForCourtExtract(false)
                                                .setLabel(STRING.next())
                                                .setWelshLabel(STRING.next())
                                                .setUserGroups(singletonList(LISTING_OFFICER_USERGROUP))
                        ).collect(Collectors.toList())
                ));

        ReferenceDataStub.stubGetAllResultDefinitions(referenceDate, allResultDefinitions.it());
        return allResultDefinitions;
    }

    private String getCategoryForResultDefinition(final UUID resultDefId) {

        if (guiltyResultList.contains(resultDefId.toString())) {
            return "F";
        }

        return "A";
    }

}