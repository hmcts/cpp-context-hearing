package uk.gov.moj.cpp.hearing.xhibit.xmlgenerator;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import uk.gov.moj.cpp.hearing.domain.xhibit.generated.iwp.CaseDetails;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.iwp.Cases;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.iwp.Court;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.iwp.Courtroom;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.iwp.Courtrooms;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.iwp.Courtsite;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.iwp.Courtsites;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.iwp.Currentcourtstatus;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.iwp.Currentstatus;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.iwp.Datetimestamp;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.iwp.DaysOfWeekType;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.iwp.Defendant;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.iwp.Defendants;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.iwp.MonthsOfYearType;
import uk.gov.moj.cpp.hearing.xhibit.CourtCentreGeneratorParameters;
import uk.gov.moj.cpp.hearing.xhibit.XmlUtils;

import java.math.BigInteger;
import java.time.ZonedDateTime;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class EmptyWebPageCourtCentreXmlGenerator implements CourtCentreXmlGenerator {

    private static final uk.gov.moj.cpp.hearing.domain.xhibit.generated.iwp.ObjectFactory webPageObjectFactory = new uk.gov.moj.cpp.hearing.domain.xhibit.generated.iwp.ObjectFactory();

    @Inject
    private XmlUtils xmlUtils;

    @Override
    public String generateXml(final CourtCentreGeneratorParameters courtCentreGeneratorParameters) {

        final Currentcourtstatus xhibitCurrentCourtStatus = webPageObjectFactory.createCurrentcourtstatus();

        xhibitCurrentCourtStatus.setPagename(EMPTY);
        xhibitCurrentCourtStatus.setCourt(getCourt());
        xhibitCurrentCourtStatus.setDatetimestamp(getDateTimeStamp(courtCentreGeneratorParameters.getLatestCourtListUploadTime()));

        return xmlUtils.createWebPage(xhibitCurrentCourtStatus);
    }

    private Datetimestamp getDateTimeStamp(final ZonedDateTime latestCourtListUploadTime) {

        final Datetimestamp datetimestamp = webPageObjectFactory.createDatetimestamp();

        datetimestamp.setDayofweek(DaysOfWeekType.valueOf(latestCourtListUploadTime.getDayOfWeek().name()));
        datetimestamp.setDate(latestCourtListUploadTime.getDayOfMonth());
        datetimestamp.setMonth(MonthsOfYearType.valueOf(latestCourtListUploadTime.getMonth().name()));
        datetimestamp.setYear(BigInteger.valueOf(latestCourtListUploadTime.getYear()));
        datetimestamp.setHour(latestCourtListUploadTime.getHour());
        datetimestamp.setMin(latestCourtListUploadTime.getMinute());

        return datetimestamp;
    }

    private Court getCourt() {
        final Court court = webPageObjectFactory.createCourt();
        court.setCourtname(EMPTY);
        court.setCourtsites(getCourtSites());
        return court;
    }


    private Courtsites getCourtSites() {

        final Courtsites courtsites = webPageObjectFactory.createCourtsites();

        final Courtsite xhibitCourtSite = webPageObjectFactory.createCourtsite();
        xhibitCourtSite.setCourtsitename(EMPTY);
        xhibitCourtSite.setCourtrooms(getCourtRooms());

        courtsites.getCourtsite().add(xhibitCourtSite);
        return courtsites;
    }

    private Courtrooms getCourtRooms() {
        final Courtrooms courtrooms = webPageObjectFactory.createCourtrooms();
        courtrooms.getCourtroom().add(getCourtRoom());
        return courtrooms;
    }

    private Courtroom getCourtRoom() {
        final Courtroom xhibitCourtRoom = webPageObjectFactory.createCourtroom();
        xhibitCourtRoom.setCourtroomname(EMPTY);
        xhibitCourtRoom.setCases(getCases());
        xhibitCourtRoom.setDefendants(getDefendants());
        xhibitCourtRoom.setCurrentstatus(getCurrentStatus());
        return xhibitCourtRoom;
    }

    private Currentstatus getCurrentStatus() {
        final Currentstatus currentstatus = webPageObjectFactory.createCurrentstatus();
        return currentstatus;
    }


    private Cases getCases() {
        final Cases xhibitCases = webPageObjectFactory.createCases();
        xhibitCases.getCaseDetails().add(addCaseDetails());
        return xhibitCases;
    }


    private CaseDetails addCaseDetails() {
        final CaseDetails xhibitCaseDetails = new CaseDetails();

        xhibitCaseDetails.setCppurn(EMPTY);
        xhibitCaseDetails.setCasetype(EMPTY);
        xhibitCaseDetails.setHearingtype(EMPTY);

        return xhibitCaseDetails;
    }

    private Defendants getDefendants() {
        final Defendants exhibitDefendants = webPageObjectFactory.createDefendants();

        final Defendant exhibitDefendant = webPageObjectFactory.createDefendant();
        exhibitDefendant.setFirstname(EMPTY);
        exhibitDefendant.setMiddlename(EMPTY);
        exhibitDefendant.setLastname(EMPTY);

        exhibitDefendants.getDefendant().add(exhibitDefendant);

        return exhibitDefendants;
    }
}
