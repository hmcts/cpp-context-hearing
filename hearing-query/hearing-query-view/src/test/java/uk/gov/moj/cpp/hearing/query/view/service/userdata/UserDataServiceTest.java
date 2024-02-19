package uk.gov.moj.cpp.hearing.query.view.service.userdata;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.json.JsonObject;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static java.util.UUID.randomUUID;
import static javax.json.Json.createReader;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.core.annotation.Component.QUERY_VIEW;
import static uk.gov.justice.services.messaging.Envelope.metadataBuilder;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;

@RunWith(MockitoJUnitRunner.class)
public class UserDataServiceTest {
    public static final String EMPTY_USER_DETAILS_JSON = "emptyUserDetails.json";
    public static final String MULTIPLE_USER_DETAILS_JSON = "multipleUserDetails.json";
    @Mock
    @ServiceComponent(QUERY_VIEW)
    private Requester requester;

    @InjectMocks
    private UserDataService userDataService;

    @Test
    public void shouldGetUserDetailsWithMultipleUserIds() {
        final String userId = "01ec3eea-2ba4-4263-8e89-51a56e966809,038b2147-27a4-4625-9f90-4945984c8bdf";
        List<String> expectedUsers = Arrays.asList(
                "directions management",
                "Marshall Douglas"
        );
        JsonEnvelope jsonEnvelope = getUserEnvelope(MULTIPLE_USER_DETAILS_JSON);
        when(requester.request(any(JsonEnvelope.class), any(Class.class))).thenReturn(jsonEnvelope);

        List<String> users = userDataService.getUserDetails(jsonEnvelope, userId);

        assertEquals(2, users.size());
        IntStream.range(0, expectedUsers.size())
                .forEach(i -> assertEquals(expectedUsers.get(i), users.get(i)));
    }

    @Test
    public void shouldNotCallExternalApiWhenInputIsNullOrEmpty() {
        JsonEnvelope jsonEnvelope = getUserEnvelope(EMPTY_USER_DETAILS_JSON);
        List<String> usersWhenEmptyInput = userDataService.getUserDetails(jsonEnvelope, "");
        List<String> usersWhenNullInput = userDataService.getUserDetails(jsonEnvelope, "");

        assertEquals(0, usersWhenEmptyInput.size());
        assertEquals(0, usersWhenNullInput.size());

        verify(requester, times(0)).requestAsAdmin(any(JsonEnvelope.class), eq(JsonObject.class));
        verifyNoMoreInteractions(requester);
    }

    @Test
    public void shouldGetUserDetailsWithWrongUserIds() {
        final String userId = randomUUID().toString();
        JsonEnvelope jsonEnvelope = getUserEnvelope(EMPTY_USER_DETAILS_JSON);
        when(requester.request(any(JsonEnvelope.class), any(Class.class))).thenReturn(jsonEnvelope);

        List<String> users = userDataService.getUserDetails(jsonEnvelope, userId);

        assertEquals(0, users.size());
    }

    private JsonEnvelope getUserEnvelope(String fileName) {
        return envelopeFrom(
                metadataBuilder().
                        withName("usersgroups.users").
                        withId(randomUUID()),
                createReader(getClass().getClassLoader().
                        getResourceAsStream(fileName)).
                        readObject()
        );
    }
}