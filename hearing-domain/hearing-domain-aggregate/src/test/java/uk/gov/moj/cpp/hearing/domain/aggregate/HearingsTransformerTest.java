package uk.gov.moj.cpp.hearing.domain.aggregate;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.gov.justice.progression.events.SendingSheetCompleted;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.Address;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.CrownCourtHearing;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.Defendant;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.Hearing;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.Interpreter;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.Offence;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.Plea;
import uk.gov.moj.cpp.hearing.domain.event.MagsCourtHearingRecorded;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;


public class HearingsTransformerTest {

    private HearingTransformer hearingTransformer;

    @Before
    public void setup() {
        hearingTransformer = new HearingTransformer();
    }


    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private LocalDate date(String strDate) {
        return LocalDate.parse(strDate, dateTimeFormatter);
    }

    private SendingSheetCompleted createSendingSheet(final int defendantCount,
                                                     final Function<Integer, Integer> defendantIndex2OffenceCount,
                                                     final BiFunction<Integer, Integer, Plea> defendantOffenceIndexToPlea,
                                                     String hearingType) {

        List<Defendant> arrDefendant = new ArrayList<>();
        for (int defendantIndex = 0; defendantIndex < defendantCount; defendantIndex++) {
            List offences = new ArrayList<>();
            int offenceCount = defendantIndex2OffenceCount.apply(defendantIndex);
            for (int offenceIndex = 0; offenceIndex < offenceCount; offenceIndex++) {
                Plea plea = defendantOffenceIndexToPlea.apply(defendantIndex, offenceIndex);
                LocalDate convictionDate = plea != null && plea.getValue().equals("GUILTY") ? plea.getPleaDate() : null;
                UUID offenceId = UUID.randomUUID();
                Offence offence = (new Offence.Builder()).withId(offenceId).withCategory("category").withConvictionDate(convictionDate).
                        withDescription("testOffence").withEndDate(date("12/11/2016")).withPlea(plea)
                        .withReason("Reason")
                        .withSection("section")
                        .withWording("wording")
                        .withOffenceCode("offenceCode")
                        .build();
                offences.add(offence);
            }

            Address address = Address.address().withAddress1("addr1").withAddress2("addr2").withAddress3("addr3").
                    withAddress4("addr4").withPostcode("AA1 1AA").build();
            UUID defendantId = UUID.randomUUID();
            Interpreter interpreter = Interpreter.interpreter().withLanguage("English").withNeeded(true).build();
            Defendant defendant = (new Defendant.Builder()).withOffences(offences).withAddress(address).withBailStatus("bailStatus").
                    withCustodyTimeLimitDate(date("11/12/2017")).withDateOfBirth(date("12/11/1978")).withDefenceOrganisation("CPP").
                    withFirstName("Geoff").withLastName("ssdfsf").withGender("Male").withId(defendantId).
                    withInterpreter(interpreter).withNationality("UK").withPersonId(UUID.randomUUID()).
                    build();
            arrDefendant.add(defendant);
        }

        UUID caseId = UUID.randomUUID();

        Hearing hearing = (new Hearing.Builder()).withCaseId(caseId).withCaseUrn("caseUrn").
                withCourtCentreId("courtCentreId").withCourtCentreName("courtCentreName").withType(hearingType).
                withSendingCommittalDate(date("14/11/2019")).withDefendants(arrDefendant).build();


        UUID courtCentreId = UUID.randomUUID();
        CrownCourtHearing crownCourtHearing = (new CrownCourtHearing.Builder()).withCcHearingDate("ccHearingDate").
                withCourtCentreId(courtCentreId).withCourtCentreName("courtCentrName").build();

        SendingSheetCompleted sendingSheetCompleted = (new SendingSheetCompleted.Builder()).withHearing(hearing).
                withCrownCourtHearing(crownCourtHearing).build();
        return sendingSheetCompleted;
    }

    private void assertEqual(Plea expected, Plea actual) {
        Assert.assertEquals(expected.getId(), actual.getId());
        Assert.assertEquals(expected.getPleaDate(), actual.getPleaDate());
        Assert.assertEquals(expected.getValue(), actual.getValue());

    }

