package uk.gov.moj.cpp.hearing.command.handler;

import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjects;
import uk.gov.moj.cpp.hearing.domain.HearingTypeEnum;
import uk.gov.moj.cpp.hearing.domain.command.ListHearing;
import uk.gov.moj.cpp.hearing.domain.command.VacateHearing;

import javax.json.JsonObject;
import java.time.LocalDate;

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
}
