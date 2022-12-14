package uk.gov.moj.cpp.hearing.command.result;

import uk.gov.moj.cpp.OperationTypeEnum;

import java.io.Serializable;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
@SuppressWarnings({"squid:S1948"})
public class Operation implements Serializable {
    private static final long serialVersionUID = 2405172041950251807L;

    private final OperationTypeEnum operationType;
    private final String path;
    private final Optional<Object> value;

    @JsonCreator
    protected Operation(@JsonProperty("operationType") final OperationTypeEnum operationType,
                        @JsonProperty("path") final String path,
                        @JsonProperty("value") final Optional<Object> value) {
        this.operationType = operationType;
        this.path = path;
        this.value = value;
    }

    private Operation(final Builder builder) {
        this.operationType = builder.operationType;
        this.path = builder.path;
        this.value = Optional.ofNullable(builder.value);
    }

    public OperationTypeEnum getOperationType() {
        return operationType;
    }

    public String getPath() {
        return path;
    }

    public Optional<Object> getValue() {
        return value;
    }

    public static final class Builder {

        private OperationTypeEnum operationType;
        private String path;
        private Object value;

        public Operation.Builder withOperationType(final OperationTypeEnum operationType) {
            this.operationType = operationType;
            return this;
        }

        public Operation.Builder withPath(final String path) {
            this.path = path;
            return this;
        }

        public Operation.Builder withValue(final Object value) {
            this.value = value;
            return this;
        }

        public Operation build() {
            return new Operation(this);
        }
    }
}

