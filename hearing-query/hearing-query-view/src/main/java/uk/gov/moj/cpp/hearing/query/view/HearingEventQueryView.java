package uk.gov.moj.cpp.hearing.query.view;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.Objects.nonNull;
import static java.util.Objects.isNull;
import static java.util.UUID.fromString;
import static java.time.ZonedDateTime.now;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static uk.gov.justice.services.core.enveloper.Enveloper.envelop;
import static uk.gov.justice.services.messaging.JsonObjects.getString;
import static uk.gov.justice.services.messaging.JsonObjects.getUUID;
import static java.time.LocalDate.parse;
import static java.util.Arrays.asList;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.Person;
import uk.gov.justice.services.common.converter.LocalDates;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.mapping.CourtApplicationsSerializer;
import uk.gov.moj.cpp.hearing.persist.entity.ha.CourtCentre;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDefenceCounsel;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingEvent;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingProsecutionCounsel;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Organisation;
import uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase;
import uk.gov.moj.cpp.hearing.persist.entity.heda.HearingEventDefinition;
import uk.gov.moj.cpp.hearing.query.view.helper.DayLightSavingHelper;
import uk.gov.moj.cpp.hearing.query.view.helper.TimeZoneHelper;
import uk.gov.moj.cpp.hearing.query.view.model.EventLog;
import uk.gov.moj.cpp.hearing.query.view.model.HearingEventReport;
import uk.gov.moj.cpp.hearing.query.view.model.LoggedHearingEvent;
import uk.gov.moj.cpp.hearing.query.view.service.HearingService;
import uk.gov.moj.cpp.hearing.query.view.service.ctl.ReferenceDataService;
import uk.gov.moj.cpp.hearing.query.view.service.userdata.UserDataService;
import uk.gov.moj.cpp.hearing.query.view.service.ProgressionService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"CdiInjectionPointsInspection", "WeakerAccess", "squid:S1172", "squid:CommentedOutCodeLine", "squid:S1481", "squid:S1854"})
public class HearingEventQueryView {

    @Inject
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Inject
    private CourtApplicationsSerializer courtApplicationsSerializer;

    @Inject
    private ProgressionService progressionService;

    private static final Logger LOGGER =
            LoggerFactory.getLogger(HearingEventQueryView.class.getName());

    private static final String FIELD_DEFENCE_COUNSEL_ID = "defenceCounselId";
    private static final String NOTE = "note";
    private static final String RESPONSE_NAME_HEARING_EVENT_DEFINITIONS = "hearing.get-hearing-event-definitions";
    private static final String RESPONSE_NAME_HEARING_EVENT_DEFINITION = "hearing.get-hearing-event-definition";
    private static final String RESPONSE_NAME_HEARING_EVENT_LOG = "hearing.get-hearing-event-log";
    private static final String RESPONSE_NAME_ACTIVE_HEARINGS_FOR_COURT_ROOM = "hearing.get-active-hearings-for-court-room";

    private static final String FIELD_HEARING_ID = "hearingId";

