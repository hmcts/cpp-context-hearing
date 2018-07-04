package uk.gov.moj.cpp.hearing.test;


import uk.gov.moj.cpp.hearing.command.initiate.Case;
import uk.gov.moj.cpp.hearing.command.initiate.Defendant;
import uk.gov.moj.cpp.hearing.command.initiate.DefendantCase;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.initiate.Judge;
import uk.gov.moj.cpp.hearing.command.initiate.Offence;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.Variant;
import uk.gov.moj.cpp.hearing.command.plea.HearingUpdatePleaCommand;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLine;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLineStatus;
import uk.gov.moj.cpp.hearing.command.result.CourtClerk;
import uk.gov.moj.cpp.hearing.command.result.ResultPrompt;
import uk.gov.moj.cpp.hearing.command.result.ShareResultsCommand;
import uk.gov.moj.cpp.hearing.command.verdict.HearingUpdateVerdictCommand;
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselUpsert;
import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselUpsert;
import uk.gov.moj.cpp.hearing.domain.event.VerdictUpsert;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.toList;

public class CommandHelpers {

    private CommandHelpers() {
    }

    public static InitiateHearingCommandHelper h(InitiateHearingCommand initiateHearingCommand) {
        return new InitiateHearingCommandHelper(initiateHearingCommand);
    }

    public static UpdatePleaCommandHelper h(HearingUpdatePleaCommand hearingUpdatePleaCommand) {
        return new UpdatePleaCommandHelper(hearingUpdatePleaCommand);
    }

    public static UpdateVerdictCommandHelper h(HearingUpdateVerdictCommand hearingUpdateVerdictCommand) {
        return new UpdateVerdictCommandHelper(hearingUpdateVerdictCommand);
    }

    public static ResultsSharedEventHelper h(ResultsShared resultsShared) {
        return new ResultsSharedEventHelper(resultsShared);
    }

    public static class InitiateHearingCommandHelper {
        private InitiateHearingCommand initiateHearingCommand;

        public InitiateHearingCommandHelper(InitiateHearingCommand initiateHearingCommand) {
            this.initiateHearingCommand = initiateHearingCommand;
        }

        public UUID getHearingId() {
            return initiateHearingCommand.getHearing().getId();
        }

        public Judge getJudge() {
            return initiateHearingCommand.getHearing().getJudge();
        }

        public UUID getFirstCaseId() {
            return initiateHearingCommand.getCases().get(0).getCaseId();
        }

        public String getFirstCaseUrn() {
            return initiateHearingCommand.getCases().get(0).getUrn();
        }

        public UUID getFirstDefendantId() {
            return initiateHearingCommand.getHearing().getDefendants().get(0).getId();
        }

        public DefendantCase getFirstCaseForFirstDefendant() {
            return initiateHearingCommand.getHearing().getDefendants().get(0).getDefendantCases().get(0);
        }

        public UUID getSecondDefendantId() {
            return initiateHearingCommand.getHearing().getDefendants().get(1).getId();
        }

        public Defendant getSecondDefendant() {
            return initiateHearingCommand.getHearing().getDefendants().get(0);
        }

        public UUID getFirstOffenceIdForFirstDefendant() {
            return initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getId();
        }

        public UUID getSecondOffenceIdForFirstDefendant() {
            return initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(1).getId();
        }

        public InitiateHearingCommand it() {
            return initiateHearingCommand;
        }

        public InitiateHearingCommandHelper setFirstCaseId(UUID caseId) {
            this.initiateHearingCommand.getCases().get(0).setCaseId(caseId);
            return this;
        }

        public Case getFirstCase() {
            return this.initiateHearingCommand.getCases().get(0);
        }

        public Defendant getFirstDefendant() {
            return this.initiateHearingCommand.getHearing().getDefendants().get(0);
        }

        public Offence getFirstOffenceForFirstDefendant() {
            return this.initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0);
        }

        public DefendantCase getFirstDefendantCaseForFirstDefendant() {
            return this.initiateHearingCommand.getHearing().getDefendants().get(0).getDefendantCases().get(0);
        }

