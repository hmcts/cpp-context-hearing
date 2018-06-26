package uk.gov.moj.cpp.hearing.event.nowsdomain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Address;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Attendees;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Cases;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.CourtCentre;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Defendants;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.GenerateNowsCommand;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Hearing;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Interpreter;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.LesserOrAlternativeOffence;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Material;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.NowResult;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.NowTypes;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Nows;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Offences;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Person;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Plea;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.PromptRef;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Prompts;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.SharedResultLines;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.UserGroups;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Verdict;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * run this to create the sample data for GenerateNowsCommand - this is a replacement for manual editing of this complex file
 */
public class CreateSampleGenerateNowsCommandJson {

    public static final String IMPRISONMENT_LABEL = "Imprisonment";
    public static final String IMPRISONMENT_DURATION_VALUE = "Imprisonment duration";
    public static final String WORMWOOD_SCRUBS_VALUE = "Wormwood Scrubs";

    public static GenerateNowsCommand createSampleGenerateNowsCommand() {
        return createSampleGenerateNowsCommand(UUID.randomUUID(), UUID.randomUUID());
    }

        public static GenerateNowsCommand createSampleGenerateNowsCommand(final UUID hearingId, final UUID defendantId) {
        final UUID caseId = UUID.randomUUID();
        final UUID offenceId = UUID.randomUUID();
        final UUID nowsTypeId = UUID.randomUUID();
        final UUID materialId = UUID.randomUUID();
        final UUID sharedResultLineId0 = UUID.randomUUID();
        final UUID sharedResultLineId1 = UUID.randomUUID();
        final UUID sharedResultLineId2 = UUID.randomUUID();
        final UUID promptId00 = UUID.randomUUID();
        final UUID promptId01 = UUID.randomUUID();
        final UUID promptId10 = UUID.randomUUID();
        final UUID promptId11 = UUID.randomUUID();
        final UUID promptId20 = UUID.randomUUID();
        final UUID promptId21 = UUID.randomUUID();
        final String promptLabel0 = "Imprisonment Duration";
        final String promptLabel1 = "Prison";
        return GenerateNowsCommand.generateNowsCommand().setHearing(
                Hearing.hearing()
                        .setId(hearingId)
                        .setStartDateTime("2016-06-01T10:00:00Z")
                        .setHearingDates(Arrays.asList("2016-06-01T10:00:00Z"))
                        .setCourtCentre(CourtCentre.courtCentre()
                                //TODO check this
                                //.setCourtCentreId(UUID.randomUUID())
                                .setCourtCentreName("Liverpool Crown Court")
                                .setCourtRoomId(UUID.randomUUID())
                                .setCourtRoomName("3"))
                        .setHearingType("Sentencing")
                        .setAttendees(Arrays.asList(
                                Attendees.attendees()
                                        //.setAttendeeId(UUID.randomUUID())
                                        .setFirstName("Cherie")
                                        .setLastName("Blair")
                                        .setType("COURTCLERK")
                                ,
                                Attendees.attendees()
                                        //.setAttendeeId(UUID.randomUUID())
                                        .setFirstName("Nina")
                                        .setLastName("Turner")
                                        //.setTitle("HHJ")
                                        .setType("JUDGE")
                                ,
                                Attendees.attendees()
                                        //.setAttendeeId(UUID.randomUUID())
                                        .setFirstName("Donald")
                                        .setLastName("Smith")
                                        .setType("DEFENCEADVOCATE")
                                        //.setStatus("Leading QC")
                                        //.setDefendants(Arrays.asList(defendantId))
                                        //.setCases(Arrays.asList(caseId))
                                )
                                        )
                        .setDefendants(Arrays.asList(
                                Defendants.defendants()
                                        .setId(defendantId)
                                        .setPerson(Person.person()
                                                .setId(UUID.randomUUID())
                                                .setTitle("Mr")
                                                .setFirstName("David")
                                                .setLastName("LLOYD")
                                                .setDateOfBirth("1980-07-15")
                                                .setNationality("England")
                                                .setGender("Male")
                                                .setHomeTelephone("02012345678")
                                                .setWorkTelephone("02012345679")
                                                .setMobile("07777777777")
                                                .setFax("02011111111")
                                                .setEmail("email@email.com")
                                                .setAddress(Address.address()
                                                        .setAddressId(UUID.randomUUID())
                                                        .setAddress1("14 Tottenham Court Road")
                                                        .setAddress2("London")
                                                        .setAddress3("England")
                                                        .setAddress4("UK")
                                                        .setPostCode("W1T 1JY")
                                                )
                                        )

                                        .setDefenceOrganisation("XYZ Solicitors")
                                        .setInterpreter(Interpreter.interpreter()
                                                .setName("Robert Carlyle")
                                                .setLanguage("English"))
                                        .setCases(Arrays.asList(
                                                Cases.cases()
                                                        .setId(caseId)
                                                        .setUrn("URN123452")
                                                        .setBailStatus("in custody")
                                                        .setCustodyTimeLimitDate("2018-01-30")
                                                        .setOffences(Arrays.asList(
                                                                Offences.offences()
                                                                        .setId(offenceId)
                                                                        .setCode("OF61131")
                                                                        .setConvictionDate("2017-08-02")
                                                                        .setPlea(
                                                                                Plea.plea()
                                                                                        .setId(UUID.randomUUID())
                                                                                        .setValue("NOT GUILTY")
                                                                                        .setDate("2017-02-02")
                                                                                        .setEnteredHearingId(UUID.randomUUID())
                                                                        )
                                                                        .setVerdict(
                                                                                Verdict.verdict()
                                                                                        .setTypeId(UUID.randomUUID())
                                                                                        .setVerdictDescription("Not Guilty, guilty of a lesser or alternative offence")
                                                                                        .setVerdictCategory("GUILTY")
                                                                                        .setLesserOrAlternativeOffence(
                                                                                                LesserOrAlternativeOffence.lesserOrAlternativeOffence()
                                                                                                        .setOffenceTypeId(UUID.randomUUID())
                                                                                                        .setCode("OF62222")
                                                                                                        .setConvictionDate("2017-08-01")
                                                                                                        .setWording("On 19/01/2016 At wandsworth bridge rd SW6 Being a passenger on a Public Service Vehicle operated on behalf of London Bus Services Limited being used for the carriage of passengers at separate fares did use in relation to the journey you did not have a ticket")
                                                                                        )
                                                                                        .setNumberOfSplitJurors("9-1")
                                                                                        .setVerdictDate("2017-02-02")
                                                                                        .setNumberOfJurors(10)
                                                                                        .setUnanimous(false)
                                                                                        .setEnteredHearingId(UUID.randomUUID())
                                                                        )
                                                                        .setWording("On 19/01/2016 At wandsworth bridge rd SW6 Being a passenger on a Public Service Vehicle operated on behalf of London Bus Services Limited being used for the carriage of passengers at separate fares did use in relation to the journey you were taking a ticket which had been issued for use by another person on terms that it is not transferable")
                                                                        .setStartDate("2016-06-21")
                                                                        .setEndDate("2017-08-01")

                                                        ))
                                        ))


                        ))
                        .setSharedResultLines(Arrays.asList(
                                SharedResultLines.sharedResultLines()
                                        .setId(sharedResultLineId0)
                                        .setCaseId(caseId)
                                        .setDefendantId(defendantId)
                                        .setOffenceId(offenceId)
                                        .setLevel("CASE")
                                        .setLabel(IMPRISONMENT_LABEL)
                                        .setRank(1)
                                        .setPrompts(
                                                Arrays.asList(
                                                        Prompts.prompts()
                                                                .setId(promptId00)
                                                                .setLabel(promptLabel0)
                                                                .setValue(IMPRISONMENT_DURATION_VALUE),
                                                        Prompts.prompts()
                                                                .setId(promptId01)
                                                                .setLabel(promptLabel1)
                                                                .setValue(WORMWOOD_SCRUBS_VALUE)
                                                )
                                        ),
                                SharedResultLines.sharedResultLines()
                                        .setId(sharedResultLineId1)
                                        .setCaseId(caseId)
                                        .setDefendantId(defendantId)
                                        .setOffenceId(offenceId)
                                        .setLevel("DEFENDANT")
                                        .setLabel(IMPRISONMENT_LABEL)
                                        .setRank(2)
                                        .setPrompts(
                                                Arrays.asList(
                                                        Prompts.prompts()
                                                                .setId(promptId10)
                                                                .setLabel(promptLabel0)
                                                                .setValue(IMPRISONMENT_DURATION_VALUE),
                                                        Prompts.prompts()
                                                                .setId(promptId11)
                                                                .setLabel(promptLabel1)
                                                                .setValue(WORMWOOD_SCRUBS_VALUE)
                                                )
                                        ),
                                SharedResultLines.sharedResultLines()
                                        .setId(sharedResultLineId2)
                                        .setCaseId(caseId)
                                        .setDefendantId(defendantId)
                                        .setOffenceId(offenceId)
                                        .setLevel("OFFENCE")
                                        .setLabel(IMPRISONMENT_LABEL)
                                        .setRank(3)
                                        .setPrompts(
                                                Arrays.asList(
                                                        Prompts.prompts()
                                                                .setId(promptId20)
                                                                .setLabel(promptLabel0)
                                                                .setValue(IMPRISONMENT_DURATION_VALUE),
                                                        Prompts.prompts()
                                                                .setId(promptId21)
                                                                .setLabel(promptLabel1)
                                                                .setValue(WORMWOOD_SCRUBS_VALUE)
                                                )
                                        )

                        ))
                        .setNows(
                                Arrays.asList(
                                        Nows.nows()
                                                .setId(UUID.randomUUID())
                                                .setNowsTypeId(nowsTypeId)
                                                .setDefendantId(defendantId)
                                                //.setNowsTemplateName("SingleTemplate")
                                                .setMaterial(Arrays.asList(
                                                        Material.material()
                                                                .setId(materialId)
                                                                .setUserGroups(Arrays.asList(
                                                                        UserGroups.userGroups()
                                                                                .setGroup("COURTCLERK"),
                                                                        UserGroups.userGroups()
                                                                                .setGroup("DEFENCECOUNSEL")
                                                                ))
                                                                .setNowResult(Arrays.asList(
                                                                        NowResult.nowResult()
                                                                                .setSharedResultId(sharedResultLineId0)
                                                                                .setSequence(1)
                                                                                .setPrompts(
                                                                                        Arrays.asList(
                                                                                                PromptRef.promptRef().setId(promptId00)
                                                                                                        .setLabel(promptLabel0),
                                                                                                PromptRef.promptRef().setId(promptId01).setLabel(promptLabel1)
                                                                                        )
                                                                                ),
                                                                        NowResult.nowResult()
                                                                                .setSharedResultId(sharedResultLineId1)
                                                                                .setSequence(2)
                                                                                .setPrompts(
                                                                                        Arrays.asList(
                                                                                                PromptRef.promptRef().setId(promptId10)
                                                                                                        .setLabel(promptLabel0),
                                                                                                PromptRef.promptRef().setId(promptId11).setLabel(promptLabel1)
                                                                                        )
                                                                                ),
                                                                        NowResult.nowResult()
                                                                                .setSharedResultId(sharedResultLineId2)
                                                                                .setSequence(3)
                                                                                .setPrompts(
                                                                                        Arrays.asList(
                                                                                                PromptRef.promptRef().setId(promptId20)
                                                                                                        .setLabel(promptLabel0),
                                                                                                PromptRef.promptRef().setId(promptId21).setLabel(promptLabel1)
                                                                                        )
                                                                                )
                                                                ))
                                                ))

                                )
                        )
                        .setNowTypes(
                                Arrays.asList(
                                        NowTypes.nowTypes()
                                                .setId(nowsTypeId)
                                                .setTemplateName("SingleTemplate")
                                                .setDescription("Imprisonment Order")
                                                .setRank(1)
                                                .setStaticText("<h3>Imprisonment</h3><p>You have been sentenced to a term of imprisonment. If you<ul><li>Do not comply with the requirements of this order during the <u>supervision period</u>; or</li><li>Commit any other offence during the <u>operational period</u></li></ul>you may be liable to serve the <u>custodial period</u> in prison.<br/><br/><br/><p>For the duration of the <u>supervision period</u>, you will be supervised by your Probation Officer, and<br/>You must<ul><li>Keep in touch with your Probation Officer as they tell you</li><li>Tell your Probation Officer if you intend to change your address</li><li>Comply with all other requirements</li></ul><p><strong>Requirements</strong> – Please refer only to the requirements that the court has specified in the details of your order, <u>as set out above</u><p><strong>Unpaid Work Requirement</strong><p>You must carry out unpaid work for the hours specified as you are told and by the date specified in the order. Your Probation Officer will tell you who will be responsible for supervising work.<p><strong>Activity Requirement</strong><p>You must present yourself as directed at the time and on the days specified in the order and you must undertake the activity the court has specified for the duration specified in the order in the way you are told by your Probation Officer<p><strong>Programme Requirement</strong><p>You must participate in the programme specified in the order at the location specified and for the number of days specified in the order<p><strong>Prohibited Activity Requirement</strong><p>You must not take part in the activity that the court has prohibited in the order for the number of days the court specified<p><strong>Curfew Requirement</strong><p>You must remain in the place or places the court has specified during the periods specified. The curfew requirement lasts for the number of days specified in the order<p>See \"Electronic Monitoring Provision\" in this order<p><strong>Exclusion Requirement</strong><p>You must not enter the place or places the court has specified between the hours specified in the order. The exclusion requirement lasts for the number of days specified in the order<p>See \"Electronic Monitoring Provision\" in this order<p><strong>Residence Requirement</strong><p>You must live at the premises the court has specified and obey any rules that apply there for the number of days specified in the order. You may live at ???? with the prior approval of your Probation Officer.<p><strong>Foreign Travel Prohibition Requirement</strong><p>You must not travel to the prohibited location specified in the order during the period the court has specified in the order.<p><strong>Mental Health Treatment Requirement</strong><p>You must have mental health treatment by or under the direction of the practitioner the court has specified at the location specified as a resident patient for the number of days specified in the order.<p><strong>Drug Rehabilitation Requirement</strong><p>You must have treatment for drug dependency by or under the direction of the practitioner the court has specified at the location specified as a resident patient for the number of days specified in the order.<p>To be sure that you do not have any illegal drug in your body, you must provide samples for testing at such times or in such circumstances as your Probation Officer or the person responsible for your treatment will tell you. The results of tests on the samples will be sent to your Probation Officer who will report the results to the court. Your Probation Officer will also tell the court how your order is progressing and the views of your treatment provider.<p>The court will review this order ????. The first review will be on the date and time specified at the court specified.<p>You must / need not attend this review hearing.<p><strong>Alcohol Treatment Requirement</strong><p>You must have treatment for alcohol dependency by or under the direction of the practitioner the court has specified at the location specified as a resident patient for the number of days specified in the order.<p><strong>Supervision Requirement</strong><p>You must attend appointments with your Probation Officer or another person at the times and places your Probation Officer says.<p><strong>Attendance Centre Requirement</strong><p>You must attend an attendance centre - see separate sheet for details<p><strong>WARNING</strong><p>If you do not comply with your order, you will be brought back to court. The court may then<ul><li>Change the order by adding extra requirements</li><li>Pass a different sentence for the original offences; or</li><li>Send you to prison</li></ul><p><strong>NOTE</strong><p>Either you or your Probation Officer can ask the court to look again at this order and the court can then change it or cancel it if it feels that is the right thing to do. The court may also pass a different sentence for the original offence(s). If you wish to ask the court to look at your order again you should get in touch with the court at the address above.")
                                                .setStaticTextWelsh("<h3> Prison </h3> <p> Fe'ch dedfrydwyd i dymor o garchar. Os ydych <ul> <li> Peidiwch â chydymffurfio â gofynion y gorchymyn hwn yn ystod y cyfnod goruchwylio </u>; neu </li> <li> Ymrwymo unrhyw drosedd arall yn ystod y cyfnod gweithredol </u> </li> </ul> efallai y byddwch yn atebol i wasanaethu'r cyfnod gwarchodaeth </u> yn y carchar. <br/> <br/> <br/> <p> Yn ystod y cyfnod goruchwylio </u>, byddwch chi'n cael eich goruchwylio gan eich Swyddog Prawf, a <br/> Rhaid ichi <ul> < li> Cadwch mewn cysylltiad â'ch Swyddog Prawf wrth iddyn nhw ddweud wrthych </li> <li> Dywedwch wrth eich Swyddog Prawf os ydych yn bwriadu newid eich cyfeiriad </li> <li> Cydymffurfio â'r holl ofynion eraill </li></ul > <p> <strong> Gofynion </strong> - Cyfeiriwch yn unig at y gofynion a nododd y llys yn manylion eich archeb, fel y nodir uchod </u> <p> <strong> Gwaith Di-dāl Gofyniad </strong><p> Rhaid i chi wneud gwaith di-dāl am yr oriau a bennir fel y dywedir wrthych a chi erbyn y dyddiad a bennir yn y gorchymyn. Bydd eich Swyddog Prawf yn dweud wrthych pwy fydd yn gyfrifol am oruchwylio gwaith.<p> <strong> Gweithgaredd Gofyniad </strong> <p> Rhaid i chi gyflwyno eich hun fel y'i cyfarwyddir ar yr amser ac ar y diwrnodau a bennir yn y gorchymyn a rhaid i chi ymgymryd â chi y gweithgaredd y mae'r llys wedi ei nodi ar gyfer y cyfnod a bennir yn y drefn yn y ffordd y dywedir wrth eich Swyddog Prawf <p> <strong> Gofyniad Rhaglen </strong><p> Rhaid i chi gymryd rhan yn y rhaglen a bennir yn y drefn yn y lleoliad a bennir ac am y nifer o ddyddiau a bennir yn y gorchymyn <p> <strong> Gofyniad Gweithgaredd Gwahardd </strong> <p> Rhaid i chi beidio â chymryd rhan yn y gweithgaredd a waharddodd y llys yn y drefn ar gyfer nifer y dyddiau llys penodol <p> <strong> Curfew Requirement </strong> <p> Rhaid i chi aros yn y lle neu lle mae'r llys wedi nodi yn ystod y cyfnodau a bennir. Mae'r gofyniad cyrffyw yn para am y nifer o ddyddiau a bennir yn y<p> Gweler \"Darpariaeth Monitro Electronig\" yn yr orchymyn hwn <p> <strong> Gofyniad Preswyl </strong> <p> Rhaid i chi fyw yn yr adeilad y llys wedi nodi ac ufuddhau i unrhyw reolau sy'n berthnasol yno am y nifer o ddyddiau a bennir yn y gorchymyn. Efallai y byddwch yn byw yn ???? gyda chymeradwyaeth ymlaen llaw eich Swyddog Prawf. <p> <strong> Gofyniad Gwahardd Teithio Tramor </strong> <p> Rhaid i chi beidio â theithio i'r lleoliad gwaharddedig a bennir yn yr orchymyn yn ystod y cyfnod y mae'r llys wedi'i bennu yn y gorchymyn. < p> <strong> Gofyniad Triniaeth Iechyd Meddwl </strong> <p> Rhaid i chi gael triniaeth iechyd meddwl gan neu o dan gyfarwyddyd yr ymarferydd y mae'r llys wedi ei nodi yn y lleoliad a bennir fel claf preswyl am y nifer o ddyddiau a bennir yn y <p> <strong> Angen Adsefydlu Cyffuriau </strong> <p> Rhaid i chi gael triniaeth ar gyfer dibyniaeth ar gyffuriau gan neu o dan gyfarwyddyd yr ymarferydd y mae'r llys wedi ei nodi yn y lleoliad a bennir fel claf preswyl am nifer y dyddiau <p> Er mwyn sicrhau nad oes gennych unrhyw gyffur anghyfreithlon yn eich corff, rhaid i chi ddarparu samplau i'w profi ar yr adegau hynny neu mewn amgylchiadau o'r fath y bydd eich Swyddog Prawf neu'r person sy'n gyfrifol am eich triniaeth yn dweud wrthych chi . Anfonir canlyniadau'r profion ar y samplau i'ch Swyddog Prawf a fydd yn adrodd y canlyniadau i'r llys. Bydd eich Swyddog Prawf hefyd yn dweud wrth y llys sut mae'ch gorchymyn yn mynd rhagddo a barn eich darparwr triniaeth. <P> Bydd y llys yn adolygu'r gorchymyn hwn ????. Bydd yr adolygiad cyntaf ar y dyddiad a'r amser a bennir yn y llys a bennir. <P> Rhaid i chi / nid oes angen i chi fynychu'r gwrandawiad hwn. <P> <strong> Gofyniad Trin Alcohol </strong> <p> Rhaid i chi gael triniaeth ar gyfer dibyniaeth ar alcohol gan neu o dan gyfarwyddyd yr ymarferydd y mae'r llys wedi ei nodi yn y lleoliad a bennir fel claf preswyl am y nifer o ddyddiau a bennir yn y gorchymyn. <p> <strong> Gofyniad Goruchwylio </strong> <p> Rhaid i chi fynychu penodiadau gyda'ch Swyddog Prawf neu berson arall ar yr adegau a lle mae eich Swyddog Prawf yn dweud. <p> <strong> Gofyniad y Ganolfan Bresennol </strong> <p> Rhaid i chi fynychu canolfan bresenoldeb - <p> <strong> RHYBUDD </strong> <p> Os na fyddwch chi'n cydymffurfio â'ch archeb, fe'ch cewch eich troi'n ôl i'r llys. Gall y llys wedyn <ul> <li> Newid y gorchymyn trwy ychwanegu gofynion ychwanegol </li> <li> Pasiwch frawddeg wahanol ar gyfer y troseddau gwreiddiol; neu </li> <li> Anfonwch chi at y carchar </li> </ul> <p> <strong> NOTE </strong> <p> Naill ai chi neu'ch Swyddog Prawf all ofyn i'r llys edrych eto ar y gorchymyn hwn ac yna gall y llys ei newid neu ei ganslo os yw'n teimlo mai dyna'r peth iawn i'w wneud. Gall y llys hefyd basio brawddeg wahanol ar gyfer y trosedd (wyr) gwreiddiol. Os hoffech ofyn i'r llys edrych ar eich archeb eto dylech gysylltu â'r llys yn y cyfeiriad uchod. ")
                                                .setPriority("0.5 hours")
                                                .setJurisdiction("B")
                                )
                        )
        );
    }



    @SuppressWarnings({"squid:S106", "squid:S2096"})
    public static void main(String[] args) throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_ABSENT);
        final GenerateNowsCommand command = createSampleGenerateNowsCommand();

        final List<String> destinations = Arrays.asList(
                "./hearing-command/hearing-command-api/src/raml/json/hearing.generate-nows.json",
                "./hearing-command/hearing-command-handler/src/raml/json/hearing.command.generate-nows.json",
                "./hearing-domain/hearing-domain-event/src/test/resources/hearing.events.nows-requested.json",
                "./hearing-event/hearing-event-listener/src/raml/json/hearing.events.nows-requested.json",
                "./hearing-event/hearing-event-processor/src/raml/json/hearing.events.nows-requested.json",
                "./hearing-event/hearing-event-processor/src/test/resources/data/hearing.events.nows-requested.json"
        );

        for (String strFile : destinations) {
            final File file = new File(strFile);
            System.out.println("writing to file " + file.getAbsolutePath());
            objectMapper.writeValue(file, command);
        }
    }

}