    private static final String FIELD_GENERIC_ID = "id";
    private static final String FIELD_HEARING_EVENT_DEFINITION_ID = "hearingEventDefinitionId";
    private static final String FIELD_HEARING_EVENT_ID = "hearingEventId";
    private static final String FIELD_RECORDED_LABEL = "recordedLabel";
    private static final String FIELD_EVENT_TIME = "eventTime";
    private static final String FIELD_LAST_MODIFIED_TIME = "lastModifiedTime";
    private static final String FIELD_HEARING_EVENTS = "events";
    private static final String FIELD_HEARING_EVENT_DEFINITIONS = "eventDefinitions";
    private static final String FIELD_ACTION_LABEL = "actionLabel";
    private static final String FIELD_ACTION_SEQUENCE = "actionSequence";
    private static final String FIELD_GROUP_SEQUENCE = "groupSequence";
    private static final String FIELD_GROUP_LABEL = "groupLabel";
    private static final String FIELD_ACTIVE_HEARINGS = "activeHearings";
    private static final String FIELD_DATE = "date";
    private static final String FIELD_EVENT_DATE = "eventDate";
    private static final String FIELD_CASE_ID = "caseId";
    private static final String FIELD_HEARING_DATE = "hearingDate";
    private static final String FIELD_APPLICATION_ID = "applicationId";
    private static final String FIELD_CASE_ATTRIBUTES = "caseAttributes";
    private static final String FIELD_DEFENDANT_NAME = "defendant.name";
    private static final String FIELD_COUNSEL_NAME = "counsel.name";
    private static final String FIELD_ALTERABLE = "alterable";
    private static final String FIELD_HAS_ACTIVE_HEARING = "hasActiveHearing";
    private static final String RESUME_HEARING_EVENT_DEFINITION_ID = "64476e43-2138-46d5-b58b-848582cf9b07";
    private static final String PAUSE_HEARING_EVENT_DEFINITION_ID = "160ecb51-29ee-4954-bbbf-daab18a24fbb";
    private static final String END_HEARING_EVENT_DEFINITION_ID = "0df93f18-0a21-40f5-9fb3-da4749cd70fe";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm 'on' dd MMM yyyy");
    private static final DateTimeFormatter eventTimeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private static final String APPLICANT_DETAILS = "applicantDetails";
    private static final String RESPONDENT_DETAILS = "respondentDetails";
    private static final String NAME = "name";
    private static final String TARGET = "\"";
    private static final String REPLACEMENT = "";

    @Inject
    private TimeZoneHelper timeZoneHelper;

    @Inject
    private HearingService hearingService;

    @Inject
    private UserDataService userDataService;

    @Inject
    ReferenceDataService referenceDataService;

    public Envelope<JsonObject> getHearingEventDefinitions(final JsonEnvelope query) {
        final List<HearingEventDefinition> hearingEventDefinitions = hearingService.getHearingEventDefinitions();
        final JsonArrayBuilder eventDefinitionsJsonArrayBuilder = createArrayBuilder();
        final JsonObjectBuilder objectBuilder = createObjectBuilder();

        hearingEventDefinitions.forEach(eventDefinition -> eventDefinitionsJsonArrayBuilder.add(prepareEventDefinitionJsonObjectVersionTwo(eventDefinition)));
        objectBuilder.add(FIELD_HEARING_EVENT_DEFINITIONS, eventDefinitionsJsonArrayBuilder);

        return envelop(objectBuilder.build())
                .withName(RESPONSE_NAME_HEARING_EVENT_DEFINITIONS)
                .withMetadataFrom(query);
    }

    public Envelope<JsonObject> getHearingEventDefinition(final JsonEnvelope query) {
        final UUID hearingEventDefinitionId = fromString(query.payloadAsJsonObject().getString(FIELD_HEARING_EVENT_DEFINITION_ID));
        final Optional<HearingEventDefinition> optionalHearingEventDefinition = hearingService.getHearingEventDefinition(hearingEventDefinitionId);

        if (optionalHearingEventDefinition.isPresent()) {
            final JsonObject jsonObject = prepareEventDefinitionJsonObject(optionalHearingEventDefinition.get()).build();
            return envelop(jsonObject)
                    .withName(RESPONSE_NAME_HEARING_EVENT_DEFINITION)
                    .withMetadataFrom(query);
        }

        return envelop((JsonObject)null)
                .withName(RESPONSE_NAME_HEARING_EVENT_DEFINITION)
                .withMetadataFrom(query);
    }

