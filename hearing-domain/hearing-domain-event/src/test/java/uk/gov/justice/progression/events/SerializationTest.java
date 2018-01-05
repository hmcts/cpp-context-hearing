package uk.gov.justice.progression.events;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import org.junit.Assert;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.Defendants;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.Gender;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.UUID;

public class SerializationTest {

    @Test
    public void deserializeTest() throws Exception {

       InputStream is = SerializationTest.class.getResourceAsStream("/progression.events.sending-sheet-completed.json");
       ObjectMapper objectMapper =  new ObjectMapperProducer().objectMapper();
       objectMapper.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);
       SendingSheetCompleted sendingSheetCompleted = (SendingSheetCompleted) objectMapper.readValue(is, SendingSheetCompleted.class);
       Defendants defendants = sendingSheetCompleted.getHearing().getDefendants().get(0);
       Assert.assertEquals( "222 Furze Road", defendants.getAddress().getAddress1() );
       Assert.assertEquals( Gender.MALE, defendants.getGender());
       LocalDate pleaDate =defendants.getOffences().get(0).getPlea().getPleaDate();
       System.out.println("pleaDate " + pleaDate);
       Assert.assertEquals(2017, pleaDate.getYear());
       Assert.assertEquals(02, pleaDate.getMonthValue());
       Assert.assertEquals(23, pleaDate.getDayOfMonth());
       System.out.println("UUID==" + sendingSheetCompleted.getHearing().getCaseId());
       Assert.assertEquals(UUID.fromString("0baecac5-222b-402d-9047-84803679edae"), sendingSheetCompleted.getHearing().getCaseId() );

   }
}
