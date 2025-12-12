package uk.gov.justice.progression.events;

import static org.junit.jupiter.api.Assertions.assertEquals;

import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.Defendant;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

public class SerializationTest {

    @Test
    public void deserializeTest() throws Exception {
        final InputStream is = SerializationTest.class.getResourceAsStream("/progression.events.sending-sheet-completed.json");
        final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();
        final SendingSheetCompleted sendingSheetCompleted = objectMapper.readValue(is, SendingSheetCompleted.class);
        final Defendant defendants = sendingSheetCompleted.getHearing().getDefendants().get(0);
        assertEquals("222 Furze Road", defendants.getAddress().getAddress1());
        assertEquals("Male", defendants.getGender());
        final LocalDate pleaDate = defendants.getOffences().get(0).getPlea().getPleaDate();
        assertEquals(2017, pleaDate.getYear());
        assertEquals(02, pleaDate.getMonthValue());
        assertEquals(23, pleaDate.getDayOfMonth());
        assertEquals(UUID.fromString("0baecac5-222b-402d-9047-84803679edae"), sendingSheetCompleted.getHearing().getCaseId());

    }
}
