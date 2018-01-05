package uk.gov.moj.cpp.hearing.domain.aggregate;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.progression.events.SendingSheetCompleted;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.*;
import uk.gov.moj.cpp.hearing.domain.event.MagsCourtHearingRecorded;
import uk.gov.moj.cpp.hearing.domain.event.PleaAdded;
import uk.gov.moj.cpp.hearing.domain.event.SendingSheetCompletedRecorded;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RunWith(MockitoJUnitRunner.class)
public class HearingsPleaAggregateTest {


    @InjectMocks
    private HearingsPleaAggregate hearingsPleaAggregate;

    @Test
    public void testRecordSendingSheetComplete() throws Exception {
         SendingSheetCompleted sendingSheetCompleted = createSendingSheet();

         Stream<Object> events = hearingsPleaAggregate.recordSendingSheetComplete(sendingSheetCompleted );
         List<Object> lEvents = events.collect(Collectors.toList());
         Assert.assertEquals(1, lEvents.size());
         Object event = lEvents.get(0);
         Assert.assertEquals(event.getClass(), SendingSheetCompletedRecorded.class);
         SendingSheetCompletedRecorded typedEvent  = (SendingSheetCompletedRecorded) event;
         Assert.assertEquals(typedEvent.getCrownCourtHearing(), sendingSheetCompleted.getCrownCourtHearing() );
         Assert.assertEquals(typedEvent.getHearing(), sendingSheetCompleted.getHearing() );

         //check that the second call does not result in another recording !
        events = hearingsPleaAggregate.recordSendingSheetComplete(sendingSheetCompleted );
        lEvents = events.collect(Collectors.toList());
        Assert.assertEquals(0, lEvents.size());

    }


    private SendingSheetCompleted createSendingSheet() {

        List<Defendants> arrDefendants = new ArrayList<>();
            List lOffences = new ArrayList<>();
                Plea plea = (new Plea.Builder()).withId(UUID.randomUUID()).withPleaValue(PleaValue.GUILTY).withPleaDate(LocalDate.now()).build();
                LocalDate convictionDate = plea!=null && plea.getValue().equals(PleaValue.GUILTY) ? plea.getPleaDate() : null;
                UUID offenceId = UUID.randomUUID();
                Offences offence = (new Offences.Builder()).withId(offenceId).withCategory("category").withConvictionDate(convictionDate).
                        withDescription("testOffence").withEndDate(LocalDate.now()).withPlea(plea)
                        .withReason("Reason")
                        .withSection("section")
                        .withWording("wording")
                        .withOffenceCode("offenceCode")
                        .build();
                lOffences.add(offence);

            Address address = (new Address.Builder()).withAddress1("addr1").withAddress2("addr2").withAddress3("addr3").
                    withAddress4("addr4").withPostcode("AA1 1AA").build();
            UUID defendantId = UUID.randomUUID();
            Interpreter interpreter = (new Interpreter.Builder()).withLanguage("English").withNeeded(true).build();
            Defendants defendant = (new Defendants.Builder()).withOffences(lOffences).withAddress(address).withBailStatus("bailStatus").
                    withDateOfBirth(LocalDate.now()).withDefenceOrganisation("CPP").
                    withFirstName("Geoff").withLastName("ssdfsf").withGender(Gender.MALE).withId(defendantId).
                    withInterpreter(interpreter).withNationality("UK").withPersonId(UUID.randomUUID()).
                    build();
            arrDefendants.add(defendant);

        UUID caseId = UUID.randomUUID();

        Hearing hearing = (new Hearing.Builder()).withCaseId(caseId).withCaseUrn("caseUrn").
                withCourtCentreId("courtCentreId").withCourtCentreName("courtCentreName").
                withDefendants(arrDefendants).build();

        UUID courtCentreId = UUID.randomUUID();
        CrownCourtHearing crownCourtHearing = (new CrownCourtHearing.Builder()).withCcHearingDate("ccHearingDate").
                withCourtCentreId(courtCentreId).withCourtCentreName("courtCentrName").build();

        SendingSheetCompleted  sendingSheetCompleted =   (new SendingSheetCompleted.Builder()).withHearing(hearing).
                withCrownCourtHearing(crownCourtHearing).build();
        return sendingSheetCompleted;
    }

    @Test
    public void testRecordMagsCourtHearing() {

        LocalDate convictionDate;
        List<Defendants> defendants=new ArrayList<>();
        List<Offences> offences;
        Defendants defendant;
        Offences offence;

        List<UUID> pleaIds = new ArrayList<>();
        List<LocalDate> convictionDates = new ArrayList<>();
        List<UUID> caseIds = new ArrayList<>();

        //instead of making separate pleas etc

        UUID caseId = UUID.randomUUID();

        int convictionCount =2;

        for (int done=0; done<convictionCount; done++) {
            convictionDate = LocalDate.now().plusDays(done);
            convictionDates.add(convictionDate);
            UUID pleaId = UUID.randomUUID();
            pleaIds.add(pleaId);
            Plea plea = (new Plea.Builder()).withPleaDate(convictionDate).withPleaValue(PleaValue.GUILTY).withId(pleaId).build();
            offence = (new Offences.Builder()).withPlea(plea).build();
            offences = Arrays.asList(offence);
            defendant = (new Defendants.Builder()).withOffences(offences).build();
            defendants.add(defendant);
            caseIds.add(caseId);
        }

        Hearing originatingHearing = (new Hearing.Builder()).withDefendants(defendants).withCaseId(caseId).build();

        //Mockito.when(HearingTransformer.transform(originatingHearing)).thenReturn(magsCourtHearingRecordeds);

        Stream<Object> events =  hearingsPleaAggregate.recordMagsCourtHearing(originatingHearing);
        List<Object> lEvents = events.collect(Collectors.toList());
        Assert.assertEquals(2*convictionCount, lEvents.size() );

        //assume that the plea added events appear insequence after the magscourthearingrecorded events

        for (int done=0; done<convictionCount; done++) {
            Assert.assertEquals(lEvents.get(done*2).getClass(), MagsCourtHearingRecorded.class);
            Assert.assertEquals(lEvents.get(done*2+1).getClass(), PleaAdded.class);
            MagsCourtHearingRecorded magsCourtHearingRecorded = (MagsCourtHearingRecorded) lEvents.get(done*2);
            //TODO add more field level checks
            Assert.assertEquals(magsCourtHearingRecorded.getOriginatingHearing().getCaseId(), caseIds.get(done));
            Assert.assertEquals(magsCourtHearingRecorded.getConvictionDate(), convictionDates.get(done));
            PleaAdded pleaAdded = (PleaAdded) lEvents.get(done*2+1);
            Assert.assertEquals(pleaAdded.getPlea().getId(), pleaIds.get(done));
        }
    }



}