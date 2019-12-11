package uk.gov.moj.cpp.hearing.xhibit.xmlgenerator;


import static java.lang.String.valueOf;
import static java.math.BigInteger.ONE;

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
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CaseDetail;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CourtSite;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CurrentCourtStatus;
import uk.gov.moj.cpp.hearing.xhibit.CourtCentreGeneratorParameters;
import uk.gov.moj.cpp.hearing.xhibit.XmlUtils;

import java.time.ZonedDateTime;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@SuppressWarnings({"squid:S3655"})
//Warning has been suppressed because CurrentCourtStatus Optional has been checked in the producer
@ApplicationScoped
public class PublicDisplayCourtCentreXmlGenerator implements CourtCentreXmlGenerator {

    @Inject
    private XmlUtils xmlUtils;

    private static final uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.ObjectFactory publicDisplayObjectFactory = new uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.ObjectFactory();

    @Override
    public String generateXml(final CourtCentreGeneratorParameters courtCentreGeneratorParameters) {

        final CurrentCourtStatus cppCurrentCourtStatus = courtCentreGeneratorParameters.getCurrentCourtStatus().get();

        final Currentcourtstatus xhibitCurrentCourtStatus = publicDisplayObjectFactory.createCurrentcourtstatus();

        xhibitCurrentCourtStatus.setPagename(cppCurrentCourtStatus.getCourt().getCourtName().toLowerCase());
        xhibitCurrentCourtStatus.setCourt(getCourt(cppCurrentCourtStatus));
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

    private Court getCourt(final CurrentCourtStatus cppCurrentCourtStatus) {

        final Court court = publicDisplayObjectFactory.createCourt();

        court.setCourtname(cppCurrentCourtStatus.getCourt().getCourtName());
        court.setCourtsites(getCourtSites(cppCurrentCourtStatus.getCourt().getCourtSites()));

        return court;
    }


    private Courtsites getCourtSites(final List<CourtSite> courtSiteList) {

        final Courtsites courtsites = publicDisplayObjectFactory.createCourtsites();

        courtSiteList.forEach(courtSite -> {

            final Courtsite xhibitCourtSite = publicDisplayObjectFactory.createCourtsite();
            xhibitCourtSite.setCourtsitename(courtSite.getCourtSiteName());
            xhibitCourtSite.setCourtrooms(getCourtRooms(courtSite));

            courtsites.getCourtsite().add(xhibitCourtSite);
        });
        return courtsites;
    }

    private Courtrooms getCourtRooms(final CourtSite courtSite) {
        final Courtrooms courtrooms = publicDisplayObjectFactory.createCourtrooms();

        courtSite.getCourtRooms().forEach(courtRoom -> courtrooms.getCourtroom().add(getCourtRoom(courtRoom)));

        return courtrooms;
    }

    private Courtroom getCourtRoom(final uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CourtRoom ccpCourtRoom) {
        final Courtroom xhibitCourtRoom = publicDisplayObjectFactory.createCourtroom();

        xhibitCourtRoom.setCourtroomname(ccpCourtRoom.getCourtRoomName());
        xhibitCourtRoom.setCases(getCases(ccpCourtRoom.getCases()));

        return xhibitCourtRoom;
    }


    private Cases getCases(final uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.Cases cppCases) {
        final Cases xhibitCases = publicDisplayObjectFactory.createCases();

        cppCases.getCasesDetails()
                .forEach(xhibitCase -> xhibitCases.getCaseDetails().add(addCaseDetails(xhibitCase)));
        return xhibitCases;
    }


    private CaseDetails addCaseDetails(final uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CaseDetail cppCaseDetail) {
        final CaseDetails xhibitCaseDetails = new CaseDetails();

        xhibitCaseDetails.setCppurn(cppCaseDetail.getCppUrn());
        xhibitCaseDetails.setCasenumber(ONE); //TODO: understand what is this
        xhibitCaseDetails.setCasetype(cppCaseDetail.getCaseType());
        xhibitCaseDetails.setHearingtype(cppCaseDetail.getHearingType());
        xhibitCaseDetails.setDefendants(getDefendants(cppCaseDetail));
        xhibitCaseDetails.setJudgename(cppCaseDetail.getJudgeName());

        return xhibitCaseDetails;
    }

    private Defendants getDefendants(final CaseDetail cppCaseDetail) {
        final Defendants exhibitDefendants = publicDisplayObjectFactory.createDefendants();

        cppCaseDetail.getDefendants().forEach(cppDefendant -> {

            final Defendant exhibitDefendant = publicDisplayObjectFactory.createDefendant();
            exhibitDefendant.setFirstname(cppDefendant.getFirstName());
            exhibitDefendant.setMiddlename(cppDefendant.getMiddleName());
            exhibitDefendant.setLastname(cppDefendant.getLastName());

            exhibitDefendants.getDefendant().add(exhibitDefendant);
        });
        return exhibitDefendants;
    }

}
