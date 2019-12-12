package uk.gov.moj.cpp.hearing.event.service;

import static java.lang.Integer.parseInt;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;

import uk.gov.justice.hearing.courts.referencedata.CourtCentreOrganisationUnit;
import uk.gov.justice.hearing.courts.referencedata.Courtrooms;
import uk.gov.justice.hearing.courts.referencedata.OuCourtRoomsResult;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.Optional;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CourtHouseReverseLookupTest extends ReferenceDataClientTestBase {

    public static final String ORG_UNIT = randomUUID().toString();
    @InjectMocks
    private CourtHouseReverseLookup courtHouseReverseLookup;

    private final UUID ID = randomUUID();
    private final int COURT_ROOM_ID = 54321;
    private final String courtRoomName = "abcdefGH";


    final Courtrooms expectedCourtRoomResult = Courtrooms.courtrooms()
            .withId(ID)
            .withCourtroomId(COURT_ROOM_ID)
            .withCourtroomName(courtRoomName)
            .build();
    final Courtrooms greenRoom = Courtrooms.courtrooms()
            .withId(randomUUID())
            .withCourtroomId(12)
            .withCourtroomName("Green")
            .build();
    final CourtCentreOrganisationUnit expectedCourtHouseByNameResult = CourtCentreOrganisationUnit.courtCentreOrganisationUnit()
            .withId(ORG_UNIT)
            .withOucodeL3Name("abCdEFG")
            .withCourtrooms(
                    asList(greenRoom, expectedCourtRoomResult)
            )
            .build();

    final CourtCentreOrganisationUnit birminghamCourtHouse = CourtCentreOrganisationUnit.courtCentreOrganisationUnit()
            .withId(randomUUID().toString())
            .withOucodeL3Name("bir")
            .withCourtrooms(
                    asList(greenRoom)
            )
            .build();

    @Test
    public void courtHouseByName() {
        final OuCourtRoomsResult queryresult = OuCourtRoomsResult.ouCourtRoomsResult()
                .withOrganisationunits(
                        asList(birminghamCourtHouse, expectedCourtHouseByNameResult)
                )
                .build();

        final JsonEnvelope requestEnvelope =  envelopeFrom(
                metadataWithRandomUUID(CourtHouseReverseLookup.GET_COURT_HOUSES),
                createObjectBuilder()
                        .add("name", "abcdEFg")
                        .build());
        final JsonEnvelope resultEnvelope = envelopeFrom(metadataWithRandomUUID("something"), objectToJsonObjectConverter.convert(queryresult));

        when(requester.requestAsAdmin(any())).thenReturn(resultEnvelope);

        Optional<CourtCentreOrganisationUnit> result = courtHouseReverseLookup.getCourtCentreByName(requestEnvelope, "abcdEFg");

        Assert.assertEquals(expectedCourtHouseByNameResult.getId(), result.get().getId());

    }

    @Test
    public void courtRoomByName() {
        Optional<Courtrooms> result = courtHouseReverseLookup.getCourtRoomByRoomName(expectedCourtHouseByNameResult, "abCdefGh");
        Assert.assertEquals(result.get().getCourtroomId(), expectedCourtRoomResult.getCourtroomId());

    }

    @Test
    public void getCourtCentreById() {

        final OuCourtRoomsResult queryresult = OuCourtRoomsResult.ouCourtRoomsResult()
                .withOrganisationunits(
                        asList(expectedCourtHouseByNameResult)
                )
                .build();

        final JsonEnvelope requestEnvelope =  envelopeFrom(
                metadataWithRandomUUID(CourtHouseReverseLookup.GET_COURT_HOUSES),
                createObjectBuilder()
                        .add("id", COURT_ROOM_ID)
                        .build());
        final JsonEnvelope resultEnvelope = envelopeFrom(metadataWithRandomUUID("something"), objectToJsonObjectConverter.convert(queryresult));

        when(requester.requestAsAdmin(any())).thenReturn(resultEnvelope);

        Optional<CourtCentreOrganisationUnit> result = courtHouseReverseLookup.getCourtCentreById(requestEnvelope, UUID.fromString(ORG_UNIT));

        Assert.assertEquals(expectedCourtHouseByNameResult.getId(), result.get().getId());
    }
}
