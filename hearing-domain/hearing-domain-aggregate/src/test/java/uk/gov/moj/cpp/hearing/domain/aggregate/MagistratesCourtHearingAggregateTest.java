package uk.gov.moj.cpp.hearing.domain.aggregate;

import org.apache.commons.lang3.SerializationException;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.Defendant;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.Hearing;
import uk.gov.moj.cpp.hearing.domain.event.MagsCourtHearingRecorded;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.fail;

@RunWith(MockitoJUnitRunner.class)
public class MagistratesCourtHearingAggregateTest {


    private static Hearing originatingHearing;
    private final static LocalDate CONVICTION_DATE = LocalDate.now();
    private final static UUID HEARING_ID = UUID.randomUUID();
    private final static UUID CASE_ID = UUID.randomUUID();
    private final static String CASE_URN = "XYZ";
    private final static String COURT_CENTRE_ID = "1";
    private final static String COURT_CENTRE_NAME = "Liverpool";
    private final static LocalDate SENDING_COMMITTAL_DATE = LocalDate.now();
    private final static String TYPE = "Magistrate Court Hearing";


    @InjectMocks
    private MagistratesCourtHearingAggregate magistratesCourtHearingAggregate;

    @After
    public void teardown() {
        try {
            // ensure aggregate is serializable
            SerializationUtils.serialize(magistratesCourtHearingAggregate);
        } catch (SerializationException e) {
            fail("Aggregate should be serializable");
        }
    }

    @Before
    public void setUp() {
        final List<Defendant> defendants = new ArrayList<>();
        originatingHearing = new Hearing(CASE_ID, CASE_URN, COURT_CENTRE_ID, COURT_CENTRE_NAME, defendants, SENDING_COMMITTAL_DATE, TYPE);
    }

    @Test
    public void testMagsCourtHearingRecorded() throws Exception {
        final Stream<Object> events = magistratesCourtHearingAggregate.initiate(getMagsCourtHearingRecorded());
        final List<Object> lEvents = events.collect(Collectors.toList());


        Object event = lEvents.get(0);
        Assert.assertEquals(1, lEvents.size());
        Assert.assertEquals(event.getClass(), MagsCourtHearingRecorded.class);

        final MagsCourtHearingRecorded magsCourtHearingRecorded = (MagsCourtHearingRecorded) event;
        Assert.assertEquals(CONVICTION_DATE, magsCourtHearingRecorded.getConvictionDate());
        Assert.assertEquals(HEARING_ID, magsCourtHearingRecorded.getHearingId());
        Assert.assertEquals(CASE_ID, magsCourtHearingRecorded.getOriginatingHearing().getCaseId());
        Assert.assertEquals(CASE_URN, magsCourtHearingRecorded.getOriginatingHearing().getCaseUrn());
        Assert.assertEquals(COURT_CENTRE_ID, magsCourtHearingRecorded.getOriginatingHearing().getCourtCentreId());
        Assert.assertEquals(COURT_CENTRE_NAME, magsCourtHearingRecorded.getOriginatingHearing().getCourtCentreName());
        Assert.assertEquals(SENDING_COMMITTAL_DATE, magsCourtHearingRecorded.getOriginatingHearing().getSendingCommittalDate());
        Assert.assertEquals(TYPE, magsCourtHearingRecorded.getOriginatingHearing().getType());

    }


    private MagsCourtHearingRecorded getMagsCourtHearingRecorded() {
        return new MagsCourtHearingRecorded(originatingHearing, CONVICTION_DATE, HEARING_ID);
    }


}