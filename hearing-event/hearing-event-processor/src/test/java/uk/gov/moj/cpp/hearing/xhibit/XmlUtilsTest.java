package uk.gov.moj.cpp.hearing.xhibit;

import static org.junit.Assert.assertTrue;

import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.Court;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.Courtroom;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.Courtrooms;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.Courtsite;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.Courtsites;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.Currentcourtstatus;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.Datetimestamp;

import org.junit.Test;

public class XmlUtilsTest {

    private static final uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.ObjectFactory publicDisplayObjectFactory = new uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.ObjectFactory();


    @Test
    public void shouldGenerateValidXml() {

        final Currentcourtstatus currentCourtStatus = getCurrentCourtStatus();

        final XmlUtils xmlUtils = new XmlUtils();
        final String publicDisplay = xmlUtils.createPublicDisplay(currentCourtStatus);

        assertTrue(!publicDisplay.isEmpty());
    }

    private Currentcourtstatus getCurrentCourtStatus() {
        final Currentcourtstatus currentcourtstatus = publicDisplayObjectFactory.createCurrentcourtstatus();
        final Court court = publicDisplayObjectFactory.createCourt();
        final Courtsites courtsites = publicDisplayObjectFactory.createCourtsites();
        final Courtsite courtsite = publicDisplayObjectFactory.createCourtsite();
        final Courtrooms courtrooms = publicDisplayObjectFactory.createCourtrooms();
        final Courtroom courtroom = publicDisplayObjectFactory.createCourtroom();
        final Datetimestamp datetimestamp = publicDisplayObjectFactory.createDatetimestamp();

        datetimestamp.setMin("1");
        datetimestamp.setHour("1");
        datetimestamp.setYear("2019");
        datetimestamp.setMonth("2");
        datetimestamp.setDayofweek("1");
        datetimestamp.setDate("123");

        courtroom.setCourtroomname("courtRoomName");
        courtrooms.getCourtroom().add(courtroom);

        courtsite.setCourtsitename("courtSiteName");
        courtsite.setCourtrooms(courtrooms);

        courtsites.getCourtsite().add(courtsite);

        court.setCourtname("testCourtName");
        court.setCourtsites(courtsites);

        currentcourtstatus.setCourt(court);
        currentcourtstatus.setPagename("hello");

        currentcourtstatus.setDatetimestamp(datetimestamp);

        return currentcourtstatus;
    }
}