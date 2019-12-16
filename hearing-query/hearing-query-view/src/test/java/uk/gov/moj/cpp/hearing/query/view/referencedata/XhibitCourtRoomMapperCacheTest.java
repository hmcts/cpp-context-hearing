package uk.gov.moj.cpp.hearing.query.view.referencedata;

import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import uk.gov.moj.cpp.external.domain.referencedata.CourtRoomMappings;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class XhibitCourtRoomMapperCacheTest {

    @Mock
    private ReferenceDataCourtRoomService referenceDataCourtRoomService;

    @InjectMocks
    private XhibitCourtRoomMapperCache xhibitCourtRoomMapperCache;

    @Test
    public void shouldCacheCourtRoom() {
        final CourtRoomMappings courtRoomMappings = mock(CourtRoomMappings.class);
        final UUID courtCentreId = randomUUID();
        final UUID courtRoomId = randomUUID();
        final String courtRoomName = "testCourtRoomName";

        when(courtRoomMappings.getCrestCourtRoomName()).thenReturn(courtRoomName);
        when(referenceDataCourtRoomService.getCourtRoomNameBy(courtCentreId, courtRoomId)).thenReturn(courtRoomMappings);

        final String xhibitCourtRoomName = xhibitCourtRoomMapperCache.getXhibitCourtRoomName(courtCentreId, courtRoomId);

        assertThat(xhibitCourtRoomName, is(courtRoomName));
    }
}