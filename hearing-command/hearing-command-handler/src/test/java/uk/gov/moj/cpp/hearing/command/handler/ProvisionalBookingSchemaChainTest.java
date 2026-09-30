package uk.gov.moj.cpp.hearing.command.handler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;

/**
 * The unconfirmed-booking commands are validated three times on their way through the service —
 * once per hop, each against its own schema, and every one of those schemas sets
 * {@code additionalProperties: false}:
 *
 * <pre>
 *   POST /hearings/{id}/hearing-slots   hearing.&lt;name&gt;.json                  (command API)
 *      -&gt; hearing.command.&lt;name&gt;        hearing.command.&lt;name&gt;.json          (command handler)
 *      -&gt; hearing.event.&lt;name&gt;          hearing.event.&lt;name&gt;.json            (event processor)
 * </pre>
 *
 * <p>A field added to only the first schema therefore passes the REST call, gets a 202, and then
 * dies asynchronously at hop two with {@code extraneous key [x] is not permitted} — a rolled-back
 * JMS transaction that the caller never sees. That is exactly how {@code bookingId} reached STE02
 * half-wired: the command API accepted it, the command handler rejected it, and the only evidence
 * was in the WildFly log.
 *
 * <p>This test pins all three hops together so the next field added to this family cannot repeat
 * it. It deliberately compares property NAMES only — the hops legitimately differ in {@code
 * required} (downstream schemas additionally require {@code hearingId}, which the REST path
 * supplies from the URL) and in per-property detail.
 */
public class ProvisionalBookingSchemaChainTest {

    private static final String COMMAND_API_SCHEMA =
            "../hearing-command-api/src/raml/json/schema/hearing.%s.json";
    private static final String COMMAND_HANDLER_SCHEMA =
            "src/raml/json/schema/hearing.command.%s.json";
    private static final String EVENT_PROCESSOR_SCHEMA =
            "../../hearing-event/hearing-event-processor/src/yaml/json/schema/hearing.event.%s.json";

    /** Supplied by the REST path/URL rather than the request body, so it only appears downstream. */
    private static final String SUPPLIED_DOWNSTREAM = "hearingId";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * The command families that traverse all three hops, each as (command name, event name).
     *
     * <p>They differ. Hops 1 and 2 — the REST action and the JMS command — were renamed to
     * "unconfirmed"; hop 3 was not, because renaming an event breaks consumers outside this
     * estate that cannot be enumerated from here. So the chain is deliberately asymmetric:
     *
     * <pre>
     *   hearing.book-unconfirmed-hearing-slots          (hop 1, REST)
     *   hearing.command.book-unconfirmed-hearing-slots  (hop 2, JMS)
     *   hearing.event.book-provisional-hearing-slots    (hop 3, event — UNCHANGED)
     * </pre>
     *
     * <p>Carrying both names is the whole reason this is a pair rather than a single string.
     * A single template silently pointed hop 3 at a file that does not exist, and
     * {@link #propertiesOf} would have failed with "schema not found" rather than comparing
     * anything — which is the failure this test exists to prevent, not to cause.
     */
    private record Family(String commandName, String eventName) {
    }

    private static final List<Family> FAMILIES = List.of(
            new Family("book-unconfirmed-hearing-slots", "book-provisional-hearing-slots"),
            new Family("release-unconfirmed-hearing-slots", "release-provisional-hearing-slots"));

    @Test
    public void everyCommandApiPropertyIsAcceptedByBothDownstreamHops() throws Exception {
        for (final Family family : FAMILIES) {
            assertCommandApiPropertiesFlowDownstream(family);
        }
    }

    private void assertCommandApiPropertiesFlowDownstream(final Family family) throws Exception {
        final String name = family.commandName();
        final Set<String> commandApi = propertiesOf(String.format(COMMAND_API_SCHEMA, name));
        final Set<String> commandHandler = propertiesOf(String.format(COMMAND_HANDLER_SCHEMA, name));
        final Set<String> eventProcessor =
                propertiesOf(String.format(EVENT_PROCESSOR_SCHEMA, family.eventName()));

        assertThat("command-handler schema for " + name + " would reject these with "
                        + "'extraneous key ... is not permitted'",
                missing(commandApi, commandHandler), is(empty()));

        assertThat("event-processor schema for " + name + " would reject these with "
                        + "'extraneous key ... is not permitted'",
                missing(commandApi, eventProcessor), is(empty()));
    }

    /**
     * The two downstream hops must also agree with each other, so a field cannot be added to the
     * handler alone and then die one hop later in the event processor.
     */
    @Test
    public void bothDownstreamHopsCarryTheSameProperties() throws Exception {
        for (final Family family : FAMILIES) {
            assertDownstreamHopsAgree(family);
        }
    }

    private void assertDownstreamHopsAgree(final Family family) throws Exception {
        final String name = family.commandName();
        final Set<String> commandHandler = propertiesOf(String.format(COMMAND_HANDLER_SCHEMA, name));
        final Set<String> eventProcessor =
                propertiesOf(String.format(EVENT_PROCESSOR_SCHEMA, family.eventName()));

        assertThat("event-processor schema for " + name + " is missing properties the handler has",
                missing(commandHandler, eventProcessor), is(empty()));
        assertThat("command-handler schema for " + name + " is missing properties the processor has",
                missing(eventProcessor, commandHandler), is(empty()));
    }

    /**
     * Guards the specific field this test was written for. If {@code bookingId} is ever dropped
     * from one of the three schemas, the re-pick flow (NEW-15) silently stops releasing the
     * previous hold, which does not surface as a failure anywhere.
     */
    @Test
    public void bookingIdIsCarriedByEveryHopOfTheBookCommand() throws Exception {
        // Hop 3 keeps the old name — see Family — so the three paths cannot share one string.
        final Family family = FAMILIES.get(0);
        final List<String> paths = List.of(
                String.format(COMMAND_API_SCHEMA, family.commandName()),
                String.format(COMMAND_HANDLER_SCHEMA, family.commandName()),
                String.format(EVENT_PROCESSOR_SCHEMA, family.eventName()));

        for (final String path : paths) {
            assertThat(path + " must permit bookingId",
                    propertiesOf(path).contains("bookingId"), is(true));
        }
    }

    private static Set<String> propertiesOf(final String path) throws Exception {
        final File file = new File(path);
        if (!file.exists()) {
            throw new IllegalStateException("schema not found: " + file.getAbsolutePath()
                    + " — if the module layout moved, update this test rather than deleting it");
        }
        final JsonNode properties = MAPPER.readTree(file).get("properties");
        if (properties == null) {
            throw new IllegalStateException("schema has no 'properties' block: " + path);
        }
        final Set<String> names = new TreeSet<>();
        properties.fieldNames().forEachRemaining(names::add);
        return names;
    }

    private static List<String> missing(final Set<String> expected, final Set<String> actual) {
        final List<String> absent = new ArrayList<>();
        for (final String property : expected) {
            if (!SUPPLIED_DOWNSTREAM.equals(property) && !actual.contains(property)) {
                absent.add(property);
            }
        }
        return absent;
    }
}
