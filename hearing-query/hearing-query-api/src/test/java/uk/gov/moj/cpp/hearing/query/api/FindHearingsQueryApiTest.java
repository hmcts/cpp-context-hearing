package uk.gov.moj.cpp.hearing.query.api;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;

import uk.gov.justice.hearing.courts.GetHearings;
import uk.gov.justice.services.core.dispatcher.EnvelopePayloadTypeConverter;
import uk.gov.justice.services.core.dispatcher.JsonEnvelopeRepacker;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.moj.cpp.hearing.query.api.service.accessfilter.AccessibleApplications;
import uk.gov.moj.cpp.hearing.query.api.service.accessfilter.AccessibleCases;
import uk.gov.moj.cpp.hearing.query.api.service.accessfilter.DDJChecker;
import uk.gov.moj.cpp.hearing.query.api.service.accessfilter.RecorderChecker;
import uk.gov.moj.cpp.hearing.query.api.service.accessfilter.UsersAndGroupsService;
import uk.gov.moj.cpp.hearing.query.api.service.accessfilter.vo.Permissions;
import uk.gov.moj.cpp.hearing.query.view.HearingQueryView;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import javax.ws.rs.BadRequestException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
public class FindHearingsQueryApiTest {

    @Mock
    private Metadata metadata;

    @Mock
    private AccessibleCases accessibleCases;

    @Mock
    private AccessibleApplications accessibleApplications;

    @Mock
    private Permissions permissions;

    @Mock
    private UsersAndGroupsService usersAndGroupsService;

    @Mock
    private DDJChecker ddjChecker;

    @Mock
    private RecorderChecker recorderChecker;

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

    @Test
    public void should_throw_bad_request_when_user_id_is_missing() {
        when(jsonInputEnvelope.metadata()).thenReturn(metadata);
        when(metadata.userId()).thenReturn(Optional.empty());
        assertThrows(BadRequestException.class, () -> hearingQueryApi.findHearing(jsonInputEnvelope));
    }

    @Test
    public void should_return_hearings_for_ddj() {
        final UUID userId = UUID.randomUUID();
        when(jsonInputEnvelope.metadata()).thenReturn(metadata);
        when(metadata.userId()).thenReturn(Optional.of(userId.toString()));
        when(usersAndGroupsService.permissions(userId.toString())).thenReturn(permissions);
        when(ddjChecker.isDDJ(permissions)).thenReturn(true);
        when(recorderChecker.isRecorder(permissions)).thenReturn(false);
        when(accessibleCases.findCases(permissions, userId.toString())).thenReturn(getAccessibleCaseList());
        when(accessibleApplications.findApplications(permissions, userId.toString())).thenReturn(new ArrayList<>());

        hearingQueryApi.findHearings(jsonInputEnvelope);
        verify(ddjChecker, times(1)).isDDJ(permissions);
        verify(accessibleCases, times(1)).findCases(permissions,userId.toString());
        verify(accessibleApplications, times(1)).findApplications(permissions,userId.toString());
        verify(usersAndGroupsService, times(1)).permissions(userId.toString());
    }

    @Test
    public void should_return_hearings_for_recorder() {
        final UUID userId = UUID.randomUUID();
        when(jsonInputEnvelope.metadata()).thenReturn(metadata);
        when(metadata.userId()).thenReturn(Optional.of(userId.toString()));
        when(usersAndGroupsService.permissions(userId.toString())).thenReturn(permissions);
        when(ddjChecker.isDDJ(permissions)).thenReturn(false);
        when(recorderChecker.isRecorder(permissions)).thenReturn(true);
        when(accessibleCases.findCases(permissions, userId.toString())).thenReturn(getAccessibleCaseList());
        when(accessibleApplications.findApplications(permissions, userId.toString())).thenReturn(new ArrayList<>());

        hearingQueryApi.findHearings(jsonInputEnvelope);
        verify(ddjChecker, times(1)).isDDJ(permissions);
        verify(accessibleCases, times(1)).findCases(permissions,userId.toString());
        verify(accessibleApplications, times(1)).findApplications(permissions,userId.toString());
        verify(usersAndGroupsService, times(1)).permissions(userId.toString());
    }

    @Test
    public void should_return_hearings_for_non_ddj() {
        final UUID userId = UUID.randomUUID();
        when(jsonInputEnvelope.metadata()).thenReturn(metadata);
        when(metadata.userId()).thenReturn(Optional.of(userId.toString()));
        when(usersAndGroupsService.permissions(userId.toString())).thenReturn(permissions);
        when(ddjChecker.isDDJ(permissions)).thenReturn(false);
        when(recorderChecker.isRecorder(permissions)).thenReturn(false);

        hearingQueryApi.findHearings(jsonInputEnvelope);
        verify(accessibleCases, times(0)).findCases(permissions,userId.toString());
        verify(accessibleApplications, times(0)).findApplications(permissions,userId.toString());
        verify(usersAndGroupsService, times(1)).permissions(userId.toString());
        verify(ddjChecker, times(1)).isDDJ(permissions);
    }

    private List<UUID> getAccessibleCaseList() {
        final List<UUID> accessibleCaseList = new ArrayList<>();
        accessibleCaseList.add(UUID.randomUUID());
        return accessibleCaseList;
    }

    private List<UUID> getAccessibleApplicationsList() {
        final List<UUID> accessibleApplicationsList = new ArrayList<>();
        accessibleApplicationsList.add(UUID.randomUUID());
        return accessibleApplicationsList;
    }

    private List<UUID> getAccessibleCasesAndApplicationsList() {
        final List<UUID> accessibleCasesAndApplicationsList = new ArrayList<>();
        accessibleCasesAndApplicationsList.addAll(getAccessibleApplicationsList());
        accessibleCasesAndApplicationsList.addAll(getAccessibleCaseList());
        return accessibleCasesAndApplicationsList;
    }
}
