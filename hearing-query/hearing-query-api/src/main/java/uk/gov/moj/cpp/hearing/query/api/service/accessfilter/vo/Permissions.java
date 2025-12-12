package uk.gov.moj.cpp.hearing.query.api.service.accessfilter.vo;

import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@SuppressWarnings({"squid:S2384"})
public class Permissions {
    private final List<Group> groups;
    private final List<UserRole> switchableRoles;
    private final List<Permission> permissionList;

    private Permissions(final List<Group> groups,
                       final List<UserRole> switchableRoles,
                       final List<Permission> permissions) {
        this.groups = groups;
        this.switchableRoles = switchableRoles;
        this.permissionList = permissions;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public List<UserRole> getSwitchableRoles() {
        return switchableRoles;
    }

    public List<Permission> getPermissionList() {
        return permissionList;
    }

    public static Builder permission() {
        return new Builder();
    }

    public static class Builder {
        private List<Group> groups;
        private List<UserRole> switchableRoles;
        private List<Permission> permissions;

        public Builder withGroups(final List<Group> groups) {
            this.groups = groups;
            return this;
        }

        public Builder withSwitchableRoles(final List<UserRole> switchableRoles) {
            this.switchableRoles = switchableRoles;
            return this;
        }

        public Builder withPermissions(final List<Permission> permissions) {
            this.permissions = permissions;
            return this;
        }


        public Permissions build() {
            return new Permissions(groups, switchableRoles, permissions);
        }
    }

    @Override
    public String toString() {
        return "Permissions{" +
                "groups=" + groups +
                ", switchableRoles=" + switchableRoles +
                ", permissions=" + permissionList +
                '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Permissions that = (Permissions) o;

        return new EqualsBuilder()
                .append(groups, that.groups)
                .append(switchableRoles, that.switchableRoles)
                .append(permissionList, that.permissionList)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(groups)
                .append(switchableRoles)
                .append(permissionList)
                .toHashCode();
    }
}
