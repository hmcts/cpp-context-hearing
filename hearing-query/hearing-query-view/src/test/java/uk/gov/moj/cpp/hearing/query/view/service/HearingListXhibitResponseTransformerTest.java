package uk.gov.moj.cpp.hearing.query.view.service;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import uk.gov.justice.core.courts.CourtCentre;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingEvent;
import uk.gov.moj.cpp.hearing.query.view.referencedata.XhibitCourtRoomMapperCache;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CurrentCourtStatus;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HearingListXhibitResponseTransformerTest {
    private static final String COURT_NAME = "Court 1";

    @Mock
    private XhibitCourtRoomMapperCache xhibitCourtRoomMapperCache;

    @Mock
    private HearingEventsToHearingMapper hearingEventsToHearingMapper;

    @Mock
    private Hearing hearing;

    @InjectMocks
    private HearingListXhibitResponseTransformer hearingListXhibitResponseTransformer;

    @Test
    public void shouldTransformFrom() {
        final UUID courtCentreId = randomUUID();
        final UUID courtRoomId = randomUUID();
        final UUID hearingId = randomUUID();
        final HearingEvent hearingEvent = HearingEvent.hearingEvent().build();
        final List<Hearing> hearingList = Arrays.asList(hearing);
        when(hearing.getId()).thenReturn(hearingId);
        when(hearing.getCourtCentre()).thenReturn(CourtCentre.courtCentre().withName(COURT_NAME).withRoomId(courtRoomId).withId(courtCentreId).build());
        when(hearingEventsToHearingMapper.getHearingList()).thenReturn(hearingList);
        when(hearingEventsToHearingMapper.getHearingEventBy(hearingId)).thenReturn(Optional.of(hearingEvent));
        when(xhibitCourtRoomMapperCache.getXhibitCourtRoomName(courtCentreId, courtRoomId)).thenReturn("x");
        final CurrentCourtStatus currentCourtStatus = hearingListXhibitResponseTransformer.transformFrom(hearingEventsToHearingMapper);
        assertThat(currentCourtStatus.getCourt().getCourtName(), is(COURT_NAME));
        assertThat(currentCourtStatus.getCourt().getCourtSites().get(0).getCourtRooms().get(0).getCourtRoomName(), is("x"));
        assertThat(currentCourtStatus.getCourt().getCourtSites().size(), is(1));
    }
}