    private void assertEqual(Offence expected, Offence actual) {
        Assert.assertEquals(expected.getId(), actual.getId());
        Assert.assertEquals(expected.getCategory(), actual.getCategory());
        Assert.assertEquals(expected.getConvictionDate(), actual.getConvictionDate());
        Assert.assertEquals(expected.getDescription(), actual.getDescription());
        Assert.assertEquals(expected.getEndDate(), actual.getEndDate());
        Assert.assertEquals(expected.getOffenceCode(), actual.getOffenceCode());
        Assert.assertEquals(expected.getSection(), actual.getSection());
        Assert.assertEquals(expected.getReason(), actual.getReason());
        Assert.assertEquals(expected.getWording(), actual.getWording());
        Assert.assertEquals(expected.getSection(), actual.getSection());
        assertEqual(expected.getPlea(), actual.getPlea());
    }

    private void assertEqual(Address expected, Address actual) {
        Assert.assertEquals(expected.getAddress1(), actual.getAddress1());
        Assert.assertEquals(expected.getAddress2(), actual.getAddress2());
        Assert.assertEquals(expected.getAddress3(), actual.getAddress3());
        Assert.assertEquals(expected.getAddress4(), actual.getAddress4());
        Assert.assertEquals(expected.getPostcode(), actual.getPostcode());
    }


    private void assertEqual(Defendant expected, Defendant actual) {
        Assert.assertEquals(expected.getId(), actual.getId());
        Assert.assertEquals(expected.getFirstName(), expected.getFirstName());
        Assert.assertEquals(expected.getLastName(), expected.getLastName());
        Assert.assertEquals(expected.getGender(), expected.getGender());
        Assert.assertEquals(expected.getBailStatus(), expected.getBailStatus());
        Assert.assertEquals(expected.getCustodyTimeLimitDate(), expected.getCustodyTimeLimitDate());
        Assert.assertEquals(expected.getDateOfBirth(), expected.getDateOfBirth());
        Assert.assertEquals(expected.getDefenceOrganisation(), expected.getDefenceOrganisation());
        assertEqual(expected.getAddress(), actual.getAddress());
    }

    private void assertEqual(Hearing expected, Hearing actual) {
        Assert.assertEquals(expected.getCaseId(), actual.getCaseId());
        Assert.assertEquals(expected.getCourtCentreId(), actual.getCourtCentreId());
        String expectedHearingType = expected.getType() == null ? HearingTransformer.DEFAULT_HEARING_TYPE : expected.getType();
        Assert.assertEquals(expectedHearingType, actual.getType());
        Assert.assertEquals(expected.getCourtCentreName(), actual.getCourtCentreName());
        Assert.assertEquals(expected.getCaseUrn(), actual.getCaseUrn());
        Assert.assertEquals(expected.getSendingCommittalDate(), actual.getSendingCommittalDate());
    }

    @Test
    public void testTranformSendingSheet1GuiltyPlea() throws Exception {
        //create a sending sheet with 1 guilty plea, 3 defendants 3*5 offences
        final int defendantCount = 3;
        final int offencesPerDefendant = 5;
        final int guiltyOffenceIndex = 2;
        final int guiltyDefendantIndex = 1;
        final BiFunction<Integer, Integer, Plea> defendantOffenceIndex2Plea = (d, o) -> {
            return (d != guiltyDefendantIndex || o != guiltyOffenceIndex) ? null : new Plea(UUID.randomUUID(), LocalDate.now().plusDays(d), "GUILTY");
        };
        String hearingType = null;
        final SendingSheetCompleted sendingSheet = createSendingSheet(defendantCount, (i) -> {
            return offencesPerDefendant;
        }, defendantOffenceIndex2Plea, hearingType);
        final List<MagsCourtHearingRecorded> events = hearingTransformer.transform(sendingSheet.getHearing());

        Assert.assertEquals(1, events.size());
        final MagsCourtHearingRecorded event = events.get(0);

        assertEqual(sendingSheet.getHearing(), event.getOriginatingHearing());

        Assert.assertEquals(1, event.getOriginatingHearing().getDefendants().size());
        final Defendant defendantOut = event.getOriginatingHearing().getDefendants().get(0);
        Assert.assertEquals(1, defendantOut.getOffences().size());
        final Offence offenceOut = defendantOut.getOffences().get(0);

        Defendant defendantIn = sendingSheet.getHearing().getDefendants().get(guiltyDefendantIndex);
        assertEqual(defendantIn, defendantOut);
        Offence offenceIn = defendantIn.getOffences().get(guiltyOffenceIndex);
        assertEqual(offenceIn, offenceOut);


    }