    public Envelope<JsonObject> getHearingEventLog(final JsonEnvelope query) {
        final UUID hearingId = fromString(query.payloadAsJsonObject().getString(FIELD_HEARING_ID));
        final LocalDate date = LocalDates.from(query.payloadAsJsonObject().getString(FIELD_DATE));
        final List<HearingEvent> hearingEvents = hearingService.getHearingEvents(hearingId, date);
        final List<UUID> hearingIds = getActiveHearingsForCourtRoom(hearingId, date);
        final JsonArrayBuilder eventLogJsonArrayBuilder = createArrayBuilder();

        hearingEvents.
                forEach(hearingEvent ->
                        {
                            final JsonObjectBuilder jsonObjectBuilder = createObjectBuilder()
                                    .add(FIELD_HEARING_EVENT_ID, hearingEvent.getId().toString())
                                    .add(FIELD_HEARING_EVENT_DEFINITION_ID, hearingEvent.getHearingEventDefinitionId().toString())
                                    .add(FIELD_RECORDED_LABEL, hearingEvent.getRecordedLabel())
                                    .add(FIELD_EVENT_TIME, ZonedDateTimes.toString(hearingEvent.getEventTime()))
                                    .add(FIELD_LAST_MODIFIED_TIME, ZonedDateTimes.toString(hearingEvent.getLastModifiedTime()))
                                    .add(FIELD_ALTERABLE, hearingEvent.isAlterable());

                            if (nonNull(hearingEvent.getDefenceCounselId())) {
                                jsonObjectBuilder.add(FIELD_DEFENCE_COUNSEL_ID,
                                        hearingEvent.getDefenceCounselId().toString());
                            }
                            if (nonNull(hearingEvent.getNote())) {
                                jsonObjectBuilder.add(NOTE,
                                        hearingEvent.getNote());
                            }

                            eventLogJsonArrayBuilder.add(jsonObjectBuilder);
                        }
                );

        return envelop(createObjectBuilder()
                .add(FIELD_HEARING_ID, hearingId.toString())
                .add(FIELD_HAS_ACTIVE_HEARING, isNotEmpty(hearingIds) ? TRUE : FALSE)
                .add(FIELD_HEARING_EVENTS, eventLogJsonArrayBuilder)
                .build())
                .withName(RESPONSE_NAME_HEARING_EVENT_LOG)
                .withMetadataFrom(query);
    }

    public Envelope<JsonObject> getHearingEventLogCount(final JsonEnvelope query) {
        final Optional<UUID> hearingId = getUUID(query.payloadAsJsonObject(), FIELD_HEARING_ID);
        final Optional<String> eventDateOptional = getString(query.payloadAsJsonObject(), FIELD_HEARING_DATE);
        final LocalDate hearingDate = eventDateOptional.isPresent() ? parse(eventDateOptional.get()) : null;

        JsonObject jsonObject = null;
        if(hearingId.isPresent()){
            jsonObject = hearingService.getHearingEventLogCount(hearingId.get(), hearingDate);
        }

        return envelop(jsonObject)
                .withName("hearing.get-hearing-event-log-count")
                .withMetadataFrom(query);
    }

    public Envelope<JsonObject> getHearingEventLogForDocuments(final JsonEnvelope query) {

        final Optional<UUID> hearingId = getUUID(query.payloadAsJsonObject(), FIELD_HEARING_ID);
        final Optional<UUID> caseId = getUUID(query.payloadAsJsonObject(), FIELD_CASE_ID);
        final Optional<UUID> applicationId = getUUID(query.payloadAsJsonObject(), FIELD_APPLICATION_ID);
        final Optional<String> hearingDateOptional = getString(query.payloadAsJsonObject(), FIELD_HEARING_DATE);
        final LocalDate hearingDate = hearingDateOptional.isPresent() ? parse(hearingDateOptional.get()) : null;

        JsonObject hearingEventLogResponse = null;
        if(hearingId.isPresent()) {
            final Hearing hearing = hearingService.getHearingDetailsByHearingForDocuments(hearingId.get());
            if (nonNull(hearing)) {
                hearingEventLogResponse = getHearingEventLogForDocument(asList(hearing), hearingDate, query, true, false, false);
            }
        } else if (caseId.isPresent()) {
            final List<Hearing> hearings = hearingService.getHearingDetailsByCaseForDocuments(caseId.get());
            hearingEventLogResponse = getHearingEventLogForDocument(hearings, hearingDate, query, false, true, false);
        } else if (applicationId.isPresent()) {
            final List<Hearing> hearings = hearingService.getHearingDetailsByApplicationForDocuments(applicationId.get());
            hearingEventLogResponse = getHearingEventLogForDocument(hearings, hearingDate, query, false, false, true);
        }

        return envelop(hearingEventLogResponse)
                .withName("hearing.get-hearing-event-log-for-documents")
                .withMetadataFrom(query);

    }

