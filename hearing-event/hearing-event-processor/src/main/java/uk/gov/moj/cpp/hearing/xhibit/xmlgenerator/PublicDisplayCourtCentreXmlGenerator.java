package uk.gov.moj.cpp.hearing.xhibit.xmlgenerator;


import static java.math.BigInteger.ONE;

import uk.gov.justice.core.courts.HearingEvent;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.CaseDetails;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.Cases;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.Court;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.Courtroom;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.Courtrooms;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.Courtsite;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.Courtsites;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.Currentcourtstatus;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.Datetimestamp;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.DaysOfWeekType;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.Defendant;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.Defendants;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.MonthsOfYearType;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CaseDetail;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CourtSite;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CurrentCourtStatus;
import uk.gov.moj.cpp.hearing.xhibit.CourtCentreGeneratorParameters;
import uk.gov.moj.cpp.hearing.xhibit.XmlUtils;
import uk.gov.moj.cpp.hearing.xhibit.refdatacache.XhibitEventMapperCache;

import java.math.BigInteger;
import java.time.ZonedDateTime;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class PublicDisplayCourtCentreXmlGenerator implements CourtCentreXmlGenerator {

    @Inject
    private XmlUtils xmlUtils;

    @Inject
    private PublicDisplayEventGenerator eventGenerator;

    @Inject
    private XhibitEventMapperCache eventMapperCache;

    private static final uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.ObjectFactory webPageObjectFactory = new uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.ObjectFactory();

    @SuppressWarnings("squid:S3655")
    @Override
    public String generateXml(final CourtCentreGeneratorParameters courtCentreGeneratorParameters) {

        final CurrentCourtStatus cppCurrentCourtStatus = courtCentreGeneratorParameters.getCurrentCourtStatus().get();

        final Currentcourtstatus xhibitCurrentCourtStatus = webPageObjectFactory.createCurrentcourtstatus();

        xhibitCurrentCourtStatus.setPagename(cppCurrentCourtStatus.getCourt().getCourtName().toLowerCase());
        xhibitCurrentCourtStatus.setCourt(getCourt(cppCurrentCourtStatus));
        xhibitCurrentCourtStatus.setDatetimestamp(getDateTimeStamp(courtCentreGeneratorParameters.getLatestCourtListUploadTime()));

        return xmlUtils.createPublicDisplay(xhibitCurrentCourtStatus);
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
        xhibitCourtRoom.setCases(getCases(ccpCourtRoom.getCases(), ccpCourtRoom.getHearingEvent(), ccpCourtRoom));
        return xhibitCourtRoom;
    }


    private Cases getCases(final uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.Cases cppCases, final HearingEvent hearingEvent, final uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CourtRoom ccpCourtRoom) {
        final Cases xhibitCases = webPageObjectFactory.createCases();

        cppCases.getCasesDetails()
                .forEach(xhibitCase -> xhibitCases.getCaseDetails().add(addCaseDetails(xhibitCase, hearingEvent,  ccpCourtRoom)));
        return xhibitCases;
    }


    private CaseDetails addCaseDetails(final uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CaseDetail cppCaseDetail, final HearingEvent hearingEvent, final uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CourtRoom ccpCourtRoom) {
        final CaseDetails xhibitCaseDetails = new CaseDetails();

        xhibitCaseDetails.setCppurn(cppCaseDetail.getCppUrn());
        xhibitCaseDetails.setCasenumber(ONE);
        xhibitCaseDetails.setCasetype(cppCaseDetail.getCaseType());
        xhibitCaseDetails.setHearingtype(cppCaseDetail.getHearingType());
        xhibitCaseDetails.setDefendants(getDefendants(cppCaseDetail));
        if (null == hearingEvent) {
            xhibitCaseDetails.setNotbeforetime(cppCaseDetail.getNotBeforeTime());
        } else {
            eventGenerator.generate(ccpCourtRoom);
        }
        xhibitCaseDetails.setHearingprogress(determineHearingProgress(hearingEvent));
        return xhibitCaseDetails;
    }

    private BigInteger determineHearingProgress(final HearingEvent hearingEvent) {
        if(null == hearingEvent) {
            return BigInteger.ZERO;
        }
        // To ask about this ??
        return BigInteger.valueOf(5);
    }

    private Defendants getDefendants(final CaseDetail cases) {
        final Defendants exhibitDefendants = webPageObjectFactory.createDefendants();

        cases.getDefendants()
                        .forEach(cppDefendant -> {

                            final Defendant exhibitDefendant = webPageObjectFactory.createDefendant();
                            exhibitDefendant.setFirstname(cppDefendant.getFirstName());
                            exhibitDefendant.setMiddlename(cppDefendant.getMiddleName());
                            exhibitDefendant.setLastname(cppDefendant.getLastName());
                            exhibitDefendants.getDefendant().add(exhibitDefendant);
                        });
        return exhibitDefendants;
    }
}


