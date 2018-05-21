package uk.gov.moj.cpp.hearing.it;

import org.junit.Test;
import uk.gov.justice.progression.events.CaseDefendantOffencesChanged;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.initiate.Offence;

import java.text.MessageFormat;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.services.test.utils.core.http.BaseUriProvider.getBaseUri;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.http.RestPoller.poll;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.INTEGER;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.it.UseCases.addOffence;
import static uk.gov.moj.cpp.hearing.it.UseCases.asDefault;
import static uk.gov.moj.cpp.hearing.it.UseCases.deleteOffence;
import static uk.gov.moj.cpp.hearing.it.UseCases.initiateHearing;
import static uk.gov.moj.cpp.hearing.it.UseCases.updateOffence;

@SuppressWarnings("unchecked")
public class CaseDefendantOffencesChangedIT extends AbstractIT {

    @Test
    public void caseDefendantOffencesChanged_addOffenceToExistingHearing() throws Exception {

        final InitiateHearingCommand initiateHearing = initiateHearing(requestSpec, asDefault());

        final CaseDefendantOffencesChanged caseDefendantOffencesChanged = addOffence(initiateHearing);

        final String queryAPIEndPoint = MessageFormat.format(ENDPOINT_PROPERTIES.getProperty("hearing.get.hearing.v2"), initiateHearing.getHearing().getId());

        final String url = getBaseUri() + "/" + queryAPIEndPoint;

        final String responseType = "application/vnd.hearing.get.hearing.v2+json";

        poll(requestParams(url, responseType).withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearingId", is(initiateHearing.getHearing().getId().toString())),
                                withJsonPath("$.cases[0].caseId", is(initiateHearing.getCases().get(0).getCaseId().toString())),
                                withJsonPath("$.cases[0].caseUrn", is(initiateHearing.getCases().get(0).getUrn())),
                                withJsonPath("$.cases[0].defendants[0].defendantId", is(initiateHearing.getHearing().getDefendants().get(0).getId().toString())),
                                withJsonPath("$.cases[0].defendants[0].offences[1].id", is(caseDefendantOffencesChanged.getAddedOffences().get(0).getAddedOffences().get(0).getId().toString())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].id", is(initiateHearing.getHearing().getDefendants().get(0).getOffences().get(0).getId().toString()))
                        )));
    }

    @Test
    public void caseDefendantOffencesChanged_updateExistingOffence() throws Exception {

        final InitiateHearingCommand initiateHearing = initiateHearing(requestSpec, asDefault());

        final CaseDefendantOffencesChanged caseDefendantOffencesChanged = updateOffence(initiateHearing);

        final String queryAPIEndPoint = MessageFormat.format(ENDPOINT_PROPERTIES.getProperty("hearing.get.hearing.v2"), initiateHearing.getHearing().getId());

        final String url = getBaseUri() + "/" + queryAPIEndPoint;

        final String responseType = "application/vnd.hearing.get.hearing.v2+json";

        poll(requestParams(url, responseType).withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearingId", is(initiateHearing.getHearing().getId().toString())),
                                withJsonPath("$.cases[0].caseId", is(initiateHearing.getCases().get(0).getCaseId().toString())),
                                withJsonPath("$.cases[0].caseUrn", is(initiateHearing.getCases().get(0).getUrn())),
                                withJsonPath("$.cases[0].defendants[0].defendantId", is(initiateHearing.getHearing().getDefendants().get(0).getId().toString())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].id", is(caseDefendantOffencesChanged.getUpdatedOffences().get(0).getId().toString()))
                        )));
    }

    @Test
    public void caseDefendantOffencesChanged_deleteExistingOffence() throws Exception {


        final InitiateHearingCommand initiateHearing = initiateHearing(requestSpec, (i) -> i.getHearing()
                .getDefendants()
                .get(0)
                .addOffence(
                        Offence.builder()
                                .withId(randomUUID())
                                .withCaseId(i.getCases().get(0).getCaseId())
                                .withOffenceCode(STRING.next())
                                .withWording(STRING.next())
                                .withSection(STRING.next())
                                .withStartDate(PAST_LOCAL_DATE.next())
                                .withEndDate(PAST_LOCAL_DATE.next())
                                .withOrderIndex(INTEGER.next())
                                .withCount(INTEGER.next())
                                .withConvictionDate(PAST_LOCAL_DATE.next())
                                .withLegislation(STRING.next())
                                .withTitle(STRING.next())
                ));


        deleteOffence(initiateHearing);

        final String queryAPIEndPoint = MessageFormat.format(ENDPOINT_PROPERTIES.getProperty("hearing.get.hearing.v2"), initiateHearing.getHearing().getId());

        final String url = getBaseUri() + "/" + queryAPIEndPoint;

        final String responseType = "application/vnd.hearing.get.hearing.v2+json";

        poll(requestParams(url, responseType).withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.hearingId", is(initiateHearing.getHearing().getId().toString())),
                                withJsonPath("$.cases[0].caseId", is(initiateHearing.getCases().get(0).getCaseId().toString())),
                                withJsonPath("$.cases[0].caseUrn", is(initiateHearing.getCases().get(0).getUrn())),
                                withJsonPath("$.cases[0].defendants[0].defendantId", is(initiateHearing.getHearing().getDefendants().get(0).getId().toString())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].id", is(initiateHearing.getHearing().getDefendants().get(0).getOffences().get(1).getId().toString()))
                        )));
    }
}