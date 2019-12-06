package uk.gov.moj.cpp.hearing;

import static java.lang.String.valueOf;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.CaseDetails;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.Cases;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.Court;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.Courtroom;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.Courtrooms;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.Courtsite;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.Courtsites;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.Currentcourtstatus;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.Datetimestamp;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.Defendant;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.Defendants;
import uk.gov.moj.cpp.hearing.xhibit.CourtCentreGeneratorParameters;
import uk.gov.moj.cpp.hearing.xhibit.CourtCentreXmlGenerator;
import uk.gov.moj.cpp.hearing.xhibit.XmlUtils;

import java.time.ZonedDateTime;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class DummyPublicDisplayCourtCentreXmlGenerator implements CourtCentreXmlGenerator {

    private static final uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.ObjectFactory publicDisplayObjectFactory = new uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.ObjectFactory();

    @Inject
    private XmlUtils xmlUtils;

    @Override
    public String generateXml(final CourtCentreGeneratorParameters courtCentreGeneratorParameters) {

        final Currentcourtstatus xhibitCurrentCourtStatus = publicDisplayObjectFactory.createCurrentcourtstatus();

        xhibitCurrentCourtStatus.setPagename(EMPTY);
        xhibitCurrentCourtStatus.setCourt(getCourt());
        xhibitCurrentCourtStatus.setDatetimestamp(getDateTimeStamp(courtCentreGeneratorParameters.getLatestCourtListUploadTime()));

        return xmlUtils.createPublicDisplay(xhibitCurrentCourtStatus);
    }

    private Datetimestamp getDateTimeStamp(final ZonedDateTime latestCourtListUploadTime) {

        final Datetimestamp datetimestamp = publicDisplayObjectFactory.createDatetimestamp();

        datetimestamp.setDayofweek(latestCourtListUploadTime.getDayOfWeek().toString());
        datetimestamp.setDate(valueOf(latestCourtListUploadTime.getDayOfMonth()));
        datetimestamp.setMonth(latestCourtListUploadTime.getMonth().name());
        datetimestamp.setYear(valueOf(latestCourtListUploadTime.getYear()));
        datetimestamp.setHour(valueOf(latestCourtListUploadTime.getHour()));
        datetimestamp.setMin(valueOf(latestCourtListUploadTime.getMinute()));

        return datetimestamp;
    }

    private Court getCourt() {
        final Court court = publicDisplayObjectFactory.createCourt();
        court.setCourtname(EMPTY);
        court.setCourtsites(getCourtSites());
        return court;
    }


    private Courtsites getCourtSites() {

        final Courtsites courtsites = publicDisplayObjectFactory.createCourtsites();

        final Courtsite xhibitCourtSite = publicDisplayObjectFactory.createCourtsite();
        xhibitCourtSite.setCourtsitename(EMPTY);
        xhibitCourtSite.setCourtrooms(getCourtRooms());

        courtsites.getCourtsite().add(xhibitCourtSite);
        return courtsites;
    }

    private Courtrooms getCourtRooms() {
        final Courtrooms courtrooms = publicDisplayObjectFactory.createCourtrooms();
        courtrooms.getCourtroom().add(getCourtRoom());
        return courtrooms;
    }

    private Courtroom getCourtRoom() {
        final Courtroom xhibitCourtRoom = publicDisplayObjectFactory.createCourtroom();
        xhibitCourtRoom.setCourtroomname(EMPTY);
        xhibitCourtRoom.setCases(getCases());
        return xhibitCourtRoom;
    }


    private Cases getCases() {
        final Cases xhibitCases = publicDisplayObjectFactory.createCases();
        xhibitCases.getCaseDetails().add(addCaseDetails());
        return xhibitCases;
    }


    private CaseDetails addCaseDetails() {
        final CaseDetails xhibitCaseDetails = new CaseDetails();

        xhibitCaseDetails.setCppurn(EMPTY);
        xhibitCaseDetails.setCasetype(EMPTY);
        xhibitCaseDetails.setHearingtype(EMPTY);
        xhibitCaseDetails.setDefendants(getDefendants());
        xhibitCaseDetails.setJudgename(EMPTY);

        return xhibitCaseDetails;
    }

    private Defendants getDefendants() {
        final Defendants exhibitDefendants = publicDisplayObjectFactory.createDefendants();

        final Defendant exhibitDefendant = publicDisplayObjectFactory.createDefendant();
        exhibitDefendant.setFirstname(EMPTY);
        exhibitDefendant.setMiddlename(EMPTY);
        exhibitDefendant.setLastname(EMPTY);

        exhibitDefendants.getDefendant().add(exhibitDefendant);

        return exhibitDefendants;
    }
}
