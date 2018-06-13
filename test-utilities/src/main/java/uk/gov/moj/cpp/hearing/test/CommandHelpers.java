package uk.gov.moj.cpp.hearing.test;

import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.plea.HearingUpdatePleaCommand;
import uk.gov.moj.cpp.hearing.command.result.ShareResultsCommand;
import uk.gov.moj.cpp.hearing.command.verdict.HearingUpdateVerdictCommand;

import java.time.LocalDate;
import java.util.UUID;

public class CommandHelpers {

    private CommandHelpers(){}

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
