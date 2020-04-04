package uk.gov.moj.cpp.hearing.domain.transformation.ancillarydocs;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static uk.gov.moj.cpp.hearing.domain.transformation.ancillarydocs.core.SchemaVariableConstants.HEARING_DEFENDANT_ATTENDANCE_UPDATED;
import static uk.gov.moj.cpp.hearing.domain.transformation.ancillarydocs.core.SchemaVariableConstants.HEARING_NOWS_REQUESTED;
import static uk.gov.moj.cpp.hearing.domain.transformation.ancillarydocs.core.SchemaVariableConstants.HEARING_PENDING_NOWS_REQUESTED;
import static uk.gov.moj.cpp.hearing.domain.transformation.ancillarydocs.core.SchemaVariableConstants.HEARING_RESULTS_SHARED;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class AttendanceDayEventTransformerTest {


    private String file;
    private String eventName;

    public AttendanceDayEventTransformerTest(final String file, final String eventName) {
        this.file = file;
        this.eventName = eventName;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"hearing.defendant-attendance-updated.json", HEARING_DEFENDANT_ATTENDANCE_UPDATED},
                {"hearing.events.nows-requested.json", HEARING_NOWS_REQUESTED},
                {"hearing.events.pending-nows-requested.json", HEARING_PENDING_NOWS_REQUESTED},
                {"hearing.results-shared.json", HEARING_RESULTS_SHARED}
        });
    }

    @Test
    public void transform() {
        JsonObject oldJsonObject = loadTestFile("old/" + file);
        JsonObject expectedJsonObject = loadTestFile("new/" + file);
        JsonObject resultJsonObject = new AttendanceDayEventTransformer().transform(eventName, oldJsonObject);
        assertThat(expectedJsonObject.toString(), equalTo(resultJsonObject.toString()));
    }

    private JsonObject loadTestFile(String resourceFileName) {
        try {
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceFileName);
            final JsonReader jsonReader = Json.createReader(is);
            return jsonReader.readObject();

        } catch (Exception ex) {
            throw new RuntimeException("failed to load test file " + resourceFileName, ex);
        }
    }
}