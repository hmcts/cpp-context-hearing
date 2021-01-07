package uk.gov.moj.cpp.hearing.query.api.service.accessfilter;

import static java.util.Objects.nonNull;
import static java.util.UUID.fromString;
import static uk.gov.justice.services.messaging.JsonObjects.getString;

import uk.gov.justice.services.messaging.Envelope;
import uk.gov.moj.cpp.hearing.query.api.service.accessfilter.vo.Permission;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.json.JsonArray;
import javax.json.JsonObject;

public class PermissionsMapper {
    public static final String PERMISSIONS = "permissions";
    public static final String ACTION = "action";
    public static final String OBJECT = "object";
    public static final String SOURCE = "source";
    public static final String TARGET = "target";

    public List<Permission> mapPermissions(final Envelope<JsonObject> response) {
        if (!response.payload().containsKey(PERMISSIONS)) {
            return Collections.emptyList();
        }
        final JsonArray permissionsJsonArray =  response.payload().getJsonArray(PERMISSIONS);

        if (permissionsJsonArray == null) {
            return Collections.emptyList();
        }

        return permissionsJsonArray.stream()
                .map(p -> (JsonObject) p)
                .map(permission ->
                        Permission.permission()
                                .withAction(getString(permission, ACTION).orElse(null))
                                .withObject(getString(permission, OBJECT).orElse(null))
                                .withSource(getNullableUUID(permission, SOURCE))
                                .withTarget(getNullableUUID(permission, TARGET))
                                .build()
                ).collect(Collectors.toList());
    }

    private static UUID getNullableUUID(final JsonObject permission, final String attribute) {
        final String uuidString = getString(permission, attribute).orElse(null);
        if (nonNull(uuidString)) {
            return fromString(uuidString);
        } else {
            return null;
        }
    }
}
