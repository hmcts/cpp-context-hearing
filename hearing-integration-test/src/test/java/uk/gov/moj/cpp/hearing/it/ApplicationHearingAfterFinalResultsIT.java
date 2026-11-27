package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.time.ZoneOffset.UTC;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataOf;
import static uk.gov.moj.cpp.hearing.it.Queries.getHearingPollForMatch;
import static uk.gov.moj.cpp.hearing.it.UseCases.initiateHearing;
import static uk.gov.moj.cpp.hearing.it.UseCases.initiateHearingWithoutBreachApplication;
import static uk.gov.moj.cpp.hearing.it.UseCases.shareResultsPerDay;
import static uk.gov.moj.cpp.hearing.it.Utilities.JsonUtil.objectToJsonObject;
import static uk.gov.moj.cpp.hearing.it.Utilities.listenFor;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.minimumInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.ShareResultsCommandTemplates.basicShareResultsCommandV2Template;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.MapStringToTypeMatcher.convertStringTo;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.getPublicTopicInstance;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.sendMessage;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_SEC;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationCase;
import uk.gov.justice.core.courts.CourtApplicationParty;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.DelegatedPowers;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.JurisdictionType;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.ProsecutingAuthority;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.justice.core.courts.Target;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.hearing.command.initiate.ExtendHearingCommand;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.result.ShareDaysResultsCommand;
import uk.gov.moj.cpp.hearing.domain.event.result.PublicHearingResultedV2;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;
import uk.gov.moj.cpp.hearing.test.HearingFactory;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.Test;

/**
 * Results the first hearing with SUSPS, FVS and FO, then shares an application listed onto a
 * later Crown hearing once that offence is concluded. The shared hearing must omit prosecutionCases.
 */
public class ApplicationHearingAfterFinalResultsIT extends AbstractIT {

    private static final String PROGRESSION_HEARING_RESULTED_CASE_UPDATED = "public.progression.hearing-resulted-case-updated";
    private static final String PROGRESSION_EVENTS_HEARING_EXTENDED = "public.progression.events.hearing-extended";
    private static final String RESULTS_SHARED_V3 = "hearing.events.results-shared-v3";
    private static final String PUBLIC_HEARING_RESULTED = "public.events.hearing.hearing-resulted";
    private static final String APPLICATION_WITHIN_CRIMINAL_PROCEEDINGS = "fde5dd50-fb49-4100-8c46-4c10096fb136";
    private static final UUID SUSPENDED_SENTENCE = fromString("8b1cff00-a456-40da-9ce4-f11c20959084");
    private static final UUID SURCHARGE = fromString("e866cd11-6073-4fdf-a229-51c9d694e1d0");
    private static final UUID FINE = fromString("969f150c-cd05-46b0-9dd9-30891efcc766");

    private final StringToJsonObjectConverter stringToJsonObjectConverter = new StringToJsonObjectConverter();
    private final JsonObjectToObjectConverter jsonObjectToObjectConverter =
            new JsonObjectToObjectConverter(new ObjectMapperProducer().objectMapper());

