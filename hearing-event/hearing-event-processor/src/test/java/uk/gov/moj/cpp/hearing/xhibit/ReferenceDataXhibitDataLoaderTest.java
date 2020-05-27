package uk.gov.moj.cpp.hearing.xhibit;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.external.domain.referencedata.CourtRoomMapping;
import uk.gov.moj.cpp.external.domain.referencedata.CourtRoomMappingsList;
import uk.gov.moj.cpp.external.domain.referencedata.XhibitEventMapping;
import uk.gov.moj.cpp.external.domain.referencedata.XhibitEventMappingsList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.json.JsonArray;
import javax.json.JsonObject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ReferenceDataXhibitDataLoaderTest {
    private static final String CREST_COURT_SITE_CODE = "123";
    private static final UUID EVENT_ID = randomUUID();
    private static final UUID COURT_ROOM_ID = randomUUID();
    private static final String CREST_COURT_SITE_NAME = "Wrexham";
    private static final String OU_CODE = "B60OW00";
    private static final int COURT_ROOM_NUMBER = 321;
    private static final String CREST_COURT_ID = "K12";
    private static final String CREST_COURT_SITE_ID = "CCS1";
    private static final String CREST_COURT_ROOM_NAME = "WATERLOO";
    private static final UUID CREST_COURT_SITE_UUID = randomUUID();

    @Mock(answer = RETURNS_DEEP_STUBS)
    private Requester requester;

    @Spy
    private UtcClock utcClock;

    @InjectMocks
    private ReferenceDataXhibitDataLoader referenceDataXhibitDataLoader;



    @Test
    public void shouldLoadXhibitCourtCentreCodeByCourtCentreId() {
        final XhibitEventMappingsList courtCentreCourtList = getExhibitEventMappingsList();

        when(requester.requestAsAdmin(any(JsonEnvelope.class), eq(XhibitEventMappingsList.class)).payload()).thenReturn(courtCentreCourtList);

        final XhibitEventMappingsList xhibitEventMappingsList = referenceDataXhibitDataLoader.getEventMapping();

        assertThat(xhibitEventMappingsList.getCpXhibitHearingEventMappings().get(0).getCpHearingEventId(), is(EVENT_ID));
        assertThat(xhibitEventMappingsList.getCpXhibitHearingEventMappings().get(0).getXhibitHearingEventCode(), is(CREST_COURT_SITE_CODE));
    }

    private CourtRoomMappingsList getCourtRoomMappingsList() {
        return new CourtRoomMappingsList(Collections.singletonList(new CourtRoomMapping(randomUUID(), COURT_ROOM_ID, CREST_COURT_SITE_NAME, OU_CODE, COURT_ROOM_NUMBER, CREST_COURT_ID, CREST_COURT_SITE_ID, CREST_COURT_SITE_CODE, CREST_COURT_ROOM_NAME, CREST_COURT_SITE_UUID)));
    }

    private XhibitEventMappingsList getExhibitEventMappingsList() {
        return new XhibitEventMappingsList(asList(new XhibitEventMapping(EVENT_ID, CREST_COURT_SITE_CODE, "", "", ""), new XhibitEventMapping(randomUUID(), "sdfsdf", "", "", "")));
    }

    private List<JsonObject> crownCounts() {

        final JsonArray jsonArrayOrganisationUnits = createArrayBuilder().add(createObjectBuilder()
                .add("id", randomUUID().toString())
                .add("oucode", "B60OW00")
                .add("oucodeL1Code", "B")
                .add("oucodeL1Name", "Magistrates' Courts")
                .add("oucodeL3Name", "Wrexham Magistrates' Court")
                .add("oucodeL3WelshName", "Llys Ynadon Wrecsam")
                .add("address1", "The Law Courts")
                .add("address2", "Bodhyfryd")
                .add("address3", "Wrexham")
                .add("address4", "Address4")
                .add("address5", "Address5")
                .add("postcode", "LL12 7BP")
                .add("welshAddress1", "Y Llysoedd Barn")
                .add("welshAddress2", "Wrecsam")
                .add("welshAddress3", "Wrecsam")
                .add("welshAddress4", "0")
                .add("welshAddress5", "0")
                .add("defaultStartTime", "10:00")
                .add("defaultDurationHrs", "7").build()).build();

        return convertToList(jsonArrayOrganisationUnits);
    }

    private List<JsonObject> convertToList(final JsonArray crownCourtsArray) {
        final List<JsonObject> crownCourts = new ArrayList<>();
        for (int i = 0; i < crownCourtsArray.size(); i++) {
            crownCourts.add((JsonObject) crownCourtsArray.get(i));
        }
        return crownCourts;
    }
}
