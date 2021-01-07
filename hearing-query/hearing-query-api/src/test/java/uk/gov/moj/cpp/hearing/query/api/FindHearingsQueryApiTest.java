package uk.gov.moj.cpp.hearing.query.api;

import static javax.json.Json.createObjectBuilder;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;

import uk.gov.justice.hearing.courts.GetHearings;
import uk.gov.justice.services.core.dispatcher.EnvelopePayloadTypeConverter;
import uk.gov.justice.services.core.dispatcher.JsonEnvelopeRepacker;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.moj.cpp.hearing.query.api.service.accessfilter.AccessibleCases;
import uk.gov.moj.cpp.hearing.query.api.service.accessfilter.DDJChecker;
import uk.gov.moj.cpp.hearing.query.api.service.accessfilter.UsersAndGroupsService;
import uk.gov.moj.cpp.hearing.query.api.service.accessfilter.vo.Permissions;
import uk.gov.moj.cpp.hearing.query.view.HearingEventQueryView;
import uk.gov.moj.cpp.hearing.query.view.HearingQueryView;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.BadRequestException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class FindHearingsQueryApiTest {

    @Mock
    private List<UUID> accessibleCaseList;

    @Mock
    private Metadata metadata;

    @Mock
    private AccessibleCases accessibleCases;

    @Mock
    private Permissions permissions;

    @Mock
    private UsersAndGroupsService usersAndGroupsService;

    @Mock
    private DDJChecker ddjChecker;

    @Mock
    private JsonEnvelope jsonInputEnvelope;

    @Mock
    private Envelope<GetHearings> jsonOutputEnvelope;

    @Spy
    private Enveloper enveloper = createEnveloper();

    @Mock
    private Function<Object, JsonEnvelope> function;

    @Mock
    private JsonEnvelopeRepacker jsonEnvelopeRepacker;

    @Mock
    private EnvelopePayloadTypeConverter envelopePayloadTypeConverter;

    @Mock
    private HearingQueryView hearingQueryView;

    @InjectMocks
    private HearingQueryApi hearingQueryApi;

    @Test(expected = BadRequestException.class)
    public void should_throw_bad_request_when_user_id_is_missing() {
        when(jsonInputEnvelope.metadata()).thenReturn(metadata);
        when(metadata.userId()).thenReturn(Optional.empty());
        hearingQueryApi.findHearings(jsonInputEnvelope);
    }

    @Test
    public void should_return_hearings_for_ddj() {
        final UUID userId = UUID.randomUUID();
        when(jsonInputEnvelope.metadata()).thenReturn(metadata);
        when(metadata.userId()).thenReturn(Optional.of(userId.toString()));
        when(usersAndGroupsService.permissions(userId.toString())).thenReturn(permissions);
        when(ddjChecker.isDDJ(userId.toString())).thenReturn(true);
        when(accessibleCases.findCases(permissions, userId.toString())).thenReturn(accessibleCaseList);
        when(hearingQueryView.findHearings(jsonInputEnvelope, accessibleCaseList, false))
                .thenReturn(jsonOutputEnvelope);

        hearingQueryApi.findHearings(jsonInputEnvelope);
        verify(ddjChecker, times(1)).isDDJ(userId.toString());
        verify(accessibleCases, times(1)).findCases(permissions,userId.toString());
        verify(usersAndGroupsService, times(1)).permissions(userId.toString());
    }

    @Test
    public void should_return_hearings_for_non_ddj() {
        final UUID userId = UUID.randomUUID();
        when(jsonInputEnvelope.metadata()).thenReturn(metadata);
        when(metadata.userId()).thenReturn(Optional.of(userId.toString()));
        when(usersAndGroupsService.permissions(userId.toString())).thenReturn(permissions);
        when(accessibleCases.findCases(permissions, userId.toString())).thenReturn(accessibleCaseList);
        when(ddjChecker.isDDJ(userId.toString())).thenReturn(false);
        when(hearingQueryView.findHearings(jsonInputEnvelope, accessibleCaseList, false))
                .thenReturn(jsonOutputEnvelope);

        hearingQueryApi.findHearings(jsonInputEnvelope);
        verify(accessibleCases, times(0)).findCases(permissions,userId.toString());
        verify(usersAndGroupsService, times(0)).permissions(userId.toString());
        verify(ddjChecker, times(1)).isDDJ(userId.toString());
    }
}
