package uk.gov.moj.cpp.hearing.command.handler;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.DefendantId;
import uk.gov.moj.cpp.hearing.domain.aggregate.NewModelHearingAggregate;

import javax.inject.Inject;
import javax.json.JsonObject;
import java.util.List;
import java.util.UUID;

import static java.util.UUID.fromString;
import static java.util.stream.Collectors.toList;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

@ServiceComponent(COMMAND_HANDLER)
public class AddWitnessCommandHandler extends AbstractCommandHandler {

    @Inject
    public AddWitnessCommandHandler(final EventSource eventSource, final Enveloper enveloper,
                                    final AggregateService aggregateService, final JsonObjectToObjectConverter jsonObjectToObjectConverter) {
        super(eventSource, enveloper, aggregateService, jsonObjectToObjectConverter);
    }

    @Handles("hearing.command.add-witness")
    public void addWitness(final JsonEnvelope envelop) throws EventStreamException {
        final JsonObject payload = envelop.payloadAsJsonObject();
        final UUID witnessId = fromString(payload.getString("id"));
        final UUID hearingId = fromString(payload.getString("hearingId"));
        final String type = payload.getString("type");
        final String classification = payload.getString("classification");
        final String title = payload.getString("title");
        final String firstName = payload.getString("firstName");
        final String lastName = payload.getString("lastName");
        List<DefendantId> defendantIdList = payload.getJsonArray("defendantIds").getValuesAs(JsonObject.class).stream()
                .map(this::extractDefendantId)
                .collect(toList());

        aggregate(NewModelHearingAggregate.class, hearingId, envelop, a ->a.addWitness(hearingId, witnessId, type, classification, title, firstName, lastName, defendantIdList));
    }

    private DefendantId extractDefendantId(JsonObject jsonObject) {
        return DefendantId.builder().withDefendantId(fromString(jsonObject.getString("defendantId"))).build();
    }
}
