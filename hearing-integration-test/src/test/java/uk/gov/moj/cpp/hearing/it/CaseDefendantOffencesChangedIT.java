package uk.gov.moj.cpp.hearing.it;

import org.junit.Test;
import uk.gov.moj.cpp.hearing.command.offence.CaseDefendantOffencesChangedCommand;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;
import uk.gov.moj.cpp.hearing.test.TestTemplates;

import java.util.concurrent.TimeUnit;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.http.RestPoller.poll;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.moj.cpp.hearing.it.UseCases.addOffence;
import static uk.gov.moj.cpp.hearing.it.UseCases.deleteOffence;
import static uk.gov.moj.cpp.hearing.it.UseCases.updateOffence;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.offenceTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;

@SuppressWarnings("unchecked")
public class CaseDefendantOffencesChangedIT extends AbstractIT {

    @Test
    public void caseDefendantOffencesChanged_addOffenceToExistingHearing() throws Exception {

        final InitiateHearingCommandHelper hearingOne = new InitiateHearingCommandHelper(
                UseCases.initiateHearing(requestSpec, standardInitiateHearingTemplate())
        );

        final CaseDefendantOffencesChangedCommand caseDefendantOffencesChanged = addOffence(hearingOne.getHearingId(), command -> {
            command.getAddedOffences().forEach(addedOffence -> {
                addedOffence
                        .setCaseId(hearingOne.getFirstCaseId())
                        .setDefendantId(hearingOne.getFirstDefendantId());
            });
        });

        poll(requestParams(getURL("hearing.get.hearing.v2", hearingOne.getHearingId()), "application/vnd.hearing.get.hearing.v2+json")
                .withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .timeout(30, TimeUnit.SECONDS)
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearingId", is(hearingOne.getHearingId().toString())),
                                withJsonPath("$.cases[0].caseId", is(hearingOne.getFirstCaseId().toString())),
                                withJsonPath("$.cases[0].caseUrn", is(hearingOne.getFirstCaseUrn())),
                                withJsonPath("$.cases[0].defendants[0].defendantId", is(hearingOne.getFirstDefendantId().toString())),
                                withJsonPath("$.cases[0].defendants[0].offences", hasSize(2)),
                                withJsonPath("$.cases[0].defendants[0].offences[*].id", hasItems(
                                        caseDefendantOffencesChanged.getAddedOffences().get(0).getOffences().get(0).getId().toString(),
                                        hearingOne.getFirstOffenceIdForFirstDefendant().toString()))
                        )));
    }

    @Test
    public void caseDefendantOffencesChanged_updateExistingOffence() throws Exception {

        final InitiateHearingCommandHelper hearingOne = new InitiateHearingCommandHelper(
                UseCases.initiateHearing(requestSpec, standardInitiateHearingTemplate())
        );

        final CaseDefendantOffencesChangedCommand caseDefendantOffencesChanged = updateOffence(hearingOne.getHearingId(), command -> {
            command.getUpdatedOffences().forEach(updatedOffence -> {
                updatedOffence.setId(hearingOne.getFirstOffenceIdForFirstDefendant());
            });
        });

        //TODO - add more assertions on offence details here.
        poll(requestParams(getURL("hearing.get.hearing.v2", hearingOne.getHearingId()), "application/vnd.hearing.get.hearing.v2+json")
                .withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .timeout(30, TimeUnit.SECONDS)
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearingId", is(hearingOne.getHearingId().toString())),
                                withJsonPath("$.cases[0].caseId", is(hearingOne.getFirstCaseId().toString())),
                                withJsonPath("$.cases[0].caseUrn", is(hearingOne.getFirstCaseUrn())),
                                withJsonPath("$.cases[0].defendants[0].defendantId", is(hearingOne.getFirstDefendantId().toString())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].id", is(caseDefendantOffencesChanged.getUpdatedOffences().get(0).getId().toString()))
                        )));
    }

    @Test
    public void caseDefendantOffencesChanged_deleteExistingOffence() throws Exception {

        final InitiateHearingCommandHelper hearingOne = new InitiateHearingCommandHelper(
                UseCases.initiateHearing(requestSpec, with(standardInitiateHearingTemplate(), i -> {
                    i.getHearing().getDefendants().get(0).getOffences().add(
                            offenceTemplate(i.getCases().get(0).getCaseId())
                    );
                }))
        );

        deleteOffence(hearingOne.getHearingId(), command -> {
            command.getDeletedOffences().forEach(deletedOffence -> {
                deletedOffence.setId(hearingOne.getFirstOffenceIdForFirstDefendant());
            });
        });

        poll(requestParams(getURL("hearing.get.hearing.v2", hearingOne.getHearingId()), "application/vnd.hearing.get.hearing.v2+json")
                .withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .timeout(30, TimeUnit.SECONDS)
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearingId", is(hearingOne.getHearingId().toString())),
                                withJsonPath("$.cases[0].caseId", is(hearingOne.getFirstCaseId().toString())),
                                withJsonPath("$.cases[0].caseUrn", is(hearingOne.getFirstCaseUrn())),
                                withJsonPath("$.cases[0].defendants[0].defendantId", is(hearingOne.getFirstDefendantId().toString())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].id", is(hearingOne.getSecondOffenceIdForFirstDefendant().toString()))
                        )));
    }
}