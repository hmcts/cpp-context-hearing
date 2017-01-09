package uk.gov.moj.cpp.hearing.query.view;

import java.util.Collections;
import java.util.Comparator;
import static java.util.UUID.fromString;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.messaging.JsonObjects.getUUID;

import uk.gov.justice.services.common.converter.ListToJsonArrayConverter;
import uk.gov.justice.services.common.converter.LocalDates;
import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.persist.HearingEventRepository;
import uk.gov.moj.cpp.hearing.persist.entity.DefenceCounsel;
import uk.gov.moj.cpp.hearing.persist.entity.DefenceCounselDefendant;
import uk.gov.moj.cpp.hearing.persist.entity.HearingEvent;
import uk.gov.moj.cpp.hearing.persist.entity.HearingOutcome;
import uk.gov.moj.cpp.hearing.persist.entity.ProsecutionCounsel;
import uk.gov.moj.cpp.hearing.query.view.convertor.DefenceCounselToDefendantMapConverter;
import uk.gov.moj.cpp.hearing.query.view.convertor.HearingOutcomesConverter;
import uk.gov.moj.cpp.hearing.query.view.convertor.ProsecutionCounselListConverter;
import uk.gov.moj.cpp.hearing.query.view.service.DefenceCounselService;
import uk.gov.moj.cpp.hearing.query.view.service.HearingEventDefinitionService;
import uk.gov.moj.cpp.hearing.query.view.service.HearingOutcomeService;
import uk.gov.moj.cpp.hearing.query.view.service.HearingService;
import uk.gov.moj.cpp.hearing.query.view.service.ProsecutionCounselService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;

@SuppressWarnings("CdiInjectionPointsInspection")
@ServiceComponent(Component.QUERY_VIEW)
public class HearingQueryView {

    private static final String HEARING_QUERY_HEARING_EVENT_DEFINITIONS_RESPONSE = "hearing.query.hearing-event-definitions-response";
    private static final String NAME_RESPONSE_HEARING_LIST = "hearing.get.hearings-by-startdate-response";
    private static final String NAME_RESPONSE_HEARING = "hearing.get.hearing-response";
    private static final String RESPONSE_NAME_HEARING_EVENT_LOG = "hearing.get-hearing-event-log";

    private static final String FIELD_HEARING_ID = "hearingId";

    private static final String FIELD_HEARING_EVENT_ID = "id";
    private static final String FIELD_RECORDED_LABEL = "recordedLabel";
    private static final String FIELD_TIMESTAMP = "timestamp";
    private static final String FIELD_HEARING_EVENTS = "events";

    private static final String FIELD_START_DATE = "startDate";
    private static final String FIELD_HEARINGS = "hearings";

    @Inject
    private ListToJsonArrayConverter listToJsonArrayConverter;

    @Inject
    private HearingService hearingService;

    @Inject
    private ProsecutionCounselService prosecutionCounselService;

    @Inject
    private HearingOutcomeService hearingOutcomeService;

    @Inject
    private DefenceCounselService defenceCounselService;

    @Inject
    private HearingEventDefinitionService hearingEventDefinitionService;

    @Inject
    private Enveloper enveloper;

    @Inject
    private ProsecutionCounselListConverter prosecutionCounselsListConverter;

    @Inject
    private HearingOutcomesConverter hearingOutcomesConverter;

    @Inject
    private DefenceCounselToDefendantMapConverter defenceCounselToDefendantMapConverter;

    @Inject
    private HearingEventRepository hearingEventRepository;

    @Handles("hearing.get.hearings-by-startdate")
    public JsonEnvelope findHearings(final JsonEnvelope envelope) {
        final LocalDate localDate = LocalDates.from(envelope.payloadAsJsonObject().getString(FIELD_START_DATE));
        return enveloper.withMetadataFrom(envelope, NAME_RESPONSE_HEARING_LIST)
                .apply(createObjectBuilder().add(FIELD_HEARINGS,
                        listToJsonArrayConverter.convert(hearingService.getHearingsByStartDate(localDate))
                        ).build()
                );
    }

