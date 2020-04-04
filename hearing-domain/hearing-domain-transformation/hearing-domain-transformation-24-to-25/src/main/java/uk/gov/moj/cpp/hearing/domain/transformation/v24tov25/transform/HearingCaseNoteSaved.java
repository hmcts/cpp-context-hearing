package uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.transform;

import static javax.json.Json.createObjectBuilder;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.core.SchemaVariableConstants.HEARING_CASE_NOTE;
import static uk.gov.moj.cpp.hearing.domain.transformation.v24tov25.util.HearingCaseNoteHelper.transformHearingCaseNote;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class HearingCaseNoteSaved implements EventInstance {

    @Override
    public JsonObject transform(final JsonObject hearingCaseNote) {

        final JsonObjectBuilder transformHearingCaseNoteBuilder = createObjectBuilder()
                .add(HEARING_CASE_NOTE, transformHearingCaseNote(hearingCaseNote.getJsonObject(HEARING_CASE_NOTE)));

        return transformHearingCaseNoteBuilder.build();
    }
}