package uk.gov.moj.cpp.hearing.domain.aggregate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Assert;
import org.junit.Test;
import uk.gov.justice.progression.events.SendingSheetCompleted;
import uk.gov.moj.cpp.hearing.domain.event.MagsCourtHearingRecorded;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;


/**
 * assumptions: conviction date is the same as guilty plea date
 */
public class HearingsTransformerTest {

    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private LocalDate date(String strDate) {
        return LocalDate.parse(strDate, dateTimeFormatter);
    }

    private SendingSheetCompleted createSendingSheet(final int defendantCount,
                                                     final Function<Integer, Integer> defendantIndex2OffenceCount,
                                                     final BiFunction<Integer, Integer, Plea> defendantOffenceIndexToPlea,
                                                     String hearingType) {

        List<Defendants> arrDefendants = new ArrayList<>();
        for (int defendantIndex = 0; defendantIndex<defendantCount; defendantIndex++) {
            List lOffences = new ArrayList<>();
            int offenceCount = defendantIndex2OffenceCount.apply(defendantIndex);
            for (int offenceIndex=0; offenceIndex<offenceCount; offenceIndex++) {
                Plea plea = defendantOffenceIndexToPlea.apply(defendantIndex, offenceIndex);
                LocalDate convictionDate = plea!=null && plea.getValue().equals(PleaValue.GUILTY) ? plea.getPleaDate() : null;
                UUID offenceId = UUID.randomUUID();
                Offences offence = (new Offences.Builder()).withId(offenceId).withCategory("category").withConvictionDate(convictionDate).
                        withDescription("testOffence").withEndDate(date("12/11/2016")).withPlea(plea)
                        .withReason("Reason")
                        .withSection("section")
                        .withWording("wording")
                        .withOffenceCode("offenceCode")
                        .build();
                lOffences.add(offence);
            }

            Address address = (new Address.Builder()).withAddress1("addr1").withAddress2("addr2").withAddress3("addr3").
                    withAddress4("addr4").withPostcode("AA1 1AA").build();
            UUID defendantId = UUID.randomUUID();
            Interpreter interpreter = (new Interpreter.Builder()).withLanguage("English").withNeeded(true).build();
            Defendants defendant = (new Defendants.Builder()).withOffences(lOffences).withAddress(address).withBailStatus("bailStatus").
                    withCustodyTimeLimitDate(date("11/12/2017")).withDateOfBirth(date("12/11/1978")).withDefenceOrganisation("CPP").
                    withFirstName("Geoff").withLastName("ssdfsf").withGender(Gender.MALE).withId(defendantId).
                    withInterpreter(interpreter).withNationality("UK").withPersonId(UUID.randomUUID()).
                    build();
            arrDefendants.add(defendant);
        }

        UUID caseId = UUID.randomUUID();

        Hearing hearing = (new Hearing.Builder()).withCaseId(caseId).withCaseUrn("caseUrn").
                withCourtCentreId("courtCentreId").withCourtCentreName("courtCentreName").withType(hearingType).
                withSendingCommittalDate(date("14/11/2019")).withDefendants(arrDefendants).build();


        UUID courtCentreId = UUID.randomUUID();
        CrownCourtHearing crownCourtHearing = (new CrownCourtHearing.Builder()).withCcHearingDate("ccHearingDate").
                withCourtCentreId(courtCentreId).withCourtCentreName("courtCentrName").build();

        SendingSheetCompleted  sendingSheetCompleted =   (new SendingSheetCompleted.Builder()).withHearing(hearing).
                withCrownCourtHearing(crownCourtHearing).build();
        return sendingSheetCompleted;
    }

    @Test
    public void testTranformSendingSheet1GuiltyPlea() throws Exception {
        int defendantCount = 3;
        int offencesPerDefendant=3;
        LocalDate[] convictionDates  ={date( "10/11/2017"), date("08/11/2017"),  date("13/11/2017") };
        int guiltyPleaFrequency  = 9;
        testTranformSendingSheet(defendantCount, offencesPerDefendant, convictionDates,  guiltyPleaFrequency, "oneguilty", "hearingTypeXX");

    }

