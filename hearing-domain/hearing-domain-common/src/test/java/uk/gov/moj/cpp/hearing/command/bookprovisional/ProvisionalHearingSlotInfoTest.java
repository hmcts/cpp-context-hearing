package uk.gov.moj.cpp.hearing.command.bookprovisional;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.services.messaging.JsonObjects.createReader;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;

import java.io.StringReader;
import java.util.UUID;

import org.junit.jupiter.api.Test;

/**
 * BookProvisionalHearingSlotsCommandHandler (hearing-command-handler module) builds each slot with
 * {@code convertToObject(slotsArray.getJsonObject(i), ProvisionalHearingSlotInfo.class)}, which
 * delegates to {@code JsonObjectToObjectConverter} — the same Jackson-backed converter exercised
 * here. This confirms that a plain getter/setter pair on {@code ProvisionalHearingSlotInfo} is
 * enough for the new {@code duration} field to be picked up with no change to the command
 * handler itself.
 */
public class ProvisionalHearingSlotInfoTest {

    private final JsonObjectToObjectConverter jsonObjectToObjectConverter =
            new JsonObjectToObjectConverter(new ObjectMapperProducer().objectMapper());

    @Test
    public void shouldDeserialiseDurationTheSameWayTheCommandHandlerDoes() {
        final UUID courtScheduleId = randomUUID();
        final String json = "{\"courtScheduleId\":\"" + courtScheduleId + "\","
                + "\"hearingStartTime\":\"2026-10-14T10:00:00.000Z\","
                + "\"duration\":90}";

        final ProvisionalHearingSlotInfo slot = jsonObjectToObjectConverter.convert(
                createReader(new StringReader(json)).readObject(), ProvisionalHearingSlotInfo.class);

        assertThat(slot.getCourtScheduleId(), is(courtScheduleId));
        assertThat(slot.getDuration(), is(90));
    }

    @Test
    public void shouldDeserialiseAbsentDurationAsNull() {
        final UUID courtScheduleId = randomUUID();
        final String json = "{\"courtScheduleId\":\"" + courtScheduleId + "\","
                + "\"hearingStartTime\":\"2026-10-14T10:00:00.000Z\"}";

        final ProvisionalHearingSlotInfo slot = jsonObjectToObjectConverter.convert(
                createReader(new StringReader(json)).readObject(), ProvisionalHearingSlotInfo.class);

        assertThat(slot.getDuration(), is(nullValue()));
    }
}
