package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withoutJsonPath;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.services.test.utils.core.http.BaseUriProvider.getBaseUri;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.http.RestPoller.poll;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.BOOLEAN;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.integer;
import static uk.gov.moj.cpp.hearing.it.TestUtilities.listenFor;
import static uk.gov.moj.cpp.hearing.it.TestUtilities.makeCommand;
import static uk.gov.moj.cpp.hearing.it.UseCases.initiateHearing;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsACourtClerk;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.verdict.Defendant;
import uk.gov.moj.cpp.hearing.command.verdict.HearingUpdateVerdictCommand;
import uk.gov.moj.cpp.hearing.command.verdict.Offence;
import uk.gov.moj.cpp.hearing.command.verdict.Verdict;
import uk.gov.moj.cpp.hearing.command.verdict.VerdictValue;
import uk.gov.moj.cpp.hearing.it.TestUtilities.EventListener;

public class VerdictIT extends AbstractIT {

    @SuppressWarnings("unchecked")
    @Test
    public void updateVerdict_shouldSetConvictionDateToStartOfHearing_givenGuiltyVerdict() throws Exception {

        givenAUserHasLoggedInAsACourtClerk(USER_ID_VALUE);

        InitiateHearingCommand initiateHearingCommand = initiateHearing(requestSpec, data -> {
            data.getHearing().getDefendants().get(0).getOffences().get(0).withConvictionDate(null);
        });

        HearingUpdateVerdictCommand hearingUpdateVerdictCommand = HearingUpdateVerdictCommand.builder()
                .withCaseId(initiateHearingCommand.getCases().get(0).getCaseId())
                .addDefendant(
                        Defendant.builder().withId(initiateHearingCommand.getHearing().getDefendants().get(0).getId())
                                .withPersonId(randomUUID())
                                .addOffence(Offence.builder()
                                        .withId(initiateHearingCommand.getHearing().getDefendants().get(0).getOffences()
                                                .get(0).getId())
                                        .withVerdict(Verdict.builder().withId(randomUUID())
                                                .withValue(VerdictValue.builder().withId(randomUUID())
                                                        .withCategory("Guilty")
                                                        .withCategoryType("GUILTY")
                                                        .withCode("A1")
                                                        .withDescription("Guilty, by jury on judge's direction")
                                                        .withVerdictTypeId(randomUUID())
                                                ).withNumberOfJurors(integer(9, 12).next())
                                                .withNumberOfSplitJurors(integer(0, 3).next())
                                                .withUnanimous(BOOLEAN.next())
                                                .withVerdictDate(PAST_LOCAL_DATE.next()))))
                .build();

        final EventListener publicEventVerdictUpdatedListener = listenFor("public.hearing.verdict-updated").withFilter(
                isJson(withJsonPath("$.hearingId", is(initiateHearingCommand.getHearing().getId().toString()))));

        final EventListener publicEventConvictionDateChangedListener = listenFor(
                "public.hearing.offence-conviction-date-changed")
                        .withFilter(isJson(allOf(
                                withJsonPath("$.offenceId", is(initiateHearingCommand.getHearing()
                                .getDefendants().get(0).getOffences().get(0).getId().toString())),
                                withJsonPath("$.caseId", is(initiateHearingCommand.getHearing()
                                        .getDefendants().get(0).getOffences().get(0).getCaseId().toString())))));

        makeCommand(requestSpec, "hearing.initiate-hearing").ofType("application/vnd.hearing.update-verdict+json")
                .withArgs(initiateHearingCommand.getHearing().getId()).withPayload(hearingUpdateVerdictCommand)
                .executeSuccessfully();

        publicEventVerdictUpdatedListener.waitFor();
        publicEventConvictionDateChangedListener.waitFor();

        final String queryAPIEndPoint = MessageFormat.format(ENDPOINT_PROPERTIES.getProperty("hearing.get.hearing.v2"),
                initiateHearingCommand.getHearing().getId());
        final String url = getBaseUri() + "/" + queryAPIEndPoint;

        final String responseType = "application/vnd.hearing.get.hearing.v2+json";

        Verdict verdict = hearingUpdateVerdictCommand.getDefendants().get(0).getOffences().get(0).getVerdict();

        poll(requestParams(url, responseType).withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue())
                .build())
                        .timeout(30,
                                TimeUnit.SECONDS)
                        .until(status().is(OK), print(), payload().isJson(allOf(
                                withJsonPath("$.hearingId", is(initiateHearingCommand.getHearing().getId().toString())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].id",
                                        equalStr(initiateHearingCommand, "hearing.defendants[0].offences[0].id")),

                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.category",
                                        is(verdict.getValue().getCategory())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.code",
                                        is(verdict.getValue().getCode())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.description",
                                        is(verdict.getValue().getDescription())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.verdictTypeId",
                                        is(verdict.getValue().getVerdictTypeId().toString())),
                                
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.verdictDate",
                                        is(verdict.getVerdictDate().toString())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.numberOfSplitJurors",
                                        is(verdict.getNumberOfSplitJurors())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.numberOfJurors",
                                        is(verdict.getNumberOfJurors())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.unanimous",
                                        is(verdict.getUnanimous())),

