package uk.gov.moj.cpp.hearing.event.nows;

import static java.util.UUID.fromString;
import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.services.common.converter.JSONObjectValueObfuscator;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.event.nows.order.NowsDocumentOrder;
import uk.gov.moj.cpp.hearing.event.nows.service.NowGeneratorService;
import uk.gov.moj.cpp.hearing.nows.events.NowsRequested;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ServiceComponent(EVENT_PROCESSOR)
public class NowsRequestedEventProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(NowsRequestedEventProcessor.class);


    private final Enveloper enveloper;
    private final Sender sender;
    private final JsonObjectToObjectConverter jsonObjectToObjectConverter;
    private final ObjectToJsonObjectConverter objectToJsonObjectConverter;

    private NowGeneratorService nowGeneratorService;

    @Inject
    public NowsRequestedEventProcessor(final Enveloper enveloper, final Sender sender,
                                       final NowGeneratorService nowGeneratorService,
                                       final JsonObjectToObjectConverter jsonObjectToObjectConverter,
                                       final ObjectToJsonObjectConverter objectToJsonObjectConverter
                                       ) {
        this.enveloper = enveloper;
        this.sender = sender;
        this.jsonObjectToObjectConverter = jsonObjectToObjectConverter;
        this.objectToJsonObjectConverter = objectToJsonObjectConverter;
        this.nowGeneratorService = nowGeneratorService;
    }

    @Handles("hearing.events.nows-requested")
    public void processNowsRequested(final JsonEnvelope event) {
        UUID userId = fromString(event.metadata().userId().orElseThrow(() -> new RuntimeException("UserId missing from event.")));

        final NowsRequested nowsRequested = jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), NowsRequested.class);
        final String hearingId = nowsRequested.getHearing().getId().toString();
        LOGGER.info("Nows requested for hearing id {}", hearingId);

        final Map<NowsDocumentOrder, NowsNotificationDocumentState> nowsDocumentOrderToNotificationState = NowsRequestedToOrderConverter.convert(nowsRequested);
        final List<NowsDocumentOrder> nowsDocumentOrdersList = new ArrayList<>(nowsDocumentOrderToNotificationState.keySet());
        nowsDocumentOrdersList.stream().sorted(Comparator.comparing(NowsDocumentOrder::getPriority)).forEach(nowsDocumentOrder -> {
            LOGGER.info("Input for docmosis order {}", JSONObjectValueObfuscator.obfuscated(objectToJsonObjectConverter.convert(nowsDocumentOrder)));

            nowGeneratorService.generateNow(sender, userId, nowsRequested, hearingId, nowsDocumentOrderToNotificationState, nowsDocumentOrder);
        });

        this.sender.send(this.enveloper.withMetadataFrom(event, "public.hearing.events.nows-requested")
                .apply(this.objectToJsonObjectConverter.convert(nowsRequested)));
    }

    @Handles("hearing.events.nows-material-status-updated")
    public void propagateNowsMaterialStatusUpdated(final JsonEnvelope envelope) {
        this.sender.send(this.enveloper.withMetadataFrom(envelope, "public.hearing.events.nows-material-status-updated")
                .apply(createObjectBuilder()
                        .add("materialId", envelope.payloadAsJsonObject().getJsonString("materialId"))
                        .build()
                ));
    }
}

