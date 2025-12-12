package uk.gov.moj.cpp.hearing.command.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.aggregate.ApplicationAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.CaseAggregate;

import javax.json.JsonObject;
import javax.json.JsonString;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.nonNull;
import static java.util.UUID.fromString;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

@ServiceComponent(COMMAND_HANDLER)
public class EjectCaseOrApplicationCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(EjectCaseOrApplicationCommandHandler.class.getName());
    private static final String HEARING_IDS = "hearingIds";
    private static final String PROSECUTION_CASE_ID = "prosecutionCaseId";
    private static final String APPLICATION_ID = "applicationId";

    @Handles("hearing.command.eject-case-or-application")
    public void ejectCaseOrApplication(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("hearing.command.eject-case-or-application received {}", envelope.toObfuscatedDebugString());
        }
        final List<UUID> hearingIds = new ArrayList<>();
        final JsonObject payload = envelope.payloadAsJsonObject();
        if(null != payload.getJsonArray(HEARING_IDS)) {

            payload.getJsonArray(HEARING_IDS).getValuesAs(JsonString.class)
                    .stream().map(JsonString::getString)
                    .map(UUID::fromString)
                    .forEach(hearingId->hearingIds.add(hearingId));

        }
        if (nonNull(payload.getString(PROSECUTION_CASE_ID, null))) {
            final UUID prosecutionCaseId = fromString(payload.getString(PROSECUTION_CASE_ID));
            aggregate(CaseAggregate.class, prosecutionCaseId, envelope, a -> a.ejectCase(prosecutionCaseId, hearingIds));

        } else {
            final UUID applicationId = fromString(payload.getString(APPLICATION_ID));
            aggregate(ApplicationAggregate.class, applicationId, envelope, a -> a.ejectApplication(applicationId, hearingIds));
        }
    }
}