    @Test
    public void shouldOmitProsecutionCasesWhenApplicationHearingIsSharedAfterFinalResults() throws Exception {
        final LocalDate resultedOn = LocalDate.now();
        final ZonedDateTime futureSitting = ZonedDateTime.now(UTC).plusDays(14).withNano(0);

        final InitiateHearingCommandHelper firstHearingCommand = h(initiateHearing(getRequestSpec(), minimumInitiateHearingTemplate()));
        final Hearing firstHearing = firstHearingCommand.getHearing();
        final ProsecutionCase prosecutionCase = firstHearing.getProsecutionCases().get(0);
        final UUID caseId = prosecutionCase.getId();
        final UUID defendantId = prosecutionCase.getDefendants().get(0).getId();
        final UUID offenceId = prosecutionCase.getDefendants().get(0).getOffences().get(0).getId();

        getHearingPollForMatch(firstHearing.getId(), isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(firstHearing.getId()))
                        .withValue(hearing -> hearing.getProsecutionCases().size(), 1)));

        final InitiateHearingCommand adhocHearingCommand = minimumInitiateHearingTemplate();
        final Hearing adhocHearing = adhocHearingCommand.getHearing();
        adhocHearing.setId(randomUUID());
        adhocHearing.setJurisdictionType(JurisdictionType.CROWN);
        adhocHearing.setProsecutionCases(new ArrayList<>(List.of(copyCase(prosecutionCase))));
        adhocHearing.getHearingDays().get(0).setSittingDay(futureSitting);
        h(initiateHearingWithoutBreachApplication(getRequestSpec(), adhocHearingCommand));

        getHearingPollForMatch(adhocHearing.getId(), isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(adhocHearing.getId()))
                        .with(Hearing::getProsecutionCases, hasItem(isBean(ProsecutionCase.class)
                                .withValue(ProsecutionCase::getId, caseId)))));

        stubCourtRoom(firstHearing);
        stubCourtRoom(adhocHearing);

        final Target firstHearingTarget = targetFromResource(
                "hearing.share-result-with-driving_number-prompts-result.json",
                firstHearing.getId(), caseId, defendantId, offenceId, resultedOn, null);
        firstHearingTarget.setResultLines(List.of(
                finalResult(firstHearingTarget.getResultLines().get(0), SUSPENDED_SENTENCE, "Suspended sentence order - imprisonment"),
                finalResult(firstHearingTarget.getResultLines().get(0), SURCHARGE, "Surcharge"),
                finalResult(firstHearingTarget.getResultLines().get(0), FINE, "Fine")));

        try (Utilities.EventListener firstHearingResulted = listenFor(PUBLIC_HEARING_RESULTED)
                .withFilter(convertStringTo(PublicHearingResultedV2.class, isBean(PublicHearingResultedV2.class)
                        .with(PublicHearingResultedV2::getHearing, isBean(Hearing.class)
                                .with(Hearing::getId, is(firstHearing.getId())))))) {
            shareDaysResult(firstHearing, firstHearingTarget, resultedOn, caseId);
            firstHearingResulted.waitFor();
        }

        final String caseUpdated = getStringFromResource("public.progression.hearing-resulted-case-updated.json")
                .replace("CASE_ID", caseId.toString())
                .replace("DEFENDANT_ID", defendantId.toString());
        sendMessage(getPublicTopicInstance().createProducer(),
                PROGRESSION_HEARING_RESULTED_CASE_UPDATED,
                stringToJsonObjectConverter.convert(caseUpdated),
                metadataOf(randomUUID(), PROGRESSION_HEARING_RESULTED_CASE_UPDATED)
                        .withUserId(randomUUID().toString())
                        .build());

        getHearingPollForMatch(adhocHearing.getId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getProsecutionCases, hasItem(isBean(ProsecutionCase.class)
                                .withValue(ProsecutionCase::getCaseStatus, "CLOSED")
                                .with(ProsecutionCase::getDefendants, hasItem(isBean(Defendant.class)
                                        .withValue(Defendant::getProceedingsConcluded, true)))))));

        final CourtApplication application = applicationWithinCriminalProceedings(defendantId, caseId, offenceId);
        final ExtendHearingCommand extendHearingCommand = new ExtendHearingCommand();
        extendHearingCommand.setHearingId(adhocHearing.getId());
        extendHearingCommand.setJurisdictionType(JurisdictionType.CROWN);
        extendHearingCommand.setCourtApplication(application);
        sendMessage(getPublicTopicInstance().createProducer(),
                PROGRESSION_EVENTS_HEARING_EXTENDED,
                objectToJsonObject(extendHearingCommand),
                metadataOf(randomUUID(), PROGRESSION_EVENTS_HEARING_EXTENDED)
                        .withUserId(randomUUID().toString())
                        .build());

        getHearingPollForMatch(adhocHearing.getId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getCourtApplications, hasItem(isBean(CourtApplication.class)
                                .withValue(CourtApplication::getId, application.getId())))
                        .withValue(hearing -> hearing.getProsecutionCases() == null ? 0 : hearing.getProsecutionCases().size(), 0)));

        stubLjaDetails(adhocHearing.getCourtCentre(),
                application.getCourtApplicationCases().get(0).getProsecutionCaseIdentifier().getProsecutionAuthorityId());

        final Target applicationTarget = targetFromResource(
                "hearing.share-application-result-with-driving_number-prompts-result.json",
                adhocHearing.getId(), null, defendantId, null, resultedOn, application.getId());
        applicationTarget.setApplicationId(application.getId());

        try (Utilities.EventListener resultsShared = listenFor(RESULTS_SHARED_V3, "hearing.event")
                .withFilter(isJson(allOf(
                        withJsonPath("$.hearingId", is(adhocHearing.getId().toString())))));
             Utilities.EventListener applicationResulted = listenFor(PUBLIC_HEARING_RESULTED)
                     .withFilter(convertStringTo(PublicHearingResultedV2.class, isBean(PublicHearingResultedV2.class)
                             .with(PublicHearingResultedV2::getHearing, isBean(Hearing.class)
                                     .with(Hearing::getId, is(adhocHearing.getId())))))) {

            shareApplicationResult(adhocHearing, applicationTarget, resultedOn);

            final JsonPath sharedEvent = resultsShared.waitFor();
            final JsonPath publicEvent = applicationResulted.waitFor();

            assertThat(sharedEvent.get("hearing.prosecutionCases"), nullValue());
            assertThat(sharedEvent.getString("hearing.courtApplications[0].id"), is(application.getId().toString()));
            assertThat(publicEvent.get("hearing.prosecutionCases"), nullValue());
            assertThat(publicEvent.getString("hearing.courtApplications[0].id"), is(application.getId().toString()));
        }
    }

    private Target targetFromResource(final String resource, final UUID hearingId, final UUID caseId,
                                      final UUID defendantId, final UUID offenceId, final LocalDate orderedDate,
                                      final UUID applicationId) throws Exception {
        String payload = getStringFromResource(resource)
                .replace("HEARING_ID", hearingId.toString())
                .replace("DEFENDANT_ID", defendantId.toString())
                .replace("ORDERED_DATE", orderedDate.toString());
        if (applicationId != null) {
            payload = payload.replace("APPLICATION_ID", applicationId.toString());
        }
        if (caseId != null) {
            payload = payload.replace("CASE_ID", caseId.toString());
        }
        if (offenceId != null) {
            payload = payload.replace("OFFENCE_ID", offenceId.toString());
        }
        return jsonObjectToObjectConverter.convert(stringToJsonObjectConverter.convert(payload), Target.class);
    }

    private ResultLine finalResult(final ResultLine template, final UUID resultDefinitionId, final String label) {
        return ResultLine.resultLine()
                .withValuesFrom(template)
                .withResultLineId(randomUUID())
                .withResultDefinitionId(resultDefinitionId)
                .withResultLabel(label)
                .withPrompts(new ArrayList<>())
                .build();
    }

    private void shareDaysResult(final Hearing hearing, final Target target, final LocalDate hearingDay, final UUID caseId) {
        final ShareDaysResultsCommand command = basicShareResultsCommandV2Template();
        command.setHearingDay(hearingDay);
        command.setCourtClerk(courtClerk());
        shareResultsPerDay(getRequestSpec(), hearing.getId(), caseId, command, List.of(target));
    }

    private void shareApplicationResult(final Hearing hearing, final Target target, final LocalDate hearingDay) {
        final ShareDaysResultsCommand command = basicShareResultsCommandV2Template();
        command.setHearingDay(hearingDay);
        shareResultsPerDay(getRequestSpec(), hearing.getId(), with(command, shareCommand -> shareCommand.setCourtClerk(courtClerk())), List.of(target));
    }

    private DelegatedPowers courtClerk() {
        return DelegatedPowers.delegatedPowers()
                .withFirstName("Andrew")
                .withLastName("Eldritch")
                .withUserId(randomUUID())
                .build();
    }

    private ProsecutionCase copyCase(final ProsecutionCase source) {
        final Defendant sourceDefendant = source.getDefendants().get(0);
        final Offence offence = Offence.offence().withValuesFrom(sourceDefendant.getOffences().get(0)).build();
        final Defendant defendant = Defendant.defendant()
                .withValuesFrom(sourceDefendant)
                .withOffences(new ArrayList<>(List.of(offence)))
                .build();
        return ProsecutionCase.prosecutionCase()
                .withValuesFrom(source)
                .withDefendants(new ArrayList<>(List.of(defendant)))
                .build();
    }

    private CourtApplication applicationWithinCriminalProceedings(final UUID defendantId, final UUID caseId, final UUID offenceId) {
        final HearingFactory hearingFactory = new HearingFactory();
        final CourtApplicationParty applicant = CourtApplicationParty.courtApplicationParty()
                .withId(randomUUID())
                .withSummonsRequired(false)
                .withNotificationRequired(false)
                .withProsecutingAuthority(ProsecutingAuthority.prosecutingAuthority()
                        .withProsecutionAuthorityId(randomUUID())
                        .withProsecutionAuthorityCode("CPS")
                        .withName("CPS")
                        .build())
                .build();
        final CourtApplicationParty respondent = hearingFactory.courtApplicationDefendant(defendantId, caseId).build();
        final CourtApplication application = hearingFactory.courtApplication(applicant, fromString(APPLICATION_WITHIN_CRIMINAL_PROCEEDINGS)).build();
        application.getType().setCode("AP00501");
        application.getType().setType("Application within criminal proceedings");
        application.setRespondents(List.of(respondent));
        application.setSubject(respondent);

        final CourtApplicationCase applicationCase = application.getCourtApplicationCases().get(0);
        applicationCase.setProsecutionCaseId(caseId);
        applicationCase.setCaseStatus("INACTIVE");
        final Offence applicationOffence = applicationCase.getOffences().get(0);
        applicationOffence.setId(offenceId);
        applicationOffence.setProceedingsConcluded(true);
        return application;
    }
}
