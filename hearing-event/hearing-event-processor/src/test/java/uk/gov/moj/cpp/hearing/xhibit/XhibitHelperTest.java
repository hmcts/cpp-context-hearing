package uk.gov.moj.cpp.hearing.xhibit;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import uk.gov.moj.cpp.listing.common.xhibit.CommonXhibitReferenceDataService;
import uk.gov.moj.cpp.listing.common.xhibit.exception.InvalidReferenceDataException;
import uk.gov.moj.cpp.listing.domain.xhibit.CourtLocation;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class XhibitHelperTest {
    @Mock
    private CommonXhibitReferenceDataService commonXhibitReferenceDataService;

    @InjectMocks
    private XhibitHelper xhibitHelper;

    private String courtCentreId;
    private CourtLocation courtLocation;

    @Before
    public void setup() {
        courtCentreId = randomUUID().toString();
        courtLocation = new CourtLocation("ouCode",
                "12345",
                "CrestCourtSiteId",
                "CourtName",
                "CourtShortName",
                "CourtSiteName",
                "CourtSiteCode",
                "CourtType");

        when(commonXhibitReferenceDataService.getCrownCourtDetails(any())).thenReturn(courtLocation);
    }

    @Test
    public void shouldGetCrestCourtIdFromCrownCourtCache() {
        final String crestCourtId = xhibitHelper.getCrestCourtId(courtCentreId);
        assertThat(crestCourtId, is("12345"));
    }

    @Test
    public void shouldGetCrestCourtIdFromMagsCourtCacheWhenGetCrownCourtCacheThrowsAnException() {
        when(commonXhibitReferenceDataService.getCrownCourtDetails(any())).thenThrow(InvalidReferenceDataException.class);
        when(commonXhibitReferenceDataService.getMagsCourtDetails(any())).thenReturn(courtLocation);

        final String crestCourtId = xhibitHelper.getCrestCourtId(courtCentreId);
        assertThat(crestCourtId, is("12345"));
    }

    @Test(expected = InvalidReferenceDataException.class)
    public void shouldThrowInvalidReferenceDataExceptionWhenCourtCenterIdIsNotAvailableInBothCourts() {
        when(commonXhibitReferenceDataService.getCrownCourtDetails(any())).thenThrow(InvalidReferenceDataException.class);
        when(commonXhibitReferenceDataService.getMagsCourtDetails(any())).thenThrow(InvalidReferenceDataException.class);

        final String crestCourtId = xhibitHelper.getCrestCourtId(courtCentreId);
        assertThat(crestCourtId, is("12345"));
    }
}
