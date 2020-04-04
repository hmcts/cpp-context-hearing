package uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.transform;

import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.DEFENDANT_DETAILS_UPDATED;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.HEARING_CASE_NOTE_SAVED;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.HEARING_EVENTS_INITIATED;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.HEARING_OFFENCE_VERDICT_UPDATED;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.INHERITED_VERDICT_ADDED;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.NOWS_REQUESTED;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.OFFENCE_ADDED;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.OFFENCE_UPDATED;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.PENDING_NOWS_REQUESTED;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.RESULTS_SHARED;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.RESULT_LINES_STATUS_UPDATED;

import java.util.HashMap;
import java.util.Map;

public class TransformFactory {

    private Map<String, EventInstance> transformEventMap;


    public TransformFactory() {
        transformEventMap = new HashMap<>();
        transformEventMap.put(HEARING_EVENTS_INITIATED, new HearingEventsInitiated());
        transformEventMap.put(INHERITED_VERDICT_ADDED, new InheritedVerdictAdded());
        transformEventMap.put(DEFENDANT_DETAILS_UPDATED, new DefendantDetailsUpdated());
        transformEventMap.put(HEARING_OFFENCE_VERDICT_UPDATED, new HearingOffenceVerdictUpdated());
        transformEventMap.put(OFFENCE_ADDED, new OffenceAdded());
        transformEventMap.put(OFFENCE_UPDATED, new OffenceUpdated());
        transformEventMap.put(RESULTS_SHARED, new ResultsShared());
        transformEventMap.put(HEARING_CASE_NOTE_SAVED, new HearingCaseNoteSaved());
        transformEventMap.put(PENDING_NOWS_REQUESTED, new PendingNowsRequested());
        transformEventMap.put(RESULT_LINES_STATUS_UPDATED, new ResultLinesStatusUpdated());
        transformEventMap.put(NOWS_REQUESTED, new NowsRequested());
    }

    public EventInstance getEventInstance(String eventName) {
        return transformEventMap.get(eventName);
    }
}