    private JsonObject getHearingEventLogForDocument(final List<Hearing> hearings, final LocalDate hearingDate, final JsonEnvelope query, final boolean hearingFlag, final boolean caseFlag, final boolean applicationFlag) {
        final Map<UUID, HearingEventReport> hearingEventReportMap = new HashMap<>();

        hearings.forEach(hearing->{
            hearingEventReportMap.putIfAbsent(hearing.getId(), new HearingEventReport());
            final HearingEventReport hearingEventReport = hearingEventReportMap.get(hearing.getId());
            populateHearingEventReportWithHearingDetails(hearingEventReport, hearing, hearingDate, query, hearingFlag, caseFlag, applicationFlag);

            final Optional<LocalDate> startDate = hearingEventReport.getHearingDates().stream().min(LocalDate::compareTo);
            startDate.ifPresent(hearingEventReport::setStartDate);

            final Optional<LocalDate> endDate = hearingEventReport.getHearingDates().stream().max(LocalDate::compareTo);
            if(!startDate.equals(endDate)) {
                endDate.ifPresent(hearingEventReport::setEndDate);
            }
            hearingEventReportMap.computeIfPresent(hearing.getId(), (key, value) -> value = hearingEventReport);
        });
        final Map<UUID, HearingEventReport> sortedHearingEventReport = sortByHearingDay(hearingEventReportMap);
        return createResponsePayloadWithHearingEvents(sortedHearingEventReport, hearingDate, query);

    }

    public static Map<UUID, HearingEventReport> sortByHearingDay(Map<UUID, HearingEventReport> hearingEventReportMap) {
        return hearingEventReportMap.entrySet()
                          .stream()
                          .sorted(Comparator.comparing(i -> i.getValue().getStartDate()))
                          .collect(Collectors.toMap(Map.Entry::getKey,Map.Entry::getValue,(e1, e2) -> e1, LinkedHashMap::new));
    }

    private JsonObject createResponsePayloadWithHearingEvents(final Map<UUID, HearingEventReport> hearingEventReportMap, final LocalDate hearingDate, final JsonEnvelope query){
        hearingEventReportMap.forEach((key, record)->{
            final List<HearingEvent> hearingEvents = hearingService.getHearingEvents(key, hearingDate);
            hearingEvents.forEach(hearingEvent -> hearingEvent.setEventTime(DayLightSavingHelper.handleDST(timeZoneHelper.isDayLightSavingOn(),hearingEvent.getEventTime())));
            final Map<LocalDate, List<EventLog>> eventLogMap= populateEventLogsDatewise(hearingEvents);
            final List<LoggedHearingEvent> loggedHearingEvents = populateLoggedHearingEvents(eventLogMap, query);
            record.setLoggedHearingEvents(loggedHearingEvents);
        });

        final JsonArrayBuilder hearingEventArray = createArrayBuilder();

        final AtomicInteger hearingCount = new AtomicInteger(0);
        hearingEventReportMap.forEach((key,value)->{
            if (nonNull(value.getLoggedHearingEvents()) && !value.getLoggedHearingEvents().isEmpty()){
                hearingCount.getAndIncrement();
            }
            final JsonObject jsonObject = objectToJsonObjectConverter.convert(value);
            hearingEventArray.add(jsonObject);
        });

        final JsonObjectBuilder responseBuilder = createObjectBuilder();

        if (hearingCount.get() == 0) {
            return responseBuilder.addNull("hearings").build();
        } else {
            responseBuilder.add("hearings", hearingEventArray.build())
                    .add("requestedTime", now().format(formatter));

            final Optional<String> requestedUser = query.metadata().userId();
            if (requestedUser.isPresent()) {
                final String requestedUserName = userDataService.getUserDetails(query, requestedUser.get()).get(0);
                responseBuilder.add("requestedUser", requestedUserName);
            }
            return responseBuilder.build();
        }
    }

