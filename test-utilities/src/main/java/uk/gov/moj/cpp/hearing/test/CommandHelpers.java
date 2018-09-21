package uk.gov.moj.cpp.hearing.test;

import static java.util.stream.Collectors.toList;

import uk.gov.justice.json.schemas.core.CourtClerk;
import uk.gov.justice.json.schemas.core.DelegatedPowers;
import uk.gov.justice.json.schemas.core.Hearing;
import uk.gov.justice.json.schemas.core.Material;
import uk.gov.justice.json.schemas.core.PleaValue;
import uk.gov.justice.json.schemas.core.ProsecutionCase;
import uk.gov.justice.json.schemas.core.ResultLine;
import uk.gov.justice.json.schemas.core.Target;
import uk.gov.justice.json.schemas.core.Verdict;
import uk.gov.justice.progression.events.CaseDefendantDetails;
import uk.gov.moj.cpp.hearing.command.defendant.UpdateDefendantAttendanceCommand;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.Variant;
import uk.gov.moj.cpp.hearing.command.offence.UpdateOffencesForDefendantCommand;
import uk.gov.moj.cpp.hearing.command.offence.DefendantCaseOffences;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLine;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLineStatus;
import uk.gov.moj.cpp.hearing.command.result.ResultPrompt;
import uk.gov.moj.cpp.hearing.command.result.ShareResultsCommand;
import uk.gov.moj.cpp.hearing.command.subscription.UploadSubscription;
import uk.gov.moj.cpp.hearing.command.subscription.UploadSubscriptionsCommand;
import uk.gov.moj.cpp.hearing.command.verdict.HearingUpdateVerdictCommand;
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselUpsert;
import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselUpsert;
import uk.gov.moj.cpp.hearing.domain.event.VerdictUpsert;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.domain.updatepleas.UpdatePleaCommand;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.NowResult;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Nows;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.PromptRef;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.UserGroups;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.AllNows;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.NowDefinition;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.AllResultDefinitions;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class CommandHelpers {

    private CommandHelpers() {
    }

    public static InitiateHearingCommandHelper h(InitiateHearingCommand initiateHearingCommand) {
        return new InitiateHearingCommandHelper(initiateHearingCommand);
    }

    public static UpdatePleaCommandHelper h(UpdatePleaCommand hearingUpdatePleaCommand) {
        return new UpdatePleaCommandHelper(hearingUpdatePleaCommand);
    }

    public static UpdateVerdictCommandHelper h(HearingUpdateVerdictCommand hearingUpdateVerdictCommand) {
        return new UpdateVerdictCommandHelper(hearingUpdateVerdictCommand);
    }

    public static ResultsSharedEventHelper h(ResultsShared resultsShared) {
        return new ResultsSharedEventHelper(resultsShared);
    }

    public static ShareResultsCommandHelper h(ShareResultsCommand shareResultsCommand) {
        return new ShareResultsCommandHelper(shareResultsCommand);
    }

    public static AllNowsReferenceDataHelper h(AllNows allNows) {
        return new AllNowsReferenceDataHelper(allNows);
    }

    public static AllResultDefinitionsReferenceDataHelper h(AllResultDefinitions allResultDefinitions) {
        return new AllResultDefinitionsReferenceDataHelper(allResultDefinitions);
    }

    public static UpdateOffencesForDefendantCommandHelper h(UpdateOffencesForDefendantCommand updateOffencesForDefendantCommand) {
        return new UpdateOffencesForDefendantCommandHelper(updateOffencesForDefendantCommand);
    }

    public static CaseDefendantDetailsHelper h(CaseDefendantDetails caseDefendantDetails) {
        return new CaseDefendantDetailsHelper(caseDefendantDetails);
    }

    public static UploadSubscriptionsCommandHelper h(UploadSubscriptionsCommand uploadSubscriptionsCommand) {
        return new UploadSubscriptionsCommandHelper(uploadSubscriptionsCommand);
    }


    public static UpdateDefendantAttendanceCommandHelper h(UpdateDefendantAttendanceCommand updateDefendantAttendanceCommand){
        return new UpdateDefendantAttendanceCommandHelper(updateDefendantAttendanceCommand);
    }

    public static NowsHelper h(List<Nows> nows) {
        return new NowsHelper(nows);
    }

    public static class NowsHelper {

        private List<Nows> nows;

        public NowsHelper(List<Nows> nows) {
            this.nows = nows;
        }

        public List<Nows> it() {
            return nows;
        }

        public Nows getFirstNow() {
            return nows.get(0);
        }

        public uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Material getFirstMaterial() {
            return nows.get(0).getMaterials().get(0);
        }

        public NowResult getFirstNowsResult() {
            return nows.get(0).getMaterials().get(0).getNowResult().get(0);
        }

        public PromptRef getFirstPrompt() {
            return nows.get(0).getMaterials().get(0).getNowResult().get(0).getPrompts().get(0);
        }

        public UserGroups getFirstUserGroup() {
            return nows.get(0).getMaterials().get(0).getUserGroups().get(0);
        }
    }

    public static class AllNowsReferenceDataHelper {
        private AllNows allNows;

        public AllNowsReferenceDataHelper(AllNows allNows) {
            this.allNows = allNows;
        }

        public NowDefinition getFirstNowDefinition() {
            return this.allNows.getNows().get(0);
        }

        public UUID getFirstNowDefinitionId() {
            return this.allNows.getNows().get(0).getId();
        }

        public UUID getFirstNowDefinitionFirstResultDefinitionId() {
            return this.allNows.getNows().get(0).getResultDefinitions().get(0).getId();
        }


        public AllNows it() {
            return this.allNows;
        }
    }

    public static class AllResultDefinitionsReferenceDataHelper {
        private AllResultDefinitions allResultDefinitions;

        public AllResultDefinitionsReferenceDataHelper(AllResultDefinitions allResultDefinitions) {
            this.allResultDefinitions = allResultDefinitions;
        }

        public Prompt getFirstResultDefinitionFirstResultPrompt() {
            return allResultDefinitions.getResultDefinitions().get(0).getPrompts().get(0);
        }

        public AllResultDefinitions it() {
            return this.allResultDefinitions;
        }
    }

    public static class InitiateHearingCommandHelper {
        private InitiateHearingCommand initiateHearingCommand;

        public InitiateHearingCommandHelper(InitiateHearingCommand initiateHearingCommand) {
            this.initiateHearingCommand = initiateHearingCommand;
        }

        public UUID getHearingId() {
            return initiateHearingCommand.getHearing().getId();
        }

        public Hearing getHearing() {
            return initiateHearingCommand.getHearing();
        }

        public ProsecutionCase getFirstCase() {
            return initiateHearingCommand.getHearing().getProsecutionCases().get(0);
        }

        public uk.gov.justice.json.schemas.core.Defendant getFirstDefendantForFirstCase() {
            return initiateHearingCommand.getHearing().getProsecutionCases().get(0).getDefendants().get(0);
        }

        public UUID getFirstOffenceIdForFirstDefendant() {
            return initiateHearingCommand.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getId();
        }

        public UUID getSecondOffenceIdForFirstDefendant() {
            return initiateHearingCommand.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(1).getId();
        }

        public InitiateHearingCommand it() {
            return initiateHearingCommand;
        }

        public uk.gov.justice.json.schemas.core.Offence getFirstOffenceForFirstDefendantForFirstCase() {
            return this.initiateHearingCommand.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0);
        }
    }

    public static class UpdatePleaCommandHelper {

        private UpdatePleaCommand hearingUpdatePleaCommand;

        public UpdatePleaCommandHelper(UpdatePleaCommand hearingUpdatePleaCommand) {
            this.hearingUpdatePleaCommand = hearingUpdatePleaCommand;
        }

        public LocalDate getFirstPleaDate() {
            return this.hearingUpdatePleaCommand.getPleas().get(0).getPleaDate();
        }

        public PleaValue getFirstPleaValue() {
            return this.hearingUpdatePleaCommand.getPleas().get(0).getValue();
        }

        public DelegatedPowers getFirstDelegatePowers() {
            return this.hearingUpdatePleaCommand.getPleas().get(0).getDelegatedPowers();
        }


    }

    public static class UpdateVerdictCommandHelper {

        private HearingUpdateVerdictCommand hearingUpdateVerdictCommand;

        public UpdateVerdictCommandHelper(HearingUpdateVerdictCommand hearingUpdateVerdictCommand) {
            this.hearingUpdateVerdictCommand = hearingUpdateVerdictCommand;
        }

        public String getFirstVerdictCategory() {
            return this.hearingUpdateVerdictCommand.getVerdicts().get(0).getVerdictType().getCategory();
        }

        public TestTemplates.VerdictCategoryType getFirstVerdictCategoryType() {
            return TestTemplates.VerdictCategoryType.valueOf(this.hearingUpdateVerdictCommand.getVerdicts().get(0).getVerdictType().getCategoryType());
        }

        public Verdict getFirstVerdict() {
            return this.hearingUpdateVerdictCommand.getVerdicts().get(0);
        }
    }

    @SuppressWarnings({"squid:S1135", "squid:CommentedOutCodeLine"})
    public static class ShareResultsCommandHelper {

        private ShareResultsCommand shareResultsCommand;

        public ShareResultsCommandHelper(ShareResultsCommand shareResultsCommand) {
            this.shareResultsCommand = shareResultsCommand;
        }

        public ShareResultsCommand it() {
            return shareResultsCommand;
        }
    }

    public static class ResultsSharedEventHelper {
        private ResultsShared resultsShared;

        public ResultsSharedEventHelper(ResultsShared resultsShared) {
            this.resultsShared = resultsShared;
        }

        public ZonedDateTime getFirstHearingDay() {
            return this.resultsShared.getHearing().getHearingDays().get(0).getSittingDay();
        }

        public UUID getHearingId() {
            return this.resultsShared.getHearing().getId();
        }

        public List<ProsecutionCase> getCases() {
            return this.resultsShared.getHearing().getProsecutionCases();
        }

        public ProsecutionCase getFirstCase() {
            return getCases().get(0);
        }

        public List<UUID> getCaseIds() {
            return getCases().stream().map(ProsecutionCase::getId).collect(toList());
        }

        public ResultsShared it() {
            return this.resultsShared;
        }

        public ProsecutionCounselUpsert getFirstProsecutionCounsel() {
            return resultsShared.getProsecutionCounsels().values().stream().findAny().orElse(null);
        }

        public DefenceCounselUpsert getFirstDefenseCounsel() {
            return resultsShared.getDefenceCounsels().values().stream().findAny().orElse(null);
        }

        public uk.gov.justice.json.schemas.core.Defendant getFirstDefendant() {
            return resultsShared.getHearing().getProsecutionCases().get(0).getDefendants().get(0);
        }

        public uk.gov.justice.json.schemas.core.Defendant getSecondDefendant() {
            return resultsShared.getHearing().getProsecutionCases().get(0).getDefendants().get(1);
        }

        public uk.gov.justice.json.schemas.core.Offence getFirstDefendantFirstOffence() {
            return getFirstDefendant().getOffences().get(0);
        }

        public uk.gov.moj.cpp.hearing.domain.Plea getFirstPlea() {
            return resultsShared.getPleas().values().stream().findFirst().orElse(null);
        }

        public VerdictUpsert getFirstVerdict() {
            return resultsShared.getVerdicts().values().stream().findFirst().orElse(null);
        }

        public ProsecutionCase getFirstDefendantCase() {
            return resultsShared.getHearing().getProsecutionCases().get(0);
        }

        public Target getFirstTarget() {
            return resultsShared.getHearing().getTargets().get(0);
        }

        public ResultLine getFirstCompletedResultLine() {
            return resultsShared.getHearing().getTargets().stream()
                    .flatMap(target -> target.getResultLines().stream())
                    .filter(ResultLine::getIsComplete)
                    .collect(Collectors.toList()).get(0);
        }

        public ResultLine getSecondCompletedResultLine() {
            return resultsShared.getHearing().getTargets().stream()
                    .flatMap(target -> target.getResultLines().stream())
                    .filter(ResultLine::getIsComplete)
                    .collect(Collectors.toList()).get(1);
        }

        public CompletedResultLineStatus getFirstCompletedResultLineStatus() {
            return resultsShared.getCompletedResultLinesStatus().values().stream().findFirst().orElse(null);
        }

        public uk.gov.justice.json.schemas.core.Prompt getFirstCompletedResultLineFirstPrompt() {
            return getFirstCompletedResultLine().getPrompts().get(0);
        }

        public Hearing getHearing() {
            return this.resultsShared.getHearing();
        }

        public CourtClerk getCourtClerk() {
            return this.resultsShared.getCourtClerk();
        }

        public Variant getFirstVariant() {
            return this.resultsShared.getVariantDirectory().get(0);
        }
    }

    public static class UpdateOffencesForDefendantCommandHelper {
        private UpdateOffencesForDefendantCommand updateOffencesForDefendantCommand;

        public UpdateOffencesForDefendantCommandHelper(UpdateOffencesForDefendantCommand updateOffencesForDefendantCommand) {
            this.updateOffencesForDefendantCommand = updateOffencesForDefendantCommand;
        }

        public DefendantCaseOffences getFirstAddedOffences() {
            return this.updateOffencesForDefendantCommand.getAddedOffences().get(0);
        }

        public uk.gov.justice.json.schemas.core.Offence getFirstOffenceFromAddedOffences() {
            return this.updateOffencesForDefendantCommand.getAddedOffences().get(0).getOffences().get(0);
        }

        public uk.gov.justice.json.schemas.core.Offence getFirstOffenceFromUpdatedOffences() {
            return this.updateOffencesForDefendantCommand.getUpdatedOffences().get(0).getOffences().get(0);
        }

        public UUID getFirstOffenceIdFromDeletedOffences() {
            return this.updateOffencesForDefendantCommand.getDeletedOffences().get(0).getOffences().get(0);
        }

        public UpdateOffencesForDefendantCommand it() {
            return this.updateOffencesForDefendantCommand;
        }
    }

    public static class CaseDefendantDetailsHelper {
        private CaseDefendantDetails caseDefendantDetails;

        public CaseDefendantDetailsHelper(CaseDefendantDetails caseDefendantDetails) {
            this.caseDefendantDetails = caseDefendantDetails;
        }

        public uk.gov.moj.cpp.hearing.command.defendant.Defendant getFirstDefendant() {
            return caseDefendantDetails.getDefendants().get(0);
        }
    }

    public static class UploadSubscriptionsCommandHelper {
        private UploadSubscriptionsCommand uploadSubscriptionsCommand;

        public UploadSubscriptionsCommandHelper(UploadSubscriptionsCommand uploadSubscriptionsCommand) {
            this.uploadSubscriptionsCommand = uploadSubscriptionsCommand;
        }

        public UploadSubscription getFirstSubscription() {
            return uploadSubscriptionsCommand.getSubscriptions().get(0);
        }
    }

    @SuppressWarnings("squid:S1068")
    public static class UpdateDefendantAttendanceCommandHelper {

        private UpdateDefendantAttendanceCommand defendantAttendanceCommand;

        public UpdateDefendantAttendanceCommandHelper(UpdateDefendantAttendanceCommand defendantAttendanceCommand) {
            this.defendantAttendanceCommand = defendantAttendanceCommand;
        }

    }
}
