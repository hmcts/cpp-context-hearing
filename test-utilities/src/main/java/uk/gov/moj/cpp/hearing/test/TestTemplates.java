package uk.gov.moj.cpp.hearing.test;

import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static uk.gov.justice.core.courts.HearingLanguage.ENGLISH;
import static uk.gov.justice.core.courts.JurisdictionType.CROWN;
import static uk.gov.justice.core.courts.JurisdictionType.MAGISTRATES;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.BOOLEAN;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.FUTURE_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.INTEGER;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.integer;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.randomEnum;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.CoreTemplateArguments.toMap;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.DefendantType.ORGANISATION;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.DefendantType.PERSON;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.associatedPerson;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.defaultArguments;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.legalEntityDefendant;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.organisation;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.personDefendant;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.customStructureInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.asList;

import uk.gov.justice.core.courts.Address;
import uk.gov.justice.core.courts.AttendanceDay;
import uk.gov.justice.core.courts.CourtDecision;
import uk.gov.justice.core.courts.CreateNowsRequest;
import uk.gov.justice.core.courts.DefenceCounsel;
import uk.gov.justice.core.courts.DefendantRepresentation;
import uk.gov.justice.core.courts.DelegatedPowers;
import uk.gov.justice.core.courts.DocumentationLanguage;
import uk.gov.justice.core.courts.FinancialOrderDetails;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.IndicatedPleaValue;
import uk.gov.justice.core.courts.Jurors;
import uk.gov.justice.core.courts.LesserOrAlternativeOffence;
import uk.gov.justice.core.courts.Now;
import uk.gov.justice.core.courts.NowType;
import uk.gov.justice.core.courts.NowVariant;
import uk.gov.justice.core.courts.NowVariantKey;
import uk.gov.justice.core.courts.NowVariantResult;
import uk.gov.justice.core.courts.NowVariantResultText;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.Organisation;
import uk.gov.justice.core.courts.Plea;
import uk.gov.justice.core.courts.PleaValue;
import uk.gov.justice.core.courts.ProsecutionCounsel;
import uk.gov.justice.core.courts.ProsecutionRepresentation;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.justice.core.courts.ResultPrompt;
import uk.gov.justice.core.courts.SharedResultLine;
import uk.gov.justice.core.courts.Source;
import uk.gov.justice.core.courts.Target;
import uk.gov.justice.core.courts.Verdict;
import uk.gov.justice.core.courts.VerdictType;
import uk.gov.justice.hearing.courts.AddDefenceCounsel;
import uk.gov.justice.hearing.courts.AddProsecutionCounsel;
import uk.gov.justice.hearing.courts.UpdateDefenceCounsel;
import uk.gov.justice.hearing.courts.UpdateProsecutionCounsel;
import uk.gov.justice.progression.events.CaseDefendantDetails;
import uk.gov.justice.services.test.utils.core.random.RandomGenerator;
import uk.gov.moj.cpp.hearing.command.defendant.CaseDefendantDetailsWithHearingCommand;
import uk.gov.moj.cpp.hearing.command.defendant.Defendant;
import uk.gov.moj.cpp.hearing.command.defendant.UpdateDefendantAttendanceCommand;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.logEvent.CreateHearingEventDefinitionsCommand;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.ResultLineReference;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.Variant;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.VariantKey;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.VariantValue;
import uk.gov.moj.cpp.hearing.command.offence.DefendantCaseOffences;
import uk.gov.moj.cpp.hearing.command.offence.DeletedOffences;
import uk.gov.moj.cpp.hearing.command.offence.UpdateOffencesForDefendantCommand;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLineStatus;
import uk.gov.moj.cpp.hearing.command.result.SaveDraftResultCommand;
import uk.gov.moj.cpp.hearing.command.result.ShareResultsCommand;
import uk.gov.moj.cpp.hearing.command.subscription.UploadSubscription;
import uk.gov.moj.cpp.hearing.command.subscription.UploadSubscriptionsCommand;
import uk.gov.moj.cpp.hearing.command.verdict.HearingUpdateVerdictCommand;
import uk.gov.moj.cpp.hearing.domain.HearingEventDefinition;
import uk.gov.moj.cpp.hearing.domain.updatepleas.UpdatePleaCommand;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.GenerateNowsCommand;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.NowDefinition;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.NowResultDefinitionRequirement;
import uk.gov.moj.cpp.hearing.message.shareResults.VariantStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@SuppressWarnings({"squid:S1188", "squid:S1135"})
public class TestTemplates {

    private static final String DAVID = "David";
    private static final String BOWIE = "Bowie";
    private static final String IMPRISONMENT_LABEL = "Imprisonment";

    private TestTemplates() {
    }

    public static Target targetTemplate() {
        return Target.target()
                .withHearingId(randomUUID())
                .withDefendantId(randomUUID())
                .withDraftResult(STRING.next())
                .withOffenceId(randomUUID())
                .withTargetId(randomUUID())
                .withResultLines(asList(
                        ResultLine.resultLine()
                                .withDelegatedPowers(
                                        DelegatedPowers.delegatedPowers()
                                                .withUserId(randomUUID())
                                                .withLastName(STRING.next())
                                                .withFirstName(STRING.next())
                                                .build())
                                .withIsComplete(BOOLEAN.next())
                                .withIsModified(BOOLEAN.next())
                                .withLevel(randomEnum(uk.gov.justice.core.courts.Level.class).next())
                                .withOrderedDate(PAST_LOCAL_DATE.next())
                                .withResultLineId(randomUUID())
                                .withResultLabel(STRING.next())
                                .withSharedDate(PAST_LOCAL_DATE.next())
                                .withResultDefinitionId(randomUUID())
                                .withPrompts(asList(
                                        uk.gov.justice.core.courts.Prompt.prompt()
                                                .withFixedListCode(STRING.next())
                                                .withId(randomUUID())
                                                .withLabel(STRING.next())
                                                .withValue(STRING.next())
                                                .withWelshValue(STRING.next())
                                                .build()))
                                .build()))
                .build();
    }

    public static Target targetTemplate(final UUID hearingId,
                                        final UUID defendantId,
                                        final UUID offenceId,
                                        final List<UUID> resultLineIds,
                                        final Map<UUID, UUID> resultDefinitionMap) {

        final List<ResultLine> resultLineList = new ArrayList<>();

        for (final UUID resultLineId : resultLineIds) {

            final UUID resultDefinitionId = resultDefinitionMap.getOrDefault(resultLineId, randomUUID());

            resultLineList.add(ResultLine.resultLine()
                    .withDelegatedPowers(
                            DelegatedPowers.delegatedPowers()
                                    .withUserId(randomUUID())
                                    .withLastName(STRING.next())
                                    .withFirstName(STRING.next())
                                    .build())
                    .withIsComplete(true)
                    .withIsModified(BOOLEAN.next())
                    .withLevel(randomEnum(uk.gov.justice.core.courts.Level.class).next())
                    .withOrderedDate(PAST_LOCAL_DATE.next())
                    .withResultLineId(resultLineId)
                    .withResultLabel(STRING.next())
                    .withSharedDate(PAST_LOCAL_DATE.next())
                    .withResultDefinitionId(resultDefinitionId)
                    .withPrompts(asList(
                            uk.gov.justice.core.courts.Prompt.prompt()
                                    .withFixedListCode(STRING.next())
                                    .withId(randomUUID())
                                    .withLabel(STRING.next())
                                    .withValue(STRING.next())
                                    .withWelshValue(STRING.next())
                                    .build()))
                    .build());
        }
        return Target.target()
                .withHearingId(hearingId)
                .withDefendantId(defendantId)
                .withDraftResult(STRING.next())
                .withOffenceId(offenceId)
                .withTargetId(randomUUID())
                .withResultLines(resultLineList)
                .build();
    }

