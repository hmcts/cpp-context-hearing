package uk.gov.justice.api.resource;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.HttpHeaders.CONTENT_DISPOSITION;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.Response.Status.OK;
import static javax.ws.rs.core.Response.status;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataBuilder;

import uk.gov.justice.services.adapter.rest.mapping.ActionMapper;
import uk.gov.justice.services.adapter.rest.multipart.FileInputDetailsFactory;
import uk.gov.justice.services.core.annotation.Adapter;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.dispatcher.SystemUserProvider;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.query.view.HearingEventQueryView;
import uk.gov.moj.cpp.system.documentgenerator.client.DocumentGeneratorClientProducer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

@Adapter(Component.QUERY_API)
public class DefaultQueryApiHearingsEventLogExtractResource implements QueryApiHearingsEventLogExtractResource {

    @Inject
    @Named("DefaultQueryApiHearingsEventLogExtractResourceActionMapper")
    ActionMapper actionMapper;

    @Context
    HttpHeaders headers;

    @Inject
    FileInputDetailsFactory fileInputDetailsFactory;

    @Inject
    private DocumentGeneratorClientProducer documentGeneratorClientProducer;

    @Inject
    private HearingEventQueryView hearingEventQueryView;

    @Inject
    private SystemUserProvider systemUserProvider;

    private static final String PDF_MIME_TYPE = "application/pdf";
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final String FILE_NAME = format("HearingEventLog_%s.pdf", ZonedDateTime.now().format(TIMESTAMP_FORMATTER));
    public static final String TEMPLATE_NAME = "HearingEventLog";
    private static final String PDF_DISPOSITION = "attachment; filename=\"" + FILE_NAME + "\"";

    @Override
    public Response getHearingsEventLogExtract(final String caseId, final String hearingId, final String applicationId, final String hearingDate, final UUID userId) throws IOException {

        final JsonObjectBuilder payloadBuilder = Json.createObjectBuilder();

        if (nonNull(hearingId)) {
            payloadBuilder.add("hearingId", hearingId);
        }
        if (nonNull(caseId)) {
            payloadBuilder.add("caseId", caseId);
        }
        if (nonNull(applicationId)) {
            payloadBuilder.add("applicationId", applicationId);
        }
        if (nonNull(hearingDate)) {
            payloadBuilder.add("hearingDate", hearingDate);
        }

        final JsonEnvelope documentQuery = envelopeFrom(
                metadataBuilder()
                        .withId(randomUUID())
                        .withName("hearing.get-hearing-event-log-extract-for-documents")
                        .withUserId(userId.toString())
                        .build(),
                payloadBuilder.build());

        final Envelope<JsonObject> envelope = hearingEventQueryView.getHearingEventLogForDocuments(documentQuery);

        final UUID systemUserId = systemUserProvider.getContextSystemUserId().orElseThrow(() -> new RuntimeException("systemUserProvider.getContextSystemUserId() not available"));
        final byte[] hearingEventLogInputStream = this.documentGeneratorClientProducer.documentGeneratorClient().generatePdfDocument(envelope.payload(), TEMPLATE_NAME, systemUserId);

        final InputStream documentInputStream = new ByteArrayInputStream(hearingEventLogInputStream);

        final Response.ResponseBuilder responseBuilder = status(OK).entity(documentInputStream);

        return responseBuilder
                .header(CONTENT_TYPE, PDF_MIME_TYPE)
                .header(CONTENT_DISPOSITION, PDF_DISPOSITION)
                .build();
    }
}

