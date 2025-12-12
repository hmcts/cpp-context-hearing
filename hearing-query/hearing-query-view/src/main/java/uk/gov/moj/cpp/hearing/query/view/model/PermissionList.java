package uk.gov.moj.cpp.hearing.query.view.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;

public class PermissionList {

    private List<Permission> permissions;

    @JsonCreator
    public PermissionList(final List<Permission> permissions) {
        this.permissions = permissions;
    }

    public List<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(final List<Permission> permissions) {
        this.permissions = permissions;
    }

    public static class Builder {
        private List<Permission> permissions;

        public Builder withPermissions(final List<Permission> permissions) {
            this.permissions = permissions;
            return this;
        }

        public PermissionList build() {
            return new PermissionList(permissions);
        }
    }
}
