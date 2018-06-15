package uk.gov.moj.cpp.hearing.test;

import uk.gov.moj.cpp.hearing.command.initiate.Case;
import uk.gov.moj.cpp.hearing.command.initiate.Defendant;
import uk.gov.moj.cpp.hearing.command.initiate.DefendantCase;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.initiate.Offence;
import uk.gov.moj.cpp.hearing.command.plea.HearingUpdatePleaCommand;
import uk.gov.moj.cpp.hearing.command.result.ShareResultsCommand;
import uk.gov.moj.cpp.hearing.command.verdict.HearingUpdateVerdictCommand;

import java.time.LocalDate;
import java.util.UUID;

public class CommandHelpers {

    private CommandHelpers(){}

    public static InitiateHearingCommandHelper h(InitiateHearingCommand initiateHearingCommand){
        return new InitiateHearingCommandHelper(initiateHearingCommand);
    }

    public static UpdatePleaCommandHelper h(HearingUpdatePleaCommand hearingUpdatePleaCommand){
        return new UpdatePleaCommandHelper(hearingUpdatePleaCommand);
    }

    public static UpdateVerdictCommandHelper h(HearingUpdateVerdictCommand hearingUpdateVerdictCommand){
        return new UpdateVerdictCommandHelper(hearingUpdateVerdictCommand);
    }

    public static class InitiateHearingCommandHelper {
        private InitiateHearingCommand initiateHearingCommand;


        public InitiateHearingCommandHelper(InitiateHearingCommand initiateHearingCommand) {
            this.initiateHearingCommand = initiateHearingCommand;
        }

        public UUID getHearingId() {
            return initiateHearingCommand.getHearing().getId();
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

        public UUID getSecondDefendantId() {
            return initiateHearingCommand.getHearing().getDefendants().get(1).getId();
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
}
