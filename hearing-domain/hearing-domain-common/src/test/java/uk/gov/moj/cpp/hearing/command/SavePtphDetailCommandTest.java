package uk.gov.moj.cpp.hearing.command;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

/**
 * The property names on this command are a wire contract: they must match
 * {@code json/schema/hearing.save-ptph-detail.json}, which is {@code additionalProperties: false},
 * so a renamed field would be rejected at the API boundary rather than failing here. The
 * round-trip tests pin those names.
 */
class SavePtphDetailCommandTest {

    private static final String KEY_REASON = "Trial fixed date required by court order";

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void shouldExposeAllValuesGivenToTheConstructor() {
        final UUID hearingId = randomUUID();

        final SavePtphDetailCommand command =
                new SavePtphDetailCommand(hearingId, Tier.TIER_2, ListType.TYPE_1_FIXED, KEY_REASON);

        assertThat(command.getHearingId(), is(hearingId));
        assertThat(command.getTier(), is(Tier.TIER_2));
        assertThat(command.getListType(), is(ListType.TYPE_1_FIXED));
        assertThat(command.getKeyReason(), is(KEY_REASON));
    }

    /**
     * A tier-only save is valid — the list type is chosen later — so the optional fields must
     * tolerate being absent.
     */
    @Test
    void shouldAllowTierOnly() {
        final UUID hearingId = randomUUID();

        final SavePtphDetailCommand command = new SavePtphDetailCommand(hearingId, Tier.TIER_1, null, null);

        assertThat(command.getHearingId(), is(hearingId));
        assertThat(command.getTier(), is(Tier.TIER_1));
        assertThat(command.getListType(), is(nullValue()));
        assertThat(command.getKeyReason(), is(nullValue()));
    }

    /**
     * The no-arg constructor exists for framework deserialisation — {@code TrialType}, the
     * precedent command in this package, carries the same one. Kept and pinned rather than
     * removed, because the handler deserialises via the framework's converter and a change
     * there would surface at runtime rather than in a unit test.
     */
    @Test
    void shouldSupportFrameworkDeserialisationViaTheDefaultConstructor() {
        final SavePtphDetailCommand command = new SavePtphDetailCommand();

        assertThat(command.getHearingId(), is(nullValue()));
        assertThat(command.getTier(), is(nullValue()));
        assertThat(command.getListType(), is(nullValue()));
        assertThat(command.getKeyReason(), is(nullValue()));
    }

    @Test
    void shouldSerialiseUsingTheSchemaPropertyNames() throws Exception {
        final UUID hearingId = randomUUID();

        final String json = mapper.writeValueAsString(
                new SavePtphDetailCommand(hearingId, Tier.TIER_3, ListType.TYPE_2_FLEXIBLE, null));

        final var node = mapper.readTree(json);
        assertThat(node.get("hearingId").asText(), is(hearingId.toString()));
        assertThat(node.get("tier").asText(), is("TIER_3"));
        assertThat(node.get("listType").asText(), is("TYPE_2_FLEXIBLE"));
    }

    @Test
    void shouldDeserialiseFromTheCommandPayload() throws Exception {
        final UUID hearingId = randomUUID();
        final String json = String.format(
                "{\"hearingId\":\"%s\",\"tier\":\"TIER_7\",\"listType\":\"TYPE_1_FIXED\",\"keyReason\":\"%s\"}",
                hearingId, KEY_REASON);

        final SavePtphDetailCommand command = mapper.readValue(json, SavePtphDetailCommand.class);

        assertThat(command.getHearingId(), is(hearingId));
        assertThat(command.getTier(), is(Tier.TIER_7));
        assertThat(command.getListType(), is(ListType.TYPE_1_FIXED));
        assertThat(command.getKeyReason(), is(KEY_REASON));
    }
}