    @Handles("hearing.get.hearing")
    public JsonEnvelope findHearing(final JsonEnvelope envelope) {
        final Optional<UUID> hearingId = getUUID(envelope.payloadAsJsonObject(), FIELD_HEARING_ID);
        return enveloper.withMetadataFrom(envelope, NAME_RESPONSE_HEARING)
                .apply(hearingService.getHearingById(hearingId.get()));
    }

    @Handles("hearing.get.prosecution-counsels")
    public JsonEnvelope getProsecutionCounsels(final JsonEnvelope query) {

        final UUID hearingId = fromString(query
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

        for (DefenceCounsel defenceCounsel : defenceCounsels) {
            defenceCounselsAndDefendantsMap.put(defenceCounsel,
                    defenceCounselService.getDefenceCounselDefendantsByDefenceCounselAttendeeId(defenceCounsel.getId()));
        }

        return enveloper.withMetadataFrom(query, "hearing.get.defence-counsels").apply(payloadFrom(defenceCounselsAndDefendantsMap));
    }

    @Handles("hearing.hearing-event-definitions")
    public JsonEnvelope findHearingEventDefinitions(final JsonEnvelope envelope) {
        return enveloper.withMetadataFrom(envelope, HEARING_QUERY_HEARING_EVENT_DEFINITIONS_RESPONSE)
                .apply(createObjectBuilder()
                        .add("eventDefinitions", listToJsonArrayConverter.convert(hearingEventDefinitionService.getHearingEventDefinitions()))
                        .build());
    }

    @Handles("hearing.get-draft-result")
    public JsonEnvelope getDraftResult(final JsonEnvelope envelope) {
        final UUID hearingId = UUID.fromString(envelope
                .payloadAsJsonObject()
                .getString(FIELD_HEARING_ID));
        final List<HearingOutcome> hearingOutcomes =
                hearingOutcomeService.getHearingOutcomeByHearingId(hearingId);
        return enveloper.withMetadataFrom(envelope, "hearing.get-draft-result-response").apply(hearingOutcomesConverter.convert(hearingOutcomes));
    }

    @Handles("hearing.get-hearing-event-log")
    public JsonEnvelope getHearingEventLog(final JsonEnvelope query) {
        final String hearingId = query.payloadAsJsonObject().getString(FIELD_HEARING_ID);

        final List<HearingEvent> hearingEvents = hearingEventRepository.findByHearingId(
                fromString(hearingId));
        Collections.sort(hearingEvents, Comparator.comparing(HearingEvent::getTimestamp));

        final JsonArrayBuilder eventLogJsonArrayBuilder = createArrayBuilder();

        hearingEvents.
                forEach(hearingEvent -> eventLogJsonArrayBuilder.add(
                createObjectBuilder()
                        .add(FIELD_HEARING_EVENT_ID, hearingEvent.getId().toString())
                        .add(FIELD_RECORDED_LABEL, hearingEvent.getRecordedLabel())
                        .add(FIELD_TIMESTAMP, ZonedDateTimes.toString(hearingEvent.getTimestamp()))
        ));

        return enveloper.withMetadataFrom(query, RESPONSE_NAME_HEARING_EVENT_LOG)
                .apply(createObjectBuilder()
                        .add(FIELD_HEARING_ID, hearingId)
                        .add(FIELD_HEARING_EVENTS, eventLogJsonArrayBuilder)
                        .build()
                );
    }

    private JsonObject payloadFrom(final List<ProsecutionCounsel> prosecutionCounsels) {
        return prosecutionCounselsListConverter.convert(prosecutionCounsels);
    }

    private JsonObject payloadFrom(final Map<DefenceCounsel, List<DefenceCounselDefendant>> defenceCounselsAndDefendantsMap) {
        return defenceCounselToDefendantMapConverter.convert(defenceCounselsAndDefendantsMap);
    }

}
