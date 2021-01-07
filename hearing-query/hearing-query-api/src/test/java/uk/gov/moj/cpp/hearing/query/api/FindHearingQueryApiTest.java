package uk.gov.moj.cpp.hearing.query.api;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.core.dispatcher.EnvelopePayloadTypeConverter;
import uk.gov.justice.services.core.dispatcher.JsonEnvelopeRepacker;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.CrackedIneffectiveVacatedTrialType;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.CrackedIneffectiveVacatedTrialTypes;
import uk.gov.moj.cpp.hearing.query.api.service.accessfilter.AccessibleCases;
import uk.gov.moj.cpp.hearing.query.api.service.accessfilter.DDJChecker;
import uk.gov.moj.cpp.hearing.query.api.service.accessfilter.UsersAndGroupsService;
import uk.gov.moj.cpp.hearing.query.api.service.accessfilter.vo.Permissions;
import uk.gov.moj.cpp.hearing.query.api.service.referencedata.ReferenceDataService;
import uk.gov.moj.cpp.hearing.query.view.HearingQueryView;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;

import javax.ws.rs.BadRequestException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;


@RunWith(MockitoJUnitRunner.class)
public class FindHearingQueryApiTest {

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
    private Envelope<HearingDetailsResponse> jsonOutputEnvelope;

    @Mock
    private ReferenceDataService referenceDataService;

    @Mock
    private Function<Object, JsonEnvelope> function;

    @Mock
    private JsonEnvelopeRepacker jsonEnvelopeRepacker;

    @Mock
    private EnvelopePayloadTypeConverter envelopePayloadTypeConverter;

    @Spy
    private Enveloper enveloper = createEnveloper();


    @Mock
    private HearingQueryView hearingQueryView;

    @InjectMocks
    private HearingQueryApi hearingQueryApi;

    @Test(expected = BadRequestException.class)
    public void should_throw_bad_request_when_user_id_is_missing() {
        when(jsonInputEnvelope.metadata()).thenReturn(metadata);
        when(metadata.userId()).thenReturn(Optional.empty());
        hearingQueryApi.findHearing(jsonInputEnvelope);
    }

    @Test
    public void should_return_hearing_for_ddj() {
        final UUID userId = UUID.randomUUID();
        final CrackedIneffectiveVacatedTrialTypes crackedIneffectiveVacatedTrialTypes = getCrackedIneffectiveVacatedTrialTypes();
        when(referenceDataService.listAllCrackedIneffectiveVacatedTrialTypes()).thenReturn(crackedIneffectiveVacatedTrialTypes);
        when(jsonInputEnvelope.metadata()).thenReturn(metadata);
        when(metadata.userId()).thenReturn(Optional.of(userId.toString()));
        when(usersAndGroupsService.permissions(userId.toString())).thenReturn(permissions);
        when(ddjChecker.isDDJ(userId.toString())).thenReturn(true);
        when(accessibleCases.findCases(permissions, userId.toString())).thenReturn(accessibleCaseList);
        when(hearingQueryView.findHearing(jsonInputEnvelope, getCrackedIneffectiveVacatedTrialTypes(), accessibleCaseList, true))
                .thenReturn(jsonOutputEnvelope);

        hearingQueryApi.findHearing(jsonInputEnvelope);
        verify(ddjChecker, times(1)).isDDJ(userId.toString());
        verify(accessibleCases, times(1)).findCases(permissions,userId.toString());
        verify(usersAndGroupsService, times(1)).permissions(userId.toString());
    }

    @Test
    public void should_return_hearing_for_non_ddj() {
        final UUID userId = UUID.randomUUID();
        final CrackedIneffectiveVacatedTrialTypes crackedIneffectiveVacatedTrialTypes = getCrackedIneffectiveVacatedTrialTypes();
        when(referenceDataService.listAllCrackedIneffectiveVacatedTrialTypes()).thenReturn(crackedIneffectiveVacatedTrialTypes);
        when(jsonInputEnvelope.metadata()).thenReturn(metadata);
        when(metadata.userId()).thenReturn(Optional.of(userId.toString()));
        when(usersAndGroupsService.permissions(userId.toString())).thenReturn(permissions);
        when(ddjChecker.isDDJ(userId.toString())).thenReturn(false);
        when(accessibleCases.findCases(permissions, userId.toString())).thenReturn(accessibleCaseList);
        when(hearingQueryView.findHearing(jsonInputEnvelope, getCrackedIneffectiveVacatedTrialTypes(), accessibleCaseList, false))
                .thenReturn(jsonOutputEnvelope);

        hearingQueryApi.findHearing(jsonInputEnvelope);
        verify(ddjChecker, times(1)).isDDJ(userId.toString());
        verify(accessibleCases, times(0)).findCases(permissions,userId.toString());
        verify(usersAndGroupsService, times(0)).permissions(userId.toString());
    }

    private CrackedIneffectiveVacatedTrialTypes getCrackedIneffectiveVacatedTrialTypes() {
        final CrackedIneffectiveVacatedTrialType crackedIneffectiveVacatedTrialType = new CrackedIneffectiveVacatedTrialType(randomUUID(), "", "", "", LocalDate.now());

        final List<CrackedIneffectiveVacatedTrialType> crackedIneffectiveVacatedTrialTypes = new ArrayList();
        crackedIneffectiveVacatedTrialTypes.add(crackedIneffectiveVacatedTrialType);

        final CrackedIneffectiveVacatedTrialTypes crackedIneffectiveVacatedTrialTypes1 = new CrackedIneffectiveVacatedTrialTypes();
        crackedIneffectiveVacatedTrialTypes1.setCrackedIneffectiveVacatedTrialTypes(crackedIneffectiveVacatedTrialTypes);
        return crackedIneffectiveVacatedTrialTypes1;
    }


}
