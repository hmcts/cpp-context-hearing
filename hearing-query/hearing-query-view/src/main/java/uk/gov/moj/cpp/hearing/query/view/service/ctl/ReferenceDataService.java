package uk.gov.moj.cpp.hearing.query.view.service.ctl;

import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataBuilder;

import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.MetadataBuilder;
import uk.gov.moj.cpp.hearing.query.view.service.ctl.model.PublicHoliday;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.json.JsonArray;
import javax.json.JsonObject;

public class ReferenceDataService {
    private static final String PUBLIC_HOLIDAYS = "publicHolidays";
    private static final String REFERENCEDATA_QUERY_PUBLIC_HOLIDAYS_NAME = "referencedata.query.public-holidays";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Inject
    @ServiceComponent(EVENT_PROCESSOR)
    private Requester requester;


    @SuppressWarnings({"squid:S1172", "squid:S1168"})
    public List<PublicHoliday> getPublicHolidays(final String division,
                                                 final LocalDate fromDate,
                                                 final LocalDate toDate) {

        final MetadataBuilder metadataBuilder = metadataBuilder()
                .withId(randomUUID())
                .withName(REFERENCEDATA_QUERY_PUBLIC_HOLIDAYS_NAME);

        final JsonObject params = getParams(division, fromDate, toDate);

        final Envelope<JsonObject> jsonObjectEnvelope = requester.requestAsAdmin(envelopeFrom(metadataBuilder, params), JsonObject.class);

        return transform(jsonObjectEnvelope);
    }

    private JsonObject getParams(final String division,
                                 final LocalDate fromDate,
                                 final LocalDate toDate) {
        return createObjectBuilder()
                .add("division", division)
                .add("dateFrom", fromDate.toString())
                .add("dateTo", toDate.toString())
                .build();
    }

    private List<PublicHoliday> transform(final Envelope<JsonObject> envelope) {
        final List<PublicHoliday> publicHolidays = new ArrayList();
        final JsonObject payload = envelope.payload();
        if (payload.containsKey(PUBLIC_HOLIDAYS)) {
            final JsonArray jsonArray = payload.getJsonArray(PUBLIC_HOLIDAYS);
            if (!jsonArray.isEmpty()) {
                final List<JsonObject> publicHolidaysArray = jsonArray.getValuesAs(JsonObject.class);
                for (final JsonObject pd : publicHolidaysArray) {
                    publicHolidays.add(toPublicHoliday(pd));
                }
            }
        }
        return publicHolidays;
    }

    private PublicHoliday toPublicHoliday(final JsonObject pd) {
        final UUID id = fromString(pd.getString("id"));
        final String title = pd.getString("title");
        final String division = pd.getString("division");
        final LocalDate date = LocalDate.parse(pd.getString("date"), DATE_FORMATTER);
        return new PublicHoliday(id, title, division, date);
    }
}
