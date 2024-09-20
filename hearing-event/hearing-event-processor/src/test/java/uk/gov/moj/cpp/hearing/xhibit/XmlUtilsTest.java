package uk.gov.moj.cpp.hearing.xhibit;

import static java.math.BigInteger.valueOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.moj.cpp.hearing.domain.xhibit.generated.iwp.DaysOfWeekType.FRIDAY;
import static uk.gov.moj.cpp.hearing.domain.xhibit.generated.iwp.MonthsOfYearType.APRIL;

import uk.gov.moj.cpp.hearing.domain.xhibit.generated.iwp.Court;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.iwp.Courtroom;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.iwp.Courtrooms;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.iwp.Courtsite;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.iwp.Courtsites;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.iwp.Currentcourtstatus;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.iwp.Datetimestamp;

import org.junit.jupiter.api.Test;

public class XmlUtilsTest {

    private static final uk.gov.moj.cpp.hearing.domain.xhibit.generated.iwp.ObjectFactory webPageObjectFactory = new uk.gov.moj.cpp.hearing.domain.xhibit.generated.iwp.ObjectFactory();


    @Test
    public void shouldGenerateValidXml() {

        final Currentcourtstatus currentCourtStatus = getCurrentCourtStatus();

        final XmlUtils xmlUtils = new XmlUtils();
        final String publicDisplay = xmlUtils.createWebPage(currentCourtStatus);

        assertTrue(!publicDisplay.isEmpty());
    }

    private Currentcourtstatus getCurrentCourtStatus() {
        final Currentcourtstatus currentcourtstatus = webPageObjectFactory.createCurrentcourtstatus();
        final Court court = webPageObjectFactory.createCourt();
        final Courtsites courtsites = webPageObjectFactory.createCourtsites();
        final Courtsite courtsite = webPageObjectFactory.createCourtsite();
        final Courtrooms courtrooms = webPageObjectFactory.createCourtrooms();
        final Courtroom courtroom = getCourtroom();
        final Datetimestamp datetimestamp = webPageObjectFactory.createDatetimestamp();

        datetimestamp.setMin(1);
        datetimestamp.setHour(1);
        datetimestamp.setYear(valueOf(2019));
        datetimestamp.setMonth(APRIL);
        datetimestamp.setDayofweek(FRIDAY);
        datetimestamp.setDate(31);

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

    private Courtroom getCourtroom() {
        final Courtroom courtroom = webPageObjectFactory.createCourtroom();
        courtroom.setCurrentstatus(webPageObjectFactory.createCurrentstatus());
        return courtroom;
    }
}