package uk.gov.moj.cpp.hearing.domain.aggregate;

import uk.gov.justice.progression.events.SendingSheetCompleted;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.Address;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.CrownCourtHearing;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.Defendant;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.Hearing;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.Interpreter;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.Offence;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.Plea;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.PleaValue;
import uk.gov.moj.cpp.hearing.domain.event.MagsCourtHearingRecorded;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Assert;
import org.junit.Test;


/**
 * assumptions: conviction date is the same as guilty plea date
 */
public class HearingsTransformerTest {

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private LocalDate date(final String strDate) {
        return LocalDate.parse(strDate, dateTimeFormatter);
    }

    private SendingSheetCompleted createSendingSheet(final int defendantCount,
                                                     final Function<Integer, Integer> defendantIndex2OffenceCount,
                                                     final BiFunction<Integer, Integer, Plea> defendantOffenceIndexToPlea,
                                                     final String hearingType) {

        final List<Defendant> arrDefendants = new ArrayList<>();
        for (int defendantIndex = 0; defendantIndex<defendantCount; defendantIndex++) {
            final List lOffences = new ArrayList<>();
            final int offenceCount = defendantIndex2OffenceCount.apply(defendantIndex);
            for (int offenceIndex=0; offenceIndex<offenceCount; offenceIndex++) {
                final Plea plea = defendantOffenceIndexToPlea.apply(defendantIndex, offenceIndex);
                final LocalDate convictionDate = plea!=null && plea.getValue().equals(PleaValue.GUILTY) ? plea.getPleaDate() : null;
                final UUID offenceId = UUID.randomUUID();
                final Offence offence = (new Offence.Builder()).withId(offenceId).withCategory("category").withConvictionDate(convictionDate).
                        withDescription("testOffence").withEndDate(date("12/11/2016")).withPlea(plea)
                        .withReason("Reason")
                        .withSection("section")
                        .withWording("wording")
                        .withOffenceCode("offenceCode")
                        .build();
                lOffences.add(offence);
            }

            final Address address = (new Address.Builder()).withAddress1("addr1").withAddress2("addr2").withAddress3("addr3").
                    withAddress4("addr4").withPostcode("AA1 1AA").build();
            final UUID defendantId = UUID.randomUUID();
            final Interpreter interpreter = (new Interpreter.Builder()).withLanguage("English").withNeeded(true).build();
            final Defendant defendant = (new Defendant.Builder()).withOffences(lOffences).withAddress(address).withBailStatus("bailStatus").
                    withCustodyTimeLimitDate(date("11/12/2017")).withDateOfBirth(date("12/11/1978")).withDefenceOrganisation("CPP").
                    withFirstName("Geoff").withLastName("ssdfsf").withGender("Male").withId(defendantId).
                    withInterpreter(interpreter).withNationality("UK").withPersonId(UUID.randomUUID()).
                    build();
            arrDefendants.add(defendant);
        }

        final UUID caseId = UUID.randomUUID();

        final Hearing hearing = (new Hearing.Builder()).withCaseId(caseId).withCaseUrn("caseUrn").
                withCourtCentreId("courtCentreId").withCourtCentreName("courtCentreName").withType(hearingType).
                withSendingCommittalDate(date("14/11/2019")).withDefendants(arrDefendants).build();


        final UUID courtCentreId = UUID.randomUUID();
        final CrownCourtHearing crownCourtHearing = (new CrownCourtHearing.Builder()).withCcHearingDate("ccHearingDate").
                withCourtCentreId(courtCentreId).withCourtCentreName("courtCentrName").build();

        final SendingSheetCompleted  sendingSheetCompleted =   (new SendingSheetCompleted.Builder()).withHearing(hearing).
                withCrownCourtHearing(crownCourtHearing).build();
        return sendingSheetCompleted;
    }

    @Test
    public void testTranformSendingSheet1GuiltyPlea() throws Exception {
        final int defendantCount = 3;
        final int offencesPerDefendant=3;
        final LocalDate[] convictionDates  ={date( "10/11/2017"), date("08/11/2017"),  date("13/11/2017") };
        final int guiltyPleaFrequency  = 9;
        testTranformSendingSheet(defendantCount, offencesPerDefendant, convictionDates,  guiltyPleaFrequency, "oneguilty", "hearingTypeXX");

    }

    private static class SendingSheetRelationships {
        Map<UUID, UUID> sendingSheetDefendantId2RelevantOffence = new HashMap<>();
        Map<UUID, UUID> sendingSheetOffence2RelevantPlea = new HashMap<>();
        Map<UUID, Defendant> uuid2Defendants = new HashMap<>();


