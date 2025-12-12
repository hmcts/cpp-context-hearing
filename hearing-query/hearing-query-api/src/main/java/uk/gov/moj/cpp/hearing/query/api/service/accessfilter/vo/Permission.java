package uk.gov.moj.cpp.hearing.query.api.service.accessfilter.vo;

import java.util.UUID;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Permission {

    private final String object;

    private final String action;

    private final UUID source;

    private final UUID target;

    private Permission(final String object,
                      final String action,
                      final UUID source,
                      final UUID target) {
        this.action = action;
        this.object = object;
        this.source = source;
        this.target = target;
    }

    public String getObject() {
        return object;
    }

    public String getAction() {
        return action;
    }

    public UUID getSource() {
        return source;
    }

    public UUID getTarget() {
        return target;
    }

    public static Builder permission() {
        return new Builder();
    }

    public static class Builder {
        private String object;

        private String action;

        private UUID source;

        private UUID target;

        public Builder withObject(final String object) {
            this.object = object;
            return this;
        }

        public Builder withAction(final String action) {
            this.action = action;
            return this;
        }

        public Builder withSource(final UUID source) {
            this.source = source;
            return this;
        }

        public Builder withTarget(final UUID target) {
            this.target = target;
            return this;
        }


        public Permission build() {
            return new Permission(object, action, source, target);
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Permission that = (Permission) o;

        return new EqualsBuilder()
                .append(source, that.source)
                .append(target, that.target)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(source)
                .append(target)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "Permission{" +
                "object='" + object + '\'' +
                ", action='" + action + '\'' +
                ", source=" + source +
                ", target=" + target +
                '}';
    }
}