    public static GenerateNowsCommand generateNowsCommandTemplate(final UUID defendantId) {
        final CreateNowsRequest nowsRequest = generateNowsRequestTemplate(defendantId);
        final GenerateNowsCommand generateNowsCommand = new GenerateNowsCommand();
        generateNowsCommand.setCreateNowsRequest(nowsRequest);
        return generateNowsCommand;
    }

    // still used by enforcement
    public static CreateNowsRequest generateNowsRequestTemplate(final UUID defendantId) {
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

        final Hearing hearing = customStructureInitiateHearingTemplate(toMap(caseId, toMap(defendantId, asList(offenceId)))).getHearing();

        final String fine = "969f150c-cd05-46b0-9dd9-30891efcc766";

        final Map<UUID, UUID> resultDefinationMap = new HashMap<>();
        resultDefinationMap.put(sharedResultLineId0, UUID.fromString(fine));

        final Map<UUID, String> promptReferenceMap = new HashMap<>();
        promptReferenceMap.put(promptId00, "FO");

        hearing.setTargets(asList(targetTemplate(hearing.getId(), defendantId, offenceId,
                asList(sharedResultLineId0, sharedResultLineId1, sharedResultLineId2),
                resultDefinationMap)
        ));

        return CreateNowsRequest.createNowsRequest()
                .withHearing(hearing)
                .withSharedResultLines(asList(
                        SharedResultLine.sharedResultLine()
                                .withId(sharedResultLineId0)
                                .withProsecutionCaseId(caseId)
                                .withDefendantId(defendantId)
                                .withOffenceId(offenceId)
                                .withLevel("CASE")
                                .withLabel(IMPRISONMENT_LABEL)
                                .withRank(BigDecimal.ONE)
                                .withPrompts(asList(
                                        ResultPrompt.resultPrompt()
                                                .withId(promptId00)
                                                .withLabel(promptLabel0)
                                                .withValue("500")
                                                .build(),
                                        ResultPrompt.resultPrompt()
                                                .withId(promptId01)
                                                .withLabel(promptLabel1)
                                                .withValue("500")
                                                .build()
                                        )
                                ).build(),
                        SharedResultLine.sharedResultLine()
                                .withId(sharedResultLineId1)
                                .withProsecutionCaseId(caseId)
                                .withDefendantId(defendantId)
                                .withOffenceId(offenceId)
                                .withLevel("DEFENDANT")
                                .withLabel(IMPRISONMENT_LABEL)
                                .withRank(BigDecimal.valueOf(2))
                                .withPrompts(asList(
                                        ResultPrompt.resultPrompt()
                                                .withId(promptId10)
                                                .withLabel(promptLabel0)
                                                .withValue("500")
                                                .build(),
                                        ResultPrompt.resultPrompt()
                                                .withId(promptId11)
                                                .withLabel(promptLabel1)
                                                .withValue("500").build()
                                        )
                                ).build(),
                        SharedResultLine.sharedResultLine()
                                .withId(sharedResultLineId2)
                                .withProsecutionCaseId(caseId)
                                .withDefendantId(defendantId)
                                .withOffenceId(offenceId)
                                .withLevel("OFFENCE")
                                .withLabel(IMPRISONMENT_LABEL)
                                .withRank(BigDecimal.valueOf(3))
                                .withPrompts(asList(
                                        ResultPrompt.resultPrompt()
                                                .withId(promptId20)
                                                .withLabel(promptLabel0)
                                                .withValue("500").build(),
                                        ResultPrompt.resultPrompt()
                                                .withId(promptId21)
                                                .withLabel(promptLabel1)
                                                .withValue("500").build()
                                        )
                                ).build()

                ))
                .withNows(asList(Now.now()
                        .withId(randomUUID())
                        .withNowsTypeId(nowsTypeId)
                        .withDefendantId(defendantId)
                        //.w(STRING.next())
                        .withDocumentationLanguage(DocumentationLanguage.ENGLISH)
                        .withFinancialOrders(FinancialOrderDetails.financialOrderDetails().withEmployerOrganisation(Organisation.organisation()
                                .withName("Testing Company Ltd")
                                .withAddress(
                                        Address.address()
                                                .withAddress1("Emp Address Line 1")
                                                .withAddress2("Emp Address Line 2")
                                                .withAddress3("Emp Address Line 3")
                                                .withAddress4("Emp Address Line 4")
                                                .withAddress5("Emp Address Line 5")
                                                .withPostcode("TF3 1YE")
                                                .build()).build()).build())
                        .withRequestedMaterials(asList(
                                NowVariant.nowVariant()
                                        .withMaterialId(materialId)
                                        .withKey(
                                                NowVariantKey.nowVariantKey()
                                                        .withDefendantId(defendantId)
                                                        .withHearingId(hearing.getId())
                                                        .withNowsTypeId(nowsTypeId)
                                                        .withUsergroups(asList("COURTCLERK", "DEFENCECOUNSEL"))
                                                        .build()
                                        )
                                        .withNowResults(asList(
                                                NowVariantResult.nowVariantResult()
                                                        .withSharedResultId(sharedResultLineId0)
                                                        .withSequence(1)
                                                        .withNowVariantResultText((NowVariantResultText
                                                                .nowVariantResultText()
                                                                .withAdditionalProperty("ABCD", "1234")
                                                                .withAdditionalProperty("1234", "ABCD")
                                                                .build()))
                                                        .withPromptRefs(asList(
                                                                promptId00, promptId01
                                                                // buildPrompt(promptId00, promptReferenceMap.getOrDefault(promptId00, STRING.next())),
                                                                // buildPrompt(promptId01, promptReferenceMap.getOrDefault(promptId01, STRING.next())))
                                                        )).build(),
                                                NowVariantResult.nowVariantResult()
                                                        .withSharedResultId(sharedResultLineId1)
                                                        .withSequence(2)
                                                        .withPromptRefs(asList(
                                                                promptId10, promptId11
                                                                 /*       PromptRefs.promptRefs()
                                                                                .withId(promptId10)
                                                                                //TODO GPE-6113 review thissetLabel(promptLabel0),
                                                                                .build(),
                                                                        PromptRefs.promptRefs()
                                                                                .withId(promptId11)
                                                                                //TODO GPE-6113 review .setLabel(promptLabel1)
                                                                                .build()*/
                                                                )
                                                        ).build(),
                                                NowVariantResult.nowVariantResult()
                                                        .withSharedResultId(sharedResultLineId2)
                                                        .withSequence(3)
                                                        .withPromptRefs(asList(
                                                                promptId20, promptId21
                                                                        /*PromptRefs.promptRefs()
                                                                                .withId(promptId20)
                                                                                //TODO GPE-6113 review this.setLabel(promptLabel0),
                                                                                .build(),
                                                                        PromptRefs.promptRefs()
                                                                                .withId(promptId21)
                                                                                //TODO GPE-6113 review .setLabel(promptLabel1)
                                                                                .build()
                                                                                */
                                                        )).build()
                                        ))
                                        .build()
                        ))
                        .build()
                ))

                .withNowTypes(asList(NowType.nowType()
                                .withId(nowsTypeId)
                                .withTemplateName("SingleTemplate")
                                .withDescription("Imprisonment Order")
                                .withRank(1)
                                .withStaticText("<h3>Imprisonment</h3><p>You have been sentenced to a term of imprisonment. If you<ul><li>Do not comply with the requirements of this order during the <u>supervision period</u>; or</li><li>Commit any other offence during the <u>operational period</u></li></ul>you may be liable to serve the <u>custodial period</u> in prison.<br/><br/><br/><p>For the duration of the <u>supervision period</u>, you will be supervised by your Probation Officer, and<br/>You must<ul><li>Keep in touch with your Probation Officer as they tell you</li><li>Tell your Probation Officer if you intend to change your address</li><li>Comply with all other requirements</li></ul><p><strong>Requirements</strong> – Please refer only to the requirements that the court has specified in the details of your order, <u>as set out above</u><p><strong>Unpaid Work Requirement</strong><p>You must carry out unpaid work for the hours specified as you are told and by the date specified in the order. Your Probation Officer will tell you who will be responsible for supervising work.<p><strong>Activity Requirement</strong><p>You must present yourself as directed at the time and on the days specified in the order and you must undertake the activity the court has specified for the duration specified in the order in the way you are told by your Probation Officer<p><strong>Programme Requirement</strong><p>You must participate in the programme specified in the order at the location specified and for the number of days specified in the order<p><strong>Prohibited Activity Requirement</strong><p>You must not take part in the activity that the court has prohibited in the order for the number of days the court specified<p><strong>Curfew Requirement</strong><p>You must remain in the place or places the court has specified during the periods specified. The curfew requirement lasts for the number of days specified in the order<p>See \"Electronic Monitoring Provision\" in this order<p><strong>Exclusion Requirement</strong><p>You must not enter the place or places the court has specified between the hours specified in the order. The exclusion requirement lasts for the number of days specified in the order<p>See \"Electronic Monitoring Provision\" in this order<p><strong>Residence Requirement</strong><p>You must live at the premises the court has specified and obey any rules that apply there for the number of days specified in the order. You may live at ???? with the prior approval of your Probation Officer.<p><strong>Foreign Travel Prohibition Requirement</strong><p>You must not travel to the prohibited location specified in the order during the period the court has specified in the order.<p><strong>Mental Health Treatment Requirement</strong><p>You must have mental health treatment by or under the direction of the practitioner the court has specified at the location specified as a resident patient for the number of days specified in the order.<p><strong>Drug Rehabilitation Requirement</strong><p>You must have treatment for drug dependency by or under the direction of the practitioner the court has specified at the location specified as a resident patient for the number of days specified in the order.<p>To be sure that you do not have any illegal drug in your body, you must provide samples for testing at such times or in such circumstances as your Probation Officer or the person responsible for your treatment will tell you. The results of tests on the samples will be sent to your Probation Officer who will report the results to the court. Your Probation Officer will also tell the court how your order is progressing and the views of your treatment provider.<p>The court will review this order ????. The first review will be on the date and time specified at the court specified.<p>You must / need not attend this review hearing.<p><strong>Alcohol Treatment Requirement</strong><p>You must have treatment for alcohol dependency by or under the direction of the practitioner the court has specified at the location specified as a resident patient for the number of days specified in the order.<p><strong>Supervision Requirement</strong><p>You must attend appointments with your Probation Officer or another person at the times and places your Probation Officer says.<p><strong>Attendance Centre Requirement</strong><p>You must attend an attendance centre - see separate sheet for details<p><strong>WARNING</strong><p>If you do not comply with your order, you will be brought back to court. The court may then<ul><li>Change the order by adding extra requirements</li><li>Pass a different sentence for the original offences; or</li><li>Send you to prison</li></ul><p><strong>NOTE</strong><p>Either you or your Probation Officer can ask the court to look again at this order and the court can then change it or cancel it if it feels that is the right thing to do. The court may also pass a different sentence for the original offence(s). If you wish to ask the court to look at your order again you should get in touch with the court at the address above.")
                                .withWelshStaticText("<h3> Prison </h3> <p> Fe'ch dedfrydwyd i dymor o garchar. Os ydych <ul> <li> Peidiwch â chydymffurfio â gofynion y gorchymyn hwn yn ystod y cyfnod goruchwylio </u>; neu </li> <li> Ymrwymo unrhyw drosedd arall yn ystod y cyfnod gweithredol </u> </li> </ul> efallai y byddwch yn atebol i wasanaethu'r cyfnod gwarchodaeth </u> yn y carchar. <br/> <br/> <br/> <p> Yn ystod y cyfnod goruchwylio </u>, byddwch chi'n cael eich goruchwylio gan eich Swyddog Prawf, a <br/> Rhaid ichi <ul> < li> Cadwch mewn cysylltiad â'ch Swyddog Prawf wrth iddyn nhw ddweud wrthych </li> <li> Dywedwch wrth eich Swyddog Prawf os ydych yn bwriadu newid eich cyfeiriad </li> <li> Cydymffurfio â'r holl ofynion eraill </li></ul > <p> <strong> Gofynion </strong> - Cyfeiriwch yn unig at y gofynion a nododd y llys yn manylion eich archeb, fel y nodir uchod </u> <p> <strong> Gwaith Di-dāl Gofyniad </strong><p> Rhaid i chi wneud gwaith di-dāl am yr oriau a bennir fel y dywedir wrthych a chi erbyn y dyddiad a bennir yn y gorchymyn. Bydd eich Swyddog Prawf yn dweud wrthych pwy fydd yn gyfrifol am oruchwylio gwaith.<p> <strong> Gweithgaredd Gofyniad </strong> <p> Rhaid i chi gyflwyno eich hun fel y'i cyfarwyddir ar yr amser ac ar y diwrnodau a bennir yn y gorchymyn a rhaid i chi ymgymryd â chi y gweithgaredd y mae'r llys wedi ei nodi ar gyfer y cyfnod a bennir yn y drefn yn y ffordd y dywedir wrth eich Swyddog Prawf <p> <strong> Gofyniad Rhaglen </strong><p> Rhaid i chi gymryd rhan yn y rhaglen a bennir yn y drefn yn y lleoliad a bennir ac am y nifer o ddyddiau a bennir yn y gorchymyn <p> <strong> Gofyniad Gweithgaredd Gwahardd </strong> <p> Rhaid i chi beidio â chymryd rhan yn y gweithgaredd a waharddodd y llys yn y drefn ar gyfer nifer y dyddiau llys penodol <p> <strong> Curfew Requirement </strong> <p> Rhaid i chi aros yn y lle neu lle mae'r llys wedi nodi yn ystod y cyfnodau a bennir. Mae'r gofyniad cyrffyw yn para am y nifer o ddyddiau a bennir yn y<p> Gweler \"Darpariaeth Monitro Electronig\" yn yr orchymyn hwn <p> <strong> Gofyniad Preswyl </strong> <p> Rhaid i chi fyw yn yr adeilad y llys wedi nodi ac ufuddhau i unrhyw reolau sy'n berthnasol yno am y nifer o ddyddiau a bennir yn y gorchymyn. Efallai y byddwch yn byw yn ???? gyda chymeradwyaeth ymlaen llaw eich Swyddog Prawf. <p> <strong> Gofyniad Gwahardd Teithio Tramor </strong> <p> Rhaid i chi beidio â theithio i'r lleoliad gwaharddedig a bennir yn yr orchymyn yn ystod y cyfnod y mae'r llys wedi'i bennu yn y gorchymyn. < p> <strong> Gofyniad Triniaeth Iechyd Meddwl </strong> <p> Rhaid i chi gael triniaeth iechyd meddwl gan neu o dan gyfarwyddyd yr ymarferydd y mae'r llys wedi ei nodi yn y lleoliad a bennir fel claf preswyl am y nifer o ddyddiau a bennir yn y <p> <strong> Angen Adsefydlu Cyffuriau </strong> <p> Rhaid i chi gael triniaeth ar gyfer dibyniaeth ar gyffuriau gan neu o dan gyfarwyddyd yr ymarferydd y mae'r llys wedi ei nodi yn y lleoliad a bennir fel claf preswyl am nifer y dyddiau <p> Er mwyn sicrhau nad oes gennych unrhyw gyffur anghyfreithlon yn eich corff, rhaid i chi ddarparu samplau i'w profi ar yr adegau hynny neu mewn amgylchiadau o'r fath y bydd eich Swyddog Prawf neu'r person sy'n gyfrifol am eich triniaeth yn dweud wrthych chi . Anfonir canlyniadau'r profion ar y samplau i'ch Swyddog Prawf a fydd yn adrodd y canlyniadau i'r llys. Bydd eich Swyddog Prawf hefyd yn dweud wrth y llys sut mae'ch gorchymyn yn mynd rhagddo a barn eich darparwr triniaeth. <P> Bydd y llys yn adolygu'r gorchymyn hwn ????. Bydd yr adolygiad cyntaf ar y dyddiad a'r amser a bennir yn y llys a bennir. <P> Rhaid i chi / nid oes angen i chi fynychu'r gwrandawiad hwn. <P> <strong> Gofyniad Trin Alcohol </strong> <p> Rhaid i chi gael triniaeth ar gyfer dibyniaeth ar alcohol gan neu o dan gyfarwyddyd yr ymarferydd y mae'r llys wedi ei nodi yn y lleoliad a bennir fel claf preswyl am y nifer o ddyddiau a bennir yn y gorchymyn. <p> <strong> Gofyniad Goruchwylio </strong> <p> Rhaid i chi fynychu penodiadau gyda'ch Swyddog Prawf neu berson arall ar yr adegau a lle mae eich Swyddog Prawf yn dweud. <p> <strong> Gofyniad y Ganolfan Bresennol </strong> <p> Rhaid i chi fynychu canolfan bresenoldeb - <p> <strong> RHYBUDD </strong> <p> Os na fyddwch chi'n cydymffurfio â'ch archeb, fe'ch cewch eich troi'n ôl i'r llys. Gall y llys wedyn <ul> <li> Newid y gorchymyn trwy ychwanegu gofynion ychwanegol </li> <li> Pasiwch frawddeg wahanol ar gyfer y troseddau gwreiddiol; neu </li> <li> Anfonwch chi at y carchar </li> </ul> <p> <strong> NOTE </strong> <p> Naill ai chi neu'ch Swyddog Prawf all ofyn i'r llys edrych eto ar y gorchymyn hwn ac yna gall y llys ei newid neu ei ganslo os yw'n teimlo mai dyna'r peth iawn i'w wneud. Gall y llys hefyd basio brawddeg wahanol ar gyfer y trosedd (wyr) gwreiddiol. Os hoffech ofyn i'r llys edrych ar eich archeb eto dylech gysylltu â'r llys yn y cyfeiriad uchod. ")
                                .withPriority("0.5 hours")
                                .withJurisdiction("B")
                                .withRequiresBulkPrinting(false)
                                .withRequiresEnforcement(false)
                                .build()
                        )
                )
                .build();
    }

