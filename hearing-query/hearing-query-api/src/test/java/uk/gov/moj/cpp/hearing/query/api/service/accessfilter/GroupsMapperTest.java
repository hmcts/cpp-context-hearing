package uk.gov.moj.cpp.hearing.query.api.service.accessfilter;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.services.messaging.Envelope.metadataBuilder;

import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.moj.cpp.hearing.query.api.service.accessfilter.vo.Group;

import java.util.List;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GroupsMapperTest {
    public static final String GROUPS = "groups";
    public static final String GROUP_ID = "groupId";
    public static final String GROUP_NAME = "groupName";
    public static final String PROSECUTING_AUTHORITY = "prosecutingAuthority";

    private static final UUID GROUP_ID1 = randomUUID();
    private static final String GROUP_NAME1 = "groupName1";
    private static final String PROSECURITY_AUTH1 = "TFL";

    private static final UUID GROUP_ID2 = randomUUID();
    private static final String GROUP_NAME2 = "groupName2";
    private static final String PROSECURITY_AUTH2 = "TVL";

    @InjectMocks
    private GroupsMapper groupsMapper;

    @Test
    public void shouldReturnGroups() {
        final String userId = randomUUID().toString();
        final Metadata metadata = metadataBuilder().withName("usersgroups.get-logged-in-user-permissions")
                .withId(randomUUID()).withUserId(userId).build();

        final JsonObjectBuilder group1Json = Json.createObjectBuilder();
        group1Json.add(GROUP_ID, GROUP_ID1.toString());
        group1Json.add(GROUP_NAME, GROUP_NAME1);
        group1Json.add(PROSECUTING_AUTHORITY, PROSECURITY_AUTH1);

        final JsonObjectBuilder group2Json = Json.createObjectBuilder();
        group2Json.add(GROUP_ID, GROUP_ID2.toString());
        group2Json.add(GROUP_NAME, GROUP_NAME2);
        group2Json.add(PROSECUTING_AUTHORITY, PROSECURITY_AUTH2);

        final JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        arrayBuilder.add(group1Json);
        arrayBuilder.add(group2Json);

        final JsonObjectBuilder permissions = Json.createObjectBuilder();
        permissions.add(GROUPS, arrayBuilder.build());

        final Envelope envelope = Envelope.envelopeFrom(metadata, permissions.build());
        final List<Group> groups = groupsMapper.mapGroups(envelope);
        assertGroups(groups);
    }

    private void assertGroups(final List<Group> groups) {
        assertThat(groups.size(), is(2));

        final Group group1 = groups.get(0);
        assertThat(group1.getGroupId(), is(GROUP_ID1));
        assertThat(group1.getGroupName(), is(GROUP_NAME1));
        assertThat(group1.getProsecutingAuthority(), is(PROSECURITY_AUTH1));

        final Group group2 = groups.get(1);
        assertThat(group2.getGroupId(), is(GROUP_ID2));
        assertThat(group2.getGroupName(), is(GROUP_NAME2));
        assertThat(group2.getProsecutingAuthority(), is(PROSECURITY_AUTH2));
    }
}