package uk.gov.moj.cpp.hearing.query.api.service;

import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.MetadataBuilder;
import uk.gov.moj.cpp.hearing.query.api.service.usergroups.UserGroupQueryService;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UserGroupQueryServiceTest {
    @Mock
    private JsonEnvelope jsonEnvelope;

    @Mock
    private Requester requester;

    @InjectMocks
    private UserGroupQueryService userGroupQueryService;

    private UUID userId = UUID.randomUUID();

    @Mock
    private JsonEnvelope responseJsonEnvelope;


    @Test
    public void shouldReturnFalseWhenQueryResponseIsNull() {

        when(requester.request(any(), any(Class.class))).thenReturn(buildEmptyOrganisationJsonEnvelope());
        assertThat(userGroupQueryService.doesUserBelongsToHmctsOrganisation(userId), is(false));
    }

    @Test
    public void shouldReturnTrueWhenResponseWithMatchingOrgId() {

        when(requester.request(any(), any(Class.class))).thenReturn(buildNoMatchingOrganisationJsonEnvelope());
        assertThat(userGroupQueryService.doesUserBelongsToHmctsOrganisation(userId), is(false));
    }

    @Test
    public void shouldReturnFalseWhenResponseWithNoMatchingOrgId() {
        when(requester.request(any(), any(Class.class))).thenReturn(buildOrganisationJsonEnvelope());
        assertThat(userGroupQueryService.doesUserBelongsToHmctsOrganisation(userId), is(true));
    }

    public static JsonEnvelope buildOrganisationJsonEnvelope() {
        return JsonEnvelope.envelopeFrom(
                JsonEnvelope.metadataBuilder().withId(randomUUID()).withName("usersgroups.get-organisation-details").build(),
                createObjectBuilder().add("organisationId", "b2d57737-6163-4bb9-88cb-97b45090d29d")
                        .add("organisationType", "HMCTS").build());
    }

    public static JsonEnvelope buildNoMatchingOrganisationJsonEnvelope() {
        return JsonEnvelope.envelopeFrom(
                JsonEnvelope.metadataBuilder().withId(randomUUID()).withName("usersgroups.get-organisation-details").build(),
                createObjectBuilder().add("organisationId", "b2d57737-6163-4bb9-88cb-97b45090d29d")
                        .add("organisationType", "NONHMCTS").build());
    }

    public static JsonEnvelope buildEmptyOrganisationJsonEnvelope() {
        return JsonEnvelope.envelopeFrom(
                JsonEnvelope.metadataBuilder().withId(randomUUID()).withName("usersgroups.get-organisation-details").build(),
                createObjectBuilder().add("organisationId", "b2d57737-6163-4bb9-88cb-97b45090d29d").build());
    }
    private MetadataBuilder getMetadataBuilder(final UUID userId) {
        return JsonEnvelope.metadataBuilder()
                .withId(randomUUID())
                .withName("usersgroups.get-logged-in-user-details")
                .withCausation(randomUUID())
                .withClientCorrelationId(randomUUID().toString())
                .withStreamId(randomUUID())
                .withUserId(userId.toString());
    }

    public static JsonEnvelope buildJsonEnvelope() {
        return JsonEnvelope.envelopeFrom(
                JsonEnvelope.metadataBuilder().withId(randomUUID()).withName("usersgroups.get-logged-in-user-details").build(),
                createObjectBuilder().add("userId", "a085e359-6069-4694-8820-7810e7dfe762").build());
    }

    public static JsonEnvelope buildNoOrgJsonEnvelope() {
        return JsonEnvelope.envelopeFrom(
                JsonEnvelope.metadataBuilder().withId(randomUUID()).withName("usersgroups.get-logged-in-user-details").build(),
                createObjectBuilder().add("userId", "a085e359-6069-4694-8820-7810e7dfe762")
                        .add("organisationId", "2471dfe8-8aa5-47f7-bb76-275b83fc312d").build());
    }
}