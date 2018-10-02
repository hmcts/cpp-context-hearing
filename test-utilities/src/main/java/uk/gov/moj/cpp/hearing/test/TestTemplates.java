package uk.gov.moj.cpp.hearing.test;

import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static uk.gov.justice.json.schemas.core.HearingLanguage.ENGLISH;
import static uk.gov.justice.json.schemas.core.JurisdictionType.CROWN;
import static uk.gov.justice.json.schemas.core.JurisdictionType.MAGISTRATES;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.BOOLEAN;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.FUTURE_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.INTEGER;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.integer;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.randomEnum;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.DefendantType.ORGANISATION;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.DefendantType.PERSON;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.associatedPerson;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.defaultArguments;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.legalEntityDefendant;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.organisation;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.personDefendant;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.asList;

import uk.gov.justice.json.schemas.core.AttendanceDay;
import uk.gov.justice.json.schemas.core.CourtDecision;
import uk.gov.justice.json.schemas.core.DefendantRepresentation;
import uk.gov.justice.json.schemas.core.DelegatedPowers;
import uk.gov.justice.json.schemas.core.Hearing;
import uk.gov.justice.json.schemas.core.IndicatedPleaValue;
import uk.gov.justice.json.schemas.core.Jurors;
import uk.gov.justice.json.schemas.core.LesserOrAlternativeOffence;
import uk.gov.justice.json.schemas.core.Offence;
import uk.gov.justice.json.schemas.core.Plea;
import uk.gov.justice.json.schemas.core.PleaValue;
import uk.gov.justice.json.schemas.core.ProsecutionRepresentation;
import uk.gov.justice.json.schemas.core.ResultLine;
import uk.gov.justice.json.schemas.core.Source;
import uk.gov.justice.json.schemas.core.Target;
import uk.gov.justice.json.schemas.core.Verdict;
import uk.gov.justice.json.schemas.core.VerdictType;
import uk.gov.justice.progression.events.CaseDefendantDetails;
import uk.gov.justice.services.test.utils.core.random.RandomGenerator;
import uk.gov.moj.cpp.hearing.command.DefendantId;
import uk.gov.moj.cpp.hearing.command.defenceCounsel.AddDefenceCounselCommand;
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
import uk.gov.moj.cpp.hearing.command.prosecutionCounsel.AddProsecutionCounselCommand;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLineStatus;
import uk.gov.moj.cpp.hearing.command.result.SaveDraftResultCommand;
import uk.gov.moj.cpp.hearing.command.result.ShareResultsCommand;
import uk.gov.moj.cpp.hearing.command.subscription.UploadSubscription;
import uk.gov.moj.cpp.hearing.command.subscription.UploadSubscriptionsCommand;
import uk.gov.moj.cpp.hearing.command.verdict.HearingUpdateVerdictCommand;
import uk.gov.moj.cpp.hearing.domain.HearingEventDefinition;
import uk.gov.moj.cpp.hearing.domain.updatepleas.UpdatePleaCommand;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.GenerateNowsCommand;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Material;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.NowResult;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.NowTypes;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Nows;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.PromptRef;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Prompts;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.SharedResultLines;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.UserGroups;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.NowDefinition;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.ResultDefinitions;
import uk.gov.moj.cpp.hearing.message.shareResults.VariantStatus;
import uk.gov.moj.cpp.hearing.nows.events.MaterialUserGroup;
import uk.gov.moj.cpp.hearing.nows.events.Now;
import uk.gov.moj.cpp.hearing.nows.events.NowType;
import uk.gov.moj.cpp.hearing.nows.events.NowsRequested;
import uk.gov.moj.cpp.hearing.nows.events.Prompt;
import uk.gov.moj.cpp.hearing.nows.events.SharedResultLine;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@SuppressWarnings("squid:S1188")
public class TestTemplates {

    public static final String DAVID = "David";
    public static final String BOWIE = "Bowie";

    public enum PleaValueType {GUILTY, NOT_GUILTY}

    public enum VerdictCategoryType {GUILTY, NOT_GUILTY, NO_VERDICT}

