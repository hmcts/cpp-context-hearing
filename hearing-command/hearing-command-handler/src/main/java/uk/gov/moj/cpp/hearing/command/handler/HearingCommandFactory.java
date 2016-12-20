package uk.gov.moj.cpp.hearing.command.handler;

import static java.util.stream.Collectors.toList;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjects;
import uk.gov.moj.cpp.hearing.domain.HearingEventDefinition;
import uk.gov.moj.cpp.hearing.domain.HearingTypeEnum;
import uk.gov.moj.cpp.hearing.domain.command.CreateHearingEventDefinitions;
import uk.gov.moj.cpp.hearing.domain.command.ListHearing;
import uk.gov.moj.cpp.hearing.domain.command.VacateHearing;

import java.time.LocalDate;
import java.util.List;

import javax.json.JsonArray;
import javax.json.JsonObject;

public class HearingCommandFactory {

    public ListHearing getListHearing(final JsonEnvelope command) {

        final JsonObject payload = command.payloadAsJsonObject();
        HearingTypeEnum hearingType = HearingTypeEnum.getHearingType(payload.getString("hearingType"));
        LocalDate dateOfSending = LocalDate.parse(payload.getString("dateOfSending"));
        return new ListHearing(JsonObjects.getUUID(payload, "hearingId").get(),
                JsonObjects.getUUID(payload, "caseId").get(), hearingType, payload.getString("courtCentreName"),
                dateOfSending, payload.getInt("duration"));
    }

    public VacateHearing getVacateHearing(final JsonEnvelope command) {
        final JsonObject payload = command.payloadAsJsonObject();
        return new VacateHearing(JsonObjects.getUUID(payload, "hearingId").get());
    }

    public CreateHearingEventDefinitions createHearingEventDefinitionsFrom(final JsonObject payload) {
        return new CreateHearingEventDefinitions(
                JsonObjects.getUUID(payload, "id").get(),
                hearingDefinitions(payload.getJsonArray("eventDefinitions")));
    }

    private List<HearingEventDefinition> hearingDefinitions(final JsonArray hearingDefinitionsJson) {
        return hearingDefinitionsJson.getValuesAs(JsonObject.class).stream()
                .map(hearingDefinitionJson -> new HearingEventDefinition(
                        hearingDefinitionJson.getString("actionLabel"),
                        hearingDefinitionJson.getString("recordedLabel")
                )).collect(toList());
    }
}