    private static class SendingSheetRelationships {
        Map<UUID, UUID> sendingSheetDefendantId2RelevantOffence = new HashMap<>();
        Map<UUID, UUID> sendingSheetOffence2RelevantPlea = new HashMap<>();
        Map<UUID, Defendants> uuid2Defendants = new HashMap<>();


        @Override
        public boolean equals(Object o) {
           return o!=null && o instanceof SendingSheetRelationships &&
                   sendingSheetDefendantId2RelevantOffence.equals(((SendingSheetRelationships) o).sendingSheetDefendantId2RelevantOffence) &&
            sendingSheetOffence2RelevantPlea.equals(((SendingSheetRelationships) o).sendingSheetOffence2RelevantPlea);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            String indent = "  ";
            String newLine = System.lineSeparator();
            sb.append("defendant to offence: " + newLine);
            sendingSheetDefendantId2RelevantOffence.entrySet().forEach(
                    (entry) -> {sb.append(indent + entry.getKey() + " => " + entry.getValue() + newLine);}
            );
            sb.append("offence to plea: " + newLine);
            sendingSheetOffence2RelevantPlea.entrySet().forEach(
                    (entry) -> {sb.append(indent + entry.getKey() + " => " + entry.getValue() + newLine);}
            );
            return sb.toString();
        }
    }

    private SendingSheetRelationships extractGuiltyRelationships(List<Defendants> defendants, LocalDate convictionDate) {
        SendingSheetRelationships result = new SendingSheetRelationships();
        defendants.forEach(
                d->{
                    //result.uuid2Defendants.put(d.getId(), );
                    d.getOffences().
                        forEach(
                                o->{
                                    if (o.getPlea()!=null && o.getPlea().getValue().equals(PleaValue.GUILTY) && o.getPlea().getPleaDate().equals(convictionDate)) {
                                        result.sendingSheetDefendantId2RelevantOffence.put(d.getId(), o.getId());
                                        result.sendingSheetOffence2RelevantPlea.put(o.getId(), o.getPlea().getId());
                                        result.uuid2Defendants.put(d.getId(), d);
                                    }
                                }
                        );
                    }
        );
        return result;
    }

    @Test
    public void testTranformSendingSheetMultiplefendantsOffencesMultipleConvictionDatesSomePartialGuilty() throws Exception {
        int defendantCount = 3;
        int offencesPerDefendant=5;
        LocalDate[] convictionDates  ={date( "10/11/2017"), date("08/11/2017"),  date("13/11/2017") };
        int guiltyPleaFrequency  = 3;
        testTranformSendingSheet(defendantCount, offencesPerDefendant, convictionDates,  guiltyPleaFrequency, "partialguilty", null);
        }

    @Test
    public void testTranformSendingSheetMultiplefendantsOffencesMultipleConvictionDatesNoneGuilty() throws Exception {
        int defendantCount = 2;
        int offencesPerDefendant=2;
        LocalDate[] convictionDates  ={date( "10/11/2017"), date("08/11/2017"),  date("13/11/2017") };
        int guiltyPleaFrequency  = 0;
        testTranformSendingSheet(defendantCount, offencesPerDefendant, convictionDates,  guiltyPleaFrequency, "nonguilty", null);
    }


    @Test
    public void testTranformSendingSheetMultiplefendantsOffencesMultipleConvictionDatesAllGuilty() throws Exception {
        int defendantCount = 4;
        int offencesPerDefendant=4;
        LocalDate[] convictionDates  ={date( "10/11/2017"), date("08/11/2017"),  date("13/11/2017") };
        int guiltyPleaFrequency  = 1;
        testTranformSendingSheet(defendantCount, offencesPerDefendant, convictionDates,  guiltyPleaFrequency, "allguilty", null);
    }


