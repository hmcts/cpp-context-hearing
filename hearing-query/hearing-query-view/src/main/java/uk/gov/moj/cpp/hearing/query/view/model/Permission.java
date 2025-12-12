package uk.gov.moj.cpp.hearing.query.view.model;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;

public class Permission {

    private UUID permissionId;
    private String object;
    private String action;
    private boolean hasPermission;

    @JsonCreator
    public Permission(final UUID permissionId, final String object, final String action, final boolean hasPermission){
        this.permissionId = permissionId;
        this.object = object;
        this.action = action;
        this.hasPermission = hasPermission;
    }

    public UUID getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(final UUID permissionId) {
        this.permissionId = permissionId;
    }

    public String getObject() {
        return object;
    }

    public void setObject(final String object) {
        this.object = object;
    }

    public String getAction() {
        return action;
    }

    public void setAction(final String action) {
        this.action = action;
    }

    public boolean isHasPermission() {
        return hasPermission;
    }

    public void setHasPermission(final boolean hasPermission) {
        this.hasPermission = hasPermission;
    }
}
