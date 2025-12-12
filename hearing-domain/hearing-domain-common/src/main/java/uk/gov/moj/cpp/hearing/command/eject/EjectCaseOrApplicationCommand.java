package uk.gov.moj.cpp.hearing.command.eject;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;
@SuppressWarnings("squid:S2384")
public class EjectCaseOrApplicationCommand implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID prosecutionCaseId;
    private UUID applicationId;
    private List<UUID> hearingIds;
    private String removalReason;

    public UUID getProsecutionCaseId() {
        return prosecutionCaseId;
    }

    public UUID getApplicationId() {
        return applicationId;
    }

    public List<UUID> getHearingIds() {
        return hearingIds;
    }

    public String getRemovalReason() {
        return removalReason;
    }

    public static final class EjectCaseOrApplicationCommandBuilder {
        private UUID prosecutionCaseId;
        private UUID applicationId;
        private List<UUID> hearingIds;
        private String removalReason;

        private EjectCaseOrApplicationCommandBuilder() {
        }

        public static EjectCaseOrApplicationCommandBuilder anEjectCaseOrApplicationCommand() {
            return new EjectCaseOrApplicationCommandBuilder();
        }

        public EjectCaseOrApplicationCommandBuilder withProsecutionCaseId(UUID prosecutionCaseId) {
            this.prosecutionCaseId = prosecutionCaseId;
            return this;
        }

        public EjectCaseOrApplicationCommandBuilder withApplicationId(UUID applicationId) {
            this.applicationId = applicationId;
            return this;
        }

        public EjectCaseOrApplicationCommandBuilder withHearingIds(List<UUID> hearingIds) {
            this.hearingIds = hearingIds;
            return this;
        }

        public EjectCaseOrApplicationCommandBuilder withRemovalReason(String removalReason) {
            this.removalReason = removalReason;
            return this;
        }

        public EjectCaseOrApplicationCommand build() {
            final EjectCaseOrApplicationCommand ejectCaseOrApplicationCommand = new EjectCaseOrApplicationCommand();
            ejectCaseOrApplicationCommand.prosecutionCaseId = this.prosecutionCaseId;
            ejectCaseOrApplicationCommand.hearingIds = this.hearingIds;
            ejectCaseOrApplicationCommand.applicationId = this.applicationId;
            ejectCaseOrApplicationCommand.removalReason = this.removalReason;
            return ejectCaseOrApplicationCommand;
        }
    }
}