    public void testTranformSendingSheet(int defendantCount,
                                        int offencesPerDefendant, LocalDate[] convictionDates, int guiltyPleaFrequency,
                                        String testName, String hearingType) throws Exception {

        Function<Integer, Integer> defendantIndex2OffenceCount = (di) -> {return offencesPerDefendant;};

        // TODO there is only 1 plea value available so cant check behaviour for other plea values !
        Set<LocalDate> pleaDatesUsedInTest = new HashSet<>();
        BiFunction<Integer, Integer, Plea> defendantOffenceIndexToPlea = (defendantIndex, offenceIndex) -> {
            int combinedOffenceIndex= offencesPerDefendant*defendantIndex + offenceIndex;
            boolean isGuiltyPlea =  guiltyPleaFrequency>0 &&  combinedOffenceIndex%guiltyPleaFrequency ==0;
            int guiltyPleaIndex =   guiltyPleaFrequency>0 ? combinedOffenceIndex/guiltyPleaFrequency : 0;
            Plea plea = null;
            if (isGuiltyPlea) {
                LocalDate pleaDate = convictionDates[guiltyPleaIndex%convictionDates.length];
                pleaDatesUsedInTest.add(pleaDate);
                plea = new Plea(UUID.randomUUID(), pleaDate, PleaValue.GUILTY);
            } else {
                plea = null;
            }
            return plea;
        };

        SendingSheetCompleted sendingSheet = createSendingSheet(defendantCount, defendantIndex2OffenceCount, defendantOffenceIndexToPlea, hearingType);
        List<MagsCourtHearingRecorded> events = HearingTransformer.transform(sendingSheet.getHearing());
        int expectedEventCount = pleaDatesUsedInTest.size();
        Assert.assertEquals(events.size(), expectedEventCount);
        //assume date order !!
        List<LocalDate> orderedConvictionDates =  new ArrayList<>(pleaDatesUsedInTest);
        orderedConvictionDates.sort((d1, d2) -> d1.compareTo(d2) );


        for (int done = 0; done<events.size(); done++) {
            LocalDate convictionDate = orderedConvictionDates.get(done);
            MagsCourtHearingRecorded magsCourtHearingRecorded = events.get(done);
            Assert.assertEquals(magsCourtHearingRecorded.getConvictionDate(), convictionDate);
            SendingSheetRelationships sendingSheetRelationsShips =  extractGuiltyRelationships(sendingSheet.getHearing().getDefendants(), convictionDate);
            SendingSheetRelationships eventRelationships =  extractGuiltyRelationships(magsCourtHearingRecorded.getOriginatingHearing().getDefendants(), convictionDate);
            Assert.assertEquals(sendingSheetRelationsShips, eventRelationships);
            String expectedHearingType = hearingType==null?HearingTransformer.DEFAULT_HEARING_TYPE:hearingType;
            Assert.assertEquals(expectedHearingType,  magsCourtHearingRecorded.getOriginatingHearing().getType());

           // sendingSheetRelationsShips.shallowCompareDefendants(eventRelationships);

            //check more values
            // check defendants
            //ShallowCompareByPublicGetters


        }


        writeResults(sendingSheet, events, testName);

    }

    private void writeResults(SendingSheetCompleted sendingSheet, List<MagsCourtHearingRecorded> outputEvents, String resultName) {
        Path filepath=null;
        try {
            Path directory = Files.createTempDirectory(resultName + "_json");
            System.out.println("writing result to " + directory.toString());
            ObjectMapper objectMapper = new ObjectMapper();
            //objectMapper.setDefaultPrettyPrinter()
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            filepath = directory.resolve("sendingSheet.json");
            objectMapper.writeValue(filepath.toFile(), sendingSheet);
            DateTimeFormatter df = DateTimeFormatter.ofPattern("dd_MM_yyyy");

            for (MagsCourtHearingRecorded event : outputEvents) {
                        Path eventFilepath = directory.resolve("forHearing_" + event.getConvictionDate().format(df) + ".json" );
                        objectMapper.writeValue(eventFilepath.toFile(), event);
                    }
            System.out.println("wrote result to " + directory.toString());

        }   catch (IOException iex) {
            System.err.println("failed to created write file " + filepath);
        }
    }


    private void writeAsJson2File(Path directory, Object o, String filename) throws Exception {
            Path filepath = directory.resolve(filename + ".json`");
            (new ObjectMapper()).writeValue(filepath.toFile(), o);
    }


}