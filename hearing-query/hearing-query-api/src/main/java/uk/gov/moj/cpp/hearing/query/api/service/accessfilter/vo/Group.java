package uk.gov.moj.cpp.hearing.query.api.service.accessfilter.vo;

import java.util.UUID;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Group {
    private final UUID groupId;
    private final String groupName;
    private final String prosecutingAuthority;

    private Group(final UUID groupId,
                 final String groupName,
                 final String prosecutingAuthority) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.prosecutingAuthority = prosecutingAuthority;
    }


    public UUID getGroupId() {
        return groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getProsecutingAuthority() {
        return prosecutingAuthority;
    }

    public static Builder group() {
        return new Builder();
    }

    public static class Builder {
        private UUID groupId;
        private String groupName;
        private String prosecutingAuthority;

        public Builder withGroupId(final UUID groupId) {
            this.groupId = groupId;
            return this;
        }

        public Builder withGroupName(final String groupName) {
            this.groupName = groupName;
            return this;
        }

        public Builder withProsecutingAuthority(final String prosecutingAuthority) {
            this.prosecutingAuthority = prosecutingAuthority;
            return this;
        }

        public Group build() {
            return new Group(groupId, groupName, prosecutingAuthority);
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

        final Group that = (Group) o;

        return new EqualsBuilder()
                .append(groupId, that.groupId)
                .append(groupName, that.groupName)
                .append(prosecutingAuthority, that.prosecutingAuthority)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(groupId)
                .append(groupName)
                .append(prosecutingAuthority)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "Group{" +
                "groupId=" + groupId +
                ", groupName='" + groupName + '\'' +
                ", prosecutingAuthority='" + prosecutingAuthority + '\'' +
                '}';
    }
}