    private void populateHearingEventReportWithHearingDetails(final HearingEventReport hearingEventReport, final Hearing hearing, final LocalDate hearingDate,  final JsonEnvelope query, final boolean hearingFlag, final boolean caseFlag, final boolean applicationFlag){
        hearingEventReport.setHearingId(hearing.getId());
        hearingEventReport.setCourtCentre(hearing.getCourtCentre().getName());
        hearingEventReport.setCourtRoom(hearing.getCourtCentre().getRoomName());
        hearingEventReport.setHearingType(hearing.getHearingType().getDescription());
        hearing.getHearingDays().forEach(day -> hearingEventReport.getHearingDates().add(day.getDate()));
        if(hearingFlag) {
            if(nonNull(hearing.getCourtApplicationsJson())) {
                final JsonObject courtApplicationObject = new StringToJsonObjectConverter().convert(hearing.getCourtApplicationsJson());
                if (courtApplicationObject.size() > 0) {
                    populateApplicationDetails(hearingEventReport, hearing, query);
                } else {
                    populateCaseDetails(hearingEventReport, hearing);
                }
            } else {
                populateCaseDetails(hearingEventReport, hearing);
            }
        }
        if (caseFlag) {
            populateCaseDetails(hearingEventReport, hearing);
        }
        if (applicationFlag) {
            populateApplicationDetails(hearingEventReport, hearing, query);
            populateCaseDetails(hearingEventReport, hearing);
        }
        getDefendantAttendees(hearing.getDefenceCounsels(), hearingEventReport, hearingDate);
        getProsecutionAttendees(hearing.getProsecutionCounsels(), hearingEventReport, hearingDate);

        hearing.getJudicialRoles().forEach(judiciary -> hearingEventReport.getJudiciary().add(judiciary.getJudicialId().toString()));
        final String judicialIds = String.join(",", hearingEventReport.getJudiciary());
        final List<String> judiciaryNames = referenceDataService.getJudiciaryTitle(query, judicialIds);
        hearingEventReport.getJudiciary().clear();
        judiciaryNames.forEach(judiciary-> hearingEventReport.getJudiciary().add(judiciary));
    }

    private void populateCaseDetails(final HearingEventReport hearingEventReport, final Hearing hearing) {
        hearing.getProsecutionCases().forEach(prosecutionCase -> {
            hearingEventReport.getCaseUrns().add(prosecutionCase.getProsecutionCaseIdentifier().getCaseURN());
            hearingEventReport.getCaseIds().add(prosecutionCase.getId().getId());
            getDefendants(prosecutionCase, hearingEventReport);
        });
    }

    private void getDefendants(final ProsecutionCase prosecutionCase, final HearingEventReport hearingEventReport){
        prosecutionCase.getDefendants().forEach(defendant->{
            final PersonDefendant personDefendant = defendant.getPersonDefendant();
            if(nonNull(personDefendant) && nonNull(personDefendant.getPersonDetails())) {
                hearingEventReport.getDefendants().add(personDefendant.getPersonDetails().getFirstName() + " " + personDefendant.getPersonDetails().getLastName());
            } else {
                final Organisation organisation = defendant.getLegalEntityOrganisation();
                if(nonNull(organisation)){
                    hearingEventReport.getDefendants().add(organisation.getName());
                }
            }
        });
    }

    private void getDefendantAttendees(final Set<HearingDefenceCounsel> hearingDefenceCounsels, final HearingEventReport hearingEventReport, final LocalDate hearingDate){
        hearingDefenceCounsels.forEach(defenceCounsel -> {
            final JsonNode jsonNode = defenceCounsel.getPayload();
            if (nonNull(jsonNode)) {
                final JsonNode attendedDays = jsonNode.get("attendanceDays");
                attendedDays.forEach(day -> {
                    if (isNull(hearingDate) || (nonNull(hearingDate) && day.asText().equals(hearingDate.toString()))) {
                        hearingEventReport.getDefendantAttendees().add(jsonNode.get("firstName").asText() + " " + jsonNode.get("lastName").asText());
                    }
                });
            }
        });
    }