        public Offence getFirstOffence() {
            return this.initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0);
        }
    }

    public static class UpdatePleaCommandHelper {
        private HearingUpdatePleaCommand hearingUpdatePleaCommand;

        public UpdatePleaCommandHelper(HearingUpdatePleaCommand hearingUpdatePleaCommand) {
            this.hearingUpdatePleaCommand = hearingUpdatePleaCommand;
        }

        public LocalDate getFirstPleaDate() {
            return this.hearingUpdatePleaCommand.getDefendants().get(0).getOffences().get(0).getPlea().getPleaDate();
        }

        public TestTemplates.PleaValueType getFirstPleaValue() {
            return TestTemplates.PleaValueType.valueOf(this.hearingUpdatePleaCommand.getDefendants().get(0).getOffences().get(0).getPlea().getValue());
        }
    }

    public static class UpdateVerdictCommandHelper {
        private HearingUpdateVerdictCommand hearingUpdateVerdictCommand;

        public UpdateVerdictCommandHelper(HearingUpdateVerdictCommand hearingUpdateVerdictCommand) {
            this.hearingUpdateVerdictCommand = hearingUpdateVerdictCommand;
        }

        public String getFirstVerdictCategory() {
            return this.hearingUpdateVerdictCommand.getDefendants().get(0).getOffences().get(0).getVerdict().getValue().getCategory();
        }

        public TestTemplates.VerdictCategoryType getFirstVerdictCategoryType() {
            return TestTemplates.VerdictCategoryType.valueOf(this.hearingUpdateVerdictCommand.getDefendants().get(0).getOffences().get(0).getVerdict().getValue().getCategoryType());
        }

        public uk.gov.moj.cpp.hearing.command.verdict.Offence getFirstOffenceForFirstDefendant() {
            return this.hearingUpdateVerdictCommand.getDefendants().get(0).getOffences().get(0);
        }
    }

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
            return this.resultsShared.getHearing().getHearingDays().get(0);
        }

        public UUID getHearingId() {
            return this.resultsShared.getHearing().getId();
        }

        public List<Case> getCases() {
            return this.resultsShared.getCases();
        }

        public Case getFirstCase(){
            return this.resultsShared.getCases().get(0);
        }

        public List<UUID> getCaseIds() {
            return this.resultsShared.getCases().stream().map(Case::getCaseId).collect(toList());
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

        public Defendant getFirstDefendant() {
            return resultsShared.getHearing().getDefendants().get(0);
        }

        public Offence getFirstDefendantFirstOffence() {
            return resultsShared.getHearing().getDefendants().get(0).getOffences().get(0);
        }

        public uk.gov.moj.cpp.hearing.domain.Plea getFirstPlea() {
            return resultsShared.getPleas().values().stream().findFirst().orElse(null);
        }

        public VerdictUpsert getFirstVerdict(){
            return resultsShared.getVerdicts().values().stream().findFirst().orElse(null);
        }

        public DefendantCase getFirstDefendantCase() {
            return resultsShared.getHearing().getDefendants().get(0).getDefendantCases().get(0);
        }

        public CompletedResultLine getFirstCompletedResultLine() {
            return resultsShared.getCompletedResultLines().get(0);
        }

        public CompletedResultLineStatus getFirstCompletedResultLineStatus() {
            return resultsShared.getCompletedResultLinesStatus().values().stream().findFirst().orElse(null);
        }

        public ResultPrompt getFirstCompletedResultLineFirstPrompt() {
            return resultsShared.getCompletedResultLines().get(0).getPrompts().get(0);
        }

        public uk.gov.moj.cpp.hearing.command.initiate.Hearing getHearing() {
            return this.resultsShared.getHearing();
        }

        public CourtClerk getCourtClerk() {
            return this.resultsShared.getCourtClerk();
        }

        public Variant getFirstVariant(){
            return this.resultsShared.getVariantDirectory().get(0);
        }
    }
}
