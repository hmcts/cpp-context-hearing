package uk.gov.moj.cpp.hearing.query.view;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

/**
 * Parity checks (CHD-04, catalogue BC-08) on how the framework's Jackson configuration reads and writes the
 * {@link ZonedDateTime} values that hearing returns in query responses.
 */
class ObjectMapperProducerTest {

    private static final String UTC_JSON = "\"2026-07-13T12:30:00.000Z\"";

    private final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();

    @Test
    void chd04_writesAUtcZonedDateTimeWithAZSuffix() throws Exception {
        final ZonedDateTime utc = ZonedDateTime.of(2026, 7, 13, 12, 30, 0, 0, ZoneId.of("UTC"));

        assertThat(objectMapper.writeValueAsString(utc), is(UTC_JSON));
    }

    // Accepted: Java 17 read "…Z" with zone ZoneId.of("UTC"); the 25.104.x framework gives ZoneOffset.UTC. Same instant
    // and same JSON either way; only the zone object (and so ZonedDateTime.equals/toString) differs. See CHD-04 in
    // java25-parity-test-report.md.
    @Test
    void chd04_readsAZSuffixedTimeAsTheSameInstantWithZoneOffsetUtc() throws Exception {
        final ZonedDateTime read = objectMapper.readValue(UTC_JSON, ZonedDateTime.class);

        assertThat(read.getZone(), is(ZoneOffset.UTC));
        assertThat(read.toInstant(), is(ZonedDateTime.of(2026, 7, 13, 12, 30, 0, 0, ZoneId.of("UTC")).toInstant()));
    }
}
