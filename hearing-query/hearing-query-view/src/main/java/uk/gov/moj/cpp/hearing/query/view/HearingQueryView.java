package uk.gov.moj.cpp.hearing.query.view;

import uk.gov.justice.services.common.converter.ListToJsonArrayConverter;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.dispatcher.Requester;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjects;
import uk.gov.moj.cpp.hearing.persist.entity.DefenceCounsel;
import uk.gov.moj.cpp.hearing.persist.entity.DefenceCounselDefendant;
import uk.gov.moj.cpp.hearing.persist.entity.ProsecutionCounsel;
import uk.gov.moj.cpp.hearing.query.view.convertor.DefenceCounselToDefendantMapConverter;
import uk.gov.moj.cpp.hearing.query.view.convertor.ProsecutionCounselListConverter;
import uk.gov.moj.cpp.hearing.query.view.service.DefenceCounselService;
import uk.gov.moj.cpp.hearing.query.view.service.HearingEventDefinitionService;
import uk.gov.moj.cpp.hearing.query.view.service.HearingService;
import uk.gov.moj.cpp.hearing.query.view.service.ProsecutionCounselService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonString;

@ServiceComponent(Component.QUERY_VIEW)
public class HearingQueryView {

    static final String FIELD_HEARING_ID = "hearingId";
    static final String FIELD_START_DATE = "startDate";
    static final String NAME_RESPONSE_HEARING_LIST = "hearing.get.hearings-by-startdate-response";
    static final String NAME_RESPONSE_HEARING = "hearing.get.hearing-response";
    private static final String HEARING_QUERY_HEARING_EVENT_DEFINITIONS_RESPONSE = "hearing.query.hearing-event-definitions-response";

    @Inject
    private Requester requester;

    @Inject
    private ListToJsonArrayConverter helperService;

    @Inject
    private HearingService hearingService;

    @Inject
    private ProsecutionCounselService prosecutionCounselService;

    @Inject
    private DefenceCounselService defenceCounselService;

    @Inject
    private HearingEventDefinitionService hearingEventDefinitionService;

    @Inject
    private Enveloper enveloper;

    @Inject
    private ProsecutionCounselListConverter prosecutionCounselsListConverter;

    @Inject
    private DefenceCounselToDefendantMapConverter defenceCounselToDefendantMapConverter;

    @Handles("hearing.get.hearings-by-startdate")
    public JsonEnvelope findHearings(final JsonEnvelope envelope) {
        Optional<JsonString> startDate =
                JsonObjects.getJsonString(envelope.payloadAsJsonObject(), FIELD_START_DATE);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-M-d");
        LocalDate localDate = LocalDate.parse(startDate.get().getChars(),formatter);
        return enveloper.withMetadataFrom(envelope, NAME_RESPONSE_HEARING_LIST)
                .apply(Json.createObjectBuilder().add("hearings",
                        helperService.convert(hearingService.getHearingsByStartDate(
                                localDate)))
                        .build());
    }

    @Handles("hearing.get.hearing")
    public JsonEnvelope findHearing(final JsonEnvelope envelope) {
        Optional<UUID> hearingId =
                JsonObjects.getUUID(envelope.payloadAsJsonObject(), FIELD_HEARING_ID);
        return enveloper.withMetadataFrom(envelope, NAME_RESPONSE_HEARING)
                .apply(hearingService.getHearingById(hearingId.get()));
    }

    @Handles("hearing.get.prosecution-counsels")
    public JsonEnvelope getProsecutionCounsels(final JsonEnvelope query) {

        final UUID hearingId = UUID.fromString(query
                .payloadAsJsonObject()
                .getString(FIELD_HEARING_ID));

        final List<ProsecutionCounsel> prosecutionCounsels =
                prosecutionCounselService.getProsecutionCounselsByHearingId(hearingId);

        return enveloper.withMetadataFrom(query, "hearing.get.prosecution-counsels").apply(payloadFrom(prosecutionCounsels));
    }

    @Handles("hearing.get.defence-counsels")
    public JsonEnvelope getDefenceCounsels(final JsonEnvelope query) {

        final UUID hearingId = UUID.fromString(query
                .payloadAsJsonObject()
                .getString(FIELD_HEARING_ID));

        final List<DefenceCounsel> defenceCounsels =
                defenceCounselService.getDefenceCounselsByHearingId(hearingId);

        final Map<DefenceCounsel, List<DefenceCounselDefendant>> defenceCounselsAndDefendantsMap = new HashMap<>();

        for (DefenceCounsel defenceCounsel: defenceCounsels) {
            defenceCounselsAndDefendantsMap.put(defenceCounsel,
                    defenceCounselService.getDefenceCounselDefendantsByDefenceCounselAttendeeId(defenceCounsel.getId()));
        }

        return enveloper.withMetadataFrom(query, "hearing.get.defence-counsels").apply(payloadFrom(defenceCounselsAndDefendantsMap));
    }

    private JsonObject payloadFrom(final Map<DefenceCounsel, List<DefenceCounselDefendant>> defenceCounselsAndDefendantsMap) {
        return defenceCounselToDefendantMapConverter.convert(defenceCounselsAndDefendantsMap);
    }

    private JsonObject payloadFrom(final List<ProsecutionCounsel> prosecutionCounsels) {
        return prosecutionCounselsListConverter.convert(prosecutionCounsels);
    }

    @Handles("hearing.hearing-event-definitions")
    public JsonEnvelope findHearingEventDefinitions(final JsonEnvelope envelope) {
        return enveloper.withMetadataFrom(envelope, HEARING_QUERY_HEARING_EVENT_DEFINITIONS_RESPONSE)
                .apply(Json.createObjectBuilder()
                        .add("eventDefinitions", helperService.convert(hearingEventDefinitionService.getHearingEventDefinitions()))
                        .build());
    }
}
