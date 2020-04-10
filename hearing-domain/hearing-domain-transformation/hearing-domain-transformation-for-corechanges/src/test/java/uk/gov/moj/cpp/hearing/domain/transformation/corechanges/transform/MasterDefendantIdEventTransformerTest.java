package uk.gov.moj.cpp.hearing.domain.transformation.corechanges.transform;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import uk.gov.justice.services.messaging.spi.DefaultJsonMetadata;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static uk.gov.moj.cpp.hearing.domain.transformation.corechanges.core.SchemaVariableConstants.HEARING_DEFENDANT_ADDED;
import static uk.gov.moj.cpp.hearing.domain.transformation.corechanges.core.SchemaVariableConstants.HEARING_EVENTS_APPLICATION_DETAIL_CHANGED;
import static uk.gov.moj.cpp.hearing.domain.transformation.corechanges.core.SchemaVariableConstants.HEARING_EVENTS_CASE_DEFENDANTS_UPDATED;
import static uk.gov.moj.cpp.hearing.domain.transformation.corechanges.core.SchemaVariableConstants.HEARING_EVENTS_CASE_DEFENDANTS_UPDATED_FOR_HEARING;
import static uk.gov.moj.cpp.hearing.domain.transformation.corechanges.core.SchemaVariableConstants.HEARING_EVENTS_HEARING_EXTENDED;
import static uk.gov.moj.cpp.hearing.domain.transformation.corechanges.core.SchemaVariableConstants.HEARING_EVENTS_INITIATED;
import static uk.gov.moj.cpp.hearing.domain.transformation.corechanges.core.SchemaVariableConstants.HEARING_EVENTS_NOWS_REQUESTED;
import static uk.gov.moj.cpp.hearing.domain.transformation.corechanges.core.SchemaVariableConstants.HEARING_EVENTS_PENDING_NOWS_REQUESTED;
import static uk.gov.moj.cpp.hearing.domain.transformation.corechanges.core.SchemaVariableConstants.HEARING_RESULTS_SHARED;

@RunWith(Parameterized.class)
public class MasterDefendantIdEventTransformerTest {


    private final String file;
    private final String eventName;

    public MasterDefendantIdEventTransformerTest(final String file, final String eventName) {
        this.file = file;
        this.eventName = eventName;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"hearing.defendant-added.json", HEARING_DEFENDANT_ADDED},
                {"hearing.events.initiated.json", HEARING_EVENTS_INITIATED},
                {"hearing.events.nows-requested.json", HEARING_EVENTS_NOWS_REQUESTED},
                {"hearing.events.hearing-extended.json", HEARING_EVENTS_HEARING_EXTENDED},
                {"hearing.events.pending-nows-requested.json", HEARING_EVENTS_PENDING_NOWS_REQUESTED},
                {"hearing.results-shared.json", HEARING_RESULTS_SHARED},
                {"hearing.events.application-detail-changed.json", HEARING_EVENTS_APPLICATION_DETAIL_CHANGED},
                {"hearing.case-defendants-updated.json", HEARING_EVENTS_CASE_DEFENDANTS_UPDATED},
                {"hearing.case-defendants-updated-for-hearing.json", HEARING_EVENTS_CASE_DEFENDANTS_UPDATED_FOR_HEARING},
        });
    }

    @Test
    public void transform() {
        final JsonObject oldJsonObject = loadTestFile("master-defendant-id/old/" + file);
        final JsonObject expectedJsonObject = loadTestFile("master-defendant-id/new/" + file);
        final JsonObject resultJsonObject = new MasterDefendantIdEventTransformer().transform(
                DefaultJsonMetadata.metadataBuilder().withName(eventName).withId(randomUUID()).build(), oldJsonObject);
        assertThat(expectedJsonObject.toString(), equalTo(resultJsonObject.toString()));
    }

    private JsonObject loadTestFile(final String resourceFileName) {
        try {
            final InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceFileName);
            final JsonReader jsonReader = Json.createReader(is);
            return jsonReader.readObject();

        } catch (final Exception ex) {
            throw new RuntimeException("failed to load test file " + resourceFileName, ex);
        }
    }
}