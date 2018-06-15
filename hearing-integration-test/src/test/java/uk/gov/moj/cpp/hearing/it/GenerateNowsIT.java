package uk.gov.moj.cpp.hearing.it;

import static com.github.tomakehurst.wiremock.client.WireMock.findAll;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
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
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.minimalInitiateHearingTemplate;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.json.Json;
import javax.json.JsonObject;

import org.junit.After;
import org.junit.Test;

import com.jayway.awaitility.Awaitility;

import uk.gov.justice.services.messaging.JsonObjectMetadata;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;

import uk.gov.moj.cpp.hearing.command.nows.NowsMaterialStatusType;
import uk.gov.moj.cpp.hearing.command.nows.UpdateNowsMaterialStatusCommand;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;
import uk.gov.moj.cpp.hearing.utils.QueueUtil;
import uk.gov.moj.cpp.hearing.utils.WireMockStubUtils;

@SuppressWarnings("unchecked")
public class GenerateNowsIT extends AbstractIT {

    private static final MessageProducer messageProducerClientPublic = QueueUtil.publicEvents.createProducer();

    @Test
    public void shouldAddUpdateNows() throws IOException {

        final InitiateHearingCommandHelper hearing = h(UseCases.initiateHearing(requestSpec, minimalInitiateHearingTemplate()));

        final String userId = randomUUID().toString();
        final String materialId = randomUUID().toString();
        final String nowsId = randomUUID().toString();
        final String nowsTypeId = randomUUID().toString();
        final String sharedResultId = randomUUID().toString();

        final TestUtilities.EventListener nowsRequestedEventListener = listenFor("public.hearing.events.nows-requested")
                .withFilter(isJson(withJsonPath("$.hearing.id", is(hearing.getHearingId().toString()))));

        makeCommand(requestSpec, "hearing.generate-nows")
                .ofType("application/vnd.hearing.generate-nows+json")
                .withPayload(getGenerateNowsCommand(
                        hearing.getHearingId().toString(),
                        hearing.getFirstDefendantId().toString(),
                        materialId, nowsId, nowsTypeId, sharedResultId))
                .executeSuccessfully();
        // ensure upload material and update status called
        Awaitility.await().atMost(30, TimeUnit.SECONDS)
                .until(this::uploadMaterialCalled);
        sendMaterialFileUploadedPublicEvent(materialId, userId);
        Awaitility.await().atMost(30, TimeUnit.SECONDS).until(this::updateMaterialStatusHmpsCalled);
        sendHearingHmpsMaterialStatusUpdatedPublicMessage(materialId, userId, hearing.getHearingId().toString());

        nowsRequestedEventListener.waitFor();

        poll(requestParams(getURL("hearing.get.nows", hearing.getHearingId().toString()), "application/vnd.hearing.get.nows+json")
                .withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.nows[0].id", is(nowsId)),
                                withJsonPath("$.nows[0].defendantId", is(hearing.getFirstDefendantId().toString())),
                                withJsonPath("$.nows[0].nowsTypeId", is(nowsTypeId)),
                                withJsonPath("$.nows[0].material[0].id", is(materialId)),
                                withJsonPath("$.nows[0].material[0].language", is("welsh")),
                                withJsonPath("$.nows[0].material[0].status", is("Generated")),
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

        final InitiateHearingCommandHelper hearing = h(UseCases.initiateHearing(requestSpec, minimalInitiateHearingTemplate()));

        final String materialId = randomUUID().toString();
        final String nowsId = randomUUID().toString();
        final String nowsTypeId = randomUUID().toString();
        final String sharedResultId = randomUUID().toString();

        final TestUtilities.EventListener nowsRequestedEventListener = listenFor("public.hearing.events.nows-requested")
                .withFilter(isJson(withJsonPath("$.hearing.id", is(hearing.getHearingId().toString()))));

        makeCommand(requestSpec, "hearing.generate-nows")
                .ofType("application/vnd.hearing.generate-nows+json")
                .withPayload(getGenerateNowsCommand(hearing.getHearingId().toString(),
                        hearing.getFirstDefendantId().toString(),
                        materialId, nowsId, nowsTypeId, sharedResultId))
                .executeSuccessfully();

        nowsRequestedEventListener.waitFor();

        poll(requestParams(getURL("hearing.get.nows", hearing.getHearingId().toString()), "application/vnd.hearing.get.nows+json")
                .withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(withJsonPath("$.nows[0].id", is(nowsId)),
                                withJsonPath("$.nows[0].material[0].id", is(materialId)),
                                withJsonPath("$.nows[0].material[0].status", is("Requested")))));

        final TestUtilities.EventListener nowsMaterialStatusUpdatedEventListener = listenFor("public.hearing.events.nows-material-status-updated")
                .withFilter(isJson(withJsonPath("$.materialId", is(materialId))));

        makeCommand(requestSpec, "hearing.update-nows-material-status")
                .withArgs(hearing.getHearingId().toString(), nowsId)
                .ofType("application/vnd.hearing.update-nows-material-status+json")
                .withPayload(UpdateNowsMaterialStatusCommand.builder()
                        .withMaterialId(fromString(materialId))
                        .withStatus(NowsMaterialStatusType.GENERATED)
                        .build())
                .executeSuccessfully();

        poll(requestParams(getURL("hearing.get.nows", hearing.getHearingId().toString()), "application/vnd.hearing.get.nows+json")
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

    private boolean uploadMaterialCalled() {
        return (findAll(postRequestedFor(
                urlPathMatching(WireMockStubUtils.MATERIAL_UPLOAD_COMMAND)))
                .size() == 1);
    }

    private boolean updateMaterialStatusHmpsCalled() {
        return (findAll(postRequestedFor(
                urlPathMatching(WireMockStubUtils.MATERIAL_STATUS_UPLOAD_COMMAND)))
                .size() == 1);
    }

    private void sendMaterialFileUploadedPublicEvent(final String materialId, final String userId) {
        final String commandName = "material.material-added";
        final Metadata metadata = JsonObjectMetadata.metadataOf(UUID.randomUUID(), commandName)
                .withUserId(userId).build();
        final JsonObject payload = Json.createObjectBuilder().add("materialId", materialId).add(
                "fileDetails",
                Json.createObjectBuilder().add("alfrescoAssetId", "aGVsbG8=")
                        .add("mimeType", "text/plain").add("fileName", "file.txt"))
                .add("materialAddedDate", "2016-04-26T13:01:787.345").build();
        QueueUtil.sendMessage(messageProducerClientPublic, commandName, payload, metadata);
    }

    private void sendHearingHmpsMaterialStatusUpdatedPublicMessage(final String materialId,
                                                                   final String userId, final String hearingId) {
        final String commandName = "public.resultinghmps.event.nows-material-status-updated";
        final Metadata metadata = JsonObjectMetadata.metadataOf(UUID.randomUUID(), commandName)
                .withUserId(userId).build();
        final JsonObject payload = Json.createObjectBuilder().add("materialId", materialId)
                .add("hearingId", hearingId).add("status", "generated").build();
        QueueUtil.sendMessage(messageProducerClientPublic, commandName, payload, metadata);
    }

    @After
    public void tearDown() throws JMSException {
        messageProducerClientPublic.close();
    }
}