    public static CaseDefendantDetailsWithHearingCommand initiateDefendantCommandTemplate(final UUID hearingId) {

        return CaseDefendantDetailsWithHearingCommand.caseDefendantDetailsWithHearingCommand()
                .setHearingId(hearingId)
                .setDefendant(defendantTemplate());
    }

    public static uk.gov.moj.cpp.hearing.command.defendant.Defendant defendantTemplate() {

        Defendant defendant = new Defendant();

        defendant.setId(randomUUID());

        defendant.setProsecutionCaseId(randomUUID());

        defendant.setNumberOfPreviousConvictionsCited(INTEGER.next());

        defendant.setProsecutionAuthorityReference(STRING.next());

        defendant.setWitnessStatement(STRING.next());

        defendant.setWitnessStatementWelsh(STRING.next());

        defendant.setMitigation(STRING.next());

        defendant.setMitigationWelsh(STRING.next());

        defendant.setAssociatedPersons(asList(associatedPerson(defaultArguments()).build()));

        defendant.setDefenceOrganisation(organisation(defaultArguments()).build());

        defendant.setPersonDefendant(personDefendant(defaultArguments()).build());

        defendant.setLegalEntityDefendant(legalEntityDefendant(defaultArguments()).build());

        return defendant;
    }

    public static Verdict verdictTemplate(final UUID offenceId, final VerdictCategoryType verdictCategoryType) {

        final boolean unanimous = BOOLEAN.next();

        final int numberOfSplitJurors = unanimous ? 0 : integer(1, 3).next();

        return Verdict.verdict()
                .withVerdictType(VerdictType.verdictType()
                        .withVerdictTypeId(randomUUID())
                        .withCategory(STRING.next())
                        .withCategoryType(verdictCategoryType.name())
                        .withDescription(STRING.next())
                        .withSequence(INTEGER.next())
                        .build()
                )
                .withOffenceId(offenceId)
                .withVerdictDate(PAST_LOCAL_DATE.next())
                .withLesserOrAlternativeOffence(LesserOrAlternativeOffence.lesserOrAlternativeOffence()
                        .withOffenceDefinitionId(randomUUID())
                        .withOffenceCode(STRING.next())
                        .withOffenceTitle(STRING.next())
                        .withOffenceTitleWelsh(STRING.next())
                        .withOffenceLegislation(STRING.next())
                        .withOffenceLegislationWelsh(STRING.next())
                        .build()
                )
                .withJurors(Jurors.jurors()
                        .withNumberOfJurors(integer(9, 12).next())
                        .withNumberOfSplitJurors(numberOfSplitJurors)
                        .withUnanimous(unanimous)
                        .build()
                )
                .build();
    }

