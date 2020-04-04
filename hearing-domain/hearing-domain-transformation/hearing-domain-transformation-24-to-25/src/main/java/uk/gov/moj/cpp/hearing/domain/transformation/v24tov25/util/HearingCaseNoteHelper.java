package uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.util;

import static javax.json.Json.createObjectBuilder;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.COURT_CLERK;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.NOTE;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.NOTE_DATE_TIME;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.NOTE_TYPE;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.ORIGINATING_HEARING_ID;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.PROSECUTION_CASES;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.util.CourtClerkHelper.transformCourtClerk;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;


public class HearingCaseNoteHelper {

    private HearingCaseNoteHelper() {

    }

    public static JsonObject transformHearingCaseNote(final JsonObject hearingCaseNote) {

        final JsonObjectBuilder transformHearingCaseNoteBuilder = createObjectBuilder()
                .add(ID, hearingCaseNote.getString(ID))
                .add(ORIGINATING_HEARING_ID, hearingCaseNote.getString(ORIGINATING_HEARING_ID))
                .add(PROSECUTION_CASES, hearingCaseNote.getJsonArray(PROSECUTION_CASES))
                .add(NOTE_DATE_TIME, hearingCaseNote.getString(NOTE_DATE_TIME))
                .add(NOTE_TYPE, hearingCaseNote.getString(NOTE_TYPE))
                .add(NOTE, hearingCaseNote.getString(NOTE))
                .add(COURT_CLERK, transformCourtClerk(hearingCaseNote.getJsonObject(COURT_CLERK)));


        return transformHearingCaseNoteBuilder.build();
    }
}
