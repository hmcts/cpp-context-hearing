package uk.gov.moj.cpp.hearing.event;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static uk.gov.moj.cpp.hearing.domain.event.ReleaseProvisionalHearingSlots.Reason.DROPPED_FROM_DRAFT;

import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.hearing.domain.event.ReleaseProvisionalHearingSlots;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.junit.jupiter.api.Test;

/**
 * The aggregate builds this event itself rather than passing a command through, so nothing else
 * checks that what it writes is what the processor's schema accepts. A mismatch would not fail
 * the save: the event would be refused at the processor, the JMS transaction rolled back, and the
 * hold kept until the overnight purge with nothing but a WildFly log line to show for it.
 */
public class ReleaseProvisionalHearingSlotsSchemaTest {

    private static final String SCHEMA = "src/yaml/json/schema/hearing.event.release-provisional-hearing-slots.json";

    private final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();

    @Test
    public void theEventTheAggregateWritesIsAcceptedByTheProcessorSchema() throws Exception {
        final ReleaseProvisionalHearingSlots event = new ReleaseProvisionalHearingSlots(
                randomUUID(), List.of(randomUUID().toString(), randomUUID().toString()), DROPPED_FROM_DRAFT);

        assertThat(schemaErrors(objectMapper.valueToTree(event)), is(Set.of()));
    }

    @Test
    public void everyReasonIsAcceptedByTheProcessorSchema() throws Exception {
        for (final ReleaseProvisionalHearingSlots.Reason reason : ReleaseProvisionalHearingSlots.Reason.values()) {
            final ReleaseProvisionalHearingSlots event =
                    new ReleaseProvisionalHearingSlots(randomUUID(), List.of(randomUUID().toString()), reason);

            assertThat(reason.name(), schemaErrors(objectMapper.valueToTree(event)), is(Set.of()));
        }
    }

    @Test
    public void anEventReleasingNothingIsRefused() throws Exception {
        final ReleaseProvisionalHearingSlots event =
                new ReleaseProvisionalHearingSlots(randomUUID(), List.of(), DROPPED_FROM_DRAFT);

        assertThat(schemaErrors(objectMapper.valueToTree(event)), is(not(empty())));
    }

    private Set<String> schemaErrors(final JsonNode payload) throws Exception {
        final File schemaFile = new File(SCHEMA);
        if (!schemaFile.exists()) {
            throw new IllegalStateException("schema not found: " + schemaFile.getAbsolutePath());
        }
        final ObjectNode schema = (ObjectNode) objectMapper.readTree(schemaFile);
        // hearingId refers to the shared definitions.json, which is not on this module's path;
        // the uuid format it adds is not what this test is about
        ((ObjectNode) schema.get("properties")).putObject("hearingId").put("type", "string");

        return JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4).getSchema(schema)
                .validate(payload).stream()
                .map(ValidationMessage::getMessage)
                .collect(Collectors.toSet());
    }
}