    public enum PleaValueType {GUILTY, NOT_GUILTY}

    public enum VerdictCategoryType {GUILTY, NOT_GUILTY, NO_VERDICT}

    public static class InitiateHearingCommandTemplates {
        private InitiateHearingCommandTemplates() {
        }

        public static InitiateHearingCommand minimumInitiateHearingTemplate() {
            return InitiateHearingCommand.initiateHearingCommand()
                    .setHearing(CoreTestTemplates.hearing(defaultArguments()
                            .setDefendantType(PERSON)
                            .setHearingLanguage(ENGLISH)
                            .setJurisdictionType(CROWN)
                            .setMinimumAssociatedPerson(true)
                            .setMinimumDefenceOrganisation(true)
                    ).build());
        }

        public static InitiateHearingCommand standardInitiateHearingTemplate() {
            return InitiateHearingCommand.initiateHearingCommand()
                    .setHearing(CoreTestTemplates.hearing(defaultArguments()
                            .setDefendantType(PERSON)
                            .setHearingLanguage(ENGLISH)
                            .setJurisdictionType(CROWN)
                    ).build());
        }

        public static InitiateHearingCommand initiateHearingTemplateForMagistrates() {
            return InitiateHearingCommand.initiateHearingCommand()
                    .setHearing(CoreTestTemplates.hearing(defaultArguments()
                            .setDefendantType(PERSON)
                            .setHearingLanguage(ENGLISH)
                            .setJurisdictionType(MAGISTRATES)
                    ).build());
        }