    private static final String IMPRISONMENT_LABEL = "Imprisonment";
    private static final String IMPRISONMENT_DURATION_VALUE = "Imprisonment duration";
    private static final String WORMWOOD_SCRUBS_VALUE = "Wormwood Scrubs";

    private TestTemplates() {
    }

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
                    .withLevel(uk.gov.justice.json.schemas.core.Level.OFFENCE)
                    .withOrderedDate(orderedDate)
                    .withResultLineId(UUID.randomUUID())
                    .withResultLabel("imprisonment")
                    .withSharedDate(LocalDate.now())
                    .withResultDefinitionId( resultDefinitionId)
                    .withPrompts(
                            asList(
                                    uk.gov.justice.json.schemas.core.Prompt.prompt()
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
            final uk.gov.justice.json.schemas.core.Defendant defendant0 = hearing.getProsecutionCases().get(0).getDefendants().get(0);
            final Offence offence0 = defendant0.getOffences().get(0);
            final Target target = Target.target()
                    .withHearingId(hearing.getId())
                    .withDefendantId(defendant0.getId())
                    .withDraftResult("draft results content")
                    .withOffenceId(offence0.getId())
                    .withTargetId(UUID.randomUUID())
                    .withResultLines(Collections.singletonList( standardResultLineTemplate(resultLineId, resultDefinitionId, orderedDate ).build()   ))
                    .build();
            return new SaveDraftResultCommand(target, null);
        }
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
                                .withLevel(randomEnum(uk.gov.justice.json.schemas.core.Level.class).next())
                                .withOrderedDate(PAST_LOCAL_DATE.next())
                                .withResultLineId(randomUUID())
                                .withResultLabel(STRING.next())
                                .withSharedDate(PAST_LOCAL_DATE.next())
                                .withResultDefinitionId(randomUUID())
                                .withPrompts(asList(
                                        uk.gov.justice.json.schemas.core.Prompt.prompt()
                                                .withFixedListCode(STRING.next())
                                                .withId(randomUUID())
                                                .withLabel(STRING.next())
                                                .withValue(STRING.next())
                                                .withWelshValue(STRING.next())
                                                .build()))
                                .build()))
                .build();
    }

    public static class ShareResultsCommandTemplates {
        private ShareResultsCommandTemplates() {
        }

        public static ShareResultsCommand basicShareResultsCommandTemplate() {

            return ShareResultsCommand.shareResultsCommand()
                    .setCourtClerk(uk.gov.justice.json.schemas.core.CourtClerk.courtClerk()
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
                    .withCourtClerk(uk.gov.justice.json.schemas.core.CourtClerk.courtClerk()
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

            public TemplateArguments setProsecutionCaseId(UUID caseId) {
                this.prosecutionCaseId = caseId;
                return this;
            }

            public TemplateArguments setDefendantId(UUID defendantId) {
                this.defendantId = defendantId;
                return this;
            }

            public TemplateArguments setOffencesToAdd(List<UUID> offencesToAdd) {
                this.offencesToAdd = offencesToAdd;
                return this;
            }

            public TemplateArguments setOffencesToUpdate(List<UUID> offencesToUpdate) {
                this.offencesToUpdate = offencesToUpdate;
                return this;
            }

            public TemplateArguments setOffenceToDelete(List<UUID> offenceToDelete) {
                this.offenceToDelete = offenceToDelete;
                return this;
            }

            public UUID getProsecutionCaseId() {
                return prosecutionCaseId;
            }

            public UUID getDefendantId() {
                return defendantId;
            }

            public List<UUID> getOffencesToAdd() {
                return offencesToAdd;
            }

            public List<UUID> getOffencesToUpdate() {
                return offencesToUpdate;
            }

            public List<UUID> getOffenceToDelete() {
                return offenceToDelete;
            }


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
                                    .withIndicatedPlea(uk.gov.justice.json.schemas.core.IndicatedPlea.indicatedPlea()
                                            .withAllocationDecision(uk.gov.justice.json.schemas.core.AllocationDecision.allocationDecision()
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
                                    .withOffenceFacts(uk.gov.justice.json.schemas.core.OffenceFacts.offenceFacts()
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

        public static AddDefenceCounselCommand standardAddDefenceCounselCommandTemplate(final UUID hearingId, final UUID defendantId) {
            return AddDefenceCounselCommand.builder()
                    .withAttendeeId(randomUUID())
                    .withPersonId(randomUUID())
                    .withHearingId(hearingId)
                    .withFirstName(STRING.next())
                    .withLastName(STRING.next())
                    .withTitle(STRING.next())
                    .withStatus(STRING.next())
                    .addDefendantId(DefendantId.builder().withDefendantId(defendantId))
                    .build();
        }
    }

    public static class AddProsecutionCounselCommandTemplates {
        private AddProsecutionCounselCommandTemplates() {
        }

        public static AddProsecutionCounselCommand addProsecutionCounselCommandTemplate(final UUID hearingId) {
            return AddProsecutionCounselCommand.builder()
                    .withAttendeeId(randomUUID())
                    .withPersonId(randomUUID())
                    .withHearingId(hearingId)
                    .withFirstName(STRING.next())
                    .withLastName(STRING.next())
                    .withTitle(STRING.next())
                    .withStatus(STRING.next())
                    .build();
        }
    }

    public static GenerateNowsCommand generateNowsCommandTemplate(final UUID defendantId) {
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

        Hearing hearing = standardInitiateHearingTemplate().getHearing();
        return GenerateNowsCommand.generateNowsCommand()

                .setHearing(hearing)
                .setSharedResultLines(asList(
                        SharedResultLines.sharedResultLines()
                                .setId(sharedResultLineId0)
                                .setCaseId(caseId)
                                .setDefendantId(defendantId)
                                .setOffenceId(offenceId)
                                .setLevel("CASE")
                                .setLabel(IMPRISONMENT_LABEL)
                                .setRank(1)
                                .setPrompts(asList(
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
                                .setPrompts(asList(
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
                                .setPrompts(asList(
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
                .setNows(asList(Nows.nows()
                                .setId(UUID.randomUUID())
                                .setNowsTypeId(nowsTypeId)
                                .setDefendantId(defendantId)
                                .setNowsTemplateName(STRING.next())
                                .setMaterials(asList(
                                        Material.material()
                                                .setId(materialId)
                                                .setUserGroups(asList(
                                                        UserGroups.userGroups()
                                                                .setGroup("COURTCLERK"),
                                                        UserGroups.userGroups()
                                                                .setGroup("DEFENCECOUNSEL")
                                                ))
                                                .setNowResult(asList(
                                                        NowResult.nowResult()
                                                                .setSharedResultId(sharedResultLineId0)
                                                                .setSequence(1)
                                                                .setPrompts(asList(
                                                                        PromptRef.promptRef()
                                                                                .setId(promptId00)
                                                                                .setLabel(promptLabel0),
                                                                        PromptRef.promptRef()
                                                                                .setId(promptId01)
                                                                                .setLabel(promptLabel1)
                                                                        )
                                                                ),
                                                        NowResult.nowResult()
                                                                .setSharedResultId(sharedResultLineId1)
                                                                .setSequence(2)
                                                                .setPrompts(asList(
                                                                        PromptRef.promptRef()
                                                                                .setId(promptId10)
                                                                                .setLabel(promptLabel0),
                                                                        PromptRef.promptRef()
                                                                                .setId(promptId11)
                                                                                .setLabel(promptLabel1)
                                                                        )
                                                                ),
                                                        NowResult.nowResult()
                                                                .setSharedResultId(sharedResultLineId2)
                                                                .setSequence(3)
                                                                .setPrompts(asList(
                                                                        PromptRef.promptRef()
                                                                                .setId(promptId20)
                                                                                .setLabel(promptLabel0),
                                                                        PromptRef.promptRef()
                                                                                .setId(promptId21)
                                                                                .setLabel(promptLabel1)
                                                                        )
                                                                )
                                                ))
                                ))

                        )
                )
                .setNowTypes(asList(NowTypes.nowTypes()
                                .setId(nowsTypeId)
                                .setTemplateName("SingleTemplate")
                                .setDescription("Imprisonment Order")
                                .setRank(1)
                                .setStaticText("<h3>Imprisonment</h3><p>You have been sentenced to a term of imprisonment. If you<ul><li>Do not comply with the requirements of this order during the <u>supervision period</u>; or</li><li>Commit any other offence during the <u>operational period</u></li></ul>you may be liable to serve the <u>custodial period</u> in prison.<br/><br/><br/><p>For the duration of the <u>supervision period</u>, you will be supervised by your Probation Officer, and<br/>You must<ul><li>Keep in touch with your Probation Officer as they tell you</li><li>Tell your Probation Officer if you intend to change your address</li><li>Comply with all other requirements</li></ul><p><strong>Requirements</strong> – Please refer only to the requirements that the court has specified in the details of your order, <u>as set out above</u><p><strong>Unpaid Work Requirement</strong><p>You must carry out unpaid work for the hours specified as you are told and by the date specified in the order. Your Probation Officer will tell you who will be responsible for supervising work.<p><strong>Activity Requirement</strong><p>You must present yourself as directed at the time and on the days specified in the order and you must undertake the activity the court has specified for the duration specified in the order in the way you are told by your Probation Officer<p><strong>Programme Requirement</strong><p>You must participate in the programme specified in the order at the location specified and for the number of days specified in the order<p><strong>Prohibited Activity Requirement</strong><p>You must not take part in the activity that the court has prohibited in the order for the number of days the court specified<p><strong>Curfew Requirement</strong><p>You must remain in the place or places the court has specified during the periods specified. The curfew requirement lasts for the number of days specified in the order<p>See \"Electronic Monitoring Provision\" in this order<p><strong>Exclusion Requirement</strong><p>You must not enter the place or places the court has specified between the hours specified in the order. The exclusion requirement lasts for the number of days specified in the order<p>See \"Electronic Monitoring Provision\" in this order<p><strong>Residence Requirement</strong><p>You must live at the premises the court has specified and obey any rules that apply there for the number of days specified in the order. You may live at ???? with the prior approval of your Probation Officer.<p><strong>Foreign Travel Prohibition Requirement</strong><p>You must not travel to the prohibited location specified in the order during the period the court has specified in the order.<p><strong>Mental Health Treatment Requirement</strong><p>You must have mental health treatment by or under the direction of the practitioner the court has specified at the location specified as a resident patient for the number of days specified in the order.<p><strong>Drug Rehabilitation Requirement</strong><p>You must have treatment for drug dependency by or under the direction of the practitioner the court has specified at the location specified as a resident patient for the number of days specified in the order.<p>To be sure that you do not have any illegal drug in your body, you must provide samples for testing at such times or in such circumstances as your Probation Officer or the person responsible for your treatment will tell you. The results of tests on the samples will be sent to your Probation Officer who will report the results to the court. Your Probation Officer will also tell the court how your order is progressing and the views of your treatment provider.<p>The court will review this order ????. The first review will be on the date and time specified at the court specified.<p>You must / need not attend this review hearing.<p><strong>Alcohol Treatment Requirement</strong><p>You must have treatment for alcohol dependency by or under the direction of the practitioner the court has specified at the location specified as a resident patient for the number of days specified in the order.<p><strong>Supervision Requirement</strong><p>You must attend appointments with your Probation Officer or another person at the times and places your Probation Officer says.<p><strong>Attendance Centre Requirement</strong><p>You must attend an attendance centre - see separate sheet for details<p><strong>WARNING</strong><p>If you do not comply with your order, you will be brought back to court. The court may then<ul><li>Change the order by adding extra requirements</li><li>Pass a different sentence for the original offences; or</li><li>Send you to prison</li></ul><p><strong>NOTE</strong><p>Either you or your Probation Officer can ask the court to look again at this order and the court can then change it or cancel it if it feels that is the right thing to do. The court may also pass a different sentence for the original offence(s). If you wish to ask the court to look at your order again you should get in touch with the court at the address above.")
                                .setWelshStaticText("<h3> Prison </h3> <p> Fe'ch dedfrydwyd i dymor o garchar. Os ydych <ul> <li> Peidiwch â chydymffurfio â gofynion y gorchymyn hwn yn ystod y cyfnod goruchwylio </u>; neu </li> <li> Ymrwymo unrhyw drosedd arall yn ystod y cyfnod gweithredol </u> </li> </ul> efallai y byddwch yn atebol i wasanaethu'r cyfnod gwarchodaeth </u> yn y carchar. <br/> <br/> <br/> <p> Yn ystod y cyfnod goruchwylio </u>, byddwch chi'n cael eich goruchwylio gan eich Swyddog Prawf, a <br/> Rhaid ichi <ul> < li> Cadwch mewn cysylltiad â'ch Swyddog Prawf wrth iddyn nhw ddweud wrthych </li> <li> Dywedwch wrth eich Swyddog Prawf os ydych yn bwriadu newid eich cyfeiriad </li> <li> Cydymffurfio â'r holl ofynion eraill </li></ul > <p> <strong> Gofynion </strong> - Cyfeiriwch yn unig at y gofynion a nododd y llys yn manylion eich archeb, fel y nodir uchod </u> <p> <strong> Gwaith Di-dāl Gofyniad </strong><p> Rhaid i chi wneud gwaith di-dāl am yr oriau a bennir fel y dywedir wrthych a chi erbyn y dyddiad a bennir yn y gorchymyn. Bydd eich Swyddog Prawf yn dweud wrthych pwy fydd yn gyfrifol am oruchwylio gwaith.<p> <strong> Gweithgaredd Gofyniad </strong> <p> Rhaid i chi gyflwyno eich hun fel y'i cyfarwyddir ar yr amser ac ar y diwrnodau a bennir yn y gorchymyn a rhaid i chi ymgymryd â chi y gweithgaredd y mae'r llys wedi ei nodi ar gyfer y cyfnod a bennir yn y drefn yn y ffordd y dywedir wrth eich Swyddog Prawf <p> <strong> Gofyniad Rhaglen </strong><p> Rhaid i chi gymryd rhan yn y rhaglen a bennir yn y drefn yn y lleoliad a bennir ac am y nifer o ddyddiau a bennir yn y gorchymyn <p> <strong> Gofyniad Gweithgaredd Gwahardd </strong> <p> Rhaid i chi beidio â chymryd rhan yn y gweithgaredd a waharddodd y llys yn y drefn ar gyfer nifer y dyddiau llys penodol <p> <strong> Curfew Requirement </strong> <p> Rhaid i chi aros yn y lle neu lle mae'r llys wedi nodi yn ystod y cyfnodau a bennir. Mae'r gofyniad cyrffyw yn para am y nifer o ddyddiau a bennir yn y<p> Gweler \"Darpariaeth Monitro Electronig\" yn yr orchymyn hwn <p> <strong> Gofyniad Preswyl </strong> <p> Rhaid i chi fyw yn yr adeilad y llys wedi nodi ac ufuddhau i unrhyw reolau sy'n berthnasol yno am y nifer o ddyddiau a bennir yn y gorchymyn. Efallai y byddwch yn byw yn ???? gyda chymeradwyaeth ymlaen llaw eich Swyddog Prawf. <p> <strong> Gofyniad Gwahardd Teithio Tramor </strong> <p> Rhaid i chi beidio â theithio i'r lleoliad gwaharddedig a bennir yn yr orchymyn yn ystod y cyfnod y mae'r llys wedi'i bennu yn y gorchymyn. < p> <strong> Gofyniad Triniaeth Iechyd Meddwl </strong> <p> Rhaid i chi gael triniaeth iechyd meddwl gan neu o dan gyfarwyddyd yr ymarferydd y mae'r llys wedi ei nodi yn y lleoliad a bennir fel claf preswyl am y nifer o ddyddiau a bennir yn y <p> <strong> Angen Adsefydlu Cyffuriau </strong> <p> Rhaid i chi gael triniaeth ar gyfer dibyniaeth ar gyffuriau gan neu o dan gyfarwyddyd yr ymarferydd y mae'r llys wedi ei nodi yn y lleoliad a bennir fel claf preswyl am nifer y dyddiau <p> Er mwyn sicrhau nad oes gennych unrhyw gyffur anghyfreithlon yn eich corff, rhaid i chi ddarparu samplau i'w profi ar yr adegau hynny neu mewn amgylchiadau o'r fath y bydd eich Swyddog Prawf neu'r person sy'n gyfrifol am eich triniaeth yn dweud wrthych chi . Anfonir canlyniadau'r profion ar y samplau i'ch Swyddog Prawf a fydd yn adrodd y canlyniadau i'r llys. Bydd eich Swyddog Prawf hefyd yn dweud wrth y llys sut mae'ch gorchymyn yn mynd rhagddo a barn eich darparwr triniaeth. <P> Bydd y llys yn adolygu'r gorchymyn hwn ????. Bydd yr adolygiad cyntaf ar y dyddiad a'r amser a bennir yn y llys a bennir. <P> Rhaid i chi / nid oes angen i chi fynychu'r gwrandawiad hwn. <P> <strong> Gofyniad Trin Alcohol </strong> <p> Rhaid i chi gael triniaeth ar gyfer dibyniaeth ar alcohol gan neu o dan gyfarwyddyd yr ymarferydd y mae'r llys wedi ei nodi yn y lleoliad a bennir fel claf preswyl am y nifer o ddyddiau a bennir yn y gorchymyn. <p> <strong> Gofyniad Goruchwylio </strong> <p> Rhaid i chi fynychu penodiadau gyda'ch Swyddog Prawf neu berson arall ar yr adegau a lle mae eich Swyddog Prawf yn dweud. <p> <strong> Gofyniad y Ganolfan Bresennol </strong> <p> Rhaid i chi fynychu canolfan bresenoldeb - <p> <strong> RHYBUDD </strong> <p> Os na fyddwch chi'n cydymffurfio â'ch archeb, fe'ch cewch eich troi'n ôl i'r llys. Gall y llys wedyn <ul> <li> Newid y gorchymyn trwy ychwanegu gofynion ychwanegol </li> <li> Pasiwch frawddeg wahanol ar gyfer y troseddau gwreiddiol; neu </li> <li> Anfonwch chi at y carchar </li> </ul> <p> <strong> NOTE </strong> <p> Naill ai chi neu'ch Swyddog Prawf all ofyn i'r llys edrych eto ar y gorchymyn hwn ac yna gall y llys ei newid neu ei ganslo os yw'n teimlo mai dyna'r peth iawn i'w wneud. Gall y llys hefyd basio brawddeg wahanol ar gyfer y trosedd (wyr) gwreiddiol. Os hoffech ofyn i'r llys edrych ar eich archeb eto dylech gysylltu â'r llys yn y cyfeiriad uchod. ")
                                .setPriority("0.5 hours")
                                .setJurisdiction("B")
                        )
                );
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
                    .setResultDefinitions(asList(ResultDefinitions.resultDefinitions()
                            .setId(randomUUID())
                            .setMandatory(true)
                            .setPrimary(true)
                            .setText(STRING.next())
                            .setWelshText(STRING.next())
                            .setSequence(1)
                    ));
        }
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

    public static class NowsRequestedTemplates {
        private NowsRequestedTemplates() {
        }

        public static NowsRequested nowsRequestedTemplate() {

            UUID nowTypeId = randomUUID();
            UUID sharedResultId1 = randomUUID();
            UUID sharedResultId2 = randomUUID();
            UUID sharedResultId3 = randomUUID();
            UUID promptId1 = randomUUID();
            UUID promptId2 = randomUUID();
            UUID promptId3 = randomUUID();

            int count = 0;

            CommandHelpers.InitiateHearingCommandHelper hearingOne = h(standardInitiateHearingTemplate());

            return NowsRequested.nowsRequested()
                    .setCourtClerk(uk.gov.justice.json.schemas.core.CourtClerk.courtClerk()
                            .withId(randomUUID())
                            .withFirstName(STRING.next())
                            .withLastName(STRING.next())
                            .build()
                    )
                    .setNowTypes(asList(NowType.nowType()
                            .setId(nowTypeId)
                            .setTemplateName(STRING.next())
                            .setStaticText(STRING.next())
                            .setDescription(STRING.next())
                            .setRemotePrintingRequired(BOOLEAN.next())
                            .setRank(INTEGER.next())
                            .setPriority(INTEGER.next().toString())
                            .setJurisdiction(STRING.next())
                            .setBilingualTemplateName(STRING.next())
                            .setWelshStaticText(STRING.next())
                            .setWelshDescription(STRING.next())
                    ))
                    .setNows(asList(Now.now()
                            .setDefendantId(hearingOne.getFirstDefendantForFirstCase().getId())
                            .setId(randomUUID())
                            .setNowsTemplateName(STRING.next())
                            .setNowsTypeId(nowTypeId)
                            .setMaterials(asList(uk.gov.moj.cpp.hearing.nows.events.Material.material()
                                    .setId(randomUUID())
                                    .setLanguage(STRING.next())
                                    .setAmended(BOOLEAN.next())
                                    .setUserGroups(asList(MaterialUserGroup.materialUserGroup().setGroup(STRING.next())))
                                    .setNowResult(asList(
                                            uk.gov.moj.cpp.hearing.nows.events.NowResult.nowResult()
                                                    .setSequence(++count)
                                                    .setSharedResultId(sharedResultId1)
                                                    .setPrompts(asList(Prompt.builder()
                                                            .withId(promptId1)
                                                            .withLabel(STRING.next())
                                                            .withValue(STRING.next())
                                                            .build())),
                                            uk.gov.moj.cpp.hearing.nows.events.NowResult.nowResult()
                                                    .setSequence(++count)
                                                    .setSharedResultId(sharedResultId2)
                                                    .setPrompts(asList(Prompt.builder()
                                                            .withId(promptId2)
                                                            .withLabel(STRING.next())
                                                            .withValue(STRING.next())
                                                            .build())),
                                            uk.gov.moj.cpp.hearing.nows.events.NowResult.nowResult()
                                                    .setSequence(++count)
                                                    .setSharedResultId(sharedResultId3)
                                                    .setPrompts(asList(Prompt.builder()
                                                            .withId(promptId3)
                                                            .withLabel(STRING.next())
                                                            .withValue(STRING.next())
                                                            .build()))
                                    ))
                            ))
                    ))
                    .setSharedResultLines(asList(
                            SharedResultLine.sharedResultLine()
                                    .setId(sharedResultId1)
                                    .setCaseId(hearingOne.getFirstCase().getId())
                                    .setDefendantId(hearingOne.getFirstDefendantForFirstCase().getId())
                                    .setOffenceId(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId())
                                    .setLabel(STRING.next())
                                    .setLevel("CASE")
                                    .setOrderedDate(PAST_LOCAL_DATE.next())
                                    .setRank(INTEGER.next())
                                    .setSharedDate(PAST_ZONED_DATE_TIME.next())
                                    .setPrompts(asList(Prompt.prompt()
                                            .setId(promptId1)
                                            .setLabel(STRING.next())
                                            .setValue(STRING.next())
                                    )),
                            SharedResultLine.sharedResultLine()
                                    .setId(sharedResultId2)
                                    .setCaseId(hearingOne.getFirstCase().getId())
                                    .setDefendantId(hearingOne.getFirstDefendantForFirstCase().getId())
                                    .setOffenceId(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId())
                                    .setLabel(STRING.next())
                                    .setLevel("DEFENDANT")
                                    .setOrderedDate(PAST_LOCAL_DATE.next())
                                    .setRank(INTEGER.next())
                                    .setSharedDate(PAST_ZONED_DATE_TIME.next())
                                    .setPrompts(asList(Prompt.prompt()
                                            .setId(promptId2)
                                            .setLabel(STRING.next())
                                            .setValue(STRING.next())
                                    )),
                            SharedResultLine.sharedResultLine()
                                    .setId(sharedResultId3)
                                    .setCaseId(hearingOne.getFirstCase().getId())
                                    .setDefendantId(hearingOne.getFirstDefendantForFirstCase().getId())
                                    .setOffenceId(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId())
                                    .setLabel(STRING.next())
                                    .setLevel("OFFENCE")
                                    .setOrderedDate(PAST_LOCAL_DATE.next())
                                    .setRank(INTEGER.next())
                                    .setSharedDate(PAST_ZONED_DATE_TIME.next())
                                    .setPrompts(asList(Prompt.prompt()
                                            .setId(promptId3)
                                            .setLabel(STRING.next())
                                            .setValue(STRING.next())
                                    ))
                    ))
                    .setHearing(hearingOne.getHearing());

        }
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
