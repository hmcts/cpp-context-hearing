package uk.gov.moj.cpp.hearing.query.api.service.accessfilter;

import static uk.gov.justice.services.messaging.JsonObjects.getString;
import static uk.gov.justice.services.messaging.JsonObjects.getUUID;

import uk.gov.justice.services.messaging.Envelope;
import uk.gov.moj.cpp.hearing.query.api.service.accessfilter.vo.Group;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.json.JsonArray;
import javax.json.JsonObject;

public class GroupsMapper {
    public static final String GROUPS = "groups";
    public static final String GROUP_ID = "groupId";
    public static final String GROUP_NAME = "groupName";
    public static final String PROSECUTING_AUTHORITY = "prosecutingAuthority";

    public List<Group> mapGroups(final Envelope<JsonObject> response) {
        if (!response.payload().containsKey(GROUPS)) {
            return Collections.emptyList();
        }
        final JsonArray groupsJsonArray = response.payload().getJsonArray(GROUPS);

        if (groupsJsonArray == null) {
            return Collections.emptyList();
        }

        return groupsJsonArray.stream()
                .map(p -> (JsonObject) p)
                .map(group ->
                        Group.group()
                                .withGroupId(getUUID(group, GROUP_ID).orElse(null))
                                .withGroupName(getString(group, GROUP_NAME).orElse(null))
                                .withProsecutingAuthority(getString(group, PROSECUTING_AUTHORITY).orElse(null))
                                .build()
                ).collect(Collectors.toList());
    }
}