        public static InitiateHearingCommand initiateHearingTemplateForDefendantTypeOrganisation() {
            return InitiateHearingCommand.initiateHearingCommand()
                    .setHearing(CoreTestTemplates.hearing(defaultArguments()
                            .setDefendantType(ORGANISATION)
                            .setHearingLanguage(ENGLISH)
                            .setJurisdictionType(CROWN)
                            .setMinimumAssociatedPerson(true)
                            .setMinimumDefenceOrganisation(true)
                    ).build());
        }

        public static InitiateHearingCommand customStructureInitiateHearingTemplate(Map<UUID, Map<UUID, List<UUID>>> structure) {
            return InitiateHearingCommand.initiateHearingCommand()
                    .setHearing(CoreTestTemplates.hearing(defaultArguments()
                            .setDefendantType(PERSON)
                            .setHearingLanguage(ENGLISH)
                            .setJurisdictionType(CROWN)
                            .setStructure(structure)
                    ).build());
        }
    }

    public static class UpdatePleaCommandTemplates {
        private UpdatePleaCommandTemplates() {
        }

        public static UpdatePleaCommand updatePleaTemplate(final UUID originatingHearingId, final UUID offenceId, final PleaValue pleaValue) {
            return UpdatePleaCommand.updatePleaCommand().setPleas(
                    asList(
                            Plea.plea()
                                    .withOriginatingHearingId(originatingHearingId)
                                    .withOffenceId(offenceId)
                                    .withPleaValue(pleaValue)
                                    .withPleaDate(PAST_LOCAL_DATE.next())
                                    .withDelegatedPowers(DelegatedPowers.delegatedPowers()
                                            .withFirstName(DAVID)
                                            .withLastName(BOWIE)
                                            .withUserId(UUID.randomUUID())
                                            .build())
                                    .build()));
        }
    }

    public static class UpdateVerdictCommandTemplates {
        private UpdateVerdictCommandTemplates() {
        }

        public static HearingUpdateVerdictCommand updateVerdictTemplate(final UUID hearingId, final UUID offenceId, final VerdictCategoryType verdictCategoryType) {

            final boolean unanimous = BOOLEAN.next();
            final int numberOfSplitJurors = unanimous ? 0 : integer(1, 3).next();

            return HearingUpdateVerdictCommand.hearingUpdateVerdictCommand()
                    .withHearingId(hearingId)
                    .withVerdicts(singletonList(Verdict.verdict()
                            .withVerdictType(VerdictType.verdictType()
                                    .withVerdictTypeId(randomUUID())
                                    .withCategory(STRING.next())
                                    .withCategoryType(verdictCategoryType.name())
                                    .withDescription(STRING.next())
                                    .withSequence(INTEGER.next())
                                    .build()
                            )
                            .withOffenceId(offenceId)
                            .withVerdictDate(PAST_LOCAL_DATE.next())
                            .withLesserOrAlternativeOffence(LesserOrAlternativeOffence.lesserOrAlternativeOffence()
                                    .withOffenceDefinitionId(randomUUID())
                                    .withOffenceCode(STRING.next())
                                    .withOffenceTitle(STRING.next())
                                    .withOffenceTitleWelsh(STRING.next())
                                    .withOffenceLegislation(STRING.next())
                                    .withOffenceLegislationWelsh(STRING.next())
                                    .build()
                            )
                            .withJurors(Jurors.jurors()
                                    .withNumberOfJurors(integer(9, 12).next())
                                    .withNumberOfSplitJurors(numberOfSplitJurors)
                                    .withUnanimous(unanimous)
                                    .build()
                            )
                            .withOriginatingHearingId(hearingId)
                            .build()
                    ));
        }
    }

    public static class SaveDraftResultsCommandTemplates {
        private SaveDraftResultsCommandTemplates() {
        }

        public static SaveDraftResultCommand standardSaveDraftTemplate(UUID hearingId, UUID defendantId, UUID offenceId, UUID resultLineId) {
            return SaveDraftResultCommand.saveDraftResultCommand()
                    .setTarget(CoreTestTemplates.target(hearingId, defendantId, offenceId, resultLineId).build());
        }

        public static SaveDraftResultCommand saveDraftResultCommandTemplate(final InitiateHearingCommand initiateHearingCommand, final LocalDate orderedDate) {
            return saveDraftResultCommandTemplate(initiateHearingCommand, orderedDate, UUID.randomUUID(), UUID.randomUUID());
        }

        public static ResultLine.Builder standardResultLineTemplate(final UUID resultLineId, final UUID resultDefinitionId, final LocalDate orderedDate) {
            return ResultLine.resultLine()
                    .withResultLineId(resultLineId)
                    .withDelegatedPowers(
                            DelegatedPowers.delegatedPowers()
                                    .withUserId(UUID.randomUUID())
                                    .withLastName(BOWIE)
                                    .withFirstName(DAVID)
                                    .build()
                    )
                    .withIsComplete(true)
                    .withIsModified(true)
                    .withLevel(uk.gov.justice.core.courts.Level.OFFENCE)
                    .withOrderedDate(orderedDate)
                    .withResultLineId(UUID.randomUUID())
                    .withResultLabel("imprisonment")
                    .withSharedDate(LocalDate.now())
                    .withResultDefinitionId(resultDefinitionId)
                    .withPrompts(
                            asList(
                                    uk.gov.justice.core.courts.Prompt.prompt()
                                            .withFixedListCode("fixedlistcode0")
                                            .withId(UUID.randomUUID())
                                            .withLabel("imprisonment term")
                                            .withValue("6 years")
                                            .withWelshValue("6 blynedd")
                                            .build()
                            )
                    );

        }

