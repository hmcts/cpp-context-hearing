package uk.gov.moj.cpp.hearing.xhibit.xmlgenerator;


import static java.math.BigInteger.ONE;
import static java.util.Optional.ofNullable;

import uk.gov.moj.cpp.hearing.domain.xhibit.generated.iwp.CaseDetails;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.iwp.Cases;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.iwp.Court;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.iwp.Courtroom;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.iwp.Courtrooms;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.iwp.Courtsite;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.iwp.Courtsites;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.iwp.Currentcourtstatus;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.iwp.Datetimestamp;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.iwp.DaysOfWeekType;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.iwp.Defendant;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.iwp.Defendants;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.iwp.MonthsOfYearType;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CourtSite;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CurrentCourtStatus;
import uk.gov.moj.cpp.hearing.utils.DateUtils;
import uk.gov.moj.cpp.hearing.xhibit.CourtCentreGeneratorParameters;
import uk.gov.moj.cpp.hearing.xhibit.XmlUtils;

import java.math.BigInteger;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@SuppressWarnings({"squid:S3655"})
//Warning has been suppressed because CurrentCourtStatus Optional has been checked in the producer
@ApplicationScoped
public class WebPageCourtCentreXmlGenerator implements CourtCentreXmlGenerator {

    @Inject
    private XmlUtils xmlUtils;

    @Inject
    private EventGenerator eventGenerator;

    private static final uk.gov.moj.cpp.hearing.domain.xhibit.generated.iwp.ObjectFactory webPageObjectFactory = new uk.gov.moj.cpp.hearing.domain.xhibit.generated.iwp.ObjectFactory();

    @Override
    public String generateXml(final CourtCentreGeneratorParameters courtCentreGeneratorParameters) {

        final CurrentCourtStatus cppCurrentCourtStatus = courtCentreGeneratorParameters.getCurrentCourtStatus().get();

        final Currentcourtstatus xhibitCurrentCourtStatus = webPageObjectFactory.createCurrentcourtstatus();

        xhibitCurrentCourtStatus.setPagename(cppCurrentCourtStatus.getCourt().getCourtName().toLowerCase());
        xhibitCurrentCourtStatus.setCourt(getCourt(cppCurrentCourtStatus));
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

    private Court getCourt(final CurrentCourtStatus cppCurrentCourtStatus) {

        final Court court = webPageObjectFactory.createCourt();

        court.setCourtname(cppCurrentCourtStatus.getCourt().getCourtName());
        court.setCourtsites(getCourtSites(cppCurrentCourtStatus.getCourt().getCourtSites()));

        return court;
    }


    private Courtsites getCourtSites(final List<CourtSite> courtSiteList) {

        final Courtsites courtsites = webPageObjectFactory.createCourtsites();

        courtSiteList.forEach(courtSite -> {

            final Courtsite xhibitCourtSite = webPageObjectFactory.createCourtsite();
            xhibitCourtSite.setCourtsitename(courtSite.getCourtSiteName());
            xhibitCourtSite.setCourtrooms(getCourtRooms(courtSite));

            courtsites.getCourtsite().add(xhibitCourtSite);
        });
        return courtsites;
    }

    private Courtrooms getCourtRooms(final CourtSite courtSite) {
        final Courtrooms courtrooms = webPageObjectFactory.createCourtrooms();

        courtSite.getCourtRooms().forEach(courtRoom -> courtrooms.getCourtroom().add(getCourtRoom(courtRoom)));

        return courtrooms;
    }

    private Courtroom getCourtRoom(final uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CourtRoom ccpCourtRoom) {
        final Courtroom xhibitCourtRoom = webPageObjectFactory.createCourtroom();

        xhibitCourtRoom.setCourtroomname(ccpCourtRoom.getCourtRoomName());
        xhibitCourtRoom.setCases(getCases(ccpCourtRoom.getCases()));
        xhibitCourtRoom.setDefendants(getDefendants(ccpCourtRoom.getCases()));
        xhibitCourtRoom.setCurrentstatus(eventGenerator.generate(ccpCourtRoom));
        ofNullable(xhibitCourtRoom.getCurrentstatus().getEvent())
                .ifPresent(event ->
                        xhibitCourtRoom.setTimestatusset(DateUtils.convertZonedDateTimeToLocalTime(ccpCourtRoom.getHearingEvent().getLastModifiedTime())));

        return xhibitCourtRoom;
    }


    private Cases getCases(final uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.Cases cppCases) {
        final Cases xhibitCases = webPageObjectFactory.createCases();

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

        return xhibitCaseDetails;
    }

    private Defendants getDefendants(final uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.Cases cases) {
        final Defendants exhibitDefendants = webPageObjectFactory.createDefendants();

        cases.getCasesDetails()
                .forEach(cppCase -> cppCase.getDefendants()
                        .forEach(cppDefendant -> {

                            final Defendant exhibitDefendant = webPageObjectFactory.createDefendant();
                            exhibitDefendant.setFirstname(cppDefendant.getFirstName());
                            exhibitDefendant.setMiddlename(cppDefendant.getMiddleName());
                            exhibitDefendant.setLastname(cppDefendant.getLastName());

                            exhibitDefendants.getDefendant().add(exhibitDefendant);
                        }));
        return exhibitDefendants;
    }

}
