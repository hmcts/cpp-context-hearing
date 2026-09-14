package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.UUID.fromString;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.persist.entity.ha.PtphDetail;
import uk.gov.moj.cpp.hearing.repository.PtphDetailRepository;

import java.util.UUID;

import javax.inject.Inject;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"squid:S2629"})
@ServiceComponent(EVENT_LISTENER)
public class PtphDetailEventListener {

    private static final String HEARING_ID = "hearingId";

    private static final Logger LOGGER = LoggerFactory.getLogger(PtphDetailEventListener.class.getName());

    @Inject
    private PtphDetailRepository repository;

    @Handles("hearing.ptph-detail-saved")
    public void ptphDetailSaved(final JsonEnvelope event) {
        LOGGER.debug("hearing.ptph-detail-saved received {}", event.toObfuscatedDebugString());
        final JsonObject payload = event.payloadAsJsonObject();
        final UUID hearingId = fromString(payload.getString(HEARING_ID));

        PtphDetail entity = repository.findBy(hearingId);
        if (entity == null) {
            entity = new PtphDetail();
            entity.setHearingId(hearingId);
        }
        entity.setTier(optionalString(payload, "tier"));
        entity.setListType(optionalString(payload, "listType"));
        entity.setKeyReason(optionalString(payload, "keyReason"));
        repository.save(entity);
    }

    @Handles("hearing.ptph-detail-finalised")
    public void ptphDetailFinalised(final JsonEnvelope event) {
        LOGGER.debug("hearing.ptph-detail-finalised received {}", event.toObfuscatedDebugString());
        final UUID hearingId = fromString(event.payloadAsJsonObject().getString(HEARING_ID));

        final PtphDetail entity = repository.findBy(hearingId);
        if (entity != null) {
            entity.setFinalised(true);
            repository.save(entity);
        }
    }

    @Handles("hearing.ptph-detail-deleted")
    public void ptphDetailDeleted(final JsonEnvelope event) {
        LOGGER.debug("hearing.ptph-detail-deleted received {}", event.toObfuscatedDebugString());
        final UUID hearingId = fromString(event.payloadAsJsonObject().getString(HEARING_ID));

        final PtphDetail entity = repository.findBy(hearingId);
        if (entity != null) {
            repository.removeAndFlush(entity);
        }
    }

    /**
     * Reads an optional string, tolerating a key that is absent <em>or</em> explicitly JSON
     * {@code null}. {@code JsonObject.getString} throws on a null value, and every hearing event
     * listener shares one queue — so a throw here would dead-letter the message and block later
     * events for unrelated hearings. Absent, null and any unexpected type all mean "not recorded".
     */
    private static String optionalString(final JsonObject payload, final String key) {
        final JsonValue value = payload.get(key);
        return value instanceof JsonString ? ((JsonString) value).getString() : null;
    }
}
