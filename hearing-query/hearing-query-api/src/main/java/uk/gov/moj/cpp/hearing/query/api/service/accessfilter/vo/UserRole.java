package uk.gov.moj.cpp.hearing.query.api.service.accessfilter.vo;

import static java.time.LocalDate.parse;

import java.time.LocalDate;
import java.util.UUID;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class UserRole {

    private final UUID roleId;
    private final String label;
    private final String description;
    private final boolean selectable;
    private LocalDate activatedDate;
    private LocalDate startDate;
    private LocalDate endDate;

    private UserRole(final UUID roleId,
                     final String label,
                     final String description,
                     final boolean selectable,
                     final LocalDate activatedDate,
                     final LocalDate startDate,
                     final LocalDate endDate) {
        this.roleId = roleId;
        this.label = label;
        this.description = description;
        this.selectable = selectable;
        this.activatedDate = activatedDate;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public UUID getRoleId() {
        return roleId;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public boolean isSelectable() {
        return selectable;
    }

    public LocalDate getActivatedDate() {
        return activatedDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public static Builder role() {
        return new Builder();
    }

    public static class Builder {
        private UUID roleId;
        private String label;
        private String description;
        private boolean selectable;
        private LocalDate activatedDate;
        private LocalDate startDate;
        private LocalDate endDate;

        public Builder withRoleId(final UUID roleId) {
            this.roleId = roleId;
            return this;
        }

        public Builder withLabel(final String label) {
            this.label = label;
            return this;
        }

        public Builder withDescription(final String description) {
            this.description = description;
            return this;
        }

        public Builder withSelectable(final boolean selectable) {
            this.selectable = selectable;
            return this;
        }

        public Builder withActivatedDate(final String activatedDate) {
            if (activatedDate!= null) {
                this.activatedDate = parse(activatedDate);
            }
            return this;
        }

        public Builder withStartDate(final String startDate) {
            if (startDate!= null) {
                this.startDate = parse(startDate);
            }
            return this;
        }

        public Builder withEndDate(final String endDate) {
            if (endDate != null) {
                this.endDate = parse(endDate);
            }
            return this;
        }
        public UserRole build() {
            return new UserRole(roleId, label, description, selectable, activatedDate, startDate, endDate);
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

        final UserRole that = (UserRole) o;

        return new EqualsBuilder()
                .append(roleId, that.roleId)
                .append(label, that.label)
                .append(description, that.description)
                .append(selectable, that.selectable)
                .append(activatedDate,that.activatedDate)
                .append(startDate, that.startDate)
                .append(endDate, that.endDate)
                .isEquals();
    }

    @Override
    public String toString() {
        return "UserRole{" +
                "roleId=" + roleId +
                ", label='" + label + '\'' +
                ", description='" + description + '\'' +
                ", selectable=" + selectable +
                ", activatedDate='" + activatedDate + '\'' +
                ", startDate='" + startDate + '\'' +
                ", endDate=" + endDate +
                '}';
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(roleId)
                .append(label)
                .append(description)
                .append(selectable)
                .append(activatedDate)
                .append(startDate)
                .append(endDate)
                .toHashCode();
    }
}