    private void getProsecutionAttendees(final Set<HearingProsecutionCounsel> hearingProsecutionCounsels, final HearingEventReport hearingEventReport, final LocalDate hearingDate){
        hearingProsecutionCounsels.forEach(prosecutionCounsel -> {
            final JsonNode jsonNode = prosecutionCounsel.getPayload();
            if (nonNull(jsonNode)) {
                final JsonNode attendedDays = jsonNode.get("attendanceDays");
                attendedDays.forEach(day -> {
                    if (isNull(hearingDate) || (nonNull(hearingDate) && day.asText().equals(hearingDate.toString()))) {
                        hearingEventReport.getProsecutionAttendees().add(jsonNode.get("firstName").asText() + " " + jsonNode.get("lastName").asText());
                    }
                });

            }
        });
    }

    private void populateApplicationDetails(final HearingEventReport hearingEventReport, Hearing hearing, final JsonEnvelope query) {
        final List<CourtApplication> courtApplications = courtApplicationsSerializer.courtApplications(hearing.getCourtApplicationsJson());
        courtApplications.forEach(application -> {
            final JsonObject courtApplicationPayload = progressionService.retrieveApplication(query, application.getId());
            hearingEventReport.getApplicationReferences().add(application.getApplicationReference());
            hearingEventReport.getApplicationIds().add(application.getId());
            if (nonNull(courtApplicationPayload)) {
                if (nonNull(courtApplicationPayload.getJsonObject(APPLICANT_DETAILS))) {
                    polpulateApplicants(hearingEventReport, courtApplicationPayload);
                }
                if (nonNull(courtApplicationPayload.getJsonArray(RESPONDENT_DETAILS))) {
                    polpulateRespondents(hearingEventReport, courtApplicationPayload);
                }
            }
            polpulateThirdParties(hearingEventReport, application);
        });
    }

    private void polpulateApplicants(final HearingEventReport hearingEventReport, final JsonObject application) {
        hearingEventReport.getApplicants().add(application.getJsonObject(APPLICANT_DETAILS).getString(NAME));
    }

    private void polpulateRespondents(final HearingEventReport hearingEventReport, final JsonObject application) {

        for (final JsonObject respondent : application.getJsonArray(RESPONDENT_DETAILS).getValuesAs(JsonObject.class)) {
            if (nonNull(respondent)) {
                hearingEventReport.getRespondents().add(respondent.get(NAME).toString().replace(TARGET, REPLACEMENT));
            }
        }
    }



    private void polpulateThirdParties(final HearingEventReport hearingEventReport, final CourtApplication application) {
        if(nonNull(application.getThirdParties())) {
            application.getThirdParties().forEach(thirdParty -> {
                final Person thirdPartyPerson = thirdParty.getPersonDetails();
                if (nonNull(thirdPartyPerson)) {
                    hearingEventReport.getThirdParties().add(thirdPartyPerson.getFirstName() + " " + thirdPartyPerson.getLastName());
                }
            });
        }
    }

    private  Map<LocalDate, List<EventLog>> populateEventLogsDatewise(final List<HearingEvent> hearingEvents){
        final Map<LocalDate, List<EventLog>> eventLogMap= new TreeMap<>();
        hearingEvents.forEach(event->{
            final EventLog eventLog = new EventLog();
            eventLog.setLabel(event.getRecordedLabel());
            eventLog.setNote(event.getNote());
            eventLog.setTime(eventTimeFormatter.format(event.getEventTime()));
            eventLog.setUserId(event.getUserId());

            eventLogMap.computeIfPresent(event.getEventTime().toLocalDate(), (key, value) -> {
                value.add(eventLog);
                return value;
            });
            eventLogMap.putIfAbsent(event.getEventTime().toLocalDate(), new ArrayList<>(Arrays.asList(eventLog)));
        });

        return eventLogMap;
    }

    private List<LoggedHearingEvent> populateLoggedHearingEvents(final Map<LocalDate, List<EventLog>> eventLogMap, final JsonEnvelope query) {
        final List<LoggedHearingEvent> loggedHearingEvents = new ArrayList<>();
        eventLogMap.forEach((key, record)->{
            final LoggedHearingEvent loggedHearingEvent = new LoggedHearingEvent();
            loggedHearingEvent.setHearingDate(key);
            loggedHearingEvent.setEventLogs(record);
            record.forEach(event -> {
                    if (nonNull(event.getUserId())) {
                        loggedHearingEvent.getCourtClerks().add(event.getUserId().toString());
                    }
                }
            );
            final String courtClerkIds = String.join(",", loggedHearingEvent.getCourtClerks());
            if(nonNull(courtClerkIds) && (courtClerkIds.length() > 0)) {
                final List<String> courtClerkNames= userDataService.getUserDetails(query, courtClerkIds);
                loggedHearingEvent.getCourtClerks().clear();
                courtClerkNames.forEach(name -> loggedHearingEvent.getCourtClerks().add(name));
            }
            loggedHearingEvents.add(loggedHearingEvent);
        });
        return loggedHearingEvents;
    }

