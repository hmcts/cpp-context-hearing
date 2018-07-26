package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.http.RestPoller.poll;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.values;
import static uk.gov.moj.cpp.hearing.it.TestUtilities.listenFor;
import static uk.gov.moj.cpp.hearing.it.TestUtilities.makeCommand;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsACourtClerk;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.ShareResultsCommandTemplates.basicShareResultsCommandTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.ShareResultsCommandTemplates.resultPromptTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.ShareResultsCommandTemplates.standardShareResultsCommandTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.saveDraftResultCommandTemplate;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.second;
import static uk.gov.moj.cpp.hearing.test.matchers.MapStringToTypeMatcher.convertStringTo;

import uk.gov.moj.cpp.hearing.command.result.CompletedResultLine;
import uk.gov.moj.cpp.hearing.command.result.Level;
import uk.gov.moj.cpp.hearing.command.result.SaveDraftResultCommand;
import uk.gov.moj.cpp.hearing.command.result.ShareResultsCommand;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.AllNows;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.NowDefinition;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.ResultDefinitions;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.AllResultDefinitions;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;
import uk.gov.moj.cpp.hearing.it.TestUtilities.EventListener;
import uk.gov.moj.cpp.hearing.message.shareResults.Case;
import uk.gov.moj.cpp.hearing.message.shareResults.Defendant;
import uk.gov.moj.cpp.hearing.message.shareResults.Hearing;
import uk.gov.moj.cpp.hearing.message.shareResults.Offence;
import uk.gov.moj.cpp.hearing.message.shareResults.ShareResultsMessage;
import uk.gov.moj.cpp.hearing.message.shareResults.SharedResultLine;
import uk.gov.moj.cpp.hearing.message.shareResults.Variant;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.AllNowsReferenceDataHelper;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.AllResultDefinitionsReferenceDataHelper;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;
import uk.gov.moj.cpp.hearing.utils.DocumentGeneratorStub;
import uk.gov.moj.cpp.hearing.utils.ReferenceDataStub;

import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("unchecked")
public class ShareResultsIT extends AbstractIT {

    public static final String DOCUMENT_TEXT = "someDocumentText";

    @Before
    public void begin() {
        ReferenceDataStub.stubRelistReferenceDataResults();
    }

    @Test
    public void shouldRaiseDraftResultSaved() {

        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(requestSpec, standardInitiateHearingTemplate()));

        final SaveDraftResultCommand saveDraftResultCommand = with(saveDraftResultCommandTemplate(hearingOne.it()),
                template -> template.setHearingId(hearingOne.getHearingId()));

        givenAUserHasLoggedInAsACourtClerk(USER_ID_VALUE);

        final EventListener publicEventResulted = listenFor("public.hearing.draft-result-saved")
                .withFilter(isJson(allOf(
                        withJsonPath("$._metadata.name", is("public.hearing.draft-result-saved")),
                        withJsonPath("$._metadata.context.user", is(USER_ID_VALUE.toString())),
                        withJsonPath("$.hearingId", is(saveDraftResultCommand.getHearingId().toString())),
                        withJsonPath("$.defendantId", is(saveDraftResultCommand.getDefendantId().toString())),
                        withJsonPath("$.targetId", is(saveDraftResultCommand.getTargetId().toString())),
                        withJsonPath("$.offenceId", is(saveDraftResultCommand.getOffenceId().toString())),
                        withJsonPath("$.draftResult", is(saveDraftResultCommand.getDraftResult()))
                )));

        makeCommand(requestSpec, "hearing.save-draft-result")
                .ofType("application/vnd.hearing.save-draft-result+json")
                .withArgs(saveDraftResultCommand.getHearingId())
                .withPayload(saveDraftResultCommand)
                .executeSuccessfully();

