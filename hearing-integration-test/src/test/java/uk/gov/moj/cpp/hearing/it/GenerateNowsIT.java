package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.core.Is.is;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.http.RestPoller.poll;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.moj.cpp.hearing.it.TestUtilities.listenFor;
import static uk.gov.moj.cpp.hearing.it.TestUtilities.makeCommand;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.initiateHearingCommandTemplateWithOnlyMandatoryFields;

import java.io.IOException;

import org.junit.Test;

import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.nows.NowsMaterialStatusType;
import uk.gov.moj.cpp.hearing.command.nows.UpdateNowsMaterialStatusCommand;

@SuppressWarnings("unchecked")
public class GenerateNowsIT extends AbstractIT {

    @Test
    public void shouldAddNows() throws IOException {

        final InitiateHearingCommand initiateHearingCommand = initiateHearingCommandTemplateWithOnlyMandatoryFields().build();
        final String hearingId = initiateHearingCommand.getHearing().getId().toString();
        final String defendantId = randomUUID().toString();
        final String materialId = randomUUID().toString();
        final String nowsId = randomUUID().toString();
        final String nowsTypeId = randomUUID().toString();
        final String sharedResultId = randomUUID().toString();

        final TestUtilities.EventListener hearingInitiatedEventListener = listenFor("public.hearing.initiated")
                .withFilter(isJson(withJsonPath("$.hearingId", is(hearingId))));

        makeCommand(requestSpec, "hearing.initiate")
                .ofType("application/vnd.hearing.initiate+json")
                .withPayload(initiateHearingCommand)
                .executeSuccessfully();

        hearingInitiatedEventListener.waitFor();

        final TestUtilities.EventListener nowsRequestedEventListener = listenFor("public.hearing.events.nows-requested")
                .withFilter(isJson(withJsonPath("$.hearing.id", is(hearingId))));

        makeCommand(requestSpec, "hearing.generate-nows")
            .ofType("application/vnd.hearing.generate-nows+json")
            .withPayload(getGenerateNowsCommand(hearingId, defendantId, materialId, nowsId, nowsTypeId, sharedResultId))
            .executeSuccessfully();

        nowsRequestedEventListener.waitFor();

        poll(requestParams(getURL("hearing.get.nows", hearingId), "application/vnd.hearing.get.nows+json")
                .withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.nows[0].id", is(nowsId)),
                                withJsonPath("$.nows[0].defendantId", is(defendantId)),
                                withJsonPath("$.nows[0].nowsTypeId", is(nowsTypeId)),
                                withJsonPath("$.nows[0].material[0].id", is(materialId)),
                                withJsonPath("$.nows[0].material[0].language", is("welsh")),
                                withJsonPath("$.nows[0].material[0].status", is("Requested")),
                                withJsonPath("$.nows[0].nowResult[0].sharedResultId", is(sharedResultId)),
                                withJsonPath("$.nows[0].nowResult[0].sequence", is(1))
                        )));

        poll(requestParams(getURL("hearing.query.search-by-material-id", materialId), "application/vnd.hearing.query.search-by-material-id+json")
                .withHeader(CPP_UID_HEADER_AS_ADMIN.getName(), CPP_UID_HEADER_AS_ADMIN.getValue()).build())
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.allowedUserGroups[0]", is("courtAdmin")),
                                withJsonPath("$.allowedUserGroups[1]", is("defence"))
                        )));
    }

    @Test
    public void shouldUpdateNowsMaterialStatusToGenerated() throws IOException {

        final InitiateHearingCommand initiateHearingCommand = initiateHearingCommandTemplateWithOnlyMandatoryFields().build();
        final String hearingId = initiateHearingCommand.getHearing().getId().toString();
        final String defendantId = randomUUID().toString();
        final String materialId = randomUUID().toString();
        final String nowsId = randomUUID().toString();
        final String nowsTypeId = randomUUID().toString();
        final String sharedResultId = randomUUID().toString();

        final TestUtilities.EventListener hearingInitiatedEventListener = listenFor("public.hearing.initiated")
                .withFilter(isJson(withJsonPath("$.hearingId", is(hearingId))));

        makeCommand(requestSpec, "hearing.initiate")
            .ofType("application/vnd.hearing.initiate+json")
            .withPayload(initiateHearingCommand)
            .executeSuccessfully();

        hearingInitiatedEventListener.waitFor();

        final TestUtilities.EventListener nowsRequestedEventListener = listenFor("public.hearing.events.nows-requested")
                .withFilter(isJson(withJsonPath("$.hearing.id", is(hearingId))));

        makeCommand(requestSpec, "hearing.generate-nows")
            .ofType("application/vnd.hearing.generate-nows+json")
            .withPayload(getGenerateNowsCommand(hearingId, defendantId, materialId, nowsId, nowsTypeId, sharedResultId))
            .executeSuccessfully();

        nowsRequestedEventListener.waitFor();

        poll(requestParams(getURL("hearing.get.nows", hearingId), "application/vnd.hearing.get.nows+json")
                .withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(withJsonPath("$.nows[0].id", is(nowsId)),
                                withJsonPath("$.nows[0].material[0].id", is(materialId)),
                                withJsonPath("$.nows[0].material[0].status", is("Requested")))));

        final TestUtilities.EventListener nowsMaterialStatusUpdatedEventListener = listenFor("public.hearing.events.nows-material-status-updated")
                .withFilter(isJson(withJsonPath("$.materialId", is(materialId))));

        makeCommand(requestSpec, "hearing.update-nows-material-status")
            .withArgs(hearingId, nowsId)
            .ofType("application/vnd.hearing.update-nows-material-status+json")
            .withPayload(UpdateNowsMaterialStatusCommand.builder()
                    .withMaterialId(fromString(materialId))
                    .withStatus(NowsMaterialStatusType.GENERATED)
                    .build())
            .executeSuccessfully();

        poll(requestParams(getURL("hearing.get.nows", hearingId), "application/vnd.hearing.get.nows+json")
                .withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(withJsonPath("$.nows[0].id", is(nowsId)),
                                withJsonPath("$.nows[0].material[0].id", is(materialId)),
                                withJsonPath("$.nows[0].material[0].status", is("Generated")))));
      
        nowsMaterialStatusUpdatedEventListener.waitFor();
    }

    private static String getGenerateNowsCommand(final String hearingId, final String defendantId, final String materialId,
            final String nowsId, final String nowsTypeId, final String sharedResultId) throws IOException {
        return getStringFromResource("hearing.generate-nows.json")
                .replace("HEARING_ID", hearingId)
                .replace("DEFENDANT_ID", defendantId)
                .replace("NOW_ID", nowsId)
                .replace("MATERIAL_ID", materialId)
                .replace("NOWTYPE_ID", nowsTypeId)
                .replace("RESULT_ID", sharedResultId);
    }
}