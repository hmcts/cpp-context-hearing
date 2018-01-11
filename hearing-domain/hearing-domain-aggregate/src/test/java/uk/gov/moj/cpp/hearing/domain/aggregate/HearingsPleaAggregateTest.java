package uk.gov.moj.cpp.hearing.domain.aggregate;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.progression.events.SendingSheetCompleted;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.Defendant;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.Offence;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.CrownCourtHearing;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.Hearing;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.Interpreter;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.PleaValue;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.Plea;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.Address;

import uk.gov.moj.cpp.hearing.domain.event.MagsCourtHearingRecorded;
import uk.gov.moj.cpp.hearing.domain.event.PleaAdded;
import uk.gov.moj.cpp.hearing.domain.event.SendingSheetCompletedPreviouslyRecorded;
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


    @Mock
    private HearingTransformer hearingTransformer;

    @InjectMocks
    private HearingsPleaAggregate hearingsPleaAggregate;

    @Test
    public void testRecordSendingSheetComplete() throws Exception {
        final SendingSheetCompleted sendingSheetCompleted = createSendingSheet();

        Stream<Object> events = hearingsPleaAggregate.recordSendingSheetComplete(sendingSheetCompleted );
        List<Object> lEvents = events.collect(Collectors.toList());
        Assert.assertEquals(1, lEvents.size());
        Object event;

        event = lEvents.get(0);
        Assert.assertEquals(event.getClass(), SendingSheetCompletedRecorded.class);
        final SendingSheetCompletedRecorded typedEvent  = (SendingSheetCompletedRecorded) event;
        Assert.assertEquals(typedEvent.getCrownCourtHearing(), sendingSheetCompleted.getCrownCourtHearing() );
        Assert.assertEquals(typedEvent.getHearing(), sendingSheetCompleted.getHearing() );

        //check that the second call does not result in another recording !
        events = hearingsPleaAggregate.recordSendingSheetComplete(sendingSheetCompleted );
        lEvents = events.collect(Collectors.toList());
        Assert.assertEquals(1, lEvents.size());
        event = lEvents.get(0);
        Assert.assertEquals(event.getClass(), SendingSheetCompletedPreviouslyRecorded.class);
        final SendingSheetCompletedPreviouslyRecorded typedNonEvent  = (SendingSheetCompletedPreviouslyRecorded) event;
        Assert.assertEquals(typedNonEvent.getCrownCourtHearing(), typedNonEvent.getCrownCourtHearing() );
        Assert.assertEquals(typedNonEvent.getHearing(), sendingSheetCompleted.getHearing() );

    }


    private SendingSheetCompleted createSendingSheet() {

        final List<Defendant> arrDefendants = new ArrayList<>();
        final List lOffences = new ArrayList<>();
        final Plea plea = (new Plea.Builder()).withId(UUID.randomUUID()).withPleaValue(PleaValue.GUILTY).withPleaDate(LocalDate.now()).build();
        final LocalDate convictionDate = plea!=null && plea.getValue().equals(PleaValue.GUILTY) ? plea.getPleaDate() : null;
        UUID offenceId = UUID.randomUUID();
        final Offence offence = (new Offence.Builder()).withId(offenceId).withCategory("category").withConvictionDate(convictionDate).
                withDescription("testOffence").withEndDate(LocalDate.now()).withPlea(plea)
                .withReason("Reason")
                .withSection("section")
                .withWording("wording")
                .withOffenceCode("offenceCode")
                .build();
        lOffences.add(offence);

        final Address address = (new Address.Builder()).withAddress1("addr1").withAddress2("addr2").withAddress3("addr3").
                withAddress4("addr4").withPostcode("AA1 1AA").build();
        final UUID defendantId = UUID.randomUUID();
        final Interpreter interpreter = (new Interpreter.Builder()).withLanguage("English").withNeeded(true).build();
        final Defendant defendant = (new Defendant.Builder()).withOffences(lOffences).withAddress(address).withBailStatus("bailStatus").
                withDateOfBirth(LocalDate.now()).withDefenceOrganisation("CPP").
                withFirstName("Geoff").withLastName("ssdfsf").withGender("Male").withId(defendantId).
                withInterpreter(interpreter).withNationality("UK").withPersonId(UUID.randomUUID()).
                build();
        arrDefendants.add(defendant);

        final UUID caseId = UUID.randomUUID();

        final Hearing hearing = (new Hearing.Builder()).withCaseId(caseId).withCaseUrn("caseUrn").
                withCourtCentreId("courtCentreId").withCourtCentreName("courtCentreName").
                withDefendants(arrDefendants).build();

        final UUID courtCentreId = UUID.randomUUID();
        final CrownCourtHearing crownCourtHearing = (new CrownCourtHearing.Builder()).withCcHearingDate("ccHearingDate").
                withCourtCentreId(courtCentreId).withCourtCentreName("courtCentrName").build();

        final SendingSheetCompleted  sendingSheetCompleted =   (new SendingSheetCompleted.Builder()).withHearing(hearing).
                withCrownCourtHearing(crownCourtHearing).build();
        return sendingSheetCompleted;
    }

    @Test
    public void testRecordMagsCourtHearing() {
        Whitebox.setInternalState(hearingsPleaAggregate, "hearingTransformer",  hearingTransformer);
        final Hearing originatingHearing = (new Hearing.Builder()).build();
        final List<MagsCourtHearingRecorded> magsCourtHearingRecordeds = new ArrayList<>();

        LocalDate convictionDate;
        List<Defendant> defendants;
        List<Offence> offences;
        Defendant defendant;
        Offence offence;

        final List<UUID> pleaIds = new ArrayList<>();
        final List<LocalDate> convictionDates = new ArrayList<>();
        final List<UUID> caseIds = new ArrayList<>();

        for (int done=0; done<2; done++) {
            MagsCourtHearingRecorded magsCourtHearingRecorded;
            convictionDate = LocalDate.now().minusDays(2+done);
            convictionDates.add(convictionDate);
            UUID pleaId = UUID.randomUUID();
            pleaIds.add(pleaId);
            Plea plea = (new Plea.Builder()).withPleaDate(convictionDate).withPleaValue(PleaValue.GUILTY).withId(pleaId).build();
            offence = (new Offence.Builder()).withPlea(plea).build();
            offences = Arrays.asList(offence);
            defendant = (new Defendant.Builder()).withOffences(offences).build();
            defendants = Arrays.asList(defendant);
            UUID caseId = UUID.randomUUID();
            caseIds.add(caseId);
            Hearing hearing = (new Hearing.Builder()).withDefendants(defendants).withCaseId(caseId).build();
            magsCourtHearingRecorded = new MagsCourtHearingRecorded(hearing, convictionDate, null);
            magsCourtHearingRecordeds.add(magsCourtHearingRecorded);
        }

        Mockito.when(hearingTransformer.transform(originatingHearing)).thenReturn(magsCourtHearingRecordeds);

        final Stream<Object> events =  hearingsPleaAggregate.recordMagsCourtHearing(originatingHearing);
        final List<Object> lEvents = events.collect(Collectors.toList());
        Assert.assertEquals(2*magsCourtHearingRecordeds.size(), lEvents.size() );

        //assume that the plea added events appear insequence after the magscourthearingrecorded events

        for (int done=0; done<magsCourtHearingRecordeds.size(); done++) {
            Assert.assertEquals(lEvents.get(done*2).getClass(), MagsCourtHearingRecorded.class);
            Assert.assertEquals(lEvents.get(done*2+1).getClass(), PleaAdded.class);
            MagsCourtHearingRecorded magsCourtHearingRecorded = (MagsCourtHearingRecorded) lEvents.get(done*2);
            Assert.assertEquals(magsCourtHearingRecorded.getOriginatingHearing().getCaseId(), caseIds.get(done));
            Assert.assertEquals(magsCourtHearingRecorded.getConvictionDate(), convictionDates.get(done));
            PleaAdded pleaAdded = (PleaAdded) lEvents.get(done*2+1);
            Assert.assertEquals(pleaAdded.getPlea().getId(), pleaIds.get(done));
        }
    }

    @Test
    public void testRecordMagsCourtHearingNoneGuilty() {
        Whitebox.setInternalState(hearingsPleaAggregate, "hearingTransformer",  hearingTransformer);
        final Hearing originatingHearing = (new Hearing.Builder()).build();
        final List<MagsCourtHearingRecorded> magsCourtHearingRecordeds = new ArrayList<>();

        Mockito.when(hearingTransformer.transform(originatingHearing)).thenReturn(magsCourtHearingRecordeds);

        final Stream<Object> events =  hearingsPleaAggregate.recordMagsCourtHearing(originatingHearing);
        final List<Object> lEvents = events.collect(Collectors.toList());

        Assert.assertEquals(0, lEvents.size() );

    }


}