        publicEventResulted.waitFor();
    }

    @Test
    public void shareResults_shouldPersistNows() {

        final LocalDate orderedDate = PAST_LOCAL_DATE.next();
        final AllNowsReferenceDataHelper allNows = setupNowsReferenceData(orderedDate);
        final AllResultDefinitionsReferenceDataHelper allResultDefinitions = setupResultDefinitionsReferenceData(orderedDate, allNows.getFirstNowDefinitionFirstResultDefinitionId());

        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(requestSpec, standardInitiateHearingTemplate()));

        givenAUserHasLoggedInAsACourtClerk(USER_ID_VALUE);

        final UUID resultLineId1 = randomUUID();
        UseCases.shareResults(requestSpec, hearingOne.getHearingId(), with(
                basicShareResultsCommandTemplate(),
                command -> command.setCompletedResultLines(singletonList(
                        CompletedResultLine.builder()
                                .withId(resultLineId1)
                                .withResultDefinitionId(allNows.getFirstNowDefinitionFirstResultDefinitionId())
                                .withDefendantId(hearingOne.getFirstDefendantId())
                                .withOffenceId(hearingOne.getFirstOffenceIdForFirstDefendant())
                                .withOrderedDate(orderedDate)
                                .withCaseId(hearingOne.getFirstCaseId())
                                .withLevel(values(Level.values()).next())
                                .withResultLabel(STRING.next())
                                .withResultPrompts(singletonList(resultPromptTemplate(
                                        allResultDefinitions.getFirstResultDefinitionFirstResultPrompt().getId(),
                                        allResultDefinitions.getFirstResultDefinitionFirstResultPrompt().getLabel(),
                                        STRING.next()
                                )))
                                .build()
                ))));

        poll(requestParams(getURL("hearing.get.nows", hearingOne.getHearingId().toString()), "application/vnd.hearing.get.nows+json")
                .withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .timeout(30, TimeUnit.SECONDS)
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.nows[0].defendantId", Is.is(hearingOne.getFirstDefendant().getId().toString()))
                        )));

    }

    @Test
    public void shareResults_shouldPublishResults_andVariantsShouldBeDrivenFromCompletedResultLines() {

        final LocalDate orderedDate = PAST_LOCAL_DATE.next();
        final AllNowsReferenceDataHelper allNows = setupNowsReferenceData(orderedDate);
        final AllResultDefinitionsReferenceDataHelper allResultDefinitions = setupResultDefinitionsReferenceData(orderedDate, allNows.getFirstNowDefinitionFirstResultDefinitionId());

        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(requestSpec, standardInitiateHearingTemplate()));

        givenAUserHasLoggedInAsACourtClerk(USER_ID_VALUE);
        DocumentGeneratorStub.stubDocumentCreate(DOCUMENT_TEXT);

        final EventListener publicEventResulted = listenFor("public.hearing.resulted")
                .withFilter(convertStringTo(ShareResultsMessage.class, isBean(ShareResultsMessage.class)
                        .with(ShareResultsMessage::getHearing, isBean(Hearing.class)
                                .with(Hearing::getId, is(hearingOne.getHearingId()))
                                .with(Hearing::getDefendants, first(isBean(Defendant.class)
                                        .with(Defendant::getId, is(hearingOne.getFirstDefendantId()))
                                        .with(Defendant::getCases, first(isBean(Case.class)
                                                .with(Case::getId, is(hearingOne.getFirstCaseId()))
                                                .with(Case::getOffences, first(isBean(Offence.class)
                                                        .with(Offence::getId, is(hearingOne.getFirstOffenceIdForFirstDefendant()))
                                                ))
                                        ))
                                ))
                                .with(Hearing::getSharedResultLines, first(isBean(SharedResultLine.class)
                                        .with(SharedResultLine::getOrderedDate, is(orderedDate))
                                ))
                        )
                        .with(ShareResultsMessage::getVariants, first(isBean(Variant.class)
                                .with(Variant::getTemplateName, is(allNows.getFirstNowDefinition().getTemplateName()))
                                .with(Variant::getDescription, is(allNows.getFirstNowDefinition().getName()))
                                .with(Variant::getStatus, anyOf(is("BUILDING"), is("GENERATED")))
                        ))

                ));

        ShareResultsCommand shareResultsCommand = UseCases.shareResults(requestSpec, hearingOne.getHearingId(), with(
                basicShareResultsCommandTemplate(),
                command -> command.setCompletedResultLines(singletonList(
                        //a completed result line should result in a material/variant being created
                        CompletedResultLine.builder()
                                .withId(randomUUID())
                                .withResultDefinitionId(allNows.getFirstNowDefinitionFirstResultDefinitionId())
                                .withDefendantId(hearingOne.getFirstDefendantId())
                                .withOffenceId(hearingOne.getFirstOffenceIdForFirstDefendant())
                                .withOrderedDate(orderedDate)
                                .withCaseId(hearingOne.getFirstCaseId())
                                .withLevel(values(Level.values()).next())
                                .withResultLabel(STRING.next())
                                .withResultPrompts(singletonList(resultPromptTemplate(
                                        allResultDefinitions.getFirstResultDefinitionFirstResultPrompt().getId(),
                                        allResultDefinitions.getFirstResultDefinitionFirstResultPrompt().getLabel(),
                                        STRING.next()
                                )))
                                .build()
                ))));

        publicEventResulted.waitFor();

        final LocalDate orderedDate2 = PAST_LOCAL_DATE.next();
        final AllNowsReferenceDataHelper allNows2 = setupNowsReferenceData(orderedDate2);
        final AllResultDefinitionsReferenceDataHelper allResultDefinitions2 = setupResultDefinitionsReferenceData(orderedDate2, allNows2.getFirstNowDefinitionFirstResultDefinitionId());

        final EventListener publicEventResulted2 = listenFor("public.hearing.resulted")
                .withFilter(convertStringTo(ShareResultsMessage.class, isBean(ShareResultsMessage.class)
                        .with(ShareResultsMessage::getHearing, isBean(Hearing.class)
                                .with(Hearing::getId, is(hearingOne.getHearingId()))
                                .with(Hearing::getDefendants, first(isBean(Defendant.class)
                                        .with(Defendant::getId, is(hearingOne.getFirstDefendantId()))
                                        .with(Defendant::getCases, first(isBean(Case.class)
                                                .with(Case::getId, is(hearingOne.getFirstCaseId()))
                                                .with(Case::getOffences, first(isBean(Offence.class)
                                                        .with(Offence::getId, is(hearingOne.getFirstOffenceIdForFirstDefendant()))
                                                ))
                                        ))
                                ))
                                .with(Hearing::getSharedResultLines, first(isBean(SharedResultLine.class)
                                        .with(SharedResultLine::getOrderedDate, is(orderedDate))
                                ))
                                .with(Hearing::getSharedResultLines, second(isBean(SharedResultLine.class)
                                        .with(SharedResultLine::getOrderedDate, is(orderedDate2))
                                ))
                        )
                        .with(ShareResultsMessage::getVariants, first(isBean(Variant.class)
                                .with(Variant::getTemplateName, is(allNows.getFirstNowDefinition().getTemplateName()))
                                .with(Variant::getDescription, is(allNows.getFirstNowDefinition().getName()))
                                .with(Variant::getStatus, anyOf(is("BUILDING"), is("GENERATED")))
                        ))
                        .with(ShareResultsMessage::getVariants, second(isBean(Variant.class)
                                .with(Variant::getTemplateName, is(allNows2.getFirstNowDefinition().getTemplateName()))
                                .with(Variant::getDescription, is(allNows2.getFirstNowDefinition().getName()))
                                .with(Variant::getStatus, anyOf(is("BUILDING"), is("GENERATED")))
                        ))
                ));

        UseCases.shareResults(requestSpec, hearingOne.getHearingId(), with(
                basicShareResultsCommandTemplate(),
                command -> command.setCompletedResultLines(asList(
                        //inclusion of the original result line for a material/variant should result in the variant being kept
                        shareResultsCommand.getCompletedResultLines().get(0),
                        //an additional completed result line should result in a material/variant being created
                        CompletedResultLine.builder()
                                .withId(randomUUID())
                                .withResultDefinitionId(allNows2.getFirstNowDefinitionFirstResultDefinitionId())
                                .withDefendantId(hearingOne.getFirstDefendantId())
                                .withOffenceId(hearingOne.getFirstOffenceIdForFirstDefendant())
                                .withOrderedDate(orderedDate2)
                                .withCaseId(hearingOne.getFirstCaseId())
                                .withLevel(values(Level.values()).next())
                                .withResultLabel(STRING.next())
                                .withResultPrompts(singletonList(resultPromptTemplate(
                                        allResultDefinitions2.getFirstResultDefinitionFirstResultPrompt().getId(),
                                        allResultDefinitions2.getFirstResultDefinitionFirstResultPrompt().getLabel(),
                                        STRING.next()
                                )))
                                .build()
                ))));

        publicEventResulted2.waitFor();

        final EventListener publicEventResulted3 = listenFor("public.hearing.resulted")
                .withFilter(convertStringTo(ShareResultsMessage.class, isBean(ShareResultsMessage.class)
                        .with(ShareResultsMessage::getHearing, isBean(Hearing.class)
                                .with(Hearing::getId, is(hearingOne.getHearingId()))
                                .with(Hearing::getDefendants, first(isBean(Defendant.class)
                                        .with(Defendant::getId, is(hearingOne.getFirstDefendantId()))
                                        .with(Defendant::getCases, first(isBean(Case.class)
                                                .with(Case::getId, is(hearingOne.getFirstCaseId()))
                                                .with(Case::getOffences, first(isBean(Offence.class)
                                                        .with(Offence::getId, is(hearingOne.getFirstOffenceIdForFirstDefendant()))
                                                ))
                                        ))
                                ))
                                .with(Hearing::getSharedResultLines, first(isBean(SharedResultLine.class)
                                        .with(SharedResultLine::getOrderedDate, is(orderedDate))
                                ))
                        )
                        .with(ShareResultsMessage::getVariants, first(isBean(Variant.class)
                                .with(Variant::getTemplateName, is(allNows.getFirstNowDefinition().getTemplateName()))
                                .with(Variant::getDescription, is(allNows.getFirstNowDefinition().getName()))
                                .with(Variant::getStatus, anyOf(is("BUILDING"), is("GENERATED")))
                        ))
                ));

        UseCases.shareResults(requestSpec, hearingOne.getHearingId(), with(
                basicShareResultsCommandTemplate(),
                command -> command.setCompletedResultLines(singletonList(
                        shareResultsCommand.getCompletedResultLines().get(0)
                        // since the second completed result line has been removed from the share results command,
                        // the variant derived from it should be removed from the share results message
                ))));

        publicEventResulted3.waitFor();
    }

    @Test
    public void shareResults_shouldSurfaceResultsLinesInGetHearings_resultLinesShouldBeAsLastSubmittedOnly() {

        final UUID resultLine1 = randomUUID(), resultLine2 = randomUUID(), resultLine3 = randomUUID();

        final LocalDate orderedDate = PAST_LOCAL_DATE.next();
        final AllNowsReferenceDataHelper allNows = setupNowsReferenceData(orderedDate);
        setupResultDefinitionsReferenceData(orderedDate, allNows.getFirstNowDefinitionFirstResultDefinitionId());

        final InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(requestSpec, standardInitiateHearingTemplate()));

        givenAUserHasLoggedInAsACourtClerk(USER_ID_VALUE);

        UseCases.shareResults(requestSpec, hearingOne.getHearingId(), with(
                standardShareResultsCommandTemplate(hearingOne.getFirstDefendantId(), hearingOne.getFirstOffenceIdForFirstDefendant(), hearingOne.getFirstCaseId(), resultLine1, resultLine2, orderedDate),
                command -> {
                    command.getCompletedResultLines().get(0).setResultDefinitionId(allNows.getFirstNowDefinitionFirstResultDefinitionId());
                }));

        UseCases.shareResults(requestSpec, hearingOne.getHearingId(), with(
                standardShareResultsCommandTemplate(hearingOne.getFirstDefendantId(), hearingOne.getFirstOffenceIdForFirstDefendant(), hearingOne.getFirstCaseId(), resultLine2, resultLine3, orderedDate),
                command -> {
                    command.getCompletedResultLines().get(0).setResultDefinitionId(allNows.getFirstNowDefinitionFirstResultDefinitionId());
                }));

        poll(requestParams(getURL("hearing.get.hearing", hearingOne.getHearingId()), "application/vnd.hearing.get.hearing+json")
                .withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearingId", is(hearingOne.getHearingId().toString())),
                                withJsonPath("$.resultLines", hasSize(3))//TODO - should only be the last 2 lines that have been shared
                        )));
    }

    private AllNowsReferenceDataHelper setupNowsReferenceData(LocalDate referenceDate) {
        final AllNowsReferenceDataHelper allNows = h(AllNows.allNows()
                .setNows(singletonList(NowDefinition.now()
                        .setId(randomUUID())
                        .setResultDefinitions(singletonList(ResultDefinitions.resultDefinitions()
                                .setId(randomUUID())
                                .setMandatory(true)
                                .setPrimary(true)
                        ))
                        .setName(STRING.next())
                        .setTemplateName(STRING.next())
                )));

        ReferenceDataStub.stubGetAllNowsMetaData(referenceDate, allNows.it());
        return allNows;
    }

    private AllResultDefinitionsReferenceDataHelper setupResultDefinitionsReferenceData(LocalDate referenceDate, UUID resultDefinitionId) {
        final String LISTING_OFFICER_USERGROUP = "Listing Officer";

        final AllResultDefinitionsReferenceDataHelper allResultDefinitions = h(AllResultDefinitions.allResultDefinitions()
                .setResultDefinitions(singletonList(ResultDefinition.resultDefinition()
                                .setId(resultDefinitionId)
                                .setUserGroups(singletonList(LISTING_OFFICER_USERGROUP))
                                .setPrompts(singletonList(Prompt.prompt()
                                                .setId(randomUUID())
                                                .setMandatory(true)
                                                .setLabel(STRING.next())
                                                .setUserGroups(singletonList(LISTING_OFFICER_USERGROUP))
                                        )
                                )
                        )
                ));

        ReferenceDataStub.stubGetAllResultDefinitions(referenceDate, allResultDefinitions.it());
        return allResultDefinitions;
    }
}