        @Override
        public boolean equals(final Object o) {
           return o!=null && o instanceof SendingSheetRelationships &&
                   sendingSheetDefendantId2RelevantOffence.equals(((SendingSheetRelationships) o).sendingSheetDefendantId2RelevantOffence) &&
            sendingSheetOffence2RelevantPlea.equals(((SendingSheetRelationships) o).sendingSheetOffence2RelevantPlea);
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            final String indent = "  ";
            final String newLine = System.lineSeparator();
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

    private SendingSheetRelationships extractGuiltyRelationships(final List<Defendant> defendants, final LocalDate convictionDate) {
        final SendingSheetRelationships result = new SendingSheetRelationships();
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
        final int defendantCount = 3;
        final int offencesPerDefendant=5;
        final LocalDate[] convictionDates  ={date( "10/11/2017"), date("08/11/2017"),  date("13/11/2017") };
        final int guiltyPleaFrequency  = 3;
        testTranformSendingSheet(defendantCount, offencesPerDefendant, convictionDates,  guiltyPleaFrequency, "partialguilty", null);
        }

    @Test
    public void testTranformSendingSheetMultiplefendantsOffencesMultipleConvictionDatesNoneGuilty() throws Exception {
        final int defendantCount = 2;
        final int offencesPerDefendant=2;
        final LocalDate[] convictionDates  ={date( "10/11/2017"), date("08/11/2017"),  date("13/11/2017") };
        final int guiltyPleaFrequency  = 0;
        testTranformSendingSheet(defendantCount, offencesPerDefendant, convictionDates,  guiltyPleaFrequency, "nonguilty", null);
    }


    @Test
    public void testTranformSendingSheetMultiplefendantsOffencesMultipleConvictionDatesAllGuilty() throws Exception {
        final int defendantCount = 4;
        final int offencesPerDefendant=4;
        final LocalDate[] convictionDates  ={date( "10/11/2017"), date("08/11/2017"),  date("13/11/2017") };
        final int guiltyPleaFrequency  = 1;
        testTranformSendingSheet(defendantCount, offencesPerDefendant, convictionDates,  guiltyPleaFrequency, "allguilty", null);
    }


    public void testTranformSendingSheet(final int defendantCount,
                                         final int offencesPerDefendant, final LocalDate[] convictionDates, final int guiltyPleaFrequency,
                                         final String testName, final String hearingType) throws Exception {

        final Function<Integer, Integer> defendantIndex2OffenceCount = (di) -> {return offencesPerDefendant;};

        // TODO there is only 1 plea value available so cant check behaviour for other plea values !
        final Set<LocalDate> pleaDatesUsedInTest = new HashSet<>();
        final BiFunction<Integer, Integer, Plea> defendantOffenceIndexToPlea = (defendantIndex, offenceIndex) -> {
            final int combinedOffenceIndex= offencesPerDefendant*defendantIndex + offenceIndex;
            final boolean isGuiltyPlea =  guiltyPleaFrequency>0 &&  combinedOffenceIndex%guiltyPleaFrequency ==0;
            final int guiltyPleaIndex =   guiltyPleaFrequency>0 ? combinedOffenceIndex/guiltyPleaFrequency : 0;
            Plea plea = null;
            if (isGuiltyPlea) {
                final LocalDate pleaDate = convictionDates[guiltyPleaIndex%convictionDates.length];
                pleaDatesUsedInTest.add(pleaDate);
                plea = new Plea(UUID.randomUUID(), pleaDate, PleaValue.GUILTY);
            } else {
                plea = null;
            }
            return plea;
        };

        final SendingSheetCompleted sendingSheet = createSendingSheet(defendantCount, defendantIndex2OffenceCount, defendantOffenceIndexToPlea, hearingType);
        final List<MagsCourtHearingRecorded> events = HearingTransformer.transform(sendingSheet.getHearing());
        final int expectedEventCount = pleaDatesUsedInTest.size();
        Assert.assertEquals(events.size(), expectedEventCount);
        //assume date order !!
        final List<LocalDate> orderedConvictionDates =  new ArrayList<>(pleaDatesUsedInTest);
        orderedConvictionDates.sort((d1, d2) -> d1.compareTo(d2) );


        for (int done = 0; done<events.size(); done++) {
            final LocalDate convictionDate = orderedConvictionDates.get(done);
            final MagsCourtHearingRecorded magsCourtHearingRecorded = events.get(done);
            Assert.assertEquals(magsCourtHearingRecorded.getConvictionDate(), convictionDate);
            final SendingSheetRelationships sendingSheetRelationsShips =  extractGuiltyRelationships(sendingSheet.getHearing().getDefendants(), convictionDate);
            final SendingSheetRelationships eventRelationships =  extractGuiltyRelationships(magsCourtHearingRecorded.getOriginatingHearing().getDefendants(), convictionDate);
            Assert.assertEquals(sendingSheetRelationsShips, eventRelationships);
            final String expectedHearingType = hearingType==null?HearingTransformer.DEFAULT_HEARING_TYPE:hearingType;
            Assert.assertEquals(expectedHearingType,  magsCourtHearingRecorded.getOriginatingHearing().getType());

           // sendingSheetRelationsShips.shallowCompareDefendants(eventRelationships);

            //check more values
            // check defendants
            //ShallowCompareByPublicGetters


        }


        writeResults(sendingSheet, events, testName);

    }

    private void writeResults(final SendingSheetCompleted sendingSheet, final List<MagsCourtHearingRecorded> outputEvents, final String resultName) {
        Path filepath=null;
        try {
            final Path directory = Files.createTempDirectory(resultName + "_json");
            System.out.println("writing result to " + directory.toString());
            final ObjectMapper objectMapper = new ObjectMapper();
            //objectMapper.setDefaultPrettyPrinter()
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            filepath = directory.resolve("sendingSheet.json");
            objectMapper.writeValue(filepath.toFile(), sendingSheet);
            final DateTimeFormatter df = DateTimeFormatter.ofPattern("dd_MM_yyyy");

            for (final MagsCourtHearingRecorded event : outputEvents) {
                        final Path eventFilepath = directory.resolve("forHearing_" + event.getConvictionDate().format(df) + ".json" );
                        objectMapper.writeValue(eventFilepath.toFile(), event);
                    }
            System.out.println("wrote result to " + directory.toString());

        }   catch (final IOException iex) {
            System.err.println("failed to created write file " + filepath);
        }
    }


    private void writeAsJson2File(final Path directory, final Object o, final String filename) throws Exception {
            final Path filepath = directory.resolve(filename + ".json`");
            (new ObjectMapper()).writeValue(filepath.toFile(), o);
    }


}
