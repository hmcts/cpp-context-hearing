package uk.gov.justice.progression.events;

import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.Defendant;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

public class SerializationTest {

    @Test
    public void deserializeTest() throws Exception {

       final InputStream is = SerializationTest.class.getResourceAsStream("/progression.events.sending-sheet-completed.json");
       final ObjectMapper objectMapper =  new ObjectMapperProducer().objectMapper();
       final SendingSheetCompleted sendingSheetCompleted = (SendingSheetCompleted) objectMapper.readValue(is, SendingSheetCompleted.class);
       final Defendant defendants = sendingSheetCompleted.getHearing().getDefendants().get(0);
       Assert.assertEquals( "222 Furze Road", defendants.getAddress().getAddress1() );
       Assert.assertEquals( "Male", defendants.getGender());
       final LocalDate pleaDate =defendants.getOffences().get(0).getPlea().getPleaDate();
       Assert.assertEquals(2017, pleaDate.getYear());
       Assert.assertEquals(02, pleaDate.getMonthValue());
       Assert.assertEquals(23, pleaDate.getDayOfMonth());
       Assert.assertEquals(UUID.fromString("0baecac5-222b-402d-9047-84803679edae"), sendingSheetCompleted.getHearing().getCaseId() );

   }
}