    public Envelope<JsonObject> getActiveHearingsForCourtRoom(final JsonEnvelope query) {
        final UUID hearingId = fromString(query.payloadAsJsonObject().getString(FIELD_HEARING_ID));
        final LocalDate eventDate = LocalDates.from(query.payloadAsJsonObject().getString(FIELD_EVENT_DATE));
        final List<UUID> hearingIds = getActiveHearingsForCourtRoom(hearingId, eventDate);
        final JsonObjectBuilder objectBuilder = createObjectBuilder();
        final JsonArrayBuilder activeHearingIds = createArrayBuilder();

        hearingIds.forEach(id -> activeHearingIds.add(id.toString()));
        objectBuilder.add(FIELD_ACTIVE_HEARINGS, activeHearingIds);

        return envelop(objectBuilder.build())
                .withName(RESPONSE_NAME_ACTIVE_HEARINGS_FOR_COURT_ROOM)
                .withMetadataFrom(query);
    }

    private List<UUID> getActiveHearingsForCourtRoom(final UUID hearingId, final LocalDate date) {
        final Optional<CourtCentre> optionalCourtCentre = hearingService.getCourtCenterByHearingId(hearingId);
        if (!optionalCourtCentre.isPresent()) {
            return Collections.emptyList();
        }

        final CourtCentre courtCentre = optionalCourtCentre.get();
        final List<HearingEvent> hearingEvents =
                hearingService
                        .getHearingEvents(
                                courtCentre.getId(),
                                courtCentre.getRoomId(),
                                date);

        return getActiveHearingIdsByHearingEvents(hearingEvents);
    }

    private List<UUID> getActiveHearingIdsByHearingEvents(final List<HearingEvent> hearingEvents) {

        final List<UUID> activeHearings = new ArrayList<>();

        final Map<UUID, Map<UUID, Long>> hearingIdsGroupByEventDefinitionId = hearingEvents.stream().collect(
                Collectors.groupingBy(HearingEvent::getHearingId, Collectors.groupingBy(HearingEvent::getHearingEventDefinitionId, Collectors.counting())));

        final List<UUID> hearingsWithStartEvents = hearingIdsGroupByEventDefinitionId.entrySet().stream()
                .filter(h -> !h.getValue().containsKey(fromString(PAUSE_HEARING_EVENT_DEFINITION_ID)))
                .filter(h -> !h.getValue().containsKey(fromString(RESUME_HEARING_EVENT_DEFINITION_ID)))
                .filter(h -> !h.getValue().containsKey(fromString(END_HEARING_EVENT_DEFINITION_ID)))
                .filter(h -> h.getValue().containsValue(1L))
                .map(Map.Entry::getKey).distinct().collect(Collectors.toList());

        final Map<UUID, Long> hearingsWithPauseEvents = getEventsByHearingDefinition(hearingIdsGroupByEventDefinitionId, PAUSE_HEARING_EVENT_DEFINITION_ID);

        final Map<UUID, Long> hearingsWithResumeEvents = getEventsByHearingDefinition(hearingIdsGroupByEventDefinitionId, RESUME_HEARING_EVENT_DEFINITION_ID);

        hearingsWithPauseEvents.forEach((hearingId, pauseCount) -> {
            final Long resumeCount = hearingsWithResumeEvents.getOrDefault(hearingId, 0L);
            if (pauseCount.equals(resumeCount)) {
                activeHearings.add(hearingId);
            }
        });

        activeHearings.addAll(hearingsWithStartEvents);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Active hearings received from same court room count: {}", activeHearings.size());
        }