        public static SaveDraftResultCommand saveDraftResultCommandTemplate(
                final InitiateHearingCommand initiateHearingCommand, final LocalDate orderedDate, final UUID resultLineId, final UUID resultDefinitionId) {
            final Hearing hearing = initiateHearingCommand.getHearing();
            final uk.gov.justice.core.courts.Defendant defendant0 = hearing.getProsecutionCases().get(0).getDefendants().get(0);
            final Offence offence0 = defendant0.getOffences().get(0);
            final Target target = Target.target()
                    .withHearingId(hearing.getId())
                    .withDefendantId(defendant0.getId())
                    .withDraftResult("draft results content")
                    .withOffenceId(offence0.getId())
                    .withTargetId(UUID.randomUUID())
                    .withResultLines(Collections.singletonList(standardResultLineTemplate(resultLineId, resultDefinitionId, orderedDate).build()))
                    .build();
            return new SaveDraftResultCommand(target, null);
        }
    }

    public static class ShareResultsCommandTemplates {
        private ShareResultsCommandTemplates() {
        }

        public static ShareResultsCommand basicShareResultsCommandTemplate() {

            return ShareResultsCommand.shareResultsCommand()
                    .setCourtClerk(uk.gov.justice.core.courts.CourtClerk.courtClerk()
                            .withId(randomUUID())
                            .withFirstName(STRING.next())
                            .withLastName(STRING.next())
                            .build());

        }

        public static ShareResultsCommand standardShareResultsCommandTemplate(final UUID hearingId) {
            return basicShareResultsCommandTemplate().setHearingId(hearingId);
        }
    }

    public static class CompletedResultLineStatusTemplates {

        private CompletedResultLineStatusTemplates() {
        }

        public static CompletedResultLineStatus completedResultLineStatus(final UUID resultLineId) {
            final ZonedDateTime startDateTime = FUTURE_ZONED_DATE_TIME.next().withZoneSameLocal(ZoneId.of("UTC"));
            return CompletedResultLineStatus.builder()
                    .withId(resultLineId)
                    .withLastSharedDateTime(startDateTime)
                    .withCourtClerk(uk.gov.justice.core.courts.CourtClerk.courtClerk()
                            .withId(randomUUID())
                            .withFirstName(STRING.next())
                            .withLastName(STRING.next())
                            .build())
                    .build();
        }

    }

    public static class CaseDefendantOffencesChangedCommandTemplates {

        private CaseDefendantOffencesChangedCommandTemplates() {
        }

        public static TemplateArguments updateOffencesForDefendantArguments(UUID prosecutionCaseId, UUID defendantId) {
            return new TemplateArguments(prosecutionCaseId, defendantId);
        }

        public static UpdateOffencesForDefendantCommand addOffencesForDefendantTemplate(TemplateArguments args) {
            return UpdateOffencesForDefendantCommand.updateOffencesForDefendantCommand()
                    .setAddedOffences(asList(defendantCaseOffences(args.getProsecutionCaseId(), args.getDefendantId(), args.getOffencesToAdd())))
                    .setModifiedDate(PAST_LOCAL_DATE.next());
        }

        public static UpdateOffencesForDefendantCommand updateOffencesForDefendantTemplate(TemplateArguments args) {
            return UpdateOffencesForDefendantCommand.updateOffencesForDefendantCommand()
                    .setUpdatedOffences(asList(defendantCaseOffences(args.getProsecutionCaseId(), args.getDefendantId(), args.getOffencesToUpdate())))
                    .setModifiedDate(PAST_LOCAL_DATE.next());
        }

        public static UpdateOffencesForDefendantCommand deleteOffencesForDefendantTemplate(TemplateArguments args) {
            return UpdateOffencesForDefendantCommand.updateOffencesForDefendantCommand()
                    .setDeletedOffences(asList(deletedOffence(args.getProsecutionCaseId(), args.getDefendantId(), args.getOffenceToDelete())))
                    .setModifiedDate(PAST_LOCAL_DATE.next());
        }

        public static DefendantCaseOffences defendantCaseOffences(UUID prosecutionCaseId, UUID defendantId, List<UUID> offenceIds) {
            return DefendantCaseOffences.defendantCaseOffences()
                    .withProsecutionCaseId(prosecutionCaseId)
                    .withDefendantId(defendantId)
                    .withOffences(offenceIds.stream()
                            .map(offenceId -> Offence.offence()
                                    .withArrestDate(PAST_LOCAL_DATE.next())
                                    .withChargeDate(PAST_LOCAL_DATE.next())
                                    //.withConvictionDate(PAST_LOCAL_DATE.next())
                                    .withCount(INTEGER.next())
                                    .withEndDate(PAST_LOCAL_DATE.next())
                                    .withId(offenceId)
                                    .withIndicatedPlea(uk.gov.justice.core.courts.IndicatedPlea.indicatedPlea()
                                            .withAllocationDecision(uk.gov.justice.core.courts.AllocationDecision.allocationDecision()
                                                    .withCourtDecision(RandomGenerator.values(CourtDecision.values()).next())
                                                    .withDefendantRepresentation(RandomGenerator.values(DefendantRepresentation.values()).next())
                                                    .withIndicationOfSentence(STRING.next())
                                                    .withProsecutionRepresentation(RandomGenerator.values(ProsecutionRepresentation.values()).next())
                                                    .build())
                                            .withIndicatedPleaDate(PAST_LOCAL_DATE.next())
                                            .withIndicatedPleaValue(RandomGenerator.values(IndicatedPleaValue.values()).next())
                                            .withOffenceId(offenceId)
                                            .withSource(RandomGenerator.values(Source.values()).next())
                                            .build())
                                    .withModeOfTrial(STRING.next())
                                    .withOffenceCode(STRING.next())
                                    .withOffenceDefinitionId(randomUUID())
                                    .withOffenceFacts(uk.gov.justice.core.courts.OffenceFacts.offenceFacts()
                                            .withAlcoholReadingAmount(STRING.next())
                                            .withAlcoholReadingMethod(STRING.next())
                                            .withVehicleRegistration(STRING.next())
                                            .build())
                                    .withOffenceLegislation(STRING.next())
                                    .withOffenceLegislationWelsh(STRING.next())
                                    .withOffenceTitle(STRING.next())
                                    .withOffenceTitleWelsh(STRING.next())
                                    .withOrderIndex(INTEGER.next())
                                    .withStartDate(PAST_LOCAL_DATE.next())
                                    .withWording(STRING.next())
                                    .withWordingWelsh(STRING.next())
                                    .build())
                            .collect(Collectors.toList())
                    );
        }

