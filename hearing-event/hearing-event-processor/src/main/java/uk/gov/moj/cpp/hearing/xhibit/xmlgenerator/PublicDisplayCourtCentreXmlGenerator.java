package uk.gov.moj.cpp.hearing.xhibit.xmlgenerator;

import static java.math.BigInteger.ONE;
import static java.math.BigInteger.valueOf;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

import uk.gov.justice.core.courts.HearingEvent;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.JsonEnvelope;
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
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.Floating;
import uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.MonthsOfYearType;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CaseDetail;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CourtSite;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CurrentCourtStatus;
import uk.gov.moj.cpp.hearing.utils.DateUtils;
import uk.gov.moj.cpp.hearing.xhibit.CourtCentreGeneratorParameters;
import uk.gov.moj.cpp.hearing.xhibit.XmlUtils;

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

    @ServiceComponent(EVENT_PROCESSOR)
    @Inject
    private Requester requester;

    @Inject
    private Enveloper enveloper;

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    private static final uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.ObjectFactory webPageObjectFactory = new uk.gov.moj.cpp.hearing.domain.xhibit.generated.pd.ObjectFactory();

    @SuppressWarnings("squid:S3655")
    @Override
    public String generateXml(final CourtCentreGeneratorParameters courtCentreGeneratorParameters) {

        final CurrentCourtStatus cppCurrentCourtStatus = courtCentreGeneratorParameters.getCurrentCourtStatus().get();

        final Currentcourtstatus xhibitCurrentCourtStatus = webPageObjectFactory.createCurrentcourtstatus();

        xhibitCurrentCourtStatus.setPagename(cppCurrentCourtStatus.getCourt().getCourtName().toLowerCase());
        xhibitCurrentCourtStatus.setCourt(getCourt(cppCurrentCourtStatus, courtCentreGeneratorParameters.getEnvelope()));
        xhibitCurrentCourtStatus.setDatetimestamp(getDateTimeStamp(courtCentreGeneratorParameters.getLatestCourtListUploadTime()));

        return xmlUtils.createPublicDisplay(xhibitCurrentCourtStatus);
    }

    private Datetimestamp getDateTimeStamp(final ZonedDateTime latestCourtListUploadTime) {

        final Datetimestamp datetimestamp = webPageObjectFactory.createDatetimestamp();

        datetimestamp.setDayofweek(DaysOfWeekType.valueOf(latestCourtListUploadTime.getDayOfWeek().name()));
        datetimestamp.setDate(latestCourtListUploadTime.getDayOfMonth());
        datetimestamp.setMonth(MonthsOfYearType.valueOf(latestCourtListUploadTime.getMonth().name()));
        datetimestamp.setYear(valueOf(latestCourtListUploadTime.getYear()));
        datetimestamp.setHour(latestCourtListUploadTime.getHour());
        datetimestamp.setMin(latestCourtListUploadTime.getMinute());

        return datetimestamp;
    }

    private Court getCourt(final CurrentCourtStatus cppCurrentCourtStatus, final JsonEnvelope envelope) {

        final Court court = webPageObjectFactory.createCourt();

        court.setCourtname(cppCurrentCourtStatus.getCourt().getCourtName());
        court.setCourtsites(getCourtSites(cppCurrentCourtStatus.getCourt().getCourtSites(), envelope));

        return court;
    }


    private Courtsites getCourtSites(final List<CourtSite> courtSiteList, final JsonEnvelope envelope) {

        final Courtsites courtsites = webPageObjectFactory.createCourtsites();

        courtSiteList.forEach(courtSite -> {

            final Courtsite xhibitCourtSite = webPageObjectFactory.createCourtsite();
            xhibitCourtSite.setCourtsitename(courtSite.getCourtSiteName());
            xhibitCourtSite.setCourtrooms(getCourtRooms(courtSite, envelope));
            final Floating floating = webPageObjectFactory.createFloating();
            xhibitCourtSite.setFloating(floating);

            courtsites.getCourtsite().add(xhibitCourtSite);
        });
        return courtsites;
    }

    private Courtrooms getCourtRooms(final CourtSite courtSite, final JsonEnvelope envelope) {
        final Courtrooms courtrooms = webPageObjectFactory.createCourtrooms();

        courtSite.getCourtRooms().forEach(courtRoom -> courtrooms.getCourtroom().add(getCourtRoom(courtRoom, envelope)));

        return courtrooms;
    }

    private Courtroom getCourtRoom(final uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CourtRoom ccpCourtRoom, final JsonEnvelope envelope) {
        final Courtroom xhibitCourtRoom = webPageObjectFactory.createCourtroom();

        xhibitCourtRoom.setCourtroomname(ccpCourtRoom.getCourtRoomName());
        xhibitCourtRoom.setCases(getCases(ccpCourtRoom.getCases(), envelope));
        return xhibitCourtRoom;
    }


    private Cases getCases(final uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.Cases cppCases, final JsonEnvelope envelope) {
        final Cases xhibitCases = webPageObjectFactory.createCases();

        cppCases.getCasesDetails()
                .forEach(xhibitCase -> xhibitCases.getCaseDetails().add(addCaseDetails(xhibitCase, envelope)));
        return xhibitCases;
    }

    @SuppressWarnings("squid:S1172")
    private CaseDetails addCaseDetails(final uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CaseDetail cppCaseDetail, final JsonEnvelope envelope) {
        final CaseDetails xhibitCaseDetails = new CaseDetails();
        final HearingEvent hearingEvent = cppCaseDetail.getHearingEvent();
        xhibitCaseDetails.setCppurn(cppCaseDetail.getCppUrn());
        xhibitCaseDetails.setCasenumber(ONE);
        xhibitCaseDetails.setHearingprogress(cppCaseDetail.getHearingprogress());
        xhibitCaseDetails.setCasetype(cppCaseDetail.getCaseType());
        xhibitCaseDetails.setActivecase(cppCaseDetail.getActivecase());
        xhibitCaseDetails.setHearingtype(cppCaseDetail.getHearingType());
        xhibitCaseDetails.setDefendants(getDefendants(cppCaseDetail));
        final String dateTime  = DateUtils.convertToLocalTime(cppCaseDetail.getNotBeforeTime());
        xhibitCaseDetails.setNotbeforetime(dateTime);
        xhibitCaseDetails.setJudgename(cppCaseDetail.getJudgeName());

        if (null == hearingEvent) {
            xhibitCaseDetails.setTimestatusset(dateTime);
            xhibitCaseDetails.setHearingprogress(BigInteger.ZERO);
        } else {
            xhibitCaseDetails.setCurrentstatus(eventGenerator.generate(cppCaseDetail));
            xhibitCaseDetails.setTimestatusset(DateUtils.convertZonedDateTimeToLocalTime(hearingEvent.getEventTime()));
        }

        return xhibitCaseDetails;
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