    @Test
    public void testTranformSendingSheetMultiplefendantsOffenceMultipleConvictionDatesPartialGuilty() throws Exception {
        //create a sending sheet with make every second offence guilty plea, 3 defendants
        int defendantCount = 3;
        int offencesPerDefendant = 5;
        int guiltyOffenceIndex = 2;
        BiFunction<Integer, Integer, Plea> defendantOffenceIndex2Plea = (d, o) -> {
            return o != guiltyOffenceIndex ? null : new Plea(UUID.randomUUID(), LocalDate.now().plusDays(d), "GUILTY");
        };
        String hearingType = null;
        SendingSheetCompleted sendingSheet = createSendingSheet(defendantCount, (i) -> {
            return offencesPerDefendant;
        }, defendantOffenceIndex2Plea, hearingType);
        List<MagsCourtHearingRecorded> events = hearingTransformer.transform(sendingSheet.getHearing());

        Assert.assertEquals(defendantCount, events.size());

        for (int done = 0; done < events.size(); done++) {
            MagsCourtHearingRecorded event = events.get(done);
            assertEqual(sendingSheet.getHearing(), event.getOriginatingHearing());

            Defendant defendantIn = sendingSheet.getHearing().getDefendants().get(done);
            Defendant defendantOut = event.getOriginatingHearing().getDefendants().get(0);
            assertEqual(defendantIn, defendantOut);
            Assert.assertEquals(defendantIn.getId(), defendantOut.getId());
            Offence offenceIn = defendantIn.getOffences().get(guiltyOffenceIndex);
            Offence offenceOut = defendantOut.getOffences().get(0);
            assertEqual(offenceIn, offenceOut);
        }
    }

    @Test
    public void testTranformSendingSheetNoneGuilty() throws Exception {
        // create a sending sheet with
        int defendantCount = 3;
        int offencesPerDefendant = 5;
        BiFunction<Integer, Integer, Plea> defendantOffenceIndex2Plea = (d, o) -> {
            return null;
        };
        String hearingType = null;

        SendingSheetCompleted sendingSheet = createSendingSheet(defendantCount, (i) -> {
            return offencesPerDefendant;
        }, defendantOffenceIndex2Plea, hearingType);
        List<MagsCourtHearingRecorded> events = hearingTransformer.transform(sendingSheet.getHearing());
        Assert.assertEquals(0, events.size());

    }

    @Test
    public void testTranformSendingSheetMultiplefendantsOffenceMultipleConvictionDatesAllGuilty() throws Exception {
        // create a sending sheet with all offences guilty
        int defendantCount = 4;
        int offencesPerDefendant = 4;
        // make Pleas with plea dates going up
        BiFunction<Integer, Integer, Plea> defendantOffenceIndex2Plea = (d, o) -> {
            return new Plea(UUID.randomUUID(), LocalDate.now().plusDays(d * defendantCount + o), "GUILTY");
        };
        String hearingType = null;
        int guiltyPleaDateCount = defendantCount * offencesPerDefendant;

        SendingSheetCompleted sendingSheet = createSendingSheet(defendantCount, (i) -> {
            return offencesPerDefendant;
        }, defendantOffenceIndex2Plea, hearingType);
        List<MagsCourtHearingRecorded> events = hearingTransformer.transform(sendingSheet.getHearing());
        Assert.assertEquals(guiltyPleaDateCount, events.size());

        for (int done = 0; done < events.size(); done++) {
            MagsCourtHearingRecorded event = events.get(done);
            assertEqual(sendingSheet.getHearing(), event.getOriginatingHearing());
            Defendant defendantIn = sendingSheet.getHearing().getDefendants().get(done / defendantCount);
            Defendant defendantOut = event.getOriginatingHearing().getDefendants().get(0);
            assertEqual(defendantIn, defendantOut);
            Assert.assertEquals(defendantIn.getId(), defendantOut.getId());
            Offence offenceIn = defendantIn.getOffences().get(done % offencesPerDefendant);
            Offence offenceOut = defendantOut.getOffences().get(0);
            assertEqual(offenceIn, offenceOut);
        }
    }

}