        public static DeletedOffences deletedOffence(UUID caseId, UUID defendantId, List<UUID> offenceIds) {
            return DeletedOffences.deletedOffences()
                    .setProsecutionCaseId(caseId)
                    .setDefendantId(defendantId)
                    .setOffences(offenceIds);
        }

        public static class TemplateArguments {

            private UUID prosecutionCaseId;
            private UUID defendantId;
            private List<UUID> offencesToAdd = new ArrayList<>();
            private List<UUID> offencesToUpdate = new ArrayList<>();
            private List<UUID> offenceToDelete = new ArrayList<>();

            public TemplateArguments(UUID prosecutionCaseId, UUID defendantId) {
                this.prosecutionCaseId = prosecutionCaseId;
                this.defendantId = defendantId;
            }

            public UUID getProsecutionCaseId() {
                return prosecutionCaseId;
            }

            public TemplateArguments setProsecutionCaseId(UUID caseId) {
                this.prosecutionCaseId = caseId;
                return this;
            }

            public UUID getDefendantId() {
                return defendantId;
            }

            public TemplateArguments setDefendantId(UUID defendantId) {
                this.defendantId = defendantId;
                return this;
            }

            public List<UUID> getOffencesToAdd() {
                return offencesToAdd;
            }

            public TemplateArguments setOffencesToAdd(List<UUID> offencesToAdd) {
                this.offencesToAdd = offencesToAdd;
                return this;
            }

            public List<UUID> getOffencesToUpdate() {
                return offencesToUpdate;
            }

            public TemplateArguments setOffencesToUpdate(List<UUID> offencesToUpdate) {
                this.offencesToUpdate = offencesToUpdate;
                return this;
            }

            public List<UUID> getOffenceToDelete() {
                return offenceToDelete;
            }

            public TemplateArguments setOffenceToDelete(List<UUID> offenceToDelete) {
                this.offenceToDelete = offenceToDelete;
                return this;
            }


        }
    }

    public static class CaseDefendantDetailsChangedCommandTemplates {

        private CaseDefendantDetailsChangedCommandTemplates() {

        }

        public static CaseDefendantDetails caseDefendantDetailsChangedCommandTemplate() {
            return CaseDefendantDetails.caseDefendantDetails()
                    .setDefendants(asList(defendantTemplate()));
        }
    }

    public static class AddDefenceCounselCommandTemplates {
        private AddDefenceCounselCommandTemplates() {
        }

        public static AddDefenceCounsel addDefenceCounselCommandTemplate(final UUID hearingId) {
            final DefenceCounsel defenceCounsel = new DefenceCounsel(
                    Arrays.asList(LocalDate.now()),
                    Arrays.asList(UUID.randomUUID()),
                    STRING.next(),
                    randomUUID(),
                    STRING.next(),
                    STRING.next(),
                    STRING.next(),
                    STRING.next()
            );
            return new AddDefenceCounsel(defenceCounsel, hearingId);
        }

        public static AddDefenceCounsel addDefenceCounselCommandTemplateWithoutMiddleName(final UUID hearingId) {
            final DefenceCounsel defenceCounsel = new DefenceCounsel(
                    Arrays.asList(LocalDate.now()),
                    Arrays.asList(UUID.randomUUID()),
                    STRING.next(),
                    randomUUID(),
                    STRING.next(),
                    null,
                    STRING.next(),
                    STRING.next()
            );
            return new AddDefenceCounsel(defenceCounsel, hearingId);
        }

        public static AddDefenceCounsel addDefenceCounselCommandTemplate(final UUID hearingId, DefenceCounsel defenceCounsel) {
            return new AddDefenceCounsel(defenceCounsel, hearingId);
        }
    }

    public static class UpdateDefenceCounselCommandTemplates {
        private UpdateDefenceCounselCommandTemplates() {
        }

        public static UpdateDefenceCounsel updateDefenceCounselCommandTemplate(final UUID hearingId) {
            final DefenceCounsel defenceCounsel = new DefenceCounsel(
                    Arrays.asList(LocalDate.now()),
                    Arrays.asList(UUID.randomUUID()),
                    STRING.next(),
                    randomUUID(),
                    STRING.next(),
                    STRING.next(),
                    STRING.next(),
                    STRING.next()
            );
            return new UpdateDefenceCounsel(defenceCounsel, hearingId);
        }

        public static UpdateDefenceCounsel updateDefenceCounselCommandTemplate(final UUID hearingId, DefenceCounsel defenceCounsel) {
            return new UpdateDefenceCounsel(defenceCounsel, hearingId);
        }
    }

    public static class AddProsecutionCounselCommandTemplates {
        private AddProsecutionCounselCommandTemplates() {
        }

        public static AddProsecutionCounsel addProsecutionCounselCommandTemplate(final UUID hearingId) {
            ProsecutionCounsel prosecutionCounsel = new ProsecutionCounsel(
                    Arrays.asList(LocalDate.now()),
                    STRING.next(),
                    randomUUID(),
                    STRING.next(),
                    STRING.next(),
                    Arrays.asList(UUID.randomUUID()),
                    STRING.next(),
                    STRING.next()
            );
            return new AddProsecutionCounsel(hearingId, prosecutionCounsel);
        }

        public static AddProsecutionCounsel addProsecutionCounselCommandTemplateWithoutMiddleName(final UUID hearingId) {
            ProsecutionCounsel prosecutionCounsel = new ProsecutionCounsel(
                    Arrays.asList(LocalDate.now()),
                    STRING.next(),
                    randomUUID(),
                    STRING.next(),
                    null,
                    Arrays.asList(UUID.randomUUID()),
                    STRING.next(),
                    STRING.next()
            );
            return new AddProsecutionCounsel(hearingId, prosecutionCounsel);
        }

        public static AddProsecutionCounsel addProsecutionCounselCommandTemplate(final UUID hearingId, ProsecutionCounsel prosecutionCounsel) {
            return new AddProsecutionCounsel(hearingId, prosecutionCounsel);
        }
    }

    public static class UpdateProsecutionCounselCommandTemplates {
        private UpdateProsecutionCounselCommandTemplates() {
        }

        public static UpdateProsecutionCounsel updateProsecutionCounselCommandTemplate(final UUID hearingId) {
            ProsecutionCounsel prosecutionCounsel = new ProsecutionCounsel(
                    Arrays.asList(LocalDate.now()),
                    STRING.next(),
                    randomUUID(),
                    STRING.next(),
                    null,
                    Arrays.asList(UUID.randomUUID()),
                    STRING.next(),
                    STRING.next()
            );
            return new UpdateProsecutionCounsel(hearingId, prosecutionCounsel);
        }

        public static UpdateProsecutionCounsel updateProsecutionCounselCommandTemplate(final UUID hearingId, ProsecutionCounsel prosecutionCounsel) {
            return new UpdateProsecutionCounsel(hearingId, prosecutionCounsel);
        }
    }

