package uk.gov.moj.cpp.hearing.query.api.service.accessfilter;

import static uk.gov.justice.services.messaging.JsonObjects.getBoolean;
import static uk.gov.justice.services.messaging.JsonObjects.getString;
import static uk.gov.justice.services.messaging.JsonObjects.getUUID;
import static uk.gov.moj.cpp.hearing.query.api.service.accessfilter.vo.UserRole.role;

import uk.gov.justice.services.messaging.Envelope;
import uk.gov.moj.cpp.hearing.query.api.service.accessfilter.vo.UserRole;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.json.JsonArray;
import javax.json.JsonObject;

public class RolesMapper {
    public static final String ROLES = "allRoles";
    public static final String SWITCHABLE_ROLES = "switchableRoles";
    public static final String ROLE_ID = "roleId";
    public static final String DESCRIPTION = "description";
    public static final String LABEL = "label";
    public static final String SELECTABLE = "selectable";
    public static final String ACTIVATED_DATE = "activatedDate";
    public static final String START_DATE = "startDate";
    public static final String END_DATE = "endDate";

    public List<UserRole> mapRoles(final Envelope<JsonObject> response) {
        if (!response.payload().containsKey(ROLES)) {
            return Collections.emptyList();
        }
        final JsonArray rolesJsonArray = response.payload().getJsonArray(ROLES);

        if (rolesJsonArray == null) {
            return Collections.emptyList();
        }
        return mapRoles(rolesJsonArray);
    }

    public List<UserRole> switchableRoles(final Envelope<JsonObject> response) {
        if (!response.payload().containsKey(SWITCHABLE_ROLES)) {
            return Collections.emptyList();
        }
        final JsonArray switchableRolesJsonArray = response.payload().getJsonArray(SWITCHABLE_ROLES);

        if (switchableRolesJsonArray == null) {
            return Collections.emptyList();
        }
        return mapRoles(switchableRolesJsonArray);
    }

    private List<UserRole> mapRoles(final JsonArray rolesJsonArray) {
        return rolesJsonArray.stream()
                .map(p -> (JsonObject) p)
                .map(role -> role()
                        .withRoleId(getUUID(role, ROLE_ID).orElse(null))
                        .withLabel(getString(role, LABEL).orElse(null))
                        .withDescription(getString(role, DESCRIPTION).orElse(null))
                        .withSelectable(getBoolean(role, SELECTABLE).orElseGet(null))
                        .withActivatedDate(getString(role, ACTIVATED_DATE).orElse(null))
                        .withStartDate(getString(role, START_DATE).orElse(null))
                        .withEndDate(getString(role, END_DATE).orElse(null))
                        .build()).collect(Collectors.toList());
    }

}
