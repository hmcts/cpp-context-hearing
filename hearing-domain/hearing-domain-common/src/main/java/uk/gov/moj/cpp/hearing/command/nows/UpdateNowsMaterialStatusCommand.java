package uk.gov.moj.cpp.hearing.command.nows;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateNowsMaterialStatusCommand {

    private final UUID materialId;
    private final String status;

    @JsonCreator
    protected UpdateNowsMaterialStatusCommand(@JsonProperty(value = "materialId", required = true) final UUID materialId,
                                              @JsonProperty(value = "status", required = true) final String status) {
        this.materialId = materialId;
        this.status = status;
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getMaterialId() {
        return materialId;
    }

    public String getStatus() {
        return status;
    }

    public static final class Builder {

        private UUID materialId;
        private String status;

        public Builder withMaterialId(final UUID materialId) {
            this.materialId = materialId;
            return this;
        }

        public Builder withStatus(final String status) {
            this.status = status;
            return this;
        }

        public UpdateNowsMaterialStatusCommand build() {
            return new UpdateNowsMaterialStatusCommand(this.materialId, this.status);
        }
    }
}