        return activeHearings;
    }

    private Map<UUID, Long> getEventsByHearingDefinition(Map<UUID, Map<UUID, Long>> hearingIdsGroupByEventDefinitionId, String hearingEventDefinitionId) {
        final Map<UUID, Long> hearingEvents = new HashMap<>();
        hearingIdsGroupByEventDefinitionId.forEach((hearingId, hearingEventDefinition) -> {
            if (hearingEventDefinition.containsKey(fromString(hearingEventDefinitionId)) && !hearingEventDefinition.containsKey(fromString(END_HEARING_EVENT_DEFINITION_ID))) {
                hearingEvents.put(hearingId, hearingEventDefinition.get(fromString(hearingEventDefinitionId)));
            }
        });
        return hearingEvents;
    }

    private boolean requireDefendantAndDefenceCounselDetails(final HearingEventDefinition eventDefinition) {
        return eventDefinition.getCaseAttribute() != null
                && eventDefinition.getCaseAttribute().contains(FIELD_COUNSEL_NAME)
                && eventDefinition.getCaseAttribute().contains(FIELD_DEFENDANT_NAME);
    }

    private JsonObjectBuilder prepareEventDefinitionJsonObject(final HearingEventDefinition eventDefinition) {
        final JsonObjectBuilder eventDefinitionBuilder = createObjectBuilder();

        if (requireDefendantAndDefenceCounselDetails(eventDefinition)) {
            eventDefinitionBuilder.add(FIELD_CASE_ATTRIBUTES, createArrayBuilder());
        }

        if (eventDefinition.getGroupLabel() != null) {
            eventDefinitionBuilder.add(FIELD_GROUP_LABEL, eventDefinition.getGroupLabel());
        }

        if (eventDefinition.getActionSequence() != null) {
            eventDefinitionBuilder.add(FIELD_ACTION_SEQUENCE, eventDefinition.getActionSequence());
        }

        if (eventDefinition.getGroupSequence() != null) {
            eventDefinitionBuilder.add(FIELD_GROUP_SEQUENCE, eventDefinition.getActionSequence());
        }

        eventDefinitionBuilder
                .add(FIELD_GENERIC_ID, eventDefinition.getId().toString())
                .add(FIELD_ACTION_LABEL, eventDefinition.getActionLabel())
                .add(FIELD_RECORDED_LABEL, eventDefinition.getRecordedLabel())
                .add(FIELD_ALTERABLE, eventDefinition.isAlterable());
        return eventDefinitionBuilder;
    }

    private JsonObjectBuilder prepareEventDefinitionJsonObjectVersionTwo(final HearingEventDefinition eventDefinition) {
        final JsonObjectBuilder eventDefinitionBuilder = createObjectBuilder();

        if (eventDefinition.getCaseAttribute() != null) {
            final JsonArrayBuilder jsonArrayBuilder = createArrayBuilder();
            Arrays.asList(eventDefinition.getCaseAttribute().split(",")).forEach(caseAttribute -> jsonArrayBuilder.add(caseAttribute.trim()));
            eventDefinitionBuilder.add(FIELD_CASE_ATTRIBUTES, jsonArrayBuilder.build());
        }

        if (eventDefinition.getGroupLabel() != null) {
            eventDefinitionBuilder.add(FIELD_GROUP_LABEL, eventDefinition.getGroupLabel());
        }

        if (eventDefinition.getActionSequence() != null) {
            eventDefinitionBuilder.add(FIELD_ACTION_SEQUENCE, eventDefinition.getActionSequence());
        }

        if (eventDefinition.getGroupSequence() != null) {
            eventDefinitionBuilder.add(FIELD_GROUP_SEQUENCE, eventDefinition.getGroupSequence());
        }

        eventDefinitionBuilder
                .add(FIELD_GENERIC_ID, eventDefinition.getId().toString())
                .add(FIELD_ACTION_LABEL, eventDefinition.getActionLabel())
                .add(FIELD_RECORDED_LABEL, eventDefinition.getRecordedLabel())
                .add(FIELD_ALTERABLE, eventDefinition.isAlterable());
        return eventDefinitionBuilder;
    }

}