    public static class NowDefinitionTemplates {
        private NowDefinitionTemplates() {
        }

        public static NowDefinition standardNowDefinition() {
            return NowDefinition.now()
                    .setId(UUID.randomUUID())
                    .setJurisdiction(STRING.next())
                    .setName(STRING.next())
                    .setRank(INTEGER.next())
                    .setJurisdiction(STRING.next())
                    .setTemplateName(STRING.next())
                    .setText(STRING.next())
                    .setWelshText(STRING.next())
                    .setWelshName(STRING.next())
                    .setBilingualTemplateName(STRING.next())
                    .setRemotePrintingRequired(BOOLEAN.next())
                    .setUrgentTimeLimitInMinutes(INTEGER.next())
                    .setResultDefinitions(asList(NowResultDefinitionRequirement.resultDefinitions()
                            .setId(randomUUID())
                            .setMandatory(true)
                            .setPrimary(true)
                            .setText(STRING.next())
                            .setWelshText(STRING.next())
                            .setSequence(1)
                    ));
        }
    }

    public static NowDefinition multiPrimaryNowDefinition() {
        return NowDefinition.now()
                .setId(UUID.randomUUID())
                .setJurisdiction(STRING.next())
                .setName(STRING.next())
                .setRank(INTEGER.next())
                .setJurisdiction(STRING.next())
                .setTemplateName(STRING.next())
                .setText(STRING.next())
                .setWelshText(STRING.next())
                .setWelshName(STRING.next())
                .setBilingualTemplateName(STRING.next())
                .setRemotePrintingRequired(BOOLEAN.next())
                .setUrgentTimeLimitInMinutes(INTEGER.next())
                .setResultDefinitions(asList(NowResultDefinitionRequirement.resultDefinitions()
                                .setId(randomUUID())
                                .setMandatory(false)
                                .setPrimary(true)
                                .setText(STRING.next())
                                .setWelshText(STRING.next())
                                .setSequence(1),
                        NowResultDefinitionRequirement.resultDefinitions()
                                .setId(randomUUID())
                                .setMandatory(false)
                                .setPrimary(true)
                                .setText(STRING.next())
                                .setWelshText(STRING.next())
                                .setSequence(2),
                        NowResultDefinitionRequirement.resultDefinitions()
                                .setId(randomUUID())
                                .setMandatory(false)
                                .setPrimary(true)
                                .setText(STRING.next())
                                .setWelshText(STRING.next())
                                .setSequence(3)
                ));
    }


    public static class VariantDirectoryTemplates {
        private VariantDirectoryTemplates() {
        }

        public static Variant standardVariantTemplate(final UUID nowTypeId, final UUID hearingId, final UUID defendantId) {
            return Variant.variant()
                    .setKey(VariantKey.variantKey()
                            .setNowsTypeId(nowTypeId)
                            .setUsergroups(asList(STRING.next(), STRING.next()))
                            .setDefendantId(defendantId)
                            .setHearingId(hearingId)
                    )
                    .setValue(VariantValue.variantValue()
                            .setMaterialId(randomUUID())
                            .setStatus(VariantStatus.BUILDING)
                            .setResultLines(asList(ResultLineReference.resultLineReference()
                                    .setResultLineId(randomUUID())
                                    .setLastSharedTime(PAST_ZONED_DATE_TIME.next().withZoneSameInstant(ZoneId.of("UTC")))
                            ))
                    ).setReferenceDate(LocalDate.now());
        }
    }

    public static class UploadSubscriptionsCommandTemplates {

        private UploadSubscriptionsCommandTemplates() {
        }

        public static UploadSubscriptionsCommand buildUploadSubscriptionsCommand() {

            final UploadSubscriptionsCommand uploadSubscriptionsCommand = new UploadSubscriptionsCommand();

            uploadSubscriptionsCommand.setSubscriptions(
                    asList(
                            buildUploadSubscriptionCommand(),
                            buildUploadSubscriptionCommand()));

            return uploadSubscriptionsCommand;
        }

        private static UploadSubscription buildUploadSubscriptionCommand() {

            final Map<String, String> properties = new HashMap<>();
            properties.putIfAbsent(STRING.next(), STRING.next());
            properties.putIfAbsent(STRING.next(), STRING.next());
            properties.putIfAbsent(STRING.next(), STRING.next());
            properties.putIfAbsent("templateId", UUID.randomUUID().toString());
            properties.putIfAbsent("fromAddress", "noreply@test.com");

            final List<UUID> courtCentreIds = asList(randomUUID(), randomUUID());

            final List<UUID> nowTypeIds = asList(randomUUID(), randomUUID());

            final UploadSubscription command = new UploadSubscription();
            command.setChannel("email");
            command.setChannelProperties(properties);
            command.setDestination(STRING.next());
            command.setUserGroups(asList(STRING.next(), STRING.next()));
            command.setCourtCentreIds(courtCentreIds);
            command.setNowTypeIds(nowTypeIds);

            return command;
        }
    }

    public static class HearingEventDefinitionsTemplates {

        private HearingEventDefinitionsTemplates() {
        }

        public static CreateHearingEventDefinitionsCommand buildCreateHearingEventDefinitionsCommand() {
            return CreateHearingEventDefinitionsCommand.builder()
                    .withId(randomUUID())
                    .withEventDefinitions(asList(
                            HearingEventDefinition.builder()
                                    .withId(randomUUID())
                                    .withActionLabel(STRING.next())
                                    .withRecordedLabel(STRING.next())
                                    .withSequence(INTEGER.next())
                                    .withSequenceType(STRING.next())
                                    .withAlterable(BOOLEAN.next())
                                    .build(),
                            HearingEventDefinition.builder()
                                    .withId(randomUUID())
                                    .withGroupLabel(STRING.next())
                                    .withActionLabel(STRING.next())
                                    .withRecordedLabel(STRING.next())
                                    .withSequence(INTEGER.next())
                                    .withSequenceType(STRING.next())
                                    .withCaseAttribute(STRING.next())
                                    .withAlterable(BOOLEAN.next())
                                    .build(),
                            HearingEventDefinition.builder().
                                    withId(randomUUID())
                                    .withActionLabel(STRING.next())
                                    .withRecordedLabel(STRING.next())
                                    .withSequence(INTEGER.next())
                                    .withSequenceType(STRING.next())
                                    .withAlterable(BOOLEAN.next())
                                    .build()))
                    .build();
        }
    }

    public static class UpdateDefendantAttendanceCommandTemplates {
        private UpdateDefendantAttendanceCommandTemplates() {
        }

        public static UpdateDefendantAttendanceCommand updateDefendantAttendanceTemplate(final UUID hearingId, final UUID defendantId, final LocalDate attendanceDate, final Boolean isInAttendance) {
            return UpdateDefendantAttendanceCommand.updateDefendantAttendanceCommand()
                    .setHearingId(hearingId)
                    .setDefendantId(defendantId)
                    .setAttendanceDay(AttendanceDay.attendanceDay()
                            .withDay(attendanceDate)
                            .withIsInAttendance(isInAttendance)
                            .build());
        }
    }
}
