package uk.gov.moj.cpp.hearing.domain.transformation.mot.transform;

import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.HEARING_EVENTS_INITIATED;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.HEARING_OFFENCE_PLEA_UPDATED;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.HEARING_SENDING_SHEET_RECORDED;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.NOWS_REQUESTED;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.OFFENCE_ADDED;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.OFFENCE_UPDATED;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.OFFENCE_UPDATED_FOR_HEARINGS;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.PENDING_NOWS_REQUESTED;
import static uk.gov.moj.cpp.hearing.domain.transformation.mot.core.SchemaVariableConstants.RESULTS_SHARED;

import java.util.HashMap;
import java.util.Map;

public class TransformFactory {

    private Map<String, EventInstance> transformEventMap;


    public TransformFactory() {
        transformEventMap = new HashMap<>();
        transformEventMap.put(HEARING_EVENTS_INITIATED, new HearingEventsInitiated());
        transformEventMap.put(OFFENCE_ADDED, new OffenceAdded());
        transformEventMap.put(OFFENCE_UPDATED, new OffenceUpdated());
        transformEventMap.put(RESULTS_SHARED, new ResultsShared());
        transformEventMap.put(PENDING_NOWS_REQUESTED, new PendingNowsRequested());
        transformEventMap.put(NOWS_REQUESTED, new NowsRequested());
        transformEventMap.put(OFFENCE_UPDATED_FOR_HEARINGS, new OffenceUpdatedOnHearings());
        transformEventMap.put(HEARING_SENDING_SHEET_RECORDED, new HearingSendingSheetRecorded());
        transformEventMap.put(HEARING_OFFENCE_PLEA_UPDATED, new HearingOffencePleaUpdated());
    }

    public EventInstance getEventInstance(String eventName) {
        return transformEventMap.get(eventName);
    }
}
