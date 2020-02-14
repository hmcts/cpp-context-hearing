package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.http.RestPoller.poll;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.moj.cpp.hearing.it.Utilities.makeCommand;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.UploadSubscriptionsCommandTemplates.buildUploadSubscriptionsCommand;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_SEC;

import uk.gov.moj.cpp.hearing.command.subscription.UploadSubscriptionsCommand;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class UploadSubscriptionsIT extends AbstractIT {

    @Test
    public void testRetrieveSubscriptionsAfterUploading() {

        final UploadSubscriptionsCommand command = buildUploadSubscriptionsCommand();

        final String referenceDate = LocalDate.now().format(DateTimeFormatter.ofPattern("ddMMyyyy"));

        final String nowTypeId = command.getSubscriptions().get(0).getNowTypeIds().get(0).toString();

        makeCommand(requestSpec, "hearing.upload-subscriptions")
                .ofType("application/vnd.hearing.upload-subscriptions+json")
                .withPayload(command)
                .withArgs(referenceDate)
                .executeSuccessfully();

        poll(requestParams(getURL("hearing.retrieve-subscriptions", referenceDate, nowTypeId), "application/vnd.hearing.retrieve-subscriptions+json")
                .withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.subscriptions[0].channel", is(command.getSubscriptions().get(0).getChannel())),
                                withJsonPath("$.subscriptions[0].destination", is(command.getSubscriptions().get(0).getDestination())),
                                withJsonPath("$.subscriptions[0].userGroups[*]", hasSize(greaterThan(0))),
                                withJsonPath("$.subscriptions[0].courtCentreIds[*]", hasSize(greaterThan(0))),
                                withJsonPath("$.subscriptions[0].nowTypeIds[*]", hasSize(greaterThan(0)))
                        )));
    }
}