                                withJsonPath("$.cases[0].defendants[0].offences[0].convictionDate",
                                        is(verdict.getVerdictDate().toString())))));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void updateVerdict_shouldSetConvictionDateToNull_givenNotGuiltyVerdict() throws Exception {
        givenAUserHasLoggedInAsACourtClerk(USER_ID_VALUE);

        InitiateHearingCommand initiateHearingCommand = initiateHearing(requestSpec, data -> {
            data.getHearing().getDefendants().get(0).getOffences().get(0).withConvictionDate(null);
        });

        HearingUpdateVerdictCommand hearingUpdateVerdictCommand = HearingUpdateVerdictCommand.builder()
                .withCaseId(initiateHearingCommand.getCases().get(0).getCaseId())

                .addDefendant(
                        Defendant.builder().withId(initiateHearingCommand.getHearing().getDefendants().get(0).getId())
                                .withPersonId(randomUUID())
                                .addOffence(Offence.builder()
                                        .withId(initiateHearingCommand.getHearing().getDefendants().get(0).getOffences()
                                                .get(0).getId())
                                        .withVerdict(Verdict.builder().withId(randomUUID())
                                                .withValue(VerdictValue.builder().withId(randomUUID())
                                                        .withCategory("Not Guilty")
                                                        .withCategoryType("NOT_GUILTY")
                                                        .withCode("A1")
                                                        .withDescription("Not guilty, by jury on judge's direction")
                                                        .withVerdictTypeId(randomUUID())
                                                ).withNumberOfJurors(integer(9, 12).next())
                                                .withNumberOfSplitJurors(integer(0, 3).next())
                                                .withUnanimous(BOOLEAN.next())
                                                .withVerdictDate(PAST_LOCAL_DATE.next()))))
                .build();

        final EventListener publicEventOffenceConvictionDateRemovedListener = listenFor(
                "public.hearing.offence-conviction-date-removed")
                        .withFilter(isJson(allOf(
                                withJsonPath("$.offenceId", is(initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getId().toString())),
                                withJsonPath("$.caseId", is(initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getCaseId().toString())))));

        makeCommand(requestSpec, "hearing.initiate-hearing").ofType("application/vnd.hearing.update-verdict+json")
                .withArgs(initiateHearingCommand.getHearing().getId()).withPayload(hearingUpdateVerdictCommand)
                .executeSuccessfully();

        publicEventOffenceConvictionDateRemovedListener.waitFor();

        final String queryAPIEndPoint = MessageFormat.format(ENDPOINT_PROPERTIES.getProperty("hearing.get.hearing.v2"),
                initiateHearingCommand.getHearing().getId());
        final String url = getBaseUri() + "/" + queryAPIEndPoint;

        final String responseType = "application/vnd.hearing.get.hearing.v2+json";

        Verdict verdict = hearingUpdateVerdictCommand.getDefendants().get(0).getOffences().get(0).getVerdict();

        poll(requestParams(url, responseType).withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue())
                .build())
                        .timeout(30,
                                TimeUnit.SECONDS)
                        .until(status().is(OK), payload().isJson(allOf(
                                withJsonPath("$.hearingId", is(initiateHearingCommand.getHearing().getId().toString())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].id",
                                        equalStr(initiateHearingCommand, "hearing.defendants[0].offences[0].id")),

                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.category",
                                        is(verdict.getValue().getCategory())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.code",
                                        is(verdict.getValue().getCode())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.description",
                                        is(verdict.getValue().getDescription())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.verdictTypeId",
                                        is(verdict.getValue().getVerdictTypeId().toString())),
                                
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.verdictDate",
                                        is(verdict.getVerdictDate().toString())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.numberOfSplitJurors",
                                        is(verdict.getNumberOfSplitJurors())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.numberOfJurors",
                                        is(verdict.getNumberOfJurors())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.unanimous",
                                        is(verdict.getUnanimous())),
                                withoutJsonPath("$.cases[0].defendants[0].offences[0].convictionDate"))));

    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void updateVerdict_shouldSetConvictionDateToStartOfHearing_givenGuiltyButOfLesserOffenceVerdict()
            throws Exception {

        givenAUserHasLoggedInAsACourtClerk(USER_ID_VALUE);

        InitiateHearingCommand initiateHearingCommand = initiateHearing(requestSpec, data -> {
            data.getHearing().getDefendants().get(0).getOffences().get(0).withConvictionDate(null);
        });

        HearingUpdateVerdictCommand hearingUpdateVerdictCommand = HearingUpdateVerdictCommand.builder()
                .withCaseId(initiateHearingCommand.getCases().get(0).getCaseId())
                .addDefendant(
                        Defendant.builder().withId(initiateHearingCommand.getHearing().getDefendants().get(0).getId())
                                .withPersonId(randomUUID())
                                .addOffence(Offence.builder()
                                        .withId(initiateHearingCommand.getHearing().getDefendants().get(0).getOffences()
                                                .get(0).getId())
                                        .withVerdict(Verdict.builder().withId(randomUUID())
                                                .withValue(VerdictValue.builder().withId(randomUUID())
                                                        .withCategory("Not Guilty")
                                                        .withCategoryType("GUILTY_BUT_OF_LESSER_OFFENCE")
                                                        .withLesserOffence("Obstructing a Police Officer")
                                                        .withCode("A1")
                                                        .withDescription("Not guilty but guilty of lesser/alternative offence not charged namely")
                                                        .withVerdictTypeId(randomUUID())
                                                ).withNumberOfJurors(integer(9, 12).next())
                                                .withNumberOfSplitJurors(integer(0, 3).next())
                                                .withUnanimous(BOOLEAN.next())
                                                .withVerdictDate(PAST_LOCAL_DATE.next()))))
                .build();

        final EventListener publicEventVerdictUpdatedListener = listenFor("public.hearing.verdict-updated").withFilter(
                isJson(withJsonPath("$.hearingId", is(initiateHearingCommand.getHearing().getId().toString()))));

        final EventListener publicEventConvictionDateChangedListener = listenFor(
                "public.hearing.offence-conviction-date-changed")
                        .withFilter(isJson(allOf(
                                withJsonPath("$.offenceId",
                                        is(initiateHearingCommand.getHearing().getDefendants().get(0).getOffences()
                                                .get(0).getId().toString())),
                                withJsonPath("$.caseId", is(initiateHearingCommand.getHearing().getDefendants().get(0)
                                        .getOffences().get(0).getCaseId().toString())))));

        makeCommand(requestSpec, "hearing.initiate-hearing").ofType("application/vnd.hearing.update-verdict+json")
                .withArgs(initiateHearingCommand.getHearing().getId()).withPayload(hearingUpdateVerdictCommand)
                .executeSuccessfully();

        publicEventVerdictUpdatedListener.waitFor();
        publicEventConvictionDateChangedListener.waitFor();

        final String queryAPIEndPoint = MessageFormat.format(ENDPOINT_PROPERTIES.getProperty("hearing.get.hearing.v2"),
                initiateHearingCommand.getHearing().getId());
        final String url = getBaseUri() + "/" + queryAPIEndPoint;

        final String responseType = "application/vnd.hearing.get.hearing.v2+json";

        Verdict verdict = hearingUpdateVerdictCommand.getDefendants().get(0).getOffences().get(0).getVerdict();

        poll(requestParams(url, responseType).withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue())
                .build())
                        .timeout(30,
                                TimeUnit.SECONDS)
                        .until(status().is(OK), print(), payload().isJson(allOf(
                                withJsonPath("$.hearingId", is(initiateHearingCommand.getHearing().getId().toString())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].id",
                                        equalStr(initiateHearingCommand, "hearing.defendants[0].offences[0].id")),

                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.category",
                                        is(verdict.getValue().getCategory())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.categoryType",
                                        is(verdict.getValue().getCategoryType())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.lesserOffence",
                                        is(verdict.getValue().getLesserOffence())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.code",
                                        is(verdict.getValue().getCode())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.description",
                                        is(verdict.getValue().getDescription())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.verdictTypeId",
                                        is(verdict.getValue().getVerdictTypeId().toString())),
                                
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.verdictDate",
                                        is(verdict.getVerdictDate().toString())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.numberOfSplitJurors",
                                        is(verdict.getNumberOfSplitJurors())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.numberOfJurors",
                                        is(verdict.getNumberOfJurors())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.unanimous",
                                        is(verdict.getUnanimous())),

                                withJsonPath("$.cases[0].defendants[0].offences[0].convictionDate",
                                        is(verdict.getVerdictDate().toString())))));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void updateVerdict_shouldNotUpdateOrClearConvictionDateWhenVerditCategoryTypeIsFinding() throws Exception {

        givenAUserHasLoggedInAsACourtClerk(USER_ID_VALUE);

        InitiateHearingCommand initiateHearingCommand = initiateHearing(requestSpec, data -> {
            data.getHearing().getDefendants().get(0).getOffences().get(0).withConvictionDate(null);
        });

        LocalDate guiltyConvictionDate = PAST_LOCAL_DATE.next();
        LocalDate findingConvictionDate = LocalDate.now();

        HearingUpdateVerdictCommand hearingUpdateVerdictCommand = HearingUpdateVerdictCommand.builder()
                .withCaseId(initiateHearingCommand.getCases().get(0).getCaseId())
                .addDefendant(
                        Defendant.builder().withId(initiateHearingCommand.getHearing().getDefendants().get(0).getId())
                                .withPersonId(randomUUID())
                                .addOffence(Offence.builder()
                                        .withId(initiateHearingCommand.getHearing().getDefendants().get(0).getOffences()
                                                .get(0).getId())
                                        .withVerdict(Verdict.builder().withId(randomUUID())
                                                .withValue(VerdictValue.builder().withId(randomUUID())
                                                        .withCategory("Guilty")
                                                        .withCategoryType("GUILTY")
                                                        .withCode("A1")
                                                        .withDescription("Guilty by judge alone  (under DVC&V Act 2004)")
                                                        .withVerdictTypeId(randomUUID())
                                                ).withNumberOfJurors(integer(9, 12).next())
                                                .withNumberOfSplitJurors(integer(0, 3).next())
                                                .withUnanimous(BOOLEAN.next())
                                                .withVerdictDate(guiltyConvictionDate))))
                .build();

        final EventListener publicEventVerdictUpdatedListener = listenFor("public.hearing.verdict-updated").withFilter(
                isJson(withJsonPath("$.hearingId", is(initiateHearingCommand.getHearing().getId().toString()))));

        final EventListener publicEventConvictionDateChangedListener = listenFor(
                "public.hearing.offence-conviction-date-changed")
                        .withFilter(isJson(allOf(
                                withJsonPath("$.offenceId",
                                        is(initiateHearingCommand.getHearing().getDefendants().get(0).getOffences()
                                                .get(0).getId().toString())),
                                withJsonPath("$.caseId", is(initiateHearingCommand.getHearing().getDefendants().get(0)
                                        .getOffences().get(0).getCaseId().toString())))));

        makeCommand(requestSpec, "hearing.initiate-hearing").ofType("application/vnd.hearing.update-verdict+json")
                .withArgs(initiateHearingCommand.getHearing().getId()).withPayload(hearingUpdateVerdictCommand)
                .executeSuccessfully();

        publicEventVerdictUpdatedListener.waitFor();
        publicEventConvictionDateChangedListener.waitFor();

        final String queryAPIEndPoint = MessageFormat.format(ENDPOINT_PROPERTIES.getProperty("hearing.get.hearing.v2"),
                initiateHearingCommand.getHearing().getId());
        final String url = getBaseUri() + "/" + queryAPIEndPoint;

        final String responseType = "application/vnd.hearing.get.hearing.v2+json";

        Verdict verdict = hearingUpdateVerdictCommand.getDefendants().get(0).getOffences().get(0).getVerdict();

        poll(requestParams(url, responseType).withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue())
                .build())
                        .timeout(30,
                                TimeUnit.SECONDS)
                        .until(status().is(OK), print(), payload().isJson(allOf(
                                withJsonPath("$.hearingId", is(initiateHearingCommand.getHearing().getId().toString())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].id",
                                        equalStr(initiateHearingCommand, "hearing.defendants[0].offences[0].id")),

                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.category",
                                        is(verdict.getValue().getCategory())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.categoryType",
                                        is(verdict.getValue().getCategoryType())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.code",
                                        is(verdict.getValue().getCode())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.description",
                                        is(verdict.getValue().getDescription())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.verdictTypeId",
                                        is(verdict.getValue().getVerdictTypeId().toString())),
                                
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.verdictDate",
                                        is(verdict.getVerdictDate().toString())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.numberOfSplitJurors",
                                        is(verdict.getNumberOfSplitJurors())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.numberOfJurors",
                                        is(verdict.getNumberOfJurors())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.unanimous",
                                        is(verdict.getUnanimous())),

                                withJsonPath("$.cases[0].defendants[0].offences[0].convictionDate",
                                        is(guiltyConvictionDate.toString())))));

        // Verdict Category type is FINDING

        HearingUpdateVerdictCommand findingCategoryTypeCommand = HearingUpdateVerdictCommand.builder()
                .withCaseId(initiateHearingCommand.getCases().get(0).getCaseId())
                .addDefendant(
                        Defendant.builder().withId(initiateHearingCommand.getHearing().getDefendants().get(0).getId())
                                .withPersonId(randomUUID())
                                .addOffence(Offence.builder()
                                        .withId(initiateHearingCommand.getHearing().getDefendants().get(0).getOffences()
                                                .get(0).getId())
                                        .withVerdict(Verdict.builder().withId(randomUUID())
                                                .withValue(VerdictValue.builder().withId(randomUUID())
                                                        .withCategory("Finding")
                                                        .withCategoryType("FINDING")
                                                        .withCode("A1")
                                                        .withDescription("Defendant found under a disability")
                                                        .withVerdictTypeId(randomUUID())
                                                ).withNumberOfJurors(integer(9, 12).next())
                                                .withNumberOfSplitJurors(integer(0, 3).next())
                                                .withUnanimous(BOOLEAN.next())
                                                .withVerdictDate(findingConvictionDate))))
                .build();

        final EventListener findingCategoryTypeListener = listenFor("public.hearing.verdict-updated").withFilter(
                isJson(withJsonPath("$.hearingId", is(initiateHearingCommand.getHearing().getId().toString()))));

        makeCommand(requestSpec, "hearing.initiate-hearing").ofType("application/vnd.hearing.update-verdict+json")
                .withArgs(initiateHearingCommand.getHearing().getId()).withPayload(findingCategoryTypeCommand)
                .executeSuccessfully();

        findingCategoryTypeListener.waitFor();

        final String findingCategoryTypeQueryAPIEndPoint = MessageFormat.format(
                ENDPOINT_PROPERTIES.getProperty("hearing.get.hearing.v2"), initiateHearingCommand.getHearing().getId());
        final String findingCategoryTypeUrl = getBaseUri() + "/" + findingCategoryTypeQueryAPIEndPoint;

        final String findingCategoryTypeResponseType = "application/vnd.hearing.get.hearing.v2+json";

        Verdict findingCategoryTypeVerdit = findingCategoryTypeCommand.getDefendants().get(0).getOffences().get(0)
                .getVerdict();

        poll(requestParams(findingCategoryTypeUrl, findingCategoryTypeResponseType)
                .withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                        .timeout(30,
                                TimeUnit.SECONDS)
                        .until(status().is(OK), print(), payload().isJson(allOf(
                                withJsonPath("$.hearingId", is(initiateHearingCommand.getHearing().getId().toString())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].id",
                                        equalStr(initiateHearingCommand, "hearing.defendants[0].offences[0].id")),

                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.category",
                                        is(findingCategoryTypeVerdit.getValue().getCategory())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.categoryType",
                                        is(findingCategoryTypeVerdit.getValue().getCategoryType())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.code",
                                        is(findingCategoryTypeVerdit.getValue().getCode())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.description",
                                        is(findingCategoryTypeVerdit.getValue().getDescription())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.verdictTypeId",
                                        is(findingCategoryTypeVerdit.getValue().getVerdictTypeId().toString())),
                                
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.verdictDate",
                                        is(findingCategoryTypeVerdit.getVerdictDate().toString())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.numberOfSplitJurors",
                                        is(findingCategoryTypeVerdit.getNumberOfSplitJurors())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.numberOfJurors",
                                        is(findingCategoryTypeVerdit.getNumberOfJurors())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.unanimous",
                                        is(findingCategoryTypeVerdit.getUnanimous())),

                                withJsonPath("$.cases[0].defendants[0].offences[0].convictionDate",
                                        is(guiltyConvictionDate.toString())))));

    }

    @SuppressWarnings("unchecked")
    @Test
    public void updateVerdict_shouldNotUpdateOrClearConvictionDateWhenVerditCategoryTypeIsNoVerdit() throws Exception {

        givenAUserHasLoggedInAsACourtClerk(USER_ID_VALUE);

        InitiateHearingCommand initiateHearingCommand = initiateHearing(requestSpec, data -> {
            data.getHearing().getDefendants().get(0).getOffences().get(0).withConvictionDate(null);
        });

        LocalDate guiltyConvictionDate = PAST_LOCAL_DATE.next();
        LocalDate noVerditConvictionDate = LocalDate.now();

        HearingUpdateVerdictCommand hearingUpdateVerdictCommand = HearingUpdateVerdictCommand.builder()
                .withCaseId(initiateHearingCommand.getCases().get(0).getCaseId())
                .addDefendant(
                        Defendant.builder().withId(initiateHearingCommand.getHearing().getDefendants().get(0).getId())
                                .withPersonId(randomUUID())
                                .addOffence(Offence.builder()
                                        .withId(initiateHearingCommand.getHearing().getDefendants().get(0).getOffences()
                                                .get(0).getId())
                                        .withVerdict(Verdict.builder().withId(randomUUID())
                                                .withValue(VerdictValue.builder().withId(randomUUID())
                                                        .withCategory("Guilty")
                                                        .withCategoryType("GUILTY")
                                                        .withCode("A1")
                                                        .withDescription("Guilty, by jury on judge's direction")
                                                        .withVerdictTypeId(randomUUID())
                                                ).withNumberOfJurors(integer(9, 12).next())
                                                .withNumberOfSplitJurors(integer(0, 3).next())
                                                .withUnanimous(BOOLEAN.next())
                                                .withVerdictDate(guiltyConvictionDate))))
                .build();

        final EventListener publicEventVerdictUpdatedListener = listenFor("public.hearing.verdict-updated").withFilter(
                isJson(withJsonPath("$.hearingId", is(initiateHearingCommand.getHearing().getId().toString()))));

        final EventListener publicEventConvictionDateChangedListener = listenFor(
                "public.hearing.offence-conviction-date-changed")
                        .withFilter(isJson(allOf(
                                withJsonPath("$.offenceId",
                                        is(initiateHearingCommand.getHearing().getDefendants().get(0).getOffences()
                                                .get(0).getId().toString())),
                                withJsonPath("$.caseId", is(initiateHearingCommand.getHearing().getDefendants().get(0)
                                        .getOffences().get(0).getCaseId().toString())))));

        makeCommand(requestSpec, "hearing.initiate-hearing").ofType("application/vnd.hearing.update-verdict+json")
                .withArgs(initiateHearingCommand.getHearing().getId()).withPayload(hearingUpdateVerdictCommand)
                .executeSuccessfully();

        publicEventVerdictUpdatedListener.waitFor();
        publicEventConvictionDateChangedListener.waitFor();

        final String queryAPIEndPoint = MessageFormat.format(ENDPOINT_PROPERTIES.getProperty("hearing.get.hearing.v2"),
                initiateHearingCommand.getHearing().getId());
        final String url = getBaseUri() + "/" + queryAPIEndPoint;

        final String responseType = "application/vnd.hearing.get.hearing.v2+json";

        Verdict verdict = hearingUpdateVerdictCommand.getDefendants().get(0).getOffences().get(0).getVerdict();

        poll(requestParams(url, responseType).withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue())
                .build())
                        .timeout(30,
                                TimeUnit.SECONDS)
                        .until(status().is(OK), print(), payload().isJson(allOf(
                                withJsonPath("$.hearingId", is(initiateHearingCommand.getHearing().getId().toString())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].id",
                                        equalStr(initiateHearingCommand, "hearing.defendants[0].offences[0].id")),

                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.category",
                                        is(verdict.getValue().getCategory())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.categoryType",
                                        is(verdict.getValue().getCategoryType())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.code",
                                        is(verdict.getValue().getCode())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.description",
                                        is(verdict.getValue().getDescription())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.verdictTypeId",
                                        is(verdict.getValue().getVerdictTypeId().toString())),
                                
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.verdictDate",
                                        is(verdict.getVerdictDate().toString())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.numberOfSplitJurors",
                                        is(verdict.getNumberOfSplitJurors())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.numberOfJurors",
                                        is(verdict.getNumberOfJurors())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.unanimous",
                                        is(verdict.getUnanimous())),

                                withJsonPath("$.cases[0].defendants[0].offences[0].convictionDate",
                                        is(guiltyConvictionDate.toString())))));

        // Verdit Category type is NO VERDIT

        HearingUpdateVerdictCommand noVerditCategoryTypeCommand = HearingUpdateVerdictCommand.builder()
                .withCaseId(initiateHearingCommand.getCases().get(0).getCaseId())
                .addDefendant(
                        Defendant.builder().withId(initiateHearingCommand.getHearing().getDefendants().get(0).getId())
                                .withPersonId(randomUUID())
                                .addOffence(Offence.builder()
                                        .withId(initiateHearingCommand.getHearing().getDefendants().get(0).getOffences()
                                                .get(0).getId())
                                        .withVerdict(Verdict.builder().withId(randomUUID())
                                                .withValue(VerdictValue.builder().withId(randomUUID())
                                                        .withCategory("No verdict")
                                                        .withCategoryType("NO_VERDICT")
                                                        .withCode("A1")
                                                        .withDescription("Jury unable to agree")
                                                        .withVerdictTypeId(randomUUID())
                                                ).withNumberOfJurors(integer(9, 12).next())
                                                .withNumberOfSplitJurors(integer(0, 3).next())
                                                .withUnanimous(BOOLEAN.next())
                                                .withVerdictDate(noVerditConvictionDate))))
                .build();

        final EventListener noVerditCategoryTypeListener = listenFor("public.hearing.verdict-updated").withFilter(
                isJson(withJsonPath("$.hearingId", is(initiateHearingCommand.getHearing().getId().toString()))));

        makeCommand(requestSpec, "hearing.initiate-hearing").ofType("application/vnd.hearing.update-verdict+json")
                .withArgs(initiateHearingCommand.getHearing().getId()).withPayload(noVerditCategoryTypeCommand)
                .executeSuccessfully();

        noVerditCategoryTypeListener.waitFor();

        final String noVerditCategoryTypeQueryAPIEndPoint = MessageFormat.format(
                ENDPOINT_PROPERTIES.getProperty("hearing.get.hearing.v2"), initiateHearingCommand.getHearing().getId());
        final String noVerditCategoryTypeUrl = getBaseUri() + "/" + noVerditCategoryTypeQueryAPIEndPoint;

        final String noVerditCategoryTypeResponseType = "application/vnd.hearing.get.hearing.v2+json";

        Verdict noVerditCategoryTypeVerdit = noVerditCategoryTypeCommand.getDefendants().get(0).getOffences().get(0)
                .getVerdict();

        poll(requestParams(noVerditCategoryTypeUrl, noVerditCategoryTypeResponseType)
                .withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                        .timeout(30,
                                TimeUnit.SECONDS)
                        .until(status().is(OK), print(), payload().isJson(allOf(
                                withJsonPath("$.hearingId", is(initiateHearingCommand.getHearing().getId().toString())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].id",
                                        equalStr(initiateHearingCommand, "hearing.defendants[0].offences[0].id")),

                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.category",
                                        is(noVerditCategoryTypeVerdit.getValue().getCategory())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.categoryType",
                                        is(noVerditCategoryTypeVerdit.getValue().getCategoryType())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.code",
                                        is(noVerditCategoryTypeVerdit.getValue().getCode())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.description",
                                        is(noVerditCategoryTypeVerdit.getValue().getDescription())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.value.verdictTypeId",
                                        is(noVerditCategoryTypeVerdit.getValue().getVerdictTypeId().toString())),
                                
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.verdictDate",
                                        is(noVerditCategoryTypeVerdit.getVerdictDate().toString())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.numberOfSplitJurors",
                                        is(noVerditCategoryTypeVerdit.getNumberOfSplitJurors())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.numberOfJurors",
                                        is(noVerditCategoryTypeVerdit.getNumberOfJurors())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].verdict.unanimous",
                                        is(noVerditCategoryTypeVerdit.getUnanimous())),

                                withJsonPath("$.cases[0].defendants[0].offences[0].convictionDate",
                                        is(guiltyConvictionDate.toString())))));

    }
}
