package uk.gov.moj.cpp.hearing.event.service;

import static java.util.Arrays.asList;

import uk.gov.justice.hearing.courts.referencedata.CourtCentreOrganisationUnit;
import uk.gov.justice.hearing.courts.referencedata.Courtrooms;
import uk.gov.justice.hearing.courts.referencedata.OuCourtRoomsResult;

import java.util.Optional;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CourtHouseReverseLookupTest extends ReferenceDataClientTestBase {

    @InjectMocks
    private CourtHouseReverseLookup target;

    private final int COURT_ROOM_ID = 54321;
    private final String courtRoomName = "abcdefGH";

    final Courtrooms expectedCourtRoomResult = Courtrooms.courtrooms()
            .withCourtroomId(COURT_ROOM_ID)
            .withCourtroomName(courtRoomName)
            .build();
    final CourtCentreOrganisationUnit expectedCourtHouseByNameResult = CourtCentreOrganisationUnit.courtCentreOrganisationUnit()
            .withId(UUID.randomUUID().toString())
            .withOucodeL3Name("abCdEFG")
            .withCourtrooms(
                    asList(expectedCourtRoomResult)
            )
            .build();

    @Test
    public void courtHouseByName() {
        final OuCourtRoomsResult queryresult = OuCourtRoomsResult.ouCourtRoomsResult()
                .withOrganisationunits(
                        asList(expectedCourtHouseByNameResult)
                )
                .build();

        mockQuery(CourtHouseReverseLookup.GET_COURT_HOUSES, queryresult, false);

        Optional<CourtCentreOrganisationUnit> result = target.getCourtCentreByName(context, "abcdEFg");

        Assert.assertEquals(expectedCourtHouseByNameResult.getId(), result.get().getId());

    }

    @Test
    public void courtRoomByName() {
        Optional<Courtrooms> result = target.getCourtRoomByRoomName(expectedCourtHouseByNameResult, "abCdefGh");
        Assert.assertEquals(result.get().getCourtroomId(), expectedCourtRoomResult.getCourtroomId());


    }

}
